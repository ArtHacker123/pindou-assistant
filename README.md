# 拼豆助手（Pindou Assistant）

把照片或卡通图变成拼豆图纸的 Android 应用。导入图片 → 裁切 → 自动生成格子图纸 → 手工编辑 → 导出打印图或项目文件，全程在本机离线完成。

## 功能

### 生成图纸
- 从相册或相机导入图片，支持裁切与缩放。
- 卡通 / 照片两种图像类型，分别采用"色板众数"与"线性光平均"的每格归约方式。
- 网格尺寸 15×15 ~ 128×128，用色数量 4 ~ 128 种（自动或手动指定）。
- 主体抠图：内置 U²-Netp ONNX 模型本地推理，附带多种抠图预设。
- 去杂色：把孤立的异色小块合并到邻近颜色，可选关 / 弱 / 中 / 强。
- 参考图：导入与格子 1:1 对齐的底图，可叠在图纸下对照、临时查看原图。
- 内置色板：MARD 221（默认）、Perler、Hama Midi、Artkal S。

### 编辑图纸
- 工具：画笔、橡皮（单格 / 区域擦除）、取色、油漆桶、色号替换、移动画布。
- 撤销 / 重做，每笔一步，最多 100 步。
- 画笔同色高亮：自动描出当前画笔色的全部格子，可在工具坞开关。
- 信息栏显示当前色号与其同色粒数，随编辑实时更新。
- 编辑中可改网格尺寸（有源图时重新生成，否则清空）或更换色板（按最近颜色重映射），均需确认。

### 导出与分享
- 导出 Form 1 打印图纸 PNG：屏幕尺寸或 A4@300DPI，可选是否嵌入参考图。
- 保存 `.pindou` 项目文件（JSON），可再次打开继续编辑。
- 分享 PNG / `.pindou` 到其他应用。

### 其他
- "我的图纸"历史列表，首页展示最近保存的图纸。
- 内置模板（爱心、星星、笑脸等）可直接从空白开始创作。
- 隐私：未申请网络权限，图片与图纸只在本机处理。

## 界面流程

```
首页 ── 导入图片 / 选择模板 ──> 裁切 ──> 生成 ──> 编辑器（查看 / 编辑）──> 导出 / 分享
  └── 我的图纸 ──> 打开 .pindou
```

## 技术栈

| 项目 | 选择 |
|------|------|
| 语言 | Kotlin 1.9.20 |
| UI | Jetpack Compose（BOM 2023.10.01）、Material 3 |
| 构建 | AGP 8.2.2、Gradle 8.5、JDK 17 |
| Android | minSdk 24 / targetSdk 34，仅 arm64-v8a |
| 序列化 | kotlinx.serialization（`.pindou` 为 JSON） |
| 图片 | Coil + 自定义采样 / 处理管线 |
| 抠图 | ONNX Runtime Android |
| 色彩 | Oklab 距离度量（取代 CIELAB dE76） |

## 项目结构

```
core/                     纯 Kotlin 领域层（无 Android 依赖）
  color/                  sRGB、Oklab
  quant/                  k-means 量化
  imaging/                采样、预处理、抠图 mask、参考图构建
  match/                  颜色策略、色板映射
  generate/               图案生成、去杂色
  editor/                 网格编辑操作（涂 / 擦 / 填充 / 替换 / 重映射）
  model/                  Grid、Palette、PindouProject 等
  serialize/              .pindou 读写
  render/                 Form 1 图纸布局计算
app/                      Android 应用
  ui/                     Compose 界面（首页 / 裁切 / 编辑器 / 导出 / 历史 / 关于）
  state/                  控制器、文档、生成会话、编辑会话、导出服务
  data/                   色板资源、历史存储、导出管理、模板
  ml/                     主体抠图（ONNX 推理与预设）
  assets/palettes/        内置色板 JSON
  assets/u2netp.onnx      主体抠图模型
openspec/                 规格驱动开发：changes/ 提案与 archive/、specs/ 主规格
docs/                     色板数据来源、待办清单
```

## 构建

前置条件：JDK 17、Android SDK 34、Gradle 8.5。

```bash
# 在仓库根目录，首次构建前确保 local.properties 含 sdk.dir
# 单元测试 + Debug APK
gradle :core:test :app:assembleDebug

# Release APK（需仓库根目录的 keystore.properties，缺失时 release 不签名）
gradle :app:assembleRelease
```

说明：

- 本仓库未附带 Gradle Wrapper，请使用本机 Gradle 8.5。
- `build.gradle.kts` 把所有模块的构建输出重定向到 ASCII 路径 `G:/pindou-build`，用于规避 Windows 下非 ASCII 工程路径导致的测试类路径问题，产物位于该目录下 `<模块名>/build/...`。在其他磁盘布局上开发时可自行修改该 `asciiBuildRoot`。
- Release 签名配置从 `keystore.properties`（已 gitignore）读取；该文件不存在时 release 构建仍可执行，只是不签名。

## 规格驱动开发

功能变更通过 [OpenSpec](https://github.com/Fission-AI/OpenSpec) 管理：先在 `openspec/changes/<name>/` 写提案、设计与规格增量，实现完成并通过验证后归档到 `openspec/changes/archive/`，同时把增量合并进 `openspec/specs/` 主规格。

```bash
npx openspec validate --specs --all --strict
```

## 第三方与致谢

- Perler / Hama Midi / Artkal S 色板数据取自开源项目 [maxcleme/beadcolors](https://github.com/maxcleme/beadcolors)（MIT 许可证），字段映射与快照日期见 [docs/palette-sources.md](docs/palette-sources.md)。色值为社区实测的屏幕近似值。
- 主体抠图使用 U²-Netp 模型（`u2netp.onnx`），随应用本地推理。

## 许可证

本仓库尚未添加开源许可证文件。第三方数据与模型的许可情况见上节及 `docs/palette-sources.md`。
