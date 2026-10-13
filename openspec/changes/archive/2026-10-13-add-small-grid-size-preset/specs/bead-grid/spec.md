# bead-grid Specification

## Purpose
TBD - created by archiving change pindou-android-pattern. Update Purpose after archive.
## ADDED Requirements

### Requirement: Grid size presets include a 15×15 option

Wherever the user chooses a pattern size — generating from an image (the parameters panel) and starting a free creation (the size picker) — the system SHALL offer the same set of square preset sizes, and SHALL include a 15×15 option (matching the quick templates) alongside the existing larger presets, up to the 128×128 maximum.

#### Scenario: 15×15 is offered
- **WHEN** the user opens a pattern size selector (generate parameters or free creation)
- **THEN** 15×15 is among the available size options

#### Scenario: Generate at 15×15
- **WHEN** the user selects 15×15 and generates a pattern
- **THEN** the pattern is produced on a 15×15 grid