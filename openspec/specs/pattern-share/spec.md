# pattern-share Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## Requirements
### Requirement: Export and share .pindou project file

The system SHALL serialize the current project (version, meta, bead palette source, paletteUsed, grid of colorCodes, and the grid reference image) to a `.pindou` JSON file, SHALL save it to a user-accessible Documents directory, and SHALL allow sharing that file via the system share sheet. On Android 10+ (API 29+) saving SHALL use `MediaStore.Files` under `Documents/PindouAssistant` (i.e. `/sdcard/Documents/`) with no storage permission; on older versions (API 24–28) it SHALL write to the public Documents directory and require `WRITE_EXTERNAL_STORAGE`. The reference image SHALL always be embedded when present, with no opt-out, so shared files stay comparable on other devices.

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
- **WHEN** the user taps 分享 on the export screen
- **THEN** the share sheet opens with the `.pindou` file attached via a FileProvider content URI, using a generic binary MIME type (`application/octet-stream`) that falls back to `*/*` when no app can receive binary files; the chooser SHALL be launched with `FLAG_ACTIVITY_NEW_TASK` so it can be started from the application context

#### Scenario: Self-contained payload
- **WHEN** the `.pindou` file is inspected after export
- **THEN** it contains the grid, `paletteUsed`, `beadPaletteSource`, and meta (grid size, palette name, colorCountTarget, image type, version) without requiring the original image

#### Scenario: Reference image always embedded
- **WHEN** the user saves or shares a `.pindou`
- **THEN** the file contains the grid-resolution reference image, and reopening that file on another device restores the reference for comparison without the original source image

#### Scenario: Older file without a reference image still opens
- **WHEN** a `.pindou` file that predates this field (no reference image) is opened
- **THEN** the app parses it successfully with no reference available and renders the pattern as before, without error

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

### Requirement: Export Form 1 as PNG to gallery
The system SHALL save the exported Form 1 PNG to the system gallery (visible in the Photos app). The export screen SHALL NOT expose a direct PNG share action; the user shares the saved image from the system gallery. On Android 10+ (API 29+) saving SHALL use `MediaStore.Images` under `Pictures/PindouAssistant` with no storage permission; on older versions (API 24–28) it SHALL write to the public Pictures directory and request `WRITE_EXTERNAL_STORAGE`.

#### Scenario: Save PNG to gallery (API 29+)
- **WHEN** the user taps "保存 PNG"
- **THEN** the rendered Form 1 image is inserted into `MediaStore.Images` (`Pictures/PindouAssistant`), appears in the system gallery, and the user is notified it was saved, with no runtime permission prompt

#### Scenario: Save PNG on legacy Android (API < 29)
- **WHEN** the user taps "保存 PNG" without storage permission on an older device
- **THEN** the app requests `WRITE_EXTERNAL_STORAGE` and, once granted, writes the PNG to the public Pictures directory

### Requirement: Export screen layout
The export screen SHALL present, on a single screen that fits without scrolling: a print preview of Form 1, the output-resolution options, and exactly two primary actions — 保存 PNG (saves the PNG to the system gallery) and 分享 (shares the `.pindou` file, never the PNG). It SHALL also provide a bottom 返回首页 action using a bead-style home icon and a back control that returns to the editor.

#### Scenario: Two primary actions
- **WHEN** the export screen is shown
- **THEN** it offers exactly 保存 PNG and 分享: 保存 PNG writes the PNG to the gallery and 分享 shares the `.pindou` project file

#### Scenario: Fits one screen
- **WHEN** the export screen is shown on a phone
- **THEN** the preview, the resolution options, and both actions are visible without scrolling

#### Scenario: Return home
- **WHEN** the user taps the bead-style 返回首页 button
- **THEN** the app navigates to the home screen

