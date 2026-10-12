## 1. Refresh trigger

- [x] 1.1 Refresh the recent/history list whenever the home screen becomes the current screen (`ui/PindouApp.kt`, keyed on the current screen).
- [x] 1.2 Refresh on `ON_RESUME` while the home screen is showing.
- [x] 1.3 Remove the one-shot refresh from `ui/HomeScreen.kt`.
- [x] 1.4 Refresh the history immediately after a save completes (`PindouController.saveProject` / `exportPindou`).

## 2. Verification

- [x] 2.1 Build `:core:test :app:assembleDebug` succeeds.
- [x] 2.2 Save from the editor, then return to home: the just-saved pattern appears.
