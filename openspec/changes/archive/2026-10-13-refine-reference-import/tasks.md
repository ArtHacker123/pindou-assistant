## Reference import, display, and loading fixes

- [x] 1.1 Rename the edit-mode reference entry to "导入参考图".
- [x] 1.2 Add a "显示参考图" toggle to the edit action bar (highlight + ✓ when active).
- [x] 1.3 Move the reference show/hide state into `NavigationState` so it survives the crop detour; import shows it by default, and generate/open/free-creation reset it to hidden.
- [x] 1.4 Pad an imported reference's crop to a centered square before deriving the reference.
- [x] 1.5 Downsample loaded images to a 2048 px longest edge.

## Verification

- [x] 2.1 `:core:test :app:assembleDebug` passes; app installs and launches without crashing.
- [x] 2.2 Importing a reference shows it by default and the state survives the crop detour.
- [x] 2.3 Large photos no longer cause an out-of-memory crash during preprocessing.
