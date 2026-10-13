## Why

A fast one-finger drag in edit mode only painted the cell under each per-frame pointer sample, so a quick swipe left visible gaps (disconnected dots) — the implementation did not meet the existing "paint along the swept cells" requirement. A first attempt at connecting samples with screen-space point sampling made diagonal strokes two cells wide.

## What Changes

- Connect consecutive pointer samples by interpolating between them so a fast drag paints a continuous stroke.
- Interpolate with a 1-cell-wide Bresenham walk in grid space so a diagonal stroke stays one cell wide instead of thickening.

## Capabilities

### Modified Capabilities
- `pattern-editing`: the "Draw using the viewport transform" requirement now specifies that swept cells are connected into a single one-cell-wide line.

## Impact

- **App**: `ui/editor/PatternGrid.kt` (`paintTo` Bresenham interpolation in the grid gesture loop).
- No core, dependency, or manifest change.
