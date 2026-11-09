package com.example.ui.components

import com.kyant.backdrop.Backdrop
import com.example.ui.glass.LiquidGlassBackdrop
import com.example.ui.glass.LiquidGlassHighlight
import com.example.ui.glass.LocalHazeState
import com.example.ui.glass.liquidGlassRim
import dev.chrisbanes.haze.HazeState
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
import com.example.ui.theme.SFProFontFamily
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
 * Core Liquid Glass Surface Modifier (Zero Blur).
 * Creates genuine physical glass appearance via layered alpha transparency,
 * overhead caustic dispersion, and crisp specular rim reflections.
 */
fun Modifier.liquidGlassSurface(
  shape: Shape = RoundedCornerShape(20.dp),
  borderWidth: Dp = 0.75.dp,
  isElevated: Boolean = false,
  isDark: Boolean = true,
  topFill: Color = LiquidGlassCardTopDark,
  bottomFill: Color = LiquidGlassCardBottomDark,
  specularBorderTop: Color = LiquidGlassBorderSpecularTop,
  specularBorderMid: Color = LiquidGlassBorderSpecularMid,
  specularBorderBottom: Color = LiquidGlassBorderSpecularBottom
): Modifier = this
  .clip(shape)
  .drawBehind {
    drawRect(color = lerp(topFill, bottomFill, 0.5f))
  }
  .liquidGlassRim(shape, isDark, borderWidth)

/**
 * Curved Optical Glass Lens Modifier (Zero Blur).
 * Specifically engineered for moving selection indicators (Segmented Controls & Nav Dock).
 * Features brighter apex convex highlight, side hairlines, and optical depth.
 */
fun Modifier.liquidGlassLens(
  shape: Shape = RoundedCornerShape(12.dp),
  borderWidth: Dp = 0.75.dp,
  isDark: Boolean = true
): Modifier = this
  .clip(shape)
  .drawBehind {
    drawRect(color = if (isDark) Color(0x261D1D1D) else Color(0xA8FFFFFF))
  }
  .liquidGlassRim(shape, isDark, borderWidth)

/**
 * Capsule Glass Modifier (Zero Blur).
 */
fun Modifier.liquidGlassCapsule(
  shape: Shape = RoundedCornerShape(20.dp),
  borderWidth: Dp = 0.75.dp,
  isDark: Boolean = true
): Modifier = this.liquidGlassSurface(
  shape = shape,
  borderWidth = borderWidth,
  isDark = isDark,
  topFill = if (isDark) LiquidGlassCapsuleTopDark else LightSurfaceElevated,
  bottomFill = if (isDark) LiquidGlassCapsuleBottomDark else LightSurfaceCard,
  specularBorderTop = if (isDark) LiquidGlassBorderSpecularTop else Color(0xB3FFFFFF),
  specularBorderMid = if (isDark) LiquidGlassBorderSpecularMid else LightHairline,
  specularBorderBottom = if (isDark) LiquidGlassBorderSpecularBottom else LightHairline.copy(alpha = 0.65f)
)

/**
 * A glass layer built from the upstream Backdrop renderer. The Haze scene is
 * recorded into its own backdrop first; the refractive layer is drawn as a
 * sibling so it never samples itself.
 */
