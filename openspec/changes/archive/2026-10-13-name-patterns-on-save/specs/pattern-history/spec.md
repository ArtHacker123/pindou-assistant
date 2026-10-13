# pattern-history Specification

## MODIFIED Requirements

### Requirement: History page lists saved patterns

The system SHALL provide a dedicated history page reachable from the home screen. It SHALL read saved `.pindou` files from the public `Documents/PindouAssistant` folder (MediaStore on Android 10+, directory listing below) and present them as a scrollable grid of cards. Each thumbnail SHALL show the pattern's grid size and bead palette name (for files that decode) overlaid at its bottom-left, and the area below the thumbnail SHALL show the pattern name as its title, followed by the save date together with share and delete actions. Tapping the thumbnail SHALL open that pattern.

#### Scenario: Browse the history grid
- **WHEN** the user opens the history page with saved patterns
- **THEN** a grid of thumbnail cards is shown, newest first, and tapping a thumbnail opens that pattern

#### Scenario: Thumbnail overlay shows size and palette name
- **WHEN** a saved `.pindou` can be decoded
- **THEN** its thumbnail shows the grid size and bead palette name overlaid at the bottom-left

#### Scenario: Card title shows the pattern name
- **WHEN** a saved `.pindou` carries a pattern name
- **THEN** its card shows that name as the title below the thumbnail

#### Scenario: Date and actions below the thumbnail
- **WHEN** a history card is shown
- **THEN** the save date, a share action, and a delete action are shown below the thumbnail

#### Scenario: History survives without the source image
- **WHEN** a saved `.pindou` no longer has its original source image available
- **THEN** the thumbnail is still rendered from the grid stored inside the file

#### Scenario: Empty history page
- **WHEN** the user opens the history page with no saved patterns
- **THEN** the page shows a hint instead of an empty grid
