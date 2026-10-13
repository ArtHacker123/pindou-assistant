# pattern-share Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## MODIFIED Requirements

### Requirement: Export and share .pindou project file

The system SHALL serialize the current project (version, meta, bead palette source, paletteUsed, grid of colorCodes, and the grid reference image) to a `.pindou` JSON file, SHALL save it to a user-accessible Documents directory, and SHALL allow sharing that file via the system share sheet. On Android 10+ (API 29+) saving SHALL use `MediaStore.Files` under `Documents/PindouAssistant` (i.e. `/sdcard/Documents/`) with no storage permission; on older versions (API 24–28) it SHALL write to the public Documents directory and require `WRITE_EXTERNAL_STORAGE`. The reference image SHALL be embedded by default and SHALL be omittable via an explicit opt-out.

Saving SHALL be available with one tap from the editor: a `保存` control in edit mode SHALL save directly, and leaving edit mode via `完成` or entering the export screen via `导出` SHALL save the pattern first. Editor-initiated saves SHALL always embed the reference image. Re-saving a pattern SHALL write to the same file name (derived from the project's creation time) and overwrite the existing file rather than inserting a duplicate.

Saving a pattern whose grid contains no beads SHALL ask for confirmation before writing, so an accidentally empty pattern is not persisted silently. When nothing has changed since the last save or open, pressing `保存`, `完成`, or `导出` SHALL NOT write the file again and SHALL NOT show the saved-notification dialog; the flow SHALL proceed directly to its follow-up action (staying in the editor, leaving edit mode, or opening the export screen).

#### Scenario: Save .pindou to Documents
- **WHEN** the user saves the pattern
- **THEN** the project is written under the public Documents directory (`Documents/PindouAssistant`) where file managers can find it, and the user is notified of the location

#### Scenario: One-tap save from the editor
- **WHEN** the user taps `保存` in edit mode
- **THEN** the `.pindou` is written to Documents with the reference image embedded, and the editor stays open

#### Scenario: Save on leaving edit mode
- **WHEN** the user taps `完成` to leave edit mode, or taps `导出` to open the export screen
- **THEN** the `.pindou` is written to Documents first

#### Scenario: Repeated saves overwrite the same file
- **WHEN** the user saves the same pattern more than once
- **THEN** each save overwrites the previously written `.pindou` file and no duplicate files accumulate

#### Scenario: Empty pattern requires confirmation
- **WHEN** the user taps `保存`, `完成`, or `导出` for a pattern whose grid has no beads and there are unsaved changes
- **THEN** a confirmation asks whether the empty file is really intended and the save/export only proceeds on confirmation

#### Scenario: No re-save when nothing changed
- **WHEN** the user has already saved (or opened) the pattern, makes no further edits, and taps `保存`, `完成`, or `导出` again
- **THEN** no file is written and no "已保存" dialog is shown, and the flow proceeds directly to its follow-up action

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
- **WHEN** the user opts out of embedding the reference image when sharing the `.pindou`
- **THEN** the shared file omits the reference image field (or writes it as null) and remains fully valid

#### Scenario: Older file without a reference image still opens
- **WHEN** a `.pindou` file that predates this field (no reference image) is opened
- **THEN** the app parses it successfully with no reference available and renders the pattern as before, without error