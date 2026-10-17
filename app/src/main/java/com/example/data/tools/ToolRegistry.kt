package com.example.data.tools

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

object ToolRegistry {

  fun getToolById(id: String): ToolDefinition? = tools.find { it.id == id }

  val tools: List<ToolDefinition> = listOf(
    // Money Tools
    ToolDefinition(
      id = "loan_emi",
      name = "Loan & EMI Calculator",
      category = ToolCategory.MONEY,
      description = "Monthly installments, interest & total payable",
      emoji = "",
      tags = listOf("emi", "loan", "interest", "finance", "bank")
    ),
    ToolDefinition(
      id = "split_bill",
      name = "Tip & Bill Splitter",
      category = ToolCategory.MONEY,
      description = "Split group checks & calculate tip shares",
      emoji = "",
      tags = listOf("tip", "bill", "split", "food", "dinner")
    ),
    ToolDefinition(
      id = "gst_tax",
      name = "GST & Tax Calculator",
      category = ToolCategory.MONEY,
      description = "Inclusive & exclusive tax breakdown",
      emoji = "",
      tags = listOf("gst", "tax", "vat", "invoice", "price")
    ),
    ToolDefinition(
      id = "margin_markup",
      name = "Margin & Markup",
      category = ToolCategory.MONEY,
      description = "Profit margin & markup percentage analysis",
      emoji = "",
      tags = listOf("margin", "markup", "profit", "pricing", "business")
    ),

    // Student Tools
    ToolDefinition(
      id = "attendance_planner",
      name = "Attendance Planner",
      category = ToolCategory.STUDENT,
      description = "Track required classes & safe bunks",
      emoji = "",
      tags = listOf("attendance", "college", "school", "bunk", "class")
    ),
    ToolDefinition(
      id = "cgpa_converter",
      name = "CGPA to Percentage",
      category = ToolCategory.STUDENT,
      description = "Convert 10 or 4 point CGPA to percentage",
      emoji = "",
      tags = listOf("cgpa", "gpa", "percentage", "grades", "exam")
    ),
    ToolDefinition(
      id = "exam_countdown",
      name = "Exam & Deadline Countdown",
      category = ToolCategory.STUDENT,
      description = "Days & hours countdown to deadlines",
      emoji = "",
      tags = listOf("exam", "deadline", "countdown", "study", "calendar")
    ),

    // Business Tools
    ToolDefinition(
      id = "profit_loss",
      name = "Profit & Loss Calculator",
      category = ToolCategory.BUSINESS,
      description = "Net profit, loss amount & ROI percentage",
      emoji = "",
      tags = listOf("profit", "loss", "roi", "sales", "revenue")
    ),
    ToolDefinition(
      id = "discount_savings",
      name = "Discount & Sale Calculator",
      category = ToolCategory.BUSINESS,
      description = "Discount savings & final checkout price",
      emoji = "",
      tags = listOf("discount", "sale", "shopping", "savings", "offer")
    ),
    ToolDefinition(
      id = "break_even",
      name = "Break-Even Estimator",
      category = ToolCategory.BUSINESS,
      description = "Units needed to cover overhead expenses",
      emoji = "",
      tags = listOf("breakeven", "units", "cost", "startup", "target")
    ),

    // General Utilities
    ToolDefinition(
      id = "date_difference",
      name = "Date & Age Calculator",
      category = ToolCategory.UTILITIES,
      description = "Duration between dates, weeks & exact age",
      emoji = "",
      tags = listOf("date", "age", "days", "calendar", "time")
    ),
    ToolDefinition(
      id = "percentage_change",
      name = "Percentage Calculator",
      category = ToolCategory.UTILITIES,
      description = "Percentage increase, decrease & ratios",
      emoji = "",
      tags = listOf("percentage", "math", "ratio", "change")
    ),
    ToolDefinition(
      id = "unit_converter",
      name = "Quick Unit Converter",
      category = ToolCategory.UTILITIES,
      description = "Convert length, weight & temperature",
      emoji = "",
      tags = listOf("unit", "convert", "length", "weight", "temperature")
    ),

    // Productivity Tools
    ToolDefinition(
      id = "pomodoro_planner",
      name = "Pomodoro Session Planner",
      category = ToolCategory.PRODUCTIVITY,
      description = "Focus blocks & rest period schedule",
      emoji = "",
      tags = listOf("pomodoro", "focus", "timer", "deepwork", "study")
    ),
    ToolDefinition(
      id = "reading_estimator",
      name = "Reading Time Estimator",
      category = ToolCategory.PRODUCTIVITY,
      description = "Estimated reading duration by word count",
      emoji = "",
      tags = listOf("reading", "words", "time", "articles", "notes")
    )
  )

