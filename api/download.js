// Vercel Serverless Function: Dynamic Latest APK Download Redirect
// Follows nodejs-backend-patterns: dynamically resolves the current release version
// from version.json to ensure download requests are always routed to the latest binary.

import fs from 'fs';
import path from 'path';

export default function handler(req, res) {
  let latestApkUrl = 'https://github.com/ddheerajccarpenter/TDLxHT/releases/download/v2.4.3/TDLxHT.2.4.3.apk';

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
