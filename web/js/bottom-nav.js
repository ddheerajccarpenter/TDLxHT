/**
 * TDlxHT Wireframe Bottom Navigation Component
 * 4 Destinations: Dashboard, Features, Calculators, Download
 * Centralized Scroll Spy with IntersectionObserver & rAF
 */

export function initBottomNav() {
  const dock = document.querySelector('.bottom-dock') || document.querySelector('.bottom-dock-nav');
  if (!dock) return;

  const items = dock.querySelectorAll('.dock-tab, .dock-item');
  const sectionIds = ['dashboard', 'features', 'calculators', 'download'];
  const path = window.location.pathname;

  function setActive(tabId) {
    items.forEach(item => {
      const id = item.getAttribute('data-tab') || item.getAttribute('data-nav');
      const isTarget = id === tabId;
      item.classList.toggle('active', isTarget);
      if (isTarget) {
        item.setAttribute('aria-current', 'page');
      } else {
        item.removeAttribute('aria-current');
      }
    });
  }

  // If on Auth Portal, unselect or keep clear
  if (path.startsWith('/auth')) {
    items.forEach(item => {
      item.classList.remove('active');
      item.removeAttribute('aria-current');
    });
    return;
  }

  // Real IntersectionObserver & Scroll Spy
  const sections = sectionIds.map(id => document.getElementById(id)).filter(Boolean);
  if (!sections.length) return;

  setActive('dashboard');

  let ticking = false;
  window.addEventListener('scroll', () => {
    if (!ticking) {
      window.requestAnimationFrame(() => {
        updateActiveSection();
        ticking = false;
      });
      ticking = true;
    }
  }, { passive: true });

  function updateActiveSection() {
    const scrollY = window.pageYOffset;
    const scrollPos = scrollY + 160;

    // Top of page
    if (scrollY < 120) {
      setActive('dashboard');
      return;
    }

    // Bottom of page
    if ((window.innerHeight + scrollY) >= (document.documentElement.scrollHeight - 120)) {
      setActive('download');
      return;
    }

    for (let i = sections.length - 1; i >= 0; i--) {
      const sec = sections[i];
      if (sec.offsetTop <= scrollPos) {
        setActive(sec.id);
        break;
      }
    }
  }

  // Smooth click scroll
  items.forEach(item => {
    item.addEventListener('click', (e) => {
      const href = item.getAttribute('href');
      if (href && (href.startsWith('#') || href.startsWith('/#'))) {
        const targetId = href.replace('/#', '').replace('#', '');
        const targetEl = document.getElementById(targetId);
        if (targetEl) {
          e.preventDefault();
          const headerOffset = 60;
          const elementPosition = targetEl.getBoundingClientRect().top;
          const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

          window.scrollTo({
            top: offsetPosition,
            behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'
          });

          if (history.pushState) {
            history.pushState(null, '', `#${targetId}`);
          }
          setActive(targetId);
        }
      }
    });
  });

  // Check initial hash
  if (window.location.hash) {
    const initialNav = window.location.hash.replace('#', '');
    if (sectionIds.includes(initialNav)) {
      setActive(initialNav);
    }
  } else {
    updateActiveSection();
  }
}
