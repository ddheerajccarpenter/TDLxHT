# TDlxHT Release Notes — Version 2.5.3 (Build 40)

**Release Date:** 2026-10-31  
**Application ID:** `com.aistudio.habittask.wvnk`  
**Target Platform:** Android (Compile SDK 36.1, Target SDK 35, Min SDK 24)  
**Binary Deliverables:** `TDLxHT.2.5.3.apk`, `TDlxHT.apk`, `dist/apk/app-liquid-glass-updated.apk`

---

## 🚀 Release Highlights — Neon Cloud Infrastructure, Solid Grey UI & Interactive Motion Components

TDlxHT 2.5.3 integrates Neon serverless cloud primitives, adds interactive spring-damped touch edge glow and progress ring checkmark animations, and overhauls all dark backgrounds to solid charcoal grey with opaque solid buttons and cards across Android and the web companion.

### 1. Neon Cloud Integration & Serverless Postgres
* **Neon Project Linking:** Linked project `patient-waterfall-08736018` under organization `DHEERAJ` (`org-square-surf-76889185`) on branch `production`.
* **Serverless Postgres Driver:** Integrated `@neondatabase/serverless` connection pool in `api/db.js` with live SQL health verification in `api/status.js`.
* **Declared Services:** Scaffolded `neon.ts` managing Serverless Postgres, Auth, AI Gateway, Functions, and Object Storage.

### 2. TouchEdgeGlowButton (Spring Physics & Ink Wash Caustics)
* **Harmonic Edge Glow:** Dynamic edge illumination running on damped spring physics (`FREQUENCY = 3.4`, `DAMPING = 0.78`) tracking finger touch and mouse hover coordinates.
* **Palette Match:** Illuminated in Slate Steel (`#6D8196`) and Warm Ivory (`#FFFFE3`) with solid button cores.
* **Multi-Platform:** Implemented in Jetpack Compose (`TouchEdgeGlowButton`) and Web (`cursor-edge-glow.js`).

### 3. ProgressRingCheck (Morphing Task Completion)
* **Dashed Idle Ring:** Uncompleted tasks feature an 8-dash circular stroke circumference in Mist Silver (`#CBCBCB`).
* **Arc Sweep & Checkmark Morph:** Tapping a task initiates an animated sweep arc that smoothly morphs into a checkmark with tactile haptic confirmation.
* **Integrated Everywhere:** Replaces standard checkboxes in Android's `HomeScreen` and `TaskListScreen` as well as the interactive web companion.

### 4. Solid Grey Backgrounds & Solid Buttons (No Transparency)
* **Solid Charcoal Grey Canvas:** Replaced all pitch-black backgrounds with solid grey (`#1E2022` canvas, `#2B2D30` cards, `#35383C` elevated sheets).
* **Solid Buttons:** Converted translucent glass buttons and ghost outlines into solid high-contrast buttons (`#4A4A4A` / `#FFFFE3` / `#6D8196`).
* **Solid Navigation Stadium Dock:** Upgraded the bottom navigation bar to an opaque solid grey dock with a solid border, removing all unwanted blur bleed.

---

# TDlxHT Release Notes — Version 2.5.2 (Build 39)

**Release Date:** 2026-10-31  
**Application ID:** `com.aistudio.habittask.wvnk`  
**Target Platform:** Android (Compile SDK 36.1, Target SDK 35, Min SDK 24)  
**Binary Deliverables:** `TDLxHT.2.5.2.apk`, `TDlxHT.apk`, `dist/apk/app-liquid-glass-updated.apk`

---

## 🚀 Release Highlights — Universal Ink Wash Theme, Performance Optimization & Credential Sanitization

TDlxHT 2.5.2 introduces a complete universal styling overhaul using an authentic Ink Wash Japanese parchment and charcoal aesthetic across Android and web, eliminates display latency and GPU thrash, cleans all visible credentials into secure external configuration, and refines list spacing.

### 1. Universal Ink Wash Palette (Android & Web)
* **Authentic Ink & Parchment Tokens:**
  * Charcoal Slate (`#4A4A4A`): Primary deep neutral, high-contrast dark text in light mode, primary action buttons.
  * Mist Silver (`#CBCBCB`): Subtle hairlines, dividers, and muted text.
  * Warm Ivory (`#FFFFE3`): Parchment canvas in light mode, glowing text in dark mode.
  * Slate Steel Wash (`#6D8196`): Universal accent, focus timer arcs, gauge sweeps, and active pills.
