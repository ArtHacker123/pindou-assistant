## Why

Two gesture defects in edit mode:

1. Painting was applied on finger-down, before the gesture was understood. When the user then added a second finger to pan/zoom, the first cell had already been written into the stroke and was committed on lift — so a two-finger pan always left one stray filled cell.
2. Grid edits during a drag stroke were only committed on lift, so the canvas did not update until the finger was released (no live feedback).

## What Changes

- **Classify the gesture before editing.** No cell is edited on finger-down. A second finger always means pan/zoom and never edits. A single finger crossing the touch slop starts a stroke (paint/erase); a clean one-finger tap applies the active tool once (paint dot, bucket fill, or eyedropper).
- **Only the drag tools draw.** Paint and erase draw continuously; bucket and eyedropper act on tap; the move tool only navigates. A second finger joining mid-stroke keeps what was already drawn and switches to navigation.
- **Live preview.** Each cell changed during a stroke is reflected in the project immediately; the whole stroke still collapses into a single undo step committed at stroke end.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `pattern-editing`: two-finger navigation never edits the grid; single taps apply the active tool; drag strokes render live while still undoing as one step.

## Impact

- **App UI**: `ui/editor/PatternGrid.kt` (gesture classification via `awaitEachGesture`, new `dragDraw` flag), `ui/editor/EditorScreen.kt` (passes `dragDraw` for paint/erase).
- **App state**: `state/EditingSession.kt` (`strokeEdit` writes through to the document during a stroke for live feedback).
- **No format change.**
