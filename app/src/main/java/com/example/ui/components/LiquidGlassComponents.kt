package com.example.ui.components

import com.kyant.backdrop.Backdrop
import com.example.ui.glass.LiquidGlassBackdrop
import com.example.ui.glass.LiquidGlassHighlight
import com.example.ui.glass.LocalHazeState
import com.example.ui.glass.liquidGlassRim
import com.example.ui.glass.drawPrismaticCausticRing
import com.example.ui.glass.prismaticCausticBorder
import com.example.ui.glass.PILL_LENS_SHADER
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
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
            lens(22f.dp.toPx(), 26f.dp.toPx(), chromaticAberration = true)
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
  shape: Shape = RoundedCornerShape(24.dp),
  borderWidth: Dp = 0.75.dp,
  hazeState: HazeState? = null,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  LiquidGlassSurface(
    modifier = modifier,
    shape = shape,
    borderWidth = borderWidth,
    hazeState = hazeState,
    onClick = onClick,
    content = content
  )
}

/**
 * Shared Backdrop-backed material for cards, controls, sheets and grouped rows.
 * The glass is rendered behind its content so labels and icons remain crisp.
 */
@Composable
fun LiquidGlassSurface(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(20.dp),
  borderWidth: Dp = 0.75.dp,
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

  Box(modifier = modifier.then(clickModifier).clip(shape)) {
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
    LiquidGlassHighlight(
      shape = shape,
      isDark = isDark,
      modifier = Modifier.matchParentSize()
    )
    Box(
      modifier = Modifier
        .matchParentSize()
        .liquidGlassRim(shape, isDark, borderWidth)
    )
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
  val isDark = LocalAppColors.current.isDark
  if (items.isEmpty()) return
  val hazeState = LocalHazeState.current
  val controlShape = RoundedCornerShape(percent = 50)
  val trackBackdrop = rememberLayerBackdrop()
  val selectedPosition by animateFloatAsState(
    targetValue = selectedIndex.coerceIn(items.indices).toFloat(),
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 480f),
    label = "segmented-glass-position"
  )
  var isSegmentPressed by remember { mutableStateOf(false) }
  val pressProgress by animateFloatAsState(
    targetValue = if (isSegmentPressed) 1f else 0f,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f),
    label = "segmented-glass-press"
  )
  val trackColor = if (isDark) Color.White.copy(alpha = 0.085f) else Color.Black.copy(alpha = 0.065f)
  val selectedColor = if (isDark) Color.White.copy(alpha = 0.075f) else Color.White.copy(alpha = 0.64f)
  BoxWithConstraints(
    modifier = modifier.fillMaxWidth().height(48.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    val sideInset = 3.dp
    val indicatorWidth = (maxWidth - sideInset * 2) / items.size
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
      Box(Modifier.matchParentSize().background(trackColor))
    }
    Box(
      Modifier
        .padding(horizontal = sideInset)
        .height(42.dp)
        .width(indicatorWidth)
        .graphicsLayer { translationX = size.width * selectedPosition }
        .drawBackdrop(
          backdrop = trackBackdrop,
          shape = { controlShape },
          effects = {
            vibrancy()
            blur(3f.dp.toPx())
            lens(11f.dp.toPx(), 21f.dp.toPx(), chromaticAberration = true)
          },
          highlight = { Highlight.Default.copy(alpha = 0.22f + 0.72f * pressProgress) },
          innerShadow = { InnerShadow(radius = 4.dp, alpha = 0.14f) },
          layerBlock = {
            scaleX = 1f + 0.035f * pressProgress
            scaleY = 1f + 0.065f * pressProgress
          },
          onDrawSurface = { drawRect(selectedColor) }
        )
    )
    Row(Modifier.fillMaxSize().padding(horizontal = sideInset, vertical = 3.dp)) {
      items.forEachIndexed { index, item ->
        val selected = index == selectedIndex
        Box(
          Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(controlShape)
            .pointerInput(index) {
              detectTapGestures(onPress = {
                isSegmentPressed = true
                val released = tryAwaitRelease()
                isSegmentPressed = false
                if (released) onItemSelected(index)
              })
            }
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
 * Streamlined, balanced Liquid Glass navigation dock.
 * Integrates 4 destinations + Center Quick Add action with a curved sliding optical lens.
 */
@Composable
fun LiquidGlassNavigationDock(
  destinations: List<AppDestination>,
  selectedTab: Int,
  onDestinationSelected: (AppDestination) -> Unit,
  modifier: Modifier = Modifier,
  isQuickAddOpen: Boolean = false,
  onQuickAdd: (() -> Unit)? = null,
  hazeState: HazeState? = null,
  backdrop: Backdrop? = null
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val isDark = LocalAppColors.current.isDark
  val reduceMotion = remember { isReducedMotionEnabled(context) }
  val coroutineScope = rememberCoroutineScope()

  val hasCenterFab = onQuickAdd != null
  val totalSlots = if (hasCenterFab) 5 else 4

  fun slotIndexForTab(tab: Int): Int =
    if (!hasCenterFab || tab < 2) tab else tab + 1

  fun tabForSlotIndex(slot: Int): Int? = when {
    !hasCenterFab -> slot.coerceIn(0, 3)
    slot < 2 -> slot
    slot == 2 -> null
    else -> (slot - 1).coerceIn(0, 3)
  }

  var tabsWidthPx by remember { mutableFloatStateOf(0f) }
  val slotWidth = if (tabsWidthPx > 0f) tabsWidthPx / totalSlots.toFloat() else 0f
  val indicatorX = remember { Animatable(0f) }
  var isDragging by remember { mutableStateOf(false) }
  var isHoldingDock by remember { mutableStateOf(false) }
  var lastHapticTab by remember { mutableIntStateOf(selectedTab) }

  // Sync indicator when not dragging
  LaunchedEffect(selectedTab, slotWidth, hasCenterFab) {
    if (!isDragging && slotWidth > 0f) {
      val targetSlot = slotIndexForTab(selectedTab)
      val targetX = slotWidth * (targetSlot + 0.5f)
      if (reduceMotion) indicatorX.snapTo(targetX) else indicatorX.animateTo(
        targetValue = targetX,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f)
      )
    }
  }

  LaunchedEffect(slotWidth, hasCenterFab) {
    if (slotWidth > 0f && indicatorX.value == 0f) {
      val targetSlot = slotIndexForTab(selectedTab)
      indicatorX.snapTo(slotWidth * (targetSlot + 0.5f))
    }
  }

  val dockScale by animateFloatAsState(
    targetValue = if (!reduceMotion && isHoldingDock) 1.02f else 1.0f,
    animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.75f, stiffness = 380f),
    label = "dockScale"
  )

  val fabRotation by animateFloatAsState(
    targetValue = if (isQuickAddOpen && !reduceMotion) 45f else 0f,
    animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.60f, stiffness = 450f),
    label = "fabRotation"
  )

  val dockBackdropModifier = if (hazeState != null) {
    Modifier
  } else {
    Modifier.liquidGlassCapsule(shape = CircleShape, borderWidth = 0.75.dp, isDark = isDark)
  }

  Box(
    modifier = modifier
      .height(64.dp)
      .graphicsLayer {
        scaleX = dockScale
        scaleY = dockScale
      }
      .then(dockBackdropModifier)
      .padding(horizontal = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    if (hazeState != null) {
      LiquidGlassBackdrop(
        hazeState = hazeState,
        shape = RoundedCornerShape(percent = 50),
        isDark = isDark,
        modifier = Modifier.matchParentSize()
      )
      LiquidGlassHighlight(
        shape = RoundedCornerShape(percent = 50),
        isDark = isDark,
        modifier = Modifier.matchParentSize()
      )
      Box(
        modifier = Modifier
          .matchParentSize()
          .liquidGlassRim(RoundedCornerShape(percent = 50), isDark, 0.75.dp)
      )
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .onSizeChanged { tabsWidthPx = it.width.toFloat() }
        .pointerInput(slotWidth, destinations, hasCenterFab) {
          if (slotWidth <= 0f) return@pointerInput

          val minX = slotWidth * 0.5f
          val maxX = slotWidth * (totalSlots - 0.5f)
          val touchSlop = viewConfiguration.touchSlop

          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            isHoldingDock = true
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
                  change.consume()
                }
              }

              if (dragStarted) {
                change.consume()
                val targetX = change.position.x.coerceIn(minX, maxX)
                coroutineScope.launch {
                  indicatorX.snapTo(targetX)
                }

                val currentSlot = ((targetX / slotWidth) - 0.5f).roundToInt().coerceIn(0, totalSlots - 1)
                val currentTab = tabForSlotIndex(currentSlot)
                if (currentTab != null && currentTab != lastHapticTab) {
                  lastHapticTab = currentTab
                  VibrationHelper.tick(context)
                  com.example.util.SoundManager.playSwipeNav(context)
                }
              }
            } while (event.changes.any { it.pressed })

            isHoldingDock = false
            if (dragStarted) {
              isDragging = false
              val finalSlot = ((indicatorX.value / slotWidth) - 0.5f).roundToInt().coerceIn(0, totalSlots - 1)
              val finalTab = tabForSlotIndex(finalSlot)
              if (finalTab != null) {
                onDestinationSelected(destinations[finalTab])
                VibrationHelper.tick(context)
                com.example.util.SoundManager.playSwipeNav(context)
                val targetSlot = slotIndexForTab(finalTab)
                coroutineScope.launch {
                  val selectedX = slotWidth * (targetSlot + 0.5f)
                  if (reduceMotion) indicatorX.snapTo(selectedX) else indicatorX.animateTo(
                    selectedX,
                    spring(dampingRatio = 0.75f, stiffness = 400f)
                  )
                }
              }
            }
          }
        },
      contentAlignment = Alignment.CenterStart
    ) {
      // 1. Sliding Circular Optical Glass Disc with Prismatic Caustic Rainbow Shine
      if (slotWidth > 0f) {
        val indicatorSizeDp = 48.dp
        val indicatorSizePx = with(density) { indicatorSizeDp.toPx() }
        val flareIntensity by animateFloatAsState(
          targetValue = if (isHoldingDock || isDragging) 1.35f else 1.0f,
          animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
          label = "flareIntensity"
        )
        val discBackground = if (isDark) Color(0x38191922) else Color(0x55FFFFFF)

        Box(
          modifier = Modifier
            .size(indicatorSizeDp)
            .graphicsLayer {
              translationX = indicatorX.value - (indicatorSizePx / 2f)
            }
            .clip(CircleShape)
            .background(discBackground)
            .drawWithCache {
              val center = Offset(size.width / 2f, size.height / 2f)
              val radius = (size.minDimension / 2f) - 1.25f.dp.toPx()
              onDrawWithContent {
                drawContent()
                // Subtle inner disc shadow for physical glass depth
                drawCircle(
                  brush = Brush.radialGradient(
                    0.70f to Color.Transparent,
                    1.00f to Color.Black.copy(alpha = if (isDark) 0.35f else 0.12f),
                    center = center,
                    radius = radius
                  ),
                  radius = radius,
                  center = center
                )
                // The Brilliant Prismatic Rainbow Caustic Shine Rim
                drawPrismaticCausticRing(
                  center = center,
                  radius = radius,
                  thickness = 2.75.dp.toPx(),
                  intensity = flareIntensity,
                  isDark = isDark
                )
              }
            }
        )
      }

      Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (hasCenterFab) {
          // Tab 0: Home
          LiquidGlassNavItem(
            destination = destinations[0],
            isSelected = selectedTab == 0,
            onClick = {
              com.example.util.SoundManager.playSwipeNav(context)
              onDestinationSelected(destinations[0])
            },
            modifier = Modifier.weight(1f)
          )
          // Tab 1: Productivity
          LiquidGlassNavItem(
            destination = destinations[1],
            isSelected = selectedTab == 1,
            onClick = {
              com.example.util.SoundManager.playSwipeNav(context)
              onDestinationSelected(destinations[1])
            },
            modifier = Modifier.weight(1f)
          )
          // Center Slot 2: Quick Add Action Button
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .testTag("nav_center_fab")
              .semantics {
                role = Role.Button
                contentDescription = if (isQuickAddOpen) "Close create sheet" else "Quick Create"
              }
              .bounceClick(shine = true) {
                com.example.util.SoundManager.playButtonClick(context)
                onQuickAdd?.invoke()
              },
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0x38191922) else Color(0x55FFFFFF))
                .drawWithCache {
                  val center = Offset(size.width / 2f, size.height / 2f)
                  val radius = (size.minDimension / 2f) - 1.25f.dp.toPx()
                  onDrawWithContent {
                    drawContent()
                    drawPrismaticCausticRing(
                      center = center,
                      radius = radius,
                      thickness = 2.0.dp.toPx(),
                      intensity = if (isQuickAddOpen) 1.25f else 0.85f,
                      isDark = isDark
                    )
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isQuickAddOpen) "Close create sheet" else "Quick Create",
                tint = if (isDark) Color.White else Color(0xFF14161E),
                modifier = Modifier
                  .size(24.dp)
                  .graphicsLayer(rotationZ = fabRotation)
              )
            }
          }
          // Tab 2: Tools
          LiquidGlassNavItem(
            destination = destinations[2],
            isSelected = selectedTab == 2,
            onClick = {
              com.example.util.SoundManager.playSwipeNav(context)
              onDestinationSelected(destinations[2])
            },
            modifier = Modifier.weight(1f)
          )
          // Tab 3: Insights
          LiquidGlassNavItem(
            destination = destinations[3],
            isSelected = selectedTab == 3,
            onClick = {
              com.example.util.SoundManager.playSwipeNav(context)
              onDestinationSelected(destinations[3])
            },
            modifier = Modifier.weight(1f)
          )
        } else {
          destinations.take(4).forEachIndexed { index, destination ->
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
      }
    }
  }
}

