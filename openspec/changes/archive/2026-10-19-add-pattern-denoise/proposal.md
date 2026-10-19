## Why

Photo patterns come out speckled: neighbouring cells land on different bead codes, so a pattern uses many colours scattered in isolated cells (杂色). This is because each cell is snapped to its nearest bead independently — photos carry gradient and sensor/JPEG noise, and cell boundaries mix two colours into an intermediate that snaps to a third bead — so a pattern generated from a photo is noisy and tedious to bead. The user wants a "colour merge / denoise" step that removes this speckle without erasing small but important features (eyes, thin outlines).

## What Changes

- Add a **pattern denoise** post-process that runs after cells are snapped and before the grid is emitted. It merges speckle while protecting high-contrast small features through a perceptual colour-distance gate.
- Two gated passes: (1) connected-component small-region merge and (2) isolated-cell removal. A region/cell is only merged when its colour is perceptually close (Oklab) to the colour it would merge into; small regions whose colour is far from their neighbours (eyes, outlines) are kept.
- Empty holes (`null`) are never modified, and the pass never introduces a bead colour that was not already in the pattern.
- A new generation setting **去杂色** with a switch and a 弱/中/强 strength, shown with the advanced options, defaulting to **on · 中** and persisted.
- No change to preprocessing or the reference image: only the emitted bead grid is cleaned.

## Capabilities

### New Capabilities
- `pattern-denoise`: Cleaning a snapped bead grid by merging perceptually-close speckle, gated so small high-contrast features survive; empty holes untouched; deterministic.

### Modified Capabilities
- `generation-settings`: the advanced options gain the 去杂色 switch and strength, with a default of on · medium and persistence.

## Impact

- **core**: new grid post-process (e.g. `generate/GridRefiner.kt`) with its thresholds/strength, invoked from `generate/PatternGenerator.kt` between snapping and `Grid` construction; new `GenerateConfig` fields.
- **app**: `state/GenerationSession.kt` carries the denoise parameters (default on · medium) and passes them through; the settings panel (`ui/editor/EditorScreen.kt`) shows the switch and strength; persistence alongside the other generation parameters.
- **specs**: new `pattern-denoise`; `generation-settings` delta.
