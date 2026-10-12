## 1. Bead-style rendering

- [x] 1.1 Add `ui/PixelIcon.kt`: draws a pixel sprite as rounded beads on a Canvas (per-cell gradient highlight), no bitmap assets.
- [x] 1.2 Add a rainbow-heart bead logo beside the home title.
- [x] 1.3 Render gallery / camera / free-creation as icon cards (icon + title + subtitle).

## 2. Quick templates

- [x] 2.1 Add `data/Templates.kt` with built-in sprites and colors.
- [x] 2.2 Add `PindouController.startTemplateCreation` and share `openBlankProject` with `startFreeCreation`.
- [x] 2.3 Show a "快速模板" row on the home screen; tapping a template opens it in edit mode at the template's size.
- [x] 2.4 Include emoji-face templates (笑哭, 大笑, 捂脸, 狗头, 点赞).

## 3. Recent area & tips

- [x] 3.1 Change "最近图纸" to a 3-column grid of thumbnail cards.
- [x] 3.2 Replace the empty state with a bead illustration + hint.
- [x] 3.3 Add a "拼豆小贴士" card with a "换一条" control.
- [x] 3.4 Make the home screen vertically scrollable.

## 4. Verification

- [x] 4.1 Build `:core:test :app:assembleDebug` succeeds and the app launches without crashing.
- [x] 4.2 Tapping a template creates and opens the pattern in edit mode.
