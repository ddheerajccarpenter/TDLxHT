package com.example.ui.screens.habits

import com.example.ui.theme.SFProFontFamily
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Unarchive
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Habit
import com.example.ui.components.AppEmptyState
import com.example.ui.components.AppHeader
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.SketchCard
import com.example.ui.components.SketchCheckbox
import com.example.ui.components.SketchFilterChip
import com.example.ui.components.SketchSearchBar
import com.example.ui.components.bounceClick
import com.example.ui.components.heroEntrance
import com.example.ui.components.pulseEntrance
import com.example.ui.components.staggeredEntrance
import com.example.ui.components.shimmerBadge
import com.example.ui.components.paperDotBackground
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.PrimaryAccentLime
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.viewmodel.HabitListFilter
import com.example.ui.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitListScreen(
  viewModel: MainViewModel,
  onNavigateToAddHabit: () -> Unit,
  onHabitClick: (Habit) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val habits by viewModel.habits.collectAsStateWithLifecycle()
  val filteredHabits by viewModel.filteredHabits.collectAsStateWithLifecycle()
  val currentFilter by viewModel.habitListFilter.collectAsStateWithLifecycle()
  val completionRecords by viewModel.completionRecords.collectAsStateWithLifecycle()
  val searchQuery by viewModel.habitSearchQuery.collectAsStateWithLifecycle()

  var habitToDelete by remember { mutableStateOf<Habit?>(null) }

  // Multi-select state
  var isMultiSelectMode by remember { mutableStateOf(false) }
  var selectedHabitIds by remember { mutableStateOf(setOf<String>()) }
  var showBatchDeleteDialog by remember { mutableStateOf(false) }

  val totalSelectedCount = selectedHabitIds.size

  val today = LocalDate.now()
  val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

  val todayScheduledHabits = remember(habits, today) {
    habits.filter { it.isActive && !it.isArchived && !it.isPaused && it.isScheduledOn(today) }
  }

  val todayCompletedIds = remember(completionRecords, todayStr) {
    completionRecords
      .filter { it.date == todayStr && it.isCompleted }
      .map { it.habitId }
      .toSet()
  }

  val todayHabitRecords by viewModel.todayHabitRecords.collectAsStateWithLifecycle()

  val maxStreak = remember(habits, completionRecords) {
    habits.filter { it.isActive && !it.isArchived }
      .maxOfOrNull { habit ->
        viewModel.getHabitStreak(habit).currentStreak
      } ?: 0
  }

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
      // Header: Reusable AppHeader
      AppHeader(
        title = "Habits",
        leftContent = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.bounceClick {
              if (isMultiSelectMode) {
                isMultiSelectMode = false
                selectedHabitIds = emptySet()
              } else {
                onBack()
              }
            }
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = InkPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isMultiSelectMode) "Done" else "Back",
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
        onActionClick = onNavigateToAddHabit
      )

      // Filter Tabs
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        HabitListFilter.entries.forEach { filter ->
          val isSelected = filter == currentFilter
          val count = when (filter) {
            HabitListFilter.ALL -> habits.count { !it.isArchived }
            HabitListFilter.DAILY -> habits.count { !it.isArchived && it.frequency.name == "EVERY_DAY" }
            HabitListFilter.WEEKLY -> habits.count { !it.isArchived && (it.frequency.name == "WEEKLY" || it.frequency.name == "WEEKDAYS") }
            HabitListFilter.PAUSED -> habits.count { !it.isArchived && it.isPaused }
            HabitListFilter.ARCHIVED -> habits.count { it.isArchived }
          }

          val label = if (filter == HabitListFilter.ALL) "All ($count)" else "${filter.displayName} ($count)"

          SketchFilterChip(
            selected = isSelected,
            onClick = { viewModel.setHabitListFilter(filter) },
            label = label
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

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
              val allSelected = filteredHabits.isNotEmpty() && selectedHabitIds.size == filteredHabits.size
              Text(
                text = if (allSelected) "Deselect All" else "Select All (${filteredHabits.size})",
                style = TextStyle(
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = InkSecondary,
                  textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                  .padding(top = 2.dp)
                  .bounceClick {
                    selectedHabitIds = if (allSelected) {
                      emptySet()
                    } else {
                      filteredHabits.map { it.id }.toSet()
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
                    selectedHabitIds = emptySet()
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
        Spacer(modifier = Modifier.height(10.dp))
      } else {
        // Summary Card: Today done, streak
        SketchCard(
          modifier = Modifier.fillMaxWidth().heroEntrance(delayMs = 60)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "${todayCompletedIds.size}/${todayScheduledHabits.size} done",
                style = TextStyle(
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
            }

            if (maxStreak > 0) {
              Box(
                modifier = Modifier
                  .pulseEntrance(delayMs = 150)
                  .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "$maxStreak d streak",
                  style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // Habit List
      if (filteredHabits.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          AppEmptyState(
            icon = Icons.Default.Check,
            title = if (searchQuery.isNotBlank()) "No matching habits" else "No habits",
            actionText = if (searchQuery.isBlank()) "Create Habit" else null,
            onAction = onNavigateToAddHabit
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f),
          contentPadding = PaddingValues(bottom = 155.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredHabits, key = { it.id }) { habit ->
            val isCompleted = todayCompletedIds.contains(habit.id)
            val streakInfo = viewModel.getHabitStreak(habit)
            val isSelected = selectedHabitIds.contains(habit.id)
            val habitIndex = filteredHabits.indexOf(habit)

            Box(modifier = Modifier.animateItem().staggeredEntrance(index = habitIndex)) {
              if (isMultiSelectMode) {
                SketchCard(
                  modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick {
                      selectedHabitIds = if (isSelected) {
                        selectedHabitIds - habit.id
                      } else {
                        selectedHabitIds + habit.id
                      }
                    },
                  backgroundColor = if (isSelected) AccentTerracotta.copy(alpha = 0.12f) else CardBackground,
                  borderColor = if (isSelected) AccentTerracotta else CardBorder,
                  borderWidth = if (isSelected) 2.dp else 1.5.dp,
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
                      Text(
                        text = habit.name,
                        style = TextStyle(
                          fontSize = 16.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = if (isCompleted) InkSecondary else InkPrimary,
                          textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                      )
                      if (habit.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = habit.description,
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
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                      onClick = { onHabitClick(habit) },
                      onLongClick = {
                        isMultiSelectMode = true
                        selectedHabitIds = selectedHabitIds + habit.id
                      }
                    )
                ) {
                  val currentSessions = todayHabitRecords[habit.id]?.currentValue?.toInt() ?: 0
                  HabitCardItemWithActions(
                    habit = habit,
                    isCompleted = isCompleted,
                    currentSessions = currentSessions,
                    streakDays = streakInfo.currentStreak,
                    onToggle = { viewModel.toggleHabitToday(habit.id) },
                    onClick = { onHabitClick(habit) },
                    onPause = { viewModel.togglePauseHabit(habit.id) },
                    onArchive = { viewModel.toggleArchiveHabit(habit.id) },
                    onDelete = { habitToDelete = habit }
                  )
                }
              }
            }
          }
        }
      }
    }

    // Single Delete Confirmation Dialog
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

    // Batch Delete Confirmation Dialog
    if (showBatchDeleteDialog) {
      DeleteConfirmationDialog(
        title = "Delete $totalSelectedCount Habits?",
        message = "Are you sure you want to permanently delete these selected habits? This action cannot be undone.",
        onConfirm = {
          viewModel.deleteHabitsBatch(selectedHabitIds.toList())
          selectedHabitIds = emptySet()
          isMultiSelectMode = false
          showBatchDeleteDialog = false
        },
        onDismiss = { showBatchDeleteDialog = false }
      )
    }
  }
}

@Composable
fun HabitCardItemWithActions(
  habit: Habit,
  isCompleted: Boolean,
  currentSessions: Int = 0,
  streakDays: Int,
  onToggle: () -> Unit,
  onClick: () -> Unit,
  onPause: () -> Unit,
  onArchive: () -> Unit,
  onDelete: () -> Unit
) {
  var menuExpanded by remember { mutableStateOf(false) }

  val streakScale by animateFloatAsState(
    targetValue = if (isCompleted) 1.10f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "streakScalePop"
  )

  val cardAlpha by animateFloatAsState(
    targetValue = if (isCompleted) 0.72f else 1.0f,
    animationSpec = tween(durationMillis = 200),
    label = "habitAlpha"
  )

  SketchCard(
    modifier = Modifier
      .fillMaxWidth()
      .alpha(cardAlpha)
      .animateContentSize(animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f))
      .bounceClick(scaleDown = 0.98f) { onClick() }
      .testTag("habit_item_${habit.id}"),
    cornerRadius = 14.dp,
    borderWidth = 1.5.dp
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
              color = if (isCompleted) InkSecondary else InkPrimary,
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

          if (habit.isPaused) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .background(InkMuted.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(text = "PAUSED", style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = InkSecondary))
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val subtitleText = if (habit.targetValue > 1f) {
          if (isCompleted) {
            "${habit.targetValue.toInt()}/${habit.targetValue.toInt()} sessions completed • ${habit.category}"
          } else if (currentSessions > 0) {
            "$currentSessions/${habit.targetValue.toInt()} sessions • ${(habit.targetValue.toInt() - currentSessions)} remaining"
          } else {
            "${habit.targetValue.toInt()} ${habit.targetUnit} daily • ${habit.category}"
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

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (habit.targetValue > 1f && !isCompleted) {
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

        Box {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .bounceClick(scaleDown = 0.88f) { menuExpanded = true },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Options",
              tint = InkSecondary,
              modifier = Modifier.size(18.dp)
            )
          }

          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
          ) {
            DropdownMenuItem(
              text = { Text(if (habit.isPaused) "Resume Habit" else "Pause Habit") },
              leadingIcon = {
                Icon(
                  imageVector = if (habit.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
              },
              onClick = {
                onPause()
                menuExpanded = false
              }
            )

            DropdownMenuItem(
              text = { Text(if (habit.isArchived) "Unarchive Habit" else "Archive Habit") },
              leadingIcon = {
                Icon(
                  imageVector = if (habit.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
              },
              onClick = {
                onArchive()
                menuExpanded = false
              }
            )

            DropdownMenuItem(
              text = { Text("Delete Habit", color = AccentRed) },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Outlined.DeleteOutline,
                  contentDescription = null,
                  tint = AccentRed,
                  modifier = Modifier.size(18.dp)
                )
              },
              onClick = {
                onDelete()
                menuExpanded = false
              }
            )
          }
        }
      }
    }
  }
}