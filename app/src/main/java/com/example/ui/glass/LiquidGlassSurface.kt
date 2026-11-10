package com.example.ui.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * CompositionLocal providing ambient HazeState for Liquid Glass surfaces throughout the tree.
 */
val LocalHazeState = androidx.compose.runtime.staticCompositionLocalOf<HazeState?> { null }

/**
 * Liquid Glass Refraction Shader (AGSL, Android 13+ / API 33).
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
        float ca = bend * 0.045;
        half3 col;
        col.r = content.eval(fragCoord + disp * (1.0 + ca)).r;
        col.g = content.eval(fragCoord + disp).g;
        col.b = content.eval(fragCoord + disp * (1.0 - ca)).b;

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
    tintColor: Color? = null,
    extraTints: List<Color> = emptyList(),
    fallbackColor: Color? = null
) {
    val glassTint = tintColor ?: if (isDark) {
        Color(0xFF0B0C10).copy(alpha = 0.34f)
    } else {
        Color.White.copy(alpha = 0.45f)
    }
    val fallback = fallbackColor ?: if (isDark) {
        Color(0xFF0B0C10).copy(alpha = 0.80f)
    } else {
        Color.White.copy(alpha = 0.86f)
    }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val refractionModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(LIQUID_GLASS_SHADER) }
        val cachedRenderEffect = remember(shader) {
            try {
                RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
            } catch (_: Throwable) { null }
        }
        val bandPx = with(density) { 20.dp.toPx() }
        val dispPx = with(density) { 9.dp.toPx() }
        val fresnel = if (isDark) 0.22f else 0.10f
        if (cachedRenderEffect != null) {
            Modifier.graphicsLayer {
                val outline = shape.createOutline(
                    androidx.compose.ui.geometry.Size(size.width, size.height),
                    layoutDirection,
                    density
                )
                val radiusPx = when (outline) {
                    is Outline.Rounded -> outline.roundRect.topLeftCornerRadius.x
                    is Outline.Rectangle -> 0f
                    is Outline.Generic -> 0f
                }.coerceIn(0f, min(size.width, size.height) * 0.5f)
                shader.setFloatUniform("uSize", size.width, size.height)
                shader.setFloatUniform("uRadius", radiusPx)
                shader.setFloatUniform("uBand", bandPx)
                shader.setFloatUniform("uDisp", dispPx)
                shader.setFloatUniform("uFresnel", fresnel)
                renderEffect = cachedRenderEffect
                clip = false
            }
        } else {
            Modifier.clip(shape)
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
                tints = buildList {
                    add(HazeTint(glassTint))
                    extraTints.forEach { add(HazeTint(it)) }
                }
                fallbackTint = HazeTint(fallback)
            },
    )
}

/**
 * Crisp optical rim with directional specular highlights.
 * Refined directional specular hairline highlight along the 0.75–1.0px glass bevel edge.
 * Completely embedded inside the glass boundary with zero outer halo rings.
 */
fun Modifier.liquidGlassRim(
    shape: Shape,
    isDark: Boolean,
    borderWidth: Dp = 0.75.dp,
): Modifier = clip(shape).drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val outlinePath = Path().apply {
        when (outline) {
            is Outline.Rectangle -> addRect(outline.rect)
            is Outline.Rounded -> addRoundRect(outline.roundRect)
            is Outline.Generic -> addPath(outline.path)
        }
    }
    val strokeWidth = borderWidth.toPx()

    // Authentic monochromatic directional specular rim:
    // Bright crisp specular apex on the upper glass rim falling off gracefully to the base.
    val specularRimBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.36f),
                Color.White.copy(alpha = 0.14f),
                Color.White.copy(alpha = 0.08f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.75f),
                Color.White.copy(alpha = 0.30f),
                Color.White.copy(alpha = 0.08f)
            )
        },
        startY = 0f,
        endY = size.height
    )

    onDrawWithContent {
        drawContent()
        drawPath(outlinePath, specularRimBrush, style = Stroke(width = strokeWidth))
    }
}

/** Crisp specular highlight and ambient surface sheen. Owns the single glass rim. */
@Composable
fun LiquidGlassHighlight(
    shape: Shape,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 0.75.dp,
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
            .liquidGlassRim(shape, isDark, borderWidth),
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