  // -------------------------------------------------------------------------
  // CALCULATION ENGINES
  // -------------------------------------------------------------------------

  fun calculateLoanEmi(principal: Double, annualRate: Double, tenureMonths: Int): ToolResult {
    if (principal <= 0 || tenureMonths <= 0) {
      return ToolResult("Monthly EMI", "₹0.00", listOf("Principal" to "₹0", "Tenure" to "0 mo"))
    }
    val monthlyRate = (annualRate / 12.0) / 100.0
    val emi = if (monthlyRate > 0) {
      (principal * monthlyRate * (1 + monthlyRate).pow(tenureMonths)) / ((1 + monthlyRate).pow(tenureMonths) - 1)
    } else {
      principal / tenureMonths
    }
    val totalPayment = emi * tenureMonths
    val totalInterest = totalPayment - principal

    return ToolResult(
      primaryLabel = "Monthly EMI",
      primaryValue = String.format(Locale.getDefault(), "₹%,.2f", emi),
      secondaryDetails = listOf(
        "Total Interest" to String.format(Locale.getDefault(), "₹%,.2f", totalInterest),
        "Total Amount Payable" to String.format(Locale.getDefault(), "₹%,.2f", totalPayment),
        "Principal Borrowed" to String.format(Locale.getDefault(), "₹%,.2f", principal),
        "Tenure" to "$tenureMonths months (${String.format(Locale.getDefault(), "%.1f", tenureMonths / 12.0)} yrs)"
      ),
      summaryNotes = "Interest accounts for ${String.format(Locale.getDefault(), "%.1f", (totalInterest / totalPayment) * 100)}% of total payment."
    )
  }

  fun calculateTipSplit(totalBill: Double, tipPercent: Double, numPeople: Int): ToolResult {
    val validPeople = if (numPeople <= 0) 1 else numPeople
    val tipAmount = totalBill * (tipPercent / 100.0)
    val grandTotal = totalBill + tipAmount
    val perPerson = grandTotal / validPeople

    return ToolResult(
      primaryLabel = "Each Person Pays",
      primaryValue = String.format(Locale.getDefault(), "₹%,.2f", perPerson),
      secondaryDetails = listOf(
        "Total Bill" to String.format(Locale.getDefault(), "₹%,.2f", totalBill),
        "Tip (${tipPercent.toInt()}%)" to String.format(Locale.getDefault(), "₹%,.2f", tipAmount),
        "Grand Total" to String.format(Locale.getDefault(), "₹%,.2f", grandTotal),
        "Split Among" to "$validPeople people"
      )
    )
  }

  fun calculateGst(amount: Double, gstRate: Double, isPriceInclusive: Boolean): ToolResult {
    val netAmount: Double
    val gstAmount: Double
    val grossAmount: Double

    if (isPriceInclusive) {
      grossAmount = amount
      netAmount = amount / (1.0 + (gstRate / 100.0))
      gstAmount = grossAmount - netAmount
    } else {
      netAmount = amount
      gstAmount = amount * (gstRate / 100.0)
      grossAmount = netAmount + gstAmount
    }

    return ToolResult(
      primaryLabel = if (isPriceInclusive) "Net Base Price" else "Total Price (with GST)",
      primaryValue = String.format(Locale.getDefault(), "₹%,.2f", if (isPriceInclusive) netAmount else grossAmount),
      secondaryDetails = listOf(
        "GST Amount" to String.format(Locale.getDefault(), "₹%,.2f", gstAmount),
        "CGST (half)" to String.format(Locale.getDefault(), "₹%,.2f", gstAmount / 2),
        "SGST (half)" to String.format(Locale.getDefault(), "₹%,.2f", gstAmount / 2),
        "GST Slab" to "${gstRate.toInt()}%",
        "Mode" to if (isPriceInclusive) "Price already includes GST" else "GST added on top"
      )
    )
  }

