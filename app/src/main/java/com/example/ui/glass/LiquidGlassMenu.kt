package com.example.ui.glass

import com.skydoves.cloudy.cloudy

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.ui.components.LiquidGlassIconButton
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.StrongFontFamily
import com.example.util.VibrationHelper
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Menu horizontal alignment relative to its anchor trigger.
 */
enum class GlassMenuAlign {
  Start,
  End
}

/**
 * Data item model for Liquid Glass pull-down menus (@zakisheriff/liquid-glass).
 */
data class LiquidGlassMenuItemData(
  val label: String,
  val icon: (@Composable () -> Unit)? = null,
  val checked: Boolean = false,
  val destructive: Boolean = false,
  val disabled: Boolean = false,
  val onSelect: () -> Unit
)

/**
 * Authentic Apple iOS 26 Liquid Glass Pull-Down Menu.
 *
 * Implements physics and animations from https://liquidglass.theatom.lk/docs:
 * - Organically blooms out of its trigger button and folds back into it.
 * - Tuned spring physics:
 *     OPEN:  dampingRatio = 0.74, stiffness = 380 (organic expansion with subtle jelly overshoot).
 *     CLOSE: dampingRatio = 0.66, stiffness = 480 (snappy folding collapse).
 * - Interactive slide-to-select: user can slide their finger across the items with tactile
 *   haptic ticks on hover, and releasing over an item triggers selection.
 * - Directional Snell's-law specular rims via liquidGlassRim and frosted glass backdrop blur.
 */
@Composable
fun LiquidGlassMenu(
  icon: @Composable () -> Unit,
  items: List<LiquidGlassMenuItemData>,
  modifier: Modifier = Modifier,
  contentDescription: String = "Options",
  align: GlassMenuAlign = GlassMenuAlign.End,
  width: Dp = 230.dp,
  buttonSize: Dp = 40.dp
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    LiquidGlassIconButton(
      onClick = { expanded = true },
      contentDescription = contentDescription,
      size = buttonSize
    ) {
      icon()
    }

    LiquidGlassMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      align = align,
      width = width
    ) {
      items.forEach { item ->
        item(
          label = item.label,
          icon = item.icon,
          checked = item.checked,
          destructive = item.destructive,
          disabled = item.disabled,
          onSelect = {
            expanded = false
            item.onSelect()
          }
        )
      }
    }
  }
}

/**
 * Scope for constructing items inside a LiquidGlassMenu.
 */
interface LiquidGlassMenuScope {
  fun item(
    label: String,
    icon: (@Composable () -> Unit)? = null,
    checked: Boolean = false,
    destructive: Boolean = false,
    disabled: Boolean = false,
    testTag: String? = null,
    onSelect: () -> Unit
  )
}

private class LiquidGlassMenuScopeImpl : LiquidGlassMenuScope {
  val items = mutableListOf<LiquidGlassMenuItemData>()

  override fun item(
    label: String,
    icon: (@Composable () -> Unit)?,
    checked: Boolean,
    destructive: Boolean,
    disabled: Boolean,
    testTag: String?,
    onSelect: () -> Unit
  ) {
    items.add(
      LiquidGlassMenuItemData(
        label = label,
        icon = icon,
        checked = checked,
        destructive = destructive,
        disabled = disabled,
        onSelect = onSelect
      )
    )
  }
}

/**
 * Controlled Liquid Glass Menu popup container with authentic bloom and fold-back physics.
 */
