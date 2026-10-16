## ADDED Requirements

### Requirement: Touching the pattern canvas collapses the open settings panel

While the generation parameters panel is open, any gesture that lands on the pattern drawing area SHALL automatically collapse the panel. Interaction with a floating overlay (the draggable tool dock or the reference peek button) SHALL NOT be treated as a canvas gesture. Collapsing the panel SHALL NOT reset the expansion state of the 高级 section.

#### Scenario: View-mode tap collapses the panel
- **WHEN** the settings panel is open and the user taps a cell in the pattern canvas
- **THEN** the settings panel collapses

#### Scenario: Editing gesture collapses the panel
- **WHEN** the settings panel is open and the user draws or erases on the pattern canvas
- **THEN** the settings panel collapses

#### Scenario: Pan or zoom collapses the panel
- **WHEN** the settings panel is open and the user pans or zooms the pattern canvas
- **THEN** the settings panel collapses

#### Scenario: Overlay interaction does not collapse the panel
- **WHEN** the settings panel is open and the user drags the floating tool dock or the reference peek button
- **THEN** the settings panel stays open

#### Scenario: Advanced section state is preserved
- **WHEN** the settings panel is collapsed by a canvas gesture while the 高级 section was expanded
- **THEN** reopening the panel shows the 高级 section still expanded

### Requirement: The settings panel floats over the canvas

The generation parameters panel SHALL be presented as an overlay on top of the pattern canvas rather than as an inline block that shares the canvas column. Opening, using, or collapsing the panel SHALL NOT change the size or position of the pattern canvas. Touches that land on the panel SHALL NOT reach the pattern grid.

#### Scenario: Opening the panel does not resize the canvas
- **WHEN** the settings panel is opened over a generated pattern
- **THEN** the pattern canvas keeps the same size and position as before it was opened

#### Scenario: Collapsing the panel does not resize the canvas
- **WHEN** the settings panel is collapsed
- **THEN** the pattern canvas keeps the same size and position as while the panel was open

#### Scenario: Touches on the panel never edit the grid
- **WHEN** the settings panel is open and the user taps or drags on the panel itself
- **THEN** no cell on the pattern grid underneath is edited or selected
