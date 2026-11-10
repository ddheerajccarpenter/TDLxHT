package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.graphics.asComposeRenderEffect
import com.example.ui.glass.PILL_LENS_SHADER

import com.kyant.backdrop.Backdrop
import com.example.ui.glass.LiquidGlassBackdrop
import com.example.ui.glass.LiquidGlassHighlight
import com.example.ui.glass.LocalHazeState
import com.example.ui.glass.liquidGlassRim
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppDestination
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.StrongFontFamily
import com.example.ui.theme.LiquidGlassBorderSpecularBottom
import com.example.ui.theme.LiquidGlassBorderSpecularMid
import com.example.ui.theme.LiquidGlassBorderSpecularTop
import com.example.ui.theme.LiquidGlassBorderSubtle
import com.example.ui.theme.LiquidGlassButtonResting
import com.example.ui.theme.LiquidGlassCapsuleBottomDark
import com.example.ui.theme.LiquidGlassCapsuleTopDark
import com.example.ui.theme.LiquidGlassCardBottomDark
import com.example.ui.theme.LiquidGlassCardTopDark
import com.example.ui.theme.LightHairline
import com.example.ui.theme.LightSurfaceCard
import com.example.ui.theme.LightSurfaceElevated
import com.example.ui.theme.LiquidGlassLensBottomDark
import com.example.ui.theme.LiquidGlassLensMidDark
import com.example.ui.theme.LiquidGlassLensTopDark
import com.example.util.VibrationHelper
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// =========================================================================
// SHARED LIQUID GLASS COMPONENTS
//
// Shared glass surfaces use Kyant0 Backdrop for refraction, lensing, and crisp
// highlights. Haze supplies a live blurred scene where the screen exposes one.
// =========================================================================

/**
 * 1. LiquidGlassCard
 * Large smoked glass panel for Today's Momentum, Time Blocks, and Card sections.
 * Supports optional Haze backdrop blur with graceful fallback.
 */
@Composable
fun LiquidGlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(20.dp),
  cornerRadius: Dp? = null,
  backgroundColor: Color? = null,
  borderColor: Color? = null,
  borderWidth: Dp = 0.75.dp,
  elevation: Dp = 0.dp,
  hazeState: HazeState? = null,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val resolvedShape = if (cornerRadius != null) RoundedCornerShape(cornerRadius) else shape
  LiquidGlassSurface(
    modifier = modifier,
    shape = resolvedShape,
    borderWidth = borderWidth,
    backgroundColor = backgroundColor,
    borderColor = borderColor,
    elevation = elevation,
    hazeState = hazeState,
    onClick = onClick,
    content = content
  )
}

/**
 * Shared Backdrop-backed material for cards, controls, sheets and grouped rows.
 * The glass is rendered behind its content so labels and icons remain crisp.
 * The specular rim is owned by [LiquidGlassHighlight] and drawn exactly once.
 */
@Composable
fun LiquidGlassSurface(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(20.dp),
  borderWidth: Dp = 0.75.dp,
  backgroundColor: Color? = null,
  borderColor: Color? = null,
  elevation: Dp = 0.dp,
  hazeState: HazeState? = null,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val isDark = LocalAppColors.current.isDark
  val clickModifier = if (onClick != null) {
    Modifier.bounceClick(shine = true, onClick = onClick)
  } else Modifier
  val resolvedHaze = hazeState ?: LocalHazeState.current
  val fallbackColor = if (isDark) LiquidGlassCardTopDark else LightSurfaceElevated
  val defaultBorder = com.example.ui.theme.CardBorder
  // Accent washes (selected rows, highlighted cards) are translucent by design.
  // Legacy opaque fills are superseded by the glass body and must not block the backdrop.
  val accentWash = backgroundColor?.takeIf {
    it != Color.Unspecified && it.alpha in 0.01f..0.35f
  }
  val accentBorder = borderColor?.takeIf {
    it != Color.Unspecified && it != defaultBorder
  }
  val shadowModifier = if (elevation > 0.dp) {
    Modifier.shadow(
      elevation = elevation,
      shape = shape,
      clip = false,
      ambientColor = Color.Black.copy(alpha = if (isDark) 0.18f else 0.06f),
      spotColor = Color.Black.copy(alpha = if (isDark) 0.22f else 0.08f)
    )
  } else Modifier

  Box(modifier = modifier.then(clickModifier).then(shadowModifier).clip(shape)) {
    if (resolvedHaze != null) {
      LiquidGlassBackdrop(
        hazeState = resolvedHaze,
        shape = shape,
        isDark = isDark,
        modifier = Modifier.matchParentSize()
      )
    } else {
      Box(
        modifier = Modifier
          .matchParentSize()
          .background(fallbackColor)
      )
    }
    if (accentWash != null) {
      Box(Modifier.matchParentSize().background(accentWash))
    }
    LiquidGlassHighlight(
      shape = shape,
      isDark = isDark,
      borderWidth = borderWidth,
      modifier = Modifier.matchParentSize()
    )
    if (accentBorder != null) {
      Box(Modifier.matchParentSize().border(borderWidth, accentBorder, shape))
    }
    Box(
      modifier = Modifier.padding(borderWidth),
      content = content
    )
  }
}

