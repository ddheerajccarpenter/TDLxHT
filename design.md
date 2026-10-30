# TDlxHT — Application Design System & UI/UX Specification

**File:** `design.md`  
**Product:** TDlxHT — To-Do List × Habit Tracker  
**Status:** Consolidated design specification; implementation details must be checked against the actual repository before code changes.

---

## Document purpose and authority

This document defines the product-wide UI/UX architecture, screen requirements, component behavior, platform rules, accessibility expectations, and acceptance criteria for TDlxHT.

The companion `designglass.md` defines the Liquid Glass material system: the canonical glass palette, material opacity, edge treatment, geometry tokens, typography tokens, and motion token values. This `design.md` must use those tokens rather than inventing competing values.

**Authority when the files are used together:**

1. `designglass.md` is authoritative for Liquid Glass material, palette, typography, geometry, and motion tokens.
2. `design.md` is authoritative for product-wide layout, screen architecture, component responsibilities, interaction behavior, platform mapping, accessibility, performance, and verification.
3. If a screen-specific instruction appears to conflict with a glass token, follow `designglass.md` for the visual token and this document for the screen's structure and behavior.
4. Do not maintain separate, contradictory token tables in the two files. A token change belongs in `designglass.md`; this file should refer to the token by name.
5. Old drafts such as `DESIGN.md`, `DESIGN-Light_Mode.md`, `DESIGN-Dark_Mode.md`, and `TDLxHT_UI_UX_Redesign.md` are historical references only. They must not override these two current specifications.

The supplied files describe a native Android application built with Jetpack Compose and mention a Web companion. Treat that as the current documented direction, **not as proof of the repository's exact implementation**. Confirm the actual project structure, Gradle configuration, frontend framework, and enabled platforms before implementation.

---

## Table of contents

