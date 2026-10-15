## MODIFIED Requirements

### Requirement: Bundle and select multiple bead palettes
The system SHALL bundle at least four bead palettes of market bead colors as assets and let the user select which palette to use. The palette system SHALL support adding additional palettes without code changes to the pipeline. The bundled palettes SHALL be:
- `mard_221.json` — displayName "MARD 标准 221 色", 221 colors, `colorCode`s letter+number (groups A–H and M, e.g. `A1`, `H7`, `M15`).
- `perler.json` — displayName "Perler 103 色", 103 colors, `colorCode`s in Perler form (e.g. `80-19001`).
- `hama_midi.json` — displayName "Hama Midi 92 色", 92 colors, `colorCode`s in Hama form (e.g. `H01`).
- `artkal_s.json` — displayName "Artkal S 199 色", 199 colors, `colorCode`s in Artkal S form (e.g. `S01`, `SE10`, `ST6`).

Each bundled palette file SHALL be self-describing with its `name`, `displayName`, `version`, and a non-empty `items` list of unique `colorCode`s.

#### Scenario: Default palette loaded
- **WHEN** the app starts and the user reaches parameter setup
- **THEN** the bundled MARD 221-color palette is loaded from assets and shown as the selected palette

#### Scenario: Switch palette
- **WHEN** the user selects a different bundled palette
- **THEN** subsequent generation maps representative colors to the newly selected palette while keeping the same representative colors

#### Scenario: Multiple palettes available
- **WHEN** multiple palette files are bundled
- **THEN** the palette picker lists each with its name and color count

#### Scenario: Brand palettes are bundled
- **WHEN** the app ships
- **THEN** the assets contain the MARD 221, Perler 103, Hama Midi 92, and Artkal S 199 palettes, each loadable by its file name

## ADDED Requirements

### Requirement: Default palette is MARD 221
The default palette used at startup and when mapping a built-in template SHALL be the MARD standard 221-color set, selected by its `name` (`mard_221`) rather than by asset ordering. Adding or removing other bundled palette assets SHALL NOT change this default.

#### Scenario: Startup default is MARD
- **WHEN** the app starts with no palette chosen
- **THEN** the selected palette is `mard_221`

#### Scenario: Template mapping uses MARD
- **WHEN** a built-in template is opened
- **THEN** its colors are mapped against the MARD 221 palette

#### Scenario: Adding a palette does not change the default
- **WHEN** a bundled palette whose file name sorts before `mard_221` is present
- **THEN** the startup/template default remains `mard_221`
