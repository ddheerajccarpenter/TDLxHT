// Vercel Serverless Function: TDlxHT Secure User Registration
// Follows nodejs-backend-patterns:
// - Boundary input validation & string sanitization
// - Origin verification and strict security headers
// - Structured AppError and standard API envelopes
// - Rate limiting to prevent volumetric abuse

import { checkRateLimit } from './_rateLimit.js';
import { applySecurityHeaders, sendSuccess, sendError, AppError } from './_middleware.js';

export default async function handler(req, res) {
  if (applySecurityHeaders(req, res, { allowedMethods: ['POST', 'OPTIONS'] })) {
    return;
  }

  if (req.method !== 'POST') {
    return sendError(res, new AppError('Method not allowed. Use POST.', 405, 'METHOD_NOT_ALLOWED'));
  }

  // Rate limit: 10 registrations per 15 minutes per IP
  if (!checkRateLimit(req, res, { limit: 10, windowMs: 15 * 60 * 1000 })) {
    return;
  }

  const { email, password, display_name } = req.body || {};

  // Input Validation
  if (!email || typeof email !== 'string' || !email.includes('@') || email.length > 255) {
    return sendError(res, new AppError('A valid email address is required (max 255 characters).', 400, 'INVALID_EMAIL'));
  }

  if (!password || typeof password !== 'string' || password.length < 6 || password.length > 128) {
    return sendError(res, new AppError('Password must be between 6 and 128 characters.', 400, 'INVALID_PASSWORD'));
  }

  const cleanEmail = email.trim().toLowerCase();
  const rawDisplayName = (display_name && typeof display_name === 'string') ? display_name.trim().slice(0, 100) : cleanEmail.split('@')[0];
  const displayName = rawDisplayName.replace(/[<>'"&]/g, '');

  const supabaseUrl = process.env.SUPABASE_URL || 'https://mtlckvkyimoqbrlvirch.supabase.co';
  const serviceRoleKey = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_SERVICE_ROLE;
  const anonKey = process.env.SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im10bGNrdmt5aW1vcWJybHZpcmNoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc5Njc0MTEsImV4cCI6MjEwMzU0MzQxMX0.gGxCER-GAzSVS6IMzKoy5p8NwuDbOZ2QiBzkEeG_-J4';

  try {
    let response;
    if (serviceRoleKey) {
      // 1. Create user via Supabase GoTrue Admin API with email auto-confirmed
      response = await fetch(`${supabaseUrl}/auth/v1/admin/users`, {
        method: 'POST',
        headers: {
          'apikey': serviceRoleKey,
          'Authorization': `Bearer ${serviceRoleKey}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          email: cleanEmail,
          password: password,
          email_confirm: true,
          user_metadata: {
            display_name: displayName,
            email_verified: true
          }
        })
      });
    } else {
      // 2. Fallback: Register user via standard GoTrue signup with Anon Key
      response = await fetch(`${supabaseUrl}/auth/v1/signup`, {
        method: 'POST',
        headers: {
          'apikey': anonKey,
          'Authorization': `Bearer ${anonKey}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          email: cleanEmail,
          password: password,
          data: {
            display_name: displayName
          }
        })
      });
    }

    const data = await response.json();

    if (!response.ok) {
      const errMsg = data.msg || data.message || data.error_description || 'Failed to create user account.';

      if (errMsg.toLowerCase().includes('already registered') || errMsg.toLowerCase().includes('already exists')) {
        return sendError(res, new AppError('An account with this email already exists. Please sign in instead.', 409, 'USER_EXISTS'));
      }

      if (errMsg.includes('Database error') || errMsg.includes('saving new user') || errMsg.includes('creating new user')) {
        return sendError(res, new AppError('Database trigger conflict during registration.', 500, 'DB_TRIGGER_ERROR', errMsg));
      }

      return sendError(res, new AppError(errMsg, response.status || 400, 'REGISTRATION_FAILED'));
    }

    const user = data.user || data;

    return sendSuccess(res, {
      message: 'User registration completed successfully.',
      user: {
        id: user.id,
        email: user.email,
        display_name: displayName,
        confirmed: true
      }
    }, 201);

  } catch (err) {
    return sendError(res, new AppError('An unexpected server error occurred during registration.', 500, 'INTERNAL_ERROR', err.message));
  }
}
