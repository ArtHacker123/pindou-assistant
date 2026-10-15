## Context

`EditingSession.defaultBrushCode(project)` picks the bead with the smallest `r + g + b` and assigns it to `brushCode` only the first time (`enter()` guards with `brushCode == null`). Two problems follow:

1. The choice is never revisited when the palette changes (a new generation, a palette remap in the editor, or an opened `.pindou`), so a stale code from another palette lingers.
2. `min r+g+b` is a poor proxy for "black": for Perler it selects `80-15261` Dark Spruce `(20,49,59)` over the actual Black `80-19018 (50,50,52)`.

Multi-brand palettes make both visible: the default no longer matches the palette in use.

## Goals / Non-Goals

**Goals:**
- The default brush is black and belongs to the palette currently in use.
- Long codes / foreign codes never appear as the default on another palette.

**Non-Goals:**
- Changing how the user chooses a brush (still full-palette, grouped by series).
- Preserving a user's chosen brush across palette changes (a palette change re-derives the default).
- Any palette data changes.

## Decisions

### D1: Prefer the bead named `Black`, else nearest to pure black
Add `PaletteDefaults.defaultBrushItem(palette): PaletteItem?`:
- return the first item whose `name` equals `Black` ignoring case and surrounding whitespace, if any;
- otherwise return `items.minByOrNull { r*r + g*g + b*b }` (nearest to pure black).
- *Alternatives*: keep `min r+g+b` (mis-picks Perler); hardcode a code per brand (fragile, and MARD has no names at all so a fallback is still needed).

### D2: Re-resolve the default on entry and on palette remap
`enter()` sets `brushCode = documents.project?.let { defaultBrushCode(it) }` unconditionally (not just when null), and `remapTo(palette)` re-derives after the palette swap. `enter()` is only invoked from the explicit "编辑" action and from blank/free-creation entry, so this does not fight an in-progress session; the user's choice is kept while editing.
- *Alternatives*: track the palette name and only reset on change (also correct, but more state; since `enter()` is a discrete user action, always resolving keeps the rule simple and matches "entering edit mode defaults to black").

### D3: Keep the helper in `core`
`defaultBrushItem` lives beside `DEFAULT_NAME` in `core`, so it is unit-testable in `:core:test` and reusable, while `EditingSession` (app) just delegates.

## Risks / Trade-offs

- [Re-entering edit mode discards a previously picked color] → acceptable and intended ("default on entry"); the picker is one tap away and the palette-consistent default is the more important guarantee.
- [Name casing/locale] → compare with `trim().equals("Black", ignoreCase = true)`; palettes without a named Black fall back to nearest-black, which already covers MARD.
