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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlin.math.min

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

/**
 * Crisp neutral rim with directional specular highlights and corner rainbow spectral dispersion.
 * Corners refract chromatic spectral light through curved glass bevels.
 */
fun Modifier.liquidGlassRim(
    shape: Shape,
    isDark: Boolean,
    borderWidth: Dp = 1.dp,
): Modifier = clip(shape).drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val outlinePath = Path().apply {
        when (outline) {
            is Outline.Rectangle -> addRect(outline.rect)
            is Outline.Rounded -> addRoundRect(outline.roundRect)
            is Outline.Generic -> addPath(outline.path)
        }
    }
    val roundedOutline = outline as? Outline.Rounded
    val radius = roundedOutline?.roundRect?.topLeftCornerRadius?.x
        ?.coerceIn(0f, min(size.width, size.height) / 2f)
        ?: 0f
    val diameter = radius * 2f
    val strokeWidth = borderWidth.toPx()
    val neutralRim = if (isDark) Color.White.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.14f)
    val spectralAlpha = if (isDark) 0.14f else 0.08f
    val spectralColors = listOf(
        Color(0xFFFF6A6A).copy(alpha = spectralAlpha),
        Color(0xFFFFD166).copy(alpha = spectralAlpha),
        Color(0xFF6EE7B7).copy(alpha = spectralAlpha),
        Color(0xFF67D9FF).copy(alpha = spectralAlpha),
        Color(0xFF9B8CFF).copy(alpha = spectralAlpha),
        Color(0xFFFF6A6A).copy(alpha = spectralAlpha)
    )
    val corners = if (radius > 0f) listOf(
        Triple(Offset.Zero, 180f, Offset(radius, radius)),
        Triple(Offset(size.width - diameter, 0f), 270f, Offset(size.width - radius, radius)),
        Triple(Offset(size.width - diameter, size.height - diameter), 0f, Offset(size.width - radius, size.height - radius)),
        Triple(Offset(0f, size.height - diameter), 90f, Offset(radius, size.height - radius))
    ) else emptyList()
    val cornerBrushes = corners.map { (_, _, center) ->
        Brush.sweepGradient(colors = spectralColors, center = center)
    }

    onDrawWithContent {
        drawContent()
        drawPath(outlinePath, neutralRim, style = Stroke(width = strokeWidth))
        corners.forEachIndexed { index, (topLeft, startAngle, _) ->
            drawArc(
                brush = cornerBrushes[index],
                startAngle = startAngle,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth * 1.5f)
            )
        }
    }
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
