## Why

When a `.pindou` file is received through a messenger (e.g. WeChat/QQ), the receiver's "打开方式" list did not include 拼豆助手: the app only declared VIEW intent filters for the custom MIME and for `.pindou` URIs, but messengers expose the received file as a `content://` URI with the unknown-type MIME `application/octet-stream`, which matched neither. There was also no way to keep a received file inside the app.

## What Changes

- Register an additional VIEW intent filter matching `application/octet-stream` (with `content`/`file` schemes) so messengers' share targets list 拼豆助手.
- Validate an opened file and show a clear, friendly error (not a crash) when it is not a `.pindou` or is corrupt.
- When a `.pindou` is opened from an external source, mark it as imported and show a card in the editor ("来自外部的图纸文件" / 保存) that copies it into `Documents/PindouAssistant` on tap; opening from the app's own history/recent does not show the card.

## Capabilities

### Modified Capabilities
- `pattern-share`: opening a shared `.pindou` now also matches the generic binary MIME, validates the file with friendly errors, and offers a "保存到我的图纸" card to import it into the app's Documents folder.

## Impact

- **App**: `AndroidManifest.xml` (new VIEW intent filter), `state/DocumentStore.kt` (`imported` flag + `displayName`), `PindouController.kt` (`openPindou(external)` + validation + `importToDocuments`), `ui/PindouApp.kt` (external flag), `ui/editor/EditorScreen.kt` (import card).
- No core, dependency, or permission change.
