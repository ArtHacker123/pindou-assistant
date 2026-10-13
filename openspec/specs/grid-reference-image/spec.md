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

The editor SHALL let the user view the reference image and the pattern independently so they can judge fidelity and edit a pattern over the reference. The reference SHALL render as a base layer under the cells and the bead layer SHALL render opaquely on top, so the reference is visible only where there is no bead. The editor SHALL provide a draggable, circular press-and-hold control (labelled "按住看参考图") that, while held, temporarily forces reference-only rendering and restores the prior layer state on release; it SHALL be shown whenever the current project has a reference image, in both view and edit modes, and SHALL be draggable anywhere within the canvas viewport while clamped to stay fully on screen. In view mode the reference SHALL be hidden by default: no reference toggle SHALL be shown, so the pattern (bead layer) is displayed and the press-and-hold control is the only way to reveal the reference. In edit mode the beads SHALL always be shown, the reference SHALL remain toggleable from the edit action bar, and the press-and-hold compare SHALL also remain available. Grid lines, group guides, empty-hole hatching and the selection spotlight SHALL render unchanged.

#### Scenario: Reference as an underlay
- **WHEN** the reference is shown together with the bead layer
- **THEN** cells with a bead render the opaque bead color and cells without a bead reveal the reference color, so the reference underlays the editable grid

#### Scenario: View mode hides the reference by default
- **WHEN** the editor is in view mode with a reference available and the user is not pressing the peek control
- **THEN** only the pattern (bead layer) is shown and no reference toggle is presented

#### Scenario: Press-and-hold compare
- **WHEN** the user presses and holds the "按住看参考图" control
- **THEN** the canvas temporarily shows reference-only, and releasing restores the layer state that was active before the press

#### Scenario: Peek control is draggable and stays on the canvas
- **WHEN** the user drags the circular peek control
- **THEN** it moves with the finger and is clamped so it remains fully inside the canvas viewport

#### Scenario: Compare with empty holes
- **WHEN** the reference layer is shown and a cell is an empty hole in the reference
- **THEN** the cell renders its usual hole treatment (no reference color shown)

#### Scenario: Reference overlay in edit mode
- **WHEN** the user is in edit mode with a reference loaded
- **THEN** the reference can be toggled from the edit action bar and the press-and-hold peek works over the always-visible bead layer

#### Scenario: Editing over the reference
- **WHEN** the reference is shown in edit mode and the user edits a cell
- **THEN** the edited bead renders opaquely on top of the reference for that cell

### Requirement: Load a reference image through the shared crop screen

The editor SHALL let the user load a reference image. After the user selects an image, the system SHALL proceed through the same crop/scale screen used for pattern generation. On confirmation, the system SHALL pad the cropped region to a centered square (the same square-padding used before generation) and derive the grid-resolution reference from it, then attach it to the current project without regenerating the pattern and return to the editor. Cancelling the crop SHALL return to the editor without changing the reference.

#### Scenario: Selecting a reference image opens the crop screen
- **WHEN** the user chooses to load a reference image and picks an image
- **THEN** the crop/scale screen opens with that image, exactly as for pattern generation

#### Scenario: Confirming the crop attaches the reference
- **WHEN** the user confirms the crop while loading a reference
- **THEN** the grid-resolution reference derived from the cropped region is attached to the current project and the editor is shown, with the pattern left unchanged

#### Scenario: Reference crop is squared to match the grid
- **WHEN** the user confirms a reference crop whose region is not square
- **THEN** the region is padded to a centered square before the reference is derived, so the reference maps 1:1 to the grid without stretching

#### Scenario: Cancelling the crop leaves the reference unchanged
- **WHEN** the user backs out of the crop screen while loading a reference
- **THEN** the editor is shown and the current reference is not modified

### Requirement: Reference-load entry availability

The editor's edit action bar SHALL provide the reference controls, positioned to the left of the brush-color control. A load entry labelled "导入参考图" SHALL be shown only when the current project has no source image (free creation or an opened project), and SHALL be hidden for a project generated from an image, whose reference is derived from the source automatically.

When the current project has a reference image, the action bar SHALL additionally show a "显示参考图" toggle that indicates its active state (with a trailing ✓ while the reference is visible) and SHALL show or hide the reference over the pattern when tapped. Importing a reference SHALL activate this toggle (the reference is shown by default), and the show/hide state SHALL be held in the navigation state so it survives the editor → crop → editor detour of the import. Generating from a source image, opening a project, and starting a free creation SHALL reset it to hidden.

#### Scenario: Free creation shows the entry
- **WHEN** the editor is in edit mode for a project with no source image
- **THEN** the "导入参考图" entry is visible in the edit action bar to the left of the brush-color control

#### Scenario: Generated project hides the entry
- **WHEN** the editor is in edit mode for a project generated from a source image
- **THEN** the "导入参考图" entry is not shown

#### Scenario: Show toggle reflects and changes visibility
- **WHEN** the project has a reference image and the user taps "显示参考图"
- **THEN** the reference is shown or hidden over the pattern accordingly, and the toggle shows a trailing ✓ while the reference is visible

#### Scenario: Import shows the reference by default
- **WHEN** the user imports a reference image and confirms the crop
- **THEN** the editor returns with the reference visible and the "显示参考图" toggle active

