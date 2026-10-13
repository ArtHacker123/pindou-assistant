## Why

The editor's top bar appended the grid size to the mode title ("生成图纸 52×52"), the color table could be collapsed (hiding the swatches behind a 收起/展开 row), and the grid size / palette name were not visible next to the used-color summary.

## What Changes

- The top bar title is shortened to "生成" (view mode) and "编辑" (edit mode), with the grid-size suffix removed.
- The color table is permanently visible (no collapse): the 收起/展开 control and its state are removed.
- The color table shows a single summary line above the swatches: grid size · palette display name · 用色 N 种, left-aligned.

## Capabilities

### Modified Capabilities
- `pattern-render`: the color table is no longer collapsible and gains a size/palette/color-count summary line.
- `pattern-editing`: the editor top-bar title is documented as "生成" / "编辑".

## Impact

- **App UI**: `ui/editor/EditorScreen.kt` — title text, removal of `tableExpanded`, and the `ColorTablePanel` header/signature.
- No core, dependency, or state change.
