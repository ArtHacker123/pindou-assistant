# fix-editor-exit-navigation Specification

## Purpose

This change corrects two editor exit behaviors:

- When the user tries to leave the editor to Home while the project has unsaved changes, the system must ask whether to save before leaving.
- The "完成" button must always return to view mode in the editor; it must not exit to Home (even for free-created projects).

## MODIFIED Requirements

### Requirement: "完成" returns to view mode

The editor's "完成" action SHALL always return the screen to view mode. It SHALL NOT navigate directly to Home for any project type.

#### Scenario: 完成 returns to view mode
- **WHEN** the user taps "完成" while in edit mode of any project (including a free-created project)
- **THEN** the editor remains on the pattern screen and switches to view mode (the edited grid is retained and visible)

### Requirement: Back action routes by context

When the user leaves the editor area via the back button or system back gesture, the system SHALL behave as follows:
- **IF** the screen is currently in edit mode of a project that has a source image (not free-created), then back SHALL return to view mode.
- **IF** the screen is currently in edit mode of a free-created project, then back SHALL leave the editor to Home (subject to the unsaved-changes prompt).
- **IF** the screen is currently in view mode, then back SHALL leave the editor to Home (subject to the unsaved-changes prompt).

#### Scenario: Edit mode with source image → back to view
- **WHEN** editing a project created from an image (not free creation) and the user taps back or presses system back
- **THEN** the editor switches to view mode without navigating to Home

#### Scenario: View mode → leave to Home
- **WHEN** in view mode and the user taps back or presses system back
- **THEN** the system attempts to leave to Home, applying the unsaved-changes prompt if needed

### Requirement: Unsaved-changes prompt before leaving to Home

If leaving to Home would discard unsaved changes, the system SHALL prompt the user to save first. "保存" SHALL save the `.pindou` document to the user's Documents directory (same location as the export/save action) and then leave to Home. "不保存" SHALL leave to Home and discard the unsaved changes.

Unsaved changes SHALL be considered present when:
- the project was newly created (free creation),
- a generation produced a new grid,
- the user has edited the grid,
- the reference image was loaded or cleared after the last save, OR
- any state that produces a mutated project that has not yet been persisted via `.pindou` save.

#### Scenario: Leave with unsaved edits prompts to save
- **WHEN** in view mode with unsaved edits and the user taps back
- **THEN** a dialog offers "保存" and "不保存"; "保存" writes the `.pindou` file and navigates to Home; "不保存" navigates to Home discarding edits

#### Scenario: Leave with no unsaved changes does not prompt
- **WHEN** the project has no unsaved changes and the user leaves to Home
- **THEN** the editor navigates directly to Home with no save prompt
