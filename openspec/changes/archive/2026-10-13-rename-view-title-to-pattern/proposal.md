## Why

The editor's view-mode title read "生成", which describes the generation action rather than the page content. The page is for viewing the pattern (图纸), so "图纸" is clearer and matches the "图纸/缩略图" layer toggles.

## What Changes

- Rename the view-mode top-bar title from "生成" to "图纸"; edit mode stays "编辑".

## Capabilities

### Modified Capabilities
- `pattern-editing`: the editor screen title in view mode is now "图纸".

## Impact

- **App**: `ui/editor/EditorScreen.kt` (top-bar title).
- No core, dependency, or manifest change.
