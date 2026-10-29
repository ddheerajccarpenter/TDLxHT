/**
 * TDlxHT Backend Middleware & Security Suite
 * Follows patterns from nodejs-backend-patterns & backend-patterns skills:
 * - Dynamic origin verification (no wildcard CORS in production)
 * - Security response headers (nosniff, DENY, strict-origin)
 * - Structured AppError and standardized API response envelope
 * - Preflight OPTIONS handling
 */

export class AppError extends Error {
  constructor(message, statusCode = 500, code = 'INTERNAL_ERROR', details = null) {
    super(message);
    this.name = 'AppError';
    this.statusCode = statusCode;
    this.code = code;
    this.details = details;
  }
}

const ALLOWED_ORIGINS = [
  'https://tdlxht.vercel.app',
  'http://localhost:5173',
  'http://localhost:3000',
  'http://127.0.0.1:5173',
  'http://127.0.0.1:3000'
];

/**
 * Validates CORS origin and applies HTTP security headers.
 * Returns true if request was handled (e.g., OPTIONS preflight), false otherwise.
 */
export function applySecurityHeaders(req, res, { allowedMethods = ['GET', 'POST', 'OPTIONS'] } = {}) {
  const origin = req.headers.origin;

  if (origin && (ALLOWED_ORIGINS.includes(origin) || origin.endsWith('.vercel.app'))) {
    res.setHeader('Access-Control-Allow-Origin', origin);
  } else {
    res.setHeader('Access-Control-Allow-Origin', 'https://tdlxht.vercel.app');
  }

  res.setHeader('Vary', 'Origin');
  res.setHeader('Access-Control-Allow-Methods', allowedMethods.join(', '));
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With');
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'DENY');
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');

  if (req.method === 'OPTIONS') {
    res.status(204).end();
    return true;
  }

  return false;
}

/**
 * Standardized success response envelope
 */
export function sendSuccess(res, data, statusCode = 200) {
  return res.status(statusCode).json({
    success: true,
    timestamp: new Date().toISOString(),
    ...data
  });
}

/**
 * Standardized error response envelope
 */
export function sendError(res, error) {
  const statusCode = error.statusCode || 500;
  const message = error.message || 'An unexpected internal error occurred';
  const code = error.code || 'INTERNAL_SERVER_ERROR';

  if (statusCode >= 500) {
    console.error(`[API Error] ${code}:`, error);
  }

  return res.status(statusCode).json({
    success: false,
    timestamp: new Date().toISOString(),
    error: message,
    code,
    ...(error.details ? { details: error.details } : {})
  });
}
