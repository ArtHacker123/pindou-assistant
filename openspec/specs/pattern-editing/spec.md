# pattern-editing Specification

## Purpose
TBD - created by archiving change add-pattern-editing. Update Purpose after archive.
## Requirements
### Requirement: Edit mode over the pattern grid
The system SHALL provide an edit mode over the same pattern grid, reached from the pattern screen's view mode. Switching to edit mode SHALL keep the project, the viewport, and the reference overlay intact. The "完成" action SHALL return to view mode. Free-created projects SHALL open directly in edit mode.

#### Scenario: Enter and leave edit mode
- **WHEN** the user taps "编辑" in view mode
- **THEN** the screen switches to edit mode over the same grid, and tapping "完成" returns to view mode with the edited grid retained

#### Scenario: Viewport preserved across the mode switch
- **WHEN** the user has panned/zoomed in view mode and enters edit mode
- **THEN** the grid stays at the same scale and position

### Requirement: Editing tools
Edit mode SHALL provide a tool bar with paint (画笔), erase (橡皮), eyedropper (取色器), bucket fill (油漆桶), and move (移动). Paint SHALL write the currently selected bead color; erase SHALL clear the cell to an empty hole (`null`); bucket fill SHALL flood the 4-connected region of cells sharing the target cell's color code (including `null`) with the selected color, bounded by the grid edges; move SHALL let a single-finger drag pan the viewport instead of drawing. The selected bead color SHALL be choosable from the color table.

#### Scenario: Paint a cell
- **WHEN** the paint tool is active with bead color `C5` and the user acts on an empty cell
- **THEN** that cell's code becomes `C5` and it renders as a bead

#### Scenario: Erase a cell
- **WHEN** the erase tool is active and the user acts on a bead cell
- **THEN** that cell becomes an empty hole with no bead and no code

#### Scenario: Move tool pans instead of drawing
- **WHEN** the move tool is active and the user drags with a single finger
- **THEN** the viewport pans and no cell is edited

#### Scenario: Bucket fill a region
- **WHEN** the bucket tool is active and the user acts on a cell
- **THEN** every 4-connected cell sharing that cell's color code is set to the selected color

#### Scenario: Bucket fill respects holes
- **WHEN** the user bucket-fills an empty hole
- **THEN** the connected run of empty holes is filled with the selected color and bead cells are left unchanged

#### Scenario: Eyedropper picks an existing bead
- **WHEN** the eyedropper tool is active and the user acts on a cell that has a bead
- **THEN** the selected brush color becomes that bead's code

#### Scenario: Eyedropper from the reference on a hole
- **WHEN** the eyedropper acts on an empty hole while a reference image is available
- **THEN** the reference color at that cell is snapped to the nearest bead color of the current palette and becomes the selected brush color

### Requirement: Editing tool bar uses icons
Edit mode SHALL present each tool as an icon (paint, erase, eyedropper, bucket, move) instead of text, and SHALL mark the active tool with a highlighted background. The tool's name SHALL remain available to accessibility (content description) and in the info bar.

#### Scenario: Drawing tools float over the canvas
- **WHEN** edit mode is active
- **THEN** the paint/erase/eyedropper/bucket tools appear in a floating dock on the canvas, and dragging the dock repositions it anywhere over the pattern without starting a stroke, while a tap on a tool selects it

#### Scenario: Move, undo, redo, and the color swatch stay in the bottom bar
- **WHEN** edit mode is active
- **THEN** the bottom bar shows the move tool, undo and redo buttons (reflecting whether they are currently possible), and the current-color swatch that opens the series picker

#### Scenario: Active tool is highlighted
- **WHEN** the user selects the bucket tool
- **THEN** the bucket icon is shown as active and the other tool icons are not, and the info bar names the active tool

### Requirement: Bead color picker grouped by series
The system SHALL let the user choose the brush color from the full palette, grouped by bead series (the leading letters of a color code, e.g. `A`, `C`, `M`). The picker SHALL be opened by tapping the current-color swatch in the tool bar, SHALL offer a series selector for quick indexing, and SHALL set the brush color when a swatch is tapped. When entering edit mode with no brush color selected, the brush color SHALL default to the palette color closest to pure black.

#### Scenario: Open the picker and choose a color
- **WHEN** the user taps the current-color swatch in the tool bar
- **THEN** the picker opens with series selectors, and tapping a swatch sets that color as the brush color and closes the picker

#### Scenario: Default brush is black
- **WHEN** the user enters edit mode (including starting free creation) with no brush color selected
- **THEN** the brush color defaults to the palette color closest to pure black (e.g. `H7` on the MARD 221 palette), so the current-color swatch is always available to open the picker