/**
 * Isolated destination item for LiquidGlassNavigationDock with stable parameters.
 * Active destination features metallic 3D beveled appearance matching reference design.
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
  val unselectedColor = if (isDark) Color.White.copy(alpha = 0.48f) else Color.Black.copy(alpha = 0.45f)
  val iconColor by animateColorAsState(
    targetValue = if (isSelected) Color.White else unselectedColor,
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
      }
      .bounceClick(
        level = com.example.ui.components.InteractionLevel.MINIMAL,
        shine = true,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    if (isSelected) {
      // 3D Metallic beveled icon with subtle drop shadow
      Icon(
        imageVector = destination.iconFilled,
        contentDescription = destination.label,
        tint = Color.Black.copy(alpha = 0.35f),
        modifier = Modifier
          .size(25.dp)
          .offset(y = 1.dp)
          .graphicsLayer {
            scaleX = iconScale
            scaleY = iconScale
          }
      )
      Icon(
        imageVector = destination.iconFilled,
        contentDescription = destination.label,
        tint = Color.White,
        modifier = Modifier
          .size(25.dp)
          .graphicsLayer {
            scaleX = iconScale
            scaleY = iconScale
          }
      )
    } else {
      Icon(
        imageVector = destination.iconOutlined,
        contentDescription = destination.label,
        tint = iconColor,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}

/**
 * 7. LiquidGlassFloatingActionButton
 * Independent 54dp floating circular Liquid Glass button with Prismatic Caustic Shine.
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
      .clip(CircleShape)
      .background(if (isDark) Color(0x38191922) else Color(0x55FFFFFF))
      .drawWithCache {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (size.minDimension / 2f) - 1.25f.dp.toPx()
        onDrawWithContent {
          drawContent()
          drawPrismaticCausticRing(
            center = center,
            radius = radius,
            thickness = 2.5.dp.toPx(),
            intensity = if (isOpen) 1.35f else 1.0f,
            isDark = isDark
          )
        }
      }
      .bounceClick(shine = true) {
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
      tint = com.example.ui.theme.InkPrimary,
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

