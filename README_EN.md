# CraftGod

A Minecraft building assistant plugin based on BlockDisplay, featuring 3D floating menus, geometry generators, template system, and building code sharing.

> Environment: Paper 1.21+ | Java 21 | Command: `/god`

---

## Core Features

| Feature | Description |
|---------|-------------|
| 3D Floating Menu | Interactive menu using BlockDisplay entities, line-of-sight selection + left-click confirm |
| Geometry Generator | Roman columns, cuboids, ellipsoids with real-time parameter adjustment and material swap |
| Template System | Select existing builds to save as reusable templates |
| Building Code | 6-letter code for cross-world/cross-server building sharing |
| Undo/Redo | Multi-step operation rollback support |
| Auto Build | Consumes materials from inventory, materializes preview block by block |

---

## Project Architecture

```
CraftGod/
├── CraftGod.java              # Plugin entry
├── BlockMenu.java             # 3D menu system
├── FileManager.java           # YAML template persistence
├── RandomCodeGenerator.java   # Building code generator
│
├── Mark/
│   ├── Mark.java              # Relative coordinate block
│   └── MarkCreator.java       # Region capture
│
├── Listeners/
│   ├── MenuHandler.java       # Menu trigger
│   ├── BuildingCreate.java    # Preview placement
│   └── ReflectListener.java   # Building code input
│
├── Menus/
│   ├── menuCreate.java        # Create menu
│   ├── menuPrint.java         # Build mode
│   ├── menuReflect.java       # Building code loading
│   └── createMenus/
│       ├── circleSpawner.java      # Ellipsoid
│       ├── linerSpawner.java       # Cuboid
│       ├── romanColumnSpawner.java # Roman column
│       └── menuController.java     # Preview controller
│
└── CommandManager/
    ├── RedoAndUndoCommand.java # Command handler
    └── TabManager.java         # Tab completion
```

---

## Data Structure Design

### 1. Mark (Relative Coordinate Block)

```java
public class Mark {
    private final int dx, dy, dz;  // Offset from origin
    private final BlockData data;   // Block data
}
```

Design considerations:
- Relative coordinates instead of absolute, enabling template reuse at any location
- `BlockData` preserves complete block state (orientation, waterlogged, etc.), not just `Material`
- Immutable object, thread-safe

### 2. Player State Management

```java
// BuildingCreate.java
static ConcurrentHashMap<String, List<Mark>> BuildingTemplate;    // Current template
static ConcurrentHashMap<String, List<Mark>> BuildingMark;        // All placed Marks
static ConcurrentHashMap<String, int[]> BuildingStartPoint;       // Building origin
static ConcurrentHashMap<String, List<BlockDisplay>> PreviewTemplate; // Preview entities
static ConcurrentHashMap<String, List<Integer>> PlayerStep;       // Mark count per step
```

Why `ConcurrentHashMap<String, ...>` instead of `HashMap<Player, ...>`:
- Player objects are garbage collected after logout, using as Key causes memory leaks or NPE
- String (player name) is immutable, suitable as Key
- `ConcurrentHashMap` supports concurrent access from async menu operations

### 3. Undo/Redo Stack

```java
// Data flow
PlayerStep: [5, 12, 8]  // Step 1: 5 Marks, Step 2: 12, Step 3: 8

// undo operation
1. step = PlayerStep.removeLast()  // Get 8
2. Remove last 8 BlockDisplays from PreviewTemplate
3. Remove last 8 Marks from BuildingMark, store in Redo
4. Time complexity: O(step)

// redo operation
1. Retrieve Mark list from Redo
2. Regenerate BlockDisplays and append to PreviewTemplate
3. Time complexity: O(step)
```

Design decisions:
- Use `List<Integer>` to record step counts, not separate snapshots per step
- Pros: Low memory footprint, undo/redo only moves pointers
- Cons: Only linear undo supported (fits building workflow)

### 4. Building Code Generation

```java
public class RandomCodeGenerator {
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Set<String> existingCodes;  // Existing codes
    
    public String generateUniqueCode(int maxRetries) {
        existingCodes = loadAllMarks();  // Load from filesystem
        while (retries < maxRetries) {
            String code = generateRandomCode();  // 6 random letters
            if (!existingCodes.contains(code)) { // O(1) lookup
                return code;
            }
            retries++;
        }
        return "ERROR";
    }
}
```

Collision probability analysis:
- Code space: 26^6 = 308,915,776 (~300 million)
- With 1000 existing codes, collision probability ≈ 0.0003%
- Probability of collision after 20 retries: (1000/308915776)^20 ≈ 0

---

## Core Algorithms

### 1. Line-of-Sight Selection (Menu Interaction)

