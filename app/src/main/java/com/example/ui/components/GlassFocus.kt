package com.example.ui.components

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAppColors

/** An inset, shape-matched keyboard focus ring that remains visible on glass. */
@Composable
fun Modifier.glassFocusRing(
  interactionSource: InteractionSource,
  shape: Shape = RoundedCornerShape(12.dp)
): Modifier {
  val focused by interactionSource.collectIsFocusedAsState()
  val keyboardMode = LocalInputModeManager.current.inputMode == InputMode.Keyboard
  val colors = LocalAppColors.current
  return drawWithCache {
    val inset = 2.dp.toPx()
    val outline = shape.createOutline(
      Size((size.width - inset * 2).coerceAtLeast(0f), (size.height - inset * 2).coerceAtLeast(0f)),
      layoutDirection,
      this
    )
    onDrawWithContent {
      drawContent()
      if (focused && keyboardMode) {
        translate(inset, inset) {
          drawOutline(outline, colors.focusHalo, style = Stroke(4.dp.toPx()))
          drawOutline(outline, colors.focusRing, style = Stroke(2.dp.toPx()))
        }
      }
    }
  }
}
