# pattern-render Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## Requirements
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

### Requirement: Invalid / empty cells carry no bead
The system SHALL square a non-square cropped image before generation by padding it to a centered square with fully transparent margins, so grid contents are never stretched. A grid cell SHALL be an empty hole (no bead, no `colorCode`) when its source region is less than 50% valid. Validity SHALL come from the image's explicit foreground mask when present, otherwise fall back to per-pixel transparency (alpha >= 8). Empty holes SHALL be rendered in both Form 1 and Form 2 as a light-gray cell with a diagonal hatch, with no bead color and no code text, and SHALL be excluded from `paletteUsed`, per-code counts, and selection.

#### Scenario: Non-square crop is not stretched
- **WHEN** the user crops a region wider than tall (or taller than wide)
- **THEN** the pattern keeps the subject's aspect ratio, with empty holes filling the padded margins instead of the subject being stretched

#### Scenario: Padding margins show no color code
- **WHEN** the pattern contains padded (invalid) margin cells
- **THEN** those cells render as light-gray hatched holes, display no code, are not selectable, and count toward no bead totals

#### Scenario: Foreground mask drives validity
- **WHEN** the image carries an explicit foreground mask (future segmentation)
- **THEN** cell validity and per-cell averaging follow the mask; when no mask is present, transparency defines validity

### Requirement: Group grid guides (Form 2)
The interactive view SHALL overlay group guides on the grid: a thick separator line every 10 cells, a finer separator line every 5 cells (thicker and darker than the base grid line, thinner and lighter than the 10-cell line), and bold numbers (10, 20, ...) at each 10-cell boundary. The 5-cell guides SHALL be skipped where a 10-cell guide would overlap. Group numbers SHALL remain pinned to the screen edges while panning/zooming and SHALL be drawn on a white chip so they stay readable over any cell color. The two axes SHALL be color-coded so rows and columns stay distinguishable at any zoom: column numbers (top edge) use blue, row numbers (left edge) use red.

#### Scenario: Group separators every 10 cells
- **WHEN** any pattern is displayed in Form 2
- **THEN** a thicker separator line appears every 10 cells and bold boundary numbers mark multiples of 10

#### Scenario: Finer separator every 5 cells
- **WHEN** any pattern is displayed in Form 2
- **THEN** a separator line with width and darkness between the base grid line and the 10-cell line appears every 5 cells, except where a 10-cell line is drawn

#### Scenario: Axis color coding
- **WHEN** the user zooms or pans so group numbers are visible
- **THEN** column numbers pinned at the top edge are blue and row numbers pinned at the left edge are red, so rows and columns are distinguishable at any zoom

#### Scenario: Group numbers pinned while panning
- **WHEN** the user pans or zooms so a group boundary crosses the visible area
- **THEN** the corresponding boundary number stays pinned at the screen edge on a white chip with rounded corners

### Requirement: Form 1 (exported image) layout with axes, cell codes, color table, and group guides
The system SHALL render Form 1 as an offline image containing: 1-based column numbers across the top, 1-based row numbers on the left, the grid body where each cell shows its bead `colorCode`, and a color lookup table (code + color swatch, with name if available). The top-left corner cell area where axes meet SHALL be left blank. Group guides SHALL match Form 2: a thicker separator line every 10 cells and a finer separator every 5 cells (base < 5-cell < 10-cell in width and darkness), and the 10-multiple axis numbers SHALL use the Form 2 axis colors (blue columns, red rows) in bold while other axis numbers stay black. The per-cell code text SHALL be drawn when the cell is large enough (same 24 px threshold as Form 2), and SHALL be shrunk so it fits within its cell (see the "Cell code labels fit within their cell" requirement). Cell code, axis number and color-table text SHALL be sized proportionally to the cell size (≈0.45× cell as the upper bound for code text) so the printout stays legible regardless of resolution. Row numbers SHALL be centered within the reserved left gutter so they never overlap the grid boundary.

