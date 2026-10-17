package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.HabitTaskApplication
import com.example.MainActivity
import com.example.data.local.toDomainModel
import com.example.data.local.toEntity
import com.example.data.model.HabitFrequency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val app = context.applicationContext as? HabitTaskApplication ?: return
    val notificationType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE) ?: TYPE_TASK

    CoroutineScope(Dispatchers.IO).launch {
      try {
        val settings = app.database.settingsDao().getSettingsSync()?.toDomainModel()
          ?: com.example.data.model.AppSettings()

        if (!settings.pushNotificationsEnabled) {
          Log.d(TAG, "Push notifications globally disabled. Suppressing notification.")
          return@launch
        }

        when (notificationType) {
          TYPE_TASK -> handleTaskReminder(context, app, intent, settings)
          TYPE_HABIT -> handleHabitReminder(context, app, intent, settings)
          TYPE_DAILY_SUMMARY -> handleDailySummary(context, app, settings)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error handling alarm in AlarmReceiver", e)
      }
    }
  }

  private suspend fun handleTaskReminder(
    context: Context,
    app: HabitTaskApplication,
    intent: Intent,
    settings: com.example.data.model.AppSettings
  ) {
    if (!settings.taskRemindersEnabled) return

    val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
    val taskEntity = app.database.taskDao().getTaskByIdSync(taskId) ?: return
    val task = taskEntity.toDomainModel()

    // Do not notify if task is completed
    if (task.isCompleted) {
      Log.d(TAG, "Task $taskId is already completed. Skipping reminder notification.")
      return
    }

    val notificationId = taskId.hashCode()
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Open App Intent
    val contentIntent = PendingIntent.getActivity(
      context,
      notificationId,
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      },
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    // Mark Complete Action
    val completeIntent = PendingIntent.getBroadcast(
      context,
      notificationId + 1,
      Intent(context, NotificationActionReceiver::class.java).apply {
        action = NotificationActionReceiver.ACTION_MARK_TASK_COMPLETE
        putExtra("EXTRA_TASK_ID", taskId)
        putExtra("EXTRA_NOTIFICATION_ID", notificationId)
      },
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    // Snooze Action (10 min)
    val snoozeIntent = PendingIntent.getBroadcast(
      context,
      notificationId + 2,
      Intent(context, NotificationActionReceiver::class.java).apply {
        action = NotificationActionReceiver.ACTION_SNOOZE_TASK
        putExtra("EXTRA_TASK_ID", taskId)
        putExtra("EXTRA_TASK_TITLE", task.title)
        putExtra("EXTRA_SNOOZE_MINUTES", 10)
        putExtra("EXTRA_NOTIFICATION_ID", notificationId)
      },
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    val notification = NotificationCompat.Builder(context, HabitTaskApplication.CHANNEL_TASKS_ID)
      .setSmallIcon(android.R.drawable.ic_popup_reminder)
      .setContentTitle("Task Reminder: ${task.title}")
      .setContentText("Due at ${task.dueTime} (${task.category})")
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setDefaults(if (settings.soundAlertsEnabled) NotificationCompat.DEFAULT_ALL else NotificationCompat.DEFAULT_VIBRATE)
      .setContentIntent(contentIntent)
      .addAction(android.R.drawable.checkbox_on_background, "Complete", completeIntent)
      .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 10m", snoozeIntent)
      .build()

    notificationManager.notify(notificationId, notification)
  }

  private suspend fun handleHabitReminder(
    context: Context,
    app: HabitTaskApplication,
    intent: Intent,
    settings: com.example.data.model.AppSettings
  ) {
    if (!settings.habitRemindersEnabled) return

    val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
    val habitEntity = app.database.habitDao().getHabitByIdSync(habitId) ?: return
    val habit = habitEntity.toDomainModel()

    if (!habit.isActive || habit.isArchived || habit.isPaused || !habit.reminderEnabled) {
      Log.d(TAG, "Habit $habitId is inactive, paused, or archived. Skipping reminder.")
      return
    }

    val today = LocalDate.now()
    if (!habit.isScheduledOn(today)) {
      Log.d(TAG, "Habit $habitId is not scheduled for today ($today). Skipping reminder.")
      // Schedule for next occurrence
      ReminderScheduler.scheduleHabitReminder(
        context = context,
        habitId = habit.id,
        habitName = habit.name,
        reminderTime = habit.reminderTime,
        frequency = habit.frequency,
        customDays = habit.customDays,
        startDate = habit.startDate
      )
      return
    }

    val sessionIndex = intent.getIntExtra(EXTRA_SESSION_INDEX, 0)
    val sessionTotal = intent.getIntExtra(EXTRA_SESSION_TOTAL, 1)

    val notificationId = habitId.hashCode() * 31 + sessionIndex
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Open App Intent
    val contentIntent = PendingIntent.getActivity(
      context,
      notificationId,
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      },
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    // Mark Done Action
    val doneIntent = PendingIntent.getBroadcast(
      context,
      notificationId + 1,
      Intent(context, NotificationActionReceiver::class.java).apply {
        action = NotificationActionReceiver.ACTION_MARK_HABIT_DONE
        putExtra("EXTRA_HABIT_ID", habitId)
        putExtra("EXTRA_NOTIFICATION_ID", notificationId)
      },
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    // Snooze Action (15 min)
    val snoozeIntent = PendingIntent.getBroadcast(
      context,
      notificationId + 2,
      Intent(context, NotificationActionReceiver::class.java).apply {
        action = NotificationActionReceiver.ACTION_SNOOZE_HABIT
        putExtra("EXTRA_HABIT_ID", habitId)
        putExtra("EXTRA_HABIT_NAME", habit.name)
        putExtra("EXTRA_SNOOZE_MINUTES", 15)
        putExtra("EXTRA_NOTIFICATION_ID", notificationId)
      },
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    val title = if (sessionTotal > 1) {
      "Habit Check-In: ${habit.name} (Session ${sessionIndex + 1}/$sessionTotal)"
    } else {
      "Habit Check-In: ${habit.name}"
    }

    val contentText = if (sessionTotal > 1) {
      "Time for session ${sessionIndex + 1}! Keep your streak alive."
    } else {
      "Keep your streak alive! Have you checked this in today?"
    }

    val notification = NotificationCompat.Builder(context, HabitTaskApplication.CHANNEL_HABITS_ID)
      .setSmallIcon(android.R.drawable.ic_popup_reminder)
      .setContentTitle(title)
      .setContentText(contentText)
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setDefaults(if (settings.soundAlertsEnabled) NotificationCompat.DEFAULT_ALL else NotificationCompat.DEFAULT_VIBRATE)
      .setContentIntent(contentIntent)
      .addAction(android.R.drawable.checkbox_on_background, "Done", doneIntent)
      .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 15m", snoozeIntent)
      .build()

    notificationManager.notify(notificationId, notification)

    // Automatically schedule this session for the next scheduled day
    val nextTime = habit.getReminderTimeForSession(sessionIndex)
    ReminderScheduler.scheduleSingleHabitReminder(
      context = context,
      habitId = habit.id,
      habitName = habit.name,
      reminderTime = nextTime,
      sessionIndex = sessionIndex,
      totalSessions = sessionTotal,
      frequency = habit.frequency,
      customDays = habit.customDays,
      startDate = habit.startDate,
      afterDate = today
    )
  }

  private suspend fun handleDailySummary(
    context: Context,
    app: HabitTaskApplication,
    settings: com.example.data.model.AppSettings
  ) {
    if (!settings.dailySummaryEnabled) return

    val today = LocalDate.now()
    val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

    val tasks = app.database.taskDao().getAllTasksSync()
    val todayTasks = tasks.filter { it.dueDate == todayStr }
    val tasksDone = todayTasks.count { it.isCompleted }
    val tasksTotal = todayTasks.size

    val habits = app.database.habitDao().getAllHabitsSync()
    val scheduledHabits = habits.filter {
      val h = it.toDomainModel()
      h.isActive && !h.isArchived && !h.isPaused && h.isScheduledOn(today)
    }
    val habitsTotal = scheduledHabits.size

    val todayRecords = app.database.habitCompletionRecordDao().getRecordsForDateSync(todayStr)
    val todayCompletedHabitIds = todayRecords.filter { it.isCompleted }.map { it.habitId }.toSet()
    val todayCompletedCount = scheduledHabits.count { todayCompletedHabitIds.contains(it.id) }

    val allHabitRecords = app.database.habitCompletionRecordDao().getAllRecordsSync().map { it.toDomainModel() }
    val maxStreak = habits.map { it.toDomainModel() }
      .filter { it.isActive && !it.isArchived }
      .maxOfOrNull { habit ->
        val recs = allHabitRecords.filter { it.habitId == habit.id }
        com.example.data.repository.HabitRepository.calculateStreak(habit, recs, today).currentStreak
      } ?: 0

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = 99999

    val contentIntent = PendingIntent.getActivity(
      context,
      notificationId,
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      },
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    val streakStr = if (maxStreak > 0) " • $maxStreak-day streak" else ""
    val summaryText = "Tasks: $tasksDone/$tasksTotal done • Habits: $todayCompletedCount/$habitsTotal checked$streakStr"

    val notification = NotificationCompat.Builder(context, HabitTaskApplication.CHANNEL_SUMMARY_ID)
      .setSmallIcon(android.R.drawable.ic_popup_reminder)
      .setContentTitle("Daily Productivity Summary")
      .setContentText(summaryText)
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(contentIntent)
      .build()

    notificationManager.notify(notificationId, notification)

    // Reschedule daily summary for tomorrow
    ReminderScheduler.scheduleDailySummary(context, settings.dailySummaryTime)
  }

  companion object {
    private const val TAG = "AlarmReceiver"
    const val EXTRA_NOTIFICATION_TYPE = "EXTRA_NOTIFICATION_TYPE"
    const val TYPE_TASK = "TYPE_TASK"
    const val TYPE_HABIT = "TYPE_HABIT"
    const val TYPE_DAILY_SUMMARY = "TYPE_DAILY_SUMMARY"

    const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
    const val EXTRA_HABIT_ID = "EXTRA_HABIT_ID"
    const val EXTRA_SESSION_INDEX = "EXTRA_SESSION_INDEX"
    const val EXTRA_SESSION_TOTAL = "EXTRA_SESSION_TOTAL"
  }
}

object ReminderScheduler {
  private const val TAG = "ReminderScheduler"

  fun scheduleTaskReminder(
    context: Context,
    taskId: String,
    title: String,
    dueDate: String,
    dueTime: String,
    minutesBefore: Int
  ) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val date = LocalDate.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE)
      val time = LocalTime.parse(dueTime, DateTimeFormatter.ofPattern("HH:mm"))
      val dueDateTime = LocalDateTime.of(date, time)
      val triggerDateTime = dueDateTime.minusMinutes(minutesBefore.toLong())

      val triggerEpochMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
      if (triggerEpochMillis <= System.currentTimeMillis()) {
        return // Date/time is in the past
      }

      val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(AlarmReceiver.EXTRA_NOTIFICATION_TYPE, AlarmReceiver.TYPE_TASK)
        putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )

      setAlarm(alarmManager, triggerEpochMillis, pendingIntent)
      Log.d(TAG, "Scheduled task reminder for $title at $triggerDateTime")
    } catch (e: Exception) {
      Log.e(TAG, "Error scheduling task reminder", e)
    }
  }

  fun cancelTaskReminder(context: Context, taskId: String) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, AlarmReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.hashCode(),
        intent,
        PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )
      if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Cancelled task reminder for task $taskId")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error cancelling task reminder", e)
    }
  }

  fun snoozeTask(context: Context, taskId: String, title: String, snoozeMinutes: Int) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val triggerEpochMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

      val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(AlarmReceiver.EXTRA_NOTIFICATION_TYPE, AlarmReceiver.TYPE_TASK)
        putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )

      setAlarm(alarmManager, triggerEpochMillis, pendingIntent)
      Log.d(TAG, "Snoozed task reminder for $title by $snoozeMinutes minutes")
    } catch (e: Exception) {
      Log.e(TAG, "Error snoozing task reminder", e)
    }
  }

  fun scheduleSingleHabitReminder(
    context: Context,
    habitId: String,
    habitName: String,
    reminderTime: String,
    sessionIndex: Int = 0,
    totalSessions: Int = 1,
    frequency: HabitFrequency = HabitFrequency.EVERY_DAY,
    customDays: List<DayOfWeek> = emptyList(),
    startDate: String = LocalDate.now().toString(),
    afterDate: LocalDate? = null
  ) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val cleanTime = if (reminderTime.isBlank()) "08:00" else reminderTime.trim()
      val time = LocalTime.parse(cleanTime, DateTimeFormatter.ofPattern("HH:mm"))
      val now = LocalDateTime.now()

      // Find the next scheduled date
      var checkDate = afterDate?.plusDays(1) ?: LocalDate.now()
      val tempHabit = com.example.data.model.Habit(
        id = habitId,
        name = habitName,
        frequency = frequency,
        customDays = customDays,
        startDate = startDate
      )

      var foundDate: LocalDate? = null
      for (i in 0..30) {
        val targetDateTime = LocalDateTime.of(checkDate, time)
        if (targetDateTime.isAfter(now) && tempHabit.isScheduledOn(checkDate)) {
          foundDate = checkDate
          break
        }
        checkDate = checkDate.plusDays(1)
      }

      if (foundDate == null) return

      val triggerDateTime = LocalDateTime.of(foundDate, time)
      val triggerEpochMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

      val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(AlarmReceiver.EXTRA_NOTIFICATION_TYPE, AlarmReceiver.TYPE_HABIT)
        putExtra(AlarmReceiver.EXTRA_HABIT_ID, habitId)
        putExtra(AlarmReceiver.EXTRA_SESSION_INDEX, sessionIndex)
        putExtra(AlarmReceiver.EXTRA_SESSION_TOTAL, totalSessions)
      }

      val requestCode = habitId.hashCode() * 31 + sessionIndex
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )

      setAlarm(alarmManager, triggerEpochMillis, pendingIntent)
      Log.d(TAG, "Scheduled habit reminder for $habitName session ${sessionIndex + 1}/$totalSessions at $triggerDateTime")
    } catch (e: Exception) {
      Log.e(TAG, "Error scheduling habit reminder", e)
    }
  }

  fun scheduleHabitReminders(
    context: Context,
    habit: com.example.data.model.Habit,
    afterDate: LocalDate? = null
  ) {
    cancelHabitReminder(context, habit.id)
    if (!habit.isActive || habit.isArchived || habit.isPaused || !habit.reminderEnabled) return

    val sessionCount = maxOf(1, habit.targetValue.toInt())
    val times = habit.reminderTimes
    for (i in 0 until sessionCount) {
      val timeStr = times.getOrNull(i) ?: times.firstOrNull() ?: "08:00"
      scheduleSingleHabitReminder(
        context = context,
        habitId = habit.id,
        habitName = habit.name,
        reminderTime = timeStr,
        sessionIndex = i,
        totalSessions = sessionCount,
        frequency = habit.frequency,
        customDays = habit.customDays,
        startDate = habit.startDate,
        afterDate = afterDate
      )
    }
  }

  fun scheduleHabitReminder(
    context: Context,
    habitId: String,
    habitName: String,
    reminderTime: String,
    frequency: HabitFrequency = HabitFrequency.EVERY_DAY,
    customDays: List<DayOfWeek> = emptyList(),
    startDate: String = LocalDate.now().toString(),
    afterDate: LocalDate? = null
  ) {
    val times = reminderTime.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    if (times.size > 1) {
      times.forEachIndexed { idx, t ->
        scheduleSingleHabitReminder(
          context = context,
          habitId = habitId,
          habitName = habitName,
          reminderTime = t,
          sessionIndex = idx,
          totalSessions = times.size,
          frequency = frequency,
          customDays = customDays,
          startDate = startDate,
          afterDate = afterDate
        )
      }
    } else {
      scheduleSingleHabitReminder(
        context = context,
        habitId = habitId,
        habitName = habitName,
        reminderTime = reminderTime,
        sessionIndex = 0,
        totalSessions = 1,
        frequency = frequency,
        customDays = customDays,
        startDate = startDate,
        afterDate = afterDate
      )
    }
  }

  fun cancelHabitReminder(context: Context, habitId: String) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      
      // Cancel legacy request code
      val legacyIntent = Intent(context, AlarmReceiver::class.java)
      val legacyPendingIntent = PendingIntent.getBroadcast(
        context,
        habitId.hashCode(),
        legacyIntent,
        PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )
      if (legacyPendingIntent != null) {
        alarmManager.cancel(legacyPendingIntent)
        legacyPendingIntent.cancel()
      }

      // Cancel up to 16 multi-session request codes
      for (session in 0..15) {
        val reqCode = habitId.hashCode() * 31 + session
        val sessionIntent = Intent(context, AlarmReceiver::class.java)
        val sessionPi = PendingIntent.getBroadcast(
          context,
          reqCode,
          sessionIntent,
          PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        if (sessionPi != null) {
          alarmManager.cancel(sessionPi)
          sessionPi.cancel()
        }
      }
      Log.d(TAG, "Cancelled habit reminders for habit $habitId")
    } catch (e: Exception) {
      Log.e(TAG, "Error cancelling habit reminder", e)
    }
  }

  fun snoozeHabit(context: Context, habitId: String, habitName: String, snoozeMinutes: Int) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val triggerEpochMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

      val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(AlarmReceiver.EXTRA_NOTIFICATION_TYPE, AlarmReceiver.TYPE_HABIT)
        putExtra(AlarmReceiver.EXTRA_HABIT_ID, habitId)
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        habitId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )

      setAlarm(alarmManager, triggerEpochMillis, pendingIntent)
      Log.d(TAG, "Snoozed habit reminder for $habitName by $snoozeMinutes minutes")
    } catch (e: Exception) {
      Log.e(TAG, "Error snoozing habit reminder", e)
    }
  }

  fun scheduleDailySummary(context: Context, timeStr: String) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
      var targetDateTime = LocalDateTime.of(LocalDate.now(), time)
      if (targetDateTime.isBefore(LocalDateTime.now())) {
        targetDateTime = targetDateTime.plusDays(1)
      }

      val triggerEpochMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
      val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(AlarmReceiver.EXTRA_NOTIFICATION_TYPE, AlarmReceiver.TYPE_DAILY_SUMMARY)
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        99999,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )

      setAlarm(alarmManager, triggerEpochMillis, pendingIntent)
      Log.d(TAG, "Scheduled daily productivity summary for $targetDateTime")
    } catch (e: Exception) {
      Log.e(TAG, "Error scheduling daily summary", e)
    }
  }

  fun cancelDailySummary(context: Context) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, AlarmReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        99999,
        intent,
        PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )
      if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error cancelling daily summary", e)
    }
  }

  fun cancelAllUserReminders(context: Context, taskIds: List<String> = emptyList(), habitIds: List<String> = emptyList()) {
    try {
      taskIds.forEach { cancelTaskReminder(context, it) }
      habitIds.forEach { cancelHabitReminder(context, it) }
      cancelDailySummary(context)
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
      notificationManager?.cancelAll()
    } catch (e: Exception) {
      Log.e(TAG, "Error cancelling all user reminders", e)
    }
  }

  fun rescheduleAllTaskReminders(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val app = context.applicationContext as? HabitTaskApplication ?: return@launch
        val tasks = app.database.taskDao().getAllTasksSync()
        for (task in tasks) {
          if (!task.isCompleted && task.dueDate.isNotBlank() && task.dueTime.isNotBlank() && task.reminderSetting != "NONE") {
            scheduleTaskReminder(
              context = context,
              taskId = task.id,
              title = task.title,
              dueDate = task.dueDate,
              dueTime = task.dueTime,
              minutesBefore = task.customReminderMinutes
            )
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error rescheduling all tasks", e)
      }
    }
  }

  fun cancelAllTaskReminders(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val app = context.applicationContext as? HabitTaskApplication ?: return@launch
        val tasks = app.database.taskDao().getAllTasksSync()
        for (task in tasks) {
          cancelTaskReminder(context, task.id)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error cancelling all task reminders", e)
      }
    }
  }

  fun rescheduleAllHabitReminders(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val app = context.applicationContext as? HabitTaskApplication ?: return@launch
        val habits = app.database.habitDao().getAllHabitsSync()
        for (habit in habits) {
          if (habit.isActive && !habit.isArchived && !habit.isPaused && habit.reminderEnabled && habit.reminderTime.isNotBlank()) {
            val freq = HabitFrequency.fromString(habit.frequency)
            val customDays = if (habit.customDaysJson.isNotBlank()) {
              habit.customDaysJson.split(",").mapNotNull {
                try { DayOfWeek.valueOf(it.trim()) } catch (_: Exception) { null }
              }
            } else emptyList()
            scheduleHabitReminder(
              context = context,
              habitId = habit.id,
              habitName = habit.name,
              reminderTime = habit.reminderTime,
              frequency = freq,
              customDays = customDays
            )
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error rescheduling all habit reminders", e)
      }
    }
  }

  fun cancelAllHabitReminders(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val app = context.applicationContext as? HabitTaskApplication ?: return@launch
        val habits = app.database.habitDao().getAllHabitsSync()
        for (habit in habits) {
          cancelHabitReminder(context, habit.id)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error cancelling all habit reminders", e)
      }
    }
  }

  private fun setAlarm(alarmManager: AlarmManager, triggerEpochMillis: Long, pendingIntent: PendingIntent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (alarmManager.canScheduleExactAlarms()) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
      } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
    } else {
      alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
    }
  }
}
