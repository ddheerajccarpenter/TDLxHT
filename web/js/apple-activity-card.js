/**
 * Kokonut UI - Apple Activity Card Component (Vanilla JS Edition)
 * Inspired by @kokonutui/apple-activity-card with sharp architectural geometry,
 * interactive SVG rings, count-up animations, and real-time task sync.
 */

export class AppleActivityCard {
  constructor(containerId, options = {}) {
    this.container = document.getElementById(containerId);
    if (!this.container) return;

    this.options = Object.assign({
      stroke: 14,
      activities: [
        {
          id: "tasks",
          label: "TASKS",
          color: "#FFFFFF",
          colorEnd: "#FFFFFF",
          size: 200,
          current: 8,
          target: 10,
          unit: "DONE"
        },
        {
          id: "habits",
          label: "HABITS",
          color: "#D4D4D4",
          colorEnd: "#D4D4D4",
          size: 156,
          current: 14,
          target: 14,
          unit: "DAYS"
        },
        {
          id: "focus",
          label: "FOCUS",
          color: "#737373",
          colorEnd: "#737373",
          size: 112,
          current: 3.5,
          target: 4.0,
          unit: "HRS"
        }
      ]
    }, options);

    this.activities = this.options.activities;
    this.activeRing = null;
    this.rendered = false;

    this.render();
    this.animateDraw();
  }

  render() {
    if (!this.container) return;
    const stroke = this.options.stroke;
    const maxSize = Math.max(...this.activities.map(a => a.size));

    // Construct SVG Gradients & Rings
    let defsHtml = '';
    let ringsHtml = '';
    let legendHtml = '';

    this.activities.forEach((activity, index) => {
      const radius = (activity.size - stroke) / 2;
      const circumference = radius * 2 * Math.PI;
      const center = maxSize / 2;
      const gradId = `ring-grad-${activity.id}`;
      const shadowId = `ring-shadow-${activity.id}`;

      defsHtml += `
        <linearGradient id="${gradId}" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="${activity.color}" />
          <stop offset="100%" stop-color="${activity.colorEnd}" />
        </linearGradient>
        <filter id="${shadowId}" x="-50%" y="-50%" width="200%" height="200%">
          <feDropShadow dx="0" dy="2" stdDeviation="3" flood-color="#000" flood-opacity="0.6" />
        </filter>
      `;

      // Base track + Animated progress ring + Cap
      ringsHtml += `
        <g class="activity-ring-group" data-ring-id="${activity.id}" data-ring-index="${index}">
          <!-- Background Track -->
          <circle
            class="activity-track"
            cx="${center}"
            cy="${center}"
            r="${radius}"
            fill="none"
            stroke="var(--hairline)"
            stroke-width="${stroke}"
          />
          <!-- Progress Stroke -->
          <circle
            class="activity-progress"
            id="ring-prog-${activity.id}"
            cx="${center}"
            cy="${center}"
            r="${radius}"
            fill="none"
            stroke="url(#${gradId})"
            stroke-width="${stroke}"
            stroke-dasharray="${circumference}"
            stroke-dashoffset="${circumference}"
            stroke-linecap="square"
            style="transform-origin: center; transform: rotate(-90deg);"
          />
        </g>
      `;

      // Legend row
      legendHtml += `
        <div class="activity-legend-item" data-ring-id="${activity.id}" data-ring-index="${index}">
          <div class="legend-color-indicator" style="background: ${activity.color};"></div>
          <div class="legend-info">
            <span class="legend-label">${activity.label}</span>
            <div class="legend-values">
              <span class="legend-current" id="val-${activity.id}">0</span>
              <span class="legend-target">/${activity.target} ${activity.unit}</span>
            </div>
          </div>
        </div>
      `;
    });

    this.container.innerHTML = `
      <div class="kokonut-activity-card">
        <div class="activity-card-header">
          <div class="activity-card-title-group">
            <span class="activity-eyebrow">ACTIVITY MOMENTUM</span>
            <h4 class="activity-card-heading">Daily Consistency Rings</h4>
          </div>
          <span class="activity-status-badge" id="activity-overall-pct">89% COMPLETED</span>
        </div>

        <div class="activity-card-body">
          <div class="activity-rings-wrapper">
            <svg
              class="activity-svg"
              viewBox="0 0 ${maxSize} ${maxSize}"
              width="${maxSize}"
              height="${maxSize}"
            >
              <defs>${defsHtml}</defs>
              ${ringsHtml}
            </svg>
          </div>

          <div class="activity-legend">
            ${legendHtml}
          </div>
        </div>
      </div>
    `;

    this.bindHoverEvents();
    this.rendered = true;
  }

