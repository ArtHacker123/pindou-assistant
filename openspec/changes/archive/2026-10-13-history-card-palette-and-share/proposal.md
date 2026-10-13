## Why

Each card on the "我的图纸" history page only showed the pattern name and a date, so it was hard to tell which bead palette a pattern used. There was also no quick way to share a saved pattern without opening it first.

## What Changes

- Show the bead palette name on each history card (decoded from the stored `.pindou`).
- Add a share action to each history card that shares the pattern's `.pindou` file through the system share sheet.

## Capabilities

### Modified Capabilities
- `pattern-history`: history cards show the bead palette name; each card offers a direct share action.

## Impact

- **App**: `data/HistoryStore.kt` (`PatternEntry.paletteName`), `data/ExportManager.kt` (`copyToShare`), `state/ExportService.kt` (`shareStoredPindou`), `PindouController.kt` (`shareHistory`), `ui/HistoryScreen.kt` (palette line + share icon).
- No core, dependency, or manifest change.
