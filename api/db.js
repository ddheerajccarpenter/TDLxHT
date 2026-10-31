// Neon Serverless Postgres Client
// Best practices from backend-patterns & nodejs-backend-patterns:
// - Connection pooling via Neon HTTP driver (@neondatabase/serverless)
// - Serverless-friendly stateless execution (zero connection leakage)
// - Structured error handling with graceful fallback

import { neon } from '@neondatabase/serverless';

function getDatabaseUrl() {
  return (
    process.env.DATABASE_URL ||
    process.env.NEON_DATABASE_URL ||
    process.env.POSTGRES_URL ||
    ''
  );
}

export function getDbClient() {
  const dbUrl = getDatabaseUrl();
  if (!dbUrl) {
    return null;
  }
  return neon(dbUrl);
}

/**
 * Health probe query for Neon Postgres
 */
export async function queryNeonHealth() {
  const sql = getDbClient();
  if (!sql) {
    return {
      connected: false,
      reason: 'DATABASE_URL not configured'
    };
  }

  const startTime = Date.now();
  try {
    const result = await sql`SELECT 1 AS alive, NOW() AS server_time, version() AS pg_version`;
    const latencyMs = Date.now() - startTime;
    return {
      connected: true,
      latencyMs,
      data: result[0]
    };
  } catch (error) {
    return {
      connected: false,
      error: error.message || 'Neon query failed'
    };
  }
}

