package com.example

import com.example.data.model.ReminderOption
import com.example.data.model.TaskPriority
import com.example.util.NaturalLanguageTaskParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class NaturalLanguageTaskParserTest {

  private val fixedToday = LocalDate.of(2026, 11, 13) // Friday
  private val fixedNow = LocalTime.of(10, 0)

  @Test
  fun testSimpleTaskWithoutTokens() {
    val result = NaturalLanguageTaskParser.parse("Buy milk", fixedToday, fixedNow)
    assertEquals("Buy milk", result.cleanTitle)
    assertEquals(fixedToday, result.dueDate)
    assertEquals(TaskPriority.MEDIUM, result.priority)
    assertFalse(result.hasExplicitDate)
    assertFalse(result.hasExplicitTime)
    assertFalse(result.hasExplicitPriority)
    assertFalse(result.isShortTerm)
  }

  @Test
  fun testTomorrowWithTimeAndPriority() {
    val result = NaturalLanguageTaskParser.parse(
      "Dentist appointment tomorrow at 3:30pm !high",
      fixedToday,
      fixedNow
    )
    assertEquals("Dentist appointment", result.cleanTitle)
    assertEquals(fixedToday.plusDays(1), result.dueDate)
    assertEquals(LocalTime.of(15, 30), result.dueTime)
    assertEquals(TaskPriority.HIGH, result.priority)
    assertTrue(result.hasExplicitDate)
    assertTrue(result.hasExplicitTime)
    assertTrue(result.hasExplicitPriority)
  }

  @Test
  fun testWeekdayAndCategoryHashtag() {
    val result = NaturalLanguageTaskParser.parse(
      "Submit economics essay by Monday 10am #College !p1",
      fixedToday,
      fixedNow
    )
    assertEquals("Submit economics essay", result.cleanTitle)
    assertEquals(DayOfWeek.MONDAY, result.dueDate.dayOfWeek)
    assertEquals(LocalTime.of(10, 0), result.dueTime)
    assertEquals("College", result.category)
    assertEquals(TaskPriority.HIGH, result.priority)
    assertTrue(result.tags.contains("College"))
  }

  @Test
  fun testShortTermScratchTask() {
    val result = NaturalLanguageTaskParser.parse(
      "Quick grocery note ~scratch",
      fixedToday,
      fixedNow
    )
    assertEquals("Quick grocery note", result.cleanTitle)
    assertTrue(result.isShortTerm)
  }

  @Test
  fun testTonightBroadTime() {
    val result = NaturalLanguageTaskParser.parse(
      "Watch movie with family tonight",
      fixedToday,
      fixedNow
    )
    assertEquals("Watch movie with family", result.cleanTitle)
    assertEquals(fixedToday, result.dueDate)
    assertEquals(LocalTime.of(20, 0), result.dueTime)
    assertTrue(result.hasExplicitTime)
  }

  @Test
  fun testMonthDayFormat() {
    val result = NaturalLanguageTaskParser.parse(
      "Christmas gift shopping on Dec 24 !medium",
      fixedToday,
      fixedNow
    )
    assertEquals("Christmas gift shopping", result.cleanTitle)
    assertEquals(LocalDate.of(2026, 12, 24), result.dueDate)
    assertEquals(TaskPriority.MEDIUM, result.priority)
    assertTrue(result.hasExplicitDate)
  }

  @Test
  fun testReminderKeyword() {
    val result = NaturalLanguageTaskParser.parse(
      "Call bank tomorrow 9am with reminder",
      fixedToday,
      fixedNow
    )
    assertEquals("Call bank", result.cleanTitle)
    assertEquals(ReminderOption.AT_DUE_TIME, result.reminderOption)
  }

  @Test
  fun testToTaskConversion() {
    val result = NaturalLanguageTaskParser.parse(
      "Prepare presentation tomorrow 2pm #Work !urgent",
      fixedToday,
      fixedNow
    )
    val task = result.toTask()
    assertNotNull(task.id)
    assertEquals("Prepare presentation", task.title)
    assertEquals("2026-11-14", task.dueDate)
    assertEquals("14:00", task.dueTime)
    assertEquals("Work", task.category)
    assertEquals(TaskPriority.HIGH, task.priority)
    assertFalse(task.isCompleted)
  }
}

