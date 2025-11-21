package sudark2.Sudark.craftGod;

import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import sudark2.Sudark.craftGod.Listeners.BuildingCreate;
import sudark2.Sudark.craftGod.Listeners.PlayerInteractEvent;
import sudark2.Sudark.craftGod.Menus.FileManager;

import java.util.ArrayList;
import java.util.List;

public final class CraftGod extends JavaPlugin {

    static List<BlockDisplay> displays = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerInteractEvent(), this);
        Bukkit.getPluginManager().registerEvents(new BuildingCreate(), this);

        FileManager.init();
    }

    public static Plugin get() {
        return Bukkit.getPluginManager().getPlugin("CraftGod");
    }

    @Override
    public void onDisable() {
        displays.forEach(BlockDisplay::remove);
    }
}
