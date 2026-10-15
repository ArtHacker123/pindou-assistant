# privacy Specification

## Purpose
TBD - created by archiving change add-about-privacy-page. Update Purpose after archive.
## Requirements
### Requirement: About & privacy page
The app SHALL provide an about & privacy page titled 「关于与隐私」 that shows the app name 「拼豆助手」 and, when the package version can be read, the version string.

#### Scenario: Page shows the app identity
- **WHEN** the about & privacy page is shown
- **THEN** it shows the title 关于与隐私 and the app name 拼豆助手

#### Scenario: Version shown when available
- **WHEN** the package version string can be read
- **THEN** the page shows it; and it omits the version row when it cannot be read, without failing to render

### Requirement: Privacy note on the about page
The about & privacy page SHALL include a privacy note stating that: images are processed on the device to generate the pattern; the app receives only the single picture the user selects with the system photo picker or captures with the system camera, and does not read the wider gallery; and any network access is used only on demand for optional features, never in the background.

#### Scenario: On-device processing is stated
- **WHEN** the about & privacy page is shown
- **THEN** the note states that images are processed on the device to generate the pattern

#### Scenario: Only the user-selected picture is used
- **WHEN** the about & privacy page is shown
- **THEN** the note states that the app only receives the picture the user picks or shoots, not the whole gallery

#### Scenario: Network use is on demand
- **WHEN** the about & privacy page is shown
- **THEN** the note states that network access happens only on demand for optional features, not in the background

