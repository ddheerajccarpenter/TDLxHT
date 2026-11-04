package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.MotionEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.TaskPriority
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.OklchIndigo
import com.example.ui.theme.PrimaryAccentLime
import com.example.ui.theme.SFProFontFamily
import com.example.util.HapticManager
import com.example.util.SafeLog
import com.example.util.SoundManager
import com.example.util.VibrationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Bidirectional JavascriptInterface Bridge for Three.js WebGL Engine.
 */
class AndroidSpatialBridge(
    private val context: Context,
    private val onModelTapped: (String) -> Unit,
    private val onModelLongPressed: (String) -> Unit,
    private val onSwipeDetected: (String) -> Unit
) {
    @JavascriptInterface
    fun notifyModelTapped(modelId: String) {
        ContextCompat.getMainExecutor(context).execute {
            VibrationHelper.tick(context)
            SoundManager.playTaskComplete(context)
            onModelTapped(modelId)
        }
    }

    @JavascriptInterface
    fun notifyModelLongPressed(modelId: String) {
        ContextCompat.getMainExecutor(context).execute {
            VibrationHelper.shakeDetected(context)
            onModelLongPressed(modelId)
        }
    }

    @JavascriptInterface
    fun notifySwipe(direction: String) {
        ContextCompat.getMainExecutor(context).execute {
            onSwipeDetected(direction)
        }
    }

    @JavascriptInterface
    fun logDiagnostics(message: String) {
        SafeLog.d("ThreeJsSpatialEngine", message)
    }
}

/**
 * Controller holder for dispatching commands to the Three.js canvas from Jetpack Compose.
 */
class SpatialCompanionController {
    var webViewRef: WebView? = null

    fun triggerCelebration(priority: TaskPriority = TaskPriority.HIGH, streak: Int = 1) {
        webViewRef?.evaluateJavascript(
            "window.triggerCelebration && window.triggerCelebration('${priority.name}', $streak);",
            null
        )
    }

    fun setFocusSession(active: Boolean, remainingSec: Int, totalSec: Int) {
        webViewRef?.evaluateJavascript(
            "window.setFocusSession && window.setFocusSession($active, $remainingSec, $totalSec);",
            null
        )
    }

    fun setThemeMode(isDark: Boolean) {
        webViewRef?.evaluateJavascript(
            "window.setThemeMode && window.setThemeMode($isDark);",
            null
        )
    }

    fun setRenderPaused(paused: Boolean) {
        webViewRef?.evaluateJavascript(
            "window.setRenderPaused && window.setRenderPaused($paused);",
            null
        )
    }
}

@Composable
fun rememberSpatialCompanionController(): SpatialCompanionController {
    return remember { SpatialCompanionController() }
}

