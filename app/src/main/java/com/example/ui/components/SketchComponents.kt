package com.example.ui.components

import android.content.Context
import com.example.ui.theme.SFProFontFamily
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.AccentTerracotta

import com.example.ui.theme.LocalAppColors

/**
 * Textured base canvas adhering to the #080813 Black Iris & #E4FD97 material design.
 * Provides a 75-80% dominant solid base color (#080813 in Dark Mode) enriched with a
 * broad, very subtle (20% intensity maximum) material depth and soft tonal graduation.
 * Zero noise, zero grain, zero repeating tiles, and 100% GPU-efficient.
 */
@Composable
fun Modifier.paperDotBackground(): Modifier {
  val baseBg = PaperBackground
  val isDark = LocalAppColors.current.isDark
  return this.drawBehind {
    // 1. Dominant Base Color Fill (75-80% visual base)
    drawRect(color = baseBg)

    if (isDark) {
      // 2. Subtle Material Depth & Tonal Character (~20% intensity):
      // Broad large-scale tonal graduation giving physical depth to #080813
      drawRect(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0x1A141424),
            Color(0x0A090916),
            Color.Transparent
          ),
          center = Offset(size.width * 0.22f, size.height * 0.16f),
          radius = size.maxDimension * 0.85f
        )
      )
      drawRect(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0x1210101E),
            Color.Transparent
          ),
          center = Offset(size.width * 0.82f, size.height * 0.78f),
          radius = size.maxDimension * 0.72f
        )
      )
    } else {
      // Light Mode: subtle soft large-scale tonal variation
      drawRect(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0x09000000),
            Color.Transparent
          ),
          center = Offset(size.width * 0.5f, 0f),
          radius = size.maxDimension * 0.65f
        )
      )
    }
  }
}

/**
 * Standard content bottom clearance for all scrolling screens above the floating navigation dock.
 * Ensures the last card/item is 100% visible and interactable.
 */
val FloatingNavContentBottomPadding: Dp = 155.dp

/**
 * Coherent physical motion specifications.
 * Fast, responsive, subtle, physically believable, and purposeful.
 * Free of cartoon wobbles, multiple oscillations, and artificial delays.
 */
object PhysicalMotionSpecs {
  val TactilePress = spring<Float>(
    dampingRatio = 0.65f,
    stiffness = 500f
  )
  val SpatialSlide = spring<Float>(
    dampingRatio = 0.75f,
    stiffness = 380f
  )
  val SpatialSlideDp = spring<Dp>(
    dampingRatio = 0.75f,
    stiffness = 380f
  )
  val Settle = spring<Float>(
    dampingRatio = 0.80f,
    stiffness = 320f
  )
  // GSAP-style physics curves for responsive bounces and pops
  val SpringPop = spring<Float>(
    dampingRatio = 0.50f,
    stiffness = 420f
  )
  val SpringElastic = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
  )
  val ModalSheetEntrance = spring<Float>(
    dampingRatio = 0.80f,
    stiffness = 380f
  )
  val DialogPopEntrance = spring<Float>(
    dampingRatio = 0.68f,
    stiffness = 440f
  )
  // Kern Society kinetic elastic curves (Priority 2)
  val KernKineticOvershoot = spring<Float>(
    dampingRatio = 0.54f,
    stiffness = 450f
  )
  val KernSquashSnap = spring<Float>(
    dampingRatio = 0.48f,
    stiffness = 480f
  )
  // Dheeraj Carpenter tactile tilt spring (Priority 1)
  val DheerajTiltSpring = spring<Float>(
    dampingRatio = 0.68f,
    stiffness = 380f
  )
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

  var hasAppeared by remember { mutableStateOf(false) }
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

  var hasAppeared by remember { mutableStateOf(false) }
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

  var hasAppeared by remember { mutableStateOf(false) }
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

  var hasAppeared by remember { mutableStateOf(false) }
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
  MICRO(scaleDown = 0.97f, pressedAlpha = 0.94f),
  SMALL(scaleDown = 0.94f, pressedAlpha = 0.88f),
  MEDIUM(scaleDown = 0.92f, pressedAlpha = 0.84f),
  MINIMAL(scaleDown = 0.985f, pressedAlpha = 0.96f)
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
fun Modifier.borderBeam(
  borderWidth: Dp = 1.6.dp,
  cornerRadius: Dp = 14.dp,
  durationMillis: Int = 3600,
  colors: List<Color>? = null
): Modifier = this

