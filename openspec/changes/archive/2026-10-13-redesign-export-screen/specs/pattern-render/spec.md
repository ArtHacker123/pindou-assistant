# pattern-render Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## MODIFIED Requirements

### Requirement: Form 1 (exported image) layout with axes, cell codes, color table, and group guides
The system SHALL render Form 1 as an offline image containing: 1-based column numbers across the top, 1-based row numbers on the left, the grid body where each cell shows its bead `colorCode`, and a color lookup table (code + color swatch, with name if available). The top-left corner cell area where axes meet SHALL be left blank. Group guides SHALL match Form 2: a thicker separator line every 10 cells and a finer separator every 5 cells (base < 5-cell < 10-cell in width and darkness), and the 10-multiple axis numbers SHALL use the Form 2 axis colors (blue columns, red rows) in bold while other axis numbers stay black. The per-cell code text SHALL be drawn when the cell is large enough (same 24 px threshold as Form 2). Cell code, axis number and color-table text SHALL be sized proportionally to the cell size (≈0.45× cell) so the printout stays legible regardless of resolution. Row numbers SHALL be centered within the reserved left gutter so they never overlap the grid boundary.

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
- **THEN** code text, axis numbers and table text scale with the cell (≈0.45× cell, with a small minimum px) so larger exports do not look cramped

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
