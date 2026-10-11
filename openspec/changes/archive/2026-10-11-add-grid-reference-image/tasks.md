## 1. Core model & codec

- [x] 1.1 Add `ReferenceGrid(width, height, argbHex)` `@Serializable` to `com.pindou.core.model` with hex encode/decode (`00000000` = transparent) and `toImage()`/`PixelImage.toReferenceGrid()` helpers (JVM-only, no Android/Base64 dependency; minSdk 24)
- [x] 1.2 Add optional `referenceGrid: ReferenceGrid? = null` to `PindouProject`; bump `CURRENT_VERSION` minor to `1.1.0`
- [x] 1.3 Configure `ProjectSerializer` JSON to not emit nulls (`explicitNulls = false`) so default saves stay small; keep `ignoreUnknownKeys = true`
- [x] 1.4 Unit tests: hex round-trip (`ReferenceGrid.toImage()` fidelity incl. transparent holes), null-field decode of a pre-1.1 payload, encode-omits-null

## 2. Core generation

- [x] 2.1 Add `AreaColor.cell(image, gridW, gridH, row, col): CellSample(coverage, rgb)` folding the region walk into one pass
- [x] 2.2 Rework `PatternGenerator.generate` to build the reference `PixelImage` from cell samples (alpha 0 when `coverage < MIN_CELL_VALID`); keep snapping on the same average
- [x] 2.3 Add optional `reference: PixelImage?` param to `generate`; reuse it (skip region pass) when dimensions equal `gridW × gridH`, else recompute; attach it to the returned project
- [x] 2.4 Unit tests: reference is `W × H`; linear-light midtone (~188) for a black/white cell; hole pixel alpha 0; reuse yields identical grid to a fresh compute

## 3. App wiring & caching

- [x] 3.1 `PindouController` keeps the current reference and passes a cached one into `generate`, keyed by `(workingImage identity, imageType id, gridW, gridH)`
- [x] 3.2 Invalidate the cache when the working image, image type, or grid size changes; verify a color-count-only change reuses it

## 4. Editor compare overlay

- [x] 4.1 Add two layer toggles ("图纸" / "缩略图") and a press-and-hold "按住看原图" control to the editor idle info bar; keep at least one layer on
- [x] 4.2 `PatternGrid` paints the reference as a base layer under each visible cell and the bead layer opaquely on top (holes/hatch, grid lines/guides/selection unchanged); hold-to-peek forces reference-only
- [x] 4.3 Verify alignment visually (cells line up exactly) and that hiding/peeking layers restores the pure pattern

## 5. .pindou persistence (opt-in)

- [x] 5.1 Embed `referenceGrid` by default at save/share; add an opt-out control ("不含参考图") that nulls it for smaller files
- [x] 5.2 Restore the reference when opening a `.pindou` containing it (VIEW intent path), enabling compare without the source image
- [x] 5.3 Verify: default save embeds the reference; opt-out save omits it; embedded save round-trips the reference across a save→open cycle

## 6. Docs sync & verification

- [x] 6.1 Sync `openspec/specs/grid-reference-image/spec.md` (new) and the `pattern-share` delta on archive
- [x] 6.2 Run `:core:test` and `:app:assembleDebug`; manually verify compare overlay and opt-in round-trip on device `642cc491`
