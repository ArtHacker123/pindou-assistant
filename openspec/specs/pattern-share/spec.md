# pattern-share Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## Requirements
### Requirement: Export and share Form 1 as PNG
The system SHALL export Form 1 as a high-resolution PNG file to device storage and SHALL allow sharing that PNG through the system share sheet via a content URI (FileProvider).

#### Scenario: Export PNG
- **WHEN** the user confirms PNG export
- **THEN** the rendered Form 1 image is written to app-accessible storage and the user is notified of the file location

#### Scenario: Share PNG
- **WHEN** the user shares the exported PNG
- **THEN** the system share sheet opens with the image attached via a FileProvider content URI

### Requirement: Export and share .pindou project file
The system SHALL serialize the current project (version, meta, bead palette source, paletteUsed, grid of colorCodes) to a `.pindou` JSON file and SHALL allow sharing that file via the system share sheet.

#### Scenario: Export .pindou
- **WHEN** the user exports the project file
- **THEN** a valid `.pindou` JSON file containing the full grid and palette data is written

#### Scenario: Share .pindou
- **WHEN** the user shares the `.pindou` file
- **THEN** the share sheet opens with the file attached via a FileProvider content URI using a custom MIME type (e.g., `application/x-pindou`)

#### Scenario: Self-contained payload
- **WHEN** the `.pindou` file is inspected after export
- **THEN** it contains the grid, `paletteUsed`, `beadPaletteSource`, and meta (grid size, palette name, colorCountTarget, image type, version) without requiring the original image

### Requirement: Open .pindou via VIEW intent for cross-device rendering
The system SHALL register a VIEW intent filter for `.pindou` files (custom MIME) so that opening a `.pindou` file on another device launches the app, parses the file, and renders Form 2 (interactive view with highlight and coordinates) directly from the parsed data.

#### Scenario: Open from file manager
- **WHEN** a user opens a `.pindou` file with the app selected
- **THEN** the app parses the JSON, reconstructs grid + palette, and displays Form 2

#### Scenario: Open via shared link/file from another device
- **WHEN** a `.pindou` file received on another device is opened with this app
- **THEN** the same interactive Form 2 (highlight same color, show coordinates) is available without the original image

#### Scenario: Corrupt or unsupported file
- **WHEN** the opened `.pindou` file is malformed or has an unsupported version
- **THEN** the app shows a clear error message and does not crash

