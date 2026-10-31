package com.example.ui.screens.home

import com.example.ui.theme.SFProFontFamily
import com.example.ui.components.AnimatedIcon
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.ui.components.AppEmptyState
import com.example.ui.components.AppHeader
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.FloatingNavContentBottomPadding
import com.example.ui.components.SketchCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassSegmented
import com.example.ui.components.fluidLensedGlass
import com.example.ui.components.SketchCheckbox
import com.example.ui.components.SketchProgressBar
import com.example.ui.components.SketchSegmentedControl
import com.example.ui.components.paperDotBackground
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.PrimaryAccentLime
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.components.bounceClick
import com.example.ui.components.heroEntrance
import com.example.ui.components.sectionEntrance
import com.example.ui.components.staggeredEntrance
import com.example.ui.components.pulseEntrance
import com.example.ui.components.shimmerBadge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.DeleteOutline
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.viewmodel.MainViewModel
import com.example.util.AppPreferences
import com.example.ui.screens.calendar.formatTimeDisplay
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  onNavigateToAddTask: () -> Unit,
  onNavigateToAddHabit: () -> Unit,
  onTaskClick: (Task) -> Unit,
  onHabitClick: (Habit) -> Unit,
  onNavigateToSettings: (() -> Unit)? = null,
  onNavigateToSearch: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val todayTasksViewMode by AppPreferences.todayTasksViewMode.collectAsStateWithLifecycle()
  val todayHabitsViewMode by AppPreferences.todayHabitsViewMode.collectAsStateWithLifecycle()

  val tasks by viewModel.tasks.collectAsStateWithLifecycle()
  val habits by viewModel.habits.collectAsStateWithLifecycle()
  val completionRecords by viewModel.completionRecords.collectAsStateWithLifecycle()
  val summary by viewModel.dailySummary.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Tasks, 2: Habits
  var isDashboardEditMode by remember { mutableStateOf(false) }
  var taskToDelete by remember { mutableStateOf<Task?>(null) }
  var habitToDelete by remember { mutableStateOf<Habit?>(null) }

  val today = LocalDate.now()
  val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
  val todayFormattedDate = today.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))

  val todayTasks = remember(tasks, today) {
    tasks.filter { it.isDueToday(today) && !(it.isShortTerm && it.isCompleted) }
  }

  val todayHabits = remember(habits, today) {
    habits.filter { it.isActive && !it.isArchived && !it.isPaused && it.isScheduledOn(today) }
  }

  val todayHabitRecords by viewModel.todayHabitRecords.collectAsStateWithLifecycle()

  val completedHabitIds = remember(completionRecords, todayStr) {
    completionRecords
      .filter { it.date == todayStr && it.isCompleted }
      .map { it.habitId }
      .toSet()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperDotBackground()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = FloatingNavContentBottomPadding)
    ) {
      // Standardized Top Header
      item {
        AppHeader(
          title = "Today",
          leftContent = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = "TDlxHT Logo",
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .border(1.dp, CardBorder, CircleShape)
              )
              Text(
                text = if (isDashboardEditMode) "Done" else "Edit",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isDashboardEditMode) PrimaryAccentLime else InkPrimary
                ),
                modifier = Modifier
                  .bounceClick { isDashboardEditMode = !isDashboardEditMode }
                  .testTag("home_edit_mode_btn")
              )
            }
          },
          rightContent = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (onNavigateToSearch != null) {
                GlassButton(
                  onClick = onNavigateToSearch,
                  contentDescription = "Search",
                  size = 38.dp,
                  modifier = Modifier.testTag("home_search_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = InkPrimary,
                    modifier = Modifier.size(19.dp)
                  )
                }
              }

              GlassButton(
                onClick = {
                  if (onNavigateToSettings != null) {
                    onNavigateToSettings()
                  } else {
                    onNavigateToAddTask()
                  }
                },
                contentDescription = "Settings",
                size = 38.dp,
                modifier = Modifier.testTag("home_settings_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Settings,
                  contentDescription = null,
                  tint = InkPrimary,
                  modifier = Modifier.size(19.dp)
                )
              }
            }
          }
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      if (isDashboardEditMode) {
        item {
          Box(modifier = Modifier.animateItem().heroEntrance(delayMs = 0)) {
            Column {
              Spacer(modifier = Modifier.height(6.dp))
              SketchCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AccentTerracotta.copy(alpha = 0.08f),
                borderColor = AccentTerracotta,
                borderWidth = 1.dp,
                cornerRadius = 14.dp
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Edit Mode",
                    style = TextStyle(
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = AccentTerracotta
                    )
                  )
                  Text(
                    text = "Manage tasks & habits",
                    style = TextStyle(
                      fontSize = 12.sp,
                      color = InkSecondary
                    )
                  )
                }
              }
              Spacer(modifier = Modifier.height(14.dp))
            }
          }
        }
      }

      // DAILY SUMMARY CARD (Apple Liquid Glass)
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth().heroEntrance(delayMs = 100),
          cornerRadius = 20.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Column {
                Text(
                  text = "DAILY SUMMARY",
                  style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = InkSecondary
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedContent(
                  targetState = "${summary.tasksCompleted}/${summary.tasksTotal}",
                  transitionSpec = {
                    slideInVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { height -> height / 2 } +
                      fadeIn(animationSpec = tween(200)) togetherWith
                      slideOutVertically(animationSpec = tween(150, easing = FastOutSlowInEasing)) { height -> -height / 2 } +
                      fadeOut(animationSpec = tween(150))
                  },
                  label = "taskCountAnim"
                ) { countText ->
                  Text(
                    text = "Tasks: $countText done",
                    style = TextStyle(
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = InkPrimary
                    )
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                // Date badge pill (subtle translucent glass)
                val isDarkLocal = com.example.ui.theme.LocalAppColors.current.isDark
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDarkLocal) Color(0x1CFFFFFF) else Color(0x10000000))
                    .border(0.6.dp, if (isDarkLocal) Color(0x28FFFFFF) else Color(0x18000000), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = todayFormattedDate,
                    style = TextStyle(
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = InkPrimary
                    )
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))
                AnimatedContent(
                  targetState = if (summary.habitsTotal == 0) {
                    "No habits scheduled"
                  } else if (summary.sessionsTotal > summary.habitsTotal) {
                    "Habits: ${summary.habitsCompleted}/${summary.habitsTotal} done (${summary.sessionsCompleted}/${summary.sessionsTotal} sessions)"
                  } else {
                    "Habits: ${summary.habitsCompleted}/${summary.habitsTotal} checked"
                  },
                  transitionSpec = {
                    slideInVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { height -> height / 2 } +
                      fadeIn(animationSpec = tween(200)) togetherWith
                      slideOutVertically(animationSpec = tween(150, easing = FastOutSlowInEasing)) { height -> -height / 2 } +
                      fadeOut(animationSpec = tween(150))
                  },
                  label = "habitCountAnim"
                ) { habitCountText ->
                  Text(
                    text = habitCountText,
                    style = TextStyle(
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Medium,
                      color = InkSecondary
                    )
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar and streak badge
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              SketchProgressBar(
                progress = summary.overallProgress,
                modifier = Modifier.weight(1f),
                height = 10.dp
              )

              if (summary.maxStreak > 0) {
                Spacer(modifier = Modifier.width(10.dp))
                val isDarkLocal = com.example.ui.theme.LocalAppColors.current.isDark
                Box(
                  modifier = Modifier
                    .pulseEntrance(delayMs = 180)
                    .fluidLensedGlass(
                      shape = RoundedCornerShape(10.dp),
                      elevation = 1.dp,
                      borderWidth = 0.6.dp,
                      isElevated = true
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                  Text(
                    text = "${summary.maxStreak}-day streak",
                    style = TextStyle(
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isDarkLocal) Color.White else Color(0xFF111113)
                    )
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      // Filter Segment: [All] [Tasks] [Habits] - Apple Liquid Glass Segmented
      item {
        GlassSegmented(
          items = listOf("All", "Tasks", "Habits"),
          selectedIndex = selectedTab,
          onItemSelected = { selectedTab = it },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
      }

      // TODAY'S TASKS SECTION
      if (selectedTab == 0 || selectedTab == 1) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .sectionEntrance(delayMs = 60)
              .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Today's Tasks",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
              if (todayTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                val isDarkTasksView = com.example.ui.theme.LocalAppColors.current.isDark
                Box(
                  modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDarkTasksView) Color(0x18FFFFFF) else Color(0x10000000))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${todayTasks.count { it.isCompleted }}/${todayTasks.size}",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkSecondary)
                  )
                }
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // Independent View Switcher (List / Grid)
              val isDarkTasksView = com.example.ui.theme.LocalAppColors.current.isDark
              val isGridTasks = todayTasksViewMode == "GRID"
              Box(
                modifier = Modifier
                  .fluidLensedGlass(
                    shape = RoundedCornerShape(8.dp),
                    elevation = if (isGridTasks) 1.dp else 0.dp,
                    borderWidth = 0.6.dp,
                    isElevated = isGridTasks,
                    isInteractive = true,
                    onClick = {
                      val next = if (todayTasksViewMode == "GRID") "LIST" else "GRID"
                      AppPreferences.setTodayTasksViewMode(context, next)
                    }
                  )
                  .padding(horizontal = 9.dp, vertical = 4.dp)
              ) {
                Text(
                  text = if (todayTasksViewMode == "GRID") "Grid" else "List",
                  style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGridTasks) (if (isDarkTasksView) Color.White else Color(0xFF111113)) else InkSecondary
                  )
                )
              }

              GlassButton(
                onClick = onNavigateToAddTask,
                icon = {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = InkPrimary,
                    modifier = Modifier.size(15.dp)
                  )
                },
                modifier = Modifier.testTag("add_task_header_btn")
              ) {
                Text(
                  text = "Add Task",
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                  )
                )
              }
            }
          }
        }

        if (todayTasks.isEmpty()) {
          item {
            AppEmptyState(
              icon = Icons.Default.Check,
              title = "No tasks",
              actionText = "Add Task",
              onAction = onNavigateToAddTask
            )
          }
        } else if (todayTasksViewMode == "GRID") {
          val taskPairs = todayTasks.chunked(2)
          items(taskPairs, key = { pair -> pair.joinToString("-") { it.id } }) { pair ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              pair.forEach { task ->
                val subtasks = viewModel.getSubtasksForTask(task.id)
                val subtasksDone = subtasks.count { it.isCompleted }
                Box(modifier = Modifier.weight(1f)) {
                  TaskGridCard(
                    task = task,
                    subtasksCount = subtasks.size,
                    subtasksDone = subtasksDone,
                    isEditMode = isDashboardEditMode,
                    is24Hour = settings.timeFormat24Hour,
                    onToggle = { viewModel.toggleTaskCompletion(task.id) },
                    onClick = { onTaskClick(task) },
                    onDelete = { taskToDelete = task }
                  )
                }
              }
              if (pair.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        } else {
          items(todayTasks, key = { it.id }) { task ->
            val subtasks = viewModel.getSubtasksForTask(task.id)
            val subtasksDone = subtasks.count { it.isCompleted }
            val itemIndex = todayTasks.indexOf(task)

            Box(modifier = Modifier.animateItem().staggeredEntrance(index = itemIndex)) {
              TaskItemCard(
                task = task,
                subtasksCount = subtasks.size,
                subtasksDone = subtasksDone,
                isEditMode = isDashboardEditMode,
                is24Hour = settings.timeFormat24Hour,
                onToggle = { viewModel.toggleTaskCompletion(task.id) },
                onClick = { onTaskClick(task) },
                onDelete = { taskToDelete = task },
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
          }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }
      }

      // TODAY'S HABITS SECTION
      if (selectedTab == 0 || selectedTab == 2) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .sectionEntrance(delayMs = 80)
              .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Today's Habits",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
              if (todayHabits.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                val isDarkHabitsView = com.example.ui.theme.LocalAppColors.current.isDark
                Box(
                  modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDarkHabitsView) Color(0x18FFFFFF) else Color(0x10000000))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${todayHabits.count { completedHabitIds.contains(it.id) }}/${todayHabits.size}",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkSecondary)
                  )
                }
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // Independent View Switcher (List / Grid)
              val isDarkHabitsView = com.example.ui.theme.LocalAppColors.current.isDark
              val isGridHabits = todayHabitsViewMode == "GRID"
              Box(
                modifier = Modifier
                  .fluidLensedGlass(
                    shape = RoundedCornerShape(8.dp),
                    elevation = if (isGridHabits) 1.dp else 0.dp,
                    borderWidth = 0.6.dp,
                    isElevated = isGridHabits,
                    isInteractive = true,
                    onClick = {
                      val next = if (todayHabitsViewMode == "GRID") "LIST" else "GRID"
                      AppPreferences.setTodayHabitsViewMode(context, next)
                    }
                  )
                  .padding(horizontal = 9.dp, vertical = 4.dp)
              ) {
                Text(
                  text = if (todayHabitsViewMode == "GRID") "Grid" else "List",
                  style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGridHabits) (if (isDarkHabitsView) Color.White else Color(0xFF111113)) else InkSecondary
                  )
                )
              }

              GlassButton(
                onClick = onNavigateToAddHabit,
                icon = {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = InkPrimary,
                    modifier = Modifier.size(15.dp)
                  )
                },
                modifier = Modifier.testTag("add_habit_header_btn")
              ) {
                Text(
                  text = "Add Habit",
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                  )
                )
              }
            }
          }
        }

        if (todayHabits.isEmpty()) {
          item {
            AppEmptyState(
              icon = Icons.Default.Check,
              title = "No habits",
              actionText = "Add Habit",
              onAction = onNavigateToAddHabit
            )
          }
        } else if (todayHabitsViewMode == "GRID") {
          val habitPairs = todayHabits.chunked(2)
          items(habitPairs, key = { pair -> pair.joinToString("-") { it.id } }) { pair ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              pair.forEach { habit ->
                val isCompleted = completedHabitIds.contains(habit.id)
                val currentSessions = todayHabitRecords[habit.id]?.currentValue?.toInt() ?: 0
                val streakInfo = viewModel.getHabitStreak(habit)
                Box(modifier = Modifier.weight(1f)) {
                  HabitGridCard(
                    habit = habit,
                    isCompleted = isCompleted,
                    currentSessions = currentSessions,
                    streakDays = streakInfo.currentStreak,
                    isEditMode = isDashboardEditMode,
                    onToggle = { viewModel.toggleHabitToday(habit.id) },
                    onClick = { onHabitClick(habit) },
                    onDelete = { habitToDelete = habit }
                  )
                }
              }
              if (pair.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        } else {
          items(todayHabits, key = { it.id }) { habit ->
            val isCompleted = completedHabitIds.contains(habit.id)
            val currentSessions = todayHabitRecords[habit.id]?.currentValue?.toInt() ?: 0
            val streakInfo = viewModel.getHabitStreak(habit)
            val habitIndex = todayHabits.indexOf(habit)

            Box(modifier = Modifier.animateItem().staggeredEntrance(index = habitIndex)) {
              HabitItemCard(
                habit = habit,
                isCompleted = isCompleted,
                currentSessions = currentSessions,
                streakDays = streakInfo.currentStreak,
                isEditMode = isDashboardEditMode,
                onToggle = { viewModel.toggleHabitToday(habit.id) },
                onClick = { onHabitClick(habit) },
                onDelete = { habitToDelete = habit },
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    // Task Delete Confirmation Dialog
    taskToDelete?.let { task ->
      DeleteConfirmationDialog(
        title = "Delete Task?",
        message = "This cannot be undone. Are you sure you want to delete \"${task.title}\"?",
        onConfirm = {
          viewModel.deleteTask(task.id)
          taskToDelete = null
        },
        onDismiss = { taskToDelete = null }
      )
    }

    // Habit Delete Confirmation Dialog
    habitToDelete?.let { habit ->
      DeleteConfirmationDialog(
        title = "Delete Habit?",
        message = "This cannot be undone. Are you sure you want to delete \"${habit.name}\"?",
        onConfirm = {
          viewModel.deleteHabit(habit.id)
          habitToDelete = null
        },
        onDismiss = { habitToDelete = null }
      )
    }
  }
}

@Composable
fun TaskItemCard(
  task: Task,
  subtasksCount: Int,
  subtasksDone: Int,
  isEditMode: Boolean = false,
  is24Hour: Boolean = true,
  onToggle: () -> Unit,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  onDelete: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val textColor by animateColorAsState(
    targetValue = if (task.isCompleted) InkMuted else InkPrimary,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "taskTextCol"
  )
  val cardAlpha by animateFloatAsState(
    targetValue = if (task.isCompleted) 0.70f else 1.0f,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "taskAlpha"
  )

  SketchCard(
    modifier = modifier
      .fillMaxWidth()
      .alpha(cardAlpha)
      .animateContentSize(animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f))
      .bounceClick(scaleDown = 0.98f, onLongClick = onLongClick) { onClick() }
      .testTag("task_card_${task.id}"),
    cornerRadius = 14.dp,
    borderWidth = 0.75.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
        Text(
          text = task.title,
          style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
          )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          val formattedTime = if (task.dueTime.isNotBlank()) formatTimeDisplay(task.dueTime, is24Hour) else ""
          val categoryAndTime = if (formattedTime.isNotBlank()) "${task.category} • $formattedTime" else task.category
          Text(
            text = categoryAndTime,
            style = TextStyle(
              fontSize = 13.sp,
              color = InkSecondary
            )
          )

          if (subtasksCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "$subtasksDone/$subtasksCount",
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
              )
            }
          }

          if (task.priority.name == "HIGH") {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(AccentTerracotta, CircleShape)
            )
          }

          if (task.isShortTerm) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(PrimaryAccentLime.copy(alpha = 0.15f))
                .border(0.8.dp, PrimaryAccentLime.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
              Text(
                text = "Short-term",
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = PrimaryAccentLime)
              )
            }
          }
        }
      }

      if (isEditMode) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .border(1.2.dp, CardBorder, CircleShape)
              .clip(CircleShape)
              .bounceClick { onClick() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Task",
              tint = InkPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
          if (onDelete != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(AccentTerracotta.copy(alpha = 0.15f), CircleShape)
                .clip(CircleShape)
                .bounceClick { onDelete() },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete Task",
                tint = AccentTerracotta,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      } else {
        SketchCheckbox(
          checked = task.isCompleted,
          onCheckedChange = { onToggle() }
        )
      }
    }
  }
}

