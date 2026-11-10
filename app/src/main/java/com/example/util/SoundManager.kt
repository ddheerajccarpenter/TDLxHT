package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.example.R
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Native Sound Management System for TDlxHT.
 *
 * All sound assets are bundled natively in res/raw:
 * - first_time_login_sound_effect.wav: Celebratory audio for first successful login (via MediaPlayer)
 * - on_tap_sound_effect.wav: Responsive micro-acoustic feedback for genuine actionable UI taps (via SoundPool)
 * - target_complete_sound.wav: High-satisfaction chime for task/habit completion (via SoundPool)
 * - update_available.wav: Acoustic notification when an app update is detected (via SoundPool)
 *
 * Notification sounds (sound_one_notification.wav and sound_two_notification.wav) are managed
 * natively through Android NotificationChannels in HabitTaskApplication.
 *
 * Zero cloud dependency. No online audio downloads or remote manifest synchronization.
 */
object SoundManager {
    private const val TAG = "SoundManager"

    enum class SoundType(val id: String, val resId: Int) {
        TAP("on_tap", R.raw.on_tap_sound_effect),
        TARGET_COMPLETE("target_complete", R.raw.target_complete_sound),
        UPDATE_AVAILABLE("update_available", R.raw.update_available)
    }

    private var soundPool: SoundPool? = null
    private val loadedSoundIds = ConcurrentHashMap<String, Int>()
    private val readySoundIds = Collections.synchronizedSet(mutableSetOf<Int>())
    private var isInitialized = false
    private var activeMediaPlayer: MediaPlayer? = null

    @Synchronized
    fun init(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()

            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    readySoundIds.add(sampleId)
                } else {
                    Log.w(TAG, "Sample $sampleId failed to load (status $status)")
                }
            }
            soundPool = pool

            // Load bundled raw audio resources into SoundPool
            SoundType.entries.forEach { soundType ->
                try {
                    val soundId = pool.load(appContext, soundType.resId, 1)
                    loadedSoundIds[soundType.id] = soundId
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load bundled sound ${soundType.id}", e)
                }
            }

            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SoundManager", e)
        }
    }

    /**
     * Plays a short, latency-sensitive sound effect from SoundPool.
     */
    fun play(context: Context, type: SoundType) {
        if (!AppPreferences.isSoundsEnabled(context)) return
        init(context)

        val soundId = loadedSoundIds[type.id]
        if (soundId != null && soundId > 0 && readySoundIds.contains(soundId)) {
            try {
                soundPool?.play(soundId, 0.90f, 0.90f, 1, 0, 1.0f)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to play sound ${type.id}", e)
            }
        }
    }

    /**
     * Plays the authentic on-tap sound effect on genuine, enabled, actionable interactions.
     */
    fun playTapSound(context: Context) = play(context, SoundType.TAP)

    /**
     * Plays the target complete sound effect when a task or habit is successfully completed.
     */
    fun playTargetComplete(context: Context) = play(context, SoundType.TARGET_COMPLETE)

    /**
     * Plays the update available sound effect when a newer app release is detected.
     */
    fun playUpdateAvailable(context: Context) = play(context, SoundType.UPDATE_AVAILABLE)

    /**
     * Plays the celebratory first-time login sound (7.51s) via MediaPlayer with automatic release.
     */
    @Synchronized
    fun playFirstLoginSound(context: Context) {
        if (!AppPreferences.isSoundsEnabled(context)) return

        try {
            activeMediaPlayer?.run {
                if (isPlaying) stop()
                release()
            }
            activeMediaPlayer = null

            val mp = MediaPlayer.create(context.applicationContext, R.raw.first_time_login_sound_effect) ?: return
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            mp.setOnCompletionListener { player ->
                player.release()
                if (activeMediaPlayer == player) activeMediaPlayer = null
            }
            mp.setOnErrorListener { player, _, _ ->
                player.release()
                if (activeMediaPlayer == player) activeMediaPlayer = null
                true
            }
            activeMediaPlayer = mp
            mp.start()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to play first login sound", e)
        }
    }

    // --- Backwards-compatible aliases for existing callers ---
    fun playButtonClick(context: Context) = playTapSound(context)
    fun playTaskComplete(context: Context) = playTargetComplete(context)
    fun playHabitTick(context: Context) = playTargetComplete(context)
    fun playSwipeNav(context: Context) = playTapSound(context)
    fun playTimerEnd(context: Context) = playTargetComplete(context)

    /**
     * Releases active audio players on application termination.
     */
    @Synchronized
    fun release() {
        try {
            activeMediaPlayer?.run {
                if (isPlaying) stop()
                release()
            }
            activeMediaPlayer = null

            soundPool?.release()
            soundPool = null
            loadedSoundIds.clear()
            readySoundIds.clear()
            isInitialized = false
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing SoundManager", e)
        }
    }
}
