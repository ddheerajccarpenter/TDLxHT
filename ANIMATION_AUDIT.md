# TDlxHT — Centralized Animation System Audit (`ANIMATION_AUDIT.md`)

**Specification Standard:** Apple Liquid Glass Motion & Hallmark Anti-Slop  
**Frame Target:** Rock-solid 60 FPS with 5–10% Velocity Motion Blur  
**Idle Policy:** Zero continuous animations when untouched (Calm When Untouched)  

---

## 1. 20-Token Centralized Motion System

| Token Name | Specification | Damping | Stiffness / Duration | Trigger | Lifecycle Category | Target Component | Reduced-Motion Fallback |
|---|---|---|---|---|---|---|---|
| `MotionMicro` | Spring | 0.85 | 800f | Chip tap, toggle switch | USER-TRIGGERED | Filter chips, small toggles | Snap (0ms) |
| `MotionButtonPress` | Spring | 0.65 | 500f | Touch down on interactive element | USER-TRIGGERED | All buttons, cards, list rows | Scale 1.0 (no movement) |
| `MotionButtonRelease`| Spring | 0.54 | 450f | Touch release (Kern elastic rebound) | USER-TRIGGERED | All buttons, cards, list rows | Scale 1.0 (instant snap) |
| `MotionIconMorph` | Spring | 0.60 | 420f | State toggle (Plus -> X, Star, Filter)| STATE-DRIVEN | Quick create plus, favorite star | Instant icon switch |
| `MotionSelection` | Spring | 0.72 | 400f | Tab / segmented control selection | STATE-DRIVEN | Bottom dock indicator, segmented pill | Instant position snap |
| `MotionNavigation` | Spring | 0.78 | 420f | Screen / Pager page change | USER-TRIGGERED | HorizontalPager, Calendar months | Instant cut |
| `MotionScreenEnter` | Tween | N/A | 220ms (FastOutSlow) | Screen presentation | ONE-SHOT | Detail screens, auth sheet | Instant opacity 1.0 |
| `MotionScreenExit` | Tween | N/A | 180ms (FastOutSlow) | Back navigation / dismissal | ONE-SHOT | Detail screens, auth sheet | Instant removal |
| `MotionSheetEnter` | Spring | 0.80 | 380f | Bottom sheet modal presentation | ONE-SHOT | Quick add sheet, tool runner | Instant slide-up |
| `MotionSheetExit` | Tween | N/A | 200ms (FastOutSlow) | Bottom sheet dismissal | ONE-SHOT | Quick add sheet, tool runner | Instant dismiss |
| `MotionListInsert` | Spring | 0.75 | 380f | New task / habit added to list | ONE-SHOT | LazyColumn items | Instant appearance |
| `MotionListRemove` | Tween | N/A | 180ms (FastOutSlow) | Task / habit deleted from list | ONE-SHOT | LazyColumn items | Instant removal |
| `MotionCompletion` | Spring | 0.52 | 440f | Checkmark vector path draw | ONE-SHOT | Task & habit checkboxes | Instant checkmark display |
| `MotionProgress` | Spring | 0.82 | 320f | Value ratio update (0.0 to 1.0) | STATE-DRIVEN | Progress bars, habit rings, gauges | Instant fill to value |
| `MotionCounter` | Spring | 0.48 | 480f | Number increment / decrement | STATE-DRIVEN | Streak counters, task done count | Instant number replacement |
| `MotionExpand` | Spring | 0.78 | 360f | Accordion / subtask expansion | USER-TRIGGERED | Settings groups, subtask lists | Instant height expansion |
| `MotionCollapse` | Tween | N/A | 180ms (FastOutSlow) | Accordion / subtask collapse | USER-TRIGGERED | Settings groups, subtask lists | Instant height collapse |
| `MotionRefresh` | Tween | N/A | 750ms (Linear) | Active network sync | CONTINUOUS | AI refresh spinner while refreshing | Static icon |
| `MotionSuccess` | Spring | 0.55 | 420f | Operation success confirmation | ONE-SHOT | Copy badge, favorite pop | Instant badge display |
| `MotionError` | Spring | 0.45 | 600f | Destructive action / validation error | ONE-SHOT | Validation text, delete warning | Instant red border |

---

## 2. Strict Animation Lifecycle Rules

1. **Category A — ONE-SHOT Animations**:
   - Must execute once per explicit user or system trigger.
   - Guarded by stable remember keys so they **never replay** during screen recomposition, scroll events, or orientation changes.
2. **Category B — STATE-DRIVEN Animations**:
   - Run exclusively when their bound state value changes (`animate*AsState`).
   - Settle smoothly without infinite oscillations.
3. **Category C — CONTINUOUS Animations**:
   - Strictly reserved for active background processes (e.g. `MotionRefresh` while `isRefreshingAi == true`).
   - Zero continuous animations when the app is untouched (CPU load drops to 0%).
4. **Category D — USER-TRIGGERED Animations**:
   - Immediate feedback on touch down (`MotionButtonPress`), snappy spring settling on release (`MotionButtonRelease`), and cancellable on scroll drag.

---

## 3. Audited Defects & Lifecycle Fixes (Tools Page Rework)

### BUG-19: Unremembered Staggered Entrance in LazyColumn (Disappearing Cards)
- **Problem**: Calculator cards became dim, disappeared to 0% opacity, and delayed popping in during vertical scroll.
- **Root Cause**: `Modifier.staggeredEntrance(index)` relied on a locally remembered `var hasAppeared by remember { mutableStateOf(false) }`. When cards were recycled during scrolling, `hasAppeared` was re-initialized to `false`, resetting `alpha` to `0f` and triggering `animateFloatAsState` with a delay of `index * 35ms`.
- **Fix**: Removed `staggeredEntrance` from recycled list items. Adopted Compose 1.7 native `Modifier.animateItem()` with `fadeInSpec = tween(200)`, `fadeOutSpec = tween(150)`, and `placementSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)`. Scrolling now recycles items at 100% stable alpha with zero replayed animations.

### BUG-20: Disappearing Header, Search & Filter Controls (Nested Scroll Architecture)
- **Problem**: Header title, search input, and category chips disappeared during scroll, and horizontal category scrolling clashed with vertical list dragging.
- **Root Cause**: Header, search, and category chips were individual `item {}` entries inside `LazyColumn`.
- **Fix**: Implemented Option A (Persistent Header). Header, search bar, and horizontal filter chips are pinned in a top container, while `LazyColumn` scrolls underneath with `weight(1f)`. Category scrolling is isolated horizontally and search/filter controls remain permanently accessible.

### BUG-21: Abrupt List Shuffling on Favorite Star Toggle
- **Problem**: Tapping a favorite star moved the card to index 0, violently shifting all other cards.
- **Root Cause**: `filteredTools` ran `orderedAll.partition { favoriteToolIds.contains(it.id) }`, recalculating card order instantly on state change.
- **Fix**: Decoupled favorite state from active list sorting. Tapping star animates the star icon and highlights the card border while preserving the item's position.


