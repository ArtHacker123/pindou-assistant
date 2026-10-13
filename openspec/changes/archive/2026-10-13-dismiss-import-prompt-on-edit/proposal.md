## Why

The "来自外部的图纸文件 / 保存到我的图纸" card kept showing for an imported `.pindou` even after the user entered edit mode or changed the pattern settings. At that point the project is no longer the file that was received, so prompting to save the original is wrong; the user just saves their edited result normally.

## What Changes

- Dismiss the import card once the user enters edit mode (tapping 编辑).
- Dismiss it on any grid/palette change (edits, palette remap, blank resize).

## Capabilities

### Modified Capabilities
- `pattern-share`: the import prompt is a one-time nudge for a pristine received file and disappears as soon as the project is edited or its settings are changed.

## Impact

- **App**: `PindouController.kt` (`enterEdit` clears the imported flag), `state/DocumentStore.kt` (`replaceGrid` / `replaceGridAndPalette` clear the imported flag).
- No core, dependency, or manifest change.
