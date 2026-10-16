## ADDED Requirements

### Requirement: Configurable mask post-processing
The system SHALL reduce the soft saliency mask to a validity mask through a configurable post-process whose behavior is described by data, not hard coded. The post-process SHALL support a foreground threshold, a hard validity threshold, region connectivity, a subject-selection mode, and optional morphological opening and closing. With no configuration supplied, the post-process SHALL reproduce the existing behavior (a single largest four-connected region above a 0.5 threshold, 0.5 hard threshold, no morphology).

#### Scenario: Defaults reproduce the current behavior
- **WHEN** the post-process runs with no configuration
- **THEN** the result is a single largest four-connected region above a 0.5 threshold and a 0.5 hard validity threshold, identical to generation before this change

#### Scenario: Opening removes background residue
- **WHEN** the configuration enables morphological opening
- **THEN** small isolated foreground specks below the opening radius are removed from the mask

#### Scenario: Closing preserves thin subject parts
- **WHEN** the configuration enables morphological closing
- **THEN** small gaps and holes within the subject are filled so thin parts are not dropped

#### Scenario: Threshold shifts the subject boundary
- **WHEN** the foreground or hard threshold is changed
- **THEN** which pixels count as subject changes accordingly, without any model or pipeline change

### Requirement: Cutout tuning presets
The system SHALL offer selectable presets next to the cutout control that apply coherent post-process settings: a standard preset matching the default behavior, a preset that reduces background residue, and a preset that preserves thin subject parts. The system SHALL remember the selected preset and start in that state for subsequent generations.

#### Scenario: Preset is applied to the mask
- **WHEN** the user selects a preset and generates with the cutout enabled
- **THEN** the mask is computed with that preset's post-process settings and the preset is remembered for next time

#### Scenario: Presets target the two failure modes
- **WHEN** the user selects the reduce-background preset or the preserve-subject preset
- **THEN** the resulting mask respectively drops more background or keeps more thin subject than the standard preset does