#### Scenario: Export labeled image
- **WHEN** the user exports Form 1 to PNG
- **THEN** the output image contains column numbers, row numbers, a grid with cell codes, 5/10 group guide lines, and a color lookup table

#### Scenario: Cell code legibility
- **WHEN** a cell's bead color is light (relative luminance >= threshold)
- **THEN** the cell code text uses dark ink with a light stroke; for dark cells it uses light ink with a dark stroke

#### Scenario: Code text visibility threshold matches Form 2
- **WHEN** the cell size is below 24 px (too small to read)
- **THEN** no code text is drawn inside cells, mirroring the interactive view

#### Scenario: Proportional typography
- **WHEN** Form 1 is rendered at any px-per-cell
- **THEN** code text, axis numbers and table text scale with the cell (≈0.45× cell, with a small minimum px), and code text is capped so it never exceeds the cell width, so larger exports do not look cramped and long codes never overlap

#### Scenario: Row numbers stay clear of the grid
- **WHEN** row numbers are drawn in the left gutter (including wide 2–3 digit numbers)
- **THEN** they are centered in the gutter with padding on both sides and do not overlap the grid boundary

#### Scenario: Color table completeness
- **WHEN** Form 1 is rendered
- **THEN** the color lookup table lists exactly the entries in `paletteUsed`

#### Scenario: A4 single-page auto-fit
- **WHEN** the user selects the A4 preset
- **THEN** the exporter computes the largest px/cell whose whole layout (axes + grid + table) fits one A4 page at 300 DPI with 10 mm margins (≈2244×3271 px), and the UI reports the resulting px-per-cell and PNG size

#### Scenario: Resolution options
- **WHEN** the user opens the export screen's output-resolution options
- **THEN** there are exactly two choices — 默认尺寸 (listed first and selected by default, targeting the largest size the device can hold) and A4 自适应 — and the UI reports the resulting PNG dimensions and px-per-cell

#### Scenario: Export pixel budget
- **WHEN** a chosen resolution would exceed the safe bitmap budget (24M pixels total ≈ 96 MB, under Android's ~100 MB single-bitmap draw limit)
- **THEN** the exporter lowers px/cell so the output stays within budget instead of failing

### Requirement: Map coordinates through viewport transform
The system SHALL convert touch coordinates to grid cell coordinates by applying the inverse of the current viewport transform (scale + translate), and SHALL ignore taps that fall outside the grid bounds.

#### Scenario: Tap while zoomed
- **WHEN** the view is zoomed and panned and the user taps a visible cell
- **THEN** the correct grid cell (row/column) is selected

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

### Requirement: Cell code labels fit within their cell
In both Form 1 (exported image) and Form 2 (interactive view), a cell's `colorCode` label SHALL be constrained to its cell. The label SHALL start from the proportional size (≈0.45× cell) and SHALL be shrunk so that its measured width does not exceed the cell width minus a small inset. The label SHALL NOT be drawn when the fitted size would fall below a legibility floor (≈8 px), in which case the cell SHALL show only its bead color with no code text. Short codes that already fit SHALL render at the proportional size, unchanged.

#### Scenario: Long code shrinks to fit
- **WHEN** a cell whose code is `80-19001` is drawn at or above the code-text size threshold
- **THEN** the label is shrunk to fit entirely within that cell and does not overlap any neighbouring cell, in both Form 1 and Form 2

#### Scenario: Short code renders unchanged
- **WHEN** a cell's code is short (e.g. `A1`) and fits at the proportional size
- **THEN** the label is drawn at ≈0.45× cell, exactly as before

#### Scenario: Below the legibility floor the code is omitted
- **WHEN** fitting a code would require a font size below the legibility floor
- **THEN** no code text is drawn in that cell, while the cell still shows its bead color

#### Scenario: Both forms apply the same fit rule
- **WHEN** the same pattern is viewed in Form 2 and exported as Form 1
- **THEN** a code that fits in one form also fits in the other, and a code omitted in one form for falling below the floor is omitted in the other

