/**
 * CursorEdgeGlowButton Controller for TDlxHT Web Companion
 * Spring-damped cursor and touch edge glow with authentic Ink Wash palette.
 * Frequency: 3.4, Damping: 0.78
 */

const FREQUENCY = 3.4;
const DAMPING = 0.78;
const GLOW_RISE = 0.5;
const EDGE_SATURATION = 0.55;
const EDGE_BRIGHTNESS = 0.12;
const HUE_SHIFT = 0; // Natural Ink Wash Slate Steel caustics

export function initCursorEdgeGlowButtons() {
  const glowContainers = document.querySelectorAll('[data-cursor-glow="true"], .btn-edge-glow');

  glowContainers.forEach((container) => {
    const lightTrack = container.querySelector('.glow-light-track');
    const rightGlow = container.querySelector('.glow-right');
    const leftGlow = container.querySelector('.glow-left');
    const button = container.querySelector('button, a');

    if (!lightTrack || !rightGlow || !leftGlow) return;

    let bound = container.getBoundingClientRect().width / 2 + 12;
    let x = bound;
    let velocity = 0;
    let target = bound;
    let inside = false;
    let last = 0;
    let animationFrame = null;

    const measure = () => {
      bound = container.getBoundingClientRect().width / 2 + 12;
    };

    const paint = () => {
      lightTrack.style.setProperty('--light-x', x.toFixed(2) + 'px');
      const normalized = Math.max(-1, Math.min(1, x / bound));
      const magnitude = Math.abs(normalized);
      const intensity = Math.pow(magnitude, GLOW_RISE);
      const colorTuning =
        'hue-rotate(' + HUE_SHIFT + 'deg)' +
        ' saturate(' + (1 + EDGE_SATURATION * magnitude).toFixed(3) + ')' +
        ' brightness(' + (1 + EDGE_BRIGHTNESS * magnitude).toFixed(3) + ')';

      rightGlow.style.opacity = (normalized > 0 ? intensity : 0).toFixed(3);
      leftGlow.style.opacity = (normalized < 0 ? intensity : 0).toFixed(3);
      rightGlow.style.filter = colorTuning;
      leftGlow.style.filter = colorTuning;
    };

    measure();
    paint();

    const resizeObserver = new ResizeObserver(measure);
    resizeObserver.observe(container);

    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return;
    }

    const frame = (now = performance.now()) => {
      const delta = Math.min((now - last) / 1000, 0.032);
      last = now;
      const angularFrequency = 2 * Math.PI * FREQUENCY;
      velocity +=
        (angularFrequency * angularFrequency * (target - x) -
          2 * DAMPING * angularFrequency * velocity) *
        delta;
      x += velocity * delta;
      paint();

      if (inside || Math.abs(target - x) > 0.15 || Math.abs(velocity) > 0.6) {
        animationFrame = requestAnimationFrame(frame);
      } else {
        animationFrame = null;
        x = target;
        velocity = 0;
        paint();
      }
    };

    const kick = () => {
      if (animationFrame === null) {
        last = performance.now();
        animationFrame = requestAnimationFrame(frame);
      }
    };

    // Pointer events (handles both mouse and touch devices)
    container.addEventListener('pointermove', (event) => {
      const bounds = container.getBoundingClientRect();
      inside = true;
      target = Math.max(-bound, Math.min(bound, event.clientX - (bounds.left + bounds.width / 2)));
      kick();
    });

    container.addEventListener('pointerdown', (event) => {
      const bounds = container.getBoundingClientRect();
      inside = true;
      target = Math.max(-bound, Math.min(bound, event.clientX - (bounds.left + bounds.width / 2)));
      kick();
    });

    container.addEventListener('pointerleave', () => {
      inside = false;
      target = x;
      kick();
    });

    container.addEventListener('pointerup', () => {
      inside = false;
      target = 0;
      kick();
    });

    container.addEventListener('pointercancel', () => {
      inside = false;
      target = 0;
      kick();
    });

    if (button) {
      button.addEventListener('focus', () => {
        inside = false;
        target = 0;
        kick();
      });
      button.addEventListener('blur', () => {
        inside = false;
        target = x;
        kick();
      });
    }
  });
}

// Auto-initialize when document is ready
if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initCursorEdgeGlowButtons);
  } else {
    initCursorEdgeGlowButtons();
  }
}

