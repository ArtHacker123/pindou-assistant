# 拼豆助手 · 待办清单（TODO / Roadmap）

记录**尚未实现**的功能与优化想法，方便慢慢推进。来源：已归档 OpenSpec change 的 Non-Goals / Open Questions / Risks，以及开发过程中确认延后的事项。

维护方式：新想法先记到这里；某个条目想动手时，再用 OpenSpec 提案（`openspec-propose`）走正式流程。勾选表示"已实现"（多数已归档，少数为直接提交而未单独归档）。

---

## A. 性能优化（生成管线）

> 分析见 `core/color/Srgb.kt`、`app/data/SourceHasher.kt`、`core/generate/*`。瓶颈是全图 O(W×H) 且每次防抖重生成都重算的几趟遍历；k-means 已被 `maxSamples=8000` 封顶，与分辨率无关。推荐实施顺序如下。

- [ ] A1 `Srgb.toLinear/fromLinear` 用 256 项 LUT 替换每像素 3×`Math.pow`（收益最大、改动最小）
- [x] A2 缓存源哈希：`SourceHasher` 逐像素 SHA-256 每次重生成都跑；按工作图缓存，或改廉价滚动哈希（种子不需要密码学强度）—— 已按工作图对象缓存，并把逐像素 `update` 合成分块批量（`GenerationSession.hashFor` + `SourceHasher.CHUNK_BYTES`）
- [x] A3 缓存"预处理 + 采样"：其结果只依赖（工作图, imageType），跨色数滑块的重复重生成可复用（目前每次重算）—— 预处理按（工作图, imageType）缓存、参考图按 `ReferenceKey(图, 类型, gw, gh)` 缓存（照片复用；卡通走众数不复用参考图）
- [ ] A4 降低工作分辨率：`ImageLoader.MAX_DIMENSION=2048` × `inSampleSize`，上限可达 4096 边（~16.7M px）；评估按目标格数自适应
- [x] A5 吸附阶段避免重复编码：`ColorSpace.nearestIndex` 每格对每候选珠色重复 `encode`，可预算并缓存已用珠色的编码向量 —— 已用珠色预算一次（`usedLab`），并走扫描 `DoubleArray` 的 `nearestIndex` 重载
- [ ] A6 `referenceGrid` 的 hex 编码约使 128×128 `.pindou` 体积增至 3 倍；评估 RLE / base64（在 `ReferenceGrid` 内部封装，不破坏格式）

## B. 图纸编辑 / 参考创作

> 来源：`add-grid-reference-image` 的 Non-Goals；D1 预留"replace all color X with Y"；grid-reference-image 已备好 1:1 对齐的参考底图。

- [x] B1 完整图纸编辑：涂 / 擦 / 替换（paint / erase / replace）单个或多个格子 —— 画笔 / 橡皮（单格 + 区域擦除）/ 油漆桶（flood fill）/ 取色 / 移动（`add-pattern-editing`、`connected-region-erase`）
- [x] B2 基于参考图创作：导入参考图后手工编辑珠子网格（参考图作底层，编辑层叠其上）（`add-grid-reference-image`、`reference-image-via-crop`、`refine-reference-import`）
- [ ] B3 "把某色号全部替换为另一色号"（D1 明确预留的跨设备能力）
- [x] B4 编辑历史：撤销 / 重做（`EditingSession` 历史，每笔一步，上限 100）

## C. 图像输入

> 来源：`pindou-android-pattern` 的 Non-Goals 与 D12。

- [ ] C1 前景分割 mask：`PixelImage.mask` 已预留钩子（当前回退到 alpha 判定）；需产出 mask 并贯通处理器 API
- [ ] C2 更多 image-type 预设（架构支持，当前仅卡通 / 照片）
- [ ] C3 自动图像类型识别（非目标，架构允许后续）

## D. 导出 / 分享

> 来源：MVP Non-Goals 与 Trade-offs。

- [ ] D1 矢量导出：SVG / PDF
- [ ] D2 更高导出分辨率（单 bitmap 物理受限，需分片拼接才能到 8K 级）
- [ ] D3 `.pindou` 压缩（gzip）以缩小体积

## E. 色板 / 色彩

> 来源：D13、D14 与 Open Questions。

- [ ] E1 多品牌 / 多套色板可选（架构支持 `PaletteProvider`，当前仅 MARD 221）
- [ ] E2 CIEDE2000 替代 dE76（同一 `ColorSpace` seam；更准但更重）—— 已先以 **Oklab** 取代 dE76 作为默认度量（`cell-reduction-and-oklab`），CIEDE2000 仍是候补
- [ ] E3 UI 暴露随机种子开关（当前固定用 `sourceHash` 派生）

## F. 交互 / UI

> 来源：D5 决策、D15 遗留、用户明确"暂不处理"。

- [ ] F1 设备兼容 / 自适应 UI（含转屏状态保存：`PindouApp` 用 `remember` 持有 controller，转屏会丢状态）—— **用户已明确暂时不处理**
- [ ] F2 D15 遗留：容器尺寸变化（如面板折叠）时 `fitScale` 不重算，缩放 floor 反映的是上次 refit；布局变化更大时需重算
- [ ] F3（可选）纯对比模式下的可拖动 wipe 分割（本次因"分割屏不适合创作"被否；仅作对比用途可再议）

## G. 渲染性能（查看态单屏）

> 现象：128×128 时拖动底部"用色"色板行明显不跟手。属**查看态**单屏（上面铺满视口的网格 + 下方色板行）的既有的单屏合成/绘制开销；128 下网格可见格数与 `paletteUsed` 都变多才显现（已排除与 `refactor-imaging-and-state` 有关：该重构仅改 `EditorScreen` 的一行 import 与控制器门面，`PatternGrid`/颜色行渲染代码未动）。需先帧剖析再定优化方案。

- [ ] G1 帧剖析定位：128 下用 `adb shell dumpsys gfxinfo com.pindou.app` / Perfetto 抓滚动帧，区分瓶颈在"网格 Canvas 绘制"还是"颜色行 item 组合"
- [ ] G2（候选）把网格 `Canvas` 隔离为独立图层（`graphicsLayer` / `drawWithCache`），避免与颜色行滚动互相拖累
- [ ] G3（候选）颜色行抽为独立可组合、只读必要状态；`paletteUsed` 多时降低单 item 成本
- [ ] G4（候选）128 fit 视图下减少绘制量（网格线/分组线批量或按需绘制）

---

## 参考：已完成（背景）

- 基础管线：导入（相册 / 相机）→ 裁切缩放 → 生成 → Form 1 导出 / Form 2 交互（`pindou-android-pattern`）
- 图纸编辑：画笔 / 橡皮（单格 + 区域擦除）/ 油漆桶 / 取色 / 移动；每笔一步的撤销重做（`add-pattern-editing`、`connected-region-erase`）
- 感知色彩管线：**Oklab** 度量（取代 CIELAB dE76）；卡通按色板众数、照片按线性光平均的每格归约；格直接吸附（`reference-color-selection`、`cell-reduction-and-oklab`）
- 网格参考图：与格子 1:1 对齐的参考底图 + 图层开关对比 + `.pindou` 默认嵌入且可退出、按依赖缓存复用；经裁切导入、可作创作底层（`add-grid-reference-image`、`reference-image-via-crop`、`refine-reference-import`）
- 生成性能：分块批量 SHA-256 + 生成参数（预处理 / 参考图 / 源哈希）按依赖缓存复用（`reference-color-selection` 系列提交）
