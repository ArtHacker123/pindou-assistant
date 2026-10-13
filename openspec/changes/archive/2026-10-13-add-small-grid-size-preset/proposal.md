## Why

The quick templates build 15×15 patterns, but 15×15 was not selectable anywhere else, so a user could not generate or free-create a pattern at the same size as a template.

## What Changes

- Add 15×15 to the square size presets offered everywhere a pattern size is chosen: the generate parameters panel and the free-creation size picker. The list becomes 15, 29, 52, 58, 78, 104, 116, 128.

## Capabilities

### Modified Capabilities
- `bead-grid`: the preset grid sizes now include a 15×15 option.

## Impact

- **App UI**: `ui/editor/EditorScreen.kt` (`GRID_SIZES`) and `ui/HomeScreen.kt` (`FREE_SIZES`) each gain the leading `15` entry.
- No core-model or file-format change.