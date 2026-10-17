package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.HabitTaskApplication
import com.example.data.model.AppSettings
import com.example.data.model.Habit
import com.example.data.model.HabitCategory
import com.example.data.model.HabitCompletionRecord
import com.example.data.model.HabitFrequency
import com.example.data.model.HabitStreakInfo
import com.example.data.model.ReminderOption
import com.example.data.model.RepeatRule
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskFilter
import com.example.data.model.TaskPriority
import com.example.data.model.TaskSort
import com.example.data.repository.HabitRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.util.ErrorMapper
import com.example.util.SafeLog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class HabitListFilter(val displayName: String) {
  ALL("All"),
  DAILY("Daily"),
  WEEKLY("Weekly"),
  PAUSED("Paused"),
  ARCHIVED("Archived")
}

data class DailySummary(
  val tasksCompleted: Int = 0,
  val tasksTotal: Int = 0,
  val habitsCompleted: Int = 0,
  val habitsTotal: Int = 0,
  val sessionsCompleted: Int = 0,
  val sessionsTotal: Int = 0,
  val maxStreak: Int = 0,
  val overallProgress: Float = 0f
)

data class CalendarDayItem(
  val date: LocalDate,
  val hasTasks: Boolean,
  val hasHabits: Boolean,
  val allTasksCompleted: Boolean,
  val allHabitsCompleted: Boolean
)

