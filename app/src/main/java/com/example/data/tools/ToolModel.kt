package com.example.data.tools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CastForEducation
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val displayName: String, val emoji: String = "") {
  ALL("All"),
  MONEY("Money"),
  STUDENT("Student"),
  BUSINESS("Business"),
  UTILITIES("Utilities"),
  PRODUCTIVITY("Productivity")
}

data class ToolDefinition(
  val id: String,
  val name: String,
  val category: ToolCategory,
  val description: String,
  val emoji: String,
  val tags: List<String> = emptyList()
)

data class ToolResult(
  val primaryLabel: String,
  val primaryValue: String,
  val secondaryDetails: List<Pair<String, String>> = emptyList(),
  val summaryNotes: String? = null
)
