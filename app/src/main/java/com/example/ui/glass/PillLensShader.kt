package com.example.ui.glass

/**
 * Optical Lens Shader (AGSL, Android 13+ / API 33).
 * Applied to content rows (navigation icons, segmented labels) to produce physical glass lens magnification:
 * - Anisotropic magnification (horizontal uZoom, vertical 0.45x) increasing smoothly from rim to center.
 * - Clean monochromatic transmission with zero chromatic dispersion or rainbow fringing.
 */
const val PILL_LENS_SHADER = """
    uniform shader content;
    uniform float2 uCenter;
    uniform float2 uHalf;
    uniform float uZoom;
    uniform float uChroma;

    float sdRoundRect(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + r;
        return length(max(q, float2(0.0))) + min(max(q.x, q.y), 0.0) - r;
    }

    half4 main(float2 fragCoord) {
        float2 p = fragCoord - uCenter;
        float d = sdRoundRect(p, uHalf, uHalf.y);
        if (d >= 0.0) {
            return content.eval(fragCoord);
        }
        float band = max(uHalf.y * 0.8, 1.0);
        float t = clamp(-d / band, 0.0, 1.0);
        float m = smoothstep(0.0, 1.0, t);
        float zx = 1.0 + uZoom * m;
        float zy = 1.0 + uZoom * 0.45 * m;
        float2 baseUv = uCenter + float2(p.x / zx, p.y / zy);
        return content.eval(baseUv);
    }
"""

