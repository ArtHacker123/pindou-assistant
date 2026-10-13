# pattern-share Specification

## Purpose

Define how generated patterns are exported and shared: PNG export to the system gallery, `.pindou` project file sharing/saving, opening `.pindou` files received from other devices (including through messengers), and the export screen layout.

## MODIFIED Requirements

### Requirement: Open .pindou via VIEW intent for cross-device rendering
The system SHALL register VIEW intent filters for `.pindou` files covering the custom MIME types, `content`/`file` URIs whose path ends in `.pindou`, and the generic binary MIME `application/octet-stream`, so that files received through messengers (which expose unknown types as `application/octet-stream` content URIs) list 拼豆助手 in the system "打开方式" chooser. Opening a `.pindou` SHALL parse the file and render Form 2 (interactive view with highlight and coordinates) directly from the parsed data. An opened file SHALL be validated, and SHALL be marked as imported (offering to be copied into the app's Documents folder) only when it comes from an external source, not when it is opened from the app's own history or recent list.

#### Scenario: Open from file manager
- **WHEN** a user opens a `.pindou` file with the app selected
- **THEN** the app parses the JSON, reconstructs grid + palette, and displays Form 2

#### Scenario: Open via shared link/file from another device
- **WHEN** a `.pindou` file received on another device is opened with this app
- **THEN** the same interactive Form 2 (highlight same color, show coordinates) is available without the original image

#### Scenario: Listed for messenger share targets
- **WHEN** a `.pindou` received in a messenger is opened via the system "打开方式" chooser (exposed as an `application/octet-stream` content URI)
- **THEN** 拼豆助手 appears in the chooser and, when picked, parses the file and shows Form 2

#### Scenario: Imported file offers to save into the app
- **WHEN** a `.pindou` is opened from an external source
- **THEN** the editor shows a card ("来自外部的图纸文件" with a 保存 action) and tapping 保存 copies the project into `Documents/PindouAssistant` and refreshes the history, after which the card is hidden

#### Scenario: History open does not prompt
- **WHEN** the user opens a `.pindou` from the app's own history or recent list
- **THEN** no import card is shown

#### Scenario: Corrupt or unsupported file
- **WHEN** the opened file is malformed, has an unsupported version, or is not a `.pindou`
- **THEN** the app shows a clear, friendly error message and does not crash
