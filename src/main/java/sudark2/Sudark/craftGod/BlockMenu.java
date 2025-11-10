package sudark2.Sudark.craftGod;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import sudark2.Sudark.craftGod.menus.*;

import java.util.ArrayList;
import java.util.List;

import static sudark2.Sudark.craftGod.CraftGod.displays;
import static sudark2.Sudark.craftGod.CraftGod.get;

public class BlockMenu {
    // 1 创造 2 打印他人的投影 3 建造 4 从建筑码获取投影 5 显示距离调整

    public static void menuInit(Player p) {
        List<BlockDisplay> choices = spawnMenu(
                List.of(
                        nameItem(Material.SCULK_SHRIEKER, "创造"),
                        nameItem(Material.BEDROCK, "打印他人的投影"),
                        nameItem(Material.AMETHYST_CLUSTER, "建造"),
                        nameItem(Material.BEACON, "从建筑码获取投影")
                ),
                p,
                1
        );

        p.removeMetadata("sneak", get());

        new BukkitRunnable() {
            Location centerLoc = p.getLocation();

            @Override
            public void run() {

                if (!p.isOnline() || p.getLocation().distanceSquared(centerLoc) > 100) {
                    p.removeMetadata("sneak", get());
                    menuFadeout(choices);
                    cancel();
                    return;
                }

                BlockDisplay bl = getTargetByAngle(p, choices);

                for (BlockDisplay display : choices) {
                    if (bl == display) {
                        if (display.getTransformation().equals(normal)) enlarge(display);
                    } else {
                        if (display.getTransformation().equals(huge)) reduce(display);
                    }

                    rotate(display);
                }

                if (!p.hasMetadata("sneak")) return;

                if (bl != null) switch (bl.getBlock().getMaterial()) {
                    case QUARTZ_STAIRS -> menuCreate.menu(p);
                    case BEDROCK -> menuPrint.menu(p);
                    case GRAY_BED -> menuBuild.menu(p);
                    case BEACON -> menuReflect.menu(p);
                }
                p.removeMetadata("sneak", get());
                menuFadeout(choices);
                cancel();

            }
        }.runTaskTimerAsynchronously(get(), 7, 2);
    }

    public static void menuFadeout(List<BlockDisplay> choices) {
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
                choices.remove(display);
            }
        }.runTaskTimer(get(), 0, 2);
    }

    static float p = 5.5f * 0.125f;
    public static Transformation normal = new Transformation(
            new Vector3f(-p / 2f, -0.5f, -p / 2f),
            new Quaternionf(), // 旋转（rotation），默认不旋转
            new Vector3f(p, p, p), // 缩放（scale）到 6/8
            new Quaternionf()
    );

    static float lp = 7 * 0.125f;
    public static Transformation huge = new Transformation(
            new Vector3f(-lp / 2f, -0.5625f, -lp / 2f),
            new Quaternionf(), // 旋转（rotation），默认不旋转
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

    public static BlockDisplay getTargetByAngle(Player pl, List<BlockDisplay> flags) {
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

    public static List<BlockDisplay> spawnMenu(List<ItemStack> blocks, Player pl, int mode) {
        Location plLoc = pl.getLocation();
        World world = plLoc.getWorld();
        Vector forward = plLoc.getDirection().setY(0); // 玩家朝向
        Vector right = new Vector(-forward.getZ(), 0, forward.getX()); // 玩家右侧方向
        Vector up = new Vector(0, 1, 0); // 玩家上方方向
        plLoc.setPitch(0);
        int size = blocks.size();
        int spacing = 1;

        List<BlockDisplay> blockDisplays = new ArrayList<>();
        if (mode == 1) {
            for (int i = 0; i < size; i++) {
                int parallel = i % 2 == 0 ? 1 : -1;
                int vertical = i / 2;
                Vector offset = forward.clone().multiply(2.25)
                        .add(up.clone().multiply(vertical * 1.5))
                        .add(right.clone().multiply(parallel * spacing));

                Location loc = plLoc.clone().add(offset).add(0, -1.5f, 0);
                BlockDisplay flag = world.spawn(loc, BlockDisplay.class);

                flag.setBlock(blocks.get(i).getType().createBlockData());

                flag.setCustomName("[§e" + blocks.get(i).getItemMeta().getDisplayName() + "§f]");

                flag.setTransformation(normal);
                flag.setInterpolationDelay(0);
                flag.setInterpolationDuration(5);
                flag.setTeleportDuration(5);
                Bukkit.getScheduler().runTaskLater(get(), () ->
                                flag.teleport(flag.getLocation().add(0, 2, 0))
                        , 2);

                blockDisplays.add(flag);
                displays.add(flag);
            }
            return blockDisplays;
        }

        for (int i = 0; i < size; i++) {
            ItemStack item = blocks.get(i);
            String name = item.getItemMeta().getDisplayName();
            int parallel = (i + 1) / 2 * (i % 2 == 0 ? 1 : -1);
            double offset = parallel * spacing;
            double offsetF = -0.125 * Math.abs(parallel);
            Vector offsetVec = forward.clone().multiply(2.5)
                    .add(forward.clone().multiply(offsetF))
                    .add(right.clone().multiply(offset));

            Location loc = plLoc.clone().add(offsetVec).add(0, -1.5f, 0);
            BlockDisplay flag = world.spawn(loc, BlockDisplay.class);
            flag.setTeleportDuration(6);
            Bukkit.getScheduler().runTaskLater(get(), () -> flag.teleport(loc.clone().add(0, 2f, 0)), 1);

            flag.setBlock(item.getType().createBlockData());

            flag.setCustomName("[" + name + "]");

            flag.setTransformation(normal);

            blockDisplays.add(flag);
            displays.add(flag);
        }
        return blockDisplays;

    }

    public static ItemStack nameItem(Material m, String name) {
        ItemStack item = new ItemStack(m, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
