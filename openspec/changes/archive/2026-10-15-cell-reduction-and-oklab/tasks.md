## 1. Oklab color space

- [x] 1.1 Add `core/.../color/Oklab.kt` implementing `OklabColorSpace : ColorSpace` (encode/decode via linear-sRGB → LMS(cbrt) → Oklab; Euclidean distance).
- [x] 1.2 Add unit tests for Oklab round-trip (encode/decode) and that it separates saturated colors at least as well as CIE76 on a fixed pair.
- [x] 1.3 Switch default `space` to `OklabColorSpace` in `PatternGenerator`, `ReferenceDirectStrategy`, `PaletteMapper`, `KMeansStrategy`, `KMeansQuantizer`, and `GridEditor.pick`; keep `LabColorSpace` exported.

## 2. Cell reduction plumbing

- [x] 2.1 Add `CellReduction { AVERAGE, MODE }` and a `repRgb: Array<Rgb>? = null` field to `GridSamples` with a `rep(i)` accessor falling back to `avgRgb[i]`.
- [x] 2.2 Add a palette-index lookup (32³ LUT: 5 bits/channel → nearest palette index in Oklab), built once per palette.
- [x] 2.3 Extend `AreaColor`/`GridSampler.sample` with `reduction` + `palette`: for `MODE`, accumulate a per-cell histogram of nearest palette indices and set `repRgb` to the winning bead color (ties → smallest natural code); still compute `avgRgb`, `coverage`, `valid` in the same pass.
- [x] 2.4 Keep `ReferenceBuilder.build(image, gw, gh)` calling the sampler with `AVERAGE` (palette-free), so the reference stays the linear-light average.
- [x] 2.5 Support `GridSampler.fromReference` by setting `repRgb = null` (falls back to average).

## 3. Generation wiring

- [x] 3.1 In `PatternGenerator.generate`, choose reduction from `config.imageType` (cartoon → MODE, photo → AVERAGE); for MODE ignore the passed `reference` and sample the preprocessed image fresh with the palette; for AVERAGE keep the `fromReference` reuse path.
- [x] 3.2 Snap each cell from `samples.rep(i)`; build `referenceGrid` from `samples.avgRgb` (unchanged).
- [x] 3.3 In `GenerationSession.runGenerate`, stop passing the cached reference for cartoon runs (or let `PatternGenerator` ignore it) while keeping it for photo; preserve the timing log.

## 4. Color selection

- [x] 4.1 Update `ReferenceDirectStrategy.select` to group/snap cells by `samples.rep(i)` instead of `samples.avgRgb`.

## 5. Tests

- [x] 5.1 Extend `PatternGeneratorTest`: cartoon boundary cell (half black / half white) reduces to a real bead (mode), not an off-palette gray; the attached reference still averages (~188).
- [x] 5.2 Add a `GridSampler`/`AreaColor` MODE test asserting the representative equals the majority nearest bead and `avgRgb` is unchanged.
- [x] 5.3 Update any tests that pin CIE76/`LabColorSpace` defaults or `GridSamples` construction; keep `ColorSpaceTest` for `LabColorSpace`.
- [x] 5.4 Run `:core:test` and ensure all pass.

## 6. Specs & build

- [x] 6.1 `npx openspec validate cell-reduction-and-oklab --strict` passes.
- [ ] 6.2 Build `:core:test :app:assembleDebug` and install for user device testing.
