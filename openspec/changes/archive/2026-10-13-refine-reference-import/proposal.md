## Why

Backfill of commit `002775a`, which added the reference-image entry rename and show toggle, squared the reference crop, and fixed an out-of-memory crash on large photos, but shipped without an OpenSpec change.

## What Changes

- The edit-mode reference-load entry is relabelled "导入参考图" and remains shown only for projects with no source image; a "显示参考图" toggle is added to the edit action bar and highlights with a trailing ✓ when the reference is visible.
- Importing a reference shows it by default, and the show/hide state lives in the navigation state so it survives the editor → crop → editor detour. Generating from an image, opening a project, and starting a free creation reset it to hidden.
- The cropped region of an imported reference is padded to a centered square (the same square-padding used before generation) before the reference is derived, so it aligns 1:1 with the grid without stretching.
- Oversized loaded images are downsampled to a 2048 px longest edge during decoding, preventing out-of-memory failures during preprocessing and generation.

## Capabilities

### Modified Capabilities
- `grid-reference-image`: reference-load entry renamed, a show toggle with import-defaults-to-visible and cross-crop persistence, and a squared reference crop.

### Added Capabilities
- `image-input`: downsampling of oversized input images.

## Impact

- **App state**: `state/NavigationState.kt` gains `showReference`/`updateShowReference`; `PindouController` exposes it and resets it on generate/open/free-creation.
- **App UI**: `ui/editor/EditorScreen.kt` reference entry + toggle, and `showReference` now read from the controller.
- **Imaging**: `data/ImageLoader.kt` decoding samples to `MAX_DIMENSION` (2048).
