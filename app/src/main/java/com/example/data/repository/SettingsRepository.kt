package com.example.data.repository

import com.example.data.local.SettingsDao
import com.example.data.local.SettingsEntity
import com.example.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingsDao: SettingsDao) {

  val settings: Flow<AppSettings> = settingsDao.getSettings().map { entity ->
    val app = com.example.HabitTaskApplication.instance
    val prefTheme = com.example.util.AppPreferences.getThemeMode(app)
    if (entity != null) {
      val effectiveTheme = if (entity.themeMode.isNotBlank() && entity.themeMode != "SYSTEM") {
        entity.themeMode
      } else if (prefTheme != "SYSTEM") {
        prefTheme
      } else {
        entity.themeMode
      }
      AppSettings(
        pushNotificationsEnabled = entity.pushNotificationsEnabled,
        taskRemindersEnabled = entity.taskRemindersEnabled,
        habitRemindersEnabled = entity.habitRemindersEnabled,
        soundAlertsEnabled = entity.soundAlertsEnabled,
        dailySummaryEnabled = entity.dailySummaryEnabled,
        dailySummaryTime = entity.dailySummaryTime,
        defaultReminderMinutesBefore = entity.defaultReminderMinutesBefore,
        defaultHabitReminderTime = entity.defaultHabitReminderTime,
        autoMarkTaskOnSubtasksDone = entity.autoMarkTaskOnSubtasksDone,
        themeMode = effectiveTheme,
        hasPromptedPermissions = entity.hasPromptedPermissions,
        calendarDefaultView = entity.calendarDefaultView,
        startOfWeek = entity.startOfWeek,
        showCompletedTasksInCalendar = entity.showCompletedTasksInCalendar,
        showCompletedHabitsInCalendar = entity.showCompletedHabitsInCalendar,
        showOverdueInCalendar = entity.showOverdueInCalendar,
        timeFormat24Hour = entity.timeFormat24Hour
      )
    } else {
      AppSettings(themeMode = prefTheme)
    }
  }

  suspend fun updateSettings(settings: AppSettings) {
    val app = com.example.HabitTaskApplication.instance
    com.example.util.AppPreferences.setThemeMode(app, settings.themeMode)
    settingsDao.insertOrUpdateSettings(
      SettingsEntity(
        id = 1,
        pushNotificationsEnabled = settings.pushNotificationsEnabled,
        taskRemindersEnabled = settings.taskRemindersEnabled,
        habitRemindersEnabled = settings.habitRemindersEnabled,
        soundAlertsEnabled = settings.soundAlertsEnabled,
        dailySummaryEnabled = settings.dailySummaryEnabled,
        dailySummaryTime = settings.dailySummaryTime,
        defaultReminderMinutesBefore = settings.defaultReminderMinutesBefore,
        defaultHabitReminderTime = settings.defaultHabitReminderTime,
        autoMarkTaskOnSubtasksDone = settings.autoMarkTaskOnSubtasksDone,
        themeMode = settings.themeMode,
        hasPromptedPermissions = settings.hasPromptedPermissions,
        calendarDefaultView = settings.calendarDefaultView,
        startOfWeek = settings.startOfWeek,
        showCompletedTasksInCalendar = settings.showCompletedTasksInCalendar,
        showCompletedHabitsInCalendar = settings.showCompletedHabitsInCalendar,
        showOverdueInCalendar = settings.showOverdueInCalendar,
        timeFormat24Hour = settings.timeFormat24Hour
      )
    )
  }
}
