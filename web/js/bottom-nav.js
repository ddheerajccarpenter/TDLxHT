/**
 * TDLxHT Liquid Glass Floating Bottom Dock Component
 * Drives optical sliding lens across tabs and smooth scroll spy
 */

export function initBottomNav() {
  const dock = document.querySelector('.bottom-dock');
  if (!dock) return;

  const tabs = dock.querySelectorAll('.dock-tab');
  const sectionIds = ['dashboard', 'features', 'calculators', 'download'];

  function setIndex(index) {
    dock.style.setProperty('--lg-index', index);
    tabs.forEach((tab, i) => {
      const isActive = i === index;
      tab.classList.toggle('active', isActive);
      if (isActive) {
        tab.setAttribute('aria-selected', 'true');
      } else {
        tab.removeAttribute('aria-selected');
      }
    });
  }

  // Click handler
  tabs.forEach((tab, index) => {
    tab.addEventListener('click', (e) => {
      const href = tab.getAttribute('href');
      if (href && href.startsWith('#')) {
        const target = document.querySelector(href);
        if (target) {
          e.preventDefault();
          setIndex(index);
          target.scrollIntoView({ behavior: 'smooth' });
        }
      }
    });
  });

  // IntersectionObserver Scroll Spy
  const sections = sectionIds.map((id) => document.getElementById(id)).filter(Boolean);
  if (!sections.length) return;

  let isUserScrolling = false;
  let scrollTimeout = null;

  const observer = new IntersectionObserver(
    (entries) => {
      // Find the most visible section
      let bestEntry = null;
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          if (!bestEntry || entry.intersectionRatio > bestEntry.intersectionRatio) {
            bestEntry = entry;
          }
        }
      });

      if (bestEntry) {
        const id = bestEntry.target.id;
        const index = sectionIds.indexOf(id);
        if (index !== -1) {
          setIndex(index);
        }
      }
    },
    {
      root: null,
      rootMargin: '-20% 0px -40% 0px',
      threshold: [0.1, 0.3, 0.6, 0.9]
    }
  );

  sections.forEach((sec) => observer.observe(sec));

  // Initialize first active tab
  setIndex(0);
}
