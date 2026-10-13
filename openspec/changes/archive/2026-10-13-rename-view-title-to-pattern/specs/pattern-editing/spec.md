# pattern-editing Specification

## Purpose

Define how a generated pattern is interactively viewed and edited in the editor.

## MODIFIED Requirements

### Requirement: Editor screen title

The editor's top bar SHALL show a short title reflecting the current mode: "图纸" in view mode and "编辑" in edit mode. The title SHALL NOT append the grid dimensions (those are shown in the color-table summary line).

#### Scenario: View mode title
- **WHEN** the editor is in view mode
- **THEN** the top bar title is "图纸"

#### Scenario: Edit mode title
- **WHEN** the editor is in edit mode
- **THEN** the top bar title is "编辑"
