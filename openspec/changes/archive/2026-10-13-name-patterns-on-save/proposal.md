## Why

Saved patterns had no name: every `.pindou` was written as `pindou_<w>x<h>_<timestamp>` and every PNG the same way, and `ProjectMeta.name` was never set. The files were impossible to tell apart, and "我的图纸" could only show size/palette/time.

## What Changes

- Prompt for a pattern name whenever a pattern is saved or exported, pre-filled with a suggested name that is selectable (so the user can keep it or type their own).
- Store the chosen name in `ProjectMeta.name` and use it as the `.pindou` and PNG file name.
- Show the pattern name as the title on each "我的图纸" card.
- Seed the suggested name with the template name for template-based patterns, and a size-based default otherwise.

## Capabilities

### Modified Capabilities
- `pattern-share`: saving and exporting prompt for a name and use it as the `.pindou`/PNG file name.
- `pattern-history`: history cards show the pattern name as their title.

## Impact

- **App**: `data/ExportManager.kt` (name sanitizer), `state/ExportService.kt` (name-based file names), `state/DocumentStore.kt` (`updateName`), `PindouController.kt` (`suggestName`, name-aware save/export, template default name), `ui/NameDialog.kt` (new), `ui/EditorScreen.kt`, `ui/ExportScreen.kt`, `ui/HistoryScreen.kt`.
- No core, dependency, or manifest change.
