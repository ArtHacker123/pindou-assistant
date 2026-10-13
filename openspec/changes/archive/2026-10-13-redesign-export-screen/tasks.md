## Export resolution options

- [x] 1.1 Reduce `ExportSize` to 默认尺寸 (default, = max) and A4 自适应, and default to 默认尺寸.
- [x] 1.2 Remove the 1080P / 4K resolution branches.

## Export screen redesign

- [x] 2.1 Lay out preview + resolution options + actions to fit one screen without scrolling.
- [x] 2.2 Keep 保存 PNG (saves the PNG to the gallery) and change the second action to 分享 (shares the `.pindou` file, never the PNG).
- [x] 2.3 Remove the "包含参考图" switch; always embed the reference when present.
- [x] 2.4 Add a bottom bead-style 返回首页 button.

## Sharing fix

- [x] 3.1 Launch the share chooser with `FLAG_ACTIVITY_NEW_TASK` so it opens from the application context.
- [x] 3.2 Share `.pindou` with `application/octet-stream` and a `*/*` fallback.

## Verification

- [x] 4.1 `:core:test :app:assembleDebug` passes; app installs and launches without crashing.
- [x] 4.2 保存 PNG writes the PNG to the gallery and 分享 opens the system share sheet with the `.pindou` file.
- [x] 4.3 The export screen shows exactly two resolution options and two actions on one screen, plus the 返回首页 button.
