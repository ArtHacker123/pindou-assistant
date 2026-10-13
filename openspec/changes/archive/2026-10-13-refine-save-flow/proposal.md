## Why

Two save-flow annoyances: saving an accidentally empty pattern (e.g. a blank free creation) silently wrote an empty file, and re-tapping 保存 / 完成 / 导出 right after a save re-wrote the file and popped the "已保存到 文档/…" dialog even though nothing had changed.

## What Changes

- When the grid has no beads, the editor confirms before saving/exporting: "当前图纸没有任何珠子，为空。是否仍要保存/导出？"
- When nothing changed since the last save or open (`needsSave == false`), 保存, 完成, and 导出 skip the file write and its completion dialog entirely and proceed straight to the follow-up action (leave edit mode / open the export screen).

## Capabilities

### Modified Capabilities
- `pattern-share`: guarded, no-op-when-clean `.pindou` saves.

## Impact

- **App state**: `PindouController.saveProject` becomes a no-op (with no dialog) when `needsSave` is false.
- **App UI**: `EditorScreen` confirms on empty grids only when there is something to save, bounded by `needsSave`.