/**
 * 2. LiquidGlassCapsule
 * Unified transparent glass capsule for metric groups and segmented controls.
 * Supports optional Haze backdrop blur with graceful fallback.
 */
@Composable
fun LiquidGlassCapsule(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(20.dp),
  borderWidth: Dp = 0.75.dp,
  hazeState: HazeState? = null,
  content: @Composable BoxScope.() -> Unit
) {
  LiquidGlassSurface(
    modifier = modifier,
    shape = shape,
    hazeState = hazeState ?: LocalHazeState.current,
    borderWidth = borderWidth,
    content = content
  )
}

/**
 * 3. LiquidGlassSegmentedControl
 * Segmented glass pill using Kyant0's backdrop, lens and highlight renderer.
 */
@Composable
fun LiquidGlassSegmentedControl(
  items: List<String>,
  selectedIndex: Int,
  onItemSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val isDark = LocalAppColors.current.isDark
  val reduceMotion = remember { isReducedMotionEnabled(context) }
  val coroutineScope = rememberCoroutineScope()
  if (items.isEmpty()) return

  val hazeState = LocalHazeState.current
  val controlShape = RoundedCornerShape(percent = 50)
  val trackBackdrop = rememberLayerBackdrop()
  val totalSlots = items.size

  // Indicator animated position
  val indicatorPos = remember { Animatable(selectedIndex.toFloat()) }
  var isDragging by remember { mutableStateOf(false) }
  var isPressed by remember { mutableStateOf(false) }
  var dragTargetPos by remember { mutableFloatStateOf(selectedIndex.toFloat()) }
  var lastHapticIndex by remember { mutableIntStateOf(selectedIndex) }

  // Sync when selectedIndex changes externally and not dragging
  LaunchedEffect(selectedIndex) {
    if (!isDragging) {
      if (reduceMotion) {
        indicatorPos.snapTo(selectedIndex.toFloat())
      } else {
        indicatorPos.animateTo(
          targetValue = selectedIndex.toFloat(),
          animationSpec = spring(dampingRatio = 0.72f, stiffness = 480f)
        )
      }
    }
  }

  // 120Hz smooth zero-allocation tracking loop during drag
  LaunchedEffect(isDragging) {
    if (!isDragging) return@LaunchedEffect
    var lastNanos = withFrameNanos { it }
    while (isDragging) {
      var nextPos = indicatorPos.value
      withFrameNanos { now ->
        val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
        lastNanos = now
        val factor = 1f - kotlin.math.exp(-dt * 38f)
        nextPos = indicatorPos.value + (dragTargetPos - indicatorPos.value) * factor
      }
      indicatorPos.snapTo(nextPos)
    }
  }


  val actionProgress by animateFloatAsState(
    targetValue = if (!reduceMotion && (isDragging || isPressed)) 1f else 0f,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
    label = "controlActionProgress"
  )

  var jiggleOffset by remember { mutableFloatStateOf(0f) }
  LaunchedEffect(isDragging, isPressed) {
    if (!isDragging && !isPressed) {
      jiggleOffset = 0f
      return@LaunchedEffect
    }
    var lastNanos = withFrameNanos { it }
    var elapsed = 0f
    while (isDragging || isPressed) {
      withFrameNanos { now ->
        val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
        lastNanos = now
        elapsed += dt
        jiggleOffset = kotlin.math.sin(elapsed * 28f) * 1.5f * actionProgress
      }
    }
    jiggleOffset = 0f
  }

  val trackBrush = if (isDark) {
    Brush.verticalGradient(listOf(Color(0x38222630), Color(0x2814161C)))
  } else {
    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.65f), Color.White.copy(alpha = 0.40f)))
  }
  val selectedColor = if (isDark) {
    Color.White.copy(alpha = 0.14f + 0.08f * actionProgress)
  } else {
    Color.White.copy(alpha = 0.88f + 0.10f * actionProgress)
  }
  val dynamicStretch = ((dragTargetPos - indicatorPos.value) * 0.08f).coerceIn(-0.16f, 0.16f)
  val liftScale = 1f + 0.12f * actionProgress
  val pillHeight = 40.dp + 10.4.dp * actionProgress

  val lensShader = remember {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      try { RuntimeShader(PILL_LENS_SHADER) } catch (_: Throwable) { null }
    } else null
  }

  BoxWithConstraints(
    modifier = modifier.fillMaxWidth().height(56.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    val sideInset = 4.dp
    val indicatorWidth = (maxWidth - sideInset * 2) / totalSlots
    val indicatorWidthPx = with(density) { indicatorWidth.toPx() }
    val sideInsetPx = with(density) { sideInset.toPx() }

    // 1. Live Layer Backdrop Glass Track (height 48.dp, centered inside 56.dp)
    Box(
      Modifier
        .fillMaxWidth()
        .height(48.dp)
        .align(Alignment.Center)
        .layerBackdrop(trackBackdrop)
        .clip(controlShape)
    ) {
      Box(Modifier.fillMaxSize().background(trackBrush))
      Box(Modifier.matchParentSize().liquidGlassRim(controlShape, isDark, 0.75.dp))
    }

    // 2. Active Pill Lens Indicator: normal size (40dp) inside track at rest, expanding to 50.4dp pop-out when touched
    Box(
      Modifier
        .align(Alignment.CenterStart)
        .padding(horizontal = sideInset)
        .height(pillHeight)
        .width(indicatorWidth)
        .graphicsLayer {
          this.translationX = indicatorWidthPx * indicatorPos.value + jiggleOffset
          this.scaleX = liftScale + dynamicStretch
          this.scaleY = liftScale
          this.shape = controlShape
          this.clip = true
          this.shadowElevation = (if (isDark) 8.dp else 4.dp).toPx() * actionProgress
          this.spotShadowColor = if (isDark) Color.Black.copy(alpha = 0.50f) else Color.Black.copy(alpha = 0.10f)
          this.ambientShadowColor = if (isDark) Color.Black.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.04f)
        }
        .drawBackdrop(
          backdrop = trackBackdrop,
          shape = { controlShape },
          effects = {
            vibrancy()
            lens((11f * actionProgress).dp.toPx(), (21f * actionProgress).dp.toPx(), chromaticAberration = false)
          },
          highlight = { Highlight.Default.copy(alpha = if (isDark) (0.25f + 0.40f * actionProgress) else (0.20f + 0.35f * actionProgress)) },
          innerShadow = {
            if (isDark) {
              InnerShadow(radius = 3.dp, alpha = 0.22f * actionProgress)
            } else {
              InnerShadow(radius = 1.dp, alpha = 0.04f * actionProgress)
            }
          },
          layerBlock = {},
          onDrawSurface = {
            drawRoundRect(
              color = selectedColor,
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillHeight.toPx() / 2f)
            )
          }
        )
    )

    // 3. Tab Items & Pointer Input with AGSL chromatic dispersion on API 33+
    val lensModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && lensShader != null) {
      Modifier.graphicsLayer {
        val centerPx = sideInsetPx + indicatorWidthPx * indicatorPos.value + indicatorWidthPx / 2f + jiggleOffset
        val halfWPx = (indicatorWidthPx / 2f) * (liftScale + dynamicStretch)
        val halfHPx = (pillHeight.toPx() / 2f) * liftScale
        lensShader.setFloatUniform("uCenter", centerPx, size.height / 2f)
        lensShader.setFloatUniform("uHalf", halfWPx, halfHPx)
        lensShader.setFloatUniform("uZoom", 0.02f + 0.18f * actionProgress)
        lensShader.setFloatUniform("uChroma", actionProgress)
        renderEffect = RenderEffect
          .createRuntimeShaderEffect(lensShader, "content")
          .asComposeRenderEffect()
      }
    } else Modifier

    Row(
      Modifier
        .fillMaxWidth()
        .height(48.dp)
        .align(Alignment.Center)
        .then(lensModifier)
        .padding(horizontal = sideInset)
        .pointerInput(totalSlots, indicatorWidthPx) {
          if (indicatorWidthPx <= 0f) return@pointerInput
          val touchSlop = viewConfiguration.touchSlop
          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            isPressed = true
            var dragStarted = false
            var totalDx = 0f
            var totalDy = 0f

            do {
              val event = awaitPointerEvent()
              val change = event.changes.firstOrNull { it.id == down.id } ?: break

              val dx = change.position.x - change.previousPosition.x
              val dy = change.position.y - change.previousPosition.y
              totalDx += dx
              totalDy += dy

              if (!dragStarted) {
                if (abs(totalDx) > touchSlop && abs(totalDx) > abs(totalDy)) {
                  dragStarted = true
                  isDragging = true
                  val slotPos = ((change.position.x - sideInsetPx) / indicatorWidthPx - 0.5f).coerceIn(0f, (totalSlots - 1).toFloat())
                  dragTargetPos = slotPos
                  change.consume()
                }
              }

              if (dragStarted) {
                change.consume()
                val slotPos = ((change.position.x - sideInsetPx) / indicatorWidthPx - 0.5f).coerceIn(0f, (totalSlots - 1).toFloat())
                dragTargetPos = slotPos

                val currentSlot = slotPos.roundToInt().coerceIn(0, totalSlots - 1)
                if (currentSlot != lastHapticIndex) {
                  lastHapticIndex = currentSlot
                  VibrationHelper.tick(context)
                  com.example.util.SoundManager.playSwipeNav(context)
                }
              }
            } while (event.changes.any { it.pressed })

            isPressed = false
            if (dragStarted) {
              isDragging = false
              val finalSlot = indicatorPos.value.roundToInt().coerceIn(0, totalSlots - 1)
              onItemSelected(finalSlot)
              VibrationHelper.tick(context)
              com.example.util.SoundManager.playSwipeNav(context)
              coroutineScope.launch {
                if (reduceMotion) indicatorPos.snapTo(finalSlot.toFloat()) else indicatorPos.animateTo(
                  finalSlot.toFloat(),
                  spring(dampingRatio = 0.72f, stiffness = 480f)
                )
              }
            } else {
              // Tap selection
              val tapSlot = ((down.position.x - sideInsetPx) / indicatorWidthPx).toInt().coerceIn(0, totalSlots - 1)
              onItemSelected(tapSlot)
              VibrationHelper.tick(context)
              com.example.util.SoundManager.playSwipeNav(context)
            }
          }
        }
    ) {
      items.forEachIndexed { index, item ->
        val selected = index == selectedIndex
        Box(
          Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(controlShape)
            .semantics {
              role = Role.Tab
              this.selected = selected
              contentDescription = item
              onClick {
                onItemSelected(index)
                true
              }
            },
          contentAlignment = Alignment.Center
        ) {
          val activeTextColor = if (isDark) Color.White else Color(0xFF1C1C1E)
          val inactiveTextColor = if (isDark) Color.White.copy(alpha = 0.50f) else Color(0xFF8E8E93)
          Text(
            text = item,
            color = if (selected) activeTextColor else inactiveTextColor,
            fontFamily = StrongFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
          )
        }
      }
    }
  }
}

