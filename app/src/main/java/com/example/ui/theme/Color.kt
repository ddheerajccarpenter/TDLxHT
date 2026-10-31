package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// TDlxHT Monochromatic Apple-Style Liquid Glass Design System
// Canonical tokens from designglass.md
// Dark Canvas:           #151515 (Deep Charcoal Canvas)
// Dark Secondary:        #1C1C1E (Elevated Panels)
// Dark Glass Base:       rgba(37, 37, 42, 0.82)
// Dark Text Primary:     #F5F5F7 (Bright Text / Highlights)
// Dark Text Secondary:   #B0B0B8 (Muted Descriptions)
// Dark Text Muted:       #7C7C85 (Inactive Controls)
// =========================================================================

// Canonical Dark Tokens (designglass.md - Liquid Glass System)
val CanonicalCanvasDark = Color(0xFF121215)
val CanonicalCanvasSecondaryDark = Color(0xFF18181D)
val CanonicalGlassFillBaseDark = Color(0x4D1A1A24) // Lensed translucent glass (~30% optical density)
val CanonicalGlassFillElevatedDark = Color(0xEB161622) // Elevated fluid glass for floating dock & modals (~92% optical density)
val CanonicalGlassFillSubtleDark = Color(0x2E14141C) // Subtle translucent track (~18% alpha)
val CanonicalGlassBorderTopDark = Color(0x38FFFFFF) // Lensed specular hairline top rim (~22% alpha)
val CanonicalGlassBorderBottomDark = Color(0x0CFFFFFF) // Bottom edge meniscus rim (~5% alpha)
val CanonicalGlassSheenTopDark = Color(0x20FFFFFF) // Overhead optical caustic refraction hint
val CanonicalTextPrimaryDark = Color(0xFFF5F5F7)
val CanonicalTextSecondaryDark = Color(0xFFB0B0B8)
val CanonicalTextMutedDark = Color(0xFF7C7C85)
val CanonicalDividerDark = Color(0x14FFFFFF) // Subtle interior separator (~8% alpha)
val CanonicalAccentActionDark = Color(0xFFFFFFFF)
val CanonicalDestructiveDark = Color(0xFFE05D5D)

// Canonical Light Tokens (designglass.md - Liquid Glass System)
val CanonicalCanvasLight = Color(0xFFF5F5F7)
val CanonicalCanvasSecondaryLight = Color(0xFFEAEAF0)
val CanonicalGlassFillBaseLight = Color(0xD0FFFFFF) // Translucent soft white glass (~82% alpha)
val CanonicalGlassFillElevatedLight = Color(0xEEFFFFFF) // Elevated white glass dock (~93% alpha)
val CanonicalGlassFillSubtleLight = Color(0x80E8E8F0) // Subtle light track
val CanonicalGlassBorderTopLight = Color(0x70FFFFFF) // Top specular highlight
val CanonicalGlassBorderBottomLight = Color(0x10000000) // Bottom rim definition
val CanonicalGlassSheenTopLight = Color(0x18FFFFFF)
val CanonicalTextPrimaryLight = Color(0xFF111113)
val CanonicalTextSecondaryLight = Color(0xFF55555C)
val CanonicalTextMutedDarkLight = Color(0xFF8E8E93)
val CanonicalTextMutedLight = Color(0xFF8E8E93)
val CanonicalDividerLight = Color(0x10000000)
val CanonicalAccentActionLight = Color(0xFF000000)
val CanonicalDestructiveLight = Color(0xFFD32F2F)

val BrandCharcoalDark = CanonicalCanvasDark
val BrandCharcoalElevated = CanonicalCanvasSecondaryDark
val BrandGlassSurface = CanonicalGlassFillBaseDark
val BrandTextPrimary = CanonicalTextPrimaryDark
val BrandTextSecondary = CanonicalTextSecondaryDark
val BrandTextMuted = CanonicalTextMutedDark
val BrandGlassBorder = CanonicalGlassBorderTopDark
val BrandGlassHighlight = CanonicalGlassSheenTopDark

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

// =========================================================================
// Perceptually Uniform OKLCH Palette (jakubkrehel/oklch-skill)
// =========================================================================
val OklchIndigo = Color(0xFF6366F1)       // oklch(0.65 0.22 275) Primary Brand Accent
val OklchIndigoSoft = Color(0x266366F1)   // Soft Indigo Tint (~15% alpha)
val OklchEmerald = Color(0xFF10B981)      // oklch(0.76 0.19 148) Success & Completion
val OklchEmeraldSoft = Color(0x2610B981)  // Soft Emerald Tint (~15% alpha)
val OklchAmber = Color(0xFFF59E0B)        // oklch(0.72 0.17 65) Flame, Streaks, In-Progress
val OklchAmberSoft = Color(0x26F59E0B)    // Soft Amber Tint (~15% alpha)
val OklchCoral = Color(0xFFEF4444)        // oklch(0.63 0.23 25) High Priority & Alerts
val OklchCoralSoft = Color(0x26EF4444)    // Soft Coral Tint (~15% alpha)
val OklchCyan = Color(0xFF0EA5E9)         // oklch(0.70 0.16 220) Focus Mode & Pomodoro
val OklchCyanSoft = Color(0x260EA5E9)     // Soft Cyan Tint (~15% alpha)
val OklchPurple = Color(0xFFA855F7)       // oklch(0.68 0.24 300) Insights & Analytics
val OklchPurpleSoft = Color(0x26A855F7)   // Soft Purple Tint (~15% alpha)

