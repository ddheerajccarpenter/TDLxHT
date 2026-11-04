package com.example.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.HabitTaskApplication
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed interface ApkDownloadState {
  object Idle : ApkDownloadState
  data class Downloading(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percentage: Int, // 0..100, or -1 if unknown total length
    val formattedSize: String
  ) : ApkDownloadState
  object Verifying : ApkDownloadState
  data class ReadyToInstall(val apkFile: File) : ApkDownloadState
  data class Failed(val error: String) : ApkDownloadState
  object Cancelled : ApkDownloadState
}

object ApkDownloadManager {
  private const val TAG = "ApkDownloadManager"
  private const val BUFFER_SIZE = 64 * 1024 // 64 KB buffer for high-throughput socket transfers
  private const val NOTIFICATION_ID_DOWNLOAD_PROGRESS = 77778
  private const val MIN_VALID_APK_SIZE = 2_000_000L // 2 MB minimum for a valid TDlxHT release APK

  private val _downloadState = MutableStateFlow<ApkDownloadState>(ApkDownloadState.Idle)
  val downloadState: StateFlow<ApkDownloadState> = _downloadState.asStateFlow()

  private val _currentUpdate = MutableStateFlow<AppUpdateInfo?>(null)
  val currentUpdate: StateFlow<AppUpdateInfo?> = _currentUpdate.asStateFlow()

  private var activeJob: Job? = null

