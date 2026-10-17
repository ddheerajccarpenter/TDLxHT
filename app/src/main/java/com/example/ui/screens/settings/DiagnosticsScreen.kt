package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.HabitTaskApplication
import com.example.data.repository.HabitRepository
import com.example.data.repository.TaskRepository
import com.example.data.service.SupabaseClient
import com.example.ui.components.SketchCard
import com.example.ui.components.bounceClick
import com.example.ui.components.paperDotBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.theme.SFProFontFamily
import com.example.util.AppPreferences
import com.example.util.AppUpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

enum class DiagnosticStatus(val label: String) {
  HEALTHY("HEALTHY"),
  DEGRADED("DEGRADED"),
  DISCONNECTED("DISCONNECTED"),
  PERMISSION_REQUIRED("PERMISSION REQUIRED"),
  AUTH_REQUIRED("AUTH REQUIRED"),
  UP_TO_DATE("UP TO DATE"),
  UPDATE_AVAILABLE("UPDATE AVAILABLE"),
  CHECKING("CHECKING...")
}

data class DiagnosticItem(
  val title: String,
  val status: DiagnosticStatus,
  val details: String,
  val latencyMs: Long? = null
)

data class DiagnosticCategory(
  val categoryName: String,
  val items: List<DiagnosticItem>
)

