## 1. Core editor module

- [x] 1.1 Create `core/.../editor` package; keep deps limited to `model` + `color` (extend `PackageBoundaryTest` if needed)
- [x] 1.2 Add `paint(grid, row, col, code)` and `erase(grid, row, col)` returning a new `Grid`, bounds-checked (out-of-range is a no-op)
- [x] 1.3 Add `floodFill(grid, row, col, code)`: 4-connected region of equal `colorCode` (including `null`), bounded by the grid
- [x] 1.4 Add `remap(grid, palette, colorSpace)`: nearest-color per non-null cell via `ColorSpace.nearestIndex`; `null` holes preserved
- [x] 1.5 Add an eyedropper resolver: bead cell → its code; empty hole + reference → nearest palette color to the reference pixel
- [x] 1.6 Unit tests for every op (edges, out-of-range no-op, 128×128 fill/remap, hole preservation)

## 2. App editing state

- [x] 2.1 Add `state/EditingSession.kt` (edit-mode flag, active tool, selected brush color) that applies pure ops and commits each change to `DocumentStore`
- [x] 2.2 Expose editing state + ops on the `PindouController` facade (observed props + thin delegations)
- [x] 2.3 Let `DocumentStore` hold a mutated project grid and recompute `paletteUsed` / per-code counts without regenerating
- [x] 2.4 Track "edited since last generation" and gate regeneration behind the confirmation rule

## 3. Editor UI

- [x] 3.1 Add the view ⇄ edit mode shell to `EditorScreen` with a tool bar and a "完成" action (viewport preserved across the switch)
- [x] 3.2 Extend `PatternGrid` with edit callbacks and live-stroke rendering; map touches via the inverse viewport transform, ignore out-of-bounds
- [x] 3.3 Implement single-finger drag painting (dedupe per cell, apply live) and two-finger pan/zoom
- [x] 3.4 Let the color table set the brush color; wire paint / erase / eyedropper / bucket selection

## 4. Free creation

- [x] 4.1 Add a Home "自由创作" entry with a palette + square-size picker (default 52×52, default palette)
- [x] 4.2 Create a blank project (no source image) and open it directly in edit mode; ensure save/export work with `sourceHash = null`

## 5. Reference in edit mode

- [x] 5.1 Allow loading a reference image in edit mode; build it at grid resolution via `ReferenceService` and reuse while inputs are unchanged
- [x] 5.2 Hide the "图纸" toggle in edit mode while keeping the reference toggle and press-and-hold peek over the always-visible beads
- [x] 5.3 Wire the eyedropper's hole sampling to the reference when available

## 6. Parameter rules

- [x] 6.1 Palette change: confirm, then remap all beads to the nearest new-palette colors (holes preserved)
- [x] 6.2 Grid-size change: confirm, then clear the grid to the new size (no crop/scale)
- [x] 6.3 Regeneration after manual edits: confirm discard before overwriting the grid

## 8. Editor polish (review round 1)

- [x] 8.1 Tool bar shows icons instead of text (paint / eraser / eyedropper / bucket), active tool highlighted; `material-icons-extended` used, eraser as a custom vector drawable
- [x] 8.2 Full-palette color picker grouped by series (leading letters of the code, e.g. A/C/M), opened by tapping the current-color swatch in the tool bar; swatch tap sets the brush
- [x] 8.3 Entering edit mode with no brush defaults to the palette color closest to pure black (computed, e.g. `H7` on MARD 221)
- [x] 8.4 `EditingSession.cursor` tracks the cell under the finger; `PatternGrid` draws a one-cell-sized outline highlight, cleared on gesture end or when leaving the grid
- [x] 8.5 Build passes (`:core:test :app:assembleDebug`)
- [x] 8.6 Default black brush lives in `EditingSession.enter()` so free creation (not just "编辑") starts with it; the current-color swatch always renders so the picker is reachable
- [x] 8.7 Picker series chips show a representative series color (average) and the row has left/right scroll arrows that disable at either end
- [x] 8.8 Eyedropper: `GridEditor.pick(preferReference)` samples the reference over a bead during the reference-only peek; falls back to the bead without a reference; `EditorScreen` passes the `peeking` flag through `applyEdit`; unit tests added

## 9. Editor polish (review round 2)

- [x] 9.1 Size change branches by project type: source + untouched regenerates silently; source + edited prompts then regenerates; no source prompts then clears to a blank grid
- [x] 9.2 Picker series row: representative color box enlarged and the left/right scroll arrows enlarged (32.dp icon, larger step)
- [x] 9.3 Add move (移动) tool: single-finger drag pans, drawing disabled while active (`PatternGrid.moveMode`)
- [x] 9.4 Add undo/redo in `EditingSession`: stroke-coalesced history (one undo per drag), palette remaps and blank resizes undoable, redo cleared on new edit, history cleared on open/new project; tool-bar buttons with enable state
- [x] 9.5 Update openspec docs (D5/D7/D12/D17, resize/undo/move requirements) and re-validate `--strict`
- [x] 9.6 Build passes (`:app:assembleDebug`) and the APK installs and launches on `642cc491`

## 10. Tool bar & history fixes (review round 3)

- [x] 10.1 Regeneration clears edit history: `GenerationSession.onRegenerated` hook → `editing.clearHistory()` (also drops an in-flight stroke); fixes undo restoring the pre-editing grid after a resize/regenerate
- [x] 10.2 Two-row tool bar: row 1 = scrollable tool icons (`LazyRow`), row 2 = undo/redo + quick used colors (up to 8) + current-color swatch
- [x] 10.3 Update openspec docs (D17/D18, tool bar + regeneration-clears-history requirements) and re-validate `--strict`
- [x] 10.4 Build passes (`:core:test :app:assembleDebug`) and the APK installs and launches on `642cc491`

## 11. Floating tool bar (review round 4)

- [x] 11.1 Floating drawing-tool dock on the canvas: paint/erase/eyedropper/bucket as a draggable dock (default right-center), gesture catcher consumes touches inside it so the grid never draws underneath, tool buttons still tappable
- [x] 11.2 Grid skips any gesture whose down press is already consumed (`PatternGrid` drain + return)
- [x] 11.3 Bottom bar keeps move / undo / redo / quick used colors / current-color swatch
- [x] 11.4 Update openspec docs (D18, tool bar requirements) and re-validate `--strict`
- [x] 11.5 Build passes (`:core:test :app:assembleDebug`) and the APK installs and launches on `642cc491`
- [x] 11.6 Remove the quick "used colors" strip from the bottom bar (it accumulated colors left of the swatch, see review round 5); color choice is back to the swatch picker + color table only

## 7. Verification

- [x] 7.1 Run `:core:test` and `:app:assembleDebug` (with `JAVA_HOME` set to the Android Studio JBR)
- [ ] 7.2 Manual smoke on device `642cc491`: edit a generated pattern (each tool); palette remap; size clear; regen-discard confirm; free creation; reference load + peek; export PNG; save/reopen `.pindou`
- [ ] 7.3 Confirm an edited and a free-created project both round-trip through `.pindou` unchanged
