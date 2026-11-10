package com.example.ui.components

import com.skydoves.cloudy.cloudy

import android.content.Context
import android.os.Build
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.IntOffset
import com.example.ui.theme.SpecialFontFamily
import com.example.ui.theme.StrongFontFamily
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawWithContent
import com.example.ui.theme.OuterCornerRadius
import com.example.ui.theme.InnerCornerRadius
import com.example.ui.theme.OuterCornerShape
import com.example.ui.theme.InnerCornerShape
import com.example.ui.theme.InkWashIvoryElevated
import com.example.ui.theme.LightGlassSpecular
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.PrimaryAccentLime
import com.example.ui.theme.PrimaryDarkCanvas
import com.example.ui.theme.MobbinInk
import com.example.ui.theme.MobbinField
import com.example.ui.theme.MobbinCanvasSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.util.VibrationHelper
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PaperBackground
import com.example.ui.theme.PaperDotColor
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.theme.ProgressBarTrack
import com.example.ui.theme.ProgressBarFilled
import com.example.ui.theme.InkWashCharcoal
import com.example.ui.theme.InkWashMist
import com.example.ui.theme.InkWashIvory
import com.example.ui.theme.InkWashCharcoalDeep
import com.example.ui.theme.InkWashCharcoalSurface
import com.example.ui.theme.InkWashCharcoalElevated
import com.example.ui.theme.InkWashCharcoalBorder
import com.example.ui.theme.InkWashIvoryPaper
import com.example.ui.theme.InkWashIvoryCard

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.material3.HorizontalDivider
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.OklchIndigo
import com.example.ui.theme.OklchIndigoSoft
import com.example.ui.theme.OklchEmerald
import com.example.ui.theme.OklchEmeraldSoft
import com.example.ui.theme.OklchAmber
import com.example.ui.theme.OklchAmberSoft
import com.example.ui.theme.OklchCoral
import com.example.ui.theme.OklchCoralSoft
import com.example.ui.theme.OklchCyan
import com.example.ui.theme.OklchCyanSoft
import com.example.ui.theme.OklchPurple
import com.example.ui.theme.OklchPurpleSoft

import com.example.ui.theme.LocalAppColors
import com.example.ui.glass.LocalHazeState
import com.example.ui.glass.liquidGlassRim
import com.example.ui.glass.LiquidGlassVariant
import com.example.ui.glass.LiquidGlassTopAppBar
import com.example.ui.glass.LiquidGlassLinearProgress
import com.example.ui.glass.LiquidGlassEffectContainer
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Flat monochrome base canvas: pure black in dark mode and white in light mode.
 * Glass surfaces provide depth separately from the page background.
 */
@Composable
fun Modifier.paperDotBackground(): Modifier {
  val colors = LocalAppColors.current
  return this.drawBehind {
    drawRect(color = colors.paperBackground)
  }
}

/**
 * Inset-aware dynamic content bottom clearance for all scrolling screens above the floating navigation dock.
 * Calculates clearance from the 64dp dock shell, its vertical padding, system gesture inset,
 * and a content gap so the bottom-most card remains reachable above the glass edge band.
 */
val FloatingNavContentBottomPadding: Dp
  @Composable get() {
    val navInsets = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return 64.dp + 16.dp + navInsets + 36.dp
  }

/**
 * Centralized Physical Motion Token System (20 Standardized Tokens).
 * Fast, responsive, subtle, physically believable, and purposeful.
 * Free of cartoon wobbles, multiple oscillations, and artificial delays.
 */
object PhysicalMotionSpecs {
  // 1. Tactile Micro-Interactions (toggles, chips, small micro-states)
  val MotionMicro = spring<Float>(dampingRatio = 0.85f, stiffness = 800f)

  // 2. Button Press (touch down elastic compression)
  val MotionButtonPress = spring<Float>(dampingRatio = 0.65f, stiffness = 500f)

  // 3. Button Release (Kern Society snappy elastic rebound)
  val MotionButtonRelease = spring<Float>(dampingRatio = 0.54f, stiffness = 450f)

  // 4. Icon Morph (plus-to-X rotation, star favorite pop, filter tilt)
  val MotionIconMorph = spring<Float>(dampingRatio = 0.60f, stiffness = 420f)

  // 5. Selection (fluid segmented pill & tab sliding indicator)
  val MotionSelection = spring<Float>(dampingRatio = 0.72f, stiffness = 400f)
  val MotionSelectionDp = spring<Dp>(dampingRatio = 0.72f, stiffness = 400f)

  // 6. Navigation (Horizontal pager screen transitions)
  val MotionNavigation = spring<Float>(dampingRatio = 0.78f, stiffness = 420f)

  // 7. Screen Enter
  val MotionScreenEnter = tween<Float>(durationMillis = 220, easing = FastOutSlowInEasing)
  val MotionScreenEnterOffset = tween<IntOffset>(durationMillis = 220, easing = FastOutSlowInEasing)

  // 8. Screen Exit
  val MotionScreenExit = tween<Float>(durationMillis = 180, easing = FastOutSlowInEasing)
  val MotionScreenExitOffset = tween<IntOffset>(durationMillis = 180, easing = FastOutSlowInEasing)

  // 9. Sheet Enter (modal bottom sheet slide-up)
  val MotionSheetEnter = spring<Float>(dampingRatio = 0.80f, stiffness = 380f)
  val MotionSheetEnterOffset = spring<IntOffset>(dampingRatio = 0.80f, stiffness = 380f)

  // 10. Sheet Exit (modal bottom sheet slide-down)
  val MotionSheetExit = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
  val MotionSheetExitOffset = tween<IntOffset>(durationMillis = 200, easing = FastOutSlowInEasing)

  // 11. List Insert (item addition with slide-up and fade)
  val MotionListInsert = spring<Float>(dampingRatio = 0.75f, stiffness = 380f)

  // 12. List Remove (item removal height collapse)
  val MotionListRemove = tween<Float>(durationMillis = 180, easing = FastOutSlowInEasing)

  // 13. Completion (checkmark drawing and pop)
  val MotionCompletion = spring<Float>(dampingRatio = 0.52f, stiffness = 440f)

  // 14. Progress (progress bar and circular gauge fill)
  val MotionProgress = spring<Float>(dampingRatio = 0.82f, stiffness = 320f)

  // 15. Counter (streak and count numeric increment pop)
  val MotionCounter = spring<Float>(dampingRatio = 0.48f, stiffness = 480f)

  // 16. Expand (accordion and dropdown expansion)
  val MotionExpand = spring<Float>(dampingRatio = 0.78f, stiffness = 360f)

  // 17. Collapse (accordion and dropdown collapse)
  val MotionCollapse = tween<Float>(durationMillis = 180, easing = FastOutSlowInEasing)

  // 18. Refresh (continuous linear rotation strictly while refreshing)
  val MotionRefresh = tween<Float>(durationMillis = 750, easing = LinearEasing)

  // 19. Success (one-shot checkmark or copy badge confirmation)
  val MotionSuccess = spring<Float>(dampingRatio = 0.55f, stiffness = 420f)

  // 20. Error (shake / validation warning)
  val MotionError = spring<Float>(dampingRatio = 0.45f, stiffness = 600f)

  // Backward-compatible aliases for existing components
  val TactilePress = MotionButtonPress
  val SpatialSlide = MotionNavigation
  val SpatialSlideDp = MotionSelectionDp
  val Settle = spring<Float>(dampingRatio = 0.80f, stiffness = 320f)
  val SpringPop = MotionSuccess
  val SpringElastic = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
  val ModalSheetEntrance = MotionSheetEnter
  val DialogPopEntrance = spring<Float>(dampingRatio = 0.68f, stiffness = 440f)
  val KernKineticOvershoot = MotionButtonRelease
  val KernSquashSnap = MotionCounter
  val DheerajTiltSpring = spring<Float>(dampingRatio = 0.68f, stiffness = 380f)
}

// Backward-compatible alias
object AppleSpringSpecs {
  val Bouncy = PhysicalMotionSpecs.SpringPop
  val Gentle = PhysicalMotionSpecs.Settle
  val Responsive = PhysicalMotionSpecs.TactilePress
  val Snappy = PhysicalMotionSpecs.TactilePress
}

/**
 * Reusable staggered entrance animation for list items and cards.
 * Each item fades in + slides up with a configurable stagger delay.
 * Respects reduced motion; when enabled, items appear instantly.
 *
 * @param index The item's position in the list (0-based). Higher indices animate later.
 * @param maxStagger Maximum number of items to stagger (items beyond this appear together).
 * @param delayPerItem Milliseconds of delay between each item's animation start.
 * @param durationMs Total animation duration per item.
 * @param slideOffsetDp How many dp the item slides up from below.
 */
