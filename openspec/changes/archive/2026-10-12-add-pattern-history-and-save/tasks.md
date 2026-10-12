## 1. Pattern history store

- [x] 1.1 Add `PatternEntry` and `HistoryStore` that list `.pindou` files under the public `Documents/PindouAssistant` folder (MediaStore on API 29+, directory listing below), decode each project, and sort newest first.
- [x] 1.2 `HistoryStore.delete(uri)` removes a saved file (MediaStore delete / file delete).
- [x] 1.3 Add `HistorySession` exposing `items`/`loading` with `refresh()` and `delete()` on a background dispatcher.

## 2. Controller and navigation

- [x] 2.1 Add `Screen.History` and route it in `PindouApp`.
- [x] 2.2 Expose `historyItems`/`historyLoading`, `goHistory()`, `refreshHistory()`, `deleteHistory()` on `PindouController`; opening an entry reuses `openPindou(uri)`.

## 3. Home screen

- [x] 3.1 Primary actions at the top: generate from gallery / camera, and free creation.
- [x] 3.2 "最近图纸" row of the most recent thumbnails with a "查看全部" entry to the history page, plus loading/empty states.
- [x] 3.3 Remove the "打开 .pindou 文件" launch entry and the inline palette card.

## 4. History page and thumbnails

- [x] 4.1 `GridThumbnail` draws a pattern grid on a Canvas (no bitmap allocation).
- [x] 4.2 `HistoryScreen` shows a grid of thumbnail cards (title / date) that open the pattern on tap.
- [x] 4.3 Each card offers delete with a confirmation dialog; the list refreshes after deletion.

## 5. One-tap save

- [x] 5.1 Edit mode gains a `保存` control between `设置` and `完成`.
- [x] 5.2 `完成` and `导出` save the project first; the editor save always embeds the reference image.
- [x] 5.3 The `.pindou` base name is stable (derived from the project's creation time) so re-saving overwrites the same Documents file.
- [x] 5.4 Remove the `保存 .pindou` button from the export screen (keep `分享 .pindou`).

## 6. Verification

- [x] 6.1 Build `:core:test :app:assembleDebug`.
- [x] 6.2 Install and smoke-test on device: home shows recent thumbnails, the history page lists/deletes/opens, and saving twice keeps a single file.
