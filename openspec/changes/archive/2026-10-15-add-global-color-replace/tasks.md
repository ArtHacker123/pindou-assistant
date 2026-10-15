## 1. Core

- [x] 1.1 Add `GridEditor.replaceAll(grid, from, to): Grid` — exact code match, holes untouched, returns the same instance when nothing changes (`core/editor/GridEditor.kt`)
- [x] 1.2 Add unit tests: replaces every matching cell; only the matching code changes; disconnected cells are all replaced; holes survive; `from == to` / no match returns the same instance

## 2. Session and controller

- [x] 2.1 Add `EditTool.REPLACE("替换")` to the tool enum (`app/state/EditingSession.kt`)
- [x] 2.2 Add `EditingSession.replaceColor(from, to)`: guard no-op (`from == to`, empty grid change), `pushUndo`, `documents.replaceGrid(next)`, clear redo, refresh history
- [x] 2.3 Handle the replace tool in `EditingSession.applyAt`: resolve the tapped cell's code as the source and replace with `brushCode` (ignore holes / null brush)
- [x] 2.4 Reach the op through the existing `applyEdit` → `EditingSession.applyAt` forward (`PindouController` needs no extra method)

## 3. UI

- [x] 3.1 Map `EditTool.REPLACE` to an icon (`Icons.Filled.FindReplace`) and a tool label in `EditorScreen` (`icon()` and the info-bar label via `EditTool.label`)
- [x] 3.2 Replace is tap-only by construction: `PatternGrid` already routes a clean tap to `onEdit`, and `dragDraw` is false for `EditTool.REPLACE` (no grid change needed)
- [x] 3.3 Add the replace tool button to the floating dock after the bucket, in the documented order

## 4. Verification

- [x] 4.1 `:core:test :app:assembleDebug` passes
- [x] 4.2 Install the APK and have the user verify: replace a color everywhere, holes/target-color no-ops, and one-step undo
- [x] 4.3 Update `docs/TODO.md` (check B3) and re-validate the change `--strict`
