package sudark2.Sudark.craftGod;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static sudark2.Sudark.craftGod.CraftGod.displays;
import static sudark2.Sudark.craftGod.CraftGod.get;

public class BlockMenu {
    // 1 创造 2 打印他人的投影 3 建造 4 从建筑码获取投影

    public static void menuInit(Player p) {
        List<BlockDisplay> choices = spawnMenu(
                List.of(
                        nameItem(Material.)
                ),
                p,
                1
        );
        new BukkitRunnable() {
            @Override
            public void run() {




            }
        }.runTaskTimerAsynchronously(get(), 0, 2);

    }

    public static void menuCreate() {
        new BukkitRunnable() {
            List<Mark> marks = new ArrayList<>();

            @Override
            public void run() {

            }
        }.runTaskTimerAsynchronously(get(), 0, 10);
    }

    public static void printMenu() {
    }

    public static void reflect() {
    }

    public static void build() {
    }

    static float p = 0.125f;
    static Transformation normal = new Transformation(
            new Vector3f(0, -0.2f, 0),            // 平移（translation）向量
            new Quaternionf().rotateY(-1 * (float) Math.PI),  // 旋转（rotation），默认不旋转
            new Vector3f(6 * p, 6 * p, 6 * p), // 缩放（scale）到 6/8
            new Quaternionf()                // 旋转中心点（leftRotation），默认不旋转
    );

    static Transformation huge = new Transformation(
            new Vector3f(0, -0.1f, -p),            // 平移（translation）向量
            new Quaternionf(),  // 旋转（rotation），默认不旋转
            new Vector3f(1.1f, 1.1f, 1.1f), // 缩放（scale）到 1
            new Quaternionf()                // 旋转中心点（leftRotation），默认不旋转
    );

    static List<BlockDisplay> spawnMenu(List<ItemStack> blocks, Player pl, int mode) {
        Location plLoc = pl.getLocation();
        World world = plLoc.getWorld();
        Vector forward = pl.getLocation().getDirection().normalize();
        Vector right = new Vector(-forward.getZ(), 0, forward.getX()); // 玩家右侧方向
        Vector up = new Vector(0, 1, 0); // 玩家上方方向
        int size = blocks.size();
        int spacing = 2;

        List<BlockDisplay> blockDisplays = new ArrayList<>();
        if (mode == 1) {
            for (int i = 0; i < size; i++) {
                ItemStack item = blocks.get(i);
                int parallel = i % 2 == 0 ? 1 : -1;
                int vertical = i / 2;
                forward.clone().multiply(2.5)
                        .add(up.multiply(vertical * 0.5))
                        .add(right.clone().multiply(parallel * spacing));
                BlockDisplay flag = world.spawn(plLoc.clone().add(forward).add(0, 0.5f, 0), BlockDisplay.class);

                flag.setBlock(blocks.get(i).getType().createBlockData());

                flag.setCustomName("[" + blocks.get(i).getItemMeta().getDisplayName() + "]");
                flag.setCustomNameVisible(true);

                flag.setTransformation(normal);

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

            BlockDisplay flag = world.spawn(plLoc.clone().add(offsetVec).add(0, 0.5f, 0), BlockDisplay.class);
            flag.setBlock(item.getType().createBlockData());

            flag.setCustomName("[" + name + "]");
            flag.setCustomNameVisible(true);

            flag.setTransformation(normal);

            blockDisplays.add(flag);
            displays.add(flag);
        }
        return blockDisplays;

    }

    static ItemStack nameItem(Material m,String name) {
        ItemStack item = new ItemStack(m, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
