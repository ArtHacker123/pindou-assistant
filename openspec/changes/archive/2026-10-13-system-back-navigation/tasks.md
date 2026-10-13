## System back navigation

- [x] 1.1 Handle the system back gesture on the history screen (return home).
- [x] 1.2 Handle the system back gesture on the export screen (return to the editor).
- [x] 1.3 Handle the system back gesture on the crop screen (cancel the crop).
- [x] 1.4 Leave the editor's and home screen's existing back behavior unchanged.

## Verification

- [x] 2.1 `:core:test :app:assembleDebug` passes; app installs.
- [ ] 2.2 Back gesture on 我的图纸 / 导出图纸 / 裁剪与缩放 navigates like the back arrow instead of closing the app.
