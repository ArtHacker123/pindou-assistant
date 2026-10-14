## MODIFIED Requirements

### Requirement: Shared grid-cell sampling
The system SHALL compute, in a single pass, each grid cell's validity and its linear-light average color from the preprocessed image, and SHALL expose that result for reuse by cell snapping, color selection, and reference-image production.

#### Scenario: One pass yields cell colors and validity
- **WHEN** the grid sampler runs for a `W × H` grid over a preprocessed image
- **THEN** it returns exactly `W × H` cell samples, each carrying a coverage/validity flag and the linear-light average of that cell's source region

#### Scenario: Generation snaps from the shared samples
- **WHEN** a pattern is generated
- **THEN** each cell's bead is chosen from the same per-cell average produced by the grid sampler, with no second per-cell averaging pass

#### Scenario: Color selection reuses the shared samples
- **WHEN** a pattern is generated with the reference-direct color-selection strategy
- **THEN** the used bead colors are chosen from the same per-cell averages produced by the grid sampler, with no separate sampling pass over the working image
