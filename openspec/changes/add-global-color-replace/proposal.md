## Why

Editing a pattern one cell at a time is impractical when the user wants to swap an entire color — e.g. a bead they do not have, replaced everywhere with one they do. The `.pindou` format was designed to be code-based precisely so a whole code can be re-mapped, and this "replace all color X with Y" was explicitly reserved as a future capability (design D1).

## What Changes

- Add a **替换 (replace)** tool to the edit tool dock. With the current brush color as the target, tapping any bead cell replaces **every** cell of that code across the grid with the brush color.
- The replace acts on a single tap only (no drag-drawing); it applies immediately with no confirmation dialog and is recorded as one undo step.
- After a replace, `paletteUsed` and the color table recompute from the result (a source color may disappear, a target color may appear); empty holes and the reference image are untouched.
- The tool appears as an icon in the floating dock alongside paint / erase / eyedropper / bucket / move, with a mode-aware info-bar label.
- Add a pure `GridEditor.replaceAll(grid, from, to)` core operation (exact code match; returns the same instance when nothing changes).

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `pattern-editing`: the tool set gains a replace tool; the icon tool bar, the compact dock, the tap/drag tool-behavior rule, the single-tap rule, and the undo/redo rule are extended to cover it.

## Impact

- **core**: `editor/GridEditor` gains `replaceAll` (Android-independent, `model`-only); new unit tests.
- **app**: `state/EditingSession` gains `replaceColor(from, to)`; `EditTool` gains `REPLACE`; `PindouController` forwards it; `ui/editor/PatternGrid` routes a replace tap to a callback; `ui/editor/EditorScreen` wires the tool button, icon, and info-bar label.
- No format, persistence, or navigation changes.
