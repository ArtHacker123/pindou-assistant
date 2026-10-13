## Why

On the 我的图纸 (history), 导出图纸 (export), and 裁剪与缩放 (crop) screens the system back button / back gesture is not handled, so it falls through to the activity and closes the app immediately. The on-screen back arrow on those screens navigates within the app (history → home, export → editor, crop → previous screen), so the gesture should do the same.

## What Changes

- Handle the system back gesture on the history, export, and crop screens so it performs the same navigation as each screen's on-screen back control instead of exiting the app.
- Leave the editor's existing back behavior (leave edit mode, else return home) and the home screen's exit-on-back behavior unchanged.

## Capabilities

### Added Capabilities
- `navigation`: system back gesture mirrors the current screen's back control.

## Impact

- **App**: `ui/HistoryScreen.kt`, `ui/ExportScreen.kt`, `ui/CropScreen.kt` (each adds a `BackHandler`).
- No core, dependency, or manifest change.
