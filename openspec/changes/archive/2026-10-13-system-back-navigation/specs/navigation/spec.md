# navigation Specification

## ADDED Requirements

### Requirement: System back gesture mirrors the screen's back control

The system back button and back gesture SHALL perform the same navigation as the current screen's on-screen back control instead of exiting the app. On the history screen (我的图纸) it SHALL return to the home screen; on the export screen (导出图纸) it SHALL return to the editor; on the crop screen (裁剪与缩放) it SHALL cancel the crop and return to the previous screen. The editor's existing back behavior SHALL be preserved (leave edit mode when editing a non-free-creation pattern, otherwise return home), and the home screen SHALL continue to exit the app on back.

#### Scenario: Back on the history screen returns home
- **WHEN** the user performs the back gesture on 我的图纸
- **THEN** the app navigates to the home screen instead of closing

#### Scenario: Back on the export screen returns to the editor
- **WHEN** the user performs the back gesture on 导出图纸
- **THEN** the app navigates back to the editor instead of closing

#### Scenario: Back on the crop screen cancels the crop
- **WHEN** the user performs the back gesture on 裁剪与缩放
- **THEN** the app cancels the crop and returns to the previous screen instead of closing

#### Scenario: Editor and home back behavior unchanged
- **WHEN** the user performs the back gesture on the editor or the home screen
- **THEN** the editor follows its existing rules (leave edit mode, then home) and the home screen exits the app as before
