## Why

The editor's default brush color is not palette-aware. It is derived once, only when no brush is selected, and is never re-derived when the palette changes — so after switching palettes the brush keeps the previous palette's code (MARD's `H7`, which looks like a foreign code on a Perler/Hama pattern and may not even exist in the new palette). Separately, the "darkest" heuristic (`min r+g+b`) picks a dark green for Perler (`80-15261` Dark Spruce) instead of the bead actually named Black (`80-19018`). Users expect the default to be *black* and to resolve to the current palette's own black bead.

## What Changes

- Define the default brush bead as: the palette bead whose name is `Black` (case-insensitive) if one exists, otherwise the palette color nearest to pure black.
- Resolve the default brush against the current palette every time edit mode is entered, and re-resolve it after an in-editor palette change (remap), so the default always belongs to the palette in use.
- Keep the rest of the picker behavior unchanged (choose from the full palette, grouped by series).

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `pattern-editing`: the "Bead color picker grouped by series" requirement is refined — the default brush is the palette's `Black` bead (falling back to the color nearest pure black) and is resolved per palette whenever edit mode is entered or the palette is remapped, not only when no brush has ever been chosen.

## Impact

- `core/.../palette/PaletteDefaults.kt` — add a `defaultBrushItem(palette)` helper (testable, shared with GenerationSession-style defaults).
- `app/.../state/EditingSession.kt` — `defaultBrushCode` delegates to the helper; `enter()` and `remapTo()` re-resolve the brush from the current palette.
- `core/src/test/.../palette/` — a test covering the Black-by-name and nearest-black fallback.
- No data, model, pipeline, or persistence changes; existing `.pindou` documents are unaffected.
