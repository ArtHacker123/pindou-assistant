## Why

Converting arbitrary images into usable 拼豆 (bead) patterns is typically limited to static image exports in existing tools. To be practical for real assembly, a pattern needs both: (1) a printable, clearly labeled layout, and (2) an interactive view that lets the user locate and highlight same-color cells with coordinates. This change introduces a data-first Android app that exports a self-contained, shareable pattern file (`.pindou`) so another device can open it and render the same interactive pattern without requiring the original image.

## What Changes

- **New Android app from scratch** (Kotlin + Jetpack Compose). No existing codebase to modify.
- **Image import and editing**: Gallery/camera import, image-type presets (cartoon/photo, extensible), and an image crop+scale screen with draggable borders, pan/zoom, and confirm/cancel.
- **Grid generation**: Target grid size W×H up to 128×128 with viewport culling and pan/zoom for large grids.
- **Color quantization (decoupled from bead palette)**: Extract N representative image colors (pluggable quantizer interface; K-means first). Image simplification degree (colorCountTarget N) is independent from which bead palette is selected.
- **Bead palette system**: Support multiple bundled market bead palettes (starting with >=221 colors), selectable by user. Each bead uses a stable `colorCode` (e.g., `C1`) for labeling, grid storage, and cross-device reconstruction.
- **Two pattern forms**:
  - **Form 1 (exported image)**: Grid with axes (row/column numbers, origin top-left), each cell labeled with its bead color code, and a right/bottom color lookup table.
  - **Form 2 (interactive)**: Canvas-based interactive view; tapping any cell highlights all cells of the same bead color and displays the clicked grid coordinates.
- **Export and share**: Export/share high-resolution PNG for Form 1, and export/share `.pindou` (JSON) representing the project (grid + palette info + metadata). `.pindou` can be opened by this app to render Form 2 on another device.
- **Extensible architecture**: Strategy interfaces for image processors, quantizers, matchers, renderers, exporters, and palette providers to support future algorithms/formats/palettes and in-place color edits.

## Capabilities

### New Capabilities

- `image-input`: Import images (gallery/camera), apply image-type presets (cartoon/photo, extensible), and provide a crop+scale editor with draggable borders, pan/zoom, and crop confirmation to produce the working image.
- `color-quantization`: Extract N representative image colors from the working image using a pluggable quantizer interface (K-means first). Supports configurable N (e.g., 8/16/32/64/...), deterministic seeding option, and uniform sampling for performance.
- `bead-palette`: Load/manage multiple bundled bead palettes, allow user selection, store palette metadata (code/name/brand/rgb), and build `paletteUsed` (distinct bead colors used). Grid values are stored as `colorCode` (stable identity).
- `bead-grid`: Generate W×H grid (≤ 128×128) by area-average sampling of the cropped image, map cells to representative colors then to selected bead palette codes, and support viewport rendering with pan/zoom for interactive viewing.
- `pattern-render`: Render Form 1 (offline high-DPI bitmap with axes, grid, cell codes, and color table) and Form 2 (interactive Compose Canvas with same-color highlight, coordinate display, collapsible color table).
- `pattern-share`: Serialize project to `.pindou` (JSON), export/share PNG and `.pindou` via FileProvider, and register VIEW intent to open `.pindou` files and launch Form 2 for cross-device interactive viewing.

### Modified Capabilities

- (none)

## Impact

- **Platform**: Android (Kotlin + Jetpack Compose), target/architecture new.
- **Data/artifacts**: New assets (`assets/palettes/*.json`), new file type `.pindou` with custom MIME (`application/vnd.pindou+json` or `application/x-pindou`) and VIEW intent handling.
- **APIs/deps**: Standard AndroidX, Coil, kotlinx-serialization, Activity Result Contracts, FileProvider. No external heavy vision libs required initially.
- **Scope**: Greenfield app under `G:\OpenCode\拼豆助手` (currently not a git repo). No existing modules impacted.
