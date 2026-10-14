## 1. Core color strategies

- [x] 1.1 Add `ColorStrategy` interface (`id`, `displayName`, `select(image, gridSamples, palette, config)`) and `ColorStrategyRegistry` (default = reference-direct) under `core/match`.
- [x] 1.2 Add `ReferenceDirectStrategy` (`id = "reference"`): nearest-bead per valid cell; uncapped when automatic, greedy maximum coverage (weights `count^0.5`, natural-code tie-break) when a manual count is set.
- [x] 1.3 Add `KMeansStrategy` (`id = "kmeans"`) wrapping the current sampler → quantizer → `mapCenters` path.
- [x] 1.4 Add `colorStrategy` and `colorCountAuto` to `GenerateConfig`.
- [x] 1.5 Rework `PatternGenerator.generate` to compute `gridSamples` first, ask the strategy for the used beads, then snap cells as today.

## 2. Persistence and session

- [x] 2.1 Add `colorStrategy: String = "reference"` to `ProjectMeta`.
- [x] 2.2 `GenerationSession`: hold `colorStrategy`, pass it + `colorCountAuto` into `GenerateConfig`, restore it in `applyFromProject`, and add `updateColorStrategy(...)` with debounced regen.
- [x] 2.3 `PindouController`: expose `colorStrategy` and `updateColorStrategy(...)`.

## 3. UI

- [x] 3.1 Add a "取色方式" dropdown to `ParamsPanel` (参考图直取 / K-means 聚类), wired through `runOrConfirm` like the other params.

## 4. Tests and verification

- [x] 4.1 Core tests for `ReferenceDirectStrategy`: uncapped keeps every bead that appears; manual N keeps the best-representing beads; a rare distinct color survives the cap; deterministic without a seed.
- [x] 4.2 Keep `PatternGeneratorTest` green under the new default (pin strategy explicitly where a test depends on K-means).
- [ ] 4.3 `:core:test :app:assembleDebug` passes; install and have the user verify both strategies.
