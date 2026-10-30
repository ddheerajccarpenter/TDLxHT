package com.example.ui.screens.tasks

import android.widget.Toast
import com.example.ui.theme.SFProFontFamily
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReminderOption
import com.example.data.model.RepeatRule
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.calendar.formatTimeDisplay
import com.example.data.model.TaskPriority
import com.example.ui.components.AppHeader
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.SketchCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassSwitch
import com.example.ui.components.SketchCheckbox
import com.example.ui.components.SketchInputField
import com.example.ui.components.bounceClick
import com.example.ui.components.paperDotBackground
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PrimaryAccentLime
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Composable
fun TaskDetailEditScreen(
  viewModel: MainViewModel,
  existingTask: Task? = null,
  initialIsShortTerm: Boolean = false,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isEditing = existingTask != null

  var title by remember { mutableStateOf(existingTask?.title ?: "") }
  var titleError by remember { mutableStateOf<String?>(null) }
  var notes by remember { mutableStateOf(existingTask?.notes ?: "") }
  var isShortTerm by remember { mutableStateOf(existingTask?.isShortTerm ?: initialIsShortTerm) }

  val defaultCategories = listOf("Work", "Personal", "Urgent!", "Study", "Shopping", "College")
  val initialIsCustom = existingTask != null && !defaultCategories.contains(existingTask.category)
  var selectedCategory by remember { mutableStateOf(if (initialIsCustom) "Other" else (existingTask?.category ?: "Work")) }
  var customCategoryText by remember { mutableStateOf(if (initialIsCustom) existingTask?.category ?: "" else "") }

  var priority by remember { mutableStateOf(existingTask?.priority ?: TaskPriority.MEDIUM) }
  var repeatRule by remember { mutableStateOf(existingTask?.repeatRule ?: RepeatRule.NONE) }

  // Initial selected date
  val initialDate = remember(existingTask) {
    if (existingTask != null) {
      try { LocalDate.parse(existingTask.dueDate, DateTimeFormatter.ISO_LOCAL_DATE) } catch (e: Exception) { LocalDate.now() }
    } else {
      LocalDate.now()
    }
  }
  var selectedDate by remember { mutableStateOf(initialDate) }
  var displayMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }

  var dueTime by remember { mutableStateOf(existingTask?.dueTime ?: "10:00") }
  var reminderEnabled by remember {
    mutableStateOf(existingTask?.reminderSetting != null && existingTask.reminderSetting != ReminderOption.NONE)
  }
  var reminderOption by remember {
    mutableStateOf(if (existingTask?.reminderSetting != ReminderOption.NONE) existingTask?.reminderSetting ?: ReminderOption.MIN_15 else ReminderOption.MIN_15)
  }

  // Subtasks list
  val existingSubtasks = remember(existingTask) {
    if (existingTask != null) viewModel.getSubtasksForTask(existingTask.id) else emptyList()
  }
  val subtaskList = remember {
    mutableStateListOf<Subtask>().apply { addAll(existingSubtasks) }
  }
  var newSubtaskText by remember { mutableStateOf("") }
  var showDeleteDialog by remember { mutableStateOf(false) }

  val settings by viewModel.settings.collectAsStateWithLifecycle()
  var isSaving by remember { mutableStateOf(false) }

  val categories = listOf("Work", "Personal", "Urgent!", "Study", "Shopping", "College", "Other")

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperDotBackground()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp)
    ) {
      // Top Navigation Bar: AppHeader
      item {
        AppHeader(
          title = if (isEditing) "Edit Task" else "New Task",
          onBack = onDismiss,
          rightContent = {
            GlassButton(
              onClick = {
                if (isSaving) return@GlassButton
                if (title.isBlank()) {
                  titleError = "Task name is required"
                  Toast.makeText(context, "Please enter a task name", Toast.LENGTH_SHORT).show()
                  return@GlassButton
                }
                titleError = null
                isSaving = true

                val finalCategory = if (selectedCategory == "Other") {
                  if (customCategoryText.isNotBlank()) customCategoryText.trim() else "Other"
                } else {
                  selectedCategory
                }

                val dueDateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val finalReminder = if (reminderEnabled) reminderOption else ReminderOption.NONE

                if (existingTask != null) {
                  val updated = existingTask.copy(
                    title = title.trim(),
                    notes = notes.trim(),
                    dueDate = dueDateStr,
                    dueTime = dueTime,
                    priority = priority,
                    category = finalCategory,
                    tags = listOf(finalCategory),
                    reminderSetting = finalReminder,
                    customReminderMinutes = 15,
                    repeatRule = repeatRule,
                    isShortTerm = isShortTerm
                  )
                  viewModel.updateTask(updated, subtaskList.toList())
                } else {
                  viewModel.createTask(
                    title = title.trim(),
                    notes = notes.trim(),
                    dueDate = dueDateStr,
                    dueTime = dueTime,
                    priority = priority,
                    category = finalCategory,
                    tags = listOf(finalCategory),
                    reminderSetting = finalReminder,
                    customReminderMinutes = 15,
                    repeatRule = repeatRule,
                    repeatDays = emptyList(),
                    subtaskTitles = subtaskList.map { it.title },
                    isShortTerm = isShortTerm
                  )
                }
                onDismiss()
              },
              modifier = Modifier.testTag("task_save_btn")
            ) {
              Text(
                text = "Save",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
            }
          }
      )

      Spacer(modifier = Modifier.height(14.dp))
    }

      // TASK DETAILS (Matching screenshot 2)
      item {
        Text(
          text = "TASK DETAILS",
          style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = InkSecondary
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        SketchInputField(
          value = title,
          onValueChange = {
            title = it
            if (it.isNotBlank()) titleError = null
          },
          placeholder = "Task name",
          textStyle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary),
          modifier = Modifier.fillMaxWidth()
        )

        if (titleError != null) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = titleError!!,
            style = TextStyle(fontSize = 12.sp, color = AccentRed, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        SketchInputField(
          value = notes,
          onValueChange = { notes = it },
          placeholder = "Add notes...",
          singleLine = false,
          minLines = 3,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Short-term Task Toggle
        SketchCard(
          modifier = Modifier
            .fillMaxWidth()
            .bounceClick { isShortTerm = !isShortTerm },
          cornerRadius = 14.dp,
          borderWidth = 1.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
              Text(
                text = "Short-term Task",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isShortTerm) PrimaryAccentLime else InkPrimary
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Auto-removes upon completion • Local record only",
                style = TextStyle(fontSize = 11.5.sp, color = InkSecondary)
              )
            }
            SketchCheckbox(
              checked = isShortTerm,
              onCheckedChange = { isShortTerm = it }
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // CATEGORY
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "CATEGORY",
            style = TextStyle(
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = InkSecondary
            )
          )

          Text(
            text = "Select tag",
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 14.sp,
              color = InkSecondary
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          categories.forEach { cat ->
            val isSelected = cat == selectedCategory
            val bg by androidx.compose.animation.animateColorAsState(
              targetValue = if (isSelected) PillBackgroundDark else Color.Transparent,
              animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
              label = "catBg"
            )
            val textCol by androidx.compose.animation.animateColorAsState(
              targetValue = if (isSelected) PillTextDark else InkSecondary,
              animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
              label = "catText"
            )
            val borderCol by androidx.compose.animation.animateColorAsState(
              targetValue = if (isSelected) PillBackgroundDark else CardBorder,
              animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
              label = "catBorder"
            )

            Box(
              modifier = Modifier
                .border(1.2.dp, borderCol, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(bg)
                .bounceClick { selectedCategory = cat }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("cat_$cat"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = cat,
                style = TextStyle(
                  fontSize = 14.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = textCol
                )
              )
            }
          }
        }

        if (selectedCategory == "Other") {
          Spacer(modifier = Modifier.height(10.dp))
          SketchInputField(
            value = customCategoryText,
            onValueChange = { customCategoryText = it },
            placeholder = "Specify other category name...",
            textStyle = TextStyle(fontSize = 15.sp, color = InkPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("custom_category_input")
          )
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // PRIORITY & REPEAT
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Priority
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "PRIORITY",
              style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = InkSecondary
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              TaskPriority.entries.forEach { p ->
                val isSelected = p == priority
                val pBg by androidx.compose.animation.animateColorAsState(
                  targetValue = if (isSelected) PillBackgroundDark else Color.Transparent,
                  animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                  label = "priorityBg"
                )
                val pText by androidx.compose.animation.animateColorAsState(
                  targetValue = if (isSelected) PillTextDark else InkSecondary,
                  animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                  label = "priorityText"
                )
                val pBorder by androidx.compose.animation.animateColorAsState(
                  targetValue = if (isSelected) PillBackgroundDark else CardBorder,
                  animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                  label = "priorityBorder"
                )
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .border(1.2.dp, pBorder, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(pBg)
                    .bounceClick { priority = p },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = p.displayName,
                    style = TextStyle(
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = pText
                    )
                  )
                }
              }
            }
          }

          // Repeat Rule: None, Daily, Weekly, Monthly
          Column(modifier = Modifier.weight(1.2f)) {
            Text(
              text = "REPEAT",
              style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = InkSecondary
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              listOf(RepeatRule.NONE, RepeatRule.DAILY, RepeatRule.WEEKLY, RepeatRule.MONTHLY).forEach { rule ->
                val isSelected = rule == repeatRule
                val rBg by androidx.compose.animation.animateColorAsState(
                  targetValue = if (isSelected) PillBackgroundDark else Color.Transparent,
                  animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                  label = "repeatBg"
                )
                val rText by androidx.compose.animation.animateColorAsState(
                  targetValue = if (isSelected) PillTextDark else InkSecondary,
                  animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                  label = "repeatText"
                )
                val rBorder by androidx.compose.animation.animateColorAsState(
                  targetValue = if (isSelected) PillBackgroundDark else CardBorder,
                  animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                  label = "repeatBorder"
                )
                Box(
                  modifier = Modifier
                    .height(34.dp)
                    .border(1.2.dp, rBorder, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(rBg)
                    .bounceClick { repeatRule = rule }
                    .padding(horizontal = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = rule.displayName,
                    style = TextStyle(
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = rText
                    )
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // DUE DATE CALENDAR & TIME PICKER (Matching screenshot 2)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "DUE DATE & TIME",
            style = TextStyle(
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = InkSecondary
            )
          )

          Box(
            modifier = Modifier
              .border(1.2.dp, CardBorder, RoundedCornerShape(10.dp))
              .clip(RoundedCornerShape(10.dp))
              .bounceClick {
                val parts = dueTime.split(":")
                val curH = parts.getOrNull(0)?.toIntOrNull() ?: 10
                val curM = parts.getOrNull(1)?.toIntOrNull() ?: 0
                android.app.TimePickerDialog(
                  context,
                  { _, hourOfDay, minute ->
                    dueTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                  },
                  curH,
                  curM,
                  true
                ).show()
              }
              .padding(horizontal = 10.dp, vertical = 4.dp)
              .testTag("time_picker_button"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${formatTimeDisplay(dueTime, settings.timeFormat24Hour)}  Edit",
              style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentTerracotta
              )
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))

        SketchCard(
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            // Month Header: "January 2024", "Today", "<", ">"
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                  )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                  modifier = Modifier
                    .border(1.2.dp, CardBorder, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .bounceClick(scaleDown = 0.92f) {
                      displayMonth = YearMonth.now()
                      selectedDate = LocalDate.now()
                    }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Today",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                  )
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                  modifier = Modifier
                    .size(30.dp)
                    .border(1.2.dp, CardBorder, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .bounceClick(scaleDown = 0.90f) { displayMonth = displayMonth.minusMonths(1) },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month",
                    tint = InkPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                }

                Box(
                  modifier = Modifier
                    .size(30.dp)
                    .border(1.2.dp, CardBorder, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .bounceClick(scaleDown = 0.90f) { displayMonth = displayMonth.plusMonths(1) },
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

            Spacer(modifier = Modifier.height(12.dp))

            // Weekday Headers: SUN, MON, TUE, WED, THU, FRI, SAT
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { dayLabel ->
                Text(
                  text = dayLabel,
                  style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkSecondary,
                    textAlign = TextAlign.Center
                  ),
                  modifier = Modifier.width(36.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid for displayMonth
            val firstDayOfMonth = displayMonth.atDay(1)
            val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
            val daysInMonth = displayMonth.lengthOfMonth()
            val prevMonthDays = displayMonth.minusMonths(1).lengthOfMonth()

            val totalCells = 35
            val rows = totalCells / 7

            for (r in 0 until rows) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                for (c in 0 until 7) {
                  val cellIndex = r * 7 + c
                  val dayNum = cellIndex - dayOfWeekOffset + 1
                  val isCurrentMonth = dayNum in 1..daysInMonth
                  val cellDate = if (isCurrentMonth) {
                    displayMonth.atDay(dayNum)
                  } else if (dayNum <= 0) {
                    displayMonth.minusMonths(1).atDay(prevMonthDays + dayNum)
                  } else {
                    displayMonth.plusMonths(1).atDay(dayNum - daysInMonth)
                  }

                  val isSelected = cellDate.isEqual(selectedDate)

                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .bounceClick(scaleDown = 0.90f) {
                        selectedDate = cellDate
                        if (!isCurrentMonth) {
                          displayMonth = YearMonth.from(cellDate)
                        }
                      }
                      .then(
                        if (isSelected) {
                          Modifier
                            .border(1.8.dp, AccentTerracotta, CircleShape)
                            .background(AccentTerracotta.copy(alpha = 0.15f))
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
                        color = if (!isCurrentMonth) InkMuted else if (isSelected) AccentTerracotta else InkPrimary
                      )
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reminder row with switch
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Remind me at ${formatTimeDisplay(dueTime, settings.timeFormat24Hour)}",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = InkPrimary)
              )

              GlassSwitch(
                checked = reminderEnabled,
                onCheckedChange = { reminderEnabled = it }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // SUBTASKS (Requirement 7)
      item {
        val completedCount = subtaskList.count { it.isCompleted }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "SUBTASKS",
            style = TextStyle(
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = InkSecondary
            )
          )

          if (subtaskList.isNotEmpty()) {
            Text(
              text = "$completedCount / ${subtaskList.size} completed",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkSecondary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Add subtask input field
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          SketchInputField(
            value = newSubtaskText,
            onValueChange = { newSubtaskText = it },
            placeholder = "Add a subtask (e.g. Finish numericals)",
            modifier = Modifier.weight(1f)
          )

          Box(
            modifier = Modifier
              .size(44.dp)
              .border(1.5.dp, CardBorder, RoundedCornerShape(10.dp))
              .clip(RoundedCornerShape(10.dp))
              .background(PillBackgroundDark)
              .bounceClick {
                if (newSubtaskText.isNotBlank()) {
                  subtaskList.add(
                    Subtask(
                      id = UUID.randomUUID().toString(),
                      parentTaskId = existingTask?.id ?: "",
                      title = newSubtaskText.trim(),
                      isCompleted = false
                    )
                  )
                  newSubtaskText = ""
                }
              }
              .testTag("add_subtask_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add Subtask",
              tint = PillTextDark,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of subtasks
        subtaskList.forEachIndexed { index, subtask ->
          val subtaskAlpha by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (subtask.isCompleted) 0.55f else 1.0f,
            animationSpec = androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            label = "subtaskAlpha"
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .weight(1f)
                .alpha(subtaskAlpha)
            ) {
              SketchCheckbox(
                checked = subtask.isCompleted,
                onCheckedChange = { checked ->
                  subtaskList[index] = subtask.copy(isCompleted = checked)
                },
                sizeDp = 22.dp
              )

              Spacer(modifier = Modifier.width(8.dp))

              Text(
                text = subtask.title,
                style = TextStyle(
                  fontSize = 15.sp,
                  color = if (subtask.isCompleted) InkMuted else InkPrimary,
                  textDecoration = if (subtask.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                )
              )
            }

            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .bounceClick(scaleDown = 0.88f) { subtaskList.removeAt(index) },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Remove",
                tint = InkMuted,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // DELETE BUTTON (if editing)
      if (isEditing) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .border(1.5.dp, AccentRed, RoundedCornerShape(12.dp))
              .clip(RoundedCornerShape(12.dp))
              .bounceClick { showDeleteDialog = true }
              .testTag("delete_task_btn"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete",
                tint = AccentRed,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Delete Task",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = AccentRed
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }

    if (showDeleteDialog && existingTask != null) {
      DeleteConfirmationDialog(
        title = "Delete Task?",
        message = "This cannot be undone. Are you sure you want to delete this task?",
        onConfirm = {
          viewModel.deleteTask(existingTask.id)
          showDeleteDialog = false
          onDismiss()
        },
        onDismiss = { showDeleteDialog = false }
      )
    }
  }
}