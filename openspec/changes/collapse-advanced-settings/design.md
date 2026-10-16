## Context

The generation parameters panel (`EditorScreen.ParamsPanel`) lists the generation controls: image type, palette (in a row), pattern size, color count (a row), color strategy (a row), and the subject-cutout block (a toggle row plus a conditional preset dropdown). The panel already reads all values from and writes them through `PindouController`; nothing about the values or their persistence changes here — only their placement.

## Goals / Non-Goals

**Goals:**
- Reduce clutter so the panel leads with the options users actually change.
- Keep the advanced settings discoverable and their current state visible without expanding.

**Non-Goals:**
- Moving options to a separate screen, bottom sheet, or tab set.
- Reordering or re-styling the primary options.
- Changing any option's effect, defaults, or persistence.

## Decisions

- **D1 — Inline collapsible section, not a bottom sheet.** Only two controls move, so an inline disclosure is lighter and keeps context. A "更多设置" bottom sheet was considered and deferred: it is more scalable if the list grows, but heavier for two controls.
- **D2 — Collapsed header shows a current-value summary** (color strategy · cutout state) rather than a bare "高级": it preserves information scent and keeps the cutout feature discoverable. A bare header is simpler but hides the state.
- **D3 — Collapsed by default; expansion remembered in-session** via `remember` (not persisted). Advanced users can pin it open for a session; restarting collapsed is acceptable and avoids a new preference key.
- **D4 — The cutout toggle stays inside 高级**, and 抠图微调 remains nested and shown only when the cutout is on — as today.
- **D5 — No controller/state changes.** The moved controls already read/write the existing `controller` state; only their position in the composable changes. The session-scoped expansion flag is local UI state.

## Risks / Trade-offs

- [Cutout feature becomes less discoverable] → put its on/off state in the collapsed summary, so users see it exists and whether it is on.
- [Session expansion not persisted] → acceptable: advanced use is rare; defaulting collapsed on restart is fine.
- [Panel height / scrolling] → the collapsed section shortens the panel; expanding restores the prior height; no new scrolling behavior is introduced.

## Open Questions

- Exact summary wording (e.g. `取色：参考图直取 · 抠图：开`) — implementation detail, decide while building.