@Composable
fun TaskGridCard(
  task: Task,
  subtasksCount: Int,
  subtasksDone: Int,
  isEditMode: Boolean = false,
  is24Hour: Boolean = true,
  onToggle: () -> Unit,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  onDelete: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val textColor by animateColorAsState(
    targetValue = if (task.isCompleted) InkMuted else InkPrimary,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "gridTaskTextCol"
  )
  val cardAlpha by animateFloatAsState(
    targetValue = if (task.isCompleted) 0.70f else 1.0f,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "gridTaskAlpha"
  )

  SketchCard(
    modifier = modifier
      .fillMaxWidth()
      .alpha(cardAlpha)
      .animateContentSize(animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f))
      .bounceClick(scaleDown = 0.98f, onLongClick = onLongClick) { onClick() }
      .testTag("task_grid_card_${task.id}"),
    cornerRadius = 14.dp,
    borderWidth = 0.75.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = task.category,
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = InkSecondary)
          )
        }

        if (isEditMode) {
          if (onDelete != null) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .background(AccentTerracotta.copy(alpha = 0.15f), CircleShape)
                .clip(CircleShape)
                .bounceClick { onDelete() },
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Outlined.DeleteOutline, "Delete", tint = AccentTerracotta, modifier = Modifier.size(14.dp))
            }
          }
        } else {
          SketchCheckbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
            sizeDp = 20.dp
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = task.title,
        style = TextStyle(
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = textColor,
          textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
        ),
        maxLines = 2
      )

      if (task.dueTime.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = formatTimeDisplay(task.dueTime, is24Hour),
          style = TextStyle(fontSize = 11.sp, color = InkMuted)
        )
      }
    }
  }
}

