## Why

The eraser only clears one cell at a time, so removing a large same-color area takes many taps. The bucket already flood-fills a 4-connected same-color region, and erasing is just that fill with an empty color — but there was no way to reach it from the eraser.

## What Changes

- The eraser gains a region-erase mode, toggled by double-tapping the eraser in the floating tool dock (a single tap still just selects the eraser).
- In that mode a single tap clears the 4-connected region of the tapped color (exactly the region the bucket would fill), as one undo step.
- The mode is shown by a distinct highlighted state on the eraser button and in the tool label; the ordinary single-cell erase remains the default.

## Capabilities

### New Requirements
- `pattern-editing`: a region-erase mode on the eraser, reached by double-tap.

### Modified Capabilities
- `pattern-editing`: the region eraser is tap-only, like the bucket.

## Impact

- **Core**: `editor/GridEditor.kt` (`regionErase`) + test.
- **App**: `state/EditingSession.kt` (mode + apply), `PindouController.kt` (exposure), `ui/editor/EditorScreen.kt` (double-tap button, highlight, label, drag behavior).
- No dependency or manifest change.
