## Context

This change creates a new Android app from scratch (Kotlin + Jetpack Compose) under `G:\OpenCode\拼豆助手` (not a git repo). The goal is a data-first bead pattern tool that produces both a printable labeled image (Form 1) and an interactive, shareable pattern (Form 2). Cross-device share requires a self-contained `.pindou` file containing grid data, palette metadata, and generation context.

## Goals / Non-Goals

**Goals:**
- Decouple image quantization (N representative image colors) from bead palette selection (real market bead colors). Quantize first, then map each representative color to nearest bead palette color.
- Support multiple bundled bead palettes, pluggable quantizers/processors/renderers/exporters for extensibility.
- Provide crop+scale image editing with draggable borders, pan/zoom before generation.
- Implement Form 1 (high-DPI export with axes, cell codes, color table) and Form 2 (interactive canvas with same-color highlight, coordinates, collapsible color table).
- Share/export PNG and `.pindou`; open `.pindou` files via VIEW intent to render Form 2.
- Support grid ≤ 128×128 with viewport culling + pan/zoom.

**Non-Goals:**
- Real-time cloud sync, user accounts, or backend services.
- Advanced vector export (SVG/PDF) in MVP (architecture should allow later).
- In-app bead catalog shopping or price lookup.
- Full automatic type detection beyond presets (architecture allows later).

## Decisions

### D1: Data-first model (store colorCode in grid)
- **Choice**: Store grid as `Array[H][W] = colorCode` (e.g., `"C1"`), not indices. Keep `beadPaletteSource` (current palette full list) and `paletteUsed` (distinct used bead items).
- **Rationale**: Stable identity across devices; enables cross-device reconstruction and future "replace all color X with Y"; share payload small and human-inspectable.
- **Alternatives**: Store palette indices – brittle if palette ordering changes or different palette selected on receiver.

### D2: Quantization decoupled from palette
- **Choice**: First extract N `quantCenters` (image-space RGB) via pluggable Quantizer from cropped working image (with uniform sampling). Then map each quantCenter to nearest bead in `selectedPalette` to build `beadMap` and `paletteUsed`. Grid cells map cellAvgRGB → nearest `quantCenter` → corresponding bead `colorCode` (Option A per discussion).
- **Rationale**: Matches "generate N-color pattern" (image simplification degree independent of bead catalog). Switching palettes remaps the same color structure to different real beads.
- **Alternatives**: Filter palette subset first – couples degree of simplification to palette membership.

### D3: Pluggable architecture via strategy interfaces
- **Choice**: Interfaces: `Quantizer`, `ImageProcessor`, `ColorDistance`, `ColorMatcher`, `Renderer` (or split Form1/Form2), `Exporter`, `PaletteProvider`, `Sampler` (optional). Registry/factory per family.
- **Rationale**: Extensible for new algorithms/formats without changing pipeline.
- **Alternatives**: Monolithic processors – harder to extend/test.

### D4: Crop+Scale screen before parameter setup
- **Choice**: Import → Crop/Scale editor (draggable borders, corners, pan/zoom, overlay mask) → confirm → workingBitmap → Parameters (image type, bead palette, colorCountTarget N, grid W×H, quantizer) → Generate → Form 2.
- **Rationale**: Reduces background noise in quantization; lets user select exact pattern area. Transform screen coords → original bitmap coords on confirm.
- **Alternatives**: Crop after generation – less efficient and can’t isolate subject early.

### D5: Rendering and export
- **Choice**: Form 2 uses Compose `Canvas` with viewport culling (compute visible grid range via inverse transform). Form 1 uses offscreen `Bitmap` with layout: left row numbers, top column numbers, grid body, right color table. Export PNG at configurable cellOutPx (e.g., 60–80). Share `.pindou` as JSON via FileProvider with VIEW intent + custom MIME.
- **Rationale**: 128×128 interactive performant with culling; printable clarity via high-DPI offscreen.
- **Alternatives**: Render full grid always for large grids – higher redraw cost at extreme zoom.

### D6: Persistence and sharing
- **Choice**: `PindouProject` serialized with kotlinx-serialization (JSON). Include `version="1.0.0"`, meta (grid, paletteName, colorCountTarget, imageType), `beadPaletteSource` (current full palette), `paletteUsed`, `grid` (2D of colorCode). No original image bytes embedded. Receiver opens `.pindou` and renders Form 2 directly.
- **Rationale**: Self-contained, robust for cross-device; small payload.
- **Alternatives**: Embed metadata in PNG text chunk only – harder to guarantee preservation across social platforms; limited for interactive state.

