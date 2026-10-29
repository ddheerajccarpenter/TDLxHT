# TDLxHT — The Memory & Architecture of the Product

> **CRITICAL INSTRUCTION FOR FUTURE CODING AGENTS:**
> Future coding agents must read this file before changing the project and update it whenever the architecture, features, build steps, design rules, or implementation status changes. Keep it factual and maintainable. Do not record passwords, tokens, API key values, signing secrets, personal user data, or speculative plans.

---

## 1. What is TDLxHT?

**TDLxHT** (Task Discipline & Habit Tracker) is an offline-first, native Android application and companion website engineered for focused execution.
- **Audience**: Students, developers, and professionals who prioritize zero-latency local productivity without cloud lock-in, advertising, or subscription fees.
- **Core Pillars**:
  1. **Offline-First Task Management**: High-performance task capture backed by Android Jetpack Room (SQLite) with microsecond query execution, priority tagging, and checklists.
  2. **Dual-Streak Habit Tracking**: Daily habit discipline tracking that records current consecutive streaks alongside all-time personal records.
  3. **14 Built-In Offline Calculators**: A suite of native utility tools (Loan EMI, GST Tax, Tip Splitter, CGPA-to-Percentage, Attendance, Unit Conversions, Fuel Math, etc.) integrated directly into the application.
  4. **Privacy-by-Design**: 100% of user data remains on the local device by default. Zero ads, zero trackers, zero forced logins.
  5. **Optional Cloud Synchronization**: Optional Supabase authentication gateway for users who explicitly choose to synchronize across devices.

---

## 2. Why Did We Create It?

Conventional productivity applications suffer from cloud dependency, invasive telemetry, full-screen advertisements, and subscription fatigue. TDLxHT exists to provide a distraction-free, reliable instrument that operates with the speed and privacy of a physical notebook and pen.

---

## 3. Strict Two-Color Design System

The entire website and Android application strictly adhere to an authored two-color design token system:
- **Dark Grey**: `#252525` (Canvas background, component surfaces, cards)
- **White**: `#FFFFFF` (Text, icons, borders, dividers, controls, active states, outlines)

**Color Policy Rules**:
- No pure black (`#000000`), blue, green, orange, red, purple, beige, tinted grey, additional shades, gradients, glow effects, or decorative color overlays.
- Selected and active states use reversed contrast (White `#FFFFFF` background with Dark Grey `#252525` text/icon).
- Statuses and warnings rely on explicit text labels, high-contrast typography, and icons—never color alone.
- If the theme toggle is activated, it reverses the two colors (White `#FFFFFF` canvas with Dark Grey `#252525` content).

---

## 4. Website Layout & Wireframe Parity

The website strictly implements the single-page long-form layout specified by `to update/TDLxHT_single_page_grayscale_wireframe.svg`:
1. **Compact Top Header**: Left brand mark (`TDLxHT` with launcher icon); right utility links (`GitHub`, `Portal`, working `Theme` toggle, and primary `Download APK` button).
2. **01 / LANDING PAGE (`#dashboard`)**:
   - Sublabel: `NATIVE ANDROID · V2.4.0`
   - Heading: `Tasks, habits and calculators. One offline-first app.`
   - Actions: `Download APK` (primary) and `View features` (outline linking to `#features`).
   - Desktop Preview: Structural wireframe preview showing phone silhouette, interactive task checklist, and real-time completion metrics.
   - Facts Strip: Four concise verified product facts (Offline-first, 14 built-in calculators, Android 7+, No ads or subscription).
3. **02 / CORE FEATURES (`#features`)**:
   - Three outlined feature cards: Task Management, Habit Tracking, and Focus Sessions with direct exploration links.
4. **03 / CALCULATOR LIBRARY (`#calculators`)**:
   - Live interactive search filter input (`#calc-search-input`).
   - 6 Category cards (Finance, Academic, Conversion, Everyday, Budgeting, More tools).
   - Working real-time inline calculator engine computing Loan EMI, GST Tax, CGPA conversion, Tip Split, and Unit conversions.
5. **04 / PRODUCT PRINCIPLES (`#principles`)**:
   - Two-column card articulating offline-first autonomy and straightforward-by-design utility.
6. **05 / DOWNLOAD (`#download`)**:
   - Clear version information (`Version 2.4.0 · 17.8 MB · Android 7–15`).
   - Direct APK download action and GitHub Releases link.
