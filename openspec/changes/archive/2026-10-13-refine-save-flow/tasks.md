## Save-flow refinement

- [x] 1.1 Add a `projectIsEmpty` check to the controller (grid has no beads).
- [x] 1.2 In the editor, confirm before 保存 / 完成 / 导出 when the grid is empty and there are unsaved changes (`guardEmpty`).
- [x] 1.3 Skip the `.pindou` write and the "已保存" dialog when `needsSave` is false in `saveProject`.
- [x] 1.4 Ensure 完成 / 导出 / the leave-and-save dialog still reach their follow-up actions.
- [x] 1.5 Confirmation dialog wording: "当前图纸没有任何珠子，为空。是否仍要保存/导出？"

## Verification

- [x] 2.1 `:core:test :app:assembleDebug` passes; app installs and launches without crashing.
- [x] 2.2 Saving an empty grid asks for confirmation.
- [x] 2.3 After a save with no further edits, re-tapping 保存 / 完成 / 导出 shows no dialog and proceeds directly.