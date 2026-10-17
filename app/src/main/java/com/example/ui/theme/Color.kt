package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// TDlxHT Strict Two-Color Design System: DARK GREY & WHITE ONLY
// DARK GREY — #252525 (Canvas and component surfaces)
// WHITE     — #FFFFFF (Text, icons, borders, dividers, controls, and outlines)
// =========================================================================

val BrandDarkGrey = Color(0xFF252525)
val BrandWhite = Color(0xFFFFFFFF)

val BrandCyprus = BrandDarkGrey
val BrandSand = BrandWhite

// Backward compatible aliases
val PaletteLilacAsh = BrandWhite
val PaletteAntiqueWhite = BrandWhite
val PaletteVintageGrape = BrandDarkGrey
val PaletteBerryCrush = BrandDarkGrey
val PaletteMintLeaf = BrandWhite

// --- Dark Mode Tokens (Dark Grey Canvas & White Highlights) ---
val GrapeCanvasDark = BrandDarkGrey
val GrapeSurfaceSoft = BrandDarkGrey
val GrapeSurfaceCard = BrandDarkGrey
val GrapeSurfaceElevated = BrandDarkGrey
val GrapeHairline = BrandWhite
val GrapeHairlineStrong = BrandWhite

val PrimaryDarkCanvas = BrandDarkGrey
val PrimaryAccentLime = BrandWhite

val BugattiCanvas = BrandDarkGrey
val BugattiSurfaceSoft = GrapeSurfaceSoft
val BugattiSurfaceCard = GrapeSurfaceCard
val BugattiSurfaceElevated = GrapeSurfaceElevated
val BugattiHairline = GrapeHairline
val BugattiHairlineStrong = GrapeHairlineStrong

val BugattiInk = BrandWhite
val BugattiBody = BrandWhite
val BugattiBodyStrong = BrandWhite
val BugattiMuted = BrandWhite
val BugattiMutedSoft = BrandWhite

val BugattiLink = BrandWhite
val BugattiWarning = BrandWhite
val BugattiSuccess = BrandWhite
val BugattiDanger = BrandWhite

// --- Light Mode Tokens (White Canvas & Dark Grey Text) ---
val LightCanvas = BrandWhite
val LightCanvasSoft = BrandWhite
val LightSurfaceCard = BrandWhite
val LightHairline = BrandDarkGrey
val LightHairlineStrong = BrandDarkGrey

val LightInkPrimary = BrandDarkGrey
val LightInkSecondary = BrandDarkGrey
val LightInkMuted = BrandDarkGrey

val MobbinCanvas = BrandWhite
val MobbinCanvasSoft = LightCanvasSoft
val MobbinField = BrandWhite
val MobbinHairline = LightHairline
val MobbinHairlineSoft = BrandDarkGrey

val MobbinInk = BrandDarkGrey
val MobbinInkSoft = LightInkSecondary
val MobbinTextMuted = LightInkMuted
val MobbinTextFaint = BrandDarkGrey
val MobbinAccent = BrandDarkGrey

val MobbinSuccess = BrandDarkGrey
val MobbinWarning = BrandDarkGrey
val MobbinDanger = BrandDarkGrey

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
  paperBackground = BrandWhite,
  paperDotColor = Color.Transparent,
  inkPrimary = BrandDarkGrey,
  inkSecondary = BrandDarkGrey,
  inkMuted = BrandDarkGrey,
  cardBackground = BrandWhite,
  cardBorder = BrandDarkGrey,
  pillBackgroundDark = BrandDarkGrey,
  pillTextDark = BrandWhite,
  progressBarTrack = BrandWhite,
  progressBarFilled = BrandDarkGrey,
  accentTerracotta = BrandDarkGrey,
  accentOrange = BrandDarkGrey,
  accentYellowHighlight = BrandDarkGrey,
  accentRed = BrandDarkGrey,
  chipBorder = BrandDarkGrey,
  chipUnselected = BrandWhite,
  isDark = false
)

val DarkColors = AppColors(
  paperBackground = BrandDarkGrey,
  paperDotColor = Color.Transparent,
  inkPrimary = BrandWhite,
  inkSecondary = BrandWhite,
  inkMuted = BrandWhite,
  cardBackground = BrandDarkGrey,
  cardBorder = BrandWhite,
  pillBackgroundDark = BrandWhite,
  pillTextDark = BrandDarkGrey,
  progressBarTrack = BrandDarkGrey,
  progressBarFilled = BrandWhite,
  accentTerracotta = BrandWhite,
  accentOrange = BrandWhite,
  accentYellowHighlight = BrandWhite,
  accentRed = BrandWhite,
  chipBorder = BrandWhite,
  chipUnselected = BrandDarkGrey,
  isDark = true
)

val LocalAppColors = staticCompositionLocalOf { DarkColors }

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
  @Composable get() = if (LocalAppColors.current.isDark) BrandWhite else BrandDarkGrey

val PrimaryAccent: Color
  @Composable get() = PaletteMintLeaf
