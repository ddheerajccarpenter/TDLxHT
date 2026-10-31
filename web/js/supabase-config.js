/**
 * TDlxHT Supabase Client Configuration
 * Bundled via Vite for reliable, offline-ready authentication without CDN race conditions.
 */
import { createClient } from '@supabase/supabase-js';

export const SUPABASE_URL = (typeof import.meta !== "undefined" && import.meta.env?.VITE_SUPABASE_URL) || "https://mtlckvkyimoqbrlvirch.supabase.co";
export const SUPABASE_ANON_KEY = (typeof import.meta !== "undefined" && import.meta.env?.VITE_SUPABASE_ANON_KEY) || "";

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
    detectSessionInUrl: true,
    flowType: "implicit"
  }
});

// Backward compatibility for scripts expecting window.TDLXHT_CONFIG
if (typeof window !== "undefined") {
  window.supabase = supabase;
  window.TDLXHT_CONFIG = {
    supabaseUrl: SUPABASE_URL,
    supabaseAnonKey: SUPABASE_ANON_KEY,
    appScheme: "tdlxht",
    webUrl: window.location.origin || "https://tdlxht.vercel.app",
    getClient: () => supabase
  };
}

export default supabase;
