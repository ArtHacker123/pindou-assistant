# pattern-share Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## Requirements
### Requirement: Export and share Form 1 as PNG
The system SHALL save the exported Form 1 PNG to the system gallery (visible in the Photos app) and SHALL allow sharing that PNG through the system share sheet via a content URI (FileProvider). On Android 10+ (API 29+) saving SHALL use `MediaStore.Images` under `Pictures/PindouAssistant` with no storage permission; on older versions (API 24–28) it SHALL write to the public Pictures directory and request `WRITE_EXTERNAL_STORAGE`.

#### Scenario: Save PNG to gallery (API 29+)
- **WHEN** the user taps "保存 PNG"
- **THEN** the rendered Form 1 image is inserted into `MediaStore.Images` (`Pictures/PindouAssistant`), appears in the system gallery, and the user is notified it was saved, with no runtime permission prompt

#### Scenario: Save PNG on legacy Android (API < 29)
- **WHEN** the user taps "保存 PNG" without storage permission on an older device
- **THEN** the app requests `WRITE_EXTERNAL_STORAGE` and, once granted, writes the PNG to the public Pictures directory

#### Scenario: Share PNG
- **WHEN** the user shares the exported PNG
- **THEN** the system share sheet opens with the image attached via a FileProvider content URI

### Requirement: Export and share .pindou project file
The system SHALL serialize the current project (version, meta, bead palette source, paletteUsed, grid of colorCodes, and the grid reference image) to a `.pindou` JSON file, SHALL save it to a user-accessible Documents directory, and SHALL allow sharing that file via the system share sheet. On Android 10+ (API 29+) saving SHALL use `MediaStore.Files` under `Documents/PindouAssistant` (i.e. `/sdcard/Documents/`) with no storage permission; on older versions (API 24–28) it SHALL write to the public Documents directory and require `WRITE_EXTERNAL_STORAGE`. The reference image SHALL be embedded by default and SHALL be omittable via an explicit opt-out.

#### Scenario: Save .pindou to Documents
- **WHEN** the user taps "保存 .pindou"
- **THEN** the project is written under the public Documents directory (`Documents/PindouAssistant`) where file managers can find it, and the user is notified of the location

#### Scenario: Share .pindou
- **WHEN** the user shares the `.pindou` file
- **THEN** the share sheet opens with the file attached via a FileProvider content URI using a custom MIME type (e.g., `application/x-pindou`)

#### Scenario: Self-contained payload
- **WHEN** the `.pindou` file is inspected after export
- **THEN** it contains the grid, `paletteUsed`, `beadPaletteSource`, and meta (grid size, palette name, colorCountTarget, image type, version) without requiring the original image

#### Scenario: Reference image embedded by default
- **WHEN** the user saves or shares a `.pindou` without opting out
- **THEN** the file contains the grid-resolution reference image, and reopening that file on another device restores the reference for comparison without the original source image

#### Scenario: Reference image omitted on opt-out
- **WHEN** the user opts out of embedding the reference image and saves or shares the `.pindou`
- **THEN** the file omits the reference image field (or writes it as null) and remains fully valid

#### Scenario: Older file without a reference image still opens
- **WHEN** a `.pindou` file that predates this field (no reference image) is opened
- **THEN** the app parses it successfully with no reference available and renders the pattern as before, without error

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

