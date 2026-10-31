package com.example.ui.screens.search

import com.example.ui.theme.SFProFontFamily
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DeleteOutline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.ui.components.AppEmptyState
import com.example.ui.components.AppHeader
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.SketchCard
import com.example.ui.components.SketchCheckbox
import com.example.ui.components.SketchFilterChip
import com.example.ui.components.SketchSearchBar
import com.example.ui.components.bounceClick
import com.example.ui.components.sectionEntrance
import com.example.ui.components.staggeredEntrance
import com.example.ui.components.paperDotBackground
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.AccentYellowHighlight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
  viewModel: MainViewModel,
  onTaskClick: (Task) -> Unit,
  onHabitClick: (Habit) -> Unit,
  onBack: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val tasks by viewModel.tasks.collectAsStateWithLifecycle()
  val habits by viewModel.habits.collectAsStateWithLifecycle()
  val completionRecords by viewModel.completionRecords.collectAsStateWithLifecycle()

  var query by remember { mutableStateOf("") }
  var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Tasks, 2: Habits, 3: Notes

  // Multi-select state
  var isMultiSelectMode by remember { mutableStateOf(false) }
  var selectedTaskIds by remember { mutableStateOf(setOf<String>()) }
  var selectedHabitIds by remember { mutableStateOf(setOf<String>()) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
  val todayCompletedHabitIds = remember(completionRecords, todayStr) {
    completionRecords.filter { it.date == todayStr && it.isCompleted }.map { it.habitId }.toSet()
  }

  val trimmedQuery = query.trim().lowercase()

  val matchingTasks = remember(tasks, trimmedQuery, selectedFilterTab) {
    if (trimmedQuery.isEmpty()) {
      tasks
    } else {
      tasks.filter { task ->
        when (selectedFilterTab) {
          3 -> task.notes.lowercase().contains(trimmedQuery)
          else -> task.title.lowercase().contains(trimmedQuery) ||
                  task.notes.lowercase().contains(trimmedQuery) ||
                  task.category.lowercase().contains(trimmedQuery) ||
                  task.tags.any { it.lowercase().contains(trimmedQuery) }
        }
      }
    }
  }

  val matchingHabits = remember(habits, trimmedQuery, selectedFilterTab) {
    if (selectedFilterTab == 3) {
      emptyList()
    } else if (trimmedQuery.isEmpty()) {
      habits.filter { !it.isArchived }
    } else {
      habits.filter { habit ->
        !habit.isArchived && (
          habit.name.lowercase().contains(trimmedQuery) ||
          habit.description.lowercase().contains(trimmedQuery) ||
          habit.category.lowercase().contains(trimmedQuery)
        )
      }
    }
  }

  val totalCount = matchingTasks.size + matchingHabits.size
  val totalSelectedCount = selectedTaskIds.size + selectedHabitIds.size

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperDotBackground()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = com.example.ui.components.FloatingNavContentBottomPadding)
    ) {
      if (onBack != null) {
        item {
          AppHeader(
            title = "Search",
            onBack = onBack
          )
          Spacer(modifier = Modifier.height(10.dp))
        }
      }

      // Top Search Input Box (Standardized & Accessible)
      item {
        SketchSearchBar(
          query = query,
          onQueryChange = { query = it },
          placeholder = "Search tasks, habits...",
          modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))
      }

      // Filter Chips
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            "All (${if (trimmedQuery.isEmpty()) tasks.size + habits.size else totalCount})" to 0,
            "Tasks (${matchingTasks.size})" to 1,
            "Habits (${matchingHabits.size})" to 2,
            "Notes" to 3
          ).forEach { (label, index) ->
            val isSelected = selectedFilterTab == index
            SketchFilterChip(
              selected = isSelected,
              onClick = { selectedFilterTab = index },
              label = label
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
      }

      // MULTI-SELECT ACTIVE BANNER & ACTIONS
      if (isMultiSelectMode) {
        item {
          SketchCard(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
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
              Column {
                Text(
                  text = "$totalSelectedCount selected",
                  style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentTerracotta
                  )
                )
                val allSelected = (matchingTasks.isNotEmpty() && selectedTaskIds.size == matchingTasks.size) &&
                                  (matchingHabits.isEmpty() || selectedHabitIds.size == matchingHabits.size)
                Text(
                  text = if (allSelected) "Deselect All" else "Select All",
                  style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkSecondary,
                    textDecoration = TextDecoration.Underline
                  ),
                  modifier = Modifier
                    .padding(top = 2.dp)
                    .bounceClick {
                      if (allSelected) {
                        selectedTaskIds = emptySet()
                        selectedHabitIds = emptySet()
                      } else {
                        selectedTaskIds = matchingTasks.map { it.id }.toSet()
                        selectedHabitIds = matchingHabits.map { it.id }.toSet()
                      }
                    }
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                // Cancel
                Text(
                  text = "Cancel",
                  style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkSecondary
                  ),
                  modifier = Modifier
                    .bounceClick {
                      isMultiSelectMode = false
                      selectedTaskIds = emptySet()
                      selectedHabitIds = emptySet()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Delete Button
                Box(
                  modifier = Modifier
                    .background(
                      if (totalSelectedCount > 0) AccentTerracotta else AccentTerracotta.copy(alpha = 0.35f),
                      RoundedCornerShape(10.dp)
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .bounceClick {
                      if (totalSelectedCount > 0) {
                        showDeleteDialog = true
                      }
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Outlined.DeleteOutline,
                      contentDescription = "Delete",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Delete ($totalSelectedCount)",
                      style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
        }
      }

      // TASK MATCHES SECTION (Matching screenshot 3)
      if (selectedFilterTab == 0 || selectedFilterTab == 1 || selectedFilterTab == 3) {
        if (matchingTasks.isNotEmpty()) {
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .sectionEntrance(delayMs = 40)
                .padding(bottom = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "TASK MATCHES",
                style = TextStyle(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = InkSecondary
                )
              )

              Text(
                text = "${matchingTasks.size} results",
                style = TextStyle(
                  fontSize = 12.sp,
                  color = InkSecondary
                )
              )
            }
          }

          items(matchingTasks, key = { it.id }) { task ->
            val isSelected = selectedTaskIds.contains(task.id)
            val taskIndex = matchingTasks.indexOf(task)
            Box(modifier = Modifier.animateItem().staggeredEntrance(index = taskIndex)) {
              SearchTaskCard(
                task = task,
                highlightQuery = trimmedQuery,
                isMultiSelectMode = isMultiSelectMode,
                isSelected = isSelected,
                onToggleSelect = {
                  selectedTaskIds = if (isSelected) {
                    selectedTaskIds - task.id
                  } else {
                    selectedTaskIds + task.id
                  }
                },
                onToggle = { viewModel.toggleTaskCompletion(task.id) },
                onClick = { onTaskClick(task) },
                onLongClick = {
                  isMultiSelectMode = true
                  selectedTaskIds = selectedTaskIds + task.id
                },
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
          }

          item { Spacer(modifier = Modifier.height(18.dp)) }
        }
      }

      // HABIT MATCH SECTION (Matching screenshot 3)
      if (selectedFilterTab == 0 || selectedFilterTab == 2) {
        if (matchingHabits.isNotEmpty()) {
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .sectionEntrance(delayMs = 60)
                .padding(bottom = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "HABIT MATCH",
                style = TextStyle(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = InkSecondary
                )
              )

              val streakInfo = viewModel.getHabitStreak(matchingHabits.first())
              if (streakInfo.currentStreak > 0) {
                Text(
                  text = "${streakInfo.currentStreak} day streak",
                  style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                  )
                )
              }
            }
          }

          items(matchingHabits, key = { it.id }) { habit ->
            val isCompleted = todayCompletedHabitIds.contains(habit.id)
            val streakInfo = viewModel.getHabitStreak(habit)
            val isSelected = selectedHabitIds.contains(habit.id)
            val habitIndex = matchingHabits.indexOf(habit)

            Box(modifier = Modifier.animateItem().staggeredEntrance(index = habitIndex)) {
              SearchHabitCard(
                habit = habit,
                isCompleted = isCompleted,
                streakDays = streakInfo.currentStreak,
                isMultiSelectMode = isMultiSelectMode,
                isSelected = isSelected,
                onToggleSelect = {
                  selectedHabitIds = if (isSelected) {
                    selectedHabitIds - habit.id
                  } else {
                    selectedHabitIds + habit.id
                  }
                },
                onToggle = { viewModel.toggleHabitToday(habit.id) },
                onClick = { onHabitClick(habit) },
                onLongClick = {
                  isMultiSelectMode = true
                  selectedHabitIds = selectedHabitIds + habit.id
                },
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
          }
        }
      }

      // Empty State
      if (matchingTasks.isEmpty() && matchingHabits.isEmpty()) {
        item {
          AppEmptyState(
            icon = Icons.Default.Search,
            title = if (trimmedQuery.isEmpty()) "No items" else "No matches",
            subtitle = if (trimmedQuery.isNotEmpty()) "\"$query\"" else ""
          )
        }
      }
    }
  }

  // Batch Delete Confirmation Dialog
  if (showDeleteDialog) {
    DeleteConfirmationDialog(
      title = "Delete $totalSelectedCount items?",
      message = "Are you sure you want to permanently delete these selected items? This action cannot be undone.",
      onConfirm = {
        if (selectedTaskIds.isNotEmpty()) {
          viewModel.deleteTasksBatch(selectedTaskIds.toList())
        }
        if (selectedHabitIds.isNotEmpty()) {
          viewModel.deleteHabitsBatch(selectedHabitIds.toList())
        }
        selectedTaskIds = emptySet()
        selectedHabitIds = emptySet()
        isMultiSelectMode = false
        showDeleteDialog = false
      },
      onDismiss = { showDeleteDialog = false }
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchTaskCard(
  task: Task,
  highlightQuery: String,
  isMultiSelectMode: Boolean = false,
  isSelected: Boolean = false,
  onToggleSelect: () -> Unit = {},
  onToggle: () -> Unit,
  onClick: () -> Unit,
  onLongClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val contentAlpha by animateFloatAsState(
    targetValue = if (task.isCompleted && !isMultiSelectMode) 0.55f else 1.0f,
    animationSpec = tween(150, easing = FastOutSlowInEasing),
    label = "searchTaskAlpha"
  )

  SketchCard(
    modifier = modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = {
          if (isMultiSelectMode) onToggleSelect() else onClick()
        },
        onLongClick = {
          onLongClick()
        }
      )
      .testTag("search_task_${task.id}"),
    backgroundColor = if (isSelected) AccentTerracotta.copy(alpha = 0.14f) else CardBackground,
    borderColor = if (isSelected) AccentTerracotta else CardBorder,
    borderWidth = if (isSelected) 1.dp else 0.75.dp,
    cornerRadius = 14.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(end = 8.dp)
          .alpha(contentAlpha)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Highlighted Title
          Text(
            text = buildHighlightedString(task.title, highlightQuery),
            style = TextStyle(
              fontSize = 17.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (task.isCompleted) InkMuted else InkPrimary,
              textDecoration = if (task.isCompleted && !isMultiSelectMode) TextDecoration.LineThrough else TextDecoration.None
            )
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Timing pill: "Today", "Weekly", etc.
          Box(
            modifier = Modifier
              .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            val label = if (task.isDueToday()) "Today" else if (task.repeatRule.name != "NONE") task.repeatRule.displayName else task.dueDate
            Text(
              text = label,
              style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
            )
          }
        }

        if (task.notes.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = buildHighlightedString(task.notes, highlightQuery),
            style = TextStyle(
              fontSize = 13.sp,
              color = InkSecondary
            )
          )
        }
      }

      if (isMultiSelectMode) {
        // Multi-select checkbox
        Box(
          modifier = Modifier
            .size(24.dp)
            .border(
              width = 1.8.dp,
              color = if (isSelected) AccentTerracotta else CardBorder,
              shape = CircleShape
            )
            .background(
              color = if (isSelected) AccentTerracotta else Color.Transparent,
              shape = CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          if (isSelected) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Selected",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchHabitCard(
  habit: Habit,
  isCompleted: Boolean,
  streakDays: Int,
  isMultiSelectMode: Boolean = false,
  isSelected: Boolean = false,
  onToggleSelect: () -> Unit = {},
  onToggle: () -> Unit,
  onClick: () -> Unit,
  onLongClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val textColor by animateColorAsState(
    targetValue = if (isCompleted && !isMultiSelectMode) InkSecondary else InkPrimary,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "searchHabitTextCol"
  )

  SketchCard(
    modifier = modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = {
          if (isMultiSelectMode) onToggleSelect() else onClick()
        },
        onLongClick = {
          onLongClick()
        }
      )
      .testTag("search_habit_${habit.id}"),
    backgroundColor = if (isSelected) AccentTerracotta.copy(alpha = 0.14f) else CardBackground,
    borderColor = if (isSelected) AccentTerracotta else CardBorder,
    borderWidth = if (isSelected) 1.dp else 0.75.dp,
    cornerRadius = 14.dp
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
              textDecoration = if (isCompleted && !isMultiSelectMode) TextDecoration.LineThrough else TextDecoration.None
            )
          )

          if (streakDays > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
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

        Text(
          text = if (habit.targetValue > 1f) "${habit.targetValue.toInt()} ${habit.targetUnit} daily" else "${habit.category} • ${habit.frequency.displayName}",
          style = TextStyle(
            fontSize = 13.sp,
            color = InkSecondary
          )
        )
      }

      if (isMultiSelectMode) {
        // Multi-select checkbox
        Box(
          modifier = Modifier
            .size(24.dp)
            .border(
              width = 1.8.dp,
              color = if (isSelected) AccentTerracotta else CardBorder,
              shape = CircleShape
            )
            .background(
              color = if (isSelected) AccentTerracotta else Color.Transparent,
              shape = CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          if (isSelected) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Selected",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
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

// Yellow highlighter text utility for search results (Matching screenshot 3)
@Composable
fun buildHighlightedString(fullText: String, query: String) = buildAnnotatedString {
  if (query.isBlank()) {
    append(fullText)
    return@buildAnnotatedString
  }

  val lowerText = fullText.lowercase()
  var startIndex = 0

  while (startIndex < fullText.length) {
    val foundIndex = lowerText.indexOf(query, startIndex)
    if (foundIndex == -1) {
      append(fullText.substring(startIndex))
      break
    } else {
      if (foundIndex > startIndex) {
        append(fullText.substring(startIndex, foundIndex))
      }
      val endIndex = foundIndex + query.length
      withStyle(
        SpanStyle(
          background = AccentYellowHighlight,
          fontWeight = FontWeight.Bold,
          color = InkPrimary
        )
      ) {
        append(fullText.substring(foundIndex, endIndex))
      }
      startIndex = endIndex
    }
  }
}