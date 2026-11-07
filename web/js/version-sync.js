/**
 * TDLxHT Version & SHA-256 Checksum Sync
 * Synchronizes download buttons and provides one-click hash copying
 */

export function initVersionSync() {
  const versionLabels = document.querySelectorAll('.apk-version-label');
  const downloadLinks = document.querySelectorAll('[data-apk-download="true"]');
  const shaHashEl = document.getElementById('sha-hash-text');
  const copyShaBtn = document.getElementById('copy-sha-btn');

  fetch('/version.json')
    .then((res) => (res.ok ? res.json() : null))
    .then((data) => {
      if (!data) return;
      const ver = data.version;
      const filename = data.filename || `TDLxHT.${ver}.apk`;
      const sha = data.sha256;
      const downloadUrl = data.apkUrl || data.fallbackUrl || `https://github.com/ddheerajccarpenter/TDLxHT/releases/download/v${ver}/${filename}`;

      versionLabels.forEach((el) => {
        el.textContent = ver;
      });

      downloadLinks.forEach((link) => {
        link.href = downloadUrl;
        const textSpan = link.querySelector('span');
        if (textSpan && textSpan.textContent.includes('Download APK')) {
          textSpan.textContent = `Download APK (v${ver})`;
        }
      });

      if (shaHashEl && sha) {
        shaHashEl.textContent = sha;
        shaHashEl.title = sha;
      }
    })
    .catch(() => {
      // Offline fallback: existing HTML hardcoded values remain intact
    });

  if (copyShaBtn && shaHashEl) {
    copyShaBtn.addEventListener('click', async () => {
      const textToCopy = shaHashEl.textContent.trim();
      try {
        await navigator.clipboard.writeText(textToCopy);
        const origText = copyShaBtn.textContent;
        copyShaBtn.textContent = 'Copied!';
        copyShaBtn.classList.add('copied');
        setTimeout(() => {
          copyShaBtn.textContent = origText;
          copyShaBtn.classList.remove('copied');
        }, 2000);
      } catch {
        // Fallback for older browsers
        const textarea = document.createElement('textarea');
        textarea.value = textToCopy;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        copyShaBtn.textContent = 'Copied!';
        setTimeout(() => {
          copyShaBtn.textContent = 'Copy';
        }, 2000);
      }
    });
  }
}