/**
 * GlassSegmented matching @zakisheriff/liquid-glass specification.
 * <GlassSegmented options={['Chat', 'Work', 'Study']} value={mode} onChange={setMode} aria-label="Mode" />
 */
@Composable
fun GlassSegmented(
  options: List<String>,
  value: Int,
  onChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
  contentDescription: String? = "Segmented control"
) {
  LiquidGlassSegmentedControl(
    items = options,
    selectedIndex = value,
    onItemSelected = onChange,
    modifier = modifier.semantics {
      if (contentDescription != null) {
        this.contentDescription = contentDescription
      }
    }
  )
}

/**
 * 4. LiquidGlassButton
 * Translucent text & icon control with hairline specular border and elastic feedback.
 */
@Composable
fun LiquidGlassButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(12.dp),
  enabled: Boolean = true,
  icon: (@Composable () -> Unit)? = null,
  content: @Composable () -> Unit
) {
  LiquidGlassSurface(
    modifier = modifier.defaultMinSize(minHeight = 34.dp),
    shape = shape,
    borderWidth = 0.75.dp,
    onClick = if (enabled) onClick else null
  ) {
    Box(Modifier.wrapContentWidth().padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (icon != null) {
          icon()
        }
        content()
      }
    }
  }
}

/**
 * 5. LiquidGlassIconButton
 * Compact circular or squircle translucent glass button (Header, Search, Settings, Close).
 */
