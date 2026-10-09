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

## Risks / Trade-offs

- [Risk] K-means randomness can produce slightly different quantCenters across runs → **Mitigation**: Support optional deterministic `seed` (default derive from `sourceHash`).
- [Risk] Large grid + high export DPI can cause OOM → **Mitigation**: Enforce max grid 128×128, clamp `cellOutPx`, estimate bitmap size before createBitmap, warn if exceeds threshold; use viewport culling for interactive.
- [Risk] `.pindou` custom MIME/intent handling varies by OEM → **Mitigation**: Define `application/x-pindou` and/or `application/vnd.pindou+json`, test VIEW intent; also support share of file URI via FileProvider.
- [Risk] Crop transform precision → **Mitigation**: Use integer rounding with bounds clamping; always crop from original bitmap.
- [Trade-off] Decoupled quantization + palette mapping gives flexibility but requires two nearest-neighbor passes (centers→palette small; cells→centers up to 128×128*k small). Acceptable for MVP.
- [Trade-off] JSON (uncompressed) for `.pindou` simple but larger than binary; 128×128 grid small (≤ ~64KB). Can gzip later if needed.

## Open Questions

- Initial bundled palette set: confirm exact “>=221 colors” source/format (code naming, brand) to include as `assets/palettes/hama_221.json`.
- Default colorCountTarget values shown on slider (proposed 8,16,32,64,96,128).
- Export DPI/cellOutPx defaults and min/max bounds.
- Whether to include optional seed toggle in UI (expose or just use sourceHash by default).
