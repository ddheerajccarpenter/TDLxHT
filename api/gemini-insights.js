// Vercel Serverless Function: Gemini Productivity Intelligence Engine
// Follows nodejs-backend-patterns & backend-patterns:
// - Boundary input sanitization with bounded numeric coercion
// - Origin validation, rate limiting, and zero client key exposure
// - Deterministic offline rule fallback engine

import { checkRateLimit } from './_rateLimit.js';
import { applySecurityHeaders, sendSuccess, sendError, AppError } from './_middleware.js';

export default async function handler(req, res) {
  if (applySecurityHeaders(req, res, { allowedMethods: ['POST', 'OPTIONS'] })) {
    return;
  }

  if (req.method !== 'POST') {
    return sendError(res, new AppError('Method not allowed. Use POST.', 405, 'METHOD_NOT_ALLOWED'));
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
    return sendSuccess(res, {
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
    return sendSuccess(res, {
      source: 'rule_engine_fallback',
      insight: generateRuleBasedInsight()
    });
  }

  try {
    const prompt = `You are the productivity intelligence engine inside TDlxHT.
Analyze these user metrics:
- Total Tasks: ${totalTasks}
- Completed Tasks: ${completedTasks}
- Overdue Tasks: ${overdueTasks}
- Active Habits: ${activeHabits}
- Habits Completed Today: ${habitsCompletedToday}
- Longest Streak: ${longestStreak} days
- Productivity Score: ${productivityScore}/100

Respond ONLY with valid JSON (no markdown formatting, no code fences):
{
  "insightType": "momentum | warning | celebration | tip",
  "title": "Short punchy title (max 40 chars)",
  "message": "Actionable, direct advice in 1-2 sentences (max 140 chars). No filler.",
  "priority": "LOW | MEDIUM | HIGH",
  "actionSuggestion": "A concrete single action they should take right now (max 50 chars)",
  "confidence": 0.95
}`;

    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${geminiApiKey}`;

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 6000);

    const apiRes = await fetch(geminiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }],
        generationConfig: {
          temperature: 0.2,
          maxOutputTokens: 250,
          responseMimeType: "application/json"
        }
      }),
      signal: controller.signal
    });

    clearTimeout(timeout);

    if (!apiRes.ok) {
      throw new Error(`Gemini API returned status ${apiRes.status}`);
    }

    const data = await apiRes.json();
    const rawText = data.candidates?.[0]?.content?.parts?.[0]?.text;

    if (!rawText) {
      throw new Error('Empty response from Gemini');
    }

    const cleanJson = rawText.replace(/```json/gi, '').replace(/```/gi, '').trim();
    const parsed = JSON.parse(cleanJson);

    return sendSuccess(res, {
      source: 'gemini_ai',
      insight: {
        insightType: parsed.insightType || 'tip',
        title: parsed.title || 'Productivity Insight',
        message: parsed.message || 'Keep moving forward on your daily priorities.',
        priority: parsed.priority || 'LOW',
        actionSuggestion: parsed.actionSuggestion || 'Check your tasks',
        confidence: typeof parsed.confidence === 'number' ? parsed.confidence : 0.9
      }
    });

  } catch (err) {
    console.warn('[Gemini Insights] Falling back to rule engine:', err.message);
    return sendSuccess(res, {
      source: 'rule_engine_fallback',
      insight: generateRuleBasedInsight()
    });
  }
}
