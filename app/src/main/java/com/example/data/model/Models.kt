package com.example.data.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class TaskPriority(val displayName: String) {
  LOW("Low"),
  MEDIUM("Medium"),
  HIGH("High");

  companion object {
    fun fromString(value: String): TaskPriority = entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
  }
}

enum class TaskCategory(val displayName: String) {
  PERSONAL("Personal"),
  COLLEGE("College"),
  WORK("Work"),
  STUDY("Study"),
  SHOPPING("Shopping"),
  OTHER("Other");

  companion object {
    fun fromString(value: String): TaskCategory = entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: PERSONAL
  }
}

enum class HabitCategory(val displayName: String) {
  PERSONAL("Personal"),
  HEALTH("Health"),
  STUDY("Study"),
  FITNESS("Fitness"),
  PRODUCTIVITY("Productivity"),
  OTHER("Other");

  companion object {
    fun fromString(value: String): HabitCategory = entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: PERSONAL
  }
}

enum class HabitFrequency(val displayName: String) {
  EVERY_DAY("Every Day"),
  WEEKDAYS("Weekdays"),
  WEEKLY("Weekly"),
  CUSTOM("Custom");

  companion object {
    fun fromString(value: String): HabitFrequency = entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: EVERY_DAY
  }
}

enum class ReminderOption(val displayName: String, val minutesBefore: Int) {
  NONE("No Reminder", -1),
  AT_DUE_TIME("At due time", 0),
  MIN_5("5 minutes before", 5),
  MIN_15("15 minutes before", 15),
  MIN_30("30 minutes before", 30),
  HOUR_1("1 hour before", 60),
  CUSTOM("Custom", 0);

  companion object {
    fun fromString(value: String): ReminderOption = entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: NONE
  }
}

enum class RepeatRule(val displayName: String) {
  NONE("None"),
  DAILY("Daily"),
  WEEKDAYS("Weekdays"),
  WEEKLY("Weekly"),
  MONTHLY("Monthly"),
  CUSTOM("Custom");

  companion object {
    fun fromString(value: String): RepeatRule = entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: NONE
  }
}

enum class TaskFilter(val title: String) {
  ALL("All"),
  TODAY("Today"),
  UPCOMING("Upcoming"),
  COMPLETED("Completed"),
  OVERDUE("Overdue"),
  HIGH_PRIORITY("High Priority")
}

enum class TaskSort(val displayName: String) {
  DUE_DATE("Due Date"),
  DUE_TIME("Due Time"),
  PRIORITY("Priority"),
  CREATION_DATE("Creation Date"),
  RECENTLY_UPDATED("Recently Updated")
}

data class Task(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val notes: String = "",
  val dueDate: String, // Format: YYYY-MM-DD
  val dueTime: String = "09:00", // Format: HH:mm
  val isCompleted: Boolean = false,
  val priority: TaskPriority = TaskPriority.MEDIUM,
  val category: String = TaskCategory.WORK.displayName,
  val tags: List<String> = emptyList(),
  val reminderSetting: ReminderOption = ReminderOption.NONE,
  val customReminderMinutes: Int = 15,
  val repeatRule: RepeatRule = RepeatRule.NONE,
  val repeatDays: List<DayOfWeek> = emptyList(),
  val isShortTerm: Boolean = false,
  val creationTimestamp: Long = System.currentTimeMillis(),
  val updatedTimestamp: Long = System.currentTimeMillis()
) {
  fun isOverdue(currentDate: LocalDate = LocalDate.now(), currentTime: LocalTime = LocalTime.now()): Boolean {
    if (isCompleted) return false
    return try {
      val due = LocalDate.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE)
      if (due.isBefore(currentDate)) {
        true
      } else if (due.isEqual(currentDate)) {
        val time = LocalTime.parse(dueTime, DateTimeFormatter.ofPattern("HH:mm"))
        time.isBefore(currentTime)
      } else {
        false
      }
    } catch (e: Exception) {
      false
    }
  }

  fun isDueToday(currentDate: LocalDate = LocalDate.now()): Boolean {
    return try {
      val due = LocalDate.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE)
      due.isEqual(currentDate)
    } catch (e: Exception) {
      false
    }
  }
}

