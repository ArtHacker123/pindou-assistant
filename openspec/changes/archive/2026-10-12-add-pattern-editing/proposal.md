## Why

Today the app only *generates* a pattern from a photo or a `.pindou` file — the grid is read-only. Users cannot fix a few mismatched beads, tweak a motif, or start a design from scratch. This change adds in-place editing and blank free creation so a generated or blank grid becomes a first-class, editable document.

## What Changes

- Add an **edit mode** to the pattern screen: a mode-based shell switches between the existing view mode and a new edit mode over the same grid.
- Add **editing tools (MVP)**: paint (brush), erase, eyedropper, and bucket fill.
- Add **free creation**: start a blank square grid (default 52×52) with a chosen palette, entering edit mode directly; a reference image can be loaded at any time as a sub-feature.
- Edits apply **live** to the in-memory project and are what gets saved/exported (no separate "save edits" step; "完成" returns to view mode).
- On the parameter panel, changing **palette** re-maps every bead to the nearest color of the new palette (after confirmation); changing **grid size** clears the grid (after confirmation). Regenerating from the source image after manual edits requires confirmation that edits will be discarded.
- Edit mode keeps the reference overlay and press-and-hold peek; the separate "图纸" toggle is not shown in edit mode.

## Capabilities

### New Capabilities
- `pattern-editing`: In-place editing of a pattern grid (paint / erase / eyedropper / bucket fill), the view-vs-edit mode shell, live-apply semantics, and the palette-remap / resize-clears / regenerate-discards rules.
- `free-creation`: Start a blank pattern from scratch (choose palette and square size), entering the editor directly, with optional reference-image import.

### Modified Capabilities
- `grid-reference-image`: The reference underlay and peek are available while editing a grid (not only while viewing), and edit mode hides the separate "图纸" toggle.

## Impact

- **App UI**: `ui/editor/EditorScreen.kt` (mode shell, edit toolbar, tool state), `ui/editor/PatternGrid.kt` (draw edits / gesture mapping), a new blank-creation entry from Home, `PindouController` facade surface.
- **App state**: `state/GenerationSession.kt` + `state/DocumentStore.kt` gain an editable-project path (mutate `Grid`/`paletteUsed` without regenerating); possibly a new `state/EditingSession.kt`.
- **Core**: a new `core/editor` package for pure, testable edit operations (apply tool to a grid; remap to a new palette; fill flood; eyedropper source). Uses `ColorSpace.nearestIndex` for remap / reference snapping.
- **No format change**: edited/free-created patterns serialize with the existing `.pindou` schema; free-created projects simply have no source image (`sourceHash = null`).
