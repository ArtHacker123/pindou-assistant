## Context

The pattern screen currently renders an immutable `PindouProject` grid (`Grid.cells: List<String?>`) and regenerates the whole pipeline on any parameter change (`GenerationSession.runGenerate`). The `refactor-imaging-and-state` change already introduced the seams this feature needs: one-way `core` packages, a shared `ReferenceService` (`preprocess`/`reference`/`referenceFrom`), and a thin `PindouController` facade over `NavigationState` / `DocumentStore` / `GenerationSession` / `ReferenceService` / `ExportService`.

This change builds the editing + free-creation feature on those seams. Editing is a **mutation of the grid that is not produced by the generator**, so we need (a) a pure, testable edit core, (b) a mode-based UI over the same grid, and (c) rules for how edits interact with the still-available generation parameters.

## Goals / Non-Goals

**Goals:**
- In-place editing of a grid with paint / erase / eyedropper / bucket fill; edits apply live to the in-memory project.
- A view-mode ⇄ edit-mode shell over the same `PatternGrid`, plus a blank "free creation" entry that starts directly in edit mode.
- Consistent, predictable rules for palette change (re-map), grid-size change (clear), and regeneration-after-edit (confirm discard).
- Reference overlay + peek work in edit mode; reference can be loaded during free creation.
- Keep the edit core pure and unit-testable in a new `core/editor` package (deps: `model`/`color` only).
- No `.pindou` format change.

**Non-Goals:**
- Undo / redo (tracked `docs/TODO.md` B4).
- Replace-all-color-of-one-code across the grid (tracked B3).
- Crop/extend on size change (size change clears instead).
- Segmentation/mask input, and the render-performance work (tracked G).

## Decisions

### D1: One feature change covering editing, free creation, and reference-on-creation
- **Choice**: Deliver editing (on generated patterns), free creation (blank), and "create from a reference image" (a free-creation sub-feature: load a reference at any time in edit mode) as one change.
- **Rationale**: They share the editor, the tool set, and the reference overlay; splitting would duplicate the editor shell three times.
- **Alternatives**: Separate "editing" and "creation" changes — more artifacts, little independent value.

### D2: Mode-based shell, not a second screen
- **Choice**: Keep a single editor screen; a mode flag switches the shell between **view mode** (current UI: parameter panel, compare chips, tap-to-highlight) and **edit mode** (tool bar, brush color, in-grid drawing). Free creation opens the project already in edit mode. The "完成" action returns to view mode.
- **Rationale**: Reuses `PatternGrid`, the reference overlay and the viewport transform; avoids a parallel screen and duplicated layout.
- **Alternatives**: A separate `EditScreen` route — duplicates grid/gesture/reference code.

### D3: Live-apply edits; generation is one-shot ("方案 A")
- **Choice**: Every edit mutates the in-memory `PindouProject.grid` (via `DocumentStore`); the mutated grid is what gets saved/exported. There is no "commit edits" step. Because generation overwrites the grid, **regenerating from the source image after manual edits asks for confirmation ("会丢弃编辑")** before running.
- **Rationale**: Simplicity and immediate feedback; matches the user's chosen option A.
- **Alternatives**: Keep edits in a separate layer with an explicit merge — more state, no MVP value.

### D4: Editing tools (MVP)
- **Choice**: `画笔` (paint with the currently selected bead color), `橡皮` (set cell to `null` empty hole), `取色器` (eyedropper), `油漆桶` (bucket fill). In the UI each tool is presented as an icon (paint/eraser/eyedropper/bucket) with the active tool highlighted, rather than as text.
- **Rationale**: The minimal set that covers y-mm fixes and motif sketching; icons keep the tool bar compact.
- **Alternatives**: Also add line/rect/shapes, move, copy — deferred.

### D5: Gesture mapping in edit mode
- **Choice**: Single-finger drag = draw/paint along the swept cells (a tap is a degenerate zero-length drag); two-finger pinch = pan/zoom, reusing the existing viewport transform. Cell mapping uses the inverse viewport transform already specified by `pattern-render`. The MVP ships tap + drag painting; pinch pan/zoom. A dedicated **move (移动)** hand tool switches single-finger drags to panning (drawing is disabled while it is active), for users who find two-finger panning awkward while editing.
- **Rationale**: Keeps drawing and navigating on the same surface, matching how the view mode maps taps; the move tool is the release valve for single-finger ergonomics.
- **Alternatives**: A persistent "brush vs pan" mode toggle blocking draw+pinch — extra UI state, worse ergonomics.

### D6: Palette change re-maps all beads
- **Choice**: Changing the palette (in view or edit mode) prompts for confirmation, then maps every non-null cell to the nearest color of the new palette using `ColorSpace.nearestIndex` (dE76/CIELAB); `null` holes stay holes. `paletteUsed` / counts are recomputed from the resulting grid.
- **Rationale**: A pattern authored against one brand's colors should degrade gracefully to the closest available beads.
- **Alternatives**: Clear the grid on palette change — loses all work.

