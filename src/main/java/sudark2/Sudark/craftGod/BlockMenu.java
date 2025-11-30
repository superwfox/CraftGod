package sudark2.Sudark.craftGod;

import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.craftGod.CraftGod.displays;
import static sudark2.Sudark.craftGod.CraftGod.get;

public class BlockMenu {
    // 1 创造 2 打印他人的投影 3 建造 4 从建筑码获取投影 5 显示距离调整

    public static final NamespacedKey MENU_INDEX_KEY = new NamespacedKey(get(), "menu_index");

    public static ConcurrentHashMap<String, Location> menuLoc = new ConcurrentHashMap<>();

    public static ItemStack nameItem(Material m, String name) {
        ItemStack item = new ItemStack(m, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public static CompletableFuture<Integer> menuInit(Player p, List<BlockDisplay> choices) {
        CompletableFuture<Integer> futureIndex = new CompletableFuture<>();
        String plName = p.getName();
        Location centerLoc = menuLoc.get(plName);

        new BukkitRunnable() {
            final int distanceSquared = 48 * 48;
            int time = 0;

            @Override
            public void run() {

                time++;

                if (time > 300 * 10) {
                    if (!futureIndex.isDone()) {
                        futureIndex.complete(-1);
                        menuLoc.remove(p.getName());
                    }
                    title(p, "[§e弃选]", "时间超过5分钟-自动为您弃选");
                    menuFadeout(choices, p);
                    cancel();
                    return;
                }

                if (!p.isOnline() || p.getLocation().distanceSquared(centerLoc) > distanceSquared) {
                    if (!futureIndex.isDone()) {
                        futureIndex.complete(-1);
                        menuLoc.remove(p.getName());
                    }
                    menuFadeout(choices, p);
                    cancel();
                    return;
                }

                BlockDisplay bl = getTarget(p, choices);

                for (BlockDisplay display : choices) {
                    if (bl == display) {
                        if (display.getTransformation().equals(normal)) enlarge(display);
                    } else {
                        if (display.getTransformation().equals(huge)) reduce(display);
                    }
                    rotate(display);
                }

                if (bl != null) bl.getWorld().spawnParticle(
                        Particle.DUST_COLOR_TRANSITION,
                        bl.getLocation(),
                        1, // 粒子的数量
                        new Particle.DustTransition(Color.YELLOW, Color.ORANGE, 1.5f)
                );

                if(p.hasMetadata("touch")) {
                    p.removeMetadata("touch", get());
                    if (!futureIndex.isDone()) {
                        futureIndex.complete(-2);
                        menuLoc.remove(p.getName());
                    }
                    menuFadeout(choices, p);
                    cancel();
                    return;
                }

                if (!p.hasMetadata("click")) return;

                if (bl != null) {
                    Integer index = bl.getPersistentDataContainer().get(MENU_INDEX_KEY, PersistentDataType.INTEGER);

                    if (index != null && !futureIndex.isDone()) {
                        p.playSound(bl.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        futureIndex.complete(index);
                    }
                    menuFadeout(choices, p);
                    cancel();
                    return;
                }

                if (!futureIndex.isDone()) {
                    futureIndex.complete(-1);
                    menuLoc.remove(p.getName());
                }
                menuFadeout(choices, p);
                cancel();
            }
        }.runTaskTimerAsynchronously(get(), 8, 2);

        return futureIndex;
    }

    static float p = 5.5f * 0.125f;
    public static Transformation normal = new Transformation(
            new Vector3f(-p / 2f, -0.5f, -p / 2f),
            new Quaternionf(),
            new Vector3f(p, p, p),
            new Quaternionf()
    );

    static float lp = 6 * 0.125f;
    public static Transformation huge = new Transformation(
            new Vector3f(-lp / 2, -0.5625f, -lp / 2),
            new Quaternionf(),
            new Vector3f(lp, lp, lp),
            new Quaternionf()
    );

    public static void enlarge(BlockDisplay bl) {
        bl.setInterpolationDelay(0);
        bl.setInterpolationDuration(5);
        bl.setTransformation(huge);
        bl.setCustomNameVisible(true);
        bl.setGlowing(true);
    }

    public static void reduce(BlockDisplay bl) {
        bl.setInterpolationDelay(0);
        bl.setInterpolationDuration(5);
        bl.setTransformation(normal);
        bl.setCustomNameVisible(false);
        bl.setGlowing(false);
    }

    static void rotate(BlockDisplay bd) {
        Location loc = bd.getLocation();
        loc.setYaw(loc.getYaw() + 1);
        Bukkit.getScheduler().runTask(get(), () -> bd.teleport(loc));
    }


    public static BlockDisplay getTarget(Player pl, List<BlockDisplay> flags) {
        Vector eyePos = pl.getEyeLocation().toVector();
        Vector dir = pl.getEyeLocation().getDirection();

        BlockDisplay target = null;
        double minAngle = Double.MAX_VALUE;
        double maxDistance = 4.0; // 最远检测距离，5格以内

        double maxAngleRadians = Math.toRadians(25);

        for (BlockDisplay flag : flags) {
            Vector toFlag = flag.getLocation().toVector().subtract(eyePos);
            double distance = toFlag.length();

            if (distance > maxDistance) continue;

            Vector toFlagNorm = toFlag.clone().normalize();

            double angle = Math.acos(dir.dot(toFlagNorm)); // 弧度

            if (angle <= maxAngleRadians && angle < minAngle) {
                minAngle = angle;
                target = flag;
            }
        }
        return target;
    }


    public static List<BlockDisplay> spawnMenu(List<ItemStack> blocks, Player pl) {
        String plName = pl.getName();
        menuLoc.putIfAbsent(plName, pl.getLocation());
        Location plLoc = menuLoc.get(plName);

        World world = plLoc.getWorld();
        Vector forward = plLoc.getDirection().setY(0); // 玩家朝向
        Vector right = new Vector(-forward.getZ(), 0, forward.getX()); // 玩家右侧方向
        plLoc.setPitch(0);
        int size = blocks.size();
        float spacing = 1.25f;

        List<BlockDisplay> blockDisplays = new ArrayList<>();
        double center = (size - 1) / 2.0;

        for (int i = 0; i < size; i++) {
            ItemStack item = blocks.get(i);
            String name = item.getItemMeta().getDisplayName();

            double offset = (i - center) * spacing;

            Vector offsetVec = forward.clone().multiply(2.5)
                    .add(right.clone().multiply(offset));

            Location loc = plLoc.clone().add(offsetVec).add(0, -1.5, 0);

            final int index = i;
            Bukkit.getScheduler().runTask(get(), () -> {
                BlockDisplay flag = world.spawn(loc, BlockDisplay.class);

                flag.setBlock(item.getType().createBlockData());

                flag.setCustomName("[§e" + name + "§f]");

                flag.setTransformation(normal);
                flag.setInterpolationDelay(0);
                flag.setInterpolationDuration(5);
                flag.setTeleportDuration(5);
                Bukkit.getScheduler().runTaskLater(get(), () ->
                                flag.teleport(flag.getLocation().add(0, 2.75, 0))
                        , 2);

                flag.getPersistentDataContainer().set(
                        MENU_INDEX_KEY, PersistentDataType.INTEGER, index
                );

                pl.playSound(pl, Sound.BLOCK_BEEHIVE_ENTER, 1, 1);
                blockDisplays.add(flag);
                displays.add(flag);
            });

        }
        return blockDisplays;

    }


    public static void menuFadeout(List<BlockDisplay> choices, Player pl) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (choices.isEmpty()) {
                    cancel();
                    return;
                }

                BlockDisplay display = choices.getFirst();
                display.setGlowing(false);
                display.setCustomNameVisible(false);
                display.setTeleportDuration(5);
                display.teleport(display.getLocation().add(0, -3, 0));
                Bukkit.getScheduler().runTaskLater(get(), () -> display.remove(), 5);
                pl.playSound(pl, Sound.BLOCK_BEEHIVE_ENTER, 1, 1);
                choices.remove(display);
            }
        }.runTaskTimer(get(), 0, 2);
    }

    public static void title(Player pl, String t1, String t2) {
        new BukkitRunnable() {
            StringBuilder temt = new StringBuilder("§7_");
            int i = 0;

            @Override
            public void run() {
                temt.append(t2.toCharArray()[i]);
                pl.sendTitle(t1, temt + "§7_", 0, 50, 20);
                i++;
                if (i == t2.length()) {
                    cancel();
                }
            }
        }.runTaskTimer(get(), 0, 2L);
    }

}
