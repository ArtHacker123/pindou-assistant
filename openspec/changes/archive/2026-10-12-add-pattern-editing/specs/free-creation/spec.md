## ADDED Requirements

### Requirement: Start a blank pattern from scratch
The system SHALL let the user start a blank pattern by choosing a palette and a square grid size, then open it directly in edit mode. The default SHALL be 52×52 with the default palette. A free-created project SHALL have no source image and SHALL save/export through the existing `.pindou` schema without requiring regeneration.

#### Scenario: Create a blank pattern
- **WHEN** the user chooses "自由创作", picks a palette and size, and confirms
- **THEN** the editor opens in edit mode with a blank grid of that size and the project uses the chosen palette

#### Scenario: Defaults
- **WHEN** the user opens free creation without changing the defaults
- **THEN** the size is 52×52 and the palette is the default palette

#### Scenario: Blank project saves and exports
- **WHEN** the user edits a free-created pattern, then saves `.pindou` or exports PNG
- **THEN** the output contains the edited grid, with no source image required

### Requirement: Load a reference image during creation
In edit mode the system SHALL let the user load a reference image at any time. The reference SHALL be built at the grid resolution and aligned 1:1 with the grid, using the same preprocessing and reference pipeline as generation, and SHALL reuse it while the working image, image type, and grid size are unchanged.

#### Scenario: Load a reference while editing
- **WHEN** the user loads a reference image in edit mode
- **THEN** a grid-resolution reference aligned to the cells becomes available as an underlay

#### Scenario: Reference aligns with the grid
- **WHEN** a reference is loaded for a `W × H` grid
- **THEN** the reference is `W × H` and cell `(row, col)` maps to reference pixel `(col, row)` with no offset
