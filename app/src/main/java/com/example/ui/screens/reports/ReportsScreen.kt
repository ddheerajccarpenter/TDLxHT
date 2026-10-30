package com.example.ui.screens.reports

import com.example.ui.theme.SFProFontFamily
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.asImageBitmap
import com.example.ui.components.FloatingNavContentBottomPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.service.AiIntelligenceEngine
import com.example.data.service.AiProductivityInsight
import com.example.ui.components.AppEmptyState
import com.example.ui.components.AppHeader
import com.example.ui.components.SketchCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.SketchSegmentedControl
import com.example.ui.components.bounceClick
import com.example.ui.components.heroEntrance
import com.example.ui.components.staggeredEntrance
import com.example.ui.components.paperDotBackground
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PaperBackground
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.theme.PrimaryAccentLime
import com.example.ui.theme.PrimaryDarkCanvas
import com.example.ui.theme.ProgressBarFilled
import com.example.ui.theme.ProgressBarTrack
import com.example.ui.viewmodel.ReportPeriod
import com.example.ui.viewmodel.ReportStats
import com.example.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ReportsScreen(
    onBack: (() -> Unit)? = null,
    onNavigateToAddTask: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val viewModel: ReportsViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val aiEngine = remember { AiIntelligenceEngine.instance }
    var isRefreshingAi by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().paperDotBackground()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = FloatingNavContentBottomPadding + 48.dp)
        ) {
            // Header
            item {
                AppHeader(
                    title = "Insights",
                    onBack = onBack
                )
                Spacer(Modifier.height(10.dp))
            }

            // AI Productivity Advisor Section — single source of truth bound to ReportStats
            item {
                AiProductivitySection(
                    stats = state.stats,
                    isRefreshing = isRefreshingAi,
                    onRefresh = {
                        coroutineScope.launch {
                            isRefreshingAi = true
                            aiEngine.refreshInsights(forceNetwork = true)
                            isRefreshingAi = false
                        }
                    },
                    onActionSuggestionClick = {
                        onNavigateToAddTask?.invoke() ?: onBack?.invoke()
                    }
                )
                Spacer(Modifier.height(14.dp))
            }

            // Period selector
            item {
                SketchSegmentedControl(
                    items = listOf("Weekly", "Monthly"),
                    selectedIndex = if (state.period == ReportPeriod.WEEKLY) 0 else 1,
                    onItemSelected = {
                        viewModel.setPeriod(if (it == 0) ReportPeriod.WEEKLY else ReportPeriod.MONTHLY)
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }

            // Navigation row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp)
                            .border(1.dp, CardBorder, CircleShape)
                            .clip(CircleShape)
                            .background(CardBackground)
                            .bounceClick { viewModel.navigatePrevious() }
                            .semantics { contentDescription = "Previous period" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = InkPrimary, modifier = Modifier.size(18.dp))
                    }

                    Text(
                        text = state.stats.periodLabel.ifEmpty { "Loading…" },
                        style = TextStyle(fontFamily = SFProFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary),
                        textAlign = TextAlign.Center
                    )

                    val canGoNext = (state.period == ReportPeriod.WEEKLY && state.weekOffset < 0) ||
                            (state.period == ReportPeriod.MONTHLY && state.monthOffset < 0)
                    Box(
                        modifier = Modifier.size(36.dp)
                            .border(1.dp, if (canGoNext) CardBorder else CardBorder.copy(alpha = 0.3f), CircleShape)
                            .clip(CircleShape)
                            .background(CardBackground)
                            .bounceClick(enabled = canGoNext) { viewModel.navigateNext() }
                            .semantics { contentDescription = "Next period" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                            tint = if (canGoNext) InkPrimary else InkMuted,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Stats cards
            item {
                if (!state.stats.hasEnoughData) {
                    EmptyStatsCard()
                } else {
                    StatsSection(stats = state.stats)
                }
            }

            // Napkin visual section
            item {
                Spacer(Modifier.height(16.dp))
                NapkinVisualSection(
                    isGenerating = state.isGeneratingVisual,
                    visualBitmap = state.visualBitmap,
                    visualUrl = state.visualResult?.imageUrl,
                    error = state.visualError,
                    isCached = state.visualResult?.isCached == true,
                    onGenerate = { viewModel.generateNapkinVisual() },
                    onRegenerate = { viewModel.generateNapkinVisual(forceRegenerate = true) }
                )
            }
        }
    }
}

@Composable
private fun StatsSection(stats: ReportStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("STATISTICS", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary))

        // Tasks card
        SketchCard(Modifier.fillMaxWidth().staggeredEntrance(index = 0)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                if (stats.tasksTotal > 0) {
                    StatRow("Tasks Completed", "${stats.tasksCompleted} / ${stats.tasksTotal}", stats.taskCompletionRate)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tasks Scheduled", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary))
                        Text("No tasks scheduled", style = TextStyle(fontSize = 13.sp, color = InkMuted))
                    }
                }
                if (stats.tasksOverdue > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text("${stats.tasksOverdue} overdue", style = TextStyle(fontSize = 13.sp, color = com.example.ui.theme.AccentRed))
                }
            }
        }

        // Habits card
        SketchCard(Modifier.fillMaxWidth().staggeredEntrance(index = 1)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                StatRow("Habit Targets Met", "${stats.habitsCompleted} / ${stats.habitsTotal} days", stats.habitCompletionRate)
                if (stats.sessionsTotal > stats.habitsTotal) {
                    Spacer(Modifier.height(8.dp))
                    StatRow("Habit Sessions Logged", "${stats.sessionsCompleted} / ${stats.sessionsTotal} sessions", stats.sessionCompletionRate)
                }
                Spacer(Modifier.height(8.dp))
                Text("Longest streak: ${stats.longestStreak} days",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AccentTerracotta))
            }
        }

        // Category distribution
        if (stats.categoryDistribution.isNotEmpty()) {
            SketchCard(Modifier.fillMaxWidth().staggeredEntrance(index = 2)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Category Distribution", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary))
                    Spacer(Modifier.height(10.dp))
                    val total = stats.categoryDistribution.values.sum().toFloat()
                    stats.categoryDistribution.entries.sortedByDescending { it.value }.take(5).forEach { (cat, count) ->
                        val pct = (count / total * 100).roundToInt()
                        val pctFloat by animateFloatAsState(targetValue = count / total, animationSpec = tween(600), label = "cat_$cat")
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(cat, style = TextStyle(fontSize = 13.sp, color = InkPrimary), modifier = Modifier.width(90.dp))
                            LinearProgressIndicator(
                                progress = { pctFloat },
                                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = ProgressBarFilled,
                                trackColor = ProgressBarTrack
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("$pct%", style = TextStyle(fontSize = 12.sp, color = InkSecondary), modifier = Modifier.width(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, rate: Float) {
    Text(label, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary))
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        val animatedRate by animateFloatAsState(targetValue = rate, animationSpec = tween(700), label = "rate_$label")
        LinearProgressIndicator(
            progress = { animatedRate },
            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = ProgressBarFilled,
            trackColor = ProgressBarTrack
        )
        Spacer(Modifier.width(12.dp))
        Text(value, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
    }
    Spacer(Modifier.height(2.dp))
    Text("${(rate * 100).roundToInt()}% completion",
        style = TextStyle(fontSize = 12.sp, color = InkSecondary))
}

@Composable
private fun EmptyStatsCard() {
    AppEmptyState(
        icon = Icons.Default.AutoAwesome,
        title = "No data yet",
        subtitle = "Complete tasks or habits to generate insights"
    )
}

@Composable
private fun NapkinVisualSection(
    isGenerating: Boolean,
    visualBitmap: android.graphics.Bitmap?,
    visualUrl: String?,
    error: String?,
    isCached: Boolean,
    onGenerate: () -> Unit,
    onRegenerate: () -> Unit
) {
    Text("AI VISUAL REPORT", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = InkSecondary))
    Spacer(Modifier.height(8.dp))

    SketchCard(Modifier.fillMaxWidth().staggeredEntrance(index = 3)) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isGenerating -> {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp, color = InkPrimary)
                    Spacer(Modifier.height(12.dp))
                    Text("Generating visual report…",
                        style = TextStyle(fontSize = 14.sp, color = InkSecondary, textAlign = TextAlign.Center))
                    Text("Rendering local Room database metrics",
                        style = TextStyle(fontSize = 11.sp, color = InkMuted))
                    Spacer(Modifier.height(16.dp))
                }
                visualBitmap != null -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Generated visual report",
                            style = TextStyle(fontSize = 12.sp, color = InkMuted)
                        )
                        Box(
                            modifier = Modifier
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .bounceClick { onRegenerate() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Refresh, "Regenerate", tint = InkSecondary, modifier = Modifier.size(14.dp))
                                Text("Refresh", style = TextStyle(fontSize = 12.sp, color = InkSecondary))
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Image(
                        bitmap = visualBitmap.asImageBitmap(),
                        contentDescription = "AI Productivity Visual Report",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    )
                }
                visualUrl != null -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (isCached) "Cached visual" else "Generated visual",
                            style = TextStyle(fontSize = 12.sp, color = InkMuted)
                        )
                        Box(
                            modifier = Modifier
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .bounceClick { onRegenerate() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Refresh, "Regenerate", tint = InkSecondary, modifier = Modifier.size(14.dp))
                                Text("Refresh", style = TextStyle(fontSize = 12.sp, color = InkSecondary))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(ProgressBarTrack, RoundedCornerShape(10.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Visual ready", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary))
                            Text("Open in browser to view", style = TextStyle(fontSize = 11.sp, color = InkSecondary))
                        }
                    }
                }
                error != null -> {
                    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(CardBackground, RoundedCornerShape(10.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(error, style = TextStyle(fontSize = 13.sp, color = InkPrimary, lineHeight = 18.sp))
                                Spacer(Modifier.height(4.dp))
                                Text("Underlying activity data remains fully preserved above.", style = TextStyle(fontSize = 11.sp, color = InkMuted))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    GenerateButton(label = "Try Again", onClick = onGenerate)
                }
                else -> {
                    Text(
                        "Generate an AI-powered visual summary of your productivity for this period.",
                        style = TextStyle(fontSize = 13.sp, color = InkSecondary, textAlign = TextAlign.Center, lineHeight = 19.sp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        "Your Napkin API key never leaves the server",
                        style = TextStyle(fontSize = 11.sp, color = InkMuted, textAlign = TextAlign.Center)
                    )
                    Spacer(Modifier.height(12.dp))
                    GenerateButton(label = "Generate Visual Report", onClick = onGenerate)
                }
            }
        }
    }
}

