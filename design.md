# TDlxHT Master Design System & UI/UX Compendium (`design.md`)

> **Single Source of Truth:** This document consolidates all UI, UX, styling, design tokens, component specifications, physics models, theme guidelines, and screen architectures across the **Android Native App** and the **Web Platform**.
> 
> **Merged Compendium of:** `designglass.md`, `DESIGN.md`, `DESIGN-Light_Mode.md`, `DESIGN-Dark_Mode.md`, `TDLxHT_UI_UX_Redesign.md`, and App Icon assets.

---

## 📑 Table of Contents

1. [Executive Design Vision & Philosophy](#1-executive-design-vision--philosophy)
2. [Centralized Design Tokens & Color Architecture](#2-centralized-design-tokens--color-architecture)
   - 2.1 [Token Mapping Matrix](#21-token-mapping-matrix)
   - 2.2 [CSS Custom Property Bridges](#22-css-custom-property-bridges)
   - 2.3 [Surface Colors & Opacities](#23-surface-colors--opacities)
   - 2.4 [Geometry & Corner Radii Scale](#24-geometry--corner-radii-scale)
   - 2.5 [Typography Scale](#25-typography-scale)
   - 2.6 [Spacing System](#26-spacing-system)
3. [Surface Hierarchy & Physical Motion System](#3-surface-hierarchy--physical-motion-system)
   - 3.1 [5-Tier Surface Hierarchy](#31-5-tier-surface-hierarchy)
   - 3.2 [Kern Society & Dheeraj Carpenter Kinetic Motion](#32-kern-society--dheeraj-carpenter-kinetic-motion)
   - 3.3 [Motion Hierarchy & Touch Curves](#33-motion-hierarchy--touch-curves)
   - 3.4 [GPU-Conscious & Anti-AI-Slop Rules](#34-gpu-conscious--anti-ai-slop-rules)
4. [Liquid Glass Component Suite](#4-liquid-glass-component-suite)
   - 4.1 [`GlassButton`](#41-glassbutton)
   - 4.2 [`GlassCard` & `SketchCard`](#42-glasscard--sketchcard)
   - 4.3 [`GlassSwitch`](#43-glassswitch)
   - 4.4 [`GlassSlider`](#44-glassslider)
   - 4.5 [`GlassInput` & `GlassSearchBar`](#45-glassinput--glasssearchbar)
   - 4.6 [`GlassSheet` (Modal Bottom Sheet)](#46-glasssheet-modal-bottom-sheet)
   - 4.7 [`GlassNav` (120fps Floating Stadium Dock)](#47-glassnav-120fps-floating-stadium-dock)
   - 4.8 [`GlassSegmented` (Filter & Tab Pill)](#48-glasssegmented-filter--tab-pill)
   - 4.9 [`GlassMenu` & `GlassMenuItem`](#49-glassmenu--glassmenuitem)
5. [Light Mode Specification (Mobbin Analysis)](#5-light-mode-specification-mobbin-analysis)
   - 5.1 [Philosophy & Character](#51-philosophy--character)
   - 5.2 [Color Tokens & Roles](#52-color-tokens--roles)
   - 5.3 [Typography & Hierarchy](#53-typography--hierarchy)
   - 5.4 [Geometry & Surfaces](#54-geometry--surfaces)
   - 5.5 [Do's and Don'ts](#55-dos-and-donts)
6. [Dark Mode Specification (Bugatti Analysis)](#6-dark-mode-specification-bugatti-analysis)
   - 6.1 [Philosophy & Precision](#61-philosophy--precision)
   - 6.2 [Color Tokens & Depth](#62-color-tokens--depth)
   - 6.3 [Typography & Hierarchy](#63-typography--hierarchy)
   - 6.4 [Surface Layering & Contrast](#64-surface-layering--contrast)
   - 6.5 [Do's and Don'ts](#65-dos-and-donts)
7. [Screen-by-Screen UX Matrix & Bug Resolutions](#7-screen-by-screen-ux-matrix--bug-resolutions)
   - 7.1 [Screen 1: Home / Today Screen](#71-screen-1-home--today-screen)
   - 7.2 [Screen 2: Productivity Screen](#72-screen-2-productivity-screen)
   - 7.3 [Screen 3: Tasks Screen & Task Detail Edit Screen](#73-screen-3-tasks-screen--task-detail-edit-screen)
   - 7.4 [Screen 4: Habits Screen & Habit Detail Edit Screen](#74-screen-4-habits-screen--habit-detail-edit-screen)
   - 7.5 [Screen 5: Calendar Screen](#75-screen-5-calendar-screen)
   - 7.6 [Screen 6: Tools Screen (14 Built-In Calculators)](#76-screen-6-tools-screen-14-built-in-calculators)
   - 7.7 [Screen 7: Insights Screen (Bottom Nav Overlap Fix)](#77-screen-7-insights-screen-bottom-nav-overlap-fix)
   - 7.8 [Screen 8: Global Quick Create Modal Sheet](#78-screen-8-global-quick-create-modal-sheet)
   - 7.9 [Screen 9: Settings & Diagnostics](#79-screen-9-settings--diagnostics)
8. [App Icon System & Launcher Assets](#8-app-icon-system--launcher-assets)

---

## 1. Executive Design Vision & Philosophy

TDLxHT pairs an artisan hand-drawn sketch aesthetic with realistic **Apple-Style Liquid Glass**, delivering a tactile, focused productivity companion.

```
┌────────────────────────────────────────────────────────┐
│               APPLE-STYLE LIQUID GLASS                 │
│  Translucent Glass Surfaces • Specular Edge Bevels     │
│  Layered Depth • Top-Arc Sheen • Natural Light Refract │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────┴─────────────────────────────┐
│             KERN SOCIETY PHYSICAL KINETICS             │
│  Elastic Volume-Preserving Squash (scaleX: 1.06, Y: 0.93)│
│  High-Tension Spring Snap (Damping: 0.54, Stiff: 450)  │
│  Subtle 3D Perspective Tilt • Tactile Haptic Ticks     │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────┴─────────────────────────────┐
│          STRICT MONOCHROMATIC BASE PALETTE             │
│  Dark Mode: Bugatti Precision Pure-Black (#151515)     │
│  Light Mode: Mobbin Gallery Minimalism (#FFFFFF/#F5)   │
└────────────────────────────────────────────────────────┘
```

### Core Tenets:
1. **Translucency with Functional Purpose:** Glass surfaces are not decorative overlays; they bend and refract the scrolling content beneath them, establishing optical hierarchy and spatial anchoring.
2. **Specular Light Edge Highlights:** Physical surfaces feature a 1px top-to-bottom specular highlight gradient (`Brush.verticalGradient(listOf(Color(0x35FFFFFF), Color(0x0EFFFFFF)))`), simulating realistic glass bevels.
3. **Volume-Preserving Elastic Feedback:** Touch down invokes a realistic physical compression (expanding horizontally while compressing vertically) rather than a uniform, cartoonish shrink.
4. **Guaranteed Contrast & Legibility:** Minimum text-to-surface contrast ratio of **4.5:1** for body text and **3:1** for large titles (WCAG AA/AAA compliant).
5. **Zero AI-Slop:** Completely free of rainbow glow borders, decorative floating blobs, oversaturated drop shadows, and unmotivated parallax shifts.

---

## 2. Centralized Design Tokens & Color Architecture

### 2.1 Token Mapping Matrix

| Design Token | Role / Description | Light Mode (Mobbin) | Dark Mode (Bugatti) |
| :--- | :--- | :--- | :--- |
| `CanvasBackground` | Root window background | `#FFFFFF` | `#151515` |
| `CardBackground` | Primary surface container | `#F6F7F9` (92% glass) | `#252528` (84% glass) |
| `CardBorder` | Specular bevel rim | `rgba(0,0,0,0.08)` | `rgba(255,255,255,0.12)` |
| `InkPrimary` | High-contrast body & headers | `#1A1A1A` | `#F5F5F7` |
| `InkSecondary` | Metadata, subtitles, timestamps | `#6B7280` | `#A1A1A6` |
| `InkMuted` | Dividers, disabled controls | `#D1D5DB` | `#48484A` |
| `GlassDockFill` | Floating navigation background | `rgba(250,251,253,0.87)`| `rgba(32,32,34,0.85)` |
| `GlassSpecularRim`| Bevel edge top-to-bottom | `rgba(255,255,255,0.80)`| `rgba(255,255,255,0.22)`|
| `PrimaryAccentLime`| Task flags / primary active | `#0066FF` | `#CCFF00` |
| `AccentTerracotta` | Habit streaks / warm highlights | `#E05638` | `#FF6B4A` |

### 2.2 CSS Custom Property Bridges

```css
:root, [data-theme="light"] {
  --color-canvas: #ffffff;
  --color-canvas-soft: #f6f7f9;
  --color-surface-card: rgba(255, 255, 255, 0.92);
  --color-ink: #141414;
  --color-ink-soft: #6b7280;
  --color-text-muted: #9ca3af;
  --color-hairline: rgba(0, 0, 0, 0.08);
  --color-hairline-specular: rgba(255, 255, 255, 0.9);
  --color-primary: #141414;
  --color-on-primary: #ffffff;
  --color-accent: #0066ff;
  --color-glass-fill: rgba(255, 255, 255, 0.82);
  --color-glass-border: rgba(0, 0, 0, 0.08);
  --glass-blur: blur(24px) saturate(190%);
}

[data-theme="dark"] {
  --color-canvas: #151515;
  --color-canvas-soft: #1c1c1e;
  --color-surface-card: rgba(37, 37, 40, 0.84);
  --color-ink: #f5f5f7;
  --color-ink-soft: #a1a1a6;
  --color-text-muted: #636366;
  --color-hairline: rgba(255, 255, 255, 0.12);
  --color-hairline-specular: rgba(255, 255, 255, 0.28);
  --color-primary: #f5f5f7;
  --color-on-primary: #151515;
  --color-accent: #ccff00;
  --color-glass-fill: rgba(37, 37, 40, 0.78);
  --color-glass-border: rgba(255, 255, 255, 0.14);
  --glass-blur: blur(28px) saturate(180%);
}
```

### 2.3 Surface Colors & Opacities
- **Glass Fill Opacity:** 78%–88% for primary cards; 85%–90% for floating bars and sheets.
- **Backdrop Blur:** 24dp–32dp with 180% saturation boost.
- **Top Arc Sheen:** `Brush.verticalGradient(listOf(Color(0x28FFFFFF), Color.Transparent))` over top 45% of cards.

### 2.4 Geometry & Corner Radii Scale
- `Radius.None`: `0.dp`
- `Radius.Micro`: `4.dp` (badges, status tags)
- `Radius.Small`: `8.dp` (chips, pill controls)
- `Radius.Medium`: `14.dp` (cards, list rows, form fields)
- `Radius.Large`: `18.dp` (hero cards, tool calculators)
- `Radius.Sheet`: `24.dp` (modal bottom sheets)
- `Radius.Pill`: `9999.dp` (`CircleShape` for FAB, buttons, floating nav dock)

### 2.5 Typography Scale

Font Family: **SF Pro** (Jetpack Compose native) / **Geist Sans** (Web companion).

| Role | Font Size | Line Height | Weight | Letter Spacing |
| :--- | :--- | :--- | :--- | :--- |
| **Hero Display** | `32.sp` | `38.sp` | `Bold (700)` | `-0.8.sp` |
| **Headline 1** | `24.sp` | `30.sp` | `Bold (700)` | `-0.5.sp` |
| **Headline 2** | `20.sp` | `26.sp` | `SemiBold (600)`| `-0.3.sp` |
| **Title Medium** | `17.sp` | `22.sp` | `SemiBold (600)`| `-0.2.sp` |
| **Body Regular** | `15.sp` | `20.sp` | `Normal (400)` | `0.sp` |
| **Body Medium** | `15.sp` | `20.sp` | `Medium (500)` | `0.sp` |
| **Caption** | `13.sp` | `17.sp` | `Normal (400)` | `+0.1.sp` |
| **Micro / Tag** | `11.sp` | `14.sp` | `Bold (700)` | `+0.4.sp` |

### 2.6 Spacing System
Base module: **4dp** grid.
`4dp` (xs) · `8dp` (sm) · `12dp` (md) · `16dp` (standard edge margin) · `20dp` (card padding) · `24dp` (section clearance) · `32dp` (hero padding).
**Floating Dock Bottom Clearance:** `155.dp` (`FloatingNavContentBottomPadding`) across all scrollable lists.

---

## 3. Surface Hierarchy & Physical Motion System

### 3.1 5-Tier Surface Hierarchy

```
┌────────────────────────────────────────────────────────┐
│ Level 5: Modal Sheets & Dialogs (GlassSheet)           │
│ Elevation: 16dp • Blur: 36dp • Top Radius: 24dp        │
├────────────────────────────────────────────────────────┤
│ Level 4: Floating Navigation Dock (GlassNav)           │
│ Elevation: 12dp • Blur: 28dp • Shape: CircleShape      │
├────────────────────────────────────────────────────────┤
│ Level 3: Interactive Controls (GlassButton, Switch)    │
│ Elevation: 4dp  • Specular Rim: 1.2dp                  │
├────────────────────────────────────────────────────────┤
│ Level 2: Surface Cards (GlassCard, SketchCard)         │
│ Elevation: 2dp–4dp • Radius: 16dp–18dp • Specular Bevel│
├────────────────────────────────────────────────────────┤
│ Level 1: Canvas Background (Paper Dot / Deep Void)     │
│ Dark: #151515 • Light: #FFFFFF                         │
└────────────────────────────────────────────────────────┘
```

### 3.2 Kern Society & Dheeraj Carpenter Kinetic Motion

Implemented in `com.example.ui.components.SketchComponents.kt`:

```kotlin
object PhysicalMotionSpecs {
  // Volume-conserving press on interactive cards & buttons
  val TactilePress = spring<Float>(dampingRatio = 0.65f, stiffness = 500f)
  
  // Spatial sheet slide-in
  val SpatialSlide = spring<Float>(dampingRatio = 0.75f, stiffness = 380f)
  
  // High-tension elastic snap with volume overshoot
  val KernKineticOvershoot = spring<Float>(dampingRatio = 0.54f, stiffness = 450f)
  
  // Interactive 3D perspective depth tilt
  val DheerajTiltSpring = spring<Float>(dampingRatio = 0.68f, stiffness = 380f)
}
```

- **`Modifier.elasticStretchPress`:** Volume-conserving physical compression. Expands horizontally (`scaleX = 1.06f`) while compressing vertically (`scaleY = 0.93f`) on press down, releasing with snap overshoot (`dampingRatio = 0.54f, stiffness = 450f`).
- **`Modifier.kernSquashCounter`:** Counters and streak badges perform a volume-conserving elastic pop on value increments.
- **`Modifier.tactile3DDepth`:** 3D perspective depth with dynamic tilt and multi-tier ambient shadow.

### 3.3 Motion Hierarchy & Touch Curves

| Interaction Level | Scale Down | Pressed Alpha | Applied Elements |
| :--- | :--- | :--- | :--- |
| **MICRO** | `0.97f` | `0.94f` | Small chips, category tags, icons |
| **SMALL** | `0.94f` | `0.88f` | Standard buttons, checkboxes, list rows |
| **MEDIUM** | `0.92f` | `0.84f` | Hero action CTA, FAB, Quick Add (+) |
| **MINIMAL** | `0.985f`| `0.96f` | Navigation dock destination tabs |

### 3.4 GPU-Conscious & Anti-AI-Slop Rules
1. **Zero Layout Thrashing:** Nav dock uses constant $O(1)$ layout geometry (5 fixed-width slots). Avoided runtime `animateContentSize` on parent containers that triggers measure passes across sibling composables.
2. **GPU Layer Rendering:** All transforms operate strictly through `graphicsLayer { alpha = ...; scaleX = ...; scaleY = ... }`.
3. **Reduced Motion Respect:** When system `ANIMATOR_DURATION_SCALE == 0f`, animations complete instantly without visual delay.

---

## 4. Liquid Glass Component Suite

### 4.1 `GlassButton`
- **Icon + Label Pattern:** `<GlassButton icon={<PlusIcon />}>Add Task</GlassButton>`.
- **Icon-Only Pattern:** `<GlassButton icon={<PlusIcon />} contentDescription="Add" size={38.dp} />`.
- **Styling:** Stadium pill (`CircleShape`) with translucent glass fill, 1px vertical gradient specular bevel, ambient shadow, and spring press.

### 4.2 `GlassCard` & `SketchCard`
- **Geometry:** 18dp corner radius with multi-tier ambient shadow.
- **Lighting:** Specular top arc highlight sheen rendered via `drawRoundRect(brush = Brush.verticalGradient(...))` across top 45%.
- **Edge:** 1dp vertical gradient rim (`Color(0x35FFFFFF)` to `Color(0x0EFFFFFF)`).

### 4.3 `GlassSwitch`
- **Track:** 50dp width × 30dp height stadium track with specular border.
- **Thumb:** 24dp white glass disc with spring translation (`dampingRatio = 0.75f, stiffness = 500f`).
- **Haptic:** Tactile tick fired upon state toggle.

### 4.4 `GlassSlider`
- **Track:** 6dp translucent glass track.
- **Fill:** High-contrast specular sheen fill.
- **Thumb:** 22dp circular floating glass disc with active elevation lift.

### 4.5 `GlassInput` & `GlassSearchBar`
- **Geometry:** 14dp rounded rectangle with 1.2dp border.
- **Focus:** Subtle luminous glow rim and elevated contrast.
- **Controls:** Embedded leading search icon slot and trailing tactile clear button (`Icons.Default.Close`).

### 4.6 `GlassSheet` (Modal Bottom Sheet)
- **Geometry:** 24dp top corner radius.
- **Header:** Centered 36dp × 4dp drag handle pill with specular edge.
- **Dismissal:** Back-press handling, tap-outside scrim dismissal, and soft slide-down spring.

### 4.7 `GlassNav` (120fps Floating Stadium Dock)
- **Geometry:** Floating stadium pill (`CircleShape`), 64dp height, 16dp horizontal margins.
- **Layout:** 4 navigation destinations (`Home`, `Productivity`, `Tools`, `Insights`) + center elevated circular Quick Add (`+`) button.
- **Sliding Indicator:** GPU-accelerated liquid pill indicator transitioning between tabs at locked 120fps.
- **Content Clearance:** All scrollable screens provide `FloatingNavContentBottomPadding` (`155.dp` + insets) to guarantee zero overlap with actionable buttons.

### 4.8 `GlassSegmented` (Filter & Tab Pill)
- **Geometry:** Enclosed glass pill container with sliding thumb selection indicator.
- **States:** Smooth spring translation when selecting filters (`All`, `Tasks`, `Habits`).

### 4.9 `GlassMenu` & `GlassMenuItem`
- **Geometry:** Translucent glass floating popover with 14dp radius and ambient shadow.
- **Items:** 44dp minimum touch target with icon slot, text label, and spring press feedback.

---

## 5. Light Mode Specification (Mobbin Analysis)

### 5.1 Philosophy & Character
Mobbin-inspired gallery minimalism. Clean, crisp, high-key white aesthetic with paper-like tactile subtle contrast.

### 5.2 Color Tokens & Roles
- **Canvas:** `#FFFFFF` pure white background.
- **Card Surface:** `#F6F7F9` (subtle off-white glass with 8% opacity).
- **Ink Primary:** `#141414` (deep charcoal-black, WCAG 14:1 contrast).
- **Ink Secondary:** `#6B7280` (neutral gray, WCAG 4.8:1 contrast).
- **Hairline Border:** `rgba(0, 0, 0, 0.08)` (subtle separation).
- **Specular Highlight:** `rgba(255, 255, 255, 0.90)` (crisp top reflection).

### 5.3 Typography & Hierarchy
- Strong, black typography with generous whitespace.
- Hero headings: 32sp Bold with `-0.8sp` tracking.
- Zero decorative cursive or playful handwriting fonts; pure structural clarity.

### 5.4 Geometry & Surfaces
- Flat surfaces layered with soft 2dp–4dp ambient shadows.
- Thin 1px borders to separate content without heavy outlines.

### 5.5 Do's and Don'ts
- **DO:** Maintain generous white margins around cards and headers.
- **DO:** Use crisp black icons and typography on white/light grey cards.
- **DON'T:** Use heavy dark drop shadows that look muddy on light backgrounds.
- **DON'T:** Use saturated background tints behind text blocks.

---

## 6. Dark Mode Specification (Bugatti Analysis)

### 6.1 Philosophy & Precision
Bugatti-inspired pure-black precision. Deep, focused, zero-bleed dark environment with engineered glass highlights.

### 6.2 Color Tokens & Depth
- **Canvas:** `#151515` deep charcoal void.
- **Glass Card:** `#252528` at 84% opacity with 28dp backdrop blur.
- **Ink Primary:** `#F5F5F7` off-white (WCAG 13:1 contrast against `#151515`).
- **Ink Secondary:** `#A1A1A6` muted slate (WCAG 5.2:1 contrast).
- **Specular Border Rim:** `Brush.verticalGradient(listOf(Color(0x35FFFFFF), Color(0x0EFFFFFF)))`.
- **Top Arc Sheen:** `Color(0x28FFFFFF)` fading to transparent at 45% card height.

### 6.3 Typography & Hierarchy
- High contrast without harsh pure `#FFFFFF` eye fatigue.
- Clear structural weights (Bold 700 for numbers and titles, SemiBold 600 for card headers).

### 6.4 Surface Layering & Contrast
- Cards rely on specular highlights and subtle elevation differences rather than light gray fills.
- High touch targets (minimum 44dp × 44dp).

### 6.5 Do's and Don'ts
- **DO:** Use subtle specular top highlights to define card edges against the black background.
- **DO:** Keep contrast high on small metadata and timestamps.
- **DON'T:** Use harsh pure blue or neon glows.
- **DON'T:** Lighten the background to a washed-out mid-gray.

---

## 7. Screen-by-Screen UX Matrix & Bug Resolutions

### 7.1 Screen 1: Home / Today Screen
- **Daily Summary Hero Glass Card:** Current date, completed fraction, completion percentage bar, and streak badge in a unified glass card.
- **`GlassSegmented` Filter:** `All`, `Tasks`, `Habits` with real synchronized data filtering.
- **Today's Tasks & Habits:** Clear section headers with view mode toggle and labeled `GlassButton` actions (`<PlusIcon /> Add Task`, `<PlusIcon /> Add Habit`).
- **Clean Empty States:** Intentional layout with action button and clear guidance, eliminating unused whitespace.

### 7.2 Screen 2: Productivity Screen
- **Dual Tab Architecture:** Tasks, Habits, and Calendar integrated into a unified container.
- **Bottom GlassSegmented Bar:** Positioned cleanly above the main bottom navigation dock with a centered separator hairline and complete scroll clearance.

### 7.3 Screen 3: Tasks Screen & Task Detail Edit Screen
- **Task List:** Status filters (`All`, `Today`, `Upcoming`, `Completed`), search filter, priority flags, and subtask completion counters.
- **Task Detail Form:** Standardized `AppHeader` with `GlassButton` Save action. Clean inputs for title, notes, due date, category chips, priority chips, reminder toggles, and repeat rules.
- **Validation:** Clear error toast on empty title; duplicate submission prevention.

### 7.4 Screen 4: Habits Screen & Habit Detail Edit Screen
- **Habit List:** Habit cards with frequency schedules, current streaks, best streaks, and daily completion checkboxes.
- **Habit Detail Form:** Standardized `GlassButton` Save action. Inputs for name, notes, category chips, frequency selector (`Every Day`, `Weekdays`, `Weekly`, `Custom`), target value steppers, and reminder time pickers.
- **Validation:** Habit name validation; prevents duplicate submissions; persistent schedule updates.

### 7.5 Screen 5: Calendar Screen
- **Month / Week / Day Switcher:** Smooth glass tab switching.
- **Interactive Calendar Grid:** Color-coded date indicators for scheduled tasks and active habits.
- **Date Agenda Timeline:** Chronological commitments for selected date with direct navigation to task/habit details.

### 7.6 Screen 6: Tools Screen (14 Built-In Calculators)
- **All 14 Utilities:** Attendance Planner, Break-Even Estimator, CGPA to Percentage, Date & Age Calculator, Discount & Sale Calculator, Exam Countdown, GST & Tax Calculator, Loan & EMI Calculator, Margin & Markup, Percentage Calculator, Pomodoro Planner, Profit & Loss, Quick Unit Converter, Reading Time Estimator.
- **Sort & Search:** Live search bar and `GlassMenu` sorting (by Name, Category, Usage).
- **Zero NaN/Infinity Protection:** Inputs coerce to valid ranges with zero-division safeguards and formatted currency/decimal outputs.

### 7.7 Screen 7: Insights Screen (Bottom Nav Overlap Fix)
- **Bottom Navigation Overlap Resolution:** Configured `contentPadding` with `FloatingNavContentBottomPadding + 32.dp` and appended a `32.dp` spacer to [`ReportsScreen.kt`](file:///Users/dheerajcarpenter/Coding%20Projects/TDlxHT/app/src/main/java/com/example/ui/screens/reports/ReportsScreen.kt), guaranteeing the "Generate Visual Report" button is 100% visible and interactable above the floating dock.
- **AI Productivity Advisor:** Real Room SQLite metrics feed with offline deterministic fallback engine.
- **Period Switching:** Weekly and Monthly views with period navigation and date boundary calculations.

### 7.8 Screen 8: Global Quick Create Modal Sheet
- **Modal Glass Interface:** Opened via center `+` button in [`MainActivity.kt`](file:///Users/dheerajcarpenter/Coding%20Projects/TDlxHT/app/src/main/java/com/example/MainActivity.kt). Displays "New Task", "New Habit", and "Short-term Task" with tactile glass rows.
- **Insets & Dismissal:** Back-press handling, tap-outside dismissal, and keyboard clearance.

### 7.9 Screen 9: Settings & Diagnostics
- **Diagnostics:** Real-time health probes (Supabase endpoint latency, Room database row counts, app release version) with zero private key exposure.
- **Data Export:** Clean JSON serialization of tasks, subtasks, habits, and completion records with Android FileProvider sharing.

---

## 8. App Icon System & Launcher Assets

### Transparent Vector Emblem
- **Iconography:** Minimalist white task checklist with cyclical plant leaf emblem symbolizing productivity and sustainable habit discipline.
- **Geometry:** Centered with Android adaptive safe-zone padding (66% diameter).
- **Background:** Sophisticated dark `#252525` depth background with subtle radial graduation and 14% specular rim edge highlight (`rgba(255, 255, 255, 0.14)`).

### Mipmap Densities Generated
- `mipmap-mdpi`: 48 × 48 px
- `mipmap-hdpi`: 72 × 72 px
- `mipmap-xhdpi`: 96 × 96 px
- `mipmap-xxhdpi`: 144 × 144 px
- `mipmap-xxxhdpi`: 192 × 192 px
- Adaptive foreground: `android/adaptive-foreground.png`
- Circular launchers: `ic_launcher_round.png` across all densities.
- Web assets: `web/favicon.png` (512 × 512 px) and `web/app-icon.png` (512 × 512 px).
