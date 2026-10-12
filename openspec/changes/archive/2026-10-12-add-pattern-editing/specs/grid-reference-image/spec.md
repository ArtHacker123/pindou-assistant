## MODIFIED Requirements

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
