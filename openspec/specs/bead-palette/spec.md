# bead-palette Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## Requirements
### Requirement: Bundle and select multiple bead palettes
The system SHALL bundle at least one bead palette of market bead colors (first version: >=221 colors) as assets and let the user select which palette to use. The palette system SHALL support adding additional palettes without code changes to the pipeline. The first bundled palette SHALL be the MARD standard 221-color set (`mard_221.json`, displayName "MARD 标准 221 色"), whose `colorCode`s are letter+number (groups A–H and M, e.g. `A1`, `H7`, `M15`).

#### Scenario: Default palette loaded
- **WHEN** the app starts and the user reaches parameter setup
- **THEN** the bundled MARD 221-color palette is loaded from assets and shown as the selected palette

#### Scenario: Switch palette
- **WHEN** the user selects a different bundled palette
- **THEN** subsequent generation maps representative colors to the newly selected palette while keeping the same representative colors

#### Scenario: Multiple palettes available
- **WHEN** multiple palette files are bundled
- **THEN** the palette picker lists each with its name and color count

### Requirement: Palette item identity via colorCode
Each bead palette item SHALL have a stable `colorCode` (e.g., `A1`) plus optional name/brand and RGB values. The `colorCode` SHALL be used for cell labels, grid storage, highlight grouping, and cross-device reconstruction.

#### Scenario: Stable code identity
- **WHEN** a grid cell is assigned to a palette item with code `A12`
- **THEN** the stored grid value is the string `A12`, not an index

#### Scenario: Code present in exported artifacts
- **WHEN** Form 1 is rendered or `.pindou` is serialized
- **THEN** every used cell references its bead `colorCode` and the corresponding palette item can be looked up

### Requirement: Build paletteUsed from generation result
The system SHALL compute `paletteUsed` as the distinct set of bead palette items actually referenced by the grid, ordered naturally by `colorCode` (e.g., A1, A2, ..., A10). This SHALL hold for every project whose grid is set — whether it comes from generation, a quick-start template, or free creation — so the editor's color table and its "用色 N 种" count are correct immediately.

#### Scenario: Distinct used colors only
- **WHEN** generation completes
- **THEN** `paletteUsed` contains only bead items that appear in the grid, with no duplicates

#### Scenario: Used count bounded by N
- **WHEN** colorCountTarget = 32
- **THEN** the size of `paletteUsed` is <= 32

#### Scenario: Natural code ordering
- **WHEN** `paletteUsed` is displayed or serialized
- **THEN** items are sorted so that A2 precedes A10

#### Scenario: Template project has a populated color table
- **WHEN** a quick-start template is opened
- **THEN** `paletteUsed` is built from the template's grid, so the editor shows the template's colors and a correct "用色 N 种" count (not zero)