fun Modifier.shimmerBadge(
  highlightColor: Color? = null,
  durationMillis: Int = 2400
): Modifier = this

fun Modifier.breathingPulse(
  glowColor: Color? = null,
  durationMillis: Int = 2600
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
  enabled: Boolean = true,
  onLongClick: (() -> Unit)? = null,
  onClick: (() -> Unit)? = null
): Modifier {
  if (!enabled || (onClick == null && onLongClick == null)) return this
  return composed {
    val context = LocalContext.current
    val isReduced = remember { isReducedMotionEnabled(context) }
    val effectiveScale = if (isReduced) 1.0f else scaleDown
    val effectiveAlpha = if (isReduced) 0.94f else level.pressedAlpha

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
      targetValue = if (isPressed) effectiveScale else 1f,
      animationSpec = PhysicalMotionSpecs.TactilePress,
      label = "springScale"
    )
    val alpha by animateFloatAsState(
      targetValue = if (isPressed) effectiveAlpha else 1f,
      animationSpec = PhysicalMotionSpecs.TactilePress,
      label = "springAlpha"
    )

    this
      .scale(scale)
      .alpha(alpha)
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
 * Kern Society Kinetic Elastic Stretch & Squash press modifier (Priority 2).
 * Simulates variable font elasticity & volume conservation on touch down:
 * expands along the horizontal axis (X) while compressing vertically (Y),
 * then snaps back with a high-tension spring overshoot on release.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.elasticStretchPress(
  horizontalStretch: Float = 1.05f,
  verticalSquash: Float = 0.93f,
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
      targetValue = if (isPressed) 0.88f else 1f,
      animationSpec = spring(dampingRatio = 0.70f, stiffness = 450f),
      label = "kernAlpha"
    )

    this
      .graphicsLayer {
        this.scaleX = scaleX
        this.scaleY = scaleY
        this.alpha = alpha
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
 * Dheeraj Carpenter Tactile 3D Perspective Depth Modifier (Priority 1).
 * Applies 3D perspective camera distance, subtle rotation on interaction,
 * multi-layer ambient shadow depth, and a crisp specular edge highlight.
 */
fun Modifier.tactile3DDepth(
  cornerRadius: Dp = 16.dp,
  elevation: Dp = 6.dp,
  tiltDegrees: Float = 3.5f,
  isDark: Boolean = true
): Modifier = composed {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val currentElevation by animateDpAsState(
    targetValue = if (isPressed) 2.dp else elevation,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 400f),
    label = "tactileElevation"
  )
  val rotationX by animateFloatAsState(
    targetValue = if (isPressed) tiltDegrees else 0f,
    animationSpec = spring(dampingRatio = 0.65f, stiffness = 380f),
    label = "tactileRotX"
  )
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.98f else 1f,
    animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
    label = "tactileScale"
  )

  this
    .graphicsLayer {
      this.rotationX = rotationX
      this.scaleX = scale
      this.scaleY = scale
      cameraDistance = 16f * density
    }
    .shadow(
      elevation = currentElevation,
      shape = RoundedCornerShape(cornerRadius),
      ambientColor = if (isDark) Color(0x65000000) else Color(0x18000000),
      spotColor = if (isDark) Color(0x45000000) else Color(0x10000000)
    )
    .border(
      width = 1.dp,
      brush = Brush.verticalGradient(
        colors = if (isDark) {
          listOf(Color(0x35FFFFFF), Color(0x0EFFFFFF))
        } else {
          listOf(Color(0x40FFFFFF), Color(0x15000000))
        }
      ),
      shape = RoundedCornerShape(cornerRadius)
    )
}

