package sudark2.Sudark.craftGod.Menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import sudark2.Sudark.craftGod.Listeners.ReflectListener;

import static sudark2.Sudark.craftGod.BlockMenu.title;
import static sudark2.Sudark.craftGod.CraftGod.get;

public class menuReflect {
    public static void menu(Player p) {

        title(p, "[输入建筑码]", "请在聊天框输入您要使用的建筑码");
        p.sendMessage("[创世神] 请在这里输入建筑码 无所谓大小写和空格 ：\n §7[一分钟后将自动取消]");
        Plugin plugin = get();

        p.setMetadata("codeInput", new FixedMetadataValue(plugin, true));
        ReflectListener listener = new ReflectListener();
        Bukkit.getPluginManager().registerEvents(listener, plugin);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (!p.hasMetadata("codeInput")) return;

            title(p, "[已取消]", "输入建筑码超时");

            p.removeMetadata("codeInput", plugin);
            HandlerList.unregisterAll(listener);
        }, 60 * 20);
    }
}
