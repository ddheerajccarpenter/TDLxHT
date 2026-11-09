package com.example.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DashboardSection(
    val id: String,
    val title: String,
    val subtitle: String
) {
    BENTO_HERO("bento_hero", "Progress & Momentum", "Circular progress ring and momentum summary"),
    QUICK_METRICS("quick_metrics", "Quick Stats", "Overview of today's key productivity metrics"),
    DAILY_PLANNER("daily_planner", "Daily Tasks", "Prioritized task list with instant completion"),
    TIME_BLOCKS("time_blocks", "Time Blocks", "Scheduled blocks throughout today"),
    HABIT_STREAKS("habit_streaks", "Habit Streaks", "Daily habit tracking and current streaks");

    companion object {
        val DEFAULT_ORDER = listOf(
            BENTO_HERO,
            QUICK_METRICS,
            DAILY_PLANNER,
            TIME_BLOCKS,
            HABIT_STREAKS
        )

        val DEFAULT_VISIBILITY = entries.associateWith { it != TIME_BLOCKS }

        fun fromId(id: String): DashboardSection? = entries.firstOrNull { it.id == id }
    }
}

object DashboardLayoutManager {
    private const val PREFS_NAME = "tdlxht_preferences"
    private const val KEY_DASHBOARD_ORDER = "dashboard_section_order"
    private const val KEY_DASHBOARD_VISIBILITY = "dashboard_section_visibility"

    private val _sectionOrder = MutableStateFlow(DashboardSection.DEFAULT_ORDER)
    val sectionOrder: StateFlow<List<DashboardSection>> = _sectionOrder.asStateFlow()

    private val _sectionVisibility = MutableStateFlow(DashboardSection.DEFAULT_VISIBILITY)
    val sectionVisibility: StateFlow<Map<DashboardSection, Boolean>> = _sectionVisibility.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Read Order
        val orderCsv = prefs.getString(KEY_DASHBOARD_ORDER, null)
        val loadedOrder = if (!orderCsv.isNullOrBlank()) {
            val parsed = orderCsv.split(",")
                .mapNotNull { DashboardSection.fromId(it.trim()) }
            val missing = DashboardSection.DEFAULT_ORDER.filter { it !in parsed }
            parsed + missing
        } else {
            DashboardSection.DEFAULT_ORDER
        }
        _sectionOrder.value = loadedOrder

        // 2. Read Visibility
        val visibilityCsv = prefs.getString(KEY_DASHBOARD_VISIBILITY, null)
        val loadedVisibility = if (!visibilityCsv.isNullOrBlank()) {
            val map = DashboardSection.DEFAULT_VISIBILITY.toMutableMap()
            visibilityCsv.split(";").forEach { pair ->
                val parts = pair.split(":")
                if (parts.size == 2) {
                    val section = DashboardSection.fromId(parts[0].trim())
                    val isVisible = parts[1].trim().toBooleanStrictOrNull()
                    if (section != null && isVisible != null) {
                        map[section] = isVisible
                    }
                }
            }
            map
        } else {
            DashboardSection.DEFAULT_VISIBILITY
        }
        _sectionVisibility.value = loadedVisibility

        isInitialized = true
    }

    fun saveLayout(context: Context, order: List<DashboardSection>, visibility: Map<DashboardSection, Boolean>) {
        init(context)
        _sectionOrder.value = order
        _sectionVisibility.value = visibility

        val orderCsv = order.joinToString(",") { it.id }
        val visibilityCsv = visibility.entries.joinToString(";") { "${it.key.id}:${it.value}" }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DASHBOARD_ORDER, orderCsv)
            .putString(KEY_DASHBOARD_VISIBILITY, visibilityCsv)
            .apply()
    }

    fun resetToDefaults(context: Context) {
        saveLayout(
            context,
            order = DashboardSection.DEFAULT_ORDER,
            visibility = DashboardSection.DEFAULT_VISIBILITY
        )
    }

    fun getOrderCsv(context: Context): String {
        init(context)
        return _sectionOrder.value.joinToString(",") { it.id }
    }

    fun getVisibilityCsv(context: Context): String {
        init(context)
        return _sectionVisibility.value.entries.joinToString(";") { "${it.key.id}:${it.value}" }
    }

    fun applyRemoteSettings(context: Context, orderCsv: String?, visibilityCsv: String?) {
        init(context)
        val loadedOrder = if (!orderCsv.isNullOrBlank()) {
            val parsed = orderCsv.split(",")
                .mapNotNull { DashboardSection.fromId(it.trim()) }
            val missing = DashboardSection.DEFAULT_ORDER.filter { it !in parsed }
            parsed + missing
        } else {
            _sectionOrder.value
        }

        val loadedVisibility = if (!visibilityCsv.isNullOrBlank()) {
            val map = _sectionVisibility.value.toMutableMap()
            visibilityCsv.split(";").forEach { pair ->
                val parts = pair.split(":")
                if (parts.size == 2) {
                    val section = DashboardSection.fromId(parts[0].trim())
                    val isVisible = parts[1].trim().toBooleanStrictOrNull()
                    if (section != null && isVisible != null) {
                        map[section] = isVisible
                    }
                }
            }
            map
        } else {
            _sectionVisibility.value
        }

        saveLayout(context, loadedOrder, loadedVisibility)
    }
}

