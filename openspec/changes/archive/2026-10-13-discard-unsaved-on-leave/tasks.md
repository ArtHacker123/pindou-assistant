## Ask before leaving with unsaved changes

- [x] 1.1 Snapshot the committed project (open / new / generation / save) in `DocumentStore`.
- [x] 1.2 `PindouController.discardEdits()` reverts to the snapshot and clears edit history.
- [x] 1.3 Editor back (button + gesture) asks whether to save, then leaves to the origin-appropriate screen; 不保存 discards.

## Verification

- [x] 2.1 `:core:test :app:assembleDebug` passes; app installs.
- [x] 2.2 Edit → back prompts; 不保存 restores the original pattern; destination is 图纸 view for normal projects and home for free creation.
