## Why

Adding non-MARD palettes exposed a rendering defect: cell code labels are drawn at a fixed `0.45 × cell` font with no width constraint, so long codes overflow their cell and overlap neighbours. Perler codes (`80-xxxxx`, 8 chars) reliably overlap in both the interactive view (Form 2) and the exported image (Form 1), while the short MARD codes never did — so the bug was latent until multi-brand palettes shipped.

## What Changes

- Constrain the per-cell code label to its cell: start from the current `≈0.45 × cell` size, then shrink the font so the measured label fits within the cell width (with a small inset), in both Form 2 (interactive) and Form 1 (export).
- Stop drawing the in-cell code when the fitted font would fall below a legibility floor (≈8 px) — the cell keeps its bead color but shows no unreadable smudge. This preserves the spirit of the existing "too small → no code text" rule.
- Keep the existing behavior otherwise: the `cell >= 24 px` gate, the 0.45 × cell proportional sizing for axis numbers and color-table text, the light/dark ink + stroke, and centering.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `pattern-render`: the Form 1 requirement ("Form 1 (exported image) layout with axes, cell codes, color table, and group guides") and the Form 2 cell-code rendering behavior are refined so a cell's code label never exceeds its cell: the code font is shrunk to fit, and the code is omitted when it cannot stay legible.

## Impact

- `app/.../ui/editor/PatternGrid.kt` — Form 2 cell-code measurement/draw (fit-to-cell, floor).
- `app/.../render/Form1BitmapRenderer.kt` — Form 1 `codeTextSize` / `drawGrid` (fit-to-cell, floor).
- `openspec/specs/pattern-render/spec.md` — refined requirement + scenarios.
- No data, model, pipeline, persistence, or palette changes; short-code palettes (e.g. MARD) render unchanged.