1. [Product and design principles](#1-product-and-design-principles)
2. [Platform and implementation boundaries](#2-platform-and-implementation-boundaries)
3. [Design-token usage](#3-design-token-usage)
4. [Liquid Glass material and surface hierarchy](#4-liquid-glass-material-and-surface-hierarchy)
5. [Shared component system](#5-shared-component-system)
6. [Global navigation and layout rules](#6-global-navigation-and-layout-rules)
7. [Screen-by-screen UX specification](#7-screen-by-screen-ux-specification)
8. [Forms, validation, empty states, and feedback](#8-forms-validation-empty-states-and-feedback)
9. [Motion, touch, and haptics](#9-motion-touch-and-haptics)
10. [Accessibility and responsive behavior](#10-accessibility-and-responsive-behavior)
11. [Performance and rendering constraints](#11-performance-and-rendering-constraints)
12. [App icon and platform assets](#12-app-icon-and-platform-assets)
13. [Quality assurance and acceptance criteria](#13-quality-assurance-and-acceptance-criteria)
14. [Repository facts that must be verified](#14-repository-facts-that-must-be-verified)
15. [Explicit non-goals and implementation rules](#15-explicit-non-goals-and-implementation-rules)

---

## 1. Product and design principles

TDlxHT is a task-management and habit-tracking application. Its interface should make daily planning, task completion, habit consistency, calendar review, utility calculations, and activity insights straightforward.

The visual direction is **Apple-inspired Liquid Glass applied to a focused productivity interface**. It must feel tactile and layered without reducing information density, clarity, accessibility, or speed.

### 1.1 Core principles

1. **Function before decoration.** Every surface, highlight, animation, and layer must improve grouping, hierarchy, state recognition, or feedback. Do not add effects solely to make the interface look more elaborate.
2. **Real glass behavior where technically supported.** Glass should respond to the content and light behind it. A translucent fill plus a border is not, by itself, a complete Liquid Glass treatment. Use supported blur, backdrop sampling, distortion, and specular treatments selectively.
3. **Controlled monochrome identity.** Use the charcoal, black, white, and neutral-gray palette defined in `designglass.md`. Do not reintroduce lime, bright blue, terracotta, neon, rainbow gradients, or unrelated accent colors from older drafts.
4. **Semantic status is restrained.** Success, warning, destructive, and error states may use the minimum semantic color necessary for recognition. These are status indicators, not a secondary brand palette. Never communicate status through color alone.
5. **Legibility is non-negotiable.** Text must remain readable over translucent surfaces, variable content, and moving backgrounds. Increase surface opacity or use an opaque fallback whenever contrast is insufficient.
6. **Consistent interaction.** Buttons, navigation, segmented controls, fields, sheets, and menus must have predictable behavior and state feedback across all screens.
7. **Preserve the product.** Retain the existing features, data model, workflows, and navigation destinations unless the source-code audit proves a feature is obsolete or broken and the change is explicitly documented.
8. **No fabricated activity.** Empty states and insights must reflect actual stored data. Do not create fake tasks, habits, streaks, report metrics, or AI recommendations to make a screen look populated.
9. **Native behavior matters.** Preserve Android Back, system bars, gesture navigation, keyboard behavior, notification permissions, and platform file-sharing conventions.
10. **No visual clutter.** Avoid decorative blobs, oversized empty cards, excessive borders, stacked containers, heavy glow, random gradients, gratuitous parallax, and unrelated illustration styles.

### 1.2 Sketch and illustration usage

Older design material mentions an artisan sketch aesthetic and a `SketchCard` component. This is not the primary UI material system.

- Do not apply hand-drawn borders, doodles, or sketch textures to every screen or glass surface.
- Preserve existing sketch assets only where they are already part of the product identity and have a clear content purpose.
- A sketch/illustration treatment must not compete with Liquid Glass, data legibility, or controls.
- Do not create new sketch decoration merely to fill empty space.

---

## 2. Platform and implementation boundaries

### 2.1 Inspect before selecting an implementation

Before making changes, inspect the repository and record:

- Whether the Android UI is native Kotlin/Jetpack Compose, Java/XML, React Native, Flutter, Capacitor, or a WebView-based interface.
- Whether a separate Web companion exists and which framework it uses.
- The exact Gradle, Kotlin, Android Gradle Plugin, Compose, compile SDK, target SDK, and minimum SDK versions in the project.
- Existing design tokens, theme setup, navigation structure, reusable components, and dependency versions.
- Existing persistence, database, API, authentication, AI, reminder, and notification architecture.
- Existing tests and build tasks.

Do not migrate frameworks, update major dependencies, replace the database, or rewrite the backend merely to implement the visual redesign.

### 2.2 Native Android implementation

If the Android application uses Jetpack Compose:

- Implement the glass component system as reusable Compose components and modifiers in the existing architecture.
- Use Compose-native state, layout, semantics, navigation, window insets, and animation APIs.
- Use Android `dp` for layout dimensions and `sp` for text. Do not copy Web CSS `px` or `rem` values directly into Compose.
- Use `graphicsLayer` for transformations where appropriate, and avoid triggering unnecessary parent remeasurement.
- Implement backdrop blur, distortion, and rendering effects only through APIs supported by the app's current minimum SDK and rendering pipeline.
- Supply a restrained fallback when advanced blur or distortion is unavailable or too expensive.
- Do not attempt to import an npm React component into a native Kotlin/Compose source set.

### 2.3 React / Next.js Web companion

If a real React-based Web companion exists, the package `@zakisheriff/liquid-glass` may be evaluated for that Web interface.

Before using it:

- Check the package's current README, installed version, peer dependencies, exports, prop names, stylesheet entry, framework compatibility, and limitations.
- Add the package only to the Web project that actually uses it.
- Import its stylesheet once from the correct application root.
- In Next.js App Router, import global styles from the root layout; in Pages Router, import them from `_app`.
- Add `'use client'` only to components that need client-side state or browser interaction.
- Verify every component name and prop against the installed package. Treat names in this design document as design roles, not proof that the package exports an identical API.
- If a required component is not exported, build a local wrapper or equivalent instead of assuming it exists.

Do not add the npm package to a native Android-only project just because the design is called Liquid Glass.

### 2.4 Cross-platform consistency

Android and Web should share the same visual decisions and interaction meaning, but each platform should use its native units and interaction conventions.

- Share token names and semantic roles.
- Map tokens into Compose theme values for Android and CSS custom properties for Web.
- Do not force identical pixel dimensions across platforms.
- Preserve platform-specific accessibility and navigation behavior.
- A visual effect may differ slightly by platform when required for performance, capability, or accessibility; document the deviation rather than introducing a broken effect.

---

## 3. Design-token usage

All canonical visual values are maintained in `designglass.md`. This section defines how the application consumes them.

### 3.1 Required token groups

The implementation must use the token groups defined in `designglass.md`:

- Canvas and secondary canvas colors.
- Base, subtle, and elevated glass fills.
- Top and bottom glass borders.
- Specular sheen and highlight values.
- Primary, secondary, and muted text colors.
- Hairline dividers.
- Inverse primary-action treatment.
- Destructive/error treatment.
- Corner radii.
- Typography scale.
- Spacing scale.
- Motion curves and spring parameters.

Do not create new parallel names such as `PrimaryAccentLime`, `AccentTerracotta`, or `color-accent` with unrelated values. Platform aliases are acceptable only when they map to the canonical token.

### 3.2 Token naming and mapping

Use one semantic token name per visual role. Recommended mapping pattern:

| Canonical token role | Android mapping | Web mapping |
|---|---|---|
| Canvas background | Compose `Color` theme value | `--tdlxht-canvas` |
| Secondary canvas | Compose `Color` theme value | `--tdlxht-canvas-secondary` |
| Base glass fill | Compose `Color` with alpha | `--tdlxht-glass-fill` |
| Elevated glass fill | Compose `Color` with alpha | `--tdlxht-glass-elevated` |
| Subtle glass fill | Compose `Color` with alpha | `--tdlxht-glass-subtle` |
| Primary text | Compose typography color | `--tdlxht-text-primary` |
| Secondary text | Compose typography color | `--tdlxht-text-secondary` |
| Muted text | Compose typography color | `--tdlxht-text-muted` |
| Hairline divider | Compose `Color` with alpha | `--tdlxht-divider` |
| Destructive state | Compose semantic color | `--tdlxht-destructive` |

These are mapping names, not new color definitions. The actual values come from `designglass.md`.

### 3.3 Theme behavior

- Support dark and light appearance if both are already part of the product scope.
- Follow the system appearance by default unless the existing app exposes a persisted user theme preference.
- If the user can explicitly select a theme, preserve that setting and its persistence behavior.
- Update system bars, status bar icons, navigation bar icons, sheets, dialogs, and app surfaces consistently.
- Never mix a dark canvas with light-mode component tokens or vice versa.
- Verify theme switching while a sheet, keyboard, menu, and form are open.

---

## 4. Liquid Glass material and surface hierarchy

### 4.1 Material behavior

Liquid Glass must be treated as a layered material, not as a generic blur overlay.

Each glass surface may use the following properties where the platform supports them:

- Translucent fill that allows controlled background visibility.
- Backdrop blur or background sampling.
- Subtle color transmission from the content behind the surface.
- A restrained top specular highlight.
- A thin perimeter rim with stronger upper and weaker lower light response.
- Slight depth separation through carefully controlled shadow/elevation.
- Optional distortion/refraction only when the rendering path supports it without visual artifacts or performance regressions.

Do not claim physical refraction when the implementation only draws a gradient border. Describe and implement the effect accurately.

### 4.2 Background context

Glass needs meaningful visual information behind it, but the background must not distract from productivity content.

- Use the app canvas and underlying scrolling content as the primary background context.
- A subtle tonal gradient may be used on the canvas if it improves glass separation and remains within the monochrome palette.
- Do not add decorative organic blobs, random image backgrounds, noisy textures, or bright gradients solely to make the glass effect visible.
- Do not place dense text directly over a busy image.
- If a surface cannot maintain readable contrast over its changing background, raise its opacity or use an opaque fallback.

### 4.3 Five-level surface hierarchy

Use the following logical levels. Elevation and blur are tuned per platform and device rather than treated as fixed guarantees.

| Level | Surface role | Examples | Treatment |
|---|---|---|---|
| 0 | Canvas | Screen background, scroll content backdrop | Base theme canvas; no unnecessary shadow |
| 1 | Content surface | Task row groups, habit panels, summary cards, calculator panels | Base glass or restrained opaque surface; subtle edge definition |
| 2 | Interactive control | Buttons, inputs, switches, segmented controls, sliders | Clear pressed/focused/selected states; minimum touch area respected |
| 3 | Floating control | Bottom navigation, popovers, floating actions | Elevated glass treatment; must not obscure content |
| 4 | Modal surface | Bottom sheets, modal dialogs, confirmation surfaces | Highest surface priority; controlled scrim, safe insets, correct dismissal |

Do not place a full glass card inside another glass card unless nested grouping is necessary and visually clear.

### 4.4 Glass use and restraint

Use glass where it helps communicate hierarchy or floating interaction:

- Floating bottom navigation.
- Quick Create sheet.
- Modal sheets and popovers.
- Primary and secondary actions.
- Segmented controls and filters.
- Search and form fields where the effect improves focus.
- Summary or grouped content panels where background separation is needed.

Do not apply heavy blur to every task row, every text label, every list item, or every small chip. A product screen full of identical glass cards loses hierarchy and becomes visually noisy.

### 4.5 Fallback behavior

When blur, distortion, or backdrop sampling is unsupported, disabled, or too expensive:

- Keep the same layout and interaction.
- Use the canonical glass fill at sufficient opacity.
- Retain the restrained rim/highlight only if it remains readable.
- Avoid a sudden unrelated flat color or a broken transparent surface.
- Ensure the fallback is still clearly part of the same design system.

---

## 5. Shared component system

The following names describe reusable component roles. On Android, implement them as Compose components using the existing project architecture. On Web, use the verified package exports or local wrappers. Do not assume an npm component is available in native Android.

### 5.1 `GlassButton`

**Purpose:** Primary, secondary, tertiary, destructive, and icon-only actions.

Requirements:

- Support the project's semantic variants: primary/inverse, glass, clear/tertiary, and destructive.
- Use a pill shape for compact actions where appropriate; use a more restrained shape when a full pill would look oversized in a form or toolbar.
- Support leading icons and visible labels.
- Icon-only buttons must expose an accessible name/description.
- Provide enabled, disabled, pressed, focused, and loading states.
- Prevent double submission while an action is processing.
- Provide adequate hit area even when the visible icon is small.
- Use restrained press feedback; do not make the control visibly deform in a way that harms text clarity.

Illustrative React usage from the supplied package direction (verify the actual API before using):

```tsx
<GlassButton icon={<PlusIcon />}>New note</GlassButton>

<GlassButton icon={<PlusIcon />} aria-label="Add" />
```

The example is conceptual until validated against the installed package's current exports and prop types.

### 5.2 `GlassCard` and optional `SketchCard`

`GlassCard` groups related information, controls, or calculations.

- Use the canonical content-surface treatment.
- Maintain consistent internal padding and alignment.
- Do not create a card for every individual label or data point.
- Use nested sections and dividers when one large card is more appropriate than multiple small cards.

`SketchCard` is optional and must only be retained if it exists in the source code and has a clear product purpose. It must not override the glass material system or introduce decorative sketch styling throughout the app.

### 5.3 `GlassSwitch`

**Purpose:** Boolean settings such as reminders or feature activation.

- Use controlled state.
- Toggling must change the actual setting, not just its appearance.
- Show checked, unchecked, disabled, and pending states where relevant.
- Provide a semantic label and an announced state for assistive technology.
- For reminder settings, show permission or scheduling failures instead of silently pretending the reminder is enabled.

### 5.4 `GlassSlider`

**Purpose:** Continuous or stepped numeric input.

- Preserve the underlying minimum, maximum, step, and current value.
- Show the current value where the value is important to the task.
- Ensure keyboard/accessibility alternatives where supported.
- `showThumb={false}` is only a visual option for compact control blocks if the actual component supports it; do not remove the only usable interaction affordance.

### 5.5 `GlassSegmented`

**Purpose:** Mutually exclusive views, tabs, and filters.

- Use only when options are mutually exclusive and few enough to fit.
- Make the selected option unmistakable.
- Keep labels stable and avoid truncating essential options.
- Update the displayed data when selection changes.
- Preserve the currently selected value when the user returns to the screen unless product behavior explicitly resets it.
- Provide an accessible role, selected state, and keyboard/DPAD behavior where applicable.

### 5.6 `GlassInput` and `GlassSearchBar`

**Purpose:** Text entry and search.

- Include a visible label or accessible name; placeholder text must not be the only label.
- Use leading icons only where they improve recognition.
- Show a clear action only when the field contains text.
- Support focused, unfocused, disabled, error, and read-only states.
- Use the correct keyboard type and IME action for the field.
- Ensure the keyboard does not hide the active field, validation message, or primary submit action.
- Search must filter the actual data set, support empty results, and offer a clear/reset action.

### 5.7 `GlassSheet`

**Purpose:** Quick Create, contextual actions, and modal workflows.

- Use the modal surface level from the hierarchy.
- Respect system status/navigation insets and the keyboard.
- Support Android Back and an explicit close action.
- Tap-outside dismissal must follow the sheet's interaction contract; destructive or unsaved forms may require confirmation.
- If draggable, provide a clear drag handle and avoid conflicting with inner scroll gestures.
- Prevent background content from receiving unintended taps while the modal is active.
- Restore the previous screen and state after dismissal.

### 5.8 `GlassNav`

**Purpose:** Main application navigation.

- Preserve the existing four primary destinations: Home, Productivity, Tools, and Insights, subject to confirmation against the current route graph.
- Keep the central Quick Create action visually distinct from route selection.
- The center `+` action is an action, not a fifth navigation destination.
- Keep slot widths and hit areas stable so the dock does not shift when selection changes.
- Mark the selected route semantically and visually.
- Preserve back-stack and route state according to the existing navigation architecture.
- Respect system gesture navigation and bottom insets.
- Do not claim a locked 120fps animation target. Motion must adapt to the device refresh rate and remain smooth within the available frame budget.

### 5.9 `GlassMenu` and `GlassMenuItem`

**Purpose:** Sorting, contextual options, and compact action menus.

- Treat these as design roles; verify whether an existing library or component exports them.
- Keep the menu anchored to its trigger and within screen bounds.
- Use a clear selected/check state where applicable.
- Provide a minimum practical hit area and a visible dismissal path.
- Ensure menu items perform the action shown by their label.

---

## 6. Global navigation and layout rules

### 6.1 Main navigation model

The documented main navigation contains:

- Home
- Productivity
- Tools
- Insights
- A central Quick Create (`+`) action

Verify these destinations against the actual source before changing route structure. Do not remove a route or hide an existing feature solely to simplify the dock.

### 6.2 Floating navigation geometry

The floating dock may use the stadium geometry defined in `designglass.md` and should have:

- Stable visual dimensions across route changes.
- Adequate spacing from screen edges.
- A clearly selected destination.
- A distinct center Quick Create action.
- Correct Android navigation-bar and gesture inset handling.
- A readable and touchable layout on narrow devices.

Do not force the dock to remain floating if a device size, accessibility setting, or current architecture makes that layout unusable. Use the same design language in an adaptive fallback.

### 6.3 Content clearance — mandatory

The existing documents specify a fixed `155.dp` bottom padding. Do not treat this as a universal value. It can create excessive unused space on some devices and still fail when the dock or system insets change.

Instead:

1. Calculate content clearance from the actual bottom dock height, its bottom offset, the system navigation/gesture inset, and an intentional content gap.
2. Apply the clearance to scrollable content and relevant fixed actions.
3. Ensure the final list item and bottom-most CTA can be fully scrolled above the dock.
4. Avoid adding multiple independent bottom spacers that compound unpredictably.
5. Test short screens, large font sizes, keyboard-open states, and gesture-navigation devices.

The Insights screen's **Generate Visual Report** action must remain fully visible and tappable above the dock. This is a specific regression test, not a one-off padding hack.

### 6.4 Screen layout baseline

- Use consistent screen-edge spacing based on the spacing tokens.
- Keep page titles and primary actions aligned consistently.
- Use scroll containers for content that can exceed the viewport.
- Avoid fixed heights for content-driven cards and forms.
- Keep section spacing consistent without creating excessive blank regions.
- Do not let sticky headers, bottom controls, or sheets cover essential content.
- Preserve scroll position when switching between nested tabs when appropriate.

---

## 7. Screen-by-screen UX specification

This inventory reflects the supplied design documents and recording. Before implementation, compare every screen and feature with the actual navigation graph and source code. A listed item is not proof that its functionality currently exists.

### 7.1 Home / Today

**Purpose:** Give the user an immediate view of the current day and a fast route to the next useful action.

**Structure:**

- Page header with the current date and relevant navigation/settings actions.
- Daily summary surface showing only metrics supported by stored data.
- Completion fraction and percentage, calculated from the correct daily task/habit set.
- Streak information only when a real streak model exists.
- Segmented filter: All, Tasks, Habits.
- Today's task and habit sections.
- Clear Add Task and Add Habit actions where supported.
- Intentional empty states with one obvious next action.

**Behavior and checks:**

- Date and counts update correctly at local date boundaries.
- All/Tasks/Habits filtering changes the visible records, not only the selected pill.
- Task and habit completion state remains synchronized with their respective screens.
- Progress values are derived from real records and use a defined denominator.
- Empty states do not fabricate activity or reserve large unnecessary blank panels.
- Add actions open the correct creation flow and prevent duplicate records.

### 7.2 Productivity

**Purpose:** Provide a consolidated view of Tasks, Habits, and Calendar if those views are part of the current product structure.

**Structure:**

- Clear page title and contextual actions.
- Segmented view switcher for Tasks, Habits, and Calendar.
- Content area appropriate to the selected view.
- Main bottom navigation remains separate from this internal switcher.

**Behavior and checks:**

- Switching views preserves the correct selected state.
- Internal tabs do not overlap the main navigation dock.
- Selected view data is accurate and refreshes after task/habit updates.
- Returning to Productivity restores the expected view and scroll state where appropriate.

### 7.3 Tasks and Task Detail / Edit

**Task list structure:**

- Search field.
- Status filters: All, Today, Upcoming, Completed, subject to existing supported filters.
- Task rows with title and only relevant metadata (due date, category, priority, completion state, subtask progress).
- A useful empty state and task creation action.

**Task form structure:**

- Title (required).
- Notes/details (optional).
- Due date/schedule if supported.
- Category and priority controls if supported.
- Reminder options if supported.
- Repeat rules if supported.
- Save and cancel/back actions.

**Behavior and checks:**

- Create, read, edit, complete, reopen, and delete all work against persistent storage.
- Empty or whitespace-only titles are rejected with an inline explanation.
- Duplicate save submissions are prevented.
- Search and filters operate on the real task list and combine correctly if the product supports combined filters.
- Completed tasks do not incorrectly appear in active views.
- Due dates, local time, sorting, and upcoming logic are consistent.
- Subtask counters match the underlying subtasks.
- A saved task appears consistently in Home, Productivity, Calendar, and Insights where applicable.

### 7.4 Habits and Habit Detail / Edit

**Habit list structure:**

- Habit title and schedule/frequency summary.
- Completion control for the current date.
- Current streak and best streak only if these are genuinely calculated and stored/supported.
- Clear create/edit actions.

**Habit form structure:**

- Habit name (required).
- Notes or motivation (optional).
- Frequency selection: Every Day, Weekdays, Weekly, Custom, if these are supported by the existing data model.
- Target/session controls where supported.
- Category selection where supported.
- Reminder toggle and time selection where supported.
- Save and cancel/back actions.

**Behavior and checks:**

- Validate the habit name and prevent duplicate submissions.
- Frequency selection changes the actual recurrence schedule.
- Custom frequency values are validated and persisted.
- Increment/decrement controls respect sensible bounds and never display invalid values.
- Reminder state, time, permissions, and scheduled work remain consistent.
- Completion history uses the correct date and timezone.
- Streak calculations handle missed days, schedule rules, and timezone boundaries correctly.
- Editing a habit updates the same record rather than creating a duplicate.

### 7.5 Calendar

**Purpose:** Provide date-based navigation across scheduled tasks and habits.

**Structure:**

- Month, Week, or Day view only if implemented/supported.
- Clear current-period navigation.
- Interactive date grid.
- Selected date state.
- Agenda/timeline for the selected date.
- Meaningful indicators for scheduled tasks and habit events.

**Behavior and checks:**

- Date selection updates the agenda.
- Previous/next month and period navigation work across month/year boundaries.
- Leap years and daylight-saving/timezone rules are handled where relevant.
- Calendar indicators match real records.
- Selecting an agenda item opens the correct detail screen.
- Empty dates have a concise empty state.

### 7.6 Tools — 14 documented utilities

The supplied documents list these 14 tools. Verify the actual registry and preserve the exact supported tool set; do not create placeholder tools to reach a count of 14.

1. Attendance Planner
2. Break-Even Estimator
3. CGPA to Percentage
4. Date & Age Calculator
5. Discount & Sale Calculator
6. Exam Countdown
7. GST & Tax Calculator
8. Loan & EMI Calculator
9. Margin & Markup
10. Percentage Calculator
11. Pomodoro Planner
12. Profit & Loss
13. Quick Unit Converter
14. Reading Time Estimator

**Tools list requirements:**

- Search filters the actual registered tools.
- Category filters reflect the existing categories (for example, Money, Student, Business, Utility) only when those categories exist in the code.
- Sorting by Name, Category, or Usage is supported only if the current app has the required ordering/usage data.
- Rows have clear names, concise descriptions, and consistent hit areas.
- Empty search results are explained and provide a clear reset action.

**Calculator requirements:**

- Validate required inputs and accepted ranges.
- Guard against divide-by-zero and invalid numeric states.
- Do not display `NaN`, `Infinity`, or silently misleading outputs.
- Handle empty, negative, zero, malformed, and unusually large values according to the mathematical rules of each tool.
- Show units, currency assumptions, percentages, and rounding consistently.
- Preserve existing formulas unless a verified defect is found.
- Test formulas against hand-checked examples and boundary values.
- Do not silently change financial or academic conversion conventions.

### 7.7 Insights / Reports

**Purpose:** Present accurate summaries of the user's actual task and habit activity.

**Structure:**

- AI Productivity Advisor only if the integration or deterministic advice feature exists.
- Weekly and Monthly period switcher if supported.
- Previous/next period navigation.
- Clearly labeled date range.
- Real activity metrics and charts.
- AI Visual Report section and Generate Visual Report action only if supported.
- Empty-data state that explains why metrics are unavailable.

**Behavior and checks:**

- Week and month boundaries use a documented calendar convention and local timezone.
- Previous/next period navigation updates the range and all derived values.
- Aggregated values match the task/habit records for that period.
- Empty data does not produce fabricated recommendations or charts.
- AI requests show loading, success, timeout, unavailable, and retry states.
- A report is generated from real user data and is not presented as complete before the operation succeeds.
- The Generate Visual Report action remains fully visible and actionable above the bottom navigation dock.
- Offline fallback advice may be used only if the application already implements or explicitly adopts a deterministic fallback; it must be labeled appropriately and must not imply that an AI service ran.

### 7.8 Global Quick Create sheet

**Purpose:** Provide fast access to the creation flows from the central navigation action.

The supplied materials list:

- New Task
- New Habit
- Short-term Task

Verify that all three are implemented in the current app before displaying them.

**Behavior and checks:**

- Each row opens the correct creation workflow.
- Sheet open/close state is stable under rapid taps.
- Android Back and the explicit close affordance work.
- Tap-outside dismissal follows the sheet's contract.
- Sheet content remains accessible with the keyboard open and on short displays.
- Returning from creation restores the previous destination correctly.
- Repeated selection or tapping does not create duplicate records.

### 7.9 Settings, export, and diagnostics

**Settings:**

- Preserve existing theme, reminder, account, privacy, and application preferences.
- Do not introduce settings that have no implementation.
- Persist settings where the current product requires persistence.

**Diagnostics:**

- Display only checks that actually exist and can be measured.
- Show app version/build information from the actual package metadata.
- Show local database status only if the app uses that database.
- Show backend connectivity/latency only if a backend exists and the probe is safe.
- Do not hard-code Supabase, Room, or any other service as a requirement without verifying the current architecture.
- Never expose API keys, tokens, credentials, private endpoints, or sensitive records.

**Data export:**

- Export only supported user data and include all related records consistently (for example, tasks, subtasks, habits, and completion history when those entities exist).
- Use valid serialization and a predictable file format/name.
- Handle empty data and export errors.
- Use Android's appropriate file-sharing mechanism (such as FileProvider/ShareSheet) only where compatible with the existing implementation.
- Do not upload personal data to a remote service unless the user explicitly initiated a feature that requires it.

---

## 8. Forms, validation, empty states, and feedback

### 8.1 Form behavior

Every form must define:

- Required and optional fields.
- Accepted input types and ranges.
- Validation timing (on submit, on blur, or while typing where helpful).
- Error placement and clear correction guidance.
- Saving/loading state.
- Success and failure feedback.
- Duplicate-submission protection.
- Cancel/back behavior and unsaved-change behavior.

Do not use a toast as the only validation explanation when the user needs to correct a specific field. Prefer inline validation near the relevant input.

### 8.2 Empty states

An empty state must explain what is empty and provide the next useful action when one exists.

- Do not show dummy records.
- Do not create a large decorative empty card without purpose.
- Do not hide a primary action below the floating dock.
- Keep the content concise and visually balanced.
- Distinguish “no data yet” from “data failed to load” and “search returned no results.”

### 8.3 Loading and error feedback

- Show a loading state for operations that take noticeable time.
- Disable or guard duplicate submissions during in-flight actions.
- Preserve user-entered form values after recoverable errors.
- Provide retry where retry is safe.
- Do not display success until the underlying operation has actually succeeded.
- Log technical details safely while presenting actionable user-facing messages.

### 8.4 Interaction truthfulness

Every visible interactive control must perform its named action. If the feature is unavailable, do not style a decorative placeholder to look like a working control.

---

## 9. Motion, touch, and haptics

Motion values are defined in `designglass.md`. Use those values as starting points, not as a demand to animate every element or guarantee a fixed frame rate.

### 9.1 Motion hierarchy

Use motion to confirm interaction and explain state changes:

- Small, quick feedback for buttons, chips, and checkboxes.
- Subtle selection movement for segmented controls and navigation.
- Smooth presentation/dismissal for sheets and menus.
- Restrained insertion/removal transitions for list items.
- Brief, localized feedback for counters and progress updates.

### 9.2 Motion rules

- Avoid large elastic deformation that distorts text or shifts nearby content.
- Do not animate layout dimensions when a graphics-layer transform can achieve the same effect without unnecessary remeasurement.
- Do not delay an action until an animation finishes.
- Prevent repeated state changes from stacking conflicting animations.
- Keep navigation responsive during transitions.
- Use haptics only for meaningful actions and respect system/user haptic settings.
- Respect system animation scaling and reduced-motion preferences.
- When animations are disabled, state changes must still happen immediately and clearly.

### 9.3 Performance expectation

Do not hard-code a 120fps promise. Aim for stable rendering within the device's available refresh rate and frame budget. Verify on representative low-, mid-, and high-tier Android hardware when available.

---

## 10. Accessibility and responsive behavior

### 10.1 Contrast

The designglass palette is the starting point, but translucent compositing changes the final rendered color. Test actual foreground/background combinations after blur and alpha compositing.

- Meet WCAG AA contrast as the minimum baseline: 4.5:1 for normal text and 3:1 for large text and applicable non-text UI indicators.
- Target WCAG AAA where practical: 7:1 for normal text and 4.5:1 for large text.
- Do not label the whole app “WCAG AAA compliant” without testing all relevant states and components.
- Do not use color as the sole signal for selected, completed, warning, or error states.
- Increase opacity or switch to an opaque fallback if contrast fails.

### 10.2 Touch targets and assistive technology

- Use a minimum 48dp Android touch target wherever practical.
- Use at least 44 CSS px for Web touch targets where practical.
- Provide content descriptions/accessible names for icon-only controls.
- Expose selected, checked, expanded, disabled, and error states semantically.
- Ensure TalkBack and other assistive technologies can identify controls and meaningful content order.
- Preserve readable content when font scaling is increased.

### 10.3 Device and layout coverage

Test:

- Narrow and wide Android displays.
- Short and tall screens.
- Portrait and landscape if supported.
- System gesture and three-button navigation.
- Keyboard-open forms.
- Large text/font scaling.
- Dark and light appearances.
- Long translated or unusually long user-entered text.
- Empty, loading, error, and populated states.

Prevent clipping, horizontal overflow, hidden CTAs, overlapping navigation, and unreachable form controls.

---

## 11. Performance and rendering constraints

### 11.1 Blur and glass rendering

Backdrop effects can be expensive. Apply them selectively.

- Avoid full-screen blur layers that redraw continuously without a user-visible need.
- Avoid stacking multiple blur effects over the same region.
- Prefer bounded glass surfaces over enormous translucent overlays.
- Cache or reuse static assets where appropriate.
- Keep scrolling and touch processing responsive.
- Provide the defined fallback for unsupported effects.

### 11.2 Compose performance

If the app uses Jetpack Compose:

- Keep composables modular and stable.
- Hoist state appropriately and avoid unnecessary recomposition.
- Use stable keys for dynamic lists.
- Avoid expensive work in composable bodies.
- Use `graphicsLayer` for suitable visual transforms.
- Avoid `animateContentSize` on large parent containers when it causes unnecessary measure/layout work.
- Use appropriate lazy containers for long lists.
- Profile before optimizing based on assumptions.

### 11.3 Web performance

If a Web companion exists:

- Limit backdrop-filter regions and large fixed glass layers.
- Avoid re-rendering the entire screen for local control changes.
- Respect reduced motion.
- Provide fallback styling for browsers without the required effects.
- Verify touch, scroll, and keyboard behavior on actual mobile browsers.

### 11.4 Data and network behavior

- Preserve the existing persistence and synchronization model.
- Do not add network dependencies to a workflow that is intended to work offline.
- Handle unavailable services explicitly.
- Keep credentials server-side where a server exists; never embed private API keys in an APK or public frontend bundle.
- Do not log secrets or sensitive user data.

---

## 12. App icon and platform assets

The supplied design material describes a minimalist white task/checklist emblem with a leaf motif on a dark background. Treat this as the current icon direction only if it matches the existing approved brand asset.

### 12.1 Icon direction

- Clear, recognizable silhouette at small sizes.
- Strong monochrome contrast.
- Centered composition with Android adaptive-icon safe-zone compliance.
- Avoid tiny details, thin lines that disappear at launcher scale, and text inside the icon.
- Ensure the foreground works with the adaptive background and supported launcher masks.

### 12.2 Asset verification

Check the actual repository before generating or replacing assets. Verify existing mipmap and adaptive-icon resources, round icons, Web favicon, and Web app icon if a Web platform exists.

The earlier density list (mdpi 48px, hdpi 72px, xhdpi 96px, xxhdpi 144px, xxxhdpi 192px) is a reference checklist, not proof that all files already exist or that those are the only required modern Android assets.

Do not replace the existing app icon unless the new asset is approved and all required launcher variants are generated and checked.

---

## 13. Quality assurance and acceptance criteria

A redesign is not complete because the screen looks correct in a static preview or because the project compiles. The final application must be functionally tested.

### 13.1 Visual QA

Check every implemented screen and state for:

- Correct use of canonical tokens.
- Consistent typography, radii, spacing, and icon treatment.
- Intentional use of glass rather than identical surfaces everywhere.
- Readable contrast after translucency and compositing.
- Correct dark/light theme behavior.
- No clipped, overlapping, or hidden controls.
- No excessive blank space, nested-card clutter, or decorative noise.
- Correct selected, pressed, disabled, loading, and error states.

### 13.2 Functional regression checklist

Test each applicable item and record the real result:

1. App installs, launches, backgrounds, and resumes without crashing.
2. Existing user data is preserved.
3. Main navigation opens every intended destination.
4. Android Back and route history behave correctly.
5. Quick Create opens, dismisses, and routes correctly.
6. Task creation, editing, completion, reopening, deletion, and persistence work.
7. Habit creation, editing, completion, recurrence, and persistence work.
8. Reminder permissions, schedule creation, and delivery are tested where supported.
9. Search and filters return correct results.
10. Calendar navigation and date selection work.
11. Each registered calculator passes normal, edge, and invalid-input tests.
12. Insights match real stored activity.
13. AI/service features handle success, offline, timeout, and malformed responses correctly.
14. Data export produces a valid file and shares it correctly where supported.
15. Diagnostics show truthful values and expose no secrets.
16. Sheets, menus, dialogs, and confirmation states work.
17. Keyboard and system insets do not hide important controls.
18. Bottom navigation never obscures the final actionable content.
19. Dark and light modes remain readable.
20. Glass fallbacks remain usable when advanced effects are unavailable.
21. Repeated taps do not create duplicate records or requests.
22. The app survives process recreation/relaunch without losing persisted data.
23. No critical runtime or build errors remain.

### 13.3 Specific regression: Insights bottom overlap

The recording and earlier design notes identify an overlap between the floating bottom navigation and the lower Insights / AI Visual Report area.

Acceptance criteria:

- Scroll content can move fully above the dock.
- The Generate Visual Report action can be brought completely into view.
- The button is not covered by the dock or system navigation area.
- The button remains tappable at the bottom-most scroll position.
- The same clearance logic works on other scrollable screens.
- The fix uses measured/inset-aware layout behavior, not only a hard-coded spacer.

### 13.4 Test reporting

For every test, record one of:

- **PASS** — executed and passed.
- **FAIL** — executed and failed; include reproduction steps and logs.
- **NOT RUN** — not executed; state why.
- **BLOCKED** — could not execute due to a missing device, permission, service, or environment dependency.

Never claim a test count, test pass rate, release build, signed APK, or device validation unless it was actually performed in the current repository/environment.

### 13.5 Build and APK delivery

- Read the actual Gradle tasks and build variants before choosing a build command.
- Use the existing application ID, package identity, signing configuration, and release policy unless there is an explicitly approved change.
- Do not hard-code a Kotlin, Compose, SDK, or Gradle version from this document; use the project's real configuration and update only when necessary.
- Run available unit tests, lint, and build checks.
- Build the appropriate APK variant from the actual project configuration.
- Install and launch the generated APK on an emulator/device when available.
- Confirm that the output file exists and is the newly generated artifact.
- Do not include signing credentials or private keys in the deliverable.
- Provide the APK, updated design documents, and truthful test report.

A release APK may be copied to `dist/apk/app-liquid-glass-updated.apk` only if that destination fits the repository's existing release workflow. Do not create several confusing APK filenames or claim a release build if only a debug APK was produced.

---

## 14. Repository facts that must be verified

The older files contain some implementation-specific statements. They must be checked against the actual project before they are treated as facts.

| Previously stated item | Required action |
|---|---|
| Native Android Jetpack Compose | Confirm the actual Android UI framework and source sets |
| Kotlin 2.2.10 | Read the actual Kotlin plugin version; do not upgrade just to match this document |
| Compile SDK 36 / Target SDK 35 / Min SDK 24 | Read the Android Gradle configuration and manifest; preserve current supported settings unless a justified change is required |
| `./gradlew testDebugUnitTest` | Confirm the task exists and run it if available |
| `./gradlew syncReleaseApk` | Confirm whether this is a real project task; do not assume it is a standard Gradle task |
| 33/33 unit tests passing | Treat as an unverified historical claim until the current tests are executed |
| Room SQLite | Confirm whether Room is actually used |
| Supabase endpoint diagnostics | Confirm whether Supabase is actually integrated and whether a safe health probe exists |
| AI Visual Report / AI Productivity Advisor | Confirm the real service, local fallback, API path, and current behavior |
| 14 built-in tools | Compare the list in Section 7.6 with the actual tool registry |
| `SketchComponents.kt` / `SketchCard` | Confirm these files/components exist and are still used |
| `MainActivity.kt` / `ReportsScreen.kt` | Confirm actual paths and names; do not rely on absolute paths from another machine |
| Existing icon assets | Inspect the current mipmap/adaptive icon resources before replacing them |
| Web companion | Confirm that a separate Web application exists before adding Web-specific dependencies or files |

Do not use machine-specific absolute paths such as `/Users/...` in project documentation. Use repository-relative paths only after confirming them.

---

## 15. Explicit non-goals and implementation rules

### 15.1 Do not

- Do not redesign only the Home screen and leave the rest of the application unchanged.
- Do not replace the application with a demo, mockup, static preview, or unrelated project.
- Do not remove features simply because they complicate the layout.
- Do not invent routes, tools, metrics, settings, or backend services.
- Do not replace actual data with sample data.
- Do not assume that a visible button works; test its complete action.
- Do not add the React Liquid Glass npm package to a native Android-only source set.
- Do not claim guaranteed 120fps, physical refraction, WCAG AAA compliance, or test success without verification.
- Do not use fixed padding as the only solution to floating navigation overlap.
- Do not embed private API keys in the APK or client-side code.
- Do not add neon, lime, bright blue, terracotta, rainbow gradients, random blobs, or gratuitous glow effects.
- Do not make every element translucent or wrap every item in a glass card.
- Do not change database schemas, API contracts, authentication, or persistence behavior as a side effect of the redesign.
- Do not overwrite the only working source copy or remove existing build configuration.

### 15.2 Required implementation sequence

1. Inspect the complete repository and identify the real framework and build configuration.
2. Inventory every existing screen, component, route, data flow, and interactive control.
3. Reproduce and document visible and source-confirmed defects.
4. Confirm the canonical token source in `designglass.md` and map it into the actual platform theme.
5. Build or adapt the reusable glass component foundation.
6. Redesign all existing screens according to Section 7.
7. Fix navigation, content insets, keyboard, sheet, and system-bar behavior.
8. Preserve and regression-test every existing feature and data flow.
9. Run visual, accessibility, performance, and functional QA.
10. Build, install, and validate the updated APK where the environment allows.
11. Deliver the APK and a factual report of changes, tests, and unresolved blockers.

### 15.3 Definition of done

The work is complete only when:

- Every existing screen has been audited and brought into the agreed design system.
- Every implemented screen follows the canonical Liquid Glass tokens from `designglass.md`.
- The native Android/Web implementation boundary is respected.
- The bottom navigation overlap on Insights is fixed and regression-tested.
- Existing task, habit, calendar, tool, reminder, insights, export, and diagnostics behavior is preserved and tested wherever those features exist.
- Accessibility, responsive layouts, and performance fallbacks have been checked.
- Build and test results are reported truthfully.
- A newly built APK is delivered and its file existence is verified.
- Any item that could not be tested or fixed is explicitly listed with its reason.

**Start with repository inspection. Do not begin by changing colors, adding blur, or installing a package. The source code determines the actual implementation; this document defines the intended product experience.**
