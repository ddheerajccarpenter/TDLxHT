package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.glass.liquidGlassRim
import com.example.ui.theme.LocalAppColors

/**
 * Apple HIG Grouped Inset Card Container.
 *
 * Consolidates multiple list items into a single unified frosted glass surface
 * with rounded corners and hairline interior dividers, eliminating container fatigue.
 */
@Composable
fun AppleGroupedCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 20.dp,
  content: @Composable ColumnScope.() -> Unit
) {
  val isDark = LocalAppColors.current.isDark
  val cardShape = RoundedCornerShape(cornerRadius)
  val cardBg = if (isDark) {
    Color.White.copy(alpha = 0.06f)
  } else {
    Color.White.copy(alpha = 0.78f)
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(cardShape)
      .background(cardBg)
      .liquidGlassRim(cardShape, isDark, 0.75.dp)
      .padding(vertical = 4.dp),
    content = content
  )
}

/**
 * Canonical iOS-style interior hairline divider.
 * Features standard 52dp leading indentation to optically align with item text.
 */
@Composable
fun AppleGroupedDivider(
  modifier: Modifier = Modifier,
  startIndent: Dp = 52.dp,
  endIndent: Dp = 16.dp
) {
  val isDark = LocalAppColors.current.isDark
  HorizontalDivider(
    modifier = modifier.padding(start = startIndent, end = endIndent),
    thickness = 0.5.dp,
    color = if (isDark) Color.White.copy(alpha = 0.09f) else Color.Black.copy(alpha = 0.07f)
  )
}