@Composable
fun Modifier.staggeredEntrance(
  index: Int,
  maxStagger: Int = 12,
  delayPerItem: Int = 35,
  durationMs: Int = 260,
  slideOffsetDp: Int = 24
): Modifier {
  val context = LocalContext.current
  val isReduced = remember { isReducedMotionEnabled(context) }
  if (isReduced) return this

  val clampedIndex = index.coerceAtMost(maxStagger)
  val delay = clampedIndex * delayPerItem
  val density = LocalDensity.current
  val slideOffsetPx = with(density) { slideOffsetDp.dp.toPx() }

  var hasAppeared by rememberSaveable(index) { mutableStateOf(false) }
  val alpha by animateFloatAsState(
    targetValue = if (hasAppeared) 1f else 0f,
    animationSpec = tween(durationMillis = durationMs, delayMillis = delay, easing = FastOutSlowInEasing),
    label = "staggerAlpha_$index"
  )
  val offsetY by animateFloatAsState(
    targetValue = if (hasAppeared) 0f else slideOffsetPx,
    animationSpec = tween(durationMillis = durationMs, delayMillis = delay, easing = FastOutSlowInEasing),
    label = "staggerOffset_$index"
  )

  LaunchedEffect(Unit) {
    hasAppeared = true
  }

  return this.graphicsLayer {
    this.alpha = alpha
    translationY = offsetY
  }
}

/**
 * Spring-based entrance for hero cards and featured content.
 * Provides a subtle scale-up + fade with organic spring physics.
 */
@Composable
fun Modifier.heroEntrance(
  delayMs: Int = 80
): Modifier {
  val context = LocalContext.current
  val isReduced = remember { isReducedMotionEnabled(context) }
  if (isReduced) return this

  var hasAppeared by rememberSaveable { mutableStateOf(false) }
  val scale by animateFloatAsState(
    targetValue = if (hasAppeared) 1f else 0.92f,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 360f),
    label = "heroScale"
  )
  val alpha by animateFloatAsState(
    targetValue = if (hasAppeared) 1f else 0f,
    animationSpec = tween(durationMillis = 240, delayMillis = delayMs, easing = FastOutSlowInEasing),
    label = "heroAlpha"
  )

  LaunchedEffect(Unit) {
    if (delayMs > 0) kotlinx.coroutines.delay(delayMs.toLong())
    hasAppeared = true
  }

  return this.graphicsLayer {
    scaleX = scale
    scaleY = scale
    this.alpha = alpha
  }
}

/**
 * Animated section header with slide-from-left entrance.
 * Used for section titles like "TODAY'S TASKS", "TODAY'S HABITS".
 */
@Composable
fun Modifier.sectionEntrance(
  delayMs: Int = 0
): Modifier {
  val context = LocalContext.current
  val isReduced = remember { isReducedMotionEnabled(context) }
  if (isReduced) return this

  var hasAppeared by rememberSaveable { mutableStateOf(false) }
  val density = LocalDensity.current
  val slideOffsetPx = with(density) { 16.dp.toPx() }

  val alpha by animateFloatAsState(
    targetValue = if (hasAppeared) 1f else 0f,
    animationSpec = tween(durationMillis = 220, delayMillis = delayMs, easing = FastOutSlowInEasing),
    label = "sectionAlpha"
  )
  val offsetX by animateFloatAsState(
    targetValue = if (hasAppeared) 0f else -slideOffsetPx,
    animationSpec = tween(durationMillis = 220, delayMillis = delayMs, easing = FastOutSlowInEasing),
    label = "sectionSlide"
  )

  LaunchedEffect(Unit) {
    hasAppeared = true
  }

  return this.graphicsLayer {
    this.alpha = alpha
    translationX = offsetX
  }
}

/**
 * Subtle spring pulse entrance on appearance for celebratory items like streak badges.
 * Provides micro-feedback without being overly dramatic.
 */
@Composable
fun Modifier.pulseEntrance(
  delayMs: Int = 120
): Modifier {
  val context = LocalContext.current
  val isReduced = remember { isReducedMotionEnabled(context) }
  if (isReduced) return this

  var hasAppeared by rememberSaveable { mutableStateOf(false) }
  val scale by animateFloatAsState(
    targetValue = if (hasAppeared) 1f else 0.8f,
    animationSpec = spring(dampingRatio = 0.58f, stiffness = 340f),
    label = "pulseScale"
  )
  val alpha by animateFloatAsState(
    targetValue = if (hasAppeared) 1f else 0f,
    animationSpec = tween(durationMillis = 200, delayMillis = delayMs, easing = FastOutSlowInEasing),
    label = "pulseAlpha"
  )

  LaunchedEffect(Unit) {
    if (delayMs > 0) kotlinx.coroutines.delay(delayMs.toLong())
    hasAppeared = true
  }

  return this.graphicsLayer {
    scaleX = scale
    scaleY = scale
    this.alpha = alpha
  }
}

/**
 * Motion hierarchy to avoid making every component bounce equally.
 * Large CTA / FAB: MEDIUM (0.92f)
 * Standard Buttons / Cards / Checkboxes: SMALL (0.94f)
 * Icon Buttons / Chips / Small Controls: MICRO (0.97f)
 * Navigation Items: MINIMAL (0.985f)
 */
enum class InteractionLevel(val scaleDown: Float, val pressedAlpha: Float) {
  MICRO(scaleDown = 0.98f, pressedAlpha = 0.96f),
  SMALL(scaleDown = 0.96f, pressedAlpha = 0.94f),
  MEDIUM(scaleDown = 0.93f, pressedAlpha = 0.90f),
  MINIMAL(scaleDown = 0.99f, pressedAlpha = 0.98f)
}

/**
 * Checks system-level accessibility setting for reduced motion.
 */
fun isReducedMotionEnabled(context: Context): Boolean {
  return try {
    val durationScale = android.provider.Settings.Global.getFloat(
      context.contentResolver,
      android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
      1.0f
    )
    durationScale == 0f
  } catch (_: Exception) {
    false
  }
}

/**
 * Clean no-ops for decorative AI-slop animations, preserving API compatibility
 * while keeping the interface completely calm, physically grounded, and GPU-conscious.
 */
fun Modifier.shimmerBadge(
  highlightColor: Color? = null,
  durationMillis: Int = 2400
): Modifier = this

/**
 * Tactile spring press feedback modifier following restrained physical motion principles.
 * Provides subtle scale compression, gentle opacity response, and instant haptic tick.
 * Avoids cartoon wobbling and exaggerated scaling.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bounceClick(
  level: InteractionLevel = InteractionLevel.SMALL,
  scaleDown: Float = level.scaleDown,
  shine: Boolean = false,
  shape: Shape? = null,
  enabled: Boolean = true,
  onLongClick: (() -> Unit)? = null,
  onClick: (() -> Unit)? = null
): Modifier {
  if (!enabled || (onClick == null && onLongClick == null)) return this
  return composed {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isReduced = remember { isReducedMotionEnabled(context) }
    val effectiveScale = if (isReduced) 1.0f else scaleDown
    val effectiveAlpha = level.pressedAlpha

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val density = LocalDensity.current
    val scale by animateFloatAsState(
      targetValue = if (isPressed) effectiveScale else if (isHovered && !isReduced) 1.02f else 1f,
      animationSpec = PhysicalMotionSpecs.TactilePress,
      label = "springScale"
    )
    val translateY by animateFloatAsState(
      targetValue = if (isHovered && !isPressed && !isReduced) -with(density) { 2.dp.toPx() } else 0f,
      animationSpec = PhysicalMotionSpecs.TactilePress,
      label = "hoverLift"
    )
    val alpha by animateFloatAsState(
      targetValue = if (isPressed) effectiveAlpha else 1f,
      animationSpec = if (isReduced) androidx.compose.animation.core.snap() else PhysicalMotionSpecs.TactilePress,
      label = "springAlpha"
    )
    val isDark = LocalAppColors.current.isDark

    // On-click shine with exact 0.3s fade-out (white for dark mode, dark for light mode)
    val clickShine = remember { Animatable(0f) }

    val shineBrush = remember(isDark) {
      if (isDark) {
        Brush.linearGradient(
          colors = listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.20f), Color.Transparent),
          start = Offset(0f, 0f),
          end = Offset(600f, 600f)
        )
      } else {
        Brush.linearGradient(
          colors = listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.08f), Color.Transparent),
          start = Offset(0f, 0f),
          end = Offset(600f, 600f)
        )
      }
    }

    val pressShine = if (shine && isPressed) 0.18f else if (shine && isHovered) 0.04f else 0f
    val totalShine = (clickShine.value * 0.40f) + pressShine

    this
      .graphicsLayer {
        this.scaleX = scale
        this.scaleY = scale
        this.translationY = translateY
      }
      .alpha(alpha)
      .drawWithContent {
        drawContent()
        if (shine && totalShine > 0.001f && size.width > 0f && size.height > 0f) {
          if (shape != null) {
            val outline = shape.createOutline(size, layoutDirection, this)
            drawOutline(outline, brush = shineBrush, alpha = totalShine)
          } else {
            // Default to rounded pill or rounded rectangle matching element size (never sharp 90-degree rectangle!)
            val cornerRadius = CornerRadius(minOf(size.width, size.height) * 0.25f)
            drawRoundRect(
              brush = shineBrush,
              alpha = totalShine,
              cornerRadius = cornerRadius
            )
          }
        }
      }
      .combinedClickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onLongClick = if (onLongClick != null) {
          {
            VibrationHelper.tick(context)
            onLongClick()
          }
        } else null,
        onClick = {
          VibrationHelper.click(context)
          com.example.util.SoundManager.playTapSound(context)
          if (shine && !isReduced) {
            coroutineScope.launch {
              clickShine.snapTo(1f)
              clickShine.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
              )
            }
          }
          onClick?.invoke()
        }
      )
  }
}

/**
 * Universal Bouncy Press Modifier (Hallmark & Apple Liquid Glass).
 * Immediate tactile compression (0.95f) on touch down with MotionButtonPress,
 * and snappy elastic rebound (1.0f) on release with MotionButtonRelease.
 * Uses GPU graphicsLayer to prevent relayout / remeasurement churn.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bouncyPress(
  scaleDown: Float = 0.95f,
  enabled: Boolean = true,
  onLongClick: (() -> Unit)? = null,
  onClick: (() -> Unit)? = null
): Modifier = composed {
  if (!enabled || (onClick == null && onLongClick == null)) return@composed this
  val context = LocalContext.current
  val isReduced = remember { isReducedMotionEnabled(context) }
  val targetScale = if (isReduced) 1.0f else scaleDown

  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) targetScale else 1f,
    animationSpec = if (isPressed) PhysicalMotionSpecs.MotionButtonPress else PhysicalMotionSpecs.MotionButtonRelease,
    label = "bouncyPressScale"
  )
  val alpha by animateFloatAsState(
    targetValue = if (isPressed) 0.92f else 1f,
    animationSpec = PhysicalMotionSpecs.MotionMicro,
    label = "bouncyPressAlpha"
  )

  this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
      this.alpha = alpha
    }
    .combinedClickable(
      interactionSource = interactionSource,
      indication = null,
      enabled = enabled,
      onLongClick = onLongClick?.let { action ->
        {
          VibrationHelper.tick(context)
          action()
        }
      },
      onClick = {
        onClick?.let { action ->
          VibrationHelper.tick(context)
          action()
        }
      }
    )
}

/**
 * Optimized scroll modifier with zero GPU fill-rate overhead.
 * Removes heavy Skia Gaussian blur re-allocations to guarantee rock-solid 120 FPS scrolling.
 */