7. **Footer**: Concise brand, secondary links (`Features`, `Calculators`, `Portal`), and wireframe note.
8. **Fixed Bottom Navigation Dock**:
   - 4 destinations: `Dashboard`, `Features`, `Calculators`, `Download`.
   - Viewport-fixed with active state driven by a centralized IntersectionObserver / rAF scroll spy.
   - Ample bottom padding (`120px`) on the document body to guarantee zero content clipping.

---

## 5. Technology Stack & Repository Map

### Web Client
- **Core**: HTML5, ES Modules, Vanilla CSS (CSS Custom Properties).
- **Tooling**: Vite 6.4.x, Node.js.
- **Entry Points**:
  - `web/index.html` (Landing page & wireframe companion)
  - `web/auth/index.html` (Authentication portal)
  - `web/auth/callback/index.html` (Deep link callback gateway)
  - `web/css/style.css` (Strict two-color stylesheet)
  - `web/js/app.js` (Web application logic, calculator runner, theme toggle)
  - `web/js/bottom-nav.js` (4-item bottom dock scroll-spy controller)

### Android Native Application
- **Language**: Kotlin 2.2.x.
- **UI Toolkit**: Jetpack Compose with Material 3 foundation.
- **Architecture**: MVVM with Kotlin Coroutines and StateFlow.
- **Persistence**: Android Jetpack Room (SQLite).
- **Minification**: Google R8 shrinking with ProGuard optimization.
- **Build Configuration**:
  - `app/build.gradle.kts`: Application ID `com.aistudio.habittask.wvnk`, `minSdk = 24`, `targetSdk = 36`, `versionCode = 28`, `versionName = "2.4.1"`.
  - `app/src/main/java/com/example/ui/theme/Color.kt`: Centralized `#252525` and `#FFFFFF` tokens.
  - `app/src/main/res/drawable/ic_launcher_background.xml`: `#252525` background.
  - `app/src/main/res/mipmap-*/`: Monochrome launcher icons with dark `#252525` depth background and white emblem.
  - `app/src/main/java/com/example/MainActivity.kt`: Liquid Glass Navigation Bar with zero-lag $O(1)$ constant-slot geometry and GPU RenderNode liquid indicator pill.
  - `app/src/main/java/com/example/ui/components/SketchComponents.kt`: Kern Society kinetic elastic squash & stretch (`Modifier.elasticStretchPress`), Dheeraj Carpenter 3D perspective depth (`Modifier.tactile3DDepth`), icon nudges (`Modifier.iconNudgePress`), and checkbox kinetic squash snap.

---

## 6. Build and Verification Instructions

### Web Production Build
```bash
npm run build
```
Builds production assets to `dist/` with zero warnings or errors.

### Local Development Server
```bash
npm run dev
```
Serves on `http://localhost:5173/`.

### Android APK Build
```bash
./gradlew assembleRelease
```
Generates release APK at `app/build/outputs/apk/release/app-release.apk` with official R8 minification and signing.
Copied to project root:
- `TDLxHT.2.4.1.apk`
- `TDlxHT.apk`

---

## 7. Verification Status

- **Web Layout**: Matches `TDLxHT_single_page_grayscale_wireframe.svg` in hierarchy, section order, proportions, and responsive behavior.
- **Color Discipline**: Verified across CSS and Kotlin Compose UI. Only `#252525` and `#FFFFFF` are authored.
- **Bottom Navigation**: 4-item dock (`Dashboard`, `Features`, `Calculators`, `Download`) synchronizes with viewport scroll state without flickering.
- **App Icons**: Launcher mipmaps (with dark depth background) and transparent web favicons updated to the new checklist + leaf vector emblem.
- **Liquid Glass Nav Bar**: Frosted glass surface, vertical specular rim gradient, and top arc sheen exclusive to the APK app.
- **Sliding Nav Bar Animation Fix**: Completely lag-free 120fps fluid transitions without layout remeasurement.
- **Kinetic Physics & Depth**: Kern Society volume-conserving elastic squash & stretch and Dheeraj Carpenter 3D perspective depth integrated into all cards and interactive controls.
- **APK Compilation**: Compiled `v2.4.1` (versionCode 28) with R8 minification and V1/V2/V3 signing.
