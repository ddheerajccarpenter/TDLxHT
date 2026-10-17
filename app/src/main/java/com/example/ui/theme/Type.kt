package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Apple SF Pro Font Family loaded directly from bundled OpenType assets
val SFProFontFamily = FontFamily(
  Font(R.font.sf_pro_regular, FontWeight.Normal),
  Font(R.font.sf_pro_medium, FontWeight.Medium),
  Font(R.font.sf_pro_semibold, FontWeight.SemiBold),
  Font(R.font.sf_pro_bold, FontWeight.Bold)
)

// The Punk Grandmother Display Typeface (Used for App Brand Name & Front Page Display)
val PunkGrandmotherFontFamily = FontFamily(
  Font(R.font.the_punk_grandmother, FontWeight.Normal)
)

val PunkGrandmotherSimpleFontFamily = FontFamily(
  Font(R.font.the_punk_grandmother_simple, FontWeight.Normal)
)

val AppleSystemFontFamily = SFProFontFamily

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 34.sp,
    lineHeight = 41.sp,
    letterSpacing = (-0.5).sp,
    color = Color.Unspecified
  ),
  displayMedium = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.3).sp,
    color = Color.Unspecified
  ),
  displaySmall = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    color = Color.Unspecified
  ),
  headlineLarge = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.3).sp,
    color = Color.Unspecified
  ),
  headlineMedium = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.2).sp,
    color = Color.Unspecified
  ),
  headlineSmall = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    color = Color.Unspecified
  ),
  titleLarge = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.2).sp,
    color = Color.Unspecified
  ),
  titleMedium = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.1).sp,
    color = Color.Unspecified
  ),
  titleSmall = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    color = Color.Unspecified
  ),
  bodyLarge = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 21.sp,
    letterSpacing = (-0.1).sp,
    color = Color.Unspecified
  ),
  bodyMedium = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 19.sp,
    color = Color.Unspecified
  ),
  bodySmall = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    color = Color.Unspecified
  ),
  labelLarge = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    lineHeight = 17.sp,
    letterSpacing = 0.2.sp,
    color = Color.Unspecified
  ),
  labelMedium = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp,
    color = Color.Unspecified
  ),
  labelSmall = TextStyle(
    fontFamily = SFProFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.3.sp,
    color = Color.Unspecified
  )
)

