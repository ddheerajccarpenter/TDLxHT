package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Seramonde Display Typeface (Used for App Brand Name & Hero Titles)
val SeramondeFontFamily = FontFamily(
  Font(R.font.seramonde, FontWeight.Normal)
)

// Strong Typeface (Used as default basic & main typography across the application)
val StrongFontFamily = FontFamily(
  Font(R.font.strong, FontWeight.Normal)
)

// Cream Cake Calligraphy Typeface (Used for streaks, celebrations & special moments)
val CreamCakeFontFamily = FontFamily(
  Font(R.font.cream_cake, FontWeight.Normal),
  Font(R.font.cream_cake_bold, FontWeight.Bold)
)

// Semantic aliases
val BrandFontFamily = SeramondeFontFamily
val MainFontFamily = StrongFontFamily
val SpecialFontFamily = CreamCakeFontFamily

// Backward compatibility
val PunkGrandmotherFontFamily = SeramondeFontFamily
val PunkGrandmotherSimpleFontFamily = SeramondeFontFamily
val AppleSystemFontFamily = StrongFontFamily

// Strong and Seramonde ship only a static Regular face. Preserve their actual
// outlines; hierarchy comes from type size, spacing, and the selected surface.
val Typography = Typography(
  displayLarge = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = SeramondeFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 34.sp,
    lineHeight = 41.sp,
    letterSpacing = (-0.5).sp,
    color = Color.Unspecified
  ),
  displayMedium = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = SeramondeFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.3).sp,
    color = Color.Unspecified
  ),
  displaySmall = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = SeramondeFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    color = Color.Unspecified
  ),
  headlineLarge = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.3).sp,
    color = Color.Unspecified
  ),
  headlineMedium = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.2).sp,
    color = Color.Unspecified
  ),
  headlineSmall = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    color = Color.Unspecified
  ),
  titleLarge = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.2).sp,
    color = Color.Unspecified
  ),
  titleMedium = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.1).sp,
    color = Color.Unspecified
  ),
  titleSmall = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    color = Color.Unspecified
  ),
  bodyLarge = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 21.sp,
    letterSpacing = (-0.1).sp,
    color = Color.Unspecified
  ),
  bodyMedium = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 19.sp,
    color = Color.Unspecified
  ),
  bodySmall = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    color = Color.Unspecified
  ),
  labelLarge = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 17.sp,
    letterSpacing = 0.2.sp,
    color = Color.Unspecified
  ),
  labelMedium = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp,
    color = Color.Unspecified
  ),
  labelSmall = TextStyle(
    fontSynthesis = FontSynthesis.None,
    fontFamily = StrongFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.3.sp,
    color = Color.Unspecified
  )
)