  // Tuned OkHttpClient for maximum download throughput & resilient connection pooling
  private val httpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(12, TimeUnit.SECONDS)
      .readTimeout(45, TimeUnit.SECONDS)
      .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
      .followRedirects(true)
      .followSslRedirects(true)
      .retryOnConnectionFailure(true)
      .build()
  }

  fun sanitizeApkFilename(filename: String): String {
    val clean = File(filename).name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    return if (clean.endsWith(".apk", ignoreCase = true) && clean.length > 4) {
      clean
    } else {
      "TDlxHT_update.apk"
    }
  }

  fun canRequestPackageInstalls(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        context.packageManager.canRequestPackageInstalls()
      } catch (e: Exception) {
        Log.w(TAG, "canRequestPackageInstalls check failed", e)
        false
      }
    } else {
      true
    }
  }

  fun openUnknownAppSourcesSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
          data = Uri.parse("package:${context.packageName}")
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to open unknown app sources settings directly", e)
        try {
          val appDetailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(appDetailsIntent)
        } catch (e2: Exception) {
          Log.e(TAG, "Failed to open application details settings", e2)
        }
      }
    }
  }

  fun cleanupDownloadedApks(context: Context) {
    try {
      val searchDirs = listOfNotNull(
        File(context.cacheDir, "updates"),
        File(context.filesDir, "updates"),
        context.getExternalFilesDir("updates"),
        context.externalCacheDir?.let { File(it, "updates") }
      )

      var deletedCount = 0
      var freedBytes = 0L

      for (dir in searchDirs) {
        if (!dir.exists() || !dir.isDirectory) continue
        val files = dir.listFiles() ?: continue
        for (file in files) {
          val name = file.name.lowercase(Locale.US)
          // Strictly delete downloaded apk and partial download artifacts from updates dir
          if (name.endsWith(".apk") || name.endsWith(".part") || name.endsWith(".tmp")) {
            val size = file.length()
            if (file.delete()) {
              deletedCount++
              freedBytes += size
              Log.i(TAG, "Cleaned up update artifact: ${file.name} (${size / 1024} KB)")
            }
          }
        }
      }
      Log.i(TAG, "Storage cleanup complete. Removed $deletedCount files, freed ${freedBytes / 1048576.0} MB")
    } catch (e: Exception) {
      Log.e(TAG, "Error cleaning up update APKs", e)
    }
  }

  fun startDownload(context: Context, update: AppUpdateInfo) {
    if (_downloadState.value is ApkDownloadState.Downloading) {
      Log.d(TAG, "Download already in progress")
      return
    }

    _currentUpdate.value = update
    val appContext = context.applicationContext

    activeJob?.cancel()
    activeJob = CoroutineScope(Dispatchers.IO).launch {
      try {
        _downloadState.value = ApkDownloadState.Downloading(
          bytesDownloaded = 0L,
          totalBytes = -1L,
          percentage = 0,
          formattedSize = "0 MB"
        )
        updateDownloadNotification(appContext, progress = 0, totalBytes = -1L, bytesDownloaded = 0L)

        val targetDir = File(appContext.cacheDir, "updates").apply { mkdirs() }
        val sanitizedName = sanitizeApkFilename(update.filename)
        val tempFile = File(targetDir, "$sanitizedName.part")
        val finalFile = File(targetDir, sanitizedName)

        if (tempFile.exists()) tempFile.delete()
        if (finalFile.exists()) finalFile.delete()

        // Build prioritized mirror candidate list (strictly enforcing HTTPS)
        val candidateUrls = mutableListOf<String>()
        val cdnMirror = "https://mtlckvkyimoqbrlvirch.supabase.co/storage/v1/object/public/apk/$sanitizedName"
        val cdnGeneric = "https://mtlckvkyimoqbrlvirch.supabase.co/storage/v1/object/public/apk/TDLxHT.apk"

        if (update.fullDownloadUrl.isNotBlank() && update.fullDownloadUrl.startsWith("https://", ignoreCase = true)) {
          if (update.fullDownloadUrl.contains("supabase.co")) {
            candidateUrls.add(update.fullDownloadUrl)
          } else {
            // Prioritize edge CDN mirror first to avoid throttled GitHub blob storage
            candidateUrls.add(cdnMirror)
            candidateUrls.add(cdnGeneric)
            candidateUrls.add(update.fullDownloadUrl)
          }
        } else {
          candidateUrls.add(cdnMirror)
          candidateUrls.add(cdnGeneric)
        }

        if (update.fallbackUrl.isNotBlank()) {
          val fullFallback = if (update.fallbackUrl.startsWith("https://", ignoreCase = true)) {
            update.fallbackUrl
          } else if (update.fallbackUrl.startsWith("http://", ignoreCase = true)) {
            update.fallbackUrl.replace("http://", "https://")
          } else {
            "https://tdlxht.vercel.app${if (update.fallbackUrl.startsWith("/")) "" else "/"}${update.fallbackUrl}"
          }
          if (!candidateUrls.contains(fullFallback)) {
            candidateUrls.add(fullFallback)
          }
        }

        var downloadSuccess = false
        var lastError: Exception? = null

        for (candidateUrl in candidateUrls) {
          Log.i(TAG, "Attempting high-speed APK download from: $candidateUrl")
          try {
            if (tempFile.exists()) tempFile.delete()

            val request = Request.Builder()
              .url(candidateUrl)
              .header("User-Agent", "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) TDlxHT-Updater")
              .header("Accept", "application/vnd.android.package-archive, application/octet-stream, */*")
              .header("Accept-Encoding", "identity") // Disable server gzip compression on binary APKs
              .header("Connection", "keep-alive")
              .build()

            httpClient.newCall(request).execute().use { response ->
              if (!response.isSuccessful) {
                throw Exception("Mirror responded with HTTP ${response.code}")
              }

              val body = response.body ?: throw Exception("Empty response body from mirror")
              val totalBytes = body.contentLength()
              val inputStream = BufferedInputStream(body.byteStream(), BUFFER_SIZE)
              val outputStream = BufferedOutputStream(FileOutputStream(tempFile), BUFFER_SIZE)

              val buffer = ByteArray(BUFFER_SIZE)
              var bytesRead: Int
              var totalRead = 0L
              var lastEmittedTime = 0L
              var lastNotifTime = 0L
              var speedCalcTime = System.currentTimeMillis()
              var bytesSinceLastSpeedCalc = 0L
              var speedMBs = 0.0

              try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                  outputStream.write(buffer, 0, bytesRead)
                  totalRead += bytesRead
                  bytesSinceLastSpeedCalc += bytesRead

                  val now = System.currentTimeMillis()
                  val deltaSpeedMs = now - speedCalcTime
                  if (deltaSpeedMs >= 400) {
                    val deltaSec = deltaSpeedMs / 1000.0
                    speedMBs = (bytesSinceLastSpeedCalc / 1048576.0) / deltaSec
                    speedCalcTime = now
                    bytesSinceLastSpeedCalc = 0L
                  }

                  // Throttle UI state emissions to 150ms to prevent recomposition thrashing on the Main thread
                  if (now - lastEmittedTime > 150 || (totalBytes > 0 && totalRead == totalBytes)) {
                    lastEmittedTime = now
                    val percent = if (totalBytes > 0) ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 100) else -1
                    val speedStr = if (speedMBs >= 0.05) String.format(Locale.US, " • %.1f MB/s", speedMBs) else ""
                    val formatted = if (totalBytes > 0) {
                      String.format(Locale.US, "%.1f MB / %.1f MB%s", totalRead / 1048576.0, totalBytes / 1048576.0, speedStr)
                    } else {
                      String.format(Locale.US, "%.1f MB%s", totalRead / 1048576.0, speedStr)
                    }
                    _downloadState.value = ApkDownloadState.Downloading(
                      bytesDownloaded = totalRead,
                      totalBytes = totalBytes,
                      percentage = percent,
                      formattedSize = formatted
                    )

                    // Throttle notification updates to 500ms
                    if (now - lastNotifTime > 500 || (totalBytes > 0 && totalRead == totalBytes)) {
                      lastNotifTime = now
                      updateDownloadNotification(appContext, percent, totalBytes, totalRead)
                    }
                  }
                }
                outputStream.flush()
              } finally {
                outputStream.close()
                inputStream.close()
              }

              // Verify file has non-trivial size
              if (tempFile.exists() && tempFile.length() > MIN_VALID_APK_SIZE) {
                downloadSuccess = true
                Log.i(TAG, "Successfully downloaded ${tempFile.length()} bytes from $candidateUrl")
                return@use
              } else {
                throw Exception("Downloaded file too small (${tempFile.length()} bytes)")
              }
            }

            if (downloadSuccess) break
          } catch (ce: CancellationException) {
            throw ce
          } catch (e: Exception) {
            Log.w(TAG, "Mirror failed ($candidateUrl): ${e.message}, trying next mirror...")
            lastError = e
          }
        }

        if (!downloadSuccess) {
          throw lastError ?: Exception("All download mirrors failed")
        }

        // Rename part file to final file
        if (!tempFile.renameTo(finalFile)) {
          tempFile.copyTo(finalFile, overwrite = true)
          tempFile.delete()
        }

        _downloadState.value = ApkDownloadState.Verifying

        // Validate downloaded file
        if (!finalFile.exists() || finalFile.length() < MIN_VALID_APK_SIZE) {
          throw Exception("Downloaded file is incomplete (${finalFile.length()} bytes)")
        }

        // 1. Validate SHA-256 digest if present in manifest
        if (update.sha256.isNotBlank()) {
          val actualSha = computeSha256(finalFile)
          if (!actualSha.equals(update.sha256, ignoreCase = true)) {
            finalFile.delete()
            throw SecurityException("Security Violation: APK SHA-256 integrity mismatch. Expected ${update.sha256}, got $actualSha")
          }
          Log.i(TAG, "APK SHA-256 integrity verified successfully: $actualSha")
        }

        // 2. Strict fail-closed package archive validation
        val pm = appContext.packageManager
        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          pm.getPackageArchiveInfo(finalFile.absolutePath, PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()))
        } else {
          @Suppress("DEPRECATION")
          pm.getPackageArchiveInfo(finalFile.absolutePath, PackageManager.GET_SIGNATURES)
        }

        if (pkgInfo == null) {
          finalFile.delete()
          throw SecurityException("Security Violation: Downloaded file is corrupt or not a valid Android package archive.")
        }

        if (pkgInfo.packageName != appContext.packageName) {
          finalFile.delete()
          throw SecurityException("Security Violation: Package name mismatch. Expected ${appContext.packageName}, got ${pkgInfo.packageName}")
        }

        // 3. Anti-downgrade validation
        val currentPkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          pm.getPackageInfo(appContext.packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()))
        } else {
          @Suppress("DEPRECATION")
          pm.getPackageInfo(appContext.packageName, PackageManager.GET_SIGNATURES)
        }

        val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          currentPkgInfo.longVersionCode
        } else {
          @Suppress("DEPRECATION")
          currentPkgInfo.versionCode.toLong()
        }

        val downloadedVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          pkgInfo.longVersionCode
        } else {
          @Suppress("DEPRECATION")
          pkgInfo.versionCode.toLong()
        }

        if (downloadedVersionCode < currentVersionCode) {
          finalFile.delete()
          throw SecurityException("Security Violation: APK downgrade detected. Target versionCode $downloadedVersionCode is lower than current $currentVersionCode.")
        }

        // 4. Verify APK signing certificate matches installed application
        try {
          val currentSignatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            currentPkgInfo.signingInfo?.apkContentsSigners
          } else {
            @Suppress("DEPRECATION")
            currentPkgInfo.signatures
          }

          val downloadedSignatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.signingInfo?.apkContentsSigners
          } else {
            @Suppress("DEPRECATION")
            pkgInfo.signatures
          }

          if (currentSignatures != null && downloadedSignatures != null) {
            val currentCertBytes = currentSignatures.firstOrNull()?.toByteArray()
            val downloadedCertBytes = downloadedSignatures.firstOrNull()?.toByteArray()
            if (currentCertBytes != null && downloadedCertBytes != null) {
              if (!currentCertBytes.contentEquals(downloadedCertBytes)) {
                finalFile.delete()
                throw SecurityException("Security Violation: APK signing certificate mismatch. Refusing installation.")
              }
            }
          }
        } catch (se: SecurityException) {
          throw se
        } catch (e: Exception) {
          Log.w(TAG, "Signing certificate pre-check skipped: ${e.message}")
        }

        Log.d(TAG, "Download and verification succeeded: ${finalFile.absolutePath} (${finalFile.length()} bytes)")
        _downloadState.value = ApkDownloadState.ReadyToInstall(finalFile)

        // Update notification to Ready-to-Install state with direct pending intent
        updateDownloadNotification(appContext, progress = 100, totalBytes = finalFile.length(), bytesDownloaded = finalFile.length(), isComplete = true, apkFile = finalFile)

        // Automatically trigger install intent on Main thread if permission is granted
        withContext(Dispatchers.Main) {
          installApk(context, finalFile)
        }
      } catch (ce: CancellationException) {
        Log.i(TAG, "Download cancelled by user")
        _downloadState.value = ApkDownloadState.Cancelled
        cancelDownloadNotification(appContext)
      } catch (e: Exception) {
        Log.e(TAG, "Download failed: ${e.message}", e)
        _downloadState.value = ApkDownloadState.Failed(e.message ?: "Download failed")
        updateDownloadNotification(appContext, 0, 0, 0, error = e.message ?: "Download failed")
      }
    }
  }

  fun cancelDownload(context: Context? = null) {
    activeJob?.cancel()
    _downloadState.value = ApkDownloadState.Cancelled
    context?.let { cancelDownloadNotification(it) }
  }

  fun reset(context: Context? = null) {
    activeJob?.cancel()
    _downloadState.value = ApkDownloadState.Idle
    _currentUpdate.value = null
    context?.let { cancelDownloadNotification(it) }
  }

  fun createInstallIntent(context: Context, apkFile: File): Intent {
    val apkUri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      apkFile
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(apkUri, "application/vnd.android.package-archive")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    val resInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.packageManager.queryIntentActivities(
        intent,
        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
      )
    } else {
      @Suppress("DEPRECATION")
      context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    }

    for (resolveInfo in resInfoList) {
      val packageName = resolveInfo.activityInfo.packageName
      context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    return intent
  }

  fun installApk(context: Context, apkFile: File) {
    if (!apkFile.exists()) {
      Log.e(TAG, "Cannot install: APK file does not exist at ${apkFile.absolutePath}")
      _downloadState.value = ApkDownloadState.Failed("APK file not found on disk")
      return
    }

    // Permission check for Android 8.0+ (API 26+)
    if (!canRequestPackageInstalls(context)) {
      Log.w(TAG, "REQUEST_INSTALL_PACKAGES permission not granted. Redirecting to settings...")
      Handler(Looper.getMainLooper()).post {
        Toast.makeText(
          context,
          "Please allow 'Install unknown apps' for TDlxHT to complete the update",
          Toast.LENGTH_LONG
        ).show()
      }
      openUnknownAppSourcesSettings(context)
      return
    }

    try {
      val intent = createInstallIntent(context, apkFile)
      context.startActivity(intent)
      Log.i(TAG, "Package installer triggered successfully for ${apkFile.name}")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to launch package installer", e)
      _downloadState.value = ApkDownloadState.Failed("Could not open package installer: ${e.message}")
    }
  }

  private fun updateDownloadNotification(
    context: Context,
    progress: Int,
    totalBytes: Long,
    bytesDownloaded: Long,
    isComplete: Boolean = false,
    apkFile: File? = null,
    error: String? = null
  ) {
    try {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

      if (error != null) {
        val errorNotif = NotificationCompat.Builder(context, HabitTaskApplication.CHANNEL_DOWNLOADS_ID)
          .setSmallIcon(android.R.drawable.stat_notify_error)
          .setContentTitle("TDlxHT Update Failed")
          .setContentText(error)
          .setAutoCancel(true)
          .setOngoing(false)
          .build()
        notificationManager.notify(NOTIFICATION_ID_DOWNLOAD_PROGRESS, errorNotif)
        return
      }

      if (isComplete && apkFile != null) {
        val installIntent = createInstallIntent(context, apkFile)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val installPendingIntent = PendingIntent.getActivity(context, 77779, installIntent, flags)

        val completeNotif = NotificationCompat.Builder(context, HabitTaskApplication.CHANNEL_DOWNLOADS_ID)
          .setSmallIcon(android.R.drawable.stat_sys_download_done)
          .setContentTitle("TDlxHT Update Downloaded")
          .setContentText("Tap to install the update.")
          .setAutoCancel(true)
          .setOngoing(false)
          .setContentIntent(installPendingIntent)
          .addAction(android.R.drawable.stat_sys_download_done, "Install Now", installPendingIntent)
          .build()
        notificationManager.notify(NOTIFICATION_ID_DOWNLOAD_PROGRESS, completeNotif)
        return
      }

      // Ongoing progress
      val notifBuilder = NotificationCompat.Builder(context, HabitTaskApplication.CHANNEL_DOWNLOADS_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle("Downloading TDlxHT Update")
        .setOngoing(true)
        .setOnlyAlertOnce(true)

      if (totalBytes > 0) {
        val currentMb = bytesDownloaded / 1048576.0
        val totalMb = totalBytes / 1048576.0
        notifBuilder.setContentText(String.format(Locale.US, "%.1f / %.1f MB (%d%%)", currentMb, totalMb, progress))
        notifBuilder.setProgress(100, progress, false)
      } else {
        notifBuilder.setContentText(String.format(Locale.US, "%.1f MB downloaded", bytesDownloaded / 1048576.0))
        notifBuilder.setProgress(0, 0, true)
      }

      notificationManager.notify(NOTIFICATION_ID_DOWNLOAD_PROGRESS, notifBuilder.build())
    } catch (e: Exception) {
      Log.w(TAG, "Failed to update download notification: ${e.message}")
    }
  }

  fun cancelDownloadNotification(context: Context) {
    try {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.cancel(NOTIFICATION_ID_DOWNLOAD_PROGRESS)
    } catch (_: Exception) {}
  }

  fun computeSha256(file: File): String {
    return try {
      val md = MessageDigest.getInstance("SHA-256")
      file.forEachBlock { buffer, bytesRead ->
        md.update(buffer, 0, bytesRead)
      }
      md.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
      ""
    }
  }
}