/**
 * Dheeraj Carpenter Icon Nudge micro-interaction (Priority 1).
 * Causes trailing icons to dynamically nudge forward with physical snap on press.
 */
@Composable
fun Modifier.iconNudgePress(
  isTriggered: Boolean = false,
  nudgeDp: Dp = 4.dp
): Modifier {
  val context = LocalContext.current
  val isReduced = remember { isReducedMotionEnabled(context) }
  if (isReduced) return this

  val density = LocalDensity.current
  val nudgePx = with(density) { nudgeDp.toPx() }

  val translationX by animateFloatAsState(
    targetValue = if (isTriggered) nudgePx else 0f,
    animationSpec = spring(dampingRatio = 0.52f, stiffness = 450f),
    label = "iconNudge"
  )

  return this.graphicsLayer {
    this.translationX = translationX
  }
}

/**
 * Kern Society kinetic number squash & stretch counter animation (Priority 2).
 * Triggers an elastic squash and vertical snap whenever the provided counter value changes.
 */
@Composable
fun Modifier.kernSquashCounter(
  key: Any?
): Modifier {
  val context = LocalContext.current
  val isReduced = remember { isReducedMotionEnabled(context) }
  if (isReduced) return this

  val scaleXAnim = remember { Animatable(1f) }
  val scaleYAnim = remember { Animatable(1f) }

  LaunchedEffect(key) {
    if (key != null) {
      scaleXAnim.snapTo(1.15f)
      scaleYAnim.snapTo(0.85f)
      launch {
        scaleXAnim.animateTo(
          targetValue = 1f,
          animationSpec = spring(dampingRatio = 0.50f, stiffness = 420f)
        )
      }
      scaleYAnim.animateTo(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.50f, stiffness = 420f)
      )
    }
  }

  return this.graphicsLayer {
    this.scaleX = scaleXAnim.value
    this.scaleY = scaleYAnim.value
  }
}

@Composable
fun SketchCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 16.dp,
  backgroundColor: Color = CardBackground,
  borderColor: Color = CardBorder,
  borderWidth: Dp = 1.dp,
  elevation: Dp = 4.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val isDark = LocalAppColors.current.isDark
  val resolvedBg = if (backgroundColor == Color.Unspecified) CardBackground else backgroundColor

  Box(
    modifier = modifier
      .shadow(
        elevation = elevation,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = if (isDark) Color(0x60000000) else Color(0x14000000),
        spotColor = if (isDark) Color(0x40000000) else Color(0x0E000000)
      )
      .then(
        if (onClick != null) {
          Modifier.elasticStretchPress(
            horizontalStretch = 1.02f,
            verticalSquash = 0.97f,
            onClick = onClick
          )
        } else Modifier
      )
      .background(resolvedBg, RoundedCornerShape(cornerRadius))
      .border(
        width = borderWidth,
        brush = Brush.verticalGradient(
          colors = if (isDark) {
            listOf(Color(0x35FFFFFF), Color(0x0EFFFFFF))
          } else {
            listOf(Color(0x20000000), Color(0x10000000))
          }
        ),
        shape = RoundedCornerShape(cornerRadius)
      )
      .drawBehind {
        // Specular top highlight sheen for physical Apple-style Liquid Glass
        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              if (isDark) Color(0x28FFFFFF) else Color(0x35FFFFFF),
              Color.Transparent
            ),
            startY = 0f,
            endY = size.height * 0.45f
          ),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        )
      }
      .clip(RoundedCornerShape(cornerRadius))
  ) {
    content()
  }
}

