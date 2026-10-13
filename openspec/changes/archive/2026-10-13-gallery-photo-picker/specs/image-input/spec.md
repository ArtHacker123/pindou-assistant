# image-input Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## MODIFIED Requirements

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