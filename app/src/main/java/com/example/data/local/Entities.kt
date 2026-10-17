package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
  @PrimaryKey val id: String,
  val title: String,
  val notes: String,
  val dueDate: String, // YYYY-MM-DD
  val dueTime: String, // HH:mm
  val isCompleted: Boolean,
  val priority: String, // LOW, MEDIUM, HIGH
  val category: String, // Personal, College, Work, etc.
  val tagsJson: String, // JSON or comma-separated
  val reminderSetting: String, // ReminderOption
  val customReminderMinutes: Int,
  val repeatRule: String, // RepeatRule
  val repeatDaysJson: String,
  val isShortTerm: Boolean = false,
  val creationTimestamp: Long,
  val updatedTimestamp: Long
)

@Entity(
  tableName = "subtasks",
  indices = [Index(value = ["parentTaskId"])]
)
data class SubtaskEntity(
  @PrimaryKey val id: String,
  val parentTaskId: String,
  val title: String,
  val isCompleted: Boolean,
  val creationTimestamp: Long
)

@Entity(tableName = "habits")
data class HabitEntity(
  @PrimaryKey val id: String,
  val name: String,
  val description: String,
  val category: String,
  val frequency: String,
  val customDaysJson: String,
  val targetValue: Float,
  val targetUnit: String,
  val startDate: String, // YYYY-MM-DD
  val reminderTime: String, // HH:mm
  val reminderEnabled: Boolean,
  val isActive: Boolean,
  val isPaused: Boolean,
  val isArchived: Boolean,
  val creationTimestamp: Long,
  val updatedTimestamp: Long
)

@Entity(
  tableName = "habit_completion_records",
  indices = [
    Index(value = ["habitId", "date"], unique = true),
    Index(value = ["date"])
  ]
)
data class HabitCompletionRecordEntity(
  @PrimaryKey val id: String,
  val habitId: String,
  val date: String, // YYYY-MM-DD
  val isCompleted: Boolean,
  val currentValue: Float,
  val targetValue: Float,
  val timestamp: Long
)

@Entity(tableName = "app_settings")
data class SettingsEntity(
  @PrimaryKey val id: Int = 1,
  val pushNotificationsEnabled: Boolean = true,
  val taskRemindersEnabled: Boolean = true,
  val habitRemindersEnabled: Boolean = true,
  val soundAlertsEnabled: Boolean = false,
  val dailySummaryEnabled: Boolean = true,
  val dailySummaryTime: String = "20:00",
  val defaultReminderMinutesBefore: Int = 15,
  val defaultHabitReminderTime: String = "08:00",
  val autoMarkTaskOnSubtasksDone: Boolean = false,
  val themeMode: String = "SYSTEM",
  val hasPromptedPermissions: Boolean = false,
  val calendarDefaultView: String = "MONTH", // MONTH, WEEK, DAY
  val startOfWeek: String = "MONDAY", // MONDAY, SUNDAY
  val showCompletedTasksInCalendar: Boolean = true,
  val showCompletedHabitsInCalendar: Boolean = true,
  val showOverdueInCalendar: Boolean = true,
  val timeFormat24Hour: Boolean = true
)
