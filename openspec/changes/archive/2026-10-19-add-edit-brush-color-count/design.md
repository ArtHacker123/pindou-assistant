## Context

编辑模式信息栏（`EditorScreen.kt` 的 `InfoBar`，编辑分支）目前显示「工具：画笔」「当前色号 A5」。查看模式的 `InfoBar` 分支已经显示「同色 N 粒」，但那是由用户点击格子产生的 `selectedCode` 驱动的，编辑模式用的是 `brushCode`，两者互不相通。

调用处（`EditorScreen.kt:539`）已经算好了一份计数：`val counts = remember(project) { project.grid.cells.filterNotNull().groupingBy { it }.eachCount() }`，目前只传给查看分支。`DocumentStore.replaceGrid` 通过 `p.copy(grid = grid, ...)` 生成新的 `PindouProject` 实例。

## Goals / Non-Goals

**Goals:**
- 编辑模式信息栏在显示当前画笔色号的同时显示该色号在整张图纸中的粒数。
- 粒数随编辑实时刷新（涂、擦、替换、撤销、重做）。
- 与查看模式「同色 N 粒」的措辞保持一致。

**Non-Goals:**
- 不改变查看模式信息栏的任何行为。
- 不显示颜色名、不按行列分组统计、不做点击粒数联动高亮。
- 不引入新的持久化或状态存储。

## Decisions

**D1 计数来源复用调用处的 `counts`，不给 controller 加接口。**
`brushCount` 之类的新 getter 会在 `PindouController` 里重复一次全图扫描；而调用处已有同一份 `counts`，编辑分支和查看分支用的是同一个 `project`，直接传参即可。备选方案（controller 暴露 `brushCount: Int`）被否，理由是无谓的重复计算与新的状态面。

**D2 实时刷新依赖现有机制，不写失效逻辑。**
每次编辑都会经 `DocumentStore.replaceGrid` / `replaceGridAndPalette` 生成新的 project 实例，`remember(project)` 因 key 变化重算 `counts`，`InfoBar` 因读取到新的 `counts` 重组合。故不需要手动 invalidate、也不需要把计数挂到 `EditingSession` 上。风险点是将来若有人把 `replaceGrid` 改成原地修改 `cells`，计数会静默失效——该点已在 spec 中以"实时刷新"的场景固化。

**D3 无画笔色时保持现有提示，不显示粒数。**
`brushCode == null`（空色板/未选色）沿用「请在下方选择珠子颜色」。粒数为 0 时照常显示「同色 0 粒」——这是有意义的反馈（该色号还没在图纸上出现过），与查看模式"点到哪算哪"的语义不冲突。

**D4 文案与位置沿用查看模式。**
在编辑分支同一行、当前色号之后追加「同色 N 粒」，不新起一行，不加图标。理由：信息栏是紧凑状态条，两处措辞一致可降低认知成本。

## Risks / Trade-offs

- [计数在大图上的性能] → 全图 `filterNotNull().groupingBy{}.eachCount()` 是 O(n)，编辑时每次替换都会重算。图纸上限为常见拼豆尺寸（数百格到数千格），且该计算在查看模式已同样存在，可接受。
- [D2 依赖 replaceGrid 产生新实例这一隐式契约] → 以 spec 场景固化"实时刷新"行为；若未来改为原地修改，测试会暴露。
- [信息栏在编辑模式行内元素变多，小屏可能拥挤] → 三个字段（工具/色号/粒数）均为短文本，采用 `Arrangement.spacedBy(16.dp)` 的横向排列，现有宽度下可容纳；不做换行处理，必要时由用户反馈再调整。
