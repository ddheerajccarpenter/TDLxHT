// Vercel Serverless Function: Gemini Productivity Intelligence Engine
// Secure server-side processing with zero client key exposure, strict rate limiting, and data minimization.

import { checkRateLimit } from './_rateLimit.js';

export default async function handler(req, res) {
  // CORS & Security headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed. Use POST.' });
  }

  // Rate limit: 30 requests / minute / IP
  if (!checkRateLimit(req, res, { limit: 30, windowMs: 60 * 1000 })) {
    return;
  }

  const raw = req.body || {};
  // Sanitize and coerce to safe bounded non-negative integers
  const toSafeInt = (v, max = 50000) => Math.max(0, Math.min(max, Math.round(Number(v) || 0)));

  const totalTasks = toSafeInt(raw.totalTasks);
  const completedTasks = toSafeInt(raw.completedTasks);
  const overdueTasks = toSafeInt(raw.overdueTasks);
  const activeHabits = toSafeInt(raw.activeHabits);
  const habitsCompletedToday = toSafeInt(raw.habitsCompletedToday);
  const longestStreak = toSafeInt(raw.longestStreak);
  const productivityScore = toSafeInt(raw.productivityScore, 100);

  // For a brand new user with 0 tasks and 0 habits, return clean onboarding guidance
  if (totalTasks === 0 && activeHabits === 0) {
    return res.status(200).json({
      source: 'rule_engine',
      insight: {
        insightType: 'onboarding',
        title: 'Welcome to TDlxHT 👋',
        message: 'Create your first task or habit to begin tracking your daily momentum and unlock personalized insights.',
        priority: 'LOW',
        actionSuggestion: 'Add your first task or habit',
        confidence: 1.0
      }
    });
  }

  // Rule-based deterministic insight fallback generator
  function generateRuleBasedInsight() {
    if (overdueTasks >= 3) {
      return {
        insightType: 'task_overload',
        title: 'Task Overload Detected',
        message: `You have ${overdueTasks} overdue tasks pending. Reschedule low-priority tasks to restore momentum.`,
        priority: 'HIGH',
        actionSuggestion: 'Review and defer non-critical tasks',
        confidence: 0.95
      };
    }

    if (longestStreak >= 7 && habitsCompletedToday === activeHabits && activeHabits > 0) {
      return {
        insightType: 'streak_milestone',
        title: 'Outstanding Consistency! 🏆',
        message: `You've maintained your streak for ${longestStreak} straight days and completed all habits today.`,
        priority: 'MEDIUM',
        actionSuggestion: 'Keep the streak alive tomorrow',
        confidence: 0.98
      };
    }

    if (activeHabits > 0 && habitsCompletedToday === 0) {
      return {
        insightType: 'missed_habits',
        title: 'Daily Habits Waiting',
        message: `You haven't completed any of your ${activeHabits} active habits yet today. Starting small with 1 habit builds momentum.`,
        priority: 'MEDIUM',
        actionSuggestion: 'Complete your quickest habit now',
        confidence: 0.90
      };
    }

    const completionRate = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
    return {
      insightType: 'daily_momentum',
      title: 'Daily Progress Summary',
      message: `You have completed ${completedTasks} of ${totalTasks} tasks (${completionRate}%) and ${habitsCompletedToday} habits today.`,
      priority: 'LOW',
      actionSuggestion: 'Review tomorrow\'s planned agenda',
      confidence: 0.88
    };
  }

  const geminiApiKey = process.env.GEMINI_API_KEY || process.env.GENIMI_API_KEY;

  if (!geminiApiKey) {
    // Return high-quality deterministic rule response if no API key is configured
    return res.status(200).json({
      source: 'rule_engine',
      insight: generateRuleBasedInsight()
    });
  }

  try {
    const prompt = `You are a concise, personal productivity advisor. Analyze these aggregate metrics:
- Total Tasks: ${totalTasks}
- Completed Tasks: ${completedTasks}
- Overdue Tasks: ${overdueTasks}
- Active Habits: ${activeHabits}
- Habits Completed Today: ${habitsCompletedToday}
- Longest Active Streak: ${longestStreak} days
- Productivity Score: ${productivityScore}%

Provide 1 actionable, calm, encouraging productivity insight.
Respond strictly in JSON matching this schema:
{
  "insightType": "task_overload" | "missed_habits" | "streak_milestone" | "daily_momentum",
  "title": string (max 40 chars),
  "message": string (1-2 sentences max),
  "priority": "HIGH" | "MEDIUM" | "LOW",
  "actionSuggestion": string (max 45 chars),
  "confidence": number (0.0 to 1.0)
}`;

    const geminiRes = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${geminiApiKey}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{ parts: [{ text: prompt }] }],
          generationConfig: {
            temperature: 0.3,
            responseMimeType: 'application/json'
          }
        })
      }
    );

    if (!geminiRes.ok) {
      throw new Error(`Gemini API error: ${geminiRes.status}`);
    }

    const data = await geminiRes.json();
    const candidateText = data.candidates?.[0]?.content?.parts?.[0]?.text;
    const parsedInsight = JSON.parse(candidateText);

    return res.status(200).json({
      source: 'gemini',
      insight: parsedInsight
    });
  } catch (err) {
    // Fail-safe: Always return valid structured response
    return res.status(200).json({
      source: 'rule_fallback',
      insight: generateRuleBasedInsight()
    });
  }
}
