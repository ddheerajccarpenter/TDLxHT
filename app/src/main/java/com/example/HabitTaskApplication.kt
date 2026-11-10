package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.launch
import com.example.data.local.AppDatabase
import com.example.data.repository.HabitRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.TaskRepository
import com.example.util.AppPreferences

class HabitTaskApplication : Application() {

  lateinit var database: AppDatabase
    private set

  lateinit var taskRepository: TaskRepository
    private set

  lateinit var habitRepository: HabitRepository
    private set

  lateinit var settingsRepository: SettingsRepository
    private set

  override fun onCreate() {
    super.onCreate()
    instance = this
    database = AppDatabase.getDatabase(this)
    taskRepository = TaskRepository(database.taskDao(), database.subtaskDao())
    habitRepository = HabitRepository(database.habitDao(), database.habitCompletionRecordDao())
    settingsRepository = SettingsRepository(database.settingsDao())

    createNotificationChannels()
    com.example.util.AppPreferences.init(this)
    com.example.util.DashboardLayoutManager.init(this)
    com.example.util.SoundManager.init(this)
    // ShakeDetectionService is started safely in MainActivity.onResume() when app is in foreground

    // Clean up temporary downloaded APK installers to save local device storage
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
      com.example.util.ApkDownloadManager.cleanupDownloadedApks(this@HabitTaskApplication)
    }
  }

  private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = getSystemService(NotificationManager::class.java)

      val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

      // 1. Notification Sound One Channel
      val soundOneUri = Uri.parse("android.resource://$packageName/${R.raw.sound_one_notification}")
      val soundOneChannel = NotificationChannel(
        CHANNEL_NOTIFICATIONS_SOUND_ONE,
        "Notifications (Sound One)",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Application reminders with Sound One"
        enableVibration(true)
        setSound(soundOneUri, audioAttributes)
      }

      // 2. Notification Sound Two Channel
      val soundTwoUri = Uri.parse("android.resource://$packageName/${R.raw.sound_two_notification}")
      val soundTwoChannel = NotificationChannel(
        CHANNEL_NOTIFICATIONS_SOUND_TWO,
        "Notifications (Sound Two)",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Application reminders with Sound Two"
        enableVibration(true)
        setSound(soundTwoUri, audioAttributes)
      }

      // 3. Silent Notifications Channel
      val silentChannel = NotificationChannel(
        CHANNEL_NOTIFICATIONS_SILENT,
        "Notifications (Silent)",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Application reminders delivered silently"
        enableVibration(true)
        setSound(null, null)
      }

      val taskChannel = NotificationChannel(
        CHANNEL_TASKS_ID,
        "Task Reminders",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notifications for scheduled tasks"
        enableVibration(true)
      }

      val habitChannel = NotificationChannel(
        CHANNEL_HABITS_ID,
        "Habit Reminders",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Daily reminders for building consistent habits"
        enableVibration(true)
      }

      val summaryChannel = NotificationChannel(
        CHANNEL_SUMMARY_ID,
        "Daily Productivity Summary",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Evening summary of daily tasks and habit streaks"
        enableVibration(true)
      }

      val updateChannel = NotificationChannel(
        CHANNEL_UPDATES_ID,
        "App Updates",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notifications when a new update or version of TDlxHT is released"
        enableVibration(true)
      }

      val downloadChannel = NotificationChannel(
        CHANNEL_DOWNLOADS_ID,
        "App Downloads & Updates",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Progress notifications during update downloads"
        enableVibration(false)
        setShowBadge(false)
      }

      notificationManager.createNotificationChannel(soundOneChannel)
      notificationManager.createNotificationChannel(soundTwoChannel)
      notificationManager.createNotificationChannel(silentChannel)
      notificationManager.createNotificationChannel(taskChannel)
      notificationManager.createNotificationChannel(habitChannel)
      notificationManager.createNotificationChannel(summaryChannel)
      notificationManager.createNotificationChannel(updateChannel)
      notificationManager.createNotificationChannel(downloadChannel)
    }
  }

  companion object {
    const val CHANNEL_NOTIFICATIONS_SOUND_ONE = "notifications_sound_one"
    const val CHANNEL_NOTIFICATIONS_SOUND_TWO = "notifications_sound_two"
    const val CHANNEL_NOTIFICATIONS_SILENT = "notifications_silent"

    const val CHANNEL_TASKS_ID = "channel_tasks"
    const val CHANNEL_HABITS_ID = "channel_habits"
    const val CHANNEL_SUMMARY_ID = "channel_daily_summary"
    const val CHANNEL_UPDATES_ID = "channel_app_updates"
    const val CHANNEL_DOWNLOADS_ID = "channel_downloads"

    fun getActiveNotificationChannelId(context: Context): String {
      val soundsEnabled = AppPreferences.isNotificationSoundsEnabled(context)
      if (!soundsEnabled) return CHANNEL_NOTIFICATIONS_SILENT
      val selection = AppPreferences.getNotificationSoundSelection(context)
      return if (selection.equals("SOUND_TWO", ignoreCase = true)) CHANNEL_NOTIFICATIONS_SOUND_TWO else CHANNEL_NOTIFICATIONS_SOUND_ONE
    }

    fun getActiveNotificationSoundUri(context: Context): Uri? {
      val soundsEnabled = AppPreferences.isNotificationSoundsEnabled(context)
      if (!soundsEnabled) return null
      val selection = AppPreferences.getNotificationSoundSelection(context)
      val resId = if (selection.equals("SOUND_TWO", ignoreCase = true)) R.raw.sound_two_notification else R.raw.sound_one_notification
      return Uri.parse("android.resource://${context.packageName}/$resId")
    }

    lateinit var instance: HabitTaskApplication
      private set
  }
}
