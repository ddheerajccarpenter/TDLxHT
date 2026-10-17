package com.example.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class ExportedTask(
    val id: String,
    val title: String,
    val notes: String,
    val dueDate: String,
    val dueTime: String,
    val isCompleted: Boolean,
    val priority: String,
    val category: String,
    val tags: List<String>,
    val repeatRule: String,
    val creationTimestamp: Long,
    val updatedTimestamp: Long
)

@Serializable
data class ExportedSubtask(
    val id: String,
    val parentTaskId: String,
    val title: String,
    val isCompleted: Boolean,
    val creationTimestamp: Long
)

@Serializable
data class ExportedHabit(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val frequency: String,
    val targetValue: Float,
    val targetUnit: String,
    val startDate: String,
    val isActive: Boolean,
    val isArchived: Boolean,
    val creationTimestamp: Long
)

@Serializable
data class ExportedHabitCompletion(
    val habitId: String,
    val date: String,
    val isCompleted: Boolean,
    val currentValue: Float,
    val targetValue: Float
)

@Serializable
data class DataExport(
    val exportVersion: Int = 1,
    val exportedAt: String,
    val tasks: List<ExportedTask>,
    val subtasks: List<ExportedSubtask>,
    val habits: List<ExportedHabit>,
    val habitCompletions: List<ExportedHabitCompletion>
)

class DataExportService(private val db: AppDatabase) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    /**
     * Exports all user data to a JSON file and returns a shareable URI.
     * NEVER exports: passwords, auth tokens, API keys, or Supabase credentials.
     */
    suspend fun exportToJson(context: Context): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val tasks = db.taskDao().getAllTasksSync().map { e ->
                ExportedTask(
                    id = e.id,
                    title = e.title,
                    notes = e.notes,
                    dueDate = e.dueDate,
                    dueTime = e.dueTime,
                    isCompleted = e.isCompleted,
                    priority = e.priority,
                    category = e.category,
                    tags = e.tagsJson.split(",").filter { it.isNotBlank() },
                    repeatRule = e.repeatRule,
                    creationTimestamp = e.creationTimestamp,
                    updatedTimestamp = e.updatedTimestamp
                )
            }

            val subtasks = db.subtaskDao().getAllSubtasksSync().map { e ->
                ExportedSubtask(e.id, e.parentTaskId, e.title, e.isCompleted, e.creationTimestamp)
            }

            val habits = db.habitDao().getAllHabitsSync().map { e ->
                ExportedHabit(e.id, e.name, e.description, e.category, e.frequency,
                    e.targetValue, e.targetUnit, e.startDate, e.isActive, e.isArchived, e.creationTimestamp)
            }

            val completions = db.habitCompletionRecordDao().getAllRecordsSync().map { e ->
                ExportedHabitCompletion(e.habitId, e.date, e.isCompleted, e.currentValue, e.targetValue)
            }

            val export = DataExport(
                exportedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                tasks = tasks,
                subtasks = subtasks,
                habits = habits,
                habitCompletions = completions
            )

            val exportJson = json.encodeToString(export)
            val fileName = "tdlxht_export_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.json"
            val exportDir = File(context.cacheDir, "exports").also { it.mkdirs() }
            val exportFile = File(exportDir, fileName)
            exportFile.writeText(exportJson)

            // Return a content URI via FileProvider (safe sharing)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", exportFile)
        }
    }

    /**
     * Validates and imports data from a previously exported JSON file.
     * All imported records are assigned to the local database.
     * Does NOT trust any user_id embedded in the export file.
     */
    suspend fun importFromJson(context: Context, uri: Uri): Result<ImportSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                ?: error("Could not read import file")

            val export = json.decodeFromString<DataExport>(jsonText)

            if (export.exportVersion != 1) error("Unsupported export version: ${export.exportVersion}")
            if (export.tasks.size > 10_000) error("Import file too large (max 10,000 tasks)")
            if (export.habits.size > 1_000) error("Import file too large (max 1,000 habits)")

            // Validate and insert tasks
            var importedTasks = 0
            var importedHabits = 0
            var importedCompletions = 0

            export.tasks.filter { it.id.isNotBlank() && it.title.isNotBlank() }.forEach { t ->
                db.taskDao().upsertAll(listOf(
                    com.example.data.local.TaskEntity(
                        id = t.id,
                        title = t.title.take(500),
                        notes = t.notes.take(5000),
                        dueDate = t.dueDate,
                        dueTime = t.dueTime,
                        isCompleted = t.isCompleted,
                        priority = t.priority,
                        category = t.category,
                        tagsJson = t.tags.joinToString(","),
                        reminderSetting = "NONE",
                        customReminderMinutes = 15,
                        repeatRule = t.repeatRule,
                        repeatDaysJson = "",
                        creationTimestamp = t.creationTimestamp,
                        updatedTimestamp = t.updatedTimestamp
                    )
                ))
                importedTasks++
            }

            export.habits.filter { it.id.isNotBlank() && it.name.isNotBlank() }.forEach { h ->
                db.habitDao().upsertAll(listOf(
                    com.example.data.local.HabitEntity(
                        id = h.id,
                        name = h.name.take(500),
                        description = h.description.take(2000),
                        category = h.category,
                        frequency = h.frequency,
                        customDaysJson = "",
                        targetValue = h.targetValue,
                        targetUnit = h.targetUnit,
                        startDate = h.startDate,
                        reminderTime = "08:00",
                        reminderEnabled = false,
                        isActive = h.isActive,
                        isPaused = false,
                        isArchived = h.isArchived,
                        creationTimestamp = h.creationTimestamp,
                        updatedTimestamp = h.creationTimestamp
                    )
                ))
                importedHabits++
            }

            export.habitCompletions.filter { it.habitId.isNotBlank() }.forEach { c ->
                db.habitCompletionRecordDao().upsertAll(listOf(
                    com.example.data.local.HabitCompletionRecordEntity(
                        id = "${c.habitId}_${c.date}",
                        habitId = c.habitId,
                        date = c.date,
                        isCompleted = c.isCompleted,
                        currentValue = c.currentValue,
                        targetValue = c.targetValue,
                        timestamp = System.currentTimeMillis()
                    )
                ))
                importedCompletions++
            }

            ImportSummary(importedTasks, importedHabits, importedCompletions)
        }
    }

    data class ImportSummary(val tasks: Int, val habits: Int, val completions: Int)
}
