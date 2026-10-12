## Why

The reference-image entry lived in the editor's top app bar and loaded an image directly, skipping the crop/scale step that every other image import uses. That meant a reference could not be framed to the region of interest, and it diverged from the generation import path. The entry was also shown for projects generated from an image, where a reference already exists.

## What Changes

- Move the reference-image entry from the top app bar to the **left of the brush-color control in the bottom edit action bar**.
- Show the entry **only when the project has no source image** (free creation / opened projects). Projects generated from an image hide it because their reference is derived from the source automatically.
- Route reference import through the **same crop/scale screen** as pattern generation. On confirm, the grid-resolution reference is derived from the cropped region and attached to the current project **without regenerating** the pattern; cancelling returns to the editor.
- Introduce a shared crop purpose (`SOURCE` / `REFERENCE`) so the crop screen and controller dispatch by intent, giving one place to extend with future preprocessing (matting/segmentation).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `grid-reference-image`: the reference is chosen from an entry in the edit action bar (hidden when a source image exists) and is imported through the shared crop/scale screen.

## Impact

- **App state**: `state/NavigationState.kt` (`CropPurpose` + `cropPurpose`), `PindouController.kt` (`pickReference`, `backFromCrop`/`onCropConfirmed` dispatch by purpose; removed the direct `loadReference(uri)`).
- **App UI**: `ui/editor/EditorScreen.kt` — top bar entry removed, entry added to `EditActionBar` (left of the color control, only when `!hasSource`), launcher calls `pickReference`.
- **No format change; the crop screen itself is unchanged.**
- **Process**: added `AGENTS.md` so file edits always use the `read`/`write`/`edit` tools (never PowerShell content cmdlets, which corrupt Chinese UTF-8).
