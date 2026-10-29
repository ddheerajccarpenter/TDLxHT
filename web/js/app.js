/**
 * TDlxHT Wireframe Web Application Logic
 * Strict Two-Color Grayscale (#252525 & #FFFFFF)
 * Includes: Theme Toggle, Calculator Runner, Search Filter, Interactive Tasks
 */

import { initBottomNav } from './bottom-nav.js';

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

// Product Preview Interactive Tasks & Stats
function initPreviewTasks() {
  const taskItems = document.querySelectorAll('.preview-task-item');
  if (!taskItems.length) return;

  taskItems.forEach(item => {
    item.addEventListener('click', () => {
      item.classList.toggle('checked');
      const checkEl = item.querySelector('.preview-task-check');
      if (checkEl) {
        checkEl.textContent = item.classList.contains('checked') ? '✓' : '';
      }
      updatePreviewStats();
    });
  });

  function updatePreviewStats() {
    const checkedCount = document.querySelectorAll('.preview-task-item.checked').length;
    const totalCount = taskItems.length;
    const statValEl = document.querySelector('.preview-stat-box .preview-stat-val');
    const statMetaEl = document.querySelector('.preview-stat-box .preview-stat-meta');

    if (statValEl) {
      statValEl.textContent = `${checkedCount} / ${totalCount}`;
    }
    if (statMetaEl) {
      statMetaEl.textContent = `${Math.round((checkedCount / totalCount) * 100)}% completed`;
    }
  }
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

  // Calculator definitions
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
          sub: `Based on standard university conversion (${cgpa} × ${mult})`
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
    }
  };

  let activeTool = 'Loan EMI';

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
  } catch (err) {
    console.debug('Version sync note:', err);
  }
}
