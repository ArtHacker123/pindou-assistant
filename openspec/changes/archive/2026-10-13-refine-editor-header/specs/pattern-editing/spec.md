# pattern-editing Specification

## Purpose
TBD - created by archiving change add-pattern-editing. Update Purpose after archive.
## ADDED Requirements

### Requirement: Editor screen title

The editor's top bar SHALL show a short title reflecting the current mode: "生成" in view mode and "编辑" in edit mode. The title SHALL NOT append the grid dimensions (those are shown in the color-table summary line).

#### Scenario: View mode title
- **WHEN** the editor is in view mode
- **THEN** the top bar title is "生成"

#### Scenario: Edit mode title
- **WHEN** the editor is in edit mode
- **THEN** the top bar title is "编辑"
