## ADDED Requirements

### Requirement: Global color replace

Edit mode SHALL let the user replace every bead of one color code with another color code in a single action. With the replace (替换) tool active, the target color SHALL be the current brush color and the source SHALL be the code of the tapped cell; a single tap on a bead cell SHALL set every cell whose code equals that source code to the brush color, across the whole grid and regardless of connectivity. Empty holes SHALL remain holes, and the reference image SHALL be unaffected. The replacement SHALL apply immediately with no confirmation and SHALL be recorded as a single undo step. When the tapped cell is an empty hole, when no brush color is selected, or when the source code already equals the brush color, the tap SHALL make no change and SHALL NOT add an undo entry. Because the brush color comes from the current palette, the target SHALL always be a color of the current palette; `paletteUsed` and the color table SHALL be recomputed from the result.

#### Scenario: Replace a color everywhere
- **WHEN** the replace tool is active with brush color `H7` and the user taps a cell whose code is `A3`
- **THEN** every cell with code `A3` in the grid becomes `H7` and the change is one undo step

#### Scenario: Only the tapped code changes
- **WHEN** the grid contains cells of code `A3` and code `B1` and the user replaces `A3`
- **THEN** every `A3` cell becomes the brush color and the `B1` cells are unchanged

#### Scenario: Replace is global, not connected
- **WHEN** beads of the same code appear in several disconnected places
- **THEN** all of them are replaced, not only the connected region around the tapped cell

#### Scenario: Holes are not replaced
- **WHEN** the grid contains empty holes
- **THEN** replacing a color leaves every hole empty

#### Scenario: Tapping a hole or the target color is a no-op
- **WHEN** the user taps an empty hole, or taps a cell whose code already equals the brush color
- **THEN** the grid is unchanged and no undo entry is added

#### Scenario: Undo restores the replaced colors
- **WHEN** the user replaces a color and then taps undo
- **THEN** the grid returns to its pre-replace colors in one step

## MODIFIED Requirements

### Requirement: Editing tools
Edit mode SHALL provide a tool bar with paint (画笔), erase (橡皮), eyedropper (取色器), bucket fill (油漆桶), replace (替换), and move (移动). Paint SHALL write the currently selected bead color; erase SHALL clear the cell to an empty hole (`null`); bucket fill SHALL flood the 4-connected region of cells sharing the target cell's color code (including `null`) with the selected color, bounded by the grid edges; replace SHALL set every cell of the tapped cell's color code to the selected bead color; move SHALL let a single-finger drag pan the viewport instead of drawing. The selected bead color SHALL be choosable from the color table.

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
Edit mode SHALL present each tool as an icon (paint, erase, eyedropper, bucket, replace, move) instead of text, and SHALL mark the active tool with a highlighted background. The tool's name SHALL remain available to accessibility (content description) and in the info bar.

#### Scenario: Drawing tools float over the canvas
- **WHEN** edit mode is active
- **THEN** the paint/erase/eyedropper/bucket/replace tools appear in a floating dock on the canvas, and dragging the dock repositions it anywhere over the pattern without starting a stroke, while a tap on a tool selects it

#### Scenario: Move, undo, redo, and the color swatch stay in the bottom bar
- **WHEN** edit mode is active
- **THEN** the bottom bar shows the move tool, undo and redo buttons (reflecting whether they are currently possible), and the current-color swatch that opens the series picker

#### Scenario: Active tool is highlighted
- **WHEN** the user selects the bucket tool
- **THEN** the bucket icon is shown as active and the other tool icons are not, and the info bar names the active tool

### Requirement: Compact draggable editing dock

In edit mode, all editing tools (move, paint, erase, eyedropper, bucket, replace) and the history actions (undo, redo) SHALL be available from a single floating dock overlaying the canvas, ordered top-to-bottom as: drag grip → move → undo → redo → divider → paint → erase → eyedropper → bucket → replace. The dock SHALL be draggable via a dedicated grip area, SHALL stay within the canvas viewport, and SHALL NOT cover the canvas more than necessary. Undo and redo SHALL appear disabled (not tappable) when there is nothing to undo or redo.