/**
 * Canonical GlassCard composable implementing Apple Liquid Glass material.
 */
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 18.dp,
  backgroundColor: Color = CardBackground,
  borderColor: Color = CardBorder,
  borderWidth: Dp = 1.dp,
  elevation: Dp = 4.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  SketchCard(
    modifier = modifier,
    cornerRadius = cornerRadius,
    backgroundColor = backgroundColor,
    borderColor = borderColor,
    borderWidth = borderWidth,
    elevation = elevation,
    onClick = onClick,
    content = content
  )
}

/**
 * Apple-Style Liquid Glass Button — Icon with Visible Label Pattern.
 * Mandatory pattern: <GlassButton icon={<PlusIcon />}>Add Task</GlassButton>
 * Renders a tactile translucent stadium pill with specular sheen and spring press.
 */
@Composable
fun GlassButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: (@Composable () -> Unit)? = null,
  enabled: Boolean = true,
  content: @Composable () -> Unit
) {
  val isDark = LocalAppColors.current.isDark
  Box(
    modifier = modifier
      .bounceClick(scaleDown = 0.94f) {
        if (enabled) onClick()
      }
      .shadow(
        elevation = if (isDark) 4.dp else 2.dp,
        shape = CircleShape,
        ambientColor = if (isDark) Color(0x60000000) else Color(0x15000000),
        spotColor = if (isDark) Color(0x40000000) else Color(0x0E000000)
      )
      .clip(CircleShape)
      .background(
        color = if (isDark) Color(0xD825252A) else Color(0xE8FFFFFF)
      )
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          colors = if (isDark) {
            listOf(Color(0x50FFFFFF), Color(0x18FFFFFF))
          } else {
            listOf(Color(0x65FFFFFF), Color(0x20000000))
          }
        ),
        shape = CircleShape
      )
      .drawBehind {
        // Specular top highlight sheen
        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              if (isDark) Color(0x2EFFFFFF) else Color(0x45FFFFFF),
              Color.Transparent
            ),
            startY = 0f,
            endY = size.height * 0.45f
          ),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2, size.height / 2)
        )
      }
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
  size: Dp = 40.dp,
  icon: @Composable () -> Unit
) {
  val isDark = LocalAppColors.current.isDark
  Box(
    modifier = modifier
      .size(size)
      .bounceClick(scaleDown = 0.92f) { onClick() }
      .shadow(
        elevation = if (isDark) 6.dp else 3.dp,
        shape = CircleShape,
        ambientColor = if (isDark) Color(0x70000000) else Color(0x18000000),
        spotColor = if (isDark) Color(0x4D000000) else Color(0x10000000)
      )
      .clip(CircleShape)
      .background(
        color = if (isDark) Color(0xD825252A) else Color(0xEBFFFFFF)
      )
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          colors = if (isDark) {
            listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))
          } else {
            listOf(Color(0x80FFFFFF), Color(0x22000000))
          }
        ),
        shape = CircleShape
      )
      .drawBehind {
        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              if (isDark) Color(0x33FFFFFF) else Color(0x50FFFFFF),
              Color.Transparent
            ),
            startY = 0f,
            endY = size.height * 0.45f
          ),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2, size.height / 2)
        )
      },
    contentAlignment = Alignment.Center
  ) {
    icon()
  }
}

/**
 * Apple-Style Liquid Glass Menu Popup.
 * Provides a translucent floating glass context menu with checkmark states.
 */
