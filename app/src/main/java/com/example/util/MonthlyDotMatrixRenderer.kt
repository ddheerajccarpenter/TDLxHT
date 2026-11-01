package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.core.content.FileProvider
import com.example.data.model.HabitCompletionRecord
import com.example.data.model.Task
import com.example.ui.components.SketchCard
import com.example.ui.theme.InnerCornerRadius
import com.example.ui.theme.InnerCornerShape
import com.example.ui.theme.OuterCornerRadius
import com.example.ui.theme.SFProFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object MonthlyDotMatrixRenderer {

    fun computeMonthlyActivity(
        tasks: List<Task>,
        records: List<HabitCompletionRecord>,
        yearMonth: YearMonth
    ): Map<LocalDate, Int> {
        val daysInMonth = yearMonth.lengthOfMonth()
        val activityMap = mutableMapOf<LocalDate, Int>()

        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val tasksDone = tasks.count { it.dueDate == dateStr && it.isCompleted }
            val habitsDone = records.count { it.date == dateStr && it.isCompleted }
            activityMap[date] = tasksDone + habitsDone
        }

        return activityMap
    }

    fun getActivityLevel(completions: Int): Int {
        return when {
            completions <= 0 -> 0
            completions in 1..2 -> 1
            completions in 3..4 -> 2
            else -> 3
        }
    }

    fun generateMonthlyDotMatrixBitmap(
        context: Context,
        yearMonth: YearMonth,
        activityMap: Map<LocalDate, Int>,
        isDark: Boolean = true
    ): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Palette setup
        val bgCol = if (isDark) 0xFF171717.toInt() else 0xFFFAFAF7.toInt()
        val cardBgCol = if (isDark) 0xFF232528.toInt() else 0xFFFFFFFF.toInt()
        val borderCol = if (isDark) 0xFF35383C.toInt() else 0xFFE2E2E8.toInt()
        val textPrimaryCol = if (isDark) 0xFFF5F5F7.toInt() else 0xFF171717.toInt()
        val textSecondaryCol = if (isDark) 0xFF9E9EA4.toInt() else 0xFF66686C.toInt()
        val accentCol = 0xFF5E6E82.toInt()

        val dotCols = intArrayOf(
            if (isDark) 0xFF2A2C30.toInt() else 0xFFEBEBF0.toInt(), // Level 0: empty
            if (isDark) 0x665E6E82.toInt() else 0x555E6E82.toInt(), // Level 1: 1-2
            if (isDark) 0xAA5E6E82.toInt() else 0x995E6E82.toInt(), // Level 2: 3-4
            accentCol                                               // Level 3: 5+
        )

        // 1. Draw Canvas Background
        canvas.drawColor(bgCol)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 2. Main Card Surface (24dp = 72px corner)
        val cardMargin = 54f
        val cardRect = RectF(cardMargin, cardMargin, width - cardMargin, height - cardMargin)
        paint.color = cardBgCol
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(cardRect, 60f, 60f, paint)

        paint.color = borderCol
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(cardRect, 60f, 60f, paint)

        // 3. Header
        paint.style = Paint.Style.FILL
        paint.color = accentCol
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.letterSpacing = 0.12f
        canvas.drawText("TDLXHT PRODUCTIVITY MATRIX", cardMargin + 48f, cardMargin + 80f, paint)

        val monthName = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
        paint.color = textPrimaryCol
        paint.textSize = 56f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.letterSpacing = 0.02f
        canvas.drawText(monthName, cardMargin + 48f, cardMargin + 155f, paint)

        val totalCompletions = activityMap.values.sum()
        val activeDays = activityMap.values.count { it > 0 }
        paint.color = textSecondaryCol
        paint.textSize = 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.letterSpacing = 0.01f
        canvas.drawText("$totalCompletions completions across $activeDays active days", cardMargin + 48f, cardMargin + 205f, paint)

        // Separator line
        paint.color = borderCol
        paint.strokeWidth = 2f
        canvas.drawLine(cardMargin + 48f, cardMargin + 245f, width - cardMargin - 48f, cardMargin + 245f, paint)

        // 4. Calendar Weekday Headers (Mon - Sun)
        val weekdays = arrayOf("M", "T", "W", "T", "F", "S", "S")
        val gridStartX = cardMargin + 64f
        val gridWidth = (width - cardMargin * 2f) - 128f
        val colStep = gridWidth / 7f
        val gridStartY = cardMargin + 310f

        paint.textSize = 28f
        paint.color = textSecondaryCol
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        for (i in weekdays.indices) {
            val cx = gridStartX + (i + 0.5f) * colStep
            canvas.drawText(weekdays[i], cx, gridStartY, paint)
        }

        // 5. 7-Column Dot Matrix Grid
        val firstDay = yearMonth.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value // 1 (Mon) .. 7 (Sun)
        val dayOffset = firstDayOfWeek - 1
        val daysInMonth = yearMonth.lengthOfMonth()
        val rowStep = 92f
        val dotRadius = 26f

        val matrixStartY = gridStartY + 45f

        for (day in 1..daysInMonth) {
            val slotIndex = dayOffset + (day - 1)
            val col = slotIndex % 7
            val row = slotIndex / 7

            val cx = gridStartX + (col + 0.5f) * colStep
            val cy = matrixStartY + (row + 0.5f) * rowStep

            val date = yearMonth.atDay(day)
            val completions = activityMap[date] ?: 0
            val level = getActivityLevel(completions)

            paint.style = Paint.Style.FILL
            paint.color = dotCols[level]
            canvas.drawCircle(cx, cy, dotRadius, paint)

            if (level == 0) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                paint.color = borderCol
                canvas.drawCircle(cx, cy, dotRadius, paint)
                paint.style = Paint.Style.FILL
            }

            // Day number in center of dot
            paint.color = if (level >= 2) 0xFFFFFFFF.toInt() else textSecondaryCol
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val textY = cy - ((paint.descent() + paint.ascent()) / 2f)
            canvas.drawText(day.toString(), cx, textY, paint)
        }

        // 6. Legend (Less -> More)
        val legendY = matrixStartY + 6 * rowStep + 40f
        paint.color = textSecondaryCol
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Less", gridStartX, legendY + 8f, paint)

        var legendX = gridStartX + 90f
        for (lvl in 0..3) {
            paint.style = Paint.Style.FILL
            paint.color = dotCols[lvl]
            canvas.drawCircle(legendX, legendY, 14f, paint)
            if (lvl == 0) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                paint.color = borderCol
                canvas.drawCircle(legendX, legendY, 14f, paint)
                paint.style = Paint.Style.FILL
            }
            legendX += 45f
        }
        paint.color = textSecondaryCol
        canvas.drawText("More", legendX + 10f, legendY + 8f, paint)

        // 7. Summary Bottom Card
        val statCardY = legendY + 50f
        val statCardRect = RectF(gridStartX, statCardY, width - cardMargin - 64f, statCardY + 140f)
        paint.style = Paint.Style.FILL
        paint.color = if (isDark) 0xFF1C1E20.toInt() else 0xFFF5F5FA.toInt()
        canvas.drawRoundRect(statCardRect, 32f, 32f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = borderCol
        paint.strokeWidth = 2f
        canvas.drawRoundRect(statCardRect, 32f, 32f, paint)

        // 3 Summary Columns: Total, Active Days, Daily Average
        paint.style = Paint.Style.FILL
        val dailyAvg = if (daysInMonth > 0) String.format(Locale.US, "%.1f", totalCompletions.toFloat() / daysInMonth) else "0.0"

        val statColWidth = statCardRect.width() / 3f
        val statsLabels = arrayOf("TOTAL", "ACTIVE DAYS", "DAILY AVG")
        val statsValues = arrayOf(totalCompletions.toString(), "$activeDays/$daysInMonth", dailyAvg)

        for (i in 0..2) {
            val statCx = statCardRect.left + (i + 0.5f) * statColWidth
            paint.color = textSecondaryCol
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(statsLabels[i], statCx, statCardY + 45f, paint)

            paint.color = if (i == 0) accentCol else textPrimaryCol
            paint.textSize = 38f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(statsValues[i], statCx, statCardY + 105f, paint)
        }

        return bitmap
    }

    suspend fun saveReportToDownloads(
        context: Context,
        bitmap: Bitmap,
        yearMonth: YearMonth
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = "TDlxHT-Productivity-${yearMonth.format(DateTimeFormatter.ofPattern("MMMM-yyyy"))}.png"
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/TDlxHT")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val uri = resolver.insert(collection, contentValues) ?: error("Failed to create MediaStore entry")

            resolver.openOutputStream(uri)?.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    error("Failed to compress PNG")
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            uri
        }
    }

    fun shareReportImage(
        context: Context,
        bitmap: Bitmap,
        yearMonth: YearMonth
    ) {
        try {
            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportsDir, "TDlxHT-Report-${yearMonth}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "TDlxHT Productivity Report - ${yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Productivity Matrix"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing report: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Compose preview component for the Monthly Dot Matrix.
 * Displays the 7-column calendar matrix directly on screen with 24dp/12dp geometry.
 */
@Composable
fun MonthlyDotMatrixComposable(
    yearMonth: YearMonth,
    activityMap: Map<LocalDate, Int>,
    modifier: Modifier = Modifier
) {
    val isDark = com.example.ui.theme.LocalAppColors.current.isDark
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDay = yearMonth.atDay(1)
    val dayOffset = firstDay.dayOfWeek.value - 1
    val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")

    val dotColors = listOf(
        if (isDark) Color(0xFF2A2C30) else Color(0xFFEBEBF0),
        if (isDark) Color(0x665E6E82) else Color(0x555E6E82),
        if (isDark) Color(0xAA5E6E82) else Color(0x995E6E82),
        Color(0xFF5E6E82)
    )

    SketchCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = OuterCornerRadius
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACTIVITY MATRIX",
                        style = TextStyle(
                            fontFamily = SFProFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF5E6E82)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = TextStyle(
                            fontFamily = SFProFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF5F5F7) else Color(0xFF171717)
                        )
                    )
                }

                val totalCompletions = activityMap.values.sum()
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF5E6E82).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$totalCompletions Done",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E6E82))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weekday Column Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekdays.forEach { day ->
                    Box(
                        modifier = Modifier.size(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = TextStyle(
                                fontFamily = SFProFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF9E9EA4) else Color(0xFF66686C)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 7-Column Calendar Grid
            val totalSlots = dayOffset + daysInMonth
            val totalRows = (totalSlots + 6) / 7

            for (r in 0 until totalRows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (c in 0..6) {
                        val slot = r * 7 + c
                        val dayNum = slot - dayOffset + 1
                        if (dayNum in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayNum)
                            val completions = activityMap[date] ?: 0
                            val lvl = MonthlyDotMatrixRenderer.getActivityLevel(completions)

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(dotColors[lvl])
                                    .then(
                                        if (lvl == 0) Modifier.border(0.75.dp, if (isDark) Color(0xFF35383C) else Color(0xFFD6D6DC), CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    style = TextStyle(
                                        fontFamily = SFProFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lvl >= 2) Color.White else (if (isDark) Color(0xFF9E9EA4) else Color(0xFF66686C))
                                    )
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(34.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Less", style = TextStyle(fontSize = 11.sp, color = if (isDark) Color(0xFF9E9EA4) else Color(0xFF66686C)))
                dotColors.forEachIndexed { idx, col ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(col)
                            .then(
                                if (idx == 0) Modifier.border(0.5.dp, if (isDark) Color(0xFF35383C) else Color(0xFFD6D6DC), CircleShape)
                                else Modifier
                            )
                    )
                }
                Text("More", style = TextStyle(fontSize = 11.sp, color = if (isDark) Color(0xFF9E9EA4) else Color(0xFF66686C)))
            }
        }
    }
}

