# image-input Specification

## ADDED Requirements

### Requirement: A new import starts from a clean slate

When the user generates a pattern from a freshly imported gallery or camera image, the system SHALL NOT display the previously opened or generated pattern while the new pattern is being produced. It SHALL clear the prior in-memory project when generation starts, so the editor shows a generating (busy) state until the new pattern is ready. Regenerating the same image (for example after changing a generation parameter) SHALL keep the current pattern visible under the busy indicator.

#### Scenario: No stale pattern while generating from a new image
- **WHEN** the user opens a saved pattern and then imports a new image and starts generation
- **THEN** the editor does not show the previously opened pattern; it shows the generating state until the new pattern appears

#### Scenario: Regenerating the same image keeps the pattern visible
- **WHEN** the user changes a generation parameter to regenerate the current image
- **THEN** the current pattern stays visible while regeneration runs
