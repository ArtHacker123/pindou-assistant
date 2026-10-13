## Open shared files

- [x] 1.1 Add a VIEW intent filter matching `application/octet-stream` (content/file) so messenger share targets list the app.
- [x] 1.2 Validate an opened file and show a friendly, non-crashing error for non-`.pindou` / corrupt files.

## Import to my patterns

- [x] 2.1 Mark externally opened projects as imported (reset on new project / history open).
- [x] 2.2 Add `importToDocuments()` that always writes to `Documents/PindouAssistant` and refreshes history.
- [x] 2.3 Show an editor card ("来自外部的图纸文件" + 保存) only for imported projects, hidden after saving.

## Verification

- [x] 3.1 `:core:test :app:assembleDebug` passes; app installs and launches without crashing.
- [x] 3.2 `pm query-activities` for `VIEW` + `application/octet-stream` lists `com.pindou.app.MainActivity`.
- [x] 3.3 Opening a `.pindou` via a `content://` VIEW intent loads the editor and shows the import card; an invalid file shows the friendly error.
