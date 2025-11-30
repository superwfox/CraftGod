# CraftGod（创世神） | [用户使用文档](https://github.com/superwfox/CraftGod/blob/master/USAGE.md)

一个基于 **BlockDisplay 预览 + 建筑码** 的建筑工具插件。  
提供 3D 悬浮菜单、模板生成、几何体快速建模、撤销/重做、建筑码分享等功能。

> 插件主要面向会用指令/菜单搭建建筑的玩家或建筑服。

---

## 环境要求

- 服务端：Spigot / Paper 等 Bukkit 系列  
- 版本：**1.19+（必须支持 BlockDisplay 与 Transformation API，推荐 1.20+）**
- 插件名：`CraftGod`
- 主类：`sudark2.Sudark.craftGod.CraftGod`
- 主要命令：`/god`

---

## 基本概念

- **模板（templates）**  
  存在于 `plugins/CraftGod/templates/`  
  - 来自“创建模板”（用避雷针框选两点截取建筑）  
  - 在“所有模板”里可以预览并设为当前模板

- **建筑码（marks）**  
  存在于 `plugins/CraftGod/marks/`  
  - `/god finish` 会把当前一次建筑流程保存为一个 **6 位大写字母码**  
  - 通过“从建筑码获取投影”+ 聊天框输入就能跨世界/跨服复用

- **预览（BlockDisplay）**  
  - 所有投影/菜单都是 BlockDisplay 实体  
  - 插件关闭时会统一清除（`CraftGod.displays`）

- **Mark（相对坐标块）**  
  - `Mark` 保存一个块的 **相对坐标(dx, dy, dz)** 和 `BlockData`  
  - `MarkCreator` 负责从两点截取世界方块生成 Mark 列表  
  - 预览时通过 `Mark.markDisplay` 在世界中生成 BlockDisplay

- **一次建筑流程相关的状态（按玩家名分组）**
  - `BuildingTemplate`：当前准备放置的模板  
  - `BuildingMark`：已经“挂在空中”的所有预览块（Mark 列表）  
  - `BuildingStartPoint`：该建筑整体的起点  
  - `PreviewTemplate`：与 `BuildingMark` 对应的 BlockDisplay 列表  
  - `PlayerStep`：每一步添加了多少个 Mark，用于撤销/重做  
  - `Redo`：`/god undo` 时被移除的一步，用于 `/god redo`

---

## 快速上手

### 0. 准备

1. 给玩家一个 **避雷针（Lightning Rod）** 放在主手。
2. 确保玩家有使用 `/god` 与相关交互的权限（自行在 `plugin.yml` 配置）。

所有菜单 / 选择 都是通过 **看着方块 + 左键 / 右键** 来完成。

---

### 1. 打开主菜单

- 手持避雷针
- **按下潜行键（Shift）再松开**  

触发 `PlayerToggleSneakEvent` 后，屏幕前方会出现一个 3D 悬浮菜单（`BlockMenu.spawnMenu`），三项：

1. **创造**（SCULK_SHRIEKER）
2. **建造**（BREWING_STAND）
3. **从建筑码获取投影**（SOUL_CAMPFIRE）

操作方式：

- 用视线对准某个菜单方块
- 在 4 格内 **左键一次空气/方块** 即可选择  
- 右键则被用于特殊操作（比如更换材质，见后文）
- 走远 / 超时 / 右键取消 / 宕机时菜单会缓慢淡出（`menuFadeout`）

---

## 子菜单与功能说明

### A. 创造菜单（`menuCreate`）

从主菜单选择 **“创造”** 后，会出现第二级菜单：

- 罗马柱
- 矩形工具
- 圆形工具
- 所有模板
- 创建模板

#### A1. 罗马柱（`romanColumnSpawner`）

- 功能：生成一个可调高度与半径的类罗马柱体模板
- 菜单选项（`columnOptions`）：
  1. 高度+  
  2. 高度-  
  3. 确认  
  4. 半径-  
  5. 半径+

- 使用流程：
  1. 从“创造”→“罗马柱”
  2. 屏幕前方出现一个柱体预览（`displayTemplate` 根据面朝方向摆放）
  3. 左键不同选项调整高度/半径
  4. **右键任意处**：用当前主手方块作为材质重新生成预览（index = -2）
  5. 选择“确认”：
     - 当前 Mark 列表保存到 `BuildingTemplate[玩家名]`
     - 预览实体销毁，等待玩家用避雷针落点

#### A2. 矩形工具（`linerSpawner`）

