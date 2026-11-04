package com.example.ui.glass

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * CompositionLocal providing ambient HazeState for Liquid Glass surfaces throughout the tree.
 */
val LocalHazeState = androidx.compose.runtime.staticCompositionLocalOf<HazeState?> { null }

/**
 * Records a softly blurred scene for the upstream Backdrop renderer to refract.
 * Backdrop is applied in a separate sibling layer by the shared glass components,
 * so the sample never contains itself.
 */
@Composable
fun LiquidGlassBackdrop(
    hazeState: HazeState,
    shape: RoundedCornerShape,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surface
    val glassTint = if (isDark) surface.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.45f)
    val fallback = surface.copy(alpha = if (isDark) 0.80f else 0.86f)
    Box(
        modifier = modifier
            .clip(shape)
            .hazeEffect(state = hazeState) {
                blurRadius = 18.dp
                noiseFactor = if (isDark) 0.03f else 0f
                tints = listOf(HazeTint(glassTint))
                fallbackTint = HazeTint(fallback)
            },
    )
}

/** Crisp neutral rim with low-intensity spectral dispersion confined to rounded corners. */
fun Modifier.liquidGlassRim(
    shape: Shape,
    isDark: Boolean,
    borderWidth: Dp = 1.dp,
): Modifier = clip(shape).border(
    width = borderWidth,
    color = if (isDark) Color.White.copy(alpha = 0.30f) else Color.Black.copy(alpha = 0.16f),
    shape = shape,
)

/** Crisp neutral rim above the rendered backdrop glass. */
@Composable
fun LiquidGlassHighlight(
    shape: RoundedCornerShape,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.liquidGlassRim(shape, isDark),
    )
}
