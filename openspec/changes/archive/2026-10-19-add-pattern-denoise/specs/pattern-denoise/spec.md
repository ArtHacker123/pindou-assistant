## ADDED Requirements

### Requirement: Denoise the snapped bead grid before it is emitted

The system SHALL apply a denoise pass to the bead grid after cells are snapped and before the grid is emitted, and only while denoise is enabled. The pass SHALL operate on valid bead cells only, SHALL never modify empty holes (`null`), SHALL never introduce a bead colour that was not already present in the grid, and SHALL be a deterministic function of the snapped grid and the selected strength.

#### Scenario: Empty holes are never modified
- **WHEN** the grid contains empty holes and denoise runs
- **THEN** every empty hole is still empty after denoise

#### Scenario: No new colours are introduced
- **WHEN** denoise merges a region
- **THEN** the region is recoloured to a colour already present among its neighbouring cells, and no colour absent from the grid beforehand appears afterwards

#### Scenario: Deterministic result
- **WHEN** the same snapped grid and strength are denoised twice
- **THEN** the two resulting grids are identical

#### Scenario: Disabled denoise is a no-op
- **WHEN** denoise is disabled
- **THEN** the emitted grid is identical to the snapped grid

### Requirement: Guarded small-region merge

The system SHALL find connected regions of a single bead colour and, for a region whose area does not exceed a strength-dependent maximum, SHALL merge the whole region into the perceptually nearest (Oklab) non-hole colour among its surrounding cells, but only when that perceptual distance does not exceed a strength-dependent maximum; otherwise the region SHALL be left unchanged.

#### Scenario: Speckle region is merged
- **WHEN** a one-cell region's colour is perceptually close to the colour of its surrounding cells
- **THEN** the region is recoloured to that surrounding colour

#### Scenario: Small distinct feature is preserved
- **WHEN** a small region's colour is perceptually far from every surrounding colour (for example a dark eye surrounded by a light face)
- **THEN** the region keeps its colour

#### Scenario: A region larger than the maximum is untouched
- **WHEN** a connected same-colour region's area exceeds the strength maximum
- **THEN** the region is left unchanged

#### Scenario: Target is always an existing neighbouring colour
- **WHEN** a small region is merged
- **THEN** the colour it takes is one that already appears in a neighbouring cell, never a synthesized or previously-absent colour

### Requirement: Isolated-cell removal

For a bead cell whose eight neighbours are all the same single bead colour, the system SHALL replace the cell with that neighbour colour when the perceptual distance between the two colours does not exceed the strength-dependent maximum; otherwise the cell SHALL be left unchanged.

#### Scenario: A lone noisy pixel is removed
- **WHEN** a cell is surrounded on all eight sides by one perceptually-close colour
- **THEN** the cell takes that colour

#### Scenario: A one-cell-wide line is preserved
- **WHEN** a line of one-cell width runs through the grid so that each line cell has same-colour neighbours along the line
- **THEN** no line cell is removed by isolated-cell removal

#### Scenario: A far-coloured lone pixel is kept
- **WHEN** a cell is surrounded on all eight sides by one colour that is perceptually far from the cell's colour
- **THEN** the cell keeps its colour

### Requirement: Denoise strength levels

The system SHALL offer the denoise strengths 关 (off), 弱, 中, and 强, where a higher strength increases both the maximum mergeable region area and the maximum perceptual distance allowed for a merge. Increasing strength SHALL merge at least as many cells as a lower strength.

#### Scenario: Stronger strength merges at least as much
- **WHEN** the same grid is denoised at 弱 and at 强
- **THEN** 强 merges at least as many cells as 弱

#### Scenario: Off performs no merge
- **WHEN** the strength is 关
- **THEN** no cell is merged
