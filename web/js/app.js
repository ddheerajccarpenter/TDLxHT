/**
 * TDlxHT Web Showcase Companion
 * Pure, lightweight, ultra-fast Vanilla JavaScript (Zero external animation dependencies).
 * Mobbin Gallery White & Bugatti Austere Dark Design System.
 */

import { init3DScene } from './scene3d.js';

document.addEventListener("DOMContentLoaded", () => {
  // If redirected from Supabase auth with tokens or recovery state in hash, forward to callback
  if (window.location.hash && (window.location.hash.includes('access_token=') || window.location.hash.includes('type=recovery') || window.location.hash.includes('error='))) {
    window.location.replace('/auth/callback' + window.location.hash);
    return;
  }

  initThemeToggle();
  initMobileMenu();
  initTaskPreview();
  initHabitPreview();
  initMockupTabs();
  initDeepLinkHandlers();
  initAutoFetchVersion();
  checkBackendHealth();
  initMagicAnimations();
  initSmoothScroll();
  init3DScene();
});

// Mobile Navigation Drawer Toggle
function initMobileMenu() {
  const menuBtn = document.getElementById("mobile-menu-btn");
  const drawer = document.getElementById("mobile-nav-drawer");
  if (!menuBtn || !drawer) return;

  menuBtn.addEventListener("click", (e) => {
    e.stopPropagation();
    const isOpen = drawer.classList.toggle("open");
    menuBtn.classList.toggle("open", isOpen);
  });

  const navItems = drawer.querySelectorAll("a");
  navItems.forEach(item => {
    item.addEventListener("click", () => {
      menuBtn.classList.remove("open");
      drawer.classList.remove("open");
    });
  });

  document.addEventListener("click", (e) => {
    if (!menuBtn.contains(e.target) && !drawer.contains(e.target)) {
      menuBtn.classList.remove("open");
      drawer.classList.remove("open");
    }
  });
}

// Theme Toggle: Mobbin Gallery White vs. Bugatti Austere Dark
function initThemeToggle() {
  const toggleBtn = document.getElementById("theme-toggle-btn");
  if (!toggleBtn) return;

  const savedTheme = localStorage.getItem("tdlxht_theme");
  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
  const initialTheme = savedTheme || (prefersDark ? "dark" : "light");

  applyTheme(initialTheme);

  toggleBtn.addEventListener("click", () => {
    const currentTheme = document.documentElement.getAttribute("data-theme") || "light";
    const nextTheme = currentTheme === "dark" ? "light" : "dark";
    applyTheme(nextTheme);
  });
}

function applyTheme(theme) {
  document.documentElement.setAttribute("data-theme", theme);
  localStorage.setItem("tdlxht_theme", theme);
  window.dispatchEvent(new CustomEvent('tdlxht-theme-changed', { detail: { theme } }));
  const toggleBtn = document.getElementById("theme-toggle-btn");
  if (toggleBtn) {
    toggleBtn.innerHTML = theme === "dark"
      ? '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>'
      : '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>';
    toggleBtn.title = theme === "dark" ? "Switch to Sand Light Mode" : "Switch to Cyprus Dark Mode";
  }
}

// Mockup Interactive Tabs
function initMockupTabs() {
  const tabs = document.querySelectorAll(".mockup-tab");
  const taskItems = document.querySelectorAll(".mockup-task-item");
  const habitItems = document.querySelectorAll(".mockup-habit-item");

  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      tabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");
      const target = tab.getAttribute("data-tab");

      if (target === "all") {
        taskItems.forEach(el => el.style.display = "flex");
        habitItems.forEach(el => el.style.display = "block");
      } else if (target === "tasks") {
        taskItems.forEach(el => el.style.display = "flex");
        habitItems.forEach(el => el.style.display = "none");
      } else if (target === "habits") {
        taskItems.forEach(el => el.style.display = "none");
        habitItems.forEach(el => el.style.display = "block");
      }
    });
  });
}

// Interactive Task List in Hero Mockup
function initTaskPreview() {
  const taskItems = document.querySelectorAll(".mockup-task-item");
  taskItems.forEach(item => {
    item.addEventListener("click", () => {
      item.classList.toggle("completed");
      const checkbox = item.querySelector(".task-checkbox");
      if (checkbox) {
        if (item.classList.contains("completed")) {
          checkbox.textContent = "✓";
        } else {
          checkbox.textContent = "";
        }
      }
      updateTaskSummary();
    });
  });
}

function updateTaskSummary() {
  const total = document.querySelectorAll(".mockup-task-item").length;
  const completed = document.querySelectorAll(".mockup-task-item.completed").length;
  const statusBadge = document.getElementById("mockup-completion-badge");
  if (statusBadge) {
    statusBadge.textContent = `${completed}/${total} Completed`;
  }
}

// Interactive Habit Streak Incrementer
function initHabitPreview() {
  const logHabitBtn = document.getElementById("log-habit-preview-btn");
  const streakCount = document.getElementById("preview-streak-count");

  if (logHabitBtn && streakCount) {
    logHabitBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      let current = parseInt(streakCount.textContent) || 14;
      current += 1;
      streakCount.textContent = `${current} Days`;

      logHabitBtn.textContent = "Checked In";
      logHabitBtn.style.background = "var(--success)";
      logHabitBtn.style.color = "#ffffff";
      logHabitBtn.disabled = true;

      showToast(`Habit Check-in logged! Streak is now ${current} days.`);

      setTimeout(() => {
        logHabitBtn.textContent = "Check In (+1)";
        logHabitBtn.style.background = "";
        logHabitBtn.style.color = "";
        logHabitBtn.disabled = false;
      }, 2500);
    });
  }
}

