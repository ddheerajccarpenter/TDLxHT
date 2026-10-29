package com.example.ui.screens.productivity

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.ui.components.GlassSegmented
import com.example.ui.components.paperDotBackground
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.habits.HabitListScreen
import com.example.ui.screens.tasks.TaskListScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryDarkCanvas
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProductivityScreen(
  viewModel: MainViewModel,
  onNavigateToAddTask: () -> Unit,
  onNavigateToAddHabit: () -> Unit,
  onTaskClick: (Task) -> Unit,
  onHabitClick: (Habit) -> Unit,
  onBackToHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Tasks, 1: Habits, 2: Calendar

  val isDark = isSystemInDarkTheme()
  val separatorBrush = remember(isDark) {
    if (isDark) {
      Brush.horizontalGradient(
        0.0f to Color.Transparent,
        0.18f to Color(0x18999999),
        0.50f to Color(0x55999999),
        0.82f to Color(0x18999999),
        1.0f to Color.Transparent
      )
    } else {
      Brush.horizontalGradient(
        0.0f to Color.Transparent,
        0.18f to Color(0x14333333),
        0.50f to Color(0x45333333),
        0.82f to Color(0x14333333),
        1.0f to Color.Transparent
      )
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperDotBackground()
  ) {
    // Tab Content (Tasks / Habits / Calendar)
    AnimatedContent(
      targetState = selectedTab,
      transitionSpec = {
        fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) togetherWith
          fadeOut(animationSpec = tween(140, easing = FastOutSlowInEasing))
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
      // 1. Productivity Tab Nav Bar (Tasks | Habits | Calendar) positioned directly above the nav bar - Apple Liquid Glass
      GlassSegmented(
        items = listOf("Tasks", "Habits", "Calendar"),
        selectedIndex = selectedTab,
        onItemSelected = { selectedTab = it },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 2. Center-strong, edge-fading separator line BETWEEN productivity tab nav and main bottom nav
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .height(1.dp)
          .background(separatorBrush)
      )

      // 3. Clearance space matching the floating main navigation bar
      Spacer(modifier = Modifier.height(78.dp))
    }
  }
}
