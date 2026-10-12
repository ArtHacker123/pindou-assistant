# grid-reference-image Specification

## Purpose
TBD - created by archiving change add-grid-reference-image. Update Purpose after archive.
## Requirements
### Requirement: Produce a grid-resolution reference image
The system SHALL produce, as part of generation, a reference image whose dimensions equal the grid (`gridW × gridH`), storing one pixel per cell. Each pixel SHALL be the linear-light average of that cell's source region (the same computation used for cell snapping), so its brightness matches perception rather than a raw sRGB mean. The image SHALL be aligned 1:1 with the bead grid: pixel `(col, row)` SHALL correspond to the cell `Grid.cells[row * gridW + col]`, requiring no resampling when layered against the pattern.

#### Scenario: One pixel per cell, aligned to the grid
- **WHEN** generation completes for a `W × H` grid
- **THEN** the reference image is exactly `W × H` and cell `(row, col)` maps to reference pixel `(col, row)` with no offset or scaling

#### Scenario: Averaging stays perceptually linear
- **WHEN** a cell's source region is an equal mix of black and white pixels
- **THEN** that reference pixel holds the light midtone (~188) rather than the too-dark sRGB mean (~128)

#### Scenario: Empty holes are transparent
- **WHEN** a cell's source region is below the validity threshold (`MIN_CELL_VALID`)
- **THEN** the corresponding reference pixel has zero alpha, and the cell is an empty hole in the grid

#### Scenario: Uniform mapping over a padded working image
- **WHEN** the working image was padded to square before generation
- **THEN** each reference pixel covers the same-size source region per cell (no X/Y stretch), consistent with the grid's uniform mapping

### Requirement: Reference image is available on the generated project
The reference image SHALL be attached to the generated project and accessible to the UI without re-deriving it from the source image. When a generation is asked to reuse an existing reference of matching dimensions, it SHALL reuse it for both the artifact and cell snapping instead of recomputing the per-cell average.

#### Scenario: Available in session
- **WHEN** a project is generated
- **THEN** the reference image is retrievable from the project in memory

#### Scenario: Reuse on color-count-only change
- **WHEN** the color count (or palette, seed, or quantizer) changes but the working image, image type and grid size stay the same
- **THEN** the existing reference is reused rather than recomputed, and the result is identical to computing it fresh

#### Scenario: Recompute on layout-affecting change
- **WHEN** the working image, image type, or grid size changes
- **THEN** a new reference image is computed for the new layout

### Requirement: Compare source reference against the generated pattern
The editor SHALL let the user view the reference image and the pattern independently so they can judge fidelity and edit a pattern over the reference. The reference SHALL render as a base layer under the cells and the bead layer SHALL render opaquely on top, so the reference is visible only where there is no bead. In view mode the editor SHALL provide a "图纸" toggle for the bead layer, a "缩略图" toggle for the reference layer (never both off), and a press-and-hold control that temporarily forces reference-only while held and restores the prior state on release. In edit mode the beads SHALL always be shown, so the "图纸" toggle SHALL be hidden; the reference toggle and the press-and-hold compare SHALL remain available. Grid lines, group guides, empty-hole hatching and the selection spotlight SHALL render unchanged.

#### Scenario: Reference as an underlay
- **WHEN** the reference layer is shown and the bead layer is also shown
- **THEN** cells with a bead render the opaque bead color and cells without a bead reveal the reference color, so the reference underlays the editable grid

#### Scenario: Reference only
- **WHEN** the bead layer is hidden (via its toggle, or `缩略图` on with `图纸` off)
- **THEN** the canvas shows the reference image for the full grid while grid lines and the selection highlight remain visible

#### Scenario: Press-and-hold compare
- **WHEN** the user presses and holds the "按住看原图" control
- **THEN** the canvas temporarily shows reference-only, and releasing restores the toggle state that was active before the press

#### Scenario: At least one layer stays visible
- **WHEN** only one layer is currently shown and the user tries to turn it off
- **THEN** the other layer is enabled instead, so the canvas is never fully blank

#### Scenario: Compare with empty holes
- **WHEN** the reference layer is shown and a cell is an empty hole in the reference
- **THEN** the cell renders its usual hole treatment (no reference color shown)

#### Scenario: Reference overlay in edit mode
- **WHEN** the user is in edit mode with a reference loaded
- **THEN** the reference toggle and press-and-hold peek work over the always-visible bead layer, and the separate "图纸" toggle is not shown

#### Scenario: Editing over the reference
- **WHEN** the reference is shown in edit mode and the user edits a cell
- **THEN** the edited bead renders opaquely on top of the reference for that cell

