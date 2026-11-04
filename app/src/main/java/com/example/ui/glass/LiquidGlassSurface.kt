package com.example.ui.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
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
 * Liquid Glass Refraction Shader (AGSL, Android 13+).
 * Evaluates the live backdrop blurred scene:
 * 1. SDF rounded rectangle clipping with 1.2px anti-aliased edge falloff.
 * 2. Curvature-based refractive coordinate offset with spectral dispersion (RGB channel separation).
 * 3. Directional Fresnel rim brightening and specular edge definition.
 */
const val LIQUID_GLASS_SHADER = """
    uniform shader content;
    uniform float2 uSize;
    uniform float uRadius;
    uniform float uBand;
    uniform float uDisp;
    uniform float uFresnel;

    float sdRoundRect(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + r;
        return length(max(q, float2(0.0))) + min(max(q.x, q.y), 0.0) - r;
    }

    half4 main(float2 fragCoord) {
        float2 halfSize = uSize * 0.5;
        float2 p = fragCoord - halfSize;
        float d = sdRoundRect(p, halfSize, uRadius);
        if (d > 0.0) {
            return half4(0.0);
        }
        float edgeAA = 1.0 - smoothstep(-1.2, 0.0, d);

        float t = clamp(-d / uBand, 0.0, 1.0);
        float bend = (1.0 - t) * (1.0 - t);

        float2 q = abs(p) - halfSize + uRadius;
        float2 n;
        if (q.x > 0.0 && q.y > 0.0) {
            n = normalize(sign(p) * q);
        } else if (q.x > q.y) {
            n = float2(sign(p.x), 0.0);
        } else {
            n = float2(0.0, sign(p.y));
        }

        float2 disp = -n * bend * uDisp;
        half3 col;
        col.r = content.eval(fragCoord + disp * 1.10).r;
        col.g = content.eval(fragCoord + disp).g;
        col.b = content.eval(fragCoord + disp * 0.90).b;

        float topBias = 0.55 + 0.45 * clamp(-p.y / halfSize.y, -1.0, 1.0);
        float rim = bend * uFresnel * topBias;
        float lineBand = smoothstep(2.5, 0.8, -d);
        col += half3(rim + lineBand * uFresnel * 0.6 * topBias);

        return half4(col * edgeAA, edgeAA);
    }
"""

/**
 * Optical Liquid Glass Backdrop:
 * Live Haze blur (20dp) with AGSL edge refraction and chromatic dispersion on Android 13+.
 * On Android <13, safely falls back to standard clip and ambient backdrop blur.
 */
@Composable
fun LiquidGlassBackdrop(
    hazeState: HazeState,
    shape: Shape,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surface
    val glassTint = if (isDark) surface.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.45f)
    val fallback = surface.copy(alpha = if (isDark) 0.80f else 0.86f)
    val density = LocalDensity.current

    val refractionModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(LIQUID_GLASS_SHADER) }
        val bandPx = with(density) { 20.dp.toPx() }
        val dispPx = with(density) { 9.dp.toPx() }
        val fresnel = if (isDark) 0.22f else 0.10f
        Modifier.graphicsLayer {
            shader.setFloatUniform("uSize", size.width, size.height)
            shader.setFloatUniform("uRadius", size.height * 0.5f)
            shader.setFloatUniform("uBand", bandPx)
            shader.setFloatUniform("uDisp", dispPx)
            shader.setFloatUniform("uFresnel", fresnel)
            renderEffect = RenderEffect
                .createRuntimeShaderEffect(shader, "content")
                .asComposeRenderEffect()
            clip = false
        }
    } else {
        Modifier.clip(shape)
    }

    Box(
        modifier = modifier
            .then(refractionModifier)
            .hazeEffect(state = hazeState) {
                blurRadius = 20.dp
                noiseFactor = if (isDark) 0.04f else 0f
                tints = listOf(HazeTint(glassTint))
                fallbackTint = HazeTint(fallback)
            },
    )
}

/** Crisp neutral rim with directional specular highlights. */
fun Modifier.liquidGlassRim(
    shape: Shape,
    isDark: Boolean,
    borderWidth: Dp = 1.dp,
): Modifier {
    val borderBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.06f))
        } else {
            listOf(Color.White.copy(alpha = 0.80f), Color.Black.copy(alpha = 0.14f))
        }
    )
    return clip(shape).border(
        width = borderWidth,
        brush = borderBrush,
        shape = shape,
    )
}

/** Crisp specular highlight and ambient surface sheen. */
@Composable
fun LiquidGlassHighlight(
    shape: Shape,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val sheenBrush = Brush.verticalGradient(
        0f to Color.White.copy(alpha = if (isDark) 0.08f else 0.10f),
        0.55f to Color.White.copy(alpha = if (isDark) 0.02f else 0.06f),
        1f to Color.White.copy(alpha = if (isDark) 0.05f else 0.08f),
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(sheenBrush)
            .liquidGlassRim(shape, isDark),
    )
}

/**
 * Pre-configured Liquid Glass container combining backdrop blur, AGSL refraction,
 * and specular edge highlight with content layered cleanly on top.
 */
@Composable
fun LiquidGlassContainer(
    hazeState: HazeState,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        LiquidGlassBackdrop(
            hazeState = hazeState,
            shape = shape,
            isDark = isDark,
            modifier = Modifier.matchParentSize(),
        )
        LiquidGlassHighlight(
            shape = shape,
            isDark = isDark,
            modifier = Modifier.matchParentSize(),
        )
        Box(modifier = Modifier.padding(contentPadding), content = content)
    }
}
