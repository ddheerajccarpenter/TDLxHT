# TDlxHT: Apple-Style Liquid Glass UI Specification (`designglass.md`)

This document serves as the canonical design specification and single source of truth for the complete Apple-Style Liquid Glass redesign of **TDlxHT** (To-Do List × Habit Tracker).

---

## 1. Design Philosophy

**TDlxHT** combines offline-first productivity engineering with an authentic Apple-style Liquid Glass aesthetic. Liquid Glass is not a flat cosmetic theme or a blur filter applied indiscriminately to existing dark rectangles; it is an optical and tactile material system inspired by Apple visionOS and iOS 18 design principles, adapted natively to Android Jetpack Compose and the Web companion.

### Core Tenets:
1. **Realistic Translucency & Refractive Edges:** Glass surfaces transmit underlying background tonality with controlled opacity (75%–88%). Surfaces are bounded by a dual-gradient specular rim (1px–1.2px) with overhead light concentration (28% white alpha at top tapering to 6% white alpha at bottom).
2. **Contextual Material Depth:** Translucent glass requires visual information behind it. The underlying canvas features a subtle, large-scale organic tonal variation (10%–15% graduation between `#151515` and `#222226`) so that controls have real depth to bend and illuminate without synthetic glow or noise.
3. **Restrained Specular Highlights:** Curved surfaces display an inner top specular arc sheen (white 20% alpha down to transparent) in the upper 40% of the component to simulate overhead environmental light.
4. **Purposeful Motion & Kinetic Physics:** Interactive elements respond with spring physics:
   - Button press: Elastic compression (`scale = 0.94f`, `dampingRatio = 0.72f`, `stiffness = 450f`).
   - Tab switching: Fluid sliding glass indicator pill (`dampingRatio = 0.78f`, `stiffness = 420f`).
   - Counter updates: Volume-conserving squash & stretch (`scaleX = 1.06f`, `scaleY = 0.94f`).
5. **Strict Monochromatic Integrity:** The interface uses an authentic charcoal, obsidian, and pure white palette. Zero random neon glows, zero arbitrary rainbow gradients, and zero decorative blobs. Functional states rely on high-contrast typography, clear geometric indicators, and purposeful contrast reversals.
6. **Accessibility & Contrast First (WCAG AAA):** Translucent fills never compromise typography contrast. Body and title text maintain high contrast (`#F5F5F5` against `#202025` glass surfaces), ensuring effortless legibility in all ambient lighting.

---

## 2. Centralized Design Tokens

### A. Palette & Surface Colors

| Token Name | Dark Mode Value | Light Mode Value | Purpose / Usage |
|---|---|---|---|
| `CanvasBackground` | `#151515` | `#F5F5F7` | Primary screen canvas background |
| `CanvasSecondary` | `#1C1C1E` | `#EBEBF0` | Secondary panel backgrounds |
| `GlassFillBase` | `rgba(37, 37, 42, 0.82)` | `rgba(255, 255, 255, 0.85)` | Standard glass cards, panels, list containers |
| `GlassFillElevated` | `rgba(48, 48, 55, 0.88)` | `rgba(255, 255, 255, 0.94)` | Floating navigation dock, active modal sheets |
| `GlassFillSubtle` | `rgba(25, 25, 28, 0.65)` | `rgba(240, 240, 245, 0.70)` | Inactive toggle tracks, unselected chips |
| `GlassBorderTop` | `rgba(255, 255, 255, 0.28)` | `rgba(255, 255, 255, 0.80)` | Top specular rim highlight for glass border |
| `GlassBorderBottom` | `rgba(255, 255, 255, 0.06)` | `rgba(0, 0, 0, 0.12)` | Bottom rim shadow for glass border |
| `GlassSheenTop` | `rgba(255, 255, 255, 0.20)` | `rgba(255, 255, 255, 0.45)` | Inner arc specular sheen gradient start |
| `TextPrimary` | `#F5F5F7` | `#111113` | Headings, active labels, primary values |
| `TextSecondary` | `#B0B0B8` | `#55555C` | Subtitles, section headers, metadata |
| `TextMuted` | `#7C7C85` | `#8E8E93` | Placeholders, timestamps, hints |
| `DividerHairline` | `rgba(255, 255, 255, 0.10)` | `rgba(0, 0, 0, 0.08)` | Interior card and row separators |
| `AccentAction` | `#FFFFFF` (inverted) | `#000000` (inverted) | Primary action badges and active pill fills |
| `Destructive` | `#E05D5D` | `#D32F2F` | Delete confirmations and error states |

