## Why

The home screen looked plain and, with few saved patterns, left a lot of empty space. The primary actions were stock Material buttons, the "recent" row felt thin, and there was nothing to do when there was nothing saved yet. Users wanted the home screen to feel like a finished, on-theme perler-bead app and to have a way to start creating without a source image.

## What Changes

- Give the home screen a bead/pixel-art look: a rainbow-heart app mark beside the title, and the primary actions (gallery / camera / free creation) rendered as tappable cards with hand-drawn bead icons instead of plain buttons. Icons are drawn on a Canvas from pixel sprites (no image assets).
- Add a "快速模板" row of built-in quick-start templates (heart, star, smiley, letter A, sun, rainbow, and emoji faces: laugh-cry, big laugh, facepalm, doge, thumbs-up). Tapping one creates a new free-creation project whose grid is that sprite at its own size, mapped to the nearest colors of the default palette, and opens it in edit mode.
- Upgrade the "最近图纸" area to a 3-column grid of thumbnail cards and replace the empty state with a bead illustration plus a hint.
- Add a "拼豆小贴士" card with a bead icon, a random tip, and a "换一条" control.
- Make the whole home screen vertically scrollable.

## Capabilities

### New Capabilities
- `home`: the home screen's bead-style presentation (entry cards, app mark, tip card).
- `pattern-templates`: built-in quick-start templates that seed a new pattern.

### Modified Capabilities
- `pattern-history`: the home "recent" area is now a grid of thumbnail cards with a bead-style empty state.

## Impact

- **App UI**: new `ui/PixelIcon.kt` (Canvas bead renderer); reworked `ui/HomeScreen.kt`; new `data/Templates.kt`.
- **App state**: `PindouController.kt` gains `startTemplateCreation` and a shared `openBlankProject`; `startFreeCreation` reuses it.
- **No file-format change**; templates produce ordinary free-creation projects.
