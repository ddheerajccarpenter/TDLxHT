package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val targetColors = if (darkTheme) DarkColors else LightColors
  val animSpec = tween<Color>(durationMillis = 200, easing = FastOutSlowInEasing)

  val paperBackground by animateColorAsState(targetColors.paperBackground, animSpec, label = "paperBg")
  val inkPrimary by animateColorAsState(targetColors.inkPrimary, animSpec, label = "inkPri")
  val inkSecondary by animateColorAsState(targetColors.inkSecondary, animSpec, label = "inkSec")
  val inkMuted by animateColorAsState(targetColors.inkMuted, animSpec, label = "inkMut")
  val cardBackground by animateColorAsState(targetColors.cardBackground, animSpec, label = "cardBg")
  val cardBorder by animateColorAsState(targetColors.cardBorder, animSpec, label = "cardBrd")
  val pillBackgroundDark by animateColorAsState(targetColors.pillBackgroundDark, animSpec, label = "pillBg")
  val pillTextDark by animateColorAsState(targetColors.pillTextDark, animSpec, label = "pillTxt")
  val progressBarTrack by animateColorAsState(targetColors.progressBarTrack, animSpec, label = "pbTrack")
  val progressBarFilled by animateColorAsState(targetColors.progressBarFilled, animSpec, label = "pbFill")
  val accentTerracotta by animateColorAsState(targetColors.accentTerracotta, animSpec, label = "accTerra")
  val accentOrange by animateColorAsState(targetColors.accentOrange, animSpec, label = "accOrg")
  val accentYellowHighlight by animateColorAsState(targetColors.accentYellowHighlight, animSpec, label = "accYel")
  val accentRed by animateColorAsState(targetColors.accentRed, animSpec, label = "accRed")
  val chipBorder by animateColorAsState(targetColors.chipBorder, animSpec, label = "chipBrd")
  val chipUnselected by animateColorAsState(targetColors.chipUnselected, animSpec, label = "chipUnsel")

  val appColors = AppColors(
    paperBackground = paperBackground,
    paperDotColor = targetColors.paperDotColor,
    inkPrimary = inkPrimary,
    inkSecondary = inkSecondary,
    inkMuted = inkMuted,
    cardBackground = cardBackground,
    cardBorder = cardBorder,
    pillBackgroundDark = pillBackgroundDark,
    pillTextDark = pillTextDark,
    progressBarTrack = progressBarTrack,
    progressBarFilled = progressBarFilled,
    accentTerracotta = accentTerracotta,
    accentOrange = accentOrange,
    accentYellowHighlight = accentYellowHighlight,
    accentRed = accentRed,
    chipBorder = chipBorder,
    chipUnselected = chipUnselected,
    isDark = darkTheme
  )

  val colorScheme = if (darkTheme) {
    darkColorScheme(
      primary = appColors.inkPrimary,
      onPrimary = appColors.cardBackground,
      primaryContainer = appColors.cardBackground,
      onPrimaryContainer = appColors.inkPrimary,
      secondary = appColors.accentTerracotta,
      onSecondary = appColors.cardBackground,
      tertiary = appColors.accentOrange,
      background = appColors.paperBackground,
      onBackground = appColors.inkPrimary,
      surface = appColors.cardBackground,
      onSurface = appColors.inkPrimary,
      surfaceVariant = appColors.paperBackground,
      onSurfaceVariant = appColors.inkSecondary,
      outline = appColors.cardBorder
    )
  } else {
    lightColorScheme(
      primary = appColors.inkPrimary,
      onPrimary = appColors.cardBackground,
      primaryContainer = appColors.cardBackground,
      onPrimaryContainer = appColors.inkPrimary,
      secondary = appColors.accentTerracotta,
      onSecondary = appColors.cardBackground,
      tertiary = appColors.accentOrange,
      background = appColors.paperBackground,
      onBackground = appColors.inkPrimary,
      surface = appColors.cardBackground,
      onSurface = appColors.inkPrimary,
      surfaceVariant = appColors.paperBackground,
      onSurfaceVariant = appColors.inkSecondary,
      outline = appColors.cardBorder
    )
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      var ctx = view.context
      while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) break
        ctx = ctx.baseContext
      }
      val activity = ctx as? Activity
      activity?.window?.let { window ->
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  CompositionLocalProvider(
    LocalAppColors provides appColors
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography
    ) {
      CompositionLocalProvider(
        androidx.compose.foundation.LocalIndication provides NoIndication,
        content = content
      )
    }
  }
}

/**
 * Clean no-op indication instance.
 * Completely suppresses unwanted rectangular Material ripples and highlights
 * globally across the application, letting intentional component shapes (circular/rounded)
 * and elastic spring feedback dictate tactile response.
 */
object NoIndication : androidx.compose.foundation.IndicationNodeFactory {
  override fun create(interactionSource: androidx.compose.foundation.interaction.InteractionSource): androidx.compose.ui.Modifier.Node {
    return object : androidx.compose.ui.Modifier.Node() {}
  }
  override fun hashCode(): Int = -1
  override fun equals(other: Any?): Boolean = other === this
}
