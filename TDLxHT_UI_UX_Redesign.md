# TDLxHT — Complete UI/UX Redesign Master Prompt

> **Purpose:** Use this file as the implementation brief for a coding agent working inside the existing TDLxHT website repository. This is a request to redesign and implement the real website—not to generate a mockup, a screenshot, or a second demo app.

## 1. Project context

TDLxHT is the website for a native Android productivity app. The current landing page describes an offline-first task manager with dual-streak habits and 14 built-in calculators. It currently includes a hero/dashboard preview, feature content, calculator information, an APK download area, an account portal, a GitHub/release link, and a theme control.

The supplied materials have different jobs:

- **Current website screenshot:** establishes the existing product, content, branding, controls, and current visual problems.
- **Screen recording:** shows the current navigation and scroll behavior. Inspect it before changing navigation code.
- **Two UI/UX reference screenshots:** use their *principles*—strong hierarchy, removing content that does not help the user, and consistent components—not their exact layout or wording.
- **Palette reference:** use the colors listed in Section 4 as the starting palette.

Do not copy the reference designs literally. Do not invent a different product or remove real app capabilities just to make the landing page look cleaner.

## 2. Main objective

Redesign the complete website UI and UX from the ground up while preserving the existing product functionality, links, downloads, route behavior, backend integrations, and valid content. The result should feel intentionally designed by a product designer: clear, restrained, distinct, and coherent—not like a generic AI-generated SaaS template.

The website should:

1. Make it immediately clear what TDLxHT does and that the Android app is offline-first.
2. Give the user a clear route to explore features, inspect calculators, download the APK, or open the account portal.
3. Have one reliable bottom navigation system on desktop and mobile.
4. Keep the active navigation item synchronized with the section actually in view and with route changes where applicable.
5. Work cleanly at narrow mobile widths, tablet widths, laptop screens, large monitors, and browser zoom levels.
6. Use a small, consistent design system rather than styling every section independently.
7. Load quickly and remain usable on modest hardware, including devices with weaker GPUs.

**Do not stop after writing a plan.** Inspect the repository, identify the actual causes of the issues, implement the redesign, and test it. Do not return only suggestions or a static UI preview.

## 3. Audit the existing code before editing

First inspect the current repository and report the relevant findings briefly in your working notes. Identify:

- Framework, router, entry points, component structure, styling method, installed UI/icon libraries, and existing font setup.
- Every landing-page section and the real destination/action behind each navigation item.
- Whether “Portal” is an in-page section, a client-side route, an external URL, or a combination of these.
- Existing download URLs, APK size/version content, release links, theme logic, analytics if present, and all other functioning actions that must be retained.
- Every source of scroll state, active-nav state, `scrollIntoView`, anchor navigation, smooth scrolling, `IntersectionObserver`, scroll listeners, animation libraries, page-level transforms, and overflow rules.
- Any repeated CSS or component styles that cause inconsistent spacing, radii, typography, colors, states, or breakpoints.
- Any horizontal overflow, clipped text, layout shifts, nested scroll containers, or fixed-position elements trapped inside transformed ancestors.

Do not assume the problem is caused by one line of CSS. Trace the actual behavior in the implementation. Do not migrate frameworks, replace the backend, change the product’s business logic, or add major dependencies just to achieve the redesign. Reuse the current stack unless there is a demonstrated technical reason not to.

Before editing, create a concise checklist of the existing actions that must keep working. After editing, verify each one.

## 4. Visual direction and color system

### Design character

Create a **minimal, editorial, product-focused interface**. Use the references as guidance for information hierarchy and consistency: one clear purpose per section, meaningful whitespace, a strong headline, restrained accent color, predictable component behavior, and fewer but more useful elements.

The current page feels crowded in the dashboard preview, uses competing accent colors, and leans on decorative background curves and repeated dashboard-style cards. Replace that visual noise with a purposeful composition. Do not simply recolor the existing layout and call it a redesign.

