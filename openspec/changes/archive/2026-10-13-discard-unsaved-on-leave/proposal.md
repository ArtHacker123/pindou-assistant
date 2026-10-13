## Why

Leaving edit mode via the back control or the back gesture silently dropped to the pattern view without asking to save, while the save reminder only appeared one level later when leaving the pattern view. Also, there was no way to abandon edits: because editing writes straight into the in-memory project, "not saving" only skipped writing the file and left the modified pattern in memory.

## What Changes

- Leaving the editor (back control or back gesture) with unsaved changes now asks whether to save.
- Choosing 不保存 discards every change since the last save, open, or generation, reverting the pattern to its last committed content.
- The leave destination depends on where editing began: leaving edit mode of a normal project returns to the pattern view; free creation and leaving the view return home.

## Capabilities

### Modified Capabilities
- `navigation`: the editor's back behavior now asks before leaving unsaved changes and returns to the appropriate screen.

### New Requirements
- `pattern-share`: leaving the editor with unsaved changes asks to save or discard, and discarding restores the last committed pattern.

## Impact

- **App**: `state/DocumentStore.kt` (committed snapshot + revert), `PindouController.kt` (`discardEdits`, commit calls), `state/GenerationSession.kt` (commit after generation), `ui/editor/EditorScreen.kt` (leave destination + discard).
- No core, dependency, or manifest change.
