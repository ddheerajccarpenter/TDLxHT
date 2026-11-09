package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Classic 12-Blade Apple Activity Spinner (loading.dev/spinners/classic style).
 * 12 radial rounded pills rotated around center at 30-degree intervals with stepped
 * alpha falloff, completing one full cycle per second.
 */
@Composable
fun ClassicLoadingSpinner(
  modifier: Modifier = Modifier,
  size: Dp = 48.dp,
  color: Color = Color.White
) {
  val infiniteTransition = rememberInfiniteTransition(label = "classicSpinner")
  val step by infiniteTransition.animateValue(
    initialValue = 0,
    targetValue = 12,
    typeConverter = Int.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "step"
  )

  Canvas(modifier = modifier.size(size)) {
    val center = Offset(this.size.width / 2f, this.size.height / 2f)
    val radius = this.size.minDimension / 2f
    val barWidth = 3.6.dp.toPx()
    val barLength = radius * 0.40f
    val innerRadius = radius * 0.46f

    for (i in 0 until 12) {
      val angleDeg = i * 30f
      // 12 stepped opacity fractions, leading edge brightest
      val alphaFraction = ((i - step + 12) % 12) / 11f
      val bladeAlpha = 0.14f + 0.86f * alphaFraction

      rotate(degrees = angleDeg, pivot = center) {
        drawRoundRect(
          color = color.copy(alpha = bladeAlpha),
          topLeft = Offset(center.x - barWidth / 2f, center.y - innerRadius - barLength),
          size = Size(barWidth, barLength),
          cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )
      }
    }
  }
}

