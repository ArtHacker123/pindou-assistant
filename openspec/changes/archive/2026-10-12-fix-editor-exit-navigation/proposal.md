## Why

Two exit-route defects shipped with `add-pattern-editing`:

1. Leaving the editor discards unsaved work silently. A generated or free-created project (and any manual edit) only becomes a persistent document when the user saves a `.pindou` file, but the back action returned Home without warning — losing edits with no chance to save.
2. The "完成" action was special-cased for free-created projects to jump straight Home, so finishing an edit never entered the view state the user expects.

## What Changes

- **Back action routes by project type.** From edit mode of a source-image project, back returns to view mode (nothing is lost, still in the editor). From a free-created project, and from view mode, back leaves the editor for Home.
- **"完成" always returns to view mode** for every project type (removes the free-creation special case).
- **Unsaved-changes prompt on leaving.** When leaving the editor to Home while the project has unsaved changes, prompt to save: 保存 persists a `.pindou` document then leaves, 不保存 leaves discarding the changes.
- **Track a saved/dirty flag** on the document (set by generation, free creation, edits, and reference changes; cleared by a successful `.pindou` save and by opening an existing file).
- The tool bar back button and the system back gesture share the same routing/prompt logic.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `pattern-editing`: adds the editor-exit routing and the unsaved-changes save prompt; clarifies that 完成 always returns to view mode.

## Impact

- **App UI**: `ui/editor/EditorScreen.kt` (back + 完成 wiring, system `BackHandler`, save-confirm dialog).
- **App state**: `state/DocumentStore.kt` (`needsSave` dirty flag + `markSaved`), `state/ExportService.kt` (save-success callback), `PindouController` (`needsSave` + open/save wiring).
- **No format change.**
