## MODIFIED Requirements

### Requirement: Bead color picker grouped by series
The system SHALL let the user choose the brush color from the full palette, grouped by bead series (the leading letters of a color code, e.g. `A`, `C`, `M`). The picker SHALL be opened by tapping the current-color swatch in the tool bar, SHALL offer a series selector for quick indexing, and SHALL set the brush color when a swatch is tapped. When entering edit mode, the brush color SHALL default to the black bead of the palette in use — the bead whose name is `Black` if the palette has one, otherwise the palette color nearest to pure black — and the default SHALL be resolved against the current palette (re-resolved after an in-editor palette change) so it never carries a code from another palette.

#### Scenario: Open the picker and choose a color
- **WHEN** the user taps the current-color swatch in the tool bar
- **THEN** the picker opens with series selectors, and tapping a swatch sets that color as the brush color and closes the picker

#### Scenario: Default brush is the current palette's black
- **WHEN** the user enters edit mode (including starting free creation) or switches palettes in the editor
- **THEN** the brush color defaults to the current palette's Black bead — e.g. `80-19018` on Perler, `H18` on Hama, `S13` on Artkal, `H7` on MARD 221 — so the current-color swatch is always available to open the picker and never shows a code belonging to a different palette

#### Scenario: Palette without a bead named Black
- **WHEN** the current palette has no bead named `Black`
- **THEN** the default brush is the palette color nearest to pure black

#### Scenario: Series selectors are distinguishable and scrollable
- **WHEN** the color picker is open
- **THEN** each series selector shows a representative series color (e.g. the average color of its swatches) next to its letter, and left/right arrows scroll the selector row, disabling at either end
