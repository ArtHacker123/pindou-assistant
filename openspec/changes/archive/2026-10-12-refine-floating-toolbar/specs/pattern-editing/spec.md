# refine-floating-toolbar Specification

## Purpose

Document the editing control surface: a single, compact, draggable dock that carries every editing tool and the undo/redo history actions, and always stays on the canvas.

## ADDED Requirements

### Requirement: Compact draggable editing dock

In edit mode, all editing tools (move, paint, erase, eyedropper, bucket) and the history actions (undo, redo) SHALL be available from a single floating dock overlaying the canvas, ordered top-to-bottom as: drag grip → move → undo → redo → divider → paint → erase → eyedropper → bucket. The dock SHALL be draggable via a dedicated grip area, SHALL stay within the canvas viewport, and SHALL NOT cover the canvas more than necessary. Undo and redo SHALL appear disabled (not tappable) when there is nothing to undo or redo.

#### Scenario: All tools and history in one dock
- **WHEN** the editor is in edit mode
- **THEN** the move/paint/erase/eyedropper/bucket tools and the undo/redo actions are all reachable from the floating dock, with the move tool first and undo/redo directly below it

#### Scenario: Undo/redo reflect availability
- **WHEN** there is no edit to undo (or redo)
- **THEN** the corresponding action in the dock is shown disabled and does not respond to taps

#### Scenario: Dock grip drags the dock
- **WHEN** the user drags the grip area at the top of the dock
- **THEN** the dock moves with the finger

#### Scenario: Dock stays on the canvas
- **WHEN** the user drags the dock toward any edge of the canvas, or the viewport size changes
- **THEN** the dock is kept fully inside the canvas viewport and cannot be dragged out of view
