## Why

Generation already computes, for every grid cell, the linear-light average color of the source region (`AreaColor.average`) but throws it away — only the snapped bead code is kept. Retaining that averaged raster as a **grid-resolution reference image** unlocks three future-facing features (source-vs-pattern comparison, editing a pattern against its target colors, and user creation from an imported reference) and makes regeneration cheaper to reason about, at essentially no extra compute.

## What Changes

- Generation additionally emits a **grid-resolution reference image**: one pixel per cell, each holding the pre-snap averaged color of that cell's source region, computed in linear light and preserving coverage (transparent cells stay transparent).
- The reference image is stored on the generated project (in-memory) and is **1:1 aligned** with the bead grid, so it can be layered directly under/over the rendered pattern with no resampling error.
- The editor gains a **compare overlay**: toggle/wipe between the reference layer and the bead-color layer to visually judge fidelity.
- The `.pindou` project file **carries the reference grid by default** (with an opt-out for smaller files) so an opened file can show the reference and compare even without the original source image (extends the existing "self-contained" story). Format gains a version bump + optional field.
- The reference image becomes a **cacheable artifact** keyed by `(preprocessed image, grid size)` — independent of the color count — so parameter changes that only affect color count reuse it.

## Capabilities

### New Capabilities
- `grid-reference-image`: producing, storing, aligning and consuming a grid-resolution linear-light averaged reference image (compare overlay; editing reference; creation-from-reference groundwork).

### Modified Capabilities
- `pattern-share`: the `.pindou` project file embeds the grid reference image by default (versioned, backward-compatible, optional opt-out) so opened files remain viewable/compare-able without the source image.

## Impact

- **core/generate**: `PatternGenerator.generate` produces the reference `PixelImage` (reusing the existing per-cell pass); `PindouProject` gains a reference field.
- **core/model + serialize**: `PindouProject` / `ProjectSerializer` gain an optional reference field + version handling (backward compatible: older files load with `null`).
- **app/PindouController**: stores the reference alongside `project`; caching/invalidation keyed by `(workingImage, imageType, grid)`.
- **app/ui/editor**: compare overlay control (toggle / wipe) over `PatternGrid`.
- **Non-goals (future changes)**: full pattern editing UX, and the user "creation from reference" workflow; this change only lays the artifact + a minimal compare overlay.
- No new dependencies.
