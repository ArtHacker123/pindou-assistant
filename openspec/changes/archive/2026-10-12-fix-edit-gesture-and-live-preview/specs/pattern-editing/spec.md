# fix-edit-gesture-and-live-preview Specification

## Purpose

Correct the edit-mode gesture handling so two-finger navigation never edits the grid, single taps reliably apply the active tool, and drag strokes render live while still undoing as a single step.

## ADDED Requirements

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
