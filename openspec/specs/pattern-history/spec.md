# pattern-history Specification

## Purpose
TBD - created by archiving change add-pattern-history-and-save. Update Purpose after archive.
## Requirements
### Requirement: Home surfaces recent saved patterns

The home screen SHALL show the most recent saved patterns (up to a small limit) as a scrollable grid of thumbnail cards, ordered newest first, together with an entry to open the full history page. It SHALL show a loading state while the list is being read and, when no patterns have been saved yet, a bead-style illustration with a hint. The list SHALL be refreshed every time the home screen becomes the current screen, when the app resumes while the home screen is showing, and immediately after a pattern is saved.

#### Scenario: Recent patterns shown
- **WHEN** the user opens the app with one or more saved `.pindou` patterns
- **THEN** the home screen shows a grid of the most recent patterns and a "查看全部" entry

#### Scenario: Empty history hint
- **WHEN** no patterns have been saved yet
- **THEN** the home screen shows a bead-style illustration and a hint that start creating

#### Scenario: Open a recent pattern
- **WHEN** the user taps a recent thumbnail
- **THEN** that `.pindou` is opened in the editor

#### Scenario: Refresh after returning from the editor
- **WHEN** the user saves a pattern in the editor and then returns to the home screen
- **THEN** the recent list is refreshed and includes that pattern

#### Scenario: Refresh on resume
- **WHEN** the app is brought back to the foreground while the home screen is showing
- **THEN** the recent list is refreshed

### Requirement: History page lists saved patterns

The system SHALL provide a dedicated history page reachable from the home screen. It SHALL read saved `.pindou` files from the public `Documents/PindouAssistant` folder (MediaStore on Android 10+, directory listing below) and present them as a scrollable grid of cards. Each thumbnail SHALL show the pattern's grid size and bead palette name (for files that decode) overlaid at its bottom-left, and the area below the thumbnail SHALL show the save date together with share and delete actions. Tapping the thumbnail SHALL open that pattern.

#### Scenario: Browse the history grid
- **WHEN** the user opens the history page with saved patterns
- **THEN** a grid of thumbnail cards is shown, newest first, and tapping a thumbnail opens that pattern

#### Scenario: Thumbnail overlay shows size and palette name
- **WHEN** a saved `.pindou` can be decoded
- **THEN** its thumbnail shows the grid size and bead palette name overlaid at the bottom-left

#### Scenario: Date and actions below the thumbnail
- **WHEN** a history card is shown
- **THEN** the save date, a share action, and a delete action are shown below the thumbnail

#### Scenario: History survives without the source image
- **WHEN** a saved `.pindou` no longer has its original source image available
- **THEN** the thumbnail is still rendered from the grid stored inside the file

#### Scenario: Empty history page
- **WHEN** the user opens the history page with no saved patterns
- **THEN** the page shows a hint instead of an empty grid

### Requirement: Delete a saved pattern

The history page SHALL let the user delete a saved pattern. Deleting SHALL require an explicit confirmation and SHALL remove the `.pindou` file from the Documents folder; the list SHALL refresh afterwards.

#### Scenario: Delete with confirmation
- **WHEN** the user chooses to delete a pattern and confirms
- **THEN** the `.pindou` file is removed and the pattern no longer appears in the history

#### Scenario: Cancel delete
- **WHEN** the user chooses to delete a pattern but cancels the confirmation
- **THEN** the file is left untouched and the pattern remains in the history

### Requirement: Share a saved pattern from history

Each card on the history page SHALL provide a share action that shares that pattern's `.pindou` file through the system share sheet, without requiring the pattern to be opened first.

#### Scenario: Share a saved pattern
- **WHEN** the user taps the share action on a history card
- **THEN** the system share sheet opens with that pattern's `.pindou` file attached