  bindHoverEvents() {
    const ringGroups = this.container.querySelectorAll(".activity-ring-group");
    const legendItems = this.container.querySelectorAll(".activity-legend-item");

    const highlightRing = (ringId) => {
      ringGroups.forEach(group => {
        if (ringId === null || group.getAttribute("data-ring-id") === ringId) {
          group.style.opacity = "1";
        } else {
          group.style.opacity = "0.2";
        }
      });
      legendItems.forEach(item => {
        if (ringId === null || item.getAttribute("data-ring-id") === ringId) {
          item.style.opacity = "1";
        } else {
          item.style.opacity = "0.4";
        }
      });
    };

    ringGroups.forEach(group => {
      const ringId = group.getAttribute("data-ring-id");
      group.addEventListener("mouseenter", () => highlightRing(ringId));
      group.addEventListener("mouseleave", () => highlightRing(null));
    });

    legendItems.forEach(item => {
      const ringId = item.getAttribute("data-ring-id");
      item.addEventListener("mouseenter", () => highlightRing(ringId));
      item.addEventListener("mouseleave", () => highlightRing(null));
    });
  }

  animateDraw() {
    const stroke = this.options.stroke;
    const duration = 1200; // ms
    const startTime = performance.now();

    const drawFrame = (currentTime) => {
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);
      // Ease out cubic
      const eased = 1 - Math.pow(1 - progress, 3);

      this.activities.forEach(activity => {
        const radius = (activity.size - stroke) / 2;
        const circumference = radius * 2 * Math.PI;
        const targetRatio = Math.min(activity.current / activity.target, 1.5);
        const currentRatio = targetRatio * eased;

        const circle = this.container.querySelector(`#ring-prog-${activity.id}`);
        if (circle) {
          const offset = circumference * (1 - Math.min(currentRatio, 1));
          circle.style.strokeDashoffset = offset;
        }

        const valEl = this.container.querySelector(`#val-${activity.id}`);
        if (valEl) {
          const currentVal = (activity.current * eased);
          valEl.textContent = activity.unit === "HRS" ? currentVal.toFixed(1) : Math.round(currentVal);
        }
      });

      if (progress < 1) {
        requestAnimationFrame(drawFrame);
      }
    };

    requestAnimationFrame(drawFrame);
  }

  updateActivity(id, newCurrent) {
    const activity = this.activities.find(a => a.id === id);
    if (!activity) return;

    activity.current = newCurrent;
    const stroke = this.options.stroke;
    const radius = (activity.size - stroke) / 2;
    const circumference = radius * 2 * Math.PI;
    const ratio = Math.min(activity.current / activity.target, 1.5);

    const circle = this.container.querySelector(`#ring-prog-${activity.id}`);
    if (circle) {
      circle.style.transition = "stroke-dashoffset 400ms cubic-bezier(0.16, 1, 0.3, 1)";
      circle.style.strokeDashoffset = circumference * (1 - Math.min(ratio, 1));
    }

    const valEl = this.container.querySelector(`#val-${activity.id}`);
    if (valEl) {
      valEl.textContent = activity.unit === "HRS" ? newCurrent.toFixed(1) : Math.round(newCurrent);
    }

    // Update overall badge
    const totalRatio = this.activities.reduce((acc, a) => acc + Math.min(a.current / a.target, 1), 0) / this.activities.length;
    const badge = this.container.querySelector("#activity-overall-pct");
    if (badge) {
      badge.textContent = `${Math.round(totalRatio * 100)}% COMPLETED`;
    }
  }
}
