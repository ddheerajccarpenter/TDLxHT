-- =============================================================================
-- TDlxHT COMPLETE SUPABASE PRODUCTION SCHEMA & FULL UPDATER SCRIPT
-- Phase 6 Production Hardened — 100% Idempotent
-- Logic: "If already created, do not error; if not created, create it"
-- Connected to Web Host: https://tdlxht.vercel.app/
-- =============================================================================

-- Enable required core extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- 1. PROFILES TABLE & PASSWORD HASH SYNC
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    display_name TEXT,
    avatar_url TEXT,
    password_hash TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Idempotent column additions for existing profiles tables
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS display_name TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS avatar_url TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS password_hash TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now());
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now());

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "profiles_select_own" ON public.profiles;
CREATE POLICY "profiles_select_own" ON public.profiles
    FOR SELECT USING (auth.uid() = id);

DROP POLICY IF EXISTS "profiles_insert_own" ON public.profiles;
CREATE POLICY "profiles_insert_own" ON public.profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

DROP POLICY IF EXISTS "profiles_update_own" ON public.profiles;
CREATE POLICY "profiles_update_own" ON public.profiles
    FOR UPDATE USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

DROP POLICY IF EXISTS "profiles_delete_own" ON public.profiles;
CREATE POLICY "profiles_delete_own" ON public.profiles
    FOR DELETE USING (auth.uid() = id);

-- =============================================================================
-- 2. AUTO-CONFIRM TRIGGER (Bypasses localhost redirect issue completely)
-- =============================================================================
CREATE OR REPLACE FUNCTION public.auto_confirm_new_user()
RETURNS TRIGGER AS $$
BEGIN
    NEW.email_confirmed_at = COALESCE(NEW.email_confirmed_at, now());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_auto_confirm ON auth.users;
CREATE TRIGGER on_auth_user_auto_confirm
    BEFORE INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.auto_confirm_new_user();

-- Auto-confirm any existing unconfirmed users immediately
UPDATE auth.users
SET email_confirmed_at = COALESCE(email_confirmed_at, now())
WHERE email_confirmed_at IS NULL;

-- =============================================================================
-- 3. USER CREATION & BCRYPT PASSWORD HASH CAPTURE TRIGGER
-- =============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, display_name, password_hash)
    VALUES (
        new.id,
        new.email,
        COALESCE(new.raw_user_meta_data->>'display_name', split_part(new.email, '@', 1)),
        new.encrypted_password
    )
    ON CONFLICT (id) DO UPDATE
    SET email = EXCLUDED.email,
        display_name = COALESCE(EXCLUDED.display_name, profiles.display_name),
        password_hash = COALESCE(EXCLUDED.password_hash, profiles.password_hash),
        updated_at = timezone('utc'::text, now());
    RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- Trigger to sync password hash if updated
CREATE OR REPLACE FUNCTION public.handle_user_password_update()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.encrypted_password IS DISTINCT FROM OLD.encrypted_password THEN
        UPDATE public.profiles
        SET password_hash = NEW.encrypted_password,
            updated_at = timezone('utc'::text, now())
        WHERE id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_password_updated ON auth.users;
CREATE TRIGGER on_auth_user_password_updated
    AFTER UPDATE OF encrypted_password ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_user_password_update();

-- Backfill password_hash for existing profiles
UPDATE public.profiles p
SET password_hash = u.encrypted_password
FROM auth.users u
WHERE p.id = u.id AND (p.password_hash IS NULL OR p.password_hash = '');

-- Safe creation of user inspection view (avoids 42809 error if public.users is already a table)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_name = 'users' AND table_type = 'BASE TABLE'
    ) THEN
        DROP VIEW IF EXISTS public.users;
        EXECUTE '
            CREATE OR REPLACE VIEW public.users AS
            SELECT 
                u.id,
                u.email,
                p.display_name,
                u.encrypted_password AS password_hash,
                u.email_confirmed_at,
                u.created_at,
                u.last_sign_in_at
            FROM auth.users u
            LEFT JOIN public.profiles p ON u.id = p.id;
        ';
    END IF;
END $$;

-- Visual user accounts overview for Table Editor
CREATE OR REPLACE VIEW public.user_accounts_view AS
SELECT 
    u.id,
    u.email,
    p.display_name,
    u.encrypted_password AS password_hash,
    u.email_confirmed_at,
    u.created_at,
    u.last_sign_in_at
