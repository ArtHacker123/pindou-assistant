## Why

The floating tool dock had a few usability problems on the canvas:

- The move tool lived in the bottom action bar, split away from the drawing tools, and the bottom bar also carried undo/redo.
- The dock was hard to grab: it could only be dragged from the thin margin around the buttons.
- After moving the divider into the dock, the dock stretched to the full canvas width (a `fillMaxWidth` divider inside a wrap-content column).
- The dock was visually large and covered a lot of the drawing area.
- The dock could be dragged completely off the visible canvas.

This change consolidates all tools and history actions into one compact, draggable dock that stays on-canvas.

## What Changes

- Move the **move tool** and **undo/redo** into the floating dock. Top-to-bottom order: grip → move → undo → redo → divider → paint → erase → eyedropper → bucket. The bottom action bar keeps only the brush-color swatch.
- Add a dedicated **drag grip** at the top of the dock (a tall, non-clickable area) so the dock is easy to grab. The whole dock still accepts drags outside the buttons.
- **Shrink** the dock: 36dp tool/history buttons, 20dp icons, 36×16dp grip area, 10dp corner radius, 2dp shadow. (Tool buttons are custom clickable boxes since Material3 `IconButton` enforces a 48dp minimum.)
- **Clamp** the dock position so it always stays inside the canvas viewport, re-clamped when the viewport or dock size changes.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — this is a presentation/layout change; the `pattern-editing` tool behavior is unchanged)

## Impact

- **App UI**: `ui/editor/EditorScreen.kt` — `FloatingToolbar` (tools, history buttons, grip, sizing), `EditActionBar` (swatch only), canvas size tracking + toolbar position clamping, new `ToolButton` / `HistoryButton` helpers.
- **No format, state, or behavior change.**
