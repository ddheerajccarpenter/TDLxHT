package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
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
import com.example.ui.theme.LiquidGlassLensBottomDark
import com.example.ui.theme.LiquidGlassLensMidDark
import com.example.ui.theme.LiquidGlassLensTopDark
import com.example.util.VibrationHelper
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// =========================================================================
// AUTHENTIC LIQUID GLASS DESIGN SYSTEM (ZERO BLUR)
//
// Constructed strictly via:
// 1. Alpha-stratified translucent surfaces revealing underlying canvas activity.
// 2. Dual-boundary specular hairline border (40% white top -> 10% lateral decay).
// 3. Overhead convex optical caustic dome (radial dispersion without blur).
// 4. Lower meniscus dark shading creating physical material separation.
// 5. Curved optical glass lens for fluid gliding selections with spring physics.
//
// STRICTLY ZERO BLUR: No RenderEffect, no Modifier.blur, no BlurMaskFilter.
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
  topFill: Color = LiquidGlassCardTopDark,
  bottomFill: Color = LiquidGlassCardBottomDark,
  specularBorderTop: Color = LiquidGlassBorderSpecularTop,
  specularBorderMid: Color = LiquidGlassBorderSpecularMid,
  specularBorderBottom: Color = LiquidGlassBorderSpecularBottom
): Modifier = this
  .clip(shape)
  .drawBehind {
    // 1. Translucent Body Fill (Transparent glass revealing underlying canvas)
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(topFill, bottomFill)
      )
    )

    // 2. Overhead Convex Caustic Dome (Active optical light-gathering, crisp zero-blur)
    val causticRadius = (size.maxDimension * 0.85f).coerceAtLeast(1f)
    drawRect(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x28FFFFFF), // Peak caustic focus
          Color(0x08FFFFFF), // Soft optical dispersion
          Color.Transparent  // Clean decay
        ),
        center = Offset(size.width * 0.5f, -size.height * 0.14f),
        radius = causticRadius
      )
    )

    // 3. Lower Meniscus Dark Shading (Crisp separation from underlying surface)
    drawRect(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x2C000000), // Dark refraction rim
          Color.Transparent
        ),
        center = Offset(size.width * 0.5f, size.height * 1.15f),
        radius = (size.width * 0.65f).coerceAtLeast(1f)
      )
    )
  }
  .border(
    width = borderWidth,
    brush = Brush.verticalGradient(
      colors = listOf(
        specularBorderTop,
        specularBorderMid,
        specularBorderBottom
      )
    ),
    shape = shape
  )

/**
 * Curved Optical Glass Lens Modifier (Zero Blur).
 * Specifically engineered for moving selection indicators (Segmented Controls & Nav Dock).
 * Features brighter apex convex highlight, side hairlines, and optical depth.
 */
fun Modifier.liquidGlassLens(
  shape: Shape = RoundedCornerShape(12.dp),
  borderWidth: Dp = 0.75.dp
): Modifier = this
  .clip(shape)
  .drawBehind {
    // 1. Multi-tone Convex Glass Lens Body
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(
          LiquidGlassLensTopDark,
          LiquidGlassLensMidDark,
          LiquidGlassLensBottomDark
        )
      )
    )

    // 2. Overhead Specular Light Concentration
    val lensHighlightRadius = (size.width * 0.70f).coerceAtLeast(1f)
    drawRect(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x38FFFFFF),
          Color(0x10FFFFFF),
          Color.Transparent
        ),
        center = Offset(size.width * 0.5f, 0f),
        radius = lensHighlightRadius
      )
    )

    // 3. Bottom Refraction Shading
    drawRect(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x35000000),
          Color.Transparent
        ),
        center = Offset(size.width * 0.5f, size.height * 1.10f),
        radius = (size.width * 0.55f).coerceAtLeast(1f)
      )
    )
  }
  .border(
    width = borderWidth,
    brush = Brush.verticalGradient(
      colors = listOf(
        Color(0x80FFFFFF), // Ultra-crisp specular top rim
        Color(0x35FFFFFF), // Lateral edge reflection
        Color(0x10FFFFFF)  // Bottom decay
      )
    ),
    shape = shape
  )

/**
 * Capsule Glass Modifier (Zero Blur).
 */
fun Modifier.liquidGlassCapsule(
  shape: Shape = RoundedCornerShape(20.dp),
  borderWidth: Dp = 0.75.dp
): Modifier = this.liquidGlassSurface(
  shape = shape,
  borderWidth = borderWidth,
  topFill = LiquidGlassCapsuleTopDark,
  bottomFill = LiquidGlassCapsuleBottomDark
)

