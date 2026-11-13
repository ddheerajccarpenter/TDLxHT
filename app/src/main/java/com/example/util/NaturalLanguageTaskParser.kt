package com.example.util

import com.example.data.model.ReminderOption
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.UUID

/**
 * Structured result produced by parsing natural language user input.
 */
data class ParsedTaskResult(
  val cleanTitle: String,
  val dueDate: LocalDate,
  val dueTime: LocalTime?,
  val priority: TaskPriority,
  val category: String,
  val tags: List<String>,
  val isShortTerm: Boolean,
  val reminderOption: ReminderOption,
  val hasExplicitDate: Boolean,
  val hasExplicitTime: Boolean,
  val hasExplicitPriority: Boolean,
  val hasExplicitCategory: Boolean
) {
  fun toTask(): Task {
    val dateStr = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val timeStr = (dueTime ?: LocalTime.of(9, 0)).format(DateTimeFormatter.ofPattern("HH:mm"))
    return Task(
      id = UUID.randomUUID().toString(),
      title = cleanTitle.ifBlank { "New Task" },
      dueDate = dateStr,
      dueTime = timeStr,
      priority = priority,
      category = category,
      tags = tags,
      isShortTerm = isShortTerm,
      reminderSetting = reminderOption,
      creationTimestamp = System.currentTimeMillis(),
      updatedTimestamp = System.currentTimeMillis()
    )
  }
}

/**
 * High-speed natural language task input parser.
 * Extracts dates, times, priority flags, categories, tags, and scratch indicators.
 */
object NaturalLanguageTaskParser {

  private val PRIORITY_HIGH_REGEX = Regex("""!(?:high|urgent|p1|h)\b""", RegexOption.IGNORE_CASE)
  private val PRIORITY_MED_REGEX = Regex("""!(?:medium|med|p2|m)\b""", RegexOption.IGNORE_CASE)
  private val PRIORITY_LOW_REGEX = Regex("""!(?:low|p3|l)\b""", RegexOption.IGNORE_CASE)

  private val SHORT_TERM_REGEX = Regex("""~(?:scratch|quick|short)\b""", RegexOption.IGNORE_CASE)
  private val REMINDER_REGEX = Regex("""\b(?:with\s+)?remind(?:er| me)?\b|\balert\b""", RegexOption.IGNORE_CASE)
  private val HASHTAG_REGEX = Regex("""#([a-zA-Z0-9_\-]+)""")

  // 12-hour time pattern: "3pm", "3:30pm", "at 10:15 am", "11am"
  private val TIME_12H_REGEX = Regex("""\b(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b""", RegexOption.IGNORE_CASE)
  // 24-hour time pattern: "at 15:30", "09:00", "22:15"
  private val TIME_24H_REGEX = Regex("""\b(?:at\s+)?([01]?\d|2[0-3]):([0-5]\d)\b""", RegexOption.IGNORE_CASE)

  // Relative keywords
  private val TODAY_REGEX = Regex("""\b(?:today)\b""", RegexOption.IGNORE_CASE)
  private val TONIGHT_REGEX = Regex("""\b(?:tonight)\b""", RegexOption.IGNORE_CASE)
  private val TOMORROW_REGEX = Regex("""\b(?:tomorrow|tmrw)\b""", RegexOption.IGNORE_CASE)
  private val YESTERDAY_REGEX = Regex("""\b(?:yesterday)\b""", RegexOption.IGNORE_CASE)

  // Weekday pattern with optional prefix: "next friday", "this monday", "on tuesday", "by sunday"
  private val WEEKDAY_REGEX = Regex(
    """\b(?:(next|this|on|by)\s+)?(monday|mon|tuesday|tue|wednesday|wed|thursday|thu|friday|fri|saturday|sat|sunday|sun)\b""",
    RegexOption.IGNORE_CASE
  )

