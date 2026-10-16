## Why

Users often want a bead pattern of just the **subject** of a photo (a pet, a person, one object), not the surrounding background. Today the whole framed region becomes beads, so background clutter consumes beads and muddies the pattern. This change adds an optional, on-device, class-agnostic "cut out the subject" step before generation, and lays the seams for richer segmentation (semantic / instance) later.

## What Changes

- Add an optional, **off-by-default** "auto cut out subject" step between crop and generation; the choice is remembered for next time.
- Compute a **class-agnostic, single-subject** foreground mask with a bundled on-device model (`u2netp.onnx`, run by ONNX Runtime); the background is excluded from the pattern.
- Hold the mask at the **working-image resolution as the single source of truth**; it drives cell validity, and the reference image derives from it.
- Introduce a **mask-source seam** (with room for prompts) so semantic / instance segmentation can be added later without changing the pipeline.
- **Gate the feature per model**: time a representative inference on first use; if it exceeds a 3 s budget on this device, disable the option (never blocking generation).
- Make preprocessing operations **preserve the validity mask** so it survives to grid sampling.

## Capabilities

### New Capabilities
- `subject-cutout`: the opt-in, on-device, class-agnostic cutout step, its mask-driven behavior, and its per-model device gate.

### Modified Capabilities
- `imaging-pipeline`: cell validity honors an explicit validity mask; preprocessing operations preserve it through to sampling.

## Impact

- `core`: `PixelImage` already carries `mask`; `ImageOps` (contrast / saturation / boxBlur / sharpen) will propagate it; `AreaColor` / `GridSampler` already consume `valid()`.
- `core`: new pure-JVM `MaskSource` / `FloatMask` seam (compose ops, prompted-capable signature) + a single-subject post-process.
- `app` / a small `:ml` seam: an on-device `u2netp` runner implementing the mask source, backed by the ONNX Runtime dependency; the `u2netp.onnx` model asset is shipped in the APK.
- `app`: generation flow gains the cutout step (toggle + preview), a device-capability probe, and mask-aware caching.
- No network access; consistent with the existing "images are processed on the device" privacy note.
- Practical note: preprocessing (and thus the cutout) is invoked from `GenerationSession` on `Dispatchers.Default` with per-stage timing already logged.
