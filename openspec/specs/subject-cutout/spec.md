# subject-cutout Specification

## Purpose
TBD - created by archiving change add-subject-cutout. Update Purpose after archive.
## Requirements
### Requirement: Opt-in subject cutout before generation
The system SHALL offer an optional subject-cutout step after cropping an imported image and before generation. The step SHALL be off by default and SHALL present a control to enable or disable the cutout. The system SHALL remember the user's last choice and start the control in that state for subsequent generations. When the cutout is left disabled, generation SHALL proceed exactly as before, with no mask applied.

#### Scenario: Off by default
- **WHEN** the subject-cutout step is shown for the first time
- **THEN** the enable control is off and generation would include the whole framed region

#### Scenario: Remembers the last choice
- **WHEN** the user enables (or disables) the cutout and later generates again
- **THEN** the control starts in the last chosen state

#### Scenario: Disabled proceeds unchanged
- **WHEN** the user leaves the cutout disabled and generates
- **THEN** no mask is applied and the result matches generation without this feature

### Requirement: Class-agnostic single-subject cutout
When the cutout is enabled, the system SHALL compute a foreground mask for a single salient subject without using object categories, and SHALL treat the rest of the frame as background. Background pixels SHALL NOT contribute to the pattern: a cell whose source region is not sufficiently covered by the mask SHALL become an empty hole, exactly as transparent padding does today. When several salient regions are present, the mask SHALL keep a single subject (the largest). The step SHALL show a preview of the cut-out result before the user generates.

#### Scenario: Background is excluded
- **WHEN** the cutout is enabled and generation runs
- **THEN** background pixels do not contribute to any cell's color or validity

#### Scenario: A single subject is kept
- **WHEN** the frame contains more than one salient region
- **THEN** the mask keeps one subject (the largest salient region)

#### Scenario: Preview before generating
- **WHEN** the cutout completes in the step
- **THEN** the step shows the subject with the background removed before generation

### Requirement: Subject cutout runs entirely on-device
The cutout SHALL be computed by a model bundled with the app and run on the device. It SHALL NOT require or use network access.

#### Scenario: No network use
- **WHEN** the cutout runs
- **THEN** it completes without any network access and without any permission beyond those the app already uses

### Requirement: Per-model device capability gate
The system SHALL decide whether a cutout model is usable on the current device by timing it. On first use it SHALL warm up and time a representative inference; if that exceeds a 3-second budget, it SHALL mark the model unavailable on this device, disable the cutout option with an explanatory message, and continue to allow generation without the cutout. A running inference SHALL be bounded by the same 3-second budget, and exceeding it SHALL cancel that inference and fall back to generating without the cutout. The gate SHALL be evaluated per model.

#### Scenario: Fast device enables the option
- **WHEN** the timed inference is within the 3-second budget
- **THEN** the cutout option is available

#### Scenario: Slow device disables the option
- **WHEN** the timed inference exceeds the 3-second budget
- **THEN** the cutout option is disabled with an explanatory message and generation still works without it

#### Scenario: Runtime timeout falls back
- **WHEN** a cutout inference exceeds the 3-second budget while running
- **THEN** it is cancelled and generation proceeds without the cutout

### Requirement: The cutout mask is the single source of truth
The cutout mask SHALL be held at the working-image resolution and SHALL be the single source of truth for the subject region; cell validity, the reference image, and any later mask editing SHALL derive from it. The source produces a soft probability mask and it SHALL be reduced to a per-pixel validity mask at that working-image resolution, so a cell only partially covered by the subject reports a matching partial coverage rather than an all-or-nothing cell. The system SHALL cache the mask for the current image so that re-generating (for example after changing a generation parameter) does not re-run the model.

#### Scenario: Partial coverage is reflected
- **WHEN** a cell's source region only partially overlaps the subject
- **THEN** the cell's coverage reflects the valid fraction of that region

#### Scenario: Reference derives from the mask
- **WHEN** a subject cutout is applied
- **THEN** the reference image reflects the cutout, showing background cells as holes

#### Scenario: Cache reuse on regeneration
- **WHEN** the user re-generates with a different generation parameter and the same image
- **THEN** the cutout model is not run again

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

