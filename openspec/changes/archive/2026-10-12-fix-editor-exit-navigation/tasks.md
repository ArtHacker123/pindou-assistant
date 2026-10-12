## 1. App state

- [x] 1.1 Add `needsSave` dirty flag to `DocumentStore` (mutableState). Set to true on: project created/updated, grid replaced, grid+palette replaced, reference grid changed, edits applied. Set to false on `markSaved()` (called after successful `.pindou` save and after opening an existing project).
- [x] 1.2 Expose `needsSave` from `PindouController` as observed state.
- [x] 1.3 Update `ExportService.exportPindou` to accept an optional `onSaved` callback invoked on successful non-share save; `PindouController.exportPindou(share, onSaved)` forwards and calls `documents.markSaved()` when a local save succeeds.

## 2. UI: exit routing and prompt

- [x] 2.1 In `EditorScreen`, implement `leaveToHome()` that checks `needsSave`: if dirty, show save-confirm dialog; else `goHome()`.
- [x] 2.2 Back button: in edit mode with source image → `exitEdit()` (return to view). In edit mode with free creation → `leaveToHome()`. In view mode → `leaveToHome()`.
- [x] 2.3 System back (`BackHandler`): same routing as back button.
- [x] 2.4 "完成" button: always calls `exitEdit()` (returns to view mode) regardless of project type.
- [x] 2.5 Save-confirm dialog: title "未保存", message "当前图纸尚未保存，退出后更改将丢失，是否保存？", confirm "保存", dismiss "不保存". "保存" calls `controller.exportPindou(share=false) { controller.goHome() }` (after marking saved). "不保存" calls `goHome()` immediately.

## 3. Integration points

- [x] 3.1 Ensure free creation sets `needsSave = true` (via `updateProject`).
- [x] 3.2 Ensure generation/regeneration paths that create a new project/grid mark `needsSave = true`.
- [x] 3.3 Ensure opening an existing `.pindou` clears `needsSave` via `markSaved()`.
