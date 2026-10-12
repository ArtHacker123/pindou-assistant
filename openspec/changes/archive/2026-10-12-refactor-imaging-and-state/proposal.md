## Why

The upcoming editing/creation work needs three things the current architecture cannot cleanly give: (1) a grid-resolution **reference image produced independently of quantization** (create-from-reference does not quantize), (2) **reusable, composable image preprocessing** the reference and generation paths can share, and (3) editable-grid logic that does not fight the "one-shot generate" model. Bolting these onto the generation-bound pipeline and the `PindouController` god object would entangle them permanently. So this change refactors first — **behavior-preserving**, verified by existing unit tests and manual smoke — so the feature change that follows is purely additive.

## What Changes

- **Extract a core `imaging` module**: move `PixelImage` operations and `Sampler` here, add a **grid-cell sampler** that produces per-cell coverage/color + the reference pixels in one pass, and a **reference builder** that produces the grid-resolution reference **without** quantization.
- **Composable preprocessing pipeline**: replace the one-whole-processor-per-`ImageType` design with an ordered list of image operations (`ImageOp`); an `ImageType` preset becomes a named pipeline. Output for existing presets is byte-identical.
- **`PatternGenerator` consumes the shared grid sampler** instead of doing its own per-cell walk; it owns quantization/palette mapping/snapping only. Image-type preprocessing moves to the shared imaging path (`ReferenceService`) so it runs once and feeds both the reference and the pattern.
- **Split the app `PindouController` god object** into cohesive, single-purpose state holders and services behind a thin facade: navigation, generation session, document store, reference service, export service. No behavior change.
- **Lay out one-way package boundaries** inside `core` (imaging → quant/match → generate → serialize) and keep the two Gradle modules (`:core`, `:app`); no Gradle split yet.
- **No user-visible behavior change**: identical generated patterns, identical reference images, identical `.pindou`, identical UI flows.

## Capabilities

### New Capabilities
- `imaging-pipeline`: the decoupled image-processing pipeline contract — preprocessing as a composable ordered chain, per-cell grid sampling shared by generation and reference production, and standalone reference-image production independent of quantization.

### Modified Capabilities
- (none — this is a behavior-preserving refactor; no existing spec requirement changes)

## Impact

- **core**: new `imaging` package (`ImageOp`, pipeline, grid sampler, reference builder); `PatternGenerator` reworked to use it; `AreaColor`/`processor` reorganized; package-boundary cleanup. `PindouProject`/`.pindou` format untouched.
- **app**: `PindouController` decomposed into state holders + services (navigation, generation, document, reference, export); UI composables unchanged in appearance and behavior.
- **Tests**: existing `:core` tests must stay green and unchanged in intent; add focused tests for the pipeline (preset output parity) and the grid sampler / reference builder (reference equals the generation-built reference).
- **Out of scope (later change)**: the pure `core/editor` grid-edit module, the editing/creation UI, tool set, undo/redo, and the `PindouProject.origin`/generation-meta model change — all land in the follow-up feature change.