@Composable
fun Modifier.motionBlurOnScroll(
  isScrollInProgress: Boolean,
  velocityFraction: Float = 0f
): Modifier = this

/**
 * Animated Checkmark Icon.
 * Renders an animated stroke draw (0f to 1f) and spring scale pop upon task/habit completion.
 */
@Composable
fun AnimatedCheckmarkIcon(
  isCompleted: Boolean,
  tint: Color,
  modifier: Modifier = Modifier
) {
  val strokeProgress by animateFloatAsState(
    targetValue = if (isCompleted) 1f else 0f,
    animationSpec = PhysicalMotionSpecs.MotionCompletion,
    label = "checkmarkProgress"
  )
  val checkScale by animateFloatAsState(
    targetValue = if (isCompleted) 1f else 0.35f,
    animationSpec = PhysicalMotionSpecs.MotionCompletion,
    label = "checkmarkScale"
  )

  Canvas(
    modifier = modifier
      .size(20.dp)
      .graphicsLayer {
        scaleX = checkScale
        scaleY = checkScale
      }
  ) {
    if (strokeProgress > 0.05f) {
      val w = size.width
      val h = size.height

      val p1 = Offset(w * 0.22f, h * 0.52f)
      val p2 = Offset(w * 0.42f, h * 0.74f)
      val p3 = Offset(w * 0.78f, h * 0.28f)

      val seg1 = kotlin.math.hypot(p2.x - p1.x, p2.y - p1.y)
      val seg2 = kotlin.math.hypot(p3.x - p2.x, p3.y - p2.y)
      val total = seg1 + seg2
      val currentLen = total * strokeProgress
      val strokeWidth = 2.2.dp.toPx()

      if (currentLen <= seg1) {
        val f = currentLen / seg1
        drawLine(
          color = tint,
          start = p1,
          end = Offset(p1.x + (p2.x - p1.x) * f, p1.y + (p2.y - p1.y) * f),
          strokeWidth = strokeWidth,
          cap = StrokeCap.Round
        )
      } else {
        drawLine(
          color = tint,
          start = p1,
          end = p2,
          strokeWidth = strokeWidth,
          cap = StrokeCap.Round
        )
        val f2 = (currentLen - seg1) / seg2
        drawLine(
          color = tint,
          start = p2,
          end = Offset(p2.x + (p3.x - p2.x) * f2, p2.y + (p3.y - p2.y) * f2),
          strokeWidth = strokeWidth,
          cap = StrokeCap.Round
        )
      }
    }
  }
}

/**
 * Lightweight Floating Undo Confirmation Pill.
 * Pops up after completing or deleting a task, offering instant restore before auto-dismissing.
 */
@Composable
fun UndoConfirmationPill(
  visible: Boolean,
  message: String = "Task completed",
  onUndo: () -> Unit,
  modifier: Modifier = Modifier
) {
  androidx.compose.animation.AnimatedVisibility(
    visible = visible,
    enter = androidx.compose.animation.slideInVertically(
      initialOffsetY = { it },
      animationSpec = PhysicalMotionSpecs.MotionSheetEnterOffset
    ) + androidx.compose.animation.fadeIn(animationSpec = PhysicalMotionSpecs.MotionScreenEnter),
    exit = androidx.compose.animation.slideOutVertically(
      targetOffsetY = { it },
      animationSpec = PhysicalMotionSpecs.MotionSheetExitOffset
    ) + androidx.compose.animation.fadeOut(animationSpec = PhysicalMotionSpecs.MotionScreenExit),
    modifier = modifier
  ) {
    Box(
      modifier = Modifier
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .fluidLensedGlass(
          shape = CircleShape,
          elevation = 10.dp,
          borderWidth = 0.75.dp,
          isElevated = true
        )
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = InkPrimary,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = message,
          style = TextStyle(
            fontFamily = StrongFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = InkPrimary
          )
        )
        Box(
          modifier = Modifier
            .bounceClick(scaleDown = 0.985f) { onUndo() }
            .background(Color(0x22FFFFFF), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = "Undo",
            style = TextStyle(
              fontFamily = StrongFontFamily,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = AccentTerracotta
            )
          )
        }
      }
    }
  }
}

/**
 * Kern Society Kinetic Elastic Stretch & Squash press modifier (Priority 2).
 * Simulates variable font elasticity & volume conservation on touch down:
 * expands along the horizontal axis (X) while compressing vertically (Y),
 * then snaps back with a high-tension spring overshoot on release.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.elasticStretchPress(
  horizontalStretch: Float = 1.015f,
  verticalSquash: Float = 0.99f,
  shineShape: Shape? = null,
  enabled: Boolean = true,
  onLongClick: (() -> Unit)? = null,
  onClick: (() -> Unit)? = null
): Modifier {
  if (!enabled || (onClick == null && onLongClick == null)) return this
  return composed {
    val context = LocalContext.current
    val isReduced = remember { isReducedMotionEnabled(context) }
    val targetScaleX = if (isReduced) 1.0f else horizontalStretch
    val targetScaleY = if (isReduced) 1.0f else verticalSquash

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleX by animateFloatAsState(
      targetValue = if (isPressed) targetScaleX else 1f,
      animationSpec = spring(
        dampingRatio = 0.54f,
        stiffness = 450f
      ),
      label = "kernScaleX"
    )
    val scaleY by animateFloatAsState(
      targetValue = if (isPressed) targetScaleY else 1f,
      animationSpec = spring(
        dampingRatio = 0.54f,
        stiffness = 450f
      ),
      label = "kernScaleY"
    )
    val alpha by animateFloatAsState(
      targetValue = if (isPressed) 0.97f else 1f,
      animationSpec = if (isReduced) androidx.compose.animation.core.snap() else spring(dampingRatio = 0.75f, stiffness = 560f),
      label = "kernAlpha"
    )
    val shineAmount by animateFloatAsState(
      targetValue = if (isPressed) 0.18f else 0f,
      animationSpec = if (isReduced) androidx.compose.animation.core.snap() else tween(120),
      label = "glassElasticPressShine"
    )

    this
      .graphicsLayer {
        this.scaleX = scaleX
        this.scaleY = scaleY
        this.alpha = alpha
      }
      .drawWithContent {
        drawContent()
        if (shineAmount > 0.001f) {
          val shine = Brush.linearGradient(
            colors = listOf(Color.White, Color.White.copy(alpha = 0.28f), Color.Transparent),
            start = Offset.Zero,
            end = Offset(size.width, size.height)
          )
          if (shineShape != null) {
            when (val outline = shineShape.createOutline(size, layoutDirection, this)) {
              is androidx.compose.ui.graphics.Outline.Rectangle ->
                drawRect(brush = shine, topLeft = outline.rect.topLeft, size = outline.rect.size, alpha = shineAmount)
              is androidx.compose.ui.graphics.Outline.Rounded ->
                drawRoundRect(
                  brush = shine,
                  topLeft = Offset(outline.roundRect.left, outline.roundRect.top),
                  size = androidx.compose.ui.geometry.Size(outline.roundRect.width, outline.roundRect.height),
                  cornerRadius = outline.roundRect.topLeftCornerRadius,
                  alpha = shineAmount
                )
              is androidx.compose.ui.graphics.Outline.Generic ->
                drawPath(path = outline.path, brush = shine, alpha = shineAmount)
            }
          } else {
            drawRect(brush = shine, alpha = shineAmount)
          }
        }
      }
      .combinedClickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onLongClick = if (onLongClick != null) {
          {
            VibrationHelper.tick(context)
            onLongClick()
          }
        } else null,
        onClick = {
          VibrationHelper.click(context)
          onClick?.invoke()
        }
      )
  }
}

/**
 * Canonical Fluid Lensed Glass Modifier.
 * Implements authentic Apple-style optical lensing:
 * 1. Convex Refraction Body: Multi-stop optical fill simulating real light transmission through a curved lens.
 * 2. Overhead Radial Caustic Dome: Smooth radial gradient focal highlight curving across the surface width.
 * 3. Specular Meniscus Rim: Dual-boundary hairline border with overhead light gathering.
 * 4. Ambient Bounce: Soft lower edge bounce illumination.
 * 5. Kinetic Fluid Elasticity: Volume-conserving squash & stretch (scaleX 1.04f, scaleY 0.96f) on press.
 */
