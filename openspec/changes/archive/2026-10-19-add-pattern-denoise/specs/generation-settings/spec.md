## ADDED Requirements

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

## MODIFIED Requirements

### Requirement: Collapsed summary shows the current advanced values

While the 高级 section is collapsed, its header SHALL show a summary of the current advanced values — the selected color-selection strategy, the subject-cutout state, and the pattern-denoise state — so the settings are visible without expanding.

#### Scenario: Summary reflects the settings
- **WHEN** the 高级 section is collapsed
- **THEN** the header shows the current color strategy name, the cutout state, and the denoise state

#### Scenario: Summary updates on change
- **WHEN** the user changes the color strategy, toggles the cutout, or changes the denoise setting and collapses the section
- **THEN** the header summary reflects the new values