### D7: Color readability
- **Choice**: For cell code labels (Form 1 and Form 2), compute relative luminance L = 0.299*r+0.587*g+0.114*b; use black text + white stroke or white text + black stroke depending on L threshold (e.g., L>=140). Draw grid lines clearly (Form 1) and subtly (Form 2).
- **Rationale**: Improves legibility over varied bead colors.
- **Alternatives**: Fixed black/white – poor contrast on very light/dark beads.

### D8: Coordinate convention and labeling
- **Choice**: Origin top-left. Axes use 1-based numbers (row 1..H, column 1..W). Cell labels are bead `colorCode` (e.g., `C12`) from selected palette.
- **Rationale**: Matches stated requirements and common assembly conventions.
- **Alternatives**: 0-based – less intuitive for users.

### D9: Crop/Scale editor interaction model (as implemented)
- **Choice** (fixed on-screen crop window): The crop `window` lives in **screen pixel coordinates** and stays put; the image is transformed underneath it by `scale` + `offset`. Window/image coordinates are decoupled; the image-coordinate crop rect is derived only at confirm via the inverse transform.
  - *Because the window is screen-space, zooming never enlarges the crop frame* (a prior issue where the frame scaled with the image).
- **Scale floor breaks at full-fit**: `scale` is clamped to `[fitScale, fitScale * 12]`. `fitScale` = `min(cw/iw, ch/ih) * 0.92` (centered). Zooming out toward the floor is allowed, but once `newScale <= fitScale` the view snaps to the canonical full-fit state (`scale = fitScale`, `offset = fitOffset`) — the image can never display smaller than full.
- **Unified edge constraint (`clampImage`)** — one rule for all zoom levels, no full-view special case: for each axis, if the scaled image is **larger** than the viewport, clamp `offset` to `[container - imgSize, 0]` so the image edge stops exactly at the screen edge; if it is **smaller**, lock that axis to its centered position. At full-fit the image is smaller than (or equal to) the viewport on at least one axis, so it is naturally pinned centered → the full-view state is "locked" (no pan, no shrink) with zero extra branching.
- **Window always on-screen and inside the picture (`clampedWindow`)**: the window is constrained to the **intersection of the on-screen image rect and the viewport** (image ∩ visible screen). Two consequences:
  1. The crop frame can never be dragged into the off-screen part of a zoomed-in image (fix for "frame slides outside the 画面" when the image extends past the viewport).
  2. The window size is capped by that region and never exceeds the visible picture.
  - Float-robustness: `coerceIn(min, max)` throws on an empty range when float drift makes `a - (a - b) < b`; all clamp calls guard the upper bound with `maxOf(region.left, region.right - w)` (and the window edge orders with 1 px guards) so the range is never inverted.
- **Gesture model** (`awaitEachGesture` on the canvas): single finger **inside** the window → `MOVE_CROP` (translate the window, then clamp); single finger **outside** → `PAN` (translate the image, then `clampImage` + `clampedWindow`); two+ fingers → `PINCH` (zoom anchored at the finger centroid, delta-driven: `newScale = scale * (curDist / prevDist)`, then `clampImage` + `clampedWindow`). Deltas come from `change.position - change.previousPosition` (guards against the removed `positionChange()` ambiguity of this Compose version).
- **Corner handles** (`CornerHandle`): white L-shaped bracket, 44 dp touch target, vertex anchored at the window corner. Each handle consumes its pointer-down (`awaitFirstDown().consume()`) and drives its own per-event drag loop, so the parent canvas gesture never double-processes the same finger (fix for the handle "following behind / offset" bug). Resize clamps opposing edges with 1 px minimums before the shared `clampedWindow`.
- **Confirm path**: `left/top = (window.min - offset) / scale`, `right/bottom = (window.max - offset) / scale`, each coerced into `[0, iw|ih]`, converted to ints with `coerceIn(0, bitmap.width-1)` / `coerceIn(1, bitmap.width)` (width/height ≥ 1), then `Bitmap.createBitmap` from the original — always in-bounds.
- **Buttons**: only **重置** (reset to full-fit + full-window) and **确认** remain; the previous duplicate **全图** button was removed.

