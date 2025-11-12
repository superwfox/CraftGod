package sudark2.Sudark.craftGod.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static sudark2.Sudark.craftGod.BlockMenu.*;
import static sudark2.Sudark.craftGod.CraftGod.get;

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
        menuInit(p, spawnMenu(buildingTypes, p, 0)).thenAccept(
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

    public static void other(Player p) {

    }

    public static void create(Player p) {
        p.setMetadata("menu", new FixedMetadataValue(get(), true));
        title(p, "[创造模板]", "使用避雷针左键方块确定模板");
    }


}
