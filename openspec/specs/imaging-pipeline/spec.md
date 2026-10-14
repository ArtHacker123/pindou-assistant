# imaging-pipeline Specification

## Purpose
TBD - created by archiving change refactor-imaging-and-state. Update Purpose after archive.
## Requirements
### Requirement: Composable image preprocessing pipeline
The system SHALL apply image-type preprocessing as an ordered pipeline of independent image operations, so that an image-type preset is a named sequence of operations rather than a monolithic processor. Adding a new operation or preset MUST NOT require modifying the generation pipeline.

#### Scenario: Preset is an ordered operation chain
- **WHEN** a generation run uses the cartoon or photo preset
- **THEN** the working image is produced by applying that preset's operations in the defined order

#### Scenario: Existing presets preserve output
- **WHEN** the cartoon or photo preset is applied to a fixed input image through the new pipeline
- **THEN** the result is identical to the previous whole-image processor output for the same input

#### Scenario: New stage without pipeline change
- **WHEN** a new image operation is added to a preset's sequence
- **THEN** it takes effect without any change to the generation orchestration code

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

### Requirement: Reference image produced independently of quantization
The system SHALL build the grid-resolution reference image directly from a preprocessed image and a grid size, without running quantization or palette mapping, and the result SHALL equal the reference produced during generation for the same inputs.

#### Scenario: Build reference without quantizing
- **WHEN** a grid-resolution reference is requested for an image and grid size
- **THEN** it is produced from per-cell sampling alone, with no quantizer or palette involved

#### Scenario: Reference matches generation
- **WHEN** the reference builder and a generation run are given the same preprocessed image and grid size
- **THEN** the standalone reference is identical to the reference attached to the generated project

#### Scenario: Empty holes are transparent
- **WHEN** a cell's source region is below the validity threshold
- **THEN** the corresponding reference pixel has zero alpha

