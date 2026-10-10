# pattern-render Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## Requirements
### Requirement: Form 2 interactive same-color highlight with coordinates
The interactive view SHALL highlight every cell sharing the selected cell's bead `colorCode` when the user taps a cell, and SHALL display the tapped cell's coordinates (1-based row and column, origin top-left) together with the bead code.

#### Scenario: Tap highlights same color
- **WHEN** the user taps a cell whose colorCode is `C5`
- **THEN** all cells with colorCode `C5` are visually highlighted (e.g., stroked/overlay)

#### Scenario: Coordinate display
- **WHEN** the user taps the cell at grid position row 12, column 15
- **THEN** the info bar shows row 12, column 15 and the tapped cell's bead code

#### Scenario: Tap outside grid clears highlight
- **WHEN** the user taps empty space outside the grid
- **THEN** the highlight is cleared and the info bar returns to its default state

#### Scenario: Highlight from color table
- **WHEN** the user taps an item in the collapsible color table
- **THEN** all cells of that bead color are highlighted, same as tapping a cell

### Requirement: Form 1 (exported image) layout with axes, cell codes, and color table
The system SHALL render Form 1 as an offline high-resolution image containing: 1-based column numbers across the top, 1-based row numbers on the left, the grid body where each cell shows its bead `colorCode`, and a color lookup table (code + color swatch, with name if available). The top-left corner cell area where axes meet SHALL be left blank.

#### Scenario: Export labeled image
- **WHEN** the user exports Form 1 to PNG
- **THEN** the output image contains column numbers, row numbers, a grid with cell codes, and a color lookup table

#### Scenario: Cell code legibility
- **WHEN** a cell's bead color is light (relative luminance >= threshold)
- **THEN** the cell code text uses dark ink with a light stroke; for dark cells it uses light ink with a dark stroke

#### Scenario: Color table completeness
- **WHEN** Form 1 is rendered
- **THEN** the color lookup table lists exactly the entries in `paletteUsed`

#### Scenario: Configurable export resolution
- **WHEN** the user chooses an export cell size (e.g., 60 or 80 pixels per cell)
- **THEN** the exported image dimensions scale accordingly

### Requirement: Collapsible color table
The interactive view SHALL provide a collapsible color table listing `paletteUsed` entries as color swatches with their codes, supporting horizontal scrolling when the list is long.

#### Scenario: Expand color table
- **WHEN** the user expands the color table
- **THEN** all `paletteUsed` entries are reachable by scrolling

#### Scenario: Collapse color table
- **WHEN** the user collapses the color table
- **THEN** the grid area gains the reclaimed vertical space

### Requirement: Map coordinates through viewport transform
The system SHALL convert touch coordinates to grid cell coordinates by applying the inverse of the current viewport transform (scale + translate), and SHALL ignore taps that fall outside the grid bounds.

#### Scenario: Tap while zoomed
- **WHEN** the view is zoomed and panned and the user taps a visible cell
- **THEN** the correct grid cell (row/column) is selected

