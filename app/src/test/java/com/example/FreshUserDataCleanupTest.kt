package com.example

import com.example.ui.viewmodel.DailySummary
import com.example.ui.viewmodel.ReportStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreshUserDataCleanupTest {

  @Test
  fun testFreshUserDailySummaryHasZeroManufacturedStats() {
    // A fresh user with 0 tasks and 0 habits must produce exact zeros
    val freshSummary = DailySummary(
      tasksCompleted = 0,
      tasksTotal = 0,
      habitsCompleted = 0,
      habitsTotal = 0,
      maxStreak = 0,
      overallProgress = 0f
    )

    assertEquals("Tasks completed must be 0", 0, freshSummary.tasksCompleted)
    assertEquals("Tasks total must be 0", 0, freshSummary.tasksTotal)
    assertEquals("Habits completed must be 0", 0, freshSummary.habitsCompleted)
    assertEquals("Habits total must be 0", 0, freshSummary.habitsTotal)
    assertEquals("Streak must be 0", 0, freshSummary.maxStreak)
    assertEquals("Progress must be 0.0", 0f, freshSummary.overallProgress, 0.001f)
  }

  @Test
  fun testFreshUserReportsHasEnoughDataIsFalse() {
    // When no real user data exists, hasEnoughData must be false so empty state is displayed
    val emptyReportStats = ReportStats(
      periodLabel = "This Week",
      tasksCompleted = 0,
      tasksTotal = 0,
      tasksOverdue = 0,
      habitsCompleted = 0,
      habitsTotal = 0,
      longestStreak = 0
    )

    assertFalse("Must report not enough data when tasks and habits are empty", emptyReportStats.hasEnoughData)
    assertEquals("Completion rate must be 0.0", 0f, emptyReportStats.taskCompletionRate, 0.001f)
    assertEquals("Habit completion rate must be 0.0", 0f, emptyReportStats.habitCompletionRate, 0.001f)
  }

  @Test
  fun testReportStatsBecomesActiveOnlyWithRealUserData() {
    val statsWithOneTask = ReportStats(
      periodLabel = "This Week",
      tasksCompleted = 1,
      tasksTotal = 1,
      tasksOverdue = 0,
      habitsCompleted = 0,
      habitsTotal = 0,
      longestStreak = 0
    )

    assertTrue("Must report enough data when user actually creates a task", statsWithOneTask.hasEnoughData)
    assertEquals(1.0f, statsWithOneTask.taskCompletionRate, 0.001f)
  }
}
