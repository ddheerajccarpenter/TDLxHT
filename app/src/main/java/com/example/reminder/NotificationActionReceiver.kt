package com.example.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.HabitTaskApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NotificationActionReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action ?: return
    val notificationId = intent.getIntExtra("EXTRA_NOTIFICATION_ID", -1)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    if (notificationId != -1) {
      notificationManager?.cancel(notificationId)
    }

    val app = context.applicationContext as? HabitTaskApplication ?: return
    val scope = CoroutineScope(Dispatchers.IO)

    when (action) {
      ACTION_MARK_TASK_COMPLETE -> {
        val taskId = intent.getStringExtra("EXTRA_TASK_ID") ?: return
        scope.launch {
          try {
            app.taskRepository.toggleTaskCompletion(taskId)
          } catch (e: Exception) {
            Log.e(TAG, "Error marking task complete from notification", e)
          }
        }
      }

      ACTION_SNOOZE_TASK -> {
        val taskId = intent.getStringExtra("EXTRA_TASK_ID") ?: return
        val taskTitle = intent.getStringExtra("EXTRA_TASK_TITLE") ?: "Task"
        val snoozeMinutes = intent.getIntExtra("EXTRA_SNOOZE_MINUTES", 10)
        ReminderScheduler.snoozeTask(context, taskId, taskTitle, snoozeMinutes)
      }

      ACTION_MARK_HABIT_DONE -> {
        val habitId = intent.getStringExtra("EXTRA_HABIT_ID") ?: return
        scope.launch {
          try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            app.habitRepository.recordHabitProgress(habitId, todayStr, 1f)
          } catch (e: Exception) {
            Log.e(TAG, "Error marking habit done from notification", e)
          }
        }
      }

      ACTION_SNOOZE_HABIT -> {
        val habitId = intent.getStringExtra("EXTRA_HABIT_ID") ?: return
        val habitName = intent.getStringExtra("EXTRA_HABIT_NAME") ?: "Habit"
        val snoozeMinutes = intent.getIntExtra("EXTRA_SNOOZE_MINUTES", 15)
        ReminderScheduler.snoozeHabit(context, habitId, habitName, snoozeMinutes)
      }
    }
  }

  companion object {
    private const val TAG = "NotificationActionReceiver"
    const val ACTION_MARK_TASK_COMPLETE = "com.example.reminder.ACTION_MARK_TASK_COMPLETE"
    const val ACTION_SNOOZE_TASK = "com.example.reminder.ACTION_SNOOZE_TASK"
    const val ACTION_MARK_HABIT_DONE = "com.example.reminder.ACTION_MARK_HABIT_DONE"
    const val ACTION_SNOOZE_HABIT = "com.example.reminder.ACTION_SNOOZE_HABIT"
  }
}
