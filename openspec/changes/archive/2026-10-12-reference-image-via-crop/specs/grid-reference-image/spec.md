# reference-image-via-crop Specification

## Purpose

Let the user attach a reference image to a source-less project through the same crop/scale step used for pattern generation, from an entry that is hidden when a reference already exists implicitly.

## ADDED Requirements

### Requirement: Load a reference image through the shared crop screen

The editor SHALL let the user load a reference image. After the user selects an image, the system SHALL proceed through the same crop/scale screen used for pattern generation. On confirmation, the system SHALL derive the grid-resolution reference from the cropped region and attach it to the current project without regenerating the pattern, then return to the editor. Cancelling the crop SHALL return to the editor without changing the reference.

#### Scenario: Selecting a reference image opens the crop screen
- **WHEN** the user chooses to load a reference image and picks an image
- **THEN** the crop/scale screen opens with that image, exactly as for pattern generation

#### Scenario: Confirming the crop attaches the reference
- **WHEN** the user confirms the crop while loading a reference
- **THEN** the grid-resolution reference derived from the cropped region is attached to the current project and the editor is shown, with the pattern left unchanged

#### Scenario: Cancelling the crop leaves the reference unchanged
- **WHEN** the user backs out of the crop screen while loading a reference
- **THEN** the editor is shown and the current reference is not modified

### Requirement: Reference-load entry availability

The reference-load entry SHALL live in the editor's edit action bar, to the left of the brush-color control. It SHALL be shown only when the current project has no source image (free creation or an opened project), and SHALL be hidden for a project generated from an image, whose reference is derived from the source automatically.

#### Scenario: Free creation shows the entry
- **WHEN** the editor is in edit mode for a project with no source image
- **THEN** the reference-load entry is visible in the edit action bar to the left of the brush-color control

#### Scenario: Generated project hides the entry
- **WHEN** the editor is in edit mode for a project generated from a source image
- **THEN** the reference-load entry is not shown
