package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// TDlxHT monochrome theme tokens.
// Light surfaces use white and near-black text; dark surfaces use black and white text.
// =========================================================================

// Canonical Dark Tokens (Solid Charcoal Grey System)
val CanonicalCanvasDark = Color.Black
val CanonicalCanvasSecondaryDark = Color(0xFF101010)
val CanonicalGlassFillBaseDark = Color(0xE6121212)
val CanonicalGlassFillElevatedDark = Color(0xF01B1B1B)
val CanonicalGlassFillSubtleDark = Color(0x66101010)
val CanonicalGlassBorderTopDark = Color(0xFF4A4A4A) // Solid Specular Hairline
val CanonicalGlassBorderBottomDark = Color(0xFF35383C)
val CanonicalGlassSheenTopDark = Color(0xFF4A4A4A)
val CanonicalTextPrimaryDark = Color.White
val CanonicalTextSecondaryDark = Color(0xFFBDBDBD)
val CanonicalTextMutedDark = Color(0xFF858585)
val CanonicalDividerDark = Color(0xFF4A4A4A) // Solid interior separator
val CanonicalAccentActionDark = Color.White
val CanonicalDestructiveDark = Color(0xFFBDBDBD)

// =========================================================================
// Authentic Liquid Glass Design System Tokens (Zero Blur)
// Smoky transparent graphite, neutral grey translucency, crisp silver highlights
// =========================================================================
val LiquidGlassCanvasDark = Color.Black
val LiquidGlassCanvasSecondaryDark = Color(0xFF101010)

// Translucent Layered Materials
val LiquidGlassCardTopDark = Color(0x261A1A1A)
val LiquidGlassCardBottomDark = Color(0x1A080808)
val LiquidGlassCapsuleTopDark = Color(0x33202020)
val LiquidGlassCapsuleBottomDark = Color(0x260A0A0A)
val LiquidGlassLensTopDark = Color(0x40FFFFFF)
val LiquidGlassLensMidDark = Color(0x25FFFFFF)
val LiquidGlassLensBottomDark = Color(0x14141414)

// Specular Rims & Hairline Highlights (Zero Blur)
val LiquidGlassBorderSpecularTop = Color(0x66FFFFFF)// 40% crisp overhead specular highlight
val LiquidGlassBorderSpecularMid = Color(0x20FFFFFF)// 12% lateral decay
val LiquidGlassBorderSpecularBottom = Color(0x0AFFFFFF)// 4% subtle dark rim
val LiquidGlassBorderSubtle = Color(0x18FFFFFF)     // Interior capsule separators

val LiquidGlassButtonResting = Color(0x33282B34)    // 20% alpha
val LiquidGlassButtonPressed = Color(0x523C404E)    // 32% alpha

// Canonical Light Tokens (Warm Ivory & Solid Surface System)
val CanonicalCanvasLight = Color.White
val CanonicalCanvasSecondaryLight = Color(0xFFF7F7F7)
val CanonicalGlassFillBaseLight = Color(0xD9FFFFFF)
val CanonicalGlassFillElevatedLight = Color(0xF2FFFFFF)
val CanonicalGlassFillSubtleLight = Color(0xBFFFFFFF)
val CanonicalGlassBorderTopLight = Color(0xFFCBCBCB)
val CanonicalGlassBorderBottomLight = Color(0xFFCBCBCB)
val CanonicalGlassSheenTopLight = Color(0xFFCBCBCB)
val CanonicalTextPrimaryLight = Color(0xFF4A4A4A)
val CanonicalTextSecondaryLight = Color(0xFF666666)
val CanonicalTextMutedDarkLight = Color(0xFF7A7D82)
val CanonicalTextMutedLight = Color(0xFF7A7D82)
val CanonicalDividerLight = Color(0xFFCBCBCB)
val CanonicalAccentActionLight = Color(0xFF4A4A4A)
val CanonicalDestructiveLight = Color(0xFF555555)

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
val InkWashCharcoal = Color.Black
val InkWashMist = Color(0xFFB0B0B0)
val InkWashIvory = Color(0xFFFFFFFF)          // Crisp white primary text
val InkWashSlate = Color(0xFF555555)

