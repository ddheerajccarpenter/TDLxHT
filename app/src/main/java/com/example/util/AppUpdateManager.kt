package com.example.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.BuildConfig
import com.example.HabitTaskApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
  val version: String,
  val versionCode: Int,
  val filename: String,
  val apkUrl: String,
  val fallbackUrl: String = "/TDlxHT.apk",
  val releaseDate: String = ""
) {
  val fullDownloadUrl: String
    get() {
      val path = if (apkUrl.isNotBlank()) apkUrl else fallbackUrl
      return if (path.startsWith("http://") || path.startsWith("https://")) {
        path
      } else {
        "https://tdlxht.vercel.app${if (path.startsWith("/")) "" else "/"}$path"
      }
    }
}

object AppUpdateManager {
  private const val TAG = "AppUpdateManager"
  private const val UPDATE_ENDPOINT = "https://tdlxht.vercel.app/version.json"
  const val NOTIFICATION_ID_UPDATE = 77777

  private val httpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(6, TimeUnit.SECONDS)
      .readTimeout(6, TimeUnit.SECONDS)
      .build()
  }

  suspend fun checkRemoteUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
    try {
      val request = Request.Builder()
        .url(UPDATE_ENDPOINT)
        .header("Cache-Control", "no-cache")
        .build()

      val body = httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
          Log.w(TAG, "Update check failed with code ${response.code}")
          return@withContext null
        }
        response.body?.string() ?: return@withContext null
      }
      val json = JSONObject(body)
      val remoteVersion = json.optString("version", "")
      val remoteVersionCode = json.optInt("versionCode", 0)
      val filename = json.optString("filename", "TDLxHT.apk")
      val apkUrl = json.optString("apkUrl", "/TDlxHT.apk")
      val fallbackUrl = json.optString("fallbackUrl", "/TDlxHT.apk")
      val releaseDate = json.optString("releaseDate", "")

      val currentVersionCode = BuildConfig.VERSION_CODE
      Log.d(TAG, "Current versionCode: $currentVersionCode, remote versionCode: $remoteVersionCode")

      if (remoteVersionCode > currentVersionCode) {
        return@withContext AppUpdateInfo(
          version = remoteVersion,
          versionCode = remoteVersionCode,
          filename = filename,
          apkUrl = apkUrl,
          fallbackUrl = fallbackUrl,
          releaseDate = releaseDate
        )
      } else {
        return@withContext null
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error checking remote update: ${e.message}")
      return@withContext null
    }
  }

  fun postUpdateNotification(context: Context, update: AppUpdateInfo) {
    try {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

      val downloadIntent = Intent(Intent.ACTION_VIEW, Uri.parse(update.fullDownloadUrl)).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }

      val pendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ID_UPDATE,
        downloadIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )

      val notification = NotificationCompat.Builder(context, HabitTaskApplication.CHANNEL_UPDATES_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setContentTitle("TDlxHT v${update.version} Available")
        .setContentText("A new release is ready with latest features. Tap to download.")
        .setStyle(
          NotificationCompat.BigTextStyle().bigText(
            "TDlxHT version ${update.version} is now available!\nTap to download and install the latest APK update directly."
          )
        )
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .addAction(android.R.drawable.stat_sys_download, "Download Now", pendingIntent)
        .build()

      notificationManager.notify(NOTIFICATION_ID_UPDATE, notification)
      Log.d(TAG, "Update notification posted for v${update.version}")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to post update notification", e)
    }
  }

  fun openDownloadUrl(context: Context, update: AppUpdateInfo) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.fullDownloadUrl)).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to open download URL: ${update.fullDownloadUrl}", e)
    }
  }

  fun checkForUpdates(
    context: Context,
    notifySystem: Boolean = true,
    forceShowDismissed: Boolean = false,
    onResult: (AppUpdateInfo?, isNewUpdate: Boolean) -> Unit = { _, _ -> }
  ) {
    CoroutineScope(Dispatchers.IO).launch {
      val update = checkRemoteUpdate()
      if (update != null) {
        val dismissedCode = AppPreferences.getDismissedUpdateVersionCode(context)
        val isDismissed = !forceShowDismissed && update.versionCode <= dismissedCode
        if (!isDismissed && notifySystem) {
          postUpdateNotification(context, update)
        }
        withContext(Dispatchers.Main) {
          onResult(update, !isDismissed)
        }
      } else {
        withContext(Dispatchers.Main) {
          onResult(null, false)
        }
      }
    }
  }
}
