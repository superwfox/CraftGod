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

public class linerSpawner {

    private final static List<ItemStack> lineOptions = List.of(
            nameItem(Material.QUARTZ_BLOCK, "高度+"),
            nameItem(Material.QUARTZ_SLAB, "高度-"),
            nameItem(Material.GOLD_BLOCK, "确认"),
            nameItem(Material.SMOOTH_QUARTZ, "宽度+"),
            nameItem(Material.QUARTZ_SLAB, "宽度-"),
            nameItem(Material.QUARTZ_SLAB, "长度-"),
            nameItem(Material.SMOOTH_QUARTZ, "长度+")
    );

    public static void menu(Player p) {
        create(p, -1, -1, -1);
    }

    public static void create(Player pl, int height, int length, int width) {
        if (height == -1 || length == -1 || width == -1) {
            height = 2;
            length = 2;
            width = 2;
        }
        List<Mark> marks = createSolidCuboid(height, length, width, Material.QUARTZ_BLOCK.createBlockData());
        List<BlockDisplay> preview = displayTemplate(pl, marks);

        int finalHeight = height;
        int finalLength = length;
        int finalWidth = width;
        menuInit(pl, spawnMenu(lineOptions, pl))
                .thenAccept(
                        index -> {
                            Bukkit.getScheduler().runTask(get(), () -> preview.forEach(BlockDisplay::remove));
                            switch (index) {
                                case 0 -> create(pl, finalHeight + 1, finalLength, finalWidth);
                                case 1 -> create(pl, finalHeight - 1, finalLength, finalWidth);
                                case 2 -> {
                                    BuildingTemplate.put(pl.getName(), marks);
                                    menuLoc.remove(pl.getName());
                                }
                                case 3 -> create(pl, finalHeight, finalLength + 1, finalWidth);
                                case 4 -> create(pl, finalHeight, finalLength - 1, finalWidth);
                                case 5 -> create(pl, finalHeight, finalLength, finalWidth - 1);
                                case 6 -> create(pl, finalHeight, finalLength, finalWidth + 1);
                            }
                        }
                );
    }

    public static List<Mark> createSolidCuboid(int height, int length, int width, BlockData data) {
        List<Mark> marks = new ArrayList<>();
        int hx = length / 2;
        int hz = width / 2;

        for (int y = 0; y < height; y++) {
            for (int x = -hx; x <= hx; x++) {
                for (int z = -hz; z <= hz; z++) {
                    marks.add(new Mark(x, y, z, data));
                }
            }
        }
        return marks;
    }


}
