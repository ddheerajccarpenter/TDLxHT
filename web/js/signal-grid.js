/**
 * getdesign.md - Signal Grid Animated Background
 * Based on https://getdesign.md/backgrounds?fx=signal-grid
 * "A fine grid of squares that quietly twinkle, dense at one edge and fading to empty."
 */

function hash(a, i) {
  const n = Math.sin(a * 127.1 + i * 311.7) * 43758.5453;
  return n - Math.floor(n);
}

export class SignalGridBackground {
  constructor(canvasId, options = {}) {
    this.canvas = document.getElementById(canvasId);
    if (!this.canvas) {
      this.canvas = document.createElement("canvas");
      this.canvas.id = canvasId;
      this.canvas.className = "signal-grid-canvas";
      document.body.prepend(this.canvas);
    }

    this.ctx = this.canvas.getContext("2d");
    if (!this.ctx) return;

    this.options = Object.assign({
      color: "#F59E0B", // Amber accent
      background: "#090C10", // Obsidian
      speed: 0.8,
      anchor: "left", // 'left' | 'right'
      cellSize: 20, // grid step
      squareSize: 9 // size of twinkling square
    }, options);

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
  }

  start() {
    if (this.isReducedMotion) {
      this.draw(1);
      return;
    }

    const startTime = performance.now();
    const loop = (now) => {
      const elapsed = (now - startTime) / 1000;
      this.draw(elapsed * this.options.speed);
      this.animationFrame = requestAnimationFrame(loop);
    };
    this.animationFrame = requestAnimationFrame(loop);
  }

  stop() {
    if (this.animationFrame) {
      cancelAnimationFrame(this.animationFrame);
    }
  }

  draw(time) {
    const ctx = this.ctx;
    const w = this.width;
    const h = this.height;
    const isDark = document.documentElement.getAttribute("data-theme") !== "light";

    const squareColor = isDark ? this.options.color : "#B45309";
    const bgColor = isDark ? this.options.background : "#F6F5F0";

    ctx.fillStyle = bgColor;
    ctx.fillRect(0, 0, w, h);

    ctx.fillStyle = squareColor;

    const u = this.options.cellSize;
    const d = this.options.squareSize;
    const cols = Math.ceil(w / u);
    const rows = Math.ceil(h / u);
    const anchor = this.options.anchor;

    for (let s = 0; s < rows; s++) {
      for (let v = 0; v < cols; v++) {
        const wVal = hash(v, s);
        const p = v * u + u / 2;
        const S = anchor === "left" ? (1 - p / w) : (p / w);

        // Density fade: denser at anchor edge, sparse across the screen
        if (wVal > S * 0.92) continue;

        // Subtle quiet twinkling sine wave
        const A = 1.6 + hash(s, v) * 3;
        const b = 0.5 + 0.5 * Math.sin((time * 6.283) / A + wVal * 6.283);
        const F = Math.pow(b, 3);

        ctx.globalAlpha = 0.05 + 0.85 * F * (0.25 + 0.75 * S);
        ctx.fillRect(p - d / 2, s * u + (u - d) / 2, d, d);
      }
    }

    ctx.globalAlpha = 1;
  }
}
