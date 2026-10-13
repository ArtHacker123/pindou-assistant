## Why

In free-creation edit mode the pattern starts with no beads, so the color table shows 用色 0 种 and its swatch row has no items. When the user draws the first bead the swatch row gains an item and grows by the swatch's height, which shrinks the `weight(1f)` canvas above it and makes the drawing area jump upward.

## What Changes

- Give the color-swatch row a fixed height so the bottom panel height never changes when used colors are added or removed.
- Show a short hint in place of the swatches when no colors are in use (empty project).

## Capabilities

### Modified Capabilities
- `pattern-editing`: the color table keeps a constant height so the canvas does not resize as used colors change.

## Impact

- **App**: `ui/editor/EditorScreen.kt` (`ColorTablePanel` fixed swatch-row height + empty hint).
- No core, dependency, or manifest change.
