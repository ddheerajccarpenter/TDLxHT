/**
 * TDlxHT Supabase Client Configuration
 * Client runtime initialization for Auth & Deep Linking.
 * Sensitive service keys are strictly kept server-side in Vercel Serverless Functions (/api).
 */
(function() {
  const CONFIG = {
    supabaseUrl: "https://mtlckvkyimoqbrlvirch.supabase.co",
    // Public anon JWT: Safe for client authentication when RLS is enforced
    supabaseAnonKey: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im10bGNrdmt5aW1vcWJybHZpcmNoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc5Njc0MTEsImV4cCI6MjEwMzU0MzQxMX0.gGxCER-GAzSVS6IMzKoy5p8NwuDbOZ2QiBzkEeG_-J4",
    appScheme: "tdlxht",
    webUrl: window.location.origin || "https://tdlxht.vercel.app"
  };

  let supabaseInstance = null;
  function getSupabaseClient() {
    if (supabaseInstance) return supabaseInstance;
    if (window.supabase && typeof window.supabase.createClient === "function") {
      try {
        supabaseInstance = window.supabase.createClient(
          CONFIG.supabaseUrl,
          CONFIG.supabaseAnonKey,
          {
            auth: {
              persistSession: true,
              autoRefreshToken: true,
              detectSessionInUrl: true,
              flowType: "implicit"
            }
          }
        );
      } catch (err) {
        console.error("Failed to initialize Supabase client:", err);
      }
    }
    return supabaseInstance;
  }

  window.TDLXHT_CONFIG = {
    ...CONFIG,
    getClient: getSupabaseClient
  };
})();
