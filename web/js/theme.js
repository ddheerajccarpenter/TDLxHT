/**
 * TDLxHT Liquid Glass Theme Engine
 * Manages Dark / Light mode switching, persistence, and synchronized glass switch animations
 */

export function getCurrentTheme() {
  return localStorage.getItem('tdlxht-theme') || 'dark';
}

export function setTheme(theme) {
  const root = document.documentElement;
  const isDark = theme === 'dark';
  root.setAttribute('data-theme', theme);
  localStorage.setItem('tdlxht-theme', theme);

  // Sync all switch checkboxes across the DOM
  document.querySelectorAll('.theme-switch-input').forEach((input) => {
    input.checked = isDark;
  });

  // Dispatch custom event for reactive glass components
  window.dispatchEvent(new CustomEvent('tdlxht-theme-change', { detail: { theme, isDark } }));
}

export function initThemeToggle() {
  const current = getCurrentTheme();
  setTheme(current);

  document.querySelectorAll('.theme-switch-input').forEach((input) => {
    input.addEventListener('change', () => {
      const nextTheme = input.checked ? 'dark' : 'light';
      setTheme(nextTheme);
    });
  });

  // Handle any legacy or header toggle buttons
  document.querySelectorAll('[data-action="toggle-theme"]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
      setTheme(isDark ? 'light' : 'dark');
    });
  });
}
