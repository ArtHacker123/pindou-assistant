## MODIFIED Requirements

### Requirement: Uniform sampling for performance
When the K-means strategy is selected, the system SHALL sample pixels uniformly from the working image rather than using every pixel, to keep quantization fast on large images. Reference-direct selection SHALL instead consume the grid-resolution cell samples and SHALL NOT require a separate uniform sampling pass.

#### Scenario: Large image sampling
- **WHEN** a multi-megapixel working image is processed with the K-means strategy
- **THEN** the system samples a bounded subset of pixels (thousands, not millions) and completes quantization without noticeable UI freeze

#### Scenario: Reference-direct uses cell samples
- **WHEN** reference-direct selection runs
- **THEN** it uses the per-cell representatives already computed for the grid, with no additional uniform sampling of the working image

### Requirement: Perceptual color comparison and averaging
The system SHALL compare and average colors in spaces that match human perception: relative color comparisons SHALL run in the Oklab space and spatial area averaging SHALL run in linear light, so nearest-bead picks and cell averages agree with human vision instead of raw sRGB arithmetic. The CIELAB (dE76) space MAY be retained as an alternative comparison space for reference and testing.

#### Scenario: Nearest bead follows perception
- **WHEN** a representative color sits between two bead palette entries
- **THEN** the bead whose perceived Oklab difference is smallest is chosen, which is not always the nearest raw-sRGB entry

#### Scenario: Cell average stays bright when mixing light and dark pixels
- **WHEN** a grid cell covers an equal mix of black and white pixels
- **THEN** the averaged color is the physically correct light midtone (~188), not the too-dark sRGB mean (~128)

#### Scenario: Cells snap to the used bead set
- **WHEN** a cell's representative color is mapped after the representative beads are fixed
- **THEN** the cell is assigned the perceptually closest bead among the beads already used by the pattern, without the extra cell-to-center indirection

### Requirement: Color-selection strategies with reference-direct default

The system SHALL choose the set of bead colors a pattern may use through a selectable color-selection strategy, and SHALL default to selecting directly from the reference image. Reference-direct selection SHALL map each valid cell's representative color to the perceptually nearest bead in the full palette; when the color count is automatic the used set SHALL contain every bead that appears (no cap); when a manual count N is set the system SHALL choose the N beads that best represent the cells by greedy maximum coverage — repeatedly adding the bead that most reduces the total perceptual distance of snapping every cell to the chosen set — and re-map every cell to the nearest bead within that set. Rare but visually distinct colors SHALL be eligible to be kept even when their area is small (selection SHALL NOT be a pure frequency ranking). Reference-direct selection SHALL be deterministic (ties broken by natural color-code order) and SHALL NOT require a seed.

#### Scenario: Default strategy is reference-direct
- **WHEN** generation runs without the user choosing a color-selection strategy
- **THEN** the used bead colors are chosen directly from the reference-image cells, not by extracting representative colors from the working image

#### Scenario: Automatic keeps every bead that appears
- **WHEN** reference-direct selection runs with an automatic color count
- **THEN** every bead color that appears in the cells is kept and no cap is applied

#### Scenario: Manual count keeps the best-representing beads
- **WHEN** reference-direct selection runs with a manual color count N and the cells map to more than N beads
- **THEN** the N beads that most reduce the cells' total perceptual error are kept and every cell is re-mapped to the nearest bead within that kept set

#### Scenario: A rare distinct color survives the cap
- **WHEN** one color covers many cells, a near-duplicate color covers many cells, and a visually distinct color covers a single cell, and the count is capped so only two can be kept
- **THEN** the distinct single-cell color is kept and the near-duplicate is dropped

#### Scenario: Deterministic without a seed
- **WHEN** reference-direct selection runs twice on the same reference cells and palette
- **THEN** the used bead set and the resulting grid are identical
