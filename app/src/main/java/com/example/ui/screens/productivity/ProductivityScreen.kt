package com.example.ui.screens.productivity

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.ui.components.GlassSegmented
import com.example.ui.components.GlassInput
import com.example.ui.components.isReducedMotionEnabled
import com.example.ui.components.paperDotBackground
import com.example.ui.navigation.horizontalSwipeNavigation
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.habits.HabitListScreen
import com.example.ui.screens.tasks.TaskListScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProductivityScreen(
  viewModel: MainViewModel,
  onNavigateToAddTask: () -> Unit,
  onNavigateToAddHabit: () -> Unit,
  onTaskClick: (Task) -> Unit,
  onHabitClick: (Habit) -> Unit,
  onBackToHome: () -> Unit,
  onSwipeToTools: (() -> Unit)? = null,
  onQuickAdd: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Tasks, 1: Habits, 2: Calendar
  val context = LocalContext.current
  val reduceMotion = remember { isReducedMotionEnabled(context) }

  val taskSearchQuery by viewModel.taskSearchQuery.collectAsStateWithLifecycle()
  val habitSearchQuery by viewModel.habitSearchQuery.collectAsStateWithLifecycle()
  val isHabits = selectedTab == 1
  val currentSearchQuery = if (isHabits) habitSearchQuery else taskSearchQuery

  val subTabAnimProgress = remember { Animatable(0f) }
  PredictiveBackHandler(enabled = selectedTab > 0) { progressFlow ->
    try {
      progressFlow.collect { backEvent ->
        subTabAnimProgress.snapTo(backEvent.progress)
      }
      withContext(NonCancellable) {
        subTabAnimProgress.animateTo(
          targetValue = 0f,
          animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)
        )
      }
      selectedTab = 0
    } catch (_: CancellationException) {
      withContext(NonCancellable) {
        subTabAnimProgress.animateTo(
          targetValue = 0f,
          animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)
        )
      }
    }
  }

  val subTabProgress = subTabAnimProgress.value
  val subTabScale = 1f - (subTabProgress * 0.05f)
  val subTabCorner = (subTabProgress * 16).dp

  Column(
    modifier = modifier
      .fillMaxSize()
      .graphicsLayer {
        scaleX = subTabScale
        scaleY = subTabScale
        shape = RoundedCornerShape(subTabCorner)
        clip = subTabCorner > 0.dp
      }
      .paperDotBackground()
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(top = 10.dp)
      .horizontalSwipeNavigation(
        onSwipeLeft = {
          if (selectedTab < 2) {
            selectedTab += 1
          } else {
            onSwipeToTools?.invoke()
          }
        },
        onSwipeRight = {
          if (selectedTab > 0) {
            selectedTab -= 1
          } else {
            onBackToHome()
          }
        }
      )
  ) {
    // 1. Top Sub-Navbar ("Tasks", "Habits", "Calendar")
    GlassSegmented(
      options = listOf("Tasks", "Habits", "Calendar"),
      value = selectedTab,
      onChange = { selectedTab = it },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 2. Liquid Glass Search Bar
    GlassInput(
      query = currentSearchQuery,
      onQueryChange = { q ->
        if (isHabits) {
          viewModel.setHabitSearchQuery(q)
        } else {
          viewModel.setTaskSearchQuery(q)
        }
      },
      placeholder = when (selectedTab) {
        1 -> "Search habits..."
        2 -> "Search calendar tasks..."
        else -> "Search tasks, tags..."
      },
      isSearch = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    // 3. Tab Content (fills remaining space and scrolls smoothly above the bottom dock)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
          if (reduceMotion) {
            fadeIn(animationSpec = tween(80)) togetherWith fadeOut(animationSpec = tween(80))
          } else if (targetState > initialState) {
            (slideInHorizontally { (it * 0.25f).toInt() } + fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing))) togetherWith
              (slideOutHorizontally { (-it * 0.25f).toInt() } + fadeOut(animationSpec = tween(140, easing = FastOutSlowInEasing)))
          } else {
            (slideInHorizontally { (-it * 0.25f).toInt() } + fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing))) togetherWith
              (slideOutHorizontally { (it * 0.25f).toInt() } + fadeOut(animationSpec = tween(140, easing = FastOutSlowInEasing)))
          }
        },
        label = "productivityTab",
        modifier = Modifier.fillMaxSize()
      ) { tabIndex ->
        when (tabIndex) {
          0 -> {
            TaskListScreen(
              viewModel = viewModel,
              onNavigateToAddTask = onNavigateToAddTask,
              onTaskClick = onTaskClick,
              onBack = onBackToHome,
              showHeader = false
            )
          }
          1 -> {
            HabitListScreen(
              viewModel = viewModel,
              onNavigateToAddHabit = onNavigateToAddHabit,
              onHabitClick = onHabitClick,
              onBack = onBackToHome,
              showHeader = false
            )
          }
          2 -> {
            CalendarScreen(
              viewModel = viewModel,
              onNavigateToAddTask = onNavigateToAddTask,
              onTaskClick = onTaskClick,
              onHabitClick = onHabitClick,
              showHeader = false
            )
          }
        }
      }
    }
  }
}
