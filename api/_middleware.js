/**
 * TDlxHT Backend Middleware & Security Suite
 * Follows patterns from nodejs-backend-patterns & backend-patterns skills:
 * - Dynamic origin verification (no wildcard CORS in production)
 * - Distributed request correlation ID (X-Request-Id) for end-to-end tracing
 * - Performance metrics & latency headers (Server-Timing)
 * - Security response headers (nosniff, DENY, strict-origin)
 * - Structured AppError and standardized API response envelope
 * - Preflight OPTIONS handling
 */

import crypto from 'crypto';

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
 * Validates CORS origin, attaches correlation tracing IDs, and applies HTTP security headers.
 * Returns true if request was handled (e.g., OPTIONS preflight), false otherwise.
 */
export function applySecurityHeaders(req, res, { allowedMethods = ['GET', 'POST', 'OPTIONS'] } = {}) {
  // Correlation ID (nodejs-backend-patterns): Inherit from upstream proxy or generate unique UUID
  const incomingReqId = req.headers?.['x-request-id'];
  const requestId = (typeof incomingReqId === 'string' && incomingReqId.trim().length > 0)
    ? incomingReqId.trim().slice(0, 64)
    : (crypto.randomUUID ? crypto.randomUUID() : `req_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`);

  req.requestId = requestId;
  req.startTime = Date.now();
  res.setHeader('X-Request-Id', requestId);

  const origin = req.headers?.origin;

  if (origin && (ALLOWED_ORIGINS.includes(origin) || origin.endsWith('.vercel.app'))) {
    res.setHeader('Access-Control-Allow-Origin', origin);
  } else {
    res.setHeader('Access-Control-Allow-Origin', 'https://tdlxht.vercel.app');
  }

  res.setHeader('Vary', 'Origin');
  res.setHeader('Access-Control-Allow-Methods', allowedMethods.join(', '));
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With, X-Request-Id');
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
export function sendSuccess(res, data, statusCode = 200, req = null) {
  const requestId = req?.requestId || (typeof res.getHeader === 'function' ? res.getHeader('X-Request-Id') : undefined);
  const durationMs = req?.startTime ? Date.now() - req.startTime : undefined;

  if (durationMs !== undefined && typeof res.setHeader === 'function') {
    res.setHeader('Server-Timing', `total;dur=${durationMs}`);
  }

  return res.status(statusCode).json({
    success: true,
    requestId,
    timestamp: new Date().toISOString(),
    ...(durationMs !== undefined ? { latency_ms: durationMs } : {}),
    ...data
  });
}

/**
 * Standardized error response envelope
 */
export function sendError(res, error, req = null) {
  const statusCode = error.statusCode || 500;
  const message = error.message || 'An unexpected internal error occurred';
  const code = error.code || 'INTERNAL_SERVER_ERROR';
  const requestId = req?.requestId || (typeof res.getHeader === 'function' ? res.getHeader('X-Request-Id') : undefined);
  const durationMs = req?.startTime ? Date.now() - req.startTime : undefined;

  if (statusCode >= 500) {
    console.error(`[API Error][${requestId || 'no-id'}] ${code}:`, error);
  }

  return res.status(statusCode).json({
    success: false,
    requestId,
    timestamp: new Date().toISOString(),
    error: message,
    code,
    ...(durationMs !== undefined ? { latency_ms: durationMs } : {}),
    ...(error.details ? { details: error.details } : {})
  });
}

