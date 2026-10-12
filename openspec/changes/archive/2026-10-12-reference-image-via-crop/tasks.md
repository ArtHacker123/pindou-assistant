## 1. Crop purpose plumbing

- [x] 1.1 Add `CropPurpose { SOURCE, REFERENCE }` and `cropPurpose` state to `NavigationState`.
- [x] 1.2 `PindouController.pickImage` sets purpose `SOURCE`; add `pickReference` that sets purpose `REFERENCE` and opens the crop screen.
- [x] 1.3 `onCropConfirmed` dispatches by purpose: `SOURCE` keeps the existing generate flow; `REFERENCE` builds the grid-resolution reference and returns to the editor without regenerating.
- [x] 1.4 `backFromCrop` dispatches by purpose: `REFERENCE` returns to the editor, `SOURCE` returns to Home.
- [x] 1.5 Remove the direct `loadReference(uri)` (image no longer bypasses crop).

## 2. Entry placement and visibility

- [x] 2.1 Remove the reference entry from the editor top app bar.
- [x] 2.2 Add the reference entry to the left of the brush-color control in the edit action bar.
- [x] 2.3 Show the entry only when the project has no source image (`!hasSource`).

## 3. Verification

- [x] 3.1 Build `:core:test :app:assembleDebug`.
- [x] 3.2 Install and smoke-test on device: free creation shows the entry, opens crop, confirming overlays the reference; a generated (source-image) project hides the entry.

## 4. Process guard

- [x] 4.1 Add `AGENTS.md` documenting that file contents must be changed with the `read`/`write`/`edit` tools, never PowerShell content cmdlets.
