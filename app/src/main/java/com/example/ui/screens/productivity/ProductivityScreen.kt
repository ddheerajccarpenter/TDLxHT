package com.example.ui.screens.productivity

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.ui.components.LiquidGlassSegmentedControl
import com.example.ui.components.isReducedMotionEnabled
import com.example.ui.components.paperDotBackground
import com.example.ui.navigation.horizontalSwipeNavigation
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.habits.HabitListScreen
import com.example.ui.screens.tasks.TaskListScreen
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
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Tasks, 1: Habits, 2: Calendar
  val context = LocalContext.current
  val reduceMotion = remember { isReducedMotionEnabled(context) }

  BackHandler(enabled = selectedTab > 0) {
    selectedTab = 0
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperDotBackground()
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
    // Tab Content (Tasks / Habits / Calendar)
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
            onBack = onBackToHome
          )
        }
        1 -> {
          HabitListScreen(
            viewModel = viewModel,
            onNavigateToAddHabit = onNavigateToAddHabit,
            onHabitClick = onHabitClick,
            onBack = onBackToHome
          )
        }
        2 -> {
          CalendarScreen(
            viewModel = viewModel,
            onNavigateToAddTask = onNavigateToAddTask,
            onTaskClick = onTaskClick,
            onHabitClick = onHabitClick
          )
        }
      }
    }

    // Bottom Controls: Productivity Tab Nav Bar + Horizontal Divider Line above Main Bottom Nav
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.navigationBars),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Secondary productivity tabs sit just above the fixed main dock.
      LiquidGlassSegmentedControl(
        items = listOf("Tasks", "Habits", "Calendar"),
        selectedIndex = selectedTab,
        onItemSelected = { selectedTab = it },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(124.dp))
    }
  }
}
