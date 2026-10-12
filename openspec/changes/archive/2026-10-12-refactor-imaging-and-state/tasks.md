## 1. Core imaging module

- [x] 1.1 Create `core/.../imaging` package; move `Sampler`/`UniformSampler` and `PixelImage` operation helpers (`ImageOps`) in, keeping public behavior unchanged
- [x] 1.2 Add `fun interface ImageOp { fun apply(PixelImage): PixelImage }` and a `ProcessorPipeline(ops)`; build the `cartoon`/`photo` sequences from the existing `ImageOps` primitives in the exact current order
- [x] 1.3 Replace per-preset `ImageProcessor` classes with pipeline definitions keyed by `ImageType` id (registry returns a pipeline); remove the old whole-image processor classes
- [x] 1.4 Add `GridSampler` + `GridSamples` (per-cell coverage, linear-light avg, validity; `MIN_CELL_VALID` hole rule) reproducing `AreaColor.cell` semantics in one pass
- [x] 1.5 Add `ReferenceBuilder.build(image, gridW, gridH): PixelImage` (grid-resolution reference; alpha 0 for holes) implemented on top of `GridSampler`

## 2. PatternGenerator rework

- [x] 2.1 Rework `PatternGenerator.generate` to run `sample → quantize → mapCenters → GridSampler → snap` on an already-preprocessed image (preprocessing owned by the shared `ReferenceService`), keeping `GenerateConfig` and the returned `PindouProject` identical (incl. `referenceGrid`)
- [x] 2.2 Preserve the reference-reuse shortcut: a provided matching reference/grid-sample skips the grid walk and yields identical results
- [x] 2.3 Remove the now-duplicated per-cell loop and unused `AreaColor` entry points (keep or relocate only what is still used)

## 3. Core package boundaries

- [x] 3.1 Enforce one-way deps (`imaging → quant/match → generate → serialize`; `model`/`color`/`util` as leaves): no `imaging → generate/quant/match` references
- [x] 3.2 Add a lightweight test that fails if a forbidden package dependency is introduced (e.g. source scan or an import check)

## 4. Tests

- [x] 4.1 Pipeline parity: pin `cartoon` and `photo` output for a fixed input image and assert it matches the prior processor output
- [x] 4.2 Reference equality: `ReferenceBuilder.build(image,w,h)` equals the `referenceGrid.toImage()` attached by `PatternGenerator.generate(image, cfg)` for the same inputs (incl. transparent holes)
- [x] 4.3 Ensure all pre-existing `:core` tests remain green with unchanged intent (adapt only imports/renames)

## 5. App state decomposition

- [x] 5.1 Extract `NavigationState` (current `Screen`) and `DocumentStore` (current project + open/save) from `PindouController`
- [x] 5.2 Extract `GenerationSession` (generation params + working image + debounced `runGenerate` + reference reuse cache) and `ReferenceService` (image → reference)
- [x] 5.3 Extract `ExportService` (PNG/.pindou render, save, share, storage permission checks)
- [x] 5.4 Keep `PindouController` as a thin facade exposing the same observed properties so `Screen` composables are unchanged

## 6. Verification

- [x] 6.1 Run `:core:test` and `:app:assembleDebug` (with `JAVA_HOME` set to the Android Studio JBR)
- [x] 6.2 Manual smoke on device `642cc491`: pick→crop→generate; change image type / palette / grid size / color count; reference layers (图纸/缩略图/看原图); export PNG; save & reopen `.pindou` (reference embedded)
- [x] 6.3 Confirm outputs are unchanged (pattern, reference, `.pindou` payload) before starting the editing/creation feature change
