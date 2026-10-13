# pattern-editing Specification

## MODIFIED Requirements

### Requirement: Edits apply live to the in-memory project
Every edit SHALL mutate the in-memory project grid immediately, with no separate commit step; the edited grid SHALL be what is exported and saved. Empty holes SHALL remain excluded from `paletteUsed`, per-code counts, and the color table. The color table SHALL keep a constant height regardless of how many colors are in use, so the canvas above it does not resize when used colors are added or removed; when no colors are in use it SHALL show a hint in place of the swatches.

#### Scenario: Edit reflected immediately
- **WHEN** the user paints a cell
- **THEN** the canvas shows the new bead at once and the color table / counts reflect it

#### Scenario: Edited grid is what is exported
- **WHEN** the user exports PNG or saves `.pindou` after editing
- **THEN** the output contains the edited grid

#### Scenario: Canvas does not move when the first color appears
- **WHEN** the user draws the first bead in an empty free-creation project (the color table changes from 0 to 1 used color)
- **THEN** the swatch row occupies the same height as before and the canvas does not move

#### Scenario: Empty color table shows a hint
- **WHEN** no beads are placed (no colors in use)
- **THEN** the color table shows a hint in place of the swatches while keeping its fixed height
