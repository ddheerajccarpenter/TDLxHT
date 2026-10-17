package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.repository.HabitRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.TaskRepository

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
    // ShakeDetectionService is started safely in MainActivity.onResume() when app is in foreground
  }

  private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = getSystemService(NotificationManager::class.java)

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

      notificationManager.createNotificationChannel(taskChannel)
      notificationManager.createNotificationChannel(habitChannel)
      notificationManager.createNotificationChannel(summaryChannel)
      notificationManager.createNotificationChannel(updateChannel)
    }
  }

  companion object {
    const val CHANNEL_TASKS_ID = "channel_tasks"
    const val CHANNEL_HABITS_ID = "channel_habits"
    const val CHANNEL_SUMMARY_ID = "channel_daily_summary"
    const val CHANNEL_UPDATES_ID = "channel_app_updates"

    lateinit var instance: HabitTaskApplication
      private set
  }
}
