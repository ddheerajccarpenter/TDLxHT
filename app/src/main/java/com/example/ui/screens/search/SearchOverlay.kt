package com.example.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassIconButton
import com.example.ui.glass.LocalHazeState
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.StrongFontFamily
import com.example.ui.viewmodel.MainViewModel
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import java.util.Locale

@Composable
fun SearchOverlay(
  viewModel: MainViewModel,
  onDismiss: () -> Unit,
  onTaskClick: (Task) -> Unit,
  onHabitClick: (Habit) -> Unit,
  modifier: Modifier = Modifier
) {
  val tasks by viewModel.tasks.collectAsStateWithLifecycle()
  val habits by viewModel.habits.collectAsStateWithLifecycle()
  val hazeState = LocalHazeState.current
  val isDark = LocalAppColors.current.isDark
  val context = LocalContext.current
  val reduceMotion = remember { com.example.ui.components.isReducedMotionEnabled(context) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current
  var query by remember { mutableStateOf("") }
  val dismissSearch = {
    keyboardController?.hide()
    focusManager.clearFocus()
    onDismiss()
  }
  val normalizedQuery = query.trim().lowercase(Locale.getDefault())
  val matchingTasks = remember(tasks, normalizedQuery) {
    if (normalizedQuery.isBlank()) emptyList() else tasks.filter { task ->
      task.title.contains(normalizedQuery, ignoreCase = true) ||
        task.notes.contains(normalizedQuery, ignoreCase = true) ||
        task.category.contains(normalizedQuery, ignoreCase = true) ||
        task.tags.any { it.contains(normalizedQuery, ignoreCase = true) }
    }
  }
  val matchingHabits = remember(habits, normalizedQuery) {
    if (normalizedQuery.isBlank()) emptyList() else habits.filter { habit ->
      !habit.isArchived && (
        habit.name.contains(normalizedQuery, ignoreCase = true) ||
          habit.description.contains(normalizedQuery, ignoreCase = true) ||
          habit.category.contains(normalizedQuery, ignoreCase = true)
        )
    }
  }

  BackHandler(onBack = dismissSearch)

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  Box(modifier = modifier.fillMaxSize()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .then(
          if (hazeState != null) {
            Modifier.hazeEffect(state = hazeState) {
              blurRadius = 18.dp
              tints = listOf(HazeTint(Color.Black.copy(alpha = if (isDark) 0.26f else 0.18f)))
              fallbackTint = HazeTint(Color.Black.copy(alpha = if (isDark) 0.34f else 0.25f))
            }
          } else Modifier.background(Color.Black.copy(alpha = if (isDark) 0.32f else 0.22f))
        )
        .background(Color.Black.copy(alpha = if (isDark) 0.16f else 0.12f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = dismissSearch
        )
    )

    AnimatedVisibility(
      visible = true,
      modifier = Modifier
        .fillMaxSize()
        .imePadding()
        .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
        .padding(horizontal = 16.dp, vertical = 16.dp),
      enter = if (reduceMotion) fadeIn(tween(1)) else fadeIn(tween(160)) +
        slideInVertically(tween(220)) { it / 28 } + scaleIn(
          initialScale = 0.985f,
          animationSpec = tween(220)
        )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.92f)
          .align(Alignment.TopCenter)
          .testTag("search_overlay"),
        verticalArrangement = Arrangement.Top
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          LiquidGlassCard(
            modifier = Modifier.weight(1f).height(64.dp),
            shape = RoundedCornerShape(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Search, contentDescription = null, tint = InkSecondary, modifier = Modifier.size(22.dp))
              BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                  .weight(1f)
                  .focusRequester(focusRequester)
                  .testTag("search_overlay_input"),
                singleLine = true,
                textStyle = TextStyle(
                  fontFamily = StrongFontFamily,
                  fontSize = 18.sp,
                  color = InkPrimary
                ),
                cursorBrush = SolidColor(InkPrimary),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                decorationBox = { innerTextField ->
                  Box {
                    if (query.isEmpty()) {
                      Text("Search tasks and habits", fontFamily = StrongFontFamily, fontSize = 16.sp, color = InkMuted)
                    }
                    innerTextField()
                  }
                }
              )
              if (query.isNotEmpty()) {
                LiquidGlassIconButton(
                  onClick = { query = "" },
                  contentDescription = "Clear search"
                ) {
                  Icon(Icons.Default.Close, contentDescription = null, tint = InkSecondary)
                }
              }
            }
          }
          SearchActionButton(onClick = {
            dismissSearch()
          })
        }

        Spacer(Modifier.height(12.dp))

        LiquidGlassCard(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          shape = RoundedCornerShape(24.dp)
        ) {
          LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            if (normalizedQuery.isBlank()) {
              item {
                Text(
                  "Type to search your tasks and habits",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                  fontFamily = StrongFontFamily,
                  fontSize = 14.sp,
                  color = InkMuted
                )
              }
            } else if (matchingTasks.isEmpty() && matchingHabits.isEmpty()) {
              item {
                Text(
                  "No matches",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                  fontFamily = StrongFontFamily,
                  fontSize = 15.sp,
                  color = InkSecondary
                )
              }
            } else {
              if (matchingTasks.isNotEmpty()) {
                item { SearchSectionLabel("Tasks") }
                items(matchingTasks, key = { "search-task-${it.id}" }) { task ->
                  SearchResultRow(
                    icon = Icons.Outlined.CheckCircleOutline,
                    title = task.title,
                    detail = listOf(task.category, task.notes.takeIf(String::isNotBlank))
                      .filterNotNull().filter(String::isNotBlank).distinct().joinToString(" · "),
                    onClick = {
                      keyboardController?.hide()
                      focusManager.clearFocus()
                      onTaskClick(task)
                    }
                  )
                }
              }
              if (matchingHabits.isNotEmpty()) {
                item { SearchSectionLabel("Habits") }
                items(matchingHabits, key = { "search-habit-${it.id}" }) { habit ->
                  SearchResultRow(
                    icon = Icons.Outlined.Repeat,
                    title = habit.name,
                    detail = listOf(habit.category, habit.description.takeIf(String::isNotBlank))
                      .filterNotNull().filter(String::isNotBlank).distinct().joinToString(" · "),
                    onClick = {
                      keyboardController?.hide()
                      focusManager.clearFocus()
                      onHabitClick(habit)
                    }
                  )
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
private fun SearchActionButton(onClick: () -> Unit) {
  LiquidGlassCard(
    modifier = Modifier
      .size(48.dp)
      .testTag("search_overlay_close_button"),
    shape = CircleShape,
    onClick = onClick
  ) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Icon(Icons.Default.Close, contentDescription = "Close search", tint = InkPrimary, modifier = Modifier.size(20.dp))
    }
  }
}

@Composable
private fun SearchSectionLabel(label: String) {
  Text(
    label,
    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
    fontFamily = StrongFontFamily,
    fontSize = 13.sp,
    fontWeight = FontWeight.SemiBold,
    color = InkMuted
  )
}

@Composable
private fun SearchResultRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  detail: String,
  onClick: () -> Unit
) {
  LiquidGlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .height(64.dp),
    shape = RoundedCornerShape(16.dp),
    onClick = onClick
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(icon, contentDescription = null, tint = InkSecondary, modifier = Modifier.size(20.dp))
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
        Text(title, fontFamily = StrongFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = InkPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (detail.isNotBlank()) {
          Text(detail, fontFamily = StrongFontFamily, fontSize = 12.sp, color = InkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
      }
      Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = InkMuted, modifier = Modifier.size(16.dp))
    }
  }
}
