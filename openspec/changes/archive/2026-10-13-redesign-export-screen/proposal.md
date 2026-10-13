## Why

The export screen was cluttered: it offered four resolution presets (A4、1080P、4K、最大尺寸), a "可打印 PNG" section with both 保存 and 分享 buttons, and a separate ".pindou" section with its own share button, the "包含参考图" opt-out switch, and a bottom "返回编辑" button — and it scrolled. The two share buttons also did nothing: the share intent was launched from the application context without `FLAG_ACTIVITY_NEW_TASK`, and the `.pindou` share used a custom MIME type (`application/x-pindou`) that almost no app can receive.

## What Changes

- Output resolution is reduced to two options: 默认尺寸 (listed first and selected by default, = the largest size the device can hold) and A4 自适应.
- The export screen is redesigned to fit one screen without scrolling: a print preview, the resolution options, and exactly two actions — 保存 PNG (saves the PNG to the system gallery) and 分享 (shares the `.pindou` file, not the PNG).
- The "包含参考图" opt-out switch is removed; the reference image is always embedded when present.
- A bead-style 返回首页 button is added at the bottom.
- Sharing is fixed by launching the chooser with `FLAG_ACTIVITY_NEW_TASK` so it opens from the application context.
- `.pindou` is shared with a generic binary MIME (`application/octet-stream`) and a `*/*` fallback instead of the custom MIME, so the share sheet has targets.

## Capabilities

### Modified Capabilities
- `pattern-render`: the Form 1 resolution presets collapse to two options (默认尺寸, default and first; A4 自适应).
- `pattern-share`: Form 1 PNG is saved to the gallery only (no in-app PNG share); `.pindou` always embeds the reference and is shared with a generic/fallback MIME launched with `FLAG_ACTIVITY_NEW_TASK`; the export screen's one-screen layout and its two actions are specified.

## Impact

- **App UI**: `ui/ExportScreen.kt` — redesign (preview + two resolution options + 保存 PNG / 分享), removal of the reference switch, and the new 返回首页 button.
- **App state**: `state/ExportService.kt` (resolution branches, always-embed reference, share MIME), `PindouController.kt` (remove `includeReference`, resolution default), `data/ExportManager.kt` (share launch flags and MIME fallback).
- No core, dependency, or manifest change.