@Composable
fun HabitGridCard(
  habit: Habit,
  isCompleted: Boolean,
  currentSessions: Int = 0,
  streakDays: Int,
  isEditMode: Boolean = false,
  onToggle: () -> Unit,
  onClick: () -> Unit,
  onDelete: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val cardAlpha by animateFloatAsState(
    targetValue = if (isCompleted) 0.70f else 1.0f,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "gridHabitAlpha"
  )

  SketchCard(
    modifier = modifier
      .fillMaxWidth()
      .alpha(cardAlpha)
      .bounceClick(scaleDown = 0.98f) { onClick() },
    cornerRadius = 14.dp,
    borderWidth = 0.75.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        if (streakDays > 0) {
          Box(
            modifier = Modifier
              .background(PillBackgroundDark, RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "${streakDays}d streak",
              style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PillTextDark)
            )
          }
        } else {
          Spacer(Modifier.size(1.dp))
        }

        if (isEditMode) {
          if (onDelete != null) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .background(AccentTerracotta.copy(alpha = 0.15f), CircleShape)
                .clip(CircleShape)
                .bounceClick { onDelete() },
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Outlined.DeleteOutline, "Delete", tint = AccentTerracotta, modifier = Modifier.size(14.dp))
            }
          }
        } else {
          if (habit.targetValue > 1f && !isCompleted) {
            // Multi-session counter pill
            Box(
              modifier = Modifier
                .height(28.dp)
                .background(PillBackgroundDark.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .bounceClick { onToggle() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "+$currentSessions/${habit.targetValue.toInt()}",
                style = TextStyle(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
            }
          } else {
            Box(
              modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .border(
                  width = 1.8.dp,
                  color = if (isCompleted) PillBackgroundDark else CardBorder,
                  shape = CircleShape
                )
                .background(
                  color = if (isCompleted) PillBackgroundDark else Color.Transparent,
                  shape = CircleShape
                )
                .bounceClick { onToggle() },
              contentAlignment = Alignment.Center
            ) {
              if (isCompleted) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Completed",
                  tint = PillTextDark,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = habit.name,
        style = TextStyle(
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (isCompleted) InkMuted else InkPrimary,
          textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
        ),
        maxLines = 2
      )

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = if (habit.targetValue > 1f) {
          if (isCompleted) "${habit.targetValue.toInt()}/${habit.targetValue.toInt()} done"
          else "$currentSessions/${habit.targetValue.toInt()} session"
        } else habit.category,
        style = TextStyle(fontSize = 11.sp, color = InkMuted)
      )
    }
  }
}

@Composable
fun HabitItemCard(
  habit: Habit,
  isCompleted: Boolean,
  currentSessions: Int = 0,
  streakDays: Int,
  isEditMode: Boolean = false,
  onToggle: () -> Unit,
  onClick: () -> Unit,
  onDelete: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val textColor by animateColorAsState(
    targetValue = if (isCompleted) InkSecondary else InkPrimary,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "habitTextCol"
  )
  val cardAlpha by animateFloatAsState(
    targetValue = if (isCompleted) 0.72f else 1.0f,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "habitAlpha"
  )
  val streakScale by animateFloatAsState(
    targetValue = if (isCompleted) 1.10f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "homeHabitStreakPop"
  )

  SketchCard(
    modifier = modifier
      .fillMaxWidth()
      .alpha(cardAlpha)
      .animateContentSize(animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f))
      .bounceClick(scaleDown = 0.98f) { onClick() }
      .testTag("habit_card_${habit.id}"),
    cornerRadius = 14.dp,
    borderWidth = 0.75.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = habit.name,
            style = TextStyle(
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
              color = textColor,
              textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
          )

          if (streakDays > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .scale(streakScale)
                .border(1.2.dp, CardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${streakDays}d streak",
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val subtitleText = if (habit.targetValue > 1f) {
          if (isCompleted) {
            "${habit.targetValue.toInt()}/${habit.targetValue.toInt()} sessions completed"
          } else if (currentSessions > 0) {
            "$currentSessions/${habit.targetValue.toInt()} sessions • ${(habit.targetValue.toInt() - currentSessions)} remaining"
          } else {
            "${habit.targetValue.toInt()} ${habit.targetUnit} daily"
          }
        } else {
          "${habit.category} • ${habit.frequency.displayName}"
        }

        Text(
          text = subtitleText,
          style = TextStyle(
            fontSize = 13.sp,
            color = InkSecondary
          )
        )
      }

      if (isEditMode) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .border(1.2.dp, CardBorder, CircleShape)
              .clip(CircleShape)
              .bounceClick { onClick() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Habit",
              tint = InkPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
          if (onDelete != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(AccentTerracotta.copy(alpha = 0.15f), CircleShape)
                .clip(CircleShape)
                .bounceClick { onDelete() },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete Habit",
                tint = AccentTerracotta,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      } else {
        if (habit.targetValue > 1f && !isCompleted) {
          // Dedicated liquid-glass multi-session logging button
          Box(
            modifier = Modifier
              .height(34.dp)
              .background(PillBackgroundDark.copy(alpha = 0.10f), RoundedCornerShape(17.dp))
              .border(1.2.dp, CardBorder, RoundedCornerShape(17.dp))
              .clip(RoundedCornerShape(17.dp))
              .bounceClick { onToggle() }
              .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Log Session",
                tint = InkPrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "+$currentSessions/${habit.targetValue.toInt()} Session",
                style = TextStyle(
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
            }
          }
        } else {
          SketchCheckbox(
            checked = isCompleted,
            onCheckedChange = { onToggle() }
          )
        }
      }
    }
  }
}