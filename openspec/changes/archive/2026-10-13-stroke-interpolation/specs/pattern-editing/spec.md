# pattern-editing Specification

## MODIFIED Requirements

### Requirement: Draw using the viewport transform
In edit mode the system SHALL map touch coordinates to grid cells through the inverse of the current viewport transform, and SHALL ignore touches outside the grid. A single-finger drag SHALL paint along the swept cells; a two-finger gesture SHALL pan/zoom instead of drawing. The swept cells SHALL be connected into a single one-cell-wide line by interpolating between consecutive pointer samples (a Bresenham walk in grid space), so a fast drag leaves no gaps and a diagonal stroke stays one cell wide.

#### Scenario: Draw while zoomed
- **WHEN** the view is zoomed and panned and the user draws on a visible cell
- **THEN** the correct grid cell is edited

#### Scenario: Drawing outside the grid is ignored
- **WHEN** the user draws in the empty space outside the grid bounds
- **THEN** no cell is changed

#### Scenario: Pinch pans instead of drawing
- **WHEN** the user performs a two-finger pinch or pan in edit mode
- **THEN** the viewport pans/zooms and no cell is edited

#### Scenario: A fast swipe draws a connected line
- **WHEN** the user drags quickly across the grid so consecutive pointer samples land several cells apart
- **THEN** every cell between the samples is painted, forming a continuous line with no gaps

#### Scenario: A diagonal stroke stays one cell wide
- **WHEN** the user draws a 45-degree line
- **THEN** the stroke is one cell wide (each step advances one cell diagonally)
