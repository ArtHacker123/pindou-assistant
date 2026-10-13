## Import prompt lifecycle

- [x] 1.1 Clear the imported flag when entering edit mode (`enterEdit`).
- [x] 1.2 Clear the imported flag on grid/palette mutations (`replaceGrid`, `replaceGridAndPalette`).

## Verification

- [x] 2.1 `:core:test :app:assembleDebug` passes; app installs.
- [x] 2.2 Opening a `.pindou` externally shows the import card; tapping 编辑 hides it.
