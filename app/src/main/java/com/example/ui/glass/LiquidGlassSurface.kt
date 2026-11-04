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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * Liquid Glass 折射 shader（AGSL，Android 13+）。
 * 對 Haze 畫好的模糊背景做三件事：
 * 1. 膠囊 SDF 裁切＋1.2px 邊緣抗鋸齒（取代 Compose clip）。
 * 2. 邊緣帶內把取樣座標往中心位移（越靠邊越強、平方衰減）→ 厚玻璃透鏡放大感；
 *    RGB 三通道位移量微差 → 邊緣輕微色散。
 * 3. Fresnel 邊緣增亮（偏向頂部）＋貼邊 1~2px 高光細線。
 *
 * 跟 [PILL_LENS_SHADER] 的差別：這支吃的是模糊背景，那支吃的是元件自己的內容。
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
 * 玻璃背景：Haze 即時背景模糊，Android 13+ 再疊 AGSL 折射／Fresnel。
 *
 * 要模糊的內容必須自己掛 `Modifier.hazeSource(hazeState)`，否則這裡只會拿到空白。
 */
@Composable
fun LiquidGlassBackdrop(
    hazeState: HazeState,
    shape: RoundedCornerShape,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surface
    // 淺色用純白提亮玻璃體——tint 太低時模糊背景的灰藍會透出來，被上下高光邊
    // 一對比，玻璃中段看起來像夾了深色層。0.45 白仍保有透感。
    val glassTint = if (isDark) surface.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.45f)
    val fallback = surface.copy(alpha = if (isDark) 0.80f else 0.86f)
    val density = LocalDensity.current

    val refractionModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(LIQUID_GLASS_SHADER) }
        val bandPx = with(density) { 20.dp.toPx() }
        val dispPx = with(density) { 9.dp.toPx() }
        // 淺色 fresnel 必須壓低：邊光只照到貼邊 ~20dp，中段照不到，
        // 對比拉伸實測會在玻璃中段夾出一條全寬羽化「陰影帶」
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
        // 沒有 RuntimeShader 就用一般裁切，玻璃感交給高光層撐場
        Modifier.clip(shape)
    }

    Box(
        modifier = modifier
            .then(refractionModifier)
            .hazeEffect(state = hazeState) {
                blurRadius = 22.dp
                noiseFactor = if (isDark) 0.08f else 0.02f
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
): Modifier = clip(shape).drawWithContent {
    drawContent()

    val radius = min(size.height / 2f, 16.dp.toPx()).coerceAtLeast(borderWidth.toPx() * 2f)
    val strokeWidth = borderWidth.toPx()
    drawRoundRect(
        color = Color.White.copy(alpha = if (isDark) 0.26f else 0.62f),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = strokeWidth)
    )

    val diameter = radius * 2f
    val spectralAlpha = if (isDark) 0.18f else 0.12f
    val spectralColors = listOf(
        Color(0xFFFF6A6A).copy(alpha = spectralAlpha),
        Color(0xFFFFD166).copy(alpha = spectralAlpha),
        Color(0xFF6EE7B7).copy(alpha = spectralAlpha),
        Color(0xFF67D9FF).copy(alpha = spectralAlpha),
        Color(0xFF9B8CFF).copy(alpha = spectralAlpha),
        Color(0xFFFF6A6A).copy(alpha = spectralAlpha)
    )
    val corners = listOf(
        Triple(Offset.Zero, 180f, Offset(radius, radius)),
        Triple(Offset(size.width - diameter, 0f), 270f, Offset(size.width - radius, radius)),
        Triple(Offset(size.width - diameter, size.height - diameter), 0f, Offset(size.width - radius, size.height - radius)),
        Triple(Offset(0f, size.height - diameter), 90f, Offset(radius, size.height - radius))
    )
    corners.forEach { (topLeft, startAngle, center) ->
        drawArc(
            brush = Brush.sweepGradient(colors = spectralColors, center = center),
            startAngle = startAngle,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth * 1.5f)
        )
    }
}

/** Crisp highlight layer. The blurred content remains underneath the refraction shader. */
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

/**
 * 現成的液態玻璃容器：模糊背景＋折射＋高光描邊，內容疊在最上層。
 *
 * 需要自訂疊層（例如導覽列還要塞拖曳藥丸）就直接拿 [LiquidGlassBackdrop] ／
 * [LiquidGlassHighlight] 兩塊自己疊；一般用途用這個就好。
 */
@Composable
fun LiquidGlassContainer(
    hazeState: HazeState,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(percent = 50),
    contentPadding: PaddingValues = PaddingValues(4.dp),
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
