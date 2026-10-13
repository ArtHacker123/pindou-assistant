# pattern-share Specification

## ADDED Requirements

### Requirement: Warn before overwriting a different file

When saving under a name that already exists as a file this project does not own, the system SHALL ask the user to confirm overwriting before writing, for both the `.pindou` document in `Documents/PindouAssistant` and the PNG in `Pictures/PindouAssistant`. Re-saving a pattern under the name it was saved or opened with SHALL NOT prompt.

#### Scenario: New pattern name collides with an existing .pindou
- **WHEN** the user saves a pattern that has no name yet under a name that already exists as a different `.pindou` in Documents
- **THEN** a confirmation dialog offers to overwrite and the file is written only after the user confirms

#### Scenario: New pattern name collides with an existing PNG
- **WHEN** the user saves a PNG under a name that already exists as a different image in the gallery
- **THEN** a confirmation dialog offers to overwrite and the image is written only after the user confirms

#### Scenario: Cancelling the overwrite keeps the original
- **WHEN** the overwrite confirmation is dismissed
- **THEN** nothing is written and the existing file is left untouched

#### Scenario: Re-saving the same pattern does not warn
- **WHEN** the user re-saves the `.pindou` or PNG of a pattern under the same name it was saved or opened with
- **THEN** no overwrite dialog is shown

## MODIFIED Requirements

### Requirement: Export and share .pindou project file

The system SHALL serialize the current project (version, meta, bead palette source, paletteUsed, grid of colorCodes, and the grid reference image) to a `.pindou` JSON file, SHALL save it to a user-accessible Documents directory, and SHALL allow sharing that file via the system share sheet. On Android 10+ (API 29+) saving SHALL use `MediaStore.Files` under `Documents/PindouAssistant` (i.e. `/sdcard/Documents/`) with no storage permission; on older versions (API 24–28) it SHALL write to the public Documents directory and require `WRITE_EXTERNAL_STORAGE`. The reference image SHALL always be embedded when present, with no opt-out, so shared files stay comparable on other devices.

Saving SHALL be available with one tap from the editor: a `保存` control in edit mode SHALL save directly, and leaving edit mode via `完成` or entering the export screen via `导出` SHALL save the pattern first. Editor-initiated saves SHALL always embed the reference image. When the pattern does not yet have a name, the app SHALL prompt for one before writing, pre-filled with a suggested name (a template name when it came from a template, otherwise a size-based default such as `拼豆 15×15`) with the text selected so the user can accept it or type their own; once the pattern has a name, saving, completing, exporting, or sharing SHALL reuse it without prompting again. The chosen name SHALL be stored in `ProjectMeta.name`, SHALL be embedded in the file, and SHALL be sanitized and used as the `.pindou` file name. Re-saving a pattern SHALL write to the same file name (derived from the pattern name) and overwrite the existing file rather than inserting a duplicate.

Saving a pattern whose grid contains no beads SHALL ask for confirmation before writing, so an accidentally empty pattern is not persisted silently. When nothing has changed since the last save or open, pressing `保存`, `完成`, or `导出` SHALL NOT prompt for a name, SHALL NOT write the file again, and SHALL NOT show the saved-notification dialog; the flow SHALL proceed directly to its follow-up action (staying in the editor, leaving edit mode, or opening the export screen).

#### Scenario: Save .pindou to Documents
- **WHEN** the user saves the pattern
- **THEN** the project is written under the public Documents directory (`Documents/PindouAssistant`) where file managers can find it, and the user is notified of the location

#### Scenario: Prompt for a name before saving
- **WHEN** the user saves a pattern that has unsaved changes and no name yet
- **THEN** a dialog asks for the pattern name with a suggestion pre-filled and selected, and the save proceeds with the accepted or edited name

#### Scenario: No repeat prompt once named
- **WHEN** the user saves, completes, exports, or shares a pattern that already has a name
- **THEN** no name dialog is shown and the existing name is reused

#### Scenario: Name is the file name
- **WHEN** the user confirms the name `爱心`
- **THEN** the `.pindou` file is written as `爱心.pindou` and the name is embedded in the project meta

#### Scenario: One-tap save from the editor
- **WHEN** the user taps `保存` in edit mode
- **THEN** the `.pindou` is written to Documents with the reference image embedded, and the editor stays open

#### Scenario: Save on leaving edit mode
- **WHEN** the user taps `完成` to leave edit mode, or taps `导出` to open the export screen
- **THEN** the `.pindou` is written to Documents first

#### Scenario: Repeated saves overwrite the same file
- **WHEN** the user saves the same pattern more than once with the same name
- **THEN** each save overwrites the previously written `.pindou` file and no duplicate files accumulate

#### Scenario: Empty pattern requires confirmation
- **WHEN** the user taps `保存`, `完成`, or `导出` for a pattern whose grid has no beads and there are unsaved changes
- **THEN** a confirmation asks whether the empty file is really intended and the save/export only proceeds on confirmation

#### Scenario: No re-save when nothing changed
- **WHEN** the user has already saved (or opened) the pattern, makes no further edits, and taps `保存`, `完成`, or `导出` again
- **THEN** no name prompt is shown, no file is written, and the flow proceeds directly to its follow-up action

#### Scenario: Share .pindou
- **WHEN** the user taps 分享 on the export screen
- **THEN** the share sheet opens with the `.pindou` file (named after the pattern; a name prompt is shown first only when the pattern has no name yet) attached via a FileProvider content URI, using a generic binary MIME type (`application/octet-stream`) that falls back to `*/*` when no app can receive binary files; the chooser SHALL be launched with `FLAG_ACTIVITY_NEW_TASK` so it can be started from the application context

#### Scenario: Self-contained payload
- **WHEN** the `.pindou` file is inspected after export
- **THEN** it contains the grid, `paletteUsed`, `beadPaletteSource`, and meta (grid size, palette name, colorCountTarget, image type, version) without requiring the original image

#### Scenario: Reference image always embedded
- **WHEN** the user saves or shares a `.pindou`
- **THEN** the file contains the grid-resolution reference image, and reopening that file on another device restores the reference for comparison without the original source image

#### Scenario: Older file without a reference image still opens
- **WHEN** a `.pindou` file that predates this field (no reference image) is opened
- **THEN** the app parses it successfully with no reference available and renders the pattern as before, without error

### Requirement: Export Form 1 as PNG to gallery

The system SHALL save the exported Form 1 PNG to the system gallery (visible in the Photos app), named after the pattern (the name prompted at export time, sanitized). Saving a PNG whose name matches an existing image SHALL overwrite that image rather than create a duplicate. The export screen SHALL NOT expose a direct PNG share action; the user shares the saved image from the system gallery. On Android 10+ (API 29+) saving SHALL use `MediaStore.Images` under `Pictures/PindouAssistant` with no storage permission; on older versions (API 24–28) it SHALL write to the public Pictures directory and request `WRITE_EXTERNAL_STORAGE`.

#### Scenario: Save PNG to gallery (API 29+)
- **WHEN** the user taps "保存 PNG" (a name prompt is shown only when the pattern has no name yet)
- **THEN** the rendered Form 1 image is inserted into `MediaStore.Images` (`Pictures/PindouAssistant`) under the pattern name, appears in the system gallery, and the user is notified it was saved, with no runtime permission prompt

#### Scenario: Save PNG on legacy Android (API < 29)
- **WHEN** the user taps "保存 PNG" without storage permission on an older device
- **THEN** the app requests `WRITE_EXTERNAL_STORAGE` and, once granted, writes the PNG to the public Pictures directory under the pattern name
