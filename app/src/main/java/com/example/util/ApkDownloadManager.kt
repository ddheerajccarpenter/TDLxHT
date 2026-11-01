package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
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

  private val _downloadState = MutableStateFlow<ApkDownloadState>(ApkDownloadState.Idle)
  val downloadState: StateFlow<ApkDownloadState> = _downloadState.asStateFlow()

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

  fun startDownload(context: Context, update: AppUpdateInfo) {
    if (_downloadState.value is ApkDownloadState.Downloading) {
      Log.d(TAG, "Download already in progress")
      return
    }

    activeJob?.cancel()
    activeJob = CoroutineScope(Dispatchers.IO).launch {
      try {
        _downloadState.value = ApkDownloadState.Downloading(
          bytesDownloaded = 0L,
          totalBytes = -1L,
          percentage = 0,
          formattedSize = "0 MB"
        )

        val targetDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val tempFile = File(targetDir, "${update.filename}.part")
        val finalFile = File(targetDir, update.filename)

        if (tempFile.exists()) tempFile.delete()
        if (finalFile.exists()) finalFile.delete()

        // Build prioritized mirror candidate list:
        // 1. Dedicated ultra-fast Cloudflare Edge CDN mirror (Supabase storage)
        // 2. Supplied apkUrl from remote version manifest
        // 3. Fallback direct release URL
        val candidateUrls = mutableListOf<String>()
        val cdnMirror = "https://mtlckvkyimoqbrlvirch.supabase.co/storage/v1/object/public/apk/${update.filename}"
        val cdnGeneric = "https://mtlckvkyimoqbrlvirch.supabase.co/storage/v1/object/public/apk/TDLxHT.apk"

        if (update.fullDownloadUrl.isNotBlank()) {
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
          val fullFallback = if (update.fallbackUrl.startsWith("http")) {
            update.fallbackUrl
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
                  }
                }
                outputStream.flush()
              } finally {
                outputStream.close()
                inputStream.close()
              }

              // Verify file has non-trivial size
              if (tempFile.exists() && tempFile.length() > 2_000_000) {
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
        if (!finalFile.exists() || finalFile.length() < 2_000_000) {
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
        val pm = context.packageManager
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

        if (pkgInfo.packageName != context.packageName) {
          finalFile.delete()
          throw SecurityException("Security Violation: Package name mismatch. Expected ${context.packageName}, got ${pkgInfo.packageName}")
        }

        // 3. Verify APK signing certificate matches installed application
        try {
          val currentPkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()))
          } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
          }

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

        // Automatically trigger install intent on Main thread
        withContext(Dispatchers.Main) {
          installApk(context, finalFile)
        }
      } catch (ce: CancellationException) {
        Log.i(TAG, "Download cancelled by user")
        _downloadState.value = ApkDownloadState.Cancelled
      } catch (e: Exception) {
        Log.e(TAG, "Download failed: ${e.message}", e)
        _downloadState.value = ApkDownloadState.Failed(e.message ?: "Download failed")
      }
    }
  }

  fun cancelDownload() {
    activeJob?.cancel()
    _downloadState.value = ApkDownloadState.Cancelled
  }

  fun reset() {
    activeJob?.cancel()
    _downloadState.value = ApkDownloadState.Idle
  }

  fun installApk(context: Context, apkFile: File) {
    if (!apkFile.exists()) {
      Log.e(TAG, "Cannot install: APK file does not exist at ${apkFile.absolutePath}")
      _downloadState.value = ApkDownloadState.Failed("APK file not found on disk")
      return
    }

    try {
      val apkUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile
      )

      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      context.startActivity(intent)
      Log.i(TAG, "Package installer triggered successfully for ${apkFile.name}")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to launch package installer", e)
      _downloadState.value = ApkDownloadState.Failed("Could not open package installer: ${e.message}")
    }
  }

  private fun computeSha256(file: File): String {
    return try {
      val md = java.security.MessageDigest.getInstance("SHA-256")
      file.forEachBlock { buffer, bytesRead ->
        md.update(buffer, 0, bytesRead)
      }
      md.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
      ""
    }
  }
}
