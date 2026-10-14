# pattern-editing Specification

## ADDED Requirements

### Requirement: Region erase

The eraser SHALL have a region-erase mode, toggled by double-tapping the eraser in the floating tool dock; a single tap SHALL keep selecting the eraser normally. In this mode a single tap clears the 4-connected region of cells sharing the tapped cell's color code — the same region the bucket fill would affect — while cells of other colors and empty holes remain unchanged. The change SHALL be recorded as a single undo step. The eraser SHALL clearly indicate the active mode (a distinct highlighted state on the tool button and a mode-aware tool label), and the ordinary single-cell erase SHALL remain the default.

#### Scenario: Toggle region-erase mode
- **WHEN** the user double-taps the eraser in the tool dock
- **THEN** the eraser is selected and switches to region erase (and back on the next double-tap), with the mode shown by a distinct highlight on the tool button and in the tool label

#### Scenario: A single tap still selects the eraser
- **WHEN** the user taps the eraser once
- **THEN** the eraser is selected in its ordinary single-cell mode and the region-erase mode is unchanged

#### Scenario: Erase a connected region
- **WHEN** the eraser is in region-erase mode and the user taps a bead
- **THEN** every 4-connected cell sharing that bead's color is cleared and the whole change is a single undo step

#### Scenario: Only the same color is erased
- **WHEN** the tapped region borders cells of another color or empty holes
- **THEN** those cells are left unchanged

## MODIFIED Requirements

### Requirement: Tool-appropriate drag behavior

Only the paint tool and the eraser in its ordinary (single-cell) mode SHALL draw continuously when dragging. The bucket, the eyedropper, and the eraser in its region-erase mode SHALL act on taps only, and the move tool SHALL only navigate; dragging with these tools SHALL pan/zoom without editing.

#### Scenario: Dragging with the move tool pans
- **WHEN** the move tool is active and the user drags with one finger
- **THEN** the grid pans and no cell is edited

#### Scenario: Dragging with the region eraser pans
- **WHEN** the eraser is in region-erase mode and the user drags with one finger
- **THEN** the grid pans and no cell is erased