// Deep Linking Trigger & Diagnostics
function initDeepLinkHandlers() {
  const deepLinkButtons = document.querySelectorAll("[data-deeplink]");
  deepLinkButtons.forEach(btn => {
    btn.addEventListener("click", (e) => {
      e.preventDefault();
      const targetPath = btn.getAttribute("data-deeplink") || "home";
      launchAppDeepLink(targetPath);
    });
  });
}

function launchAppDeepLink(path) {
  const url = `tdlxht://${path.replace(/^\//, '')}`;
  showToast(`Routing to Android App: ${url}`);
  window.location.href = url;
}

// Toast notification helper
function showToast(message) {
  let toast = document.getElementById("global-toast");
  if (!toast) {
    toast = document.createElement("div");
    toast.id = "global-toast";
    toast.style.position = "fixed";
    toast.style.bottom = "24px";
    toast.style.right = "24px";
    toast.style.padding = "12px 22px";
    toast.style.borderRadius = "9999px";
    toast.style.background = "var(--ink)";
    toast.style.color = "var(--canvas)";
    toast.style.border = "1px solid var(--hairline)";
    toast.style.zIndex = "9999";
    toast.style.boxShadow = "0 8px 24px rgba(0,0,0,0.18)";
    toast.style.fontFamily = "var(--font-mono, monospace)";
    toast.style.fontSize = "0.84rem";
    toast.style.fontWeight = "600";
    toast.style.transition = "opacity 0.25s ease, transform 0.25s ease";
    document.body.appendChild(toast);
  }

  toast.textContent = message;
  toast.style.display = "block";
  toast.style.opacity = "0";
  toast.style.transform = "translateY(12px)";
  
  requestAnimationFrame(() => {
    toast.style.opacity = "1";
    toast.style.transform = "translateY(0)";
  });

  clearTimeout(window._toastTimeout);
  window._toastTimeout = setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateY(8px)";
    setTimeout(() => { toast.style.display = "none"; }, 250);
  }, 3200);
}

// Auto-fetch updated APK version and update all download links and badges dynamically
async function initAutoFetchVersion() {
  let version = "2.3.8";
  let apkUrl = `/TDLxHT.${version}.apk`;
  let filename = `TDLxHT.${version}.apk`;

  try {
    const res = await fetch(`/version.json?t=${Date.now()}`);
    if (res.ok) {
      const data = await res.json();
      if (data.version) version = data.version;
      if (data.filename) filename = data.filename;
      if (data.apkUrl) apkUrl = data.apkUrl;
    } else {
      const apiRes = await fetch(`/api/status?t=${Date.now()}`);
      if (apiRes.ok) {
        const apiData = await apiRes.json();
        if (apiData.version) version = apiData.version;
        if (apiData.apk_filename) filename = apiData.apk_filename;
        if (apiData.apk_url) apkUrl = apiData.apk_url;
      }
    }
  } catch (err) {
    console.debug("Auto-fetch fallback:", err);
  }

  const downloadLinks = document.querySelectorAll('a[data-apk-download], a[href$=".apk"], a[download$=".apk"], a[href*="TDLxHT"]');
  downloadLinks.forEach(link => {
    link.setAttribute('href', apkUrl);
    link.setAttribute('download', filename);
  });

  const versionTags = document.querySelectorAll('.apk-version-label, [data-version-text]');
  versionTags.forEach(tag => {
    tag.textContent = `v${version}`;
  });

  const allElements = document.querySelectorAll('a, button, span');
  allElements.forEach(el => {
    if (el.childNodes.length === 1 && el.textContent.includes('Download TDlxHT APK (v')) {
      el.textContent = `Download TDlxHT APK (v${version})`;
    }
  });

  const footerApkLink = document.getElementById("footer-apk-download");
  if (footerApkLink) {
    footerApkLink.textContent = `Download APK (v${version})`;
    footerApkLink.setAttribute('href', apkUrl);
    footerApkLink.setAttribute('download', filename);
  }

  const backendTag = document.getElementById("backend-health-tag");
  if (backendTag) {
    backendTag.textContent = `V${version} ONLINE`;
  }
}

// Backend Health Check
async function checkBackendHealth() {
  const statusEl = document.getElementById("backend-health-tag");
  if (!statusEl) return;

  try {
    const res = await fetch("/api/status");
    if (res.ok) {
      const data = await res.json();
      statusEl.textContent = `BACKEND V${data.version} ONLINE`;
      statusEl.style.color = "var(--success)";
    } else {
      statusEl.textContent = "Offline Room Sync Ready";
    }
  } catch (e) {
    statusEl.textContent = "Room DB Offline Engine";
  }
}

// Cursor Spotlight Tracking (Clean Vanilla CSS custom property update)
function initMagicAnimations() {
  const spotlightElements = document.querySelectorAll(".spotlight-card, .feature-card, .spec-box, .app-device-card");
  spotlightElements.forEach((el) => {
    el.classList.add("spotlight-card");
    el.addEventListener("mousemove", (e) => {
      const rect = el.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      el.style.setProperty("--mouse-x", `${x}px`);
      el.style.setProperty("--mouse-y", `${y}px`);
    });
  });
}

// Smooth scrolling for navigation anchor links
function initSmoothScroll() {
  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", (e) => {
      const href = anchor.getAttribute("href");
      if (!href || href === "#") return;
      const target = document.querySelector(href);
      if (target) {
        e.preventDefault();
        target.scrollIntoView({ behavior: "smooth" });
      }
    });
  });
}
