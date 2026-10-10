# color-quantization Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## Requirements
### Requirement: Extract N representative image colors
The system SHALL extract N representative image colors (quantization centers) from the working image, where N (colorCountTarget) is user-configurable. Extraction SHALL be independent of the selected bead palette.

#### Scenario: Quantize to configured N
- **WHEN** the user sets colorCountTarget = 32 and starts generation
- **THEN** the system extracts approximately 32 representative image colors from the working image

#### Scenario: Different N changes result
- **WHEN** the same working image is processed with N = 16 and later with N = 64
- **THEN** the two runs produce different sets of representative colors, with N = 64 retaining more color detail

#### Scenario: Decoupled from bead palette
- **WHEN** the same working image and the same N are processed while switching between two different bead palettes
- **THEN** the representative image colors extracted are identical; only the subsequent bead palette mapping differs

### Requirement: Pluggable quantizer interface
The system SHALL abstract representative-color extraction behind a quantizer interface with a registry. The first implementation SHALL be K-means. Additional algorithms MUST be addable without modifying the generation pipeline.

#### Scenario: Default quantizer is K-means
- **WHEN** generation runs with default settings
- **THEN** the K-means quantizer is used to produce representative colors

#### Scenario: Registry resolution by id
- **WHEN** a quantizer with a given id is registered and selected
- **THEN** the pipeline invokes that quantizer instead of K-means with no pipeline code changes

### Requirement: Uniform sampling for performance
The system SHALL sample pixels uniformly from the working image rather than using every pixel, to keep quantization fast on large images.

#### Scenario: Large image sampling
- **WHEN** a multi-megapixel working image is processed
- **THEN** the system samples a bounded subset of pixels (thousands, not millions) and completes quantization without noticeable UI freeze

### Requirement: Deterministic quantization via seeding
The system SHALL support deterministic quantization so that repeated runs on the same input with the same parameters can produce the same representative colors.

#### Scenario: Repeated run with seed
- **WHEN** generation is re-run on the same working image with the same N and the same seed (e.g., derived from source hash)
- **THEN** the representative colors and the resulting grid are identical to the previous run

### Requirement: Perceptual color comparison and averaging
The system SHALL compare and average colors in spaces that match human perception: relative color comparisons SHALL run in CIELAB (dE76) and spatial area averaging SHALL run in linear light, so nearest-bead picks and cell averages agree with human vision instead of raw sRGB arithmetic.

#### Scenario: Nearest bead follows perception
- **WHEN** a representative color sits between two bead palette entries
- **THEN** the bead whose perceived difference is smallest (per dE76) is chosen, which is not always the nearest raw-sRGB entry

#### Scenario: Cell average stays bright when mixing light and dark pixels
- **WHEN** a grid cell covers an equal mix of black and white pixels
- **THEN** the averaged color is the physically correct light midtone (~188), not the too-dark sRGB mean (~128)

#### Scenario: Cells snap to the used bead set
- **WHEN** a cell's average color is mapped after the representative beads are fixed
- **THEN** the cell is assigned the perceptually closest bead among the beads already used by the pattern, without the extra cell-to-center indirection

