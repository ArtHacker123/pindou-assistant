# pattern-render Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## RENAMED Requirements
- FROM: `### Requirement: Collapsible color table`
- TO: `### Requirement: Color table with size and palette summary`
## MODIFIED Requirements

### Requirement: Form 2 interactive same-color highlight with coordinates
The interactive view SHALL highlight every cell sharing the selected cell's bead `colorCode` when the user taps a cell, and SHALL display the tapped cell's coordinates (1-based row and column, origin top-left) together with the bead code. The highlight SHALL use a spotlight style: cells of the selected code keep their full color and get an inset accent (amber) outline, while all other cells are dimmed by a black veil (≈55%) that preserves hue. The exact tapped cell SHALL additionally be marked with a hollow circle tangent to the inset accent square, using the same ink color as the cell's code text (dark ink on light cells, light ink on dark cells) so the marker stays visible on any cell color.

#### Scenario: Tap highlights same color
- **WHEN** the user taps a cell whose colorCode is `C5`
- **THEN** all cells with colorCode `C5` keep full color with an inset accent outline, and all other cells are dimmed by a black spotlight veil; the exact tapped cell also gets a hollow circle tangent to its accent square

#### Scenario: Coordinate display
- **WHEN** the user taps the cell at grid position row 12, column 15
- **THEN** the info bar shows row 12, column 15 and the tapped cell's bead code

#### Scenario: Tap outside grid clears highlight
- **WHEN** the user taps empty space outside the grid
- **THEN** the highlight is cleared and the info bar returns to its default state

#### Scenario: Tap highlighted cell clears highlight
- **WHEN** the user taps any cell currently highlighted (i.e., any cell of the selected `colorCode`)
- **THEN** the highlight is cleared, regardless of zoom level; this keeps deselection possible while zoomed in and the grid fills the viewport

#### Scenario: Highlight from color table
- **WHEN** the user taps an item in the color table
- **THEN** all cells of that bead color are highlighted, same as tapping a cell

### Requirement: Color table with size and palette summary

The interactive view SHALL always show a color table listing `paletteUsed` entries as color swatches with their codes, supporting horizontal scrolling when the list is long. The table SHALL NOT be collapsible: it is permanently visible below the canvas and has no expand/collapse control. Above the swatches, the table SHALL show a single left-aligned summary line containing, in order, the grid size (`W×H`), the palette display name, and the number of colors used ("用色 N 种"), separated by `·`.

#### Scenario: Color table is always visible
- **WHEN** a pattern is displayed
- **THEN** the color table and its summary line are shown, with no expand/collapse control

#### Scenario: Summary line shows size, palette, and color count
- **WHEN** a pattern is displayed
- **THEN** the summary line reads `<W>×<H> · <palette display name> · 用色 <N> 种`, where `N` is the size of `paletteUsed`

#### Scenario: Long color list scrolls
- **WHEN** `paletteUsed` has more entries than fit on screen
- **THEN** all entries are reachable by horizontal scrolling
