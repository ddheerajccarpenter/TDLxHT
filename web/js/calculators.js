/**
 * TDLxHT 14 Native Offline Calculators
 * Pure Client-side Reactive Computation Engine
 */

export const CALCULATORS = {
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
      const r = (parseFloat(v.f2) || 0) / 12 / 100;
      const n = (parseFloat(v.f3) || 1) * 12;
      if (P <= 0 || r <= 0 || n <= 0) return { main: '$0.00', sub: 'Enter valid principal, rate, and tenure' };
      const emi = (P * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
      const total = emi * n;
      return {
        main: `$${emi.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
        sub: `Total Interest: $${(total - P).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })} · Total: $${total.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
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
        sub: `GST Tax: $${tax.toFixed(2)} · Net: $${net.toFixed(2)}`
      };
    }
  },
  'CGPA to %': {
    category: 'ACADEMIC',
    label: 'Academic Percentage',
    fields: [
      { id: 'f1', label: 'CGPA (10-Point Scale)', val: 8.4, min: 0, max: 10, step: 0.1 },
      { id: 'f2', label: 'Multiplier', val: 9.5, min: 1, max: 10, step: 0.1 }
    ],
    calc: (v) => {
      const cgpa = parseFloat(v.f1) || 0;
      const mult = parseFloat(v.f2) || 9.5;
      const pct = cgpa * mult;
      return {
        main: `${pct.toFixed(1)}%`,
        sub: `Standard conversion (${cgpa} × ${mult})`
      };
    }
  },
  'Tip Splitter': {
    category: 'EVERYDAY',
    label: 'Per Person Share',
    fields: [
      { id: 'f1', label: 'Bill Amount ($)', val: 120, min: 1, step: 5 },
      { id: 'f2', label: 'Tip (%)', val: 15, min: 0, step: 1 },
      { id: 'f3', label: 'Number of People', val: 4, min: 1, max: 20, step: 1 }
    ],
    calc: (v) => {
      const bill = parseFloat(v.f1) || 0;
      const tipPct = parseFloat(v.f2) || 0;
      const people = parseInt(v.f3) || 1;
      const tip = (bill * tipPct) / 100;
      const total = bill + tip;
      return {
        main: `$${(total / Math.max(1, people)).toFixed(2)}`,
        sub: `Total: $${total.toFixed(2)} (Tip: $${tip.toFixed(2)})`
      };
    }
  },
  'Unit Conversion': {
    category: 'CONVERSION',
    label: 'Converted Distance',
    fields: [
      { id: 'f1', label: 'Kilometers (km)', val: 10, min: 0, step: 0.5 }
    ],
    calc: (v) => {
      const km = parseFloat(v.f1) || 0;
      const miles = km * 0.621371;
      return {
        main: `${miles.toFixed(2)} Miles`,
        sub: `${km} km = ${(km * 1000).toLocaleString()} meters · ${(km * 3280.84).toFixed(0)} ft`
      };
    }
  },
  'Age Calculator': {
    category: 'EVERYDAY',
    label: 'Current Age',
    fields: [
      { id: 'f1', label: 'Birth Year', val: 2000, min: 1920, max: 2026, step: 1 }
    ],
    calc: (v) => {
      const year = parseInt(v.f1) || 2000;
      const currentYear = new Date().getFullYear();
      const age = Math.max(0, currentYear - year);
      return {
        main: `${age} Years Old`,
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
      const curPct = (attended / Math.max(1, total)) * 100;
      if (curPct >= target) {
        const safeBunks = Math.floor((attended - (target / 100) * total) / (target / 100));
        return {
          main: `${curPct.toFixed(1)}%`,
          sub: safeBunks > 0 ? `Target met! You can safely miss ${safeBunks} class${safeBunks > 1 ? 'es' : ''}.` : `Target met! Attend the next class to maintain ${target}%.`
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
    label: 'Final Price',
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
    label: 'Focus Schedule',
    fields: [
      { id: 'f1', label: 'Total Available Hours', val: 4, min: 0.5, max: 16, step: 0.5 },
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
        main: `${blocks} Focus Sessions`,
        sub: `${totalFocusMins} mins focus · ${blocks * rest} mins rest`
      };
    }
  },
  'Compound Interest': {
    category: 'FINANCE',
    label: 'Future Value',
    fields: [
      { id: 'f1', label: 'Principal ($)', val: 10000, min: 100, step: 500 },
      { id: 'f2', label: 'Annual Rate (%)', val: 7, min: 0.1, step: 0.1 },
      { id: 'f3', label: 'Years', val: 10, min: 1, max: 50, step: 1 },
      { id: 'f4', label: 'Compounds / Year', val: 12, min: 1, max: 365, step: 1 }
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

let currentCalcKey = 'Loan EMI';

export function initCalculatorSuite() {
  const runner = document.getElementById('calc-runner');
  const chipsContainer = document.getElementById('calc-chips-container');
  const searchInput = document.getElementById('calc-search-input');
  const categoryTabs = document.querySelectorAll('[data-calc-category]');

  if (!runner) return;

  function renderChips(filterCat = 'ALL', query = '') {
    if (!chipsContainer) return;
    chipsContainer.innerHTML = '';

    const keys = Object.keys(CALCULATORS).filter((key) => {
      const item = CALCULATORS[key];
      const matchCat = filterCat === 'ALL' || item.category === filterCat;
      const matchQuery = !query || key.toLowerCase().includes(query.toLowerCase());
      return matchCat && matchQuery;
    });

    if (keys.length === 0) {
      chipsContainer.innerHTML = '<span class="calc-empty-note">No matching calculators found.</span>';
      return;
    }

    keys.forEach((key) => {
      const chip = document.createElement('button');
      chip.type = 'button';
      chip.className = `calc-chip ${key === currentCalcKey ? 'active' : ''}`;
      chip.textContent = key;
      chip.addEventListener('click', () => {
        currentCalcKey = key;
        renderChips(filterCat, query);
        renderWorkbench(key);
      });
      chipsContainer.appendChild(chip);
    });
  }

  function renderWorkbench(key) {
    const data = CALCULATORS[key];
    if (!data) return;

    const titleEl = document.getElementById('calc-runner-title');
    const catTagEl = document.getElementById('calc-category-tag');
    const formContainer = document.getElementById('calc-form-container');
    const resultLabelEl = document.getElementById('calc-result-label');
    const resultValEl = document.getElementById('calc-result-value');
    const resultSubEl = document.getElementById('calc-result-sub');

    if (titleEl) titleEl.textContent = key;
    if (catTagEl) catTagEl.textContent = data.category;
    if (resultLabelEl) resultLabelEl.textContent = data.label;

    if (!formContainer) return;
    formContainer.innerHTML = '';

    const inputValues = {};

    data.fields.forEach((field) => {
      inputValues[field.id] = field.val;

      const group = document.createElement('div');
      group.className = 'calc-input-group';

      const label = document.createElement('label');
      label.className = 'calc-input-label';
      label.setAttribute('for', `calc-input-${field.id}`);
      label.textContent = field.label;

      const input = document.createElement('input');
      input.id = `calc-input-${field.id}`;
      input.type = 'number';
      input.className = 'calc-field lg-input__field';
      input.value = field.val;
      if (field.min !== undefined) input.min = field.min;
      if (field.max !== undefined) input.max = field.max;
      if (field.step !== undefined) input.step = field.step;

      input.addEventListener('input', () => {
        inputValues[field.id] = input.value;
        updateCalculation();
      });

      group.appendChild(label);
      group.appendChild(input);
      formContainer.appendChild(group);
    });

    function updateCalculation() {
      const res = data.calc(inputValues);
      if (resultValEl) resultValEl.textContent = res.main;
      if (resultSubEl) resultSubEl.textContent = res.sub;
    }

    // Reset button
    const resetBtn = document.getElementById('calc-reset-btn');
    if (resetBtn) {
      resetBtn.onclick = () => {
        renderWorkbench(key);
      };
    }

    updateCalculation();
  }

  // Category filter tabs
  categoryTabs.forEach((tab) => {
    tab.addEventListener('click', () => {
      categoryTabs.forEach((t) => t.classList.remove('active'));
      tab.classList.add('active');
      const cat = tab.getAttribute('data-calc-category') || 'ALL';
      renderChips(cat, searchInput ? searchInput.value : '');
    });
  });

  // Search input
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      const activeTab = document.querySelector('[data-calc-category].active');
      const cat = activeTab ? activeTab.getAttribute('data-calc-category') : 'ALL';
      renderChips(cat, e.target.value);
    });
  }

  // Initial render
  renderChips('ALL');
  renderWorkbench(currentCalcKey);
}

