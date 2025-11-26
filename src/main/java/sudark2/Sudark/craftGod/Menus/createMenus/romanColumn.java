package sudark2.Sudark.craftGod.Menus.createMenus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static sudark2.Sudark.craftGod.BlockMenu.*;
import static sudark2.Sudark.craftGod.Listeners.BuildingCreate.BuildingTemplate;

public class romanColumn {

    private final static List<ItemStack> columnOptions = List.of(
            nameItem(Material.QUARTZ_BLOCK, "高度+"),
            nameItem(Material.QUARTZ_BRICKS, "高度-"),
            nameItem(Material.GOLD_BLOCK, "确认"),
            nameItem(Material.QUARTZ_PILLAR, "宽度+"),
            nameItem(Material.QUARTZ_SLAB, "宽度-")
    );

    public static void create(Player pl) {
        BuildingTemplate.put();

        menuInit(pl, spawnMenu(columnOptions, pl))
                .thenAccept(
                        index -> {

                        }
                );
    }

    private static void heightUp(){

    }

    private final Map<Integer, Consumer<Player>> menuActions = Map.of(
            0,
    );

}
