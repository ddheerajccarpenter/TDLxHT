package com.example.ui.screens.calendar

import com.example.data.model.Habit
import com.example.data.model.HabitCompletionRecord
import com.example.data.model.HabitFrequency
import com.example.data.model.HabitStreakInfo
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.repository.HabitRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

enum class StatsTimePeriod(val displayName: String) {
  TODAY("Today"),
  THIS_WEEK("This Week"),
  THIS_MONTH("This Month"),
  ALL_TIME("All Time")
}

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

data class ProductivityOverview(
  val tasksCompleted: Int,
  val tasksTotal: Int,
  val habitsCompleted: Int,
  val habitsTotal: Int,
  val overallScore: Float, // 0..100
  val completionTrend: List<Pair<String, Float>> = emptyList() // Day name to completion rate
)

data class TaskStatistics(
  val tasksCreated: Int,
  val tasksCompleted: Int,
  val tasksOverdue: Int,
  val completionPercentage: Float?, // null if 0 tasks
  val completionByDay: List<Pair<String, Int>>, // Day label -> completed count
  val completionByCategory: Map<String, Pair<Int, Int>>, // Category -> (Completed, Total)
  val completionByPriority: Map<TaskPriority, Pair<Int, Int>>
)

data class HabitStatistics(
  val totalCompletions: Int,
  val scheduledDays: Int,
  val missedDays: Int,
  val completionPercentage: Float?, // null if 0 scheduled days
  val currentStreak: Int,
  val longestStreak: Int,
  val weeklyConsistencyPct: Float,
  val monthlyConsistencyPct: Float
)

data class FullProductivityReport(
  val period: StatsTimePeriod,
  val startDate: LocalDate,
  val endDate: LocalDate,
  val overview: ProductivityOverview,
  val taskStats: TaskStatistics,
  val habitStats: HabitStatistics,
  val completedTasksHistory: List<Task>,
  val hasEnoughData: Boolean
)

object ProductivityStatsHelper {

