# pattern-templates Specification

## Purpose
TBD - created by archiving change beautify-home-and-templates. Update Purpose after archive.
## Requirements
### Requirement: Quick-start templates

The home screen SHALL offer a row of built-in quick-start templates, each defined as a pixel-art sprite with a name. Tapping a template SHALL create a new free-creation project whose grid is the template sprite at the template's own size, with every pixel mapped to the nearest color of the default bead palette, and SHALL open it directly in edit mode.

#### Scenario: Apply a template
- **WHEN** the user taps a template on the home screen
- **THEN** a new pattern matching the template is created at the template's own size and opened in edit mode

#### Scenario: Template colors map to the palette
- **WHEN** a template is applied
- **THEN** each pixel uses the nearest color available in the default bead palette

