package com.example.data.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Service for acquiring Napkin productivity visual graphs.
 * Direct call to Napkin AI visual endpoint using configured NAPKIN_API_KEY.
 * Falls back to Supabase Edge Function or high-resolution native Canvas chart
 * using real database metrics.
 */
class NapkinVisualService(
    private val napkinService: NapkinService = NapkinService(),
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "NapkinVisualService"
    }

    suspend fun generateVisualGraph(
        contentPrompt: String,
        period: String = "weekly",
        stats: NapkinStats? = null,
        format: String = "png"
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.NAPKIN_API_KEY
        val cleanPrompt = formatProfessionalPrompt(contentPrompt, period, stats)

        // 1. Direct Napkin AI API call with clean, professional, non-slop technical formatting
        if (apiKey.isNotBlank() && !apiKey.contains("placeholder", ignoreCase = true)) {
            try {
                val directBitmap = generateFromNapkinApi(cleanPrompt, apiKey)
                if (directBitmap != null) {
                    return@withContext Result.success(directBitmap)
                }
            } catch (e: Exception) {
                com.example.util.SafeLog.w(TAG, "Direct Napkin API call encountered error, trying fallback", e)
            }
        }

        // 2. Attempt remote generation via Supabase Edge Function if stats are provided
        if (stats != null) {
            try {
                val remoteResult = napkinService.generateVisual(
                    period = period,
                    stats = stats,
                    forceRegenerate = false
                )

                if (remoteResult.isSuccess) {
                    val visual = remoteResult.getOrNull()
                    val imageUrl = visual?.imageUrl
                    if (!imageUrl.isNullOrBlank()) {
                        val downloadRequest = Request.Builder().url(imageUrl).get().build()
                        val bitmap = httpClient.newCall(downloadRequest).execute().use { response ->
                            if (response.isSuccessful) {
                                val bytes = response.body?.bytes()
                                if (bytes != null && bytes.isNotEmpty()) {
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } else null
                            } else null
                        }
                        if (bitmap != null) {
                            return@withContext Result.success(bitmap)
                        }
                    }
                }
            } catch (e: Exception) {
                com.example.util.SafeLog.w(TAG, "Edge function generation failed", e)
            }
        }

        // 3. Fallback: Generate a crisp native Android Canvas chart from real metrics
        try {
            val localBitmap = renderLocalMetricsChart(contentPrompt, period, stats)
            Result.success(localBitmap)
        } catch (err: Exception) {
            com.example.util.SafeLog.e(TAG, "Fatal chart render failure", err)
            Result.failure(IOException("Failed to render visual chart"))
        }
    }

    private suspend fun generateFromNapkinApi(
        cleanPrompt: String,
        apiKey: String
    ): Bitmap? {
        val jsonPayload = JSONObject().apply {
            put("format", "png")
            put("content", cleanPrompt)
            put("color_mode", "dark")
            put("transparent_background", false)
        }

        val request = Request.Builder()
            .url("https://api.napkin.ai/v1/visual")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val submitJson = httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string()
                com.example.util.SafeLog.w(TAG, "Napkin API submission returned ${response.code}: $errBody")
                return null
            }
            val bodyStr = response.body?.string() ?: return null
            try { JSONObject(bodyStr) } catch (_: Exception) { return null }
        }

        val requestId = submitJson.optString("id")
        if (requestId.isBlank()) {
            return null
        }

        // Poll status every 1.5s, up to 12 attempts (~18s max)
        for (attempt in 1..12) {
            delay(1500)
            val statusRequest = Request.Builder()
                .url("https://api.napkin.ai/v1/visual/$requestId/status")
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build()

            val statusJson = httpClient.newCall(statusRequest).execute().use { statusResponse ->
                if (!statusResponse.isSuccessful) return@use null
                val bodyStr = statusResponse.body?.string() ?: return@use null
                try { JSONObject(bodyStr) } catch (_: Exception) { null }
            } ?: continue

            val status = statusJson.optString("status")

            if (status == "completed") {
                val files = statusJson.optJSONArray("generated_files")
                val fileUrl = files?.optJSONObject(0)?.optString("url")
                if (!fileUrl.isNullOrBlank()) {
                    val downloadReq = Request.Builder()
                        .url(fileUrl)
                        .header("Authorization", "Bearer $apiKey")
                        .get()
                        .build()

                    val bitmap = httpClient.newCall(downloadReq).execute().use { downloadRes ->
                        if (downloadRes.isSuccessful) {
                            val bytes = downloadRes.body?.bytes()
                            if (bytes != null && bytes.isNotEmpty()) {
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } else null
                        } else null
                    }
                    if (bitmap != null) {
                        return bitmap
                    }
                }
                return null
            } else if (status == "failed") {
                val error = statusJson.optString("error")
                com.example.util.SafeLog.w(TAG, "Napkin generation failed: $error")
                return null
            }
        }
        return null
    }

    private fun formatProfessionalPrompt(contentPrompt: String, period: String, stats: NapkinStats?): String {
        if (stats != null) {
            val periodText = if (period.equals("monthly", ignoreCase = true)) "Monthly" else "Weekly"
            val pct = (stats.completionRate * 100).toInt()
            return buildString {
                appendLine("$periodText Productivity Performance Overview")
                appendLine("- Tasks Executed: ${stats.tasksCompleted} of ${stats.tasksTotal} completed ($pct% execution rate)")
                appendLine("- Habit Momentum: ${stats.habitsCompleted} sessions completed across ${stats.habitsTotal} tracked routines")
                appendLine("- Best Consecutive Streak: ${stats.longestStreak} days active")
                if (stats.topCategories.isNotEmpty()) {
                    appendLine("- Core Focus Areas: ${stats.topCategories.joinToString(", ")}")
                }
                appendLine("- Timeline: ${stats.periodLabel}")
            }.trim()
        }
        return contentPrompt.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("You are", ignoreCase = true) && !it.startsWith("Draw", ignoreCase = true) }
            .joinToString("\n")
    }

    /**
     * Renders a high-res (800x480) dark-mode tactile productivity dashboard
     * directly to a Bitmap from real database metrics.
     */
    private fun renderLocalMetricsChart(
        contentPrompt: String,
        period: String,
        stats: NapkinStats?
    ): Bitmap {
        val width = 800
        val height = 480
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.parseColor("#252525"))

        // Card border & background
        val cardPaint = Paint().apply {
            color = Color.parseColor("#252525")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val cardRect = RectF(20f, 20f, width - 20f, height - 20f)
        canvas.drawRoundRect(cardRect, 24f, 24f, cardPaint)
        canvas.drawRoundRect(cardRect, 24f, 24f, borderPaint)

        // Title
        val titlePaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val periodText = if (period.equals("monthly", ignoreCase = true)) "Monthly" else "Weekly"
        canvas.drawText("TDlxHT • $periodText Productivity Overview", 50f, 75f, titlePaint)

        // Subtitle
        val subPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            textSize = 16f
            isAntiAlias = true
        }
        val label = stats?.periodLabel ?: "Real Activity Insights"
        canvas.drawText(label, 50f, 105f, subPaint)

        // Divider
        val divPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            strokeWidth = 2f
        }
        canvas.drawLine(50f, 125f, width - 50f, 125f, divPaint)

        // Metrics columns
        val taskTotal = stats?.tasksTotal ?: 1
        val taskDone = stats?.tasksCompleted ?: 0
        val habitTotal = stats?.habitsTotal ?: 0
        val habitDone = stats?.habitsCompleted ?: 0
        val streak = stats?.longestStreak ?: 0
        val compRate = stats?.completionRate ?: if (taskTotal > 0) taskDone.toDouble() / taskTotal else 0.0

        // Column 1: Task Progress Card
        drawMetricBox(
            canvas,
            left = 50f,
            top = 150f,
            boxWidth = 210f,
            boxHeight = 160f,
            title = "TASKS DONE",
            value = "$taskDone / $taskTotal",
            accentColor = Color.parseColor("#FFFFFF"),
            sub = "${(compRate * 100).toInt()}% completed"
        )

        // Column 2: Habit Streaks
        drawMetricBox(
            canvas,
            left = 285f,
            top = 150f,
            boxWidth = 210f,
            boxHeight = 160f,
            title = "LONGEST STREAK",
            value = "$streak Days",
            accentColor = Color.parseColor("#FFFFFF"),
            sub = "Consistency record"
        )

        // Column 3: Habits Completed
        drawMetricBox(
            canvas,
            left = 520f,
            top = 150f,
            boxWidth = 230f,
            boxHeight = 160f,
            title = "HABIT SESSIONS",
            value = "$habitDone completions",
            accentColor = Color.parseColor("#FFFFFF"),
            sub = "Active momentum"
        )

        // Bottom Progress Bar
        val barBgPaint = Paint().apply {
            color = Color.parseColor("#252525")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val barFillPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val barRect = RectF(50f, 350f, width - 50f, 370f)
        canvas.drawRoundRect(barRect, 10f, 10f, barBgPaint)

        val fillWidth = (width - 100f) * compRate.toFloat().coerceIn(0f, 1f)
        if (fillWidth > 10f) {
            val fillRect = RectF(50f, 350f, 50f + fillWidth, 370f)
            canvas.drawRoundRect(fillRect, 10f, 10f, barFillPaint)
        }

        // Footer note
        val footerPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText("Generated from local Room database • Offline-first • Secure", 50f, 420f, footerPaint)

        return bitmap
    }

    private fun drawMetricBox(
        canvas: Canvas,
        left: Float,
        top: Float,
        boxWidth: Float,
        boxHeight: Float,
        title: String,
        value: String,
        accentColor: Int,
        sub: String
    ) {
        val rect = RectF(left, top, left + boxWidth, top + boxHeight)
        val fill = Paint().apply {
            color = Color.parseColor("#252525")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val border = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(rect, 16f, 16f, fill)
        canvas.drawRoundRect(rect, 16f, 16f, border)

        // Accent pill indicator
        val pillPaint = Paint().apply {
            color = accentColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(left + 16f, top + 16f, left + 40f, top + 22f), 3f, 3f, pillPaint)

        val titlePaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, left + 16f, top + 45f, titlePaint)

        val valPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(value, left + 16f, top + 88f, valPaint)

        val subPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            textSize = 13f
            isAntiAlias = true
        }
        canvas.drawText(sub, left + 16f, top + 125f, subPaint)
    }
}
