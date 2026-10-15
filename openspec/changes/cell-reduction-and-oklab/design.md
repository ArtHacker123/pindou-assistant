## Context

`PatternGenerator` 目前的映射是两段：`GridSampler.sample` 用 `AreaColor.cell` 把每个网格对应的源图区域**在线性光下求平均**，得到 `GridSamples.avgRgb`；再由颜色策略选出可用豆色集，最后 `nearestIndex` 把每格平均色吸附到最近豆色（CIE76/CIELAB）。

`GridSamples.avgRgb` 同时被 `ReferenceBuilder` 用来生成网格分辨率参考图。生成编排在 `GenerationSession`：先 `references.reference(processed, gw, gh)` 建参考图，再把它作为 `reference` 传给 `PatternGenerator`（`generate` 走 `GridSampler.fromReference` 复用快速路径），并把同一张参考图缓存给后续重生成复用。

问题：① 面积为每格唯一媒介，跨色块边界的格子被塌缩成色板里不存在的中间色；② CIE76 对高饱和/蓝色/深色偏差大。

## Goals / Non-Goals

**Goals:**
- 按图像类型选择格内取色方式：卡通=色板众数，照片=线性光平均。
- 一次遍历同时产出「参考图用平均色」与「吸附用代表色」。
- 参考图 `referenceGrid` 语义保持「线性光平均」不变。
- 默认感知空间升级为 Oklab（欧氏，零接口改动）。
- 保持 `LabColorSpace`、既有策略注册与 `.pindou` 存储格式不变。

**Non-Goals:**
- 不引入抖动（dithering）。
- 不实现 CIEDE2000。
- 不改「参考图即平均色」的展示语义（不改成 representative）。
- 不改 UI（沿用「图像类型」下拉）。

## Decisions

### 1. 用「代表色（representative）」表达每格吸附输入，参考图仍用平均色
`GridSamples` 增加可选字段 `repRgb: Array<Rgb>? = null`，并加访问器 `rep(i) = repRgb?.get(i) ?: avgRgb[i]`（默认回退到平均）。`avgRgb` 语义不变、仍供 `ReferenceBuilder` 使用；`ReferenceDirectStrategy` 与 `PatternGenerator` 的吸附改用 `rep(i)`。
- 为什么：既向后兼容现有构造/测试（带默认值），又让「参考图=平均、吸附=代表」两种用途各取所需。
- 备选：把代表色直接覆盖进 `avgRgb`（会污染参考图语义）——否决。

### 2. 归约方式由图像类型决定，进入 `GridSampler.sample`
新增枚举 `CellReduction { AVERAGE, MODE }`；`GridSampler.sample(image, gw, gh, reduction = AVERAGE, palette: Palette? = null)`。`MODE` 需要 palette。`ReferenceBuilder.build(image, gw, gh)` 仍以 `AVERAGE` 调用，与 palette 无关。
- 为什么：归约需要 palette，放在采样帧内一次完成，避免二次遍历。
- 备选：在生成层用原图重扫算众数（两次遍历）——否决。

### 3. 众数实现：per-cell 直方图 + 32³ 最近色 LUT
- 每个 palette 构建一次 `PaletteLut`：把 sRGB 各通道 5 bit（32 级）的三维立方（32³=32768 项）映射到最近 palette 索引，最近判定用 Oklab。
- 每格：复用一块 `IntArray(palette.size)` 直方图，遍历格内有效像素 → `lut[bin]` → `hist[idx]++`；取计数最大者（并列取自然色号小者，保证确定性）。代表色 = 该 palette 色。`avgRgb` 在同一次遍历里照常累加；`valid`/`coverage` 逻辑不变。
- 为什么：逐像素对全色板求最近色是 O(像素×色板)（4.2M×221），不可接受；5 bit LUT 把最近色查询变成 O(1)，众数是多数投票对量化误差鲁棒。
- 备选：全精度最近色（太慢）；精确 8 bit LUT（256³ 内存过大）——否决。

### 4. 默认色彩空间改为 Oklab
新增 `OklabColorSpace : ColorSpace`（sRGB→linear→LMS(cbrt)→Oklab，欧氏距离）。把以下默认 `space` 由 `LabColorSpace` 改为 `OklabColorSpace`：`PatternGenerator`、`ReferenceDirectStrategy`、`PaletteMapper`、`KMeansStrategy`、`KMeansQuantizer`、`GridEditor.pick`。
- 为什么：Oklab 是真欧氏空间，`nearestIndex`/贪心/k-means 全部零改动即可用；比 CIE76 明显更贴人眼（尤其饱和色/蓝/深色）。
- 备选：CIEDE2000（非欧氏，需扩展距离接口并改所有消费点，成本高）——本变更不做，留待后续。

### 5. 卡通路径不复用参考图快速路径
`GenerationSession` 仍构建并缓存参考图（用于展示/照片复用）。`PatternGenerator.generate` 对 `MODE` 归约**忽略传入的 `reference`**、直接 `GridSampler.sample(processed, gw, gh, MODE, palette)`；对 `AVERAGE` 保持 `fromReference` 复用。生成的 `referenceGrid` 仍由本次采样的 `avgRgb` 构建，故与展示参考图一致。
- 为什么：众数无法从「平均色参考图」还原，必须回到预处理原图。
- 代价：卡通每次生成多一次每格扫描（约数十毫秒级），远小于预处理开销。

### 6. 归约由图像类型映射
`ImageType.CARTOON → MODE`，`ImageType.PHOTO → AVERAGE`。映射放在 `PatternGenerator`（一个小函数），不污染 `ImageType` 枚举语义。

## Risks / Trade-offs

- [LUT 量化在色板边界附近可能误投票] → 5 bit/通道减少误判；众数为多数投票，个边界像素不影响；最终豆色仍是真实色板色，Display 精确。必要时可升到 6 bit/通道（256KB）。
- [卡通生成失去参考图复用，变慢] → 仅多一次每格扫描；实测远小于预处理（曾达数十秒级，属另一议题）。
- [默认空间变更导致既有 k-means/换色板结果与旧版略有不同] → 属预期改善；覆盖相关单测期望。
- [卡通下参考图（平均）与网格（众数）观感不一致] → 有意的：参考图是描摹/明暗底图；文档与规格中明确。
- [`GridSamples` 新增字段破坏位置参数构造] → 新字段置于末尾并带默认值，现有命名/位置构造仍编译。
- [Oklab 数值量级与 Lab 不同] → 不影响 argmin；贪心权重用 `count^0.5` 与距离量级无关。
