## Context

`AssetPaletteProvider.available()` scans `assets/palettes/*.json` and builds a `PaletteMeta` per file, so the pipeline already supports any number of bundled palettes with no code change. Today only `mard_221.json` exists. The `Palette`/`PaletteItem` model already carries `code`, `name`, and `brand`, and `PaletteItem.id` is informational only. Existing `.pindou` documents embed their own `paletteSource`, so they never depend on which assets ship.

Two places resolve a "default" palette by taking the first meta in asset-sorted order:
`GenerationSession.paletteName` initial value and `PindouController.startTemplateCreation`. Asset names sort as `artkal_s < hama_midi < mard_221 < perler`, so once we add palettes the implicit default would silently drift away from MARD.

Color data comes from the open-source `maxcleme/beadcolors` dataset (MIT license), a community-maintained set of raw CSVs in the form `code,name,r,g,b,contributor`.

## Goals / Non-Goals

**Goals:**
- Bundle Perler (103), Hama Midi (92), and Artkal S-5MM (199) with their official codes and names.
- Keep MARD 221 as the default/startup/template palette regardless of asset ordering.
- Validate every bundled palette file with tests.

**Non-Goals:**
- Per-palette color-accuracy claims (community RGB is a screen reference, not a dye-lot match).
- Mini beads (2.6 mm) or non-bead sets (Diamond Dotz) — out of scope for v1.
- A user-facing palette importer or remote palette download.
- Replacing/updating the existing MARD 221 data (its values stay as-is).

## Decisions

### D1: Vendor a one-off snapshot from `maxcleme/beadcolors` (MIT)
Download the three raw CSVs, map `[code,name,r,g,b,contributor]` → `PaletteItem(code, name, r, g, b, id, brand)`, and commit the resulting JSON. No CSV files or generation scripts are kept in the repo; provenance (source URL, license, snapshot date) is recorded in `docs/palette-sources.md`.
- *Alternatives*: parse CSVs at build time (adds a build step and network dependency for reproducibility); scrape vendor sites (no clean license).

### D2: File / `name` / `displayName` conventions
| file | `name` | `displayName` | `brand` | codes |
|---|---|---|---|---|
| `perler.json` | `perler` | `Perler 103 色` | `Perler` | `80-xxxxx` |
| `hama_midi.json` | `hama_midi` | `Hama Midi 92 色` | `Hama` | `H0x` |
| `artkal_s.json` | `artkal_s` | `Artkal S 199 色` | `Artkal` | `S0x`, `SE…`, `ST…` |
Official codes are preserved (a Perler code such as `80-19001` is long as a cell label but matches the physical bead). The dropdown already renders `displayName（colorCount 色）`; `displayName` omits the count to avoid duplication.

### D3: Item ordering and `id`
Items are sorted by `NaturalOrder` of `code` (consistent with the MARD file and `build paletteUsed`), and `id` is assigned `1..N` in that order. This keeps the existing MARD test invariant (ids `1..221`) and makes new files uniform.

### D4: Pin the default palette to MARD 221
Add `core` constant `PaletteDefaults.DEFAULT_NAME = "mard_221"` and a `defaultMeta(metas)` helper that resolves `metas.firstOrNull { it.name == DEFAULT_NAME } ?: metas.firstOrNull()`. Use it everywhere a default is chosen: `GenerationSession.paletteName`, `PindouController.startTemplateCreation`, the Home screen's free-creation dialog, and `PaletteRegistry.default()`.
- *Alternatives*: rename assets to control sort order (fragile); make the default "whichever is first" (changes silently as palettes are added).

### D5: Generic bundled-palette file test
Replace the MARD-specific `Mard221PaletteFileTest` with a `BundledPaletteFileTest` that iterates every `app/src/main/assets/palettes/*.json` and asserts: `name` equals the file base name, non-empty, unique codes, `r/g/b` in `0..255`, and codes natural-order sorted. Add explicit count assertions (`mard_221`=221, `perler`=103, `hama_midi`=92, `artkal_s`=199).

### D6: Provenance note
Add `docs/palette-sources.md` recording the source repo, license (MIT), raw URLs, snapshot date, and the field mapping.

## Risks / Trade-offs

- **Community RGB ≠ physical beads** → documented in `docs/palette-sources.md`; HEX/RGB is a screen reference (same caveat as the existing MARD file).
- **Default drift when assets are added** → mitigated by D4's pinned default.
- **Long Perler codes in the color table** → acceptable; the label is `maxLines = 1`. Revisit only if it hurts readability.
- **Upstream data corrections** → snapshot is vendored, so we control when to refresh; the provenance note records the snapshot for future diffs.
- **`NaturalOrder` behavior on mixed sufixed codes (`S01` vs `SE1`)** → deterministic; main `S`-series sorts before special series, which is a reasonable grouping.
