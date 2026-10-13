## Why

Opening a saved blank pattern (a free-creation or template project) imports its stored `colorCountTarget = 0` into the shared generation session. The "用色数量" parameter then shows `0`, and because gallery/camera generation reuses those shared parameters, the next generation ran with a zero target (coerced to a single color) and wrote `0` back into the new file, so the problem repeated across entry points.

## What Changes

- A project with a non-positive color-count target (blank/template projects skip quantization) is treated as "automatic" when loaded into the session, so the shared parameters are never poisoned by a `0`.
- Generation always uses at least one color, so a legitimately solid-color result remains possible.

## Capabilities

### New Requirements
- `color-quantization`: an unset (zero/non-positive) color-count target falls back to automatic, and generation never runs with zero colors.

## Impact

- **App**: `state/GenerationSession.kt` only (`applyFromProject` fallback + `runGenerate` floor).
- No core, dependency, or manifest change.
