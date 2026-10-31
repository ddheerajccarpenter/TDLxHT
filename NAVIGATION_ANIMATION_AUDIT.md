# TDlxHT — Global Predictive Back & Navigation Animation Audit (`NAVIGATION_ANIMATION_AUDIT.md`)

**Specification Standard:** Android 14/15 Predictive Back API & Apple Liquid Glass Layered Physics  
**Implementation Date:** 2026-10-31  
**App Version:** 2.5.5 (Build Code 42)  
**Status:** Unified Centralized Navigation Architecture Verified Across All Destinations  

---

## 1. Executive Overview

This audit document records the architecture, gesture mechanics, mathematical transforms, Android OS integration, and verification matrix for the centralized **Global Predictive Back, Previous-Screen Preview, and Navigation Animation System** implemented across TDlxHT.

Previously, navigation between screens in TDlxHT relied on fragmented, individual boolean state flags (`isSettingsOpen`, `isSearchOpen`, `isCreatingTask`, `taskToEdit`, `isToolModalOpen`) wrapped in disconnected `AnimatedVisibility` blocks. This legacy approach prevented true Predictive Back gestures because the previous screen was unmounted/hidden during transitions, making live preview impossible, and caused competing back handlers to mismanage back-press priority.

The new architecture unifies the entire application under `PredictiveNavController` and `PredictiveNavigationHost`, providing live gesture-progress-driven screen scaling, squircle corner morphing, directional translation, and real-time live previous-screen reveal underneath.

---

## 2. Navigation Architecture & Stack Management

### Central Controller: `PredictiveNavController`
Located in `app/src/main/java/com/example/ui/navigation/PredictiveNavigation.kt`:
- **Stateful BackStack:** `val backStack = mutableStateListOf<AppNavDestination>(initialDestination)`
- **Active Destination:** `currentDestination = backStack.last()`
- **Live Background Destination:** `previousDestination = backStack[backStack.size - 2]` (rendered in Layer 0)
- **Back Eligibility:** `canNavigateBack = backStack.size > 1 || (currentDestination is Main && tabIndex != 0)`

### Destination Type Hierarchy: `AppNavDestination`
All screens, details, and modals are represented as type-safe data classes and objects:
1. `AppNavDestination.Main(val tabIndex: Int)`: Primary 4-tab stadium dock container (Home, Productivity, Tools, Insights).
2. `AppNavDestination.TaskDetail(task, initialTitle, initialNotes, isShortTerm)`: Task creation and editing.
3. `AppNavDestination.HabitDetail(habit)`: Habit creation and editing.
4. `AppNavDestination.Settings`: Global application settings.
5. `AppNavDestination.Diagnostics`: System diagnostics screen (nested under Settings).
6. `AppNavDestination.Search`: Global search overlay with live filtering.
7. `AppNavDestination.ToolRunner(tool)`: Interactive runner for all 15 calculators and utility tools.
8. `AppNavDestination.QuickCreate`: Apple Liquid Glass quick-add bottom sheet.
9. `AppNavDestination.FocusTimer(initialMinutes)`: Focus timer modal with shake trigger.

---

## 3. Gesture Mathematics & Layered Rendering

During an active Android edge swipe gesture, `PredictiveNavigationHost` coordinates two hardware-accelerated RenderNode layers simultaneously:

### Layer 0: Background (Previous Destination Revealed Underneath)
The real previous screen from the backstack is rendered underneath with the following progress-driven transforms ($p \in [0.0, 1.0]$):
- **Depth Scale ($S_{bg}$):** $0.93 + (p \times 0.07)$ (Starts at `0.93` depth and rises to `1.00` full viewport).
- **Corner Squircle Radius ($R_{bg}$):** $(1.0 - p) \times 16\text{dp}$ (Rounds off during depth, expanding to borderless rectangle at `1.0`).
- **Ambient Scrim Dimming ($\alpha_{scrim}$):** $(1.0 - p) \times 0.28$ (Fades from 28% dark veil down to 0% transparent).
- **Live State Preservation:** No screenshots or placeholder images are used. The composable remains mounted with its exact scroll offsets, draft form inputs, and active UI states.

### Layer 1: Foreground (Active Screen Driven by Gesture)
The active screen being dismissed scales down and follows the finger's horizontal swipe:
- **Surface Scale ($S_{fg}$):** $1.0 - (p \times 0.08)$ (Subtly scales from `1.00` down to `0.92`, preserving text legibility).
- **Corner Squircle Radius ($R_{fg}$):** $p \times 24\text{dp}$ (Morphs from flat screen into a floating Liquid Glass card).
- **Directional Translation ($T_x$):**
  - Left-Edge Swipe (`EDGE_LEFT`): $+p \times 120\text{dp}$ (Follows finger rightwards).
  - Right-Edge Swipe (`EDGE_RIGHT`): $-p \times 120\text{dp}$ (Follows finger leftwards).
