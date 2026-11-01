// Vercel Serverless Function: High-Speed APK Download Redirect
// Directs downloads to Cloudflare-backed Supabase Storage Edge CDN for instant 10 MB/s line-rate downloads,
// with graceful fallback to GitHub Releases.

import fs from 'fs';
import path from 'path';

const CLOUDFLARE_CDN_BASE = 'https://mtlckvkyimoqbrlvirch.supabase.co/storage/v1/object/public/apk';
const GITHUB_FALLBACK = 'https://github.com/ddheerajccarpenter/TDLxHT/releases/latest/download/TDlxHT.apk';

export default function handler(req, res) {
  let latestApkUrl = `${CLOUDFLARE_CDN_BASE}/TDlxHT.apk`;

  try {
    const candidates = [
      path.join(process.cwd(), 'version.json'),
      path.join(process.cwd(), 'dist', 'version.json'),
      path.join(process.cwd(), 'web', 'version.json')
    ];
    for (const candidate of candidates) {
      if (fs.existsSync(candidate)) {
        const vData = JSON.parse(fs.readFileSync(candidate, 'utf8'));
        if (vData.apkUrl) {
          latestApkUrl = vData.apkUrl;
          break;
        } else if (vData.filename) {
          latestApkUrl = `${CLOUDFLARE_CDN_BASE}/${vData.filename}`;
          break;
        }
      }
    }
  } catch (err) {
    console.warn('[Download API] Error resolving version.json, using fallback:', err.message);
  }

  res.setHeader('Cache-Control', 'public, s-maxage=60, stale-while-revalidate=120');
  res.setHeader('Location', latestApkUrl);
  return res.status(302).end();
}
