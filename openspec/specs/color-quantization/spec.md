# color-quantization Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## Requirements
### Requirement: Extract N representative image colors
When the K-means color-selection strategy is selected, the system SHALL extract N representative image colors (quantization centers) from the working image, where N (colorCountTarget) is user-configurable. Extraction SHALL be independent of the selected bead palette. This requirement applies only to the K-means strategy; the default reference-direct strategy does not extract representative colors.

#### Scenario: Quantize to configured N
- **WHEN** the K-means strategy runs with colorCountTarget = 32
- **THEN** the system extracts approximately 32 representative image colors from the working image

#### Scenario: Different N changes result
- **WHEN** the same working image is processed with the K-means strategy at N = 16 and later at N = 64
- **THEN** the two runs produce different sets of representative colors, with N = 64 retaining more color detail

#### Scenario: Decoupled from bead palette
- **WHEN** the same working image and the same N are processed with the K-means strategy while switching between two different bead palettes
- **THEN** the representative image colors extracted are identical; only the subsequent bead palette mapping differs

### Requirement: Pluggable quantizer interface
The system SHALL abstract representative-color extraction behind a quantizer interface with a registry; the K-means strategy uses it. Additional quantization algorithms MUST be addable without modifying the generation pipeline.

#### Scenario: K-means uses the quantizer registry
- **WHEN** the K-means strategy runs
- **THEN** it obtains representative colors from the registered quantizer (K-means by default) selected by id

#### Scenario: Registry resolution by id
- **WHEN** a quantizer with a given id is registered and selected
- **THEN** the K-means strategy invokes that quantizer instead of K-means with no pipeline code changes

### Requirement: Uniform sampling for performance
When the K-means strategy is selected, the system SHALL sample pixels uniformly from the working image rather than using every pixel, to keep quantization fast on large images. Reference-direct selection SHALL instead consume the grid-resolution cell samples and SHALL NOT require a separate uniform sampling pass.

#### Scenario: Large image sampling
- **WHEN** a multi-megapixel working image is processed with the K-means strategy
- **THEN** the system samples a bounded subset of pixels (thousands, not millions) and completes quantization without noticeable UI freeze

#### Scenario: Reference-direct uses cell samples
- **WHEN** reference-direct selection runs
- **THEN** it uses the per-cell representatives already computed for the grid, with no additional uniform sampling of the working image

### Requirement: Deterministic quantization via seeding
The system SHALL support deterministic quantization so that repeated runs on the same input with the same parameters can produce the same representative colors.

#### Scenario: Repeated run with seed
- **WHEN** generation is re-run on the same working image with the same N and the same seed (e.g., derived from source hash)
- **THEN** the representative colors and the resulting grid are identical to the previous run

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

### Requirement: Fall back to automatic when the color count is unset

When a project carries no usable color-count target (a non-positive value, as stored by blank free-creation and template projects, which skip quantization), the system SHALL treat the color count as automatic so the shared generation parameters are never poisoned by such a project. Automatic SHALL mean "use every bead that appears, uncapped" under the reference-direct strategy and the default automatic count under the K-means strategy. Generation SHALL always produce at least one color.

#### Scenario: Opening a blank pattern keeps the parameter valid
- **WHEN** the user opens a saved blank pattern whose stored `colorCountTarget` is 0
- **THEN** the "用色数量" parameter shows "自动" and does not hold 0

#### Scenario: Automatic is uncapped for reference-direct
- **WHEN** generation runs with the reference-direct strategy and an automatic color count after opening a blank pattern
- **THEN** the used bead set is uncapped (every bead that appears), not reduced to a single color

#### Scenario: Automatic falls back to the default count for K-means
- **WHEN** generation runs with the K-means strategy and an automatic color count
- **THEN** it uses the default automatic count (16)

#### Scenario: Generation never uses zero colors
- **WHEN** generation runs with a non-positive color-count target for any reason
- **THEN** it produces at least one color instead of a zero-color result

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

### Requirement: K-means strategy remains selectable

The system SHALL retain the existing K-means color-selection strategy — sampling the working image, extracting representative colors, and mapping them to beads — as a selectable alternative to reference-direct, and SHALL let the user choose the strategy among the generation parameters without changing the generation pipeline.

#### Scenario: User selects K-means
- **WHEN** the user selects the K-means color-selection strategy and generates
- **THEN** the representative colors are extracted from the working image as before and the pattern is produced from them

