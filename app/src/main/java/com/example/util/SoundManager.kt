package com.example.util

import android.content.Context
import android.media.AudioAttributes
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
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
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
    private val loadedSoundIds = ConcurrentHashMap<String, Int>()
    private val readySoundIds = Collections.synchronizedSet(mutableSetOf<Int>())
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val pool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                readySoundIds.add(sampleId)
            }
        }
        soundPool = pool

        // 1. Load cached custom audio files if already present
        loadCachedSounds(appContext)

        // 2. Synthesize & cache local WAV fallback tones so SoundPool is always primed immediately
        ensureFallbackSounds(appContext)

        isInitialized = true

        // 3. Refresh remote sound manifest in background
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

    /**
     * Pre-synthesizes crisp local fallback tones into valid WAV files on disk and loads them into SoundPool.
     * Guarantees zero-allocation, non-blocking hardware playback without AudioTrack churn or Thread.sleep.
     */
    private fun ensureFallbackSounds(context: Context) {
        val pool = soundPool ?: return
        val audioDir = File(context.filesDir, AUDIO_DIR).apply { mkdirs() }

        SoundType.entries.forEach { soundType ->
            if (!loadedSoundIds.containsKey(soundType.id)) {
                val fallbackFile = File(audioDir, "${soundType.id}_fallback.wav")
                if (!fallbackFile.exists() || fallbackFile.length() == 0L) {
                    val pcm = synthesizePcm(soundType)
                    writeWavFile(fallbackFile, pcm, 44100)
                }
                if (fallbackFile.exists() && fallbackFile.length() > 0L) {
                    try {
                        val soundId = pool.load(fallbackFile.absolutePath, 1)
                        loadedSoundIds[soundType.id] = soundId
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load fallback sound ${soundType.id}", e)
                    }
                }
            }
        }
    }

    private fun writeWavFile(file: File, pcm: ShortArray, sampleRate: Int) {
        try {
            val byteRate = sampleRate * 1 * 2
            val dataSize = pcm.size * 2
            val totalSize = 36 + dataSize
            file.outputStream().use { out ->
                val header = ByteArray(44)
                header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
                header[4] = (totalSize and 0xff).toByte()
                header[5] = ((totalSize shr 8) and 0xff).toByte()
                header[6] = ((totalSize shr 16) and 0xff).toByte()
                header[7] = ((totalSize shr 24) and 0xff).toByte()
                header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
                header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
                header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
                header[20] = 1; header[21] = 0 // PCM
                header[22] = 1; header[23] = 0 // Mono
                header[24] = (sampleRate and 0xff).toByte()
                header[25] = ((sampleRate shr 8) and 0xff).toByte()
                header[26] = ((sampleRate shr 16) and 0xff).toByte()
                header[27] = ((sampleRate shr 24) and 0xff).toByte()
                header[28] = (byteRate and 0xff).toByte()
                header[29] = ((byteRate shr 8) and 0xff).toByte()
                header[30] = ((byteRate shr 16) and 0xff).toByte()
                header[31] = ((byteRate shr 24) and 0xff).toByte()
                header[32] = 2; header[33] = 0 // Block align
                header[34] = 16; header[35] = 0 // Bits per sample
                header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
                header[40] = (dataSize and 0xff).toByte()
                header[41] = ((dataSize shr 8) and 0xff).toByte()
                header[42] = ((dataSize shr 16) and 0xff).toByte()
                header[43] = ((dataSize shr 24) and 0xff).toByte()

                out.write(header)
                val buffer = ByteArray(pcm.size * 2)
                for (i in pcm.indices) {
                    val s = pcm[i]
                    buffer[i * 2] = (s.toInt() and 0xff).toByte()
                    buffer[i * 2 + 1] = ((s.toInt() shr 8) and 0xff).toByte()
                }
                out.write(buffer)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write WAV fallback: ${e.message}")
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
                            continue
                        }

                        // Download verified sound
                        downloadSoundFile(soundUrl, targetFile, expectedSha256)

                        // Reload in SoundPool
                        soundPool?.let { pool ->
                            if (targetFile.exists()) {
                                val oldId = loadedSoundIds[soundId]
                                val newPoolId = pool.load(targetFile.absolutePath, 1)
                                loadedSoundIds[soundId] = newPoolId
                                if (oldId != null && oldId > 0) {
                                    pool.unload(oldId)
                                    readySoundIds.remove(oldId)
                                }
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
        if (soundId != null && soundId > 0 && readySoundIds.contains(soundId)) {
            soundPool?.play(soundId, 0.85f, 0.85f, 1, 0, 1.0f)
        }
    }

    fun playTaskComplete(context: Context) = play(context, SoundType.TASK_COMPLETE)
    fun playHabitTick(context: Context) = play(context, SoundType.HABIT_TICK)
    fun playButtonClick(context: Context) = play(context, SoundType.BUTTON_CLICK)
    fun playTimerEnd(context: Context) = play(context, SoundType.TIMER_END)
    fun playSwipeNav(context: Context) = play(context, SoundType.SWIPE_NAV)

    /**
     * Organic micro-tone synthesis formulas.
     */
    private fun synthesizePcm(type: SoundType): ShortArray {
        val sampleRate = 44100
        return when (type) {
            SoundType.TASK_COMPLETE -> {
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
    }
}
