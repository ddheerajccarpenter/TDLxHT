/**
 * TDLxHT Liquid Glass Integration
 * Uses @zakisheriff/liquid-glass CSS classes on vanilla HTML elements
 * NOT React — pure DOM manipulation using the library's CSS
 */
import '@zakisheriff/liquid-glass/styles.css';

// Initialize all glass enhancements after DOM is ready
document.addEventListener('DOMContentLoaded', () => {
  initGlassNav();
  initGlassButtons();
  initGlassCards();
  initGlassSearch();
  initGlassHeader();
  initMouseGlow();
  initGlassMenu();
});

// ─── Nav: Upgrade the bottom dock to lg-track tabbar ─────────────────────────
function initGlassNav() {
  const dock = document.querySelector('.bottom-dock');
  if (!dock) return;

  // Add lg-track classes (already in HTML but ensure they're present)
  dock.classList.add('lg-track', 'lg-track--tabbar');

  // Ensure the glass background element exists
  if (!dock.querySelector('.lg-track__glass')) {
    const glassEl = document.createElement('div');
    glassEl.className = 'lg-track__glass lg-glass';
    dock.insertBefore(glassEl, dock.firstChild);
  }

  // Ensure lens + select elements exist for active indicator
  if (!dock.querySelector('.lg-track__lens')) {
    const lens = document.createElement('div');
    lens.className = 'lg-track__lens lg-glass';
    dock.appendChild(lens);
  }
  if (!dock.querySelector('.lg-track__select')) {
    const select = document.createElement('div');
    select.className = 'lg-track__select';
    dock.appendChild(select);
  }

  // Upgrade each dock-tab to lg-track__item
  const tabs = dock.querySelectorAll('.dock-tab');
  const count = tabs.length;
  dock.style.setProperty('--lg-count', count);

  tabs.forEach((tab, i) => {
    tab.classList.add('lg-track__item');
    const icon = tab.querySelector('.dock-tab-icon');
    const label = tab.querySelector('span:not(.dock-tab-icon)');
    if (icon) icon.classList.add('lg-nav__icon');
    if (label) label.classList.add('lg-nav__label');
    if (tab.classList.contains('active')) {
      tab.setAttribute('aria-selected', 'true');
      dock.style.setProperty('--lg-index', i);
    }
  });

  // Mark nav as ready so CSS transitions apply
  setTimeout(() => dock.setAttribute('data-ready', ''), 100);

  // Track active tab index for the glass lens position
  tabs.forEach((tab, i) => {
    tab.addEventListener('click', () => {
      tabs.forEach(t => t.removeAttribute('aria-selected'));
      tab.setAttribute('aria-selected', 'true');
      dock.style.setProperty('--lg-index', i);
    });
  });

  // Also watch for external setActive calls (scroll spy updates .active class)
  const observer = new MutationObserver(() => {
    tabs.forEach((tab, i) => {
      if (tab.classList.contains('active')) {
        dock.style.setProperty('--lg-index', i);
      }
    });
  });
  tabs.forEach(tab => observer.observe(tab, { attributes: true, attributeFilter: ['class'] }));
}

// ─── Buttons: Add lg-button glass classes ────────────────────────────────────
function initGlassButtons() {
  // Primary buttons → filled glass style
  document.querySelectorAll('.btn-primary').forEach(btn => {
    btn.classList.add('lg-button', 'lg-button--filled');
  });

  // Outline / secondary buttons → clear glass
  document.querySelectorAll('.btn-outline').forEach(btn => {
    btn.classList.add('lg-button', 'lg-button--clear', 'lg-glass');
  });

  // Theme toggle button → clear glass
  const themeBtn = document.getElementById('theme-toggle-btn');
  if (themeBtn) themeBtn.classList.add('lg-button', 'lg-button--clear', 'lg-glass');

  // Press glow feedback for all glass buttons
  document.querySelectorAll('.lg-glass').forEach(el => {
    el.addEventListener('pointerdown', () => el.setAttribute('data-pressed', ''));
    el.addEventListener('pointerup', () => el.removeAttribute('data-pressed'));
    el.addEventListener('pointerleave', () => el.removeAttribute('data-pressed'));
    el.addEventListener('pointercancel', () => el.removeAttribute('data-pressed'));
  });
}

