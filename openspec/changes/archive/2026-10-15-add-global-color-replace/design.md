## Context

Edit mode already supports paint / erase / eyedropper / bucket / move over the immutable `Grid`, with every edit committed straight to `DocumentStore` and a bounded undo/redo history. `GridEditor` is the pure, Android-independent seam (`model` + `color` only), and `DocumentStore.replaceGrid` already recomputes `paletteUsed` from the grid and preserves a size-aligned reference image. Palette re-map (`GridEditor.remap`) exists but maps to the *nearest* color of another palette — it is not an exact `X → Y` substitution.

## Goals / Non-Goals

**Goals:**
- Let the user replace every bead of one color code with another code in a single action.
- Keep it consistent with the existing tool dock and undo/redo model.
- Zero changes to persistence, navigation, or the image pipeline.

**Non-Goals:**
- Replacing holes (bulk-fill empty cells) — a separate idea.
- Cross-palette nearest-color replacement (that is `remap`, already shipped).
- Partial/region-scoped replacement (the bucket and region erase already cover regions); this is strictly global by code.

## Decisions

### D1: A pure `GridEditor.replaceAll(grid, from, to)`
Exact code match, holes untouched, returns the *same* instance when nothing changes. Symmetric with `paint`/`floodFill` and unit-testable without Android.
- *Alternatives*: reuse `remap` with a one-color target — rejected: `remap` is nearest-distance over a whole palette and cannot express "this code only"; a UI-layer cell loop — rejected: breaks the pure-core convention and is untestable in isolation.

### D2: Entry point — a new 替换 tool in the floating dock
Target = the **current brush color** (already shown in the bottom bar); source = the **tapped cell's code**. Rationale: fits the existing tool mental model and direct-manipulation canvas; the brush color is always visible/preset.
- *Alternatives*: long-press a used-color swatch (hidden; conflicts with the used-color row's "tap = pick brush" behavior); an info-bar "全部替换为…" button with a second palette pick (more taps, needs the picker to support a "target" mode).

### D3: Apply immediately, no confirmation; one undo step
`EditingSession.replaceColor` pushes one undo entry and commits to the document at once. Undo fully reverses it, so a confirmation dialog is unnecessary friction.
- *Trade-off*: a mis-tap can mass-edit a large grid — mitigated by immediate, single-step undo and the always-visible active-tool label.
- *Note*: unlike palette change / resize / regenerate (which confirm because they are destructive or discard edits), replace is trivially reversible.

### D4: Tap-only tool
Like bucket and eyedropper, replace edits on a single clean tap and lets a drag pan/zoom (covered by the existing "tool-appropriate drag behavior" rule). Reuses the existing gesture classification (`canDraw`/`dragDraw`).

### D5: No-op guards
If the tapped cell is a hole, or the brush color is unset, or the source code equals the brush code, the tap does nothing (no undo entry). The target is always a palette code because it comes from the brush.

## Risks / Trade-offs

- **Accidental mass edit** → immediate single-step undo + the info bar names the active tool.
- **Stale `paletteUsed`** → not a risk: `DocumentStore.replaceGrid` recomputes it from the resulting grid.
- **Reference misalignment** → not a risk: grid size is unchanged, so the size-aligned reference is preserved.
