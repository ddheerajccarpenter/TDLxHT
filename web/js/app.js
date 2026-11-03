/**
 * TDlxHT Wireframe Web Application Logic
 * Strict Two-Color Grayscale (#252525 & #FFFFFF)
 * Includes: Theme Toggle, Calculator Runner, Search Filter, Interactive Tasks
 */

import { initBottomNav } from './bottom-nav.js';
import { init3DScene } from './scene3d.js';

document.addEventListener("DOMContentLoaded", () => {
  // Hash check for Supabase Auth redirect
  if (window.location.hash && (window.location.hash.includes('access_token=') || window.location.hash.includes('type=recovery') || window.location.hash.includes('error='))) {
    window.location.replace('/auth/callback' + window.location.hash);
    return;
  }

  initThemeToggle();
  initBottomNav();
  initPreviewTasks();
  initCalculatorLibrary();
  initVersionSync();
  initScrollMotionBlur();
  init3DScene();
});

// Theme Toggle (Supports both GlassSwitch inputs and legacy buttons)
function initThemeToggle() {
  const switchInputs = document.querySelectorAll('.theme-switch-input');
  const legacyToggleBtn = document.getElementById('theme-toggle-btn');

  const currentTheme = localStorage.getItem('tdlxht-theme') || 'dark';
  document.documentElement.setAttribute('data-theme', currentTheme);

  // Sync state to all switches
  switchInputs.forEach(input => {
    input.checked = (currentTheme === 'dark');
    input.addEventListener('change', () => {
      const nextTheme = input.checked ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', nextTheme);
      localStorage.setItem('tdlxht-theme', nextTheme);
      // Synchronize all switch inputs on page
      switchInputs.forEach(other => {
        if (other !== input) other.checked = input.checked;
      });
    });
  });

  if (legacyToggleBtn) {
    legacyToggleBtn.addEventListener('click', () => {
      const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
      const nextTheme = isDark ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', nextTheme);
      localStorage.setItem('tdlxht-theme', nextTheme);
      switchInputs.forEach(input => {
        input.checked = (nextTheme === 'dark');
      });
    });
  }
}

