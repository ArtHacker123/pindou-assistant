# bead-palette Specification

## Purpose

Define how the bead palette source and the set of used colors are derived and ordered.

## MODIFIED Requirements

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