### B. Geometry & Corner Radii

| Element Type | Corner Radius Token | Exact DP Value |
|---|---|---|
| Floating Navigation Dock | `RadiusPill` | `CircleShape` (`9999.dp`) |
| Action Buttons (`GlassButton`) | `RadiusPill` | `CircleShape` (`9999.dp`) |
| Segmented Switchers (`GlassSegmented`) | `RadiusPill` | `CircleShape` (`9999.dp`) |
| Input Fields (`GlassInput`) | `RadiusMedium` | `14.dp` |
| Primary Content Cards (`GlassCard`) | `RadiusLarge` | `18.dp` |
| Hero / Summary Cards | `RadiusXLarge` | `22.dp` |
| Modal Bottom Sheets (`GlassSheet`) | `RadiusSheet` | `Top: 24.dp, Bottom: 0.dp` |
| Context Menus (`GlassMenu`) | `RadiusLarge` | `16.dp` |

### C. Typography Scale (SF Pro / System Sans)

| Style Token | Font Size | Line Height | Font Weight | Usage |
|---|---|---|---|---|
| `DisplayHero` | `28.sp` | `34.sp` | Bold (`700`) | Main section headings ("Today", "Insights") |
| `TitleCard` | `18.sp` | `24.sp` | SemiBold (`600`) | Card titles, tool names, dialog titles |
| `BodyRegular` | `15.sp` | `21.sp` | Normal (`400`) | Task descriptions, tool descriptions |
| `BodyBold` | `15.sp` | `21.sp` | SemiBold (`600`) | Interactive button labels, active selections |
| `CaptionMeta` | `13.sp` | `18.sp` | Medium (`500`) | Due dates, category tags, streak counters |
| `EyebrowLabel`| `11.sp` | `14.sp` | Bold (`700`) | Section markers ("01 / TODAY'S TASKS") |

### D. Motion Curves & Tactile Physics

| Motion Token | Damping Ratio | Stiffness | Response Behavior |
|---|---|---|---|
| `SpringTactilePress` | `0.72f` | `450f` | Snappy scale-down (`0.94f`) on button press |
| `SpringLensSlide` | `0.78f` | `420f` | Fluid sliding lens on tab navigation |
| `SpringPopOvershoot` | `0.54f` | `450f` | Elastic snap on Quick Add FAB and Checkbox check |
| `SpringSheetModal` | `0.80f` | `380f` | Natural slide-up presentation for sheets |

---

## 3. Surface Hierarchy

The Liquid Glass system establishes 5 distinct optical levels:

```
[Level 4: Modal Sheets & Dialogs]  — GlassSheet, Quick Create, Delete Dialogs (Elevated 16dp shadow, 88% opacity)
       ▲
[Level 3: Floating Controls]       — GlassNav Floating Dock, GlassMenu Popups (Elevated 14dp shadow, 86% opacity)
       ▲
[Level 2: Interactive Controls]    — GlassButton, GlassSwitch, GlassSegmented, GlassInput (Elevated 4dp shadow)
       ▲
[Level 1: Content Surfaces]        — GlassCard, Today Summary, Task Items, Habit Items (Elevated 2dp shadow, 80% opacity)
       ▲
[Level 0: Textured Canvas]         — Subtle organic charcoal depth (0dp shadow, 100% opacity base)
```

