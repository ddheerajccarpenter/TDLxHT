package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// TDlxHT Strict Two-Color Design System
// CYPRUS — #004643 (Primary brand / Deep surface)
// SAND   — #F0EDE5 (Complementary light surface)
// All glass layers, borders, and shadows are pure alpha variants of these 2 colors.
// =========================================================================

val BrandCyprus = Color(0xFF004643)
val BrandSand = Color(0xFFF0EDE5)

// Backward compatible aliases
val PaletteLilacAsh = BrandSand.copy(alpha = 0.70f)
val PaletteAntiqueWhite = BrandSand
val PaletteVintageGrape = BrandCyprus
val PaletteBerryCrush = BrandCyprus
val PaletteMintLeaf = BrandSand

// --- Dark Mode Tokens (Cyprus Canvas & Sand Glass) ---
val GrapeCanvasDark = BrandCyprus
val GrapeSurfaceSoft = BrandCyprus.copy(alpha = 0.88f)
val GrapeSurfaceCard = BrandSand.copy(alpha = 0.08f)
val GrapeSurfaceElevated = BrandSand.copy(alpha = 0.12f)
val GrapeHairline = BrandSand.copy(alpha = 0.18f)
val GrapeHairlineStrong = BrandSand.copy(alpha = 0.28f)

val PrimaryDarkCanvas = BrandCyprus
val PrimaryAccentLime = BrandSand

val BugattiCanvas = BrandCyprus
val BugattiSurfaceSoft = GrapeSurfaceSoft
val BugattiSurfaceCard = GrapeSurfaceCard
val BugattiSurfaceElevated = GrapeSurfaceElevated
val BugattiHairline = GrapeHairline
val BugattiHairlineStrong = GrapeHairlineStrong

val BugattiInk = BrandSand
val BugattiBody = BrandSand.copy(alpha = 0.78f)
val BugattiBodyStrong = BrandSand
val BugattiMuted = BrandSand.copy(alpha = 0.45f)
val BugattiMutedSoft = BrandSand.copy(alpha = 0.30f)

val BugattiLink = BrandSand
val BugattiWarning = BrandSand.copy(alpha = 0.90f)
val BugattiSuccess = BrandSand
val BugattiDanger = BrandSand.copy(alpha = 0.65f)

// --- Light Mode Tokens (Sand Canvas & Cyprus Glass) ---
val LightCanvas = BrandSand
val LightCanvasSoft = BrandCyprus.copy(alpha = 0.05f)
val LightSurfaceCard = BrandSand
val LightHairline = BrandCyprus.copy(alpha = 0.16f)
val LightHairlineStrong = BrandCyprus.copy(alpha = 0.25f)

val LightInkPrimary = BrandCyprus
val LightInkSecondary = BrandCyprus.copy(alpha = 0.72f)
val LightInkMuted = BrandCyprus.copy(alpha = 0.45f)

val MobbinCanvas = BrandSand
val MobbinCanvasSoft = LightCanvasSoft
val MobbinField = BrandCyprus.copy(alpha = 0.06f)
val MobbinHairline = LightHairline
val MobbinHairlineSoft = BrandCyprus.copy(alpha = 0.10f)

val MobbinInk = BrandCyprus
val MobbinInkSoft = LightInkSecondary
val MobbinTextMuted = LightInkMuted
val MobbinTextFaint = BrandCyprus.copy(alpha = 0.30f)
val MobbinAccent = BrandCyprus

val MobbinSuccess = BrandCyprus
val MobbinWarning = BrandCyprus.copy(alpha = 0.85f)
val MobbinDanger = BrandCyprus.copy(alpha = 0.60f)

data class AppColors(
  val paperBackground: Color,
  val paperDotColor: Color,
  val inkPrimary: Color,
  val inkSecondary: Color,
  val inkMuted: Color,
  val cardBackground: Color,
  val cardBorder: Color,
  val pillBackgroundDark: Color,
  val pillTextDark: Color,
  val progressBarTrack: Color,
  val progressBarFilled: Color,
  val accentTerracotta: Color,
  val accentOrange: Color,
  val accentYellowHighlight: Color,
  val accentRed: Color,
  val chipBorder: Color,
  val chipUnselected: Color,
  val isDark: Boolean
)

