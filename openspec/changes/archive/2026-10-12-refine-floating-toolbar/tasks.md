## 1. Consolidate tools into the floating dock

- [x] 1.1 Move the move tool from the bottom action bar into the floating dock.
- [x] 1.2 Move undo/redo from the bottom action bar into the floating dock, placed below the move tool.
- [x] 1.3 Order the dock: grip → move → undo → redo → divider → paint → erase → eyedropper → bucket.
- [x] 1.4 Leave only the brush-color swatch in the bottom action bar.

## 2. Drag affordance and sizing

- [x] 2.1 Add a tall, non-clickable drag grip at the top of the dock so it can be grabbed easily.
- [x] 2.2 Shrink the dock: 36dp buttons, 20dp icons, 36×16dp grip area, 10dp corner radius, 2dp shadow.

## 3. Fix width and keep on-canvas

- [x] 3.1 Fix the dock stretching to full width (divider changed from `fillMaxWidth` to a fixed width).
- [x] 3.2 Track canvas and dock sizes; clamp the dock position so it stays inside the canvas, re-clamping on size changes; hide until measured to avoid a first-frame jump.

## 4. Verification

- [x] 4.1 Build `:core:test :app:assembleDebug`.
- [x] 4.2 Install and smoke-test on device: all tools + undo/redo reachable in the dock; dock compact; dock cannot leave the canvas; grip drags reliably.