data class Subtask(
  val id: String = UUID.randomUUID().toString(),
  val parentTaskId: String,
  val title: String,
  val isCompleted: Boolean = false,
  val creationTimestamp: Long = System.currentTimeMillis()
)

data class Habit(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val description: String = "",
  val category: String = HabitCategory.PERSONAL.displayName,
  val frequency: HabitFrequency = HabitFrequency.EVERY_DAY,
  val customDays: List<DayOfWeek> = emptyList(),
  val targetValue: Float = 1.0f,
  val targetUnit: String = "times",
  val startDate: String, // Format: YYYY-MM-DD
  val reminderTime: String = "08:00", // Format: HH:mm
  val reminderEnabled: Boolean = false,
  val isActive: Boolean = true,
  val isPaused: Boolean = false,
  val isArchived: Boolean = false,
  val creationTimestamp: Long = System.currentTimeMillis(),
  val updatedTimestamp: Long = System.currentTimeMillis()
) {
  fun isScheduledOn(date: LocalDate): Boolean {
    if (!isActive || isArchived) return false
    val start = try { LocalDate.parse(startDate) } catch (e: Exception) { date }
    if (date.isBefore(start)) return false

    return when (frequency) {
      HabitFrequency.EVERY_DAY -> true
      HabitFrequency.WEEKDAYS -> date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY
      HabitFrequency.WEEKLY -> {
        // Scheduled once a week, on the day of week matching start date or first day
        val scheduledDay = start.dayOfWeek
        date.dayOfWeek == scheduledDay
      }
      HabitFrequency.CUSTOM -> {
        if (customDays.isEmpty()) true else customDays.contains(date.dayOfWeek)
      }
    }
  }

  val reminderTimes: List<String>
    get() {
      if (reminderTime.isBlank()) return listOf("08:00")
      val list = reminderTime.split(",").map { it.trim() }.filter { it.isNotEmpty() }
      return if (list.isEmpty()) listOf("08:00") else list
    }

  fun getReminderTimeForSession(sessionIndex: Int): String {
    val times = reminderTimes
    return times.getOrNull(sessionIndex) ?: times.firstOrNull() ?: "08:00"
  }
}

data class HabitCompletionRecord(
  val id: String = UUID.randomUUID().toString(),
  val habitId: String,
  val date: String, // Format: YYYY-MM-DD
  val isCompleted: Boolean,
  val currentValue: Float = 1.0f,
  val targetValue: Float = 1.0f,
  val timestamp: Long = System.currentTimeMillis()
)

data class HabitStreakInfo(
  val habitId: String,
  val currentStreak: Int,
  val longestStreak: Int,
  val totalCompletions: Int,
  val completionRate: Float, // 0.0 to 1.0
  val scheduledDaysCount: Int,
  val missedScheduledDaysCount: Int
)

data class AppSettings(
  val pushNotificationsEnabled: Boolean = true,
  val taskRemindersEnabled: Boolean = true,
  val habitRemindersEnabled: Boolean = true,
  val soundAlertsEnabled: Boolean = false,
  val dailySummaryEnabled: Boolean = true,
  val dailySummaryTime: String = "20:00",
  val defaultReminderMinutesBefore: Int = 15,
  val defaultHabitReminderTime: String = "08:00",
  val autoMarkTaskOnSubtasksDone: Boolean = false,
  val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
  val hasPromptedPermissions: Boolean = false,
  val calendarDefaultView: String = "MONTH", // MONTH, WEEK, DAY
  val startOfWeek: String = "MONDAY", // MONDAY, SUNDAY
  val showCompletedTasksInCalendar: Boolean = true,
  val showCompletedHabitsInCalendar: Boolean = true,
  val showOverdueInCalendar: Boolean = true,
  val timeFormat24Hour: Boolean = true
)
