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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.components.motionBlurOnScroll
import com.example.ui.components.UndoConfirmationPill
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.util.DashboardLayoutManager
import com.example.util.DashboardSection
import com.example.ui.theme.OuterCornerRadius
import com.example.ui.theme.InnerCornerRadius
import com.example.ui.theme.OuterCornerShape
import com.example.ui.theme.InnerCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.model.TaskPriority
import com.example.ui.components.AppEmptyState
import com.example.ui.components.AppHeader
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.FloatingNavContentBottomPadding
import com.example.ui.components.SketchCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassCapsule
import com.example.ui.components.LiquidGlassSegmentedControl
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassIconButton
import com.example.ui.components.LiquidGlassProgressIndicator
import com.example.ui.components.GlassSegmented
import com.example.ui.components.BentoHeroCard
import com.example.ui.components.CircularProgressGauge
import com.example.ui.components.PriorityPill
import com.example.ui.components.StreakFlameBadge
import com.example.ui.components.fluidLensedGlass
import com.example.ui.components.SketchCheckbox
import com.example.ui.components.SketchProgressBar
import com.example.ui.components.SketchSegmentedControl
import com.example.ui.components.paperDotBackground
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.PrimaryAccentLime
import com.example.ui.theme.OklchIndigo
import com.example.ui.theme.OklchEmerald
import com.example.ui.theme.OklchAmber
import com.example.ui.theme.OklchCoral
import com.example.ui.theme.OklchCyan
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

  val sectionOrder by DashboardLayoutManager.sectionOrder.collectAsStateWithLifecycle()
  val sectionVisibility by DashboardLayoutManager.sectionVisibility.collectAsStateWithLifecycle()
  var editOrder by remember(isDashboardEditMode, sectionOrder) { mutableStateOf(sectionOrder) }
  var editVisibility by remember(isDashboardEditMode, sectionVisibility) { mutableStateOf(sectionVisibility) }
  var draggedSectionId by remember { mutableStateOf<String?>(null) }
  var dragAccumulatedY by remember { mutableFloatStateOf(0f) }

  val today = LocalDate.now()
  val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
  val todayFormattedDate = today.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))

  val todayTasks = remember(tasks, today) {
    tasks.filter { it.isDueToday(today) }
  }

  val todayHabits = remember(habits, today) {
    habits.filter { it.isActive && !it.isArchived && !it.isPaused && it.isScheduledOn(today) }
  }

  val isDark = com.example.ui.theme.LocalAppColors.current.isDark

  val scheduledTasks = remember(todayTasks) {
    todayTasks.filter { it.dueTime.isNotBlank() }
      .sortedBy { it.dueTime }
  }

  val todayHabitRecords by viewModel.todayHabitRecords.collectAsStateWithLifecycle()

  val completedHabitIds = remember(completionRecords, todayStr) {
    completionRecords
      .filter { it.date == todayStr && it.isCompleted }
      .map { it.habitId }
      .toSet()
  }

  val prioritySpotlightTask = remember(todayTasks) {
    todayTasks.firstOrNull { !it.isCompleted && (it.priority == TaskPriority.HIGH || it.priority == TaskPriority.MEDIUM) }
  }

  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  var showUndoPill by remember { mutableStateOf(false) }
  var lastCompletedTaskId by remember { mutableStateOf<String?>(null) }
  var undoDismissJob by remember { mutableStateOf<Job?>(null) }

  val handleTaskToggle: (Task) -> Unit = { task ->
    val willBeCompleted = !task.isCompleted
    viewModel.toggleTaskCompletion(task.id)
    if (willBeCompleted) {
      lastCompletedTaskId = task.id
      showUndoPill = true
      com.example.util.SoundManager.playTaskComplete(context)
      com.example.util.HapticManager.taskCompleted(context)
      undoDismissJob?.cancel()
      undoDismissJob = coroutineScope.launch {
        delay(3500)
        showUndoPill = false
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperDotBackground()
  ) {
    LazyColumn(
      state = listState,
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
              LiquidGlassButton(
                onClick = {
                  if (isDashboardEditMode) {
                    DashboardLayoutManager.saveLayout(context, editOrder, editVisibility)
                  }
                  isDashboardEditMode = !isDashboardEditMode
                },
                modifier = Modifier.testTag("home_edit_mode_btn")
              ) {
                Text(
                  text = if (isDashboardEditMode) "Done" else "Layout",
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDashboardEditMode) PrimaryAccentLime else InkPrimary
                  )
                )
              }
            }
          },
          rightContent = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (onNavigateToSearch != null) {
                LiquidGlassIconButton(
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

              LiquidGlassIconButton(
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
        // 1. Layout Editor Header Card
        item {
          SketchCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = OuterCornerRadius
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Text(
                text = "DASHBOARD LAYOUT",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = OklchIndigo
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Reorder sections and toggle visibility. Changes persist across app reinstalls.",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 13.sp,
                  color = InkSecondary
                )
              )
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        // 2. Sections Reordering & Visibility List
        itemsIndexed(editOrder, key = { _, s -> s.id }) { index, section ->
          val isVisible = editVisibility[section] != false
          val isFirst = index == 0
          val isLast = index == editOrder.size - 1
          val isBeingDragged = draggedSectionId == section.id
          val dragScale by animateFloatAsState(
            targetValue = if (isBeingDragged) 1.03f else 1.0f,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
            label = "dragCardScale"
          )

          SketchCard(
            modifier = Modifier
              .fillMaxWidth()
              .graphicsLayer {
                scaleX = dragScale
                scaleY = dragScale
              }
              .animateItem(),
            cornerRadius = InnerCornerRadius,
            borderWidth = if (isBeingDragged) 1.5.dp else 1.dp,
            borderColor = if (isBeingDragged) InkPrimary else if (isVisible) CardBorder else CardBorder.copy(alpha = 0.4f)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Touch Drag Handle
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(
                    if (isBeingDragged) (if (isDark) Color(0x33FFFFFF) else Color(0x22000000))
                    else Color.Transparent
                  )
                  .pointerInput(editOrder) {
                    detectVerticalDragGestures(
                      onDragStart = {
                        draggedSectionId = section.id
                        dragAccumulatedY = 0f
                        com.example.util.VibrationHelper.tick(context)
                      },
                      onDragEnd = {
                        draggedSectionId = null
                        dragAccumulatedY = 0f
                      },
                      onDragCancel = {
                        draggedSectionId = null
                        dragAccumulatedY = 0f
                      },
                      onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulatedY += dragAmount
                        val thresholdPx = 48.dp.toPx()
                        val curIdx = editOrder.indexOfFirst { it.id == section.id }
                        if (curIdx != -1) {
                          if (dragAccumulatedY > thresholdPx && curIdx < editOrder.size - 1) {
                            val newOrder = editOrder.toMutableList()
                            val temp = newOrder[curIdx]
                            newOrder[curIdx] = newOrder[curIdx + 1]
                            newOrder[curIdx + 1] = temp
                            editOrder = newOrder
                            dragAccumulatedY = 0f
                            com.example.util.VibrationHelper.tick(context)
                            com.example.util.SoundManager.playSwipeNav(context)
                          } else if (dragAccumulatedY < -thresholdPx && curIdx > 0) {
                            val newOrder = editOrder.toMutableList()
                            val temp = newOrder[curIdx]
                            newOrder[curIdx] = newOrder[curIdx - 1]
                            newOrder[curIdx - 1] = temp
                            editOrder = newOrder
                            dragAccumulatedY = 0f
                            com.example.util.VibrationHelper.tick(context)
                            com.example.util.SoundManager.playSwipeNav(context)
                          }
                        }
                      }
                    )
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.DragHandle,
                  contentDescription = "Drag to reorder",
                  tint = if (isBeingDragged) InkPrimary else InkSecondary.copy(alpha = 0.6f),
                  modifier = Modifier.size(20.dp)
                )
              }

              // Up / Down Reorder Arrows
              Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Box(
                  modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (!isFirst) (if (isDark) Color(0x18FFFFFF) else Color(0x10000000)) else Color.Transparent)
                    .clickable(enabled = !isFirst) {
                      if (!isFirst) {
                        val newOrder = editOrder.toMutableList()
                        val temp = newOrder[index]
                        newOrder[index] = newOrder[index - 1]
                        newOrder[index - 1] = temp
                        editOrder = newOrder
                        com.example.util.VibrationHelper.tick(context)
                        com.example.util.SoundManager.playSwipeNav(context)
                      }
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Move Up",
                    tint = if (!isFirst) InkPrimary else InkSecondary.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                  )
                }

                Box(
                  modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (!isLast) (if (isDark) Color(0x18FFFFFF) else Color(0x10000000)) else Color.Transparent)
                    .clickable(enabled = !isLast) {
                      if (!isLast) {
                        val newOrder = editOrder.toMutableList()
                        val temp = newOrder[index]
                        newOrder[index] = newOrder[index + 1]
                        newOrder[index + 1] = temp
                        editOrder = newOrder
                        com.example.util.VibrationHelper.tick(context)
                        com.example.util.SoundManager.playSwipeNav(context)
                      }
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Move Down",
                    tint = if (!isLast) InkPrimary else InkSecondary.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                  )
                }
              }

              // Section Info
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = section.title,
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isVisible) InkPrimary else InkSecondary
                  )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = section.subtitle,
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 12.sp,
                    color = InkSecondary
                  ),
                  maxLines = 1
                )
              }

              // Visibility Eye Toggle
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(
                    if (isVisible) (if (isDark) Color(0x22FFFFFF) else Color(0x14000000))
                    else Color.Transparent
                  )
                  .clickable {
                    val map = editVisibility.toMutableMap()
                    map[section] = !isVisible
                    editVisibility = map
                    com.example.util.VibrationHelper.click(context)
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = if (isVisible) "Hide section" else "Show section",
                  tint = if (isVisible) OklchIndigo else InkSecondary.copy(alpha = 0.5f),
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        // 3. Action Buttons Row (Reset / Cancel / Save)
        item {
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Reset Button
            Box(
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .fluidLensedGlass(shape = InnerCornerShape, elevation = 2.dp)
                .clickable {
                  editOrder = DashboardSection.DEFAULT_ORDER
                  editVisibility = DashboardSection.entries.associateWith { true }
                  com.example.util.VibrationHelper.click(context)
                },
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = null,
                  tint = InkSecondary,
                  modifier = Modifier.size(16.dp)
                )
                Text("Reset", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkSecondary))
              }
            }

            // Cancel Button
            Box(
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .fluidLensedGlass(shape = InnerCornerShape, elevation = 2.dp)
                .clickable {
                  editOrder = sectionOrder
                  editVisibility = sectionVisibility
                  isDashboardEditMode = false
                  com.example.util.VibrationHelper.click(context)
                },
              contentAlignment = Alignment.Center
            ) {
              Text("Cancel", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkSecondary))
            }

            // Save & Apply Button (Authentic Liquid Glass)
            Box(
              modifier = Modifier
                .weight(1.2f)
                .height(44.dp)
                .fluidLensedGlass(
                  shape = InnerCornerShape,
                  elevation = 3.dp,
                  borderWidth = 0.85.dp,
                  isElevated = true,
                  isInteractive = true,
                  onClick = {
                    DashboardLayoutManager.saveLayout(context, editOrder, editVisibility)
                    isDashboardEditMode = false
                    com.example.util.VibrationHelper.click(context)
                  }
                ),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = PrimaryAccentLime,
                  modifier = Modifier.size(16.dp)
                )
                Text("Save", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryAccentLime))
              }
            }
          }
          Spacer(modifier = Modifier.height(24.dp))
        }
      } else {
        if (sectionVisibility[DashboardSection.BENTO_HERO] != false) {
          // BENTO HERO DASHBOARD (Translucent Smoked Glass + Zero-Blur Optical Gauge)
          item {
            LiquidGlassCard(
              modifier = Modifier
                .fillMaxWidth()
                .heroEntrance(delayMs = 100),
              shape = RoundedCornerShape(24.dp)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(18.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Authentic Zero-Blur Circular Progress Gauge
                  LiquidGlassProgressIndicator(
                    progress = summary.overallProgress,
                    modifier = Modifier.size(72.dp),
                    strokeWidth = 7.dp,
                    progressColor = OklchIndigo
                  )

                  Spacer(modifier = Modifier.width(16.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = "TODAY'S MOMENTUM",
                        style = TextStyle(
                          fontFamily = SFProFontFamily,
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold,
                          letterSpacing = 1.sp,
                          color = OklchIndigo
                        )
                      )

                      if (summary.maxStreak > 0) {
                        StreakFlameBadge(streakCount = summary.maxStreak)
                      }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                      text = "${summary.tasksCompleted}/${summary.tasksTotal} Tasks • ${summary.habitsCompleted}/${summary.habitsTotal} Habits",
                      style = TextStyle(
                        fontFamily = SFProFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                      )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                      text = todayFormattedDate,
                      style = TextStyle(
                        fontSize = 12.sp,
                        color = InkSecondary
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SketchProgressBar(
                  progress = summary.overallProgress,
                  modifier = Modifier.fillMaxWidth(),
                  height = 6.dp
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))
          }
        }

        if (sectionVisibility[DashboardSection.QUICK_METRICS] != false) {
          // QUICK METRICS 4-STAT UNIFIED GLASS CAPSULE
          item {
            LiquidGlassCapsule(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(20.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Metric 1: Tasks
                Column(
                  modifier = Modifier.weight(1f),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text("TASKS", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = InkSecondary))
                  Spacer(modifier = Modifier.height(3.dp))
                  Text("${todayTasks.count { it.isCompleted }}/${todayTasks.size}", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
                }

                Box(modifier = Modifier.width(0.75.dp).height(26.dp).background(Color(0x18FFFFFF)))

                // Metric 2: Habits
                Column(
                  modifier = Modifier.weight(1f),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text("HABITS", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = InkSecondary))
                  Spacer(modifier = Modifier.height(3.dp))
                  Text("${todayHabits.count { completedHabitIds.contains(it.id) }}/${todayHabits.size}", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
                }

                Box(modifier = Modifier.width(0.75.dp).height(26.dp).background(Color(0x18FFFFFF)))

                // Metric 3: Streak
                Column(
                  modifier = Modifier.weight(1f),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text("STREAK", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = InkSecondary))
                  Spacer(modifier = Modifier.height(3.dp))
                  Text("${summary.maxStreak}d", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
                }

                Box(modifier = Modifier.width(0.75.dp).height(26.dp).background(Color(0x18FFFFFF)))

                // Metric 4: Rate
                Column(
                  modifier = Modifier.weight(1f),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text("RATE", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = InkSecondary))
                  Spacer(modifier = Modifier.height(3.dp))
                  Text("${(summary.overallProgress * 100).toInt()}%", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OklchIndigo))
                }
              }
            }
            Spacer(modifier = Modifier.height(16.dp))
          }
        }

        if (sectionVisibility[DashboardSection.DAILY_PLANNER] != false) {

      // PRIORITY SPOTLIGHT (If uncompleted High/Med priority task is scheduled for today)
      if (prioritySpotlightTask != null && (selectedTab == 0 || selectedTab == 1)) {
        item {
          BentoHeroCard(
            modifier = Modifier
              .fillMaxWidth()
              .bounceClick { onTaskClick(prioritySpotlightTask) },
            accentColor = if (prioritySpotlightTask.priority == TaskPriority.HIGH) OklchCoral else OklchAmber,
            cornerRadius = 16.dp
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                SketchCheckbox(
                  checked = prioritySpotlightTask.isCompleted,
                  onCheckedChange = { handleTaskToggle(prioritySpotlightTask) }
                )
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                      text = "PRIORITY SPOTLIGHT",
                      style = TextStyle(
                        fontFamily = SFProFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = if (prioritySpotlightTask.priority == TaskPriority.HIGH) OklchCoral else OklchAmber
                      )
                    )
                    PriorityPill(priority = prioritySpotlightTask.priority)
                  }
                  Text(
                    text = prioritySpotlightTask.title,
                    style = TextStyle(
                      fontFamily = SFProFontFamily,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = InkPrimary
                    ),
                    maxLines = 1
                  )
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }
      }

      // Filter Segment: [All] [Tasks] [Habits] - Liquid Glass Segmented with Gliding Curved Lens
      item {
        LiquidGlassSegmentedControl(
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
              LiquidGlassButton(
                onClick = {
                  val next = if (todayTasksViewMode == "GRID") "LIST" else "GRID"
                  AppPreferences.setTodayTasksViewMode(context, next)
                },
                shape = RoundedCornerShape(10.dp)
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

              LiquidGlassButton(
                onClick = onNavigateToAddTask,
                shape = RoundedCornerShape(10.dp),
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
                    onToggle = { handleTaskToggle(task) },
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
                onToggle = { handleTaskToggle(task) },
                onClick = { onTaskClick(task) },
                onDelete = { taskToDelete = task },
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
          }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }
      }
      } // end DAILY_PLANNER

      if (sectionVisibility[DashboardSection.TIME_BLOCKS] != false) {
        // TIME BLOCKS SECTION
        item {
          LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "TIME BLOCKS",
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = OklchIndigo
                  )
                )
                Text(
                  text = "${scheduledTasks.size} Scheduled",
                  style = TextStyle(fontSize = 12.sp, color = InkSecondary)
                )
              }
              Spacer(modifier = Modifier.height(10.dp))
              if (scheduledTasks.isEmpty()) {
                Text(
                  text = "No time-blocked tasks scheduled for today. Add a due time to schedule tasks in your day.",
                  style = TextStyle(fontSize = 13.sp, color = InkSecondary)
                )
              } else {
                scheduledTasks.take(4).forEach { task ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(InnerCornerShape)
                      .clickable { onTaskClick(task) }
                      .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .clip(CircleShape)
                        .background(OklchIndigo.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                      Text(
                        text = task.dueTime ?: "",
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OklchIndigo)
                      )
                    }
                    Text(
                      text = task.title,
                      style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (task.isCompleted) InkSecondary else InkPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                      ),
                      maxLines = 1,
                      modifier = Modifier.weight(1f)
                    )
                  }
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(18.dp))
        }
      }

      // TODAY'S HABITS SECTION
      if (sectionVisibility[DashboardSection.HABIT_STREAKS] != false) {
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
              LiquidGlassButton(
                onClick = {
                  val next = if (todayHabitsViewMode == "GRID") "LIST" else "GRID"
                  AppPreferences.setTodayHabitsViewMode(context, next)
                },
                shape = RoundedCornerShape(10.dp)
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

              LiquidGlassButton(
                onClick = onNavigateToAddHabit,
                shape = RoundedCornerShape(10.dp),
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
                    onToggle = {
                      viewModel.toggleHabitToday(habit.id)
                      com.example.util.SoundManager.playHabitTick(context)
                      com.example.util.HapticManager.sliderSnap(context)
                    },
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
                onToggle = {
                  viewModel.toggleHabitToday(habit.id)
                  com.example.util.SoundManager.playHabitTick(context)
                  com.example.util.HapticManager.sliderSnap(context)
                },
                onClick = { onHabitClick(habit) },
                onDelete = { habitToDelete = habit },
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
          }
        }
      }
      } // end HABIT_STREAKS
      } // end else (!isDashboardEditMode)
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

    // Floating Undo Confirmation Pill (Liquid Glass elevation)
    UndoConfirmationPill(
      visible = showUndoPill,
      message = "Task completed",
      onUndo = {
        lastCompletedTaskId?.let { id ->
          viewModel.toggleTaskCompletion(id)
        }
        showUndoPill = false
      },
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = FloatingNavContentBottomPadding + 8.dp)
    )
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
    targetValue = if (task.isCompleted) InkSecondary else InkPrimary,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "taskTextCol"
  )
  val cardAlpha by animateFloatAsState(
    targetValue = if (task.isCompleted) 0.92f else 1.0f,
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
