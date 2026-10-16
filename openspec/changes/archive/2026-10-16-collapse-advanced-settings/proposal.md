## Why

The generation parameters panel shows all five controls at once. Two of them — the color-selection strategy (取色方式) and the subject cutout (自动抠出主体) — are "how it's done" tweaks that most generations never touch, yet they add visual weight and push the primary choices down. Grouping them keeps the panel focused on what users actually change, without hiding what is currently set.

## What Changes

- Keep the primary options visible: 图像类型 / 拼豆色板 / 图纸尺寸 / 用色数量.
- Move 取色方式 and 自动抠出主体 (together with its 抠图微调 preset, shown when the cutout is on) into a new collapsible **高级** section, **collapsed by default**.
- Show a **summary of the current values** on the collapsed 高级 header — the color strategy name and whether the cutout is on — so the state stays visible without expanding.
- Tapping the header expands/collapses; the expanded state is remembered for the session.
- No behavior change: the options keep the same effect and the same persistence.

## Capabilities

### New Capabilities
- `generation-settings`: the editor's generation parameters panel — which options are primary, and how the advanced options are grouped and displayed (collapsed 高级 section with a current-value summary).

### Modified Capabilities
<!-- none: the moved options keep the same behavior and requirements; only their presentation changes. -->

## Impact

- `app`: `EditorScreen.ParamsPanel` gains the collapsible 高级 section and its summary header; the two controls move inside it.
- UI-only: no `core`, model, controller, state, or generation-pipeline change — the values already exist and persist as today.
