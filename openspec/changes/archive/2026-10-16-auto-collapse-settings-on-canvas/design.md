## Context

`EditorScreen` renders, top to bottom: the optional 设置 panel (`ParamsPanel`, gated by `paramsExpanded`), an optional edit action bar, the canvas `Box` (hosting `PatternGrid`), and the info/color-table panels. The 设置 panel therefore steals vertical space from the canvas. Today the only way to collapse it is the 设置 button in the top bar.

Collapsing the panel inline reflows the column: the canvas `Box` (with `weight(1f)`) immediately grows, so the viewport jumps under the user's finger mid-gesture. This was observed on device and rejected as poor UX.

`PatternGrid` already owns all canvas gestures in a single `awaitEachGesture` block. Its first step is `awaitFirstDown(requireUnconsumed = false)` followed by an early return when `down.isConsumed` — that is exactly the signal that a floating overlay (the draggable `FloatingToolbar`) consumed the touch rather than the canvas. The `FloatingPeekButton` consumes its own touches the same way.

## Goals / Non-Goals

**Goals:**
- Any gesture that lands on the pattern canvas (view-mode tap, edit-mode draw, pan/zoom) collapses the 设置 panel when it is open.
- Opening or collapsing the panel never resizes the canvas, so nothing shifts under the user's finger.
- Overlay interactions (tool dock, peek button) do not collapse it.
- Minimal, local change with no model/persistence impact.

**Non-Goals:**
- Do not change the 高级 section's own expansion state or persistence.
- Do not change what each gesture does (drawing, selection, pan/zoom stay identical).
- Do not add a "changed something" condition — collapse is unconditional on canvas touch (per product decision).

## Decisions

- **Expose `onInteract: () -> Unit = {}` on `PatternGrid` and fire it after the `down.isConsumed` early-return.** This is the single point in the existing gesture pipeline where a non-overlay down is known, so it covers tap, draw, pan, and zoom without adding a second gesture detector that could fight the existing one.
  - Alternative: wrap the canvas `Box` in a separate `pointerInput`. Rejected — two competing gesture detectors on the same area risk consuming or racing the grid's own gestures.
- **Collapse the outer panel only (`paramsExpanded = false`) in `EditorScreen`.** `advancedExpanded` is intentionally session-persistent (see `generation-settings`), so it is left untouched; when the panel is reopened it reappears as the user left it.
- **Unconditional collapse** (no dirty flag), matching the confirmed product decision: the panel is transient UI over the canvas, so touching the canvas always dismisses it.
- **Move the panel out of the column and into the canvas `Box` as a top-aligned overlay** (`Surface` with shadow), so the canvas `Box` no longer depends on `paramsExpanded` and never changes size when the panel opens or collapses.
  - Alternative: keep the inline panel and animate its height. Rejected — an animation still resizes the canvas (moving content mid-gesture), just more slowly, and it would leave the canvas smaller while the panel is open.
  - Alternative: keep the inline panel and compensate the grid offset when collapsing. Rejected as fragile — it needs the removed height measured and applied in the same frame to avoid a flicker.
- **Block touches that land on the overlay panel** with a transparent `matchParentSize` gesture catcher (the same pattern `FloatingToolbar` uses), so a tap on the panel never falls through to the grid and edits a cell. Because the catcher consumes the down, `PatternGrid`'s existing `down.isConsumed` early-return already skips `onInteract` for those touches.

## Risks / Trade-offs

- [A pan/zoom that the user started only to reposition while reading the panel will also collapse it] → Accepted: the panel is meant to be transient, and the 设置 button reopens it with one tap.
- [A gesture that starts on the canvas but is consumed by an overlay must not collapse] → The existing `down.isConsumed` early-return already excludes overlays; `onInteract` is fired after that check.
