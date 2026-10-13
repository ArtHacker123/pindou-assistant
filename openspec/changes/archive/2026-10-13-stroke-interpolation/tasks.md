## Connect drawn strokes without thickening them

- [x] 1.1 Interpolate between consecutive pointer samples so a fast drag is continuous.
- [x] 1.2 Use a 1-cell-wide Bresenham walk in grid space so diagonals stay one cell wide.

## Verification

- [x] 2.1 `:core:test :app:assembleDebug` passes; app installs.
- [x] 2.2 A fast swipe draws a connected line and a 45° line is one cell wide.
