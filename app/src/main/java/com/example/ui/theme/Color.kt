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

// Canonical Dark Tokens (Solid Charcoal Grey System)
val CanonicalCanvasDark = Color(0xFF1E2022) // Solid Charcoal Grey Canvas
val CanonicalCanvasSecondaryDark = Color(0xFF242629)
val CanonicalGlassFillBaseDark = Color(0xFF2B2D30) // Solid Opaque Card Surface Grey
val CanonicalGlassFillElevatedDark = Color(0xFF35383C) // Solid Opaque Elevated Dock & Modal Grey
val CanonicalGlassFillSubtleDark = Color(0xFF242629) // Solid Subtle Track
val CanonicalGlassBorderTopDark = Color(0xFF4A4A4A) // Solid Specular Hairline
val CanonicalGlassBorderBottomDark = Color(0xFF35383C)
val CanonicalGlassSheenTopDark = Color(0xFF4A4A4A)
val CanonicalTextPrimaryDark = Color(0xFFFFFFE3)
val CanonicalTextSecondaryDark = Color(0xFFCBCBCB)
val CanonicalTextMutedDark = Color(0xFF8E9296)
val CanonicalDividerDark = Color(0xFF4A4A4A) // Solid interior separator
val CanonicalAccentActionDark = Color(0xFFFFFFE3)
val CanonicalDestructiveDark = Color(0xFF8C5252)

// Canonical Light Tokens (Warm Ivory & Solid Surface System)
val CanonicalCanvasLight = Color(0xFFFFFFE3) // Warm Ivory
val CanonicalCanvasSecondaryLight = Color(0xFFF4F3D6)
val CanonicalGlassFillBaseLight = Color(0xFFFAF9F0) // Solid Translucent-free Card
val CanonicalGlassFillElevatedLight = Color(0xFFFAF9F0) // Solid Elevated Dock
val CanonicalGlassFillSubtleLight = Color(0xFFF4F3D6)
val CanonicalGlassBorderTopLight = Color(0xFFCBCBCB)
val CanonicalGlassBorderBottomLight = Color(0xFFCBCBCB)
val CanonicalGlassSheenTopLight = Color(0xFFCBCBCB)
val CanonicalTextPrimaryLight = Color(0xFF4A4A4A)
val CanonicalTextSecondaryLight = Color(0xFF6D8196)
val CanonicalTextMutedDarkLight = Color(0xFF7A7D82)
val CanonicalTextMutedLight = Color(0xFF7A7D82)
val CanonicalDividerLight = Color(0xFFCBCBCB)
val CanonicalAccentActionLight = Color(0xFF4A4A4A)
val CanonicalDestructiveLight = Color(0xFF8C5252)

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
// Ink Wash Universal Palette (Authentic Japanese Ink & Neutral Parchment)
// Canonical Swatches (Premium Monochromatic & Refined Warm Off-White):
// 1. Charcoal Slate:      #171717 / #4A4A4A
// 2. Mist Silver:         #CBCBCB / #D6D6DC
// 3. Off-White Canvas:    #FAFAF7 (Purged all yellow/cream #FFFFE3)
// 4. Slate Steel Wash:    #5E6E82 / #6D8196
// Zero yellow cast, zero neon blue, zero neon green, zero AI-sloped colors.
// =========================================================================
val InkWashCharcoal = Color(0xFF171717)       // Primary deep charcoal / Dark text / Light primary button
val InkWashMist = Color(0xFFD6D6DC)           // Mid neutral / Soft gray edge definition
val InkWashIvory = Color(0xFFFAFAF7)          // Clean off-white canvas with subtle warm undertone
val InkWashSlate = Color(0xFF5E6E82)          // Slate steel wash / Universal active highlight & badge

// Tonal derivatives ensuring Dark Mode is darker than Light Mode
val InkWashCharcoalDeep = Color(0xFF1E2022)   // Dark Canvas base (darker than #4A4A4A)
val InkWashCharcoalSurface = Color(0xFF2B2D30)// Dark Card Surface
val InkWashCharcoalElevated = Color(0xFF35383C)// Dark Elevated Dock & Modals
val InkWashCharcoalBorder = Color(0xFF4A4A4A) // Dark Hairline Border
val InkWashIvoryPaper = Color(0xFFFAFAF7)     // Light Canvas (#FAFAF7)
val InkWashIvoryCard = Color(0xB8FFFFFF)      // Primary glass surface: rgba(255, 255, 255, 0.72)
val InkWashIvoryElevated = Color(0xD6FFFFFF)  // Elevated glass surface: rgba(255, 255, 255, 0.84)
val InkWashRustMuted = Color(0xFF8C5252)      // Subtle muted rust for delete/destructive actions

// Semantic Tokens redirected to Ink Wash
val OklchIndigo = InkWashSlate
val OklchIndigoSoft = Color(0x335E6E82)
val OklchEmerald = InkWashSlate
val OklchEmeraldSoft = Color(0x265E6E82)
val OklchAmber = InkWashSlate
val OklchAmberSoft = Color(0x265E6E82)
val OklchCoral = InkWashRustMuted
val OklchCoralSoft = Color(0x268C5252)
val OklchCyan = InkWashSlate
val OklchCyanSoft = Color(0x265E6E82)
val OklchPurple = InkWashSlate
val OklchPurpleSoft = Color(0x265E6E82)

// --- Dark Mode Tokens (Deep Charcoal & Translucent Glass) ---
val GrapeCanvasDark = InkWashCharcoalDeep
val GrapeSurfaceSoft = InkWashCharcoalSurface
val GrapeSurfaceCard = InkWashCharcoalSurface
val GrapeSurfaceElevated = InkWashCharcoalElevated
val GrapeHairline = InkWashCharcoalBorder
val GrapeHairlineStrong = InkWashMist

