## Context

Cell code labels are rendered in two places, both of which draw text centered at a fixed `≈0.45 × cell` size with no width constraint:

- Form 2 (interactive): `app/.../ui/editor/PatternGrid.kt` — `textMeasurer.measure(...)` at `textPx = cell * 0.45f`, only when `cell >= 24f`.
- Form 1 (export): `app/.../render/Form1BitmapRenderer.kt` — `codeTextSize = (cell * 0.45f).coerceAtLeast(9f)`, only when `cell >= 24`.

For MARD (`A1`, `B2`, 2–3 chars) the label always fits, so the defect was latent. Perler codes (`80-19001`, 8 chars) are wider than the cell and bleed into neighbours in both renderers.

## Goals / Non-Goals

**Goals:**
- A cell's code label never exceeds its cell in either renderer.
- Preserve current look for codes that already fit (short MARD codes unchanged).
- Keep both renderers consistent (same fit rule and same legibility floor).

**Non-Goals:**
- Changing palette codes or shortening brand codes.
- Introducing per-pattern index numbers in cells.
- Changing the `cell >= 24 px` gate, ink/stroke colors, or axis/table typography.

## Decisions

### D1: Shrink the font to fit the cell width
Start from the current natural size (`0.45 × cell`, min as today) and, when the measured label is wider than a target (`cell × 0.9`, giving ~5% inset each side), scale the font down by `targetWidth / measuredWidth`. Height is not binding (0.45 × cell text height is well under a cell), so only width is constrained.
- *Alternatives*: raise the `cell >= 24` threshold per-code-complexity (keeps overflow at high zoom); truncate codes (ambiguous); use index numbers (larger change, affects MARD too).

### D2: Omit the code below a legibility floor
If the fitted font is below ≈8 px, do not draw the in-cell code — the cell keeps its bead color only. This is the same philosophy as the existing "too small → no code" rule, and avoids a sub-pixel smudge that still technically fits. The color table remains the source of truth for codes.

### D3: Measure-then-fit per renderer, keep caches keyed on the fitted size
- Form 2: measure once at the natural size, compute the fitted size, then measure again at the fitted size; cache layouts keyed by `code + cell size` (the old key used the natural font size, which no longer uniquely determines the layout), with a companion set of keys that were omitted for falling below the floor so they are not re-measured every frame.
- Form 1: `codePaint.measureText(code)` gives the natural width; compute `textSize * target / width` and set `codePaint.textSize` before `drawText`. Font-size mutation is already how `codePaint` is used here.

### D4: One rule, two call sites, no shared abstraction
The two renderers use different text APIs (Compose `TextMeasurer` vs `android.graphics.Paint`), so the tiny fit formula is inlined at each site with a named constant for the inset and floor, rather than introducing a cross-cutting abstraction for ~5 lines.

## Risks / Trade-offs

- [Perler codes become small at low zoom] → unavoidable and acceptable: the code is unreadable at that size anyway; below the floor it is omitted instead of overlapping, and zooming in restores a larger fitted size.
- [Two renderers drift] → both use the same constants (target 0.9 × cell, floor 8 px); a spec scenario requires the same behavior in both forms.
- [Double text measurement in Form 2] → only paid when a code actually overflows; short codes keep the single-measure fast path. Layout caching further amortizes it while panning/zooming.
