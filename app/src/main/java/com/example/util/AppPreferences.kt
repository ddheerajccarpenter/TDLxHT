package com.example.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppPreferences {
    private const val PREFS_NAME = "tdlxht_preferences"
    private const val KEY_SHAKE_TO_OPEN = "shake_to_open_enabled"
    private const val KEY_VIBRATIONS = "vibrations_enabled"
    private const val KEY_TODAY_TASKS_VIEW_MODE = "today_tasks_view_mode"
    private const val KEY_TODAY_HABITS_VIEW_MODE = "today_habits_view_mode"
    private const val KEY_FAVORITE_TOOL_IDS = "favorite_tool_ids"
    private const val KEY_CUSTOM_TOOL_ORDER = "custom_tool_order"
    private const val KEY_THEME_MODE = "app_theme_mode" // "SYSTEM", "LIGHT", "DARK"
    private const val KEY_DISMISSED_UPDATE_VERSION_CODE = "dismissed_update_version_code"
    private const val KEY_SHAKE_ACTION = "shake_action" // "CREATE_TASK", "START_TIMER", "QUICK_CHOOSER", "DISABLED"
    private const val KEY_SHAKE_TIMER_DURATION = "shake_timer_duration_minutes"

    private val _themeMode = MutableStateFlow("SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _shakeToOpen = MutableStateFlow(true)
    val shakeToOpen: StateFlow<Boolean> = _shakeToOpen.asStateFlow()

    private val _shakeAction = MutableStateFlow("CREATE_TASK")
    val shakeAction: StateFlow<String> = _shakeAction.asStateFlow()

    private val _shakeTimerDuration = MutableStateFlow(25)
    val shakeTimerDuration: StateFlow<Int> = _shakeTimerDuration.asStateFlow()

    private val _vibrations = MutableStateFlow(true)
    val vibrations: StateFlow<Boolean> = _vibrations.asStateFlow()

    private val _todayTasksViewMode = MutableStateFlow("LIST")
    val todayTasksViewMode: StateFlow<String> = _todayTasksViewMode.asStateFlow()

    private val _todayHabitsViewMode = MutableStateFlow("LIST")
    val todayHabitsViewMode: StateFlow<String> = _todayHabitsViewMode.asStateFlow()

    private val _favoriteToolIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteToolIds: StateFlow<Set<String>> = _favoriteToolIds.asStateFlow()

    private val _customToolOrder = MutableStateFlow<List<String>>(emptyList())
    val customToolOrder: StateFlow<List<String>> = _customToolOrder.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _themeMode.value = prefs.getString(KEY_THEME_MODE, "SYSTEM")?.uppercase() ?: "SYSTEM"
        _shakeToOpen.value = prefs.getBoolean(KEY_SHAKE_TO_OPEN, true)
        _shakeAction.value = prefs.getString(KEY_SHAKE_ACTION, "CREATE_TASK") ?: "CREATE_TASK"
        _shakeTimerDuration.value = prefs.getInt(KEY_SHAKE_TIMER_DURATION, 25)
        _vibrations.value = prefs.getBoolean(KEY_VIBRATIONS, true)
        _todayTasksViewMode.value = prefs.getString(KEY_TODAY_TASKS_VIEW_MODE, "LIST") ?: "LIST"
        _todayHabitsViewMode.value = prefs.getString(KEY_TODAY_HABITS_VIEW_MODE, "LIST") ?: "LIST"
        _favoriteToolIds.value = prefs.getStringSet(KEY_FAVORITE_TOOL_IDS, emptySet())?.toSet() ?: emptySet()
        val orderCsv = prefs.getString(KEY_CUSTOM_TOOL_ORDER, "") ?: ""
        _customToolOrder.value = if (orderCsv.isNotBlank()) orderCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList()
        isInitialized = true
    }

    fun getThemeMode(context: Context): String {
        init(context)
        return _themeMode.value
    }

    fun setThemeMode(context: Context, mode: String) {
        init(context)
        val normalized = when (mode.trim().uppercase()) {
            "LIGHT" -> "LIGHT"
            "DARK" -> "DARK"
            else -> "SYSTEM"
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, normalized)
            .commit() // Synchronous disk commit so restart/recreation immediately reads new theme
        _themeMode.value = normalized
    }

    fun isShakeToOpenEnabled(context: Context): Boolean {
        init(context)
        return _shakeToOpen.value
    }

    fun setShakeToOpenEnabled(context: Context, enabled: Boolean) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHAKE_TO_OPEN, enabled)
            .apply()
        _shakeToOpen.value = enabled
    }

    fun isVibrationsEnabled(context: Context): Boolean {
        init(context)
        return _vibrations.value
    }

    fun setVibrationsEnabled(context: Context, enabled: Boolean) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_VIBRATIONS, enabled)
            .apply()
        _vibrations.value = enabled
    }

    fun getTodayTasksViewMode(context: Context): String {
        init(context)
        return _todayTasksViewMode.value
    }

    fun setTodayTasksViewMode(context: Context, mode: String) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TODAY_TASKS_VIEW_MODE, mode)
            .apply()
        _todayTasksViewMode.value = mode
    }

    fun getTodayHabitsViewMode(context: Context): String {
        init(context)
        return _todayHabitsViewMode.value
    }

    fun setTodayHabitsViewMode(context: Context, mode: String) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TODAY_HABITS_VIEW_MODE, mode)
            .apply()
        _todayHabitsViewMode.value = mode
    }

    fun getFavoriteToolIds(context: Context): Set<String> {
        init(context)
        return _favoriteToolIds.value
    }

    fun toggleToolFavorite(context: Context, toolId: String) {
        init(context)
        val current = _favoriteToolIds.value
        val updated = if (current.contains(toolId)) current - toolId else current + toolId
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_FAVORITE_TOOL_IDS, updated)
            .apply()
        _favoriteToolIds.value = updated
    }

    fun setFavoriteToolIds(context: Context, ids: Set<String>) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_FAVORITE_TOOL_IDS, ids)
            .apply()
        _favoriteToolIds.value = ids
    }

    fun getCustomToolOrder(context: Context): List<String> {
        init(context)
        return _customToolOrder.value
    }

    fun setCustomToolOrder(context: Context, order: List<String>) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_TOOL_ORDER, order.joinToString(","))
            .apply()
        _customToolOrder.value = order
    }

    fun getDismissedUpdateVersionCode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_DISMISSED_UPDATE_VERSION_CODE, 0)
    }

    fun setDismissedUpdateVersionCode(context: Context, versionCode: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_DISMISSED_UPDATE_VERSION_CODE, versionCode)
            .apply()
    }

    fun getShakeAction(context: Context): String {
        init(context)
        return _shakeAction.value
    }

    fun setShakeAction(context: Context, action: String) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SHAKE_ACTION, action)
            .apply()
        _shakeAction.value = action
    }

    fun getShakeTimerDuration(context: Context): Int {
        init(context)
        return _shakeTimerDuration.value
    }

    fun setShakeTimerDuration(context: Context, minutes: Int) {
        init(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_SHAKE_TIMER_DURATION, minutes)
            .apply()
        _shakeTimerDuration.value = minutes
    }
}