#### Scenario: Series selectors are distinguishable and scrollable
- **WHEN** the color picker is open
- **THEN** each series selector shows a representative series color (e.g. the average color of its swatches) next to its letter, and left/right arrows scroll the selector row, disabling at either end

### Requirement: Cursor highlight in edit mode
While the user draws in edit mode, the system SHALL highlight the grid cell currently under the finger with an outline sized to one cell. The highlight SHALL clear when the gesture ends or the finger leaves the grid.

#### Scenario: Highlight follows the finger
- **WHEN** the user presses and drags across cells in edit mode
- **THEN** the cell under the finger is outlined, and the outline disappears on release

### Requirement: Eyedropper prefers the reference during a peek
While the reference image is shown on its own (the press-and-hold peek temporarily shows only the reference), the eyedropper SHALL sample the reference first — even over an existing bead — so the picked color matches what the user sees. Without a reference image it SHALL fall back to the bead code.

#### Scenario: Pick the visible reference color over a bead
- **WHEN** the user peeks (holding "按住看原图") and taps a cell that has a bead with the eyedropper
- **THEN** the brush color becomes the nearest palette color to that cell's reference pixel, not the bead's code

#### Scenario: Peek without a reference
- **WHEN** the user uses the eyedropper while peeking but no reference image is available
- **THEN** the brush color falls back to the tapped bead's code

### Requirement: Edits apply live to the in-memory project
Every edit SHALL mutate the in-memory project grid immediately, with no separate commit step; the edited grid SHALL be what is exported and saved. Empty holes SHALL remain excluded from `paletteUsed`, per-code counts, and the color table.

#### Scenario: Edit reflected immediately
- **WHEN** the user paints a cell
- **THEN** the canvas shows the new bead at once and the color table / counts reflect it

#### Scenario: Edited grid is what is exported
- **WHEN** the user exports PNG or saves `.pindou` after editing
- **THEN** the output contains the edited grid

### Requirement: Draw using the viewport transform
In edit mode the system SHALL map touch coordinates to grid cells through the inverse of the current viewport transform, and SHALL ignore touches outside the grid. A single-finger drag SHALL paint along the swept cells; a two-finger gesture SHALL pan/zoom instead of drawing.

#### Scenario: Draw while zoomed
- **WHEN** the view is zoomed and panned and the user draws on a visible cell
- **THEN** the correct grid cell is edited

#### Scenario: Drawing outside the grid is ignored
- **WHEN** the user draws in the empty space outside the grid bounds
- **THEN** no cell is changed

#### Scenario: Pinch pans instead of drawing
- **WHEN** the user performs a two-finger pinch or pan in edit mode
- **THEN** the viewport pans/zooms and no cell is edited

### Requirement: Palette change re-maps the grid
Changing the palette SHALL prompt for confirmation and, on confirm, remap every non-null cell to the nearest color of the new palette by perceived color distance (CIELAB / dE76). Empty holes SHALL stay holes, and `paletteUsed` / per-code counts SHALL be recomputed from the result. Cancelling SHALL leave the palette and grid unchanged.

#### Scenario: Remap to nearest colors
- **WHEN** the user changes the palette and confirms, and a cell's bead code has no exact match in the new palette
- **THEN** that cell's code becomes the nearest color in the new palette

#### Scenario: Holes survive a remap
- **WHEN** the palette is changed while the grid contains empty holes
- **THEN** those holes remain empty holes

#### Scenario: Cancelling a palette change
- **WHEN** the user changes the palette and cancels the confirmation
- **THEN** the palette and every cell are unchanged

### Requirement: Grid-size change regenerates or clears
Changing the grid size SHALL behave differently by project type. For a project with a source image that has not been edited, the change SHALL regenerate the pattern at the new size without any prompt (nothing is lost). For a project with a source image that has been edited, the change SHALL prompt (regeneration discards the edits) and, on confirm, regenerate. For a project with no source image (e.g. free creation), the change SHALL prompt and, on confirm, clear the grid to a blank grid of the new size (all cells `null`). The system SHALL NOT crop, scale, or extend existing cells. Cancelling SHALL leave the size and grid unchanged.

#### Scenario: Regenerate when untouched
- **WHEN** the user changes the grid size of a source-image project that has no manual edits
- **THEN** the pattern regenerates at the new size without a confirmation prompt

#### Scenario: Confirm discard then regenerate
- **WHEN** the user changes the grid size of a source-image project that has manual edits
- **THEN** the system asks to confirm that edits will be discarded, and regenerates at the new size on confirm

