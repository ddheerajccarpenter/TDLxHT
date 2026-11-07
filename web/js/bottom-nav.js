/**
 * TDLxHT Liquid Glass Floating Bottom Dock Component
 * Single Unified Navigation Bar with optical sliding lens and precision scroll spy
 */

export function initBottomNav() {
  const dock = document.querySelector('.bottom-dock');
  if (!dock) return;

  // Select navigation tabs only (excluding utility buttons like theme switch)
  const navTabs = dock.querySelectorAll('a.dock-tab');
  const sectionIds = ['dashboard', 'features', 'calculators', 'download'];

  function setIndex(index) {
    if (index < 0 || index >= navTabs.length) return;
    dock.style.setProperty('--lg-index', index);
    navTabs.forEach((tab, i) => {
      const isActive = i === index;
      tab.classList.toggle('active', isActive);
      if (isActive) {
        tab.setAttribute('aria-selected', 'true');
      } else {
        tab.removeAttribute('aria-selected');
      }
    });
  }

  // Click handler for navigation tabs
  navTabs.forEach((tab, index) => {
    tab.addEventListener('click', (e) => {
      const href = tab.getAttribute('href');
      if (href && href.startsWith('#')) {
        const target = document.querySelector(href);
        if (target) {
          e.preventDefault();
          setIndex(index);
          target.scrollIntoView({ behavior: 'smooth' });
        } else {
          // On non-root routes (e.g. /auth), navigate back to root with anchor
          window.location.href = '/' + href;
        }
      }
    });
  });

  // IntersectionObserver Scroll Spy (only active on pages with these sections)
  const sections = sectionIds.map((id) => document.getElementById(id)).filter(Boolean);
  if (sections.length > 0) {
    const observer = new IntersectionObserver(
      (entries) => {
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

    // Scroll-to-bottom fallback to ensure #download tab activates on short viewports
    window.addEventListener(
      'scroll',
      () => {
        const scrollPosition = window.innerHeight + window.scrollY;
        const totalHeight = document.documentElement.scrollHeight;
        if (scrollPosition >= totalHeight - 65) {
          const downloadIndex = sectionIds.indexOf('download');
          if (downloadIndex !== -1) {
            setIndex(downloadIndex);
          }
        }
      },
      { passive: true }
    );
  }

  // Route-aware initial active tab
  if (window.location.pathname.startsWith('/auth')) {
    const portalTab = dock.querySelector('[data-tab="portal"]');
    if (portalTab) {
      const portalIndex = Array.from(navTabs).indexOf(portalTab);
      if (portalIndex !== -1) {
        setIndex(portalIndex);
        return;
      }
    }
  }

  setIndex(0);
}
