# pattern-history Specification

## Purpose
TBD - created by archiving change add-pattern-history-and-save. Update Purpose after archive.
## Requirements
### Requirement: Home surfaces recent saved patterns

The home screen SHALL show the most recent saved patterns (up to a small limit) as thumbnails, ordered newest first, together with an entry to open the full history page. It SHALL show a loading state while the list is being read and a hint when no patterns have been saved yet.

#### Scenario: Recent patterns shown
- **WHEN** the user opens the app with one or more saved `.pindou` patterns
- **THEN** the home screen shows thumbnails of the most recent patterns and a "查看全部" entry

#### Scenario: Empty history hint
- **WHEN** no patterns have been saved yet
- **THEN** the home screen shows a hint that saved patterns will appear there, and no thumbnails

#### Scenario: Open a recent pattern
- **WHEN** the user taps a recent thumbnail
- **THEN** that `.pindou` is opened in the editor

### Requirement: History page lists saved patterns

The system SHALL provide a dedicated history page reachable from the home screen. It SHALL read saved `.pindou` files from the public `Documents/PindouAssistant` folder (MediaStore on Android 10+, directory listing below) and present them as a scrollable grid of cards, each showing a thumbnail of the pattern, its name (or its grid size when unnamed) and a date. Tapping a card SHALL open that pattern.

#### Scenario: Browse the history grid
- **WHEN** the user opens the history page with saved patterns
- **THEN** a grid of thumbnail cards is shown, newest first, and tapping a card opens that pattern

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

