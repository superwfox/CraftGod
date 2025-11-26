package sudark2.Sudark.craftGod.Menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import sudark2.Sudark.craftGod.Mark.Mark;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static sudark2.Sudark.craftGod.BlockMenu.*;
import static sudark2.Sudark.craftGod.Listeners.BuildingCreate.BuildingTemplate;
import static sudark2.Sudark.craftGod.CraftGod.get;
import static sudark2.Sudark.craftGod.FileManager.loadAllTemplateNames;
import static sudark2.Sudark.craftGod.FileManager.loadTemplate;

public class menuCreate {

    static final List<ItemStack> buildingTypes = List.of(
            nameItem(Material.QUARTZ_PILLAR, "罗马柱"),
            nameItem(Material.TWISTING_VINES, "直线工具"),
            nameItem(Material.HONEY_BLOCK, "圆形工具"),
            nameItem(Material.BEEHIVE, "所有模板"),
            nameItem(Material.YELLOW_GLAZED_TERRACOTTA, "创建模板")
    );

    private static final Map<Integer, Consumer<Player>> menuActions = Map.of(
            0, menuCreate::pillar,
            1, menuCreate::liner,
            2, menuCreate::circle,
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

    public static void pillar(Player p) {

    }

    public static void liner(Player p) {

    }

    public static void circle(Player p) {

    }

    static int[][] directions = {
            {-1, 1}, {1, -1}, {1, -1}, {-1, 1}//z+ x+ x- z-
    };

    static int[][] faces = {
            {0, 5}, {5, 0}, {0, -5}, {-5, 0}//z+ x+ x- z-
    };

    public static void other(Player p) {
        if (loadAllTemplateNames().isEmpty()) {
            title(p, "[没有模板]", "请先§e创建模板");
            return;
        }
        float yaw = p.getYaw();

        World world = p.getWorld();
        int index;

        if (yaw < 135 && yaw > 45) index = 3;
        else if (yaw <= 45 && yaw > -45) index = 0;
        else if (yaw <= -45 && yaw > -135) index = 1;
        else index = 2;

        int[] selectedDirection = directions[index];
        int dx = selectedDirection[0];
        int dz = selectedDirection[1];
        int moveX = faces[index][0];
        int moveZ = faces[index][1];
        temMenu(p, 0, world, moveX, moveZ, dx, dz);
    }


    public static void create(Player p) {
        p.setMetadata("menu", new FixedMetadataValue(get(), true));
        title(p, "[创造模板]", "使用避雷针左键方块确定模板");
    }

    public static void temMenu(Player p, int order, World world, int moveX, int moveZ, int dx, int dz) {
        List<String> names = loadAllTemplateNames();
        String nameNow = names.get(order);
        List<Mark> marks = loadTemplate(nameNow, world).right();
        final int N = names.size();

        String nextName = names.get((order + 1) % N);
        String previousName = names.get((order + N - 1) % N);
        List<BlockDisplay> preview = spawnCreature(moveX, moveZ, marks, p, dx, dz, world);

        menuInit(p, spawnMenu(List.of(
                nameItem(Material.OXIDIZED_COPPER_GRATE, previousName),
                nameItem(Material.GOLD_BLOCK, nameNow),
                nameItem(Material.WEATHERED_COPPER_GRATE, nextName)
        ), p)).thenAccept(
                in -> {
                    System.out.println(in);
                    if (in == -1) return;
                    switch (in) {
                        case 0 -> {
                            Bukkit.getScheduler().runTask(get(), () -> preview.forEach(BlockDisplay::remove));
                            temMenu(p, (order + N - 1) % N, world, moveX, moveZ, dx, dz);
                        }
                        case 1 -> {
                            Bukkit.getScheduler().runTask(get(), () -> preview.forEach(BlockDisplay::remove));
                            BuildingTemplate.put(p.getName(), marks);
                        }
                        case 2 -> {
                            Bukkit.getScheduler().runTask(get(), () -> preview.forEach(BlockDisplay::remove));
                            temMenu(p, (order + 1) % N, world, moveX, moveZ, dx, dz);
                        }
                    }
                });
    }


}