- 功能：生成可调 **高度 × 长度 × 宽度** 的实体长方体
- 菜单选项（`lineOptions`）：
  1. 高度+  
  2. 高度-  
  3. 确认  
  4. 宽度+  
  5. 宽度-  
  6. 长度-  
  7. 长度+

其他交互逻辑与罗马柱类似：

- 初始尺寸默认 `2×2×2`
- 右键：以手中方块作为材质重新生成预览
- “确认”：把模板写入 `BuildingTemplate`，进入放置阶段

#### A3. 圆形工具（`circleSpawner`）

- 功能：生成可调的椭球体/圆柱组合（通过 `createSphere`）
- 菜单选项（`circleOptions`）同矩形工具：高度、宽度、长度的 +/-

交互与矩形工具一致：

- 支持用主手方块改变材质
- 预览通过 `displayTemplate` 按视角方向排布

#### A4. 所有模板（本地模板浏览）

- 入口：创造菜单 → “所有模板”（`menuCreate.other`）
- 数据来源：`plugins/CraftGod/templates/` 下所有文件名（`loadAllTemplateNames`）
- 流程：
  1. 插件读取所有模板名（含 `.yml`）
  2. 构建左右切换菜单：  
     - 上一个模板  
     - 当前模板  
     - 下一个模板
  3. 选择“当前模板”：
     - 将该模板的 `List<Mark>` 写入 `BuildingTemplate[玩家名]`
     - 关闭预览

#### A5. 创建模板（从现有建筑框选）

- 入口：创造菜单 → “创建模板”（`menuCreate.create`）
- 流程：
  1. 插件给玩家加上 `metadata("menu")` 并提示：
     - “使用避雷针左键方块确定模板”
  2. 玩家手持避雷针 **左键第一块方块**  
     - 记为点 A，提示“还差一角”
  3. 再左键第二块方块  
     - 调用 `MarkCreator.createMark(A, B)`，截取 A/B 之间非空气方块为模板  
     - 通过 `FileManager.saveTemplate(templateFolder, 玩家名, mark)` 保存到 `templates/`
     - 提示“现在你可以在[所有模板]中使用该模板”
  4. 截取时会把最外层边界缩一格（看源码逻辑中的 `min + 1`），避免把选框本身当成实体边框保存

---

### B. 建造菜单（`menuPrint`）

从主菜单选择 **“建造”**：

- 再次使用时会起到 **开关** 作用：  
  - 若该玩家建造任务正在运行 → 取消任务、提示“建造结束”  
  - 若没有任务 → 启动一个每秒执行一次的建造任务

建造任务逻辑：

1. 每 tick（实际是每秒 / 20 tick）：
   - 显示 ActionBar：`[BUILDING]`
   - 获取玩家附近 15 格内的所有 BlockDisplay
2. 对每个 BlockDisplay：
   - 读取其 `BlockData` 对应的 `Material`
   - 调用 `consumeItem(cost, inv)` 从玩家背包扣一单位方块
   - 扣成功：
     - 播放 `BLOCK_BAMBOO_PLACE` 声音
     - 将该位置的原本方块设置为对应 `BlockData`
     - 移除 BlockDisplay（完成实体化）
   - 扣失败：
     - 提示“材料不足 缺少 X”
     - 停止本次循环

`consumeItem` 支持两种材料来源：

1. **带 PDC 的特殊堆叠物品**  
   - key：`sudark:extra_amount`（`NamespacedKey("sudark", "extra_amount")`）  
   - 使用时优先从这些堆叠中扣减 `amount`，并更新 Lore 显示 `+amount`
2. 普通物品  
   - 找到一个对应材质的物品，直接消耗 1 个

> 说明：`extra_amount` 物品的创建不在本插件内实现，需要你自己发放。

---

### C. 从建筑码获取投影（`menuReflect`）

从主菜单选择 **“从建筑码获取投影”**：

1. 插件清除该玩家的菜单位置 `menuLoc`
2. Title 提示“输入建筑码”，并在聊天里提示：
   - 建筑码可以忽略大小写与空格
   - 一分钟后自动取消
3. 给玩家打上 `metadata("codeInput")`，并注册 `ReflectListener`

在有 `codeInput` 的玩家发送聊天信息时：

1. 拦截 `PlayerChatEvent`，取消发言
2. 将消息：
   - 转为大写
   - 去掉所有空格
   - 拼接 `.yml` 得到文件名
3. 若 `marks/` 文件夹中存在该文件：
   - 调用 `FileManager.loadTemplate(markFolder, name, world)`
   - 将 `mark.right()`（Mark 列表）写入 `BuildingTemplate[玩家名]`
   - Title 提示“已加载，用避雷针放置来确定位置”
   - 注销监听器、移除 `codeInput` 元数据
