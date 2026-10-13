# navigation Specification

## MODIFIED Requirements

### Requirement: System back gesture mirrors the screen's back control

The system back button and back gesture SHALL perform the same navigation as the current screen's on-screen back control instead of exiting the app. On the history screen (我的图纸) it SHALL return to the home screen; on the export screen (导出图纸) it SHALL return to the editor; on the crop screen (裁剪与缩放) it SHALL cancel the crop and return to the previous screen. On the editor, the back control SHALL leave edit mode when editing a non-free-creation pattern (returning to the pattern view 图纸) and SHALL return home for a free-creation pattern or when leaving the pattern view itself; whenever the pattern has unsaved changes it SHALL ask whether to save or discard before leaving. The home screen SHALL continue to exit the app on back.

#### Scenario: Back on the history screen returns home
- **WHEN** the user performs the back gesture on 我的图纸
- **THEN** the app navigates to the home screen instead of closing

#### Scenario: Back on the export screen returns to the editor
- **WHEN** the user performs the back gesture on 导出图纸
- **THEN** the app navigates back to the editor instead of closing

#### Scenario: Back on the crop screen cancels the crop
- **WHEN** the user performs the back gesture on 裁剪与缩放
- **THEN** the app cancels the crop and returns to the previous screen instead of closing

#### Scenario: Editor back asks before leaving unsaved changes
- **WHEN** the user performs the back gesture (or taps the back control) on the editor with unsaved changes
- **THEN** the editor asks whether to save before leaving, instead of leaving silently

#### Scenario: Editor back destination
- **WHEN** the user leaves edit mode of a non-free-creation pattern via back
- **THEN** the pattern view (图纸) is shown; for a free-creation pattern, or from the pattern view itself, back returns to the home screen

#### Scenario: Home back exits the app
- **WHEN** the user performs the back gesture on the home screen
- **THEN** the app exits as before
