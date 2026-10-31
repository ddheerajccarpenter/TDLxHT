# TDlxHT — Predictive Back Navigation Bug Fix & Transition Repair Report

**Release Version:** `2.5.6` (Build `43`)  
**Date:** 2026-10-31  
**Project:** TDlxHT Android Application & Web Distribution  

---

## 1. Executive Summary

This report documents the root-cause analysis, architecture redesign, and verification for the critical navigation and predictive back defects identified in TDlxHT. The repairs preserve the Android-native Predictive Back gesture with live previous-screen preview while eliminating screen overlapping, accidental application termination, transition blackout frames, and broken modal backdrops.

---

## 2. Root Cause Analysis & Technical Resolutions

### Bug 1: Two Screens Overlapping Incorrectly (Edit Habit & Today at ~3s)
* **Symptom:** During a back gesture from Edit Habit, the current screen and Today screen appeared simultaneously on top of each other, resulting in overlapping toolbars, misplaced cards, double content exposure, and visual duplication.
* **Root Causes:**
  1. **Incomplete Gesture Exit Offset:** In `PredictiveNavigation.kt`, `maxShiftPx` was hard-coded to `120.dp`. When the user released the gesture and it committed past threshold, `animateTo(1f)` only translated the foreground card by 120dp. The foreground screen remained 88% opaque (`alpha = 1f - progress * 0.12f`) right in the center of the display over the previous screen.
  2. **Asynchronous BackStack Pop Swap:** When `animProgress` hit 1.0f, `navController.navigateBack()` flipped `isPredictingBack = false` synchronously while destroying the dual-layer box, triggering a sudden swap and double animation in `AnimatedContent`.
  3. **Window Insets Inconsistency:** `AppNavDestination.Main` had a `Scaffold` with `calculateTopPadding()` (~40dp status bar), whereas `HabitDetailEditScreen` had no status bar padding. As a result, when both screens were rendered simultaneously, the headers and cards had misaligned vertical offsets.
* **Resolution:**
  - **Off-Screen Commit Physics:** `translationX` is computed relative to the full viewport width (`fullWidthPx * progress * direction`). While dragging, the card translates smoothly with gesture resistance. Upon commit (`animProgress -> 1f`), the card animates **100% off-screen** (`translationX = fullWidthPx`) and fades to `alpha = 0f`.
  - **Late Stack Pop:** `navController.popBackStackDirect()` is executed only after the card has cleanly exited the viewport. Because the card is already off-screen and invisible, resetting `isPredictingBack = false` produces zero visual jump.
  - **Unified Status Bar Geometry:** Wrapped all subscreen destinations (`TaskDetail`, `HabitDetail`, `Settings`, `Diagnostics`, `Search`) in `.windowInsetsPadding(WindowInsets.statusBars)`, ensuring pixel-perfect header alignment matching `HomeScreen`.
  - **Solid Layer 1 Surface:** Foreground card remains 100% solid and opaque with `PaperBackground` and drop shadow while dragging (`activeAlpha = 1f`), fading only near the very edge (`progress > 0.70f`).

---

### Bug 2: Application Closing or Exiting Unexpectedly (~4s)
* **Symptom:** Navigating backwards inside the application caused the app to close or drop to the device launcher instead of returning to the previous screen.
* **Root Causes:**
  1. **Premature Callback Disabling:** In `PredictiveNavigation.kt`, `PredictiveBackHandler` had `enabled = navController.canNavigateBack`. When `navigateBack()` was called and stack size reached 1, `canNavigateBack` flipped to `false` while the Android dispatcher still had in-flight gesture events. Because no other handler intercepted back, Android's system dispatcher called `ComponentActivity.onBackPressed()` -> `finish()`.
  2. **Missing Root BackHandler:** In `MainActivity.kt`, the outer `BackHandler` had been removed, leaving secondary tabs (Productivity, Tools, Insights) and rapid back presses unintercepted.
* **Resolution:**
  - **Two-Tier Back Navigation System:**
    - **Tier 1 (Predictive Back Gesture):** `PredictiveBackHandler(enabled = navController.canPredictiveBack)` is enabled strictly when `activeOverlay == null && backStack.size > 1 && !isTransitioning`. It manages the two-layer gesture with previous-screen preview.
    - **Tier 2 (Global Hardware/System Back Guard):** Implemented an explicit `BackHandler` in `MainActivity`:
      ```kotlin
      BackHandler(
        enabled = navController.activeOverlay != null ||
          navController.backStack.size > 1 ||
          pagerState.currentPage != 0
      ) {
        when {
          navController.activeOverlay != null -> navController.dismissOverlay()
          navController.backStack.size > 1 -> navController.navigateBack()
          pagerState.currentPage != 0 -> coroutineScope.launch { pagerState.animateScrollToPage(0) }
        }
      }
      ```
    - The app **never** finishes when inside any subscreen, modal sheet, or secondary tab. Only on `Main(0)` (Today tab) with no modals and `backStack.size == 1` does standard Android system exit behavior apply.
  - **Transition Lock:** Added `isTransitioning` state guard in `PredictiveNavController` to absorb rapid multi-touch or double-swipe events.

