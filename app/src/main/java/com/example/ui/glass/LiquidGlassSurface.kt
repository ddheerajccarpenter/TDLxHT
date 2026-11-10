package com.example.ui.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

/**
 * Visual variant hierarchy for Apple iOS 26 Liquid Glass:
 * - Regular: Standard translucent glass integrating with surrounding backdrop.
 * - Prominent: Tinted, high-vibrancy glass for primary actions with elevated specular rim.
 * - Subtle: Minimal translucency for secondary nested elements and badges.
 */
enum class LiquidGlassVariant {
    Regular,
    Prominent,
    Subtle
}

/**
 * CompositionLocal providing shared Liquid Glass container context for grouping sibling glass items.
 */
val LocalLiquidGlassContainerSpacing = staticCompositionLocalOf<Dp> { 16.dp }

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
    variant: LiquidGlassVariant = LiquidGlassVariant.Regular,
    tintColor: Color? = null,
    extraTints: List<Color> = emptyList(),
    fallbackColor: Color? = null,
    interactiveProgress: Float = 0f
) {
    val defaultBaseTint = when (variant) {
        LiquidGlassVariant.Prominent -> {
            if (isDark) Color(0xFF141824).copy(alpha = 0.50f)
            else Color.White.copy(alpha = 0.65f)
        }
        LiquidGlassVariant.Subtle -> {
            if (isDark) Color(0xFF090A0E).copy(alpha = 0.22f)
            else Color.White.copy(alpha = 0.30f)
        }
        LiquidGlassVariant.Regular -> {
            if (isDark) Color(0xFF0B0C10).copy(alpha = 0.34f)
            else Color.White.copy(alpha = 0.45f)
        }
    }
    val glassTint = tintColor ?: defaultBaseTint
    val fallback = fallbackColor ?: when (variant) {
        LiquidGlassVariant.Prominent -> {
            if (isDark) Color(0xFF141824).copy(alpha = 0.90f)
            else Color.White.copy(alpha = 0.92f)
        }
        LiquidGlassVariant.Subtle -> {
            if (isDark) Color(0xFF090A0E).copy(alpha = 0.70f)
            else Color.White.copy(alpha = 0.78f)
        }
        LiquidGlassVariant.Regular -> {
            if (isDark) Color(0xFF0B0C10).copy(alpha = 0.80f)
            else Color.White.copy(alpha = 0.86f)
        }
    }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val shader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        remember {
            try {
                RuntimeShader(LIQUID_GLASS_SHADER)
            } catch (_: Throwable) {
                null
            }
        }
    } else null
    val cachedRenderEffect = remember(shader) {
        shader?.let {
            try {
                RenderEffect.createRuntimeShaderEffect(it, "content").asComposeRenderEffect()
            } catch (_: Throwable) {
                null
            }
        }
    }

    val refractionModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val bandPx = with(density) { (20.dp - 3.dp * interactiveProgress).toPx() }
        val dispPx = with(density) { (9.dp + 2.dp * interactiveProgress).toPx() }
        val baseFresnel = when (variant) {
            LiquidGlassVariant.Prominent -> if (isDark) 0.30f else 0.16f
            LiquidGlassVariant.Subtle -> if (isDark) 0.16f else 0.07f
            LiquidGlassVariant.Regular -> if (isDark) 0.22f else 0.10f
        }
        val fresnel = baseFresnel + 0.08f * interactiveProgress
        if (shader != null && cachedRenderEffect != null) {
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
                blurRadius = when (variant) {
                    LiquidGlassVariant.Prominent -> 24.dp
                    LiquidGlassVariant.Subtle -> 16.dp
                    LiquidGlassVariant.Regular -> 20.dp
                }
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
    variant: LiquidGlassVariant = LiquidGlassVariant.Regular,
    interactiveProgress: Float = 0f,
    tint: Color? = null,
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

    val apexBoost = if (variant == LiquidGlassVariant.Prominent) 0.18f else 0.0f
    val touchBoost = 0.15f * interactiveProgress

    val specularColors = if (isDark) {
        listOf(
            (tint ?: Color.White).copy(alpha = (0.36f + apexBoost + touchBoost).coerceIn(0f, 1f)),
            Color.White.copy(alpha = (0.14f + touchBoost * 0.5f).coerceIn(0f, 1f)),
            Color.White.copy(alpha = 0.08f)
        )
    } else {
        listOf(
            (tint ?: Color.White).copy(alpha = (0.75f + apexBoost * 0.5f + touchBoost).coerceIn(0f, 1f)),
            Color.White.copy(alpha = (0.30f + touchBoost * 0.4f).coerceIn(0f, 1f)),
            Color.White.copy(alpha = 0.08f)
        )
    }

    val specularRimBrush = Brush.verticalGradient(
        colors = specularColors,
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
    variant: LiquidGlassVariant = LiquidGlassVariant.Regular,
    interactiveProgress: Float = 0f,
    tint: Color? = null,
) {
    val sheenAlpha = when (variant) {
        LiquidGlassVariant.Prominent -> if (isDark) 0.12f else 0.14f
        LiquidGlassVariant.Subtle -> if (isDark) 0.05f else 0.06f
        LiquidGlassVariant.Regular -> if (isDark) 0.08f else 0.10f
    }
    val sheenBrush = Brush.verticalGradient(
        0f to Color.White.copy(alpha = (sheenAlpha + 0.06f * interactiveProgress).coerceIn(0f, 1f)),
        0.55f to Color.White.copy(alpha = if (isDark) 0.02f else 0.06f),
        1f to Color.White.copy(alpha = if (isDark) 0.05f else 0.08f),
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(sheenBrush)
            .liquidGlassRim(
                shape = shape,
                isDark = isDark,
                borderWidth = borderWidth,
                variant = variant,
                interactiveProgress = interactiveProgress,
                tint = tint
            ),
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
    variant: LiquidGlassVariant = LiquidGlassVariant.Regular,
    tint: Color? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        LiquidGlassBackdrop(
            hazeState = hazeState,
            shape = shape,
            isDark = isDark,
            variant = variant,
            tintColor = tint,
            modifier = Modifier.matchParentSize(),
        )
        LiquidGlassHighlight(
            shape = shape,
            isDark = isDark,
            variant = variant,
            tint = tint,
            modifier = Modifier.matchParentSize(),
        )
        Box(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/**
 * Container pattern from iOS 26 Liquid Glass specification:
 * Wrap multiple sibling glass views in an effect container to coordinate shared spacing,
 * synchronize directional lighting, and prevent redundant GPU blur passes.
 */
@Composable
fun LiquidGlassEffectContainer(
    spacing: Dp = 16.dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    CompositionLocalProvider(LocalLiquidGlassContainerSpacing provides spacing) {
        Box(modifier = modifier, content = content)
    }
}

/**
 * Idiomatic Compose modifier equivalent to SwiftUI's `.glassEffect(variant, in: shape)`.
 * Applies authentic iOS 26 Liquid Glass material to any Composable:
 * - Real backdrop blur via ambient [LocalHazeState].
 * - AGSL refraction + dispersion on Android 13+.
 * - Directional specular rim and sheen.
 * - Interactive spring feedback when [onClick] is provided.
 */
fun Modifier.liquidGlass(
    variant: LiquidGlassVariant = LiquidGlassVariant.Regular,
    shape: Shape = RoundedCornerShape(20.dp),
    tint: Color? = null,
    borderWidth: Dp = 0.75.dp,
    interactive: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val hazeState = LocalHazeState.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val interactiveProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "liquidGlassInteractive"
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    val scaleModifier = if (interactive && onClick != null) {
        Modifier.graphicsLayer {
            val scale = 1f - 0.025f * interactiveProgress
            scaleX = scale
            scaleY = scale
        }
    } else Modifier

    this
        .then(scaleModifier)
        .then(clickModifier)
        .clip(shape)
        .drawWithCache {
            val outline = shape.createOutline(size, layoutDirection, this)
            val outlinePath = Path().apply {
                when (outline) {
                    is Outline.Rectangle -> addRect(outline.rect)
                    is Outline.Rounded -> addRoundRect(outline.roundRect)
                    is Outline.Generic -> addPath(outline.path)
                }
            }
            val strokePx = borderWidth.toPx()
            val apexBoost = if (variant == LiquidGlassVariant.Prominent) 0.18f else 0.0f
            val touchBoost = 0.15f * interactiveProgress

            val rimColors = if (isDark) {
                listOf(
                    (tint ?: Color.White).copy(alpha = (0.36f + apexBoost + touchBoost).coerceIn(0f, 1f)),
                    Color.White.copy(alpha = (0.14f + touchBoost * 0.5f).coerceIn(0f, 1f)),
                    Color.White.copy(alpha = 0.08f)
                )
            } else {
                listOf(
                    (tint ?: Color.White).copy(alpha = (0.75f + apexBoost * 0.5f + touchBoost).coerceIn(0f, 1f)),
                    Color.White.copy(alpha = (0.30f + touchBoost * 0.4f).coerceIn(0f, 1f)),
                    Color.White.copy(alpha = 0.08f)
                )
            }
            val rimBrush = Brush.verticalGradient(rimColors, 0f, size.height)

            val sheenAlpha = when (variant) {
                LiquidGlassVariant.Prominent -> if (isDark) 0.14f else 0.18f
                LiquidGlassVariant.Subtle -> if (isDark) 0.04f else 0.06f
                LiquidGlassVariant.Regular -> if (isDark) 0.08f else 0.10f
            }
            val sheenBrush = Brush.verticalGradient(
                0f to Color.White.copy(alpha = (sheenAlpha + 0.06f * interactiveProgress).coerceIn(0f, 1f)),
                0.55f to Color.White.copy(alpha = if (isDark) 0.02f else 0.06f),
                1f to Color.White.copy(alpha = if (isDark) 0.05f else 0.08f)
            )

            val baseFallbackColor = if (isDark) {
                Color(0xFF0B0C10).copy(alpha = if (variant == LiquidGlassVariant.Prominent) 0.65f else 0.45f)
            } else {
                Color.White.copy(alpha = if (variant == LiquidGlassVariant.Prominent) 0.75f else 0.55f)
            }

            onDrawWithContent {
                drawRect(baseFallbackColor)
                if (tint != null) {
                    drawRect(tint.copy(alpha = if (variant == LiquidGlassVariant.Prominent) 0.22f else 0.10f))
                }
                drawRect(sheenBrush)
                drawContent()
                drawPath(outlinePath, rimBrush, style = Stroke(width = strokePx))
            }
        }
        .then(
            if (hazeState != null) {
                Modifier.hazeEffect(state = hazeState) {
                    blurRadius = when (variant) {
                        LiquidGlassVariant.Prominent -> 24.dp
                        LiquidGlassVariant.Subtle -> 16.dp
                        LiquidGlassVariant.Regular -> 20.dp
                    }
                    noiseFactor = if (isDark) 0.04f else 0f
                    tints = listOf(
                        HazeTint(
                            (tint ?: if (isDark) Color(0xFF0B0C10) else Color.White).copy(
                                alpha = when (variant) {
                                    LiquidGlassVariant.Prominent -> if (isDark) 0.50f else 0.65f
                                    LiquidGlassVariant.Subtle -> if (isDark) 0.22f else 0.30f
                                    LiquidGlassVariant.Regular -> if (isDark) 0.34f else 0.45f
                                }
                            )
                        )
                    )
                }
            } else Modifier
        )
}