### Starting palette

Use the palette reference as the foundation:

| Token | Color | Intended role |
|---|---|---|
| Black | `#000000` | Primary dark canvas and strongest contrast areas |
| Spicy Paprika | `#CF5C36` | Primary accent, selected state, and main CTA |
| Lavender Blush | `#EEE5E9` | Light surface and light-theme background/surface color |
| Grey | `#7C7C7C` | Secondary visual tone, borders or non-essential labels where contrast allows |
| Apricot Cream | `#EFC88B` | Secondary warm highlight, small metadata accents, or occasional emphasis |

Add only the neutral shades required for legibility and component states—for example, a near-black raised surface, a dark border, and a high-contrast text color. Treat those as functional neutrals, not new accent colors.

Rules:

- Use **Paprika as the dominant accent**. Apricot is secondary; do not make both compete for every button and badge.
- Keep charts, icons, badges, navigation, and buttons in the same semantic color system. Remove the current unrelated green/blue/orange mix unless a color has a real data meaning that must be preserved.
- Do not use gradients as decoration, neon borders, glow effects, rainbow charts, animated background waves, glassmorphism, or blurred translucent panels.
- Do not put small body text in Grey on a background where it fails contrast. Use a darker/lighter neutral variant as needed.
- Keep the existing theme toggle if it is functional. Both dark and light themes must use the same semantic tokens and preserve readable contrast; do not implement a separate, inconsistent palette for each theme.
- Buttons, links, selected navigation, focus rings, and success/error states must be distinguishable without relying on color alone.

### Typography

If a font file, name, or visual font reference is supplied with the task, inspect it and use it where licensing and technical compatibility permit. If none is supplied, use **Geist Sans** as the default, with sensible system sans-serif fallbacks. Use a monospaced face such as Geist Mono only where it improves scanability for small technical metadata or numerical values.

- Define a deliberate type scale for hero headings, section headings, body copy, labels, and technical metadata.
- Use responsive type sizing with sensible maximum line lengths. Avoid oversized headings that crush the section or make mobile layouts wrap awkwardly.
- Use strong weight contrast rather than excessive uppercase labels or arbitrary letter spacing.
- Avoid loading unnecessary font families or third-party font resources without checking the existing project setup.

## 5. Page structure and content hierarchy

Audit the existing content first, then reorganize it into a clear flow. Keep only content that helps a user understand the app, evaluate it, or take an action. Do not delete a working product feature merely because its current presentation is cluttered.

Recommended information architecture:

1. **Compact header:** TDLxHT wordmark and version; a small number of utility actions. Do not repeat the complete primary navigation here if the bottom dock is the primary navigation system. Preserve the theme control if it works. Keep an obvious route to the Account Portal.
2. **Dashboard/Home hero:** concise product message, a short explanation of offline-first task/habit tracking and built-in calculators, one dominant “Download APK” action, and one secondary action such as “View features” or “GitHub Releases.” A real product preview may be used if it helps users understand the app.
3. **Features:** organize the existing, verified product features into a small number of clearly differentiated groups. Each card must have a meaningful purpose; avoid making every feature a nearly identical card.
4. **Calculators/tools:** make the availability of the 14 built-in calculators easy to understand. Do not turn every calculator into a separate navigation destination unless that is the app’s real architecture. Preserve accurate names and behavior.
5. **Download area:** provide the valid APK link, current version, verified file size, Android compatibility details, and QR code if one already exists or can be generated from the actual download URL. Include GitHub Releases as a secondary option. Do not invent checksums, version numbers, compatibility guarantees, or download statistics.
6. **Account Portal entry:** retain the working portal route/action. If it is a separate page, treat it as a route, not as a fake scroll section.
7. **Footer:** concise secondary links, GitHub/release links, version or legal details if they currently exist. Do not duplicate the entire header and bottom navigation.