/**
 * Interactive Three.js 3D Spatial Companion Widget.
 *
 * Embeds a hardware-accelerated, transparent Android WebView running Three.js WebGL
 * with touch-orbit inertia physics, celebration particle bursts, and focus session aura.
 *
 * Strictly follows ZERO BLUR policy: crisp refractive borders, translucent stratified glass.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ThreeJsSpatialCompanion(
    modifier: Modifier = Modifier,
    controller: SpatialCompanionController = rememberSpatialCompanionController(),
    onOpenFocusTimer: (() -> Unit)? = null,
    onModelInteracted: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isDark = LocalAppColors.current.isDark
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isFocusModeActive by remember { mutableStateOf(false) }
    var interactionToast by remember { mutableStateOf<String?>(null) }
    var interactionCount by remember { mutableIntStateOf(0) }
    var isWebViewLoading by remember { mutableStateOf(true) }
    var hasWebViewError by remember { mutableStateOf(false) }

    // Sync theme to Three.js canvas
    LaunchedEffect(isDark) {
        controller.setThemeMode(isDark)
    }

    val bridge = remember {
        AndroidSpatialBridge(
            context = context,
            onModelTapped = {
                interactionCount++
                interactionToast = "Purr! +1 Momentum"
                coroutineScope.launch {
                    delay(2200)
                    interactionToast = null
                }
                onModelInteracted?.invoke()
            },
            onModelLongPressed = {
                interactionToast = "Acrobatic Leap!"
                coroutineScope.launch {
                    delay(2200)
                    interactionToast = null
                }
                onModelInteracted?.invoke()
            },
            onSwipeDetected = { _ -> }
        )
    }

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .heroEntrance(delayMs = 80),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header Bar: Title + Status + 360 Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isDark) PrimaryAccentLime else OklchIndigo)
                    )
                    Text(
                        text = "SPATIAL COMPANION",
                        style = TextStyle(
                            fontFamily = SFProFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = if (isDark) PrimaryAccentLime else OklchIndigo
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Interaction toast pill
                    AnimatedVisibility(
                        visible = interactionToast != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0x33FFFFFF) else Color(0x18000000))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = interactionToast.orEmpty(),
                                style = TextStyle(
                                    fontFamily = SFProFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) PrimaryAccentLime else OklchIndigo
                                )
                            )
                        }
                    }

                    // 360 Indicator Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0x14FFFFFF) else Color(0x0D000000))
                            .border(0.5.dp, CardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "360° ORBIT",
                            style = TextStyle(
                                fontFamily = SFProFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = InkSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Keep the interactive companion visible without pushing today's tasks down.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0x0C000000) else Color(0x06FFFFFF))
                    .border(0.75.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            setBackgroundResource(0)

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(false)
                                builtInZoomControls = false
                                displayZoomControls = false
                                cacheMode = WebSettings.LOAD_NO_CACHE
                            }

                            addJavascriptInterface(bridge, "AndroidBridge")

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isWebViewLoading = true
                                    hasWebViewError = false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isWebViewLoading = false
                                    hasWebViewError = false
                                    controller.setThemeMode(isDark)
                                }

                                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                    isWebViewLoading = false
                                    hasWebViewError = true
                                }
                            }

                            // Touch Disambiguation: horizontal orbit vs vertical list scroll
                            var touchDownX = 0f
                            var touchDownY = 0f
                            setOnTouchListener { v, event ->
                                when (event.actionMasked) {
                                    MotionEvent.ACTION_DOWN -> {
                                        touchDownX = event.x
                                        touchDownY = event.y
                                        v.parent?.requestDisallowInterceptTouchEvent(true)
                                    }
                                    MotionEvent.ACTION_MOVE -> {
                                        val dx = abs(event.x - touchDownX)
                                        val dy = abs(event.y - touchDownY)
                                        if (dy > dx * 1.3f && dy > 20f) {
                                            v.parent?.requestDisallowInterceptTouchEvent(false)
                                        } else {
                                            v.parent?.requestDisallowInterceptTouchEvent(true)
                                        }
                                    }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(false)
                                    }
                                }
                                false
                            }

                            loadUrl("file:///android_asset/three3d/index.html")
                            controller.webViewRef = this
                        }
                    },
                    update = { view ->
                        controller.webViewRef = view
                    }
                )

                // Loading Skeleton State
                if (isWebViewLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isDark) Color(0x18000000) else Color(0x0A000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isDark) PrimaryAccentLime else OklchIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Loading Spatial Engine...",
                                style = TextStyle(
                                    fontFamily = SFProFontFamily,
                                    fontSize = 11.sp,
                                    color = InkSecondary
                                )
                            )
                        }
                    }
                }

                // Error / Fallback State
                if (hasWebViewError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isDark) Color(0x2A15161A) else Color(0x18000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Spatial Companion Ready",
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                            )
                            LiquidGlassButton(
                                onClick = { controller.webViewRef?.reload() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("Reload", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Lifecycle power governor
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                controller.webViewRef?.onResume()
                                controller.setRenderPaused(false)
                            }
                            Lifecycle.Event.ON_PAUSE -> {
                                controller.setRenderPaused(true)
                                controller.webViewRef?.onPause()
                            }
                            Lifecycle.Event.ON_DESTROY -> {
                                controller.webViewRef?.destroy()
                                controller.webViewRef = null
                            }
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        controller.setRenderPaused(true)
                        controller.webViewRef?.destroy()
                        controller.webViewRef = null
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Pills Row: [Pet] [Focus Core] [Celebrate] (Typographically protected, zero wrapping)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Pet Action
                LiquidGlassButton(
                    onClick = {
                        VibrationHelper.tick(context)
                        SoundManager.playTaskComplete(context)
                        controller.triggerCelebration(TaskPriority.MEDIUM, 1)
                        interactionToast = "Happy Companion!"
                        coroutineScope.launch {
                            delay(2000)
                            interactionToast = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = InkPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                ) {
                    Text(
                        text = "Pet",
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = SFProFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
                        )
                    )
                }

                // 2. Zen Focus Core Toggle
                LiquidGlassButton(
                    onClick = {
                        VibrationHelper.click(context)
                        isFocusModeActive = !isFocusModeActive
                        controller.setFocusSession(isFocusModeActive, 1500, 1500)
                        if (isFocusModeActive && onOpenFocusTimer != null) {
                            onOpenFocusTimer()
                        }
                    },
                    modifier = Modifier.weight(1.15f),
                    shape = RoundedCornerShape(10.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (isFocusModeActive) OklchIndigo else InkSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                ) {
                    Text(
                        text = if (isFocusModeActive) "Focusing" else "Focus Core",
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = SFProFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFocusModeActive) OklchIndigo else InkPrimary
                        )
                    )
                }

                // 3. Celebrate Burst Action
                LiquidGlassButton(
                    onClick = {
                        HapticManager.taskCompleted(context)
                        SoundManager.playTaskComplete(context)
                        controller.triggerCelebration(TaskPriority.HIGH, 7)
                        interactionToast = "Confetti Burst!"
                        coroutineScope.launch {
                            delay(2000)
                            interactionToast = null
                        }
                    },
                    modifier = Modifier.weight(1.15f),
                    shape = RoundedCornerShape(10.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = OklchIndigo,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                ) {
                    Text(
                        text = "Celebrate",
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = SFProFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    )
                }
            }
        }
    }
}
