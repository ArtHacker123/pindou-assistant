## Why

Generating a new pattern from a freshly imported gallery/camera image is asynchronous, and the editor kept rendering whatever project was already in memory (from a previously opened or generated pattern) until the new one was ready. The user therefore saw the previous pattern under the busy spinner when they expected a blank generating state.

## What Changes

- Starting a generation from a newly imported image clears the in-memory project first, so the editor shows a generating (busy) state instead of the stale pattern.
- Regenerating the same image (for example after changing a parameter) still shows the current pattern under the busy indicator.

## Capabilities

### New Requirements
- `image-input`: a new import starts from a clean slate — the previously opened/generated pattern is not shown while the new one is being produced.

## Impact

- **App**: `state/DocumentStore.kt` (`clearProject`), `PindouController.kt` (clear before a fresh source generation).
- No core, dependency, or manifest change.
