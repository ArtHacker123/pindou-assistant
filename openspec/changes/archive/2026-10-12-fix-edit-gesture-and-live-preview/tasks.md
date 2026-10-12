## 1. Gesture classification (PatternGrid)

- [x] 1.1 Remove the finger-down edit; track the down position and defer editing until the gesture is classified.
- [x] 1.2 Treat `pressedCount >= 2` as navigation: never edit; if a stroke is active, finalize it (keep drawn cells) and stop drawing.
- [x] 1.3 On single-finger slop crossing with a drag tool (paint/erase), start a stroke and paint from the down cell along the path.
- [x] 1.4 On a clean one-finger tap (no slop, no second finger), apply the active tool once at the down cell.
- [x] 1.5 Add a `dragDraw` flag so bucket/eyedropper/move only navigate; pass it from `EditorScreen` for paint/erase.

## 2. Live preview (EditingSession)

- [x] 2.1 In `strokeEdit`, when a stroke is open, write each change to the document so the canvas updates live.
- [x] 2.2 Keep a single undo entry per stroke (pushed once in `endStroke`).

## 3. Verification

- [x] 3.1 Build `:core:test :app:assembleDebug`.
- [x] 3.2 Install and smoke-test on device: two-finger pan leaves no stray cell; single tap paints/fills/picks; drag paints live; undo removes a whole stroke.
