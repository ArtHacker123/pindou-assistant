## Context

`PatternGrid` handles drawing with a raw `awaitEachGesture` loop. The gesture is ambiguous at finger-down: the first contact point might become a paint stroke, or it might be the first finger of a two-finger pan/zoom. The previous logic edited the down cell immediately and tried to cancel later, which cannot undo an already-committed edit. Separately, grid edits during a stroke were only committed at stroke end, so painting did not render until the finger lifted.

## Goals / Non-Goals

**Goals:**
- A two-finger gesture must never edit the grid.
- Single taps must apply the active tool reliably (paint dot, bucket fill, eyedropper).
- Drag strokes must render live, while still collapsing into one undo step.
- Keep the existing pure `GridEditor` core and the `beginStroke`/`endStroke` coalescing model.

**Non-Goals:**
- Changing the editing tools themselves or the palette-remap / resize rules.
- Touch-pressure or stylus handling; multi-touch drawing.

## Decisions

### D1: Classify before editing (defer the first edit)
Do not edit on finger-down. Decide the gesture from the pointer stream:
- `pressedCount >= 2` → navigation; if a stroke was already active, finalize it (keep the drawn part) and stop drawing.
- single finger crossing `touchSlop` → start a stroke and paint from the down cell (paint/erase only).
- single finger lifted without crossing slop → a tap; apply the tool once at the down cell.

Rationale: the ambiguity is only at the start; deferring the first mutation removes the stray-cell problem entirely, and tap-on-up is imperceptible.

Alternative rejected: apply immediately but keep the edit rollback-able. Fragile (requires the document to lag the scratch, which conflicts with live preview) and can flicker.

### D2: Distinguish drag tools from tap tools
Paint and erase draw continuously on drag; bucket and eyedropper act on a tap; the move tool only navigates. A `dragDraw` flag is passed from the editor so the grid stays tool-agnostic about the actual operation.

### D3: Write-through during a stroke for live preview
`EditingSession.strokeEdit` now writes each change to the document while the stroke is open, so the canvas updates immediately. The single undo entry is still pushed once in `endStroke`, preserving one-step undo per stroke.
