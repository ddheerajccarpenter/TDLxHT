package com.example.data.service

import com.example.HabitTaskApplication
import com.example.data.local.AppDatabase
import com.example.data.local.HabitCompletionRecordEntity
import com.example.data.local.HabitEntity
import com.example.data.local.SubtaskEntity
import com.example.data.local.TaskEntity
import com.example.util.AppPreferences
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// DTOs — must exactly match Supabase column names (snake_case)
// ---------------------------------------------------------------------------

@Serializable
data class CloudAppSettings(
    @SerialName("user_id") val userId: String,
    @SerialName("theme_mode") val themeMode: String = "DARK",
    @SerialName("push_notifications_enabled") val pushNotificationsEnabled: Boolean = true,
    @SerialName("task_reminders_enabled") val taskRemindersEnabled: Boolean = true,
    @SerialName("habit_reminders_enabled") val habitRemindersEnabled: Boolean = true,
    @SerialName("daily_summary_enabled") val dailySummaryEnabled: Boolean = true,
    @SerialName("daily_summary_time") val dailySummaryTime: String = "20:00",
    @SerialName("default_reminder_minutes") val defaultReminderMinutes: Int = 15,
    @SerialName("calendar_default_view") val calendarDefaultView: String = "MONTH",
    @SerialName("start_of_week") val startOfWeek: String = "MONDAY",
    @SerialName("time_format_24_hour") val timeFormat24Hour: Boolean = false,
    @SerialName("favorite_tool_ids") val favoriteToolIds: List<String> = emptyList(),
    @SerialName("custom_tool_order") val customToolOrder: List<String> = emptyList(),
    @SerialName("today_tasks_view_mode") val todayTasksViewMode: String = "LIST",
    @SerialName("today_habits_view_mode") val todayHabitsViewMode: String = "LIST"
)

@Serializable
data class CloudTask(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val notes: String,
    @SerialName("due_date") val dueDate: String,
    @SerialName("due_time") val dueTime: String,
    @SerialName("is_completed") val isCompleted: Boolean,
    val priority: String,
    val category: String,
    val tags: List<String>,
    @SerialName("reminder_setting") val reminderSetting: String,
    @SerialName("custom_reminder_minutes") val customReminderMinutes: Int,
    @SerialName("repeat_rule") val repeatRule: String,
    @SerialName("repeat_days") val repeatDays: List<String>,
    @SerialName("creation_timestamp") val creationTimestamp: Long,
    @SerialName("updated_timestamp") val updatedTimestamp: Long
)

@Serializable
data class CloudSubtask(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("parent_task_id") val parentTaskId: String,
    val title: String,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("creation_timestamp") val creationTimestamp: Long
)

@Serializable
data class CloudHabit(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val description: String,
    val category: String,
    val frequency: String,
    @SerialName("custom_days") val customDays: List<String>,
    @SerialName("target_value") val targetValue: Float,
    @SerialName("target_unit") val targetUnit: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("reminder_time") val reminderTime: String,
    @SerialName("reminder_enabled") val reminderEnabled: Boolean,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("is_paused") val isPaused: Boolean,
    @SerialName("is_archived") val isArchived: Boolean,
    @SerialName("creation_timestamp") val creationTimestamp: Long,
    @SerialName("updated_timestamp") val updatedTimestamp: Long
)

@Serializable
data class CloudHabitCompletion(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("habit_id") val habitId: String,
    val date: String,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("current_value") val currentValue: Float,
    @SerialName("target_value") val targetValue: Float,
    val timestamp: Long
)

// ---------------------------------------------------------------------------
// SyncService — bidirectional sync between Room (local) and Supabase (cloud)
// ---------------------------------------------------------------------------

class SyncService(private val db: AppDatabase) {

    private val supabase = SupabaseClient.client
    private val auth get() = supabase.auth
    private val postgrest get() = supabase.postgrest

    private fun userId(): String? = auth.currentSessionOrNull()?.user?.id?.toString()

    // -----------------------------------------------------------------------
    // UPLOAD: push all local data to Supabase (upsert, idempotent)
    // -----------------------------------------------------------------------