@Composable
fun Modifier.fluidLensedGlass(
  shape: androidx.compose.ui.graphics.Shape = OuterCornerShape,
  elevation: Dp = 2.dp,
  borderWidth: Dp = 0.75.dp,
  isElevated: Boolean = false,
  isInteractive: Boolean = false,
  customBackgroundColor: Color? = null,
  customBorderColor: Color? = null,
  onClick: (() -> Unit)? = null
): Modifier {
  val isDark = LocalAppColors.current.isDark
  val hazeState = LocalHazeState.current
  val defaultCardBg = CardBackground
  val defaultCardBrd = CardBorder
  val glassFill = when {
    customBackgroundColor != null && customBackgroundColor != Color.Unspecified && customBackgroundColor != defaultCardBg ->
      customBackgroundColor.copy(alpha = if (isDark) 0.14f else 0.10f)
    isDark -> Color.White.copy(alpha = if (isElevated) 0.07f else 0.05f)
    else -> Color.White.copy(alpha = if (isElevated) 0.38f else 0.30f)
  }
  val hazeModifier = hazeState?.let { state ->
    Modifier.hazeEffect(state = state) {
      blurRadius = if (isElevated) 20.dp else 14.dp
      noiseFactor = if (isDark) 0.04f else 0f
      tints = listOf(
        HazeTint(
          if (isDark) Color(0xFF0B0C10).copy(alpha = 0.34f)
          else Color.White.copy(alpha = 0.45f)
        )
      )
      fallbackTint = HazeTint(
        if (isDark) Color(0xFF0B0C10).copy(alpha = 0.80f)
        else Color.White.copy(alpha = 0.86f)
      )
    }
  } ?: Modifier

  return this
    .then(hazeModifier)
    .then(if (isInteractive) Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp) else Modifier)
    .clip(shape)
    .then(
      if (isInteractive && onClick != null) {
        Modifier.elasticStretchPress(
          horizontalStretch = 1.015f,
          verticalSquash = 0.99f,
          shineShape = shape,
          onClick = onClick
        )
      } else Modifier
    )
    .drawBehind {
      drawRect(color = glassFill)
    }
    .liquidGlassRim(
      shape = shape,
      isDark = isDark,
      borderWidth = borderWidth,
      variant = if (isElevated) LiquidGlassVariant.Prominent else LiquidGlassVariant.Regular,
      tint = customBackgroundColor
    )
    .then(
      if (customBorderColor != null && customBorderColor != Color.Unspecified && customBorderColor != defaultCardBrd) {
        Modifier.border(borderWidth, customBorderColor.copy(alpha = 0.45f), shape)
      } else Modifier
    )
}

/**
 * Optical specular shining sweep modifier.
 * Creates a soft 20-degree light band traversing smoothly across surfaces,
 * providing genuine specular glass reflection without heavy blur.
 */
/**
 * Physically-grounded specular highlight for Liquid Glass surfaces.
 * Replaces fake infinite looping shine with authentic overhead ambient light capture.
 */
@Composable
fun Modifier.glassSheen(
  enabled: Boolean = true,
  sheenColor: Color = LightGlassSpecular,
  shape: androidx.compose.ui.graphics.Shape = OuterCornerShape
): Modifier {
  if (!enabled) return this
  return this
    .clip(shape)
    .drawWithContent {
      drawContent()
      drawLine(
        color = sheenColor.copy(alpha = 0.38f),
        start = Offset(12.dp.toPx(), 1.dp.toPx()),
        end = Offset((size.width - 12.dp.toPx()).coerceAtLeast(12.dp.toPx()), 1.dp.toPx()),
        strokeWidth = 1.dp.toPx()
      )
    }
}

@Composable
fun SketchCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = OuterCornerRadius,
  backgroundColor: Color = CardBackground,
  borderColor: Color = CardBorder,
  borderWidth: Dp = 0.75.dp,
  elevation: Dp = 2.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  LiquidGlassCard(
    modifier = modifier,
    cornerRadius = cornerRadius,
    backgroundColor = backgroundColor,
    borderColor = borderColor,
    borderWidth = borderWidth,
    elevation = elevation,
    onClick = onClick
  ) {
    content()
  }
}

/**
 * Canonical GlassCard composable implementing authentic Apple Liquid Glass material.
 * Completely free of opaque painted metallic shine and chrome bevels.
 * Optical translucency lets background environment softly illuminate content.
 */
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = OuterCornerRadius,
  backgroundColor: Color = CardBackground,
  borderColor: Color = CardBorder,
  borderWidth: Dp = 0.75.dp,
  elevation: Dp = 2.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  LiquidGlassCard(
    modifier = modifier,
    cornerRadius = cornerRadius,
    backgroundColor = backgroundColor,
    borderColor = borderColor,
    borderWidth = borderWidth,
    elevation = elevation,
    onClick = onClick
  ) {
    content()
  }
}

/**
 * Apple-Style Liquid Glass Button — Icon with Visible Label Pattern.
 * Mandatory pattern: <GlassButton icon={<PlusIcon />}>Add Task</GlassButton>
 * Renders an authentic optical translucent glass stadium pill with spring press.
 */
@Composable
fun GlassButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: (@Composable () -> Unit)? = null,
  enabled: Boolean = true,
  content: @Composable () -> Unit
) {
  Box(
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .fluidLensedGlass(
        shape = CircleShape,
        elevation = 2.dp,
        borderWidth = 0.75.dp,
        isElevated = true,
        isInteractive = enabled,
        onClick = if (enabled) onClick else null
      )
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
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

/**
 * Apple-Style Liquid Glass Button — Circular Icon-Only Pattern.
 * Mandatory pattern: <GlassButton icon={<PlusIcon />} aria-label="Add" />
 * Accessible name (contentDescription) is required.
 */
@Composable
fun GlassButton(
  onClick: () -> Unit,
  contentDescription: String,
  modifier: Modifier = Modifier,
  buttonSize: Dp = 48.dp,
  size: Dp = buttonSize,
  enabled: Boolean = true,
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
    Box(
      modifier = Modifier
        .size(visualSize)
        .fluidLensedGlass(
          shape = CircleShape,
          elevation = 2.dp,
          borderWidth = 0.75.dp,
          isElevated = true
        ),
      contentAlignment = Alignment.Center
    ) {
      icon()
    }
  }
}

enum class GlassButtonVariant {
  FILLED, TINTED, PLAIN
}

/**
 * Apple-Style Liquid Glass Button (@zakisheriff/liquid-glass).
 * Mandatory pattern: <GlassButton variant="filled" onClick={() => save()} disabled={saving}>Save</GlassButton>
 */
@Composable
fun GlassButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  variant: GlassButtonVariant = GlassButtonVariant.FILLED,
  enabled: Boolean = true,
  destructive: Boolean = false,
  icon: (@Composable () -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val isDark = LocalAppColors.current.isDark
  val shape = RoundedCornerShape(12.dp)
  val colors = LocalAppColors.current

  val surfaceModifier = when (variant) {
    GlassButtonVariant.FILLED -> {
      val fillBg = if (destructive) {
        AccentRed.copy(alpha = if (isDark) 0.85f else 0.90f)
      } else if (isDark) {
        Color.White.copy(alpha = 0.18f)
      } else {
        Color.White.copy(alpha = 0.88f)
      }
      Modifier
        .clip(shape)
        .background(fillBg)
        .border(0.75.dp, if (isDark) Color.White.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.12f), shape)
    }
    GlassButtonVariant.TINTED -> {
      val tintBg = if (destructive) {
        AccentRed.copy(alpha = 0.15f)
      } else {
        colors.oklchPrimary.copy(alpha = 0.15f)
      }
      Modifier
        .clip(shape)
        .background(tintBg)
        .border(0.75.dp, colors.oklchPrimary.copy(alpha = 0.35f), shape)
    }
    GlassButtonVariant.PLAIN -> {
      Modifier
        .clip(shape)
        .border(0.75.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f), shape)
    }
  }

  Box(
    modifier = modifier
      .defaultMinSize(minHeight = 44.dp)
      .then(surfaceModifier)
      .bounceClick(
        shape = shape,
        enabled = enabled,
        onClick = onClick
      )
      .padding(horizontal = 16.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (icon != null) {
        icon()
      }
      androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.material3.LocalContentColor provides when {
          destructive -> AccentRed
          variant == GlassButtonVariant.FILLED && isDark -> Color.White
          variant == GlassButtonVariant.FILLED && !isDark -> Color(0xFF1C1C1E)
          else -> InkPrimary
        }
      ) {
        content()
      }
    }
  }
}

