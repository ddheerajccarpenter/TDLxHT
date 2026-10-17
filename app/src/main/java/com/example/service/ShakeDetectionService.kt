package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.util.AppPreferences
import com.example.util.SafeLog
import com.example.util.ShakeDetector
import com.example.util.ShakeEventBus
import com.example.util.VibrationHelper

class ShakeDetectionService : Service() {

    private var shakeDetector: ShakeDetector? = null

    companion object {
        private const val TAG = "ShakeDetectionService"
        const val CHANNEL_SERVICE_ID = "tdlxht_shake_service_channel"
        const val CHANNEL_ALERT_ID = "tdlxht_shake_alert_channel"
        private const val SERVICE_NOTIFICATION_ID = 9001
        private const val ALERT_NOTIFICATION_ID = 9002

        fun start(context: Context) {
            if (!AppPreferences.isShakeToOpenEnabled(context)) {
                SafeLog.d(TAG, "Shake to Quick Add is disabled in preferences; not starting service")
                return
            }
            try {
                val intent = Intent(context, ShakeDetectionService::class.java)
                ContextCompat.startForegroundService(context, intent)
                SafeLog.d(TAG, "Foreground service start requested")
            } catch (e: Exception) {
                SafeLog.e(TAG, "Failed to start ShakeDetectionService as foreground service", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, ShakeDetectionService::class.java)
                context.stopService(intent)
                SafeLog.d(TAG, "Foreground service stop requested")
            } catch (e: Exception) {
                SafeLog.e(TAG, "Failed to stop ShakeDetectionService", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        SafeLog.d(TAG, "Service onCreate")
        createNotificationChannels()
        startInForeground()

        if (AppPreferences.isShakeToOpenEnabled(this)) {
            shakeDetector = ShakeDetector {
                onDeviceShaken()
            }
            shakeDetector?.start(this)
            SafeLog.d(TAG, "Shake detector started in background service")
        } else {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!AppPreferences.isShakeToOpenEnabled(this)) {
            SafeLog.d(TAG, "Feature disabled; stopping service")
            stopForegroundService()
            return START_NOT_STICKY
        }
        startInForeground()
        return START_STICKY
    }

    private fun startInForeground() {
        val notification = buildPersistentNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    SERVICE_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(SERVICE_NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            SafeLog.e(TAG, "Error calling startForeground", e)
        }
    }

    private fun onDeviceShaken() {
        SafeLog.d(TAG, "Deliberate shake confirmed — triggering Quick Add flow")
        VibrationHelper.shakeDetected(this)

        // 1. Notify in-foreground Activity immediately via shared event bus
        ShakeEventBus.emitShake()

        // 2. Prepare intent to open/bring MainActivity directly to foreground with Quick Add flag
        val targetIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra(MainActivity.EXTRA_SHAKE_TRIGGERED, true)
        }

        val activityOptions = android.app.ActivityOptions.makeBasic()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activityOptions.setPendingIntentBackgroundActivityStartMode(
                android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
            )
        }
        val optionsBundle = activityOptions.toBundle()

        val pendingIntent = PendingIntent.getActivity(
            this,
            ALERT_NOTIFICATION_ID,
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Try direct activity launch
        var launchedDirectly = false
        try {
            startActivity(targetIntent, optionsBundle)
            launchedDirectly = true
            SafeLog.d(TAG, "startActivity invoked directly with background start options")
        } catch (e: Exception) {
            SafeLog.w(TAG, "startActivity directly from background was restricted by platform", e)
        }

        // Try sending pendingIntent directly with background activity start options
        if (!launchedDirectly) {
            try {
                pendingIntent.send(this, 0, null, null, null, null, optionsBundle)
                launchedDirectly = true
                SafeLog.d(TAG, "pendingIntent.send invoked with background start options")
            } catch (e: Exception) {
                SafeLog.w(TAG, "pendingIntent.send was restricted", e)
            }
        }

        // 3. Guaranteed pop via FullScreenIntent with high-priority channel
        postQuickAddAlertNotification(pendingIntent)
    }

    private fun postQuickAddAlertNotification(pendingIntent: PendingIntent) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val alertNotification = NotificationCompat.Builder(this, CHANNEL_ALERT_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("TDlxHT")
            .setContentText("Shake detected. Tap to Quick Add Task")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        notificationManager.notify(ALERT_NOTIFICATION_ID, alertNotification)
    }

    private fun buildPersistentNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            SERVICE_NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, CHANNEL_SERVICE_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("TDlxHT")
            .setContentText("Shake to Quick Add is active")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setShowWhen(false)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // Channel for the silent persistent foreground service notification
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE_ID,
                "Shake to Quick Add Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maintains background shake detection for instant Quick Add"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(serviceChannel)

            // Channel for high-priority alert when shake is detected while app is in background
            val alertChannel = NotificationChannel(
                CHANNEL_ALERT_ID,
                "Shake Quick Add Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts to launch Quick Add when device is shaken from background"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    private fun stopForegroundService() {
        shakeDetector?.stop()
        shakeDetector = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Exception) {
        }
        stopSelf()
    }

    override fun onDestroy() {
        SafeLog.d(TAG, "Service onDestroy: cleaning up sensor listener")
        shakeDetector?.stop()
        shakeDetector = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
