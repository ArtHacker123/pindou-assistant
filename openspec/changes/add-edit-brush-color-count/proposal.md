## Why

编辑模式下用户靠新增的同色高亮来识别"当前画笔色铺在哪些格子"，但高亮只回答"在哪里"，不回答"有多少"。用户在排料、估算用量时需要立刻知道当前色号总共要用多少粒，目前必须退到查看模式点格子才能看到这个数字。

查看模式的 InfoBar 已经会显示「同色 N 粒」（`EditorScreen.kt` 的 `InfoBar` 非编辑分支），编辑模式却只显示「当前色号 A5」——同样的信息在编辑模式缺失，是不一致的。

## What Changes

- 编辑模式的信息栏在显示当前画笔色号的同时，显示该色号在整张图纸中的粒数。
- 粒数随涂色、擦除、替换、撤销/重做实时刷新（沿用现有按 project 计数的机制，不新增状态）。
- 未选择画笔色时保持现有提示文案，不显示粒数。
- 查看模式的信息栏行为保持不变。

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `pattern-editing`: 新增一条编辑模式信息栏的读数要求——显示当前画笔色号及其在整张图纸中的同色粒数，并实时刷新。

## Impact

- 代码：`app/src/main/kotlin/com/pindou/app/ui/editor/EditorScreen.kt`（`InfoBar` 编辑分支 + 调用处传入粒数）。
- 不涉及 core、不涉及 `.pindou` 文件格式、不涉及手势与工具行为。
- 计数来源复用调用处已有的 `counts`（`remember(project)`），编辑替换网格时 `DocumentStore.replaceGrid` 走 `copy()` 生成新 project 实例，故计数自动随编辑变化，无需新增订阅或缓存失效逻辑。
