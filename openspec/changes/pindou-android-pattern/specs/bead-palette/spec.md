## ADDED Requirements

### Requirement: Bundle and select multiple bead palettes
The system SHALL bundle at least one bead palette of market bead colors (first version: >=221 colors) as assets and let the user select which palette to use. The palette system SHALL support adding additional palettes without code changes to the pipeline.

#### Scenario: Default palette loaded
- **WHEN** the app starts and the user reaches parameter setup
- **THEN** the bundled palette (e.g., 221-color) is loaded from assets and shown as the selected palette

#### Scenario: Switch palette
- **WHEN** the user selects a different bundled palette
- **THEN** subsequent generation maps representative colors to the newly selected palette while keeping the same representative colors

#### Scenario: Multiple palettes available
- **WHEN** multiple palette files are bundled
- **THEN** the palette picker lists each with its name and color count

### Requirement: Palette item identity via colorCode
Each bead palette item SHALL have a stable `colorCode` (e.g., `C1`) plus optional name/brand and RGB values. The `colorCode` SHALL be used for cell labels, grid storage, highlight grouping, and cross-device reconstruction.

#### Scenario: Stable code identity
- **WHEN** a grid cell is assigned to a palette item with code `C12`
- **THEN** the stored grid value is the string `C12`, not an index

#### Scenario: Code present in exported artifacts
- **WHEN** Form 1 is rendered or `.pindou` is serialized
- **THEN** every used cell references its bead `colorCode` and the corresponding palette item can be looked up

### Requirement: Build paletteUsed from generation result
The system SHALL compute `paletteUsed` as the distinct set of bead palette items actually referenced by the grid, ordered naturally by `colorCode` (e.g., C1, C2, ..., C10).

#### Scenario: Distinct used colors only
- **WHEN** generation completes
- **THEN** `paletteUsed` contains only bead items that appear in the grid, with no duplicates

#### Scenario: Used count bounded by N
- **WHEN** colorCountTarget = 32
- **THEN** the size of `paletteUsed` is <= 32

#### Scenario: Natural code ordering
- **WHEN** `paletteUsed` is displayed or serialized
- **THEN** items are sorted so that C2 precedes C10
