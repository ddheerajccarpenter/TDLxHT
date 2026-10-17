package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Event bus for notifying in-foreground components (e.g. MainActivity)
 * when a deliberate shake has been detected by ShakeDetectionService.
 */
object ShakeEventBus {
    private val _events = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun emitShake() {
        _events.tryEmit(System.currentTimeMillis())
    }
}

/**
 * Production-grade shake detector that detects intentional, vigorous back-and-forth
 * phone movements while rejecting false positives from walking, rotation, or table placement.
 */
class ShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    // Algorithm state (primitive values only, 0 heap allocation on hot path)
    private var lastMaxAxis: Int = -1
    private var lastMaxSign: Int = 0
    private var directionChangeCount: Int = 0
    private var windowStartTime: Long = 0L
    private var cooldownUntil: Long = 0L

    companion object {
        // Minimum acceleration in Gs to count as an intentional gesture movement
        private const val SHAKE_THRESHOLD_GRAVITY = 2.20f

        // Time window in ms within which direction reversals must occur
        private const val SHAKE_WINDOW_MS = 650L

        // Minimum number of rapid acceleration direction reversals required
        private const val MIN_DIRECTION_CHANGES = 2

        // Anti-repeat cooldown period in ms after a successful shake trigger
        private const val SHAKE_COOLDOWN_MS = 1800L
    }

    fun start(context: Context): Boolean {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        return accelerometer?.let {
            // SENSOR_DELAY_GAME (approx 50Hz) provides the ideal balance between
            // rapid reversal detection and low battery consumption.
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            SafeLog.d("ShakeDetector", "Sensor registered with SENSOR_DELAY_GAME")
            true
        } ?: false
    }

    fun stop() {
        try {
            sensorManager?.unregisterListener(this)
            SafeLog.d("ShakeDetector", "Sensor unregistered")
        } catch (_: Exception) {
        }
        sensorManager = null
        accelerometer = null
        directionChangeCount = 0
        windowStartTime = 0L
        lastMaxAxis = -1
        lastMaxSign = 0
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val now = System.currentTimeMillis()

        // 1. Anti-repeat cooldown check
        if (now < cooldownUntil) {
            return
        }

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        // 2. Threshold check: must exceed deliberate gesture intensity
        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val absX = abs(gX)
            val absY = abs(gY)
            val absZ = abs(gZ)

            // Determine dominant acceleration axis (0=X, 1=Y, 2=Z)
            val maxAxis = if (absX > absY && absX > absZ) 0 else if (absY > absZ) 1 else 2
            val maxSign = when (maxAxis) {
                0 -> if (gX > 0f) 1 else -1
                1 -> if (gY > 0f) 1 else -1
                else -> if (gZ > 0f) 1 else -1
            }

            // 3. Direction reversal check: phone accelerated, stopped, and accelerated oppositely
            if (lastMaxAxis == maxAxis && lastMaxSign != 0 && lastMaxSign != maxSign) {
                if (directionChangeCount == 0 || (now - windowStartTime <= SHAKE_WINDOW_MS)) {
                    if (directionChangeCount == 0) {
                        windowStartTime = now
                    }
                    directionChangeCount++

                    if (directionChangeCount >= MIN_DIRECTION_CHANGES) {
                        // Intentional shake confirmed!
                        SafeLog.d("ShakeDetector", "Shake confirmed with $directionChangeCount direction changes, gForce=$gForce")
                        directionChangeCount = 0
                        windowStartTime = 0L
                        lastMaxAxis = -1
                        lastMaxSign = 0
                        cooldownUntil = now + SHAKE_COOLDOWN_MS
                        onShake()
                        return
                    }
                } else {
                    // Window expired: start new candidate sequence
                    windowStartTime = now
                    directionChangeCount = 1
                }
            } else if (directionChangeCount == 0) {
                windowStartTime = now
            }

            lastMaxAxis = maxAxis
            lastMaxSign = maxSign
        } else {
            // Decay candidate counter if movement has stopped past the window
            if (directionChangeCount > 0 && (now - windowStartTime > SHAKE_WINDOW_MS)) {
                directionChangeCount = 0
                windowStartTime = 0L
                lastMaxAxis = -1
                lastMaxSign = 0
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
