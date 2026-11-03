package com.example.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitCompletionRecord
import com.example.ui.components.SketchCard
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import java.time.format.DateTimeFormatter

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
