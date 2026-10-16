## 1. Mask options and morphology (core)

- [x] 1.1 Add `MaskOptions` (foreground threshold, hard threshold, connectivity, subject mode, open/close radii) with defaults equal to today's behavior
- [x] 1.2 Add `MaskFilters` with pure `FloatMask` operators: `erode`, `dilate`, `open`, `close`
- [x] 1.3 Add `MaskPostProcess.process(mask, options)` as an ordered operator list (open → close → subject) and keep `keepLargestComponent` as a thin wrapper
- [x] 1.4 Extend subject reduction to honor `MaskConnectivity` (four/eight) and `SubjectMode`
- [x] 1.5 Unit-test morphology (open removes speckle, close fills a hole) and the pipeline (defaults equal current behavior; presets change the mask coherently)

## 2. App wiring

- [x] 2.1 Add `options` to `SaliencyMaskSource` and use it in `decode` (replace the hardcoded 0.5), leaving the `MaskSource` signature unchanged
- [x] 2.2 Add `options` to `CutoutService` and apply it for both the mask and `toBooleanMask`
- [x] 2.3 Persist the selected preset by name in `CutoutPrefs` (unknown/missing falls back to Standard)
- [x] 2.4 Expose the current preset and an update method from `GenerationSession`, mapping each preset to its `MaskOptions`

## 3. UI

- [x] 3.1 Add a compact preset selector (标准 / 收紧背景 / 保留主体) next to the cutout toggle in `EditorScreen`
- [x] 3.2 Keep it consistent with the toggle's availability state (hidden/disabled when the cutout is not applicable)

## 4. Verification

- [x] 4.1 `:core:test :app:assembleDebug` passes
- [x] 4.2 Install the APK and have the user verify: each preset visibly changes the cutout (background tighter / subject preserved) and the choice is remembered
- [x] 4.3 Re-validate the change `--strict`
