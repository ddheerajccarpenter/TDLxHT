# TDLxHT Master Tracker & Implementation Status

> **Tracking Document:** Real-time tracking of defects, requirements, design system overhaul, wireframe parity, and release builds across the **Web** and **Android APK**.

---

## 1. Web UI/UX Overhaul & Wireframe Parity

### A. Authoritative Reference Alignment (`to update/TDLxHT_single_page_grayscale_wireframe.svg`)
- [x] **Compact Top Header**: Brand `TDLxHT` with new monochrome icon, GitHub link, Portal link, working Theme toggle, and Download APK action.
- [x] **01 / LANDING PAGE (`#dashboard`)**:
  - Eyebrow: `01 / LANDING PAGE` with separator line.
  - Sublabel: `NATIVE ANDROID · V2.4.0`.
  - Heading: `Tasks, habits and calculators. One offline-first app.`
  - Actions: Primary `Download APK` and outlined `View features` (links to `#features`).
  - Desktop Preview: Structural grayscale wireframe placeholder showing phone frame, interactive tasks, and live completion stats.
  - 4 Product Facts: Offline-first, 14 built-in calculators, Android 7+, No ads or subscription.
- [x] **02 / CORE FEATURES (`#features`)**:
  - 3 Outlined cards: Task Management, Habit Tracking, Focus Sessions with exploration links.
- [x] **03 / CALCULATOR LIBRARY (`#calculators`)**:
  - Live search filter input (`#calc-search-input`).
  - 6 Category cards (Finance, Academic, Conversion, Everyday, Budgeting, More tools).
  - Working interactive calculator runner for Loan EMI, GST Tax, CGPA conversion, Tip Split, and Unit conversion.
- [x] **04 / PRODUCT PRINCIPLES (`#principles`)**:
  - Two-column card: Offline-first autonomy and Straightforward-by-design utility.
- [x] **05 / DOWNLOAD (`#download`)**:
  - Release specifications: `Version 2.4.0 · 17.8 MB · Android 7–15`.
  - Direct download action for `TDLxHT.2.4.0.apk` and GitHub Releases link.
- [x] **Footer**: Concise brand, secondary links, and wireframe notice.
- [x] **Fixed Bottom Navigation Dock**:
  - 4 destinations: `Dashboard`, `Features`, `Calculators`, `Download`.
  - Viewport-fixed with active state driven by centralized scroll spy.
  - Ample bottom body padding (`120px`) preventing any content overlap.

### B. Strict Two-Color Palette Rule
- [x] **Dark Grey (`#252525`)**: Canvas and component surfaces.
- [x] **White (`#FFFFFF`)**: Text, icons, borders, dividers, controls, active states, and outlines.
- [x] Zero non-monochrome colors: No pure black, blue, green, orange, red, purple, beige, tinted grey, additional shades, gradients, or glow colors.

### C. App Icons Replacement (`AppIcons/`)
- [x] Updated Android launcher mipmaps: `mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi` (regular and round).
- [x] Updated Android launcher background: `#252525`.
- [x] Updated Android `app_logo.png` (drawable and drawable-nodpi).
- [x] Updated Web `app-icon.png` (512x512) and `favicon.png` (64x64).

---

## 2. APK Release Build Status

- **Application ID**: `com.aistudio.habittask.wvnk`
- **Target SDK**: 36 | **Min SDK**: 24
- **Version**: `v2.4.0` (versionCode `27`)
- **Binary Outputs**:
  - `app/build/outputs/apk/release/app-release.apk`
  - `TDLxHT.2.4.0.apk` (18.6 MB, 18,584,292 bytes)
  - `TDlxHT.apk` (Root alias)
- **Minification & Signing**: Google R8 bytecode shrinking with official V1, V2, and V3 signatures.
- **Verification**: Gradle build succeeded in 1m 52s (0 errors).

---

## 3. Implementation Status Summary

| Area | Status | Verification Result |
|---|:---:|---|
| **Tracker Document** | DONE | `tracker.md` updated and active. |
| **Memory Document** | DONE | `memory.md` complete with instructions, architecture, and verification. |
| **Wireframe Parity** | DONE | Matches `TDLxHT_single_page_grayscale_wireframe.svg` across all 8 sections. |
| **Strict Two-Color Palette** | DONE | Strictly `#252525` and `#FFFFFF` across Web CSS and Android Compose theme. |
| **App Icons Update** | DONE | Replaced launcher mipmaps, adaptive assets, and web favicons from `AppIcons/`. |
| **4-Item Bottom Dock** | DONE | Dashboard, Features, Calculators, Download with rAF scroll spy. |
| **Web Production Build** | DONE | `npm run build` completed in 400ms with 0 errors. |
| **Vercel Redirects** | DONE | `vercel.json` redirects updated to `v2.4.0`. |
| **APK Release Build** | DONE | Compiled `TDLxHT.2.4.0.apk` (18.6 MB) with R8 minification & signature. |