### D10: Pattern preview (Form 2) interaction model (as implemented)
- **Choice — one unified gesture loop on the canvas**: a single `awaitEachGesture` block distinguishes tap / pan / pinch itself. A single finger that stays within `panSlop (= touchSlop * 1.6)` until release → **tap** → `onCellTap(row, col)` via inverse transform; movement past the slop → **pan/zoom** (delta-driven `panChange` / `zoomChange` computed from `position - previousPosition`), consuming the change. `detectTapGestures` + `detectTransformGestures` are not used: they cannot share one loop, and the transform detector's default `touchSlop` swallowed real-finger taps whose finger wobbled more than the slop (the pattern is centred-locked at fit, so the resulting "pan" showed no movement → looked like a dead tap).
- **Refit vs. clamp**: `refit()` runs only on first layout or when the grid dimensions change; a container-only resize just re-clamps (`clampOffset`) so the user's zoom/pan is preserved. The pattern `Box` uses `clipToBounds()` so a zoomed pattern can never bleed into the parameter/color panels.
- **Case — root cause of "第二次生成后点击图纸格子无响应" (archived for reference)**: selection state lives in the parent as `remember(project)` so it resets when the pattern is replaced. The canvas held its gesture loop under `pointerInput(grid.width, grid.height)`. When the user regenerated at the **same** grid size (e.g. 52×52), those keys did not change → the gesture coroutine was **not restarted** → it kept the callback captured from the *previous* composition, so `onCellTap` wrote into the **discarded** `remember(project)` state object. Outcome: the gesture fired and the cell was computed correctly, but the live selection state never changed → "no response". Injected `adb` taps never triggered a regeneration, so automated tests passed while real use failed from the second generation onward.
- **Fix**: key the gesture block on the pattern identity — `.pointerInput(project)`. On regeneration the coroutine restarts and re-captures the current `onCellTap`, so writes land on the current selection state.
- **Lesson**: when a `pointerInput` (or any remembered effect) captures a lambda/state that can be rebuilt while its own keys stay constant, key it on the captured state's *identity* (here `project`), never on a derived value (grid dimensions) that may stay unchanged across a rebuild.

### D11: Form 2 selection spotlight & deselection (as implemented)
- **Spotlight style — final**: cells of the selected `colorCode` keep full color and get an **inset amber outline** (`SELECT_ACCENT`, stroke `(cell*0.04).coerceIn(1.5f, 3f)`, inset `cell*0.08` so it never collides with the fine grid lines or the thicker group lines); all other cells are dimmed by a **black veil** (`SPOTLIGHT_DIM = Color(0x8C000000)`, ≈55%) that preserves hue (not gray).
- **Iteration history** (why the final form): (1) desaturating every non-selected cell made the whole canvas gray and near-invisible for white beads → rejected; (2) a translucent gray mask (`0x70808080`) read as "dirty gray screen", no pop → rejected; (3) a frosted white veil (70%) muted colors into pastel and looked good, then was thinned to 50% — but with a **gray selected code** the veiled non-selected cells fell into the same tone range, so membership was ambiguous; also overall contrast stayed weak → replaced.
- **Gray-parity fix**: because the veil relies on tone, it cannot disambiguate a gray selection, so a **hue-independent hard marker** (the accent outline) is always drawn on selected-code cells. Membership is decidable from the border, not from tone.
- **Deselection**: (a) tapping outside the grid clears (works at fit zoom where margins exist); (b) **tapping any currently highlighted cell (same code) also clears** — this keeps deselection possible when zoomed in and the grid fills the entire viewport. The hint is folded into the idle info-bar text ("点击格子查看色号与坐标，点高亮格可取消选择") instead of permanent chrome.
- **90° zoom-size note**: max zoom is a fixed cell size cap (`MAX_CELL_PX = 128f`) rather than a multiple of fit scale, so zoom behavior is uniform across pattern sizes.

