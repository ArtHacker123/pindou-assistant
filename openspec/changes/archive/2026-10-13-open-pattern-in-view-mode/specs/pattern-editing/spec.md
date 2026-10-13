# pattern-editing Specification

## Purpose

Define how a generated pattern is interactively viewed and edited in the editor.

## MODIFIED Requirements

### Requirement: Edit mode over the pattern grid
The system SHALL provide an edit mode over the same pattern grid, reached from the pattern screen's view mode. Switching to edit mode SHALL keep the project, the viewport, and the reference overlay intact. The "完成" action SHALL return to view mode. Opening an existing pattern — from the home recent list, the history page, or an external `.pindou` — SHALL enter view mode (图纸), never edit mode, regardless of any previous editing session. Free-created projects and templates SHALL open directly in edit mode.

#### Scenario: Enter and leave edit mode
- **WHEN** the user taps "编辑" in view mode
- **THEN** the screen switches to edit mode over the same grid, and tapping "完成" returns to view mode with the edited grid retained

#### Scenario: Viewport preserved across the mode switch
- **WHEN** the user has panned/zoomed in view mode and enters edit mode
- **THEN** the grid stays at the same scale and position

#### Scenario: Opening a saved pattern starts in view mode
- **WHEN** the user opens an existing pattern from the home recent list, the history page, or an external `.pindou`
- **THEN** the editor opens in view mode with the title "图纸", not in edit mode
