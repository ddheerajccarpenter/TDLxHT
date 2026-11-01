# TDlxHT — Complete UI/UX & Source Code Audit (`UI_UX_AUDIT.md`)

**Date:** 2026-10-31  
**Target:** TDlxHT Android Application (`com.aistudio.habittask.wvnk`) & Web Companion  
**Audit Standard:** Apple Liquid Glass, Hallmark Anti-Slop, Better Typography, WCAG 2.2 AAA  

---

## Executive Summary
This audit inspects the TDlxHT codebase and screen recordings, diagnosing root causes and specifying concrete engineering corrections across navigation, animations, typography, and screen layouts.

---

## Findings Matrix

| ID | Screen / Area | Component | Description | Severity | Root Cause | Required Correction | Status |
|---|---|---|---|---|---|---|---|
| **NAV-01** | Global Navigation | `SketchBottomNavigationBar` | Plus button centered with text labels below icons, creating cluttered layout and potential touch interference. | **P1** | Legacy 5-slot layout with center FAB. | Redesign into 1 continuous Liquid Glass capsule dock: Home (0) → Productivity (1) → Tools (2) → Insights (3) → Plus (far right). Remove text labels (icons only). | **Verified / Fixed** |
| **NAV-02** | Tools & Insights | Scroll Container | Bottom cards and action buttons partially hidden beneath floating navigation dock on full scroll. | **P2** | Static or insufficient bottom content padding in LazyColumns. | Implement dynamic, inset-aware `FloatingNavContentBottomPadding` (`64dp + 16dp + navInsets + 32dp`) across all scrolling screens. | **Verified / Fixed** |
| **ANIM-01** | Global Press | Interactive Buttons | Lack of uniform tactile spring response; inconsistent press feedback across lists and cards. | **P3** | Multiple disparate scale modifiers (`bounceClick`, custom states) with varying damping. | Implement standardized `Modifier.bouncyPress` using `MotionButtonPress` (`scale = 0.95f`) and `MotionButtonRelease` with haptics. | **Verified / Fixed** |
| **ANIM-02** | Quick Create | Plus Button | FAB rotation state could desync or cause dock reflow during presentation. | **P2** | Unsynchronized rotation keys in previous revisions. | Bind rotation strictly to `isQuickAddSheetOpen` using `MotionIconMorph` (45° rotation to `X` with zero layout shift). | **Verified / Fixed** |
| **TASK-01** | Tasks & Home | Task Item Card | Checkbox click produces static or abrupt checkmark toggle; title strikethrough lacked smooth transition. | **P3** | Boolean toggle without path-trim or crossfade animation. | Implement `AnimatedCheckmarkIcon` with vector path drawing (`0f` to `1f`) and smooth text-decoration transition. | **Verified / Fixed** |
| **TASK-02** | Tasks & Home | Undo Surface | Accidental task completion required navigating to completed filter to restore. | **P2** | Missing lightweight undo confirmation. | Implement floating `UndoConfirmationPill` with 3.5s auto-dismiss and instant restore callback. | **Verified / Fixed** |
| **HABIT-01**| Habits & Home | Habit Card | Streak counter updated with instantaneous text change rather than celebratory tactile pop. | **P3** | Static text rendering without numeric transition. | Implement `AnimatedContent` numeric slide-up pop with `MotionCounter` and circular progress ring fill with `MotionProgress`. | **Verified / Fixed** |
| **CAL-01** | Calendar | Month Grid | Switching months caused instantaneous grid replacement without spatial continuity. | **P3** | Direct recomposition without slide transition. | Wrap month card in `AnimatedContent` with horizontal slide transitions (`MotionNavigation`). | **Verified / Fixed** |
| **TOOL-01** | Tools | Tool Item Cards | Tool cards displayed arbitrary two-letter monograms ("AT", "BE", "CG") rather than semantic icons. | **P2** | Placeholder monogram implementation. | Replace with standard semantic Material icons (`School`, `TrendingUp`, `ReceiptLong`, etc.). | **Verified / Fixed** |
| **TOOL-02** | Tools | Calculator Outputs | Calculated values replaced without transition; copy button lacked visual success confirmation. | **P3** | Raw text updates and missing feedback state. | Wrap output in `AnimatedContent` and add animated "Copied!" checkmark badge. | **Verified / Fixed** |
| **TYPO-01** | Global | Counters & Timers | Changing numbers jittered horizontally due to variable-width glyphs. | **P3** | Proportional font digits without tabular numbers. | Apply `FontFeatureSettings("tnum")` (`tabular-nums`) to all counters, streaks, timers, and calculation outputs. | **Verified / Fixed** |
| **PERF-01** | Display / Scroll | Refresh Rate | Variable 120Hz LTPO display hopping created uneven micro-stutter during rapid scrolling. | **P2** | Unconstrained display mode defaulting to variable refresh. | Lock window refresh rate to rock-solid 60Hz (`preferredRefreshRate = 60f`) for cinematic frame pacing. | **Verified / Fixed** |
| **PERF-02** | Display / Scroll | Motion Trail | Fast flings lacked optical continuity, appearing harsh on OLED displays. | **P3** | No velocity-dependent optical motion attenuation. | Implement 5–10% optical motion blur (`Modifier.motionBlurOnScroll`) on rapid scroll, clearing to 0 when idle. | **Verified / Fixed** |
| **WEB-01**  | Web Companion | Layout & Typography | Cluttered cards, redundant text strings, and inconsistent dock styling compared to native app. | **P2** | Web companion lagged behind native design system. | Overhaul `web/` with Hallmark floor, shadcn/ui card composition, better-typography tabular digits, and continuous dock. | **Verified / Fixed** |
| **NAV-03**  | Global Navigation | `LiquidGlassNavDock` & Plus Button | Plus button resided inside the dock capsule, causing asymmetry and cramped layout on 4-tab navigation. | **P1** | Integrated Plus button inside stadium capsule. | Separated Plus into Component B: an independent 54dp circular floating Liquid Glass button positioned 10dp right of the 4-tab stadium dock with rotating Add-to-Close icon. | **Verified / Fixed** |
| **DASH-01** | Home Dashboard | `DashboardLayoutManager` | Dashboard layout was rigid and non-reconfigurable; user could not customize widget ordering or hide unused sections. | **P1** | Hardcoded vertical layout in `HomeScreen.kt`. | Implemented `DashboardLayoutManager` with 5 modular sections, drag/button reordering, eye visibility toggles, and cloud sync. | **Verified / Fixed** |
| **REP-01**  | Reports Screen | `MonthlyDotMatrixRenderer` | Third-party AI API key required for reports, resulting in non-deterministic graphics and potential key leak. | **P0** | Napkin visual API dependency. | Built 100% deterministic native Canvas `MonthlyDotMatrixRenderer` drawing directly from Room SQLite records with 1080x1350px PNG export and system share sheet. | **Verified / Fixed** |
| **AUD-01**  | Audio Feedback | `SoundManager` | Silent interactions and missing raw audio assets created risk of crash or lifeless UI. | **P1** | No centralized sound manager. | Implemented `SoundManager` with remote manifest sync (SHA-256 integrity) and 100% offline native PCM micro-synthesizer fallback for harmonic bell chimes and clicks. | **Verified / Fixed** |

---

## Action Plan
All audited items across P0, P1, P2, and P3 have been fully engineered, verified, and released.

