# home Specification

## Purpose
TBD - created by archiving change beautify-home-and-templates. Update Purpose after archive.
## Requirements
### Requirement: Bead-style entry cards

The home screen SHALL present its primary actions (generate from gallery, generate from camera, start free creation) as tappable cards instead of plain buttons. Each card SHALL show a hand-drawn bead/pixel-art icon rendered from a pixel sprite, a title, and a short subtitle. The 自由创作 card SHALL use the subtitle 「空白图纸，创作你的专属」.

#### Scenario: Primary actions shown as cards
- **WHEN** the home screen is shown
- **THEN** the gallery, camera, and free-creation actions appear as tappable icon cards

#### Scenario: Free-creation card subtitle
- **WHEN** the home screen is shown
- **THEN** the 自由创作 card shows the subtitle 空白图纸，创作你的专属

### Requirement: Bead-style app mark

The home screen SHALL show a bead/pixel-art logo next to the "拼豆助手" title.

#### Scenario: App mark visible
- **WHEN** the home screen is shown
- **THEN** a bead-style logo appears beside the app title

### Requirement: Bead tip card

The home screen SHALL show a tip card with a bead-style icon, one short perler-bead tip, and a control to show another tip.

#### Scenario: Cycle through tips
- **WHEN** the user taps the "换一条" control on the tip card
- **THEN** a different tip is shown

### Requirement: Home section order

The home screen SHALL present its sections top to bottom in this order: the app title and tagline; the two generate cards (从相册生成, 拍照生成); the 自由创作 card; the saved-patterns section titled 「我的图纸」 with a 查看全部 entry; the 快速模板 row; the 拼豆小贴士 tip card; and finally a footer entry 「关于与隐私」 that opens the about & privacy page. The saved-patterns section SHALL sit above the template row.

#### Scenario: Sections appear in order
- **WHEN** the home screen is shown
- **THEN** the generate cards and 自由创作 card appear first, followed by the 「我的图纸」 section, then the 快速模板 row, the tip card, and finally the 关于与隐私 footer entry

#### Scenario: About entry opens the about page
- **WHEN** the user taps the 关于与隐私 footer entry on the home screen
- **THEN** the about & privacy page is shown

