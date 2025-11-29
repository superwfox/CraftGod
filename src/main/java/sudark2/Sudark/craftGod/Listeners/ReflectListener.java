package sudark2.Sudark.craftGod.Listeners;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import sudark2.Sudark.craftGod.FileManager;
import sudark2.Sudark.craftGod.Mark.Mark;

import java.util.List;

import static sudark2.Sudark.craftGod.BlockMenu.title;
import static sudark2.Sudark.craftGod.CraftGod.get;
import static sudark2.Sudark.craftGod.FileManager.markFolder;
import static sudark2.Sudark.craftGod.Listeners.BuildingCreate.BuildingTemplate;

public class ReflectListener implements Listener {

    @EventHandler
    public void onReflect(PlayerChatEvent e) {
        Player pl = e.getPlayer();
        if (pl.hasMetadata("codeInput")) {
            e.setCancelled(true);
            String code = e.getMessage().toUpperCase().replace(" ", "");
            String name = code + ".yml";

            if (FileManager.loadAllMarks().contains(name)) {
                Pair<Location, List<Mark>> mark = FileManager.loadTemplate(markFolder, name, pl.getWorld());
                BuildingTemplate.put(pl.getName(), mark.right());
                title(pl, "[已加载]", "用避雷针放置来确定位置");

                HandlerList.unregisterAll(this);
                pl.removeMetadata("codeInput", get());
            }

        }

    }
}
