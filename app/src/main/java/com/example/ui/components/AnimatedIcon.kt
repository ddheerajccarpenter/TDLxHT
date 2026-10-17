package com.example.ui.components

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

/**
 * AnimatedIcon renders animated vector icons inspired by Iconly Pro and Flaticon.
 * Built with Lottie Compose for crisp, 60fps buttery animations with dynamic theme tinting.
 */
@Composable
fun AnimatedIcon(
  @RawRes resId: Int,
  modifier: Modifier = Modifier,
  size: Dp = 24.dp,
  tint: Color = Color.Unspecified,
  isSelected: Boolean = true,
  autoPlay: Boolean = true,
  repeatCount: Int = 1,
  speed: Float = 1.0f,
  contentDescription: String? = null
) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))

  val progress by animateLottieCompositionAsState(
    composition = composition,
    isPlaying = isSelected && autoPlay,
    restartOnPlay = true,
    iterations = if (isSelected) repeatCount else 1,
    speed = speed
  )

  val dynamicProperties = if (tint != Color.Unspecified) {
    rememberLottieDynamicProperties(
      rememberLottieDynamicProperty(
        property = LottieProperty.COLOR_FILTER,
        value = PorterDuffColorFilter(tint.toArgb(), PorterDuff.Mode.SRC_ATOP),
        keyPath = arrayOf("**")
      )
    )
  } else {
    null
  }

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    LottieAnimation(
      composition = composition,
      progress = { if (isSelected) progress else 0f },
      dynamicProperties = dynamicProperties,
      modifier = Modifier.size(size)
    )
  }
}
