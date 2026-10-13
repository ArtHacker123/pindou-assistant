# image-input Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## ADDED Requirements

### Requirement: Downsample oversized input images

The system SHALL downsample a loaded image so that its longest edge does not exceed 2048 pixels, choosing the sub-sampling factor during decoding, to keep preprocessing and generation within memory limits. This SHALL apply to images imported from the gallery and images captured with the camera.

#### Scenario: Large photo is downsampled
- **WHEN** the user imports or captures an image whose longest edge exceeds 2048 pixels
- **THEN** the decoded image has a longest edge of at most 2048 pixels and generation proceeds without running out of memory

#### Scenario: Small image is untouched
- **WHEN** the user imports an image whose longest edge is 2048 pixels or less
- **THEN** the image is decoded at its original resolution
