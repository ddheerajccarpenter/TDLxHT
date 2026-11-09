package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.MotionEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.ConsoleMessage
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
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
import com.example.ui.theme.PrimaryAccentLime
import com.example.ui.theme.SFProFontFamily
import com.example.util.HapticManager
import com.example.util.SafeLog
import com.example.util.SoundManager
import com.example.util.VibrationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val SPATIAL_COMPANION_URL =
    "https://appassets.androidplatform.net/assets/three3d/index.html"

/**
 * Bidirectional JavascriptInterface Bridge for Three.js WebGL Engine.
 */
class AndroidSpatialBridge(
    private val context: Context,
    private val onModelTapped: (String) -> Unit,
    private val onModelLongPressed: (String) -> Unit,
    private val onSwipeDetected: (String) -> Unit,
    private val onModelFallback: (String) -> Unit,
    private val onModelLoaded: () -> Unit
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
    fun notifyModelFallback(reason: String) {
        ContextCompat.getMainExecutor(context).execute { onModelFallback(reason) }
    }

    @JavascriptInterface
    fun notifyModelLoaded() {
        ContextCompat.getMainExecutor(context).execute { onModelLoaded() }
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
    private var primaryWebViewRef: WebView? = null
    private var studioWebViewRef: WebView? = null

    fun setPrimaryWebView(webView: WebView) {
        primaryWebViewRef = webView
        if (studioWebViewRef == null) webViewRef = webView
    }

    fun openStudio(webView: WebView) {
        primaryWebViewRef?.evaluateJavascript("window.setRenderPaused && window.setRenderPaused(true);", null)
        studioWebViewRef = webView
        webViewRef = webView
    }

    fun closeStudio() {
        studioWebViewRef?.evaluateJavascript("window.setRenderPaused && window.setRenderPaused(true);", null)
        studioWebViewRef?.onPause()
        studioWebViewRef?.destroy()
        studioWebViewRef = null
        webViewRef = primaryWebViewRef
        primaryWebViewRef?.onResume()
        primaryWebViewRef?.evaluateJavascript("window.setRenderPaused && window.setRenderPaused(false);", null)
    }

    fun destroyViews() {
        studioWebViewRef?.onPause()
        studioWebViewRef?.destroy()
        primaryWebViewRef?.onPause()
        primaryWebViewRef?.destroy()
        studioWebViewRef = null
        primaryWebViewRef = null
        webViewRef = null
    }

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

    fun setReducedMotion(reducedMotion: Boolean) {
        webViewRef?.evaluateJavascript(
            "window.setReducedMotion && window.setReducedMotion($reducedMotion);",
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
    val reduceMotion = remember { com.example.ui.components.isReducedMotionEnabled(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isFocusModeActive by remember { mutableStateOf(false) }
    var interactionToast by remember { mutableStateOf<String?>(null) }
    var isWebViewLoading by remember { mutableStateOf(true) }
    var hasWebViewError by remember { mutableStateOf(false) }
    var isUsingProceduralFallback by remember { mutableStateOf(false) }
    var hasCompanionResponse by remember { mutableStateOf(false) }
    var fallbackReason by remember { mutableStateOf("") }
    var showExpandedPopup by remember { mutableStateOf(false) }

    val openExpandedPopup = {
        isWebViewLoading = true
        hasWebViewError = false
        hasCompanionResponse = false
        isUsingProceduralFallback = false
        fallbackReason = ""
        showExpandedPopup = true
    }

    // Sync theme to Three.js canvas
    LaunchedEffect(isDark) {
        controller.setThemeMode(isDark)
    }
    LaunchedEffect(reduceMotion) {
        controller.setReducedMotion(reduceMotion)
    }

    val bridge = remember {
        AndroidSpatialBridge(
            context = context,
            onModelTapped = {
                interactionToast = "Tapped"
                coroutineScope.launch {
                    delay(2200)
                    interactionToast = null
                }
                onModelInteracted?.invoke()
            },
            onModelLongPressed = {
                openExpandedPopup()
                onModelInteracted?.invoke()
            },
            onSwipeDetected = { _ -> },
            onModelFallback = { reason ->
                hasCompanionResponse = true
                isUsingProceduralFallback = true
                fallbackReason = reason
            },
            onModelLoaded = {
                hasCompanionResponse = true
                isUsingProceduralFallback = false
                fallbackReason = ""
            }
        )
    }

    LaunchedEffect(isWebViewLoading, hasWebViewError, hasCompanionResponse) {
        if (!isWebViewLoading && !hasWebViewError && !hasCompanionResponse) {
            delay(7000)
            if (!hasCompanionResponse) {
                isUsingProceduralFallback = true
                fallbackReason = "The companion preview did not start"
            }
        }
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
                            .background(InkSecondary)
                    )
                    Text(
                        text = "COMPANION",
                        style = TextStyle(
                            fontFamily = SFProFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = InkPrimary
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
                                    color = InkPrimary
                                )
                            )
                        }
                    }

                    // 360 Indicator & Pop-up trigger
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0x14FFFFFF) else Color(0x0D000000))
                            .border(0.5.dp, CardBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                VibrationHelper.tick(context)
                                openExpandedPopup()
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = "Expand 3D Studio",
                                tint = InkSecondary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Keep the interactive companion visible without pushing today's tasks down.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0x0C000000) else Color(0x06FFFFFF))
                    .border(0.75.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val assetLoader = WebViewAssetLoader.Builder()
                            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(ctx))
                            .build()

                        WebView(ctx).apply {
                            contentDescription = "Interactive spatial companion"
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            setBackgroundResource(0)

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = false
                                allowContentAccess = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(false)
                                builtInZoomControls = false
                                displayZoomControls = false
                                cacheMode = WebSettings.LOAD_NO_CACHE
                            }

                            addJavascriptInterface(bridge, "AndroidBridge")
                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                                    if (message?.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                                        SafeLog.e(
                                            "SpatialCompanionWeb",
                                            "${message.message()} at line ${message.lineNumber()}"
                                        )
                                        hasCompanionResponse = true
                                        isUsingProceduralFallback = true
                                        fallbackReason = "The companion preview encountered an error"
                                    }
                                    return true
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean = request?.url?.host != "appassets.androidplatform.net"

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isWebViewLoading = true
                                    hasWebViewError = false
                                    isUsingProceduralFallback = false
                                    hasCompanionResponse = false
                                    fallbackReason = ""
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isWebViewLoading = false
                                    controller.setThemeMode(isDark)
                                    controller.setReducedMotion(reduceMotion)
                                    controller.setRenderPaused(
                                        !lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                                    )
                                }

                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): android.webkit.WebResourceResponse? {
                                    return request?.url?.let(assetLoader::shouldInterceptRequest)
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    if (request?.isForMainFrame == true) {
                                        isWebViewLoading = false
                                        hasWebViewError = true
                                    } else if (request?.url?.lastPathSegment == "cat.glb") {
                                        hasCompanionResponse = true
                                        isUsingProceduralFallback = true
                                        fallbackReason = "The 3D model could not be loaded"
                                    } else if (request?.url?.lastPathSegment == "bundle.js") {
                                        hasCompanionResponse = true
                                        isUsingProceduralFallback = true
                                        fallbackReason = "The companion engine could not be loaded"
                                    }
                                }

                                override fun onReceivedHttpError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    errorResponse: android.webkit.WebResourceResponse?
                                ) {
                                    val failedResource = request?.url?.lastPathSegment
                                    if (request?.isForMainFrame == true && (errorResponse?.statusCode ?: 200) >= 400) {
                                        isWebViewLoading = false
                                        hasWebViewError = true
                                    } else if (
                                        request?.isForMainFrame == false &&
                                        (errorResponse?.statusCode ?: 200) >= 400 &&
                                        failedResource in setOf("cat.glb", "bundle.js")
                                    ) {
                                        hasCompanionResponse = true
                                        isUsingProceduralFallback = true
                                        fallbackReason = if (failedResource == "cat.glb") {
                                            "The 3D model could not be loaded"
                                        } else {
                                            "The companion engine could not be loaded"
                                        }
                                    }
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

                            loadUrl(SPATIAL_COMPANION_URL)
                            controller.setPrimaryWebView(this)
                        }
                    },
                    update = { view ->
                        controller.setPrimaryWebView(view)
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
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = InkSecondary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Loading companion…",
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
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = InkSecondary, modifier = Modifier.size(22.dp))
                            Text("Companion unavailable", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary))
                            LiquidGlassIconButton(
                                onClick = {
                                    hasWebViewError = false
                                    isWebViewLoading = true
                                    hasCompanionResponse = false
                                    isUsingProceduralFallback = false
                                    fallbackReason = ""
                                    controller.webViewRef?.loadUrl(SPATIAL_COMPANION_URL)
                                },
                                contentDescription = "Retry spatial companion",
                                size = 48.dp
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = InkPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (
                    isUsingProceduralFallback && !hasWebViewError && !isWebViewLoading &&
                    fallbackReason != "The 3D model could not be loaded"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isDark) Color(0xF0101010) else Color(0xF2FFFFFF))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = InkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Companion preview", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                            Text("Interactive model unavailable", fontSize = 10.sp, color = InkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        LiquidGlassIconButton(
                            onClick = {
                                isWebViewLoading = true
                                hasCompanionResponse = false
                                isUsingProceduralFallback = false
                                fallbackReason = ""
                                controller.webViewRef?.loadUrl(SPATIAL_COMPANION_URL)
                            },
                            contentDescription = "Retry companion model. $fallbackReason",
                            size = 48.dp
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = InkPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                } else if (isUsingProceduralFallback && !hasWebViewError && !isWebViewLoading) {
                    Box(Modifier.align(Alignment.BottomEnd).padding(4.dp)) {
                        LiquidGlassIconButton(
                            onClick = {
                                isWebViewLoading = true
                                hasCompanionResponse = false
                                isUsingProceduralFallback = false
                                fallbackReason = ""
                                controller.webViewRef?.loadUrl(SPATIAL_COMPANION_URL)
                            },
                            contentDescription = "Retry companion model",
                            size = 48.dp
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = InkPrimary, modifier = Modifier.size(18.dp))
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
                                controller.destroyViews()
                            }
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        controller.setRenderPaused(true)
                        controller.destroyViews()
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
                        interactionToast = "Petted"
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
                            tint = if (isFocusModeActive) InkPrimary else InkSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                ) {
                    Text(
                        text = if (isFocusModeActive) "Focusing" else "Focus",
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
                            tint = InkSecondary,
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

    val dismissExpandedPopup = {
        showExpandedPopup = false
        controller.closeStudio()
    }

    if (showExpandedPopup) {
        Dialog(onDismissRequest = dismissExpandedPopup) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Spatial Companion",
                                style = TextStyle(
                                    fontFamily = SFProFontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
                                )
                            )
                        }
                        LiquidGlassIconButton(
                            onClick = dismissExpandedPopup,
                            contentDescription = "Close 3D Studio",
                            size = 36.dp
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = InkPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Large 3D Studio Stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0x18000000) else Color(0x08000000))
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                val assetLoader = WebViewAssetLoader.Builder()
                                    .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(ctx))
                                    .build()

                                WebView(ctx).apply {
                                    contentDescription = "Expanded interactive spatial companion"
                                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    setBackgroundResource(0)

                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        allowFileAccess = false
                                        allowContentAccess = false
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        setSupportZoom(false)
                                        cacheMode = WebSettings.LOAD_NO_CACHE
                                    }

                                    addJavascriptInterface(bridge, "AndroidBridge")
                                    webChromeClient = object : WebChromeClient() {
                                        override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                                            if (message?.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                                                SafeLog.e(
                                                    "SpatialCompanionWeb",
                                                    "${message.message()} at line ${message.lineNumber()}"
                                                )
                                                hasCompanionResponse = true
                                                isUsingProceduralFallback = true
                                                fallbackReason = "The companion preview encountered an error"
                                            }
                                            return true
                                        }
                                    }
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): android.webkit.WebResourceResponse? {
                                            return request?.url?.let(assetLoader::shouldInterceptRequest)
                                        }
                                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
                                            request?.url?.host != "appassets.androidplatform.net"

                                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                            isWebViewLoading = true
                                            hasWebViewError = false
                                            hasCompanionResponse = false
                                            isUsingProceduralFallback = false
                                            fallbackReason = ""
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isWebViewLoading = false
                                            controller.setThemeMode(isDark)
                                            controller.setReducedMotion(reduceMotion)
                                            controller.setRenderPaused(
                                                !lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                                            )
                                        }

                                        override fun onReceivedError(
                                            view: WebView?,
                                            request: WebResourceRequest?,
                                            error: WebResourceError?
                                        ) {
                                            if (request?.isForMainFrame == true) {
                                                isWebViewLoading = false
                                                hasWebViewError = true
                                            } else if (request?.url?.lastPathSegment in setOf("cat.glb", "bundle.js")) {
                                                hasCompanionResponse = true
                                                isUsingProceduralFallback = true
                                                fallbackReason = "The 3D companion model could not be loaded"
                                            }
                                        }

                                        override fun onReceivedHttpError(
                                            view: WebView?,
                                            request: WebResourceRequest?,
                                            errorResponse: android.webkit.WebResourceResponse?
                                        ) {
                                            if ((errorResponse?.statusCode ?: 200) < 400) return
                                            if (request?.isForMainFrame == true) {
                                                isWebViewLoading = false
                                                hasWebViewError = true
                                            } else if (request?.url?.lastPathSegment in setOf("cat.glb", "bundle.js")) {
                                                hasCompanionResponse = true
                                                isUsingProceduralFallback = true
                                                fallbackReason = "The 3D companion model could not be loaded"
                                            }
                                        }
                                    }
                                    loadUrl(SPATIAL_COMPANION_URL)
                                    controller.openStudio(this)
                                }
                            }
                        )

                        if (isWebViewLoading) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0xCC111111) else Color(0xCCFFFFFF))
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text("Loading companion…", fontSize = 11.sp, color = InkSecondary)
                            }
                        } else if (hasWebViewError || isUsingProceduralFallback) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xCC111111) else Color(0xCCFFFFFF))
                                    .padding(start = 12.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (hasWebViewError) "Companion unavailable" else "Using preview fallback",
                                    fontSize = 11.sp,
                                    color = InkSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                LiquidGlassIconButton(
                                    onClick = {
                                        isWebViewLoading = true
                                        hasWebViewError = false
                                        hasCompanionResponse = false
                                        isUsingProceduralFallback = false
                                        fallbackReason = ""
                                        controller.webViewRef?.loadUrl(SPATIAL_COMPANION_URL)
                                    },
                                    contentDescription = "Retry spatial companion",
                                    size = 48.dp
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = InkPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Studio Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LiquidGlassButton(
                            onClick = {
                                VibrationHelper.tick(context)
                                SoundManager.playTaskComplete(context)
                                controller.triggerCelebration(TaskPriority.MEDIUM, 1)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = InkPrimary, modifier = Modifier.size(15.dp)) }
                        ) {
                            Text("Pet", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary))
                        }

                        LiquidGlassButton(
                            onClick = {
                                HapticManager.taskCompleted(context)
                                SoundManager.playTaskComplete(context)
                                controller.triggerCelebration(TaskPriority.HIGH, 7)
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = InkPrimary, modifier = Modifier.size(15.dp)) }
                        ) {
                            Text("Celebrate", style = TextStyle(fontFamily = SFProFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkPrimary))
                        }
                    }
                }
            }
        }
    }
}
