## Clear the stale project when a new generation starts

- [x] 1.1 `DocumentStore.clearProject()` empties the project and its derived flags (and the committed baseline) while keeping the working image.
- [x] 1.2 `PindouController.onCropConfirmed` (SOURCE) clears the project before running generation.
- [x] 1.3 `:core:test :app:assembleDebug` passes and the app installs; verified by the user.

## Verification

- [x] 2.1 Open a saved pattern, then generate from a new gallery/camera image: the editor shows the generating state, not the previously opened pattern.
- [x] 2.2 Changing a parameter to regenerate the current image keeps the current pattern visible while busy.
