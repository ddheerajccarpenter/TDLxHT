// Vercel Serverless Function: TDlxHT Secure Password Recovery Handler
// Dispatches a password recovery email strictly to the user's inbox.
// Zero Secret Leakage: Does NOT expose tokens or action links in HTTP response.

import nodemailer from 'nodemailer';
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

  // Rate limit: 5 password recovery requests per 15 minutes per IP
  if (!checkRateLimit(req, res, { limit: 5, windowMs: 15 * 60 * 1000 })) {
    return;
  }

  const { email } = req.body || {};
  if (!email || typeof email !== 'string' || !email.includes('@') || email.length > 255) {
    return res.status(400).json({ error: 'A valid email address is required (max 255 characters).' });
  }

  const cleanEmail = email.trim().toLowerCase();
  const supabaseUrl = process.env.SUPABASE_URL || 'https://mtlckvkyimoqbrlvirch.supabase.co';
  const serviceRoleKey = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_SERVICE_ROLE;

  const anonKey = process.env.SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im10bGNrdmt5aW1vcWJybHZpcmNoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc5Njc0MTEsImV4cCI6MjEwMzU0MzQxMX0.gGxCER-GAzSVS6IMzKoy5p8NwuDbOZ2QiBzkEeG_-J4';

  try {
    const webUrl = process.env.APP_WEB_URL || 'https://tdlxht.vercel.app';
    const callbackUrl = `${webUrl}/auth/callback`;

    let actionLink = null;

    if (serviceRoleKey) {
      // 1. Generate the official one-time verification link via Supabase GoTrue Admin API
      try {
        const response = await fetch(`${supabaseUrl}/auth/v1/admin/generate_link`, {
          method: 'POST',
          headers: {
            'apikey': serviceRoleKey,
            'Authorization': `Bearer ${serviceRoleKey}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            type: 'recovery',
            email: cleanEmail,
            options: {
              redirectTo: callbackUrl
            }
          })
        });

        const data = await response.json();
        if (response.ok && data.action_link) {
          actionLink = data.action_link;
        }
      } catch (adminErr) {
        console.warn('Admin generate_link failed:', adminErr);
      }
    }

    // 2. Dispatch Email via SMTP (Gmail, Resend, or custom SMTP) if configured
    const smtpHost = process.env.SMTP_HOST;
    const smtpUser = process.env.SMTP_USER;
    const smtpPass = process.env.SMTP_PASS;
    const smtpPort = parseInt(process.env.SMTP_PORT || '587', 10);

    let emailSent = false;

    if (smtpHost && smtpUser && smtpPass && actionLink) {
      try {
        const transporter = nodemailer.createTransport({
          host: smtpHost,
          port: smtpPort,
          secure: smtpPort === 465,
          auth: {
            user: smtpUser,
            pass: smtpPass
          }
        });

        await transporter.sendMail({
          from: `"TDlxHT Security" <${smtpUser}>`,
          to: cleanEmail,
          subject: 'Reset Your TDlxHT Password',
          html: `
            <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 560px; margin: 0 auto; padding: 36px 24px; background: #ffffff; border: 1px solid #e5e5e5; border-radius: 16px;">
              <div style="margin-bottom: 24px;">
                <span style="font-size: 20px; font-weight: 800; color: #141414; letter-spacing: -0.5px;">✓ TDlxHT</span>
              </div>
              <h2 style="font-size: 20px; color: #141414; font-weight: 700; margin-bottom: 12px;">Reset Your Password</h2>
              <p style="font-size: 15px; color: #555555; line-height: 1.6; margin-bottom: 24px;">
                We received a request to reset the password for your TDlxHT account (<strong>${cleanEmail}</strong>). Click the button below to set a new password:
              </p>
              <div style="text-align: center; margin: 32px 0;">
                <a href="${actionLink}" style="background: #10b981; color: #ffffff; padding: 14px 32px; border-radius: 9999px; text-decoration: none; font-weight: 600; font-size: 15px; display: inline-block;">
                  Reset Password Now
                </a>
              </div>
              <p style="font-size: 13px; color: #888888; line-height: 1.5; margin-top: 24px; border-top: 1px solid #f0f0f0; padding-top: 18px;">
                If you did not request this change, you can safely ignore this email. Your password will remain unchanged. This link is valid for 1 hour.
              </p>
            </div>
          `
        });
        emailSent = true;
      } catch (mailErr) {
        console.error('SMTP Mail send failed:', mailErr);
      }
    }

    // 3. Fallback: If custom backend SMTP is not set, try Supabase native recover
    if (!emailSent) {
      try {
        const anonKey = process.env.SUPABASE_ANON_KEY || serviceRoleKey;
        await fetch(`${supabaseUrl}/auth/v1/recover`, {
          method: 'POST',
          headers: {
            'apikey': anonKey,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            email: cleanEmail,
            redirect_to: callbackUrl
          })
        });
      } catch (nativeErr) {
        console.warn('Native GoTrue recover attempt:', nativeErr);
      }
    }

    // CRITICAL SECURITY INVARIANT:
    // NEVER leak action_link or verification tokens in the response body!
    return res.status(200).json({
      success: true,
      message: 'If an account exists with this email address, a password recovery link has been sent to your Gmail inbox. Please check your inbox and spam folder.'
    });

  } catch (err) {
    console.error('Password recovery error:', err);
    return res.status(500).json({
      error: 'An unexpected error occurred while processing your request.'
    });
  }
}
