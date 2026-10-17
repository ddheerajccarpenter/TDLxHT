/**
 * TDlxHT Supabase Client Configuration
 * Bundled via Vite for reliable, offline-ready authentication without CDN race conditions.
 */
import { createClient } from '@supabase/supabase-js';

export const SUPABASE_URL = "https://mtlckvkyimoqbrlvirch.supabase.co";
export const SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im10bGNrdmt5aW1vcWJybHZpcmNoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc5Njc0MTEsImV4cCI6MjEwMzU0MzQxMX0.gGxCER-GAzSVS6IMzKoy5p8NwuDbOZ2QiBzkEeG_-J4";

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