// Tonal derivatives ensuring Dark Mode is darker than Light Mode
val InkWashCharcoalDeep = Color.Black
val InkWashCharcoalSurface = Color(0xFF101010)
val InkWashCharcoalElevated = Color(0xFF1A1A1A)
val InkWashCharcoalBorder = Color(0x14FFFFFF) // Subtle Hairline Border (8% white)
val InkWashIvoryPaper = Color.White
val InkWashIvoryCard = Color(0xB8FFFFFF)      // Primary glass surface: rgba(255, 255, 255, 0.72)
val InkWashIvoryElevated = Color(0xD6FFFFFF)  // Elevated glass surface: rgba(255, 255, 255, 0.84)
val InkWashRustMuted = Color(0xFF555555)

// Semantic tokens are resolved from the active theme. Light mode uses slate ink;
// dark mode uses a lighter steel tint so small labels retain clear contrast.
val OklchIndigoSoft = Color(0x335E5E5E)
// These accessors are declared below LocalAppColors so every screen follows the
// same light/dark semantic palette rather than carrying its own accent colors.

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
  progressBarTrack = Color(0xFFE6E6E6),
  progressBarFilled = Color(0xFF333333),
  accentTerracotta = Color(0xFF333333),
  accentOrange = Color(0xFF333333),
  accentYellowHighlight = Color(0x18000000),
  accentRed = InkWashRustMuted,
  chipBorder = LightHairline,
  chipUnselected = Color(0xFFF2F2F5),
  isDark = false,
  oklchPrimary = InkWashSlate,
  oklchSuccess = Color(0xFF333333),
  oklchWarning = Color(0xFF666666),
  oklchDanger = Color(0xFF333333),
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
  inkMuted = Color(0xFF5C6168),
  cardBackground = InkWashCharcoalSurface,
  cardBorder = InkWashCharcoalBorder,
  pillBackgroundDark = InkWashIvory,
  pillTextDark = InkWashCharcoalDeep,
  progressBarTrack = Color(0x1AFFFFFF),
  progressBarFilled = Color(0xFFD8D8D8),
  accentTerracotta = Color(0xFFD8D8D8),
  accentOrange = Color(0xFFD8D8D8),
  accentYellowHighlight = Color(0x20FFFFFF),
  accentRed = InkWashRustMuted,
  chipBorder = InkWashCharcoalBorder,
  chipUnselected = Color(0x14FFFFFF),
  isDark = true,
  oklchPrimary = Color(0xFFE2E2E2),
  oklchSuccess = Color(0xFFE2E2E2),
  oklchWarning = Color(0xFFAAAAAA),
  oklchDanger = InkWashRustMuted,
  oklchFocus = Color(0xFFE2E2E2),
  oklchPurple = Color(0xFFE2E2E2),
  surfaceCard = InkWashCharcoalSurface,
  surfaceElevated = InkWashCharcoalElevated
)

val LocalAppColors = staticCompositionLocalOf { DarkColors }

val OklchIndigo: Color
  @Composable get() = LocalAppColors.current.oklchPrimary
val OklchEmerald: Color
  @Composable get() = LocalAppColors.current.oklchSuccess
val OklchEmeraldSoft: Color
  @Composable get() = LocalAppColors.current.oklchSuccess.copy(alpha = 0.16f)
val OklchAmber: Color
  @Composable get() = LocalAppColors.current.oklchWarning
val OklchAmberSoft: Color
  @Composable get() = LocalAppColors.current.oklchWarning.copy(alpha = 0.16f)
val OklchCoral: Color
  @Composable get() = LocalAppColors.current.oklchDanger
val OklchCoralSoft: Color
  @Composable get() = LocalAppColors.current.oklchDanger.copy(alpha = 0.16f)
val OklchCyan: Color
  @Composable get() = LocalAppColors.current.oklchFocus
val OklchCyanSoft: Color
  @Composable get() = LocalAppColors.current.oklchFocus.copy(alpha = 0.16f)
val OklchPurple: Color
  @Composable get() = LocalAppColors.current.oklchPurple
val OklchPurpleSoft: Color
  @Composable get() = LocalAppColors.current.oklchPurple.copy(alpha = 0.16f)

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
