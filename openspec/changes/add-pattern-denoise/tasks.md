## 1. Core — denoise algorithm

- [x] 1.1 Add `core/src/main/kotlin/com/pindou/core/generate/DenoiseStrength.kt`: enum `DenoiseStrength { OFF(关), WEAK(弱), MEDIUM(中), STRONG(强) }` with `id`, `displayName`, `fromId`, and a `maxArea` / `maxDelta` pair per design D5 (OFF = no pass; 1/0.03, 2/0.06, 4/0.10).
- [x] 1.2 Add `core/src/main/kotlin/com/pindou/core/generate/GridRefiner.kt` exposing `refine(cells, width, height, paletteByCode, strength): List<String?>`. Single pass: 8-connected same-code regions with `area <= maxArea` merge into the perceptually nearest (Oklab) non-hole neighbour colour, gated by `distance <= maxDelta`. Regions are read from a snapshot and processed smallest first (a lone cell is just the smallest region, so no separate isolated-cell pass is needed); holes (`null`) are inert; targets are always existing neighbour codes; ties break by natural code order.

## 2. Core — integration

- [x] 2.1 Add denoise fields to `GenerateConfig` (`denoiseStrength: DenoiseStrength = DenoiseStrength.MEDIUM`; OFF means disabled) — no change to field semantics of the rest.
- [x] 2.2 In `core/.../generate/PatternGenerator.kt`, after the snap loop builds `cells` and before `Grid(...)`, call `GridRefiner.refine` with a code→`Rgb` map built from `config.palette`; skip the call when the strength is OFF. `paletteUsed` already derives from the final grid, so it shrinks automatically.

## 3. Core — tests

- [x] 3.1 `core/src/test/kotlin/com/pindou/core/generate/GridRefinerTest.kt`: a perceptually-close 1-cell speckle is merged; a far-coloured isolated cell (dark eye on light face) is preserved; a region larger than `maxArea` is untouched; a lone pixel with 8 same-colour neighbours is removed; a one-cell-wide line is preserved; empty holes are untouched; no colour absent beforehand appears; two runs are identical; OFF is a no-op; 强 merges at least as many cells as 弱.

## 4. App — state, persistence, pass-through

- [x] 4.1 Add a preferences store (SharedPreferences, mirroring `CutoutPrefs`) for the denoise switch and strength; default on · 中 when unset.
- [x] 4.2 `app/.../state/GenerationSession.kt`: expose `denoiseEnabled` / `denoiseStrength` state loaded from prefs; add update methods that persist the choice and `scheduleRegen()`; pass `DenoiseStrength.OFF` when disabled, otherwise the chosen strength, into `GenerateConfig`.
- [x] 4.3 Confirm the denoise setting is a generation-time parameter only and is NOT written into `.pindou` meta (no document-format change); `applyFree` / `applyFromProject` are left unchanged.

## 5. App — UI

- [x] 5.1 In the settings panel (`app/.../ui/editor/EditorScreen.kt`, the 高级 section), add a 去杂色 switch and a 弱/中/强 strength control (disabled/hidden while off), wired to the `GenerationSession` updaters.
- [x] 5.2 Extend the collapsed 高级 summary to include the denoise state (e.g. `去杂色 中` / `去杂色 关`).

## 6. Verify

- [x] 6.1 `:core:test :app:assembleDebug` (and `npx openspec validate add-pattern-denoise --strict`) pass.
- [ ] 6.2 Build and install the APK, then let the user verify on a photo that speckle is reduced and eyes/outlines survive across the three strengths; only commit/archive after the user confirms.