  fun calculateMarginMarkup(cost: Double, revenue: Double): ToolResult {
    if (cost <= 0 || revenue <= 0) {
      return ToolResult("Gross Profit", "₹0.00", listOf("Margin" to "0%", "Markup" to "0%"))
    }
    val profit = revenue - cost
    val marginPercent = (profit / revenue) * 100.0
    val markupPercent = (profit / cost) * 100.0

    return ToolResult(
      primaryLabel = "Gross Profit",
      primaryValue = String.format(Locale.getDefault(), "₹%,.2f", profit),
      secondaryDetails = listOf(
        "Profit Margin" to String.format(Locale.getDefault(), "%.2f%%", marginPercent),
        "Markup on Cost" to String.format(Locale.getDefault(), "%.2f%%", markupPercent),
        "Cost Price" to String.format(Locale.getDefault(), "₹%,.2f", cost),
        "Selling Revenue" to String.format(Locale.getDefault(), "₹%,.2f", revenue)
      ),
      summaryNotes = if (profit >= 0) "Healthy gross profit margin." else "Operating at a gross loss!"
    )
  }

  fun calculateAttendance(attended: Int, total: Int, targetPercent: Double): ToolResult {
    if (total <= 0) {
      return ToolResult("Current Attendance", "0%", listOf("Status" to "No classes recorded"))
    }
    val currentPercent = (attended.toDouble() / total.toDouble()) * 100.0
    val diff = targetPercent - currentPercent

    val (status, detail) = if (currentPercent >= targetPercent) {
      // How many classes can be safely skipped
      // attended / (total + x) >= targetPercent / 100
      // attended * 100 / targetPercent - total = x
      val skippable = ((attended * 100.0) / targetPercent).toInt() - total
      val safeBunks = maxOf(0, skippable)
      "Safe to bunk" to if (safeBunks > 0) "You can miss $safeBunks class(es) without falling below ${targetPercent.toInt()}%." else "You are exactly on target! Don't miss next class."
    } else {
      // How many more classes must be attended consecutively
      // (attended + y) / (total + y) = targetPercent / 100
      // 100 * (attended + y) = target * (total + y)
      // 100*attended + 100*y = target*total + target*y
      // y * (100 - target) = target*total - 100*attended
      val numerator = (targetPercent * total) - (100.0 * attended)
      val denominator = 100.0 - targetPercent
      val needed = if (denominator > 0) kotlin.math.ceil(numerator / denominator).toInt() else 0
      val safeNeeded = maxOf(1, needed)
      "Shortage Alert!" to "Attend next $safeNeeded consecutive class(es) to reach ${targetPercent.toInt()}%."
    }

    return ToolResult(
      primaryLabel = "Current Attendance",
      primaryValue = String.format(Locale.getDefault(), "%.1f%%", currentPercent),
      secondaryDetails = listOf(
        "Classes Attended" to "$attended / $total",
        "Target Requirement" to "${targetPercent.toInt()}%",
        "Action Plan" to detail
      ),
      summaryNotes = status
    )
  }

  fun calculateCgpa(cgpa: Double, scale: Double): ToolResult {
    val validCgpa = cgpa.coerceIn(0.0, scale)
    val percentage = if (scale == 10.0) {
      // Standard CBSE / AICTE formula: CGPA * 9.5
      validCgpa * 9.5
    } else {
      // 4.0 scale conversion: (CGPA / 4.0) * 100
      (validCgpa / scale) * 100.0
    }
    val division = when {
      percentage >= 75.0 -> "First Class with Distinction"
      percentage >= 60.0 -> "First Division"
      percentage >= 50.0 -> "Second Division"
      percentage >= 40.0 -> "Pass Division"
      else -> "Needs Improvement"
    }

    return ToolResult(
      primaryLabel = "Equivalent Percentage",
      primaryValue = String.format(Locale.getDefault(), "%.2f%%", percentage),
      secondaryDetails = listOf(
        "Academic Classification" to division,
        "Formula Applied" to if (scale == 10.0) "AICTE / CBSE: CGPA × 9.5" else "(CGPA / 4.0) × 100",
        "Entered CGPA" to "$validCgpa / $scale"
      )
    )
  }

  fun calculateExamCountdown(targetDate: LocalDate): ToolResult {
    val today = LocalDate.now()
    val daysLeft = ChronoUnit.DAYS.between(today, targetDate)
    val weeks = daysLeft / 7
    val remDays = daysLeft % 7

    return ToolResult(
      primaryLabel = if (daysLeft >= 0) "$daysLeft Days Left" else "Event Passed",
      primaryValue = if (daysLeft >= 0) "$daysLeft Days" else "${-daysLeft} days ago",
      secondaryDetails = listOf(
        "Weeks & Days" to "$weeks weeks, $remDays days",
        "Hours Left" to "${daysLeft * 24} hours approx",
        "Target Date" to targetDate.toString(),
        "Today's Date" to today.toString()
      ),
      summaryNotes = if (daysLeft in 0..7) "Final countdown week. Review key notes." else "Maintain steady daily prep."
    )
  }

