## 1. Mask seam (core, no ML)

- [x] 1.1 Add a pure-JVM `FloatMask` value (width, height, `FloatArray` in 0..1) with compose operators (`union`, `intersect`, `difference`)
- [x] 1.2 Add a `MaskSource` interface (`encode(image): Handle`, `decode(handle, prompts): FloatMask`) with a `Prompts` placeholder (empty for saliency, points/boxes later)
- [x] 1.3 Add a single-subject post-process: threshold the probability map and keep the largest connected component
- [x] 1.4 Make `ImageOps` propagate `image.mask` on every output (contrast, saturation, boxBlur, sharpen); verify square padding already maps the mask
- [x] 1.5 Attach the mask to the working image before sampling; confirm `AreaColor` / `GridSampler` validity honors it end-to-end
- [x] 1.6 Unit tests with a fake `MaskSource` (e.g. a disk): a cell fully inside, fully outside, and partially overlapping reports the right coverage/hole; the reference image reflects the mask; a preset op preserves the mask

## 2. Saliency model (on-device)

- [x] 2.1 Add the `u2netp.onnx` model asset to the APK and record its origin/license (Apache-2.0) — done; see `design.md` D7 for provenance and the pinned SHA-256
- [x] 2.2 Add the single inference runtime dependency (ONNX Runtime for Android) and implement a `SaliencyMaskSource` (resize to the model input, normalize, upsample to working resolution)
- [x] 2.3 Wire the source into the generation flow (after crop, before preprocessing)

## 3. Flow and UI

- [x] 3.1 Add the subject-cutout step after crop: an off-by-default toggle and a preview of the cut-out subject
- [x] 3.2 Persist and restore the user's last choice as the default
- [x] 3.3 Device-capability probe: warm up + time a representative inference on first use; persist per-model availability; disable the option with a message when over 3 s; enforce a 3 s runtime timeout that falls back to no cutout

## 4. Caching

- [x] 4.1 Include the mask in the generation cache key (`ReferenceKey`) so re-generating the same image does not re-run the model

## 5. Verification

- [x] 5.1 `:core:test :app:assembleDebug` passes
- [x] 5.2 Install the APK and have the user verify: cutout off → unchanged output; on → background removed with a correct preview and the choice remembered; regeneration is fast (mask cached); on a slow/emulated device the option is disabled with a message and generation still works
- [x] 5.3 Re-validate the change `--strict`
