package sudark2.Sudark.craftGod.Menus.createMenus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.craftGod.Mark.Mark;

import java.util.ArrayList;
import java.util.List;

import static sudark2.Sudark.craftGod.BlockMenu.*;
import static sudark2.Sudark.craftGod.CraftGod.get;
import static sudark2.Sudark.craftGod.Listeners.BuildingCreate.BuildingTemplate;
import static sudark2.Sudark.craftGod.Menus.createMenus.menuController.displayTemplate;

public class romanColumnSpawner {

    private final static List<ItemStack> columnOptions = List.of(
            nameItem(Material.QUARTZ_PILLAR, "高度+"),
            nameItem(Material.QUARTZ_SLAB, "高度-"),
            nameItem(Material.GOLD_BLOCK, "确认"),
            nameItem(Material.QUARTZ_SLAB, "半径-"),
            nameItem(Material.SMOOTH_QUARTZ, "半径+")
    );

    public static void menu(Player p) {
        create(p, -1, -1);
    }

    public static void create(Player pl, int height, int radius) {
        if (height == -1 || radius == -1) {
            height = 8;
            radius = 2;
        }
        List<Mark> marks = createRomanPillar(height, radius, Material.QUARTZ_PILLAR.createBlockData());
        List<BlockDisplay> preview = displayTemplate(pl, marks);

        int finalHeight = height;
        int finalRadius = radius;
        menuInit(pl, spawnMenu(columnOptions, pl))
                .thenAccept(
                        index -> {
                            Bukkit.getScheduler().runTask(get(), () -> preview.forEach(BlockDisplay::remove));
                            switch (index) {
                                case 0 -> create(pl, finalHeight + 1, finalRadius);
                                case 1 -> create(pl, finalHeight - 1, finalRadius);
                                case 2 -> {
                                    BuildingTemplate.put(pl.getName(), marks);
                                    menuLoc.remove(pl.getName());
                                }
                                case 3 -> create(pl, finalHeight, finalRadius - 1);
                                case 4 -> create(pl, finalHeight, finalRadius + 1);
                            }
                        }
                );
    }

    public static List<Mark> createRomanPillar(int height, int radius, BlockData material) {
        List<Mark> pillar = new ArrayList<>();

        // 柱基（2层，去掉四角）
        int r = radius + 1;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                pillar.add(new Mark(x, 0, z, material));
                // 边缘加高一层，但四角不加
                if ((Math.abs(x) == r || Math.abs(z) == r) &&
                        !(Math.abs(x) == r && Math.abs(z) == r)) {
                    pillar.add(new Mark(x, 1, z, material));
                }
            }
        }

        for (int y = 2; y < height - 2; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {

                    // 归一化到 [-1, 1] 区间，稍微 +0.2 防止太瘦
                    double nx = (double) x / (radius + 0.2);
                    double nz = (double) z / (radius + 0.2);

                    // n 越大越接近方形（圆角矩形）
                    double n = 4.0; // 可以试 3.0, 4.0, 5.0
                    double value = Math.pow(Math.abs(nx), n) + Math.pow(Math.abs(nz), n);

                    if (value <= 1.0) {
                        pillar.add(new Mark(x, y, z, material));
                    }
                }
            }
        }

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                // 只保留非四角的方块
                if (!(Math.abs(x) == r && Math.abs(z) == r)) {
                    pillar.add(new Mark(x, height - 2, z, material));
                    pillar.add(new Mark(x, height - 1, z, material));
                }
            }
        }

        return pillar;
    }

}
