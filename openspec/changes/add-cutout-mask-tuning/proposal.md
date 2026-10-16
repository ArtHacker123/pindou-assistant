## Why

The subject-cutout mask is produced by a hardcoded post-process (a single 0.5 threshold plus "keep the largest region"), so when the model's saliency is imperfect there is no way to adapt: background pixels left behind ("background residue") or thin subject parts dropped ("subject erosion") cannot be corrected by the user. This change makes the post-process data-driven and adds morphological operations, then surfaces three presets that map directly onto those two symptoms — without touching the model, the `MaskSource` seam, or the generation pipeline.

## What Changes

- Replace the hardcoded mask post-process with a **configurable, composable pipeline** driven by a `MaskOptions` value object (foreground/hard thresholds, connectivity, subject mode, open/close radii).
- Add pure-JVM **morphology operators** (`erode`/`dilate`/`open`/`close`) that operate on `FloatMask`, so a new step is one line in the pipeline (extensibility point).
- Add three **cutout presets** shown next to the cutout toggle and remembered as the default:
  - **标准 (Standard)**: current behavior.
  - **收紧背景 (Tighten background)**: higher foreground threshold + opening + single largest subject — removes residue.
  - **保留主体 (Preserve subject)**: lower threshold + closing + 8-connectivity — keeps thin/eroded subject parts.
- Keep the existing `MaskPostProcess.keepLargestComponent(mask, threshold)` signature as a thin wrapper so current tests and callers are unaffected.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `subject-cutout`: mask post-processing becomes configurable (thresholds, connectivity, subject mode, morphology) and offers three user-selectable presets; the mask-source decode honors the selected options.

## Impact

- `core`: new `MaskOptions` value object and `MaskFilters` (erode/dilate/open/close); `MaskPostProcess` gains a `process(mask, options)` pipeline while keeping the old entry point as a wrapper.
- `core`: new unit tests for morphology and the pipeline; existing mask tests unchanged.
- `app`: `SaliencyMaskSource` carries a `MaskOptions` (no change to the `MaskSource.decode` signature); `CutoutService` passes the options through; `CutoutPrefs` persists the chosen preset; `GenerationSession` exposes the preset; `EditorScreen` adds a compact preset selector next to the toggle.
- No new dependency, no model change, no network access.