@Composable
fun LiquidGlassIconButton(
  onClick: () -> Unit,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  size: Dp = 48.dp,
  enabled: Boolean = true,
  shape: Shape = CircleShape,
  icon: @Composable () -> Unit
) {
  // 48dp touch target with a smaller visible glass circle (designglass §05).
  val visualSize = size.coerceIn(32.dp, 48.dp)
  Box(
    modifier = modifier
      .size(48.dp)
      .then(if (enabled) Modifier.bounceClick { onClick() } else Modifier),
    contentAlignment = Alignment.Center
  ) {
    LiquidGlassSurface(
      modifier = Modifier.size(visualSize),
      shape = shape,
      borderWidth = 0.75.dp
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .semantics {
            if (contentDescription != null) this.contentDescription = contentDescription
            role = Role.Button
          },
        contentAlignment = Alignment.Center
      ) { icon() }
    }
  }
}

/**
 * 6. LiquidGlassNavigationDock
 * Streamlined 4-destination Apple-style Liquid Glass navigation dock.
 * Matches LiquidGlassSegmentedControl 1:1 in height (48dp), padding (16dp), and dark smoked glass material.
 * Features 120Hz gesture tracking with dynamic pop-out lens physics matching iOS 26 Liquid Glass.
 */
@Composable
fun LiquidGlassNavigationDock(
  destinations: List<AppDestination>,
  selectedTab: Int,
  onDestinationSelected: (AppDestination) -> Unit,
  modifier: Modifier = Modifier,
  hazeState: HazeState? = null,
  externalPageOffset: Float? = null,
  onDragOffset: ((fraction: Float) -> Unit)? = null,
  onDragReleased: ((targetIndex: Int) -> Unit)? = null
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val colors = LocalAppColors.current
  val isDark = colors.isDark
  val reduceMotion = remember { isReducedMotionEnabled(context) }
  val coroutineScope = rememberCoroutineScope()
  val activeDestinations = destinations.take(4)
  val totalSlots = activeDestinations.size.coerceAtLeast(1)

  val controlShape = RoundedCornerShape(percent = 50)
  val trackBackdrop = rememberLayerBackdrop()
  val resolvedHaze = hazeState ?: LocalHazeState.current

  // Indicator animated position
  val indicatorPos = remember { Animatable(selectedTab.coerceIn(0, totalSlots - 1).toFloat()) }
  var isDragging by remember { mutableStateOf(false) }
  var isPressed by remember { mutableStateOf(false) }
  var dragTargetPos by remember { mutableFloatStateOf(selectedTab.toFloat()) }
  var lastHapticIndex by remember { mutableIntStateOf(selectedTab) }

  // Sync indicator when not dragging: smoothly track external page offset (from pager) or selectedTab
  LaunchedEffect(externalPageOffset, selectedTab, isDragging) {
    if (!isDragging) {
      if (externalPageOffset != null) {
        indicatorPos.snapTo(externalPageOffset.coerceIn(0f, (totalSlots - 1).toFloat()))
      } else {
        val target = selectedTab.coerceIn(0, totalSlots - 1).toFloat()
        if (reduceMotion) indicatorPos.snapTo(target) else indicatorPos.animateTo(
          targetValue = target,
          animationSpec = spring(dampingRatio = 0.72f, stiffness = 480f)
        )
      }
    }
  }

  // 120Hz smooth tracking loop during drag
  LaunchedEffect(isDragging) {
    if (!isDragging) return@LaunchedEffect
    var lastNanos = withFrameNanos { it }
    while (isDragging) {
      var nextPos = indicatorPos.value
      withFrameNanos { now ->
        val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
        lastNanos = now
        val factor = 1f - kotlin.math.exp(-dt * 38f)
        nextPos = indicatorPos.value + (dragTargetPos - indicatorPos.value) * factor
      }
      indicatorPos.snapTo(nextPos)
    }
  }

  // "Comes into action" when clicked, held, or tapped: expands from normal resting size to 1.2dp pop-out overhang & frosted fill
  val actionProgress by animateFloatAsState(
    targetValue = if (!reduceMotion && (isDragging || isPressed)) 1f else 0f,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
    label = "dockActionProgress"
  )
  val tapPopAnim = remember { Animatable(0f) }
  val combinedActionProgress = if (reduceMotion) 0f else (actionProgress + tapPopAnim.value).coerceIn(0f, 1f)

  var jiggleOffset by remember { mutableFloatStateOf(0f) }
  LaunchedEffect(isDragging, isPressed) {
    if (!isDragging && !isPressed) {
      jiggleOffset = 0f
      return@LaunchedEffect
    }
    var lastNanos = withFrameNanos { it }
    var elapsed = 0f
    while (isDragging || isPressed) {
      withFrameNanos { now ->
        val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
        lastNanos = now
        elapsed += dt
        jiggleOffset = kotlin.math.sin(elapsed * 28f) * 1.5f * actionProgress
      }
    }
    jiggleOffset = 0f
  }

  val trackBrush = if (isDark) {
    Brush.verticalGradient(listOf(Color(0x28222630), Color(0x1814161C)))
  } else {
    // Subtle, low-gradient effect with high translucency matching user request
    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.42f), Color.White.copy(alpha = 0.25f)))
  }
  // Resting: subtle translucent frosted pill. In action: glowing liquid glass fill.
  val selectedColor = if (isDark) {
    Color.White.copy(alpha = 0.08f + 0.14f * combinedActionProgress)
  } else {
    Color.White.copy(alpha = 0.25f + 0.65f * combinedActionProgress)
  }
  val dynamicStretch = ((dragTargetPos - indicatorPos.value) * 0.08f).coerceIn(-0.16f, 0.16f)
  val lengthStretch = dynamicStretch + (tapPopAnim.value * 0.22f) + (combinedActionProgress * 0.12f)
  val liftScale = 1f + 0.12f * combinedActionProgress
  // Height: 40dp at rest (flush inside 48dp track), expanding to 50.4dp in action (1.2dp top/bottom overhang)
  val pillHeight = 40.dp + 10.4.dp * combinedActionProgress

  val lensShader = remember {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      try { RuntimeShader(PILL_LENS_SHADER) } catch (_: Throwable) { null }
    } else null
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(56.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    val sideInset = 4.dp
    val indicatorWidth = (maxWidth - sideInset * 2) / totalSlots
    val indicatorWidthPx = with(density) { indicatorWidth.toPx() }
    val sideInsetPx = with(density) { sideInset.toPx() }

    val trackHazeModifier = if (resolvedHaze != null) {
      Modifier.clip(controlShape).hazeEffect(state = resolvedHaze) {
        blurRadius = 24.dp
      }
    } else Modifier

    // 1. Live Layer Backdrop Glass Track (height 48.dp, centered inside 56.dp)
    Box(
      Modifier
        .fillMaxWidth()
        .height(48.dp)
        .align(Alignment.Center)
        .then(trackHazeModifier)
        .layerBackdrop(trackBackdrop)
        .clip(controlShape)
    ) {
      Box(Modifier.fillMaxSize().background(trackBrush))
      Box(Modifier.matchParentSize().liquidGlassRim(controlShape, isDark, 0.75.dp))
    }

    // 2. Active Pill Lens Indicator:
    // Sits at normal size (40dp) inside the track at rest with subtle frosted glass anchor;
    // Expands to 50.4dp (1.2dp overhang) and fills with glowing liquid glass when touched/held/tapped.
    Box(
      Modifier
        .align(Alignment.CenterStart)
        .padding(horizontal = sideInset)
        .height(pillHeight)
        .width(indicatorWidth)
        .graphicsLayer {
          this.translationX = indicatorWidthPx * indicatorPos.value + jiggleOffset
          this.scaleX = liftScale + lengthStretch
          this.scaleY = liftScale
          this.shape = controlShape
          this.clip = true
          this.shadowElevation = (if (isDark) 8.dp else 5.dp).toPx() * combinedActionProgress
          this.spotShadowColor = if (isDark) Color.Black.copy(alpha = 0.50f) else Color.Black.copy(alpha = 0.12f)
          this.ambientShadowColor = if (isDark) Color.Black.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.05f)
        }
        .drawBackdrop(
          backdrop = trackBackdrop,
          shape = { controlShape },
          effects = {
            vibrancy()
            lens((11f * combinedActionProgress).dp.toPx(), (21f * combinedActionProgress).dp.toPx(), chromaticAberration = false)
          },
          highlight = { Highlight.Default.copy(alpha = if (isDark) (0.12f + 0.38f * combinedActionProgress) else (0.15f + 0.25f * combinedActionProgress)) },
          innerShadow = {
            if (isDark) {
              InnerShadow(radius = 3.dp, alpha = 0.05f + 0.17f * combinedActionProgress)
            } else {
              InnerShadow(radius = 1.dp, alpha = 0.02f + 0.04f * combinedActionProgress)
            }
          },
          layerBlock = {},
          onDrawSurface = {
            drawRoundRect(
              color = selectedColor,
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillHeight.toPx() / 2f)
            )
          }
        )
    )

    // 3. Tab Items & Pointer Input (supports tap and horizontal drag with AGSL lens)
    val lensModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && lensShader != null && combinedActionProgress > 0.01f) {
      Modifier.graphicsLayer {
        val centerPx = sideInsetPx + indicatorWidthPx * indicatorPos.value + indicatorWidthPx / 2f + jiggleOffset
        val halfWPx = (indicatorWidthPx / 2f) * (liftScale + lengthStretch)
        val halfHPx = (pillHeight.toPx() / 2f) * liftScale
        lensShader.setFloatUniform("uCenter", centerPx, size.height / 2f)
        lensShader.setFloatUniform("uHalf", halfWPx, halfHPx)
        lensShader.setFloatUniform("uZoom", 0.02f + 0.18f * combinedActionProgress)
        lensShader.setFloatUniform("uChroma", combinedActionProgress)
        renderEffect = RenderEffect
          .createRuntimeShaderEffect(lensShader, "content")
          .asComposeRenderEffect()
      }
    } else Modifier

    Row(
      Modifier
        .fillMaxWidth()
        .height(48.dp)
        .align(Alignment.Center)
        .then(lensModifier)
        .padding(horizontal = sideInset)
        .pointerInput(totalSlots, indicatorWidthPx) {
          if (indicatorWidthPx <= 0f) return@pointerInput
          val touchSlop = viewConfiguration.touchSlop
          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            isPressed = true
            var dragStarted = false
            var totalDx = 0f
            var totalDy = 0f

            do {
              val event = awaitPointerEvent()
              val change = event.changes.firstOrNull { it.id == down.id } ?: break

              val dx = change.position.x - change.previousPosition.x
              val dy = change.position.y - change.previousPosition.y
              totalDx += dx
              totalDy += dy

              if (!dragStarted) {
                if (abs(totalDx) > touchSlop && abs(totalDx) > abs(totalDy)) {
                  dragStarted = true
                  isDragging = true
                  val slotPos = ((change.position.x - sideInsetPx) / indicatorWidthPx - 0.5f).coerceIn(0f, (totalSlots - 1).toFloat())
                  dragTargetPos = slotPos
                  onDragOffset?.invoke(slotPos)
                  change.consume()
                }
              }

              if (dragStarted) {
                change.consume()
                val slotPos = ((change.position.x - sideInsetPx) / indicatorWidthPx - 0.5f).coerceIn(0f, (totalSlots - 1).toFloat())
                dragTargetPos = slotPos
                onDragOffset?.invoke(slotPos)

                val currentSlot = slotPos.roundToInt().coerceIn(0, totalSlots - 1)
                if (currentSlot != lastHapticIndex) {
                  lastHapticIndex = currentSlot
                  VibrationHelper.tick(context)
                  com.example.util.SoundManager.playSwipeNav(context)
                }
              }
            } while (event.changes.any { it.pressed })

            isPressed = false
            if (dragStarted) {
              isDragging = false
              val finalSlot = indicatorPos.value.roundToInt().coerceIn(0, totalSlots - 1)
              onDragReleased?.invoke(finalSlot)
              onDestinationSelected(activeDestinations[finalSlot])
              VibrationHelper.tick(context)
              com.example.util.SoundManager.playSwipeNav(context)
              coroutineScope.launch {
                if (reduceMotion) indicatorPos.snapTo(finalSlot.toFloat()) else indicatorPos.animateTo(
                  finalSlot.toFloat(),
                  spring(dampingRatio = 0.72f, stiffness = 480f)
                )
              }
            } else {
              // Tap selection: triggers pop animation with 1.2px overhang and 1.5 length stretch
              val tapSlot = ((down.position.x - sideInsetPx) / indicatorWidthPx).toInt().coerceIn(0, totalSlots - 1)
              onDestinationSelected(activeDestinations[tapSlot])
              VibrationHelper.tick(context)
              com.example.util.SoundManager.playSwipeNav(context)
              coroutineScope.launch {
                tapPopAnim.snapTo(1f)
                tapPopAnim.animateTo(
                  targetValue = 0f,
                  animationSpec = spring(dampingRatio = 0.68f, stiffness = 320f)
                )
              }
            }
          }
        }
    ) {
      activeDestinations.forEachIndexed { index, destination ->
        LiquidGlassNavItem(
          destination = destination,
          isSelected = selectedTab == index,
          onClick = {
            com.example.util.SoundManager.playSwipeNav(context)
            VibrationHelper.tick(context)
            coroutineScope.launch {
              tapPopAnim.snapTo(1f)
              tapPopAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.68f, stiffness = 320f)
              )
            }
            onDestinationSelected(destination)
          },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

/**
 * Standalone Floating Action Button for Quick Create with authentic iOS 26 Liquid Glass styling.
 * Floats separated above the navigation bar with an elevated backdrop lens and deep drop shadow.
 */
@Composable
fun FloatingLiquidGlassActionButton(
  isOpen: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isDark = LocalAppColors.current.isDark
  val reduceMotion = remember { isReducedMotionEnabled(context) }
  val rotation by animateFloatAsState(
    targetValue = if (isOpen && !reduceMotion) 45f else 0f,
    animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.60f, stiffness = 450f),
    label = "fabRotation"
  )
  val hazeState = LocalHazeState.current
  val trackBackdrop = rememberLayerBackdrop()
  val selectedColor = if (isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.85f)
  val trackColor = if (isDark) Color(0x38181A20) else Color.Black.copy(alpha = 0.065f)

  Box(
    modifier = modifier
      .size(52.dp)
      .graphicsLayer {
        shadowElevation = 10.dp.toPx()
        spotShadowColor = if (isDark) Color.Black.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.18f)
        ambientShadowColor = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.08f)
        shape = CircleShape
        clip = false
      }
      .bounceClick(
        level = com.example.ui.components.InteractionLevel.MEDIUM,
        shine = false
      ) {
        VibrationHelper.click(context)
        com.example.util.SoundManager.playButtonClick(context)
        onClick()
      }
      .semantics {
        role = Role.Button
        contentDescription = if (isOpen) "Close create sheet" else "Quick Create"
      },
    contentAlignment = Alignment.Center
  ) {
    // Backdrop source layer
    Box(
      modifier = Modifier
        .matchParentSize()
        .layerBackdrop(trackBackdrop)
        .clip(CircleShape)
        .background(trackColor)
    ) {
      Box(Modifier.fillMaxSize().background(trackColor))
    }

    // Refractive lens surface
    Box(
      modifier = Modifier
        .matchParentSize()
        .drawBackdrop(
          backdrop = trackBackdrop,
          shape = { CircleShape },
          effects = {
            vibrancy()
            blur(4f.dp.toPx())
            lens(14f.dp.toPx(), 24f.dp.toPx(), chromaticAberration = false)
          },
          highlight = { Highlight.Default.copy(alpha = if (isDark) 0.45f else 0.35f) },
          innerShadow = {
            if (isDark) {
              InnerShadow(radius = 4.dp, alpha = 0.22f)
            } else {
              InnerShadow(radius = 1.dp, alpha = 0.04f)
            }
          },
          onDrawSurface = { drawRect(selectedColor) }
        )
    )

    // Specular rim
    Box(
      modifier = Modifier
        .matchParentSize()
        .liquidGlassRim(CircleShape, isDark, 0.75.dp)
    )

    Icon(
      imageVector = Icons.Default.Add,
      contentDescription = if (isOpen) "Close create sheet" else "Quick Create",
      tint = if (isDark) Color.White else Color(0xFF1C1C1E),
      modifier = Modifier
        .size(24.dp)
        .graphicsLayer(rotationZ = rotation)
    )
  }
}

