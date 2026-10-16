## Why

The 设置 panel is shown above the pattern canvas and takes vertical space the user needs for drawing and inspecting. After adjusting settings, the user must currently tap the 设置 button again to get the space back, which interrupts the flow of working on the pattern.

## What Changes

- Touching the pattern drawing area (any canvas gesture: tapping a cell in view mode, drawing in edit mode, or panning/zooming) SHALL automatically collapse the 设置 panel whenever it is open.
- Interaction with the floating overlays (the draggable tool dock and the reference peek button) is not a canvas gesture and does not collapse the panel.
- The nested 高级 section keeps its existing expansion state; only the outer 设置 panel collapses.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities
- `generation-settings`: add a requirement that any interaction with the pattern canvas collapses the open settings panel.

## Impact

- `app/src/main/kotlin/com/pindou/app/ui/editor/PatternGrid.kt`: expose a new gesture-start callback.
- `app/src/main/kotlin/com/pindou/app/ui/editor/EditorScreen.kt`: wire the callback to collapse `paramsExpanded`.
- No new dependencies; no persistence or model changes.
