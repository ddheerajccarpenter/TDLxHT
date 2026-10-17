/**
 * TDlxHT Minimal Web Companion & Release Portal
 * Pure Vanilla JavaScript — Zero heavy dependencies, lightning fast.
 */

document.addEventListener("DOMContentLoaded", () => {
  // If redirected from Supabase auth with tokens in hash, forward to callback
  if (window.location.hash && (window.location.hash.includes('access_token=') || window.location.hash.includes('type=recovery') || window.location.hash.includes('error='))) {
    window.location.replace('/auth/callback' + window.location.hash);
    return;
  }

  initThemeToggle();
  initAutoFetchVersion();
  initInteractivePreview();
  initDeepLinks();
});

// Theme Toggle: Sand Light vs. Cyprus Dark
function initThemeToggle() {
  const toggleBtn = document.getElementById("theme-toggle-btn");
  if (!toggleBtn) return;

  const savedTheme = localStorage.getItem("tdlxht_theme");
  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
  const initialTheme = savedTheme || (prefersDark ? "dark" : "light");

  applyTheme(initialTheme);

  toggleBtn.addEventListener("click", () => {
    const currentTheme = document.documentElement.getAttribute("data-theme") || "dark";
    const nextTheme = currentTheme === "dark" ? "light" : "dark";
    applyTheme(nextTheme);
  });
}

function applyTheme(theme) {
  document.documentElement.setAttribute("data-theme", theme);
  localStorage.setItem("tdlxht_theme", theme);
  const toggleBtn = document.getElementById("theme-toggle-btn");
  if (toggleBtn) {
    toggleBtn.innerHTML = theme === "dark"
      ? '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>'
      : '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>';
    toggleBtn.title = theme === "dark" ? "Switch to Sand Light Mode" : "Switch to Cyprus Dark Mode";
  }
}

// Auto-fetch updated APK version and configure all download links to GitHub Release
async function initAutoFetchVersion() {
  let version = "2.3.9";
  let apkUrl = "https://github.com/ddheerajccarpenter/TDLxHT/releases/download/v2.3.9/TDLxHT.2.3.9.apk";
  let releaseUrl = "https://github.com/ddheerajccarpenter/TDLxHT/releases/tag/v2.3.9";

  try {
    const res = await fetch(`/version.json?t=${Date.now()}`);
    if (res.ok) {
      const data = await res.json();
      if (data.version) version = data.version;
      if (data.apkUrl && data.apkUrl.startsWith("http")) apkUrl = data.apkUrl;
      if (data.githubReleaseUrl) releaseUrl = data.githubReleaseUrl;
    }
  } catch (err) {
    console.debug("Version manifest auto-fetch:", err);
  }

  // Update download buttons
  const downloadLinks = document.querySelectorAll('a[data-apk-download]');
  downloadLinks.forEach(link => {
    link.setAttribute('href', apkUrl);
    link.setAttribute('target', '_blank');
    link.setAttribute('rel', 'noopener');
  });

  // Update release button
  const releaseBtn = document.getElementById('hero-release-btn');
  if (releaseBtn) {
    releaseBtn.setAttribute('href', releaseUrl);
  }

  // Update version labels
  const versionLabels = document.querySelectorAll('.apk-version-label');
  versionLabels.forEach(label => {
    label.textContent = `v${version}`;
  });
}

// Interactive Preview Card
function initInteractivePreview() {
  const task1 = document.getElementById("task-preview-1");
  const task2 = document.getElementById("task-preview-2");
  const progressPill = document.getElementById("preview-progress-pill");
  const progressFill = document.getElementById("preview-progress-fill");

  const tasks = [task1, task2].filter(Boolean);

  function updateProgress() {
    const completedCount = tasks.filter(t => t.classList.contains("completed")).length;
    const total = tasks.length + 1; // +1 for habit
    const totalCompleted = completedCount + (habitChecked ? 1 : 0);
    const percent = Math.round((totalCompleted / total) * 100);

    if (progressPill) {
      progressPill.textContent = `${totalCompleted} / ${total} Done`;
    }
    if (progressFill) {
      progressFill.style.width = `${percent}%`;
    }
  }

  tasks.forEach(task => {
    task.addEventListener("click", () => {
      task.classList.toggle("completed");
      const btn = task.querySelector(".preview-checkbox");
      if (btn) {
        btn.textContent = task.classList.contains("completed") ? "✓" : "";
      }
      updateProgress();
    });
  });

  // Habit check-in
  let habitChecked = false;
  let streakDays = 14;
  const habitBtn = document.getElementById("habit-checkin-btn");
  const streakBadge = document.getElementById("preview-streak-badge");
  const habitFill = document.getElementById("habit-progress-fill");

  if (habitBtn) {
    habitBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      habitChecked = !habitChecked;
      if (habitChecked) {
        streakDays += 1;
        habitBtn.textContent = "✓ Done";
        habitBtn.classList.remove("btn-soft");
        habitBtn.classList.add("btn-primary");
        if (habitFill) habitFill.style.width = "100%";
      } else {
        streakDays -= 1;
        habitBtn.textContent = "+1";
        habitBtn.classList.remove("btn-primary");
        habitBtn.classList.add("btn-soft");
        if (habitFill) habitFill.style.width = "80%";
      }
      if (streakBadge) {
        streakBadge.textContent = `🔥 ${streakDays}d`;
      }
      updateProgress();
    });
  }
}

// Deep links fallback
function initDeepLinks() {
  const links = document.querySelectorAll('a[data-deeplink]');
  links.forEach(link => {
    link.addEventListener('click', (e) => {
      const fallbackUrl = "https://github.com/ddheerajccarpenter/TDLxHT/releases/latest";
      setTimeout(() => {
        if (!document.hidden) {
          window.location.href = fallbackUrl;
        }
      }, 1500);
    });
  });
}
