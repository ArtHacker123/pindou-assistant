## Why

The app bundles only one bead palette (MARD 标准 221 色). Real workspace users own beads from other major brands — Perler, Hama, Artkal — whose color codes and RGB values differ, so a pattern generated against MARD cannot be followed with another brand's beads. This is tracked as TODO E1 (多品牌 / 多套色板可选). Adding bundled brand palettes makes generated patterns directly followable with the beads the user actually owns.

## What Changes

- Bundle three more midi (5 mm) bead palettes as assets alongside `mard_221.json`:
  - `perler.json` — Perler 103 色 (US, codes `80-xxxxx`)
  - `hama_midi.json` — Hama Midi 92 色 (Denmark, codes `H0x`)
  - `artkal_s.json` — Artkal S-5MM 199 色 (深圳, codes `S0x`)
- Vendor the color data as a snapshot from the open-source `maxcleme/beadcolors` dataset (MIT license; raw CSV `code,name,r,g,b,contributor`), preserving each brand's official color codes and names.
- Pin the default palette to MARD 221 so adding earlier-sorting asset names does not silently change the startup / template-mapping palette.
- Extend the bundled-palette file tests to validate every palette asset generically (name matches file, non-empty, unique codes, RGB range, natural code order).

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `bead-palette`: the bundled-palette set grows from one to four; a new requirement pins the default/selected palette to MARD 221 independent of asset ordering; per-brand palette requirements (codes/names/counts) are specified.

## Impact

- `app/src/main/assets/palettes/` — three new JSON files (+ updated count test).
- `app/.../state/GenerationSession.kt` — default `paletteName` resolves MARD 221, not the first sorted asset.
- `app/.../PindouController.kt` — `startTemplateCreation` default palette resolves MARD 221.
- `core/src/test/.../palette/` — generic bundled-palette validation test.
- Data provenance note under `docs/`.
- No pipeline, persistence, or format changes: `PaletteProvider` already auto-discovers assets, and `PaletteItem` already carries `code/name/brand`.
- Existing `.pindou` documents embed their own `paletteSource`, so they are unaffected.
