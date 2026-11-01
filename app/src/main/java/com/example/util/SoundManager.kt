package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.sin

object SoundManager {
    private const val TAG = "SoundManager"
    private const val AUDIO_DIR = "app_audio"

    enum class SoundType(val id: String, val title: String) {
        TASK_COMPLETE("task_complete", "Task Completed"),
        HABIT_TICK("habit_tick", "Habit Tick"),
        BUTTON_CLICK("button_click", "Button Click"),
        TIMER_END("timer_end", "Timer Finished"),
        SWIPE_NAV("swipe_nav", "Swipe Navigation")
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var soundPool: SoundPool? = null
    private val loadedSoundIds = mutableMapOf<String, Int>()
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Check if any cached custom audio files exist
        loadCachedSounds(appContext)

        isInitialized = true

        // Refresh remote sound manifest in background
        scope.launch {
            checkRemoteSounds(appContext)
        }
    }

    private fun loadCachedSounds(context: Context) {
        val pool = soundPool ?: return
        val audioDir = File(context.filesDir, AUDIO_DIR)
        if (!audioDir.exists()) return

        SoundType.entries.forEach { soundType ->
            val file = File(audioDir, "${soundType.id}.ogg")
            if (file.exists() && file.length() > 0) {
                try {
                    val soundId = pool.load(file.absolutePath, 1)
                    loadedSoundIds[soundType.id] = soundId
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load cached sound ${soundType.id}", e)
                }
            }
        }
    }