### D7: Grid-size change regenerates or clears by project type
- **Choice**: Projects with a source image regenerate at the new size: silently when untouched, with the existing confirm-discard gate when edited. Projects with no source image (free creation, projects opened from `.pindou`) prompt and then clear to a blank `width×height` of `null` cells. No crop/scale/extend.
- **Rationale**: Regeneration is the honest resize for source-image projects (nothing is lost when untouched, and an edited grid already has a discard gate); source-less projects cannot regenerate, so clearing is the only honest option. Forcing clearing on an untouched generated grid destroyed legit new-size designs.
- **Alternatives**: Always clear with a confirm — the reported problem (untouched generated projects lost their pattern); Nearest-neighbor resample / crop — deferred (complex, lossy).

### D8: Bucket fill is a 4-connected flood
- **Choice**: Fill floods the 4-connected region of cells sharing the target cell's `colorCode` (including `null` for holes) with the selected color; bounded by the grid edges.
- **Rationale**: Standard paint-tool semantics; deterministic and easy to test.
- **Alternatives**: 8-connected — leaks diagonally across intended boundaries.

### D9: Eyedropper source
- **Choice**: On a cell with a bead, the eyedropper picks that bead's `colorCode`. On an empty hole, if a reference image is available it reads the reference pixel color and snaps it to the nearest bead color of the current palette (`nearestIndex`); with no reference it is a no-op.
- **Rationale**: Lets users sample intended colors from the underlying photo, matching the "create from reference" flow.
- **Alternatives**: Only pick existing beads — misses the main use case for reference-guided editing.

### D10: Free creation
- **Choice**: From Home, "自由创作" starts a blank project: choose palette and square size (default **52×52**, default palette **MARD 221**), then open directly in edit mode. The project has no source image (`sourceHash = null`); save/export use the existing `.pindou` schema unchanged. A reference image can be loaded at any time (a sub-feature of edit mode) and is built at **grid resolution** through the shared `ReferenceService` (`preprocess` → `reference`), so it aligns 1:1 like a generated reference.
- **Rationale**: Reuses the generator's reference pipeline and overlay; no new format.
- **Alternatives**: A separate model for blank projects — unnecessary.

### D11: Reference overlay in edit mode
- **Choice**: Edit mode keeps the reference overlay toggle and press-and-hold peek; it hides the separate "图纸" toggle (beads are always shown while editing) and shows only the "参考图" control. View mode keeps its current two-toggle behavior.
- **Rationale**: While editing you need to see beads; the reference is the optional aid.
- **Alternatives**: Keep both toggles — confusing in edit mode.

### D12: Pure edit core in `core/editor`
- **Choice**: Add `core/editor` with pure functions over the immutable `Grid`, returning a new `Grid`: paint/erase a cell, flood-fill, remap-to-palette, and an eyedropper resolver. Dependencies limited to `model` + `color`. The app's `EditingSession` applies these ops to the current project grid and commits each change immediately to `DocumentStore`, so edits show live; per-stroke edits are coalesced in a scratch grid so undo gets one step per drag stroke.
- **Rationale**: Testable without Android; keeps the one-way package rule (`editor → model/color`).
- **Alternatives**: Mutate in the UI layer — untestable and leaks logic into Compose.

### D13: `EditingSession` app-state holder
- **Choice**: Add `state/EditingSession.kt` (edit-mode flag, active tool, selected brush color, cursor cell, edit ops) coordinated with `DocumentStore` (writes the edited project) and `ReferenceService`. `PindouController` exposes it as a thin facade; `GenerationSession`'s regenerate path consults the edit guard before overwriting.
- **Rationale**: Keeps generation and editing concerns separate; both funnel through `DocumentStore`.
- **Alternatives**: Extend `GenerationSession` — mixes two lifetimes and hurts testability.

### D14: Series-grouped color picker, black default
- **Choice**: The full palette is presented grouped by bead **series** — the leading letters of a color code (`A`, `C`, `M`, …). A series selector indexes into the palette; tapping a swatch sets the brush color. The picker opens from the current-color swatch in the tool bar. When edit mode is entered with no brush selected, the brush defaults to the palette color **closest to pure black** (computed, not hard-coded, so it holds for any palette). The default is applied in `EditingSession.enter()`, so it covers both the "编辑" action and free creation. Each series selector shows a representative series color (the average of its swatches) and the selector row has left/right scroll arrows that disable at either end; the current-color swatch always renders so the picker is reachable.
- **Rationale**: A 200+ swatch flat list is unusable; series match how beads are physically organized. Black is the most common default for sketching; distinguishing series visually + arrows make the strip obviously scrollable.
- **Alternatives**: A single scrollable grid of all colors — poor indexing; a search field — extra complexity for MVP.

### D15: Cursor highlight
- **Choice**: While drawing, `EditingSession` tracks the cell under the finger and `PatternGrid` draws a one-cell-sized outline (theme accent over a dark halo for contrast) at that cell; the highlight clears on gesture end or when the finger leaves the grid. This is a non-interactive affordance only (edits already apply live) — it makes the target cell legible under the fingertip.
- **Rationale**: The finger occludes the cell; a cell-sized outline shows exactly what is being edited.
- **Alternatives**: No highlight — the user cannot tell which cell is targeted.

