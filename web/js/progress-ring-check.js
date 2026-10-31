/**
 * ProgressRingCheck for TDlxHT Web Companion
 * Morphing SVG circular progress ring for task completion.
 * States: pending (8 dashed intervals) -> running (arc sweep) -> done (spring checkmark with strikethrough).
 */

const CHECK_PATH = "M7.5 12.25 10.5 15.25 16.75 8.75";

export function initProgressRingCheck() {
  const taskItems = document.querySelectorAll('.preview-task-item');

  taskItems.forEach((item) => {
    const isChecked = item.classList.contains('checked');
    const existingCheck = item.querySelector('.preview-task-check');

    // Create ProgressRingCheck SVG container
    const ringWrapper = document.createElement('span');
    ringWrapper.className = 'progress-ring-check-wrapper';
    ringWrapper.setAttribute('data-status', isChecked ? 'done' : 'pending');

    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.setAttribute('width', '20');
    svg.setAttribute('height', '20');
    svg.setAttribute('class', 'progress-ring-check-svg');

    // Background circle
    const bgCircle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
    bgCircle.setAttribute('cx', '12');
    bgCircle.setAttribute('cy', '12');
    bgCircle.setAttribute('r', '8.5');
    bgCircle.setAttribute('class', 'prc-bg');
    bgCircle.setAttribute('transform', 'rotate(-90 12 12)');

    // Dash ring
    const ringCircle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
    ringCircle.setAttribute('cx', '12');
    ringCircle.setAttribute('cy', '12');
    ringCircle.setAttribute('r', '8.5');
    ringCircle.setAttribute('class', 'prc-ring');
    ringCircle.setAttribute('transform', 'rotate(-90 12 12)');

    // Checkmark path
    const checkPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    checkPath.setAttribute('d', CHECK_PATH);
    checkPath.setAttribute('class', 'prc-check');
    checkPath.setAttribute('pathLength', '1');

    svg.appendChild(bgCircle);
    svg.appendChild(ringCircle);
    svg.appendChild(checkPath);

    if (existingCheck) {
      existingCheck.replaceWith(ringWrapper);
      ringWrapper.appendChild(svg);
    } else {
      item.prepend(ringWrapper);
      ringWrapper.appendChild(svg);
    }

    // Interactive click toggle with spring morph
    item.addEventListener('click', () => {
      const currentStatus = ringWrapper.getAttribute('data-status');
      if (currentStatus === 'done') {
        ringWrapper.setAttribute('data-status', 'pending');
        item.classList.remove('checked');
      } else {
        // Transition: running -> done
        ringWrapper.setAttribute('data-status', 'running');
        setTimeout(() => {
          ringWrapper.setAttribute('data-status', 'done');
          item.classList.add('checked');
        }, 280);
      }
    });
  });
}

// Auto-initialize when DOM is ready
if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initProgressRingCheck);
  } else {
    initProgressRingCheck();
  }
}

