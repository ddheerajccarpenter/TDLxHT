package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Query("SELECT * FROM tasks ORDER BY creationTimestamp DESC")
  fun getAllTasks(): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks ORDER BY creationTimestamp DESC")
  suspend fun getAllTasksSync(): List<TaskEntity>

  @Query("SELECT * FROM tasks WHERE id = :id")
  fun getTaskById(id: String): Flow<TaskEntity?>

  @Query("SELECT * FROM tasks WHERE id = :id")
  suspend fun getTaskByIdSync(id: String): TaskEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTask(task: TaskEntity)

  @Update
  suspend fun updateTask(task: TaskEntity)

  @Query("DELETE FROM tasks WHERE id = :id")
  suspend fun deleteTaskById(id: String)

  @Query("DELETE FROM tasks WHERE id IN (:ids)")
  suspend fun deleteTasksByIds(ids: List<String>)

  @Query("DELETE FROM tasks")
  suspend fun clearAllTasks()

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(tasks: List<TaskEntity>)
}

@Dao
interface SubtaskDao {
  @Query("SELECT * FROM subtasks WHERE parentTaskId = :parentTaskId ORDER BY creationTimestamp ASC")
  fun getSubtasksForTask(parentTaskId: String): Flow<List<SubtaskEntity>>

  @Query("SELECT * FROM subtasks ORDER BY creationTimestamp ASC")
  fun getAllSubtasks(): Flow<List<SubtaskEntity>>

  @Query("SELECT * FROM subtasks WHERE parentTaskId = :parentTaskId")
  suspend fun getSubtasksForTaskSync(parentTaskId: String): List<SubtaskEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubtask(subtask: SubtaskEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

  @Update
  suspend fun updateSubtask(subtask: SubtaskEntity)

  @Query("DELETE FROM subtasks WHERE id = :id")
  suspend fun deleteSubtaskById(id: String)

  @Query("DELETE FROM subtasks WHERE parentTaskId = :parentTaskId")
  suspend fun deleteSubtasksForTask(parentTaskId: String)

  @Query("DELETE FROM subtasks WHERE parentTaskId IN (:parentTaskIds)")
  suspend fun deleteSubtasksForTasks(parentTaskIds: List<String>)

  @Query("DELETE FROM subtasks")
  suspend fun clearAllSubtasks()

  @Query("SELECT * FROM subtasks ORDER BY creationTimestamp ASC")
  suspend fun getAllSubtasksSync(): List<SubtaskEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(subtasks: List<SubtaskEntity>)
}

@Dao
interface HabitDao {
  @Query("SELECT * FROM habits ORDER BY creationTimestamp DESC")
  fun getAllHabits(): Flow<List<HabitEntity>>

  @Query("SELECT * FROM habits ORDER BY creationTimestamp DESC")
  suspend fun getAllHabitsSync(): List<HabitEntity>

  @Query("SELECT * FROM habits WHERE id = :id")
  fun getHabitById(id: String): Flow<HabitEntity?>

  @Query("SELECT * FROM habits WHERE id = :id")
  suspend fun getHabitByIdSync(id: String): HabitEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHabit(habit: HabitEntity)

  @Update
  suspend fun updateHabit(habit: HabitEntity)

  @Query("DELETE FROM habits WHERE id = :id")
  suspend fun deleteHabitById(id: String)

  @Query("DELETE FROM habits WHERE id IN (:ids)")
  suspend fun deleteHabitsByIds(ids: List<String>)

  @Query("DELETE FROM habits")
  suspend fun clearAllHabits()

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(habits: List<HabitEntity>)
}

@Dao
interface HabitCompletionRecordDao {
  @Query("SELECT * FROM habit_completion_records")
  fun getAllRecords(): Flow<List<HabitCompletionRecordEntity>>

  @Query("SELECT * FROM habit_completion_records WHERE habitId = :habitId ORDER BY date DESC")
  fun getRecordsForHabit(habitId: String): Flow<List<HabitCompletionRecordEntity>>

  @Query("SELECT * FROM habit_completion_records WHERE habitId = :habitId")
  suspend fun getRecordsForHabitSync(habitId: String): List<HabitCompletionRecordEntity>

  @Query("SELECT * FROM habit_completion_records WHERE habitId = :habitId AND date = :date LIMIT 1")
  suspend fun getRecordForHabitAndDate(habitId: String, date: String): HabitCompletionRecordEntity?

  @Query("SELECT * FROM habit_completion_records")
  suspend fun getAllRecordsSync(): List<HabitCompletionRecordEntity>

  @Query("SELECT * FROM habit_completion_records WHERE date = :date")
  fun getRecordsForDateSync(date: String): List<HabitCompletionRecordEntity>

  @Query("SELECT * FROM habit_completion_records WHERE date = :date")
  fun getRecordsForDate(date: String): Flow<List<HabitCompletionRecordEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateRecord(record: HabitCompletionRecordEntity)

  @Query("DELETE FROM habit_completion_records WHERE habitId = :habitId")
  suspend fun deleteRecordsForHabit(habitId: String)

  @Query("DELETE FROM habit_completion_records WHERE habitId IN (:habitIds)")
  suspend fun deleteRecordsForHabits(habitIds: List<String>)

  @Query("DELETE FROM habit_completion_records WHERE habitId = :habitId AND date = :date")
  suspend fun deleteRecordForHabitAndDate(habitId: String, date: String)

  @Query("DELETE FROM habit_completion_records")
  suspend fun clearAllRecords()

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(records: List<HabitCompletionRecordEntity>)
}

@Dao
interface SettingsDao {
  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  fun getSettings(): Flow<SettingsEntity?>

  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  suspend fun getSettingsSync(): SettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateSettings(settings: SettingsEntity)
}
