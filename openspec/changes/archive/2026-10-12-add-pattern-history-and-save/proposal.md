## Why

The home screen only offered a stack of import buttons and never surfaced previously saved work, so returning users could not quickly resume a pattern. The only way to persist a `.pindou` was the export screen, saving produced a fresh timestamped file every time (accumulating duplicates), and the reference image was embedded only when the export toggle allowed it. Users want to open the app and immediately (a) start a new pattern and (b) reopen a previous one, and to save the current pattern with one tap.

## What Changes

- Redesign the home screen: primary actions (generate from gallery / camera, free creation) plus a "最近图纸" row of recent saved thumbnails and a "查看全部" entry to a dedicated history page.
- Add a history page listing every saved `.pindou` as a thumbnail grid (name/size + date) with per-item delete.
- Add one-tap saving from the editor: a `保存` control in edit mode, plus automatic save when leaving edit mode via `完成` or entering `导出`; each save always embeds the reference image.
- Give a pattern's `.pindou` file a stable name so re-saving overwrites the previous file instead of accumulating duplicates.
- Remove the redundant `保存 .pindou` button from the export screen (keep `分享 .pindou`).
- Remove the "打开 .pindou 文件" launch entry from the home screen (shared/attached files still open via the VIEW intent).

## Capabilities

### New Capabilities
- `pattern-history`: discover, preview, open, and delete saved `.pindou` patterns (home "recent" row + a dedicated history page).

### Modified Capabilities
- `pattern-share`: saving a `.pindou` is available with one tap from the editor and on leaving the editor, always embeds the reference image, and overwrites the same pattern's previous file; the export screen no longer offers saving.

## Impact

- **App data**: new `data/HistoryStore.kt` (MediaStore/Documents scan + decode + delete); `data/ExportManager.kt` (overwrite an existing Documents file instead of inserting a duplicate).
- **App state**: new `state/HistorySession.kt`; `PindouController.kt` (`Screen.History`, history API, `saveProject`); `state/ExportService.kt` (stable `.pindou` name, `includeReference` parameter).
- **App UI**: rewritten `ui/HomeScreen.kt`; new `ui/HistoryScreen.kt` and `ui/GridThumbnail.kt`; `ui/PindouApp.kt` (route); `ui/editor/EditorScreen.kt` (save control; auto-save on `完成`/`导出`); `ui/ExportScreen.kt` (remove the save button).
- **No file-format change.** Android 10+ needs no storage permission; API 24–28 still requires `WRITE_EXTERNAL_STORAGE` to write Documents.