---

### Bug 3: Black Screen During Navigation Transition (~11s)
* **Symptom:** During transitions between screens, the display flickered or turned pitch black for several frames.
* **Root Causes:**
  1. **Transparent Transition Container:** `PredictiveNavigationHost` used an unpainted `Box(modifier.fillMaxSize())` without a background brush. During `AnimatedContent` cross-fades (`fadeOut` to 0), the transparent space revealed Android's edge-to-edge window canvas, which defaults to pure black.
  2. **Layer 0 Scrim Over Black Canvas:** In Layer 0, `scrimAlpha` was rendered directly over transparent buffers when no previous destination was attached.
* **Resolution:**
  - **Solid Canvas Compositing:** Bound `modifier.fillMaxSize().background(PaperBackground)` at the outermost container of `PredictiveNavigationHost`, at the root of `MainActivity`, and around every single destination inside `AnimatedContent`.
  - **Soft Cross-Fade Parameters:** Updated `transitionSpec` so exiting screens fade to a minimum opacity (`targetAlpha = 0.3f`) while entering screens fade in from `initialAlpha = 0.5f`, guaranteeing continuous visual continuity with zero pitch-black frames.

---

### Bug 4: Quick Create Screen Backdrop Defect (~12–13s)
* **Symptom:** Opening Quick Create presented the sheet over a black or missing background instead of floating over the active underlying screen.
* **Root Causes:**
  1. **Modal Stack Replacement:** `QuickCreate` was treated as an `AppNavDestination.QuickCreate` entry in the navigation backstack inside `AnimatedContent`. Navigating to `QuickCreate` unmounted `AppNavDestination.Main`, replacing it entirely.
  2. **Empty Scrim Canvas:** Because `Main` was unmounted, `GlassSheet` was rendered over an empty layout. Dismissing it required recomposing `Main` from scratch, causing a jarring blink/reload.
* **Resolution:**
  - **Dedicated Overlay Architecture:** Separated full-screen stack destinations from modal overlays (`AppNavOverlay.QuickCreate`, `AppNavOverlay.FocusTimer`, `AppNavOverlay.ToolRunner`).
  - **Persistent Underlying Destination:** Modals are rendered in an overlay layer directly above `PredictiveNavigationHost`. The active screen remains 100% mounted, visible, and interactive beneath the dimmed glass scrim.
  - **Zero-Flicker Dismissal:** Dismissing Quick Create simply sets `activeOverlay = null`, smoothly sliding the sheet down and revealing the already-rendered screen underneath with zero recomposition or delay.
  - **Smooth Forward Navigation:** Selecting "New Task" or "New Habit" dismisses the overlay and navigates forward, transitioning smoothly from the current screen to the editor.

---

## 3. Verification & Build Artifacts

| Verification Step | Command / Target | Result |
| :--- | :--- | :--- |
| **Unit Tests** | `./gradlew testDebugUnitTest` | **PASSED** (35 tasks, 0 failures) |
| **Kotlin Compilation** | `./gradlew compileDebugKotlin` | **PASSED** (0 errors) |
| **Release APK Build** | `./gradlew syncReleaseApk` | **SUCCESS** (`TDLxHT.2.5.6.apk` & `TDlxHT.apk`, 17.35 MB) |
| **Version Metadata** | `version.json`, `web/version.json` | **UPDATED** (`versionCode = 43`, `v2.5.6`) |
| **Web Production Build** | `npm run build` | **SUCCESS** (Vite v6.4.4 production build) |

---

## 4. Modified Files

1. `app/src/main/java/com/example/ui/navigation/PredictiveNavigation.kt`
   - Added `AppNavOverlay` interface for floating modal sheets.
   - Refactored `PredictiveNavController` with `canPredictiveBack`, `activeOverlay`, `isTransitioning`, and `popBackStackDirect()`.
   - Re-engineered `PredictiveNavigationHost` with full-width off-screen commit physics and solid `PaperBackground` canvas.
2. `app/src/main/java/com/example/MainActivity.kt`
   - Added Tier-2 `BackHandler` guard against accidental exit.
   - Fixed status bar insets across detail screens.
   - Separated overlay layer (`GlassSheet`, `FocusTimerDialog`, `ToolRunnerModal`) from stack destinations.
3. `app/build.gradle.kts`
   - Bumped `versionCode = 43` and `versionName = "2.5.6"`.
4. `package.json` & `package-lock.json`
   - Updated version to `2.5.6`.
5. `web/index.html`
   - Updated release links and version tags to `2.5.6`.
6. `version.json` & `web/version.json`
   - Synced release metadata for in-app updater.

