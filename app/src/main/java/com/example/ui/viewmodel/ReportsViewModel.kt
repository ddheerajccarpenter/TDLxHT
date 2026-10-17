package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.HabitTaskApplication
import com.example.data.model.Habit
import com.example.data.model.HabitCompletionRecord
import com.example.data.model.Task
import com.example.data.service.NapkinService
import com.example.data.service.NapkinStats
import com.example.data.service.NapkinVisualResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

enum class ReportPeriod { WEEKLY, MONTHLY }

data class ReportStats(
    val periodLabel: String = "",
    val tasksCompleted: Int = 0,
    val tasksTotal: Int = 0,
    val tasksOverdue: Int = 0,
    val habitsCompleted: Int = 0,
    val habitsTotal: Int = 0,
    val sessionsCompleted: Int = 0,
    val sessionsTotal: Int = 0,
    val longestStreak: Int = 0,
    val dailyTaskCompletion: List<Pair<String, Float>> = emptyList(),
    val categoryDistribution: Map<String, Int> = emptyMap(),
    val topCategories: List<String> = emptyList()
) {
    val taskCompletionRate: Float get() = if (tasksTotal > 0) tasksCompleted.toFloat() / tasksTotal else 0f
    val habitCompletionRate: Float get() = if (habitsTotal > 0) habitsCompleted.toFloat() / habitsTotal else 0f
    val sessionCompletionRate: Float get() = if (sessionsTotal > 0) sessionsCompleted.toFloat() / sessionsTotal else 0f
    val hasEnoughData: Boolean get() = tasksTotal > 0 || habitsTotal > 0 || sessionsTotal > 0
    val hasTasks: Boolean get() = tasksTotal > 0
    val hasHabits: Boolean get() = habitsTotal > 0
}

data class ReportsUiState(
    val period: ReportPeriod = ReportPeriod.WEEKLY,
    val weekOffset: Int = 0,
    val monthOffset: Int = 0,
    val stats: ReportStats = ReportStats(),
    val isGeneratingVisual: Boolean = false,
    val visualBitmap: android.graphics.Bitmap? = null,
    val visualResult: NapkinVisualResult? = null,
    val visualError: String? = null
)

class ReportsViewModel : ViewModel() {

