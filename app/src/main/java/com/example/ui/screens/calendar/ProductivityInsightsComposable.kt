package com.example.ui.screens.calendar

import com.example.ui.theme.SFProFontFamily
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitCompletionRecord
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.ui.components.SketchCard
import com.example.ui.components.bounceClick
import com.example.ui.components.heroEntrance
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProductivityInsightsSection(
  tasks: List<Task>,
  habits: List<Habit>,
  records: List<HabitCompletionRecord>,
  onTaskClick: (Task) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedPeriod by remember { mutableStateOf(StatsTimePeriod.THIS_WEEK) }
  var showCompletedHistory by remember { mutableStateOf(false) }

  val report = remember(tasks, habits, records, selectedPeriod) {
    ProductivityStatsHelper.calculateReport(
      tasks = tasks,
      habits = habits,
      records = records,
      period = selectedPeriod
    )
  }

  Column(modifier = modifier.fillMaxWidth()) {
    // Section Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "PRODUCTIVITY INSIGHTS",
        style = TextStyle(
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = InkSecondary
        )
      )

      Text(
        text = "Real Analytics",
        style = TextStyle(fontSize = 11.sp, color = InkSecondary)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Time Period Filter: [Today] [This Week] [This Month] [All Time]
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      StatsTimePeriod.values().forEach { period ->
        val isSelected = period == selectedPeriod
        val bg = if (isSelected) PillBackgroundDark else Color.Transparent
        val textCol = if (isSelected) PillTextDark else InkPrimary

        Box(
          modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .border(1.2.dp, CardBorder, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .bounceClick { selectedPeriod = period }
            .testTag("period_tab_${period.name}"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = period.displayName,
            style = TextStyle(
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = textCol
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (!report.hasEnoughData) {
      // Empty state (Section 30 requirement: No misleading 0% or fake charts)
      SketchCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Not enough data yet",
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = InkPrimary
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Complete a few tasks and check in on habits to see your productivity trends!",
            style = TextStyle(fontSize = 13.sp, color = InkSecondary, textAlign = TextAlign.Center)
          )
        }
      }
      return
    }

    // 1. PRODUCTIVITY OVERVIEW CARD
    SketchCard(
      modifier = Modifier.fillMaxWidth().heroEntrance(delayMs = 40)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Overall Productivity",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
              )
            )
            Text(
              text = "${selectedPeriod.displayName} Score",
              style = TextStyle(fontSize = 12.sp, color = InkSecondary)
            )
          }

          // Circular Score representation
          Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(
              progress = { (report.overview.overallScore / 100f).coerceIn(0f, 1f) },
              modifier = Modifier.size(54.dp),
              color = AccentTerracotta,
              trackColor = CardBorder.copy(alpha = 0.2f),
              strokeWidth = 5.dp,
              strokeCap = StrokeCap.Round
            )
            Text(
              text = "${report.overview.overallScore.toInt()}%",
              style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Metrics breakdown row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${report.overview.tasksCompleted} / ${report.overview.tasksTotal}",
              style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
            )
            Text(
              text = "Tasks Done",
              style = TextStyle(fontSize = 11.sp, color = InkSecondary)
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${report.overview.habitsCompleted} / ${report.overview.habitsTotal}",
              style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
            )
            Text(
              text = "Habit Check-ins",
              style = TextStyle(fontSize = 11.sp, color = InkSecondary)
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${report.habitStats.currentStreak}d",
              style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
            )
            Text(
              text = "Current Streak",
              style = TextStyle(fontSize = 11.sp, color = InkSecondary)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. TASK ANALYTICS CARD
    SketchCard(
      modifier = Modifier.fillMaxWidth().staggeredEntrance(index = 1)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Task Analytics",
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = InkPrimary
            )
          )

          if (report.taskStats.tasksOverdue > 0) {
            Box(
              modifier = Modifier
                .background(AccentRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .border(1.dp, AccentRed.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${report.taskStats.tasksOverdue} overdue",
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentRed)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Daily completion sketch bar chart
        Text(
          text = "COMPLETION BY DAY",
          style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        val maxDaily = (report.taskStats.completionByDay.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          report.taskStats.completionByDay.forEach { (dayLabel, count) ->
            val fraction = (count.toFloat() / maxDaily.toFloat()).coerceIn(0.08f, 1f)
            val barHeight = (60 * fraction).dp

            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Bottom
            ) {
              if (count > 0) {
                Text(
                  text = count.toString(),
                  style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                )
                Spacer(modifier = Modifier.height(2.dp))
              }
              Box(
                modifier = Modifier
                  .width(22.dp)
                  .height(barHeight)
                  .border(1.2.dp, CardBorder, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                  .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                  .background(if (count > 0) PillBackgroundDark else CardBorder.copy(alpha = 0.2f))
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = dayLabel,
                style = TextStyle(fontSize = 10.sp, color = InkSecondary)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = CardBorder.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Category breakdown
        Text(
          text = "COMPLETION BY CATEGORY",
          style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (report.taskStats.completionByCategory.isEmpty()) {
          Text(text = "No categorized tasks in this period", style = TextStyle(fontSize = 12.sp, color = InkMuted))
        } else {
          report.taskStats.completionByCategory.forEach { (category, counts) ->
            val (done, total) = counts
            val progress = if (total > 0) done.toFloat() / total.toFloat() else 0f
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = category, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = InkPrimary))
                Text(text = "$done / $total", style = TextStyle(fontSize = 12.sp, color = InkSecondary))
              }
              Spacer(modifier = Modifier.height(4.dp))
              LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = PillBackgroundDark,
                trackColor = CardBorder.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. HABIT ANALYTICS CARD
    SketchCard(
      modifier = Modifier.fillMaxWidth().staggeredEntrance(index = 2)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Text(
          text = "Habit Consistency",
          style = TextStyle(
            fontFamily = SFProFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${report.habitStats.weeklyConsistencyPct.toInt()}%",
              style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
            )
            Text(text = "7-Day Consistency", style = TextStyle(fontSize = 11.sp, color = InkSecondary))
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${report.habitStats.monthlyConsistencyPct.toInt()}%",
              style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
            )
            Text(text = "30-Day Consistency", style = TextStyle(fontSize = 11.sp, color = InkSecondary))
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${report.habitStats.longestStreak}d",
              style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
            )
            Text(text = "Longest Streak", style = TextStyle(fontSize = 11.sp, color = InkSecondary))
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = CardBorder.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Total completions: ${report.habitStats.totalCompletions}",
            style = TextStyle(fontSize = 12.sp, color = InkPrimary)
          )
          Text(
            text = "Missed scheduled days: ${report.habitStats.missedDays}",
            style = TextStyle(
              fontSize = 12.sp,
              color = if (report.habitStats.missedDays > 0) AccentRed else InkSecondary
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 4. HISTORICAL TASK ACTIVITY (Section 17)
    SketchCard(
      modifier = Modifier.fillMaxWidth().staggeredEntrance(index = 3)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .bounceClick { showCompletedHistory = !showCompletedHistory },
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Completed Task History",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
              )
            )
            Text(
              text = "${report.completedTasksHistory.size} tasks completed all-time",
              style = TextStyle(fontSize = 12.sp, color = InkSecondary)
            )
          }

          Icon(
            imageVector = if (showCompletedHistory) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = "Toggle History",
            tint = InkPrimary
          )
        }

        AnimatedVisibility(visible = showCompletedHistory) {
          Column(modifier = Modifier.padding(top = 12.dp)) {
            if (report.completedTasksHistory.isEmpty()) {
              Text(
                text = "No completed tasks yet",
                style = TextStyle(fontSize = 13.sp, color = InkMuted)
              )
            } else {
              report.completedTasksHistory.take(15).forEach { task ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .bounceClick { onTaskClick(task) },
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PillBackgroundDark,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = task.title,
                      style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                    )
                    Text(
                      text = "Due: ${task.dueDate} ${task.dueTime} • Category: ${task.category}",
                      style = TextStyle(fontSize = 11.sp, color = InkSecondary)
                    )
                  }
                }
                HorizontalDivider(color = CardBorder.copy(alpha = 0.1f), thickness = 0.8.dp)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun HabitHistoryGrid(
  habit: Habit,
  records: List<HabitCompletionRecord>,
  modifier: Modifier = Modifier
) {
  val historyDays = remember(habit, records) {
    ProductivityStatsHelper.getHabitHistoryDays(habit, records, daysCount = 28)
  }

  val completedCount = historyDays.count { it.status == HabitDayStatus.COMPLETED }
  val missedCount = historyDays.count { it.status == HabitDayStatus.MISSED }

  SketchCard(
    modifier = modifier.fillMaxWidth(),
    cornerRadius = 14.dp,
    borderWidth = 1.5.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "28-DAY HABIT HISTORY",
          style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = InkSecondary
          )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "$completedCount done", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
          Text(text = "• $missedCount missed", style = TextStyle(fontSize = 11.sp, color = if (missedCount > 0) AccentRed else InkSecondary))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4 rows of 7 days
      val rows = 4
      for (r in 0 until rows) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          for (c in 0 until 7) {
            val index = r * 7 + c
            if (index < historyDays.size) {
              val item = historyDays[index]
              val (bg, borderCol, iconTint) = when (item.status) {
                HabitDayStatus.COMPLETED -> Triple(PillBackgroundDark, CardBorder, PillTextDark)
                HabitDayStatus.MISSED -> Triple(AccentRed.copy(alpha = 0.15f), AccentRed, AccentRed)
                HabitDayStatus.NOT_SCHEDULED -> Triple(Color.Transparent, CardBorder.copy(alpha = 0.25f), InkMuted)
                HabitDayStatus.FUTURE -> Triple(Color.Transparent, Color.Transparent, Color.Transparent)
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .border(1.dp, borderCol, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg),
                  contentAlignment = Alignment.Center
                ) {
                  when (item.status) {
                    HabitDayStatus.COMPLETED -> Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Completed",
                      tint = iconTint,
                      modifier = Modifier.size(16.dp)
                    )
                    HabitDayStatus.MISSED -> Text(
                      text = "•",
                      style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = iconTint)
                    )
                    HabitDayStatus.NOT_SCHEDULED -> Text(
                      text = "-",
                      style = TextStyle(fontSize = 14.sp, color = iconTint)
                    )
                    HabitDayStatus.FUTURE -> Unit
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = item.date.format(DateTimeFormatter.ofPattern("d")),
                  style = TextStyle(fontSize = 9.sp, color = InkSecondary)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Legend
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(modifier = Modifier.size(10.dp).background(PillBackgroundDark, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "Completed", style = TextStyle(fontSize = 10.sp, color = InkSecondary))

        Spacer(modifier = Modifier.width(12.dp))

        Box(modifier = Modifier.size(10.dp).background(AccentRed.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "Missed", style = TextStyle(fontSize = 10.sp, color = InkSecondary))

        Spacer(modifier = Modifier.width(12.dp))

        Box(modifier = Modifier.size(10.dp).border(1.dp, CardBorder.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "Not Scheduled", style = TextStyle(fontSize = 10.sp, color = InkSecondary))
      }
    }
  }
}