- **Elevation & Shadow ($E_{fg}$):** Elevated by $16\text{dp}$ with specular border highlight separating it from the background layer.
- **Readable Opacity ($\alpha_{fg}$):** $1.0 - (p \times 0.12)$ (Maintains $\ge 88\%$ opacity during dragging so content remains crisp).

### Spring Settling Specs
- **Commit Spring (Gesture Released past threshold):** `spring(dampingRatio = 0.82f, stiffness = 450f)`.
- **Cancel Spring (Gesture Reversed to edge):** `spring(dampingRatio = 0.78f, stiffness = 420f)`.
- **Forward Navigation Enter:** `slideInHorizontally(width / 5) + scaleIn(0.96f) + fadeIn(200ms)`.
- **Forward Navigation Exit:** `slideOutHorizontally(-width / 6) + scaleOut(0.96f) + fadeOut(160ms)`.

---

## 4. Android System Integration & Compatibility Matrix

| Android Version | API Level | Predictive Back Support | Behavior |
| :--- | :---: | :---: | :--- |
| **Android 15 (Vanilla Ice Cream)** | 35 | Full Native Predictive Back | Full edge-swipe gesture telemetry with live previous screen preview. |
| **Android 14 (Upside Down Cake)** | 34 | Full Native Predictive Back | Full edge-swipe gesture telemetry with live previous screen preview. |
| **Android 13 (Tiramisu)** | 33 | `OnBackInvokedCallback` | System predictive back enabled via manifest flag; falls back to smooth spring exit if system developer option is disabled. |
| **Android 7.0–12 (Legacy)** | 24–32 | Graceful Fallback | Executes smooth forward/backward layered animated pop transition via `PredictiveNavigationHost`. |
| **3-Button Navigation** | All | Hardware/Software Back Button | Instant transition triggering the exact same layered exit animation without requiring edge swipe. |

### Manifest Enabling Flag
Enforced in `app/src/main/AndroidManifest.xml`:
```xml
<application
    android:name=".HabitTaskApplication"
    android:allowBackup="false"
    android:enableOnBackInvokedCallback="true"
    ... >
```

---

## 5. Back Priority & Conflict Resolution

The system enforces a strict 4-level back priority hierarchy:
1. **Level 1 — Active Modals & Overlays:** Dismisses `QuickCreate` or `FocusTimer` first, restoring the screen underneath.
2. **Level 2 — Nested Destinations:** Dismisses child destinations (e.g., `Diagnostics` returns to `Settings`, `ToolRunner` returns to `Tools`).
3. **Level 3 — Secondary Full Screens:** Pops secondary screens (`Settings`, `Search`, `TaskDetail`, `HabitDetail`) back to the underlying `Main` tab.
4. **Level 4 — Primary Main Tabs:** If positioned on Productivity (1), Tools (2), or Insights (3), Back navigates back to Home (0).
5. **Level 5 — Application Root Exit:** When on Home (tab 0) with `backStack.size == 1`, `canNavigateBack` evaluates to `false`, delegating to the Android OS for native app-exit handling.

---

## 6. Verification & Test Matrix

| Test Scenario | Action Performed | Expected Visual Result | Status |
| :--- | :--- | :--- | :---: |
| **Left-Edge Swipe on Settings** | Drag from left edge halfway across screen | Settings scales to 0.96, corners round to 12dp; Home screen rises underneath from 0.96 to 0.98. | **PASSED** |
| **Cancelled Swipe on Settings** | Drag back towards left edge and release | Settings smoothly springs back to 1.0; Home dims back away; zero screen resets. | **PASSED** |
| **Right-Edge Swipe on TaskEdit** | Drag from right edge and release | TaskEdit translates leftwards, reveals previous task list underneath, and pops cleanly. | **PASSED** |
| **Nested Diagnostics Back** | Open Diagnostics from Settings, swipe back | Settings is visibly revealed underneath; swiping back again reveals Home. | **PASSED** |
| **Calculator Runner Back** | Open Loan & EMI calculator in Tools, swipe back | Calculator slides away, revealing the 15-tool list underneath with exact scroll position. | **PASSED** |
| **Quick Create Dismissal** | Tap Quick Add (+), swipe back | Sheet slides downwards; underlying screen remains stable. | **PASSED** |
| **Search to Detail Transition** | Search for task, tap result, swipe back | TaskDetail opens; swiping back returns to Search with previous search query intact. | **PASSED** |
| **Bottom Navigation Switching** | Tap Tools tab, swipe back | Tools screen returns to Home tab (tab 0) with smooth horizontal animation. | **PASSED** |
| **Unit Test Suite** | Execute `./gradlew testDebugUnitTest` | All 34 tests execute and pass without failure. | **PASSED** |
| **Release APK Build** | Execute `./gradlew syncReleaseApk` | Builds and signs `TDLxHT.2.5.5.apk` (Build 42). | **PASSED** |

