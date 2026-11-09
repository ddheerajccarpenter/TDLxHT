/**
 * TDLxHT Modern Liquid Glass Web Application Orchestrator
 * High-performance, clean, modular client-side app
 */

import { initThemeToggle } from './theme.js';
import { initBottomNav } from './bottom-nav.js';
import { initCalculatorSuite } from './calculators.js';
import { initFocusWidget } from './focus-widget.js';
import { initVersionSync } from './version-sync.js';

document.addEventListener('DOMContentLoaded', () => {
  // Proactively terminate any legacy zombie service workers in background
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.getRegistrations().then((registrations) => {
      for (const reg of registrations) {
        reg.unregister();
      }
    });
  }

  // Check for Supabase Auth redirect hashes
  if (
    window.location.hash &&
    (window.location.hash.includes('access_token=') ||
      window.location.hash.includes('type=recovery') ||
      window.location.hash.includes('error='))
  ) {
    window.location.replace('/auth/callback' + window.location.hash);
    return;
  }

  // Initialize Core Systems
  initThemeToggle();
  initBottomNav();
  initCalculatorSuite();
  initFocusWidget();
  initVersionSync();

  // Cold launch classic 12-blade spinner dismissal
  const loader = document.getElementById('cold-launch-loader');
  if (loader) {
    setTimeout(() => {
      loader.classList.add('fade-out');
      setTimeout(() => loader.remove(), 400);
    }, 650);
  }
});
