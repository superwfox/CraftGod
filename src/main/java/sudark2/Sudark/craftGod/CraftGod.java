package sudark2.Sudark.craftGod;

import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import sudark2.Sudark.craftGod.CommandManager.RedoAndUndoCommand;
import sudark2.Sudark.craftGod.CommandManager.TabManager;
import sudark2.Sudark.craftGod.Listeners.BuildingCreate;
import sudark2.Sudark.craftGod.Listeners.MenuHandler;

import java.util.ArrayList;
import java.util.List;

public final class CraftGod extends JavaPlugin {

    public static List<BlockDisplay> displays = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new MenuHandler(), this);
        Bukkit.getPluginManager().registerEvents(new BuildingCreate(), this);

        Bukkit.getPluginCommand("god").setExecutor(new RedoAndUndoCommand());
        Bukkit.getPluginCommand("god").setTabCompleter(new TabManager());

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