// ─── Cards: Add lg-card lg-glass classes ─────────────────────────────────────
function initGlassCards() {
  // Feature cards
  document.querySelectorAll('.feature-card').forEach(card => {
    card.classList.add('lg-card', 'lg-glass');
  });

  // Product preview card
  document.querySelectorAll('.product-preview-card').forEach(card => {
    card.classList.add('lg-glass');
  });

  // Principles card
  document.querySelectorAll('.principles-card').forEach(card => {
    card.classList.add('lg-glass');
  });

  // Download card
  document.querySelectorAll('.download-card').forEach(card => {
    card.classList.add('lg-glass');
  });

  // Calc runner panel
  document.querySelectorAll('.calc-runner-panel').forEach(panel => {
    panel.classList.add('lg-glass');
  });

  // Preview stat boxes
  document.querySelectorAll('.preview-stat-box').forEach(box => {
    box.classList.add('lg-glass');
  });

  // Calculator category cards
  document.querySelectorAll('.calc-category-card').forEach(card => {
    card.classList.add('lg-glass');
  });

  // Product fact cells
  document.querySelectorAll('.product-fact-cell').forEach(cell => {
    cell.classList.add('lg-glass');
  });
}

// ─── Search: Add glass to search box ─────────────────────────────────────────
function initGlassSearch() {
  const searchBox = document.querySelector('.calc-search-box');
  if (searchBox) {
    searchBox.classList.add('lg-glass');
    const input = searchBox.querySelector('input');
    if (input) input.setAttribute('type', 'search');
  }
}

// ─── Header: Glass header frame ───────────────────────────────────────────────
function initGlassHeader() {
  const headerFrame = document.querySelector('.header-frame');
  if (headerFrame) {
    headerFrame.classList.add('lg-glass');
  }
}

// ─── Mouse Glow: Track cursor position for glass glow effect ─────────────────
function initMouseGlow() {
  document.addEventListener('pointermove', (e) => {
    const glassEls = document.querySelectorAll('.lg-glass');
    glassEls.forEach(el => {
      const rect = el.getBoundingClientRect();
      const x = ((e.clientX - rect.left) / rect.width) * 100;
      const y = ((e.clientY - rect.top) / rect.height) * 100;
      el.style.setProperty('--lg-x', `${x}%`);
      el.style.setProperty('--lg-y', `${y}%`);
    });
  });
}

// ─── GlassMenu: Sort dropdown with checkmarks ─────────────────────────────────
function initGlassMenu() {
  const trigger = document.getElementById('calc-sort-trigger');
  const popover = document.getElementById('calc-sort-popover');
  if (!trigger || !popover) return;

  let currentSort = 'Name';

  function openMenu() {
    popover.removeAttribute('hidden');
    trigger.setAttribute('aria-expanded', 'true');
    // Add lift effect
    popover.setAttribute('data-open', '');
  }

  function closeMenu() {
    popover.setAttribute('hidden', '');
    trigger.setAttribute('aria-expanded', 'false');
    popover.removeAttribute('data-open');
  }

  trigger.addEventListener('click', (e) => {
    e.stopPropagation();
    const isOpen = trigger.getAttribute('aria-expanded') === 'true';
    isOpen ? closeMenu() : openMenu();
  });

  // Close on outside click
  document.addEventListener('click', (e) => {
    if (!document.getElementById('calc-sort-menu').contains(e.target)) {
      closeMenu();
    }
  });

  // Close on Escape
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeMenu();
  });

  // Handle menu item selection
  popover.querySelectorAll('.glass-menu-item').forEach(item => {
    item.addEventListener('click', () => {
      const sort = item.getAttribute('data-sort');
      currentSort = sort;

      // Update checked state
      popover.querySelectorAll('.glass-menu-item').forEach(i => {
        i.setAttribute('aria-checked', i.getAttribute('data-sort') === sort ? 'true' : 'false');
      });

      closeMenu();

      // Dispatch sort event for future use
      document.dispatchEvent(new CustomEvent('calcSort', { detail: { sort } }));
    });
  });

  // Start with 'Name' checked
  popover.querySelectorAll('.glass-menu-item').forEach(item => {
    item.setAttribute('aria-checked', item.getAttribute('data-sort') === currentSort ? 'true' : 'false');
  });
}
