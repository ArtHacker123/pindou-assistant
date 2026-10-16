## MODIFIED Requirements

### Requirement: Shared grid-cell sampling
The system SHALL compute, in a single pass, each grid cell's validity, its linear-light average color, and a representative color used for bead snapping, all from the preprocessed image, and SHALL expose that result for reuse by cell snapping, color selection, and reference-image production. Cell validity SHALL honor the image's explicit validity mask when one is present (falling back to per-pixel transparency otherwise), so a subject-cutout mask decides which pixels contribute to a cell. The representative color SHALL be the linear-light average under the photo preset and the palette mode under the cartoon preset, where the palette mode is the bead color that the most of the cell's valid pixels are perceptually nearest to.

#### Scenario: One pass yields average, representative, and validity
- **WHEN** the grid sampler runs for a `W × H` grid over a preprocessed image
- **THEN** it returns exactly `W × H` cell samples, each carrying a coverage/validity flag, the linear-light average of that cell's source region, and a representative color

#### Scenario: Cartoon cells reduce to the palette mode
- **WHEN** the grid sampler runs for the cartoon preset and within a cell a single bead color is the nearest for more valid pixels than any other bead
- **THEN** that bead color is the cell's representative color

#### Scenario: Photo cells reduce to the linear-light average
- **WHEN** the grid sampler runs for the photo preset
- **THEN** each cell's representative color equals its linear-light average color

#### Scenario: Generation snaps from the shared representative
- **WHEN** a pattern is generated
- **THEN** each cell's bead is chosen from the same per-cell representative produced by the grid sampler, with no second per-cell reduction pass over the working image

#### Scenario: Color selection reuses the shared samples
- **WHEN** a pattern is generated with the reference-direct color-selection strategy
- **THEN** the used bead colors are chosen from the same per-cell representatives produced by the grid sampler, with no separate sampling pass over the working image

#### Scenario: Explicit mask decides cell validity
- **WHEN** the preprocessed image carries a validity mask
- **THEN** a cell's coverage counts only the pixels the mask marks valid, and the pixels it marks invalid do not contribute to that cell's average or validity

## ADDED Requirements

### Requirement: Preprocessing preserves the validity mask
Each preprocessing operation SHALL carry the input image's validity mask through to its output, so a mask attached before preprocessing still governs cell validity after the preset's operations run. Operations that change only color (contrast, saturation, blur, sharpen) SHALL keep the mask unchanged; operations that change geometry (square padding) SHALL transform the mask consistently with the pixels.

#### Scenario: Color operations keep the mask
- **WHEN** a preset color operation (contrast, saturation, blur, or sharpen) processes an image that carries a validity mask
- **THEN** the operation's output carries the same validity mask

#### Scenario: A geometry operation maps the mask
- **WHEN** an image that carries a validity mask is padded to a square
- **THEN** the mask is padded to the same square alignment as the pixels

#### Scenario: Mask reaches grid sampling through the preset
- **WHEN** a mask is attached to the image before the preset's operations run and the grid sampler then samples that image
- **THEN** cell validity reflects the mask, not the raw pixels