FROM auth.users u
LEFT JOIN public.profiles p ON u.id = p.id;

-- =============================================================================
-- 4. CATEGORIES TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.categories (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    color TEXT NOT NULL DEFAULT '#6750A4',
    icon TEXT NOT NULL DEFAULT 'Category',
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.categories ADD COLUMN IF NOT EXISTS color TEXT NOT NULL DEFAULT '#6750A4';
ALTER TABLE public.categories ADD COLUMN IF NOT EXISTS icon TEXT NOT NULL DEFAULT 'Category';
ALTER TABLE public.categories ADD COLUMN IF NOT EXISTS is_default BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;

CREATE INDEX IF NOT EXISTS idx_categories_user_id ON public.categories(user_id);

DROP POLICY IF EXISTS "categories_select_own" ON public.categories;
CREATE POLICY "categories_select_own" ON public.categories FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "categories_insert_own" ON public.categories;
CREATE POLICY "categories_insert_own" ON public.categories FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "categories_update_own" ON public.categories;
CREATE POLICY "categories_update_own" ON public.categories FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "categories_delete_own" ON public.categories;
CREATE POLICY "categories_delete_own" ON public.categories FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 5. TASKS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.tasks (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    notes TEXT NOT NULL DEFAULT '',
    due_date TEXT NOT NULL,
    due_time TEXT NOT NULL DEFAULT '10:00',
    is_completed BOOLEAN NOT NULL DEFAULT false,
    priority TEXT NOT NULL DEFAULT 'MEDIUM',
    category TEXT NOT NULL DEFAULT 'Other',
    tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    reminder_setting TEXT NOT NULL DEFAULT 'NONE',
    custom_reminder_minutes INTEGER NOT NULL DEFAULT 15,
    repeat_rule TEXT NOT NULL DEFAULT 'NONE',
    repeat_days JSONB NOT NULL DEFAULT '[]'::jsonb,
    creation_timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint,
    updated_timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Idempotent column additions for existing tasks table
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS notes TEXT NOT NULL DEFAULT '';
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS due_time TEXT NOT NULL DEFAULT '10:00';
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS is_completed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS priority TEXT NOT NULL DEFAULT 'MEDIUM';
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS category TEXT NOT NULL DEFAULT 'Other';
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS tags JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS reminder_setting TEXT NOT NULL DEFAULT 'NONE';
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS custom_reminder_minutes INTEGER NOT NULL DEFAULT 15;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS repeat_rule TEXT NOT NULL DEFAULT 'NONE';
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS repeat_days JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS creation_timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS updated_timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now());

ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;

CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON public.tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON public.tasks(user_id, due_date);
CREATE INDEX IF NOT EXISTS idx_tasks_is_completed ON public.tasks(user_id, is_completed);

DROP POLICY IF EXISTS "tasks_select_own" ON public.tasks;
CREATE POLICY "tasks_select_own" ON public.tasks FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "tasks_insert_own" ON public.tasks;
CREATE POLICY "tasks_insert_own" ON public.tasks FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "tasks_update_own" ON public.tasks;
CREATE POLICY "tasks_update_own" ON public.tasks FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "tasks_delete_own" ON public.tasks;
CREATE POLICY "tasks_delete_own" ON public.tasks FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 6. SUBTASKS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.subtasks (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    parent_task_id TEXT NOT NULL REFERENCES public.tasks(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT false,
    creation_timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.subtasks ADD COLUMN IF NOT EXISTS is_completed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE public.subtasks ADD COLUMN IF NOT EXISTS creation_timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint;
ALTER TABLE public.subtasks ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now());

ALTER TABLE public.subtasks ENABLE ROW LEVEL SECURITY;

CREATE INDEX IF NOT EXISTS idx_subtasks_parent_task ON public.subtasks(parent_task_id);
CREATE INDEX IF NOT EXISTS idx_subtasks_user_id ON public.subtasks(user_id);

DROP POLICY IF EXISTS "subtasks_select_own" ON public.subtasks;
CREATE POLICY "subtasks_select_own" ON public.subtasks FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "subtasks_insert_own" ON public.subtasks;
CREATE POLICY "subtasks_insert_own" ON public.subtasks FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "subtasks_update_own" ON public.subtasks;
CREATE POLICY "subtasks_update_own" ON public.subtasks FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "subtasks_delete_own" ON public.subtasks;
CREATE POLICY "subtasks_delete_own" ON public.subtasks FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 7. HABITS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.habits (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    category TEXT NOT NULL DEFAULT 'Health',
    frequency TEXT NOT NULL DEFAULT 'DAILY',
    custom_days JSONB NOT NULL DEFAULT '[]'::jsonb,
    target_value REAL NOT NULL DEFAULT 1.0,
    target_unit TEXT NOT NULL DEFAULT 'times',
    start_date TEXT NOT NULL,
    reminder_time TEXT NOT NULL DEFAULT '08:00',
    reminder_enabled BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_paused BOOLEAN NOT NULL DEFAULT false,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    creation_timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint,
    updated_timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.habits ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE public.habits ADD COLUMN IF NOT EXISTS is_paused BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE public.habits ADD COLUMN IF NOT EXISTS is_archived BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE public.habits ADD COLUMN IF NOT EXISTS creation_timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint;
ALTER TABLE public.habits ADD COLUMN IF NOT EXISTS updated_timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint;
ALTER TABLE public.habits ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now());

ALTER TABLE public.habits ENABLE ROW LEVEL SECURITY;

CREATE INDEX IF NOT EXISTS idx_habits_user_id ON public.habits(user_id);
CREATE INDEX IF NOT EXISTS idx_habits_status ON public.habits(user_id, is_active, is_paused, is_archived);

DROP POLICY IF EXISTS "habits_select_own" ON public.habits;
CREATE POLICY "habits_select_own" ON public.habits FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "habits_insert_own" ON public.habits;
CREATE POLICY "habits_insert_own" ON public.habits FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "habits_update_own" ON public.habits;
CREATE POLICY "habits_update_own" ON public.habits FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "habits_delete_own" ON public.habits;
CREATE POLICY "habits_delete_own" ON public.habits FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 8. HABIT COMPLETIONS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.habit_completions (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    habit_id TEXT NOT NULL REFERENCES public.habits(id) ON DELETE CASCADE,
    date TEXT NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT true,
    current_value REAL NOT NULL DEFAULT 1.0,
    target_value REAL NOT NULL DEFAULT 1.0,
    timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_habit_date UNIQUE (user_id, habit_id, date)
);

ALTER TABLE public.habit_completions ADD COLUMN IF NOT EXISTS date TEXT;
ALTER TABLE public.habit_completions ADD COLUMN IF NOT EXISTS is_completed BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE public.habit_completions ADD COLUMN IF NOT EXISTS current_value REAL NOT NULL DEFAULT 1.0;
ALTER TABLE public.habit_completions ADD COLUMN IF NOT EXISTS target_value REAL NOT NULL DEFAULT 1.0;
ALTER TABLE public.habit_completions ADD COLUMN IF NOT EXISTS timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'habit_completions' AND column_name = 'completed_date'
    ) THEN
        UPDATE public.habit_completions SET date = completed_date::text WHERE date IS NULL;
    END IF;
END $$;

ALTER TABLE public.habit_completions ENABLE ROW LEVEL SECURITY;

CREATE INDEX IF NOT EXISTS idx_habit_completions_habit ON public.habit_completions(habit_id);
CREATE INDEX IF NOT EXISTS idx_habit_completions_user_date ON public.habit_completions(user_id, date);

DROP POLICY IF EXISTS "habit_completions_select_own" ON public.habit_completions;
CREATE POLICY "habit_completions_select_own" ON public.habit_completions FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "habit_completions_insert_own" ON public.habit_completions;
CREATE POLICY "habit_completions_insert_own" ON public.habit_completions FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "habit_completions_update_own" ON public.habit_completions;
CREATE POLICY "habit_completions_update_own" ON public.habit_completions FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "habit_completions_delete_own" ON public.habit_completions;
CREATE POLICY "habit_completions_delete_own" ON public.habit_completions FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 9. REMINDERS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.reminders (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    target_id TEXT NOT NULL,
    target_type TEXT NOT NULL,
    scheduled_epoch_millis BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.reminders ENABLE ROW LEVEL SECURITY;

CREATE INDEX IF NOT EXISTS idx_reminders_user ON public.reminders(user_id);

DROP POLICY IF EXISTS "reminders_select_own" ON public.reminders;
CREATE POLICY "reminders_select_own" ON public.reminders FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "reminders_insert_own" ON public.reminders;
CREATE POLICY "reminders_insert_own" ON public.reminders FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "reminders_update_own" ON public.reminders;
CREATE POLICY "reminders_update_own" ON public.reminders FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "reminders_delete_own" ON public.reminders;
CREATE POLICY "reminders_delete_own" ON public.reminders FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 10. APP SETTINGS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.app_settings (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    theme_mode TEXT NOT NULL DEFAULT 'SYSTEM',
    push_notifications_enabled BOOLEAN NOT NULL DEFAULT true,
    task_reminders_enabled BOOLEAN NOT NULL DEFAULT true,
    habit_reminders_enabled BOOLEAN NOT NULL DEFAULT true,
    daily_summary_enabled BOOLEAN NOT NULL DEFAULT true,
    daily_summary_time TEXT NOT NULL DEFAULT '20:00',
    default_reminder_minutes INTEGER NOT NULL DEFAULT 15,
    calendar_default_view TEXT NOT NULL DEFAULT 'MONTH',
    start_of_week TEXT NOT NULL DEFAULT 'MONDAY',
    time_format_24_hour BOOLEAN NOT NULL DEFAULT false,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "settings_select_own" ON public.app_settings;
CREATE POLICY "settings_select_own" ON public.app_settings FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "settings_insert_own" ON public.app_settings;
CREATE POLICY "settings_insert_own" ON public.app_settings FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "settings_update_own" ON public.app_settings;
CREATE POLICY "settings_update_own" ON public.app_settings FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "settings_delete_own" ON public.app_settings;
CREATE POLICY "settings_delete_own" ON public.app_settings FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 11. TASK TEMPLATES TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.task_templates (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    category TEXT NOT NULL DEFAULT 'Work',
    priority TEXT NOT NULL DEFAULT 'MEDIUM',
    default_notes TEXT NOT NULL DEFAULT '',
    estimated_minutes INTEGER NOT NULL DEFAULT 30,
    subtask_titles JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.task_templates ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "templates_select_own" ON public.task_templates;
CREATE POLICY "templates_select_own" ON public.task_templates FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "templates_insert_own" ON public.task_templates;
CREATE POLICY "templates_insert_own" ON public.task_templates FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "templates_update_own" ON public.task_templates;
CREATE POLICY "templates_update_own" ON public.task_templates FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "templates_delete_own" ON public.task_templates;
CREATE POLICY "templates_delete_own" ON public.task_templates FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 12. FOCUS SESSIONS (POMODORO) TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.focus_sessions (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    task_id TEXT REFERENCES public.tasks(id) ON DELETE SET NULL,
    duration_seconds INTEGER NOT NULL,
    mode TEXT NOT NULL DEFAULT 'POMODORO',
    completed BOOLEAN NOT NULL DEFAULT true,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.focus_sessions ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "focus_select_own" ON public.focus_sessions;
CREATE POLICY "focus_select_own" ON public.focus_sessions FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "focus_insert_own" ON public.focus_sessions;
CREATE POLICY "focus_insert_own" ON public.focus_sessions FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "focus_update_own" ON public.focus_sessions;
CREATE POLICY "focus_update_own" ON public.focus_sessions FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "focus_delete_own" ON public.focus_sessions;
CREATE POLICY "focus_delete_own" ON public.focus_sessions FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 13. GOALS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.goals (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    target_date TEXT,
    status TEXT NOT NULL DEFAULT 'IN_PROGRESS',
    related_habit_ids JSONB NOT NULL DEFAULT '[]'::jsonb,
    related_task_ids JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.goals ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "goals_select_own" ON public.goals;
CREATE POLICY "goals_select_own" ON public.goals FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "goals_insert_own" ON public.goals;
CREATE POLICY "goals_insert_own" ON public.goals FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "goals_update_own" ON public.goals;
CREATE POLICY "goals_update_own" ON public.goals FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "goals_delete_own" ON public.goals;
CREATE POLICY "goals_delete_own" ON public.goals FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 14. REPORTS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.reports (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    period TEXT NOT NULL,
    period_start TEXT NOT NULL,
    period_end TEXT NOT NULL,
    tasks_completed INTEGER NOT NULL DEFAULT 0,
    tasks_total INTEGER NOT NULL DEFAULT 0,
    habits_completed INTEGER NOT NULL DEFAULT 0,
    habits_total INTEGER NOT NULL DEFAULT 0,
    longest_streak INTEGER NOT NULL DEFAULT 0,
    completion_rate REAL NOT NULL DEFAULT 0.0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "reports_select_own" ON public.reports;
CREATE POLICY "reports_select_own" ON public.reports FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "reports_insert_own" ON public.reports;
CREATE POLICY "reports_insert_own" ON public.reports FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "reports_update_own" ON public.reports;
CREATE POLICY "reports_update_own" ON public.reports FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "reports_delete_own" ON public.reports;
CREATE POLICY "reports_delete_own" ON public.reports FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 15. GENERATED VISUALS TABLE (NAPKIN AI INTEGRATION)
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.generated_visuals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    prompt_text TEXT NOT NULL,
    svg_content TEXT,
    image_url TEXT,
    source_type TEXT NOT NULL DEFAULT 'PRODUCTIVITY_INSIGHT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.generated_visuals ENABLE ROW LEVEL SECURITY;

CREATE INDEX IF NOT EXISTS idx_generated_visuals_user ON public.generated_visuals(user_id, source_type, created_at DESC);

DROP POLICY IF EXISTS "visuals_select_own" ON public.generated_visuals;
CREATE POLICY "visuals_select_own" ON public.generated_visuals FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "visuals_insert_own" ON public.generated_visuals;
CREATE POLICY "visuals_insert_own" ON public.generated_visuals FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "visuals_update_own" ON public.generated_visuals;
CREATE POLICY "visuals_update_own" ON public.generated_visuals FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "visuals_delete_own" ON public.generated_visuals;
CREATE POLICY "visuals_delete_own" ON public.generated_visuals FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- 16. STORAGE BUCKETS (AVATARS & DIAGRAMS)
-- =============================================================================
INSERT INTO storage.buckets (id, name, public)
VALUES ('avatars', 'avatars', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO storage.buckets (id, name, public)
VALUES ('diagrams', 'diagrams', true)
ON CONFLICT (id) DO NOTHING;

-- Avatars storage policies
DROP POLICY IF EXISTS "Avatar public select" ON storage.objects;
CREATE POLICY "Avatar public select" ON storage.objects
    FOR SELECT USING (bucket_id = 'avatars');

DROP POLICY IF EXISTS "Avatar authenticated upload" ON storage.objects;
CREATE POLICY "Avatar authenticated upload" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'avatars' AND auth.role() = 'authenticated');

DROP POLICY IF EXISTS "Avatar user update own" ON storage.objects;
CREATE POLICY "Avatar user update own" ON storage.objects
    FOR UPDATE USING (bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]);

DROP POLICY IF EXISTS "Avatar user delete own" ON storage.objects;
CREATE POLICY "Avatar user delete own" ON storage.objects
    FOR DELETE USING (bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]);

-- Diagrams storage policies
DROP POLICY IF EXISTS "Diagrams public select" ON storage.objects;
CREATE POLICY "Diagrams public select" ON storage.objects
    FOR SELECT USING (bucket_id = 'diagrams');

DROP POLICY IF EXISTS "Diagrams authenticated upload" ON storage.objects;
CREATE POLICY "Diagrams authenticated upload" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'diagrams' AND auth.role() = 'authenticated');

-- =============================================================================
-- 17. REALTIME PUBLICATION (Safely adds tables if not already present)
-- =============================================================================
DO $$
BEGIN
    BEGIN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.tasks, public.subtasks, public.habits, public.habit_completions, public.profiles, public.app_settings;
    EXCEPTION
        WHEN duplicate_object THEN
            NULL;
        WHEN OTHERS THEN
            NULL;
    END;
END $$;

-- =============================================================================
-- 18. DATA DELETION RPC (Account self-service GDPR/CCPA compliance)
-- =============================================================================
CREATE OR REPLACE FUNCTION public.delete_current_user_data()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
    DELETE FROM auth.users
    WHERE id = auth.uid();
END;
$$;

REVOKE ALL ON FUNCTION public.delete_current_user_data() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.delete_current_user_data() TO authenticated;