// --- Dark Mode Tokens (Deep Charcoal & Translucent Glass) ---
val GrapeCanvasDark = BrandCharcoalDark
val GrapeSurfaceSoft = BrandCharcoalElevated
val GrapeSurfaceCard = BrandGlassSurface
val GrapeSurfaceElevated = CanonicalGlassFillElevatedDark
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
val BugattiDanger = CanonicalDestructiveDark

// --- Light Mode Tokens (Clean White Canvas & Translucent Soft Surfaces) ---
val LightCanvas = CanonicalCanvasLight
val LightCanvasSoft = CanonicalCanvasSecondaryLight
val LightSurfaceCard = CanonicalGlassFillBaseLight
val LightHairline = CanonicalDividerLight
val LightHairlineStrong = Color(0x30000000)

val LightInkPrimary = CanonicalTextPrimaryLight
val LightInkSecondary = CanonicalTextSecondaryLight
val LightInkMuted = CanonicalTextMutedLight

val MobbinCanvas = LightCanvas
val MobbinCanvasSoft = LightCanvasSoft
val MobbinField = Color(0xFFEBEBF0)
val MobbinHairline = LightHairline
val MobbinHairlineSoft = Color(0x10000000)

val MobbinInk = LightInkPrimary
val MobbinInkSoft = LightInkSecondary
val MobbinTextMuted = LightInkMuted
val MobbinTextFaint = Color(0xFFA1A1A6)
val MobbinAccent = CanonicalAccentActionLight

val MobbinSuccess = LightInkPrimary
val MobbinWarning = LightInkPrimary
val MobbinDanger = CanonicalDestructiveLight

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
  val isDark: Boolean,
  val oklchPrimary: Color = OklchIndigo,
  val oklchSuccess: Color = OklchEmerald,
  val oklchWarning: Color = OklchAmber,
  val oklchDanger: Color = OklchCoral,
  val oklchFocus: Color = OklchCyan,
  val oklchPurple: Color = OklchPurple,
  val surfaceCard: Color = if (isDark) CanonicalGlassFillBaseDark else CanonicalGlassFillBaseLight,
  val surfaceElevated: Color = if (isDark) CanonicalGlassFillElevatedDark else CanonicalGlassFillElevatedLight
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
  progressBarFilled = OklchIndigo,
  accentTerracotta = OklchIndigo,
  accentOrange = OklchAmber,
  accentYellowHighlight = Color(0x156366F1),
  accentRed = OklchCoral,
  chipBorder = LightHairline,
  chipUnselected = Color(0xFFF2F2F7),
  isDark = false,
  oklchPrimary = OklchIndigo,
  oklchSuccess = OklchEmerald,
  oklchWarning = OklchAmber,
  oklchDanger = OklchCoral,
  oklchFocus = OklchCyan,
  oklchPurple = OklchPurple,
  surfaceCard = CanonicalGlassFillBaseLight,
  surfaceElevated = CanonicalGlassFillElevatedLight
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
  progressBarTrack = Color(0x2EFFFFFF),
  progressBarFilled = OklchIndigo,
  accentTerracotta = OklchIndigo,
  accentOrange = OklchAmber,
  accentYellowHighlight = Color(0x2E6366F1),
  accentRed = OklchCoral,
  chipBorder = BrandGlassBorder,
  chipUnselected = Color(0x33252528),
  isDark = true,
  oklchPrimary = OklchIndigo,
  oklchSuccess = OklchEmerald,
  oklchWarning = OklchAmber,
  oklchDanger = OklchCoral,
  oklchFocus = OklchCyan,
  oklchPurple = OklchPurple,
  surfaceCard = BrandGlassSurface,
  surfaceElevated = CanonicalGlassFillElevatedDark
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
  @Composable get() = LocalAppColors.current.oklchPrimary

val AppleGreen: Color
  @Composable get() = LocalAppColors.current.oklchSuccess

val AppleOrange: Color
  @Composable get() = LocalAppColors.current.oklchWarning

val PrimaryAccent: Color
  @Composable get() = LocalAppColors.current.oklchPrimary

val OklchPrimaryToken: Color
  @Composable get() = LocalAppColors.current.oklchPrimary

val OklchSuccessToken: Color
  @Composable get() = LocalAppColors.current.oklchSuccess

val OklchWarningToken: Color
  @Composable get() = LocalAppColors.current.oklchWarning

val OklchDangerToken: Color
  @Composable get() = LocalAppColors.current.oklchDanger

val OklchFocusToken: Color
  @Composable get() = LocalAppColors.current.oklchFocus

val OklchPurpleToken: Color
  @Composable get() = LocalAppColors.current.oklchPurple

val SurfaceCardToken: Color
  @Composable get() = LocalAppColors.current.surfaceCard

val SurfaceElevatedToken: Color
  @Composable get() = LocalAppColors.current.surfaceElevated