1. **Level 0 (Textured Canvas):** The base canvas (`#151515`) rendered with subtle large-scale radial graduation to illuminate translucent surfaces.
2. **Level 1 (Content Surfaces):** `GlassCard` panels grouping tasks, habits, and calculator outputs. 80% opacity fill with 1px specular gradient rim.
3. **Level 2 (Interactive Controls):** `GlassButton`, `GlassInput`, `GlassSwitch`, `GlassSlider`. High touch targets (minimum 44dp × 44dp), responsive spring feedback.
4. **Level 3 (Floating Controls):** `GlassNav` floating stadium dock. Elevated 14dp ambient drop shadow, 88% opacity frosted fill, constant-geometry $O(1)$ tab slots.
5. **Level 4 (Modal Sheets & Dialogs):** `GlassSheet` modal surfaces. Full width with 24dp top corners, top drag pill indicator, backdrop dimming (`rgba(0, 0, 0, 0.65)`), and complete safe-area compliance.

---

## 4. Component Implementations

### A. `GlassButton`
- **Variants:**
  - `PillWithLabel`: Stadium pill (`CircleShape`), 40–44dp height. Contains leading icon and visible label. Example: `<PlusIcon /> Add Task`.
  - `CircularIconOnly`: True circle (`CircleShape`), 38–44dp diameter. Centered icon with mandatory `contentDescription`.
  - `Destructive`: Subtle red glass border (`rgba(224, 93, 93, 0.40)`) and text (`#E05D5D`).
- **Feedback:** Spring scale-down (`0.94f`) on press with haptic tick.

### B. `GlassSwitch`
- **Geometry:** 52dp width × 30dp height stadium track.
- **Visuals:** Translucent track (`#252528` dark, `#E0E0E5` light) with 1px glass border.
- **Thumb:** 24dp circular white glass disc with top specular sheen, spring sliding horizontally (`stiffness = 500f, dampingRatio = 0.75f`).

### C. `GlassSegmented`
- **Geometry:** Stadium pill container (`CircleShape`), 44dp height.
- **Indicator:** Continuous GPU-rendered glass lens that smoothly glides beneath the active tab with spring physics.
- **States:** Active label text turns high-contrast `#FFFFFF` (dark) or `#000000` (light).

### D. `GlassInput` & `GlassSearchBar`
- **Geometry:** `RoundedCornerShape(14.dp)`, 44dp height.
- **Visuals:** Dark translucent fill (`#1C1C20`), 1px specular rim, leading search/text icon, trailing clear button on active input.

### E. `GlassSheet`
- **Geometry:** Modal bottom sheet with 24dp top corners.
- **Indicators:** Centered 36dp × 4dp drag handle pill with specular edge.
- **Dismissal:** Back-press handling, tap-outside handling, and smooth spring slide-down.

### F. `GlassNav` (Floating Dock)
- **Geometry:** Floating stadium pill (`CircleShape`), 64dp height, 16dp horizontal margins.
- **Layout:** 4 navigation destinations (`Home`, `Productivity`, `Tools`, `Insights`) + center elevated circular Quick Add (`+`) button.
- **Content Clearance:** All scrollable screens provide `FloatingNavContentBottomPadding` (`155.dp` + insets) to guarantee zero overlap with actionable buttons.

---

## 5. Screen Redesign Inventory & Bug Resolution

### Screen 1: Home / Today Screen
- **Daily Summary Hero Glass Card:** Current date, completed fraction, completion percentage bar, and streak badge in a unified glass card.
- **GlassSegmented Filter:** `All`, `Tasks`, `Habits` with real synchronized data filtering.
- **Today's Tasks & Habits:** Clear section headers with view mode toggle and labeled `GlassButton` actions (`<PlusIcon /> Add Task`, `<PlusIcon /> Add Habit`).
- **Clean Empty States:** Intentional layout with action button and clear guidance, eliminating unused whitespace.

