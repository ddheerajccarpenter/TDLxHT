package com.example.ui.screens.calendar

import com.example.data.model.Habit
import com.example.data.model.HabitCompletionRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class HabitDayStatus {
  COMPLETED,
  MISSED,
  NOT_SCHEDULED,
  FUTURE
}

data class HabitDayHistoryItem(
  val date: LocalDate,
  val status: HabitDayStatus,
  val value: Float = 0f
)

object ProductivityStatsHelper {

  fun getHabitHistoryDays(
    habit: Habit,
    records: List<HabitCompletionRecord>,
    daysCount: Int = 30,
    today: LocalDate = LocalDate.now()
  ): List<HabitDayHistoryItem> {
    val recordsMap = records.filter { it.habitId == habit.id }
      .associateBy { it.date }

    val habitStart = try {
      LocalDate.parse(habit.startDate, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: Exception) { today }

    return (0 until daysCount).map { i ->
      val date = today.minusDays(i.toLong())
      val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
      val record = recordsMap[dateStr]

      val status = when {
        date.isAfter(today) -> HabitDayStatus.FUTURE
        date.isBefore(habitStart) -> HabitDayStatus.NOT_SCHEDULED
        !habit.isScheduledOn(date) -> HabitDayStatus.NOT_SCHEDULED
        record != null && record.isCompleted -> HabitDayStatus.COMPLETED
        else -> HabitDayStatus.MISSED
      }

      HabitDayHistoryItem(
        date = date,
        status = status,
        value = record?.currentValue ?: 0f
      )
    }
  }
}
