// Lightweight Sliding-Window In-Memory Rate Limiter for Vercel Serverless Functions
// Protects backend endpoints against brute-force attacks, credential stuffing, and volumetric spam.

const clients = new Map();

/**
 * Checks rate limit for the incoming request based on client IP.
 * @param {object} req - HTTP request object
 * @param {object} res - HTTP response object
 * @param {object} options - Options { limit: number, windowMs: number }
 * @returns {boolean} true if request is allowed, false if rate limited (429 sent)
 */
export function checkRateLimit(req, res, { limit = 10, windowMs = 60 * 1000 } = {}) {
  const forwarded = req.headers['x-forwarded-for'];
  const rawIp = forwarded ? forwarded.split(',')[0].trim() : (req.socket?.remoteAddress || '127.0.0.1');
  const ip = rawIp.replace(/[^a-zA-Z0-9.:_-]/g, '').slice(0, 45) || '127.0.0.1';

  const now = Date.now();
  const entry = clients.get(ip) || { count: 0, resetTime: now + windowMs };

  // Reset window if expired
  if (now > entry.resetTime) {
    entry.count = 1;
    entry.resetTime = now + windowMs;
  } else {
    entry.count += 1;
  }

  clients.set(ip, entry);

  // Periodic pruning of stale records to prevent memory leak
  if (clients.size > 2000) {
    for (const [key, val] of clients.entries()) {
      if (now > val.resetTime) {
        clients.delete(key);
      }
    }
  }

  if (entry.count > limit) {
    const retrySec = Math.max(1, Math.ceil((entry.resetTime - now) / 1000));
    res.setHeader('Retry-After', retrySec.toString());
    res.setHeader('X-RateLimit-Limit', limit.toString());
    res.setHeader('X-RateLimit-Remaining', '0');
    res.setHeader('X-RateLimit-Reset', Math.ceil(entry.resetTime / 1000).toString());
    res.status(429).json({
      error: 'Too many requests. Please slow down and try again later.',
      retryAfterSeconds: retrySec
    });
    return false;
  }

  res.setHeader('X-RateLimit-Limit', limit.toString());
  res.setHeader('X-RateLimit-Remaining', Math.max(0, limit - entry.count).toString());
  res.setHeader('X-RateLimit-Reset', Math.ceil(entry.resetTime / 1000).toString());
  return true;
}
