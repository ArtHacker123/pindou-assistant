## 1. Form 2 (interactive view)

- [x] 1.1 In `PatternGrid.kt`, add constants for the cell-fill factor (0.9× cell) and the legibility floor (≈8 px)
- [x] 1.2 Measure the code at the natural size (`cell * 0.45f`); if wider than the target, recompute the font size as `natural * target / measured`
- [x] 1.3 Skip drawing the code when the fitted size is below the floor; otherwise re-measure at the fitted size and draw centered
- [x] 1.4 Update the layout cache key to key on the cell size (not the natural font size) and cache omitted codes so they are not re-measured

## 2. Form 1 (exported image)

- [x] 2.1 In `Form1BitmapRenderer.kt`, add the same cell-fill factor and legibility floor
- [x] 2.2 After computing `codeTextSize`, use `codePaint.measureText(code)` to detect overflow and shrink `codePaint.textSize` to fit; skip the code when below the floor
- [x] 2.3 Keep the existing `cell >= 24` gate, ink/stroke selection, and centering

## 3. Verification

- [x] 3.1 `:core:test :app:assembleDebug` passes
- [x] 3.2 Install the APK and have the user verify: MARD stays unchanged; Perler codes no longer overlap at any zoom; at small sizes long codes are omitted rather than smudged; the exported Form 1 matches the on-screen behavior
- [x] 3.3 Re-validate the change `--strict`
