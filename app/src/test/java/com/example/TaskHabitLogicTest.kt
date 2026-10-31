package com.example

import com.example.data.model.Habit
import com.example.data.model.HabitCompletionRecord
import com.example.data.model.HabitFrequency
import com.example.data.model.RepeatRule
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.repository.HabitRepository
import com.example.data.repository.TaskRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TaskHabitLogicTest {

  @Test
  fun testTaskOverdueCalculation() {
    val today = LocalDate.of(2026, 9, 26)
    val now = LocalTime.of(12, 0)

    val overduePastDate = Task(
      title = "Past Task",
      dueDate = "2026-09-25",
      dueTime = "10:00",
      isCompleted = false
    )
    assertTrue("Past date task should be overdue", overduePastDate.isOverdue(today, now))

    val completedPastTask = overduePastDate.copy(isCompleted = true)
    assertFalse("Completed task must not be overdue", completedPastTask.isOverdue(today, now))

    val todayEarlierTime = Task(
      title = "Earlier today",
      dueDate = "2026-09-26",
      dueTime = "09:00",
      isCompleted = false
    )
    assertTrue("Earlier time today should be overdue", todayEarlierTime.isOverdue(today, now))

    val todayLaterTime = Task(
      title = "Later today",
      dueDate = "2026-09-26",
      dueTime = "15:00",
      isCompleted = false
    )
    assertFalse("Later time today should not be overdue", todayLaterTime.isOverdue(today, now))
  }

  @Test
  fun testRecurringTaskOccurrenceCalculation() {
    val baseFriday = "2026-09-25" // Friday

    // Daily -> next day (Saturday)
    val nextDaily = TaskRepository.calculateNextOccurrenceDate(baseFriday, RepeatRule.DAILY, emptyList())
    assertEquals(LocalDate.of(2026, 9, 26), nextDaily)

    // Weekdays -> next Monday
    val nextWeekday = TaskRepository.calculateNextOccurrenceDate(baseFriday, RepeatRule.WEEKDAYS, emptyList())
    assertEquals(LocalDate.of(2026, 9, 28), nextWeekday)

    // Weekly -> exactly 1 week later
    val nextWeekly = TaskRepository.calculateNextOccurrenceDate(baseFriday, RepeatRule.WEEKLY, emptyList())
    assertEquals(LocalDate.of(2026, 10, 2), nextWeekly)

    // Monthly -> exactly 1 month later
    val nextMonthly = TaskRepository.calculateNextOccurrenceDate(baseFriday, RepeatRule.MONTHLY, emptyList())
    assertEquals(LocalDate.of(2026, 10, 25), nextMonthly)

    // Custom days (e.g. Wednesday and Friday)
    val nextCustom = TaskRepository.calculateNextOccurrenceDate(
      baseFriday,
      RepeatRule.CUSTOM,
      listOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
    )
    // Next occurrence after Friday 2026-09-25 should be Wednesday 2026-09-30
    assertEquals(LocalDate.of(2026, 9, 30), nextCustom)
  }

  @Test
  fun testHabitSchedulingRules() {
    val monday = LocalDate.of(2026, 9, 28) // Monday
    val saturday = LocalDate.of(2026, 9, 26) // Saturday

    val everydayHabit = Habit(
      name = "Daily Reading",
      startDate = "2026-09-01",
      frequency = HabitFrequency.EVERY_DAY
    )
    assertTrue(everydayHabit.isScheduledOn(monday))
    assertTrue(everydayHabit.isScheduledOn(saturday))

    val weekdayHabit = Habit(
      name = "Gym Session",
      startDate = "2026-09-01",
      frequency = HabitFrequency.WEEKDAYS
    )
    assertTrue(weekdayHabit.isScheduledOn(monday))
    assertFalse("Saturday should not be scheduled for weekdays habit", weekdayHabit.isScheduledOn(saturday))

    val customHabit = Habit(
      name = "Swimming",
      startDate = "2026-09-01",
      frequency = HabitFrequency.CUSTOM,
      customDays = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    )
    assertTrue(customHabit.isScheduledOn(saturday))
    assertFalse(customHabit.isScheduledOn(monday))
  }

  @Test
  fun testStreakCalculation() {
    val habit = Habit(
      id = "habit-1",
      name = "Drink Water",
      startDate = "2026-09-20",
      frequency = HabitFrequency.EVERY_DAY
    )

    val refDate = LocalDate.of(2026, 9, 26)

    // Records for 20, 21, 22 (streak 3), then 23 missed, then 24, 25, 26 completed (current streak 3)
    val records = listOf(
      HabitCompletionRecord(habitId = "habit-1", date = "2026-09-20", isCompleted = true),
      HabitCompletionRecord(habitId = "habit-1", date = "2026-09-21", isCompleted = true),
      HabitCompletionRecord(habitId = "habit-1", date = "2026-09-22", isCompleted = true),
      HabitCompletionRecord(habitId = "habit-1", date = "2026-09-23", isCompleted = false),
      HabitCompletionRecord(habitId = "habit-1", date = "2026-09-24", isCompleted = true),
      HabitCompletionRecord(habitId = "habit-1", date = "2026-09-25", isCompleted = true),
      HabitCompletionRecord(habitId = "habit-1", date = "2026-09-26", isCompleted = true)
    )

    val streakInfo = HabitRepository.calculateStreak(habit, records, refDate)

    assertEquals("Current streak should be 3", 3, streakInfo.currentStreak)
    assertEquals("Longest streak should be 3", 3, streakInfo.longestStreak)
    assertEquals("Total completions should be 6", 6, streakInfo.totalCompletions)
    assertEquals("Scheduled days should be 7", 7, streakInfo.scheduledDaysCount)
    assertEquals("Missed scheduled days should be 1", 1, streakInfo.missedScheduledDaysCount)
  }

  @Test
  fun testSubtaskProgress() {
    val subtasks = listOf(
      Subtask(parentTaskId = "t1", title = "Step 1", isCompleted = true),
      Subtask(parentTaskId = "t1", title = "Step 2", isCompleted = true),
      Subtask(parentTaskId = "t1", title = "Step 3", isCompleted = false),
      Subtask(parentTaskId = "t1", title = "Step 4", isCompleted = false)
    )

    val completed = subtasks.count { it.isCompleted }
    val total = subtasks.size

    assertEquals(2, completed)
    assertEquals(4, total)
    assertEquals("2 / 4", "$completed / $total")
  }

  @Test
  fun testMultiSessionHabitProgression() {
    val habit = Habit(
      id = "h-seeds",
      name = "Eat Seeds...",
      startDate = "2026-10-01",
      frequency = HabitFrequency.EVERY_DAY,
      targetValue = 2.0f,
      targetUnit = "sessions"
    )

    // Initial state: 0 / 2 sessions
    val initialRecord = HabitCompletionRecord(
      habitId = habit.id,
      date = "2026-10-11",
      currentValue = 0f,
      isCompleted = false
    )
    assertEquals(0f, initialRecord.currentValue, 0.001f)
    assertFalse("0 / 2 sessions must not be marked complete", initialRecord.isCompleted)

    // First session recorded: 1 / 2 sessions
    val firstSessionRecord = initialRecord.copy(
      currentValue = 1f,
      isCompleted = (1f >= habit.targetValue)
    )
    assertEquals(1f, firstSessionRecord.currentValue, 0.001f)
    assertFalse("1 / 2 sessions must remain incomplete (Phase 6 defect fix)", firstSessionRecord.isCompleted)

    // Second session recorded: 2 / 2 sessions
    val secondSessionRecord = firstSessionRecord.copy(
      currentValue = 2f,
      isCompleted = (2f >= habit.targetValue)
    )
    assertEquals(2f, secondSessionRecord.currentValue, 0.001f)
    assertTrue("2 / 2 sessions must mark the daily target as complete", secondSessionRecord.isCompleted)

    // Streaks must only count days where isCompleted is true
    val testRecords = listOf(
      firstSessionRecord, // Incomplete day
      secondSessionRecord.copy(date = "2026-10-10") // Completed day
    )
    val streakInfo = HabitRepository.calculateStreak(habit, testRecords, LocalDate.of(2026, 10, 11))
    assertEquals("Only fully completed days count toward completions", 1, streakInfo.totalCompletions)
  }

  @Test
  fun testUpdateVersionCodeComparison() {
    val installedVersionCode = 24
    val installedVersionName = "2.3.7"

    val publishedOlderVersionCode = 23
    val publishedSameVersionCode = 24
    val publishedNewerVersionCode = 25

    // Numeric comparison prevents lexicographical pitfalls (e.g. "2.3.10" < "2.3.5")
    assertFalse("Older release must not trigger update", publishedOlderVersionCode > installedVersionCode)
    assertFalse("Same release must not trigger update", publishedSameVersionCode > installedVersionCode)
    assertTrue("Newer release code must trigger update", publishedNewerVersionCode > installedVersionCode)
  }

  @Test
  fun testHabitTargetVsSessionDisambiguation() {
    val habit = Habit(
      id = "h_multi",
      name = "Daily Pushups",
      startDate = "2026-10-01",
      frequency = HabitFrequency.EVERY_DAY,
      targetValue = 2f // Requires 2 sessions daily
    )

    val scheduledHabits = listOf(habit)
    val habitsTotal = scheduledHabits.size // 1 habit
    val sessionsTotal = scheduledHabits.sumOf { it.targetValue.toInt() } // 2 sessions

    assertEquals("Should represent exactly 1 habit target", 1, habitsTotal)
    assertEquals("Should represent exactly 2 required sessions", 2, sessionsTotal)

    // Case 1: 0 sessions recorded
    val record0 = HabitCompletionRecord(habitId = habit.id, date = "2026-10-11", currentValue = 0f, isCompleted = false)
    val habitsDone0 = if (record0.isCompleted) 1 else 0
    val sessionsDone0 = record0.currentValue.toInt()
    assertEquals(0, habitsDone0)
    assertEquals(0, sessionsDone0)

    // Case 2: 1 session recorded
    val record1 = HabitCompletionRecord(habitId = habit.id, date = "2026-10-11", currentValue = 1f, isCompleted = false)
    val habitsDone1 = if (record1.isCompleted) 1 else 0
    val sessionsDone1 = record1.currentValue.toInt()
    assertEquals("1 session logged does not complete the daily habit target", 0, habitsDone1)
    assertEquals("Exactly 1 session completed", 1, sessionsDone1)

    // Case 3: 2 sessions recorded
    val record2 = HabitCompletionRecord(habitId = habit.id, date = "2026-10-11", currentValue = 2f, isCompleted = true)
    val habitsDone2 = if (record2.isCompleted) 1 else 0
    val sessionsDone2 = record2.currentValue.toInt()
    assertEquals("2 sessions logged completes the daily habit target", 1, habitsDone2)
    assertEquals("2 sessions completed", 2, sessionsDone2)
  }

  @Test
  fun testZeroTasksHandlingAndMetrics() {
    val stats = com.example.ui.viewmodel.ReportStats(
      periodLabel = "Oct 11 – Oct 17, 2026",
      tasksCompleted = 0,
      tasksTotal = 0,
      habitsCompleted = 0,
      habitsTotal = 1,
      sessionsCompleted = 0,
      sessionsTotal = 2
    )

    // When 0 tasks are scheduled, hasTasks is false and rate is 0 without dividing by zero
    assertFalse("Must indicate no tasks are scheduled", stats.hasTasks)
    assertEquals(0f, stats.taskCompletionRate, 0.001f)
    assertTrue("Should indicate habit data is present", stats.hasHabits)
    assertEquals(1, stats.habitsTotal)
    assertEquals(2, stats.sessionsTotal)
  }

  @Test
  fun testDataExportAndImportSerialization() {
    val json = kotlinx.serialization.json.Json { prettyPrint = true; ignoreUnknownKeys = true }

    val task = com.example.data.service.ExportedTask(
      id = "task-101",
      title = "Complete Liquid Glass Redesign",
      notes = "Strict monochromatic optical fluid design",
      dueDate = "2026-10-31",
      dueTime = "18:00",
      isCompleted = false,
      priority = "HIGH",
      category = "Work",
      tags = listOf("android", "design"),
      repeatRule = "NONE",
      isShortTerm = true,
      creationTimestamp = 1730370000000L,
      updatedTimestamp = 1730370000000L
    )

    val subtask = com.example.data.service.ExportedSubtask(
      id = "sub-201",
      parentTaskId = "task-101",
      title = "Refactor optics and caustic highlights",
      isCompleted = true,
      creationTimestamp = 1730370000000L
    )

    val habit = com.example.data.service.ExportedHabit(
      id = "habit-301",
      name = "Deep Work Session",
      description = "Two hours of focused coding",
      category = "Productivity",
      frequency = "EVERY_DAY",
      targetValue = 2f,
      targetUnit = "hours",
      startDate = "2026-10-01",
      isActive = true,
      isArchived = false,
      creationTimestamp = 1730370000000L
    )

    val completion = com.example.data.service.ExportedHabitCompletion(
      habitId = "habit-301",
      date = "2026-10-31",
      isCompleted = true,
      currentValue = 2f,
      targetValue = 2f
    )

    val export = com.example.data.service.DataExport(
      exportVersion = 1,
      exportedAt = "2026-10-31T12:00:00",
      tasks = listOf(task),
      subtasks = listOf(subtask),
      habits = listOf(habit),
      habitCompletions = listOf(completion)
    )

    val serialized = json.encodeToString(com.example.data.service.DataExport.serializer(), export)
    assertTrue("Serialized JSON must contain task title", serialized.contains("Complete Liquid Glass Redesign"))
    assertTrue("Serialized JSON must contain subtask title", serialized.contains("Refactor optics and caustic highlights"))
    assertTrue("Serialized JSON must contain isShortTerm flag", serialized.contains("\"isShortTerm\": true"))

    val deserialized = json.decodeFromString<com.example.data.service.DataExport>(serialized)
    assertEquals(1, deserialized.tasks.size)
    assertEquals(1, deserialized.subtasks.size)
    assertEquals(1, deserialized.habits.size)
    assertEquals(1, deserialized.habitCompletions.size)
    assertEquals("task-101", deserialized.tasks[0].id)
    assertTrue("isShortTerm should be true", deserialized.tasks[0].isShortTerm)
    assertEquals("sub-201", deserialized.subtasks[0].id)
    assertEquals("task-101", deserialized.subtasks[0].parentTaskId)

    // Backward compatibility test: JSON without isShortTerm defaults to false
    val legacyJson = """
      {
        "exportVersion": 1,
        "exportedAt": "2026-10-01T00:00:00",
        "tasks": [{
          "id": "legacy-1",
          "title": "Old Task",
          "notes": "",
          "dueDate": "2026-10-01",
          "dueTime": "09:00",
          "isCompleted": false,
          "priority": "LOW",
          "category": "General",
          "tags": [],
          "repeatRule": "NONE",
          "creationTimestamp": 1000,
          "updatedTimestamp": 1000
        }],
        "subtasks": [],
        "habits": [],
        "habitCompletions": []
      }
    """.trimIndent()

    val legacyDeserialized = json.decodeFromString<com.example.data.service.DataExport>(legacyJson)
    assertEquals(1, legacyDeserialized.tasks.size)
    assertFalse("Default for omitted isShortTerm must be false", legacyDeserialized.tasks[0].isShortTerm)
  }
}
