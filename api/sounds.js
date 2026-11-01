// Vercel Serverless Function: TDlxHT Audio System & Remote Sound Manifest
// Strict Security Architecture:
// - Android APK never carries ADMIN_API_KEY
// - Public GET endpoint returns verified sound catalog with SHA-256 integrity hashes
// - Mutation POST endpoint strictly guarded by X-Admin-Key matching process.env.ADMIN_API_KEY

import { checkRateLimit } from './_rateLimit.js';
import { applySecurityHeaders } from './_middleware.js';
import fs from 'fs';
import path from 'path';

// Canonical default sound manifest
const DEFAULT_SOUNDS = [
  {
    id: "task_complete",
    title: "Task Completed (Organic Chime)",
    url: "https://tdlxht.vercel.app/sounds/task_complete.ogg",
    sha256: "c10c174c1da938a700825f950446d5025e7746aaf8a807210c50c6e9b5f2e539",
    updated_at: "2026-11-01T00:00:00Z"
  },
  {
    id: "habit_tick",
    title: "Habit Completed (Pop)",
    url: "https://tdlxht.vercel.app/sounds/habit_tick.ogg",
    sha256: "29ba8ea28a089de1a3d0b3a83b83e9d2530e13f0807e61d5a74d25c9b5ffe858",
    updated_at: "2026-11-01T00:00:00Z"
  },
  {
    id: "button_click",
    title: "Button Click (Haptic Wood)",
    url: "https://tdlxht.vercel.app/sounds/button_click.ogg",
    sha256: "3299ade6b42e052637b2c412048ab4bf2173aadd4963f7e9d5712e07729a64e3",
    updated_at: "2026-11-01T00:00:00Z"
  },
  {
    id: "timer_end",
    title: "Timer Finished (Harmonic Bell)",
    url: "https://tdlxht.vercel.app/sounds/timer_end.ogg",
    sha256: "63e9879ddc3d22382e813daacdb2f92aaa829115921447a566e4c2d006732686",
    updated_at: "2026-11-01T00:00:00Z"
  },
  {
    id: "swipe_nav",
    title: "Swipe Navigation (Subtle Woosh)",
    url: "https://tdlxht.vercel.app/sounds/swipe_nav.ogg",
    sha256: "f7a5b6f8de261ca58b582a8285e9d6b099a5591d1437eed735f0e93f01078b2c",
    updated_at: "2026-11-01T00:00:00Z"
  }
];

// In-memory runtime cache for serverless lifetime
let runtimeSounds = [...DEFAULT_SOUNDS];
const TMP_STORAGE = '/tmp/tdlxht_sounds.json';

function loadStoredSounds() {
  try {
    if (fs.existsSync(TMP_STORAGE)) {
      const content = fs.readFileSync(TMP_STORAGE, 'utf8');
      const parsed = JSON.parse(content);
      if (Array.isArray(parsed) && parsed.length > 0) {
        runtimeSounds = parsed;
      }
    }
  } catch (err) {
    // Non-fatal, use runtime cache
  }
  return runtimeSounds;
}

function saveStoredSounds(sounds) {
  runtimeSounds = sounds;
  try {
    fs.writeFileSync(TMP_STORAGE, JSON.stringify(sounds, null, 2), 'utf8');
  } catch (err) {
    // Non-fatal in read-only environments
  }
}

export default async function handler(req, res) {
  // CORS & Security headers
  if (applySecurityHeaders(req, res, { allowedMethods: ['GET', 'POST', 'OPTIONS', 'HEAD'] })) {
    return;
  }

  // Rate limiting (60 req/min for GET, 15 req/min for POST)
  const isPost = req.method === 'POST';
  const limit = isPost ? 15 : 60;
  if (!checkRateLimit(req, res, { limit, windowMs: 60 * 1000 })) {
    return;
  }

  const sounds = loadStoredSounds();

  if (req.method === 'GET' || req.method === 'HEAD') {
    res.setHeader('Content-Type', 'application/json; charset=utf-8');
    res.setHeader('Cache-Control', 'public, max-age=300, stale-while-revalidate=86400');
    return res.status(200).json(sounds);
  }

  if (req.method === 'POST') {
    // Authenticate Admin Key
    const providedKey = req.headers['x-admin-key'] ||
      (req.headers.authorization && req.headers.authorization.replace(/^Bearer\s+/i, '').trim());

    const expectedKey = process.env.ADMIN_API_KEY || process.env.ADMIN_KEY;

    if (!expectedKey) {
      return res.status(503).json({
        error: 'ADMIN_API_KEY is not configured on the server environment. Writes are disabled.'
      });
    }

    if (!providedKey || providedKey !== expectedKey) {
      return res.status(401).json({
        error: 'Unauthorized: Invalid or missing X-Admin-Key credentials.'
      });
    }

    // Process update payload
    const body = req.body || {};
    const { id, title, url, sha256, sounds: bulkSounds } = body;

    if (Array.isArray(bulkSounds)) {
      // Bulk update
      const validated = bulkSounds.map(s => ({
        id: String(s.id || '').trim(),
        title: String(s.title || '').trim(),
        url: String(s.url || '').trim(),
        sha256: String(s.sha256 || '').trim().toLowerCase(),
        updated_at: new Date().toISOString()
      })).filter(s => s.id && s.url);

      saveStoredSounds(validated);
      return res.status(200).json({
        success: true,
        message: 'Bulk sound manifest updated successfully.',
        count: validated.length,
        sounds: validated
      });
    }

    if (!id || !url) {
      return res.status(400).json({
        error: 'Missing required fields: id and url must be provided.'
      });
    }

    const existingIndex = sounds.findIndex(s => s.id === id);
    const updatedEntry = {
      id,
      title: title || (existingIndex >= 0 ? sounds[existingIndex].title : id),
      url,
      sha256: sha256 ? sha256.trim().toLowerCase() : (existingIndex >= 0 ? sounds[existingIndex].sha256 : ""),
      updated_at: new Date().toISOString()
    };

    if (existingIndex >= 0) {
      sounds[existingIndex] = updatedEntry;
    } else {
      sounds.push(updatedEntry);
    }

    saveStoredSounds(sounds);

    return res.status(200).json({
      success: true,
      message: `Sound '${id}' saved successfully.`,
      sound: updatedEntry,
      sounds
    });
  }

  return res.status(405).json({
    error: `Method ${req.method} not allowed.`
  });
}

