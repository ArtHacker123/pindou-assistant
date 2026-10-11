## MODIFIED Requirements

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
