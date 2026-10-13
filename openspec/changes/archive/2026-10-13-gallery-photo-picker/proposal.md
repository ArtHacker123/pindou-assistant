## Why

"从相册生成" opened a generic file browser (`GetContent`, `image/*`), forcing the user to navigate folders to find a photo. It should open the photo gallery directly in its thumbnail preview so a picture can be chosen in one step.

## What Changes

- The home "从相册生成" action now launches the system photo picker (`ActivityResultContracts.PickVisualMedia` in `ImageOnly` mode) instead of the generic document/file picker, so the user lands directly on a photo thumbnail grid instead of a folder browser.
- Picking a photo still loads the original image and proceeds to the crop/scale screen unchanged.

## Capabilities

### Modified Capabilities
- `image-input`: gallery import now uses the system photo picker instead of a folder-based file browser.

## Impact

- **App UI**: `ui/HomeScreen.kt` swaps `ActivityResultContracts.GetContent` for `PickVisualMedia` (photo-only).
- No permission or manifest change; the system photo picker needs none.