This is a recommended structure, not permission to fabricate content. If the repository contains a different valid information architecture, preserve the real functionality and adapt this structure accordingly.

### Hero and product preview

The current hero is visually imbalanced: large empty areas coexist with a dense, tall dashboard panel, and its rings use multiple competing colors. Improve the balance and make the first viewport answer three questions quickly: what is this, why should I care, and what can I do next?

- Keep the main copy short and specific. No exaggerated claims or generic filler slogans.
- Give the primary download action the strongest hierarchy.
- Make any dashboard/product mockup support the message instead of overpowering it.
- Do not invent user counts, ratings, testimonials, activity metrics, or “trusted by” claims.
- If sample content is needed to illustrate the product, label it as illustrative and keep it realistic. Do not present sample metrics as live user data.
- Remove decorative charts, rings, badges, arrows, and floating cards that do not explain a real feature.

## 6. Bottom navigation — required on desktop and mobile

The bottom navigation is a primary requirement. Redesign it rather than removing it. It must look intentional at both desktop and mobile sizes and must remain predictable while scrolling.

### Destinations

Keep the **four primary destinations consistent across breakpoints**:

1. **Dashboard** — the home/hero section.
2. **Features** — the features section.
3. **Calculators** — the tools/calculators section.
4. **Download** — the APK download section.

Keep **Account Portal** and **GitHub/Releases** available through secondary utility links in the header, hero, download section, or footer. If auditing the repository proves that the Portal is an essential fifth in-page destination, document the evidence before deviating; do not squeeze five or more items into a narrow mobile dock.

### Visual behavior

- Use one compact, centered bottom dock on desktop and a properly fitted bottom bar on mobile. It should feel like part of the design system, not a floating demo widget.
- Use consistent icon size, label alignment, padding, hit areas, borders, radius, item width, and selected-state treatment.
- The selected item should be clear but restrained: use the primary accent and a subtle background or indicator, not a glow, oversized icon, or dramatic scaling.
- All navigation items must have meaningful labels and accessible names. Icons must be consistent in visual weight and must come from the existing icon library if suitable. Do not mix emoji with outline icons.
- The dock must not cover important content, form controls, download buttons, footer links, or focus outlines. Reserve bottom space using a layout-aware offset, including `env(safe-area-inset-bottom)` where appropriate.
- Keep it anchored to the viewport. Inspect its ancestors for `transform`, `filter`, `perspective`, `contain`, `will-change`, and overflow rules that can change fixed-position behavior. Do not leave a supposedly fixed dock inside an ancestor that causes it to move or scale with section content.
- It must not become oversized, clipped, horizontally scrollable, or cover the page when the browser is zoomed. Support native browser zoom; **do not disable pinch zoom, Ctrl/Cmd +/- zoom, or accessibility scaling**.
- Account for short viewports and browser UI/safe-area insets. On mobile, ensure it does not collide with the device home-indicator area.

### One source of truth for active navigation

The recording and screenshot show a mismatch: the Calculator item can appear active while the hero/dashboard is visible, and the recording also contains severe magnification/cropping and navigation overflow. Investigate the implementation and fix the root cause rather than hardcoding a different tab.

Implement one authoritative navigation-state model:

