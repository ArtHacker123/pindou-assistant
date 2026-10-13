## Why

The editor's reference comparison used two small chips in the info bar ("图纸" / "参考图") plus a "按住看原图" chip. The chips cluttered the info bar, the tiny peek chip was hard to reach and could not be repositioned, and on the generate (view) page the reference could be left showing when the user only wanted the pattern.

## What Changes

- Remove the view-mode "图纸" / "参考图" compare chips and the old "按住看原图" chip from the info bar.
- Add a draggable circular press-and-hold control labelled "按住看参考图" that forces reference-only rendering while held and restores the prior layer state on release. It appears in both view and edit modes whenever the project has a reference image, and is clamped so it stays fully inside the canvas viewport.
- View mode now hides the reference by default (beads only); the press-and-hold control is the only way to reveal it.
- Edit mode keeps its action-bar reference toggle and gains the same draggable press-and-hold control; the bead layer stays always visible.

## Capabilities

### Modified Capabilities
- `grid-reference-image`: reference comparison moves to a draggable press-and-hold control, and view mode defaults to beads-only.
- `pattern-editing`: the peek control label is updated to "按住看参考图".

## Impact

- **App UI**: `EditorScreen` gains `FloatingPeekButton` (54dp, circular, dock-style, press-and-hold + drag); `InfoBar` loses the compare/peek chips and their parameters; `toolbar`/`peek` offset clamping is shared through `clampToCanvas`.