/**
 * 1. LiquidGlassCard
 * Large smoked glass panel for Today's Momentum, Time Blocks, and Card sections.
 */
@Composable
fun LiquidGlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(24.dp),
  borderWidth: Dp = 0.75.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val clickModifier = if (onClick != null) {
    Modifier.bounceClick(onClick = onClick)
  } else Modifier

  Box(
    modifier = modifier
      .liquidGlassSurface(
        shape = shape,
        borderWidth = borderWidth,
        topFill = LiquidGlassCardTopDark,
        bottomFill = LiquidGlassCardBottomDark
      )
      .then(clickModifier),
    content = content
  )
}

/**
 * 2. LiquidGlassCapsule
 * Unified transparent glass capsule for metric groups and segmented controls.
 */
@Composable
fun LiquidGlassCapsule(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(20.dp),
  borderWidth: Dp = 0.75.dp,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier
      .liquidGlassSurface(
        shape = shape,
        borderWidth = borderWidth,
        topFill = LiquidGlassCapsuleTopDark,
        bottomFill = LiquidGlassCapsuleBottomDark
      ),
    content = content
  )
}

/**
 * 3. LiquidGlassSegmentedControl
 * Moving curved glass lens gliding smoothly between segments with spring physics.
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

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(44.dp)
      .liquidGlassCapsule(shape = RoundedCornerShape(14.dp), borderWidth = 0.75.dp)
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(3.dp)
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
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 420f),
        label = "glassSegmentOffset"
      )

      // 1. Gliding Optical Glass Lens
      Box(
        modifier = Modifier
          .offset(x = animatedOffset)
          .width(segmentWidth)
          .fillMaxHeight()
          .liquidGlassLens(shape = RoundedCornerShape(11.dp), borderWidth = 0.75.dp)
      )

      // 2. Foreground Typography Labels
      Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        items.forEachIndexed { index, title ->
          val isSelected = index == selectedIndex
          val textColor by animateColorAsState(
            targetValue = if (isSelected) Color(0xFFF5F5F7) else Color(0xFF8E9298),
            animationSpec = tween(160),
            label = "glassSegmentText"
          )

          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                if (selectedIndex != index) {
                  onItemSelected(index)
                  VibrationHelper.tick(context)
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = title,
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
              )
            )
          }
        }
      }
    }
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
  icon: (@Composable () -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  Box(
    modifier = modifier
      .defaultMinSize(minHeight = 44.dp)
      .liquidGlassSurface(
        shape = shape,
        borderWidth = 0.75.dp,
        topFill = LiquidGlassButtonResting,
        bottomFill = LiquidGlassCardBottomDark
      )
      .bounceClick {
        VibrationHelper.click(context)
        onClick()
      }
      .padding(horizontal = 14.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (icon != null) {
        icon()
      }
      content()
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
  size: Dp = 44.dp,
  shape: Shape = CircleShape,
  icon: @Composable () -> Unit
) {
  val context = LocalContext.current
  Box(
    modifier = modifier
      .size(size)
      .liquidGlassSurface(
        shape = shape,
        borderWidth = 0.75.dp,
        topFill = LiquidGlassButtonResting,
        bottomFill = LiquidGlassCardBottomDark
      )
      .bounceClick {
        VibrationHelper.click(context)
        onClick()
      },
    contentAlignment = Alignment.Center
  ) {
    icon()
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
  onQuickAdd: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val isDark = LocalAppColors.current.isDark
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
      indicatorX.animateTo(
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
    targetValue = if (isHoldingDock) 1.02f else 1.0f,
    animationSpec = spring(dampingRatio = 0.75f, stiffness = 380f),
    label = "dockScale"
  )

  val fabRotation by animateFloatAsState(
    targetValue = if (isQuickAddOpen) 45f else 0f,
    animationSpec = spring(dampingRatio = 0.60f, stiffness = 450f),
    label = "fabRotation"
  )

  Box(
    modifier = modifier
      .height(50.dp)
      .graphicsLayer {
        scaleX = dockScale
        scaleY = dockScale
      }
      .liquidGlassCapsule(shape = CircleShape, borderWidth = 0.75.dp)
      .padding(horizontal = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
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
                  indicatorX.animateTo(
                    slotWidth * (targetSlot + 0.5f),
                    spring(dampingRatio = 0.75f, stiffness = 400f)
                  )
                }
              }
            } else {
              val tappedSlot = (down.position.x / slotWidth).toInt().coerceIn(0, totalSlots - 1)
              if (hasCenterFab && tappedSlot == 2) {
                VibrationHelper.click(context)
                com.example.util.SoundManager.playButtonClick(context)
                onQuickAdd?.invoke()
              } else {
                val tappedTab = tabForSlotIndex(tappedSlot)
                if (tappedTab != null) {
                  VibrationHelper.tick(context)
                  com.example.util.SoundManager.playSwipeNav(context)
                  onDestinationSelected(destinations[tappedTab])
                  val targetSlot = slotIndexForTab(tappedTab)
                  coroutineScope.launch {
                    indicatorX.animateTo(
                      slotWidth * (targetSlot + 0.5f),
                      spring(dampingRatio = 0.75f, stiffness = 400f)
                    )
                  }
                }
              }
            }
          }
        },
      contentAlignment = Alignment.CenterStart
    ) {
      // 1. Sliding Curved Optical Glass Lens
      if (slotWidth > 0f) {
        val capsuleWidthPx = (slotWidth * 0.90f).coerceAtLeast(with(density) { 34.dp.toPx() })
        val capsuleWidthDp = with(density) { capsuleWidthPx.toDp() }

        Box(
          modifier = Modifier
            .width(capsuleWidthDp)
            .height(34.dp)
            .graphicsLayer {
              translationX = indicatorX.value - (capsuleWidthPx / 2f)
            }
            .liquidGlassLens(shape = CircleShape, borderWidth = 0.75.dp)
        )
      }

      // 2. Icon Row
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
              VibrationHelper.tick(context)
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
              VibrationHelper.tick(context)
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
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                VibrationHelper.click(context)
                com.example.util.SoundManager.playButtonClick(context)
                onQuickAdd?.invoke()
              },
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .liquidGlassLens(shape = CircleShape, borderWidth = 0.75.dp),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isQuickAddOpen) "Close create sheet" else "Quick Create",
                tint = if (isDark) Color(0xFFF5F5F7) else Color(0xFF1E2022),
                modifier = Modifier
                  .size(19.dp)
                  .graphicsLayer(rotationZ = fabRotation)
              )
            }
          }
          // Tab 2: Tools
          LiquidGlassNavItem(
            destination = destinations[2],
            isSelected = selectedTab == 2,
            onClick = {
              VibrationHelper.tick(context)
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
              VibrationHelper.tick(context)
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
                VibrationHelper.tick(context)
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
 * Skips recomposition during continuous finger gestures or indicator interpolation.
 */
