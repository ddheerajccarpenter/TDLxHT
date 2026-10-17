package com.example.data.service

import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class NapkinStats(
    val tasksCompleted: Int,
    val tasksTotal: Int,
    val habitsCompleted: Int,
    val habitsTotal: Int,
    val longestStreak: Int,
    val completionRate: Double,
    val topCategories: List<String>,
    val periodLabel: String
)

@Serializable
private data class NapkinRequest(
    val period: String,
    val stats: NapkinStats,
    val forceRegenerate: Boolean = false
)

data class NapkinVisualResult(
    val imageUrl: String?,
    val svgContent: String?,
    val isCached: Boolean,
    val generatedAt: String?
)

class NapkinService {

    private val supabase = SupabaseClient.client
    private val auth get() = supabase.auth
    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Calls the secure Supabase Edge Function.
     * The Napkin API key NEVER leaves the Supabase server — stored as a Supabase secret.
     * Only aggregated statistics are sent from the Android app.
     */
    suspend fun generateVisual(
        period: String, // "weekly" or "monthly"
        stats: NapkinStats,
        forceRegenerate: Boolean = false
    ): Result<NapkinVisualResult> = withContext(Dispatchers.IO) {
        runCatching {
            val session = auth.currentSessionOrNull() ?: error("Not authenticated")
            val accessToken = session.accessToken
            val supabaseUrl = supabase.supabaseUrl
            val supabaseKey = supabase.supabaseKey

            val requestBodyJson = json.encodeToString(NapkinRequest(period, stats, forceRegenerate))

            val request = Request.Builder()
                .url("$supabaseUrl/functions/v1/napkin-visual-generator")
                .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("apikey", supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build()

            val responseBody = httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Edge function error: ${response.code}")
                response.body?.string() ?: error("Empty response")
            }

            val parsed = json.parseToJsonElement(responseBody).jsonObject
            NapkinVisualResult(
                imageUrl = parsed["imageUrl"]?.jsonPrimitive?.contentOrNull,
                svgContent = parsed["svgContent"]?.jsonPrimitive?.contentOrNull,
                isCached = parsed["cached"]?.jsonPrimitive?.boolean ?: false,
                generatedAt = parsed["generatedAt"]?.jsonPrimitive?.contentOrNull
            )
        }
    }
}
