package com.example.ui.screens.habits

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Habit
import com.example.data.model.HabitCategory
import com.example.data.model.HabitFrequency
import com.example.ui.components.AppHeader
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.SketchCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassSwitch
import com.example.ui.components.SketchInputField
import com.example.ui.components.bounceClick
import com.example.ui.components.paperDotBackground
import com.example.ui.screens.calendar.HabitHistoryGrid
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.viewmodel.MainViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HabitDetailEditScreen(
  viewModel: MainViewModel,
  existingHabit: Habit? = null,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isEditing = existingHabit != null
  val completionRecords by viewModel.completionRecords.collectAsStateWithLifecycle()

  var name by remember { mutableStateOf(existingHabit?.name ?: "") }
  var nameError by remember { mutableStateOf<String?>(null) }
  var description by remember { mutableStateOf(existingHabit?.description ?: "") }

  val defaultCategoryTags = listOf("Study", "Health", "Fitness", "Mindfulness", "Personal", "Productivity")
  val initialIsCustomHabit = existingHabit != null && !defaultCategoryTags.contains(existingHabit.category)
  var selectedCategory by remember { mutableStateOf(if (initialIsCustomHabit) "Other" else (existingHabit?.category ?: "Study")) }
  var customCategoryText by remember { mutableStateOf(if (initialIsCustomHabit) existingHabit?.category ?: "" else "") }

  var frequency by remember { mutableStateOf(existingHabit?.frequency ?: HabitFrequency.EVERY_DAY) }
  var targetValue by remember { mutableFloatStateOf(existingHabit?.targetValue ?: 1f) }
  var targetUnit by remember { mutableStateOf(existingHabit?.targetUnit ?: "session") }

  fun getDefaultTimeForSession(index: Int, total: Int): String {
    return when {
      total <= 1 -> "08:00"
      total == 2 -> if (index == 0) "09:00" else "20:00"
      total == 3 -> when (index) {
        0 -> "08:00"
        1 -> "14:00"
        else -> "20:00"
      }
      total == 4 -> when (index) {
        0 -> "08:00"
        1 -> "12:00"
        2 -> "16:00"
        else -> "20:00"
      }
      else -> {
        val hour = 8 + (index * 12 / maxOf(1, total - 1))
        String.format(java.util.Locale.getDefault(), "%02d:00", hour.coerceIn(6, 23))
      }
    }
  }

  val initialParsedTimes = remember(existingHabit) {
    val raw = existingHabit?.reminderTime?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    if (raw.isNotEmpty()) raw else listOf("08:00")
  }

  var reminderTimes by remember {
    mutableStateOf(
      List(maxOf(1, targetValue.toInt())) { idx ->
        initialParsedTimes.getOrNull(idx) ?: getDefaultTimeForSession(idx, maxOf(1, targetValue.toInt()))
      }
    )
  }

  var reminderEnabled by remember { mutableStateOf(existingHabit?.reminderEnabled ?: false) }

  var showDeleteDialog by remember { mutableStateOf(false) }
  var isSaving by remember { mutableStateOf(false) }

  val frequencies = listOf(
    HabitFrequency.EVERY_DAY to "Every Day",
    HabitFrequency.WEEKDAYS to "Weekdays",
    HabitFrequency.WEEKLY to "Weekly",
    HabitFrequency.CUSTOM to "Custom"
  )

  val categoryTags = listOf("Study", "Health", "Fitness", "Mindfulness", "Personal", "Productivity", "Other")

  val streakInfo = remember(existingHabit) {
    if (existingHabit != null) viewModel.getHabitStreak(existingHabit) else null
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
      contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
    ) {
      // Top Navigation Bar: AppHeader
      item {
        AppHeader(
          title = if (isEditing) "Edit Habit" else "New Habit",
          onBack = onDismiss,
          rightContent = {
            GlassButton(
              onClick = {
                if (isSaving) return@GlassButton
                if (name.isBlank()) {
                  nameError = "Habit name is required"
                  Toast.makeText(context, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                  return@GlassButton
                }
                nameError = null
                isSaving = true

                val finalCategory = if (selectedCategory == "Other") {
                  if (customCategoryText.isNotBlank()) customCategoryText.trim() else "Other"
                } else {
                  selectedCategory
                }

                val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val finalReminderTime = reminderTimes.take(maxOf(1, targetValue.toInt())).joinToString(",")

                if (existingHabit != null) {
                  val updated = existingHabit.copy(
                    name = name.trim(),
                    description = description.trim(),
                    category = finalCategory,
                    frequency = frequency,
                    targetValue = targetValue,
                    targetUnit = targetUnit,
                    reminderTime = finalReminderTime,
                    reminderEnabled = reminderEnabled
                  )
                  viewModel.updateHabit(updated)
                } else {
                  viewModel.createHabit(
                    name = name.trim(),
                    description = description.trim(),
                    category = finalCategory,
                    frequency = frequency,
                    customDays = emptyList(),
                    targetValue = targetValue,
                    targetUnit = targetUnit,
                    startDate = todayStr,
                    reminderTime = finalReminderTime,
                    reminderEnabled = reminderEnabled
                  )
                }
                onDismiss()
              },
              modifier = Modifier.testTag("save_habit_header_btn")
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

      // HABIT DETAILS
      item {
        Text(
          text = "HABIT DETAILS",
          style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = InkSecondary
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        SketchInputField(
          value = name,
          onValueChange = {
            name = it
            if (it.isNotBlank()) nameError = null
          },
          placeholder = "Habit name (e.g. Daily Reading)",
          textStyle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary),
          modifier = Modifier.fillMaxWidth()
        )

        if (nameError != null) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = nameError!!,
            style = TextStyle(fontSize = 12.sp, color = AccentRed, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        SketchInputField(
          value = description,
          onValueChange = { description = it },
          placeholder = "Notes or motivation (e.g. At least 20 pages before bed)",
          singleLine = false,
          minLines = 3,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))
      }

      // FREQUENCY & GOALS (Matching screenshot 7)
      item {
        Text(
          text = "FREQUENCY & GOALS",
          style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = InkSecondary
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          frequencies.forEach { (freq, label) ->
            val isSelected = freq == frequency
            val bg by androidx.compose.animation.animateColorAsState(
              targetValue = if (isSelected) PillBackgroundDark else Color.Transparent,
              animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
              label = "habitFreqBg"
            )
            val textCol by androidx.compose.animation.animateColorAsState(
              targetValue = if (isSelected) PillTextDark else InkSecondary,
              animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
              label = "habitFreqText"
            )
            val borderCol by androidx.compose.animation.animateColorAsState(
              targetValue = if (isSelected) PillBackgroundDark else CardBorder,
              animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
              label = "habitFreqBorder"
            )

            Box(
              modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .border(1.2.dp, borderCol, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(bg)
                .bounceClick(scaleDown = 0.94f) { frequency = freq }
                .testTag("freq_$label"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                style = TextStyle(
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = textCol
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // DAILY TARGET (Matching screenshot 7: Stepper [-] [1] [+])
      item {
        SketchCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 14.dp,
          borderWidth = 1.5.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "DAILY TARGET",
                style = TextStyle(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = InkSecondary
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${targetValue.toInt()} $targetUnit",
                style = TextStyle(
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Decrement button
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .border(1.2.dp, CardBorder, CircleShape)
                  .clip(CircleShape)
                  .bounceClick(scaleDown = 0.88f) {
                    if (targetValue > 1f) {
                      targetValue -= 1f
                      val count = targetValue.toInt()
                      if (reminderTimes.size > count) {
                        reminderTimes = reminderTimes.take(count)
                      }
                    }
                  }
                  .testTag("target_minus_btn"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Remove,
                  contentDescription = "Decrease",
                  tint = InkPrimary,
                  modifier = Modifier.size(18.dp)
                )
              }

              Text(
                text = "${targetValue.toInt()}",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
              )

              // Increment button
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .border(1.2.dp, CardBorder, CircleShape)
                  .clip(CircleShape)
                  .bounceClick(scaleDown = 0.88f) {
                    targetValue += 1f
                    val count = targetValue.toInt()
                    val updatedList = reminderTimes.toMutableList()
                    while (updatedList.size < count) {
                      updatedList.add(getDefaultTimeForSession(updatedList.size, count))
                    }
                    reminderTimes = updatedList
                  }
                  .testTag("target_plus_btn"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Increase",
                  tint = InkPrimary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // REMINDERS (Matching screenshot 7 with multi-session support)
      item {
        Text(
          text = "REMINDERS",
          style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = InkSecondary
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        val sessionCount = maxOf(1, targetValue.toInt())
        SketchCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 14.dp,
          borderWidth = 1.5.dp
        ) {
          if (sessionCount <= 1) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              val singleTime = reminderTimes.firstOrNull() ?: "08:00"
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Notifications,
                  contentDescription = null,
                  tint = InkPrimary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                  modifier = Modifier.bounceClick {
                    val parts = singleTime.split(":")
                    val curH = parts.getOrNull(0)?.toIntOrNull() ?: 8
                    val curM = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    android.app.TimePickerDialog(
                      context,
                      { _, h, m ->
                        val formatted = String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m)
                        reminderTimes = listOf(formatted)
                      },
                      curH,
                      curM,
                      true
                    ).show()
                  }
                ) {
                  Text(
                    text = "Daily Reminder",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                  )
                  Text(
                    text = "Every day at $singleTime  Edit",
                    style = TextStyle(fontSize = 12.sp, color = AccentTerracotta, fontWeight = FontWeight.Medium)
                  )
                }
              }

              GlassSwitch(
                checked = reminderEnabled,
                onCheckedChange = { reminderEnabled = it },
                modifier = Modifier.testTag("habit_reminder_switch")
              )
            }
          } else {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = InkPrimary,
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = "Daily Reminders ($sessionCount sessions)",
                      style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                    )
                    Text(
                      text = if (reminderEnabled) "Different reminder set for each session" else "Reminders disabled",
                      style = TextStyle(
                        fontSize = 12.sp,
                        color = if (reminderEnabled) AccentTerracotta else InkSecondary,
                        fontWeight = FontWeight.Medium
                      )
                    )
                  }
                }

                GlassSwitch(
                  checked = reminderEnabled,
                  onCheckedChange = { reminderEnabled = it },
                  modifier = Modifier.testTag("habit_reminder_switch")
                )
              }

              if (reminderEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                for (sessionIdx in 0 until sessionCount) {
                  val sessionTime = reminderTimes.getOrNull(sessionIdx) ?: getDefaultTimeForSession(sessionIdx, sessionCount)
                  if (sessionIdx > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                  }
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(CardBorder.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                      .border(1.dp, CardBorder.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                      .clip(RoundedCornerShape(10.dp))
                      .bounceClick {
                        val parts = sessionTime.split(":")
                        val curH = parts.getOrNull(0)?.toIntOrNull() ?: 8
                        val curM = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        android.app.TimePickerDialog(
                          context,
                          { _, h, m ->
                            val formatted = String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m)
                            val updatedList = reminderTimes.toMutableList()
                            while (updatedList.size <= sessionIdx) {
                              updatedList.add(getDefaultTimeForSession(updatedList.size, sessionCount))
                            }
                            updatedList[sessionIdx] = formatted
                            reminderTimes = updatedList
                          },
                          curH,
                          curM,
                          true
                        ).show()
                      }
                      .padding(horizontal = 12.dp, vertical = 10.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Text(
                          text = "Session ${sessionIdx + 1} Reminder",
                          style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                        )
                        Text(
                          text = "Every day at $sessionTime  Edit",
                          style = TextStyle(fontSize = 12.sp, color = AccentTerracotta, fontWeight = FontWeight.Medium)
                        )
                      }

                      Box(
                        modifier = Modifier
                          .background(PillBackgroundDark, RoundedCornerShape(8.dp))
                          .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = sessionTime,
                          style = TextStyle(fontSize = 13.sp, color = PillTextDark, fontWeight = FontWeight.Bold)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // CATEGORY TAGS (Matching screenshot 7)
      item {
        Text(
          text = "CATEGORY TAGS",
          style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = InkSecondary
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          categoryTags.forEach { cat ->
            val isSelected = cat == selectedCategory
            val bg by androidx.compose.animation.animateColorAsState(
              targetValue = if (isSelected) PillBackgroundDark else Color.Transparent,
              animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
              label = "habitTagBg"
            )
            val textCol by androidx.compose.animation.animateColorAsState(
              targetValue = if (isSelected) PillTextDark else InkSecondary,
              animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
              label = "habitTagText"
            )
            val borderCol by androidx.compose.animation.animateColorAsState(
              targetValue = if (isSelected) PillBackgroundDark else CardBorder,
              animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing),
              label = "habitTagBorder"
            )

            Box(
              modifier = Modifier
                .border(1.2.dp, borderCol, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(bg)
                .bounceClick { selectedCategory = cat }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("habit_tag_$cat"),
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
            placeholder = "Specify other habit category...",
            textStyle = TextStyle(fontSize = 15.sp, color = InkPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("custom_habit_category_input")
          )
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Historical Stats Card (if editing)
      if (streakInfo != null) {
        item {
          SketchCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            borderWidth = 1.5.dp
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
            ) {
              Text(
                text = "HABIT STATS",
                style = TextStyle(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = InkSecondary
                )
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "${streakInfo.currentStreak}d", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
                  Text(text = "Current Streak", style = TextStyle(fontSize = 11.sp, color = InkSecondary))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "${streakInfo.longestStreak}d", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
                  Text(text = "Best Streak", style = TextStyle(fontSize = 11.sp, color = InkSecondary))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "${streakInfo.totalCompletions}", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
                  Text(text = "Total Done", style = TextStyle(fontSize = 11.sp, color = InkSecondary))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  val ratePct = (streakInfo.completionRate * 100).toInt()
                  Text(text = "$ratePct%", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
                  Text(text = "Success Rate", style = TextStyle(fontSize = 11.sp, color = InkSecondary))
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }

        if (existingHabit != null) {
          item {
            HabitHistoryGrid(
              habit = existingHabit,
              records = completionRecords,
              modifier = Modifier.padding(bottom = 20.dp)
            )
          }
        }
      }

      // SAVE HABIT BUTTON — Apple Liquid Glass Button
      item {
        GlassButton(
          onClick = {
            if (isSaving) return@GlassButton
            if (name.isBlank()) {
              nameError = "Habit name is required"
              Toast.makeText(context, "Please enter a habit name", Toast.LENGTH_SHORT).show()
              return@GlassButton
            }
            nameError = null
            isSaving = true

            val finalCategory = if (selectedCategory == "Other") {
              if (customCategoryText.isNotBlank()) customCategoryText.trim() else "Other"
            } else {
              selectedCategory
            }

            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val finalReminderTime = reminderTimes.take(maxOf(1, targetValue.toInt())).joinToString(",")

            if (existingHabit != null) {
              val updated = existingHabit.copy(
                name = name.trim(),
                description = description.trim(),
                category = finalCategory,
                frequency = frequency,
                targetValue = targetValue,
                targetUnit = targetUnit,
                reminderTime = finalReminderTime,
                reminderEnabled = reminderEnabled
              )
              viewModel.updateHabit(updated)
            } else {
              viewModel.createHabit(
                name = name.trim(),
                description = description.trim(),
                category = finalCategory,
                frequency = frequency,
                customDays = emptyList(),
                targetValue = targetValue,
                targetUnit = targetUnit,
                startDate = todayStr,
                reminderTime = finalReminderTime,
                reminderEnabled = reminderEnabled
              )
            }
            onDismiss()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("save_habit_btn")
        ) {
          Text(
            text = "Save Habit",
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = InkPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      // Delete Habit Button (if editing)
      if (isEditing) {
        item {
          GlassButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("delete_habit_btn")
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
                text = "Delete Habit",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 15.sp,
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

    if (showDeleteDialog && existingHabit != null) {
      DeleteConfirmationDialog(
        title = "Delete Habit?",
        message = "This cannot be undone. Are you sure you want to delete this habit?",
        onConfirm = {
          viewModel.deleteHabit(existingHabit.id)
          showDeleteDialog = false
          onDismiss()
        },
        onDismiss = { showDeleteDialog = false }
      )
    }
  }
}