/**
 * Data item for declarative GlassMenu.
 */
data class GlassMenuItemData(
  val label: String,
  val icon: (@Composable () -> Unit)? = null,
  val checked: Boolean = false,
  val destructive: Boolean = false,
  val onSelect: () -> Unit
)

/**
 * Declarative Apple-Style Liquid Glass Menu Popup (@zakisheriff/liquid-glass).
 * Anchored directly to trigger icon, blooming outward with authentic spring physics.
 */
@Composable
fun GlassMenu(
  icon: @Composable () -> Unit,
  items: List<GlassMenuItemData>,
  modifier: Modifier = Modifier,
  contentDescription: String = "More",
  buttonSize: Dp = 40.dp
) {
  com.example.ui.glass.LiquidGlassMenu(
    icon = icon,
    items = items.map {
      com.example.ui.glass.LiquidGlassMenuItemData(
        label = it.label,
        icon = it.icon,
        checked = it.checked,
        destructive = it.destructive,
        onSelect = it.onSelect
      )
    },
    modifier = modifier,
    contentDescription = contentDescription,
    buttonSize = buttonSize
  )
}

/**
 * Anchored Floating Liquid Glass Menu Container with Apple spring physics.
 */
@Composable
fun GlassMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
  val isDark = LocalAppColors.current.isDark
  val density = LocalDensity.current
  val hazeState = LocalHazeState.current
  val coroutineScope = rememberCoroutineScope()

  var isMounted by remember { mutableStateOf(false) }
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(expanded) {
    if (expanded) {
      isMounted = true
      // OPEN spring: dampingRatio = 0.74f, stiffness = 380f
      animProgress.animateTo(1f, spring(dampingRatio = 0.74f, stiffness = 380f))
    } else if (isMounted) {
      // CLOSE spring: dampingRatio = 0.66f, stiffness = 480f
      animProgress.animateTo(0f, spring(dampingRatio = 0.66f, stiffness = 480f))
      isMounted = false
    }
  }

  if (isMounted || animProgress.value > 0.01f) {
    val positionProvider = remember {
      object : PopupPositionProvider {
        override fun calculatePosition(
          anchorBounds: IntRect,
          windowSize: IntSize,
          layoutDirection: LayoutDirection,
          popupContentSize: IntSize
        ): IntOffset {
          val marginPx = (8 * density.density).toInt()
          val x = (anchorBounds.right - popupContentSize.width).coerceIn(marginPx, windowSize.width - popupContentSize.width - marginPx)
          val spaceBelow = windowSize.height - anchorBounds.bottom
          val y = if (spaceBelow >= popupContentSize.height + marginPx) {
            anchorBounds.bottom + (4 * density.density).toInt()
          } else {
            (anchorBounds.top - popupContentSize.height - (4 * density.density).toInt()).coerceAtLeast(marginPx)
          }
          return IntOffset(x, y)
        }
      }
    }

    Popup(
      popupPositionProvider = positionProvider,
      onDismissRequest = {
        coroutineScope.launch {
          animProgress.animateTo(0f, spring(dampingRatio = 0.66f, stiffness = 480f))
          onDismissRequest()
        }
      },
      properties = PopupProperties(
        focusable = true,
        dismissOnBackPress = true,
        dismissOnClickOutside = true
      )
    ) {
      val progress = animProgress.value
      val scale = 0.35f + 0.65f * progress
      val alpha = (progress / 0.22f).coerceIn(0f, 1f)
      val cornerRadius = (16.dp + (6.dp * progress)).coerceIn(16.dp, 22.dp)
      val menuShape = RoundedCornerShape(cornerRadius)

      val hazeModifier = if (hazeState != null) {
        Modifier.clip(menuShape).hazeEffect(state = hazeState) {
          blurRadius = 24.dp
          noiseFactor = if (isDark) 0.03f else 0f
          tints = buildList {
            if (isDark) {
              add(dev.chrisbanes.haze.HazeTint(Color(0xFF14151B).copy(alpha = 0.85f)))
            } else {
              add(dev.chrisbanes.haze.HazeTint(Color(0xFFFAFAFC).copy(alpha = 0.90f)))
            }
          }
        }
      } else {
        Modifier.clip(menuShape).cloudy(radius = 24)
      }

      val fallbackBg = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xF0181A22), Color(0xF2121319)))
      } else {
        Brush.verticalGradient(listOf(Color(0xF5FFFFFF), Color(0xFAF4F5F7)))
      }

      Box(
        modifier = modifier
          .widthIn(min = 200.dp, max = 280.dp)
          .graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
            this.alpha = alpha
            this.transformOrigin = TransformOrigin(1f, 0f)
            this.shadowElevation = (if (isDark) 20.dp else 14.dp).toPx() * progress
            this.spotShadowColor = if (isDark) Color.Black.copy(alpha = 0.60f) else Color.Black.copy(alpha = 0.16f)
            this.ambientShadowColor = if (isDark) Color.Black.copy(alpha = 0.30f) else Color.Black.copy(alpha = 0.06f)
            this.shape = menuShape
            this.clip = false
          }
          .then(hazeModifier)
          .background(fallbackBg, menuShape)
          .liquidGlassRim(
            shape = menuShape,
            isDark = isDark,
            borderWidth = 0.75.dp,
            variant = LiquidGlassVariant.Regular
          )
          .padding(6.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(2.dp),
          content = content
        )
      }
    }
  }
}

/**
 * Single item inside a GlassMenu with optional checkmark indicator.
 */
@Composable
fun GlassMenuItem(
  label: String,
  checked: Boolean = false,
  onSelect: () -> Unit,
  modifier: Modifier = Modifier,
  destructive: Boolean = false,
  icon: (@Composable () -> Unit)? = null
) {
  val isDark = LocalAppColors.current.isDark
  val context = LocalContext.current
  val itemShape = RoundedCornerShape(12.dp)

  val itemContentColor = when {
    destructive -> AccentRed
    checked -> InkPrimary
    else -> InkSecondary
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(itemShape)
      .defaultMinSize(minHeight = 44.dp)
      .bounceClick(shine = false) {
        VibrationHelper.tick(context)
        onSelect()
      }
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (icon != null) {
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
          icon()
        }
      }
      Text(
        text = label,
        style = TextStyle(
          fontSize = 15.sp,
          fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
          color = itemContentColor
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
    if (checked) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = "Selected",
        tint = if (destructive) AccentRed else LocalAppColors.current.oklchPrimary,
        modifier = Modifier.size(17.dp)
      )
    }
  }
}

/**
 * Apple-Style Liquid Glass Switch Toggle (@zakisheriff/liquid-glass).
 * Synced to app dark & light theme colors, with elastic sliding white glass thumb pill.
 */
@Composable
fun GlassSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  tint: Color? = null,
  enabled: Boolean = true,
  ariaLabel: String? = null
) {
  val isDark = LocalAppColors.current.isDark
  val context = LocalContext.current
  val density = LocalDensity.current
  val reduceMotion = remember { isReducedMotionEnabled(context) }
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val appThemeTint = if (isDark) {
    Color(0xFFE5A93C) // Warm Gold / PrimaryAccent in dark mode
  } else {
    Color(0xFF2C2C2E) // Deep Ink in light mode
  }
  val activeTint = tint ?: appThemeTint

  val trackWidth = 51.dp
  val trackHeight = 31.dp
  val thumbSize = 27.dp
  val thumbPadding = 2.dp

  val maxThumbOffset = with(density) { (trackWidth - thumbSize - (thumbPadding * 2)).toPx() }
  val targetOffset = if (checked) maxThumbOffset else 0f

  val animatedOffset by animateFloatAsState(
    targetValue = targetOffset,
    animationSpec = if (reduceMotion) androidx.compose.animation.core.snap() else spring(
      dampingRatio = 0.72f,
      stiffness = 520f
    ),
    label = "glassSwitchOffset"
  )

  val trackBgColor by animateColorAsState(
    targetValue = if (checked) {
      activeTint
    } else {
      if (isDark) Color(0x38353A45) else Color(0x33000000)
    },
    animationSpec = if (reduceMotion) androidx.compose.animation.core.snap() else tween(180),
    label = "glassSwitchTrackBg"
  )

  val trackBorderColor = if (checked) {
    activeTint.copy(alpha = 0.95f)
  } else {
    if (isDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.20f)
  }

  val pressScale by animateFloatAsState(
    targetValue = if (isPressed && !reduceMotion) 0.96f else 1f,
    animationSpec = if (reduceMotion) androidx.compose.animation.core.snap() else spring(dampingRatio = 0.65f, stiffness = 650f),
    label = "glassSwitchPressScale"
  )

  Box(
    modifier = modifier
      .size(width = trackWidth, height = 48.dp)
      .graphicsLayer {
        scaleX = pressScale
        scaleY = pressScale
      }
      .toggleable(
        value = checked,
        enabled = enabled,
        role = Role.Switch,
        interactionSource = interactionSource,
        indication = null
      ) { newValue ->
        VibrationHelper.tick(context)
        onCheckedChange(newValue)
      }
      .semantics {
        if (ariaLabel != null) {
          contentDescription = ariaLabel
        }
      },
    contentAlignment = Alignment.Center
  ) {
    // 1. Outer Pill Track with Hairline Specular Border
    Box(
      modifier = Modifier
        .size(trackWidth, trackHeight)
        .clip(CircleShape)
        .background(trackBgColor)
        .border(1.dp, trackBorderColor, CircleShape)
        .padding(thumbPadding),
      contentAlignment = Alignment.CenterStart
    ) {
      // 2. High-specular white rounded glass thumb with drop shadow
      Box(
        modifier = Modifier
          .graphicsLayer {
            translationX = animatedOffset
            shadowElevation = if (checked) with(density) { 3.dp.toPx() } else with(density) { 1.5.dp.toPx() }
            shape = CircleShape
            clip = false
            spotShadowColor = Color.Black.copy(alpha = 0.40f)
            ambientShadowColor = Color.Black.copy(alpha = 0.18f)
          }
          .size(thumbSize)
          .clip(CircleShape)
          .background(
            Brush.verticalGradient(
              listOf(
                Color.White,
                Color(0xFFF2F2F7)
              )
            )
          )
          .border(
            0.5.dp,
            Brush.verticalGradient(
              listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.50f)
              )
            ),
            CircleShape
          )
      )
    }
  }
}

