# TDlxHT Master Technical Manual & Architecture Compendium (`technical.md`)

> **Single Source of Truth:** This document is the unified, authoritative technical, architectural, and operational engineering manual for **TDLxHT** (To-Do List × Habit Tracker).
> 
> **Merged Compendium of:** `ARCHITECTURE.md`, `app.md`, `memory.md`, `tracker.md`, `README.md`, and Expo/Universal client architecture.

---

## 📑 Table of Contents

1. [Executive Summary & System Architecture](#1-executive-summary--system-architecture)
   - 1.1 [Product Vision & Core Pillars](#11-product-vision--core-pillars)
   - 1.2 [Unidirectional Data Flow (UDF) Architecture](#12-unidirectional-data-flow-udf-architecture)
   - 1.3 [Technology Stack Matrix](#13-technology-stack-matrix)
   - 1.4 [Monorepo Directory Topology](#14-monorepo-directory-topology)
2. [Native Android Application Architecture (`app/`)](#2-native-android-application-architecture-app)
   - 2.1 [Application Lifecycle & Android Manifest](#21-application-lifecycle--android-manifest)
   - 2.2 [Jetpack Compose UI & Screen Matrix](#22-jetpack-compose-ui--screen-matrix)
   - 2.3 [State Management & ViewModels](#23-state-management--viewmodels)
   - 2.4 [Room SQLite Database, DAOs & Entities](#24-room-sqlite-database-daos--entities)
   - 2.5 [Repository Layer & Domain Mapping](#25-repository-layer--domain-mapping)
   - 2.6 [Background Scheduling, Alarms & Sensors](#26-background-scheduling-alarms--sensors)
   - 2.7 [AI Intelligence & Napkin Visual Engine](#27-ai-intelligence--napkin-visual-engine)
   - 2.8 [Gradle Build System, R8 Minification & Signing](#28-gradle-build-system-r8-minification--signing)
3. [Web Platform & Vercel Serverless Architecture (`web/` & `api/`)](#3-web-platform--vercel-serverless-architecture-web--api)
   - 3.1 [Vite Build Pipeline & Static Landing Hub](#31-vite-build-pipeline--static-landing-hub)
   - 3.2 [Serverless API Endpoints Matrix](#32-serverless-api-endpoints-matrix)
   - 3.3 [Correlation Tracing & Server-Timing Middleware](#33-correlation-tracing--server-timing-middleware)
   - 3.4 [Sliding-Window IP Rate Limiting](#34-sliding-window-ip-rate-limiting)
4. [Supabase Cloud Database & Security Architecture](#4-supabase-cloud-database--security-architecture)
   - 4.1 [Complete PostgreSQL Database Schema](#41-complete-postgresql-database-schema)
   - 4.2 [Row-Level Security (RLS) Isolation](#42-row-level-security-rls-isolation)
   - 4.3 [Zero-Leak Client Architecture](#43-zero-leak-client-architecture)
5. [Universal Mobile Client Architecture (`TDLxHT/`)](#5-universal-mobile-client-architecture-tdlxht)
   - 5.1 [Expo Router Architecture](#51-expo-router-architecture)
   - 5.2 [EAS Build Pipelines](#52-eas-build-pipelines)
6. [Build, Verification & Deployment Playbook](#6-build-verification--deployment-playbook)
   - 6.1 [Local Environment Setup](#61-local-environment-setup)
   - 6.2 [Automated Unit Test Suite](#62-automated-unit-test-suite)
   - 6.3 [Release APK Compilation (`syncReleaseApk`)](#63-release-apk-compilation-syncreleaseapk)
   - 6.4 [Web Companion Build & Vercel Deploy](#64-web-companion-build--vercel-deploy)
   - 6.5 [GitHub Release Assets Synchronization](#65-github-release-assets-synchronization)
   - 6.6 [One-Command Production Release Workflow](#66-one-command-production-release-workflow)
7. [Changelog & Technical Defect History](#7-changelog--technical-defect-history)
   - 7.1 [Resolution of 'App Not Installed' Error](#71-resolution-of-app-not-installed-error)
   - 7.2 [120fps Constant Layout Navigation Engine](#72-120fps-constant-layout-navigation-engine)
   - 7.3 [Insights Screen Viewport Overlap Fix](#73-insights-screen-viewport-overlap-fix)

---

## 1. Executive Summary & System Architecture

### 1.1 Product Vision & Core Pillars

**TDLxHT** (To-Do List × Habit Tracker) is an offline-first, native Android application and companion web platform engineered for focused execution.
- **Offline-First Persistence:** High-performance local data storage backed by Android Jetpack Room (SQLite) with microsecond query execution.
- **Dual-Streak Habit Engine:** Tracks both current consecutive day streaks and all-time personal best streaks.
- **14 Built-In Calculators:** Fully native offline mathematical utilities (Loan EMI, GST Tax, Attendance Planner, CGPA to Percentage, Unit Converter, etc.) requiring zero cloud round-trips.
- **Privacy-by-Design:** 100% of user data remains on device by default. Zero trackers, zero ads, zero telemetry.
- **Optional Cloud Synchronization:** Optional Supabase authentication gateway for users who explicitly choose to synchronize across devices.

### 1.2 Unidirectional Data Flow (UDF) Architecture

```
┌────────────────────────────────────────────────────────┐
│             Jetpack Compose UI Layer                    │
│  (Home, Productivity, Tools, Insights, Settings)        │
└──────────────────────────▲─────────────────────────────┘
                           │ StateFlow / UI Events
┌──────────────────────────┴─────────────────────────────┐
│                   ViewModels                           │
│  (MainViewModel, AuthViewModel, ReportsViewModel)      │
└──────────────────────────▲─────────────────────────────┘
                           │ Coroutines / Flow
┌──────────────────────────┴─────────────────────────────┐
│                  Repositories                          │
│   (TaskRepository, HabitRepository, AuthRepository)    │
└───────────────▲────────────────────────▲───────────────┘
                │                        │
       Local Operations           Cloud Sync (Postgrest)
                │                        │
┌───────────────▼──────────────┐ ┌───────▼───────────────┐
│     Room SQLite Database     │ │   Supabase Cloud      │
│  (Tasks, Subtasks, Habits,   │ │ (PostgreSQL + RLS +   │
│   Completions, Settings)     │ │  Edge Functions)      │
└──────────────────────────────┘ └───────────────────────┘
```

### 1.3 Technology Stack Matrix

| Subsystem | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Android OS** | Android SDK | Compile 36, Target 35, Min 24 | Operating system target |
| **Android UI** | Jetpack Compose + Material 3 | 1.7.x / 1.3.x | Declarative UI & liquid glass components |
| **Language** | Kotlin | 2.2.10 | Native mobile application language |
| **Local Database**| Room SQLite + KSP | 2.6.1 | Microsecond local persistence |
| **Concurrency** | Kotlin Coroutines & Flow | 1.8.x | Reactive asynchronous streams |
| **Serialization**| Moshi Kotlin Codegen | 1.15.x | JSON serialization / deserialization |
| **Networking** | Retrofit 2 + OkHttp 3 | 2.11.x / 4.12.x | HTTP client for Supabase & Napkin AI |
| **Web Companion**| Vite + React | 6.2.x / 19.3.x | Static web application & portal |
| **Glass Material**| `@zakisheriff/liquid-glass`| 0.3.6 | Web companion liquid glass foundation |
| **Cloud Database**| Supabase PostgreSQL | 15.x | Remote synchronization & user auth |
| **Serverless API**| Vercel Serverless | Node.js 24.x | Authentication, rate limiting & downloads |

### 1.4 Monorepo Directory Topology

```
TDlxHT/
├── app/                              # Native Android Application (Jetpack Compose)
│   ├── src/main/
│   │   ├── AndroidManifest.xml       # Permissions, services, receivers, activities
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt       # Root activity, pager, nav dock, sheet modals
│   │   │   ├── data/                 # Room database, DAOs, entities, repositories
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── dao/              # TaskDao, HabitDao, CompletionDao, SettingsDao
│   │   │   │   ├── entity/           # TaskEntity, HabitEntity, etc.
│   │   │   │   └── repository/       # TaskRepository, HabitRepository, AuthRepository
│   │   │   ├── service/              # ShakeDetectionService, BootReceiver, Reminders
│   │   │   ├── ui/
│   │   │   │   ├── components/       # SketchComponents.kt (GlassButton, Switch, Card...)
│   │   │   │   ├── screens/          # Home, Productivity, Tools, Reports, Settings
│   │   │   │   ├── theme/            # Color.kt, Type.kt, Theme.kt
│   │   │   │   └── viewmodel/        # MainViewModel, AuthViewModel, ReportsViewModel
│   │   └── res/                      # Drawables, mipmaps, values, xml configurations
│   └── build.gradle.kts              # Android Gradle build script & syncReleaseApk task
├── api/                              # Vercel Serverless API Functions
│   ├── _middleware.js                # Request ID correlation & Server-Timing headers
│   ├── _rateLimit.js                 # Sliding-window IP rate limiter
│   ├── status.js                     # Health check & database diagnostics
│   ├── download.js                   # Dynamic APK redirect to latest release
│   ├── register.js                   # Supabase authentication registration
│   ├── recover.js                    # Account recovery gateway
│   └── gemini-insights.js            # Server-side Gemini AI report generation
├── web/                              # Web Companion & Static Landing Page
│   ├── index.html                    # Single-page wireframe parity landing hub
│   ├── version.json                  # Remote update discovery manifest
│   ├── css/style.css                 # Web design system styles & CSS variables
│   └── js/                           # Web application scripts & Supabase auth
├── TDLxHT/                           # Universal Expo / React Native Client
├── design.md                         # Master UI/UX Design Compendium
└── technical.md                      # Master Technical Manual & Architecture Compendium
```

---

## 2. Native Android Application Architecture (`app/`)

### 2.1 Application Lifecycle & Android Manifest

- **Application ID:** `com.aistudio.habittask.wvnk`
- **Compile SDK:** 36 (Android 16 Developer / 36.1)
- **Target SDK:** 35 (Android 15 Production Stable)
- **Min SDK:** 24 (Android 7.0 Nougat — 99.8% device compatibility)

#### Permissions Architecture:
```xml
<!-- Minimal Production Permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.VIBRATE" />
```
*Note on Hardening:* All dangerous permission flags (`REQUEST_INSTALL_PACKAGES`, `SYSTEM_ALERT_WINDOW`, `USE_FULL_SCREEN_INTENT`) were permanently stripped to eliminate false-positive Google Play Protect installation warnings.

### 2.2 Jetpack Compose UI & Screen Matrix

The application adopts a 4-tab top-level horizontal navigation matrix managed by `HorizontalPager` with fixed layout boundaries:

1. **Index 0 — Today Dashboard (`HomeScreen.kt`):**
   - Hero daily summary glass card (fraction completed, progress bar, streak count).
   - `GlassSegmented` filter (`All`, `Tasks`, `Habits`).
   - Today's active tasks and scheduled habits.
2. **Index 1 — Productivity Hub (`ProductivityScreen.kt`):**
   - Dual-tab container integrating `TaskListScreen`, `HabitListScreen`, and `CalendarScreen`.
3. **Index 2 — Tools & Calculators (`ToolsScreen.kt`):**
   - 14 offline native calculators with live search, category sorting, and zero division guards.
4. **Index 3 — Insights & Analytics (`ReportsScreen.kt`):**
   - Real-time productivity score, completion rates, period selector (Weekly/Monthly), and AI visual report generation.

### 2.3 State Management & ViewModels

- **`MainViewModel`:**
  - Manages primary application state via `StateFlow` (`tasks`, `habits`, `todayTasks`, `completionRecords`, `settings`).
  - Implements optimistic UI updates: Database mutations occur on `Dispatchers.IO` while UI state updates reactively via Room's reactive `Flow` queries.
  - Implements the dual-streak algorithm:
    ```kotlin
    fun getHabitStreak(habit: Habit): StreakInfo {
      val records = completionRecordsMap[habit.id] ?: emptyList()
      val sortedDates = records.filter { it.isCompleted }.map { it.date }.sortedDescending()
      // Computes current streak & all-time longest streak
      ...
    }
    ```
- **`AuthViewModel`:** Manages Supabase session tokens, cloud sync states, and offline credentials.
- **`ReportsViewModel`:** Aggregates multi-week metrics and generates deterministic SVG/vector reports.

### 2.4 Room SQLite Database, DAOs & Entities

- **Database Class:** `com.example.data.AppDatabase` (Version: 4, Schema Export: false).
- **Entities:**
  - `TaskEntity` (`id`, `title`, `description`, `category`, `priority`, `dueDate`, `dueTime`, `isCompleted`, `isShortTerm`, `subtasksJson`, `reminderEnabled`, `repeatRule`, `syncStatus`, `updatedAt`).
  - `HabitEntity` (`id`, `name`, `description`, `category`, `frequency`, `targetValue`, `targetUnit`, `reminderTime`, `reminderEnabled`, `currentStreak`, `bestStreak`, `isPaused`, `isArchived`, `syncStatus`, `updatedAt`).
  - `HabitCompletionEntity` (`id`, `habitId`, `date`, `isCompleted`, `sessionsCompleted`, `syncStatus`, `updatedAt`).
  - `SettingsEntity` (`id`, `isDarkMode`, `dailySummaryEnabled`, `summaryTime`, `soundEnabled`, `vibrationEnabled`, `hasPromptedPermissions`).

#### DAO Query Guarantees:
- Microsecond execution via SQLite indexing on `date`, `habitId`, and `dueDate`.
- Conflict strategy: `OnConflictStrategy.REPLACE` for idempotent data synchronization.

### 2.5 Repository Layer & Domain Mapping

Repositories act as the single source of truth, mediating between Room SQLite and the Supabase Postgrest API:
- Local operations write immediately to Room.
- If network connectivity is detected, background coroutines dispatch delta payloads to Supabase.
- If offline, mutations are stamped with `syncStatus = "PENDING"` and queued for next network availability.

### 2.6 Background Scheduling, Alarms & Sensors

- **Exact Alarms (`AlarmManager`):** Schedules precise notifications for task deadlines and multi-session habit reminders using `setExactAndAllowWhileIdle()`.
- **Boot Recovery (`BootReceiver`):** Listens to `ACTION_BOOT_COMPLETED` and reconstitutes all scheduled alarms directly from the Room database on device reboot.
- **Shake Detection (`ShakeDetectionService`):** Background accelerometer sensor listener detecting physical shake gestures to trigger quick-create workflows.

### 2.7 AI Intelligence & Napkin Visual Engine

- **Hybrid Intelligence:** When online, metrics are processed via Supabase Edge Function to Google Gemini 1.5 Flash.
- **Deterministic Offline Fallback:** If internet is unavailable, a local mathematical engine deterministically computes productivity scores, quadrant distributions, and trend analysis directly from SQLite tables.

### 2.8 Gradle Build System, R8 Minification & Signing

Configured in `app/build.gradle.kts`:
- **Code Shrinking & Obfuscation:** Google R8 minification enabled (`isMinifyEnabled = true`, `isShrinkResources = true`).
- **Signing Scheme:**
  - APK Signature Scheme v1 (JAR signing): Supported.
  - APK Signature Scheme v2 (Full APK block): **Enabled (`true`)**.
  - APK Signature Scheme v3 (Key rotation support): **Enabled (`true`)**.
- **Sync Task (`syncReleaseApk`):** Automatically copies `app-release.apk` to root as `TDLxHT.<version>.apk` and `TDlxHT.apk`, synchronizes `version.json`, and updates web distribution directories.

---

## 3. Web Platform & Vercel Serverless Architecture (`web/` & `api/`)

### 3.1 Vite Build Pipeline & Static Landing Hub

- **Build Tool:** Vite 6.4.4.
- **Output:** Fully static, pre-rendered bundle in `dist/`.
- **Assets Copied:** Favicons, app icons, robots.txt, sitemaps, Android asset links (`.well-known/assetlinks.json`), and version manifests.

### 3.2 Serverless API Endpoints Matrix

| Endpoint | Method | Purpose | Protection |
| :--- | :--- | :--- | :--- |
| `/api/status` | `GET` | Health check, DB latency, version check | Rate limited (60/min) |
| `/api/download` | `GET` | 302 Redirect to latest GitHub Release APK | Public / Cached |
| `/api/register` | `POST` | Supabase auth user registration gateway | Rate limited / Validated |
| `/api/recover` | `POST` | Password reset & account recovery email | Rate limited / Sanitized |
| `/api/gemini-insights`| `POST` | AI productivity report generation | Service-role authenticated |

### 3.3 Correlation Tracing & Server-Timing Middleware

Implemented in `api/_middleware.js`:
- **Distributed Request Tracing:** Generates or passes through `X-Request-Id` (UUID v4) across all incoming requests and outgoing responses.
- **Latency Metrics:** Emits standard `Server-Timing: total;dur=<ms>` headers for granular performance observability.
- **Security Headers:**
  ```http
  Strict-Transport-Security: max-age=63072000; includeSubDomains; preload
  X-Content-Type-Options: nosniff
  X-Frame-Options: DENY
  X-XSS-Protection: 1; mode=block
  Referrer-Policy: strict-origin-when-cross-origin
  Permissions-Policy: camera=(), microphone=(), geolocation=()
  ```

### 3.4 Sliding-Window IP Rate Limiting

Implemented in `api/_rateLimit.js`:
- In-memory sliding-window limiter enforcing 60 requests per minute per IP address.
- Responds with `429 Too Many Requests` and standard `X-RateLimit-Limit`, `X-RateLimit-Remaining`, and `X-RateLimit-Reset` headers.

---

## 4. Supabase Cloud Database & Security Architecture

### 4.1 Complete PostgreSQL Database Schema

```sql
-- Tasks Table
CREATE TABLE public.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    category TEXT DEFAULT 'Personal',
    priority TEXT DEFAULT 'MEDIUM',
    due_date DATE,
    due_time TIME,
    is_completed BOOLEAN DEFAULT FALSE,
    is_short_term BOOLEAN DEFAULT FALSE,
    subtasks_json JSONB DEFAULT '[]'::jsonb,
    reminder_enabled BOOLEAN DEFAULT FALSE,
    repeat_rule TEXT DEFAULT 'NONE',
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Habits Table
CREATE TABLE public.habits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT DEFAULT 'Health',
    frequency TEXT DEFAULT 'EVERY_DAY',
    target_value REAL DEFAULT 1.0,
    target_unit TEXT DEFAULT 'times',
    reminder_time TEXT,
    reminder_enabled BOOLEAN DEFAULT FALSE,
    current_streak INT DEFAULT 0,
    best_streak INT DEFAULT 0,
    is_paused BOOLEAN DEFAULT FALSE,
    is_archived BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Habit Completions Table
CREATE TABLE public.habit_completions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    habit_id UUID NOT NULL REFERENCES public.habits(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    sessions_completed INT DEFAULT 1,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_habit_date UNIQUE(habit_id, date)
);
```

### 4.2 Row-Level Security (RLS) Isolation

Every table enforces strict Row-Level Security so that users can only access their own records:

```sql
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.habits ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.habit_completions ENABLE ROW LEVEL SECURITY;

-- Tasks Policy
CREATE POLICY "Users can only access own tasks"
ON public.tasks FOR ALL
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Habits Policy
CREATE POLICY "Users can only access own habits"
ON public.habits FOR ALL
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Habit Completions Policy
CREATE POLICY "Users can only access own habit completions"
ON public.habit_completions FOR ALL
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);
```

### 4.3 Zero-Leak Client Architecture

1. **Android Client (`.env`):** Contains only public `SUPABASE_URL` and `SUPABASE_ANON_KEY`.
2. **Secrets Gradle Plugin:** Excludes sensitive variables via `ignoreList` in `app/build.gradle.kts`.
3. **Verification:** Decompiled release `classes.dex` verified to contain **0 private server keys**.
4. **Napkin AI / Gemini API Keys:** Hosted exclusively inside Vercel Serverless environment variables and Supabase Edge Function secrets.

---

## 5. Universal Mobile Client Architecture (`TDLxHT/`)

- **Framework:** Expo SDK 57 / React Native 0.86 with Expo Router.
- **Commands:**
  - `npx expo install <package>`: Installs SDK-compatible versions.
  - `npx expo start`: Launches the development bundler.
  - `npx expo lint`: Runs code quality checks.
  - `npx tsc --noEmit`: Strict TypeScript typechecking.

---

## 6. Build, Verification & Deployment Playbook

### 6.1 Local Environment Setup

```bash
# Clone and inspect repository
git clone https://github.com/ddheerajccarpenter/TDLxHT.git
cd TDLxHT

# Install Node dependencies
npm install
```

### 6.2 Automated Unit Test Suite

Executes all 33 unit tests validating Room DAOs, ViewModels, calculators, and streak engines:

```bash
./gradlew testDebugUnitTest
```

### 6.3 Release APK Compilation (`syncReleaseApk`)

Compiles the production APK with R8 minification, v2/v3 signatures, and copies outputs to root:

```bash
./gradlew syncReleaseApk
```
*Outputs:* `TDLxHT.<version>.apk` and `TDlxHT.apk` in project root and `dist/apk/`.

### 6.4 Web Companion Build & Vercel Deploy

```bash
# Compile static web companion
npm run build

# Deploy to Vercel production
npx vercel --prod --yes
```

### 6.5 GitHub Release Assets Synchronization

```bash
# Upload / overwrite release assets
gh release upload v2.4.3 TDLxHT.2.4.3.apk TDlxHT.apk --clobber
```

### 6.6 One-Command Production Release Workflow

Configured in `package.json:9`:
```bash
npm run release
```
*(Executes `./gradlew syncReleaseApk && npm run build && npx vercel --prod --yes`)*.

---

## 7. Changelog & Technical Defect History

### 7.1 Resolution of 'App Not Installed' Error
- **Root Cause:** Android OS package manager blocked updates due to versionCode parity conflicts and dangerous permission flags.
- **Fix Implemented:**
  - Bumped `versionCode` to `30` (`versionName = "2.4.3"`).
  - Target SDK standardized to `35` (Android 15 Production Stable).
  - Purged dangerous permission flags (`REQUEST_INSTALL_PACKAGES`, `SYSTEM_ALERT_WINDOW`, `USE_FULL_SCREEN_INTENT`).

### 7.2 120fps Constant Layout Navigation Engine
- **Root Cause:** Sliding navigation indicator previously used unoptimized gesture detectors and `animateContentSize` on parent containers, triggering measure/layout thrashing across sibling composables every frame.
- **Fix Implemented:**
  - Enforced $O(1)$ constant layout geometry with 5 fixed-width slots.
  - Isolated indicator translation to GPU RenderNode properties via `graphicsLayer { ... }`.
  - Guaranteed locked 120fps swipe performance.

### 7.3 Insights Screen Viewport Overlap Fix
- **Root Cause:** `HorizontalPager` did not inherit Scaffold bottom insets, causing the bottom of the Insights screen to render underneath the floating dock.
- **Fix Implemented:**
  - Added `FloatingNavContentBottomPadding + 32.dp` content padding in `ReportsScreen.kt`.
  - Appended a 32dp trailing spacer at the bottom of the report view.
  - Converted the "Generate Visual Report" CTA to a standard `GlassButton`.

