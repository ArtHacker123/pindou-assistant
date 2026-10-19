## Why

编辑模式下用户经常需要"边涂边看同色分布"（数格子、找漏涂、分组施工），但当前编辑模式完全没有高亮：`PatternGrid` 在编辑模式收到的 `selectedCode` 恒为 null。查看模式的点击高亮（Form 2 聚光）在编辑模式不可用，因为同一个"单击"手势已被工具占用（画笔/油漆桶/吸管等）。

## What Changes

- 编辑模式新增「画笔联动同色高亮」：底部色板/取色器选中某个色号后，画布自动给所有同色格描边高亮（**只描边、不压暗**其余格子——编辑模式需看清全图）
- 高亮跟随当前画笔：换色即切换高亮对象；该色号从网格上消失（全部被替换/擦除）时高亮自然无匹配格
- 浮动工具坞新增「同色高亮」开关图标，默认开启，可随时关闭；关闭后选色不再触发高亮
- 查看模式的 Form 2 聚光高亮（描边+压暗+坐标）行为不变

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `pattern-editing`: 新增「编辑模式画笔联动同色高亮」requirement——选色即高亮同色格（只描边不压暗）、高亮跟随画笔、工具坞开关可关、与所有编辑工具手势兼容

## Impact

- `app/src/main/kotlin/com/pindou/app/ui/editor/PatternGrid.kt`：新增编辑模式高亮渲染分支（描边不压暗）
- `app/src/main/kotlin/com/pindou/app/ui/editor/EditorScreen.kt`：编辑模式把 `brushCode` 作为高亮对象传入 `PatternGrid`；`FloatingToolbar` 加开关按钮
- `app/src/main/kotlin/com/pindou/app/state/EditingSession.kt`：高亮开关状态
- 无 core 模块改动、无 `.pindou` 格式改动、无手势逻辑改动（零冲突）
