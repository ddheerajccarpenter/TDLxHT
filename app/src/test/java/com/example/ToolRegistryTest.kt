package com.example

import com.example.data.tools.ToolCategory
import com.example.data.tools.ToolRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ToolRegistryTest {

  @Test
  fun testRegistryCompleteness() {
    val tools = ToolRegistry.tools
    assertTrue("Registry must have at least 10 mini tools", tools.size >= 10)
    assertTrue("Must have Money tools", tools.any { it.category == ToolCategory.MONEY })
    assertTrue("Must have Student tools", tools.any { it.category == ToolCategory.STUDENT })
    assertTrue("Must have Business tools", tools.any { it.category == ToolCategory.BUSINESS })
    assertTrue("Must have Utility tools", tools.any { it.category == ToolCategory.UTILITIES })
    assertTrue("Must have Productivity tools", tools.any { it.category == ToolCategory.PRODUCTIVITY })
  }

  @Test
  fun testLoanEmiCalculation() {
    // Principal: 100,000, 12% annual rate, 12 months
    val result = ToolRegistry.calculateLoanEmi(100000.0, 12.0, 12)
    assertEquals("Monthly EMI", result.primaryLabel)
    // 100k @ 12% for 1 yr EMI is approx ₹8,884.88
    assertTrue("EMI should be around 8,884", result.primaryValue.contains("8,884"))
    assertTrue("Total interest must be calculated", result.secondaryDetails.any { it.first == "Total Interest" })
  }

  @Test
  fun testTipSplitCalculation() {
    // Bill: 1000, 10% tip, 2 people -> 1100 total, 550 per person
    val result = ToolRegistry.calculateTipSplit(1000.0, 10.0, 2)
    assertEquals("Each Person Pays", result.primaryLabel)
    assertTrue("Per person should be ₹550", result.primaryValue.contains("550"))
  }

  @Test
  fun testGstCalculationExclusiveAndInclusive() {
    // Exclusive: 1000 + 18% = 1180
    val exclusiveResult = ToolRegistry.calculateGst(1000.0, 18.0, isPriceInclusive = false)
    assertEquals("Total Price (with GST)", exclusiveResult.primaryLabel)
    assertTrue("Gross price should be 1,180", exclusiveResult.primaryValue.contains("1,180"))
    assertTrue("GST amount should be 180", exclusiveResult.secondaryDetails.any { it.second.contains("180") })

    // Inclusive: 1180 inclusive of 18% -> net base 1000
    val inclusiveResult = ToolRegistry.calculateGst(1180.0, 18.0, isPriceInclusive = true)
    assertEquals("Net Base Price", inclusiveResult.primaryLabel)
    assertTrue("Base price should be 1,000", inclusiveResult.primaryValue.contains("1,000"))
  }

  @Test
  fun testMarginAndMarkupCalculation() {
    // Cost: 100, Selling Revenue: 150 -> Profit: 50, Markup: 50%, Margin: 33.33%
    val result = ToolRegistry.calculateMarginMarkup(100.0, 150.0)
    assertEquals("Gross Profit", result.primaryLabel)
    assertTrue("Profit should be 50", result.primaryValue.contains("50"))
    assertTrue("Margin should be around 33.33%", result.secondaryDetails.any { it.first == "Profit Margin" && it.second.contains("33.33%") })
    assertTrue("Markup should be 50.00%", result.secondaryDetails.any { it.first == "Markup on Cost" && it.second.contains("50.00%") })
  }

  @Test
  fun testAttendancePlanner() {
    // Attended: 42, Total: 50 (84%) with Target: 75% -> Safe to bunk
    val safeResult = ToolRegistry.calculateAttendance(42, 50, 75.0)
    assertTrue("Attendance is 84.0%", safeResult.primaryValue.contains("84.0%"))
    assertEquals("Safe to bunk", safeResult.summaryNotes)

    // Attended: 30, Total: 50 (60%) with Target: 75% -> Shortage Alert!
    val shortageResult = ToolRegistry.calculateAttendance(30, 50, 75.0)
    assertTrue("Attendance is 60.0%", shortageResult.primaryValue.contains("60.0%"))
    assertEquals("Shortage Alert!", shortageResult.summaryNotes)
  }

  @Test
  fun testCgpaConversion() {
    // 10.0 scale: 8.4 * 9.5 = 79.8% -> Distinction
    val result10 = ToolRegistry.calculateCgpa(8.4, 10.0)
    assertTrue("Percentage is 79.80%", result10.primaryValue.contains("79.80%"))
    assertTrue("First Class with Distinction", result10.secondaryDetails.any { it.second.contains("Distinction") })

    // 4.0 scale: 3.5 / 4.0 = 87.5%
    val result4 = ToolRegistry.calculateCgpa(3.5, 4.0)
    assertTrue("Percentage is 87.50%", result4.primaryValue.contains("87.50%"))
  }

  @Test
  fun testProfitLossAndBreakEven() {
    // Profit & Loss: Cost 35, Selling 50, Qty 1000 -> 15k profit
    val plResult = ToolRegistry.calculateProfitLoss(35.0, 50.0, 1000)
    assertEquals("Net Profit", plResult.primaryLabel)
    assertTrue("Profit is 15,000", plResult.primaryValue.contains("15,000"))

    // Break-even: Fixed 100,000, Price 500, VarCost 300 -> Contribution 200 -> 500 units
    val beResult = ToolRegistry.calculateBreakEven(100000.0, 500.0, 300.0)
    assertEquals("Break-Even Units", beResult.primaryLabel)
    assertTrue("Units should be 500", beResult.primaryValue.contains("500 units"))
  }

  @Test
  fun testDiscountAndDateDifference() {
    // Discount: 1200 with 25% discount -> Pay 900, Save 300
    val disc = ToolRegistry.calculateDiscount(1200.0, 25.0)
    assertTrue("Final price is 900", disc.primaryValue.contains("900"))
    assertTrue("Savings is 300", disc.secondaryDetails.any { it.first == "You Save" && it.second.contains("300") })

    // Date Difference
    val start = LocalDate.of(2026, 1, 1)
    val end = LocalDate.of(2026, 1, 15)
    val diff = ToolRegistry.calculateDateDifference(start, end)
    assertTrue("14 days difference", diff.primaryValue.contains("14 days"))
  }

  @Test
  fun testReadingTimeAndPomodoro() {
    // Reading 600 words @ 200 wpm -> 3.0 mins
    val reading = ToolRegistry.calculateReadingTime(600, 200)
    assertTrue("Reading time is 3.0 mins", reading.primaryValue.contains("3 minutes"))

    // Pomodoro 4 cycles of 25m focus + 5m break -> 100m focus, 15m break
    val pomo = ToolRegistry.calculatePomodoro(4, 25, 5)
    assertTrue("Session structure is preserved", pomo.secondaryDetails.any { it.second.contains("25m focus") })
  }

  @Test
  fun testGetToolById() {
    val loanTool = ToolRegistry.getToolById("loan_emi")
    assertNotNull(loanTool)
    assertEquals("loan_emi", loanTool?.id)

    val gstTool = ToolRegistry.getToolById("gst_tax")
    assertNotNull(gstTool)
    assertEquals("gst_tax", gstTool?.id)

    val nonExistent = ToolRegistry.getToolById("non_existent_id")
    assertNull(nonExistent)
  }
}
