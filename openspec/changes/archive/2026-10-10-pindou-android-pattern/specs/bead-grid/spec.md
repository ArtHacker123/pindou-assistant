## ADDED Requirements

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
The system SHALL map each grid cell by computing the average RGB of the cell's source region in the working image, finding the nearest representative color (quantization center), and assigning the bead `colorCode` mapped from that representative color.

#### Scenario: Cell assigned via representative color
- **WHEN** a cell's region average color is closest to representative color R5, and R5 maps to bead code `C7`
- **THEN** that cell stores `C7`

#### Scenario: Consistent grouping
- **WHEN** two source regions fall under the same representative color
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
