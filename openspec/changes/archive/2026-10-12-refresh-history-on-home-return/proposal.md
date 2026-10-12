## Why

After saving in the editor, returning to the home screen could still show a stale "最近图纸" list. The recent list was only read once, when the home screen was first composed, so it missed patterns saved later in the same session. Saving is also asynchronous (the `.pindou` is written off the main thread), so a refresh triggered on navigation could even run before the file landed. Users expect the recent list to always reflect what they just saved.

## What Changes

- Refresh the recent/history list every time the home screen becomes the current screen (returning from the editor, export, crop, or history page).
- Refresh when the app returns to the foreground while the home screen is showing (the home screen is not recomposed when the app was merely backgrounded).
- Refresh immediately after a save completes, so a just-saved pattern appears regardless of how quickly the user navigates back to home.

## Capabilities

### Modified Capabilities
- `pattern-history`: the home "recent" list (and history page) now refresh on returning to home, on app resume, and immediately after a successful save.

## Impact

- **App UI**: `ui/PindouApp.kt` (screen- and lifecycle-driven refresh instead of a one-shot effect in `ui/HomeScreen.kt`).
- **App state**: `PindouController.kt` refreshes the history after a save completes.
- **No file-format change.**
