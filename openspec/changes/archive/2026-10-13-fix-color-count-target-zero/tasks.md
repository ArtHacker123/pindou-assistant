## Fall back to automatic for an unset color count

- [x] 1.1 `GenerationSession.applyFromProject` maps `colorCountTarget <= 0` to automatic (auto = true, target = 16).
- [x] 1.2 `GenerationSession.runGenerate` uses `colorCountTarget.coerceAtLeast(1)` so generation never uses zero colors.
- [x] 1.3 `:core:test :app:assembleDebug` passes and the app installs; verified by the user.

## Verification

- [x] 2.1 Open a saved blank pattern: "用色数量" shows 自动 (not 0).
- [x] 2.2 After opening a blank pattern, generate from a gallery/camera image: it uses the automatic count, not 0 / single color.