// Product Preview Interactive Tasks, Progress Ring & 3D Tilt
function initPreviewTasks() {
  const previewCard = document.getElementById('product-preview-card');
  const taskItems = document.querySelectorAll('.preview-task-item');
  const progressRing = document.getElementById('preview-progress-ring');
  const progressPercent = document.getElementById('preview-progress-percent');
  const progressTitle = document.getElementById('preview-progress-title');
  const clockEl = document.getElementById('preview-clock');
  const dateLabelEl = document.getElementById('preview-date-label');
  const timerBtn = document.getElementById('preview-timer-btn');
  const timerDisplay = document.getElementById('preview-timer-display');
  const timerState = document.getElementById('preview-timer-state');

  // Real-time local date & time in phone status bar
  if (clockEl) {
    const now = new Date();
    clockEl.textContent = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
  }
  if (dateLabelEl) {
    const now = new Date();
    dateLabelEl.textContent = now.toLocaleDateString([], { weekday: 'long', month: 'short', day: 'numeric' });
  }

  // 3D Perspective Cursor Tilt
  if (previewCard && window.matchMedia('(hover: hover) and (pointer: fine)').matches) {
    previewCard.addEventListener('mousemove', (e) => {
      const rect = previewCard.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      const centerX = rect.width / 2;
      const centerY = rect.height / 2;
      const rotateX = ((y - centerY) / centerY) * -6; // Max 6 deg tilt
      const rotateY = ((x - centerX) / centerX) * 6;

      previewCard.style.transform = `perspective(1200px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg) scale3d(1.012, 1.012, 1.012)`;
    });

    previewCard.addEventListener('mouseleave', () => {
      previewCard.style.transform = 'perspective(1200px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
    });
  }

  // Interactive Task Toggles
  function updatePreviewStats() {
    const checkedCount = document.querySelectorAll('.preview-task-item.checked').length;
    const totalCount = taskItems.length || 4;
    const percent = Math.round((checkedCount / totalCount) * 100);
    const circumference = 163.36; // 2 * PI * 26

    if (progressRing) {
      const offset = circumference - (circumference * percent) / 100;
      progressRing.style.strokeDashoffset = offset.toFixed(2);
    }
    if (progressPercent) {
      progressPercent.textContent = `${percent}%`;
    }
    if (progressTitle) {
      progressTitle.textContent = `${checkedCount} of ${totalCount} tasks completed`;
    }
  }

  function toggleTask(item) {
    const isNowChecked = !item.classList.contains('checked');
    item.classList.toggle('checked', isNowChecked);
    item.setAttribute('aria-checked', isNowChecked ? 'true' : 'false');

    const checkEl = item.querySelector('.preview-task-check');
    if (checkEl) checkEl.textContent = isNowChecked ? '✓' : '';

    updatePreviewStats();
  }

  taskItems.forEach(item => {
    item.addEventListener('click', () => toggleTask(item));
    item.addEventListener('keydown', (e) => {
      if (e.key === ' ' || e.key === 'Enter') {
        e.preventDefault();
        toggleTask(item);
      }
    });
  });

  // Filter preview tasks when GlassMenu changes
  document.addEventListener('sortChanged', (e) => {
    if (e.detail && e.detail.menuId === 'hero-sort-menu') {
      const filter = e.detail.sort;
      taskItems.forEach(item => {
        const isChecked = item.classList.contains('checked');
        if (filter === 'Active') {
          item.style.display = isChecked ? 'none' : 'flex';
        } else if (filter === 'Completed') {
          item.style.display = isChecked ? 'flex' : 'none';
        } else {
          item.style.display = 'flex';
        }
      });
    }
  });

  // Focus Timer Runner in Preview Tile
  let timerInterval = null;
  let timerSeconds = 25 * 60;
  let isTimerRunning = false;

  if (timerBtn && timerDisplay && timerState) {
    timerBtn.addEventListener('click', () => {
      isTimerRunning = !isTimerRunning;
      if (isTimerRunning) {
        timerState.textContent = '⏸ Pause';
        timerBtn.style.background = 'rgba(255, 69, 58, 0.2)';
        timerBtn.style.borderColor = 'rgba(255, 69, 58, 0.4)';
        timerInterval = setInterval(() => {
          if (timerSeconds > 0) {
            timerSeconds--;
            const mins = Math.floor(timerSeconds / 60).toString().padStart(2, '0');
            const secs = (timerSeconds % 60).toString().padStart(2, '0');
            timerDisplay.textContent = `${mins}:${secs}`;
          } else {
            clearInterval(timerInterval);
            isTimerRunning = false;
            timerState.textContent = '↺ Reset';
            timerSeconds = 25 * 60;
          }
        }, 1000);
      } else {
        clearInterval(timerInterval);
        timerState.textContent = '▶ Resume';
        timerBtn.style.background = '';
        timerBtn.style.borderColor = '';
      }
    });
  }

  // Initial sync immediately on load
  updatePreviewStats();
}