### D12: Square padding + cell validity (mask-first) (as implemented)
- **Stretch root cause**: `AreaColor.average` maps the whole image onto the grid with independent X/Y sampling, while the grid is locked square (`updateGridSize`). A non-square crop therefore samples a non-square source region per cell and the subject appears stretched.
- **Fix — pad to square with transparent margins**: `PixelImage.paddedToSquare()` (new, centered, `fillArgb = 0x00000000`) squares the working image once at crop-confirm; every subsequent re-generation reuses it at zero cost. Transparent (not white) is deliberate: both the sampler (`UniformSampler`) and area-averaging already filter fully transparent pixels, so the padding neither pollutes quantization centers nor wastes a color slot.
- **Empty holes (`Grid.cells` now `List<String?>`):** `PatternGenerator` writes `null` for any cell whose source region is below `MIN_CELL_VALID = 0.5` (via new `AreaColor.coverage`), serialized as JSON null. `null` cells are excluded from `distinctCodes`/`paletteUsed`, per-code counts and selection; both Form 1 and Form 2 render them as a light-gray cell with a diagonal hatch and no code.
- **Pixel-level mask (foreground segmentation hook):** `PixelImage` gained an optional `mask: BooleanArray?` (true = valid). When null, validity falls back to transparency (alpha >= 8). Sampling, averaging and coverage all route through `PixelImage.valid`, so a future foreground/background segmentation only needs to produce a mask — no pipeline change.
- **Alternatives not taken**: (a) white padding — pollutes the quantization color centers and wastes a bead slot; (b) trimming the grid to valid content — breaks the square, uniform grid the user asked for.
- **Interactions**: tapping an empty hole is a no-op (does not select, does not clear); counts filter out `null`; the exact-cell marker only draws on valid cells.

### D13: Replace placeholder palette with MARD standard 221 (as implemented)
- **Decision**: replace the synthetic placeholder `generic_221` (codes `C1..C221`: a generated gray ramp + 40 synthetic hue groups) with the real **MARD standard 221-color** table, bundled as `app/src/main/assets/palettes/mard_221.json` (displayName "MARD 标准 221 色"); `generic_221.json` is deleted.
- **Source / license**: `github.com/HansBug/pindou-color-data` (**MIT**), variant `mard-221-alfonse-doudou` (cross-checked against MARD tooling). 221 items = groups A26/B32/C29/D26/E24/F25/G21/H23/M15 (codes `A1..M15`). MARD is the de-facto "221" standard in the CN bead community; Hama official Midi is only ~92 colors, so "官方 221" was interpreted as MARD 221.
- **Mapping**: `code`/rgb copied verbatim from source; `id` = 1..221 by file order; `name`/`brand` left empty (source has no Chinese names, keeping the InfoBar clean).
- **Impact**: color codes change from `C*` to letter+number (`A1`, `H7`, `M15`); `NaturalOrder` already sorts these correctly. Old `.pindou` files whose meta `paletteName = "generic_221"` no longer resolve to a bundled palette (render falls back to white) — accepted, no migration shim.
- **Guard**: `Mard221PaletteFileTest` decodes the bundled asset and asserts 221 unique codes, `id` = 1..221, RGB in range, and natural-order sorting.
- **Alternatives not taken**: `mard-221-github` (same codes, 77 RGB values differ from the cross-checked set) and `artkal-m-221-official` (Artkal's own M221 wording).

## Risks / Trade-offs

- [Risk] K-means randomness can produce slightly different quantCenters across runs → **Mitigation**: Support optional deterministic `seed` (default derive from `sourceHash`).
- [Risk] Large grid + high export DPI can cause OOM → **Mitigation**: Enforce max grid 128×128, clamp `cellOutPx`, estimate bitmap size before createBitmap, warn if exceeds threshold; use viewport culling for interactive.
- [Risk] `.pindou` custom MIME/intent handling varies by OEM → **Mitigation**: Define `application/x-pindou` and/or `application/vnd.pindou+json`, test VIEW intent; also support share of file URI via FileProvider.
- [Risk] Crop transform precision → **Mitigation**: Use integer rounding with bounds clamping; always crop from original bitmap.
- [Trade-off] Decoupled quantization + palette mapping gives flexibility but requires two nearest-neighbor passes (centers→palette small; cells→centers up to 128×128*k small). Acceptable for MVP.
- [Trade-off] JSON (uncompressed) for `.pindou` simple but larger than binary; 128×128 grid small (≤ ~64KB). Can gzip later if needed.
- [Risk] Explicit `PixelImage.mask` must be carried through any processor that rebuilds the image or it will silently fall back to alpha-based validity → **Mitigation**: today no explicit mask exists (alpha drives validity), and `ImageProcessorRegistry` defaults are RGB-shaping only; when segmentation lands, thread the mask through the processor API as part of that work.

## Open Questions

- ~~Initial bundled palette set: confirm exact “>=221 colors” source/format (code naming, brand)~~ → **Resolved by D13**: bundled palette is the MARD standard 221 set (`mard_221.json`, codes `A1..M15`), sourced from the MIT-licensed pindou-color-data dataset.
- Default colorCountTarget values shown on slider (proposed 8,16,32,64,96,128).
- Export DPI/cellOutPx defaults and min/max bounds.
- Whether to include optional seed toggle in UI (expose or just use sourceHash by default).