class MainViewModel(
  private val taskRepository: TaskRepository,
  private val habitRepository: HabitRepository,
  private val settingsRepository: SettingsRepository
) : ViewModel() {

  // Primary data streams
  val tasks: StateFlow<List<Task>> = taskRepository.allTasks
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val subtasks: StateFlow<List<Subtask>> = taskRepository.allSubtasks
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val habits: StateFlow<List<Habit>> = habitRepository.allHabits
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val completionRecords: StateFlow<List<HabitCompletionRecord>> = habitRepository.allRecords
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todayHabitRecords: StateFlow<Map<String, HabitCompletionRecord>> = completionRecords.map { list ->
    val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    list.filter { it.date == todayStr }.associateBy { it.habitId }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  val subtasksByTaskId: StateFlow<Map<String, List<Subtask>>> = taskRepository.allSubtasks
    .map { list -> list.groupBy { it.parentTaskId } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  val habitStreaks: StateFlow<Map<String, HabitStreakInfo>> = combine(habits, completionRecords) { habitList, recordList ->
    val today = LocalDate.now()
    val recordMap = recordList.groupBy { it.habitId }
    habitList.associate { habit ->
      val recs = recordMap[habit.id].orEmpty()
      habit.id to HabitRepository.calculateStreak(habit, recs, today)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  val settings: StateFlow<AppSettings> = settingsRepository.settings
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      AppSettings(themeMode = com.example.util.AppPreferences.getThemeMode(com.example.HabitTaskApplication.instance))
    )

  // UI state filters
  private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
  val taskFilter: StateFlow<TaskFilter> = _taskFilter.asStateFlow()

  private val _taskSort = MutableStateFlow(TaskSort.DUE_DATE)
  val taskSort: StateFlow<TaskSort> = _taskSort.asStateFlow()

  private val _taskSearchQuery = MutableStateFlow("")
  val taskSearchQuery: StateFlow<String> = _taskSearchQuery.asStateFlow()

  private val _habitListFilter = MutableStateFlow(HabitListFilter.ALL)
  val habitListFilter: StateFlow<HabitListFilter> = _habitListFilter.asStateFlow()

  private val _habitSearchQuery = MutableStateFlow("")
  val habitSearchQuery: StateFlow<String> = _habitSearchQuery.asStateFlow()

  private val _selectedCalendarDate = MutableStateFlow(LocalDate.now())
  val selectedCalendarDate: StateFlow<LocalDate> = _selectedCalendarDate.asStateFlow()

  // Daily Summary for Home
  val dailySummary: StateFlow<DailySummary> = combine(
    tasks,
    habits,
    completionRecords
  ) { taskList, habitList, recordList ->
    val today = LocalDate.now()
    val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

    val todayTasks = taskList.filter { it.isDueToday(today) }
    val tasksCompleted = todayTasks.count { it.isCompleted }
    val tasksTotal = todayTasks.size

    val scheduledHabits = habitList.filter { it.isActive && !it.isArchived && !it.isPaused && it.isScheduledOn(today) }
    val habitsTotal = scheduledHabits.size

    val todayCompletedHabitIds = recordList
      .filter { it.date == todayStr && it.isCompleted }
      .map { it.habitId }
      .toSet()

    val habitsCompleted = scheduledHabits.count { todayCompletedHabitIds.contains(it.id) }

    val sessionsTotal = scheduledHabits.sumOf { maxOf(1, it.targetValue.toInt()) }
    val sessionsCompleted = scheduledHabits.sumOf { habit ->
      val rec = recordList.find { it.habitId == habit.id && it.date == todayStr }
      val target = maxOf(1, habit.targetValue.toInt())
      val cur = rec?.currentValue?.toInt() ?: (if (rec?.isCompleted == true) target else 0)
      cur.coerceIn(0, target)
    }

    // Streak calculation for top badge
    val maxStreak = habitList.filter { it.isActive && !it.isArchived }
      .maxOfOrNull { habit ->
        val habitRecs = recordList.filter { it.habitId == habit.id }
        HabitRepository.calculateStreak(habit, habitRecs, today).currentStreak
      } ?: 0

    val totalItems = tasksTotal + habitsTotal
    val doneItems = tasksCompleted + habitsCompleted
    val progress = if (totalItems > 0) doneItems.toFloat() / totalItems.toFloat() else 0f

    DailySummary(
      tasksCompleted = tasksCompleted,
      tasksTotal = tasksTotal,
      habitsCompleted = habitsCompleted,
      habitsTotal = habitsTotal,
      sessionsCompleted = sessionsCompleted,
      sessionsTotal = sessionsTotal,
      maxStreak = maxStreak,
      overallProgress = progress
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummary())

  // Filtered and Sorted Tasks
  val filteredTasks: StateFlow<List<Task>> = combine(
    tasks,
    taskFilter,
    taskSort,
    taskSearchQuery
  ) { taskList, filter, sort, query ->
    val today = LocalDate.now()
    val nowTime = LocalTime.now()

    val activeTasks = taskList.filter { !(it.isShortTerm && it.isCompleted) }
    var filtered = when (filter) {
      TaskFilter.ALL -> activeTasks
      TaskFilter.TODAY -> activeTasks.filter { it.isDueToday(today) }
      TaskFilter.UPCOMING -> activeTasks.filter {
        try {
          val due = LocalDate.parse(it.dueDate, DateTimeFormatter.ISO_LOCAL_DATE)
          due.isAfter(today) && !it.isCompleted
        } catch (e: Exception) { false }
      }
      TaskFilter.COMPLETED -> taskList.filter { it.isCompleted }
      TaskFilter.OVERDUE -> activeTasks.filter { it.isOverdue(today, nowTime) }
      TaskFilter.HIGH_PRIORITY -> activeTasks.filter { it.priority == TaskPriority.HIGH }
    }

    if (query.isNotBlank()) {
      val q = query.trim().lowercase()
      filtered = filtered.filter { task ->
        task.title.lowercase().contains(q) ||
        task.notes.lowercase().contains(q) ||
        task.category.lowercase().contains(q) ||
        task.tags.any { it.lowercase().contains(q) }
      }
    }

    when (sort) {
      TaskSort.DUE_DATE -> filtered.sortedBy { it.dueDate }
      TaskSort.DUE_TIME -> filtered.sortedWith(compareBy({ it.dueDate }, { it.dueTime }))
      TaskSort.PRIORITY -> filtered.sortedByDescending { it.priority.ordinal }
      TaskSort.CREATION_DATE -> filtered.sortedByDescending { it.creationTimestamp }
      TaskSort.RECENTLY_UPDATED -> filtered.sortedByDescending { it.updatedTimestamp }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Filtered Habits
  val filteredHabits: StateFlow<List<Habit>> = combine(
    habits,
    habitListFilter,
    habitSearchQuery
  ) { habitList, filter, query ->
    var filtered = when (filter) {
      HabitListFilter.ALL -> habitList.filter { !it.isArchived }
      HabitListFilter.DAILY -> habitList.filter { !it.isArchived && it.frequency == HabitFrequency.EVERY_DAY }
      HabitListFilter.WEEKLY -> habitList.filter { !it.isArchived && (it.frequency == HabitFrequency.WEEKLY || it.frequency == HabitFrequency.WEEKDAYS) }
      HabitListFilter.PAUSED -> habitList.filter { !it.isArchived && it.isPaused }
      HabitListFilter.ARCHIVED -> habitList.filter { it.isArchived }
    }

    if (query.isNotBlank()) {
      val q = query.trim().lowercase()
      filtered = filtered.filter { habit ->
        habit.name.lowercase().contains(q) ||
        habit.description.lowercase().contains(q) ||
        habit.category.lowercase().contains(q)
      }
    }

    filtered.sortedByDescending { it.creationTimestamp }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Dates with activity for Calendar markers
  val activityDates: StateFlow<Set<String>> = combine(tasks, completionRecords) { taskList, recordList ->
    val dates = mutableSetOf<String>()
    taskList.forEach { dates.add(it.dueDate) }
    recordList.filter { it.isCompleted }.forEach { dates.add(it.date) }
    dates
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

  fun setTaskFilter(filter: TaskFilter) {
    _taskFilter.value = filter
  }

  fun setTaskSort(sort: TaskSort) {
    _taskSort.value = sort
  }

  fun setTaskSearchQuery(query: String) {
    _taskSearchQuery.value = query
  }

  fun setHabitListFilter(filter: HabitListFilter) {
    _habitListFilter.value = filter
  }

  fun setHabitSearchQuery(query: String) {
    _habitSearchQuery.value = query
  }

  fun setSelectedCalendarDate(date: LocalDate) {
    _selectedCalendarDate.value = date
  }

  // --- Task Operations ---
  fun createTask(
    title: String,
    notes: String,
    dueDate: String,
    dueTime: String,
    priority: TaskPriority,
    category: String,
    tags: List<String>,
    reminderSetting: ReminderOption,
    customReminderMinutes: Int,
    repeatRule: RepeatRule,
    repeatDays: List<DayOfWeek>,
    subtaskTitles: List<String>,
    isShortTerm: Boolean = false
  ) {
    if (title.isBlank()) return
    viewModelScope.launch {
      val taskId = UUID.randomUUID().toString()
      val task = Task(
        id = taskId,
        title = title.trim(),
        notes = notes.trim(),
        dueDate = dueDate,
        dueTime = dueTime,
        isCompleted = false,
        priority = priority,
        category = category,
        tags = tags,
        reminderSetting = reminderSetting,
        customReminderMinutes = customReminderMinutes,
        repeatRule = repeatRule,
        repeatDays = repeatDays,
        isShortTerm = isShortTerm,
        creationTimestamp = System.currentTimeMillis(),
        updatedTimestamp = System.currentTimeMillis()
      )
      val subtasks = subtaskTitles.filter { it.isNotBlank() }.map { subTitle ->
        Subtask(
          id = UUID.randomUUID().toString(),
          parentTaskId = taskId,
          title = subTitle.trim(),
          isCompleted = false,
          creationTimestamp = System.currentTimeMillis()
        )
      }
      taskRepository.insertTask(task, subtasks)
    }
  }

  fun updateTask(
    task: Task,
    subtasks: List<Subtask>
  ) {
    if (task.title.isBlank()) return
    viewModelScope.launch {
      taskRepository.updateTask(task, subtasks)
    }
  }

  fun toggleTaskCompletion(taskId: String) {
    viewModelScope.launch {
      taskRepository.toggleTaskCompletion(taskId)
    }
  }

  fun deleteTask(taskId: String) {
    viewModelScope.launch {
      taskRepository.deleteTask(taskId)
    }
  }

  fun deleteTasksBatch(taskIds: List<String>) {
    if (taskIds.isEmpty()) return
    viewModelScope.launch {
      taskRepository.deleteTasksBatch(taskIds)
    }
  }

  fun toggleSubtask(subtask: Subtask) {
    viewModelScope.launch {
      val autoMark = settings.value.autoMarkTaskOnSubtasksDone
      taskRepository.toggleSubtask(subtask, autoMark)
    }
  }

  fun addSubtask(parentTaskId: String, title: String) {
    if (title.isBlank()) return
    viewModelScope.launch {
      taskRepository.insertSubtask(
        Subtask(
          id = UUID.randomUUID().toString(),
          parentTaskId = parentTaskId,
          title = title.trim(),
          isCompleted = false,
          creationTimestamp = System.currentTimeMillis()
        )
      )
    }
  }

  fun deleteSubtask(subtaskId: String) {
    viewModelScope.launch {
      taskRepository.deleteSubtask(subtaskId)
    }
  }

  // --- Habit Operations ---
  fun createHabit(
    name: String,
    description: String,
    category: String,
    frequency: HabitFrequency,
    customDays: List<DayOfWeek>,
    targetValue: Float,
    targetUnit: String,
    startDate: String,
    reminderTime: String,
    reminderEnabled: Boolean
  ) {
    if (name.isBlank()) return
    viewModelScope.launch {
      val habit = Habit(
        id = UUID.randomUUID().toString(),
        name = name.trim(),
        description = description.trim(),
        category = category,
        frequency = frequency,
        customDays = customDays,
        targetValue = targetValue,
        targetUnit = targetUnit,
        startDate = startDate,
        reminderTime = reminderTime,
        reminderEnabled = reminderEnabled,
        isActive = true,
        isPaused = false,
        isArchived = false,
        creationTimestamp = System.currentTimeMillis(),
        updatedTimestamp = System.currentTimeMillis()
      )
      habitRepository.insertHabit(habit)
    }
  }

  fun updateHabit(habit: Habit) {
    if (habit.name.isBlank()) return
    viewModelScope.launch {
      habitRepository.updateHabit(habit)
    }
  }

  fun toggleHabitToday(habitId: String) {
    viewModelScope.launch {
      habitRepository.toggleHabitCompletionToday(habitId)
    }
  }

  fun recordHabitProgress(habitId: String, date: String, value: Float) {
    viewModelScope.launch {
      habitRepository.recordHabitProgress(habitId, date, value)
    }
  }

  fun togglePauseHabit(habitId: String) {
    viewModelScope.launch {
      habitRepository.togglePauseHabit(habitId)
    }
  }

  fun toggleArchiveHabit(habitId: String) {
    viewModelScope.launch {
      habitRepository.toggleArchiveHabit(habitId)
    }
  }

  fun deleteHabit(habitId: String) {
    viewModelScope.launch {
      habitRepository.deleteHabit(habitId)
    }
  }

  fun deleteHabitsBatch(habitIds: List<String>) {
    if (habitIds.isEmpty()) return
    viewModelScope.launch {
      habitRepository.deleteHabitsBatch(habitIds)
    }
  }

  // --- Settings ---
  fun updateSettings(newSettings: AppSettings) {
    viewModelScope.launch {
      settingsRepository.updateSettings(newSettings)
    }
  }

  // Streak calculation helper for composables - O(1) lookup
  fun getHabitStreak(habit: Habit): HabitStreakInfo {
    return habitStreaks.value[habit.id] ?: HabitRepository.calculateStreak(habit, emptyList(), LocalDate.now())
  }

  // Helper for subtasks of a specific task - O(1) lookup
  fun getSubtasksForTask(taskId: String): List<Subtask> {
    return subtasksByTaskId.value[taskId] ?: emptyList()
  }

  // --- Napkin AI Visual Graphs ---
  private val napkinVisualService = com.example.data.service.NapkinVisualService()

  private val _napkinGraphBitmap = MutableStateFlow<android.graphics.Bitmap?>(null)
  val napkinGraphBitmap: StateFlow<android.graphics.Bitmap?> = _napkinGraphBitmap.asStateFlow()

  private val _isGeneratingGraph = MutableStateFlow(false)
  val isGeneratingGraph: StateFlow<Boolean> = _isGeneratingGraph.asStateFlow()

  private val _graphGenerationMessage = MutableStateFlow<String?>(null)
  val graphGenerationMessage: StateFlow<String?> = _graphGenerationMessage.asStateFlow()

  fun generateNapkinGraph(period: String = "weekly") {
    viewModelScope.launch {
      val currentTasks = tasks.value
      val currentHabits = habits.value

      if (currentTasks.isEmpty() && currentHabits.isEmpty()) {
        _graphGenerationMessage.value = "Create at least one task or habit to generate your visual progress chart! (Zero mock data)"
        return@launch
      }

      _isGeneratingGraph.value = true
      _graphGenerationMessage.value = "Analyzing real activity & generating graph with Napkin AI..."

      val today = LocalDate.now()
      val startDate = if (period.lowercase() == "monthly") today.minusDays(30) else today.minusDays(7)
      val startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

      val periodTasks = currentTasks.filter { it.dueDate >= startDateStr }
      val completedCount = periodTasks.count { it.isCompleted }

      val habitLines = currentHabits.take(5).joinToString("\n") { habit ->
        val streak = getHabitStreak(habit)
        "- ${habit.name}: ${streak.currentStreak} day streak (${(streak.completionRate * 100).toInt()}% success)"
      }

      val prompt = buildString {
        appendLine("TDlxHT ${period.replaceFirstChar { it.uppercase() }} Habit & Task Progress Report:")
        appendLine("• Tasks Completed: $completedCount of ${periodTasks.size.coerceAtLeast(1)}")
        if (habitLines.isNotBlank()) {
          appendLine("• Active Habits:")
          appendLine(habitLines)
        }
        appendLine("• Consistency Goal: Maintain streak momentum and task clarity.")
      }

      val maxStreak = currentHabits.maxOfOrNull { getHabitStreak(it).longestStreak } ?: 0
      val topCategories = currentTasks.map { it.category }.distinct().take(3)
      val stats = com.example.data.service.NapkinStats(
        tasksCompleted = completedCount,
        tasksTotal = periodTasks.size.coerceAtLeast(1),
        habitsCompleted = currentHabits.sumOf { getHabitStreak(it).currentStreak },
        habitsTotal = currentHabits.size,
        longestStreak = maxStreak,
        completionRate = if (periodTasks.isNotEmpty()) completedCount.toDouble() / periodTasks.size else 0.0,
        topCategories = topCategories,
        periodLabel = if (period.equals("monthly", ignoreCase = true)) "Last 30 Days" else "Last 7 Days"
      )

      val result = napkinVisualService.generateVisualGraph(
        contentPrompt = prompt,
        period = period,
        stats = stats
      )
      if (result.isSuccess) {
        _napkinGraphBitmap.value = result.getOrNull()
        _graphGenerationMessage.value = null
      } else {
        SafeLog.w("MainViewModel", "Napkin generation failed", result.exceptionOrNull())
        _graphGenerationMessage.value = ErrorMapper.mapNapkin(result.exceptionOrNull()).title
      }
      _isGeneratingGraph.value = false
    }
  }

  fun dismissGraph() {
    _napkinGraphBitmap.value = null
    _graphGenerationMessage.value = null
  }

  // Factory
  companion object {
    fun provideFactory(application: HabitTaskApplication): ViewModelProvider.Factory {
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return MainViewModel(
            application.taskRepository,
            application.habitRepository,
            application.settingsRepository
          ) as T
        }
      }
    }
  }
}
