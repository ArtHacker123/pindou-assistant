# generation-settings Specification

## Purpose
TBD - created by archiving change collapse-advanced-settings. Update Purpose after archive.
## Requirements
### Requirement: Advanced generation options are grouped and collapsed

The generation parameters panel SHALL keep the primary options — image type, palette, pattern size, and color count — visible, and SHALL group the advanced options (the color-selection strategy 取色方式 and the subject cutout 自动抠出主体, together with its tuning preset) under a single collapsible 高级 section that is collapsed by default. The section SHALL expand or collapse when its header is tapped. The options inside SHALL keep their existing behavior and persistence.

#### Scenario: Primary options stay visible
- **WHEN** the generation parameters panel is shown
- **THEN** image type, palette, pattern size, and color count are visible, and the advanced options are not shown until the section is expanded

#### Scenario: Advanced section starts collapsed
- **WHEN** the panel is shown for a new generation
- **THEN** the 高级 section is collapsed

#### Scenario: Expanding reveals the advanced options
- **WHEN** the user taps the 高级 header
- **THEN** the color-selection strategy and the subject-cutout controls become visible

#### Scenario: Moved options keep working
- **WHEN** the user changes the color strategy or toggles the cutout inside the 高级 section
- **THEN** the change applies and persists exactly as it did when the options were shown directly

#### Scenario: Expansion state is kept within the session
- **WHEN** the user expands the 高级 section, generates, and the panel is shown again in the same session
- **THEN** the section is still expanded

### Requirement: Collapsed summary shows the current advanced values

While the 高级 section is collapsed, its header SHALL show a summary of the current advanced values — the selected color-selection strategy, the subject-cutout state, and the pattern-denoise state — so the settings are visible without expanding.

#### Scenario: Summary reflects the settings
- **WHEN** the 高级 section is collapsed
- **THEN** the header shows the current color strategy name, the cutout state, and the denoise state

#### Scenario: Summary updates on change
- **WHEN** the user changes the color strategy, toggles the cutout, or changes the denoise setting and collapses the section
- **THEN** the header summary reflects the new values

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

### Requirement: Advanced options include pattern denoise

The advanced (高级) generation options SHALL include a 去杂色 control that combines an on/off switch with a strength (弱 / 中 / 强), defaulting to on with the medium strength. Changing the control SHALL trigger regeneration, and the chosen switch and strength SHALL be remembered across sessions, like the sibling subject-cutout toggle in the same section.

#### Scenario: Denoise control is in the advanced section
- **WHEN** the user expands the 高级 section of the generation parameters panel
- **THEN** a 去杂色 switch and a strength control are shown among the advanced options

#### Scenario: Default is on and medium
- **WHEN** the parameters panel is first shown for a new generation
- **THEN** 去杂色 is on with the medium strength

#### Scenario: Changing denoise regenerates
- **WHEN** the user toggles 去杂色 or changes its strength
- **THEN** the pattern is regenerated with the new denoise setting

#### Scenario: Denoise choice is remembered
- **WHEN** the user changes the 去杂色 switch or strength and later reopens the app
- **THEN** the 去杂色 switch and strength still show the chosen values

