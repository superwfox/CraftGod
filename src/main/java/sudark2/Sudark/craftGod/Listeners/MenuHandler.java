package sudark2.Sudark.craftGod.Listeners;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import sudark2.Sudark.craftGod.Mark.Mark;
import sudark2.Sudark.craftGod.Menus.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static sudark2.Sudark.craftGod.BlockMenu.*;
import static sudark2.Sudark.craftGod.CraftGod.get;
import static sudark2.Sudark.craftGod.FileManager.templateFolder;
import static sudark2.Sudark.craftGod.Mark.MarkCreator.createMark;
import static sudark2.Sudark.craftGod.FileManager.saveTemplate;

public class MenuHandler implements Listener {

    static ConcurrentHashMap<String, Location> temp = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerClickAtAir(org.bukkit.event.player.PlayerInteractEvent event) {
        Player pl = event.getPlayer();
        Action ac = event.getAction();

        if (ac.equals(Action.LEFT_CLICK_AIR) || ac.equals(Action.LEFT_CLICK_BLOCK)) {
            pl.setMetadata("click", new FixedMetadataValue(get(), true));
            Bukkit.getScheduler().runTaskLater(get(), () -> pl.removeMetadata("click", get()), 3);

            if (!pl.hasMetadata("menu")) return;

            ItemStack item = pl.getItemInHand();
            if (item.getType() != Material.LIGHTNING_ROD) return;

            String name = pl.getName();
            Location tarLoc = event.getClickedBlock().getLocation();
            if (!temp.containsKey(name)) {
                temp.put(name, tarLoc);
                title(pl, "[还差一角]", "已记录点A 还需要第二个角落");
                return;
            }

            title(pl, "[已记录点B]", "现在你可以在§b[所有模板]§f中使用该模板");

            Pair<Location, List<Mark>> mark = createMark(tarLoc, temp.get(name));
            pl.removeMetadata("menu", get());
            saveTemplate(templateFolder, pl.getName(), mark);
        }

        if(ac.equals(Action.RIGHT_CLICK_AIR) || ac.equals(Action.RIGHT_CLICK_BLOCK)) {
            pl.setMetadata("touch", new FixedMetadataValue(get(), true));
            Bukkit.getScheduler().runTaskLater(get(), () -> pl.removeMetadata("touch", get()), 3);
        }
    }

    @EventHandler
    public void onMenuInit(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        Player pl = event.getPlayer();
        ItemStack item = pl.getItemInHand();
        if (item.getType() != Material.LIGHTNING_ROD) return;

        if (pl.isFlying()) return;

        if (!pl.isSneaking()) {
            pl.setMetadata("sneak", new FixedMetadataValue(get(), true));
            Bukkit.getScheduler().runTaskLater(get(), () -> pl.removeMetadata("sneak", get()), 3);

            menuInit(
                    pl, spawnMenu(List.of(
                                    nameItem(Material.SCULK_SHRIEKER, "创造"),
                                    nameItem(Material.BREWING_STAND, "建造"),
                                    nameItem(Material.SOUL_CAMPFIRE, "从建筑码获取投影")
                            ),
                            pl
                    )
            ).thenAccept(index -> {
                if (index == -1) return;
                Consumer<Player> action = menuActions.get(index);
                if (action != null)
                    action.accept(pl);
            });
        }
    }

    private final Map<Integer, Consumer<Player>> menuActions = Map.of(
            0, menuCreate::menu,
            1, menuPrint::menu,
            2, menuReflect::menu
    );

}