* **Elimination of Saturated Slop Colors:** Removed all neon purple, vibrant green, and oversaturated cyan across the app and web landing page.
* **Tonal Dark Mode:** Dark mode strictly uses deep charcoal tones (`#1E2022`, `#2B2D30`) ensuring dark mode is perceptibly darker than light mode.

### 2. Elimination of Scroll Lag & High Refresh Rate Unlock
* **Skia Blur Pass Removal:** Eliminated active `RenderEffect.createBlurEffect` continuous re-allocations from `motionBlurOnScroll` during LazyColumn scrolling.
* **Unlocked Native High Refresh Rate:** Removed the 60 FPS clamp (`preferredRefreshRate = 60f`), enabling native 90Hz and 120Hz LTPO display fluidity.
* **Settings Content Padding:** Added +48dp bottom clearance to prevent any navigation dock collision with footer controls.

### 3. Credential & Key Sanitization
* **Secure Property Loading:** Removed all hardcoded API keys, Supabase JWTs, and keystore passwords from `app/build.gradle.kts`, Supabase edge functions, serverless API routes, and web scripts into `local.properties` and environment variables.

---

# TDlxHT Release Notes — Version 2.5.1 (Build 38)

**Release Date:** 2026-10-31  
**Application ID:** `com.aistudio.habittask.wvnk`  
**Target Platform:** Android (Compile SDK 36.1, Target SDK 35, Min SDK 24)  
**Binary Deliverables:** `TDLxHT.2.5.1.apk`, `TDlxHT.apk`, `dist/apk/app-liquid-glass-updated.apk`

---

## 🚀 Release Highlights — Complete UI Overhaul, Perceptually Uniform OKLCH Palette & Settings Rebuild

TDlxHT 2.5.1 delivers a complete, modern UI redesign across all screens, integrating the `jakubkrehel/oklch-skill` and `jakubkrehel/skills` systems, introducing Bento grid dashboards, Circular Arc Progress Gauges, Momentum Flame badges, and completely eliminating card overlap bugs in Settings.

### 1. Settings Screen Zero-Overlap Rebuild (Fix for Card Collisions)
* **Apple iOS 18 / Linear Grouped Inset Lists:** Replaced disparate stacked cards with single-container `GroupedInsetCard` layouts with hairline dividers.
* **Top Profile Hero:** Clean single account hero card displaying user avatar, email, cloud sync status, "Sync Now" action, and "Sign Out".
* **Eliminated Duplicate About App Cards:** Fully removed duplicate About cards and rogue `Box` containers that caused overlapping elements in previous builds.

### 2. Perceptually Uniform OKLCH Palette
* **Semantic Tones:** Integrated high-contrast, WCAG AAA-compliant OKLCH colors:
  * Electric Indigo (`#6366F1`) for primary actions and active states.
  * Emerald Success (`#10B981`) for habit completion and positive momentum.
  * Amber Flame (`#F59E0B`) for streaks and active timers.
  * Coral Alert (`#EF4444`) for high-priority items and destructive actions.
  * Focus Cyan (`#0EA5E9`) for Pomodoro timer sweeps.
  * Analytics Purple (`#A855F7`) for Executive Scores and Insights.

### 3. Today Bento Dashboard & Circular Arc Gauges
* **Bento Hero:** Modern Bento layout on Today screen featuring GPU-drawn `CircularProgressGauge` showing overall completion %, accompanied by `StreakFlameBadge`.
* **Priority Spotlight Card:** Automatic top-level spotlight for uncompleted High/Medium priority tasks scheduled for today.

### 4. Interactive Focus Timer with Smooth Sweep Arcs
* **Circular Sweep Arc Gauge:** Transformed the Pomodoro modal with a dynamic sweep arc indicating remaining focus session time in crisp OKLCH Cyan and Indigo.

---

# TDlxHT Release Notes — Version 2.5.0 (Build 37)

**Release Date:** 2026-10-31  
**Application ID:** `com.aistudio.habittask.wvnk`  
**Target Platform:** Android (Compile SDK 36.1, Target SDK 35, Min SDK 24)  
**Binary Deliverables:** `TDLxHT.2.5.0.apk`, `TDlxHT.apk`, `dist/apk/app-liquid-glass-updated.apk`

---

## 🚀 Release Highlights — New Visual Identity, App Logo & Icon System

TDlxHT 2.5.0 introduces the brand's official geometric ribbon monogram icon across all native Android density buckets, launcher shortcuts, and web distribution assets.