@Composable
private fun UpstreamLiquidGlassSurface(
  modifier: Modifier = Modifier,
  shape: Shape,
  isDark: Boolean,
  hazeState: HazeState?,
  borderWidth: Dp,
  fallbackColor: Color,
  content: @Composable BoxScope.() -> Unit
) {
  val surfaceBackdrop = rememberLayerBackdrop()
  Box(modifier = modifier.clip(shape)) {
    Box(
      modifier = Modifier
        .matchParentSize()
        .layerBackdrop(surfaceBackdrop)
    ) {
      if (hazeState != null) {
        LiquidGlassBackdrop(
          hazeState = hazeState,
          shape = shape,
          isDark = isDark,
          modifier = Modifier.matchParentSize()
        )
      } else {
        Box(Modifier.fillMaxSize().background(fallbackColor))
      }
    }
    Box(
      modifier = Modifier
        .matchParentSize()
        .drawBackdrop(
          backdrop = surfaceBackdrop,
          shape = { shape },
          effects = {
            vibrancy()
            blur(8f.dp.toPx())
            lens(22f.dp.toPx(), 26f.dp.toPx(), chromaticAberration = false)
          },
          highlight = { Highlight.Default.copy(alpha = 0.24f) },
          innerShadow = { InnerShadow(radius = 7.dp, alpha = 0.13f) },
          onDrawSurface = {
            drawRect(
              if (isDark) Color.White.copy(alpha = 0.055f)
              else Color.White.copy(alpha = 0.16f)
            )
          }
        )
    )
    Box(
      Modifier
        .matchParentSize()
        .liquidGlassRim(shape, isDark, borderWidth)
    )
    Box(Modifier.padding(borderWidth), content = content)
  }
}

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

  val dragProgress by animateFloatAsState(
    targetValue = if (!reduceMotion && (isDragging || isPressed)) 1f else 0f,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
    label = "segDragProgress"
  )

  val trackColor = if (isDark) Color.White.copy(alpha = 0.085f) else Color.Black.copy(alpha = 0.065f)
  val selectedColor = if (isDark) Color.White.copy(alpha = 0.075f) else Color.White.copy(alpha = 0.64f)

  BoxWithConstraints(
    modifier = modifier.fillMaxWidth().height(48.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    val sideInset = 3.dp
    val indicatorWidth = (maxWidth - sideInset * 2) / totalSlots
    val indicatorWidthPx = with(density) { indicatorWidth.toPx() }
    val sideInsetPx = with(density) { sideInset.toPx() }
    val indicatorHeightDp = (42f + 10f * dragProgress).dp

    // 1. Glass track. The layer backdrop records the blurred screen behind the
    // control so the moving lens below refracts real content, not a flat tint.
    Box(
      Modifier
        .matchParentSize()
        .layerBackdrop(trackBackdrop)
        .clip(controlShape)
    ) {
      if (hazeState != null) {
        LiquidGlassBackdrop(
          hazeState = hazeState,
          shape = controlShape,
          isDark = isDark,
          modifier = Modifier.matchParentSize()
        )
      } else {
        Box(Modifier.fillMaxSize().background(trackColor))
      }
    }

    // 2. Active Pill Lens Indicator (Pops out symmetrically during drag)
    Box(
      Modifier
        .padding(horizontal = sideInset)
        .height(indicatorHeightDp)
        .width(indicatorWidth)
        .graphicsLayer {
          translationX = indicatorWidthPx * indicatorPos.value
          scaleX = 1f + 0.035f * dragProgress
          scaleY = 1f + 0.065f * dragProgress
          clip = false
        }
        .drawBackdrop(
          backdrop = trackBackdrop,
          shape = { controlShape },
          effects = {
            vibrancy()
            lens(11f.dp.toPx(), 21f.dp.toPx(), chromaticAberration = false)
          },
          highlight = { Highlight.Default.copy(alpha = 0.22f + 0.72f * dragProgress) },
          innerShadow = { InnerShadow(radius = 4.dp, alpha = 0.14f) },
          onDrawSurface = { drawRect(selectedColor) }
        )
    )

    // 3. Tab Items & Pointer Input (supports tap and horizontal drag)
    Row(
      Modifier
        .fillMaxSize()
        .padding(horizontal = sideInset, vertical = 3.dp)
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
          Text(
            text = item,
            color = if (selected) com.example.ui.theme.InkPrimary else com.example.ui.theme.InkSecondary,
            fontFamily = SFProFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
          )
        }
      }
    }
    Box(Modifier.matchParentSize().border(0.8.dp, if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.12f), controlShape))
  }
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
  LiquidGlassSurface(
    modifier = modifier.size(size.coerceAtLeast(48.dp)),
    shape = shape,
    borderWidth = 0.75.dp,
    onClick = if (enabled) onClick else null
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
  hazeState: HazeState? = null
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

  // Sync indicator when not dragging
  LaunchedEffect(selectedTab) {
    if (!isDragging) {
      val target = selectedTab.coerceIn(0, totalSlots - 1).toFloat()
      if (reduceMotion) indicatorPos.snapTo(target) else indicatorPos.animateTo(
        targetValue = target,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 480f)
      )
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

  val dragProgress by animateFloatAsState(
    targetValue = if (!reduceMotion && (isDragging || isPressed)) 1f else 0f,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
    label = "dockDragProgress"
  )

  val trackColor = if (isDark) Color.White.copy(alpha = 0.085f) else Color.Black.copy(alpha = 0.065f)
  val selectedColor = if (isDark) Color.White.copy(alpha = 0.075f) else Color.White.copy(alpha = 0.64f)

  BoxWithConstraints(
    modifier = modifier.fillMaxWidth().height(48.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    val sideInset = 3.dp
    val indicatorWidth = (maxWidth - sideInset * 2) / totalSlots
    val indicatorWidthPx = with(density) { indicatorWidth.toPx() }
    val sideInsetPx = with(density) { sideInset.toPx() }
    val indicatorHeightDp = (42f + 10f * dragProgress).dp

    // 1. Live Layer Backdrop Glass Track (1:1 identical to LiquidGlassSegmentedControl)
    Box(
      Modifier
        .matchParentSize()
        .layerBackdrop(trackBackdrop)
        .clip(controlShape)
    ) {
      if (resolvedHaze != null) {
        LiquidGlassBackdrop(
          hazeState = resolvedHaze,
          shape = controlShape,
          isDark = isDark,
          modifier = Modifier.matchParentSize()
        )
      } else {
        Box(Modifier.fillMaxSize().background(trackColor))
      }
      Box(Modifier.matchParentSize().background(trackColor))
    }

    // 2. Active Pill Lens Indicator (Pops out symmetrically during drag)
    Box(
      Modifier
        .padding(horizontal = sideInset)
        .height(indicatorHeightDp)
        .width(indicatorWidth)
        .graphicsLayer {
          translationX = indicatorWidthPx * indicatorPos.value
          scaleX = 1f + 0.035f * dragProgress
          scaleY = 1f + 0.065f * dragProgress
          clip = false
        }
        .drawBackdrop(
          backdrop = trackBackdrop,
          shape = { controlShape },
          effects = {
            vibrancy()
            blur(3f.dp.toPx())
            lens(11f.dp.toPx(), 21f.dp.toPx(), chromaticAberration = dragProgress > 0.1f)
          },
          highlight = { Highlight.Default.copy(alpha = 0.22f + 0.72f * dragProgress) },
          innerShadow = { InnerShadow(radius = 4.dp, alpha = 0.14f) },
          onDrawSurface = { drawRect(selectedColor) }
        )
    )

    // 3. Tab Items & Pointer Input (supports tap and horizontal drag)
    Row(
      Modifier
        .fillMaxSize()
        .padding(horizontal = sideInset, vertical = 3.dp)
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
              // Tap selection
              val tapSlot = ((down.position.x - sideInsetPx) / indicatorWidthPx).toInt().coerceIn(0, totalSlots - 1)
              onDestinationSelected(activeDestinations[tapSlot])
              VibrationHelper.tick(context)
              com.example.util.SoundManager.playSwipeNav(context)
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
            onDestinationSelected(destination)
          },
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 4. Subtle Specular Rim Border
    Box(Modifier.matchParentSize().border(0.8.dp, if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.12f), controlShape))
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
  val selectedColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.75f)
  val trackColor = if (isDark) Color.White.copy(alpha = 0.095f) else Color.White.copy(alpha = 0.88f)

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
    ) {
      if (hazeState != null) {
        LiquidGlassBackdrop(
          hazeState = hazeState,
          shape = CircleShape,
          isDark = isDark,
          modifier = Modifier.matchParentSize()
        )
      } else {
        Box(Modifier.fillMaxSize().background(trackColor))
      }
      Box(Modifier.matchParentSize().background(trackColor))
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
          highlight = { Highlight.Default.copy(alpha = 0.30f) },
          innerShadow = { InnerShadow(radius = 5.dp, alpha = 0.16f) },
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
      tint = if (isDark) Color.White else com.example.ui.theme.CocaNoir,
      modifier = Modifier
        .size(24.dp)
        .graphicsLayer(rotationZ = rotation)
    )
  }
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
  val selectedColor = if (isDark) Color.White else com.example.ui.theme.CocaNoir
  val unselectedColor = if (isDark) Color.White.copy(alpha = 0.50f) else Color(0xFF6B6E76)

  val iconColor by animateColorAsState(
    targetValue = if (isSelected) selectedColor else unselectedColor,
    animationSpec = if (reduceMotion) snap() else tween(durationMillis = 180),
    label = "navIconColor"
  )
  val iconScale by animateFloatAsState(
    targetValue = if (isSelected && !reduceMotion) 1.05f else 1.0f,
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
 * 7. LiquidGlassFloatingActionButton
 * Independent 54dp floating circular Liquid Glass button (Image 2 style).
 */
@Composable
fun LiquidGlassFloatingActionButton(
  onClick: () -> Unit,
  isOpen: Boolean,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isDark = LocalAppColors.current.isDark
  val fabRotation by animateFloatAsState(
    targetValue = if (isOpen) 45f else 0f,
    animationSpec = spring(dampingRatio = 0.60f, stiffness = 450f),
    label = "fabRotation"
  )

  Box(
    modifier = modifier
      .size(54.dp)
      .graphicsLayer {
        shadowElevation = 8.dp.toPx()
        spotShadowColor = if (isDark) Color.Black.copy(alpha = 0.50f) else com.example.ui.theme.CocaNoir.copy(alpha = 0.22f)
        ambientShadowColor = if (isDark) Color.Black.copy(alpha = 0.20f) else com.example.ui.theme.CocaNoir.copy(alpha = 0.08f)
        shape = CircleShape
        clip = true
      }
      .background(if (isDark) Color.White.copy(alpha = 0.085f) else Color.White.copy(alpha = 0.85f))
      .border(
        0.75.dp,
        if (isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.06f),
        CircleShape
      )
      .liquidGlassRim(CircleShape, isDark, 0.75.dp)
      .bounceClick(shine = false) {
        VibrationHelper.click(context)
        com.example.util.SoundManager.playButtonClick(context)
        onClick()
      }
      .testTag("nav_center_fab"),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = Icons.Default.Add,
      contentDescription = if (isOpen) "Close create sheet" else "Quick Create",
      tint = if (isDark) Color.White else com.example.ui.theme.CocaNoir,
      modifier = Modifier
        .size(26.dp)
        .graphicsLayer(rotationZ = fabRotation)
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
        fontFamily = SFProFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = com.example.ui.theme.InkPrimary
      )
    )
  }
}