    private val app = HabitTaskApplication.instance
    private val taskRepository = app.taskRepository
    private val habitRepository = app.habitRepository
    private val napkinService = NapkinService()
    private val napkinVisualService = com.example.data.service.NapkinVisualService()

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        combine(
            taskRepository.allTasks,
            habitRepository.allHabits,
            habitRepository.allRecords
        ) { tasks, habits, records ->
            recomputeStats(tasks, habits, records, _uiState.value)
        }.launchIn(viewModelScope)
    }

    private fun recomputeStats(
        tasks: List<Task>,
        habits: List<Habit>,
        records: List<HabitCompletionRecord>,
        state: ReportsUiState
    ) {
        val (startDate, endDate) = getPeriodRange(state.period, state.weekOffset, state.monthOffset)
        val periodLabel = formatPeriodLabel(state.period, startDate, endDate)

        // Tasks in period
        val periodTasks = tasks.filter { task ->
            try {
                val d = LocalDate.parse(task.dueDate)
                !d.isBefore(startDate) && !d.isAfter(endDate)
            } catch (e: Exception) { false }
        }

        val completedTasks = periodTasks.count { it.isCompleted }
        val overdueTasks = periodTasks.count { it.isOverdue() }
        val categoryDist = periodTasks.groupBy { it.category }.mapValues { it.value.size }
        val topCats = categoryDist.entries.sortedByDescending { it.value }.take(3).map { it.key }

        // Day-by-day task completion trend
        val dayRange = buildList {
            var d = startDate
            while (!d.isAfter(endDate)) { add(d); d = d.plusDays(1) }
        }
        val dailyTrend = dayRange.map { date ->
            val dayTasks = periodTasks.filter { task ->
                try { LocalDate.parse(task.dueDate) == date } catch (e: Exception) { false }
            }
            val rate = if (dayTasks.isEmpty()) -1f else dayTasks.count { it.isCompleted }.toFloat() / dayTasks.size
            date.format(DateTimeFormatter.ofPattern("MM/dd")) to rate
        }

        // Habit scheduling in period
        val activeHabits = habits.filter { it.isActive && !it.isArchived }
        val periodRecords = records.filter { rec ->
            try {
                val d = LocalDate.parse(rec.date)
                !d.isBefore(startDate) && !d.isAfter(endDate)
            } catch (e: Exception) { false }
        }

        var scheduledCount = 0
        var completedHabitCount = 0
        var scheduledSessions = 0
        var completedSessions = 0

        dayRange.forEach { date ->
            val dateStr = date.toString()
            activeHabits.forEach { habit ->
                if (habit.isScheduledOn(date)) {
                    scheduledCount++
                    val targetSessions = maxOf(1, habit.targetValue.toInt())
                    scheduledSessions += targetSessions

                    val rec = periodRecords.find { it.habitId == habit.id && it.date == dateStr }
                    if (rec?.isCompleted == true) completedHabitCount++

                    val logged = rec?.currentValue?.toInt() ?: (if (rec?.isCompleted == true) targetSessions else 0)
                    completedSessions += logged.coerceIn(0, targetSessions)
                }
            }
        }

        // Longest streak
        val today = LocalDate.now()
        val maxStreak = activeHabits.maxOfOrNull { habit ->
            val habitRecords = records.filter { it.habitId == habit.id }
            calculateStreak(habit, habitRecords, today)
        } ?: 0

        _uiState.value = state.copy(
            stats = ReportStats(
                periodLabel = periodLabel,
                tasksCompleted = completedTasks,
                tasksTotal = periodTasks.size,
                tasksOverdue = overdueTasks,
                habitsCompleted = completedHabitCount,
                habitsTotal = scheduledCount,
                sessionsCompleted = completedSessions,
                sessionsTotal = scheduledSessions,
                longestStreak = maxStreak,
                dailyTaskCompletion = dailyTrend,
                categoryDistribution = categoryDist,
                topCategories = topCats
            )
        )
    }

    private fun calculateStreak(habit: Habit, records: List<HabitCompletionRecord>, today: LocalDate): Int {
        var streak = 0
        var date = today
        while (true) {
            if (!habit.isScheduledOn(date)) { date = date.minusDays(1); continue }
            val rec = records.find { it.date == date.toString() }
            if (rec?.isCompleted == true) { streak++; date = date.minusDays(1) } else break
        }
        return streak
    }

    fun setPeriod(period: ReportPeriod) {
        _uiState.value = _uiState.value.copy(period = period, weekOffset = 0, monthOffset = 0, visualResult = null)
        refreshStats()
    }

    fun navigatePrevious() {
        val s = _uiState.value
        _uiState.value = if (s.period == ReportPeriod.WEEKLY) s.copy(weekOffset = s.weekOffset - 1, visualResult = null)
        else s.copy(monthOffset = s.monthOffset - 1, visualResult = null)
        refreshStats()
    }

    fun navigateNext() {
        val s = _uiState.value
        if (s.period == ReportPeriod.WEEKLY && s.weekOffset >= 0) return
        if (s.period == ReportPeriod.MONTHLY && s.monthOffset >= 0) return
        _uiState.value = if (s.period == ReportPeriod.WEEKLY) s.copy(weekOffset = s.weekOffset + 1, visualResult = null)
        else s.copy(monthOffset = s.monthOffset + 1, visualResult = null)
        refreshStats()
    }

    private fun refreshStats() {
        viewModelScope.launch {
            // Collect a single snapshot from the repositories' flows
            combine(
                taskRepository.allTasks,
                habitRepository.allHabits,
                habitRepository.allRecords
            ) { tasks, habits, records -> Triple(tasks, habits, records) }
                .collect { (tasks, habits, records) ->
                    recomputeStats(tasks, habits, records, _uiState.value)
                    // Single shot — end collect
                    throw CancellationException("snapshot done")
                }
        }
    }

    fun generateNapkinVisual(forceRegenerate: Boolean = false) {
        val stats = _uiState.value.stats
        if (!stats.hasEnoughData) {
            _uiState.value = _uiState.value.copy(visualError = "Not enough data to generate a visual report.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingVisual = true, visualError = null)
            val period = if (_uiState.value.period == ReportPeriod.WEEKLY) "weekly" else "monthly"
            val napkinStats = NapkinStats(
                tasksCompleted = stats.tasksCompleted,
                tasksTotal = stats.tasksTotal,
                habitsCompleted = stats.habitsCompleted,
                habitsTotal = stats.habitsTotal,
                longestStreak = stats.longestStreak,
                completionRate = stats.taskCompletionRate.toDouble(),
                topCategories = stats.topCategories,
                periodLabel = stats.periodLabel
            )
            val prompt = "Productivity report for ${stats.periodLabel} with ${stats.tasksCompleted} of ${stats.tasksTotal} tasks done and ${stats.habitsCompleted} of ${stats.habitsTotal} habit targets completed."
            val graphResult = napkinVisualService.generateVisualGraph(
                contentPrompt = prompt,
                period = period,
                stats = napkinStats
            )

            graphResult.fold(
                onSuccess = { bitmap ->
                    _uiState.value = _uiState.value.copy(
                        isGeneratingVisual = false,
                        visualBitmap = bitmap,
                        visualError = null
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isGeneratingVisual = false,
                        visualError = "Visual generation unavailable. Showing statistics only."
                    )
                }
            )
        }
    }

    private fun getPeriodRange(period: ReportPeriod, weekOffset: Int, monthOffset: Int): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return if (period == ReportPeriod.WEEKLY) {
            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(weekOffset.toLong())
            monday to monday.plusDays(6)
        } else {
            val firstOfMonth = today.withDayOfMonth(1).plusMonths(monthOffset.toLong())
            firstOfMonth to firstOfMonth.with(TemporalAdjusters.lastDayOfMonth())
        }
    }

    private fun formatPeriodLabel(period: ReportPeriod, start: LocalDate, end: LocalDate): String {
        val fmt = DateTimeFormatter.ofPattern("MMM d")
        return if (period == ReportPeriod.WEEKLY) "${start.format(fmt)} – ${end.format(fmt)}, ${end.year}"
        else start.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }
}

private class CancellationException(msg: String) : Exception(msg)
