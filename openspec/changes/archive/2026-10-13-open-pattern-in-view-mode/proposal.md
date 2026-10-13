## Why

Opening an existing pattern (from the home recent list, the history page, or a shared `.pindou`) could land in edit mode instead of the 图纸 view: the editor's edit-mode flag is a session flag that was not reset when a document was loaded, so a previous editing session leaked into the next opened file.

## What Changes

- Opening an existing pattern always enters view mode (图纸), regardless of any prior editing session.
- The same reset applies when a new pattern is generated from a picked image.

## Capabilities

### Modified Capabilities
- `pattern-editing`: opening an existing pattern enters the editor in view mode, never edit mode.

## Impact

- **App**: `PindouController.kt` (`openProject` and the source-generation path exit edit mode).
- No core, dependency, or manifest change.
