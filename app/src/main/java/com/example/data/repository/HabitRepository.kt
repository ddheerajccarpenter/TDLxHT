package com.example.data.repository

import com.example.HabitTaskApplication
import com.example.data.local.HabitCompletionRecordDao
import com.example.data.local.HabitCompletionRecordEntity
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.local.toDomainModel
import com.example.data.local.toEntity
import com.example.data.model.Habit
import com.example.data.model.HabitCategory
import com.example.data.model.HabitCompletionRecord
import com.example.data.model.HabitFrequency
import com.example.data.model.HabitStreakInfo
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class HabitRepository(
  private val habitDao: HabitDao,
  private val recordDao: HabitCompletionRecordDao
) {

  val allHabits: Flow<List<Habit>> = habitDao.getAllHabits().map { entities ->
    entities.map { it.toDomainModel() }
  }

  val allRecords: Flow<List<HabitCompletionRecord>> = recordDao.getAllRecords().map { entities ->
    entities.map { it.toDomainModel() }
  }

  fun getHabitById(id: String): Flow<Habit?> {
    return habitDao.getHabitById(id).map { it?.toDomainModel() }
  }

  fun getRecordsForHabit(habitId: String): Flow<List<HabitCompletionRecord>> {
    return recordDao.getRecordsForHabit(habitId).map { entities ->
      entities.map { it.toDomainModel() }
    }
  }

  suspend fun insertHabit(habit: Habit) {
    habitDao.insertHabit(habit.toEntity())
    if (habit.reminderEnabled && habit.isActive && !habit.isPaused && !habit.isArchived) {
      ReminderScheduler.scheduleHabitReminders(
        context = HabitTaskApplication.instance,
        habit = habit
      )
    }
  }

  suspend fun updateHabit(habit: Habit) {
    val updated = habit.copy(updatedTimestamp = System.currentTimeMillis())
    habitDao.updateHabit(updated.toEntity())

    ReminderScheduler.cancelHabitReminder(HabitTaskApplication.instance, habit.id)
    if (updated.reminderEnabled && updated.isActive && !updated.isPaused && !updated.isArchived) {
      ReminderScheduler.scheduleHabitReminders(
        context = HabitTaskApplication.instance,
        habit = updated
      )
    }
  }

  suspend fun deleteHabit(habitId: String) {
    ReminderScheduler.cancelHabitReminder(HabitTaskApplication.instance, habitId)
    habitDao.deleteHabitById(habitId)
    recordDao.deleteRecordsForHabit(habitId)
    com.example.data.service.SyncService.deleteCloudHabits(listOf(habitId))
  }

  suspend fun deleteHabitsBatch(habitIds: List<String>) {
    if (habitIds.isEmpty()) return
    habitIds.forEach { ReminderScheduler.cancelHabitReminder(HabitTaskApplication.instance, it) }
    habitDao.deleteHabitsByIds(habitIds)
    recordDao.deleteRecordsForHabits(habitIds)
    com.example.data.service.SyncService.deleteCloudHabits(habitIds)
  }

  suspend fun togglePauseHabit(habitId: String) {
    val habit = habitDao.getHabitByIdSync(habitId)?.toDomainModel() ?: return
    val newPaused = !habit.isPaused
    val updated = habit.copy(isPaused = newPaused, updatedTimestamp = System.currentTimeMillis())
    habitDao.updateHabit(updated.toEntity())

    if (newPaused) {
      ReminderScheduler.cancelHabitReminder(HabitTaskApplication.instance, habitId)
    } else if (updated.reminderEnabled) {
      ReminderScheduler.scheduleHabitReminders(
        context = HabitTaskApplication.instance,
        habit = updated
      )
    }
  }

  suspend fun toggleArchiveHabit(habitId: String) {
    val habit = habitDao.getHabitByIdSync(habitId)?.toDomainModel() ?: return
    val newArchived = !habit.isArchived
    val updated = habit.copy(isArchived = newArchived, updatedTimestamp = System.currentTimeMillis())
    habitDao.updateHabit(updated.toEntity())

    if (newArchived) {
      ReminderScheduler.cancelHabitReminder(HabitTaskApplication.instance, habitId)
    } else if (updated.reminderEnabled && !updated.isPaused) {
      ReminderScheduler.scheduleHabitReminders(
        context = HabitTaskApplication.instance,
        habit = updated
      )
    }
  }

  suspend fun toggleHabitCompletionToday(habitId: String) {
    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val habit = habitDao.getHabitByIdSync(habitId)?.toDomainModel() ?: return
    val existing = recordDao.getRecordForHabitAndDate(habitId, today)

    if (habit.targetValue > 1f) {
      // Multi-session habit logic (e.g. 2 sessions daily: 0/2 -> 1/2 -> 2/2 [done] -> reset 0/2)
      if (existing == null) {
        val isDone = 1f >= habit.targetValue
        val newRecord = HabitCompletionRecordEntity(
          id = UUID.randomUUID().toString(),
          habitId = habitId,
          date = today,
          isCompleted = isDone,
          currentValue = 1f,
          targetValue = habit.targetValue,
          timestamp = System.currentTimeMillis()
        )
        recordDao.insertOrUpdateRecord(newRecord)
      } else if (existing.isCompleted) {
        // When habit is already fully completed, tap resets back to 0 for replay/undo
        val updated = existing.copy(
          isCompleted = false,
          currentValue = 0f,
          timestamp = System.currentTimeMillis()
        )
        recordDao.insertOrUpdateRecord(updated)
      } else {
        // Increment session count
        val nextValue = existing.currentValue + 1f
        val isDone = nextValue >= habit.targetValue
        val updated = existing.copy(
          isCompleted = isDone,
          currentValue = nextValue,
          targetValue = habit.targetValue,
          timestamp = System.currentTimeMillis()
        )
        recordDao.insertOrUpdateRecord(updated)
      }
    } else {
      // Single session habit (targetValue <= 1f)
      if (existing != null) {
        val newCompleted = !existing.isCompleted
        val updated = existing.copy(
          isCompleted = newCompleted,
          currentValue = if (newCompleted) habit.targetValue else 0f,
          timestamp = System.currentTimeMillis()
        )
        recordDao.insertOrUpdateRecord(updated)
      } else {
        val newRecord = HabitCompletionRecordEntity(
          id = UUID.randomUUID().toString(),
          habitId = habitId,
          date = today,
          isCompleted = true,
          currentValue = habit.targetValue,
          targetValue = habit.targetValue,
          timestamp = System.currentTimeMillis()
        )
        recordDao.insertOrUpdateRecord(newRecord)
      }
    }
  }

  suspend fun resetHabitToday(habitId: String) {
    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val existing = recordDao.getRecordForHabitAndDate(habitId, today) ?: return
    val updated = existing.copy(
      isCompleted = false,
      currentValue = 0f,
      timestamp = System.currentTimeMillis()
    )
    recordDao.insertOrUpdateRecord(updated)
  }

  suspend fun recordHabitProgress(habitId: String, date: String, value: Float) {
    val habit = habitDao.getHabitByIdSync(habitId)?.toDomainModel() ?: return
    val existing = recordDao.getRecordForHabitAndDate(habitId, date)
    val isCompleted = value >= habit.targetValue

    if (existing != null) {
      val updated = existing.copy(
        isCompleted = isCompleted,
        currentValue = value,
        targetValue = habit.targetValue,
        timestamp = System.currentTimeMillis()
      )
      recordDao.insertOrUpdateRecord(updated)
    } else {
      val record = HabitCompletionRecordEntity(
        id = UUID.randomUUID().toString(),
        habitId = habitId,
        date = date,
        isCompleted = isCompleted,
        currentValue = value,
        targetValue = habit.targetValue,
        timestamp = System.currentTimeMillis()
      )
      recordDao.insertOrUpdateRecord(record)
    }
  }

  companion object {
    fun calculateStreak(
      habit: Habit,
      records: List<HabitCompletionRecord>,
      referenceDate: LocalDate = LocalDate.now()
    ): HabitStreakInfo {
      val recordMap = records.associateBy { it.date }
      val startDate = try {
        LocalDate.parse(habit.startDate, DateTimeFormatter.ISO_LOCAL_DATE)
      } catch (e: Exception) {
        referenceDate
      }

      val effectiveStart = if (startDate.isAfter(referenceDate)) referenceDate else startDate

      var currentStreak = 0
      var longestStreak = 0
      var tempStreak = 0
      var scheduledDays = 0
      var totalCompletions = 0
      var missedScheduledDays = 0

      // Compute total completions and historical streaks by iterating forward from start date
      var d = effectiveStart
      while (!d.isAfter(referenceDate)) {
        val isScheduled = habit.isScheduledOn(d)
        if (isScheduled) {
          scheduledDays++
          val dateStr = d.format(DateTimeFormatter.ISO_LOCAL_DATE)
          val record = recordMap[dateStr]
          val completed = record?.isCompleted == true

          if (completed) {
            totalCompletions++
            tempStreak++
            if (tempStreak > longestStreak) {
              longestStreak = tempStreak
            }
          } else {
            // If d is before referenceDate, it's missed
            if (d.isBefore(referenceDate)) {
              missedScheduledDays++
            }
            tempStreak = 0
          }
        }
        d = d.plusDays(1)
      }

      // Calculate current streak backwards from referenceDate
      // If habit is paused, current streak is preserved from the last active streak
      var checkDate = referenceDate
      val todayStr = checkDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
      val todayScheduled = habit.isScheduledOn(checkDate)
      val todayRecord = recordMap[todayStr]

      var streakCount = 0
      if (todayScheduled && todayRecord?.isCompleted == true) {
        streakCount++
        checkDate = checkDate.minusDays(1)
      } else if (todayScheduled && (todayRecord == null || !todayRecord.isCompleted)) {
        // Today is scheduled but not yet completed. The streak from yesterday is still valid!
        checkDate = checkDate.minusDays(1)
      } else if (!todayScheduled) {
        // Today not scheduled, start looking from yesterday
        checkDate = checkDate.minusDays(1)
      }

      // Now traverse backwards until start date or a broken streak
      while (!checkDate.isBefore(effectiveStart)) {
        val isSched = habit.isScheduledOn(checkDate)
        if (isSched) {
          val dateStr = checkDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
          val rec = recordMap[dateStr]
          if (rec?.isCompleted == true) {
            streakCount++
          } else {
            // Broken streak
            break
          }
        }
        checkDate = checkDate.minusDays(1)
      }

      currentStreak = streakCount
      if (currentStreak > longestStreak) {
        longestStreak = currentStreak
      }

      val completionRate = if (scheduledDays > 0) {
        totalCompletions.toFloat() / scheduledDays.toFloat()
      } else {
        0.0f
      }

      return HabitStreakInfo(
        habitId = habit.id,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        totalCompletions = totalCompletions,
        completionRate = completionRate,
        scheduledDaysCount = scheduledDays,
        missedScheduledDaysCount = missedScheduledDays
      )
    }
  }
}