  // Month + Day: "Nov 15", "November 15th", "Dec 25", "15 Nov", "15th of December"
  private val MONTH_NAMES = "jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?"
  private val MONTH_FIRST_REGEX = Regex("""\b($MONTH_NAMES)\s+(\d{1,2})(?:st|nd|rd|th)?\b""", RegexOption.IGNORE_CASE)
  private val DAY_FIRST_REGEX = Regex("""\b(\d{1,2})(?:st|nd|rd|th)?(?:\s+of)?\s+($MONTH_NAMES)\b""", RegexOption.IGNORE_CASE)

  // Broad time of day: "morning", "in the afternoon", "evening", "at night"
  private val TIME_OF_DAY_REGEX = Regex("""\b(?:(?:in\s+the|at)\s+)?(morning|afternoon|evening|night)\b""", RegexOption.IGNORE_CASE)

  fun parse(
    input: String,
    baseDate: LocalDate = LocalDate.now(),
    baseTime: LocalTime = LocalTime.now()
  ): ParsedTaskResult {
    var text = input
    val rangesToRemove = mutableListOf<IntRange>()

    // 1. Priority
    var priority = TaskPriority.MEDIUM
    var hasExplicitPriority = false
    PRIORITY_HIGH_REGEX.find(text)?.let {
      priority = TaskPriority.HIGH
      hasExplicitPriority = true
      rangesToRemove.add(it.range)
    }
    if (!hasExplicitPriority) {
      PRIORITY_MED_REGEX.find(text)?.let {
        priority = TaskPriority.MEDIUM
        hasExplicitPriority = true
        rangesToRemove.add(it.range)
      }
    }
    if (!hasExplicitPriority) {
      PRIORITY_LOW_REGEX.find(text)?.let {
        priority = TaskPriority.LOW
        hasExplicitPriority = true
        rangesToRemove.add(it.range)
      }
    }

    // 2. Short-term flag
    var isShortTerm = false
    SHORT_TERM_REGEX.find(text)?.let {
      isShortTerm = true
      rangesToRemove.add(it.range)
    }

    // 3. Reminder option
    var reminderOption = ReminderOption.NONE
    REMINDER_REGEX.find(text)?.let {
      reminderOption = ReminderOption.AT_DUE_TIME
      rangesToRemove.add(it.range)
    }

    // 4. Hashtags & Category
    val tags = mutableListOf<String>()
    var category = TaskCategory.WORK.displayName
    var hasExplicitCategory = false

    HASHTAG_REGEX.findAll(text).forEach { match ->
      val tagContent = match.groupValues[1]
      tags.add(tagContent)
      rangesToRemove.add(match.range)

      // Check if tag maps to a standard category
      val matchedCategory = TaskCategory.entries.firstOrNull {
        it.displayName.equals(tagContent, ignoreCase = true) || it.name.equals(tagContent, ignoreCase = true)
      }
      if (matchedCategory != null && !hasExplicitCategory) {
        category = matchedCategory.displayName
        hasExplicitCategory = true
      }
    }

    // 5. Time parsing
    var dueTime: LocalTime? = null
    var hasExplicitTime = false

    // Check 12-hour time ("3:30pm", "4pm")
    val time12Match = TIME_12H_REGEX.find(text)
    if (time12Match != null) {
      val hourStr = time12Match.groupValues[1]
      val minStr = time12Match.groupValues[2].ifBlank { "00" }
      val amPm = time12Match.groupValues[3].lowercase(Locale.ROOT)

      var hour = hourStr.toIntOrNull() ?: 9
      val min = minStr.toIntOrNull() ?: 0

      if (amPm == "pm" && hour < 12) hour += 12
      if (amPm == "am" && hour == 12) hour = 0

      dueTime = LocalTime.of(hour.coerceIn(0, 23), min.coerceIn(0, 59))
      hasExplicitTime = true
      rangesToRemove.add(time12Match.range)
    }

    // Check 24-hour time ("15:30") if not already matched
    if (!hasExplicitTime) {
      val time24Match = TIME_24H_REGEX.find(text)
      if (time24Match != null) {
        val hour = time24Match.groupValues[1].toIntOrNull() ?: 9
        val min = time24Match.groupValues[2].toIntOrNull() ?: 0
        dueTime = LocalTime.of(hour.coerceIn(0, 23), min.coerceIn(0, 59))
        hasExplicitTime = true
        rangesToRemove.add(time24Match.range)
      }
    }

    // Check broad time of day if still no time
    if (!hasExplicitTime) {
      val timeOfDayMatch = TIME_OF_DAY_REGEX.find(text)
      if (timeOfDayMatch != null) {
        val period = timeOfDayMatch.groupValues[1].lowercase(Locale.ROOT)
        dueTime = when (period) {
          "morning" -> LocalTime.of(9, 0)
          "afternoon" -> LocalTime.of(14, 0)
          "evening" -> LocalTime.of(18, 0)
          "night" -> LocalTime.of(20, 0)
          else -> LocalTime.of(9, 0)
        }
        hasExplicitTime = true
        rangesToRemove.add(timeOfDayMatch.range)
      }
    }

    // 6. Date parsing
    var dueDate = baseDate
    var hasExplicitDate = false

    // Relative days
    val tomorrowMatch = TOMORROW_REGEX.find(text)
    if (tomorrowMatch != null) {
      dueDate = baseDate.plusDays(1)
      hasExplicitDate = true
      rangesToRemove.add(tomorrowMatch.range)
    }

    val tonightMatch = TONIGHT_REGEX.find(text)
    if (!hasExplicitDate && tonightMatch != null) {
      dueDate = baseDate
      if (!hasExplicitTime) {
        dueTime = LocalTime.of(20, 0)
        hasExplicitTime = true
      }
      hasExplicitDate = true
      rangesToRemove.add(tonightMatch.range)
    }

    val todayMatch = TODAY_REGEX.find(text)
    if (!hasExplicitDate && todayMatch != null) {
      dueDate = baseDate
      hasExplicitDate = true
      rangesToRemove.add(todayMatch.range)
    }

    val yesterdayMatch = YESTERDAY_REGEX.find(text)
    if (!hasExplicitDate && yesterdayMatch != null) {
      dueDate = baseDate.minusDays(1)
      hasExplicitDate = true
      rangesToRemove.add(yesterdayMatch.range)
    }

    // Weekdays ("next friday", "tuesday")
    if (!hasExplicitDate) {
      val weekdayMatch = WEEKDAY_REGEX.find(text)
      if (weekdayMatch != null) {
        val modifier = weekdayMatch.groupValues[1].lowercase(Locale.ROOT)
        val dayName = weekdayMatch.groupValues[2].lowercase(Locale.ROOT)
        val targetDay = parseDayOfWeek(dayName)

        if (targetDay != null) {
          dueDate = if (modifier == "next") {
            // Next week's occurrence
            baseDate.with(TemporalAdjusters.next(targetDay))
          } else {
            // Upcoming occurrence (if today is that day and not specified as 'this', next)
            if (baseDate.dayOfWeek == targetDay) {
              baseDate
            } else {
              baseDate.with(TemporalAdjusters.next(targetDay))
            }
          }
          hasExplicitDate = true
          rangesToRemove.add(weekdayMatch.range)
        }
      }
    }

    // Month + Day ("Nov 15" or "15 Nov")
    if (!hasExplicitDate) {
      val monthFirstMatch = MONTH_FIRST_REGEX.find(text)
      if (monthFirstMatch != null) {
        val monthStr = monthFirstMatch.groupValues[1]
        val day = monthFirstMatch.groupValues[2].toIntOrNull() ?: 1
        val month = parseMonth(monthStr)
        if (month != null) {
          var year = baseDate.year
          val candidate = try {
            LocalDate.of(year, month, day.coerceIn(1, month.length(year % 4 == 0)))
          } catch (_: Exception) {
            LocalDate.of(year, month, 1)
          }
          // If the parsed date has already passed this year, assign to next year
          dueDate = if (candidate.isBefore(baseDate)) candidate.plusYears(1) else candidate
          hasExplicitDate = true
          rangesToRemove.add(monthFirstMatch.range)
        }
      } else {
        val dayFirstMatch = DAY_FIRST_REGEX.find(text)
        if (dayFirstMatch != null) {
          val day = dayFirstMatch.groupValues[1].toIntOrNull() ?: 1
          val monthStr = dayFirstMatch.groupValues[2]
          val month = parseMonth(monthStr)
          if (month != null) {
            var year = baseDate.year
            val candidate = try {
              LocalDate.of(year, month, day.coerceIn(1, month.length(year % 4 == 0)))
            } catch (_: Exception) {
              LocalDate.of(year, month, 1)
            }
            dueDate = if (candidate.isBefore(baseDate)) candidate.plusYears(1) else candidate
            hasExplicitDate = true
            rangesToRemove.add(dayFirstMatch.range)
          }
        }
      }
    }

    // 7. Clean title reconstruction
    // Sort ranges descending so removing doesn't skew subsequent indices
    val sortedRanges = rangesToRemove.sortedByDescending { it.first }
    val sb = StringBuilder(text)
    for (range in sortedRanges) {
      if (range.first in 0 until sb.length && range.last < sb.length) {
        sb.delete(range.first, range.last + 1)
      }
    }

    // Clean extraneous spaces, dangling prepositions, and punctuation
    var cleanTitle = sb.toString()
      .replace(Regex("""\s+"""), " ")
      .trim()

    // Remove leading/trailing connecting words left behind (e.g., "on", "at", "by", "for", "with")
    cleanTitle = cleanTitle
      .replace(Regex("""^(?:on|at|by|for|with|due)\s+""", RegexOption.IGNORE_CASE), "")
      .replace(Regex("""\s+(?:on|at|by|for|with|due)$""", RegexOption.IGNORE_CASE), "")
      .trim(' ', ',', '-', ':', '.')

    if (cleanTitle.isBlank()) {
      cleanTitle = "New Task"
    }

    return ParsedTaskResult(
      cleanTitle = cleanTitle,
      dueDate = dueDate,
      dueTime = dueTime,
      priority = priority,
      category = category,
      tags = tags,
      isShortTerm = isShortTerm,
      reminderOption = reminderOption,
      hasExplicitDate = hasExplicitDate,
      hasExplicitTime = hasExplicitTime,
      hasExplicitPriority = hasExplicitPriority,
      hasExplicitCategory = hasExplicitCategory
    )
  }

  private fun parseDayOfWeek(str: String): DayOfWeek? {
    return when (str.lowercase(Locale.ROOT).take(3)) {
      "mon" -> DayOfWeek.MONDAY
      "tue" -> DayOfWeek.TUESDAY
      "wed" -> DayOfWeek.WEDNESDAY
      "thu" -> DayOfWeek.THURSDAY
      "fri" -> DayOfWeek.FRIDAY
      "sat" -> DayOfWeek.SATURDAY
      "sun" -> DayOfWeek.SUNDAY
      else -> null
    }
  }

  private fun parseMonth(str: String): Month? {
    return when (str.lowercase(Locale.ROOT).take(3)) {
      "jan" -> Month.JANUARY
      "feb" -> Month.FEBRUARY
      "mar" -> Month.MARCH
      "apr" -> Month.APRIL
      "may" -> Month.MAY
      "jun" -> Month.JUNE
      "jul" -> Month.JULY
      "aug" -> Month.AUGUST
      "sep" -> Month.SEPTEMBER
      "oct" -> Month.OCTOBER
      "nov" -> Month.NOVEMBER
      "dec" -> Month.DECEMBER
      else -> null
    }
  }
}

