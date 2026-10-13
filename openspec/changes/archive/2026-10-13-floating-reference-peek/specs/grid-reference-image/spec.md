# grid-reference-image Specification

## Purpose
TBD - created by archiving change add-grid-reference-image. Update Purpose after archive.
## MODIFIED Requirements

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
