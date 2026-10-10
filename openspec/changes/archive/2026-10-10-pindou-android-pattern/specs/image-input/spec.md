## ADDED Requirements

### Requirement: Import image from gallery or camera
The system SHALL allow the user to import an image either from the device gallery or by capturing a new photo with the camera.

#### Scenario: Import from gallery
- **WHEN** the user selects "import from gallery" and picks an image
- **THEN** the system loads the original image at full available resolution and proceeds to the crop/scale screen

#### Scenario: Import from camera
- **WHEN** the user selects "take photo" and captures an image
- **THEN** the system loads the captured image and proceeds to the crop/scale screen

#### Scenario: Import failure
- **WHEN** the selected image cannot be decoded (corrupt/unsupported format)
- **THEN** the system shows an error message and remains on the import screen without crashing

### Requirement: Provide image type presets
The system SHALL let the user choose an image-type preset that influences preprocessing. The first version SHALL include at least two presets: cartoon and photo. The preset list MUST be extensible through a processor registry.

#### Scenario: Select cartoon preset
- **WHEN** the user selects the cartoon preset for a flat-color illustration
- **THEN** the system applies cartoon-appropriate preprocessing (edge/contrast emphasis) during generation

#### Scenario: Select photo preset
- **WHEN** the user selects the photo preset for a landscape photo
- **THEN** the system applies photo-appropriate preprocessing (denoise/contrast adjustment) during generation

#### Scenario: Preset registry extensibility
- **WHEN** a new image processor is registered under a new preset id
- **THEN** the preset appears as a selectable option without modifying the generation pipeline

### Requirement: Crop and scale the imported image
The system SHALL provide a crop/scale editing screen where the user can pan the image, pinch-zoom, and drag the crop frame borders/corners to define the region used for pattern generation.

#### Scenario: Drag crop border
- **WHEN** the user drags a crop-frame edge or corner
- **THEN** the crop frame resizes accordingly, constrained within the displayed image bounds

#### Scenario: Pan and zoom image
- **WHEN** the user pans with one finger or pinch-zooms with two fingers
- **THEN** the image view transforms while the crop frame remains controllable independently

#### Scenario: Confirm crop
- **WHEN** the user confirms the crop
- **THEN** the system transforms the screen-space crop rectangle back to original-image pixel coordinates (clamped to image bounds) and produces a working bitmap cropped from the original image

#### Scenario: Cancel crop
- **WHEN** the user cancels the crop screen
- **THEN** the system returns to the import step without generating a working image

#### Scenario: Reset crop
- **WHEN** the user taps the reset action on the crop screen
- **THEN** the crop frame returns to the full image and the view resets to fit

#### Scenario: Enforce minimum crop size
- **WHEN** the user attempts to shrink the crop frame below the minimum allowed size
- **THEN** the crop frame stops at the minimum size and does not invert