@Composable
fun LiquidGlassNavItem(
  destination: AppDestination,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val iconColor by animateColorAsState(
    targetValue = if (isSelected) Color(0xFFF5F5F7) else Color(0xFF8E9298),
    animationSpec = tween(durationMillis = 180),
    label = "navIconColor"
  )
  val iconScale by animateFloatAsState(
    targetValue = if (isSelected) 1.08f else 1.0f,
    animationSpec = spring(dampingRatio = 0.7f, stiffness = 420f),
    label = "navIconScale"
  )

  Box(
    modifier = modifier
      .fillMaxHeight()
      .testTag("nav_${destination.name.lowercase()}")
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = if (isSelected) destination.iconFilled else destination.iconOutlined,
      contentDescription = destination.label,
      tint = iconColor,
      modifier = Modifier
        .size(23.dp)
        .graphicsLayer {
          scaleX = iconScale
          scaleY = iconScale
        }
    )
  }
}

/**
 * 7. LiquidGlassFloatingActionButton
 * Independent 54dp floating circular Liquid Glass button for Quick Create.
 */
@Composable
fun LiquidGlassFloatingActionButton(
  onClick: () -> Unit,
  isOpen: Boolean,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val fabRotation by animateFloatAsState(
    targetValue = if (isOpen) 45f else 0f,
    animationSpec = spring(dampingRatio = 0.60f, stiffness = 450f),
    label = "fabRotation"
  )

  Box(
    modifier = modifier
      .size(54.dp)
      .liquidGlassLens(shape = CircleShape, borderWidth = 0.85.dp)
      .bounceClick {
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
      tint = Color(0xFFF5F5F7),
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
  trackColor: Color = Color(0x18FFFFFF),
  progressColor: Color = Color(0xFFE2E8F0)
) {
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
        color = trackColor,
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
          brush = Brush.sweepGradient(
            colors = listOf(
              progressColor.copy(alpha = 0.80f),
              progressColor,
              progressColor
            )
          ),
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
        color = Color(0xFFF5F5F7)
      )
    )
  }
}

