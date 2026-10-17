package com.example.ui.screens.calendar

import com.example.ui.theme.SFProFontFamily
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Habit
import com.example.data.model.HabitCompletionRecord
import com.example.data.model.Task
import com.example.ui.components.AppEmptyState
import com.example.ui.components.AppHeader
import com.example.ui.components.SketchCard
import com.example.ui.components.SketchSegmentedControl
import com.example.ui.components.bounceClick
import com.example.ui.components.heroEntrance
import com.example.ui.components.sectionEntrance
import com.example.ui.components.staggeredEntrance
import com.example.ui.components.paperDotBackground
import com.example.ui.screens.home.HabitItemCard
import com.example.ui.screens.home.TaskItemCard
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.viewmodel.MainViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun CalendarScreen(
  viewModel: MainViewModel,
  onNavigateToAddTask: () -> Unit,
  onTaskClick: (Task) -> Unit,
  onHabitClick: (Habit) -> Unit,
  modifier: Modifier = Modifier
) {
  val tasks by viewModel.tasks.collectAsStateWithLifecycle()
  val habits by viewModel.habits.collectAsStateWithLifecycle()
  val completionRecords by viewModel.completionRecords.collectAsStateWithLifecycle()
  val selectedDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()

  val napkinGraphBitmap by viewModel.napkinGraphBitmap.collectAsStateWithLifecycle()
  val isGeneratingGraph by viewModel.isGeneratingGraph.collectAsStateWithLifecycle()
  val graphMessage by viewModel.graphGenerationMessage.collectAsStateWithLifecycle()
  var showFullscreenGraph by remember { mutableStateOf(false) }

  // 0: Month, 1: Week, 2: Day
  val initialViewMode = when (settings.calendarDefaultView.uppercase()) {
    "WEEK" -> 1
    "DAY" -> 2
    else -> 0
  }
  var calendarViewMode by remember { mutableIntStateOf(initialViewMode) }
  var displayMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

  // Sync displayMonth when selectedDate changes outside
  LaunchedEffect(selectedDate) {
    displayMonth = YearMonth.from(selectedDate)
  }

  val selectedDateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
  val today = LocalDate.now()
  val nowTime = LocalTime.now()

  // Filter tasks for selected date respecting user preferences
  val tasksForDate = remember(tasks, selectedDateStr, settings) {
    tasks.filter { task ->
      task.dueDate == selectedDateStr &&
        !(task.isShortTerm && task.isCompleted) &&
        (settings.showCompletedTasksInCalendar || !task.isCompleted) &&
        (settings.showOverdueInCalendar || !task.isOverdue(today, nowTime))
    }
  }

  // Habits scheduled on selected date respecting user preferences
  val habitsForDate = remember(habits, selectedDate, settings) {
    habits.filter { it.isActive && !it.isArchived && it.isScheduledOn(selectedDate) }
  }

  // Completed habits on selected date
  val completedHabitIdsOnDate = remember(completionRecords, selectedDateStr) {
    completionRecords
      .filter { it.date == selectedDateStr && it.isCompleted }
      .map { it.habitId }
      .toSet()
  }

  val visibleHabitsForDate = remember(habitsForDate, completedHabitIdsOnDate, settings) {
    if (settings.showCompletedHabitsInCalendar) {
      habitsForDate
    } else {
      habitsForDate.filter { !completedHabitIdsOnDate.contains(it.id) }
    }
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
      contentPadding = PaddingValues(top = 16.dp, bottom = 155.dp)
    ) {
      // Header: Unified AppHeader
      item {
        AppHeader(
          title = "Calendar",
          leftContent = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .bounceClick {
                  val currentDeviceDate = LocalDate.now()
                  viewModel.setSelectedCalendarDate(currentDeviceDate)
                  displayMonth = YearMonth.from(currentDeviceDate)
                }
                .testTag("calendar_today_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Today,
                contentDescription = "Jump to Today",
                tint = InkPrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Today",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = InkPrimary
                )
              )
            }
          },
          actionIcon = Icons.Default.Add,
          onActionClick = onNavigateToAddTask
        )

        Spacer(modifier = Modifier.height(10.dp))
      }

      // View mode segmented toggle: [Month] [Week] [Day]
      item {
        SketchSegmentedControl(
          items = listOf("Month", "Week", "Day"),
          selectedIndex = calendarViewMode,
          onItemSelected = { calendarViewMode = it },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))
      }

      // 1. CALENDAR VIEW CARD (Month, Week, or Day)
      item {
        Box(modifier = Modifier.heroEntrance(delayMs = 40)) {
          when (calendarViewMode) {
            0 -> {
              // MONTH VIEW
              MonthViewCard(
                displayMonth = displayMonth,
                selectedDate = selectedDate,
                tasks = tasks,
                habits = habits,
                records = completionRecords,
                startOfWeek = settings.startOfWeek,
                onMonthChange = { displayMonth = it },
                onDateSelected = { date ->
                  viewModel.setSelectedCalendarDate(date)
                  displayMonth = YearMonth.from(date)
                }
              )
            }
            1 -> {
              // WEEK VIEW
              WeekViewCard(
                selectedDate = selectedDate,
                tasks = tasks,
                habits = habits,
                records = completionRecords,
                startOfWeek = settings.startOfWeek,
                onDateSelected = { date ->
                  viewModel.setSelectedCalendarDate(date)
                  displayMonth = YearMonth.from(date)
                }
              )
            }
            2 -> {
              // DAY VIEW SELECTOR STRIP
              DayViewSelector(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                  viewModel.setSelectedCalendarDate(date)
                  displayMonth = YearMonth.from(date)
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // 2. AGENDA / SCHEDULE FOR SELECTED DATE
      item {
        val formattedSelected = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()))
        val isToday = selectedDate == today

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .sectionEntrance(delayMs = 60),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isToday) "TODAY'S AGENDA • $formattedSelected" else "AGENDA FOR $formattedSelected",
            style = TextStyle(
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = InkSecondary
            )
          )

          if (isToday) {
            Box(
              modifier = Modifier
                .background(AccentTerracotta.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .border(1.dp, AccentTerracotta.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "Today",
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentTerracotta)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
      }

      // If in DAY VIEW mode, show Chronological Hourly Agenda + Untimed Items
      if (calendarViewMode == 2) {
        val timedTasks = tasksForDate.filter { it.dueTime.isNotBlank() }
          .sortedBy { it.dueTime }
        val untimedTasks = tasksForDate.filter { it.dueTime.isBlank() }

        // Untimed Section
        if (untimedTasks.isNotEmpty()) {
          item {
            Text(
              text = "Untimed / All Day Tasks (${untimedTasks.size})",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
          }

          items(untimedTasks, key = { it.id }) { task ->
            val subtasks = viewModel.getSubtasksForTask(task.id)
            val taskIndex = untimedTasks.indexOf(task)
            Box(modifier = Modifier.animateItem().staggeredEntrance(index = taskIndex)) {
              TaskItemCard(
                task = task,
                subtasksCount = subtasks.size,
                subtasksDone = subtasks.count { it.isCompleted },
                onToggle = { viewModel.toggleTaskCompletion(task.id) },
                onClick = { onTaskClick(task) },
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
          }

          item { Spacer(modifier = Modifier.height(10.dp)) }
        }

        // Timed Section (Chronological)
        if (timedTasks.isNotEmpty()) {
          item {
            Text(
              text = "Timed Schedule (${timedTasks.size})",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
          }

          items(timedTasks, key = { it.id }) { task ->
            val subtasks = viewModel.getSubtasksForTask(task.id)
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .width(62.dp)
                  .padding(end = 8.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                Text(
                  text = formatTimeDisplay(task.dueTime, settings.timeFormat24Hour),
                  style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentTerracotta
                  )
                )
              }
              Box(modifier = Modifier.weight(1f).animateItem()) {
                TaskItemCard(
                  task = task,
                  subtasksCount = subtasks.size,
                  subtasksDone = subtasks.count { it.isCompleted },
                  onToggle = { viewModel.toggleTaskCompletion(task.id) },
                  onClick = { onTaskClick(task) }
                )
              }
            }
          }

          item { Spacer(modifier = Modifier.height(10.dp)) }
        }
      } else {
        // Standard view: Tasks for selected date
        if (tasksForDate.isNotEmpty()) {
          item {
            Text(
              text = "Tasks (${tasksForDate.size})",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
          }

          items(tasksForDate, key = { it.id }) { task ->
            val subtasks = viewModel.getSubtasksForTask(task.id)
            val taskIndex = tasksForDate.indexOf(task)
            Box(modifier = Modifier.animateItem().staggeredEntrance(index = taskIndex)) {
              TaskItemCard(
                task = task,
                subtasksCount = subtasks.size,
                subtasksDone = subtasks.count { it.isCompleted },
                onToggle = { viewModel.toggleTaskCompletion(task.id) },
                onClick = { onTaskClick(task) },
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
          }

          item { Spacer(modifier = Modifier.height(14.dp)) }
        }
      }

      // Habits for selected date
      if (visibleHabitsForDate.isNotEmpty()) {
        item {
          Text(
            text = "Scheduled Habits (${visibleHabitsForDate.size})",
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = InkPrimary
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
        }

        items(visibleHabitsForDate, key = { it.id }) { habit ->
          val isDone = completedHabitIdsOnDate.contains(habit.id)
          val streakInfo = viewModel.getHabitStreak(habit)
          val habitIndex = visibleHabitsForDate.indexOf(habit)

          Box(modifier = Modifier.animateItem().staggeredEntrance(index = habitIndex)) {
            HabitItemCard(
              habit = habit,
              isCompleted = isDone,
              streakDays = streakInfo.currentStreak,
              onToggle = {
                if (selectedDate == today) {
                  viewModel.toggleHabitToday(habit.id)
                } else {
                  viewModel.recordHabitProgress(habit.id, selectedDateStr, if (isDone) 0f else habit.targetValue)
                }
              },
              onClick = { onHabitClick(habit) },
              modifier = Modifier.padding(vertical = 4.dp)
            )
          }
        }
      }

      if (tasksForDate.isEmpty() && visibleHabitsForDate.isEmpty()) {
        item {
          AppEmptyState(
            icon = Icons.Default.Today,
            title = "No plans for this date",
            actionText = "Add Task",
            onAction = onNavigateToAddTask
          )
        }
      }

      // 3. PRODUCTIVITY INSIGHTS & ANALYTICS (Section 11, 12, 13, 14, 15, 17)
      item {
        Spacer(modifier = Modifier.height(24.dp))
        ProductivityInsightsSection(
          tasks = tasks,
          habits = habits,
          records = completionRecords,
          onTaskClick = onTaskClick
        )
      }

      // 4. NAPKIN AI VISUAL PROGRESS SECTION
      item {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "AI VISUAL PROGRESS (NAPKIN AI)",
            style = TextStyle(
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = InkSecondary
            )
          )

          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AccentTerracotta,
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SketchCard(
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Text(
              text = "Visual Progress",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
              )
            )

            Text(
              text = "Generate activity charts.",
              style = TextStyle(fontSize = 12.sp, color = InkSecondary)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Weekly Button
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .border(1.2.dp, CardBorder, RoundedCornerShape(10.dp))
                  .clip(RoundedCornerShape(10.dp))
                  .background(PillBackgroundDark)
                  .bounceClick {
                    if (!isGeneratingGraph) {
                      viewModel.generateNapkinGraph("weekly")
                    }
                  }
                  .testTag("generate_weekly_graph_btn"),
                contentAlignment = Alignment.Center
              ) {
                if (isGeneratingGraph) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                      color = PillTextDark,
                      strokeWidth = 2.dp,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Generating...",
                      style = TextStyle(fontSize = 12.sp, color = PillTextDark, fontWeight = FontWeight.SemiBold)
                    )
                  }
                } else {
                  Text(
                    text = "Weekly Chart",
                    style = TextStyle(
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = PillTextDark
                    )
                  )
                }
              }

              // Monthly Button
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .border(1.2.dp, CardBorder, RoundedCornerShape(10.dp))
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isGeneratingGraph) PillBackgroundDark.copy(alpha = 0.5f) else Color.Transparent)
                  .bounceClick {
                    if (!isGeneratingGraph) {
                      viewModel.generateNapkinGraph("monthly")
                    }
                  }
                  .testTag("generate_monthly_graph_btn"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Monthly Chart",
                  style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                  )
                )
              }
            }

            // Status message
            if (graphMessage != null) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = graphMessage ?: "",
                style = TextStyle(
                  fontSize = 12.sp,
                  color = if (graphMessage?.contains("error", ignoreCase = true) == true || graphMessage?.contains("failed", ignoreCase = true) == true) AccentRed else InkSecondary,
                  fontWeight = FontWeight.Medium
                )
              )
            }

            // Generated Graph Display
            AnimatedVisibility(
              visible = napkinGraphBitmap != null,
              enter = expandVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(200)),
              exit = shrinkVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(150))
            ) {
              napkinGraphBitmap?.let { bitmap ->
                Column(modifier = Modifier.padding(top = 14.dp)) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(200.dp)
                      .border(1.2.dp, CardBorder, RoundedCornerShape(12.dp))
                      .clip(RoundedCornerShape(12.dp))
                      .background(CardBackground)
                      .bounceClick(scaleDown = 0.98f) { showFullscreenGraph = true }
                  ) {
                    Image(
                      bitmap = bitmap.asImageBitmap(),
                      contentDescription = "Napkin AI Generated Graph",
                      modifier = Modifier.fillMaxSize(),
                      contentScale = ContentScale.Fit
                    )

                    Box(
                      modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(PillBackgroundDark.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = "Tap to enlarge",
                        style = TextStyle(fontSize = 10.sp, color = PillTextDark, fontWeight = FontWeight.Bold)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                  ) {
                    Text(
                      text = "Dismiss Chart",
                      style = TextStyle(
                        fontFamily = SFProFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkSecondary
                      ),
                      modifier = Modifier.bounceClick { viewModel.dismissGraph() }
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Fullscreen Dialog for Napkin Graph
    if (showFullscreenGraph && napkinGraphBitmap != null) {
      Dialog(onDismissRequest = { showFullscreenGraph = false }) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(2.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Napkin AI Progress Graph",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )

              Box(
                modifier = Modifier
                  .size(34.dp)
                  .border(1.dp, CardBorder, CircleShape)
                  .clip(CircleShape)
                  .background(CardBackground)
                  .bounceClick(scaleDown = 0.88f) { showFullscreenGraph = false },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close",
                  tint = InkPrimary,
                  modifier = Modifier.size(17.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            napkinGraphBitmap?.let { bmp ->
              Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Fullscreen Napkin Graph",
                modifier = Modifier
                  .fillMaxWidth()
                  .height(340.dp),
                contentScale = ContentScale.Fit
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MonthViewCard(
  displayMonth: YearMonth,
  selectedDate: LocalDate,
  tasks: List<Task>,
  habits: List<Habit>,
  records: List<HabitCompletionRecord>,
  startOfWeek: String,
  onMonthChange: (YearMonth) -> Unit,
  onDateSelected: (LocalDate) -> Unit
) {
  val today = LocalDate.now()
  val nowTime = LocalTime.now()

  // Calculate day activity maps for clear indicators
  val datesWithTasks = remember(tasks) { tasks.groupBy { it.dueDate } }
  val datesWithHabitCompletions = remember(records) {
    records.filter { it.isCompleted }.groupBy { it.date }
  }
  val activeHabits = remember(habits) {
    habits.filter { it.isActive && !it.isArchived }
  }

  val isStartSunday = startOfWeek.equals("SUNDAY", ignoreCase = true)
  val dayHeaders = if (isStartSunday) listOf("S", "M", "T", "W", "T", "F", "S") else listOf("M", "T", "W", "T", "F", "S", "S")

  SketchCard(
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Month Header with Prev/Next
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "PLANNING",
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary)
          )
          Text(
            text = "GRID",
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary)
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .border(1.dp, CardBorder, CircleShape)
              .clip(CircleShape)
              .background(CardBackground)
              .bounceClick(scaleDown = 0.90f) { onMonthChange(displayMonth.minusMonths(1)) },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ChevronLeft,
              contentDescription = "Previous Month",
              tint = InkPrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          AnimatedContent(
            targetState = displayMonth,
            transitionSpec = {
              (fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.96f, animationSpec = tween(180, easing = FastOutSlowInEasing)))
                .togetherWith(fadeOut(animationSpec = tween(120, easing = FastOutSlowInEasing)))
            },
            label = "monthYearAnim"
          ) { month ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = month.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())),
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
              )
              Text(
                text = month.year.toString(),
                style = TextStyle(fontSize = 11.sp, color = InkSecondary)
              )
            }
          }

          Box(
            modifier = Modifier
              .size(34.dp)
              .border(1.dp, CardBorder, CircleShape)
              .clip(CircleShape)
              .background(CardBackground)
              .bounceClick(scaleDown = 0.90f) { onMonthChange(displayMonth.plusMonths(1)) },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = "Next Month",
              tint = InkPrimary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Day of week labels
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        dayHeaders.forEach { dayLabel ->
          Text(
            text = dayLabel,
            style = TextStyle(
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = InkSecondary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.width(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Month day cells
      val firstDay = displayMonth.atDay(1)
      val dayOfWeekVal = firstDay.dayOfWeek.value // 1 (Mon) .. 7 (Sun)
      val offset = if (isStartSunday) dayOfWeekVal % 7 else dayOfWeekVal - 1
      val totalDays = displayMonth.lengthOfMonth()
      val prevMonthLength = displayMonth.minusMonths(1).lengthOfMonth()

      val rows = 6
      for (r in 0 until rows) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          for (c in 0 until 7) {
            val cellIndex = r * 7 + c
            val dayNum = cellIndex - offset + 1
            val isCurrent = dayNum in 1..totalDays

            val cellDate = try {
              if (isCurrent) {
                displayMonth.atDay(dayNum)
              } else if (dayNum <= 0) {
                displayMonth.minusMonths(1).atDay((prevMonthLength + dayNum).coerceIn(1, prevMonthLength))
              } else {
                val nextMonthLength = displayMonth.plusMonths(1).lengthOfMonth()
                displayMonth.plusMonths(1).atDay((dayNum - totalDays).coerceIn(1, nextMonthLength))
              }
            } catch (e: Exception) {
              LocalDate.now()
            }

            val isSelected = cellDate.isEqual(selectedDate)
            val dateKey = cellDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val dayTasks = datesWithTasks[dateKey].orEmpty()
            val hasTasks = dayTasks.isNotEmpty()
            val hasOverdue = dayTasks.any { it.isOverdue(today, nowTime) }
            val allTasksDone = hasTasks && dayTasks.all { it.isCompleted }

            val hasHabits = activeHabits.any { it.isScheduledOn(cellDate) }
            val hasHabitDone = datesWithHabitCompletions.containsKey(dateKey)

            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .width(36.dp)
                .testTag("calendar_day_$dateKey")
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .bounceClick(scaleDown = 0.90f) { onDateSelected(cellDate) }
                  .then(
                    if (isSelected) {
                      Modifier.border(1.8.dp, CardBorder, CircleShape)
                    } else {
                      Modifier
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = cellDate.dayOfMonth.toString(),
                  style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (!isCurrent) InkMuted else if (cellDate == today) AccentTerracotta else InkPrimary
                  )
                )
              }

              // Multi-activity dots indicator (Tasks, Habits, Completed, Overdue)
              Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(6.dp)
              ) {
                if (hasOverdue) {
                  Box(modifier = Modifier.size(4.dp).background(AccentRed, CircleShape))
                } else if (allTasksDone) {
                  Box(modifier = Modifier.size(4.dp).background(PillBackgroundDark, CircleShape))
                } else if (hasTasks) {
                  Box(modifier = Modifier.size(4.dp).background(AccentTerracotta, CircleShape))
                }

                if (hasHabitDone) {
                  Box(modifier = Modifier.size(4.dp).background(PillBackgroundDark, CircleShape))
                } else if (hasHabits) {
                  Box(modifier = Modifier.size(4.dp).background(InkSecondary, CircleShape))
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun WeekViewCard(
  selectedDate: LocalDate,
  tasks: List<Task>,
  habits: List<Habit>,
  records: List<HabitCompletionRecord>,
  startOfWeek: String,
  onDateSelected: (LocalDate) -> Unit
) {
  val isStartSunday = startOfWeek.equals("SUNDAY", ignoreCase = true)
  val startDayOfWeek = if (isStartSunday) DayOfWeek.SUNDAY else DayOfWeek.MONDAY

  val weekStart = remember(selectedDate, startDayOfWeek) {
    selectedDate.with(TemporalAdjusters.previousOrSame(startDayOfWeek))
  }
  val weekDays = remember(weekStart) {
    (0..6).map { weekStart.plusDays(it.toLong()) }
  }

  val today = LocalDate.now()
  val nowTime = LocalTime.now()

  SketchCard(
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "WEEK VIEW",
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary)
          )
          val startFormat = weekStart.format(DateTimeFormatter.ofPattern("MMM d"))
          val endFormat = weekDays.last().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
          Text(
            text = "$startFormat – $endFormat",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .border(1.dp, CardBorder, CircleShape)
              .clip(CircleShape)
              .background(CardBackground)
              .bounceClick(scaleDown = 0.90f) { onDateSelected(selectedDate.minusWeeks(1)) },
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Week", tint = InkPrimary, modifier = Modifier.size(16.dp))
          }

          Box(
            modifier = Modifier
              .size(32.dp)
              .border(1.dp, CardBorder, CircleShape)
              .clip(CircleShape)
              .background(CardBackground)
              .bounceClick(scaleDown = 0.90f) { onDateSelected(selectedDate.plusWeeks(1)) },
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next Week", tint = InkPrimary, modifier = Modifier.size(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 7-day strip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        weekDays.forEach { date ->
          val isSelected = date == selectedDate
          val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
          val dayTasks = tasks.filter { it.dueDate == dateStr }
          val hasOverdue = dayTasks.any { it.isOverdue(today, nowTime) }
          val taskCount = dayTasks.size
          val isToday = date == today

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .padding(vertical = 4.dp)
          ) {
            Text(
              text = date.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())).uppercase(),
              style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isToday) AccentTerracotta else InkSecondary)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .bounceClick(scaleDown = 0.90f) { onDateSelected(date) }
                .background(if (isSelected) PillBackgroundDark else Color.Transparent)
                .border(1.2.dp, if (isSelected) PillBackgroundDark else CardBorder.copy(alpha = 0.5f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = date.dayOfMonth.toString(),
                style = TextStyle(
                  fontSize = 14.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) PillTextDark else InkPrimary
                )
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (taskCount > 0) {
              Box(
                modifier = Modifier
                  .size(5.dp)
                  .background(if (hasOverdue) AccentRed else AccentTerracotta, CircleShape)
              )
            } else {
              Spacer(modifier = Modifier.height(5.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
fun DayViewSelector(
  selectedDate: LocalDate,
  onDateSelected: (LocalDate) -> Unit
) {
  val today = LocalDate.now()

  SketchCard(
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .border(1.2.dp, CardBorder, RoundedCornerShape(8.dp))
          .clip(RoundedCornerShape(8.dp))
          .bounceClick(scaleDown = 0.90f) { onDateSelected(selectedDate.minusDays(1)) },
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Day", tint = InkPrimary)
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())),
          style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
        )
        Text(
          text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())),
          style = TextStyle(fontSize = 12.sp, color = if (selectedDate == today) AccentTerracotta else InkSecondary)
        )
      }

      Box(
        modifier = Modifier
          .size(34.dp)
          .border(1.2.dp, CardBorder, RoundedCornerShape(8.dp))
          .clip(RoundedCornerShape(8.dp))
          .bounceClick(scaleDown = 0.90f) { onDateSelected(selectedDate.plusDays(1)) },
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = InkPrimary)
      }
    }
  }
}

fun formatTimeDisplay(timeStr: String, use24Hour: Boolean): String {
  return try {
    val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
    if (use24Hour) {
      time.format(DateTimeFormatter.ofPattern("HH:mm"))
    } else {
      time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
    }
  } catch (e: Exception) {
    timeStr
  }
}