package com.example.ui.glass

/**
 * Optical Lens Shader (AGSL, Android 13+ / API 33).
 * Applied to content rows (navigation icons, segmented labels) to produce physical glass lens magnification:
 * - Anisotropic magnification (horizontal uZoom, vertical 0.45x) increasing smoothly from rim to center.
 * - Dynamic RGB chromatic dispersion along the lens rim (physical spectral fringing).
 * - Rest state uses gentle lens magnification; drag/transition state opens full dynamic dispersion.
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
        float rimMask = t * (1.0 - t) * 4.0;
        float ca = uChroma * rimMask * 0.09;
        float2 baseUv = uCenter + float2(p.x / zx, p.y / zy);
        half4 c;
        c.r = content.eval(uCenter + float2(p.x / (zx * (1.0 + ca)), p.y / zy)).r;
        half4 g = content.eval(baseUv);
        c.g = g.g;
        c.b = content.eval(uCenter + float2(p.x / (zx * (1.0 - ca)), p.y / zy)).b;
        c.a = g.a;
        return c;
    }
"""
