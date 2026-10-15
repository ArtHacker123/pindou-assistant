## 1. Palette data

- [x] 1.1 Vendor the snapshot: map `beadcolors` raw CSVs (`perler.csv`, `hama.csv`, `artkal_s.csv`) to `PaletteItem(code, name, r, g, b, id, brand)`, natural-sort by code, assign `id = 1..N`
- [x] 1.2 Write `app/src/main/assets/palettes/perler.json` (name `perler`, displayName `Perler 103 色`, brand `Perler`, 103 items)
- [x] 1.3 Write `app/src/main/assets/palettes/hama_midi.json` (name `hama_midi`, displayName `Hama Midi 92 色`, brand `Hama`, 92 items)
- [x] 1.4 Write `app/src/main/assets/palettes/artkal_s.json` (name `artkal_s`, displayName `Artkal S 199 色`, brand `Artkal`, 199 items)

## 2. Default palette

- [x] 2.1 Add `core` constant `PaletteDefaults.DEFAULT_NAME = "mard_221"`
- [x] 2.2 In `GenerationSession`, initialize `paletteName` to the meta whose name is `PaletteDefaults.DEFAULT_NAME` (fallback to first meta)
- [x] 2.3 In `PindouController.startTemplateCreation`, resolve the default palette via `PaletteDefaults.DEFAULT_NAME` (fallback to first meta)

## 3. Tests and docs

- [x] 3.1 Replace `Mard221PaletteFileTest` with a `BundledPaletteFileTest` that validates every `assets/palettes/*.json` (name == file base, non-empty, unique codes, RGB 0..255, natural-order sorted)
- [x] 3.2 Add explicit count assertions: `mard_221`=221, `perler`=103, `hama_midi`=92, `artkal_s`=199
- [x] 3.3 Add `docs/palette-sources.md` with source repo, MIT license, raw URLs, snapshot date, and the CSV→JSON field mapping

## 4. Verification

- [x] 4.1 `:core:test :app:assembleDebug` passes
- [x] 4.2 Install the APK and have the user verify the palette dropdown lists all four palettes and that MARD is still the default
- [x] 4.3 Update `docs/TODO.md` (check E1) and re-validate the change `--strict`
