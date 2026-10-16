## 1. Canvas interaction hook

- [x] 1.1 Add an `onInteract: () -> Unit = {}` parameter to `PatternGrid` and fire it once per gesture after the `down.isConsumed` overlay early-return.

## 2. Float the settings panel over the canvas

- [x] 2.1 Move `ParamsPanel` out of the editor `Column` and render it as a top-aligned overlay inside the canvas `Box`, so the canvas never resizes when the panel opens or collapses.
- [x] 2.2 Add a transparent `matchParentSize` gesture catcher over the panel so taps on the panel never fall through to the grid.
- [x] 2.3 Drop the leading `Divider()` from `ParamsPanel` now that it is a floating card.

## 3. Wire the settings collapse

- [x] 3.1 Pass `onInteract` from `EditorScreen` to collapse `paramsExpanded` when it is open, leaving `advancedExpanded` untouched.

## 4. Verify

- [x] 4.1 Build and install the APK for device verification.
