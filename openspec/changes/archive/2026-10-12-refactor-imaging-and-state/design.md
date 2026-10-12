## Context

Today the whole generation pipeline lives in `PatternGenerator.generate` (`core/.../generate/PatternGenerator.kt`): it preprocesses via a single whole-image `ImageProcessor` chosen by `ImageType`, samples, quantizes, maps palette centers, then walks the grid once (`AreaColor.cell`) to produce both the snapped cells **and** the grid-resolution reference (`refPixels`). The reference is therefore a by-product of generation and cannot be produced on its own — but the upcoming creation-from-reference flow needs exactly that.

Preprocessing is also "one class per preset" (`CartoonProcessor`, `PhotoProcessor`), so future multi-stage work (segmentation, background removal) would mean rewriting those classes. And all app state lives in one `PindouController` (navigation, generation params, reference cache, export, permissions), which will not absorb editing/creation state without becoming unmaintainable.

This change is a **pure refactor**: same observable behavior, better seams. It is the prerequisite for the editing/creation feature change.

## Goals / Non-Goals

**Goals:**
- Make image preprocessing a **composable ordered pipeline** (`ImageOp` chain); an `ImageType` preset is a named pipeline.
- Extract a **grid-cell sampler** that yields per-cell coverage/color + reference pixels in one pass, and a **reference builder** that produces the grid-resolution reference **independently of quantization**.
- Make `PatternGenerator` consume the shared sampler; it keeps only quantization / palette mapping / snapping.
- Decompose `PindouController` into cohesive state holders + services behind a thin facade.
- Establish one-way package boundaries in `core`; keep `:core` + `:app`.
- Byte-identical outputs for existing presets and identical `.pindou` payloads.

**Non-Goals:**
- New user-visible behavior; any change to `.pindou` / `PindouProject` model.
- The `core/editor` grid-edit module, editing/creation UI, tools, undo/redo (later feature change).
- Gradle module splitting.
- Performance work (LUT, caching) — tracked separately.

## Decisions

### D1: `core` package layout with one-way dependencies
- **Choice**: Package/layer order `imaging → quant/match → generate → serialize`, with `model`, `color`, `util` as leaf foundations. `generate` may depend on `imaging`; `imaging` MUST NOT depend on `generate`/`quant`/`match`. New `imaging` package absorbs `PixelImage` ops, sampling, grid sampling and reference building.
- **Rationale**: The reference path and the generation path then share one implementation without a cycle; future preprocessing plugs into `imaging` only.
- **Alternatives**: Keep everything in `generate` (current) — reference stays generation-bound. Split Gradle modules now — disproportionate build cost for 2 modules.

### D2: Composable preprocessing pipeline (`ImageOp`)
- **Choice**: Introduce `fun interface ImageOp { fun apply(image: PixelImage): PixelImage }` and a `ProcessorPipeline(ops: List<ImageOp>)`. `ImageType` becomes a preset id mapped to a named pipeline (e.g. `cartoon = [contrast(1.12), sharpen(0.6), saturate(1.08)]`, `photo = [blur(1), contrast(1.06), saturate(1.05)]`) built from the existing `ImageOps` primitives. The old `ImageProcessor` per-preset classes are replaced by these pipeline definitions; `ImageProcessorRegistry` becomes a pipeline registry keyed by id.
- **Rationale**: Multi-stage/未来 operations become "append an `ImageOp`"; no class rewrite. Existing primitives already exist in `ImageOps`.
- **Parity**: the carton/photo pipelines MUST reproduce the current op order exactly so output is byte-identical; a pinned-output test guards this.
- **Alternatives**: Keep whole-image processors and add a "composite" processor — still couples type→class and doesn't compose.

### D3: Grid sampler + reference builder (single pass, quantization-free)
- **Choice**: Add `GridSampler.sample(image, gridW, gridH, mask?): GridSamples` where `GridSamples` holds `coverage[]`, `avgRgb[]` (linear-light), `valid[]` and can emit the reference `PixelImage` (alpha 0 for holes, `MIN_CELL_VALID` threshold). `ReferenceBuilder.build(image, gridW, gridH): PixelImage` is just `GridSampler → reference`. `PatternGenerator` uses the same `GridSamples` to snap cells.
- **Rationale**: One implementation of the per-cell walk serves both the reference and cell colors; the reference is now independently producible for create-from-reference. Preserves the linear-light/coverage semantics of `AreaColor.cell` (D14).
- **Alternatives**: Keep `AreaColor.cell` loop inside `generate` and write a second loop for reference-only — duplicate logic that can drift.

