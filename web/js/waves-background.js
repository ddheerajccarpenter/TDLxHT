/**
 * ReactBits - Waves & GradientWaves Background Component (Vanilla JS Edition)
 * Based on @react-bits/Waves-JS-CSS and @react-bits/GradientWaves-TS-TW
 * Features fluid undulating satin wave lines, interactive cursor-tracking swells,
 * spring restitution, and seamless adaptation to dark/light themes.
 */

export class WavesBackground {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    if (!this.canvas) {
      this.canvas = document.createElement("canvas");
      this.canvas.id = canvasId;
      this.canvas.className = "waves-bg-canvas";
      document.body.prepend(this.canvas);
    }

    this.ctx = this.canvas.getContext("2d");
    if (!this.ctx) return;

    this.mouse = {
      x: window.innerWidth / 2,
      y: window.innerHeight / 2,
      targetX: window.innerWidth / 2,
      targetY: window.innerHeight / 2,
      radius: 180,
      active: false
    };

    this.time = 0;
    this.animationFrame = null;
    this.isReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    this.initCanvasSize();
    this.bindEvents();
    this.start();
  }

  initCanvasSize() {
    this.dpr = Math.min(window.devicePixelRatio || 1, 2);
    this.width = window.innerWidth;
    this.height = window.innerHeight;
    this.canvas.width = this.width * this.dpr;
    this.canvas.height = this.height * this.dpr;
    this.canvas.style.width = `${this.width}px`;
    this.canvas.style.height = `${this.height}px`;
    this.ctx.scale(this.dpr, this.dpr);
  }

  bindEvents() {
    window.addEventListener("resize", () => {
      this.initCanvasSize();
    });

    window.addEventListener("pointermove", (e) => {
      this.mouse.targetX = e.clientX;
      this.mouse.targetY = e.clientY;
      this.mouse.active = true;
    });

    window.addEventListener("pointerleave", () => {
      this.mouse.active = false;
    });

    // Swell impulse on click (inspired by ReactBits click-activated swells)
    window.addEventListener("click", (e) => {
      this.mouse.radius = 280;
      setTimeout(() => {
        this.mouse.radius = 180;
      }, 400);
    });
  }

  start() {
    if (this.isReducedMotion) {
      this.renderStatic();
      return;
    }

    const loop = () => {
      this.update();
      this.render();
      this.animationFrame = requestAnimationFrame(loop);
    };
    this.animationFrame = requestAnimationFrame(loop);
  }

  stop() {
    if (this.animationFrame) {
      cancelAnimationFrame(this.animationFrame);
    }
  }

  update() {
    this.time += 0.012;
    // Smooth lerp pointer
    this.mouse.x += (this.mouse.targetX - this.mouse.x) * 0.06;
    this.mouse.y += (this.mouse.targetY - this.mouse.y) * 0.06;
  }

  render() {
    const ctx = this.ctx;
    const w = this.width;
    const h = this.height;
    const isDark = document.documentElement.getAttribute("data-theme") !== "light";

    ctx.clearRect(0, 0, w, h);

    // Number of flowing wave ribbons
    const waveCount = 5;
    const baseFreq = 0.0022;

    for (let i = 0; i < waveCount; i++) {
      ctx.beginPath();
      const progress = i / (waveCount - 1);
      const yOffset = h * 0.45 + (i - 2) * 55;
      const speed = this.time * (0.8 + i * 0.2);
      const amp = 35 + i * 8;

      let firstX = 0;
      let firstY = 0;

      for (let x = 0; x <= w; x += 12) {
        // Multi-frequency wave calculation
        let y = yOffset + Math.sin(x * baseFreq + speed) * amp + Math.cos(x * baseFreq * 1.8 - speed * 0.7) * (amp * 0.45);

        // Cursor swell reaction
        if (this.mouse.active) {
          const dx = x - this.mouse.x;
          const dy = y - this.mouse.y;
          const dist = Math.sqrt(dx * dx + dy * dy);
          if (dist < this.mouse.radius) {
            const influence = (1 - dist / this.mouse.radius);
            const swell = Math.sin(influence * Math.PI) * 45;
            y += (dy > 0 ? swell : -swell);
          }
        }

        if (x === 0) {
          ctx.moveTo(x, y);
          firstX = x;
          firstY = y;
        } else {
          ctx.lineTo(x, y);
        }
      }

      // Satin gradient line style
      const lineGrad = ctx.createLinearGradient(0, 0, w, 0);
      if (isDark) {
        lineGrad.addColorStop(0, "rgba(245, 158, 11, 0.02)");
        lineGrad.addColorStop(0.3, `rgba(245, 158, 11, ${0.08 + i * 0.03})`);
        lineGrad.addColorStop(0.6, `rgba(255, 122, 0, ${0.12 + i * 0.04})`);
        lineGrad.addColorStop(0.85, `rgba(16, 185, 129, ${0.06 + i * 0.02})`);
        lineGrad.addColorStop(1, "rgba(245, 158, 11, 0.01)");
        ctx.strokeStyle = lineGrad;
        ctx.lineWidth = 1.4 + i * 0.4;
      } else {
        lineGrad.addColorStop(0, "rgba(180, 83, 9, 0.02)");
        lineGrad.addColorStop(0.3, `rgba(180, 83, 9, ${0.06 + i * 0.02})`);
        lineGrad.addColorStop(0.6, `rgba(217, 119, 6, ${0.09 + i * 0.03})`);
        lineGrad.addColorStop(1, "rgba(180, 83, 9, 0.01)");
        ctx.strokeStyle = lineGrad;
        ctx.lineWidth = 1.2 + i * 0.3;
      }

      ctx.stroke();
    }
  }

  renderStatic() {
    this.update();
    this.render();
  }
}
