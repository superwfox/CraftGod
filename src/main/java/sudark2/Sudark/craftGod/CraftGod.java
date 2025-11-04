package sudark2.Sudark.craftGod;

import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class CraftGod extends JavaPlugin {

    static List<BlockDisplay> displays = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerInteractEvent(), this);
    }

    public static Plugin get() {
        return Bukkit.getPluginManager().getPlugin("CraftGod");
    }

    @Override
    public void onDisable() {
        displays.forEach(BlockDisplay::remove);
    }
}