#### Scenario: Resize clears a free-created project
- **WHEN** the user changes the grid size of a project with no source image and confirms
- **THEN** the grid becomes the new size with every cell empty

#### Scenario: Cancelling a resize
- **WHEN** the user changes the grid size and cancels the confirmation
- **THEN** the size and the grid stay as they were

### Requirement: Undo and redo edit actions
Edit mode SHALL provide undo (撤销) and redo (重做). Grid changes made by editing, palette re-maps, and blank resizes SHALL be undoable. All cell changes of a single drag stroke SHALL be undone as one step. Undoing SHALL restore the previous grid; redoing SHALL re-apply it. A new edit SHALL clear the redo history. The history SHALL be cleared when opening or starting a project.

#### Scenario: Undo and redo a stroke
- **WHEN** the user paints a multi-cell stroke, then taps undo once, then taps redo once
- **THEN** the stroke's cells revert in one step and are re-applied in one step

#### Scenario: New edit clears redo
- **WHEN** the user undoes a change and then makes a new edit
- **THEN** the redo button becomes disabled and the undone change cannot be re-applied

#### Scenario: Undo restores after a remap
- **WHEN** the user re-maps the palette to a new one and then taps undo
- **THEN** the grid returns to the pre-remap colors

#### Scenario: Regeneration clears the history
- **WHEN** the user edits a grid, then regenerates it (e.g. by changing the size), then taps undo
- **THEN** undo is unavailable (no stale pre-edited grid is restored) because the regenerated grid is a fresh base

### Requirement: Regeneration after manual edits requires confirmation
Because generating from the source image overwrites the grid, the system SHALL ask for confirmation before running a regeneration that would discard manual edits made since the last generation. Cancelling SHALL leave the edited grid untouched.

#### Scenario: Confirm discard on regenerate
- **WHEN** the grid has manual edits and the user changes a generation parameter or triggers regeneration
- **THEN** the system asks to confirm that edits will be discarded, and only regenerates on confirm

#### Scenario: No prompt when there are no edits
- **WHEN** the grid has no manual edits since the last generation
- **THEN** parameter changes regenerate without a confirmation prompt

### Requirement: Two-finger navigation never edits the grid

In edit mode, editing SHALL only begin after the gesture is classified. No cell SHALL be edited on finger-down. When a second finger is present at any point before a stroke has been classified, the gesture SHALL be treated as pan/zoom and SHALL NOT edit any cell. If a stroke has already started and a second finger is added, the cells already drawn SHALL be kept and the gesture SHALL switch to navigation for the remainder.

#### Scenario: Two-finger pan leaves no stray cell
- **WHEN** the user places one finger then a second finger and drags with both to pan the grid
- **THEN** no cell is painted, erased, or filled

#### Scenario: Second finger during a stroke
- **WHEN** the user starts painting with one finger and then adds a second finger
- **THEN** the cells drawn so far remain and further motion pans/zooms the grid instead of drawing

### Requirement: Single taps apply the active tool

A one-finger tap that does not move beyond the touch slop and never involves a second finger SHALL apply the active tool once at the tapped cell: paint places one bead, erase clears one bead, bucket floods the connected region, and the eyedropper picks the color.

#### Scenario: Tap to paint one cell
- **WHEN** the paint tool is active and the user taps a cell without moving
- **THEN** that single cell receives the brush color

#### Scenario: Tap to fill or pick
- **WHEN** the bucket tool is active and the user taps a cell (or the eyedropper tool is active and the user taps a cell)
- **THEN** the region is flood-filled (or the color is picked) exactly once

### Requirement: Drag strokes render live

While a paint or erase stroke is in progress, each cell changed SHALL be reflected in the project immediately so the canvas updates as the finger moves. The stroke SHALL still be recorded as a single undo step when it ends.

#### Scenario: Painting updates immediately
- **WHEN** the user drags the paint tool across cells
- **THEN** beads appear under the finger as it moves, before the finger is lifted

#### Scenario: One undo step per stroke
- **WHEN** the user drags a multi-cell stroke and then taps undo once
- **THEN** the entire stroke is removed in one step

### Requirement: Tool-appropriate drag behavior

Only the paint and erase tools SHALL draw continuously when dragging. The bucket and eyedropper tools SHALL act on taps only, and the move tool SHALL only navigate; dragging with these tools SHALL pan/zoom without editing.

#### Scenario: Dragging with the move tool pans
- **WHEN** the move tool is active and the user drags with one finger
- **THEN** the grid pans and no cell is edited

