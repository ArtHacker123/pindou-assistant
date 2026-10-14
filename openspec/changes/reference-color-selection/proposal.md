## Why

Today the pattern's usable bead colors are chosen by running K-means over the full-resolution working image, which is indirect: the grid cells are already averaged into the grid-resolution reference image, yet the color set is decided from raw pixels that never end up in the pattern. Selecting colors straight from the reference cells makes the used beads track what the pattern actually shows, is deterministic, and is faster. The reference image is already computed and used for per-cell snapping, so this only changes how the used bead set is chosen.

## What Changes

- Add a **reference-direct** color-selection strategy: map each reference-image cell to the perceptually nearest bead in the full palette; when automatic, keep every bead that appears (no cap); when a manual color count N is set, choose the N beads by **greedy maximum coverage** (the beads that most reduce the cells' total perceptual error), so rare but visually distinct colors are not dropped. **This becomes the default.**
- Keep the existing K-means strategy as a selectable alternative (not removed; may be deleted later if the new one proves better).
- Add a color-strategy selector to the generation parameters and remember the choice alongside the other generation parameters.
- Redefine "automatic" color count for the default strategy as "use all beads that appear" (no cap).
- Generalize the shared cell-mapping wording so it no longer assumes a representative-center indirection.

No breaking change to saved `.pindou` files: the recorded `colorCountTarget` keeps its meaning (0 = automatic), and existing projects keep working with the K-means strategy available.

## Capabilities

### New Capabilities
<!-- none: this extends existing color selection -->

### Modified Capabilities
- `color-quantization`: introduce pluggable color-selection strategy with reference-direct as the default, keep K-means selectable, and make automatic mean "no cap" for the default strategy.
- `bead-grid`: describe cell color mapping as snapping to the chosen bead set rather than mapping through representative centers.
- `imaging-pipeline`: reuse the shared per-cell samples for color selection, not only snapping and reference production.

## Impact

- **Core**: `generate/PatternGenerator.kt` (strategy branch), `generate/GenerateConfig.kt` (strategy + auto flag), a new color-selection component under `core` (reference-direct), `match/PaletteMapper` (nearest-bead helper reused).
- **App**: `state/GenerationSession.kt` (pass strategy/auto; keep params), `PindouController` (expose strategy selection), the generation-parameters UI (new selector), `ProjectMeta` persistence convention (0 = automatic).
- **Tests**: new core tests for the reference-direct strategy (no-cap and top-N), plus parameter round-trip.
- No dependency, permission, or manifest change.
