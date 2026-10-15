## 1. Core helper

- [x] 1.1 Add `PaletteDefaults.defaultBrushItem(palette): PaletteItem?` — the bead named `Black` (case-insensitive, trimmed) else the color nearest to pure black (`min r*r + g*g + b*b`)
- [x] 1.2 Add a `:core:test` covering the named-Black case and the nearest-black fallback (e.g. synthetic palettes)

## 2. Editor wiring

- [x] 2.1 `EditingSession.defaultBrushCode` delegates to `PaletteDefaults.defaultBrushItem(...)?.code`
- [x] 2.2 `EditingSession.enter()` resolves the brush to the current palette's default unconditionally
- [x] 2.3 `EditingSession.remapTo(palette)` re-resolves the brush against the new palette

## 3. Verification

- [x] 3.1 `:core:test :app:assembleDebug` passes
- [x] 3.2 Install the APK and have the user verify: entering edit mode shows the palette's black (MARD `H7`, Perler `80-19018`, Hama `H18`, Artkal `S13`), and switching palettes in the editor updates the default
- [x] 3.3 Re-validate the change `--strict`
