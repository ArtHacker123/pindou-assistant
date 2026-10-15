## MODIFIED Requirements

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

## ADDED Requirements

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
