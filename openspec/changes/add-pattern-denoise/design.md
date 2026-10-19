## Context

A photo pattern is produced in two stages: `ReferenceDirectStrategy` picks the bead set (uncapped/automatic by default), then `PatternGenerator.generate` snaps **each cell independently** to its perceptually nearest bead (`PatternGenerator.kt:79-92`). Nothing in the pipeline looks at the grid *spatially*. Photos carry gradient and sensor/JPEG noise, `AreaColor.cell` averages a cell's region in linear light (mixing two colours into a third at boundaries), and photo preprocessing is only a weak `boxBlur(radius = 1)` (`ImageProcessorRegistry.kt:46-50`). The result is speckle: isolated cells and tiny blobs of many distinct codes.

The editor already has reusable connected-region machinery (`GridEditor.floodFill`, `MaskPostProcess`), and the project already reasons in Oklab everywhere (`ColorSpace`, `OklabColorSpace`), so a perceptual distance gate is idiomatic here.

## Goals / Non-Goals

**Goals:**
- Remove spatial speckle from a snapped grid, mainly for photos, so a pattern uses fewer codes and is practical to bead.
- Preserve small but important features (eyes, highlights, thin outlines) — never eat a high-contrast detail.
- Never touch empty holes (`null`); never introduce a bead colour that is not already in the pattern.
- Deterministic (pure function of the snapped grid + options), cheap (grid ≤ 128×128 = 16 384 cells).
- User-controllable: a switch plus 弱/中/强 strength, default on · 中, persisted.

**Non-Goals:**
- Changing preprocessing or the reference image (the reference stays the smooth pre-denoise average).
- Changing colour *selection* (the greedy coverage / K-means stages are untouched).
- Multi-scale / mean-shift / superpixel segmentation, or a bilateral filter on the source.
- Cleaning cartoon line art differently (the colour gate makes the same rule safe for cartoons).

## Decisions

### D1: Run after snapping, in `PatternGenerator`, not in preprocessing or selection
The speckle is a property of the *snapped grid*, and its cause (independent per-cell nearest-bead) can only be corrected there. Preprocessing a blur would damage the source and the reference image; the selection stage only chooses a colour *set* and cannot fix spatial distribution. So the pass runs between snapping and `Grid` construction, and `paletteUsed` (already recomputed from the final grid) shrinks for free.
- *Alternatives*: stronger `boxBlur` in the photo preset (blurs detail, changes the reference); a mode filter inside the strategy (wrong layer, no spatial data).

### D2: Merge is gated by a perceptual colour distance, not by area/frequency alone
A naive 3×3 majority filter erases a 1-wide outline (each line cell has 6 background neighbours) and a 1-cell eye (all 8 neighbours identical), so it is rejected. Instead a candidate region/cell is merged **only when its colour is perceptually close to the colour it would merge into**. Noise and gradient steps are close (merge); an eye or outline is far (keep). This mirrors `ReferenceDirectStrategy`'s existing "keep rare but distinct colours" philosophy.

### D3: One pass, gated by colour distance
Find 8-connected regions of one code. If `area ≤ maxArea`, look at the set of colours in the surrounding cells; take the perceptually nearest non-hole surrounding colour; merge the whole region to it **iff** that distance `≤ maxDelta`. Otherwise keep the region unchanged.

A separate isolated-cell pass was originally planned but is redundant: a lone cell is simply the smallest region (area 1), and the pass already handles it. Regions are read from a snapshot and processed **smallest first**, which also prevents two adjacent small regions from swapping colours (mutual attraction): the smaller one is absorbed into its larger neighbour first.

### D4: Merge target and tie-breaks are deterministic
- Target colour = the perceptually nearest colour among the region's/cell's surrounding (8-neighbour) non-hole colours.
- Ties (equal Oklab distance) break by natural colour-code order, matching the rest of the pipeline.
- Because the target is always a colour already on the grid, the pass never invents a colour and only ever *reduces* the distinct-code set.

### D5: Strength maps to (maxArea, maxDelta)
A `DenoiseStrength` with three levels (plus OFF). Initial thresholds in Oklab Euclidean distance and region area:

| level | maxArea | maxDelta |
|-------|---------|----------|
| OFF   | –       | applied only as off (no pass) |
| 弱    | 1       | 0.03 |
| 中 (default) | 2 | 0.06 |
| 强    | 4       | 0.10 |

Values are initial and expected to be tuned against the tests/prototype; the *shape* (area grows with strength, delta grows with strength) is the decision.

### D6: Empty holes are inert
`null` cells are never candidates and never targets. A region touching holes only considers non-hole neighbours; a region with no non-hole neighbour is kept.

## Risks / Trade-offs

- **Eats a legitimately small distinct feature** → the colour gate is the mitigation, and it is regression-locked by tests (an isolated eye cell and a 1-wide line must survive at every strength). If a feature's colour is genuinely close to its background, merging it is visually correct anyway.
- **Default-on changes existing outputs** (patterns generated before this change look cleaner now) → accepted; the switch lets a user turn it off, and the reference image is unchanged. No persisted format changes, so old `.pindou` files open unchanged.
- **Thresholds feel wrong on some photos** → three strengths plus OFF; thresholds are isolated constants so they can be retuned without touching the algorithm.
- **Determinism/order dependence** → snapshot-based passes with natural-code tie-breaks; a test asserts two runs are identical.

## Open Questions

- Whether the strength control belongs in the 高级 section next to 取色方式/自动抠出主体 (assumed) or as a top-level option — assumed 高级.
- Exact numeric thresholds (tunable without an interface change).
