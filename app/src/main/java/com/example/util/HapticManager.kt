package com.example.util

import android.content.Context

object HapticManager {

    enum class HapticType {
        SLIDER_SNAP,
        TASK_COMPLETED,
        BUTTON_TAP,
        DELETE_WARNING,
        SHAKE_TRIGGERED
    }

    fun performHaptic(context: Context, type: HapticType) {
        if (!AppPreferences.isVibrationsEnabled(context)) return

        when (type) {
            HapticType.SLIDER_SNAP -> VibrationHelper.tick(context)
            HapticType.BUTTON_TAP -> VibrationHelper.click(context)
            HapticType.TASK_COMPLETED -> VibrationHelper.success(context)
            HapticType.DELETE_WARNING -> VibrationHelper.warning(context)
            HapticType.SHAKE_TRIGGERED -> VibrationHelper.shakeDetected(context)
        }
    }

    fun sliderSnap(context: Context) = performHaptic(context, HapticType.SLIDER_SNAP)
    fun buttonTap(context: Context) = performHaptic(context, HapticType.BUTTON_TAP)
    fun taskCompleted(context: Context) = performHaptic(context, HapticType.TASK_COMPLETED)
    fun deleteWarning(context: Context) = performHaptic(context, HapticType.DELETE_WARNING)
    fun shakeTriggered(context: Context) = performHaptic(context, HapticType.SHAKE_TRIGGERED)
}