- In-page destinations must point to real, unique section IDs such as `#dashboard`, `#features`, `#calculators`, and `#download`.
- Active state must reflect the section actually in view during manual wheel, trackpad, keyboard, touch, and scrollbar scrolling—not just the last clicked nav item.
- Clicking a destination must navigate to that real section, account for any sticky header/offset, and update the URL hash when that matches the existing routing approach.
- Use one centralized scroll-spy mechanism (for example, a carefully tuned `IntersectionObserver` or a single animation-frame-throttled scroll tracker). Do not create several competing listeners or separate `setActiveTab` logic for each component.
- Avoid flickering between adjacent sections when section boundaries pass the viewport. Define one clear activation rule and test both short and tall sections.
- During smooth scrolling, navigation feedback must remain stable and must settle on the real destination. Do not use arbitrary timeouts to fake active state.
- Do not treat route links as scroll sections. If Portal opens a separate route, derive its active state from the route; on that route, the landing-page section spy must not override the route state.
- Handle direct page loads with a hash, browser Back/Forward, refresh, and navigation initiated elsewhere in the page.
- Respect `prefers-reduced-motion`. When reduced motion is requested, use immediate or reduced-motion scrolling rather than forcing smooth animation.
- Do not trap the user in navigation, prevent natural scrolling, cancel ordinary wheel/touch events, or interfere with browser zoom.

### Navigation acceptance tests

Run these checks after implementation:

- Load the page at the top: **Dashboard** is active.
- Manually scroll into Features: **Features** becomes active without clicking the dock.
- Manually scroll into Calculators: **Calculators** becomes active.
- Manually scroll into Download: **Download** becomes active.
- Click each destination: the correct section is reached and active state settles correctly.
- Click Download, scroll back upward manually, and verify the active state follows the viewport rather than staying stuck on Download.
- Load a valid `#features` URL directly and refresh it; the browser lands at the correct section with no heading hidden behind a sticky element.
- Use Back and Forward after section navigation and after any real route change; behavior remains coherent.
- Scroll through the whole page with mouse wheel, trackpad, scrollbar dragging, Page Up/Down, Home/End, and mobile touch.
- Zoom the browser in and out and test narrow widths. The dock must not break, scale unpredictably, or push the document sideways.
- Confirm the last section and footer are reachable and not hidden beneath the dock.

## 7. Diagnose the magnification, cropping, and overflow shown in the recording

Do not assume the apparent magnification is definitely caused by CSS; confirm it. Inspect the recording against the code and determine whether the behavior is coming from page-level transforms, scroll-driven animation, horizontal overflow, a nested scroll container, fixed positioning inside a transformed parent, gesture handlers, or browser zoom.

Then correct the underlying layout or event-handling defect. Specific requirements:

- The website must not unexpectedly scale, jump, or crop horizontally during normal navigation or scrolling.
- No section or navigation interaction may create a temporary giant layout, off-screen content, or a horizontal page scrollbar at normal zoom.
- Avoid applying `scale()` or scroll-linked transforms to the whole document or to the fixed navigation. If an existing animation is responsible, remove or redesign it rather than layering more CSS overrides over it.
- Check widths, `min-width`, `100vw` usage, transforms, absolute positioning, and responsive breakpoint logic. Ensure cards and diagrams can shrink or reflow.
- Do not fix the symptom by forcing `overflow-x: hidden` on everything without finding the cause; that can hide content and accessibility issues. Use clipping only on the specific decorative element that needs it.
- Do not intercept pinch gestures or browser zoom. The page must remain usable under normal browser magnification.

## 8. Build a reusable design system first

Before styling each section, define central tokens and reusable components using the project’s current styling approach. At minimum, standardize:

- Color tokens for backgrounds, surfaces, main text, muted text, borders, primary accent, secondary accent, focus, success, warning, and error.
- Type scale, font weights, line heights, readable line lengths, and label styles.
- Spacing scale, content max-widths, grid gaps, responsive breakpoints, and section spacing.
- Border widths, corner radii, elevation/shadows where justified, and interactive transitions.
- Button variants, navigation items, feature cards, section headings, tags/metadata, icon containers, download details, inputs, and focus states.

Every shared component must have consistent default, hover, focus-visible, active, disabled, and loading states where relevant. Do not add hover animation to elements that are not interactive. Keep animation short, functional, and low-cost; avoid springy motion on every object.

Use a consistent grid and alignment system. Content should share left edges and spacing rules; do not independently center or offset every element. Set sensible content widths so large desktop screens do not create enormous line lengths.

