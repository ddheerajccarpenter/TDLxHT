// deno-lint-ignore-file
// @ts-nocheck
// Supabase Edge Function: napkin-visual-generator
// Deploy: supabase functions deploy napkin-visual-generator
// Set secret: supabase secrets set NAPKIN_API_KEY=sk-776...

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.0";

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

declare const Deno: any;

Deno.serve(async (req: Request) => {
  // Handle CORS preflight
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: CORS_HEADERS });
  }

  // 1. Authenticate caller via Supabase JWT
  const authHeader = req.headers.get("Authorization");
  if (!authHeader) {
    return new Response(JSON.stringify({ error: "Missing authorization header" }), {
      status: 401,
      headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
    });
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
  const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
  const supabase = createClient(supabaseUrl, supabaseAnonKey, {
    global: { headers: { Authorization: authHeader } },
  });

  const { data: { user }, error: authError } = await supabase.auth.getUser();
  if (authError || !user) {
    return new Response(JSON.stringify({ error: "Unauthorized" }), {
      status: 401,
      headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
    });
  }

  const userId = user.id; // Trust server-verified identity ONLY

  // 2. Parse request body (aggregated stats only — no raw private data)
  let body: {
    period: "weekly" | "monthly";
    stats: {
      tasksCompleted: number;
      tasksTotal: number;
      habitsCompleted: number;
      habitsTotal: number;
      longestStreak: number;
      completionRate: number;
      topCategories: string[];
      periodLabel: string;
    };
    forceRegenerate?: boolean;
  };

  try {
    body = await req.json();
  } catch {
    return new Response(JSON.stringify({ error: "Invalid JSON body" }), {
      status: 400,
      headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
    });
  }

  if (!body.period || !body.stats) {
    return new Response(JSON.stringify({ error: "Missing period or stats" }), {
      status: 400,
      headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
    });
  }

  // 3. Check cache
  if (!body.forceRegenerate) {
    const cacheWindow = body.period === "weekly" ? 7 : 30;
    const since = new Date(Date.now() - cacheWindow * 24 * 60 * 60 * 1000).toISOString();

    const { data: cached } = await supabase
      .from("generated_visuals")
      .select("id, image_url, svg_content, created_at")
      .eq("user_id", userId)
      .eq("source_type", body.period === "weekly" ? "PRODUCTIVITY_INSIGHT" : "HABIT_SUMMARY")
      .gte("created_at", since)
      .order("created_at", { ascending: false })
      .limit(1)
      .single();

    if (cached?.image_url || cached?.svg_content) {
      return new Response(
        JSON.stringify({
          cached: true,
          imageUrl: cached.image_url,
          svgContent: cached.svg_content,
          generatedAt: cached.created_at,
        }),
        { headers: { ...CORS_HEADERS, "Content-Type": "application/json" } }
      );
    }
  }

  // 4. Build prompt from AGGREGATED STATS only
  const s = body.stats;
  const prompt = `Productivity ${body.period} report for ${s.periodLabel}:
- Tasks: ${s.tasksCompleted}/${s.tasksTotal} completed (${Math.round(s.completionRate * 100)}%)
- Habits: ${s.habitsCompleted}/${s.habitsTotal} completed
- Longest streak: ${s.longestStreak} days
- Top categories: ${s.topCategories.slice(0, 3).join(", ")}
Create a clean productivity dashboard visualization for this data.`;

  // 5. Call Napkin API
  const napkinKey = Deno.env.get("NAPKIN_API_KEY") || "sk-deff27b1093c44fc3a79b5d48cf3d3f470e2eaeca4baf9eb631f227455c88759";
  let napkinResult: { imageUrl?: string; svgContent?: string } = {};

  try {
    const submitRes = await fetch("https://api.napkin.ai/v1/visual", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${napkinKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        content: prompt,
        format: "png",
        color_mode: "dark",
        transparent_background: false,
      }),
    });

    if (submitRes.ok) {
      const submitData = await submitRes.json();
      const requestId = submitData.id;

      if (requestId) {
        for (let i = 0; i < 10; i++) {
          await new Promise((resolve) => setTimeout(resolve, 1500));
          const statusRes = await fetch(`https://api.napkin.ai/v1/visual/${requestId}/status`, {
            headers: { "Authorization": `Bearer ${napkinKey}` },
          });

          if (statusRes.ok) {
            const statusData = await statusRes.json();
            if (statusData.status === "completed" && statusData.generated_files?.length > 0) {
              napkinResult = {
                imageUrl: statusData.generated_files[0].url,
              };
              break;
            } else if (statusData.status === "failed") {
              break;
            }
          }
        }
      }
    }
  } catch (err) {
    console.error("Napkin fetch error:", err);
  }

  // 6. Cache result in Supabase
  if (napkinResult.imageUrl || napkinResult.svgContent) {
    await supabase.from("generated_visuals").insert({
      user_id: userId,
      prompt_text: prompt,
      svg_content: napkinResult.svgContent ?? null,
      image_url: napkinResult.imageUrl ?? null,
      source_type: body.period === "weekly" ? "PRODUCTIVITY_INSIGHT" : "HABIT_SUMMARY",
    });
  }

  return new Response(
    JSON.stringify({
      cached: false,
      imageUrl: napkinResult.imageUrl ?? null,
      svgContent: napkinResult.svgContent ?? null,
      generatedAt: new Date().toISOString(),
    }),
    { headers: { ...CORS_HEADERS, "Content-Type": "application/json" } }
  );
});
