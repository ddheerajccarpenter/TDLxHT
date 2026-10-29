package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// TDlxHT Monochromatic Apple-Style Liquid Glass Design System
// Main Background:       #151515 (Deep Charcoal Canvas)
// Secondary Background:  #202020 (Elevated Panels)
// Primary Text:          #F5F5F5 (Bright Text / Highlights)
// Secondary Text:        #B8B8B8 (Muted Descriptions)
// Muted Icons:           #969696 (Inactive Controls)
// Glass Surface Fill:    #222226 with Translucency
// Glass Border:          #FFFFFF with 20% alpha
// =========================================================================

val BrandCharcoalDark = Color(0xFF151515)
val BrandCharcoalElevated = Color(0xFF202020)
val BrandGlassSurface = Color(0xCC222226) // 80% opacity translucent charcoal
val BrandTextPrimary = Color(0xFFF5F5F5)
val BrandTextSecondary = Color(0xFFB8B8B8)
val BrandTextMuted = Color(0xFF969696)
val BrandGlassBorder = Color(0x38FFFFFF) // 22% white highlight border
val BrandGlassHighlight = Color(0x4DFFFFFF) // Specular top rim

// Aliases for system compatibility
val BrandDarkGrey = BrandCharcoalDark
val BrandWhite = BrandTextPrimary

val BrandCyprus = BrandCharcoalDark
val BrandSand = BrandTextPrimary

// Backward compatible aliases
val PaletteLilacAsh = BrandTextPrimary
val PaletteAntiqueWhite = BrandTextPrimary
val PaletteVintageGrape = BrandCharcoalDark
val PaletteBerryCrush = BrandCharcoalDark
val PaletteMintLeaf = BrandTextPrimary

// --- Dark Mode Tokens (Deep Charcoal & Translucent Glass) ---
val GrapeCanvasDark = BrandCharcoalDark
val GrapeSurfaceSoft = BrandCharcoalElevated
val GrapeSurfaceCard = BrandGlassSurface
val GrapeSurfaceElevated = Color(0xD92A2A2E)
val GrapeHairline = BrandGlassBorder
val GrapeHairlineStrong = Color(0x4DFFFFFF)

val PrimaryDarkCanvas = BrandCharcoalDark
val PrimaryAccentLime = BrandTextPrimary

val BugattiCanvas = BrandCharcoalDark
val BugattiSurfaceSoft = GrapeSurfaceSoft
val BugattiSurfaceCard = GrapeSurfaceCard
val BugattiSurfaceElevated = GrapeSurfaceElevated
val BugattiHairline = GrapeHairline
val BugattiHairlineStrong = GrapeHairlineStrong

val BugattiInk = BrandTextPrimary
val BugattiBody = BrandTextSecondary
val BugattiBodyStrong = BrandTextPrimary
val BugattiMuted = BrandTextMuted
val BugattiMutedSoft = BrandTextMuted

val BugattiLink = BrandTextPrimary
val BugattiWarning = BrandTextPrimary
val BugattiSuccess = BrandTextPrimary
val BugattiDanger = Color(0xFFE05D5D)

// --- Light Mode Tokens (Clean White Canvas & Translucent Soft Surfaces) ---
val LightCanvas = Color(0xFFFAFAFC)
val LightCanvasSoft = Color(0xFFF0F0F3)
val LightSurfaceCard = Color(0xEEFFFFFF)
val LightHairline = Color(0x28000000)
val LightHairlineStrong = Color(0x40000000)

val LightInkPrimary = Color(0xFF151515)
val LightInkSecondary = Color(0xFF6E6E73)
val LightInkMuted = Color(0xFF86868B)

val MobbinCanvas = LightCanvas
val MobbinCanvasSoft = LightCanvasSoft
val MobbinField = Color(0xFFEAEAEA)
val MobbinHairline = LightHairline
val MobbinHairlineSoft = Color(0x18000000)

val MobbinInk = LightInkPrimary
val MobbinInkSoft = LightInkSecondary
val MobbinTextMuted = LightInkMuted
val MobbinTextFaint = Color(0xFFA1A1A6)
val MobbinAccent = LightInkPrimary

val MobbinSuccess = LightInkPrimary
val MobbinWarning = LightInkPrimary
val MobbinDanger = Color(0xFFD32F2F)

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
  paperBackground = LightCanvas,
  paperDotColor = Color.Transparent,
  inkPrimary = LightInkPrimary,
  inkSecondary = LightInkSecondary,
  inkMuted = LightInkMuted,
  cardBackground = LightSurfaceCard,
  cardBorder = LightHairline,
  pillBackgroundDark = LightInkPrimary,
  pillTextDark = Color.White,
  progressBarTrack = Color(0xFFE5E5EA),
  progressBarFilled = LightInkPrimary,
  accentTerracotta = LightInkPrimary,
  accentOrange = LightInkSecondary,
  accentYellowHighlight = Color(0x15000000),
  accentRed = Color(0xFFD32F2F),
  chipBorder = LightHairline,
  chipUnselected = Color(0xFFF2F2F7),
  isDark = false
)

val DarkColors = AppColors(
  paperBackground = BrandCharcoalDark,
  paperDotColor = Color.Transparent,
  inkPrimary = BrandTextPrimary,
  inkSecondary = BrandTextSecondary,
  inkMuted = BrandTextMuted,
  cardBackground = BrandGlassSurface,
  cardBorder = BrandGlassBorder,
  pillBackgroundDark = BrandTextPrimary,
  pillTextDark = BrandCharcoalDark,
  progressBarTrack = Color(0x33FFFFFF),
  progressBarFilled = BrandTextPrimary,
  accentTerracotta = BrandTextPrimary,
  accentOrange = BrandTextSecondary,
  accentYellowHighlight = Color(0x1FFFFFFF),
  accentRed = Color(0xFFE05D5D),
  chipBorder = BrandGlassBorder,
  chipUnselected = Color(0x33252528),
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
