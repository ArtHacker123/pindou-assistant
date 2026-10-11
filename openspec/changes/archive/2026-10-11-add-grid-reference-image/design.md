## Context

Generation already computes, per grid cell, the linear-light average of the source region (`AreaColor.average`, `AreaColor.kt:40`) — but only the snapped bead code survives into `Grid.cells`. The pre-snap averaged color is discarded. This change keeps it as a first-class artifact: a **grid-resolution reference image** (one pixel per cell), produced for free inside the existing per-cell pass.

Two forces make this worth doing now:
1. **Future features need an aligned target raster.** (a) compare source vs. generated pattern; (b) edit a pattern with the original colors as a visual reference; (c) user creation — import a reference image, then hand-edit the bead grid over it. All three want a raster that is *1:1 aligned with the bead grid*, not a full-resolution thumbnail (which would need resampling and would not line up cell-for-cell).
2. **Regeneration cost is dominated by full-image passes that rerun on every parameter change.** The per-cell average (and the `SourceHasher` pass, and preprocessing) are `O(W·H)` and re-executed on every debounced regen (`PindouController.scheduleRegen`), even when only the color count changed. Materializing the reference lets us reuse it across color-count-only changes, turning one of those passes into a cache hit.

The current model (`PindouProject`) is the self-contained `.pindou` document — meta + palette + grid of color codes, no image bytes (D1/D6). This change extends that document carefully: the reference is a large-ish optional addition, so persistence is a design decision, not an afterthought.

## Goals / Non-Goals

**Goals:**
- Produce a grid-resolution reference image (per-cell linear-light average, linear-light correct, coverage/alpha preserved) as a byproduct of generation.
- Guarantee 1:1 index alignment with `Grid.cells` so it can be layered over/under the pattern with zero resampling.
- Store it on the project (in-memory, always) and support **optional** persistence in `.pindou`.
- Add a minimal **compare overlay** in the Form 2 editor to prove the artifact.
- Reuse the reference across regenerations that do not change it (cache key by its true dependencies).

**Non-Goals:**
- Full pattern-editing UX (paint/erase/replace) — separate future change.
- The user "creation from an imported reference" workflow — separate future change.
- Performance work on `SourceHasher` / `toLinear` LUT / lower working resolution — tracked separately; this change only *does not add* cost and enables reference reuse.
- Any change to quantization, snapping, palette mapping, or rendering geometry.

## Decisions

### D1: Artifact shape — grid-resolution `PixelImage`, derived view of a serializable `ReferenceGrid`
- **Choice**: Add an optional field to `PindouProject`:
  - `referenceGrid: ReferenceGrid? = null` — a `@Serializable` value: `width`, `height`, `argbHex` (concatenated ARGB hex, length `w*h*8`, `"00000000"` = transparent).
  - A helper `ReferenceGrid.toImage(): PixelImage` / `PixelImage.toReferenceGrid()` in core.
  The editor/renderer consume the `PixelImage`; serialization uses the hex form.
- **Rationale**: Core must stay Android-independent and unit-testable on the JVM (`PixelImage` is deliberately not `Bitmap`-coupled). `java.util.Base64` is unavailable below API 26 (`minSdk = 24`), and `android.util.Base64` would break core purity — so a **manual hex** encoding avoids both. Hex is inspectable and deterministic, in the spirit of D1 (grid stored as human-readable codes).
- **Alternatives**: (a) `IntArray` serialized directly — verbose (16k JSON numbers, ~2× size, unreadable). (b) base64 — compact but API-26 gated or Android-coupled. (c) store reference as palette indices — wrong, the reference is pre-snap continuous color, not a code. (d) embed a PNG blob — needs Android encoders and re-introduces an image codec into core.

### D2: Production — fold coverage + average into one region pass
- **Choice**: Add `AreaColor.cell(image, gridW, gridH, row, col): CellSample(coverage, rgb)` that walks the region **once** (today `coverage` and `average` each walk it separately — `PatternGenerator.kt:57,64`). Generator builds the reference from these samples: `PixelImage(gridW, gridH, argb)` where each pixel is the averaged ARGB, or **alpha 0** when `coverage < MIN_CELL_VALID` (matches the hole semantics of D12).
- **Rationale**: Producing the reference costs essentially nothing (one pass instead of two) and *reduces* work. Transparent holes let the reference underlay show through exactly where the pattern has holes, matching the hatch rendering.
- **Alternatives**: a separate resize pass over the source — a second full-image walk plus its own resampling/quality questions; reuse of the existing per-cell data is strictly cheaper and already correct (linear light, coverage-aware).

### D3: Alignment contract — same index space as `Grid`
- **Choice**: The reference is exactly `gridW × gridH`; pixel `(col, row)` corresponds to `Grid.cells[row * gridW + col]`. The renderer overlays it using the **same** cell→rect transform it already uses for bead cells.
- **Rationale**: Zero resampling error; a wipe/blend lines up cell-for-cell by construction. Any future editor paints the same index space.
- **Alternatives**: overlay a full-res thumbnail — needs independent registration and drifts against the grid.

