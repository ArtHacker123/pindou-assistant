## Context

Subject cutout (change `add-subject-cutout`) produces a soft probability mask at the working-image resolution, then reduces it to a per-pixel validity mask. Today that reduction is hardcoded in `SaliencyMaskSource.decode` and `CutoutService.cutout`:

```
soft = SaliencyPreprocess.toWorkingMask(saliency, 320, w, h)
keepLargestComponent(soft, threshold = 0.5)     // one blob, 4-connected
image.withMask(soft.toBooleanMask(threshold = 0.5))
```

There is no way to correct two common failure modes: **background residue** (salient background survives) and **subject erosion** (thin subject parts fall below the threshold and are dropped). Morphology can address both, but it must be added without reworking the model, the `MaskSource` seam, or the generation pipeline.

The mask pipeline is the natural extension point: thresholds, connectivity, subject selection, and morphology are all transforms on a `FloatMask`.

## Goals / Non-Goals

**Goals:**
- Make the mask post-process data-driven so behavior is a value (`MaskOptions`), not code.
- Add reusable morphology operators (`erode` / `dilate` / `open` / `close`) on `FloatMask`.
- Offer three user presets that map onto the two failure modes plus the current behavior.
- Keep the change additive: existing `MaskPostProcess` callers and tests keep working.

**Non-Goals:**
- Changing the model, its input size, or the normalization contract.
- Changing the `MaskSource` encode/decode signature or the generation flow.
- Per-pixel interactive mask editing or prompted segmentation (seam exists, unused here).
- Aspect-preserving resize and multi-subject retention are noted as future levers, not built here.

## Decisions

### D1: One `MaskOptions` value object + an ordered operator list
All knobs live in a single data class with defaults equal to today's behavior. `MaskPostProcess.process(mask, options)` builds an ordered list of `(FloatMask) -> FloatMask` steps and folds the mask through it. Adding a new step is one line; adding a knob is one field with a default, so no call site breaks. Alternatives considered: (a) more parameters on `keepLargestComponent` — grows unwieldy and leaks into call sites; (b) a DSL — overkill for this stage.

### D2: Morphology as pure `FloatMask` functions in `core`
`erode`/`dilate` run a min/max filter over a square neighborhood of a given radius; `open` (dilate∘erode) and `close` (erode∘dilate) are compositions. Grayscale (soft-map) morphology is used directly on the probability map, so faint subject edges are preserved rather than cut at a hard threshold first. Kept in `core` (no Android) so it is JVM-unit-testable, matching `ImageOps`/`MaskPostProcess`.

### D3: Options ride on the source, not the seam
`MaskSource.decode(handle, prompts)` is unchanged. `SaliencyMaskSource` gains a `@Volatile var options: MaskOptions`; `CutoutService` sets it before submitting work and uses it for `toBooleanMask`. This keeps the extensible seam stable and localizes the change to two files.

### D4: Presets are named `MaskOptions`, persisted by name
Three presets:
- **Standard** — `MaskOptions()` (today's behavior).
- **Tighten background** — higher `foregroundThreshold`, `openRadius = 1`, `SubjectMode.LARGEST`.
- **Preserve subject** — lower `foregroundThreshold`, `closeRadius = 1`, `MaskConnectivity.EIGHT`.

The chosen preset is stored as a string in the existing `CutoutPrefs` file; unknown/missing values fall back to Standard, so future presets are additive.

### D5: Preserve the old entry point
`keepLargestComponent(mask, threshold)` stays as a thin wrapper over the new subject-reduction step, so existing tests and any caller are untouched. Only the decode path moves to `process(mask, options)`.

### D6: Operator order — open, close, subject
`process` applies, in order: **open** (remove background speckle), then **close** (fill holes inside the subject), then subject reduction (connectivity + subject mode). Opening before closing follows the standard "remove small objects, then fill small holes" convention; subject reduction runs last so morphology cannot resurrect a discarded region.

## Risks / Trade-offs

- **Morphology cost at working resolution** → Caps/radii are tiny (0 or 1 by default) and the operators are simple min/max passes; the existing `cutPre/cutRun/cutPost` timing will confirm. If it ever dominates, the reduction can run at the model's 320×320 and upsample.
- **Three interacting thresholds** (`foregroundThreshold`, `hardThreshold`, `MIN_CELL_VALID`) → Presets set them coherently; defaults keep all three at their current values so Standard is a no-op.
- **Grayscale vs binary morphology** → Operating on the soft map is intentional (preserves soft edges) but means a large radius can smear; presets keep radii ≤ 1 and the diameter/erosion is documented on the operators.
- **Preset may not fit every image** → Only three coarse choices; fine per-image tuning is out of scope. Presets are data, so more can be added without code changes to callers.
