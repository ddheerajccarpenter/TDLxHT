package com.example.data.local

import com.example.data.model.AppSettings
import com.example.data.model.Habit
import com.example.data.model.HabitCompletionRecord
import com.example.data.model.HabitFrequency
import com.example.data.model.ReminderOption
import com.example.data.model.RepeatRule
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import java.time.DayOfWeek

fun TaskEntity.toDomainModel(): Task {
  val tagList = if (tagsJson.isBlank()) emptyList() else tagsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
  val repeatDayList = if (repeatDaysJson.isBlank()) emptyList() else {
    repeatDaysJson.split(",").mapNotNull { name ->
      try { DayOfWeek.valueOf(name.trim()) } catch (e: Exception) { null }
    }
  }

  return Task(
    id = id,
    title = title,
    notes = notes,
    dueDate = dueDate,
    dueTime = dueTime,
    isCompleted = isCompleted,
    priority = TaskPriority.fromString(priority),
    category = category,
    tags = tagList,
    reminderSetting = ReminderOption.fromString(reminderSetting),
    customReminderMinutes = customReminderMinutes,
    repeatRule = RepeatRule.fromString(repeatRule),
    repeatDays = repeatDayList,
    isShortTerm = isShortTerm,
    creationTimestamp = creationTimestamp,
    updatedTimestamp = updatedTimestamp
  )
}

fun Task.toEntity(): TaskEntity {
  return TaskEntity(
    id = id,
    title = title,
    notes = notes,
    dueDate = dueDate,
    dueTime = dueTime,
    isCompleted = isCompleted,
    priority = priority.name,
    category = category,
    tagsJson = tags.joinToString(","),
    reminderSetting = reminderSetting.name,
    customReminderMinutes = customReminderMinutes,
    repeatRule = repeatRule.name,
    repeatDaysJson = repeatDays.joinToString(",") { it.name },
    isShortTerm = isShortTerm,
    creationTimestamp = creationTimestamp,
    updatedTimestamp = updatedTimestamp
  )
}

fun SubtaskEntity.toDomainModel(): Subtask {
  return Subtask(
    id = id,
    parentTaskId = parentTaskId,
    title = title,
    isCompleted = isCompleted,
    creationTimestamp = creationTimestamp
  )
}

fun Subtask.toEntity(): SubtaskEntity {
  return SubtaskEntity(
    id = id,
    parentTaskId = parentTaskId,
    title = title,
    isCompleted = isCompleted,
    creationTimestamp = creationTimestamp
  )
}

fun HabitEntity.toDomainModel(): Habit {
  val customDayList = if (customDaysJson.isBlank()) emptyList() else {
    customDaysJson.split(",").mapNotNull { name ->
      try { DayOfWeek.valueOf(name.trim()) } catch (e: Exception) { null }
    }
  }

  return Habit(
    id = id,
    name = name,
    description = description,
    category = category,
    frequency = HabitFrequency.fromString(frequency),
    customDays = customDayList,
    targetValue = targetValue,
    targetUnit = targetUnit,
    startDate = startDate,
    reminderTime = reminderTime,
    reminderEnabled = reminderEnabled,
    isActive = isActive,
    isPaused = isPaused,
    isArchived = isArchived,
    creationTimestamp = creationTimestamp,
    updatedTimestamp = updatedTimestamp
  )
}

fun Habit.toEntity(): HabitEntity {
  return HabitEntity(
    id = id,
    name = name,
    description = description,
    category = category,
    frequency = frequency.name,
    customDaysJson = customDays.joinToString(",") { it.name },
    targetValue = targetValue,
    targetUnit = targetUnit,
    startDate = startDate,
    reminderTime = reminderTime,
    reminderEnabled = reminderEnabled,
    isActive = isActive,
    isPaused = isPaused,
    isArchived = isArchived,
    creationTimestamp = creationTimestamp,
    updatedTimestamp = updatedTimestamp
  )
}

fun HabitCompletionRecordEntity.toDomainModel(): HabitCompletionRecord {
  return HabitCompletionRecord(
    id = id,
    habitId = habitId,
    date = date,
    isCompleted = isCompleted,
    currentValue = currentValue,
    targetValue = targetValue,
    timestamp = timestamp
  )
}

fun HabitCompletionRecord.toEntity(): HabitCompletionRecordEntity {
  return HabitCompletionRecordEntity(
    id = id,
    habitId = habitId,
    date = date,
    isCompleted = isCompleted,
    currentValue = currentValue,
    targetValue = targetValue,
    timestamp = timestamp
  )
}

fun SettingsEntity.toDomainModel(): AppSettings {
  return AppSettings(
    pushNotificationsEnabled = pushNotificationsEnabled,
    taskRemindersEnabled = taskRemindersEnabled,
    habitRemindersEnabled = habitRemindersEnabled,
    soundAlertsEnabled = soundAlertsEnabled,
    dailySummaryEnabled = dailySummaryEnabled,
    dailySummaryTime = dailySummaryTime,
    defaultReminderMinutesBefore = defaultReminderMinutesBefore,
    defaultHabitReminderTime = defaultHabitReminderTime,
    autoMarkTaskOnSubtasksDone = autoMarkTaskOnSubtasksDone,
    themeMode = themeMode,
    hasPromptedPermissions = hasPromptedPermissions,
    calendarDefaultView = calendarDefaultView,
    startOfWeek = startOfWeek,
    showCompletedTasksInCalendar = showCompletedTasksInCalendar,
    showCompletedHabitsInCalendar = showCompletedHabitsInCalendar,
    showOverdueInCalendar = showOverdueInCalendar,
    timeFormat24Hour = timeFormat24Hour
  )
}

fun AppSettings.toEntity(): SettingsEntity {
  return SettingsEntity(
    id = 1,
    pushNotificationsEnabled = pushNotificationsEnabled,
    taskRemindersEnabled = taskRemindersEnabled,
    habitRemindersEnabled = habitRemindersEnabled,
    soundAlertsEnabled = soundAlertsEnabled,
    dailySummaryEnabled = dailySummaryEnabled,
    dailySummaryTime = dailySummaryTime,
    defaultReminderMinutesBefore = defaultReminderMinutesBefore,
    defaultHabitReminderTime = defaultHabitReminderTime,
    autoMarkTaskOnSubtasksDone = autoMarkTaskOnSubtasksDone,
    themeMode = themeMode,
    hasPromptedPermissions = hasPromptedPermissions,
    calendarDefaultView = calendarDefaultView,
    startOfWeek = startOfWeek,
    showCompletedTasksInCalendar = showCompletedTasksInCalendar,
    showCompletedHabitsInCalendar = showCompletedHabitsInCalendar,
    showOverdueInCalendar = showOverdueInCalendar,
    timeFormat24Hour = timeFormat24Hour
  )
}