@Composable
fun GlassMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
  if (expanded) {
    Dialog(onDismissRequest = onDismissRequest) {
      val isDark = LocalAppColors.current.isDark
      Box(
        modifier = modifier
          .widthIn(min = 180.dp, max = 280.dp)
          .shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(18.dp),
            ambientColor = Color.Black.copy(alpha = 0.6f),
            spotColor = Color.Black.copy(alpha = 0.4f)
          )
          .clip(RoundedCornerShape(18.dp))
          .background(if (isDark) Color(0xEE202024) else Color(0xF5FFFFFF))
          .border(
            1.dp,
            Brush.verticalGradient(
              listOf(Color(0x60FFFFFF), Color(0x15FFFFFF))
            ),
            RoundedCornerShape(18.dp)
          )
          .padding(8.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(4.dp),
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
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .bounceClick { onSelect() }
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (icon != null) {
        icon()
      }
      Text(
        text = label,
        style = TextStyle(
          fontFamily = SFProFontFamily,
          fontSize = 15.sp,
          fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
          color = when {
            destructive -> Color(0xFFE05D5D)
            checked -> InkPrimary
            else -> InkSecondary
          }
        )
      )
    }
    if (checked) {
      Text(
        text = "✓",
        style = TextStyle(
          fontFamily = SFProFontFamily,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = InkPrimary
        )
      )
    }
  }
}

/**
 * Canonical GlassSegmented composable implementing Apple Liquid Glass segmented tab.
 */
@Composable
fun GlassSegmented(
  items: List<String>,
  selectedIndex: Int,
  onItemSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  SketchSegmentedControl(
    items = items,
    selectedIndex = selectedIndex,
    onItemSelected = onItemSelected,
    modifier = modifier
  )
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
  actionContentDescription: String? = null,
  onActionClick: (() -> Unit)? = null,
  actionTestTag: String? = null,
  leftContent: (@Composable () -> Unit)? = null,
  rightContent: (@Composable () -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // 1. Left Action (Back or custom control)
    Box(
      modifier = Modifier.widthIn(min = 44.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      if (leftContent != null) {
        leftContent()
      } else if (onBack != null) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .border(1.dp, CardBorder, CircleShape)
            .clip(CircleShape)
            .background(CardBackground)
            .bounceClick { onBack() }
            .testTag("header_back_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = backLabel,
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
        fontFamily = SFProFontFamily,
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
      modifier = Modifier.widthIn(min = 44.dp),
      contentAlignment = Alignment.CenterEnd
    ) {
      if (rightContent != null) {
        rightContent()
      } else if (actionIcon != null && onActionClick != null) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .border(1.dp, CardBorder, CircleShape)
            .clip(CircleShape)
            .background(CardBackground)
            .bounceClick { onActionClick() }
            .then(if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = actionIcon,
            contentDescription = actionContentDescription,
            tint = InkPrimary,
            modifier = Modifier.size(19.dp)
          )
        }
      }
    }
  }
}

/**
 * Reusable Circular Icon Button following the brand reference design (smooth circle, 1.dp hairline border,
 * tactile spring bounce press, and contrast container fill).
 */
@Composable
fun SketchCircularIconButton(
  icon: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  size: androidx.compose.ui.unit.Dp = 38.dp,
  iconSize: androidx.compose.ui.unit.Dp = 19.dp,
  tint: Color = InkPrimary,
  backgroundColor: Color = CardBackground,
  borderColor: Color = CardBorder,
  testTag: String? = null
) {
  Box(
    modifier = modifier
      .size(size)
      .border(1.dp, borderColor, CircleShape)
      .clip(CircleShape)
      .background(backgroundColor)
      .elasticStretchPress(
        horizontalStretch = 1.08f,
        verticalSquash = 0.91f,
        enabled = enabled,
        onClick = onClick
      )
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = tint,
      modifier = Modifier.size(iconSize)
    )
  }
}