@Composable
fun LiquidGlassMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  align: GlassMenuAlign = GlassMenuAlign.End,
  width: Dp = 230.dp,
  content: @Composable LiquidGlassMenuScope.() -> Unit
) {
  val scopeImpl = remember { LiquidGlassMenuScopeImpl() }
  scopeImpl.items.clear()
  scopeImpl.content()
  val items = scopeImpl.items

  val context = LocalContext.current
  val density = LocalDensity.current
  val isDark = LocalAppColors.current.isDark
  val hazeState = LocalHazeState.current
  val coroutineScope = rememberCoroutineScope()

  // Track mounting state to allow fold-back exit animation before Popup unmounts
  var isMounted by remember { mutableStateOf(false) }
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(expanded) {
    if (expanded) {
      isMounted = true
      // OPEN spring: dampingRatio = 0.74f, stiffness = 380f (~360ms organic jelly bloom)
      animProgress.animateTo(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.74f, stiffness = 380f)
      )
    } else if (isMounted) {
      // CLOSE spring: dampingRatio = 0.66f, stiffness = 480f (~420ms snappy fold-back)
      animProgress.animateTo(
        targetValue = 0f,
        animationSpec = spring(dampingRatio = 0.66f, stiffness = 480f)
      )
      isMounted = false
    }
  }

  if (isMounted || animProgress.value > 0.01f) {
    val positionProvider = remember(align) {
      object : PopupPositionProvider {
        override fun calculatePosition(
          anchorBounds: IntRect,
          windowSize: IntSize,
          layoutDirection: LayoutDirection,
          popupContentSize: IntSize
        ): IntOffset {
          val marginPx = (6 * density.density).toInt()
          val x = if (align == GlassMenuAlign.End) {
            (anchorBounds.right - popupContentSize.width).coerceIn(marginPx, windowSize.width - popupContentSize.width - marginPx)
          } else {
            anchorBounds.left.coerceIn(marginPx, windowSize.width - popupContentSize.width - marginPx)
          }

          // If close to screen bottom, anchor above the trigger
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
      // Transform origin anchored to trigger corner
      val transformOrigin = if (align == GlassMenuAlign.End) {
        TransformOrigin(1f, 0f)
      } else {
        TransformOrigin(0f, 0f)
      }

      val scale = 0.35f + 0.65f * progress
      val alpha = (progress / 0.22f).coerceIn(0f, 1f)
      val cornerRadius = (16.dp + (6.dp * progress)).coerceIn(16.dp, 22.dp)
      val menuShape = RoundedCornerShape(cornerRadius)

      var hoveredIndex by remember { mutableIntStateOf(-1) }
      var lastHapticIndex by remember { mutableIntStateOf(-1) }

      val itemHeightPx = with(density) { 46.dp.toPx() }
      val topPadPx = with(density) { 6.dp.toPx() }

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
          .width(width)
          .graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
            this.alpha = alpha
            this.transformOrigin = transformOrigin
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
          // Interactive touch & slide-to-select pointer tracking (@zakisheriff/liquid-glass)
          .pointerInput(items.size, isMounted) {
            awaitEachGesture {
              val down = awaitFirstDown(requireUnconsumed = false)
              val calcIndex = ((down.position.y - topPadPx) / itemHeightPx).toInt().coerceIn(-1, items.size - 1)
              hoveredIndex = calcIndex
              if (calcIndex in items.indices && !items[calcIndex].disabled && calcIndex != lastHapticIndex) {
                lastHapticIndex = calcIndex
                VibrationHelper.tick(context)
              }

              var isReleased = false
              do {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                if (!change.pressed) {
                  isReleased = true
                  val finalIndex = ((change.position.y - topPadPx) / itemHeightPx).toInt()
                  if (finalIndex in items.indices) {
                    val selItem = items[finalIndex]
                    if (!selItem.disabled) {
                      VibrationHelper.tick(context)
                      coroutineScope.launch {
                        animProgress.animateTo(0f, spring(dampingRatio = 0.66f, stiffness = 480f))
                        selItem.onSelect()
                      }
                    }
                  } else {
                    coroutineScope.launch {
                      animProgress.animateTo(0f, spring(dampingRatio = 0.66f, stiffness = 480f))
                      onDismissRequest()
                    }
                  }
                  break
                } else {
                  val curIndex = ((change.position.y - topPadPx) / itemHeightPx).toInt()
                  if (curIndex != hoveredIndex) {
                    hoveredIndex = curIndex
                    if (curIndex in items.indices && !items[curIndex].disabled && curIndex != lastHapticIndex) {
                      lastHapticIndex = curIndex
                      VibrationHelper.tick(context)
                    }
                  }
                }
              } while (event.changes.any { it.pressed })

              hoveredIndex = -1
              lastHapticIndex = -1
            }
          }
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          items.forEachIndexed { index, item ->
            val isHovered = hoveredIndex == index
            val itemShape = RoundedCornerShape(14.dp)

            val itemBg = if (isHovered && !item.disabled) {
              if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.07f)
            } else {
              Color.Transparent
            }

            val itemContentColor = when {
              item.disabled -> InkMuted.copy(alpha = 0.45f)
              item.destructive -> AccentRed
              else -> InkPrimary
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(itemShape)
                .background(itemBg, itemShape)
                .clickable(
                  enabled = !item.disabled,
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  VibrationHelper.tick(context)
                  coroutineScope.launch {
                    animProgress.animateTo(0f, spring(dampingRatio = 0.66f, stiffness = 480f))
                    item.onSelect()
                  }
                }
                .padding(horizontal = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                if (item.icon != null) {
                  Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    item.icon.invoke()
                  }
                }

                Text(
                  text = item.label,
                  style = TextStyle(
                    fontFamily = StrongFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = itemContentColor
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              if (item.checked) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = if (item.destructive) AccentRed else LocalAppColors.current.oklchPrimary,
                  modifier = Modifier.size(17.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
