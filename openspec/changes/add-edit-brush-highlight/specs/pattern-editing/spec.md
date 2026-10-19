## ADDED Requirements

### Requirement: Edit-mode brush-linked same-color highlight

In edit mode, the system SHALL highlight every grid cell whose bead code equals the current brush color whenever the brush-linked highlight is enabled. The highlight SHALL be a translucent accent wash plus a thickened inset outline drawn in the code-label ink color (the same black/white ink used for the bead code text, which always contrasts with the bead) on matching cells only — non-matching cells SHALL NOT be dimmed or veiled, so the whole pattern stays visible for drawing. The wash and ink outline are required so marked cells remain recognizable on any bead color. The highlight SHALL follow the current brush color automatically (including colors picked with the eyedropper) and SHALL update live as cells are painted, erased, or replaced. A toggle in the editing dock SHALL enable/disable the highlight; its state SHALL persist across tool changes and mode switches within the editing session, and SHALL default to enabled. The highlight SHALL NOT alter any tool gesture: a tap still applies the active tool, drags still draw, and two-finger gestures still pan/zoom.

#### Scenario: Selecting a brush color highlights its cells
- **WHEN** edit mode is active with the brush-linked highlight enabled and the user picks bead code `A5` in the color table
- **THEN** every cell in the grid whose code is `A5` is outlined with the accent inset, and cells of other codes are shown undimmed

#### Scenario: Changing the brush switches the highlight
- **WHEN** the brush changes from `A5` to `B2` (via the color table or the eyedropper)
- **THEN** the `A5` outlines disappear and every `B2` cell is outlined instead

#### Scenario: Painting brings new cells into the highlight
- **WHEN** the user paints a previously different-colored cell with the current brush color
- **THEN** the newly painted cell shows the highlight outline on the next frame without any extra action

#### Scenario: Erasing removes cells from the highlight
- **WHEN** the user erases a cell that had the brush color
- **THEN** that cell no longer participates in the highlight (it is an empty hole and is not outlined)

#### Scenario: Global replace lights up the converted cells
- **WHEN** the brush color is `B2`, the user globally replaces another code (e.g. `A5`) with the brush color, and the replacement converts cells across the grid
- **THEN** every newly converted `B2` cell is outlined on the next frame, so the highlight shows the full extent of the replaced color; because the highlight always tracks the current brush code (never a cached set of cells), a color being replaced away can never leave stale outlines

#### Scenario: Highlight toggle turns the highlight off
- **WHEN** the user taps the highlight toggle in the editing dock to disable it, then changes the brush color
- **THEN** no cell is outlined, and tool behavior is unchanged

#### Scenario: Highlight toggle state persists within the session
- **WHEN** the user disables the highlight, switches to another tool and back, or leaves and re-enters edit mode
- **THEN** the highlight stays disabled until re-enabled

#### Scenario: Tool gestures are unaffected
- **WHEN** the brush-linked highlight is enabled and the user taps a cell with the paint tool, drags a stroke, or pans with two fingers
- **THEN** the tool applies exactly as before (the tap paints one bead, the drag draws, the two-finger gesture pans) and no highlight behavior interferes

#### Scenario: No brush color means no highlight
- **WHEN** no brush color is selected (e.g. an empty palette state)
- **THEN** no cell is outlined
