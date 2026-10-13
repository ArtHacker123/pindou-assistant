# pattern-editing Specification

## Purpose
TBD - created by archiving change add-pattern-editing. Update Purpose after archive.
## MODIFIED Requirements

### Requirement: Eyedropper prefers the reference during a peek
While the reference image is shown on its own (the press-and-hold peek temporarily shows only the reference), the eyedropper SHALL sample the reference first — even over an existing bead — so the picked color matches what the user sees. Without a reference image it SHALL fall back to the bead code.

#### Scenario: Pick the visible reference color over a bead
- **WHEN** the user peeks (holding the "按住看参考图" control) and taps a cell that has a bead with the eyedropper
- **THEN** the brush color becomes the nearest palette color to that cell's reference pixel, not the bead's code

#### Scenario: Peek without a reference
- **WHEN** the user uses the eyedropper while peeking but no reference image is available
- **THEN** the brush color falls back to the tapped bead's code