@Composable
private fun GenerateButton(label: String, onClick: () -> Unit) {
    GlassButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .semantics { contentDescription = label },
        icon = {
            Icon(Icons.Default.AutoAwesome, null, tint = InkPrimary, modifier = Modifier.size(18.dp))
        }
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
        )
    }
}

@Composable
private fun AiProductivitySection(
    stats: ReportStats,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onActionSuggestionClick: (() -> Unit)? = null
) {
    val totalItems = stats.tasksTotal + stats.habitsTotal
    val totalCompleted = stats.tasksCompleted + stats.habitsCompleted
    val overallRate = if (totalItems > 0) totalCompleted.toFloat() / totalItems else 0f
    val pct = (overallRate * 100).roundToInt()

    val title: String
    val message: String
    val actionSuggestion: String

    if (stats.hasEnoughData) {
        if (stats.tasksTotal == 0 && stats.habitsTotal > 0) {
            val habitPct = (stats.habitCompletionRate * 100).roundToInt()
            title = if (stats.habitsCompleted > 0) "Habits Active ($habitPct% Targets Met)" else "Habits In Motion"
            message = if (stats.habitsCompleted > 0) {
                "You have met ${stats.habitsCompleted} of ${stats.habitsTotal} habit daily targets (${stats.sessionsCompleted}/${stats.sessionsTotal} sessions logged) during ${stats.periodLabel}. No tasks are currently scheduled for this period."
            } else {
                "No tasks are scheduled for ${stats.periodLabel}. You have ${stats.habitsTotal} habit check-in days scheduled (${stats.sessionsTotal} sessions total). Add a task to start tracking your daily to-dos alongside your habits."
            }
            actionSuggestion = "Add a Task"
        } else if (stats.tasksTotal > 0 && stats.habitsTotal == 0) {
            title = when {
                stats.taskCompletionRate >= 0.8f -> "Peak Performance ($pct% Done)"
                stats.taskCompletionRate >= 0.5f -> "Solid Momentum ($pct% Done)"
                else -> "Focus Opportunity ($pct% Done)"
            }
            message = when {
                stats.taskCompletionRate >= 0.8f -> "You completed ${stats.tasksCompleted} of ${stats.tasksTotal} scheduled tasks during ${stats.periodLabel}. Excellent execution!"
                stats.taskCompletionRate >= 0.5f -> "You completed ${stats.tasksCompleted} of ${stats.tasksTotal} tasks this period.${if (stats.tasksOverdue > 0) " Notice: ${stats.tasksOverdue} overdue tasks need attention." else " Keep pushing forward to close remaining goals."}"
                else -> "Completed ${stats.tasksCompleted} of ${stats.tasksTotal} scheduled tasks.${if (stats.tasksOverdue > 0) " You have ${stats.tasksOverdue} overdue tasks to review." else " Break tasks into smaller subtasks to build momentum."}"
            }
            actionSuggestion = when {
                stats.tasksOverdue > 0 -> "Resolve ${stats.tasksOverdue} Overdue"
                stats.taskCompletionRate >= 0.8f -> "Maintain Current Cadence"
                stats.taskCompletionRate >= 0.5f -> "Prioritize Pending Tasks"
                else -> "Focus on 1 Quick Win"
            }
        } else {
            title = when {
                overallRate >= 0.8f -> "Peak Performance ($pct% Done)"
                overallRate >= 0.5f -> "Solid Momentum ($pct% Done)"
                else -> "Focus Opportunity ($pct% Done)"
            }
            message = when {
                overallRate >= 0.8f -> "You completed ${stats.tasksCompleted} of ${stats.tasksTotal} tasks and maintained ${stats.habitsCompleted} of ${stats.habitsTotal} habit targets during ${stats.periodLabel}. Outstanding consistency!"
                overallRate >= 0.5f -> "You completed ${stats.tasksCompleted} of ${stats.tasksTotal} tasks this period with ${stats.habitsCompleted} habit targets met.${if (stats.tasksOverdue > 0) " Notice: ${stats.tasksOverdue} overdue tasks need attention." else " Keep pushing forward to close remaining goals."}"
                else -> "Completed ${stats.tasksCompleted} of ${stats.tasksTotal} scheduled tasks and ${stats.habitsCompleted} of ${stats.habitsTotal} habit targets.${if (stats.tasksOverdue > 0) " You have ${stats.tasksOverdue} overdue tasks to review." else " Break tasks into smaller subtasks to build momentum."}"
            }
            actionSuggestion = when {
                stats.tasksOverdue > 0 -> "Resolve ${stats.tasksOverdue} Overdue"
                overallRate >= 0.8f -> "Maintain Current Cadence"
                overallRate >= 0.5f -> "Prioritize Pending Tasks"
                else -> "Focus on 1 Quick Win"
            }
        }
    } else {
        title = "No Activity Data"
        message = "No scheduled tasks or habits were recorded for ${stats.periodLabel.ifEmpty { "this period" }}. Complete tasks or log habit sessions to unlock personalized AI productivity guidance."
        actionSuggestion = "Schedule a Task"
    }

    val isDark = com.example.ui.theme.LocalAppColors.current.isDark
    val advisorAccent = if (isDark) PrimaryAccentLime else AccentTerracotta

    GlassCard(
        modifier = Modifier.fillMaxWidth().heroEntrance(delayMs = 40),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = advisorAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "AI PRODUCTIVITY ADVISOR",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = advisorAccent
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .border(1.dp, if (isDark) PrimaryAccentLime.copy(alpha = 0.5f) else CardBorder, CircleShape)
                        .clip(CircleShape)
                        .bounceClick { if (!isRefreshing) onRefresh() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = advisorAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Insights",
                            tint = if (isDark) PrimaryAccentLime else InkSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = title,
                style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = message,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = InkSecondary,
                    lineHeight = 18.sp
                )
            )

            if (actionSuggestion.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassButton(
                        onClick = { onActionSuggestionClick?.invoke() }
                    ) {
                        Text(
                            text = actionSuggestion,
                            style = TextStyle(
                                fontFamily = SFProFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkPrimary
                            )
                        )
                    }

                    Text(
                        text = "Real activity analysis",
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = InkMuted)
                    )
                }
            }
        }
    }
}