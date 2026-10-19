## ADDED Requirements

### Requirement: Edit mode info bar shows the brush color bead count

In edit mode, the info bar SHALL display, alongside the current brush color code, the number of beads of that color code in the entire pattern. The count SHALL be recomputed from the live pattern so it stays accurate as the pattern is edited, and SHALL use the same "同色 N 粒" wording as the view-mode info bar. When no brush color is selected, the info bar SHALL keep its existing "please pick a color" hint and SHALL NOT show a count. The view-mode info bar SHALL be unaffected.

#### Scenario: Picking a brush color shows its code and bead count
- **WHEN** edit mode is active and the user taps a color code in the bottom color table to set it as the brush
- **THEN** the info bar shows the brush color code and a bead count equal to the number of cells in the grid carrying that code

#### Scenario: The count updates live as the pattern is edited
- **WHEN** the brush color is `A5` with a live count shown, and the user paints an additional `A5` bead, erases one, performs a global replace, or undoes/redoes
- **THEN** the displayed count reflects the new number of `A5` beads on the next frame with no extra user action

#### Scenario: A color not yet used on the pattern shows a zero count
- **WHEN** the user picks a brush color that appears in the palette but has no beads on the grid
- **THEN** the info bar shows the color code and a count of 0

#### Scenario: No brush color means no count
- **WHEN** edit mode is active with no brush color selected
- **THEN** the info bar keeps the existing "请在下方选择珠子颜色" hint and shows no bead count

#### Scenario: View mode info bar is unchanged
- **WHEN** the user is in view mode
- **THEN** the info bar behaves exactly as before this change (cell position, color code, color name, and same-color count driven by the tapped cell)
