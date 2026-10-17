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
import okhttp3.OkHttpClient
import okhttp3.Request
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

  private val _downloadState = MutableStateFlow<ApkDownloadState>(ApkDownloadState.Idle)
  val downloadState: StateFlow<ApkDownloadState> = _downloadState.asStateFlow()

  private var activeJob: Job? = null

  private val httpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .followRedirects(true)
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

        val request = Request.Builder()
          .url(update.fullDownloadUrl)
          .header("User-Agent", "TDlxHT-Android-Updater")
          .build()

        httpClient.newCall(request).execute().use { response ->
          if (!response.isSuccessful) {
            throw Exception("Server responded with HTTP ${response.code}")
          }

          val body = response.body ?: throw Exception("Empty response body from update server")
          val totalBytes = body.contentLength()
          val inputStream = body.byteStream()
          val outputStream = FileOutputStream(tempFile)

          val buffer = ByteArray(8192)
          var bytesRead: Int
          var totalRead = 0L
          var lastEmittedTime = 0L

          try {
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
              outputStream.write(buffer, 0, bytesRead)
              totalRead += bytesRead

              val now = System.currentTimeMillis()
              // Throttle state updates to avoid excessive Compose recompositions
              if (now - lastEmittedTime > 80 || totalRead == totalBytes) {
                lastEmittedTime = now
                val percent = if (totalBytes > 0) ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 100) else -1
                val formatted = if (totalBytes > 0) {
                  String.format(Locale.US, "%.1f MB / %.1f MB", totalRead / 1048576.0, totalBytes / 1048576.0)
                } else {
                  String.format(Locale.US, "%.1f MB", totalRead / 1048576.0)
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
        }

        // Rename part file to final file
        if (!tempFile.renameTo(finalFile)) {
          tempFile.copyTo(finalFile, overwrite = true)
          tempFile.delete()
        }

        _downloadState.value = ApkDownloadState.Verifying

        // Validate downloaded file
        if (!finalFile.exists() || finalFile.length() < 1_000_000) {
          throw Exception("Downloaded file is incomplete or corrupted (${finalFile.length()} bytes)")
        }

        // Verify package archive info
        val pm = context.packageManager
        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          pm.getPackageArchiveInfo(finalFile.absolutePath, PackageManager.PackageInfoFlags.of(0))
        } else {
          @Suppress("DEPRECATION")
          pm.getPackageArchiveInfo(finalFile.absolutePath, 0)
        }

        if (pkgInfo != null && pkgInfo.packageName != context.packageName) {
          finalFile.delete()
          throw Exception("Package mismatch: expected ${context.packageName}, got ${pkgInfo.packageName}")
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

  fun installApk(context: Context, file: File) {
    try {
      if (!file.exists()) {
        Log.e(TAG, "Cannot install: APK file does not exist at ${file.absolutePath}")
        return
      }

      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
      }
      context.startActivity(intent)
      Log.d(TAG, "Triggered package installer for ${file.name}")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to launch package installer", e)
      _downloadState.value = ApkDownloadState.Failed("Could not open package installer: ${e.message}")
    }
  }
}
