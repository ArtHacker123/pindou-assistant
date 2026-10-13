# color-quantization Specification

## ADDED Requirements

### Requirement: Fall back to automatic when the color count is unset

When a project carries no usable color-count target (a non-positive value, as stored by blank free-creation and template projects, which skip quantization), the system SHALL treat the color count as automatic and use the default automatic count instead of 0, so the shared generation parameters are never poisoned by such a project. Generation SHALL always run with at least one color, so a solid-color result remains possible.

#### Scenario: Opening a blank pattern keeps the parameter valid
- **WHEN** the user opens a saved blank pattern whose stored `colorCountTarget` is 0
- **THEN** the "用色数量" parameter shows "自动" and holds the default automatic count, not 0

#### Scenario: A new generation after opening a blank pattern uses a valid count
- **WHEN** the user opens a blank pattern and then generates from a gallery or camera image without changing the parameter
- **THEN** the generation uses the automatic default color count (16), not 0, and is not silently reduced to a single color

#### Scenario: Generation never uses zero colors
- **WHEN** generation runs with a non-positive color-count target for any reason
- **THEN** it uses at least one color instead of a zero-color extraction
