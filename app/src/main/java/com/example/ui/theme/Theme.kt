package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// =========================================================================
// Standardized TDlxHT 24dp Outer / 12dp Inner Geometry System
// =========================================================================
val OuterCornerRadius = 24.dp
val InnerCornerRadius = 12.dp
val OuterCornerShape = RoundedCornerShape(24.dp)
val InnerCornerShape = RoundedCornerShape(12.dp)
val FloatingDockShape = CircleShape
val PlusButtonShape = CircleShape

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  // Resolve the entire palette atomically; interpolating colors creates mixed themes.
  val appColors = if (darkTheme) DarkColors else LightColors

  val colorScheme = if (darkTheme) {
    darkColorScheme(
      primary = appColors.inkPrimary,
      onPrimary = appColors.pillTextDark,
      primaryContainer = appColors.cardBackground,
      onPrimaryContainer = appColors.inkPrimary,
      secondary = appColors.accentTerracotta,
      onSecondary = appColors.pillTextDark,
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
      onPrimary = appColors.pillTextDark,
      primaryContainer = appColors.cardBackground,
      onPrimaryContainer = appColors.inkPrimary,
      secondary = appColors.accentTerracotta,
      onSecondary = appColors.pillTextDark,
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