## 9. Responsive requirements

Design from narrow screens upward or otherwise verify all major breakpoints explicitly. Do not treat desktop as the only finished layout.

- **Small phones:** no horizontal page scroll; readable text; buttons are large enough to tap; bottom navigation fits without compressed labels; cards stack in a useful order; decorative imagery never crowds content.
- **Large phones:** keep sensible side padding and avoid overly wide text blocks.
- **Tablet:** use space efficiently; prevent a desktop two-column layout from squeezing content into unreadable columns.
- **Laptop/desktop:** maintain a deliberate max-width, well-balanced hero, consistent grid, and comfortable spacing. Keep the dock centered and fully visible.
- **Large monitors:** do not stretch body copy, feature cards, or navigation across the entire screen.
- **Zoom/accessibility scaling:** layouts reflow instead of clipping. At high zoom, allow appropriate wrapping or a breakpoint change rather than preserving a fixed desktop row at all costs.

Verify with actual browser viewport sizes at a minimum of approximately 360px, 390px, 768px, 1024px, 1280px, and 1440px wide, and at 200% browser zoom. Include height checks for short laptop windows and mobile safe areas. Do not use device sniffing when CSS media queries solve the problem.

## 10. Interaction, motion, and accessibility

- Use semantic HTML and real links/buttons; do not make clickable `<div>` elements without keyboard behavior.
- Provide visible `:focus-visible` states and sensible keyboard focus order.
- Meet WCAG AA contrast targets for normal text and controls where applicable.
- Give icon-only controls accessible names and tooltips where they are not self-explanatory.
- Preserve the theme toggle’s functional behavior if it already works, and ensure state is not lost unexpectedly on route navigation.
- Use `prefers-reduced-motion`; do not make scrolling, section entry, or page loading dependent on animation.
- Keep transitions purposeful—color, opacity, or a small positional change is usually enough. Do not animate every card on scroll.
- Do not add sounds, confetti, cursor effects, scroll-jacking, automatic carousel movement, or forced entrance animations.
- Preserve natural browser scrolling, text selection, keyboard shortcuts, and zoom.

## 11. Anti-AI-slop rules — non-negotiable

Avoid the common template patterns that make a site look automatically generated:

- No meaningless decorative gradients, glowing blobs, neon outlines, or unrelated background curves.
- No glassmorphism or translucent blur panels just to signal “modern.”
- No arbitrary floating pills, badges, status chips, or icon boxes without a user purpose.
- No repetitive row of identical cards for every piece of content.
- No random emoji icons; use one coherent icon family.
- No invented metrics, fake testimonials, fabricated download counts, fake ratings, or unsupported trust claims.
- No copy such as “revolutionary,” “supercharge your productivity,” “next-gen experience,” or vague AI-marketing filler.
- No giant blank areas used only to make the interface look luxurious, and no cramped clusters of many cards used to make the page look feature-rich.
- No needless animated counters, custom cursors, parallax on large sections, or 3D effects. Performance and clarity take priority.
- No inconsistent spacing, radii, shadows, icon stroke widths, or button heights between sections.
- No fake controls or buttons that do nothing.

The visual quality should come from typography, composition, alignment, deliberate contrast, real product content, and restraint—not decoration. If an element does not improve comprehension, navigation, trust, or a real task, remove it.

## 12. Performance and code quality

- Keep existing dependencies where possible. Before adding a library, verify that the existing stack cannot handle the need.
- Avoid heavy animation packages, unnecessary video backgrounds, oversized imagery, and large synchronous work on scroll.
- Use optimized assets and lazy-load below-the-fold images where appropriate.
- Avoid layout shift when fonts, QR codes, screenshots, or preview panels load.
- Clean up event listeners and observers; make effects safe under the framework’s development/strict-mode behavior.
- Avoid duplicate CSS overrides. Remove obsolete styles once the new system is in place.
- Do not hardcode viewport-specific offsets that break at different heights; use measured layout values, CSS variables, or a clear token system.
- Keep code typed and component boundaries understandable if the existing project uses TypeScript.
- Do not store secrets in client-side code or change backend security as part of a visual redesign.