val PrimaryDarkCanvas = InkWashCharcoalDeep
val PrimaryAccentLime = Color(0xFFF0F0F2)

val BugattiCanvas = InkWashCharcoalDeep
val BugattiSurfaceSoft = GrapeSurfaceSoft
val BugattiSurfaceCard = GrapeSurfaceCard
val BugattiSurfaceElevated = GrapeSurfaceElevated
val BugattiHairline = GrapeHairline
val BugattiHairlineStrong = GrapeHairlineStrong

val BugattiInk = Color(0xFFF5F5F7)
val BugattiBody = InkWashMist
val BugattiBodyStrong = Color(0xFFF5F5F7)
val BugattiMuted = Color(0xFF8E9296)
val BugattiMutedSoft = Color(0xFF8E9296)

val BugattiLink = InkWashSlate
val BugattiWarning = InkWashSlate
val BugattiSuccess = InkWashSlate
val BugattiDanger = InkWashRustMuted

// --- Light Mode Tokens (Clean Off-White Canvas & Translucent Soft Surfaces) ---
val LightCanvas = InkWashIvoryPaper           // #FAFAF7
val LightCanvasSoft = Color(0xFFF5F5F0)       // #F5F5F0
val LightSurfaceCard = InkWashIvoryCard       // rgba(255, 255, 255, 0.72)
val LightSurfaceElevated = InkWashIvoryElevated// rgba(255, 255, 255, 0.84)
val LightHairline = Color(0x171E1E1E)         // rgba(30, 30, 30, 0.09)
val LightHairlineStrong = Color(0xFF4A4A4A)
val LightGlassSpecular = Color(0xCCFFFFFF)    // rgba(255, 255, 255, 0.80)

val LightInkPrimary = Color(0xFF171717)       // #171717
val LightInkSecondary = Color(0xFF66686C)     // #66686C
val LightInkMuted = Color(0xFF8E9096)

val MobbinCanvas = LightCanvas
val MobbinCanvasSoft = LightCanvasSoft
val MobbinField = Color(0xFFF2F2F5)
val MobbinHairline = LightHairline
val MobbinHairlineSoft = Color(0x121E1E1E)

val MobbinInk = LightInkPrimary
val MobbinInkSoft = LightInkSecondary
val MobbinTextMuted = LightInkMuted
val MobbinTextFaint = Color(0xFF9E9FA3)
val MobbinAccent = InkWashCharcoal

val MobbinSuccess = InkWashSlate
val MobbinWarning = InkWashSlate
val MobbinDanger = InkWashRustMuted

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
  val oklchPrimary: Color = InkWashSlate,
  val oklchSuccess: Color = InkWashSlate,
  val oklchWarning: Color = InkWashSlate,
  val oklchDanger: Color = InkWashRustMuted,
  val oklchFocus: Color = InkWashSlate,
  val oklchPurple: Color = InkWashSlate,
  val surfaceCard: Color = if (isDark) InkWashCharcoalSurface else InkWashIvoryCard,
  val surfaceElevated: Color = if (isDark) InkWashCharcoalElevated else InkWashIvoryCard
)

val LightColors = AppColors(
  paperBackground = InkWashIvoryPaper,
  paperDotColor = Color.Transparent,
  inkPrimary = LightInkPrimary,
  inkSecondary = LightInkSecondary,
  inkMuted = LightInkMuted,
  cardBackground = InkWashIvoryCard,
  cardBorder = LightHairline,
  pillBackgroundDark = LightInkPrimary,
  pillTextDark = Color(0xFFFAFAF7),
  progressBarTrack = Color(0xFFE8E8EE),
  progressBarFilled = InkWashSlate,
  accentTerracotta = InkWashSlate,
  accentOrange = InkWashSlate,
  accentYellowHighlight = Color(0x205E6E82),
  accentRed = InkWashRustMuted,
  chipBorder = LightHairline,
  chipUnselected = Color(0xFFF2F2F5),
  isDark = false,
  oklchPrimary = InkWashSlate,
  oklchSuccess = InkWashSlate,
  oklchWarning = InkWashSlate,
  oklchDanger = InkWashRustMuted,
  oklchFocus = InkWashSlate,
  oklchPurple = InkWashSlate,
  surfaceCard = InkWashIvoryCard,
  surfaceElevated = InkWashIvoryElevated
)

val DarkColors = AppColors(
  paperBackground = InkWashCharcoalDeep,
  paperDotColor = Color.Transparent,
  inkPrimary = InkWashIvory,
  inkSecondary = InkWashMist,
  inkMuted = Color(0xFF8E9296),
  cardBackground = InkWashCharcoalSurface,
  cardBorder = InkWashCharcoalBorder,
  pillBackgroundDark = InkWashIvory,
  pillTextDark = InkWashCharcoalDeep,
  progressBarTrack = Color(0x334A4A4A),
  progressBarFilled = InkWashSlate,
  accentTerracotta = InkWashSlate,
  accentOrange = InkWashSlate,
  accentYellowHighlight = Color(0x286D8196),
  accentRed = InkWashRustMuted,
  chipBorder = InkWashCharcoalBorder,
  chipUnselected = Color(0x334A4A4A),
  isDark = true,
  oklchPrimary = InkWashSlate,
  oklchSuccess = InkWashSlate,
  oklchWarning = InkWashSlate,
  oklchDanger = InkWashRustMuted,
  oklchFocus = InkWashSlate,
  oklchPurple = InkWashSlate,
  surfaceCard = InkWashCharcoalSurface,
  surfaceElevated = InkWashCharcoalElevated
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
