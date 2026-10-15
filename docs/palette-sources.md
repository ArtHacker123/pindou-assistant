# 色板数据来源

App 内置的拼豆色板以 JSON 资源形式放在 `app/src/main/assets/palettes/`。
每个文件自描述：`name`（= 文件名，用作标识）、`displayName`（展示名）、`version`、`items`。

## 数据来源

除 `mard_221.json` 外，新增的 Perler / Hama Midi / Artkal S 色板取自开源项目
**[maxcleme/beadcolors](https://github.com/maxcleme/beadcolors)**（站点
<https://beadcolors.eremes.xyz/>），采用 **MIT 许可证**。

- 原始 CSV 入口（`/raw` 目录，格式 `code,name,r,g,b,contributor`）：
  - Perler：<https://beadcolors.eremes.xyz/raw/perler.csv>
  - Hama Midi：<https://beadcolors.eremes.xyz/raw/hama.csv>
  - Artkal S-5MM：<https://beadcolors.eremes.xyz/raw/artkal_s.csv>
- 快照日期：2026-10-15（一次性 vendored；如需更新，对照上游 `/raw` 重新生成）。

## 字段映射

| CSV 字段 | JSON 字段 | 说明 |
|---|---|---|
| `reference_code` | `code` | 保留品牌官方色号（如 `80-19001`、`H01`、`S01`） |
| `name` | `name` | 官方英文色名 |
| `rgb_r` | `r` | 0–255 |
| `rgb_g` | `g` | 0–255 |
| `rgb_b` | `b` | 0–255 |
| `contributor` | — | 不落库 |
| — | `id` | 按 `code` 自然序排序后从 1 递增 |
| — | `brand` | 品牌名（`Perler` / `Hama` / `Artkal`） |

`items` 按 `code` 的**自然序**（`NaturalOrder`，数字段按数值比较，如 `S2` < `S10`）排列。

## 说明

- 这些 RGB 为社区实测的**屏幕近似值**，受光线、批次与材质影响，不等同于实物色。
  特殊系列（透明、珠光、荧光、夜光等）无法用单一 RGB 完整表示。
- 每个色板文件由 `BundledPaletteFileTest` 校验（文件名与 `name` 一致、色号唯一、
  RGB 范围、`id` 连续 `1..N`、色号自然序）。
- 默认色板固定为 `mard_221`（见 `com.pindou.core.palette.PaletteDefaults`），
  不随资源文件排序变化。
