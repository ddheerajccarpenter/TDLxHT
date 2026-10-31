package com.example.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.example.ui.theme.SFProFontFamily
import com.example.HabitTaskApplication
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.util.AppPreferences
import com.example.util.SafeLog
import com.example.util.VibrationHelper
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppSettings
import com.example.reminder.ReminderScheduler
import com.example.ui.components.AppHeader
import com.example.ui.components.BentoHeroCard
import com.example.ui.components.GroupedDivider
import com.example.ui.components.GroupedInsetCard
import com.example.ui.components.InteractionLevel
import com.example.ui.components.SketchCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassSwitch
import com.example.ui.components.SketchInputField
import com.example.ui.components.bounceClick
import com.example.ui.components.staggeredEntrance
import com.example.ui.components.paperDotBackground
import com.example.ui.screens.calendar.formatTimeDisplay
import com.example.ui.theme.AccentRed
import com.example.ui.components.AnimatedIcon
import com.example.ui.theme.PrimaryAccentLime
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.theme.OklchIndigo
import com.example.ui.theme.OklchEmerald
import com.example.ui.theme.OklchAmber
import com.example.ui.theme.OklchCoral
import com.example.ui.theme.OklchCyan
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SyncStatus

@Composable
fun SettingsScreen(
  viewModel: MainViewModel,
  authViewModel: AuthViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val shakeToOpen by AppPreferences.shakeToOpen.collectAsStateWithLifecycle()
  val vibrations by AppPreferences.vibrations.collectAsStateWithLifecycle()
  val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
  val syncState by authViewModel.syncState.collectAsStateWithLifecycle()

  var showSummaryTimeDialog by remember { mutableStateOf(false) }
  var isDiagnosticsOpen by remember { mutableStateOf(false) }
  var showLogoutDialog by remember { mutableStateOf(false) }
  var showDeleteAccountDialog by remember { mutableStateOf(false) }

  LaunchedEffect(syncState) {
    when (val s = syncState) {
      is SyncStatus.Success -> Toast.makeText(context, s.message, Toast.LENGTH_SHORT).show()
      is SyncStatus.Error -> Toast.makeText(context, s.message, Toast.LENGTH_SHORT).show()
      else -> Unit
    }
  }

  if (isDiagnosticsOpen) {
    BackHandler { isDiagnosticsOpen = false }
    DiagnosticsScreen(
      taskRepository = HabitTaskApplication.instance.taskRepository,
      habitRepository = HabitTaskApplication.instance.habitRepository,
      onNavigateBack = { isDiagnosticsOpen = false }
    )
    return
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Notifications won't appear without permission", Toast.LENGTH_LONG).show()
    }
  }

  val exportService = remember { com.example.data.service.DataExportService(HabitTaskApplication.instance.database) }
  val coroutineScope = rememberCoroutineScope()
  var isExporting by remember { mutableStateOf(false) }
  var isImporting by remember { mutableStateOf(false) }

  val importLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      isImporting = true
      coroutineScope.launch {
        val result = exportService.importFromJson(context, uri)
        isImporting = false
        result.onSuccess { summary ->
          Toast.makeText(
            context,
            "Restored ${summary.tasks} tasks, ${summary.subtasks} subtasks, ${summary.habits} habits",
            Toast.LENGTH_LONG
          ).show()
        }.onFailure { err ->
          SafeLog.w("SettingsScreen", "Import failed", err)
          Toast.makeText(context, "Import failed: ${err.localizedMessage ?: "Invalid file"}", Toast.LENGTH_LONG).show()
        }
      }
    }
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
      contentPadding = PaddingValues(top = 16.dp, bottom = com.example.ui.components.FloatingNavContentBottomPadding)
    ) {
      // Header
      item {
        AppHeader(
          title = "Settings",
          onBack = onBack,
          rightContent = {
            AnimatedIcon(
              resId = R.raw.anim_settings,
              isSelected = true,
              tint = PrimaryAccentLime,
              size = 20.dp,
              contentDescription = "Settings Icon"
            )
          }
        )
        Spacer(modifier = Modifier.height(14.dp))
      }

      // 1. USER PROFILE & CLOUD HERO
      item {
        val email = currentUser?.email ?: "Guest User"
        val isGuest = currentUser == null

        BentoHeroCard(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 0),
          accentColor = OklchIndigo
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(OklchIndigo.copy(alpha = 0.22f))
                .border(1.5.dp, OklchIndigo.copy(alpha = 0.5f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = email.take(1).uppercase(),
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  color = OklchIndigo
                )
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isGuest) "Offline Account" else "Account & Cloud Sync",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = InkSecondary
                )
              )
              Text(
                text = email,
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Sync to Cloud Button
            Box(
              modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(
                  PillBackgroundDark.copy(alpha = if (syncState is SyncStatus.Syncing) 0.5f else 1f),
                  RoundedCornerShape(10.dp)
                )
                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .bounceClick(enabled = syncState !is SyncStatus.Syncing) { authViewModel.syncNow() }
                .semantics { contentDescription = "Sync data to cloud" },
              contentAlignment = Alignment.Center
            ) {
              if (syncState is SyncStatus.Syncing) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = PillTextDark)
                  Text("Syncing…", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 13.sp, color = PillTextDark, fontWeight = FontWeight.SemiBold))
                }
              } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Default.CloudSync, contentDescription = null, tint = PillTextDark, modifier = Modifier.size(16.dp))
                  Text("Sync Now", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 13.sp, color = PillTextDark, fontWeight = FontWeight.SemiBold))
                }
              }
            }

            // Sign Out Button
            Box(
              modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .bounceClick { showLogoutDialog = true }
                .semantics { contentDescription = "Sign out of account" },
              contentAlignment = Alignment.Center
            ) {
              Text("Sign Out", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 13.sp, color = InkPrimary, fontWeight = FontWeight.SemiBold))
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // 2. THEME & DISPLAY (Grouped Inset)
      item {
        Text(
          text = "APPEARANCE & CLOCK",
          style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        GroupedInsetCard(modifier = Modifier.fillMaxWidth().staggeredEntrance(index = 1)) {
          // Theme selector
          Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
              text = "Theme Mode",
              style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val prefTheme by com.example.util.AppPreferences.themeMode.collectAsStateWithLifecycle()
            val effectiveTheme = if (prefTheme.isNotBlank()) prefTheme else settings.themeMode

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (mode, label) ->
                val isSelected = effectiveTheme.equals(mode, ignoreCase = true)
                val bg by animateColorAsState(
                  targetValue = if (isSelected) PillBackgroundDark else Color.Transparent,
                  animationSpec = tween(180, easing = FastOutSlowInEasing),
                  label = "themeBg_$mode"
                )
                val textCol by animateColorAsState(
                  targetValue = if (isSelected) PillTextDark else InkSecondary,
                  animationSpec = tween(180, easing = FastOutSlowInEasing),
                  label = "themeText_$mode"
                )
                val borderCol by animateColorAsState(
                  targetValue = if (isSelected) PillBackgroundDark else CardBorder,
                  animationSpec = tween(180, easing = FastOutSlowInEasing),
                  label = "themeBorder_$mode"
                )

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .border(1.2.dp, borderCol, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .bounceClick(level = InteractionLevel.MICRO) {
                      com.example.util.AppPreferences.setThemeMode(context, mode)
                      viewModel.updateSettings(settings.copy(themeMode = mode))
                      VibrationHelper.tick(context)
                    }
                    .testTag("theme_btn_$mode"),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = label,
                    style = TextStyle(
                      fontFamily = SFProFontFamily,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = textCol
                    )
                  )
                }
              }
            }
          }

          GroupedDivider()

          // Time format
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Time Format",
              style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(false to "12h", true to "24h").forEach { (is24, label) ->
                val isSelected = settings.timeFormat24Hour == is24
                val bg by animateColorAsState(
                  targetValue = if (isSelected) PillBackgroundDark else Color.Transparent,
                  animationSpec = tween(180, easing = FastOutSlowInEasing),
                  label = "timeFormatBg_$is24"
                )
                val textCol by animateColorAsState(
                  targetValue = if (isSelected) PillTextDark else InkSecondary,
                  animationSpec = tween(180, easing = FastOutSlowInEasing),
                  label = "timeFormatText_$is24"
                )
                val borderCol by animateColorAsState(
                  targetValue = if (isSelected) PillBackgroundDark else CardBorder,
                  animationSpec = tween(180, easing = FastOutSlowInEasing),
                  label = "timeFormatBorder_$is24"
                )
                Box(
                  modifier = Modifier
                    .border(1.2.dp, borderCol, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .bounceClick(scaleDown = 0.92f) { viewModel.updateSettings(settings.copy(timeFormat24Hour = is24)) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
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
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // 3. NOTIFICATIONS & REMINDERS (Grouped Inset)
      item {
        Text(
          text = "NOTIFICATIONS & REMINDERS",
          style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        GroupedInsetCard(modifier = Modifier.fillMaxWidth().staggeredEntrance(index = 2)) {
          Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            // Push Notifications master
            SettingsToggleRow(
              title = "Push Notifications",
              checked = settings.pushNotificationsEnabled,
              onCheckedChange = { checked ->
                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                  val hasPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                  ) == PackageManager.PERMISSION_GRANTED
                  if (!hasPerm) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                }
                if (checked) {
                  if (settings.taskRemindersEnabled) ReminderScheduler.rescheduleAllTaskReminders(context)
                  if (settings.habitRemindersEnabled) ReminderScheduler.rescheduleAllHabitReminders(context)
                  if (settings.dailySummaryEnabled) ReminderScheduler.scheduleDailySummary(context, settings.dailySummaryTime)
                } else {
                  ReminderScheduler.cancelAllTaskReminders(context)
                  ReminderScheduler.cancelAllHabitReminders(context)
                  ReminderScheduler.cancelDailySummary(context)
                }
                viewModel.updateSettings(settings.copy(pushNotificationsEnabled = checked))
              }
            )

            GroupedDivider()

            // Task Reminders
            SettingsToggleRow(
              title = "Task Reminders",
              checked = settings.taskRemindersEnabled,
              onCheckedChange = { checked ->
                if (checked) {
                  if (settings.pushNotificationsEnabled) ReminderScheduler.rescheduleAllTaskReminders(context)
                } else {
                  ReminderScheduler.cancelAllTaskReminders(context)
                }
                viewModel.updateSettings(settings.copy(taskRemindersEnabled = checked))
              }
            )

            GroupedDivider()

            // Habit Reminders
            SettingsToggleRow(
              title = "Habit Reminders",
              checked = settings.habitRemindersEnabled,
              onCheckedChange = { checked ->
                if (checked) {
                  if (settings.pushNotificationsEnabled) ReminderScheduler.rescheduleAllHabitReminders(context)
                } else {
                  ReminderScheduler.cancelAllHabitReminders(context)
                }
                viewModel.updateSettings(settings.copy(habitRemindersEnabled = checked))
              }
            )

            GroupedDivider()

            // Sound Alerts
            SettingsToggleRow(
              title = "Sound Alerts",
              checked = settings.soundAlertsEnabled,
              onCheckedChange = { checked ->
                viewModel.updateSettings(settings.copy(soundAlertsEnabled = checked))
              }
            )

            GroupedDivider()

            // Daily Summary
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                  text = "Daily Productivity Summary",
                  style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                )
                Text(
                  text = "Recap of done/remaining at ${formatTimeDisplay(settings.dailySummaryTime, settings.timeFormat24Hour)}",
                  style = TextStyle(fontSize = 12.sp, color = InkSecondary)
                )
                if (settings.dailySummaryEnabled) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Tap to change time",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OklchIndigo),
                    modifier = Modifier.bounceClick { showSummaryTimeDialog = true }
                  )
                }
              }

              GlassSwitch(
                checked = settings.dailySummaryEnabled,
                onCheckedChange = { checked ->
                  val updated = settings.copy(dailySummaryEnabled = checked)
                  if (checked && updated.pushNotificationsEnabled) {
                    ReminderScheduler.scheduleDailySummary(context, updated.dailySummaryTime)
                  } else {
                    ReminderScheduler.cancelDailySummary(context)
                  }
                  viewModel.updateSettings(updated)
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // 4. CALENDAR PREFERENCES (Grouped Inset)
      item {
        Text(
          text = "CALENDAR PREFERENCES",
          style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        GroupedInsetCard(modifier = Modifier.fillMaxWidth().staggeredEntrance(index = 3)) {
          Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            // Default Calendar View
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Default View",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
              )

              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("MONTH" to "Month", "WEEK" to "Week", "DAY" to "Day").forEach { (viewKey, label) ->
                  val isSelected = settings.calendarDefaultView.equals(viewKey, ignoreCase = true)
                  val bg by animateColorAsState(
                    targetValue = if (isSelected) PillBackgroundDark else Color.Transparent,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "calViewBg_$viewKey"
                  )
                  val textCol by animateColorAsState(
                    targetValue = if (isSelected) PillTextDark else InkSecondary,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "calViewText_$viewKey"
                  )
                  val borderCol by animateColorAsState(
                    targetValue = if (isSelected) PillBackgroundDark else CardBorder,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "calViewBorder_$viewKey"
                  )
                  Box(
                    modifier = Modifier
                      .border(1.2.dp, borderCol, RoundedCornerShape(8.dp))
                      .clip(RoundedCornerShape(8.dp))
                      .background(bg)
                      .bounceClick(scaleDown = 0.92f) { viewModel.updateSettings(settings.copy(calendarDefaultView = viewKey)) }
                      .padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = label,
                      style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textCol
                      )
                    )
                  }
                }
              }
            }

            GroupedDivider()

            // Start of Week
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Start of Week",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
              )

              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("MONDAY" to "Monday", "SUNDAY" to "Sunday").forEach { (dow, label) ->
                  val isSelected = settings.startOfWeek.equals(dow, ignoreCase = true)
                  val bg by animateColorAsState(
                    targetValue = if (isSelected) PillBackgroundDark else Color.Transparent,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "sowBg_$dow"
                  )
                  val textCol by animateColorAsState(
                    targetValue = if (isSelected) PillTextDark else InkSecondary,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "sowText_$dow"
                  )
                  val borderCol by animateColorAsState(
                    targetValue = if (isSelected) PillBackgroundDark else CardBorder,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "sowBorder_$dow"
                  )
                  Box(
                    modifier = Modifier
                      .border(1.2.dp, borderCol, RoundedCornerShape(8.dp))
                      .clip(RoundedCornerShape(8.dp))
                      .background(bg)
                      .bounceClick(scaleDown = 0.92f) { viewModel.updateSettings(settings.copy(startOfWeek = dow)) }
                      .padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = label,
                      style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textCol
                      )
                    )
                  }
                }
              }
            }

            GroupedDivider()

            SettingsToggleRow(
              title = "Show Completed Tasks",
              checked = settings.showCompletedTasksInCalendar,
              onCheckedChange = { checked ->
                viewModel.updateSettings(settings.copy(showCompletedTasksInCalendar = checked))
              }
            )

            GroupedDivider()

            SettingsToggleRow(
              title = "Show Completed Habits",
              checked = settings.showCompletedHabitsInCalendar,
              onCheckedChange = { checked ->
                viewModel.updateSettings(settings.copy(showCompletedHabitsInCalendar = checked))
              }
            )

            GroupedDivider()

            SettingsToggleRow(
              title = "Highlight Overdue Items",
              checked = settings.showOverdueInCalendar,
              onCheckedChange = { checked ->
                viewModel.updateSettings(settings.copy(showOverdueInCalendar = checked))
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // 5. AUTOMATION, HAPTICS & GESTURES
      item {
        Text(
          text = "AUTOMATION & GESTURES",
          style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        GroupedInsetCard(modifier = Modifier.fillMaxWidth().staggeredEntrance(index = 4)) {
          val currentShakeAction by AppPreferences.shakeAction.collectAsStateWithLifecycle()
          val currentTimerDuration by AppPreferences.shakeTimerDuration.collectAsStateWithLifecycle()

          Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            SettingsToggleRow(
              title = "Auto-Complete Parent Task",
              subtitle = "When all subtasks finish",
              checked = settings.autoMarkTaskOnSubtasksDone,
              onCheckedChange = { checked ->
                viewModel.updateSettings(settings.copy(autoMarkTaskOnSubtasksDone = checked))
              }
            )

            GroupedDivider()

            SettingsToggleRow(
              title = "Haptic Vibrations",
              subtitle = "Tactile response on click & drag",
              checked = vibrations,
              onCheckedChange = { checked ->
                AppPreferences.setVibrationsEnabled(context, checked)
                if (checked) {
                  VibrationHelper.click(context)
                }
              }
            )

            GroupedDivider()

            SettingsToggleRow(
              title = "Shake Device Gesture",
              subtitle = "Perform quick action on device shake",
              checked = shakeToOpen,
              onCheckedChange = { checked ->
                AppPreferences.setShakeToOpenEnabled(context, checked)
                if (checked) {
                  com.example.service.ShakeDetectionService.start(context)
                  VibrationHelper.shakeDetected(context)
                  Toast.makeText(context, "Shake gesture enabled", Toast.LENGTH_SHORT).show()
                } else {
                  com.example.service.ShakeDetectionService.stop(context)
                }
              }
            )

            if (shakeToOpen) {
              GroupedDivider()

              Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(
                  text = "ACTION ON SHAKE",
                  style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = InkSecondary)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  listOf(
                    "CREATE_TASK" to "Task",
                    "START_TIMER" to "Timer",
                    "QUICK_CHOOSER" to "Actions",
                    "DISABLED" to "Off"
                  ).forEach { (actionKey, label) ->
                    val isSel = currentShakeAction == actionKey
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .background(if (isSel) PillBackgroundDark else PillBackgroundDark.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .bounceClick {
                          AppPreferences.setShakeAction(context, actionKey)
                          VibrationHelper.tick(context)
                        },
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = label,
                        style = TextStyle(
                          fontFamily = SFProFontFamily,
                          fontSize = 12.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = if (isSel) PillTextDark else InkPrimary
                        )
                      )
                    }
                  }
                }

                if (currentShakeAction == "START_TIMER") {
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = "DEFAULT TIMER DURATION",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = InkSecondary)
                  )
                  Spacer(modifier = Modifier.height(8.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    listOf(5, 15, 25, 45, 60).forEach { mins ->
                      val isSel = currentTimerDuration == mins
                      Box(
                        modifier = Modifier
                          .weight(1f)
                          .height(32.dp)
                          .background(if (isSel) PillBackgroundDark else PillBackgroundDark.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .bounceClick {
                          AppPreferences.setShakeTimerDuration(context, mins)
                          VibrationHelper.tick(context)
                        },
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = "${mins}m",
                          style = TextStyle(
                            fontFamily = SFProFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) PillTextDark else InkPrimary
                          )
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // 6. DATA & PRIVACY (Backup & System Diagnostics)
      item {
        Text(
          text = "DATA & PRIVACY",
          style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        GroupedInsetCard(modifier = Modifier.fillMaxWidth().staggeredEntrance(index = 5)) {
          Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
              text = "Backup & Restore",
              style = TextStyle(fontFamily = SFProFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Export clean JSON archives or restore your tasks & habits anytime.",
              style = TextStyle(fontSize = 12.sp, color = InkSecondary)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .background(PillBackgroundDark, RoundedCornerShape(10.dp))
                  .border(1.2.dp, CardBorder, RoundedCornerShape(10.dp))
                  .clip(RoundedCornerShape(10.dp))
                  .bounceClick(enabled = !isExporting && !isImporting) {
                    isExporting = true
                    coroutineScope.launch {
                      val result = exportService.exportToJson(context)
                      isExporting = false
                      result.onSuccess { uri ->
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                          type = "application/json"
                          putExtra(Intent.EXTRA_STREAM, uri)
                          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export TDlxHT Data"))
                      }.onFailure { err ->
                        SafeLog.w("SettingsScreen", "Export failed", err)
                        Toast.makeText(context, "Unable to export data", Toast.LENGTH_SHORT).show()
                      }
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                if (isExporting) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PillTextDark, strokeWidth = 2.dp)
                } else {
                  Text(
                    text = "Export Data",
                    style = TextStyle(fontFamily = SFProFontFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PillTextDark)
                  )
                }
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .background(CardBackground, RoundedCornerShape(10.dp))
                  .border(1.2.dp, CardBorder, RoundedCornerShape(10.dp))
                  .clip(RoundedCornerShape(10.dp))
                  .bounceClick(enabled = !isExporting && !isImporting) {
                    importLauncher.launch("*/*")
                  },
                contentAlignment = Alignment.Center
              ) {
                if (isImporting) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), color = InkPrimary, strokeWidth = 2.dp)
                } else {
                  Text(
                    text = "Import Data",
                    style = TextStyle(fontFamily = SFProFontFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                  )
                }
              }
            }
          }

          GroupedDivider()

          // System Diagnostics Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .bounceClick { isDiagnosticsOpen = true }
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
              Text(
                text = "System Diagnostics",
                style = TextStyle(fontFamily = SFProFontFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
              )
              Text(
                text = "Inspect backend connection, sync health & sensors",
                style = TextStyle(fontSize = 12.sp, color = InkSecondary)
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Open Diagnostics",
              tint = InkSecondary,
              modifier = Modifier.size(18.dp)
            )
          }

          GroupedDivider()

          // Danger Zone: Delete Account
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showDeleteAccountDialog = true }
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Delete Account & Cloud Data",
              style = TextStyle(fontSize = 13.sp, color = AccentRed, fontWeight = FontWeight.SemiBold)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // 7. ABOUT TDLXHT (Single Authoritative Card - ZERO collisions)
      item {
        SketchCard(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 6),
          cornerRadius = 18.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Image(
              painter = painterResource(R.drawable.app_logo),
              contentDescription = "TDlxHT Logo",
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(2.dp, CardBorder, CircleShape)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "TDlxHT • Planner & Habits",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Version ${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE}) • Liquid Glass Edition",
              style = TextStyle(fontSize = 12.sp, color = InkSecondary)
            )

            Spacer(modifier = Modifier.height(14.dp))

            var isCheckingUpdate by remember { mutableStateOf(false) }

            Box(
              modifier = Modifier
                .border(1.2.dp, CardBorder, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .bounceClick(enabled = !isCheckingUpdate) {
                  isCheckingUpdate = true
                  Toast.makeText(context, "Checking for latest release...", Toast.LENGTH_SHORT).show()
                  com.example.util.AppUpdateManager.checkForUpdates(
                    context = context,
                    notifySystem = false,
                    forceShowDismissed = true
                  ) { update, isNew ->
                    isCheckingUpdate = false
                    if (isNew && update != null) {
                      Toast.makeText(context, "New version v${update.version} available! Starting download...", Toast.LENGTH_LONG).show()
                      com.example.util.ApkDownloadManager.startDownload(context, update)
                    } else {
                      Toast.makeText(context, "You are on the latest version (v${com.example.BuildConfig.VERSION_NAME})", Toast.LENGTH_SHORT).show()
                    }
                  }
                }
                .padding(horizontal = 18.dp, vertical = 9.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (isCheckingUpdate) "Checking Updates..." else "Check for Updates",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }

    // Daily Summary Time Dialog
    if (showSummaryTimeDialog) {
      Dialog(onDismissRequest = { showSummaryTimeDialog = false }) {
        SketchCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
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
                text = "Daily Summary Time",
                style = TextStyle(fontFamily = SFProFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
              )
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .border(1.dp, CardBorder, CircleShape)
                  .clip(CircleShape)
                  .background(CardBackground)
                  .bounceClick(scaleDown = 0.88f) { showSummaryTimeDialog = false },
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = InkPrimary, modifier = Modifier.size(16.dp))
              }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "Select when you'd like your evening productivity recap:",
              style = TextStyle(fontSize = 13.sp, color = InkSecondary)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val timePresets = listOf("18:00", "19:00", "20:00", "21:00", "21:30", "22:00")
            timePresets.chunked(3).forEach { rowPresets ->
              Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                rowPresets.forEach { preset ->
                  val isSelected = settings.dailySummaryTime == preset
                  val bg by animateColorAsState(
                    targetValue = if (isSelected) PillBackgroundDark else Color.Transparent,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "summaryPresetBg_$preset"
                  )
                  val textCol by animateColorAsState(
                    targetValue = if (isSelected) PillTextDark else InkSecondary,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "summaryPresetText_$preset"
                  )
                  val borderCol by animateColorAsState(
                    targetValue = if (isSelected) PillBackgroundDark else CardBorder,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "summaryPresetBorder_$preset"
                  )
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .height(40.dp)
                      .border(1.2.dp, borderCol, RoundedCornerShape(10.dp))
                      .clip(RoundedCornerShape(10.dp))
                      .background(bg)
                      .bounceClick(scaleDown = 0.94f) {
                        val updated = settings.copy(dailySummaryTime = preset)
                        if (updated.dailySummaryEnabled && updated.pushNotificationsEnabled) {
                          ReminderScheduler.scheduleDailySummary(context, preset)
                        }
                        viewModel.updateSettings(updated)
                        showSummaryTimeDialog = false
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = formatTimeDisplay(preset, settings.timeFormat24Hour),
                      style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textCol
                      )
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Sign Out Dialog
    if (showLogoutDialog) {
      Dialog(onDismissRequest = { showLogoutDialog = false }) {
        SketchCard(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
          Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sign Out?", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
            Spacer(Modifier.height(10.dp))
            Text(
              text = "Your account will be safely signed out, and local data cleared to isolate accounts. Sign back in at any time to sync with the cloud.",
              style = TextStyle(fontSize = 13.sp, color = InkSecondary, textAlign = TextAlign.Center, lineHeight = 18.sp)
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(
                modifier = Modifier.weight(1f).height(42.dp)
                  .border(1.2.dp, CardBorder, RoundedCornerShape(10.dp))
                  .clip(RoundedCornerShape(10.dp))
                  .bounceClick { showLogoutDialog = false },
                contentAlignment = Alignment.Center
              ) {
                Text("Cancel", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 14.sp, color = InkPrimary, fontWeight = FontWeight.SemiBold))
              }
              Box(
                modifier = Modifier.weight(1f).height(42.dp)
                  .background(AccentRed.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                  .border(1.2.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                  .clip(RoundedCornerShape(10.dp))
                  .bounceClick { showLogoutDialog = false; authViewModel.signOut(context) },
                contentAlignment = Alignment.Center
              ) {
                Text("Sign Out", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 14.sp, color = AccentRed, fontWeight = FontWeight.SemiBold))
              }
            }
          }
        }
      }
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
      Dialog(onDismissRequest = { showDeleteAccountDialog = false }) {
        SketchCard(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
          Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Delete Account?", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentRed))
            Spacer(Modifier.height(10.dp))
            Text(
              text = "Are you sure you want to delete your account? All your cloud data, tasks, habits, and history will be permanently deleted. This action cannot be undone.",
              style = TextStyle(fontSize = 13.sp, color = InkSecondary, textAlign = TextAlign.Center, lineHeight = 18.sp)
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(
                modifier = Modifier.weight(1f).height(42.dp)
                  .border(1.2.dp, CardBorder, RoundedCornerShape(10.dp))
                  .clip(RoundedCornerShape(10.dp))
                  .bounceClick { showDeleteAccountDialog = false },
                contentAlignment = Alignment.Center
              ) {
                Text("Cancel", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 14.sp, color = InkPrimary, fontWeight = FontWeight.SemiBold))
              }
              Box(
                modifier = Modifier.weight(1f).height(42.dp)
                  .background(AccentRed, RoundedCornerShape(10.dp))
                  .clip(RoundedCornerShape(10.dp))
                  .bounceClick {
                    showDeleteAccountDialog = false
                    authViewModel.deleteAccount(context)
                  },
                contentAlignment = Alignment.Center
              ) {
                Text("Delete", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SettingsToggleRow(
  title: String,
  subtitle: String? = null,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
      Text(
        text = title,
        style = TextStyle(
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          color = InkPrimary
        )
      )
      if (!subtitle.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = TextStyle(
            fontSize = 12.sp,
            color = InkSecondary
          )
        )
      }
    }

    GlassSwitch(
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}
