## MODIFIED Requirements

### Requirement: Cell color mapping through representative colors
The system SHALL map each grid cell by computing the linear-light average RGB of the cell's source region (the reference value) and assigning the bead `colorCode` that is perceptually nearest among the beads chosen by the active color-selection strategy. A cell SHALL be assigned directly from its own average without routing through an intermediate representative-center indirection.

#### Scenario: Cell assigned from its average
- **WHEN** a cell's region average color is perceptually closest to bead code `C7` among the chosen beads
- **THEN** that cell stores `C7`

#### Scenario: Consistent grouping
- **WHEN** two source regions both map to the same chosen bead
- **THEN** both cells store the same bead `colorCode`, forming a uniform color group
