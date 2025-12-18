# CraftGod（创世神 |  [[用户使用文档]](https://github.com/superwfox/CraftGod/blob/master/USAGE.md)  |  [[English Documention]](https://github.com/superwfox/CraftGod/blob/master/README_EN.md)

基于 BlockDisplay 的 Minecraft 建筑辅助插件，提供 3D 悬浮菜单、几何体生成、模板系统、建筑码分享等功能。

> 环境：Paper 1.21+ | Java 21 | 命令：`/god`

---

## 核心特性

| 功能 | 说明 |
|------|------|
| 3D 悬浮菜单 | 基于 BlockDisplay 实体的交互式菜单，视线选择 + 左键确认 |
| 几何体生成 | 罗马柱、矩形、椭球体，支持实时调参和材质替换 |
| 模板系统 | 框选现有建筑保存为模板，可复用 |
| 建筑码 | 6 位字母码，跨世界/跨服分享建筑 |
| 撤销/重做 | 支持多步操作回退 |
| 自动建造 | 从背包扣材料，逐块实体化预览 |

---

## 项目架构

```
CraftGod/
├── CraftGod.java              # 插件入口
├── BlockMenu.java             # 3D 菜单系统
├── FileManager.java           # YAML 模板持久化
├── RandomCodeGenerator.java   # 建筑码生成器
│
├── Mark/
│   ├── Mark.java              # 相对坐标方块
│   └── MarkCreator.java       # 区域截取
│
├── Listeners/
│   ├── MenuHandler.java       # 菜单触发
│   ├── BuildingCreate.java    # 预览放置
│   └── ReflectListener.java   # 建筑码输入
│
├── Menus/
│   ├── menuCreate.java        # 创造菜单
│   ├── menuPrint.java         # 建造模式
│   ├── menuReflect.java       # 建筑码加载
│   └── createMenus/
│       ├── circleSpawner.java      # 椭球体
│       ├── linerSpawner.java       # 长方体
│       ├── romanColumnSpawner.java # 罗马柱
│       └── menuController.java     # 预览控制
│
└── CommandManager/
    ├── RedoAndUndoCommand.java # 命令处理
    └── TabManager.java         # Tab 补全
```

---

## 数据结构设计

### 1. Mark（相对坐标方块）

```java
public class Mark {
    private final int dx, dy, dz;  // 相对于起点的偏移
    private final BlockData data;   // 方块数据
}
```

设计考量：
- 使用相对坐标而非绝对坐标，使模板可在任意位置复用
- `BlockData` 保留完整方块状态（朝向、含水等），而非仅 `Material`
- 不可变对象，线程安全

### 2. 玩家状态管理

```java
// BuildingCreate.java
static ConcurrentHashMap<String, List<Mark>> BuildingTemplate;    // 当前模板
static ConcurrentHashMap<String, List<Mark>> BuildingMark;        // 已放置的所有 Mark
static ConcurrentHashMap<String, int[]> BuildingStartPoint;       // 建筑起点
static ConcurrentHashMap<String, List<BlockDisplay>> PreviewTemplate; // 预览实体
static ConcurrentHashMap<String, List<Integer>> PlayerStep;       // 每步 Mark 数量
```

为什么用 `ConcurrentHashMap<String, ...>` 而非 `HashMap<Player, ...>`：
- Player 对象在玩家下线后会被回收，用作 Key 会导致内存泄漏或 NPE
- String（玩家名）是暂时不可变的，适合作为 Key
- `ConcurrentHashMap` 支持异步菜单操作的并发访问

### 3. 撤销/重做栈

```java
// 数据流
PlayerStep: [5, 12, 8]  // 第1步放了5个Mark，第2步12个，第3步8个

// undo 操作
1. step = PlayerStep.removeLast()  // 取出 8
2. 从 PreviewTemplate 尾部移除 8 个 BlockDisplay
3. 从 BuildingMark 尾部移除 8 个 Mark，存入 Redo
4. 时间复杂度：O(step)

// redo 操作
1. 从 Redo 取出 Mark 列表
2. 重新生成 BlockDisplay 并追加到 PreviewTemplate
3. 时间复杂度：O(step)
```

设计决策：
- 使用 `List<Integer>` 记录每步数量，而非为每步创建独立快照
- 优点：内存占用低，undo/redo 只需移动指针
- 缺点：只支持线性撤销（符合建筑场景需求）

### 4. 建筑码生成

```java
public class RandomCodeGenerator {
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Set<String> existingCodes;  // 已存在的码
    
    public String generateUniqueCode(int maxRetries) {
        existingCodes = loadAllMarks();  // 从文件系统加载
        while (retries < maxRetries) {
            String code = generateRandomCode();  // 6位随机字母
            if (!existingCodes.contains(code)) { // O(1) 查重
                return code;
            }
            retries++;
        }
        return "ERROR";
    }
}
```

碰撞概率分析：
- 码空间：26^6 = 308,915,776（约 3 亿）
- 假设已有 1000 个码，碰撞概率 ≈ 0.0003%
- 20 次重试后仍碰撞概率：(1000/308915776)^20 ≈ 0

---

## 核心算法

### 1. 视线选择算法（菜单交互）