/**
 * Apple-Style Liquid Glass Input Field (@zakisheriff/liquid-glass).
 * Supports text, search, and form inputs with a translucent glass background,
 * trailing action support (clear / mic), and clear focus state without dark smudges.
 */
@Composable
fun GlassInput(
  value: String = "",
  onValueChange: (String) -> Unit = {},
  modifier: Modifier = Modifier,
  query: String = value,
  onQueryChange: ((String) -> Unit)? = null,
  placeholder: String = "Search",
  type: String = "search",
  isSearch: Boolean = type == "search",
  leadingIcon: (@Composable () -> Unit)? = null,
  trailing: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = trailing,
  ariaLabel: String? = null,
  enabled: Boolean = true,
  onClear: (() -> Unit)? = null
) {
  val activeValue = if (onQueryChange != null) query else value
  val activeChange: (String) -> Unit = onQueryChange ?: onValueChange
  val isDark = LocalAppColors.current.isDark
  var isFocused by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(48.dp)
      .fluidLensedGlass(
        shape = RoundedCornerShape(14.dp),
        elevation = 1.dp,
        borderWidth = if (isFocused) 1.dp else 0.75.dp,
        isElevated = false
      )
      .padding(horizontal = 14.dp)
      .semantics {
        if (ariaLabel != null) {
          contentDescription = ariaLabel
        }
      },
    contentAlignment = Alignment.CenterStart
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (leadingIcon != null) {
        leadingIcon()
      } else if (isSearch) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          tint = InkMuted,
          modifier = Modifier.size(18.dp)
        )
      }

      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
        if (activeValue.isEmpty()) {
          Text(
            text = placeholder,
            style = TextStyle(
              fontSize = 15.sp,
              color = InkMuted
            )
          )
        }
        BasicTextField(
          value = activeValue,
          onValueChange = activeChange,
          enabled = enabled,
          textStyle = TextStyle(
            fontSize = 15.sp,
            color = InkPrimary
          ),
          cursorBrush = SolidColor(if (isDark) Color.White else Color.Black),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
        )
      }

      if (activeValue.isNotEmpty() && onClear != null) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .bounceClick { onClear() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Clear",
            tint = InkMuted,
            modifier = Modifier.size(18.dp)
          )
        }
      } else if (trailing != null) {
        trailing()
      } else if (trailingIcon != null) {
        trailingIcon()
      }
    }
  }
}

/**
 * Apple-Style Liquid Glass Bottom Sheet / Modal.
 * Renders a floating modal sheet with translucent glass material,
 * centered drag handle pill, and back-press handling.
 */
@Composable
fun GlassSheet(
  visible: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
  if (visible) {
    Dialog(onDismissRequest = onDismissRequest) {
      val isDark = LocalAppColors.current.isDark
      AnimatedDialogContent(transformOrigin = TransformOrigin(0.85f, 1.0f)) {
        Box(
          modifier = modifier
            .fillMaxWidth()
            .fluidLensedGlass(
              shape = OuterCornerShape,
              elevation = 12.dp,
              borderWidth = 0.75.dp,
              isElevated = true
            )
            .padding(20.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Drag handle pill
            Box(
              modifier = Modifier
                .size(width = 38.dp, height = 4.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0x35FFFFFF) else Color(0x30000000))
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
          }
        }
      }
    }
  }
}

/**
 * Unified Top Header System across all TDlxHT screens.
 * Strict structure: [Left Action (Back / Nav)] [Page Title (Centered / Anchored)] [Right Action (Primary Action)]
 * Guarantees identical typography (28.sp Bold), vertical rhythm, baseline alignment, and touch targets everywhere.
 */
@Composable
fun AppHeader(
  title: String,
  modifier: Modifier = Modifier,
  onBack: (() -> Unit)? = null,
  backLabel: String = "Back",
  actionIcon: ImageVector? = null,
  actionText: String? = null,
  actionContentDescription: String? = null,
  onActionClick: (() -> Unit)? = null,
  actionTestTag: String? = null,
  leftContent: (@Composable () -> Unit)? = null,
  rightContent: (@Composable () -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(56.dp)
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // 1. Left Action (Back or custom control)
    Box(
      modifier = Modifier.widthIn(min = 48.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      if (leftContent != null) {
        leftContent()
      } else if (onBack != null) {
        LiquidGlassIconButton(
          onClick = onBack,
          contentDescription = backLabel,
          size = 38.dp,
          variant = LiquidGlassVariant.Regular,
          modifier = Modifier.testTag("header_back_btn")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = InkPrimary,
            modifier = Modifier.size(19.dp)
          )
        }
      }
    }

    // 2. Centralized / Standardized Page Title
    Text(
      text = title,
      style = TextStyle(
        fontFamily = StrongFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = InkPrimary
      ),
      textAlign = TextAlign.Center,
      maxLines = 1,
      modifier = Modifier.weight(1f, fill = false)
    )

    // 3. Right Action (Primary Action button or contextual control)
    Box(
      modifier = Modifier.widthIn(min = 48.dp),
      contentAlignment = Alignment.CenterEnd
    ) {
      if (rightContent != null) {
        LiquidGlassEffectContainer(spacing = 8.dp) {
          rightContent()
        }
      } else if (actionText != null && onActionClick != null) {
        LiquidGlassButton(
          onClick = onActionClick,
          shape = RoundedCornerShape(10.dp),
          variant = LiquidGlassVariant.Prominent,
          modifier = Modifier.height(34.dp).then(if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier)
        ) {
          Text(
            text = actionText,
            style = TextStyle(
              fontFamily = StrongFontFamily,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = LocalAppColors.current.oklchPrimary
            )
          )
        }
      } else if (actionIcon != null && onActionClick != null) {
        LiquidGlassIconButton(
          onClick = onActionClick,
          contentDescription = actionContentDescription ?: "Action",
          size = 38.dp,
          variant = LiquidGlassVariant.Regular,
          modifier = if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier
        ) {
          Icon(
            imageVector = actionIcon,
            contentDescription = null,
            tint = InkPrimary,
            modifier = Modifier.size(19.dp)
          )
        }
      }
    }
  }
}

/**
 * Standardized Liquid Glass Filter Chip across Tasks, Habits, Tools, Calendar.
 * Understated, refined translucent fluid glass stadium pill with hairline rim and crisp text.
 */
@Composable
fun SketchFilterChip(
  label: String,
  selected: Boolean = false,
  isSelected: Boolean = selected,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String? = null
) {
  val active = selected || isSelected
  val isDark = LocalAppColors.current.isDark

  val textCol by animateColorAsState(
    targetValue = if (active) {
      if (isDark) Color.White else Color(0xFF111113)
    } else InkSecondary,
    animationSpec = tween(180, easing = FastOutSlowInEasing),
    label = "chipText"
  )

  Box(
    modifier = modifier
      .height(48.dp)
      .fluidLensedGlass(
        shape = CircleShape,
        elevation = if (active) 2.dp else 0.dp,
        borderWidth = if (active) 0.85.dp else 0.65.dp,
        isElevated = active,
        isInteractive = true,
        onClick = onClick
      )
      .padding(horizontal = 14.dp)
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 13.sp,
        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
        color = textCol
      )
    )
  }
}

/**
 * Reusable Minimal Empty State pattern.
 */
