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
// Ink Wash Universal Palette (Authentic Japanese Ink & Parchment)
// Canonical Swatches:
// 1. Charcoal Slate:   #4A4A4A
// 2. Mist Silver:      #CBCBCB
// 3. Warm Ivory:       #FFFFE3
// 4. Slate Steel Wash: #6D8196
// Zero purple, zero neon blue, zero neon green, zero AI-sloped colors.
// =========================================================================
val InkWashCharcoal = Color(0xFF4A4A4A)       // Primary deep neutral / Dark text / Light primary button
val InkWashMist = Color(0xFFCBCBCB)           // Mid neutral / Hairlines / Muted accents
val InkWashIvory = Color(0xFFFFFFE3)          // Warm cream ivory / Light canvas / Dark text primary
val InkWashSlate = Color(0xFF6D8196)          // Slate steel wash / Universal active highlight & badge

// Tonal derivatives ensuring Dark Mode is darker than Light Mode
val InkWashCharcoalDeep = Color(0xFF1E2022)   // Dark Canvas base (darker than #4A4A4A)
val InkWashCharcoalSurface = Color(0xFF2B2D30)// Dark Card Surface
val InkWashCharcoalElevated = Color(0xFF35383C)// Dark Elevated Dock & Modals
val InkWashCharcoalBorder = Color(0xFF4A4A4A) // Dark Hairline Border
val InkWashIvoryPaper = Color(0xFFFFFFE3)     // Light Canvas (Warm Ivory)
val InkWashIvoryCard = Color(0xFFFAF9F0)      // Light Card Surface
val InkWashRustMuted = Color(0xFF8C5252)      // Subtle muted rust for delete/destructive actions

// Semantic Tokens redirected to Ink Wash
val OklchIndigo = InkWashSlate
val OklchIndigoSoft = Color(0x336D8196)
val OklchEmerald = InkWashSlate
val OklchEmeraldSoft = Color(0x266D8196)
val OklchAmber = InkWashSlate
val OklchAmberSoft = Color(0x266D8196)
val OklchCoral = InkWashRustMuted
val OklchCoralSoft = Color(0x268C5252)
val OklchCyan = InkWashSlate
val OklchCyanSoft = Color(0x266D8196)
val OklchPurple = InkWashSlate
val OklchPurpleSoft = Color(0x266D8196)

// --- Dark Mode Tokens (Deep Charcoal & Translucent Glass) ---
val GrapeCanvasDark = InkWashCharcoalDeep
val GrapeSurfaceSoft = InkWashCharcoalSurface
val GrapeSurfaceCard = InkWashCharcoalSurface
val GrapeSurfaceElevated = InkWashCharcoalElevated
val GrapeHairline = InkWashCharcoalBorder
val GrapeHairlineStrong = InkWashMist

val PrimaryDarkCanvas = InkWashCharcoalDeep
val PrimaryAccentLime = InkWashIvory

val BugattiCanvas = InkWashCharcoalDeep
val BugattiSurfaceSoft = GrapeSurfaceSoft
val BugattiSurfaceCard = GrapeSurfaceCard
val BugattiSurfaceElevated = GrapeSurfaceElevated
val BugattiHairline = GrapeHairline
val BugattiHairlineStrong = GrapeHairlineStrong

val BugattiInk = InkWashIvory
val BugattiBody = InkWashMist
val BugattiBodyStrong = InkWashIvory
val BugattiMuted = Color(0xFF8E9296)
val BugattiMutedSoft = Color(0xFF8E9296)

val BugattiLink = InkWashSlate
val BugattiWarning = InkWashSlate
val BugattiSuccess = InkWashSlate
val BugattiDanger = InkWashRustMuted

// --- Light Mode Tokens (Clean White Canvas & Translucent Soft Surfaces) ---
val LightCanvas = InkWashIvoryPaper
val LightCanvasSoft = Color(0xFFF5F5DC)
val LightSurfaceCard = InkWashIvoryCard
val LightHairline = InkWashMist
val LightHairlineStrong = InkWashCharcoal

val LightInkPrimary = InkWashCharcoal
val LightInkSecondary = InkWashSlate
val LightInkMuted = Color(0xFF7A7D82)

val MobbinCanvas = LightCanvas
val MobbinCanvasSoft = LightCanvasSoft
val MobbinField = Color(0xFFF3F3E8)
val MobbinHairline = LightHairline
val MobbinHairlineSoft = Color(0x184A4A4A)

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
  inkPrimary = InkWashCharcoal,
  inkSecondary = InkWashSlate,
  inkMuted = Color(0xFF7A7D82),
  cardBackground = InkWashIvoryCard,
  cardBorder = InkWashMist,
  pillBackgroundDark = InkWashCharcoal,
  pillTextDark = InkWashIvory,
  progressBarTrack = Color(0xFFE5E5DB),
  progressBarFilled = InkWashSlate,
  accentTerracotta = InkWashSlate,
  accentOrange = InkWashSlate,
  accentYellowHighlight = Color(0x206D8196),
  accentRed = InkWashRustMuted,
  chipBorder = InkWashMist,
  chipUnselected = Color(0xFFF3F3E8),
  isDark = false,
  oklchPrimary = InkWashSlate,
  oklchSuccess = InkWashSlate,
  oklchWarning = InkWashSlate,
  oklchDanger = InkWashRustMuted,
  oklchFocus = InkWashSlate,
  oklchPurple = InkWashSlate,
  surfaceCard = InkWashIvoryCard,
  surfaceElevated = InkWashIvoryCard
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