@Composable
fun DiagnosticsScreen(
  taskRepository: TaskRepository,
  habitRepository: HabitRepository,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var isRunningChecks by remember { mutableStateOf(false) }
  var categories by remember { mutableStateOf<List<DiagnosticCategory>>(emptyList()) }
  var lastCheckedTimestamp by remember { mutableStateOf<Long?>(null) }

  fun runAllDiagnostics() {
    isRunningChecks = true
    coroutineScope.launch {
      val results = mutableListOf<DiagnosticCategory>()

      // 1. Backend Connectivity
      val backendItems = withContext(Dispatchers.IO) {
        val list = mutableListOf<DiagnosticItem>()
        // Supabase reachability
        val startPing = System.currentTimeMillis()
        var supabaseHealthy = false
        var pingLatency = 0L
        try {
          val client = OkHttpClient.Builder().connectTimeout(4, TimeUnit.SECONDS).build()
          val req = Request.Builder().url("${BuildConfig.SUPABASE_URL}/rest/v1/").head().build()
          client.newCall(req).execute().use { res ->
            pingLatency = System.currentTimeMillis() - startPing
            supabaseHealthy = res.isSuccessful || res.code == 401 || res.code == 400 // Endpoint responded
          }
        } catch (_: Exception) {}

        list.add(
          DiagnosticItem(
            title = "Supabase Cloud Endpoint",
            status = if (supabaseHealthy) DiagnosticStatus.HEALTHY else DiagnosticStatus.DISCONNECTED,
            details = if (supabaseHealthy) "Endpoint reachable (${pingLatency}ms)" else "Unreachable or connection timed out",
            latencyMs = if (supabaseHealthy) pingLatency else null
          )
        )

        // Auth session status
        val authRepo = com.example.data.repository.AuthRepository()
        val user = try { authRepo.currentUser.firstOrNull() } catch (_: Exception) { null }
        list.add(
          DiagnosticItem(
            title = "Authentication Session",
            status = if (user != null) DiagnosticStatus.HEALTHY else DiagnosticStatus.AUTH_REQUIRED,
            details = if (user != null) "Active session established (${user.email ?: "Authenticated user"})" else "Local mode / Sign in available for cloud sync"
          )
        )
        list
      }
      results.add(DiagnosticCategory("Backend & Cloud", backendItems))

      // 2. Local Database & Sync
      val dbItems = withContext(Dispatchers.IO) {
        val list = mutableListOf<DiagnosticItem>()
        val tasks = taskRepository.allTasks.firstOrNull() ?: emptyList()
        val habits = habitRepository.allHabits.firstOrNull() ?: emptyList()
        val records = habitRepository.allRecords.firstOrNull() ?: emptyList()

        list.add(
          DiagnosticItem(
            title = "Room Local Database",
            status = DiagnosticStatus.HEALTHY,
            details = "Active • ${tasks.size} tasks, ${habits.size} habits, ${records.size} session logs"
          )
        )
        list
      }
      results.add(DiagnosticCategory("Data & Persistence", dbItems))

      // 3. Application Release & Version Service
      val releaseItems = withContext(Dispatchers.IO) {
        val list = mutableListOf<DiagnosticItem>()
        val update = AppUpdateManager.checkRemoteUpdate()
        val installedVer = BuildConfig.VERSION_NAME
        val installedCode = BuildConfig.VERSION_CODE

        if (update != null) {
          list.add(
            DiagnosticItem(
              title = "Release Synchronization",
              status = DiagnosticStatus.UPDATE_AVAILABLE,
              details = "Installed: v$installedVer (code $installedCode) • Latest: v${update.version} (code ${update.versionCode})"
            )
          )
        } else {
          list.add(
            DiagnosticItem(
              title = "Release Synchronization",
              status = DiagnosticStatus.UP_TO_DATE,
              details = "Installed: v$installedVer (code $installedCode) • Authoritative version confirmed"
            )
          )
        }
        list
      }
      results.add(DiagnosticCategory("Update & Release", releaseItems))

      // 4. Notifications & Alarms
      val notifItems = withContext(Dispatchers.IO) {
        val list = mutableListOf<DiagnosticItem>()
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        val areNotificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          notifManager?.areNotificationsEnabled() == true
        } else true

        list.add(
          DiagnosticItem(
            title = "Push & Reminder Notifications",
            status = if (areNotificationsEnabled) DiagnosticStatus.HEALTHY else DiagnosticStatus.PERMISSION_REQUIRED,
            details = if (areNotificationsEnabled) "Enabled • System delivery channels active" else "System notifications are disabled in Android Settings"
          )
        )
        list
      }
      results.add(DiagnosticCategory("Notifications", notifItems))

      // 5. Sensors & Gesture Capabilities
      val sensorItems = withContext(Dispatchers.IO) {
        val list = mutableListOf<DiagnosticItem>()
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accel = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val hasAccel = accel != null
        val shakeAction = AppPreferences.getShakeAction(context)
        val timerDuration = AppPreferences.getShakeTimerDuration(context)

        list.add(
          DiagnosticItem(
            title = "Accelerometer Hardware",
            status = if (hasAccel) DiagnosticStatus.HEALTHY else DiagnosticStatus.DEGRADED,
            details = if (hasAccel) "${accel?.name ?: "Hardware sensor ready"}" else "No accelerometer hardware detected"
          )
        )

        list.add(
          DiagnosticItem(
            title = "Shake Gesture Action",
            status = if (shakeAction != "DISABLED") DiagnosticStatus.HEALTHY else DiagnosticStatus.DEGRADED,
            details = "Configured: $shakeAction${if (shakeAction == "START_TIMER") " (${timerDuration} min preset)" else ""}"
          )
        )
        list
      }
      results.add(DiagnosticCategory("Sensors & Gestures", sensorItems))

      // 6. Local Storage Sandbox
      val storageItems = withContext(Dispatchers.IO) {
        val list = mutableListOf<DiagnosticItem>()
        val updatesDir = File(context.cacheDir, "updates")
        val cacheSizeMb = (context.cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()) / (1024f * 1024f)

        list.add(
          DiagnosticItem(
            title = "Local App Cache & Sandbox",
            status = DiagnosticStatus.HEALTHY,
            details = "Cache size: String.format(Locale.US, \"%.1f MB\", cacheSizeMb) • Updates staging: ${updatesDir.exists()}"
          )
        )
        list
      }
      results.add(DiagnosticCategory("Storage Integrity", storageItems))

      categories = results
      lastCheckedTimestamp = System.currentTimeMillis()
      isRunningChecks = false
    }
  }

  LaunchedEffect(Unit) {
    runAllDiagnostics()
  }

  fun copyRedactedReport() {
    val sb = StringBuilder()
    sb.appendLine("===========================================")
    sb.appendLine("TDlxHT SYSTEM DIAGNOSTICS REPORT (REDACTED)")
    sb.appendLine("App Version: v${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})")
    sb.appendLine("Android OS: API ${Build.VERSION.SDK_INT} (${Build.MANUFACTURER} ${Build.MODEL})")
    sb.appendLine("Timestamp: ${System.currentTimeMillis()}")
    sb.appendLine("===========================================")

    for (cat in categories) {
      sb.appendLine("\n[${cat.categoryName}]")
      for (item in cat.items) {
        sb.appendLine("- ${item.title}: [${item.status.label}] - ${item.details}")
      }
    }
    sb.appendLine("\n===========================================")
    sb.appendLine("All authentication tokens and sensitive credentials have been safely omitted.")

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("TDlxHT Diagnostics", sb.toString()))
    Toast.makeText(context, "Redacted diagnostic report copied to clipboard", Toast.LENGTH_SHORT).show()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .paperDotBackground()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .border(1.2.dp, CardBorder, CircleShape)
              .clip(CircleShape)
              .bounceClick { onNavigateBack() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = InkPrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "System Diagnostics",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
              )
            )
            Text(
              text = "Connection health & service verification",
              style = TextStyle(fontSize = 12.sp, color = InkSecondary)
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .border(1.2.dp, CardBorder, CircleShape)
              .clip(CircleShape)
              .bounceClick(enabled = !isRunningChecks) { runAllDiagnostics() },
            contentAlignment = Alignment.Center
          ) {
            if (isRunningChecks) {
              CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = InkPrimary,
                modifier = Modifier.size(18.dp)
              )
            } else {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = InkPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .size(40.dp)
              .border(1.2.dp, CardBorder, CircleShape)
              .clip(CircleShape)
              .bounceClick { copyRedactedReport() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Report",
              tint = InkPrimary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(categories.size) { idx ->
          val cat = categories[idx]
          SketchCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            borderWidth = 1.2.dp
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Text(
                text = cat.categoryName,
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )

              Spacer(modifier = Modifier.height(12.dp))

              cat.items.forEachIndexed { itemIdx, item ->
                if (itemIdx > 0) {
                  Spacer(modifier = Modifier.height(10.dp))
                }
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                      text = item.title,
                      style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = item.details,
                      style = TextStyle(fontSize = 11.sp, color = InkSecondary)
                    )
                  }

                  // Strict two-color status badge
                  val badgeBg = when (item.status) {
                    DiagnosticStatus.HEALTHY, DiagnosticStatus.UP_TO_DATE -> PillBackgroundDark
                    DiagnosticStatus.UPDATE_AVAILABLE -> PillBackgroundDark
                    else -> PillBackgroundDark.copy(alpha = 0.15f)
                  }
                  val badgeText = when (item.status) {
                    DiagnosticStatus.HEALTHY, DiagnosticStatus.UP_TO_DATE -> PillTextDark
                    DiagnosticStatus.UPDATE_AVAILABLE -> PillTextDark
                    else -> InkPrimary
                  }

                  Box(
                    modifier = Modifier
                      .background(badgeBg, RoundedCornerShape(12.dp))
                      .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                      .padding(horizontal = 9.dp, vertical = 4.dp)
                  ) {
                    Text(
                      text = item.status.label,
                      style = TextStyle(
                        fontFamily = SFProFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                      )
                    )
                  }
                }
              }
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}
