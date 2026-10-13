## Gallery import via the system photo picker

- [x] 1.1 Switch the home "从相册生成" action from `ActivityResultContracts.GetContent` to `ActivityResultContracts.PickVisualMedia` in `ImageOnly` mode.
- [x] 1.2 Keep the picked-photo flow unchanged (load original → crop/scale).

## Verification

- [x] 2.1 `:core:test :app:assembleDebug` passes; app installs and launches without crashing.
- [x] 2.2 Tapping "从相册生成" opens the photo picker directly in a thumbnail grid (no folder browser) on a supported device.