## 13. Preserve real functionality

The redesign must not break or silently change:

- APK download and its actual destination.
- GitHub repository/release links.
- Account Portal navigation and authentication behavior, if present.
- Theme switching, if currently supported.
- Calculator tools or existing calculator links.
- Any existing routes, URL hashes, forms, or functional buttons.
- Correct product/version/compatibility information.

Do not replace real content with lorem ipsum or a fake dashboard. Do not introduce a new backend, authentication provider, database, analytics service, or API. If an existing link or piece of content is broken, identify it explicitly and only change it when the correct destination or behavior can be verified from the repository.

## 14. Implementation workflow

Follow this sequence:

1. **Audit:** inspect the codebase and all supplied visual references/recording; identify root causes before changing the UI.
2. **Plan the hierarchy:** map existing sections and actions to the simpler information architecture; list what will be retained, moved, merged, or removed and why.
3. **Define tokens:** implement the palette, typography, spacing, radii, breakpoints, and interaction states centrally.
4. **Fix navigation architecture:** establish real section IDs, distinguish anchors from routes, implement the single active-state source, and correct the dock’s fixed positioning/overflow behavior.
5. **Redesign the page:** implement header, hero, features, calculators, download, portal entry, and footer using reusable components.
6. **Responsive pass:** validate all layouts and the dock at mobile, tablet, desktop, high zoom, short viewport, and safe-area conditions.
7. **Functional pass:** click every CTA and navigation item; verify the theme toggle, portal, downloads, GitHub/release destinations, and calculators.
8. **Visual pass:** remove inconsistent components, excess decoration, accidental overflow, fake content, and placeholder styles.
9. **Build/test:** run the project’s existing lint, type-check, test, and production build commands where available. Fix regressions introduced by this work.
10. **Report:** summarize the actual changes, explain the root cause found for the nav/zoom issue, list the tests run, and flag only genuine unresolved blockers.

Do not claim that a test passed unless it was actually run. If browser automation is available, inspect and interact with the site at multiple viewport widths. If it is unavailable, run the project checks that are available and clearly state what could not be verified.

## 15. Definition of done

The task is complete only when all of the following are true:

- [ ] The whole website has a coherent new visual system rather than isolated cosmetic edits.
- [ ] The reference screenshots’ principles—hierarchy, restraint, and consistency—are applied without copying their exact design.
- [ ] The provided palette is used intentionally and accent colors no longer compete.
- [ ] The font supplied by the user is used if available; otherwise Geist Sans (with fallbacks) is used consistently.
- [ ] The bottom nav is present, usable, and visually balanced on desktop and mobile.
- [ ] The active nav state follows the visible in-page section during manual scrolling and works when a destination is clicked.
- [ ] Route-based destinations such as Portal do not fight the landing-page scroll spy.
- [ ] The mismatch between visible section and highlighted nav item is fixed.
- [ ] The cause of the recording’s magnification/cropping/overflow behavior has been investigated and corrected where it originates in the page code.
- [ ] Browser zoom and reduced-motion settings remain respected.
- [ ] No important content is hidden behind the fixed dock; there is no unintended horizontal overflow at supported viewport sizes.
- [ ] Downloads, theme switching, portal navigation, GitHub/release links, and existing calculators still work.
- [ ] There is no invented data, placeholder copy, useless decoration, or fake interaction.
- [ ] Build/lint/type checks were run where available and any unresolved failures are reported accurately.

**Final instruction:** Prioritize clarity, navigational correctness, responsive behavior, and visual consistency over novelty. Make the website feel designed for TDLxHT specifically, not like a reskinned generic dashboard template. Every visual and interaction decision must have a reason.