@Composable
fun AppEmptyState(
  icon: ImageVector,
  title: String,
  subtitle: String? = null,
  actionText: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .heroEntrance(delayMs = 60)
      .padding(horizontal = 24.dp, vertical = 40.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .border(1.dp, CardBorder, CircleShape)
        .clip(CircleShape)
        .background(CardBackground),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = InkMuted,
        modifier = Modifier.size(22.dp)
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = title,
      style = TextStyle(
        fontFamily = StrongFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = InkPrimary
      ),
      textAlign = TextAlign.Center
    )

    if (!subtitle.isNullOrBlank()) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        style = TextStyle(
          fontFamily = StrongFontFamily,
          fontSize = 13.sp,
          color = InkMuted
        ),
        textAlign = TextAlign.Center
      )
    }

    if (actionText != null && onAction != null) {
      Spacer(modifier = Modifier.height(14.dp))
      GlassButton(
        onClick = onAction
      ) {
        Text(
          text = actionText,
          style = TextStyle(
            fontFamily = StrongFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkPrimary
          )
        )
      }
    }
  }
}

@Composable
fun SketchCheckbox(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  sizeDp: Dp = 26.dp
) {
  val context = LocalContext.current
  val isDark = LocalAppColors.current.isDark
  val isReduced = remember { isReducedMotionEnabled(context) }

  // Spring pop-and-bounce scale on check toggle
  val popScale = remember { Animatable(1f) }
  val squashX = remember { Animatable(1f) }
  val squashY = remember { Animatable(1f) }

  LaunchedEffect(checked) {
    if (!isReduced) {
      if (checked) {
        launch {
          popScale.snapTo(0.82f)
          popScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.48f, stiffness = 400f)
          )
        }
        squashX.snapTo(1.18f)
        squashY.snapTo(0.82f)
        launch {
          squashX.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.50f, stiffness = 440f)
          )
        }
        squashY.animateTo(
          targetValue = 1f,
          animationSpec = spring(dampingRatio = 0.50f, stiffness = 440f)
        )
      } else {
        launch {
          popScale.snapTo(0.92f)
          popScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.60f, stiffness = 500f)
          )
        }
        squashX.snapTo(1f)
        squashY.snapTo(1f)
      }
    } else {
      popScale.snapTo(1f)
      squashX.snapTo(1f)
      squashY.snapTo(1f)
    }
  }

  val bgColor by animateColorAsState(
    targetValue = if (checked) {
      if (isDark) PrimaryAccentLime else PillBackgroundDark
    } else Color.Transparent,
    animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
    label = "chkBg"
  )
  val checkProgress by animateFloatAsState(
    targetValue = if (checked) 1f else 0f,
    animationSpec = if (isReduced) tween(0) else spring(dampingRatio = 0.65f, stiffness = 450f),
    label = "chkProgress"
  )
  val checkMarkColor = if (checked) {
    if (isDark) PrimaryDarkCanvas else PillTextDark
  } else Color.Transparent

  val borderColor by animateColorAsState(
    targetValue = if (checked) {
      if (isDark) PrimaryAccentLime else PillBackgroundDark
    } else CardBorder,
    animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
    label = "chkBrd"
  )

  Box(
    modifier = modifier
      .size(size = 48.dp)
      .elasticStretchPress(
        horizontalStretch = 1.015f,
        verticalSquash = 0.99f,
        shineShape = CircleShape
      ) {
        val next = !checked
        if (next) {
          VibrationHelper.success(context)
        } else {
          VibrationHelper.click(context)
        }
        onCheckedChange(next)
      }
      .testTag("task_checkbox"),
    contentAlignment = Alignment.Center
  ) {
    val ringColor = if (checked) {
      if (isDark) InkWashIvory else InkWashCharcoal
    } else {
      if (isDark) InkWashMist.copy(alpha = 0.5f) else InkWashCharcoal.copy(alpha = 0.35f)
    }

    Box(
      modifier = Modifier
        .size(sizeDp)
        .graphicsLayer {
          scaleX = squashX.value * popScale.value
          scaleY = squashY.value * popScale.value
        },
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.matchParentSize()) {
        val strokeWidth = 2.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeftOffset = Offset(
          (size.width - diameter) / 2f,
          (size.height - diameter) / 2f
        )
        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

        if (!checked) {
          // ProgressRingCheck Idle State: Solid refined circular rim
          drawCircle(
            color = ringColor,
            radius = diameter / 2f,
            center = center,
            style = Stroke(
              width = strokeWidth,
              cap = StrokeCap.Round
            )
          )
        } else {
          // ProgressRingCheck Done State: Solid ring with animated checkmark
          drawArc(
            color = ringColor,
            startAngle = -90f,
            sweepAngle = 360f * checkProgress,
            useCenter = false,
            topLeft = topLeftOffset,
            size = arcSize,
            style = Stroke(
              width = strokeWidth,
              cap = StrokeCap.Round
            )
          )
          // Solid subtle fill
          drawCircle(
            color = ringColor.copy(alpha = 0.12f),
            radius = diameter / 2f,
            center = center
          )
        }
      }

      if (checkProgress > 0.01f) {
        Canvas(modifier = Modifier.size(sizeDp * 0.58f)) {
          val strokeWidth = 2.4.dp.toPx()
          val w = size.width
          val h = size.height

          val p1 = Offset(w * 0.15f, h * 0.52f)
          val p2 = Offset(w * 0.42f, h * 0.80f)
          val p3 = Offset(w * 0.88f, h * 0.22f)

          val segment1Length = (p2 - p1).getDistance()
          val segment2Length = (p3 - p2).getDistance()
          val totalLength = segment1Length + segment2Length
          val currentLength = totalLength * checkProgress

          val path = androidx.compose.ui.graphics.Path()
          path.moveTo(p1.x, p1.y)

          if (currentLength <= segment1Length) {
            val frac = (currentLength / segment1Length).coerceIn(0f, 1f)
            val cur = Offset(
              p1.x + (p2.x - p1.x) * frac,
              p1.y + (p2.y - p1.y) * frac
            )
            path.lineTo(cur.x, cur.y)
          } else {
            path.lineTo(p2.x, p2.y)
            val frac = ((currentLength - segment1Length) / segment2Length).coerceIn(0f, 1f)
            val cur = Offset(
              p2.x + (p3.x - p2.x) * frac,
              p2.y + (p3.y - p2.y) * frac
            )
            path.lineTo(cur.x, cur.y)
          }

          drawPath(
            path = path,
            color = ringColor,
            style = Stroke(
              width = strokeWidth,
              cap = androidx.compose.ui.graphics.StrokeCap.Round,
              join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
          )
        }
      }
    }
  }
}

@Composable
fun SketchProgressBar(
  progress: Float,
  modifier: Modifier = Modifier,
  height: Dp = 10.dp
) {
  LiquidGlassLinearProgress(
    progress = progress,
    modifier = modifier,
    height = height
  )
}

@Composable
fun SketchInputField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  singleLine: Boolean = true,
  minLines: Int = 1,
  textStyle: TextStyle? = null,
  backgroundColor: Color? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
  val style = textStyle ?: TextStyle(fontSize = 16.sp, color = InkPrimary)
  val primaryColor = InkPrimary

  SketchCard(
    modifier = modifier.fillMaxWidth(),
    cornerRadius = 10.dp,
    borderWidth = 0.75.dp,
    backgroundColor = backgroundColor ?: CardBackground
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
      if (value.isEmpty()) {
        Text(
          text = placeholder,
          style = style.copy(color = InkMuted)
        )
      }
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = style,
        cursorBrush = SolidColor(primaryColor),
        keyboardOptions = keyboardOptions,
        modifier = Modifier.fillMaxWidth().testTag("input_$placeholder")
      )
    }
  }
}

/**
 * Standardized Liquid Glass Search Bar component.
 * Uses refined translucent glass input surface with smooth focus state and clear action.
 */
@Composable
fun SketchSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "Search tasks, habits, notes...",
  onClear: () -> Unit = { onQueryChange("") },
  leadingIcon: @Composable (() -> Unit)? = null
) {
  GlassInput(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier,
    placeholder = placeholder,
    isSearch = true,
    leadingIcon = leadingIcon,
    onClear = onClear
  )
}

/**
 * Smooth physics-based dialog container entrance animation.
 * Replaces abrupt modal popups with organic spring scale + fade entrance.
 */
@Composable
fun AnimatedDialogContent(
  modifier: Modifier = Modifier,
  transformOrigin: TransformOrigin = TransformOrigin.Center,
  content: @Composable () -> Unit
) {
  var isVisible by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val isReduced = remember { isReducedMotionEnabled(context) }

  LaunchedEffect(Unit) {
    isVisible = true
  }

  val scale by animateFloatAsState(
    targetValue = if (isVisible || isReduced) 1f else 0.88f,
    animationSpec = PhysicalMotionSpecs.DialogPopEntrance,
    label = "dialogScale"
  )
  val alpha by animateFloatAsState(
    targetValue = if (isVisible || isReduced) 1f else 0f,
    animationSpec = tween(160, easing = FastOutSlowInEasing),
    label = "dialogAlpha"
  )

  Box(
    modifier = modifier
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        this.transformOrigin = transformOrigin
      }
  ) {
    content()
  }
}