    suspend fun uploadAll(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = userId() ?: error("Not signed in")
            uploadTasks(uid)
            uploadSubtasks(uid)
            uploadHabits(uid)
            uploadHabitCompletions(uid)
            runCatching { uploadSettings(uid) }
            Unit
        }
    }

    private suspend fun uploadTasks(uid: String) {
        val entities = db.taskDao().getAllTasksSync().filter { !it.isShortTerm }
        if (entities.isEmpty()) return
        val cloud = entities.map { e ->
            CloudTask(
                id = e.id,
                userId = uid,
                title = e.title,
                notes = e.notes,
                dueDate = e.dueDate,
                dueTime = e.dueTime,
                isCompleted = e.isCompleted,
                priority = e.priority,
                category = e.category,
                tags = e.tagsJson.split(",").filter { it.isNotBlank() },
                reminderSetting = e.reminderSetting,
                customReminderMinutes = e.customReminderMinutes,
                repeatRule = e.repeatRule,
                repeatDays = e.repeatDaysJson.split(",").filter { it.isNotBlank() },
                creationTimestamp = e.creationTimestamp,
                updatedTimestamp = e.updatedTimestamp
            )
        }
        postgrest["tasks"].upsert(cloud)
    }

    private suspend fun uploadSubtasks(uid: String) {
        val entities = db.subtaskDao().getAllSubtasksSync()
        if (entities.isEmpty()) return
        val cloud = entities.map { e ->
            CloudSubtask(
                id = e.id,
                userId = uid,
                parentTaskId = e.parentTaskId,
                title = e.title,
                isCompleted = e.isCompleted,
                creationTimestamp = e.creationTimestamp
            )
        }
        postgrest["subtasks"].upsert(cloud)
    }

    private suspend fun uploadHabits(uid: String) {
        val entities = db.habitDao().getAllHabitsSync()
        if (entities.isEmpty()) return
        val cloud = entities.map { e ->
            CloudHabit(
                id = e.id,
                userId = uid,
                name = e.name,
                description = e.description,
                category = e.category,
                frequency = e.frequency,
                customDays = e.customDaysJson.split(",").filter { it.isNotBlank() },
                targetValue = e.targetValue,
                targetUnit = e.targetUnit,
                startDate = e.startDate,
                reminderTime = e.reminderTime,
                reminderEnabled = e.reminderEnabled,
                isActive = e.isActive,
                isPaused = e.isPaused,
                isArchived = e.isArchived,
                creationTimestamp = e.creationTimestamp,
                updatedTimestamp = e.updatedTimestamp
            )
        }
        postgrest["habits"].upsert(cloud)
    }

    private suspend fun uploadHabitCompletions(uid: String) {
        val entities = db.habitCompletionRecordDao().getAllRecordsSync()
        if (entities.isEmpty()) return
        val cloud = entities.map { e ->
            CloudHabitCompletion(
                id = e.id,
                userId = uid,
                habitId = e.habitId,
                date = e.date,
                isCompleted = e.isCompleted,
                currentValue = e.currentValue,
                targetValue = e.targetValue,
                timestamp = e.timestamp
            )
        }
        postgrest["habit_completions"].upsert(cloud)
    }

    private suspend fun uploadSettings(uid: String) {
        val app = HabitTaskApplication.instance
        val settings = db.settingsDao().getSettingsSync() ?: com.example.data.local.SettingsEntity()
        val favTools = AppPreferences.getFavoriteToolIds(app).toList()
        val toolOrder = AppPreferences.getCustomToolOrder(app)
        val tasksViewMode = AppPreferences.getTodayTasksViewMode(app)
        val habitsViewMode = AppPreferences.getTodayHabitsViewMode(app)
        val prefTheme = AppPreferences.getThemeMode(app)
        val resolvedTheme = if (prefTheme.isNotBlank()) prefTheme else settings.themeMode

        val cloud = CloudAppSettings(
            userId = uid,
            themeMode = resolvedTheme,
            pushNotificationsEnabled = settings.pushNotificationsEnabled,
            taskRemindersEnabled = settings.taskRemindersEnabled,
            habitRemindersEnabled = settings.habitRemindersEnabled,
            dailySummaryEnabled = settings.dailySummaryEnabled,
            dailySummaryTime = settings.dailySummaryTime,
            defaultReminderMinutes = settings.defaultReminderMinutesBefore,
            calendarDefaultView = settings.calendarDefaultView,
            startOfWeek = settings.startOfWeek,
            timeFormat24Hour = settings.timeFormat24Hour,
            favoriteToolIds = favTools,
            customToolOrder = toolOrder,
            todayTasksViewMode = tasksViewMode,
            todayHabitsViewMode = habitsViewMode
        )
        postgrest["app_settings"].upsert(cloud)
    }

    // -----------------------------------------------------------------------
    // DOWNLOAD: pull all data from Supabase and merge into local Room DB
    // -----------------------------------------------------------------------

    suspend fun downloadAll(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = userId() ?: error("Not signed in")
            downloadTasks()
            downloadSubtasks()
            downloadHabits()
            downloadHabitCompletions()
            runCatching { downloadSettings(uid) }
            Unit
        }
    }

    private suspend fun downloadTasks() {
        val cloud = postgrest["tasks"].select(Columns.ALL).decodeList<CloudTask>()
        val entities = cloud.map { c ->
            TaskEntity(
                id = c.id,
                title = c.title,
                notes = c.notes,
                dueDate = c.dueDate,
                dueTime = c.dueTime,
                isCompleted = c.isCompleted,
                priority = c.priority,
                category = c.category,
                tagsJson = c.tags.joinToString(","),
                reminderSetting = c.reminderSetting,
                customReminderMinutes = c.customReminderMinutes,
                repeatRule = c.repeatRule,
                repeatDaysJson = c.repeatDays.joinToString(","),
                creationTimestamp = c.creationTimestamp,
                updatedTimestamp = c.updatedTimestamp
            )
        }
        if (entities.isNotEmpty()) db.taskDao().upsertAll(entities)
    }

    private suspend fun downloadSubtasks() {
        val cloud = postgrest["subtasks"].select(Columns.ALL).decodeList<CloudSubtask>()
        val entities = cloud.map { c ->
            SubtaskEntity(
                id = c.id,
                parentTaskId = c.parentTaskId,
                title = c.title,
                isCompleted = c.isCompleted,
                creationTimestamp = c.creationTimestamp
            )
        }
        if (entities.isNotEmpty()) db.subtaskDao().upsertAll(entities)
    }

    private suspend fun downloadHabits() {
        val cloud = postgrest["habits"].select(Columns.ALL).decodeList<CloudHabit>()
        val entities = cloud.map { c ->
            HabitEntity(
                id = c.id,
                name = c.name,
                description = c.description,
                category = c.category,
                frequency = c.frequency,
                customDaysJson = c.customDays.joinToString(","),
                targetValue = c.targetValue,
                targetUnit = c.targetUnit,
                startDate = c.startDate,
                reminderTime = c.reminderTime,
                reminderEnabled = c.reminderEnabled,
                isActive = c.isActive,
                isPaused = c.isPaused,
                isArchived = c.isArchived,
                creationTimestamp = c.creationTimestamp,
                updatedTimestamp = c.updatedTimestamp
            )
        }
        if (entities.isNotEmpty()) db.habitDao().upsertAll(entities)
    }

    private suspend fun downloadHabitCompletions() {
        val cloud = postgrest["habit_completions"].select(Columns.ALL).decodeList<CloudHabitCompletion>()
        val entities = cloud.map { c ->
            HabitCompletionRecordEntity(
                id = c.id,
                habitId = c.habitId,
                date = c.date,
                isCompleted = c.isCompleted,
                currentValue = c.currentValue,
                targetValue = c.targetValue,
                timestamp = c.timestamp
            )
        }
        if (entities.isNotEmpty()) db.habitCompletionRecordDao().upsertAll(entities)
    }

    private suspend fun downloadSettings(uid: String) {
        val list = postgrest["app_settings"].select {
            filter { eq("user_id", uid) }
        }.decodeList<CloudAppSettings>()
        val cloud = list.firstOrNull() ?: return

        val app = HabitTaskApplication.instance
        val existing = db.settingsDao().getSettingsSync() ?: com.example.data.local.SettingsEntity()
        val localTheme = AppPreferences.getThemeMode(app)
        val finalTheme = if (cloud.themeMode.isNotBlank() && cloud.themeMode != "SYSTEM") {
            cloud.themeMode
        } else if (localTheme != "SYSTEM") {
            localTheme
        } else {
            cloud.themeMode
        }
        AppPreferences.setThemeMode(app, finalTheme)

        val updated = existing.copy(
            themeMode = finalTheme,
            pushNotificationsEnabled = cloud.pushNotificationsEnabled,
            taskRemindersEnabled = cloud.taskRemindersEnabled,
            habitRemindersEnabled = cloud.habitRemindersEnabled,
            dailySummaryEnabled = cloud.dailySummaryEnabled,
            dailySummaryTime = cloud.dailySummaryTime,
            defaultReminderMinutesBefore = cloud.defaultReminderMinutes,
            calendarDefaultView = cloud.calendarDefaultView,
            startOfWeek = cloud.startOfWeek,
            timeFormat24Hour = cloud.timeFormat24Hour
        )
        db.settingsDao().insertOrUpdateSettings(updated)

        if (cloud.favoriteToolIds.isNotEmpty()) {
            AppPreferences.setFavoriteToolIds(app, cloud.favoriteToolIds.toSet())
        }
        if (cloud.customToolOrder.isNotEmpty()) {
            AppPreferences.setCustomToolOrder(app, cloud.customToolOrder)
        }
        if (cloud.todayTasksViewMode.isNotBlank()) {
            AppPreferences.setTodayTasksViewMode(app, cloud.todayTasksViewMode)
        }
        if (cloud.todayHabitsViewMode.isNotBlank()) {
            AppPreferences.setTodayHabitsViewMode(app, cloud.todayHabitsViewMode)
        }
    }

    companion object {
        suspend fun deleteCloudTasks(taskIds: List<String>) = withContext(Dispatchers.IO) {
            runCatching {
                if (taskIds.isEmpty()) return@runCatching
                val client = SupabaseClient.client
                val uid = client.auth.currentSessionOrNull()?.user?.id?.toString() ?: return@runCatching
                client.postgrest["tasks"].delete {
                    filter {
                        isIn("id", taskIds)
                        eq("user_id", uid)
                    }
                }
            }
        }

        suspend fun deleteCloudHabits(habitIds: List<String>) = withContext(Dispatchers.IO) {
            runCatching {
                if (habitIds.isEmpty()) return@runCatching
                val client = SupabaseClient.client
                val uid = client.auth.currentSessionOrNull()?.user?.id?.toString() ?: return@runCatching
                client.postgrest["habits"].delete {
                    filter {
                        isIn("id", habitIds)
                        eq("user_id", uid)
                    }
                }
            }
        }
    }
}