// Calculator Library: Search Filter + Live Calculator Engine
function initCalculatorLibrary() {
  const searchInput = document.getElementById('calc-search-input');
  const catCards = document.querySelectorAll('.calc-category-card');
  const runnerTitle = document.getElementById('calc-runner-title');
  const runnerTag = document.getElementById('calc-category-tag');
  const formContainer = document.getElementById('calc-form-container');
  const resultLabel = document.getElementById('calc-result-label');
  const resultVal = document.getElementById('calc-result-value');
  const resultSub = document.getElementById('calc-result-sub');

  if (!catCards.length || !formContainer) return;

  // Search filter
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      const q = e.target.value.toLowerCase().trim();
      catCards.forEach(card => {
        const text = card.textContent.toLowerCase();
        const matches = text.includes(q);
        card.style.display = matches ? 'flex' : 'none';
      });
    });
  }

  // Calculator definitions (All 14 native tools matching Android ToolRegistry)
  const calculators = {
    'Loan EMI': {
      category: 'FINANCE',
      label: 'Monthly EMI',
      fields: [
        { id: 'f1', label: 'Principal Amount ($)', val: 100000, min: 1000, step: 1000 },
        { id: 'f2', label: 'Annual Interest Rate (%)', val: 8.5, min: 0.1, step: 0.1 },
        { id: 'f3', label: 'Tenure (Years)', val: 5, min: 1, max: 30, step: 1 }
      ],
      calc: (v) => {
        const P = parseFloat(v.f1) || 0;
        const r = (parseFloat(v.f2) || 0) / 1200;
        const n = (parseFloat(v.f3) || 1) * 12;
        if (P <= 0 || r <= 0 || n <= 0) return { main: '$0.00', sub: 'Total Interest: $0.00' };
        const emi = (P * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        const total = emi * n;
        return {
          main: `$${emi.toFixed(2)}`,
          sub: `Total Interest: $${(total - P).toFixed(2)}`
        };
      }
    },
    'GST Tax': {
      category: 'BUDGETING',
      label: 'Total with GST',
      fields: [
        { id: 'f1', label: 'Net Amount ($)', val: 500, min: 1, step: 10 },
        { id: 'f2', label: 'GST Rate (%)', val: 18, min: 0, step: 1 }
      ],
      calc: (v) => {
        const net = parseFloat(v.f1) || 0;
        const rate = parseFloat(v.f2) || 0;
        const tax = (net * rate) / 100;
        return {
          main: `$${(net + tax).toFixed(2)}`,
          sub: `GST Tax Amount: $${tax.toFixed(2)}`
        };
      }
    },
    'CGPA to %': {
      category: 'ACADEMIC',
      label: 'Academic Percentage',
      fields: [
        { id: 'f1', label: 'CGPA (10-Point Scale)', val: 8.4, min: 0, max: 10, step: 0.1 },
        { id: 'f2', label: 'Formula Multiplier', val: 9.5, min: 1, max: 10, step: 0.1 }
      ],
      calc: (v) => {
        const cgpa = parseFloat(v.f1) || 0;
        const mult = parseFloat(v.f2) || 9.5;
        const pct = cgpa * mult;
        return {
          main: `${pct.toFixed(1)}%`,
          sub: `Standard university conversion (${cgpa} × ${mult})`
        };
      }
    },
    'Tip Splitter': {
      category: 'EVERYDAY',
      label: 'Per Person Share',
      fields: [
        { id: 'f1', label: 'Bill Amount ($)', val: 120, min: 1, step: 5 },
        { id: 'f2', label: 'Tip Percentage (%)', val: 15, min: 0, step: 1 },
        { id: 'f3', label: 'Number of People', val: 4, min: 1, max: 20, step: 1 }
      ],
      calc: (v) => {
        const bill = parseFloat(v.f1) || 0;
        const tipPct = parseFloat(v.f2) || 0;
        const people = parseInt(v.f3) || 1;
        const total = bill * (1 + tipPct / 100);
        return {
          main: `$${(total / people).toFixed(2)}`,
          sub: `Total Bill: $${total.toFixed(2)} (Tip: $${(bill * tipPct / 100).toFixed(2)})`
        };
      }
    },
    'Unit Conversion': {
      category: 'CONVERSION',
      label: 'Converted Value',
      fields: [
        { id: 'f1', label: 'Kilometers (km)', val: 10, min: 0, step: 0.5 }
      ],
      calc: (v) => {
        const km = parseFloat(v.f1) || 0;
        const miles = km * 0.621371;
        return {
          main: `${miles.toFixed(2)} Miles`,
          sub: `${km} km = ${(km * 1000).toLocaleString()} meters`
        };
      }
    },
    'Age Calculator': {
      category: 'EVERYDAY',
      label: 'Current Age',
      fields: [
        { id: 'f1', label: 'Birth Year', val: 2000, min: 1900, max: 2026, step: 1 }
      ],
      calc: (v) => {
        const year = parseInt(v.f1) || 2000;
        const currentYear = new Date().getFullYear();
        const age = currentYear - year;
        return {
          main: `${age} Years`,
          sub: `Approx. ${(age * 365.25).toLocaleString()} days lived`
        };
      }
    },
    'Attendance Planner': {
      category: 'ACADEMIC',
      label: 'Current Attendance',
      fields: [
        { id: 'f1', label: 'Classes Attended', val: 32, min: 0, step: 1 },
        { id: 'f2', label: 'Total Classes Held', val: 40, min: 1, step: 1 },
        { id: 'f3', label: 'Target Percentage (%)', val: 75, min: 1, max: 100, step: 1 }
      ],
      calc: (v) => {
        const attended = parseInt(v.f1) || 0;
        const total = parseInt(v.f2) || 1;
        const target = parseFloat(v.f3) || 75;
        const curPct = (attended / total) * 100;
        if (curPct >= target) {
          const safeBunks = Math.floor((attended - (target / 100) * total) / (target / 100));
          return {
            main: `${curPct.toFixed(1)}%`,
            sub: safeBunks > 0 ? `Target met! You can safely miss ${safeBunks} class${safeBunks > 1 ? 'es' : ''}.` : `Target met! Attend next class to maintain target.`
          };
        } else {
          const needed = Math.ceil(((target / 100) * total - attended) / (1 - (target / 100)));
          return {
            main: `${curPct.toFixed(1)}%`,
            sub: `Shortage! Attend next ${Math.max(1, needed)} consecutive class${needed > 1 ? 'es' : ''} to reach ${target}%.`
          };
        }
      }
    },
    'Margin & Markup': {
      category: 'FINANCE',
      label: 'Profit Margin',
      fields: [
        { id: 'f1', label: 'Cost Price ($)', val: 80, min: 0.01, step: 5 },
        { id: 'f2', label: 'Selling Price ($)', val: 120, min: 0.01, step: 5 }
      ],
      calc: (v) => {
        const cost = parseFloat(v.f1) || 0;
        const sell = parseFloat(v.f2) || 0;
        const profit = sell - cost;
        const margin = sell > 0 ? (profit / sell) * 100 : 0;
        const markup = cost > 0 ? (profit / cost) * 100 : 0;
        return {
          main: `${margin.toFixed(1)}% Margin`,
          sub: `Markup: ${markup.toFixed(1)}% · Net Profit: $${profit.toFixed(2)}`
        };
      }
    },
    'Discount & Sale': {
      category: 'BUDGETING',
      label: 'Final Checkout Price',
      fields: [
        { id: 'f1', label: 'Original Price ($)', val: 150, min: 0, step: 5 },
        { id: 'f2', label: 'Discount (%)', val: 25, min: 0, max: 100, step: 1 }
      ],
      calc: (v) => {
        const orig = parseFloat(v.f1) || 0;
        const disc = parseFloat(v.f2) || 0;
        const savings = (orig * disc) / 100;
        const finalPrice = Math.max(0, orig - savings);
        return {
          main: `$${finalPrice.toFixed(2)}`,
          sub: `You save $${savings.toFixed(2)} (${disc}% off)`
        };
      }
    },
    'Profit & Loss': {
      category: 'FINANCE',
      label: 'Net Result',
      fields: [
        { id: 'f1', label: 'Cost Price ($)', val: 200, min: 0, step: 10 },
        { id: 'f2', label: 'Total Revenue ($)', val: 280, min: 0, step: 10 }
      ],
      calc: (v) => {
        const cost = parseFloat(v.f1) || 0;
        const rev = parseFloat(v.f2) || 0;
        const net = rev - cost;
        const roi = cost > 0 ? (net / cost) * 100 : 0;
        return {
          main: `${net >= 0 ? '+' : '-'}$${Math.abs(net).toFixed(2)}`,
          sub: `${net >= 0 ? 'Profit' : 'Loss'} · ROI: ${roi.toFixed(1)}%`
        };
      }
    },
    'Break-Even Estimator': {
      category: 'BUSINESS',
      label: 'Break-Even Units',
      fields: [
        { id: 'f1', label: 'Fixed Costs ($)', val: 5000, min: 0, step: 100 },
        { id: 'f2', label: 'Price per Unit ($)', val: 50, min: 1, step: 1 },
        { id: 'f3', label: 'Variable Cost per Unit ($)', val: 25, min: 0, step: 1 }
      ],
      calc: (v) => {
        const fixed = parseFloat(v.f1) || 0;
        const price = parseFloat(v.f2) || 1;
        const variable = parseFloat(v.f3) || 0;
        const contribution = price - variable;
        if (contribution <= 0) return { main: 'N/A', sub: 'Price must exceed variable cost per unit' };
        const units = Math.ceil(fixed / contribution);
        return {
          main: `${units.toLocaleString()} Units`,
          sub: `Revenue to break even: $${(units * price).toLocaleString()}`
        };
      }
    },
    'Percentage Calculator': {
      category: 'UTILITIES',
      label: 'Percentage Change',
      fields: [
        { id: 'f1', label: 'Initial Value', val: 50, min: 0, step: 1 },
        { id: 'f2', label: 'New Value', val: 75, min: 0, step: 1 }
      ],
      calc: (v) => {
        const v1 = parseFloat(v.f1) || 0;
        const v2 = parseFloat(v.f2) || 0;
        const diff = v2 - v1;
        const pctChange = v1 !== 0 ? (diff / v1) * 100 : 0;
        return {
          main: `${pctChange >= 0 ? '+' : ''}${pctChange.toFixed(1)}%`,
          sub: `Difference: ${diff >= 0 ? '+' : ''}${diff.toFixed(2)} (${v2} vs ${v1})`
        };
      }
    },
    'Pomodoro Planner': {
      category: 'PRODUCTIVITY',
      label: 'Work Focus Blocks',
      fields: [
        { id: 'f1', label: 'Total Study Time (Hours)', val: 4, min: 0.5, max: 16, step: 0.5 },
        { id: 'f2', label: 'Focus Block (Minutes)', val: 25, min: 15, max: 90, step: 5 },
        { id: 'f3', label: 'Break Length (Minutes)', val: 5, min: 3, max: 30, step: 1 }
      ],
      calc: (v) => {
        const totalMins = (parseFloat(v.f1) || 0) * 60;
        const focus = parseInt(v.f2) || 25;
        const rest = parseInt(v.f3) || 5;
        const cycle = focus + rest;
        const blocks = cycle > 0 ? Math.floor(totalMins / cycle) : 0;
        const totalFocusMins = blocks * focus;
        return {
          main: `${blocks} Sessions`,
          sub: `${totalFocusMins} mins pure focus · ${blocks * rest} mins rest`
        };
      }
    },
    'Compound Interest': {
      category: 'FINANCE',
      label: 'Future Value',
      fields: [
        { id: 'f1', label: 'Principal ($)', val: 10000, min: 100, step: 500 },
        { id: 'f2', label: 'Annual Rate (%)', val: 7, min: 0.1, step: 0.1 },
        { id: 'f3', label: 'Time Period (Years)', val: 10, min: 1, max: 50, step: 1 },
        { id: 'f4', label: 'Compounding (Times/Yr)', val: 12, min: 1, max: 365, step: 1 }
      ],
      calc: (v) => {
        const P = parseFloat(v.f1) || 0;
        const r = (parseFloat(v.f2) || 0) / 100;
        const t = parseFloat(v.f3) || 1;
        const n = parseInt(v.f4) || 12;
        const A = P * Math.pow(1 + (r / n), n * t);
        const interest = A - P;
        return {
          main: `$${A.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
          sub: `Total Interest: $${interest.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
        };
      }
    }
  };

  let activeTool = 'Loan EMI';

  const resetBtn = document.getElementById('calc-reset-btn');
  const viewAllBtn = document.getElementById('view-all-calc-btn');
  const catalogModal = document.getElementById('calc-catalog-modal');
  const catalogGrid = document.getElementById('calc-catalog-grid');

  function renderCalculator(toolName) {
    activeTool = toolName;
    const def = calculators[toolName] || calculators['Loan EMI'];

    runnerTitle.textContent = `${toolName} Calculator`;
    runnerTag.textContent = def.category;
    resultLabel.textContent = def.label;

    formContainer.innerHTML = def.fields.map(f => `
      <div class="calc-input-group">
        <label class="calc-input-label" for="${f.id}">${f.label}</label>
        <input type="number" id="${f.id}" class="calc-field" value="${f.val}" min="${f.min || ''}" max="${f.max || ''}" step="${f.step || 'any'}">
      </div>
    `).join('');

    // Attach listeners
    def.fields.forEach(f => {
      const input = document.getElementById(f.id);
      if (input) {
        input.addEventListener('input', compute);
      }
    });

    if (catalogGrid) {
      catalogGrid.querySelectorAll('.calc-tool-chip').forEach(chip => {
        chip.classList.toggle('active', chip.getAttribute('data-tool') === toolName);
      });
    }

    compute();
  }

  function compute() {
    const def = calculators[activeTool] || calculators['Loan EMI'];
    const values = {};
    def.fields.forEach(f => {
      const input = document.getElementById(f.id);
      values[f.id] = input ? input.value : f.val;
    });

    const res = def.calc(values);
    resultVal.textContent = res.main;
    resultSub.textContent = res.sub;
  }

  // Populate 14-tool catalog grid
  if (catalogGrid) {
    catalogGrid.innerHTML = Object.keys(calculators).map(name => `
      <button class="calc-tool-chip ${name === activeTool ? 'active' : ''}" data-tool="${name}" type="button">
        <span>${name}</span>
      </button>
    `).join('');

    catalogGrid.querySelectorAll('.calc-tool-chip').forEach(chip => {
      chip.addEventListener('click', () => {
        const tool = chip.getAttribute('data-tool');
        renderCalculator(tool);
        catCards.forEach(c => {
          c.classList.toggle('active', c.getAttribute('data-tool') === tool);
        });
        const runnerEl = document.getElementById('calc-runner');
        if (runnerEl) {
          runnerEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
      });
    });
  }

  // View All / Catalog modal toggle
  if (viewAllBtn && catalogModal) {
    viewAllBtn.addEventListener('click', () => {
      const isOpen = catalogModal.classList.toggle('open');
      viewAllBtn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
      viewAllBtn.textContent = isOpen ? 'Hide all 14 calculators ↑' : 'View all 14 calculators ↓';
    });
  }

  // Reset inputs button
  if (resetBtn) {
    resetBtn.addEventListener('click', () => {
      const def = calculators[activeTool] || calculators['Loan EMI'];
      def.fields.forEach(f => {
        const input = document.getElementById(f.id);
        if (input) input.value = f.val;
      });
      compute();
    });
  }

  // Click handler on category cards
  catCards.forEach(card => {
    card.addEventListener('click', () => {
      catCards.forEach(c => c.classList.remove('active'));
      card.classList.add('active');
      const tool = card.getAttribute('data-tool') || 'Loan EMI';
      renderCalculator(tool);

      // Smooth scroll to runner
      const runnerEl = document.getElementById('calc-runner');
      if (runnerEl) {
        runnerEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    });
  });

  // Initial render
  renderCalculator('Loan EMI');
}

// Sync latest version details from /version.json
async function initVersionSync() {
  try {
    const res = await fetch('/version.json');
    if (!res.ok) return;
    const data = await res.json();
    if (!data || !data.version) return;

    // Update labels and download links
    document.querySelectorAll('.apk-version-label').forEach(el => {
      el.textContent = `v${data.version}`;
    });

    if (data.apkUrl) {
      document.querySelectorAll('[data-apk-download="true"]').forEach(el => {
        el.setAttribute('href', data.apkUrl);
      });
    }

    // SHA-256 Checksum badge sync and copy
    const shaText = document.getElementById('sha-hash-text');
    if (shaText && data.sha256) {
      shaText.textContent = data.sha256;
      shaText.title = data.sha256;
    }

    const copyBtn = document.getElementById('copy-sha-btn');
    if (copyBtn && data.sha256) {
      copyBtn.addEventListener('click', async () => {
        try {
          await navigator.clipboard.writeText(data.sha256);
          const origText = copyBtn.textContent;
          copyBtn.textContent = 'Copied!';
          copyBtn.style.color = 'var(--color-primary, #10b981)';
          setTimeout(() => {
            copyBtn.textContent = origText;
            copyBtn.style.color = '';
          }, 2000);
        } catch (err) {
          console.debug('Clipboard write failed:', err);
        }
      });
    }
  } catch (err) {
    console.debug('Version sync note:', err);
  }
}


// 60 FPS Optical Motion Blur on High-Velocity Scrolling
function initScrollMotionBlur() {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

  const mainContainer = document.querySelector('main') || document.body;
  mainContainer.classList.add('scroll-blur-container');

  let lastScrollY = window.pageYOffset;
  let lastTimestamp = performance.now();
  let blurTimeout = null;
  let ticking = false;

  window.addEventListener('scroll', () => {
    if (!ticking) {
      window.requestAnimationFrame(() => {
        const now = performance.now();
        const currentScrollY = window.pageYOffset;
        const dt = Math.max(now - lastTimestamp, 8);
        const dy = Math.abs(currentScrollY - lastScrollY);
        const velocity = dy / dt; // px per ms

        // 5-10% optical motion blur capped at 2.2px during rapid scroll
        const targetBlur = Math.min(Math.max((velocity - 0.4) * 1.2, 0), 2.2);
        document.documentElement.style.setProperty('--scroll-blur', targetBlur.toFixed(2) + 'px');

        lastScrollY = currentScrollY;
        lastTimestamp = now;
        ticking = false;

        clearTimeout(blurTimeout);
        blurTimeout = setTimeout(() => {
          document.documentElement.style.setProperty('--scroll-blur', '0px');
        }, 80);
      });
      ticking = true;
    }
  }, { passive: true });
}