### D17: Undo/redo history and move tool
- **Choice**: `EditingSession` owns an undo/redo history (bounded at 100 entries). Each drag stroke is one undo step (scratch grid accumulates the sweep; the pre-stroke grid is pushed only when the sweep actually changed cells). Palette re-maps and blank resizes push undo entries too. History clears on opening/starting a project and on every regeneration — a regenerated grid is a fresh base, so old undo entries (which restored pre-regeneration grids, at stale sizes) are dropped via an `onRegenerated` hook in `GenerationSession`. A new edit clears the redo branch. `PatternGrid` gains a `moveMode` flag that disables drawing so single-finger drags pan.
- **Rationale**: Without history, small mistakes force a full regenerate (the confirmation gates guard wholesale ops but not an accidental sweep). Stroke-level granularity avoids undo spam on the canvas.
- **Alternatives**: Per-op undo stack — tens of undo taps per stroke; redo of only the last stroke — weaker UX.

### D18: Floating drawing-tool dock + fixed action bar
- **Choice**: The drawing tools (paint, erase, eyedropper, bucket, and future drawing tools) live in a small floating dock that is a sibling overlay on the canvas box and can be **dragged to any position** over the pattern. A transparent gesture catcher fills the dock and consumes every touch inside its bounds (with the grid skipping any gesture whose down press is already consumed), so touching the dock never starts a stroke underneath; the tool buttons sit on top of the catcher and still receive taps. The bottom bar keeps the items that need stable reach: **move (移动), undo, redo**, and the current-color swatch that opens the full series picker. The dock starts at the right-center of the canvas.
- **Rationale**: The feed-back "bottom bar too cramped" pointed to a scaling problem—more tools are planned. A floating dock frees the whole width for the pattern, and dragging it aside lets users put it exactly where their non-drawing hand is. Keeping move/undo/redo/swatch fixed avoids hunting for critical, frequently-used controls. An auto-listing used-color strip was tried in the bottom bar and rejected in review — a growing strip pushed the swatch around and duplicated the color table, so color choice stays in the picker and the color table.
- **Alternatives**: Scrolling bottom rows — width still competes with the pattern; overflow menus — extra taps for common tools. Both rejected in review.

### D16: Peek aware eyedropper (WYSIWYG)
- **Choice**: `GridEditor.pick` gains a `preferReference` flag. During the press-and-hold peek (reference shown alone over the grid), the eyedropper passes it through, so it samples that cell's reference pixel and snaps to the nearest palette color **even when a bead sits there** — matching what the user sees. Without a reference it falls back to the bead code. `EditorScreen` closes over the `peeking` state into the edit callback, so the flag tracks the actual view.
- **Rationale**: Peek exists to compare the photo against beads; sampling by sight is its whole point. Bead-preference would contradict what is on screen.
- **Alternatives**: No peek-aware sampling — user edits against a photo but picks the wrong (overlaid) color.

## Risks / Trade-offs

- [Risk] Per-move immutable `Grid` copies (16k cells at 128) cause stroke jank → **Mitigation**: the stroke scratch buffer means the copy happens once per gesture end, not per swept cell; the dominant cost is the existing full-grid redraw (tracked as TODO G), which editing does not add to. Revisit if profiling shows a need.
- [Risk] Regeneration silently discarding edits → **Mitigation**: D3 confirmation gate + a session flag tracking whether edits exist since the last generation.
- [Risk] Editing amplifies the known full-grid redraw cost (TODO G) → **Mitigation**: rely on the same viewport culling already used; keep edits local; leave deeper render optimization to TODO G.
- [Risk] Palette re-map surprises users (colors change) → **Mitigation**: confirm before applying; `null` holes preserved.
- [Trade-off] Undo/redo restores the grid only, not the generation parameters that produced it → accepted for MVP; a full "revert to pre-edit" also covers regenerate + reset.

## Migration Plan

1. Add `core/editor` pure ops + unit tests (paint/erase/fill/remap/eyedropper).
2. Add `EditingSession` + facade surface; wire `DocumentStore` mutation and the regenerate guard.
3. Add the mode shell + edit toolbar to `EditorScreen`; wire tools/gestures into `PatternGrid`.
4. Add the Home "自由创作" entry (palette/size picker → blank project → edit mode).
5. Reference load-in-edit + grid-resolution build via `ReferenceService`; adjust `grid-reference-image` behavior (hide 图纸 in edit mode).
6. Editor polish: icon tool bar, series-grouped color picker (series = code prefix), black default brush, finger cursor highlight, move tool, undo/redo history.
7. Manual smoke on device `642cc491`; then tests/lint.

**Rollback**: revert the change; existing `.pindou` files remain loadable and no format changed.

## Open Questions

- Whether to ship drag-painting in the first MVP pass or start with tap-to-paint (consensus says tap first, then drag) — resolve during implementation; neither changes the specs.