    private suspend fun checkRemoteSounds(context: Context) = withContext(Dispatchers.IO) {
        try {
            val endpoint = "${com.example.BuildConfig.APP_WEB_URL.trimEnd('/')}/api/sounds"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(jsonString)
                val audioDir = File(context.filesDir, AUDIO_DIR).apply { mkdirs() }

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val soundId = obj.optString("id")
                    val soundUrl = obj.optString("url")
                    val expectedSha256 = obj.optString("sha256")

                    if (soundId.isNotBlank() && soundUrl.isNotBlank()) {
                        val targetFile = File(audioDir, "${soundId}.ogg")
                        val currentSha256 = if (targetFile.exists()) computeSha256(targetFile) else ""

                        if (expectedSha256.isNotBlank() && currentSha256.equals(expectedSha256, ignoreCase = true)) {
                            // Up to date
                            continue
                        }

                        // Download sound
                        downloadSoundFile(soundUrl, targetFile, expectedSha256)

                        // Reload in SoundPool
                        soundPool?.let { pool ->
                            if (targetFile.exists()) {
                                val newPoolId = pool.load(targetFile.absolutePath, 1)
                                loadedSoundIds[soundId] = newPoolId
                            }
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            Log.d(TAG, "Remote sound sync skipped: ${e.message}")
        }
    }

    private fun downloadSoundFile(urlString: String, targetFile: File, expectedSha256: String) {
        try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            if (conn.responseCode == 200) {
                val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
                conn.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (expectedSha256.isNotBlank()) {
                    val actualSha = computeSha256(tempFile)
                    if (!actualSha.equals(expectedSha256, ignoreCase = true)) {
                        tempFile.delete()
                        return
                    }
                }

                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
            }
            conn.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Download failed for $urlString", e)
        }
    }

    private fun computeSha256(file: File): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            file.forEachBlock { buffer, bytesRead ->
                md.update(buffer, 0, bytesRead)
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    fun play(context: Context, type: SoundType) {
        if (!AppPreferences.isSoundsEnabled(context)) return

        init(context)

        val soundId = loadedSoundIds[type.id]
        if (soundId != null && soundId > 0) {
            soundPool?.play(soundId, 0.85f, 0.85f, 1, 0, 1.0f)
        } else {
            // High fidelity micro-synthesizer fallback for 100% offline responsiveness
            scope.launch {
                synthesizeSound(type)
            }
        }
    }

    fun playTaskComplete(context: Context) = play(context, SoundType.TASK_COMPLETE)
    fun playHabitTick(context: Context) = play(context, SoundType.HABIT_TICK)
    fun playButtonClick(context: Context) = play(context, SoundType.BUTTON_CLICK)
    fun playTimerEnd(context: Context) = play(context, SoundType.TIMER_END)
    fun playSwipeNav(context: Context) = play(context, SoundType.SWIPE_NAV)

    /**
     * Crisp, organic micro-tone synthesizer using AudioTrack PCM.
     * Guaranteed zero-crash, non-annoying audio feedback even before any assets are downloaded.
     */
    private fun synthesizeSound(type: SoundType) {
        try {
            val sampleRate = 44100
            val pcm: ShortArray = when (type) {
                SoundType.TASK_COMPLETE -> {
                    // Gentle two-tone pentatonic chime (C5 523Hz -> E5 659Hz)
                    val durationSeconds = 0.16
                    val numSamples = (sampleRate * durationSeconds).toInt()
                    val half = numSamples / 2
                    ShortArray(numSamples) { i ->
                        val freq = if (i < half) 523.25 else 659.25
                        val t = i.toDouble() / sampleRate
                        val envelope = if (i < half) {
                            (1.0 - (i.toDouble() / half)).coerceIn(0.0, 1.0)
                        } else {
                            (1.0 - ((i - half).toDouble() / half)).coerceIn(0.0, 1.0)
                        }
                        val sample = sin(2.0 * PI * freq * t) * envelope
                        (sample * 16000).toInt().toShort()
                    }
                }
                SoundType.HABIT_TICK -> {
                    // Soft bubble pop (880Hz quickly dropping to 440Hz)
                    val durationSeconds = 0.07
                    val numSamples = (sampleRate * durationSeconds).toInt()
                    ShortArray(numSamples) { i ->
                        val progress = i.toDouble() / numSamples
                        val freq = 880.0 - 440.0 * progress
                        val t = i.toDouble() / sampleRate
                        val envelope = (1.0 - progress) * (1.0 - progress)
                        val sample = sin(2.0 * PI * freq * t) * envelope
                        (sample * 14000).toInt().toShort()
                    }
                }
                SoundType.BUTTON_CLICK -> {
                    // Mechanical crisp click
                    val durationSeconds = 0.02
                    val numSamples = (sampleRate * durationSeconds).toInt()
                    ShortArray(numSamples) { i ->
                        val progress = i.toDouble() / numSamples
                        val t = i.toDouble() / sampleRate
                        val envelope = 1.0 - progress
                        val sample = sin(2.0 * PI * 1200.0 * t) * envelope
                        (sample * 12000).toInt().toShort()
                    }
                }
                SoundType.TIMER_END -> {
                    // Three-tone tranquil chime (587Hz -> 659Hz -> 880Hz)
                    val durationSeconds = 0.35
                    val numSamples = (sampleRate * durationSeconds).toInt()
                    val third = numSamples / 3
                    ShortArray(numSamples) { i ->
                        val freq = when {
                            i < third -> 587.33
                            i < third * 2 -> 659.25
                            else -> 880.0
                        }
                        val t = i.toDouble() / sampleRate
                        val segProgress = (i % third).toDouble() / third
                        val envelope = (1.0 - segProgress)
                        val sample = sin(2.0 * PI * freq * t) * envelope
                        (sample * 18000).toInt().toShort()
                    }
                }
                SoundType.SWIPE_NAV -> {
                    // Micro tactile acoustic click
                    val durationSeconds = 0.015
                    val numSamples = (sampleRate * durationSeconds).toInt()
                    ShortArray(numSamples) { i ->
                        val progress = i.toDouble() / numSamples
                        val t = i.toDouble() / sampleRate
                        val envelope = 1.0 - progress
                        val sample = sin(2.0 * PI * 950.0 * t) * envelope
                        (sample * 10000).toInt().toShort()
                    }
                }
            }

            val bufferSize = pcm.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep((pcm.size.toDouble() / sampleRate * 1000).toLong() + 20)
            track.release()
        } catch (_: Exception) {
        }
    }
}

