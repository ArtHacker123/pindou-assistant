## MODIFIED Requirements

### Requirement: Home section order

The home screen SHALL present its sections top to bottom in this order: the app title and tagline; the two generate cards (从相册生成, 拍照生成); the 自由创作 card; the saved-patterns section titled 「我的图纸」 with a 查看全部 entry; the 快速模板 row; the 拼豆小贴士 tip card; and finally a footer entry 「关于与隐私」 that opens the about & privacy page. The saved-patterns section SHALL sit above the template row.

#### Scenario: Sections appear in order
- **WHEN** the home screen is shown
- **THEN** the generate cards and 自由创作 card appear first, followed by the 「我的图纸」 section, then the 快速模板 row, the tip card, and finally the 关于与隐私 footer entry

#### Scenario: About entry opens the about page
- **WHEN** the user taps the 关于与隐私 footer entry on the home screen
- **THEN** the about & privacy page is shown
