package com.example.ui.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.min

/**
 * AGSL Runtime Shader for Physical Prismatic Caustic Ring (Android 13+ / API 33).
 *
 * Simulates chromatic dispersion and internal caustic reflections through a curved glass lens rim:
 * 1. Computes exact distance from the annular radius with smooth anti-aliased profile.
 * 2. Generates full continuous spectral rainbow wavelength colors.
 * 3. Directional caustic lighting from top-left (-135°) with hot white specular crest.
 * 4. Secondary reciprocal internal caustic bounce on the lower-right rim (+45°).
 */
const val PRISMATIC_CAUSTIC_SHADER = """
    uniform float2 uCenter;
    uniform float uRadius;
    uniform float uThickness;
    uniform float uIntensity;
    uniform float uTime;

    // Pure continuous rainbow spectral function
    half3 spectralRainbow(float t) {
        float r = 0.5 + 0.5 * cos(6.28318 * (t + 0.00));
        float g = 0.5 + 0.5 * cos(6.28318 * (t + 0.33));
        float b = 0.5 + 0.5 * cos(6.28318 * (t + 0.67));
        return pow(half3(r, g, b), half3(1.3)) * 1.35;
    }

    half4 main(float2 fragCoord) {
        float2 p = fragCoord - uCenter;
        float dist = length(p);
        float d = dist - uRadius;
        
        // Annulus radial mask
        float halfThick = uThickness * 0.5;
        if (abs(d) > halfThick * 2.2) {
            return half4(0.0);
        }

        float angle = atan(p.y, p.x); // -PI to PI
        float normAngle = (angle + 3.14159265) / 6.2831853; // 0 to 1

        // Top-left light direction (-0.707, -0.707) -> angle ~ -2.35 rad (135 deg)
        float2 lightDir = normalize(float2(-0.85, -0.85));
        float2 normal = (dist > 0.001) ? normalize(p) : float2(0.0, 1.0);
        float dotNL = dot(normal, lightDir);

        // Directional intensity: intense on top-left crest, secondary reflection on bottom-right
        float topCrest = pow(max(0.0, dotNL), 5.0);
        float bottomBounce = pow(max(0.0, -dotNL), 3.0) * 0.38;
        float baseGlow = 0.30 + 0.70 * (topCrest + bottomBounce);

        // Rainbow spectral phase modulated by angle
        float t = fract(normAngle + 0.32);
        half3 rainbow = spectralRainbow(t);

        // Radial bell-curve profile for the caustic line
        float ringCore = exp(-pow(d / (halfThick * 0.55), 2.0));
        float ringBloom = exp(-pow(d / (halfThick * 1.15), 2.0)) * 0.45;
        float profile = ringCore + ringBloom;

        // Hot white specular hairline peak on top-left
        float specularPeak = exp(-pow(d / (halfThick * 0.28), 2.0)) * topCrest * 2.4;

        half3 finalColor = (rainbow * baseGlow * uIntensity) + half3(specularPeak);
        float finalAlpha = clamp(profile * uIntensity, 0.0, 1.0);

        return half4(finalColor * finalAlpha, finalAlpha);
    }
"""

/**
 * Multi-stop spectral palette matching the reference image chromatic rainbow caustic.
 * Electric Cyan -> Neon Lime -> Golden Yellow -> Neon Orange -> Red Coral -> Vivid Magenta -> Electric Cyan.
 */
val PrismaticCausticColors = listOf(
    Color(0xFF00E5FF), // Electric Cyan
    Color(0xFF00FF88), // Neon Lime
    Color(0xFFFFDD00), // Golden Yellow
    Color(0xFFFF7700), // Fiery Orange
    Color(0xFFFF0055), // Red Coral
    Color(0xFFD500F9), // Vivid Magenta
    Color(0xFF7C4DFF), // Deep Violet
    Color(0xFF00B0FF), // Sky Blue
    Color(0xFF00E5FF)  // Wrap back to Cyan
)

/**
 * Draws the high-intensity Prismatic Rainbow Caustic Ring on any [DrawScope].
 * Features layered bloom, chromatic caustic core, and directional hot white specular crest.
 */
fun DrawScope.drawPrismaticCausticRing(
    center: Offset,
    radius: Float,
    thickness: Float = 2.75.dp.toPx(),
    intensity: Float = 1.0f,
    isDark: Boolean = true
) {
    if (radius <= 0f) return

    val diameter = radius * 2f
    val topLeft = Offset(center.x - radius, center.y - radius)
    val size = Size(diameter, diameter)

    // 1. Soft optical caustic bloom underlay
    val bloomAlpha = if (isDark) 0.38f * intensity else 0.22f * intensity
    val bloomColors = PrismaticCausticColors.map { it.copy(alpha = it.alpha * bloomAlpha) }
    drawCircle(
        brush = Brush.sweepGradient(colors = bloomColors, center = center),
        radius = radius,
        center = center,
        style = Stroke(width = thickness * 2.2f)
    )

    // 2. Razor-sharp vibrant chromatic caustic core
    val coreAlpha = if (isDark) 0.88f * intensity else 0.72f * intensity
    val coreColors = PrismaticCausticColors.map { it.copy(alpha = it.alpha * coreAlpha) }
    drawCircle(
        brush = Brush.sweepGradient(colors = coreColors, center = center),
        radius = radius,
        center = center,
        style = Stroke(width = thickness)
    )

    // 3. Top-Left Hot White Specular Crest (starts around 195°, sweeps 90° over the top-left rim)
    val crestAlpha = (0.95f * intensity).coerceIn(0f, 1f)
    val crestBrush = Brush.sweepGradient(
        0.54f to Color.Transparent,
        0.60f to Color.White.copy(alpha = crestAlpha),
        0.72f to Color.White.copy(alpha = crestAlpha * 0.85f),
        0.80f to Color.Transparent,
        center = center
    )
    drawArc(
        brush = crestBrush,
        startAngle = 185f,
        sweepAngle = 105f,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style = Stroke(width = thickness * 1.35f, cap = StrokeCap.Round)
    )

    // 4. Secondary lower-right subtle reciprocal reflection (around 20° to 90°)
    val bounceAlpha = (0.35f * intensity).coerceIn(0f, 1f)
    val bounceBrush = Brush.sweepGradient(
        0.05f to Color.Transparent,
        0.12f to Color(0xFF00E5FF).copy(alpha = bounceAlpha),
        0.20f to Color(0xFFD500F9).copy(alpha = bounceAlpha * 0.75f),
        0.26f to Color.Transparent,
        center = center
    )
    drawArc(
        brush = bounceBrush,
        startAngle = 20f,
        sweepAngle = 75f,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style = Stroke(width = thickness * 0.85f, cap = StrokeCap.Round)
    )
}

/**
 * Modifier to render the Prismatic Caustic Ring around a shape.
 */
fun Modifier.prismaticCausticBorder(
    shape: Shape = CircleShape,
    thickness: Dp = 2.5.dp,
    intensity: Float = 1.0f,
    isDark: Boolean = true
): Modifier = this.drawWithCache {
    val radius = min(size.width, size.height) / 2f - (thickness.toPx() / 2f)
    val center = Offset(size.width / 2f, size.height / 2f)
    val thicknessPx = thickness.toPx()

    onDrawWithContent {
        drawContent()
        drawPrismaticCausticRing(
            center = center,
            radius = radius,
            thickness = thicknessPx,
            intensity = intensity,
            isDark = isDark
        )
    }
}
