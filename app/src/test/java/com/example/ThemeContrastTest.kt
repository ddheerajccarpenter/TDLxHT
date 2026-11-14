package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.example.ui.theme.DarkColors
import com.example.ui.theme.LightColors
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeContrastTest {
  @Test fun supportingTextPassesOnCanvasAndRestingGlass() {
    for (palette in listOf(LightColors, DarkColors)) {
      val backgrounds = listOf(palette.paperBackground,
        palette.cardBackground.compositeOver(palette.paperBackground),
        palette.surfaceElevated.compositeOver(palette.paperBackground))
      for (background in backgrounds) for (text in listOf(palette.inkPrimary, palette.inkSecondary, palette.inkMuted)) {
        assertTrue("Text contrast must be at least 4.5:1: $text on $background", contrast(text, background) >= 4.5f)
      }
    }
  }

  @Test fun focusRingAndHaloHaveDistinctContrast() {
    for (palette in listOf(LightColors, DarkColors)) {
      assertTrue(contrast(palette.focusRing, palette.focusHalo) >= 3f)
    }
  }

  private fun contrast(foreground: Color, background: Color): Float {
    val a = foreground.compositeOver(background).luminance()
    val b = background.luminance()
    return (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
  }
}
