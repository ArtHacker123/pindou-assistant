## Editor header and color table

- [x] 1.1 Shorten the top-bar title to "生成" / "编辑" and drop the grid-size suffix.
- [x] 1.2 Remove the color table collapse state (`tableExpanded`) and the 收起/展开 control.
- [x] 1.3 Show the color table swatches permanently.
- [x] 1.4 Add a left-aligned summary line above the swatches: `W×H · <palette display name> · 用色 N 种`.

## Verification

- [x] 2.1 `:core:test :app:assembleDebug` passes; app installs and launches without crashing.
- [x] 2.2 View mode shows "生成" and edit mode shows "编辑", with no size suffix.
- [x] 2.3 The color table is always visible with no expand/collapse control, and its summary line shows size, palette name and color count.
