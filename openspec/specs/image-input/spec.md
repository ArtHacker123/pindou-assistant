# image-input Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## Requirements
### Requirement: Import image from gallery or camera

The system SHALL import an image either from the device gallery or by capturing a new photo with the camera. Gallery import SHALL open the system photo picker in photo-only mode (a thumbnail grid over the user's photos) rather than a folder-based file browser, so the user picks a photo directly from the preview without navigating folders. Both paths SHALL load the image and proceed to the crop/scale screen.

#### Scenario: Import from gallery
- **WHEN** the user selects "从相册生成"
- **THEN** the system photo picker opens in photo-only mode showing a grid of photo thumbnails (not a folder/file browser), and choosing a photo loads the original image at full available resolution and proceeds to the crop/scale screen

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

### Requirement: Downsample oversized input images

The system SHALL downsample a loaded image so that its longest edge does not exceed 2048 pixels, choosing the sub-sampling factor during decoding, to keep preprocessing and generation within memory limits. This SHALL apply to images imported from the gallery and images captured with the camera.

#### Scenario: Large photo is downsampled
- **WHEN** the user imports or captures an image whose longest edge exceeds 2048 pixels
- **THEN** the decoded image has a longest edge of at most 2048 pixels and generation proceeds without running out of memory

#### Scenario: Small image is untouched
- **WHEN** the user imports an image whose longest edge is 2048 pixels or less
- **THEN** the image is decoded at its original resolution

