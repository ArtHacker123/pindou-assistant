# grid-reference-image Specification

## Purpose
TBD - created by archiving change add-grid-reference-image. Update Purpose after archive.
## MODIFIED Requirements

### Requirement: Reference-load entry availability

The editor's edit action bar SHALL provide the reference controls, positioned to the left of the brush-color control. A load entry labelled "导入参考图" SHALL be shown only when the current project has no source image (free creation or an opened project), and SHALL be hidden for a project generated from an image, whose reference is derived from the source automatically.

When the current project has a reference image, the action bar SHALL additionally show a "显示参考图" toggle that indicates its active state (with a trailing ✓ while the reference is visible) and SHALL show or hide the reference over the pattern when tapped. Importing a reference SHALL activate this toggle (the reference is shown by default), and the show/hide state SHALL be held in the navigation state so it survives the editor → crop → editor detour of the import. Generating from a source image, opening a project, and starting a free creation SHALL reset it to hidden.

#### Scenario: Free creation shows the entry
- **WHEN** the editor is in edit mode for a project with no source image
- **THEN** the "导入参考图" entry is visible in the edit action bar to the left of the brush-color control

#### Scenario: Generated project hides the entry
- **WHEN** the editor is in edit mode for a project generated from a source image
- **THEN** the "导入参考图" entry is not shown

#### Scenario: Show toggle reflects and changes visibility
- **WHEN** the project has a reference image and the user taps "显示参考图"
- **THEN** the reference is shown or hidden over the pattern accordingly, and the toggle shows a trailing ✓ while the reference is visible

#### Scenario: Import shows the reference by default
- **WHEN** the user imports a reference image and confirms the crop
- **THEN** the editor returns with the reference visible and the "显示参考图" toggle active

### Requirement: Load a reference image through the shared crop screen

The editor SHALL let the user load a reference image. After the user selects an image, the system SHALL proceed through the same crop/scale screen used for pattern generation. On confirmation, the system SHALL pad the cropped region to a centered square (the same square-padding used before generation) and derive the grid-resolution reference from it, then attach it to the current project without regenerating the pattern and return to the editor. Cancelling the crop SHALL return to the editor without changing the reference.

#### Scenario: Selecting a reference image opens the crop screen
- **WHEN** the user chooses to load a reference image and picks an image
- **THEN** the crop/scale screen opens with that image, exactly as for pattern generation

#### Scenario: Confirming the crop attaches the reference
- **WHEN** the user confirms the crop while loading a reference
- **THEN** the grid-resolution reference derived from the cropped region is attached to the current project and the editor is shown, with the pattern left unchanged

#### Scenario: Reference crop is squared to match the grid
- **WHEN** the user confirms a reference crop whose region is not square
- **THEN** the region is padded to a centered square before the reference is derived, so the reference maps 1:1 to the grid without stretching

#### Scenario: Cancelling the crop leaves the reference unchanged
- **WHEN** the user backs out of the crop screen while loading a reference
- **THEN** the editor is shown and the current reference is not modified
