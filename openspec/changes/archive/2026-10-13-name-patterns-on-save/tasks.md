## Name patterns on save

- [x] 1.1 Add a file-name sanitizer (`ExportManager.safeFileName`).
- [x] 1.2 Use the chosen name for the `.pindou` and PNG file names (`ExportService`).
- [x] 1.3 Store the name on the project (`DocumentStore.updateName`).
- [x] 1.4 Add `suggestName` and name-aware save/export on the controller; seed template names.
- [x] 1.5 Add a reusable `NameDialog` (suggestion pre-filled and selected, editable).
- [x] 1.6 Prompt for a name before saving/completing/exporting (editor) and before PNG save / `.pindou` share (export screen), only when the pattern has no name yet.
- [x] 1.7 Show the pattern name as the history card title.
- [x] 1.8 Skip the prompt once the pattern already has a name (reuse it silently); keep the template name as the first-time suggestion.
- [x] 1.9 Warn before overwriting a different existing `.pindou`/PNG (only when the name is not the current pattern's own file); overwrite same-name files instead of duplicating.

## Verification

- [x] 2.1 `:core:test :app:assembleDebug` passes; app installs.
- [ ] 2.2 Saving shows the name prompt (first time only); the saved `.pindou`/PNG use the name; 我的图纸 shows it.
- [ ] 2.3 Saving a new pattern under an existing different name shows the overwrite warning for both `.pindou` and PNG.
