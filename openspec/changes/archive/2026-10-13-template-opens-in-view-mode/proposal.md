## Why

Quick-start templates were created and opened directly in edit mode, unlike saved patterns, which open in the 图纸 view. A template is a starting point to look at first; the user preferred it to open in view mode like an existing pattern. Free creation still opens in edit mode because it starts from an empty grid meant to be filled immediately.

## What Changes

- Quick-start templates open in view mode (图纸) instead of edit mode.
- Free creation keeps opening directly in edit mode.

## Capabilities

### Modified Capabilities
- `pattern-templates`: applying a template opens it in view mode.
- `pattern-editing`: only free-created projects open directly in edit mode; templates open in view mode.

## Impact

- **App**: `PindouController.kt` (`openBlankProject` takes an `editMode` flag; templates pass false, free creation passes true).
- No core, dependency, or manifest change.