val LightColors = AppColors(
  paperBackground = BrandSand,
  paperDotColor = Color.Transparent,
  inkPrimary = BrandCyprus,
  inkSecondary = BrandCyprus.copy(alpha = 0.72f),
  inkMuted = BrandCyprus.copy(alpha = 0.45f),
  cardBackground = BrandSand,
  cardBorder = BrandCyprus.copy(alpha = 0.16f),
  pillBackgroundDark = BrandCyprus,
  pillTextDark = BrandSand,
  progressBarTrack = BrandCyprus.copy(alpha = 0.12f),
  progressBarFilled = BrandCyprus,
  accentTerracotta = BrandCyprus,
  accentOrange = BrandCyprus.copy(alpha = 0.85f),
  accentYellowHighlight = BrandCyprus.copy(alpha = 0.15f),
  accentRed = BrandCyprus,
  chipBorder = BrandCyprus.copy(alpha = 0.20f),
  chipUnselected = BrandCyprus.copy(alpha = 0.08f),
  isDark = false
)

val DarkColors = AppColors(
  paperBackground = BrandCyprus,
  paperDotColor = Color.Transparent,
  inkPrimary = BrandSand,
  inkSecondary = BrandSand.copy(alpha = 0.78f),
  inkMuted = BrandSand.copy(alpha = 0.48f),
  cardBackground = BrandCyprus,
  cardBorder = BrandSand.copy(alpha = 0.22f),
  pillBackgroundDark = BrandSand,
  pillTextDark = BrandCyprus,
  progressBarTrack = BrandSand.copy(alpha = 0.16f),
  progressBarFilled = BrandSand,
  accentTerracotta = BrandSand,
  accentOrange = BrandSand.copy(alpha = 0.85f),
  accentYellowHighlight = BrandSand.copy(alpha = 0.20f),
  accentRed = BrandSand,
  chipBorder = BrandSand.copy(alpha = 0.24f),
  chipUnselected = BrandSand.copy(alpha = 0.10f),
  isDark = true
)

val LocalAppColors = staticCompositionLocalOf { LightColors }

// Composable accessors allowing 100% backward compatibility across all screens
val PaperBackground: Color
  @Composable get() = LocalAppColors.current.paperBackground

val PaperDotColor: Color
  @Composable get() = LocalAppColors.current.paperDotColor

val InkPrimary: Color
  @Composable get() = LocalAppColors.current.inkPrimary

val InkSecondary: Color
  @Composable get() = LocalAppColors.current.inkSecondary

val InkMuted: Color
  @Composable get() = LocalAppColors.current.inkMuted

val CardBackground: Color
  @Composable get() = LocalAppColors.current.cardBackground

val CardBorder: Color
  @Composable get() = LocalAppColors.current.cardBorder

val PillBackgroundDark: Color
  @Composable get() = LocalAppColors.current.pillBackgroundDark

val PillTextDark: Color
  @Composable get() = LocalAppColors.current.pillTextDark

val ProgressBarTrack: Color
  @Composable get() = LocalAppColors.current.progressBarTrack

val ProgressBarFilled: Color
  @Composable get() = LocalAppColors.current.progressBarFilled

val AccentTerracotta: Color
  @Composable get() = LocalAppColors.current.accentTerracotta

val AccentOrange: Color
  @Composable get() = LocalAppColors.current.accentOrange

val AccentYellowHighlight: Color
  @Composable get() = LocalAppColors.current.accentYellowHighlight

val AccentRed: Color
  @Composable get() = LocalAppColors.current.accentRed

val ChipBorder: Color
  @Composable get() = LocalAppColors.current.chipBorder

val ChipUnselected: Color
  @Composable get() = LocalAppColors.current.chipUnselected

val AppleBlue: Color
  @Composable get() = PaletteMintLeaf

val AppleGreen: Color
  @Composable get() = PaletteMintLeaf

val AppleOrange: Color
  @Composable get() = if (LocalAppColors.current.isDark) Color(0xFFDCA452) else Color(0xFFCC8538)

val PrimaryAccent: Color
  @Composable get() = PaletteMintLeaf
