## Why

The home screen's saved-patterns section was labelled 最近图纸 (same content as the history page, which is titled 我的图纸), the quick-template row sat above that section, and the free-creation card used a terse subtitle.

## What Changes

- Rename the home saved-patterns section title from 最近图纸 to 我的图纸 (label only).
- Move the 快速模板 row below the 我的图纸 (saved patterns) section.
- Change the 自由创作 card subtitle to 空白图纸，创作你的专属.

## Capabilities

### Modified Capabilities
- `home`: the saved-patterns section is titled 我的图纸 and sits above the template row; the free-creation card subtitle is 空白图纸，创作你的专属.

## Impact

- **App**: `ui/HomeScreen.kt` (section label, section order, card subtitle).
- No core, dependency, or manifest change.
