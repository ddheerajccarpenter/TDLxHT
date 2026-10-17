// Vercel Serverless Function: TDlxHT Secure User Registration
// Creates users via GoTrue Admin API with auto-confirmed email (bypasses localhost confirmation issue)

import { checkRateLimit } from './_rateLimit.js';

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  // Rate limit: 10 registrations per 15 minutes per IP
  if (!checkRateLimit(req, res, { limit: 10, windowMs: 15 * 60 * 1000 })) {
    return;
  }

  const { email, password, display_name } = req.body || {};

  if (!email || typeof email !== 'string' || !email.includes('@') || email.length > 255) {
    return res.status(400).json({ error: 'A valid email address is required (max 255 characters).' });
  }

  if (!password || typeof password !== 'string' || password.length < 6 || password.length > 128) {
    return res.status(400).json({ error: 'Password must be between 6 and 128 characters.' });
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
      
      // Check for common trigger collision error
      if (errMsg.includes('Database error') || errMsg.includes('saving new user') || errMsg.includes('creating new user')) {
        return res.status(500).json({
          error: 'Database trigger conflict: Please execute the updated Section 1-3 in supabase_setup.sql via Supabase SQL Editor to drop the on_auth_user_auto_confirm trigger.',
          code: 'DB_TRIGGER_ERROR',
          details: errMsg
        });
      }

      if (errMsg.toLowerCase().includes('already registered') || errMsg.toLowerCase().includes('already exists')) {
        return res.status(409).json({
          error: 'An account with this email already exists. Please sign in instead.'
        });
      }

      return res.status(response.status).json({ error: errMsg });
    }

    return res.status(200).json({
      success: true,
      message: 'Account created successfully with auto-confirmed email.',
      user: {
        id: data.id,
        email: data.email,
        display_name: displayName,
        email_confirmed: true
      }
    });
  } catch (err) {
    console.error('Registration API error:', err);
    return res.status(500).json({
      error: err.message || 'Server error creating account.'
    });
  }
}