### D4: `PatternGenerator` becomes a thin orchestrator
- **Choice**: `generate(preprocessedImage, config, reference?) = sample → quantize → mapCenters → GridSampler → snap`. Preprocessing is **not** done inside `generate`; it lives in the shared imaging path (`ReferenceService` / `ImageProcessorRegistry`) so the caller preprocesses once and feeds the same image to both the reference and the generator. `GenerateConfig` keeps its shape; the reference-reuse shortcut stays (reused reference skips the grid walk).
- **Rationale**: Follows the existing pluggable-stage ethos (`Quantizer`, `Sampler`, `ColorSpace` already are), extends it to grid sampling, and — crucially — keeps preprocessing a single shared step instead of one inside `generate` plus one in the reference path (which would double the most expensive per-pixel work).
- **Alternatives**: Keep preprocessing inside `generate` — then the reference path must either duplicate it (double preprocessing) or derive a reference from generation output, which stops `ReferenceService` being genuinely shared by both paths.

### D5: Decompose `PindouController` into state holders + services
- **Choice**: Keep `PindouController` as a thin facade that Compose still observes, but move cohesive state out of it:
  - `NavigationState` — current `Screen`.
  - `GenerationSession` — generation params + working image + `ReferenceService` reuse cache + `runGenerate`.
  - `DocumentStore` — current `PindouProject` + open/save.
  - `ReferenceService` — the shared imaging entry point: `preprocess(image, type)` (image-type preset) and `reference(processed, gw, gh)` (grid-resolution reference), plus `referenceFrom(...)` for creation. Both generation and creation call it, so preprocessing runs exactly once.
  - `ExportService` — PNG/.pindou render+save+share, permissions.
  No `lifecycle-viewmodel-compose` (not a dependency): plain classes with `mutableStateOf`, held by the facade.
- **Rationale**: Editing/creation will add its own `EditSession`; a per-concern split keeps each piece testable and prevents the god object. Compose call sites keep reading `controller.x`.
- **Alternatives**: One giant controller (status quo) — will not scale past the feature.

### D6: Test strategy for a behavior-preserving refactor
- **Choice**: (a) run/keep all existing `:core` tests; (b) add a **pipeline parity test** pinning `cartoon`/`photo` output for a fixed input; (c) add a **reference-equality test** asserting `ReferenceBuilder.build(...)` equals the reference embedded by `PatternGenerator.generate(...)` for the same inputs.
- **Rationale**: The two guarantees that must not drift are "preset preprocessing unchanged" and "reference identical whether built alone or via generation".
- **Alternatives**: rely on manual testing only — too weak for a structural refactor.

## Risks / Trade-offs

- [Risk] Pipeline refactor silently changes preset output (op order/params) → **Mitigation**: pin exact op sequences and a golden-output test; keep `ImageOps` untouched.
- [Risk] App state split introduces subtle state-ownership bugs → **Mitigation**: behavior-preserving checklist (pick→crop→generate; param changes; reference toggle/peek; export PNG; save/open `.pindou`, incl. reference-embedded) run on device `642cc491`.
- [Risk] Over-abstraction beyond current reuse → **Mitigation**: only extract `imaging` pieces that are reused by both paths today; defer `editor`, Gradle split, SPI.
- [Trade-off] Two-step delivery (refactor, then feature) is more overhead than one change → accepted: a behavior-preserving change is independently verifiable and makes the feature additive.

## Migration Plan

1. Create `imaging` package; move `PixelImage` ops + `Sampler`; add `ImageOp`/pipeline + `GridSampler` + `ReferenceBuilder`; port `ImageType` presets to pipelines.
2. Rework `PatternGenerator` to consume `GridSampler`; keep `GenerateConfig`/output identical.
3. Add parity + reference-equality tests; ensure all existing tests green.
4. Decompose `PindouController` behind the facade; UI unchanged.
5. Manual smoke on device; then start the editing/creation feature change.

**Rollback**: revert the branch; no data/format change, so no migration concerns.

## Open Questions

- None blocking. Naming (`imaging` vs `pipeline`; `GridSamples` fields) to be finalized during implementation.