### 1. New App Logo & Adaptive Launcher Icons
* **Geometric Ribbon Monogram:** Upgraded `app_logo.png` (512x512) with the brand's ribbon emblem on a deep `#161616` canvas with normalized OLED contrast.
* **Full Brand Lockup (`app_logo_full.png`):** Added the complete vertical brand emblem combining the ribbon mark, bold "TDLxHT" wordmark, and "LIGHTWEIGHT PRODUCTIVITY" subtitle.
* **Multi-Density Adaptive Mipmaps:** Generated squircle `ic_launcher.png` and circular `ic_launcher_round.png` across all density buckets (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) with Android safe-zone centering.
* **Vector Launcher Background:** Synchronized `ic_launcher_background.xml` to `#161616` for seamless borderless launcher integration.

---

# TDlxHT Release Notes — Version 2.4.9 (Build 36)

**Release Date:** 2026-10-31  
**Application ID:** `com.aistudio.habittask.wvnk`  
**Target Platform:** Android (Compile SDK 36.1, Target SDK 35, Min SDK 24)  
**Binary Deliverables:** `TDLxHT.2.4.9.apk`, `TDlxHT.apk`, `dist/apk/app-liquid-glass-updated.apk`

---

## 🚀 Release Highlights — Advanced Motion, Liquid Glass Dock Rebuild & 60 FPS Filmic Cadence

TDlxHT 2.4.9 delivers a complete UI/UX, animation, typography, and navigation rework, introducing physical springs, continuous stadium docking, optical motion blur, and filmic 60 FPS pacing.

### 1. Continuous Liquid Glass Bottom Dock Redesign
* **Layout Structure:** Strict 5-action order: **Home (0) → Productivity (1) → Tools (2) → Insights (3) → Plus / Add (Far Right)**.
* **Icons Only:** Removed all visible text labels for a clean, distraction-free aesthetic with full accessibility screen-reader content descriptions and min touch targets.
* **Sliding Lens Droplet:** Internal sliding Liquid Glass lens indicator with 1.10f active scale pop.
* **Far-Right Plus Action:** Fixed far-right position rotating 45° into an `X` on elastic spring rebound with zero dock layout shifting.

### 2. Motion Engine & Physics Overhaul
* **Centralized Motion Tokens:** Catalog of 20 physical motion tokens in `PhysicalMotionSpecs` (micro-interactions, bouncy press compression, sheet transitions, stroke draws).
* **Bouncy Press Modifier (`Modifier.bouncyPress`):** Immediate 0.95f compression on touch down with snappy elastic release on touch up.
* **Fast-Scroll Optical Motion Blur:** 5–10% hardware-accelerated trailing motion blur on Android 12+ (`RenderEffect.createBlurEffect`), settling to zero when stationary.
* **60 FPS Cadence:** Enforced `window.attributes.preferredRefreshRate = 60f` for filmic, rock-solid scroll cadence.
* **Floating Undo Pill:** Liquid Glass stadium undo pill auto-dismissing after 3.5 seconds.

### 3. Typography & Tool Modal Polish
* **Tabular Figures (`tnum`):** All counters, progress badges, and multi-select tallies formatted with `fontFeatureSettings = "tnum"` for fixed-width grid alignment.
* **Semantic Tool Icons:** Replaced two-letter initial circles in `ToolRunnerModal` with authentic vector tool glyphs.

---

# TDlxHT Release Notes — Version 2.4.8 (Build 35)

**Release Date:** 2026-10-31  
**Application ID:** `com.aistudio.habittask.wvnk`  
**Target Platform:** Android (Compile SDK 36.1, Target SDK 35, Min SDK 24)  
**Binary Deliverables:** `TDLxHT.2.4.8.apk`, `TDlxHT.apk`, `dist/apk/app-liquid-glass-updated.apk`

---

## 🚀 Release Highlights — Visual Polish, Taste Skill Directives & Multi-Channel Messaging

TDlxHT 2.4.8 delivers comprehensive UI bug fixes across calendar, navigation dock, tool runners, and task cards, incorporating anti-slop frontend taste skill standards and `@sentdm/sentdm` integration.

### 1. Visual Defect Resolutions
* **Calendar Overlap Fix:** Stripped duplicate productivity analytics and visual graph sections from `CalendarScreen.kt`. Added dedicated bottom content clearance (`FloatingNavContentBottomPadding + 64.dp`) ensuring agenda tasks scroll completely above the productivity segmented control and floating dock.
* **Quick-Add FAB Rotation State:** Bound center FAB's 45° rotation state strictly to `isQuickAddSheetOpen`, guaranteeing a clean `+` icon when closed and morphing to `X` only when open.
* **Floating Dock Optical Density (~92%):** Elevated floating dock base opacity to `0xEE222230` with convex caustic lensing, completely eliminating text bleed-through when scrolling in Tools and Insights.
* **Tools Modal High-Contrast Surface:** Upgraded `ToolRunnerModal` with a 78% scrim, elevated lensed glass, solid input troughs (`#15151B`), and a solid result card (`#1B1B24`) with subtle 40% terracotta border.

