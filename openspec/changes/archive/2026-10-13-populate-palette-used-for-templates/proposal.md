## Why

A pattern started from a quick-start template showed "用色 0 种" with an empty color table: `paletteUsed` was left empty when the blank/template project was created, because it was only computed by the generation pipeline.

## What Changes

- Build `paletteUsed` from the grid for any directly-set grid, including templates (and free creation, where it stays empty because there are no beads).

## Capabilities

### Modified Capabilities
- `bead-palette`: `paletteUsed` is built from the grid for every project, not only generation, so the color table and 用色 count are correct for templates.

## Impact

- **App**: `PindouController.kt` (`openBlankProject` computes used items), `state/DocumentStore.kt` (`usedItems` exposed to the controller).
- No core, dependency, or manifest change.