  fun calculateProfitLoss(costPrice: Double, sellingPrice: Double, quantity: Int): ToolResult {
    val qty = maxOf(1, quantity)
    val totalCost = costPrice * qty
    val totalRev = sellingPrice * qty
    val netDifference = totalRev - totalCost
    val isProfit = netDifference >= 0
    val roiPercent = if (totalCost > 0) (netDifference / totalCost) * 100.0 else 0.0

    return ToolResult(
      primaryLabel = if (isProfit) "Net Profit" else "Net Loss",
      primaryValue = String.format(Locale.getDefault(), "₹%,.2f", kotlin.math.abs(netDifference)),
      secondaryDetails = listOf(
        "Return on Investment" to String.format(Locale.getDefault(), "%.2f%%", roiPercent),
        "Total Revenue" to String.format(Locale.getDefault(), "₹%,.2f", totalRev),
        "Total Cost Basis" to String.format(Locale.getDefault(), "₹%,.2f", totalCost),
        "Per-Unit Margin" to String.format(Locale.getDefault(), "₹%,.2f", sellingPrice - costPrice)
      ),
      summaryNotes = if (isProfit) "Profitable deal." else "Deal is incurring a loss."
    )
  }

  fun calculateDiscount(originalPrice: Double, discountPercent: Double): ToolResult {
    val discount = originalPrice * (discountPercent.coerceIn(0.0, 100.0) / 100.0)
    val finalPrice = originalPrice - discount

    return ToolResult(
      primaryLabel = "Final Discounted Price",
      primaryValue = String.format(Locale.getDefault(), "₹%,.2f", finalPrice),
      secondaryDetails = listOf(
        "You Save" to String.format(Locale.getDefault(), "₹%,.2f", discount),
        "Original Price" to String.format(Locale.getDefault(), "₹%,.2f", originalPrice),
        "Discount Applied" to "${discountPercent.toInt()}% OFF"
      )
    )
  }

  fun calculateBreakEven(fixedCosts: Double, pricePerUnit: Double, variableCostPerUnit: Double): ToolResult {
    val contributionMargin = pricePerUnit - variableCostPerUnit
    if (contributionMargin <= 0) {
      return ToolResult("Break-Even Units", "Infinite", listOf("Error" to "Price must exceed variable cost per unit"))
    }
    val units = (fixedCosts / contributionMargin).roundToInt()
    val revenueNeeded = units * pricePerUnit

    return ToolResult(
      primaryLabel = "Break-Even Units",
      primaryValue = "$units units",
      secondaryDetails = listOf(
        "Required Sales Revenue" to String.format(Locale.getDefault(), "₹%,.2f", revenueNeeded),
        "Unit Contribution Margin" to String.format(Locale.getDefault(), "₹%,.2f", contributionMargin),
        "Fixed Overhead" to String.format(Locale.getDefault(), "₹%,.2f", fixedCosts)
      ),
      summaryNotes = "Sell at least $units units to start generating profit."
    )
  }

  fun calculateDateDifference(start: LocalDate, end: LocalDate): ToolResult {
    val days = ChronoUnit.DAYS.between(start, end)
    val months = ChronoUnit.MONTHS.between(start, end)
    val years = ChronoUnit.YEARS.between(start, end)

    return ToolResult(
      primaryLabel = "Total Days Difference",
      primaryValue = "$days days",
      secondaryDetails = listOf(
        "In Weeks" to "${days / 7} weeks, ${days % 7} days",
        "In Months" to "$months months",
        "In Years" to "$years years",
        "From Date" to start.toString(),
        "To Date" to end.toString()
      )
    )
  }

  fun calculatePomodoro(sessions: Int, focusMin: Int, breakMin: Int): ToolResult {
    val validSessions = maxOf(1, sessions)
    val totalFocus = validSessions * focusMin
    val totalBreak = (validSessions - 1) * breakMin
    val totalTime = totalFocus + totalBreak

    return ToolResult(
      primaryLabel = "Total Study/Work Time",
      primaryValue = "${totalTime / 60}h ${totalTime % 60}m",
      secondaryDetails = listOf(
        "Pure Deep Work" to "${totalFocus / 60}h ${totalFocus % 60}m ($validSessions blocks)",
        "Scheduled Rest" to "$totalBreak minutes",
        "Session Structure" to "${focusMin}m focus / ${breakMin}m break"
      )
    )
  }

