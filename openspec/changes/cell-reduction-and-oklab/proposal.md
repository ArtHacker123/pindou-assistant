## Why

参考图生成时，每个网格先把源图区域压成「线性光平均色」，再吸附到最近色板色。对卡通/线稿，一格跨越色块边界会把黑边与白底混成色板里不存在的灰，吸附必然偏色；同时吸附用的 CIE76 (CIELAB) 色差对高饱和色、蓝色、深色不够贴合人眼。需要按图像类型选择更合适的「格内取色」方式，并升级感知色差度量，以提升像素到色号的映射准确度。

## What Changes

- 新增「格内取色（cell reduction）」概念，按图像类型分流：
  - 卡通（线稿为主）→ **色板众数**：把格内每个像素吸附到色板，取出现最多的色号作为该格代表色。
  - 照片 → 保持**线性光平均**（现状）。
- 网格采样在**一次遍历**内同时产出：用于参考图的线性光平均色，以及用于吸附的代表色（卡通=众数，照片=平均）。
- 参考图 `referenceGrid` **仍存线性光平均色**（语义不变）。
- **默认感知色彩空间由 CIELAB(CIE76) 改为 Oklab**（欧氏空间，可直接替换；对高饱和/蓝色/深色更准），用于吸附、reference-direct 选色、k-means 与取色工具；保留 `LabColorSpace` 供对照与既有测试。
- 众数所需的「像素→最近色号」查询用**量化 LUT**（每通道 5 bit，32³）加速，仅卡通路径使用。
- 卡通不再走 `fromReference` 参考图复用快速路径（众数需从预处理后的原图重新归约），改为按原图重新采样；照片路径保持快速路径不变。

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `imaging-pipeline`: 网格采样要求从「仅每格线性光平均」扩展为「一次遍历同时产出：参考图用平均色 + 吸附用代表色，代表色按图像类型（卡通=色板众数、照片=线光平均）」。参考图仍由平均色产生。
- `color-quantization`: 感知比较空间由 CIELAB(dE76) 改为 Oklab；reference-direct 选色改为基于每格代表色（而非仅平均色）。

## Impact

- core（新增/修改）：
  - 新增 `color/Oklab.kt`（Oklab 编解码）；`color/ColorSpace.kt`（新增 `OklabColorSpace`，不动 `LabColorSpace`）。
  - `imaging/AreaColor.kt`、`imaging/GridSampler.kt`（支持 `reduction` 与 palette，产出代表色）。
  - `generate/PatternGenerator.kt`（按图像类型选归约、用代表色吸附、参考图仍存平均）。
  - `match/ReferenceDirectStrategy.kt`、`match/PaletteMapper.kt`、`match/KMeansStrategy.kt`、`quant/KMeansQuantizer.kt`、`editor/GridEditor.kt`（默认 space 改 Oklab）。
- app：`state/GenerationSession.kt`（卡通不传参考图复用、按类型驱动归约）。
- specs：`imaging-pipeline`、`color-quantization`。
- 测试：新增 MODE 归约与 Oklab 用例；`GridSamples` 新增字段带默认值以兼容现有构造。
- 无 `.pindou` 存储格式变更；无 UI 变更（沿用「图像类型」下拉触发）。
