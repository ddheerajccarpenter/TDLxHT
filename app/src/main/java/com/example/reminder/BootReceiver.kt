package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.HabitTaskApplication
import com.example.data.local.toDomainModel
import com.example.data.local.toEntity
import com.example.data.model.ReminderOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action
    if (action != Intent.ACTION_BOOT_COMPLETED &&
        action != Intent.ACTION_MY_PACKAGE_REPLACED &&
        action != Intent.ACTION_TIME_CHANGED &&
        action != Intent.ACTION_TIMEZONE_CHANGED) {
      return
    }

    Log.d(TAG, "Device rebooted or time changed, rescheduling all active reminders...")
    val app = context.applicationContext as? HabitTaskApplication ?: return

    if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
      if (com.example.util.AppPreferences.isShakeToOpenEnabled(context)) {
        com.example.service.ShakeDetectionService.start(context)
      }
    }

    CoroutineScope(Dispatchers.IO).launch {
      try {
        val settings = app.database.settingsDao().getSettingsSync()?.toDomainModel()
          ?: com.example.data.model.AppSettings()

        if (!settings.pushNotificationsEnabled) {
          Log.d(TAG, "Push notifications disabled in settings. Skipping reschedule.")
          return@launch
        }

        // Reschedule Daily Summary if enabled
        if (settings.dailySummaryEnabled) {
          ReminderScheduler.scheduleDailySummary(context, settings.dailySummaryTime)
        }

        // Reschedule Task Reminders
        if (settings.taskRemindersEnabled) {
          val tasks = app.database.taskDao().getAllTasksSync()
          val today = LocalDate.now()
          tasks.forEach { entity ->
            val task = entity.toDomainModel()
            if (!task.isCompleted && task.reminderSetting != ReminderOption.NONE) {
              val minutesBefore = if (task.reminderSetting == ReminderOption.CUSTOM) {
                task.customReminderMinutes
              } else {
                task.reminderSetting.minutesBefore
              }
              ReminderScheduler.scheduleTaskReminder(
                context = context,
                taskId = task.id,
                title = task.title,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                minutesBefore = minutesBefore
              )
            }
          }
        }

        // Reschedule Habit Reminders
        if (settings.habitRemindersEnabled) {
          val habits = app.database.habitDao().getAllHabitsSync()
          habits.forEach { entity ->
            val habit = entity.toDomainModel()
            if (habit.isActive && !habit.isArchived && !habit.isPaused && habit.reminderEnabled) {
              ReminderScheduler.scheduleHabitReminders(context, habit)
            }
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error rescheduling reminders on boot", e)
      }
    }
  }

  companion object {
    private const val TAG = "BootReceiver"
  }
}
