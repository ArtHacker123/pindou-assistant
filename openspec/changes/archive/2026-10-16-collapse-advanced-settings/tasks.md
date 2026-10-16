## 1. Panel restructure (app)

- [x] 1.1 Add a collapsible "高级" header (title + current-value summary + expand indicator) to `EditorScreen.ParamsPanel`, backed by a session-local expansion state
- [x] 1.2 Move 取色方式 and the 自动抠出主体 block (toggle + 抠图微调) inside the expandable section, preserving their enabled/visibility conditions
- [x] 1.3 Build the collapsed summary from the current color strategy name and the cutout state; update it whenever either changes
- [x] 1.4 Keep the primary options (图像类型 / 拼豆色板 / 图纸尺寸 / 用色数量) visible; default the section to collapsed and keep it expanded within the session

## 2. Verification

- [x] 2.1 `:app:assembleDebug` passes
- [x] 2.2 Install the APK and have the user verify: primary options stay visible; 高级 starts collapsed with a correct summary; expanding reveals both controls, which still work and persist; the section stays expanded within the session
- [x] 2.3 Re-validate the change `--strict`
