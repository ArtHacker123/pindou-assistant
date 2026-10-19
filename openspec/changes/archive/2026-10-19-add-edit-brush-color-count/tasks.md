## 1. Info bar wiring

- [x] 1.1 `app/.../ui/editor/EditorScreen.kt` (`InfoBar`): in the edit-mode branch, after the current brush color code, render the bead count as `同色 N 粒` using the existing `sameColorCount` parameter; when `brushCode` is null keep the existing "请在下方选择珠子颜色" hint and show no count. The view-mode branch stays untouched.
- [x] 1.2 `app/.../ui/editor/EditorScreen.kt` (call site): pass `sameColorCount = controller.brushCode?.let { counts[it] } ?: 0` so the edit branch reads from the same `counts` map already computed for the view branch.

## 2. Verify

- [x] 2.1 `:core:test :app:assembleDebug` and `npx openspec validate add-edit-brush-color-count --strict` pass.
- [ ] 2.2 Build and install the APK; verify: pick a brush color → info bar shows code + correct count; paint/erase/replace/undo → count updates live; pick an unused palette color → count shows 0; no brush color → hint only; view mode unchanged. Only commit/archive after the user confirms.
