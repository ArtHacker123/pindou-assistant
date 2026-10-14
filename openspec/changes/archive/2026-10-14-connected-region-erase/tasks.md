## Region erase

- [x] 1.1 `GridEditor.regionErase` clears the 4-connected same-color region (reuses `floodFill(..., null)`), with core tests.
- [x] 1.2 `EditingSession` holds an `eraseConnected` mode, toggled via `toggleEraseConnected`, applied in the `ERASE` branch as one undo step.
- [x] 1.3 `PindouController` exposes `eraseConnected`, `toggleEraseConnected`, and a mode-aware `editToolLabel`.
- [x] 1.4 Editor dock: double-tap the eraser to toggle; a distinct highlight + label show the mode; the region eraser is tap-only (no drag draw).
- [x] 1.5 `:core:test :app:assembleDebug` passes and the app installs; verified by the user.

## Verification

- [x] 2.1 Double-tapping the eraser toggles region-erase mode (button highlights; tool label shows 橡皮·区域擦除); a single tap only selects the eraser.
- [x] 2.2 Tapping a bead in region mode clears the whole 4-connected same-color region in one undo step; other colors/holes are untouched.
- [x] 2.3 Dragging in region mode pans instead of erasing.
