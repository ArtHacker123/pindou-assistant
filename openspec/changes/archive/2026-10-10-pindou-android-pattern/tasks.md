## 1. Project Setup

- [x] 1.1 Create new Android project (Kotlin + Jetpack Compose) under G:\OpenCode\拼豆助手 with package structure (core, data, ui, utils)
- [x] 1.2 Add dependencies: kotlinx-serialization, Coil, Activity Result Contracts, FileProvider config
- [x] 1.3 Configure AndroidManifest with FileProvider and custom MIME/VIEW intent placeholders for `.pindou`

## 2. Data Model & Palette System

- [x] 2.1 Define `PindouProject` data classes (meta, beadPaletteSource, paletteUsed, grid) with `@Serializable` and `version = "1.0.0"`
- [x] 2.2 Define `PaletteItem` (id, code, name, brand, r, g, b) and `Palette` models
- [x] 2.3 Create bundled palette asset `assets/palettes/<name>.json` (>=221 colors, stable codes like C1..C221)
- [x] 2.4 Implement `PaletteProvider` interface + asset-backed implementation and `PaletteRegistry` for multiple palettes
- [x] 2.5 Implement natural-sort ordering utility for palette codes (C1, C2, ..., C10)

## 3. Image Input (Import + Crop/Scale)

- [x] 3.1 Implement gallery/camera import via Activity Result Contracts with decode-failure handling
- [x] 3.2 Implement crop/scale screen: image pan (one-finger drag), pinch-zoom (two-finger), fit-to-view initial state
- [x] 3.3 Implement draggable crop frame (edges + corners) with image-bounds constraint and minimum-size enforcement
- [x] 3.4 Implement semi-transparent mask outside crop frame and top bar (cancel/reset/confirm)
- [x] 3.5 On confirm, inverse-transform screen crop rect to original bitmap pixel coords (clamped) and produce working bitmap; implement reset to full image

## 4. Core Processing Pipeline

- [x] 4.1 Define `Quantizer` interface (`quantize(samples, k, config)`), `QuantConfig` (maxIter, seed), and `QuantizerRegistry`
- [x] 4.2 Implement uniform pixel `Sampler` producing bounded sample list from working bitmap
- [x] 4.3 Implement `KMeansQuantizer` (init, assignment, update, convergence/iter cap, optional deterministic seed)
- [x] 4.4 Implement image-type preset system: `ImageProcessor` interface + `CartoonProcessor` and `PhotoProcessor` + processor registry
- [x] 4.5 Implement `ColorDistance` (RGB squared Euclidean) + nearest-color matcher
- [x] 4.6 Map quantCenters to selected bead palette (nearest bead) to build `beadMap` and distinct `paletteUsed`
- [x] 4.7 Generate W×H grid (<=128×128): per-cell area-average RGB of working image -> nearest quantCenter -> bead colorCode; build `PindouProject`
- [x] 4.8 Wire parameter screen (image type preset, bead palette picker, colorCountTarget N slider, grid W×H input) into generation flow

## 5. Form 2 Interactive View

- [x] 5.1 Implement Compose Canvas grid renderer with viewport culling (compute visible cell range from inverse transform)
- [x] 5.2 Implement pan and pinch-zoom gestures (centroid-based scale, min/max scale limits)
- [x] 5.3 Implement touch -> grid coordinate mapping (inverse transform) and same-color highlight of all matching colorCode cells
- [x] 5.4 Implement bottom info bar: 1-based row/column of tapped cell, bead code, highlight count; clear highlight on outside tap
- [x] 5.5 Implement collapsible, horizontally scrollable color table (swatch + code); tapping an entry highlights that color group
- [x] 5.6 Cell code text rendering with luminance-based ink/stroke choice for legibility

## 6. Form 1 Export Renderer

- [x] 6.1 Implement offscreen layout calculator (row-number gutter width, column-number header height, grid size at cellOutPx, color-table size, total bitmap dimensions with bounds check)
- [x] 6.2 Render axes: 1-based column numbers on top, 1-based row numbers on left, blank top-left corner
- [x] 6.3 Render grid body with cell fills, grid lines, and per-cell colorCode labels using luminance-based ink/stroke
- [x] 6.4 Render color lookup table (code + swatch + optional name) from `paletteUsed` (natural code order), placed right/bottom
- [x] 6.5 Export rendered bitmap as PNG to app storage with filename including grid size/date; notify user of location
- [x] 6.6 Add export settings UI (cell size choice, e.g. 60/80 px per cell) with pre-export preview

## 7. Share & Cross-Device Open

- [x] 7.1 Implement `.pindou` JSON serialization/deserialization of `PindouProject` (version check + corrupt-file error handling)
- [x] 7.2 Save project file to cache/share directory and share PNG via FileProvider content URI (share sheet)
- [x] 7.3 Share `.pindou` file via FileProvider with custom MIME (`application/x-pindou`)
- [x] 7.4 Configure VIEW intent-filter for `.pindou`; on open, parse file, reconstruct grid + palette, navigate directly to Form 2
- [x] 7.5 Verify cross-device flow: receive `.pindou` -> open with app -> interactive highlight/coordinates work without original image

## 8. Polish & Robustness

- [x] 8.1 Enforce grid-size limit (128×128) and export bitmap size guard (warn/clamp on large cellOutPx to avoid OOM)
- [x] 8.2 Default deterministic seed derived from source hash for reproducible generation
- [x] 8.3 Error/loading states: import failure, generation progress, file open failure messages
- [x] 8.4 Verify pixel-data path uses original cropped bitmap (not preview-scaled) for sampling and grid generation