### Screen 2: Productivity Screen
- **Dual Tab Architecture:** Tasks, Habits, and Calendar integrated into a unified container.
- **Bottom GlassSegmented Bar:** Positioned cleanly above the main bottom navigation dock with a centered separator hairline and complete scroll clearance.

### Screen 3: Tasks Screen & Task Detail Edit Screen
- **Task List:** Status filters (`All`, `Today`, `Upcoming`, `Completed`), search filter, priority flags, and subtask completion counters.
- **Task Detail Form:** Standardized `AppHeader` with `GlassButton` Save action. Clean inputs for title, notes, due date, category chips, priority chips, reminder toggles, and repeat rules.
- **Validation:** Clear error toast on empty title; duplicate submission prevention.

### Screen 4: Habits Screen & Habit Detail Edit Screen
- **Habit List:** Habit cards with frequency schedules, current streaks, best streaks, and daily completion checkboxes.
- **Habit Detail Form:** Standardized `GlassButton` Save action. Inputs for name, notes, category chips, frequency selector (`Every Day`, `Weekdays`, `Weekly`, `Custom`), target value steppers, and reminder time pickers.
- **Validation:** Habit name validation; prevents duplicate submissions; persistent schedule updates.

### Screen 5: Calendar Screen
- **Month / Week / Day Switcher:** Smooth glass tab switching.
- **Interactive Calendar Grid:** Color-coded date indicators for scheduled tasks and active habits.
- **Date Agenda Timeline:** Chronological commitments for selected date with direct navigation to task/habit details.

### Screen 6: Tools Screen (14 Calculators)
- **All 14 Utilities:** Attendance Planner, Break-Even Estimator, CGPA to Percentage, Date & Age Calculator, Discount & Sale Calculator, Exam Countdown, GST & Tax Calculator, Loan & EMI Calculator, Margin & Markup, Percentage Calculator, Pomodoro Planner, Profit & Loss, Quick Unit Converter, Reading Time Estimator.
- **Sort & Search:** Live search bar and `GlassMenu` sorting (by Name, Category, Usage).
- **Zero NaN/Infinity Protection:** Inputs coerce to valid ranges with zero-division safeguards and formatted currency/decimal outputs.

### Screen 7: Insights Screen (Reports Screen)
- **Bottom Navigation Overlap Fix:** Guaranteed bottom clearance (`FloatingNavContentBottomPadding + 24.dp`) ensuring the "Generate Visual Report" button is fully visible and interactable above the navigation dock.
- **AI Productivity Advisor:** Driven by real Room SQLite metrics with offline deterministic fallback engine.
- **Period Switching:** Weekly and Monthly views with period navigation and date boundary calculations.

### Screen 8: Quick Create Sheet
- **Modal Glass Interface:** Opened via center `+` button. Displays "New Task", "New Habit", and "Short-term Task" with tactile glass rows.
- **Insets & Dismissal:** Back-press handling, tap-outside dismissal, and keyboard clearance.

### Screen 9: Settings & Diagnostics
- **Diagnostics:** Real-time health probes (Supabase endpoint latency, Room database row counts, app release version) with zero private key exposure.
- **Data Export:** Clean JSON serialization of tasks, subtasks, habits, and completion records with Android FileProvider sharing.

---

## 6. Verification & Build Criteria

- **Framework:** Native Android Jetpack Compose (Kotlin 2.2.10, Android Compile SDK 36, Target SDK 35, Min SDK 24).
- **Test Suite:** 33/33 unit tests passing in `./gradlew testDebugUnitTest`.
- **Release Compilation:** Signed release APK built with Google R8 minification via `./gradlew syncReleaseApk`.
- **Outputs:** `TDLxHT.2.4.3.apk`, `TDlxHT.apk`, and `dist/apk/app-liquid-glass-updated.apk`.