@Composable
fun DeleteConfirmationDialog(
  title: String,
  message: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    AnimatedDialogContent {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
          .fluidLensedGlass(shape = RoundedCornerShape(22.dp), elevation = 8.dp, isElevated = true)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Red trash icon in glass circular container
          Box(
            modifier = Modifier
              .size(54.dp)
              .fluidLensedGlass(shape = CircleShape, elevation = 2.dp, isElevated = true),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.DeleteOutline,
              contentDescription = "Delete",
              tint = AccentRed,
              modifier = Modifier.size(26.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = title,
            style = TextStyle(
              fontFamily = StrongFontFamily,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = InkPrimary
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = message,
            style = TextStyle(
              fontSize = 14.sp,
              color = InkSecondary,
              textAlign = TextAlign.Center,
              lineHeight = 20.sp
            )
          )

          Spacer(modifier = Modifier.height(24.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Cancel button (glass)
            LiquidGlassButton(
              onClick = onDismiss,
              shape = RoundedCornerShape(12.dp),
              variant = LiquidGlassVariant.Regular,
              modifier = Modifier.weight(1f).height(48.dp).testTag("dialog_cancel")
            ) {
              Text(
                text = "Cancel",
                style = TextStyle(
                  fontFamily = StrongFontFamily,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = InkPrimary
                )
              )
            }

            // Delete button (destructive glass)
            LiquidGlassButton(
              onClick = onConfirm,
              shape = RoundedCornerShape(12.dp),
              variant = LiquidGlassVariant.Prominent,
              tint = AccentRed,
              modifier = Modifier.weight(1f).height(48.dp).testTag("dialog_delete")
            ) {
              Text(
                text = "Delete",
                style = TextStyle(
                  fontFamily = StrongFontFamily,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = AccentRed
                )
              )
            }
          }
        }
      }
    }
  }
}

// =========================================================================
// Modern OKLCH Bento & Grouped Inset Components
// =========================================================================

/**
 * Clean grouped inset container inspired by iOS 18 and Linear.
 * Groups related settings or list items into a single rounded card with subtle borders.
 */
@Composable
fun GroupedInsetCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 18.dp,
  content: @Composable ColumnScope.() -> Unit
) {
  val isDark = LocalAppColors.current.isDark
  val surfaceColor = if (isDark) InkWashCharcoalSurface else InkWashIvoryCard
  val borderColor = if (isDark) InkWashCharcoalBorder else InkWashMist

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(cornerRadius))
      .background(surfaceColor)
      .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
  ) {
    content()
  }
}

/**
 * Hairline separator for inside grouped inset lists.
 */
@Composable
fun GroupedDivider(
  modifier: Modifier = Modifier,
  startIndent: Dp = 16.dp
) {
  val isDark = LocalAppColors.current.isDark
  val dividerColor = if (isDark) InkWashCharcoalBorder.copy(alpha = 0.5f) else InkWashMist.copy(alpha = 0.6f)
  HorizontalDivider(
    modifier = modifier.padding(start = startIndent),
    thickness = 0.8.dp,
    color = dividerColor
  )
}

/**
 * Bento Hero Card with rich specular highlight gradient and elevation tint.
 */
@Composable
fun BentoHeroCard(
  modifier: Modifier = Modifier,
  accentColor: Color? = null,
  cornerRadius: Dp = 22.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit
) {
  val shape = RoundedCornerShape(cornerRadius)

  Column(
    modifier = modifier
      .fluidLensedGlass(
        shape = shape,
        isElevated = true,
        isInteractive = onClick != null,
        onClick = onClick
      )
      .padding(18.dp)
  ) {
    content()
  }
}

/**
 * Color cycle picker matching user specification (Red, Yellow, Green, Blue, Purple, None).
 */
@Composable
fun ColorCyclePicker(
  selectedColorTag: String? = null,
  onColorTagSelected: (String?) -> Unit = {},
  selectedTag: String? = selectedColorTag,
  onTagSelected: ((String?) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val activeTag = selectedTag ?: selectedColorTag
  val activeCallback: (String?) -> Unit = onTagSelected ?: onColorTagSelected
  val isDark = LocalAppColors.current.isDark
  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // None / Default option
    val isNoneSelected = activeTag == null
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(
          if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
        )
        .border(
          width = if (isNoneSelected) 2.dp else 0.75.dp,
          color = if (isNoneSelected) (if (isDark) Color.White else Color.Black) else Color.Transparent,
          shape = CircleShape
        )
        .bounceClick(shape = CircleShape) {
          activeCallback(null)
        },
      contentAlignment = Alignment.Center
    ) {
      if (isNoneSelected) {
        Box(
          Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (isDark) Color.White else Color.Black)
        )
      }
    }

    // Color options
    com.example.data.model.AppColorTag.entries.forEach { tag ->
      val isSelected = tag.id.equals(activeTag, ignoreCase = true)
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(tag.color)
          .border(
            width = if (isSelected) 2.5.dp else 0.75.dp,
            color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.20f),
            shape = CircleShape
          )
          .graphicsLayer {
            if (isSelected) {
              scaleX = 1.12f
              scaleY = 1.12f
            }
          }
          .bounceClick(shape = CircleShape) {
            activeCallback(tag.id)
          },
        contentAlignment = Alignment.Center
      ) {
        if (isSelected) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = tag.displayName,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

/**
 * Tap-to-cycle color tag badge for task and habit list cards.
 */
@Composable
fun ColorTagBadge(
  colorTag: String?,
  onCycleNext: () -> Unit,
  modifier: Modifier = Modifier
) {
  val tag = com.example.data.model.AppColorTag.fromId(colorTag)
  val isDark = LocalAppColors.current.isDark
  val tagColor = tag?.color ?: if (isDark) Color(0x33FFFFFF) else Color(0x22000000)

  Box(
    modifier = modifier
      .size(12.dp)
      .clip(CircleShape)
      .background(tagColor)
      .border(
        0.5.dp,
        if (isDark) Color.White.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.20f),
        CircleShape
      )
      .bounceClick(scaleDown = 0.85f, shape = CircleShape) {
        onCycleNext()
      }
  )
}

/**
 * High-precision circular arc gauge drawn with smooth GPU Canvas shaders.
 */
@Composable
fun CircularProgressGauge(
  progress: Float,
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 8.dp,
  primaryColor: Color? = null,
  secondaryColor: Color = InkWashIvory,
  centerContent: (@Composable () -> Unit)? = null
) {
  val resolvedPrimaryColor = primaryColor ?: LocalAppColors.current.oklchPrimary
  val animatedProgress by animateFloatAsState(
    targetValue = progress.coerceIn(0f, 1f),
    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
    label = "gaugeProgress"
  )

  val trackColor = if (LocalAppColors.current.isDark) Color(0x334A4A4A) else Color(0x33CBCBCB)

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.matchParentSize()) {
      val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
      val diameter = size.minDimension - stroke.width
      val topLeftOffset = Offset(
        (size.width - diameter) / 2f,
        (size.height - diameter) / 2f
      )
      val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

      // Background track
      drawArc(
        color = trackColor,
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeftOffset,
        size = arcSize,
        style = stroke
      )

      // Monochrome progress arc
      if (animatedProgress > 0.001f) {
        val sweep = animatedProgress * 360f
        drawArc(
          color = resolvedPrimaryColor,
          startAngle = -90f,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = topLeftOffset,
          size = arcSize,
          style = stroke
        )
      }
    }

    if (centerContent != null) {
      centerContent()
    } else {
      Text(
        text = "${(animatedProgress * 100).toInt()}%",
        style = TextStyle(
          fontFamily = StrongFontFamily,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = InkPrimary
        )
      )
    }
  }
}

/**
 * Compact priority pill badge with Ink Wash tints.
 */
@Composable
fun PriorityPill(
  priority: com.example.data.model.TaskPriority,
  modifier: Modifier = Modifier
) {
  val isDark = LocalAppColors.current.isDark
  val priorityAccent = LocalAppColors.current.oklchPrimary
  val (pillColor, textColor, label) = when (priority) {
    com.example.data.model.TaskPriority.HIGH -> {
      if (isDark) Triple(InkWashCharcoalElevated, InkWashIvory, "High")
      else Triple(InkWashCharcoal, InkWashIvory, "High")
    }
    com.example.data.model.TaskPriority.MEDIUM -> {
      if (isDark) Triple(priorityAccent.copy(alpha = 0.20f), priorityAccent, "Med")
      else Triple(priorityAccent.copy(alpha = 0.12f), priorityAccent, "Med")
    }
    com.example.data.model.TaskPriority.LOW -> {
      if (isDark) Triple(InkWashMist.copy(alpha = 0.15f), InkWashMist, "Low")
      else Triple(InkWashMist.copy(alpha = 0.25f), InkWashCharcoal, "Low")
    }
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(pillColor)
      .border(0.8.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
      .padding(horizontal = 7.dp, vertical = 2.5.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = TextStyle(
        fontFamily = StrongFontFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
      )
    )
  }
}

/**
 * Momentum flame streak pill badge with bounce animation.
 */
@Composable
fun StreakFlameBadge(
  streakCount: Int,
  modifier: Modifier = Modifier
) {
  val flameColor = LocalAppColors.current.oklchPrimary
  val bgColor = if (LocalAppColors.current.isDark) InkWashCharcoalSurface else InkWashIvoryCard

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .border(1.dp, flameColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
      .padding(horizontal = 9.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text("🔥", fontSize = 12.sp)
    Text(
      text = "$streakCount",
      style = TextStyle(
        fontFamily = SpecialFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = flameColor
      )
    )
  }
}

