# bead-grid Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## Requirements
### Requirement: Generate W×H grid bounded at 128×128
The system SHALL let the user define the grid dimensions (W×H) with a maximum of 128 columns and 128 rows. The generated grid SHALL be a 2D array of bead `colorCode` values with origin at the top-left cell.

#### Scenario: Custom grid size
- **WHEN** the user sets the grid to 40×60
- **THEN** the system generates a grid with 40 columns and 60 rows

#### Scenario: Reject oversize grid
- **WHEN** the user enters 129×129
- **THEN** the system rejects or clamps the value to the 128×128 limit

#### Scenario: Origin convention
- **WHEN** the top-left cell of the generated grid is inspected
- **THEN** it corresponds to coordinate (row 1, column 1) as shown to the user

### Requirement: Cell color mapping through representative colors
The system SHALL map each grid cell by computing the linear-light average RGB of the cell's source region (the reference value) and assigning the bead `colorCode` that is perceptually nearest among the beads chosen by the active color-selection strategy. A cell SHALL be assigned directly from its own average without routing through an intermediate representative-center indirection.

#### Scenario: Cell assigned from its average
- **WHEN** a cell's region average color is perceptually closest to bead code `C7` among the chosen beads
- **THEN** that cell stores `C7`

#### Scenario: Consistent grouping
- **WHEN** two source regions both map to the same chosen bead
- **THEN** both cells store the same bead `colorCode`, forming a uniform color group

### Requirement: Interactive viewport with pan and zoom
The interactive pattern view (Form 2) SHALL support pan and pinch-zoom gestures and SHALL render only the visible grid cells (viewport culling) rather than all cells.

#### Scenario: Pan across large grid
- **WHEN** the user drags on a 128×128 grid
- **THEN** the view pans and redraws only the newly visible cells

#### Scenario: Zoom to single cell
- **WHEN** the user pinch-zooms in
- **THEN** the view scales around the gesture centroid and cells become individually readable

#### Scenario: Culling performance
- **WHEN** a 128×128 grid is zoomed in
- **THEN** only the cells intersecting the viewport are drawn each frame

### Requirement: Grid size presets include a 15×15 option

Wherever the user chooses a pattern size — generating from an image (the parameters panel) and starting a free creation (the size picker) — the system SHALL offer the same set of square preset sizes, and SHALL include a 15×15 option (matching the quick templates) alongside the existing larger presets, up to the 128×128 maximum.

#### Scenario: 15×15 is offered
- **WHEN** the user opens a pattern size selector (generate parameters or free creation)
- **THEN** 15×15 is among the available size options

#### Scenario: Generate at 15×15
- **WHEN** the user selects 15×15 and generates a pattern
- **THEN** the pattern is produced on a 15×15 grid

