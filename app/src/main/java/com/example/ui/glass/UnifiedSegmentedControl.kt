package com.example.ui.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.zIndex
import com.example.ui.components.InteractionLevel
import com.example.ui.components.bounceClick
import com.example.ui.theme.CocaNoir
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.StrongFontFamily
import com.example.util.SoundManager
import com.example.util.VibrationHelper
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Universal Segmented Capsule Standard (Design Reference: Tasks / Habits / Calendar).
 *
 * Implements a unified rounded stadium capsule with:
 * - Soft, warm, translucent track surface.
 * - Thin 0.75dp hairline borders.
 * - Distinct elevated inner selected sliding pill with soft diffusion shadow.
 * - Quiet, legible unselected destinations.
 * - Zero-coroutine 120Hz gesture drag engine with spring physics.
 */
@Composable
fun UnifiedSegmentedControl(
  items: List<String>,
  selectedIndex: Int,
  onItemSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
  height: Dp = 44.dp
) {
  if (items.isEmpty()) return

  val context = LocalContext.current
  val density = LocalDensity.current
  val isDark = isSystemInDarkTheme()
  val count = items.size
  val safeIndex = selectedIndex.coerceIn(0, count - 1)
  val onSelectUpdated by rememberUpdatedState(onItemSelected)
  val coroutineScope = rememberCoroutineScope()

  val capsuleShape = RoundedCornerShape(percent = 50)
  val trackBg = if (isDark) Color.White.copy(alpha = 0.075f) else Color.Black.copy(alpha = 0.055f)
  val trackBorder = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)

  val pillFill = if (isDark) Color(0xFF23252E) else Color.White.copy(alpha = 0.96f)
  val pillBorder = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.06f)
  val shadowSpot = if (isDark) Color.Black.copy(alpha = 0.40f) else CocaNoir.copy(alpha = 0.16f)
  val shadowAmbient = if (isDark) Color.Black.copy(alpha = 0.20f) else CocaNoir.copy(alpha = 0.06f)

  BoxWithConstraints(
    modifier = modifier
      .height(height)
      .fillMaxWidth()
      .clip(capsuleShape)
      .background(trackBg)
      .border(0.75.dp, trackBorder, capsuleShape)
      .padding(3.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    val barWidthPx = with(density) { maxWidth.toPx() }
    val itemWidthPx = if (count > 0) barWidthPx / count.toFloat() else 0f
    val pillWidthPx = itemWidthPx
    val pillWidthDp = with(density) { pillWidthPx.toDp() }

    fun slotCenter(idx: Int) = itemWidthPx * (idx + 0.5f)
    fun indexForX(x: Float): Int = ((x / itemWidthPx).toInt()).coerceIn(0, count - 1)

    val indicatorX = remember { Animatable(slotCenter(safeIndex) - pillWidthPx / 2f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragTargetX by remember { mutableFloatStateOf(slotCenter(safeIndex) - pillWidthPx / 2f) }
    var lastHapticIndex by remember { mutableIntStateOf(safeIndex) }

    // Sync indicator position on programmatic selection change
    LaunchedEffect(safeIndex, itemWidthPx) {
      if (!isDragging && itemWidthPx > 0f) {
        val targetX = slotCenter(safeIndex) - pillWidthPx / 2f
        indicatorX.animateTo(
          targetValue = targetX,
          animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f)
        )
      }
    }

    // 120Hz gesture tracking loop
    LaunchedEffect(isDragging) {
      if (!isDragging) return@LaunchedEffect
      var lastNanos = withFrameNanos { it }
      while (isDragging) {
        var nextX = indicatorX.value
        withFrameNanos { now ->
          val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
          lastNanos = now
          val factor = 1f - kotlin.math.exp(-dt * 38f)
          nextX = indicatorX.value + (dragTargetX - indicatorX.value) * factor
        }
        indicatorX.snapTo(nextX)
      }
    }

    // Gesture input interceptor
    Box(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(itemWidthPx, count) {
          if (itemWidthPx <= 0f) return@pointerInput
          val minX = 0f
          val maxX = barWidthPx - pillWidthPx
          val touchSlop = viewConfiguration.touchSlop

          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
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
                  dragTargetX = (change.position.x - pillWidthPx / 2f).coerceIn(minX, maxX)
                  change.consume()
                }
              }

              if (dragStarted) {
                change.consume()
                dragTargetX = (change.position.x - pillWidthPx / 2f).coerceIn(minX, maxX)
                val currentSlot = indexForX(change.position.x)
                if (currentSlot != lastHapticIndex) {
                  lastHapticIndex = currentSlot
                  VibrationHelper.tick(context)
                  SoundManager.playSwipeNav(context)
                }
              }
            } while (event.changes.any { it.pressed })

            if (dragStarted) {
              isDragging = false
              val finalSlot = indexForX(indicatorX.value + pillWidthPx / 2f)
              onSelectUpdated(finalSlot)
              VibrationHelper.tick(context)
              SoundManager.playSwipeNav(context)
              coroutineScope.launch {
                val targetX = slotCenter(finalSlot) - pillWidthPx / 2f
                indicatorX.animateTo(targetX, spring(dampingRatio = 0.75f, stiffness = 420f))
              }
            }
          }
        }
    )

    // Inner Selected Sliding Pill
    Box(
      modifier = Modifier
        .width(pillWidthDp)
        .fillMaxHeight()
        .graphicsLayer {
          translationX = indicatorX.value
          shadowElevation = 3.dp.toPx()
          spotShadowColor = shadowSpot
          ambientShadowColor = shadowAmbient
          shape = capsuleShape
          clip = true
        }
        .background(pillFill)
        .border(0.75.dp, pillBorder, capsuleShape)
    )

    // Interactive Tab Labels
    Row(
      modifier = Modifier
        .fillMaxSize()
        .zIndex(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      items.forEachIndexed { index, title ->
        val isSelected = index == safeIndex

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(capsuleShape)
            .semantics {
              role = Role.Tab
              selected = isSelected
              contentDescription = title
              onClick {
                if (index != safeIndex) {
                  VibrationHelper.tick(context)
                  SoundManager.playSwipeNav(context)
                  onSelectUpdated(index)
                }
                true
              }
            }
            .bounceClick(
              level = InteractionLevel.MICRO,
              scaleDown = 0.985f,
              shine = true
            ) {
              if (index != safeIndex) {
                VibrationHelper.tick(context)
                SoundManager.playSwipeNav(context)
                onSelectUpdated(index)
              }
            }
            .testTag("segment_$title"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = title,
            style = TextStyle(
              fontFamily = StrongFontFamily,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
              color = if (isSelected) {
                if (isDark) Color.White else CocaNoir
              } else {
                if (isDark) Color.White.copy(alpha = 0.50f) else InkSecondary
              }
            ),
            maxLines = 1
          )
        }
      }
    }
  }
}