4. 若玩家一分钟内没有输入有效建筑码：
   - 定时任务检测到仍有 `codeInput` → 认为超时
   - Title 提示“输入建筑码超时”，同时清理监听与元数据

---

## 放置模板与撤销/重做流程

### 1. 放置模板（`BuildingCreate`）

当玩家 **手持模板** 且 **放置避雷针方块** 时：

1. 在 `BlockPlaceEvent` 中检查：
   - 放置方块类型是否为 `Material.LIGHTNING_ROD`
   - `BuildingTemplate` 中是否有该玩家的模板
2. 如果有：
   - 获取目标落点 `putLoc`
   - 调用 `applyMark(玩家名, BuildingTemplate[玩家名], putLoc)`
   - `event.setBuild(false)` 阻止避雷针实际落地

`applyMark` 会：

- 使用 `Mark.markDisplay` 把模板转换为一组 BlockDisplay 作为预览
- 在 `PreviewTemplate[玩家名]` 记录所有预览实体
- 把今回合的 Mark 数量写入 `PlayerStep[玩家名]`
- 通过 `appendMark` 把本次所有 Mark 平移到整个建筑起点坐标系下，附加到 `BuildingMark[玩家名]`

> 玩家可以多次放置避雷针，组成多段建筑步骤。

### 2. `/god undo`（撤销最后一步）

- 命令执行类：`RedoAndUndoCommand`
- 流程：
  1. 从 `PlayerStep[玩家名]` 取出最后一步的 Mark 数量 `step`
  2. 从 `PreviewTemplate` 中删除最后 `step` 个 BlockDisplay 并移除实体
  3. 从 `BuildingMark` 中删掉最后 `step` 个 Mark  
     同时把它们追加到 `Redo[玩家名]` 用于重做
  4. 从 `PlayerStep` 中删除该步记录

### 3. `/god redo`（重做）

- 若 `Redo` 中没有该玩家记录：
  - Title 提示“没有可重做的步骤 你需要/undo”
- 否则：
  1. 取出 `Redo[玩家名]` 的 Mark 列表
  2. 根据 `BuildingStartPoint` 重新计算落点并调用 `Mark.markDisplay`
  3. 把生成的 BlockDisplay 添回 `PreviewTemplate`
  4. 把 Mark 列表追加回 `BuildingMark`
  5. 在 `PlayerStep` 新增一个步长（本次 Mark 数量）
  6. 清空该玩家的 `Redo` 记录

### 4. `/god finish`（生成建筑码）

- 若该玩家没有正在编辑的建筑：
  - Title 提示“没有可创建的建筑”
- 否则：
  1. Title 提示“请稍等 正在保存建筑”
  2. 创建 `RandomCodeGenerator` 并调用 `generateUniqueCode(maxRetries=20)`
     - 内部会读取 `marks/` 文件名集合，保证 6 位字母码不重复
  3. 如果返回 `"ERROR"` → 提示失败  
  4. 否则：
     - 取 `BuildingMark` 中第一个 Mark 的绝对坐标作为起点  
       （构造 `Location putLoc`）
     - 通过 `FileManager.saveTemplate(markFolder, code, Pair(putLoc, BuildingMark))`
       保存到 `marks/<CODE>.yml`
     - 把所有预览 BlockDisplay 下移一段再淡出删除
     - 40 tick 后弹出 Title + 聊天提示：
       - 建筑码格式类似 `ABC DEF`（中间自动加空格便于记忆）
     - 清空该玩家的 `PreviewTemplate`、`PlayerStep`、`BuildingMark`、`BuildingStartPoint`

### 5. `/god cancel`（取消建筑）

虽然 Tab 补全只给出 `undo / redo / finish`，源码中还支持：

- `/god cancel`：  
  - 若没有在建建筑 → 提示“没有可取消的建筑”  
  - 否则：
    - 删除所有当前预览 BlockDisplay
    - 清空 `PreviewTemplate`、`PlayerStep`、`BuildingMark`、`BuildingStartPoint`

---

## 数据与文件结构

- 插件数据目录：`plugins/CraftGod/`
  - `templates/`：玩家本地模板（通过“创建模板”保存）
  - `marks/`：通过 `/god finish` 导出的建筑码文件  

文件格式为 Yaml，结构大致为：

```yaml
start:
  x: <int>
  y: <int>
  z: <int>

marks:
  0:
    dx: <int>
    dy: <int>
    dz: <int>
    data: "<BlockData asString>"
  1:
    ...