```java
public static BlockDisplay getTarget(Player pl, List<BlockDisplay> flags) {
    Vector eyePos = pl.getEyeLocation().toVector();
    Vector dir = pl.getEyeLocation().getDirection();
    
    BlockDisplay target = null;
    double minAngle = Double.MAX_VALUE;
    double maxAngleRadians = Math.toRadians(25);  // 25° tolerance
    
    for (BlockDisplay flag : flags) {
        Vector toFlag = flag.getLocation().toVector().subtract(eyePos);
        double distance = toFlag.length();
        if (distance > 4.0) continue;  // 4 block distance limit
        
        double angle = Math.acos(dir.dot(toFlag.normalize()));
        if (angle <= maxAngleRadians && angle < minAngle) {
            minAngle = angle;
            target = flag;
        }
    }
    return target;
}
```

Algorithm explanation:
- Uses vector dot product to calculate angle between line-of-sight and target
- Selects target with smallest angle within 25° tolerance
- Time complexity: O(n), n = menu option count (typically 3-7)

### 2. Ellipsoid Generation Algorithm

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
                
                // Ellipsoid equation: (x/a)² + (y/b)² + (z/c)² ≤ 1
                if (dx*dx + dy*dy + dz*dz <= 1.0f) {
                    marks.add(new Mark(x, y, z, data));
                }
            }
        }
    }
    return marks;
}
```

### 3. Roman Column Algorithm (Superellipse Cross-section)

```java
// Column body uses superellipse equation, n=4 approximates rounded rectangle
double n = 4.0;
double value = Math.pow(Math.abs(nx), n) + Math.pow(Math.abs(nz), n);
if (value <= 1.0) {
    pillar.add(new Mark(x, y, z, material));
}
```

Superellipse properties:
- n=2: Standard ellipse
- n=4: Rounded rectangle (Roman column cross-section)
- n→∞: Rectangle

---

## Performance Design

### Event-Driven vs Polling

| Scenario | This Project | Alternative | Performance Comparison |
|----------|--------------|-------------|------------------------|
| Menu trigger | `PlayerToggleSneakEvent` | Scan all players every tick | O(1) vs O(n) |
| Placement detection | `BlockPlaceEvent` filter | Check player held item every tick | Event-driven, no polling overhead |
| Building code input | Dynamic listener registration, unregister on timeout | Global chat listener | On-demand registration, reduces unnecessary calls |

### Async Processing Model

```java
// BlockMenu.java
public static CompletableFuture<Integer> menuInit(Player p, List<BlockDisplay> choices) {
    CompletableFuture<Integer> futureIndex = new CompletableFuture<>();
    
    new BukkitRunnable() {
        @Override
        public void run() {
            // Async poll player line-of-sight
            BlockDisplay bl = getTarget(p, choices);
            
            if (p.hasMetadata("click") && bl != null) {
                futureIndex.complete(index);  // Complete Future
                cancel();
            }
        }
    }.runTaskTimerAsynchronously(get(), 8, 2);  // Check every 2 ticks
    
    return futureIndex;
}

// Caller
menuInit(pl, choices).thenAccept(index -> {
    // Main thread callback handles selection result
});
```

Advantages:
- Menu waiting doesn't block main thread
- `CompletableFuture` chain calls, clean code
- Async polling interval 2 ticks (100ms), balances responsiveness and performance

### Entity Management

```java
// CraftGod.java
public static List<BlockDisplay> displays = new ArrayList<>();  // Global entity tracking

@Override
public void onDisable() {
    displays.forEach(BlockDisplay::remove);  // Cleanup on plugin disable
}
```

Preventing entity leaks:
- All spawned BlockDisplays added to global list
- Unified cleanup on plugin disable
- Menu close triggers gradual fadeout removal

---

## Usage

### Basic Operations

1. Hold lightning rod, press and release sneak key to open menu
2. Look at option, left-click to select; right-click to change material
3. After selecting geometry/template, place lightning rod to set position
4. Select "Build" to start materializing from inventory

### Commands

| Command | Function |
|---------|----------|
| `/god undo` | Undo last preview step |
| `/god redo` | Redo undone step |
| `/god finish` | Generate building code and cleanup |
| `/god cancel` | Cancel current build |

---

## Data Persistence

```
plugins/CraftGod/
├── templates/    # Local templates
└── marks/        # Building code files
```

YAML structure:
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

## Tech Stack

- Paper API 1.21+ (BlockDisplay, Transformation API)
- fastutil (High-performance Pair)
- CompletableFuture (Async programming)
- ConcurrentHashMap (Concurrent state management)

---

[User Guide (Chinese)](USAGE.md)
