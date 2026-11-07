/**
 * TDLxHT Interactive Glass Focus Hub Widget
 * Coordinates interactive tasks, real-time SVG progress ring, and working Pomodoro timer
 */

export function initFocusWidget() {
  const taskItems = document.querySelectorAll('.preview-task-item');
  const progressRing = document.getElementById('preview-progress-ring');
  const progressPercent = document.getElementById('preview-progress-percent');
  const progressTitle = document.getElementById('preview-progress-title');
  const timerBtn = document.getElementById('preview-timer-btn');
  const timerDisplay = document.getElementById('preview-timer-display');
  const timerResetBtn = document.getElementById('preview-timer-reset');

  // 1. Task Toggle & Circular Progress Ring
  function updateProgress() {
    const total = taskItems.length;
    if (total === 0) return;

    let checkedCount = 0;
    taskItems.forEach((item) => {
      if (item.classList.contains('checked')) {
        checkedCount++;
      }
    });

    const percent = Math.round((checkedCount / total) * 100);
    const circumference = 163.36; // 2 * PI * 26
    const offset = circumference - (circumference * percent) / 100;

    if (progressRing) {
      progressRing.style.strokeDashoffset = `${offset}`;
    }
    if (progressPercent) {
      progressPercent.textContent = `${percent}%`;
    }
    if (progressTitle) {
      progressTitle.textContent = `${checkedCount} of ${total} tasks completed`;
    }
  }

  taskItems.forEach((item) => {
    item.addEventListener('click', () => {
      const isChecked = item.classList.toggle('checked');
      item.setAttribute('aria-checked', isChecked ? 'true' : 'false');
      const checkEl = item.querySelector('.preview-task-check');
      if (checkEl) {
        checkEl.textContent = isChecked ? '✓' : '';
      }
      updateProgress();
    });

    // Keyboard support
    item.addEventListener('keydown', (e) => {
      if (e.key === ' ' || e.key === 'Enter') {
        e.preventDefault();
        item.click();
      }
    });
  });

  updateProgress();

  // 2. Working Pomodoro Timer
  let timerInterval = null;
  let remainingSeconds = 25 * 60; // 25 minutes
  let isRunning = false;

  function renderTimer() {
    if (!timerDisplay) return;
    const mins = Math.floor(remainingSeconds / 60);
    const secs = remainingSeconds % 60;
    timerDisplay.textContent = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  function startTimer() {
    if (isRunning) return;
    isRunning = true;
    if (timerBtn) {
      timerBtn.innerHTML = '<span>⏸ Pause</span>';
    }
    timerInterval = setInterval(() => {
      if (remainingSeconds > 0) {
        remainingSeconds--;
        renderTimer();
      } else {
        pauseTimer();
        alert('Focus session complete!');
        remainingSeconds = 25 * 60;
        renderTimer();
      }
    }, 1000);
  }

  function pauseTimer() {
    isRunning = false;
    clearInterval(timerInterval);
    timerInterval = null;
    if (timerBtn) {
      timerBtn.innerHTML = '<span>▶ Resume</span>';
    }
  }

  if (timerBtn) {
    timerBtn.addEventListener('click', () => {
      if (isRunning) {
        pauseTimer();
      } else {
        startTimer();
      }
    });
  }

  if (timerResetBtn) {
    timerResetBtn.addEventListener('click', () => {
      pauseTimer();
      remainingSeconds = 25 * 60;
      renderTimer();
      if (timerBtn) {
        timerBtn.innerHTML = '<span>▶ Start</span>';
      }
    });
  }

  renderTimer();
}