### D4: Persistence — embedded by default, with an opt-out
- **Choice**: The `.pindou` format gains the optional `referenceGrid` field, **embedded by default** so an opened file always has the reference (cross-device compare without the source image). Saving/sharing exposes an opt-out ("不含参考图") for users who want the smallest file. In-session the reference is always available regardless.
- **Rationale**: Self-containment is a core value (D6 in the archived design), and compare/edit-from-reference should work when a file travels to another device. The cost is size: the reference roughly triples a 128×128 file (≈131 KB hex vs. a ≈64 KB grid); small grids are negligible, and the opt-out covers the size-sensitive case. Versioning: keep major `1` (decode only checks major, `ProjectSerializer.kt:29`); bump minor to `1.1.0`. Older files decode with `referenceGrid = null`; older apps ignore the unknown field (`ignoreUnknownKeys = true`).
- **Alternatives**: (a) opt-in/default-off — smaller everyday files but loses cross-device compare unless the user remembers to enable it. (b) never embed — contradicts the self-contained ethos. (c) separate sidecar file — breaks single-file sharing.

### D5: Consumption (MVP) — layer visibility (图纸 / 缩略图) + press-and-hold "看原图"
- **Choice**: Two independent toggles in the editor's idle info bar, plus a press-and-hold control:
  - **图纸** — paints the bead layer.
  - **缩略图** — paints the grid-resolution reference as a **base layer** under every cell (holes stay empty).
  - When both are on, beads are opaque and sit on top, so the reference shows through **only where there is no bead**; the two toggles can never both be off.
  - **按住看原图** — while held, forces reference-only (hides the bead layer); release restores the toggles. This is the A/B flicker compare.
  Grid lines, group guides, holes and the selection spotlight draw on top unchanged.
- **Rationale**: A 50% blend of opaque beads over the reference was hard to read (both layers fade). The independent-layer model is exactly what the **future edit/create-from-reference** workflow needs — the reference underlays the cells being edited and beads sit opaquely on top — so this interaction generalizes instead of being throwaway. For the compare use case (all cells beaded), press-and-hold reference-only gives the most legible diff (flicker A/B, as in diff tooling) and costs little.
- **Alternatives**: (a) 50% blend — rejected: muddy, unclear. (b) draggable wipe / side-by-side split — rejected: they divide the screen, so the reference is hidden on the half you are editing (bad for creation; see D3 rationale). (c) bead-opacity crossfade slider — rejected: at intermediate values it is muddy again and it does not give the editing underlay.

### D6: Reuse across regenerations — cache key `(workingImage, imageType, grid size)`
- **Choice**: `PatternGenerator.generate` accepts an optional `reference: PixelImage?`; when its dimensions match `gridW × gridH` it is reused (skipping the region pass) for both the artifact and cell snapping. `PindouController` caches the last reference keyed by `(workingImage identity, imageType id, gridW, gridH)` — everything the reference depends on; it does **not** depend on `colorCountTarget`, palette, seed or quantizer.
- **Rationale**: Changing only the color count currently re-averages the whole image for nothing. Reusing the reference makes that a cache hit and yields the same artifact. Preprocessing and sampling remain per-run (their own caching is future perf work).
- **Alternatives**: recompute always (current behavior) — wastes the most expensive repeatable pass.

## Risks / Trade-offs

- [Risk] Hex encoding roughly triples the 128×128 `.pindou` size when embedded → **Mitigation**: embedded by default for self-containment, with an explicit opt-out; small grids are negligible; a future switch to base64/RLE (behind `ReferenceGrid`) can shrink it without a format break.
- [Risk] A reused reference could go stale if the working image, image type, or grid changes without invalidating the cache → **Mitigation**: the cache key is exactly the reference's dependency set; assert dimensions on reuse and fall back to recompute if they mismatch.
- [Risk] The reference must not be mistaken for the pattern (a user could think the underlay view is the real result) → **Mitigation**: both layers are explicit toggles with a visible on-state; beads render opaque on top, so the pattern always reads as the pattern, and selection/grid chrome still render.
- [Trade-off] Alpha-0 holes make the reference "incomplete" on its own; that is intended — holes are holes in both layers.
- [Risk] Adding a field to a `@Serializable` model with `encodeDefaults = true` would emit `referenceGrid: null` in every file → **Mitigation**: keep the field optional and set the serializer to omit nulls (`explicitNulls = false`), or store it only when opted in.

## Migration Plan

1. Core: add `ReferenceGrid` + `PindouProject.referenceGrid` (optional) + hex codec; `AreaColor.cell`; generator produces/reuses the reference; adjust `Json` config to not emit nulls.
2. App: controller stores the reference and caches it by key; editor adds the compare toggle.
3. Existing `.pindou` files and tests are unaffected (field `null`, unknown-key tolerant). No data migration; rollback is deleting the field (old files remain valid).
4. Sync `openspec/specs/grid-reference-image` (new) and the `pattern-share` delta; then archive.

## Open Questions

- ~~Compare interaction: is the 50% blend toggle enough, or is a draggable wipe worth the extra UI now?~~ → **Resolved**: replaced the blend with layer visibility (图纸 / 缩略图) + press-and-hold "看原图" (D5) after the blend proved hard to read; a draggable wipe is still rejected for creation (it splits the screen) and remains out of scope.
- ~~Persistence default: default-OFF with opt-in, or embed by default?~~ → **Resolved (D4)**: embedded by default, with an opt-out for smaller files.
- ~~Should reference reuse (D6) land in this change, or split into the performance change?~~ → **Resolved**: included in this change.