/**
 * Backward compatibility wrapper for LiquidGlassFloatingActionButton.
 */
@Composable
fun LiquidGlassFloatingActionButton(
  onClick: () -> Unit,
  isOpen: Boolean,
  modifier: Modifier = Modifier
) {
  FloatingLiquidGlassActionButton(
    isOpen = isOpen,
    onClick = onClick,
    modifier = modifier
  )
}

/**
 * Isolated destination item for LiquidGlassNavigationDock with stable parameters.
 * Active destination features crisp high-contrast icon matching Image 2.
 */
@Composable
fun LiquidGlassNavItem(
  destination: AppDestination,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isDark = LocalAppColors.current.isDark
  val reduceMotion = remember { isReducedMotionEnabled(context) }
  val selectedColor = if (isDark) Color.White else Color(0xFF1C1C1E)
  val unselectedColor = if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF8E8E93)

  val iconColor by animateColorAsState(
    targetValue = if (isSelected) selectedColor else unselectedColor,
    animationSpec = if (reduceMotion) snap() else tween(durationMillis = 180),
    label = "navIconColor"
  )
  val iconScale by animateFloatAsState(
    targetValue = if (isSelected && !reduceMotion) 1.08f else 1.0f,
    animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.7f, stiffness = 420f),
    label = "navIconScale"
  )

  Box(
    modifier = modifier
      .fillMaxHeight()
      .testTag("nav_${destination.name.lowercase()}")
      .semantics(mergeDescendants = true) {
        role = Role.Tab
        selected = isSelected
        contentDescription = destination.label
      },
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = if (isSelected) destination.iconFilled else destination.iconOutlined,
      contentDescription = destination.label,
      tint = iconColor,
      modifier = Modifier
        .size(24.dp)
        .graphicsLayer {
          scaleX = iconScale
          scaleY = iconScale
        }
    )
  }
}

