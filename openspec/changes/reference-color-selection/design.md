## Context

Generation (`PatternGenerator.generate`) currently has two responsibilities: (1) pick the set of bead colors the pattern may use — by sampling the full-resolution image and running K-means to get representative colors, then mapping them to beads; and (2) assign each grid cell a bead — by averaging the cell's source region into the grid-resolution reference and snapping each average to the used set.

The reference image (one pixel per cell, linear-light average) is already computed before generation and passed in (`ReferenceService.reference` → `GenerateConfig` → `PatternGenerator`), and the per-cell averages are already the input to cell snapping (via `GridSampler.fromReference`). So the color set is currently derived from pixels that never appear in the output, while the colors that *do* appear (the cell averages) are only used after that.

`colorCountTarget` is already stored in `ProjectMeta`, with `0` meaning "automatic" (a convention established by the recent color-count fix). Generation parameters live in a single in-memory session (`GenerationSession`) and are restored from meta when a project is opened.

## Goals / Non-Goals

**Goals:**
- Choose the pattern's bead colors directly from the reference-image cells (the colors the pattern actually shows), making **reference-direct** the default strategy.
- Keep the existing K-means strategy selectable; do not delete it.
- Automatic color count = "no cap" for the default strategy.
- Deterministic output without a seed for the default strategy.
- Keep saved `.pindou` files working (additive, backward-compatible metadata).

**Non-Goals:**
- Removing K-means or the `Quantizer` interface.
- Changing the reference image itself, preprocessing, or cell snapping math.
- Persisting generation parameters across process death (unchanged: only meta restores params).
- A new color-similarity metric (reuse CIELAB `nearestIndex`).

## Decisions

### 1. Introduce a pluggable `ColorStrategy` alongside the `Quantizer` registry
- A `ColorStrategy` interface (`id`, `displayName`, `select(image, gridSamples, palette, config): List<PaletteItem>`) with a registry, mirroring the existing `Quantizer`/`QuantizerRegistry` style.
- Implementations: `ReferenceDirectStrategy` (`id = "reference"`, new **default**) and `KMeansStrategy` (`id = "kmeans"`, wraps the current sample → quantize → mapCenters path).
- **Why over a boolean flag / inline `when`**: matches the codebase's pluggable-stage convention, keeps `PatternGenerator` free of algorithm branches, and makes "delete K-means later" a one-line registry change.
- `PatternGenerator` computes `gridSamples` first (from the reusable reference or by sampling), then asks the strategy for the used beads, then snaps cells as today. The sampler/quantizer/mapper move into `KMeansStrategy`.

### 2. Reference-direct algorithm
- Uncapped (automatic): for each valid cell, find the nearest bead in the full palette; the used set is the distinct beads that appear. Snapping every cell back to that set is a no-op (it already contains each cell's global nearest), so the pattern is exactly "each cell → its nearest bead".
- Capped (manual N): choose N beads by **greedy maximum coverage**. Because the final pattern snaps every cell to the nearest chosen bead, the objective is the downstream error: minimize the total perceptual (squared CIELAB) distance from each cell to its nearest chosen bead. Cells are grouped by their nearest full-palette bead (each group carries a weight and a Lab centroid) so the greedy runs over ≤ palette-size candidates instead of every cell. The first pick is the weighted medoid; each further pick is the bead that most reduces the remaining total error. Cell weights use `count^γ` with **γ = 0.5**, so small areas carry relatively more weight — this biases toward rare key colors while still respecting area. Finally every cell is re-mapped to the nearest bead within the chosen set.
- **Why maximum coverage over frequency**: a pure frequency ranking drops small but distinct colors (eyes, outlines, highlights) even though dropping them costs a large error. Maximum coverage keeps them when they are far from the already-chosen beads, and only merges colors that are genuinely close. Alternatives considered: farthest-point / k-center (maximizes spread but may spend slots on outliers/noise and ignores area), K-means on cell colors (seed-dependent and can collapse on mapping), frequency + a distance threshold (threshold is arbitrary and still area-led).

### 3. Determinism
- Reference-direct needs no seed: ties in frequency are broken by natural `colorCode` order, so the same input always yields the same set. K-means keeps using `config.seed`.

### 4. Automatic color count is strategy-dependent
- `GenerateConfig` gains `colorStrategy: String` and `colorCountAuto: Boolean`.
- `reference` + auto → uncapped; `reference` + manual N → top-N; `kmeans` + auto → `AUTO_COLOR_COUNT` (16, unchanged); `kmeans` + manual N → N.
- Persistence keeps `ProjectMeta.colorCountTarget` (0 = automatic). `ProjectMeta` gains `colorStrategy: String` defaulting to `"reference"`.

### 5. Retain and expose K-means
- Add a "取色方式" dropdown to the generation parameters panel (`ParamsPanel`) with 参考图直取（默认）/ K-means 聚类, wired like the other param dropdowns (confirms dirty-edit regen, remembered in the session).

## Risks / Trade-offs

- **Default change alters existing generation results** → Intended. Verified the existing `PatternGeneratorTest` cases (banded colors, invalid holes, reference emission, reproducibility, reference reuse) still hold under reference-direct; the reproducibility test is pinned to a strategy explicitly to stay meaningful.
- **Top-N by frequency can drop rare, visually important colors (e.g. eyes/highlights)** → Replaced by greedy maximum coverage with `count^0.5` weights, which keeps distinct small colors (see Decision 2). Automatic is uncapped by default, so capping is always the user's explicit choice.
- **Old `.pindou` files lack `colorStrategy`** → Field defaults to `"reference"`, so they open and regenerate with the new default; no data loss and no deserialization error.
- **`GridSampler.fromReference` treats any opaque cell as fully valid** → Pre-existing behavior, unchanged by this work.

## Migration Plan

- Additive, no migration: `ProjectMeta.colorStrategy` has a default, so old files deserialize unchanged.
- Rollback: switch the registry default back to `"kmeans"` (single line) — no data changes needed.

## Open Questions

- None blocking. (Future: whether to delete K-means once reference-direct is validated in use — deliberately deferred per the request.)
