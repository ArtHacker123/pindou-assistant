## 1. State & controller

- [x] 1.1 `app/.../state/EditingSession.kt`: add `brushHighlight: Boolean` state defaulting to `true`, with a `toggleBrushHighlight()` method (session-scoped, not persisted).
- [x] 1.2 `app/.../PindouController.kt`: expose `brushHighlight` and `toggleBrushHighlight()`.

## 2. Grid rendering

- [x] 2.1 `app/.../ui/editor/PatternGrid.kt`: add a `dimUnselected: Boolean = true` parameter; when `false`, matching cells keep the inset accent outline but non-matching cells get no dark veil. View mode keeps the default (spotlight unchanged).
- [x] 2.2 `app/.../ui/editor/EditorScreen.kt`: in edit mode pass `selectedCode = if (editMode && controller.brushHighlight) controller.brushCode else if (editMode) null else selectedCode` and `dimUnselected = !editMode` into `PatternGrid`.

## 3. Dock toggle

- [x] 3.1 `app/.../ui/editor/EditorScreen.kt` (`FloatingToolbar`): add a brush-highlight toggle icon (selected-state background like the tools) between the history group and the paint tool; wired to `controller.toggleBrushHighlight()`.

## 4. Verify

- [x] 4.1 `:core:test :app:assembleDebug` and `npx openspec validate add-edit-brush-highlight --strict` pass.
- [ ] 4.2 Build and install the APK; verify in edit mode: pick a color → its cells outline (others undimmed), switch tools / re-enter edit mode → toggle persists, tap/drag/two-finger gestures all unchanged; only commit/archive after the user confirms.
