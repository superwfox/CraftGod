package sudark2.Sudark.craftGod.Menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import sudark2.Sudark.craftGod.Mark.Mark;
import sudark2.Sudark.craftGod.Menus.createMenus.circleSpawner;
import sudark2.Sudark.craftGod.Menus.createMenus.linerSpawner;
import sudark2.Sudark.craftGod.Menus.createMenus.romanColumnSpawner;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static sudark2.Sudark.craftGod.BlockMenu.*;
import static sudark2.Sudark.craftGod.FileManager.*;
import static sudark2.Sudark.craftGod.Listeners.BuildingCreate.BuildingTemplate;
import static sudark2.Sudark.craftGod.CraftGod.get;
import static sudark2.Sudark.craftGod.Menus.createMenus.menuController.*;

public class menuCreate {

    static final List<ItemStack> buildingTypes = List.of(
            nameItem(Material.QUARTZ_PILLAR, "罗马柱"),
            nameItem(Material.CYAN_SHULKER_BOX, "矩形工具"),
            nameItem(Material.HONEY_BLOCK, "圆形工具"),
            nameItem(Material.BEEHIVE, "所有模板"),
            nameItem(Material.YELLOW_GLAZED_TERRACOTTA, "创建模板")
    );

    private static final Map<Integer, Consumer<Player>> menuActions = Map.of(
            0, romanColumnSpawner::menu,
            1, linerSpawner::menu,
            2, circleSpawner::menu,
            3, menuCreate::other,
            4, menuCreate::create
    );

    public static void menu(Player p) {
        menuInit(p, spawnMenu(buildingTypes, p)).thenAccept(
                index -> {
                    if (index == -1) return;
                    Consumer<Player> action = menuActions.get(index);
                    if (action != null)
                        action.accept(p);
                }
        );
    }

    public static void other(Player p) {
        if (loadAllTemplateNames().isEmpty()) {
            title(p, "[没有模板]", "请先§e创建模板");
            return;
        }
        temMenu(p, 0);
    }

    public static void create(Player p) {
        p.setMetadata("menu", new FixedMetadataValue(get(), true));
        title(p, "[创造模板]", "使用避雷针左键方块确定模板");
    }

    public static void temMenu(Player p, int order) {
        List<String> names = loadAllTemplateNames();
        String nameNow = names.get(order);
        World world = p.getWorld();

        List<Mark> marks = loadTemplate(templateFolder,nameNow, world).right();
        final int N = names.size();

        String nextName = names.get((order + 1) % N);
        String previousName = names.get((order + N - 1) % N);
        List<BlockDisplay> preview = displayTemplate(p, marks);

        menuInit(p, spawnMenu(List.of(
                nameItem(Material.OXIDIZED_COPPER_GRATE, previousName),
                nameItem(Material.GOLD_BLOCK, nameNow),
                nameItem(Material.WEATHERED_COPPER_GRATE, nextName)
        ), p)).thenAccept(
                in -> {
                    if (in == -1) return;
                    switch (in) {
                        case 0 -> {
                            Bukkit.getScheduler().runTask(get(), () -> preview.forEach(BlockDisplay::remove));
                            temMenu(p, (order + N - 1) % N);
                        }
                        case 1 -> {
                            Bukkit.getScheduler().runTask(get(), () -> preview.forEach(BlockDisplay::remove));
                            BuildingTemplate.put(p.getName(), marks);
                        }
                        case 2 -> {
                            Bukkit.getScheduler().runTask(get(), () -> preview.forEach(BlockDisplay::remove));
                            temMenu(p, (order + 1) % N);
                        }
                    }
                });
    }


}
