## MODIFIED Requirements

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