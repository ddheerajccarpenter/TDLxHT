package com.example.ui.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Universal horizontal swipe navigation modifier for TDlxHT.
 *
 * Implements strict directional arbitration:
 * 1. Horizontal swipes are recognized ONLY when horizontal movement is significantly greater than vertical movement (dx > 1.5 * dy).
 * 2. Vertical list scrolling is preserved without interference.
 * 3. Left/right edge margins (28dp) are guarded to ensure full compatibility with Android System Predictive Back gestures.
 * 4. Activation requires exceeding the horizontal activation threshold (default 56dp).
 */
fun Modifier.horizontalSwipeNavigation(
  enabled: Boolean = true,
  edgeGuardDp: Dp = 28.dp,
  swipeThresholdDp: Dp = 56.dp,
  onSwipeLeft: () -> Unit,
  onSwipeRight: () -> Unit
): Modifier = pointerInput(enabled) {
  if (!enabled) return@pointerInput

  val edgeGuardPx = edgeGuardDp.toPx()
  val swipeThresholdPx = swipeThresholdDp.toPx()
  val touchSlop = viewConfiguration.touchSlop

  awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false)

    // Guard: ignore touches originating in system edge gesture margins (< edgeGuardPx or > width - edgeGuardPx)
    if (down.position.x < edgeGuardPx || down.position.x > size.width - edgeGuardPx) {
      return@awaitEachGesture
    }

    var totalDx = 0f
    var totalDy = 0f
    var isHorizontalSwipe = false
    var isVerticalScroll = false

    do {
      val event = awaitPointerEvent()
      val change = event.changes.firstOrNull { it.id == down.id } ?: break

      // If child control consumed touch, do not interfere
      if (change.isConsumed && !isHorizontalSwipe) {
        break
      }

      val dx = change.position.x - change.previousPosition.x
      val dy = change.position.y - change.previousPosition.y
      totalDx += dx
      totalDy += dy

      if (!isHorizontalSwipe && !isVerticalScroll) {
        if (abs(totalDx) > touchSlop && abs(totalDx) > abs(totalDy) * 1.5f) {
          isHorizontalSwipe = true
          change.consume()
        } else if (abs(totalDy) > touchSlop) {
          isVerticalScroll = true
        }
      } else if (isHorizontalSwipe) {
        change.consume()
      }
    } while (event.changes.any { it.pressed })

    // Execute navigation transition on finger release if swipe threshold was met
    if (isHorizontalSwipe) {
      if (totalDx < -swipeThresholdPx) {
        onSwipeLeft()
      } else if (totalDx > swipeThresholdPx) {
        onSwipeRight()
      }
    }
  }
}

