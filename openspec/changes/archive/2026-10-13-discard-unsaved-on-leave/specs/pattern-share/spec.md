# pattern-share Specification

## ADDED Requirements

### Requirement: Leaving the editor with unsaved changes asks to save or discard
When the user leaves the editor via the back control or the system back gesture and the current pattern has unsaved changes, the app SHALL ask whether to save before leaving. Choosing 保存 SHALL write the `.pindou` first (prompting for a name only when the pattern has none yet) and then proceed; choosing 不保存 SHALL discard every change made since the last save, open, or generation, so the pattern content (grid, palette, reference, and name) returns to its last committed state, and then proceed. When there are no unsaved changes, leaving SHALL proceed directly without asking. The commit points that 不保存 reverts to SHALL be: opening a pattern, starting a blank (free-creation or template) pattern, completing a generation, and a successful `.pindou` save.

#### Scenario: Leaving edit mode with edits prompts
- **WHEN** the user enters edit mode from the pattern view, edits the grid, and presses the back control or the back gesture
- **THEN** a 「未保存」 dialog offers 保存 and 不保存 before leaving

#### Scenario: Declining restores the committed pattern
- **WHEN** the user chooses 不保存
- **THEN** the grid (and any palette, reference, or name changes made since the last save, open, or generation) revert to the last committed state, and the editor undoes no longer apply

#### Scenario: Saving from the leave prompt
- **WHEN** the user chooses 保存 on the leave prompt
- **THEN** the `.pindou` is written (prompting for a name first only when the pattern has none) and leaving proceeds

#### Scenario: No prompt when nothing changed
- **WHEN** the user leaves the editor with no unsaved changes
- **THEN** no dialog is shown and leaving proceeds directly