/**
 * Standardized Filter Chip across Tasks, Habits, Tools, Calendar.
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
  val bg by animateColorAsState(
    targetValue = if (active) PillBackgroundDark else Color.Transparent,
    animationSpec = tween(180, easing = FastOutSlowInEasing),
    label = "chipBg"
  )
  val textCol by animateColorAsState(
    targetValue = if (active) PillTextDark else InkPrimary,
    animationSpec = tween(180, easing = FastOutSlowInEasing),
    label = "chipText"
  )
  val borderCol = if (active) PillBackgroundDark else CardBorder

  Box(
    modifier = modifier
      .height(34.dp)
      .border(1.dp, borderCol, RoundedCornerShape(10.dp))
      .clip(RoundedCornerShape(10.dp))
      .background(bg)
      .elasticStretchPress(
        horizontalStretch = 1.06f,
        verticalSquash = 0.93f,
        onClick = onClick
      )
      .padding(horizontal = 14.dp)
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = TextStyle(
        fontFamily = SFProFontFamily,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
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
        fontFamily = SFProFontFamily,
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
          fontFamily = SFProFontFamily,
          fontSize = 13.sp,
          color = InkMuted
        ),
        textAlign = TextAlign.Center
      )
    }

    if (actionText != null && onAction != null) {
      Spacer(modifier = Modifier.height(14.dp))
      Box(
        modifier = Modifier
          .height(36.dp)
          .background(PillBackgroundDark, RoundedCornerShape(10.dp))
          .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
          .clip(RoundedCornerShape(10.dp))
          .bounceClick { onAction() }
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = actionText,
          style = TextStyle(
            fontFamily = SFProFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = PillTextDark
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

  // Kern Society kinetic elastic squash & stretch on check completion (Priority 2)
  val squashX = remember { Animatable(1f) }
  val squashY = remember { Animatable(1f) }

  LaunchedEffect(checked) {
    if (checked && !isReduced) {
      squashX.snapTo(1.16f)
      squashY.snapTo(0.84f)
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
      .size(size = 46.dp)
      .elasticStretchPress(
        horizontalStretch = 1.10f,
        verticalSquash = 0.90f
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
    Box(
      modifier = Modifier
        .size(sizeDp)
        .graphicsLayer {
          scaleX = squashX.value
          scaleY = squashY.value
        }
        .background(bgColor, RoundedCornerShape(7.dp))
        .border(1.6.dp, borderColor, RoundedCornerShape(7.dp)),
      contentAlignment = Alignment.Center
    ) {
      if (checkProgress > 0.01f) {
        Canvas(modifier = Modifier.size(sizeDp * 0.62f)) {
          val strokeWidth = 2.2.dp.toPx()
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
            color = checkMarkColor,
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
fun SketchSegmentedControl(
  items: List<String>,
  selectedIndex: Int,
  onItemSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isDark = LocalAppColors.current.isDark

  SketchCard(
    modifier = modifier.fillMaxWidth(),
    cornerRadius = 14.dp,
    borderWidth = 1.dp
  ) {
    androidx.compose.foundation.layout.BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
        .pointerInput(items, selectedIndex) {
          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val count = items.size
            fun selectForX(x: Float) {
              if (count == 0 || size.width <= 0) return
              val itemWidth = size.width / count.toFloat()
              val targetIndex = (x / itemWidth).toInt().coerceIn(0, count - 1)
              if (targetIndex != selectedIndex) {
                onItemSelected(targetIndex)
                VibrationHelper.tick(context)
              }
            }
            selectForX(down.position.x)
            while (true) {
              val event = awaitPointerEvent()
              val change = event.changes.firstOrNull() ?: break
              if (!change.pressed) break
              selectForX(change.position.x)
            }
          }
        }
    ) {
      val count = items.size.coerceAtLeast(1)
      val segmentWidth = maxWidth / count
      val safeIndex = selectedIndex.coerceIn(0, count - 1)

      val animatedOffset by animateDpAsState(
        targetValue = segmentWidth * safeIndex,
        animationSpec = PhysicalMotionSpecs.SpatialSlideDp,
        label = "segmentOffset"
      )

      // 1. Single Continuous Physical Sliding Indicator Capsule
      Box(
        modifier = Modifier
          .offset(x = animatedOffset)
          .width(segmentWidth)
          .height(34.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(PillBackgroundDark)
          .border(
            1.dp,
            PillBackgroundDark,
            RoundedCornerShape(10.dp)
          )
      )

      // 2. Interactive labels on top
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        items.forEachIndexed { index, title ->
          val isSelected = index == selectedIndex
          val animatedTextColor by animateColorAsState(
            targetValue = if (isSelected) {
              PillTextDark
            } else InkSecondary,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            label = "segText_$index"
          )

          Box(
            modifier = Modifier
              .weight(1f)
              .height(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .bounceClick(level = InteractionLevel.MICRO, scaleDown = 0.97f) {
                if (index != selectedIndex) {
                  onItemSelected(index)
                  VibrationHelper.tick(context)
                }
              }
              .testTag("segment_$title"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = title,
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 13.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = animatedTextColor
              )
            )
          }
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
  val clamped = progress.coerceIn(0f, 1f)
  var initialLoaded by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    initialLoaded = true
  }
  val targetVal = if (initialLoaded) clamped else 0f
  val animatedProgress by animateFloatAsState(
    targetValue = targetVal,
    animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
    label = "progressBar"
  )
  Box(
    modifier = modifier
      .height(height)
      .fillMaxWidth()
      .clip(RoundedCornerShape(5.dp))
      .background(ProgressBarTrack)
      .border(1.2.dp, CardBorder, RoundedCornerShape(5.dp))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(animatedProgress)
        .height(height)
        .background(ProgressBarFilled, RoundedCornerShape(4.dp))
    )
  }
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
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
  val style = textStyle ?: TextStyle(fontSize = 16.sp, color = InkPrimary)
  val primaryColor = InkPrimary

  SketchCard(
    modifier = modifier.fillMaxWidth(),
    cornerRadius = 10.dp,
    borderWidth = 1.5.dp
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
 * Standardized, production-grade Search Bar component.
 * Fixes fake cursor bug, overlay displacement, and provides smooth focus state & clear action.
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
  var isFocused by remember { mutableStateOf(false) }
  val borderColor by animateColorAsState(
    targetValue = if (isFocused) InkPrimary else CardBorder,
    animationSpec = tween(180, easing = FastOutSlowInEasing),
    label = "searchBorder"
  )

  SketchCard(
    modifier = modifier.fillMaxWidth(),
    cornerRadius = 14.dp,
    borderColor = borderColor,
    borderWidth = 1.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (leadingIcon != null) {
        leadingIcon()
        Spacer(modifier = Modifier.width(10.dp))
      } else {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = if (isFocused) InkPrimary else InkSecondary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
      }

      Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.CenterStart
      ) {
        if (query.isEmpty()) {
          Text(
            text = placeholder,
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 15.sp,
              color = InkMuted
            )
          )
        }
        BasicTextField(
          value = query,
          onValueChange = onQueryChange,
          singleLine = true,
          textStyle = TextStyle(
            fontFamily = SFProFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = InkPrimary
          ),
          cursorBrush = SolidColor(InkPrimary),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .testTag("search_text_input")
        )
      }

      if (query.isNotEmpty()) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .bounceClick { onClear() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Clear search",
            tint = InkSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

/**
 * Smooth physics-based dialog container entrance animation.
 * Replaces abrupt modal popups with organic spring scale + fade entrance.
 */
@Composable
fun AnimatedDialogContent(
  modifier: Modifier = Modifier,
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
      SketchCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        cornerRadius = 20.dp,
        borderWidth = 2.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Red trash icon in circle
          Box(
            modifier = Modifier
              .size(54.dp)
              .border(1.8.dp, AccentRed, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.DeleteOutline,
              contentDescription = "Delete",
              tint = AccentRed,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = title,
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 24.sp,
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
            // Cancel button (black outline)
            Box(
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .border(1.5.dp, CardBorder, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .bounceClick { onDismiss() }
                .testTag("dialog_cancel"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Cancel",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = InkPrimary
                )
              )
            }

            // Delete button (red text)
            Box(
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .border(1.5.dp, AccentRed, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .bounceClick { onConfirm() }
                .testTag("dialog_delete"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Delete",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 18.sp,
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