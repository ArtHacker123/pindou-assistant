## Context

Image preprocessing is a `ProcessorPipeline` of `ImageOp`s (`core/imaging`), applied to a working `PixelImage` (pure JVM, packed ARGB + optional `mask: BooleanArray?`). Downstream, `GridSampler` / `AreaColor` fold each cell to a linear-light average + coverage and mark a cell a hole when coverage < `MIN_CELL_VALID` (0.5). `PixelImage.valid(x, y)` already prefers an explicit `mask` and falls back to `alpha >= 8`, and its doc comment states a future segmentation can supply a mask "without touching the pipeline". Generation runs in `GenerationSession.runGenerate` on `Dispatchers.Default` and logs per-stage timings.

Two facts shape this change:
- The **mask seam already exists** downstream — segmentation's whole job is to produce a mask; `valid()` / coverage already consume it.
- But the current `ImageOp` implementations construct `PixelImage(w, h, out)` **without** the input mask, so an attached mask is dropped by the preset operations today.

## Goals / Non-Goals

**Goals:**
- Opt-in, class-agnostic, single-subject cutout before generation; background excluded from the pattern.
- Keep the whole feature on-device and offline.
- A seam that lets semantic / instance (prompted) segmentation slot in later without pipeline changes.
- Degrade gracefully: on devices where the model is too slow, the option is simply unavailable.

**Non-Goals (this change):**
- Detection models, object categories, or class-aware "keep only X".
- Instance segmentation / tap-to-select-multiple subjects (the seam is designed for it, but the first version keeps one subject).
- A manual mask brush (a later editor tool; it will edit the same mask buffer).
- Background restoration / compositing (background is simply excluded).

## Decisions

### D1: Class-agnostic saliency, single subject — no detector
Use a salient-object model (`u2netp`, the "portable" U²-Net variant) that outputs a foreground probability map; it is category-free, which matches "remove background". A single subject is obtained by keeping the **largest connected component** of the thresholded map, so multiple salient regions still yield one subject without any detector. A detector is only needed later for multi-subject choice / categories / box prompts, and is deliberately out of scope. (Note: full U²-Net is ~176 MB — not viable; `u2netp` at 4.36 MB is the mobile choice.)

### D2: Mask via `PixelImage.mask` (approach B), not alpha baking
The mask is attached as `PixelImage.mask` rather than punching `alpha = 0`, because:
- `ImageOp`s must then **carry the mask**; that is the field's intended purpose and it is a small change (pass `image.mask` into each op's output).
- The RGB under the mask is preserved, so the pipeline can render/preview the subject with its background intact.
- It avoids the `boxBlur` halo that alpha-baking before preprocessing would cause.
- A future mask brush edits this same buffer.

### D3: The working-resolution mask is the single source of truth
The mask lives at the working-image resolution (not the 1-pixel-per-cell reference), and cell validity, the reference image, and future mask editing all derive from it. Storing it only at the reference resolution would cap precision at one cell and, via `GridSampler.fromReference`, collapse coverage to binary. Keeping it at working resolution preserves soft edge values so partial cell coverage is computed correctly.

### D4: Optional, off-by-default step; remember the last choice
The cutout is a separate step after crop, off by default, with a preview; the last choice is remembered. Leaving it off makes generation byte-for-byte the pre-existing behavior, so the feature cannot regress the default path.

### D5: A prompted-capable `MaskSource` seam
`core` defines `MaskSource { encode(image): Handle; decode(handle, prompts): FloatMask }` with a `Prompts` placeholder that is empty for saliency today. `FloatMask` (0..1) plus `union` / `intersect` / `difference` let semantic and instance masks compose later. The Android implementation (holding the ONNX Runtime session) lives above `core`, so `core` stays pure JVM and testable with a fake source. The encode/decode split mirrors SAM's "encode once per image, decode per prompt" cost model the future instance path will use.

### D6: Per-model 3 s gate, measured, not assumed
Device fragmentation makes static model allow-lists unreliable, so the gate is empirical: on first use, warm up and time a representative inference; if it exceeds 3 s, persist "model unavailable on this device", disable the option with a message, and never block generation. A running inference is bounded by the same 3 s and is cancelled on timeout, falling back to generation without the cutout. The gate is keyed **per model** so a future light model can be available where a heavy one is not.

### D7: Bundled, offline model (privacy-consistent)
The model ships in the APK; there is no model download and no network use. This matches the existing privacy note.

Runtime choice: the only published U²-Net LiteRT/TFLite artifact is `u2net_fp16.tflite` — the **full** 44 M-parameter model at 88 MB, which D1 already rules out; no small `u2netp` TFLite exists. The portable `u2netp` is published as ONNX, so the runtime is **ONNX Runtime for Android** and the asset is `u2netp.onnx`.

Provenance and licensing:
- Asset: `app/src/main/assets/u2netp.onnx`, 4,574,861 bytes.
- Source: Hugging Face `edgetools/u2netp` — a byte-for-byte mirror of the `u2netp.onnx` asset published by `danielgatis/rembg`.
- SHA-256 (pinned, verified at download): `309c8469258dda742793dce0ebea8e6dd393174f89934733ecc8b14c76f4ddd8`.
- Weights license: **Apache-2.0** (U²-Net, Qin et al., Pattern Recognition 2020). Cite the paper if the feature ships.
- Model format license: the mirror originates from `rembg` (MIT).
- Runtime: `com.microsoft.onnxruntime:onnxruntime-android` — **MIT**.

The model's I/O is read off the graph rather than hardcoded: input `float32[1,3,320,320]` NCHW RGB, output `float32[1,1,320,320]` saliency in `[0,1]`.

### D8: Order — mask on the cropped source, then preprocessing
The mask is computed on the cropped source image before preprocessing. Preset operations are geometry-preserving (contrast / saturation / blur / sharpen), so a mask attached before them stays aligned to cell coordinates with no resampling. The one geometry op, square padding, already transforms the mask (see `PixelImage.paddedToSquare`).

## Risks / Trade-offs

- [`u2netp` classification accuracy] → it is category-free and tuned for salient objects; if a subject is not salient it may miss it. Mitigations: preview before generating (user can skip), and the seam allows a stronger model later.
- [Saliency returns several regions] → largest-connected-component keeps one; this can drop a genuinely wanted second subject, accepted for the single-subject scope.
- [Mask must reach sampling through all ops] → D2 + a spec scenario require every op to preserve the mask; a regression test guards it.
- [Low-end devices] → D6 disables the option rather than shipping a multi-second stall; those users keep the current behavior.
- [APK size] → the model is only 4.36 MB, but each ABI's `libonnxruntime.so` is huge (~32 MB arm64, ~23 MB armv7). Mitigated by restricting `abiFilters` to `arm64-v8a` only and dropping the emulator and 32-bit ABIs, so the fat APK carries a single native library. Target devices are 64-bit (Android 13+); a 32-bit-only device would fail to install. Accepted for bundled offline inference, and revisited if a smaller runtime appears.
- [Soft-coverage round-trip] → avoided by D3 (mask stays at working resolution; the reference is derived, not the source).