  fun calculateReport(
    tasks: List<Task>,
    habits: List<Habit>,
    records: List<HabitCompletionRecord>,
    period: StatsTimePeriod,
    today: LocalDate = LocalDate.now(),
    nowTime: LocalTime = LocalTime.now()
  ): FullProductivityReport {
    val (startDate, endDate) = getDateRange(period, today)
    val startStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val endStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

    // Filter tasks for period
    val periodTasks = when (period) {
      StatsTimePeriod.TODAY -> tasks.filter { it.dueDate == startStr }
      StatsTimePeriod.ALL_TIME -> tasks
      else -> tasks.filter { task ->
        try {
          val due = LocalDate.parse(task.dueDate, DateTimeFormatter.ISO_LOCAL_DATE)
          !due.isBefore(startDate) && !due.isAfter(endDate)
        } catch (e: Exception) { false }
      }
    }

    // Task stats
    val tasksCreated = periodTasks.size
    val tasksCompleted = periodTasks.count { it.isCompleted }
    val tasksOverdue = periodTasks.count { it.isOverdue(today, nowTime) }
    val taskCompletionPct = if (tasksCreated > 0) (tasksCompleted.toFloat() / tasksCreated.toFloat()) * 100f else null

    // Completion by category
    val completionByCategory = periodTasks.groupBy { it.category }.mapValues { (_, taskList) ->
      Pair(taskList.count { it.isCompleted }, taskList.size)
    }

    // Completion by priority
    val completionByPriority = periodTasks.groupBy { it.priority }.mapValues { (_, taskList) ->
      Pair(taskList.count { it.isCompleted }, taskList.size)
    }

    // Completion by day (last 7 days or days within period)
    val daysForTrend = when (period) {
      StatsTimePeriod.TODAY -> listOf(today)
      StatsTimePeriod.THIS_WEEK -> (0..6).map { startDate.plusDays(it.toLong()) }.filter { !it.isAfter(today) }
      StatsTimePeriod.THIS_MONTH -> (0..6).map { today.minusDays(6 - it.toLong()) }
      StatsTimePeriod.ALL_TIME -> (0..6).map { today.minusDays(6 - it.toLong()) }
    }

    val taskCompletionByDay = daysForTrend.map { date ->
      val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
      val label = date.format(DateTimeFormatter.ofPattern("EEE"))
      val count = tasks.count { it.dueDate == dateStr && it.isCompleted }
      Pair(label, count)
    }

    // Habit stats in period
    val activeHabits = habits.filter { !it.isArchived }
    var totalScheduledHabitDays = 0
    var totalHabitCompletions = 0
    var missedScheduledDays = 0

    val completedRecordsMap = records.filter { it.isCompleted }.associateBy { "${it.habitId}_${it.date}" }

    // Iterate through dates in range up to today
    var cur = startDate
    val effectiveEnd = if (endDate.isAfter(today)) today else endDate
    while (!cur.isAfter(effectiveEnd)) {
      val dateStr = cur.format(DateTimeFormatter.ISO_LOCAL_DATE)
      for (habit in activeHabits) {
        if (habit.isScheduledOn(cur)) {
          totalScheduledHabitDays++
          val key = "${habit.id}_$dateStr"
          if (completedRecordsMap.containsKey(key)) {
            totalHabitCompletions++
          } else {
            missedScheduledDays++
          }
        }
      }
      cur = cur.plusDays(1)
    }

    val habitCompletionPct = if (totalScheduledHabitDays > 0) {
      (totalHabitCompletions.toFloat() / totalScheduledHabitDays.toFloat()) * 100f
    } else null

    // Habit streaks
    var maxCurrentStreak = 0
    var maxLongestStreak = 0
    activeHabits.forEach { habit ->
      val habitRecs = records.filter { it.habitId == habit.id }
      val streak = HabitRepository.calculateStreak(habit, habitRecs, today)
      if (streak.currentStreak > maxCurrentStreak) maxCurrentStreak = streak.currentStreak
      if (streak.longestStreak > maxLongestStreak) maxLongestStreak = streak.longestStreak
    }

    // Weekly consistency (past 7 days)
    var weekScheduled = 0
    var weekCompleted = 0
    for (i in 0..6) {
      val d = today.minusDays(i.toLong())
      val dStr = d.format(DateTimeFormatter.ISO_LOCAL_DATE)
      for (h in activeHabits) {
        if (h.isScheduledOn(d)) {
          weekScheduled++
          if (completedRecordsMap.containsKey("${h.id}_$dStr")) weekCompleted++
        }
      }
    }
    val weeklyConsistency = if (weekScheduled > 0) (weekCompleted.toFloat() / weekScheduled.toFloat()) * 100f else 0f

    // Monthly consistency (past 30 days)
    var monthScheduled = 0
    var monthCompleted = 0
    for (i in 0..29) {
      val d = today.minusDays(i.toLong())
      val dStr = d.format(DateTimeFormatter.ISO_LOCAL_DATE)
      for (h in activeHabits) {
        if (h.isScheduledOn(d)) {
          monthScheduled++
          if (completedRecordsMap.containsKey("${h.id}_$dStr")) monthCompleted++
        }
      }
    }
    val monthlyConsistency = if (monthScheduled > 0) (monthCompleted.toFloat() / monthScheduled.toFloat()) * 100f else 0f

    // Overall Overview
    val totalItems = tasksCreated + totalScheduledHabitDays
    val totalDone = tasksCompleted + totalHabitCompletions
    val overallScore = if (totalItems > 0) (totalDone.toFloat() / totalItems.toFloat()) * 100f else 0f

    // Completion trend (days rate)
    val trend = daysForTrend.map { date ->
      val dStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
      val dayLabel = date.format(DateTimeFormatter.ofPattern("EEE"))

      val dayTasks = tasks.filter { it.dueDate == dStr }
      val dTaskDone = dayTasks.count { it.isCompleted }
      val dTaskTotal = dayTasks.size

      var dHabitDone = 0
      var dHabitTotal = 0
      for (h in activeHabits) {
        if (h.isScheduledOn(date)) {
          dHabitTotal++
          if (completedRecordsMap.containsKey("${h.id}_$dStr")) dHabitDone++
        }
      }

      val allCount = dTaskTotal + dHabitTotal
      val allDone = dTaskDone + dHabitDone
      val rate = if (allCount > 0) allDone.toFloat() / allCount.toFloat() else 0f
      Pair(dayLabel, rate)
    }

    val completedTasksHistory = tasks.filter { it.isCompleted }
      .sortedByDescending { it.updatedTimestamp }

    val hasEnoughData = tasks.isNotEmpty() || records.isNotEmpty()

    return FullProductivityReport(
      period = period,
      startDate = startDate,
      endDate = endDate,
      overview = ProductivityOverview(
        tasksCompleted = tasksCompleted,
        tasksTotal = tasksCreated,
        habitsCompleted = totalHabitCompletions,
        habitsTotal = totalScheduledHabitDays,
        overallScore = overallScore,
        completionTrend = trend
      ),
      taskStats = TaskStatistics(
        tasksCreated = tasksCreated,
        tasksCompleted = tasksCompleted,
        tasksOverdue = tasksOverdue,
        completionPercentage = taskCompletionPct,
        completionByDay = taskCompletionByDay,
        completionByCategory = completionByCategory,
        completionByPriority = completionByPriority
      ),
      habitStats = HabitStatistics(
        totalCompletions = totalHabitCompletions,
        scheduledDays = totalScheduledHabitDays,
        missedDays = missedScheduledDays,
        completionPercentage = habitCompletionPct,
        currentStreak = maxCurrentStreak,
        longestStreak = maxLongestStreak,
        weeklyConsistencyPct = weeklyConsistency,
        monthlyConsistencyPct = monthlyConsistency
      ),
      completedTasksHistory = completedTasksHistory,
      hasEnoughData = hasEnoughData
    )
  }

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
    } catch (e: Exception) { today }

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

  private fun getDateRange(period: StatsTimePeriod, today: LocalDate): Pair<LocalDate, LocalDate> {
    return when (period) {
      StatsTimePeriod.TODAY -> Pair(today, today)
      StatsTimePeriod.THIS_WEEK -> {
        val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        Pair(start, end)
      }
      StatsTimePeriod.THIS_MONTH -> {
        val start = today.with(TemporalAdjusters.firstDayOfMonth())
        val end = today.with(TemporalAdjusters.lastDayOfMonth())
        Pair(start, end)
      }
      StatsTimePeriod.ALL_TIME -> {
        val start = today.minusYears(2)
        Pair(start, today)
      }
    }
  }
}