### 2. Taste Skill Polish (`Leonxlnx/taste-skill`)
* Standardized card borders to `0.75.dp` hairlines (`1.0.dp` for multi-selection) with `14.dp` corners.
* Replaced uppercase scream headers with clean Title Case (`Today's Tasks`, `Today's Habits`) and dynamic count badges (`completed/total`).

### 3. Multi-Channel Messaging Integration
* Integrated `@sentdm/sentdm@0.48.1` with `api/sent-messaging.js` supporting SMS, WhatsApp, and RCS reminders.

---

# TDlxHT Release Notes — Version 2.4.6 (Build 33)

**Release Date:** 2026-10-31  
**Application ID:** `com.aistudio.habittask.wvnk`  
**Target Platform:** Android (Compile SDK 36.1, Target SDK 35, Min SDK 24)  
**Binary Deliverables:** `TDLxHT.2.4.6.apk`, `TDlxHT.apk`, `dist/apk/app-liquid-glass-updated.apk`

---

## 🚀 Release Highlights — Fluid / Lensed Glass Engine Architecture

TDlxHT 2.4.6 completes the transition from flat, hollow "clear glass" outlines and dark metallic plastic to authentic **Fluid / Lensed Glass** across native Jetpack Compose and the Web companion.

### 1. Dedicated `Modifier.fluidLensedGlass` Rendering Engine
* **Active Optical Convex Lensing:** Rather than flat, see-through 2D windowpanes or solid opaque gray rectangles, components are rendered with multi-stop optical refraction bodies (`Color(0x4422222E)` to `Color(0x2C12121A)`) simulating physical light transmission through sculpted optical glass.
* **Overhead Radial Caustic Dome:** A soft radial caustic highlight (`Brush.radialGradient` centered at `(0.5w, -0.12h)`) bends overhead ambient environmental illumination across the lens curvature, providing authentic 3D optical convexity without artificial painted chrome streaks.
* **Dual-Boundary Fresnel Meniscus Rim:** Restrained 0.75dp specular hairline outer boundary paired with an internal edge reflection simulating physical glass thickness and liquid meniscus.
* **Subtle Bottom Ambient Bounce:** Soft lower radial bounce refraction (`(0.5w, 1.15h)`) captures ambient light reflecting through the base of the glass lens.

### 2. Fluid Droplet Dynamics & Volume-Conserving Kinetics
* **Fluid Gliding Tab Indicator:** `SketchSegmentedControl` and `GlassSegmented` now render the sliding indicator as an optical fluid glass lens that smoothly glides beneath active tabs with physical spring dynamics (`SpatialSlideDp`). Replaces stark opaque white sticker blocks with an authentic refracting fluid lens.
* **Elastic Squash & Stretch Press:** Interactive glass controls (`GlassButton`, `SketchFilterChip`, view switchers) respond to touch with volume-conserving fluid compression (`scaleX = 1.04f, scaleY = 0.96f` on press, snapping back with `spring(dampingRatio = 0.62f, stiffness = 420f)`).
* **Floating Stadium Pill Dock:** `SketchBottomNavigationBar` and `LiquidGlassNavItem` upgraded to fluid lensed glass with active tab droplet lenses.

### 3. Web Companion Architecture Overhaul
* **Elimination of `--clear` hollow styles:** Removed all `lg-button--clear` classes from secondary buttons, theme toggles, and sort menu triggers in `web/js/liquid-glass.js` and `web/index.html`.
* **CSS Fluid Lensed Styling:** Added radial caustic lens domes, optical body fills, `backdrop-filter: blur(28px) saturate(190%)`, and spring-press micro-interactions across all web glass components.

### 4. Verification & Testing
* **100% Automated Unit Test Pass Rate:** All 34 automated unit tests passing (`./gradlew testDebugUnitTest`).
* **Clean Release Build & Signing:** Zero compiler warnings or errors (`./gradlew syncReleaseApk`).

---

# TDlxHT Release Notes — Version 2.4.5 (Build 32)

**Release Date:** 2026-10-30  
**Application ID:** `com.aistudio.habittask.wvnk`  
**Target Platform:** Android (Compile SDK 36.1, Target SDK 35, Min SDK 24)  
**Binary Deliverables:** `TDLxHT.2.4.5.apk`, `TDlxHT.apk`, `dist/apk/app-liquid-glass-updated.apk`

