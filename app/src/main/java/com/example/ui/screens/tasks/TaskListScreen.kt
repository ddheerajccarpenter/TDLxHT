package com.example.ui.screens.tasks

import com.example.ui.theme.SFProFontFamily
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.ui.text.style.TextDecoration
import com.example.data.model.Task
import com.example.data.model.TaskFilter
import com.example.data.model.TaskSort
import com.example.ui.components.AppEmptyState
import com.example.ui.components.AppHeader
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.SketchCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassMenu
import com.example.ui.components.GlassMenuItem
import com.example.ui.components.SketchCheckbox
import com.example.ui.components.SketchFilterChip
import com.example.ui.components.SketchSearchBar
import com.example.ui.components.bounceClick
import com.example.ui.components.staggeredEntrance
import com.example.ui.components.paperDotBackground
import com.example.ui.screens.home.TaskItemCard
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskListScreen(
  viewModel: MainViewModel,
  onNavigateToAddTask: () -> Unit,
  onTaskClick: (Task) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
  val currentFilter by viewModel.taskFilter.collectAsStateWithLifecycle()
  val currentSort by viewModel.taskSort.collectAsStateWithLifecycle()
  val searchQuery by viewModel.taskSearchQuery.collectAsStateWithLifecycle()
  val allTasks by viewModel.tasks.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()

  var taskToDelete by remember { mutableStateOf<Task?>(null) }
  var sortMenuExpanded by remember { mutableStateOf(false) }

  // Multi-select state
  var isMultiSelectMode by remember { mutableStateOf(false) }
  var selectedTaskIds by remember { mutableStateOf(setOf<String>()) }
  var showBatchDeleteDialog by remember { mutableStateOf(false) }

  val totalSelectedCount = selectedTaskIds.size

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperDotBackground()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      // Standardized Top Navigation Header
      AppHeader(
        title = "Tasks",
        onBack = {
          if (isMultiSelectMode) {
            isMultiSelectMode = false
            selectedTaskIds = emptySet()
          } else {
            onBack()
          }
        },
        backLabel = if (isMultiSelectMode) "Done" else "Back",
        actionIcon = Icons.Default.Add,
        actionContentDescription = "Add Task",
        onActionClick = onNavigateToAddTask,
        actionTestTag = "tasks_add_button"
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Search Bar
      SketchSearchBar(
        query = searchQuery,
        onQueryChange = { viewModel.setTaskSearchQuery(it) },
        placeholder = "Search tasks, tags...",
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Filter Chips + Sort Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier
            .weight(1f)
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          TaskFilter.entries.forEach { filter ->
            SketchFilterChip(
              label = filter.title,
              isSelected = filter == currentFilter,
              onClick = { viewModel.setTaskFilter(filter) },
              testTag = "filter_${filter.name}"
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Sort Menu Button (Apple Liquid Glass)
        Box {
          GlassButton(
            onClick = { sortMenuExpanded = true },
            contentDescription = "Sort by",
            size = 36.dp
          ) {
            Icon(
              imageVector = Icons.Default.FilterList,
              contentDescription = null,
              tint = InkPrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          GlassMenu(
            expanded = sortMenuExpanded,
            onDismissRequest = { sortMenuExpanded = false }
          ) {
            TaskSort.entries.forEach { sortOption ->
              GlassMenuItem(
                label = sortOption.displayName,
                checked = sortOption == currentSort,
                onSelect = {
                  viewModel.setTaskSort(sortOption)
                  sortMenuExpanded = false
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // MULTI-SELECT ACTIVE BANNER & ACTIONS
      if (isMultiSelectMode) {
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
              val allSelected = filteredTasks.isNotEmpty() && selectedTaskIds.size == filteredTasks.size
              Text(
                text = if (allSelected) "Deselect All" else "Select All (${filteredTasks.size})",
                style = TextStyle(
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = InkSecondary,
                  textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                  .padding(top = 2.dp)
                  .bounceClick {
                    selectedTaskIds = if (allSelected) {
                      emptySet()
                    } else {
                      filteredTasks.map { it.id }.toSet()
                    }
                  }
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
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
                  }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              )

              Spacer(modifier = Modifier.width(6.dp))

              Box(
                modifier = Modifier
                  .background(
                    if (totalSelectedCount > 0) AccentTerracotta else AccentTerracotta.copy(alpha = 0.35f),
                    RoundedCornerShape(10.dp)
                  )
                  .clip(RoundedCornerShape(10.dp))
                  .bounceClick {
                    if (totalSelectedCount > 0) {
                      showBatchDeleteDialog = true
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
        Spacer(modifier = Modifier.height(6.dp))
      }

      // Task List
      if (filteredTasks.isEmpty()) {
        val totalTasksInAccount = allTasks.size
        val hasActiveFilter = currentFilter != TaskFilter.ALL || searchQuery.isNotBlank()
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          if (totalTasksInAccount == 0) {
            AppEmptyState(
              icon = Icons.Default.Check,
              title = "No tasks yet",
              subtitle = "Add your first task to start organizing your day.",
              actionText = "Create task",
              onAction = onNavigateToAddTask
            )
          } else if (hasActiveFilter) {
            AppEmptyState(
              icon = Icons.Default.Check,
              title = if (searchQuery.isNotBlank()) "No matching tasks" else "No ${currentFilter.title.lowercase()} tasks",
              subtitle = if (searchQuery.isNotBlank()) "No tasks match \"$searchQuery\"." else "No tasks found with the ${currentFilter.title} filter.",
              actionText = "Clear filters",
              onAction = {
                viewModel.setTaskFilter(TaskFilter.ALL)
                viewModel.setTaskSearchQuery("")
              }
            )
          } else {
            AppEmptyState(
              icon = Icons.Default.Check,
              title = "No tasks",
              subtitle = "All tasks completed.",
              actionText = "Create task",
              onAction = onNavigateToAddTask
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f),
          contentPadding = PaddingValues(bottom = com.example.ui.components.FloatingNavContentBottomPadding + 48.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredTasks, key = { it.id }) { task ->
            val subtasks = viewModel.getSubtasksForTask(task.id)
            val subtasksDone = subtasks.count { it.isCompleted }
            val isSelected = selectedTaskIds.contains(task.id)
            val taskIndex = filteredTasks.indexOf(task)

            Box(modifier = Modifier.animateItem().staggeredEntrance(index = taskIndex)) {
              if (isMultiSelectMode) {
                SketchCard(
                  modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick {
                      selectedTaskIds = if (isSelected) {
                        selectedTaskIds - task.id
                      } else {
                        selectedTaskIds + task.id
                      }
                    },
                  backgroundColor = if (isSelected) AccentTerracotta.copy(alpha = 0.12f) else CardBackground,
                  borderColor = if (isSelected) AccentTerracotta else CardBorder,
                  borderWidth = if (isSelected) 2.dp else 1.5.dp,
                  cornerRadius = 12.dp
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
                          color = if (task.isCompleted) InkMuted else InkPrimary,
                          textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                      )
                      if (task.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = task.notes,
                          style = TextStyle(fontSize = 13.sp, color = InkSecondary),
                          maxLines = 1
                        )
                      }
                    }

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
                  }
                }
              } else {
                TaskItemCard(
                  task = task,
                  subtasksCount = subtasks.size,
                  subtasksDone = subtasksDone,
                  is24Hour = settings.timeFormat24Hour,
                  onToggle = { viewModel.toggleTaskCompletion(task.id) },
                  onClick = { onTaskClick(task) },
                  onLongClick = {
                    isMultiSelectMode = true
                    selectedTaskIds = setOf(task.id)
                  },
                  onDelete = { taskToDelete = task }
                )
              }
            }
          }
        }
      }
    }

    // Single Delete Confirmation Dialog
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

    // Batch Delete Confirmation Dialog
    if (showBatchDeleteDialog) {
      DeleteConfirmationDialog(
        title = "Delete $totalSelectedCount Tasks?",
        message = "Are you sure you want to permanently delete these selected tasks? This action cannot be undone.",
        onConfirm = {
          viewModel.deleteTasksBatch(selectedTaskIds.toList())
          selectedTaskIds = emptySet()
          isMultiSelectMode = false
          showBatchDeleteDialog = false
        },
        onDismiss = { showBatchDeleteDialog = false }
      )
    }
  }
}
