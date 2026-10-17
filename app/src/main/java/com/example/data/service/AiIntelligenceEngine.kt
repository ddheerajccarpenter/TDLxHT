package com.example.data.service

import android.content.Context
import android.content.SharedPreferences
import com.example.HabitTaskApplication
import com.example.data.local.AppDatabase
import com.example.data.local.toDomainModel
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class AiProductivityInsight(
  val insightType: String,
  val title: String,
  val message: String,
  val priority: String,
  val actionSuggestion: String,
  val confidence: Float = 0.9f,
  val source: String = "local"
)

@Serializable
private data class GeminiInsightResponse(
  val source: String? = null,
  val insight: AiProductivityInsight? = null
)

class AiIntelligenceEngine(
  private val context: Context = HabitTaskApplication.instance,
  private val db: AppDatabase = HabitTaskApplication.instance.database
) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("ai_intelligence_prefs", Context.MODE_PRIVATE)

  private val _latestInsight = MutableStateFlow<AiProductivityInsight?>(null)
  val latestInsight: StateFlow<AiProductivityInsight?> = _latestInsight.asStateFlow()

  private val json = Json { ignoreUnknownKeys = true }

  init {
    loadCachedInsight()
  }

  private fun loadCachedInsight() {
    val cachedTitle = prefs.getString("cached_title", null)
    val cachedMsg = prefs.getString("cached_msg", null)
    if (cachedTitle != null && cachedMsg != null) {
      _latestInsight.value = AiProductivityInsight(
        insightType = prefs.getString("cached_type", "daily_momentum") ?: "daily_momentum",
        title = cachedTitle,
        message = cachedMsg,
        priority = prefs.getString("cached_priority", "LOW") ?: "LOW",
        actionSuggestion = prefs.getString("cached_action", "Keep up the daily momentum") ?: "",
        source = prefs.getString("cached_source", "local") ?: "local"
      )
    }
  }

  private fun saveInsight(insight: AiProductivityInsight) {
    prefs.edit()
      .putString("cached_type", insight.insightType)
      .putString("cached_title", insight.title)
      .putString("cached_msg", insight.message)
      .putString("cached_priority", insight.priority)
      .putString("cached_action", insight.actionSuggestion)
      .putString("cached_source", insight.source)
      .putLong("last_fetch_time", System.currentTimeMillis())
      .apply()
    _latestInsight.value = insight
  }

  suspend fun refreshInsights(forceNetwork: Boolean = false): AiProductivityInsight = withContext(Dispatchers.IO) {
    val today = LocalDate.now()
    val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

    val tasks = db.taskDao().getAllTasksSync()
    val habits = db.habitDao().getAllHabitsSync()
    val records = db.habitCompletionRecordDao().getAllRecordsSync()

    val todayTasks = tasks.filter { it.dueDate == todayStr }
    val completedToday = todayTasks.count { it.isCompleted }
    val overdueTasks = tasks.count { !it.isCompleted && it.dueDate.isNotBlank() && it.dueDate < todayStr }

    val activeHabits = habits.filter { it.isActive && !it.isArchived && !it.isPaused }
    val todayCompletedHabits = records.count { it.date == todayStr && it.isCompleted }

    var longestStreak = 0
    habits.forEach { h ->
      val domainHabit = h.toDomainModel()
      val habitRecords = records.filter { it.habitId == h.id }.map { it.toDomainModel() }
      val streak = HabitRepository.calculateStreak(domainHabit, habitRecords, today).currentStreak
      if (streak > longestStreak) longestStreak = streak
    }

    val totalDayItems = todayTasks.size + activeHabits.size
    val completedDayItems = completedToday + todayCompletedHabits
    val score = if (totalDayItems > 0) ((completedDayItems.toDouble() / totalDayItems) * 100).toInt() else 0

    // For brand-new users with zero tasks and habits, provide onboarding guidance without fabricated activity
    if (totalDayItems == 0) {
      val onboardingInsight = AiProductivityInsight(
        insightType = "onboarding",
        title = "Welcome to TDlxHT",
        message = "Create your first task or habit to begin tracking your daily momentum and unlock personalized insights.",
        priority = "LOW",
        actionSuggestion = "Add your first task or habit",
        confidence = 1.0f,
        source = "local_rules"
      )
      saveInsight(onboardingInsight)
      _latestInsight.value = onboardingInsight
      return@withContext onboardingInsight
    }

    val lastFetch = prefs.getLong("last_fetch_time", 0L)
    val timeSinceLast = System.currentTimeMillis() - lastFetch
    val canCallNetwork = forceNetwork || timeSinceLast > 3600000L // 1 hour rate limit

    if (canCallNetwork) {
      try {
        val payload = """
          {
            "totalTasks": ${todayTasks.size},
            "completedTasks": $completedToday,
            "overdueTasks": $overdueTasks,
            "activeHabits": ${activeHabits.size},
            "habitsCompletedToday": $todayCompletedHabits,
            "longestStreak": $longestStreak,
            "productivityScore": $score
          }
        """.trimIndent()

        val baseUrl = try {
          com.example.BuildConfig.APP_WEB_URL.trimEnd('/')
        } catch (_: Exception) {
          "https://tdlxht.vercel.app"
        }
        val url = URL("$baseUrl/api/gemini-insights")
        val conn = (url.openConnection() as HttpURLConnection).apply {
          requestMethod = "POST"
          setRequestProperty("Content-Type", "application/json")
          setRequestProperty("Accept", "application/json")
          doOutput = true
          connectTimeout = 5000
          readTimeout = 5000
        }

        conn.outputStream.use { os ->
          os.write(payload.toByteArray(Charsets.UTF_8))
        }

        if (conn.responseCode == 200) {
          val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
          val parsed = json.decodeFromString<GeminiInsightResponse>(responseStr)
          parsed.insight?.let {
            val finalInsight = it.copy(source = parsed.source ?: "gemini")
            saveInsight(finalInsight)
            return@withContext finalInsight
          }
        }
      } catch (_: Exception) {
        // Fall back to local rule engine without crashing
      }
    }

    // Local Deterministic Rule Engine
    val localInsight = when {
      totalDayItems == 0 -> AiProductivityInsight(
        insightType = "onboarding",
        title = "Welcome to TDlxHT",
        message = "Create your first task or habit to begin tracking your daily momentum and unlock personalized insights.",
        priority = "LOW",
        actionSuggestion = "Add your first task or habit",
        confidence = 1.0f,
        source = "local_rules"
      )
      overdueTasks >= 3 -> AiProductivityInsight(
        insightType = "task_overload",
        title = "Task Overload Alert",
        message = "You have $overdueTasks overdue tasks. Clear the backlog or reschedule non-urgent items to stay focused.",
        priority = "HIGH",
        actionSuggestion = "Reschedule non-urgent tasks",
        confidence = 0.95f,
        source = "local_rules"
      )
      longestStreak >= 7 && todayCompletedHabits == activeHabits.size && activeHabits.isNotEmpty() -> AiProductivityInsight(
        insightType = "streak_milestone",
        title = "Incredible Consistency",
        message = "You have an active streak of $longestStreak days and completed all habits today!",
        priority = "MEDIUM",
        actionSuggestion = "Rest well and repeat tomorrow",
        confidence = 0.98f,
        source = "local_rules"
      )
      activeHabits.isNotEmpty() && todayCompletedHabits == 0 -> AiProductivityInsight(
        insightType = "missed_habits",
        title = "Daily Habits Waiting",
        message = "You have ${activeHabits.size} active habits waiting for today. Tackle the easiest one first!",
        priority = "MEDIUM",
        actionSuggestion = "Check off your first habit",
        confidence = 0.92f,
        source = "local_rules"
      )
      else -> AiProductivityInsight(
        insightType = "daily_momentum",
        title = "Daily Progress Tracked",
        message = "Completed $completedToday/${todayTasks.size} tasks and $todayCompletedHabits habits today ($score% score).",
        priority = "LOW",
        actionSuggestion = "Keep up the momentum",
        confidence = 0.88f,
        source = "local_rules"
      )
    }

    saveInsight(localInsight)
    localInsight
  }

  fun clearCache() {
    prefs.edit().clear().apply()
    _latestInsight.value = null
  }

  companion object {
    val instance by lazy { AiIntelligenceEngine() }
  }
}
