## Floating reference peek

- [x] 1.1 Add a draggable circular `FloatingPeekButton` overlay shown whenever the project has a reference image.
- [x] 1.2 Press-and-hold forces reference-only rendering; release restores the prior layer state.
- [x] 1.3 Clamp the peek control inside the canvas (shared `clampToCanvas` with the tool dock).
- [x] 1.4 Remove the view-mode "图纸" / "参考图" chips and the old "按住看原图" chip from `InfoBar`.
- [x] 1.5 View mode defaults to beads-only (reference hidden); the peek control is the only reveal.
- [x] 1.6 Update the peek label to "按住看参考图".

## Verification

- [x] 2.1 `:core:test :app:assembleDebug` passes; app installs and launches without crashing.
- [x] 2.2 On the generate (view) page the reference is hidden until the control is held.
- [x] 2.3 Holding the control shows the reference; dragging moves it and keeps it inside the canvas.