```java
public static BlockDisplay getTarget(Player pl, List<BlockDisplay> flags) {
    Vector eyePos = pl.getEyeLocation().toVector();
    Vector dir = pl.getEyeLocation().getDirection();
    
    BlockDisplay target = null;
    double minAngle = Double.MAX_VALUE;
    double maxAngleRadians = Math.toRadians(25);  // 25° 容差
    
    for (BlockDisplay flag : flags) {
        Vector toFlag = flag.getLocation().toVector().subtract(eyePos);
        double distance = toFlag.length();
        if (distance > 4.0) continue;  // 4格距离限制
        
        double angle = Math.acos(dir.dot(toFlag.normalize()));
        if (angle <= maxAngleRadians && angle < minAngle) {
            minAngle = angle;
            target = flag;
        }
    }
    return target;
}
```

算法说明：
- 使用向量点积计算视线与目标夹角
- 选择夹角最小且在 25° 容差内的目标
- 时间复杂度：O(n)，n 为菜单选项数（通常 3-7 个）

### 2. 椭球体生成算法

```java
public static List<Mark> createSphere(int height, int length, int width, BlockData data) {
    List<Mark> marks = new ArrayList<>();
    float rx = (length + 1) / 2.0f;
    float ry = (height + 1) / 2.0f;
    float rz = (width + 1) / 2.0f;
    
    for (int y = 0; y < height; y++) {
        for (int x = -hx; x <= hx; x++) {
            for (int z = -hz; z <= hz; z++) {
                float dx = (x + 0.5f) / rx;
                float dy = (y + 0.5f) / ry;
                float dz = (z + 0.5f) / rz;
                
                // 椭球方程：(x/a)² + (y/b)² + (z/c)² ≤ 1
                if (dx*dx + dy*dy + dz*dz <= 1.0f) {
                    marks.add(new Mark(x, y, z, data));
                }
            }
        }
    }
    return marks;
}
```

### 3. 罗马柱生成算法（超椭圆截面）

```java
// 柱身使用超椭圆方程，n=4 时接近圆角矩形
double n = 4.0;
double value = Math.pow(Math.abs(nx), n) + Math.pow(Math.abs(nz), n);
if (value <= 1.0) {
    pillar.add(new Mark(x, y, z, material));
}
```

超椭圆特性：
- n=2：标准椭圆
- n=4：圆角矩形（罗马柱截面）
- n→∞：矩形

---

## 性能设计

### 事件驱动 vs 定时轮询

| 场景 | 本项目方案 | 替代方案 | 性能对比 |
|------|-----------|----------|----------|
| 菜单触发 | `PlayerToggleSneakEvent` | 每 tick 扫描所有玩家 | O(1) vs O(n) |
| 放置检测 | `BlockPlaceEvent` 过滤 | 每 tick 检查玩家手持物品 | 事件驱动，无轮询开销 |
| 建筑码输入 | 动态注册监听器，超时注销 | 全局监听所有聊天 | 按需注册，减少无效调用 |

### 异步处理模型

```java
// BlockMenu.java
public static CompletableFuture<Integer> menuInit(Player p, List<BlockDisplay> choices) {
    CompletableFuture<Integer> futureIndex = new CompletableFuture<>();
    
    new BukkitRunnable() {
        @Override
        public void run() {
            // 异步轮询玩家视线
            BlockDisplay bl = getTarget(p, choices);
            
            if (p.hasMetadata("click") && bl != null) {
                futureIndex.complete(index);  // 完成 Future
                cancel();
            }
        }
    }.runTaskTimerAsynchronously(get(), 8, 2);  // 每 2 tick 检查
    
    return futureIndex;
}

// 调用方
menuInit(pl, choices).thenAccept(index -> {
    // 主线程回调处理选择结果
});
```

优势：
- 菜单等待不阻塞主线程
- `CompletableFuture` 链式调用，代码清晰
- 异步轮询间隔 2 tick（100ms），平衡响应速度与性能

### 实体管理

```java
// CraftGod.java
public static List<BlockDisplay> displays = new ArrayList<>();  // 全局实体追踪

@Override
public void onDisable() {
    displays.forEach(BlockDisplay::remove);  // 插件卸载时清理
}
```

防止实体泄漏：
- 所有生成的 BlockDisplay 加入全局列表
- 插件禁用时统一清理
- 菜单关闭时逐个淡出移除

---

## 使用流程

### 基本操作

1. 手持避雷针，按下并松开潜行键打开菜单
2. 视线对准选项，左键选择；右键可更换材质
3. 选择几何体/模板后，用避雷针放置确定位置
4. 选择"建造"开始从背包扣材料实体化

### 命令

| 命令 | 功能 |
|------|------|
| `/god undo` | 撤销最后一步预览 |
| `/god redo` | 重做撤销的步骤 |
| `/god finish` | 生成建筑码并清理 |
| `/god cancel` | 取消当前建筑 |

---

## 数据持久化

```
plugins/CraftGod/
├── templates/    # 本地模板
└── marks/        # 建筑码文件
```

YAML 结构：
```yaml
start:
  x: 100
  y: 64
  z: 200
marks:
  0:
    dx: 0
    dy: 0
    dz: 0
    data: "minecraft:quartz_pillar[axis=y]"
  1:
    dx: 1
    dy: 0
    dz: 0
    data: "minecraft:stone"
```

---

## 技术栈

- Paper API 1.21+（BlockDisplay、Transformation API）
- fastutil（高性能 Pair）
- CompletableFuture（异步编程）
- ConcurrentHashMap（并发状态管理）

---