---

## 🚀 Release Highlights — Critical Liquid Glass Architecture Correction

TDlxHT 2.4.5 delivers a fundamental overhaul of the underlying Liquid Glass material and rendering foundation, resolving real-device visual defects where elements previously appeared as "dark metallic plastic, glossy charcoal buttons, beveled surfaces, and gradient-filled cards."

### 1. Authentic Optical Translucency (Overcoming Opaque Metallic Plastic)
* **Translucent Glass Fills:** Reduced opaque gray pigment from 82%–88% down to authentic translucent obsidian glass (`Color(0x40181820)` ~25% alpha for content cards; `Color(0x5E14141C)` ~37% alpha for the floating dock).
* **Elimination of Painted Metallic Chrome Sheens:** Completely stripped the artificial 45% white-to-transparent gradient arcs (`drawRoundRect`) that gave components the look of curved plastic bumpers.
* **Hairline Refractive Edges:** Replaced thick 1.2dp–1.5dp borders with ultra-fine 0.75dp hairlines (`listOf(Color(0x24FFFFFF), Color(0x08FFFFFF))`), eliminating the "silver marker outline" look.
* **Soft Ambient Lighting:** Replaced heavy black drop shadows (`elevation = 18.dp`, 60% black) with soft, natural ambient lighting (`elevation = 2.dp`–`3.dp`, 16% black).
* **Optical Environmental Canvas:** Upgraded `paperDotBackground` with subtle multi-stop vertical graduation and ambient charcoal luminance pools, giving glass surfaces real underlying light to bend and transmit.

### 2. Optical Lens Segmented Controls & Cohesive Glass Pills
* **Floating Optical Lens Indicator:** Segmented controls (`SketchSegmentedControl`) no longer render active selections as stark, solid white blocks (`#F5F5F7`). Instead, the active indicator is an authentic translucent floating glass lens (`Color(0x28FFFFFF)` with a 0.7dp hairline border `Color(0x3EFFFFFF)`) with crisp white typography.
* **Glass Filter Chips:** Rebuilt `SketchFilterChip` into refined translucent glass stadium pills (`CircleShape`) with hairline rims, harmonizing Tasks, Tools, Habits, and Search.
* **Refined Glass Buttons:** Add Task, Add Habit, and action buttons converted into tactile translucent glass pills (`GlassButton`) free of painted surface gloss.

### 3. Screen-Specific Improvements & Overlap Fixes
* **Tools Screen Redesign:**
  - Replaced heavy dark slabs with clean, lightweight translucent `GlassCard` surfaces.
  - Replaced aggressive 44dp circular abbreviation badges ("AT", "BR", "CG", etc.) with refined 38dp rounded glass squircles containing contextually mapped Material Outlined vector icons.
  - Expanded `contentPadding` bottom clearance to `FloatingNavContentBottomPadding + 48.dp`, ensuring "Loan & EMI Calculator" and all 15 utilities scroll comfortably above the dock.
* **Insights / Reports Screen Redesign:**
  - Rebuilt AI Productivity Advisor card and AI Visual Report card into clean translucent glass surfaces.
  - Resolved the critical CTA overlap where "Generate Visual Report" was covered by the navigation dock. Increased bottom clearance to `FloatingNavContentBottomPadding + 48.dp` so all interactive elements remain 100% visible and unobstructed.
* **Floating Navigation Dock:**
  - Converted the floating dock from a dark metallic bumper into a truly translucent stadium pill dock with 0.75dp hairline rim and soft 8dp elevation. Content scrolling beneath is perceived softly through translucent obsidian glass.

### 4. Verification & Testing
* **Automated Unit Testing:** 34 of 34 automated unit tests passing (`./gradlew testDebugUnitTest`).
* **Deprecation Resolution:** Migrated deprecated icons to `Icons.AutoMirrored.Outlined` equivalents in `ToolsScreen.kt`.
* **Synchronized Web Companion:** Rebuilt static web bundle and updated version manifests (`npm run build`).

---

## 📦 Deliverables & Verification

* **Versioned Release APK:** `TDLxHT.2.4.4.apk` (17.3 MB / 17,337,160 bytes)
* **Root APK:** `TDlxHT.apk` (17.3 MB / 17,337,160 bytes)
* **Distribution APK:** `dist/apk/app-liquid-glass-updated.apk` (17.3 MB / 17,337,160 bytes)
* **Signing Schemes:** APK Signature Scheme v1, v2 (Full APK block), and v3 (Key rotation support) verified.