  fun calculateReadingTime(words: Int, wpm: Int = 200): ToolResult {
    val validWpm = maxOf(50, wpm)
    val minutes = kotlin.math.ceil(words.toDouble() / validWpm).toInt()

    return ToolResult(
      primaryLabel = "Est. Reading Time",
      primaryValue = if (minutes >= 60) "${minutes / 60} hr ${minutes % 60} min" else "$minutes minutes",
      secondaryDetails = listOf(
        "Word Count" to "$words words",
        "Reading Speed" to "$validWpm words/min (standard adult)",
        "Audio Equivalent" to "${kotlin.math.ceil(words / 150.0).toInt()} min (150 wpm speech)"
      )
    )
  }

  fun calculatePercentageChange(initialValue: Double, finalValue: Double): ToolResult {
    if (initialValue == 0.0) {
      return ToolResult(
        primaryLabel = "Percentage Change",
        primaryValue = "0.0%",
        secondaryDetails = listOf("Difference" to String.format(Locale.getDefault(), "%.2f", finalValue))
      )
    }
    val diff = finalValue - initialValue
    val percentChange = (diff / kotlin.math.abs(initialValue)) * 100.0
    val isIncrease = diff >= 0
    val ratio = if (initialValue != 0.0) finalValue / initialValue else 0.0

    return ToolResult(
      primaryLabel = if (isIncrease) "Percentage Increase" else "Percentage Decrease",
      primaryValue = String.format(Locale.getDefault(), "%+.2f%%", percentChange),
      secondaryDetails = listOf(
        "Absolute Difference" to String.format(Locale.getDefault(), "%+.2f", diff),
        "Initial Value" to String.format(Locale.getDefault(), "%.2f", initialValue),
        "Final Value" to String.format(Locale.getDefault(), "%.2f", finalValue),
        "Growth Factor" to String.format(Locale.getDefault(), "%.3fx", ratio)
      ),
      summaryNotes = if (isIncrease) "Value grew by ${String.format(Locale.getDefault(), "%.1f", percentChange)}%" else "Value dropped by ${String.format(Locale.getDefault(), "%.1f", -percentChange)}%"
    )
  }

  fun calculateUnitConverter(value: Double, categoryIndex: Int = 0): ToolResult {
    return when (categoryIndex) {
      0 -> { // Length
        val feet = value * 3.28084
        val inches = value * 39.3701
        val miles = (value / 1000.0) * 0.621371
        val km = value / 1000.0
        ToolResult(
          primaryLabel = "Meters to Feet",
          primaryValue = String.format(Locale.getDefault(), "%.2f ft", feet),
          secondaryDetails = listOf(
            "Inches" to String.format(Locale.getDefault(), "%.2f in", inches),
            "Kilometers" to String.format(Locale.getDefault(), "%.3f km", km),
            "Miles" to String.format(Locale.getDefault(), "%.3f mi", miles),
            "Input Meters" to String.format(Locale.getDefault(), "%.2f m", value)
          )
        )
      }
      1 -> { // Weight
        val lbs = value * 2.20462
        val oz = value * 35.274
        val grams = value * 1000.0
        ToolResult(
          primaryLabel = "Kilograms to Pounds",
          primaryValue = String.format(Locale.getDefault(), "%.2f lbs", lbs),
          secondaryDetails = listOf(
            "Grams" to String.format(Locale.getDefault(), "%.1f g", grams),
            "Ounces" to String.format(Locale.getDefault(), "%.2f oz", oz),
            "Input Kg" to String.format(Locale.getDefault(), "%.2f kg", value)
          )
        )
      }
      else -> { // Temperature
        val fahrenheit = (value * 9.0 / 5.0) + 32.0
        val kelvin = value + 273.15
        ToolResult(
          primaryLabel = "Celsius to Fahrenheit",
          primaryValue = String.format(Locale.getDefault(), "%.1f°F", fahrenheit),
          secondaryDetails = listOf(
            "Kelvin" to String.format(Locale.getDefault(), "%.2f K", kelvin),
            "Input Celsius" to String.format(Locale.getDefault(), "%.1f°C", value)
          )
        )
      }
    }
  }
}
