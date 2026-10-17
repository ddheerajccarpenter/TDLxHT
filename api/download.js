// Vercel Serverless Function: Dynamic Latest APK Download Redirect
// Ensures direct links to /api/download always serve the newest APK without manual URL changes.

import fs from 'fs';
import path from 'path';

export default function handler(req, res) {
  let apkUrl = '/TDLxHT.2.3.8.apk';

  try {
    const candidates = [
      path.join(process.cwd(), 'version.json'),
      path.join(process.cwd(), 'dist', 'version.json'),
      path.join(process.cwd(), 'web', 'version.json')
    ];
    for (const candidate of candidates) {
      if (fs.existsSync(candidate)) {
        const data = JSON.parse(fs.readFileSync(candidate, 'utf8'));
        if (data.apkUrl) {
          apkUrl = data.apkUrl;
          break;
        } else if (data.filename) {
          apkUrl = `/${data.filename}`;
          break;
        }
      }
    }
  } catch (_) {}

  res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
  return res.redirect(302, apkUrl);
}