#### Scenario: All tools and history in one dock
- **WHEN** the editor is in edit mode
- **THEN** the move/paint/erase/eyedropper/bucket/replace tools and the undo/redo actions are all reachable from the floating dock

#### Scenario: Undo/redo reflect availability
- **WHEN** there is no edit to undo (or redo)
- **THEN** the corresponding action in the dock is shown disabled and does not respond to taps

#### Scenario: Dock grip drags the dock
- **WHEN** the user drags the grip area at the top of the dock
- **THEN** the dock moves with the finger

#### Scenario: Dock stays on the canvas
- **WHEN** the user drags the dock toward any edge of the canvas, or the viewport size changes
- **THEN** the dock is kept fully inside the canvas viewport and cannot be dragged out of view

### Requirement: Tool-appropriate drag behavior

Only the paint tool and the eraser in its ordinary (single-cell) mode SHALL draw continuously when dragging. The bucket, the eyedropper, the replace tool, and the eraser in its region-erase mode SHALL act on taps only, and the move tool SHALL only navigate; dragging with these tools SHALL pan/zoom without editing.

#### Scenario: Dragging with the move tool pans
- **WHEN** the move tool is active and the user drags with one finger
- **THEN** the grid pans and no cell is edited

#### Scenario: Dragging with the region eraser pans
- **WHEN** the eraser is in region-erase mode and the user drags with one finger
- **THEN** the grid pans and no cell is erased

### Requirement: Single taps apply the active tool

A one-finger tap that does not move beyond the touch slop and never involves a second finger SHALL apply the active tool once at the tapped cell: paint places one bead, erase clears one bead, bucket floods the connected region, replace substitutes the tapped cell's code everywhere with the brush color, and the eyedropper picks the color.

#### Scenario: Tap to paint one cell
- **WHEN** the paint tool is active and the user taps a cell without moving
- **THEN** that single cell receives the brush color

#### Scenario: Tap to fill or pick
- **WHEN** the bucket tool is active and the user taps a cell (or the eyedropper tool is active and the user taps a cell)
- **THEN** the region is flood-filled (or the color is picked) exactly once

#### Scenario: Tap to replace a color
- **WHEN** the replace tool is active and the user taps a bead cell without moving
- **THEN** every cell of that bead's code is set to the brush color exactly once

### Requirement: Undo and redo edit actions
Edit mode SHALL provide undo (撤销) and redo (重做). Grid changes made by editing, global color replacement, palette re-maps, and blank resizes SHALL be undoable. All cell changes of a single drag stroke SHALL be undone as one step. Undoing SHALL restore the previous grid; redoing SHALL re-apply it. A new edit SHALL clear the redo history. The history SHALL be cleared when opening or starting a project.

#### Scenario: Undo and redo a stroke
- **WHEN** the user paints a multi-cell stroke, then taps undo once, then taps redo once
- **THEN** the stroke's cells revert in one step and are re-applied in one step

#### Scenario: New edit clears redo
- **WHEN** the user undoes a change and then makes a new edit
- **THEN** the redo button becomes disabled and the undone change cannot be re-applied

#### Scenario: Undo restores after a remap
- **WHEN** the user re-maps the palette to a new one and then taps undo
- **THEN** the grid returns to the pre-remap colors

#### Scenario: Undo restores after a global replace
- **WHEN** the user globally replaces a color and then taps undo
- **THEN** the grid returns to its pre-replace colors

#### Scenario: Regeneration clears the history
- **WHEN** the user edits a grid, then regenerates it (e.g. by changing the size), then taps undo
- **THEN** undo is unavailable (no stale pre-edited grid is restored) because the regenerated grid is a fresh base
