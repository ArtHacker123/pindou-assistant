## ADDED Requirements

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

While the 高级 section is collapsed, its header SHALL show a summary of the current advanced values — the selected color-selection strategy and the subject-cutout state — so the settings are visible without expanding.

#### Scenario: Summary reflects the settings
- **WHEN** the 高级 section is collapsed
- **THEN** the header shows the current color strategy name and the cutout state

#### Scenario: Summary updates on change
- **WHEN** the user changes the color strategy or toggles the cutout and collapses the section
- **THEN** the header summary reflects the new values