/**
 * 8. LiquidGlassProgressIndicator
 * Clean dual-arc circular progress gauge for Today's Momentum card (Zero Blur).
 */
@Composable
fun LiquidGlassProgressIndicator(
  progress: Float,
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 7.dp,
  trackColor: Color? = null,
  progressColor: Color? = null
) {
  val isDark = LocalAppColors.current.isDark
  val resolvedTrackColor = trackColor ?: if (isDark) Color(0x18FFFFFF) else Color(0x1A171717)
  val resolvedProgressColor = progressColor ?: LocalAppColors.current.oklchPrimary
  val animatedProgress by animateFloatAsState(
    targetValue = progress.coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
    label = "liquidGlassProgress"
  )

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
      val strokePx = strokeWidth.toPx()
      val arcSize = size.minDimension - strokePx
      val topLeft = Offset((size.width - arcSize) / 2f, (size.height - arcSize) / 2f)

      // 1. Layered Background Track Ring
      drawArc(
        color = resolvedTrackColor,
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
      )

      // 2. Active Progress Arc
      if (animatedProgress > 0f) {
        drawArc(
          color = resolvedProgressColor,
          startAngle = -90f,
          sweepAngle = animatedProgress * 360f,
          useCenter = false,
          topLeft = topLeft,
          size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
          style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
      }
    }

    Text(
      text = "${(animatedProgress * 100).toInt()}%",
      style = TextStyle(
        fontFamily = StrongFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = com.example.ui.theme.InkPrimary
      )
    )
  }
}

