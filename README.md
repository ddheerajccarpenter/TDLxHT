# TDlxHT — To-Do List × Habit Tracker

<div align="center">

**A tactile, sketch-aesthetic, offline-first Android productivity companion.**  
*Engineered for mindful task management, habit streak habituation, and personal analytics.*

[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-2ea44f?style=flat-square&logo=android)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin%20%7C%20Jetpack%20Compose-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Storage](https://img.shields.io/badge/Storage-Room%20SQLite%20(Offline%20First)-4285F4?style=flat-square&logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![Version](https://img.shields.io/badge/Version-v2.4.3%20(Build%2030)-black?style=flat-square)](version.json)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

</div>

---

> 📖 **Master Documentation:**
> - 🎨 **[Master Design System & UI/UX Compendium (`design.md`)](design.md)**: Apple-Style Liquid Glass, centralized design tokens, physics, 5-level surface hierarchy, component specifications, and screen UX matrix.
> - ⚙️ **[Master Technical Manual & Architecture Compendium (`technical.md`)](technical.md)**: Clean architecture, Room SQLite schema, background scheduling, Napkin AI engine, Vercel serverless APIs, Supabase RLS, and release playbook.

---

## 💡 What is TDlxHT?

**TDlxHT** (To-Do List × Habit Tracker) is designed around a single core belief: **productivity tools should feel tactile, responsive, and completely dependable.**

Unlike cluttered corporate project managers or ephemeral web apps, TDlxHT pairs an artisan hand-drawn sketch aesthetic with organic spring physics and a rock-solid, offline-first architecture. It bridges daily task execution, habit habituation, focus sessions, and analytical progress reporting into a unified, responsive interface that works anytime, anywhere—even without an internet connection.

---

## 📱 How the App Works

```
┌───────────────────────────────────────────────────────────────────┐
│                       TODAY'S DASHBOARD                           │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ 📊 DAILY SUMMARY: 5/7 Completed • 80% • 🔥 5-Day Streak    │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                   │
│   [ All ]                 [ Tasks ]                [ Habits ]     │
│                                                                   │
│  TODAY'S TASKS                                                    │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ [✓] Finalize architecture review                 Due 2:00 PM│  │
│  │ [ ] Submit mobile release APK (2 subtasks)       Due 6:00 PM│  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                   │
│  TODAY'S HABITS                                                   │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ [✓] 30m Deep Reading (Daily Target)              🔥 12 Days │  │
│  │ [ ] Drink 2.5L Water (3/5 glasses logged)        🔥 4 Days  │  │
│  └─────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────┘
```

### 1. Today's Dashboard & Tactile Task Management
- **Daily Summary Hero Card**: At a glance, view total tasks and habit check-ins completed for the day, accompanied by a dynamic progress bar that smoothly fills from zero and a celebratory streak badge.
- **Segmented Filter Switcher**: Effortlessly switch between **All**, **Tasks**, and **Habits** with fluid slider transitions and micro-haptic feedback.
- **Subtasks & Notes**: Break down complex tasks into subtasks with instant completion tracking and automatic parent-task resolution when all steps are finished.
- **Dashboard Edit Mode**: Toggle edit mode to batch-manage, prioritize, or clean up overdue items directly from the home screen.
- **Custom Categorization & Tags**: Organize tasks by categories (Work, Personal, Health, Finance, Learning) with customized color pills and priority flags (Urgent, High, Medium, Low).

### 2. Habits & Streak Habituation Engine
- **Flexible Frequency Schedules**: Set habits to repeat daily, on weekdays, or custom intervals (e.g., 3 times per week).
- **Streak Tracker & Recovery**: Build momentum with continuous streak counters that celebrate milestones.
- **Session & Metric Logging**: For habits that require measurable progress (e.g., glasses of water, meditation minutes, pages read), tap to record each session toward your target.
- **Celebration Micro-Feedback**: High streaks feature an organic spring-pulse badge animation that acknowledges consistency without intrusive popups.

### 3. Chronological Calendar & Agenda
- **Three-Way View Switcher**: Seamlessly alternate between **Month View**, **Week View**, and **Day View**.
- **Interactive Month Grid**: Color-coded date indicators highlight days with scheduled tasks, completed items, or active habits.
- **Day Agenda Timeline**: View a clear chronological timeline of timed commitments alongside untimed to-dos.
- **Instant "Jump to Today"**: Quick-navigation button to instantly return to today's schedule from any date.

### 4. Focus Hub & Utilities
- **Countdown Focus Timer**: Built-in Pomodoro and customizable deep-work timer with tactile dial controls, subtle tick vibrations, and background persistence.
- **"Shake to Action" Gesture**: Shake your physical device to trigger immediate actions—such as opening the focus timer or launching the quick task creator—detected via hardware accelerometer sensors.
- **Productivity Utilities Toolbox**: Quick access to utility calculators, scratchpad notes, and reorderable productivity tools.

### 5. AI Productivity Advisor & Analytics
- **Weekly & Monthly Insights**: Review comprehensive analytics comparing task completion rates, habit consistency, and active focus durations.
- **Category Distribution Heatmaps**: Visualize where your energy goes with proportional breakdown indicators.
- **On-Device Visual Reports**: Generate high-resolution summary charts and cards rendered entirely from local Room SQLite database statistics with zero mock data.

---

## 🎨 Design Philosophy & Motion Experience

TDlxHT is crafted to stand out from generic template designs:

- **Artisan Sketch Aesthetic**: Clean 1.2dp bordered cards, pill controls, and subtle paper dot backgrounds deliver a tactile notebook feel.
- **Dual Visual Modes**:
  - **Light Mode**: Warm, gallery-minimalist Sand (`#F5F5F0`) and Terracotta accents inspired by curated sketchbooks.
  - **Dark Mode**: Pitch-black canvas (`#0A0A0A`) with electric Cyber Lime accents engineered for OLED power efficiency and night focus.
- **Living Motion System**:
  - **Tactile Springs**: Buttons and cards depress with spring dampening (`dampingRatio = 0.72f`) and return on release.
  - **Staggered Cascades**: Lists and cards glide in sequentially rather than popping in abruptly.
  - **Progress Fill**: Progress meters smoothly interpolate from zero to provide a visual sense of accomplishment.
- **Accessibility by Default**: The entire animation pipeline listens to system accessibility settings (`isReducedMotionEnabled`). When reduced motion is enabled, all screens transition instantly without animations.

---

## 🔒 Offline-First & Privacy Architecture

- **100% Offline Capable**: Your data is stored locally on your device in an encrypted Room SQLite database. You can use every core feature—tasks, habits, timer, analytics—without ever creating an account.
- **Zero Third-Party Trackers**: No third-party advertising SDKs, behavioral analytics, or telemetry libraries are bundled.
- **Optional Cloud Sync**: For multi-device continuity, users can optionally sign in via Supabase cloud auth. Every cloud table is protected by strict Row-Level Security (`auth.uid() = user_id`), ensuring data isolation.

---

## 🚀 Installation & Getting Started

### Download the Android APK

You can install the latest release directly onto any Android device running **Android 8.0 (API 24) or higher**:

1. Download [**TDLxHT.2.4.3.apk (v2.4.3)**](https://github.com/ddheerajccarpenter/TDLxHT/releases/latest) directly from GitHub Releases or from the web portal at **[tdlxht.vercel.app](https://tdlxht.vercel.app)**.
2. Open the downloaded `.apk` file on your Android device.
3. If prompted, allow **"Install unknown apps"** for your browser or file manager.
4. Tap **Install** and open **TDlxHT**!

### Navigation Walkthrough

Once installed, use the floating stadium navigation bar at the bottom of the screen to explore:

| Icon | Tab Name | Purpose |
| :---: | :--- | :--- |
| 🏠 | **Home** | Daily summary, quick filters, today's tasks & habits, search, settings |
| ⚡ | **Productivity** | Comprehensive task lists, full habit management, calendar & agenda |
| 🛠️ | **Tools** | Focus timer, customizable productivity utilities, scratch tools |
| 📈 | **Insights** | AI Productivity Advisor, weekly/monthly statistics, category distributions |

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
