package sudark2.Sudark.craftGod.menus;

import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import sudark2.Sudark.craftGod.Mark;

import java.util.ArrayList;
import java.util.List;

import static sudark2.Sudark.craftGod.BlockMenu.*;
import static sudark2.Sudark.craftGod.CraftGod.get;

public class menuCreate {

    public static void menu(Player p) {
        new BukkitRunnable() {
            List<Mark> marks = new ArrayList<>();

            List<BlockDisplay> choices = spawnMenu(
                    List.of(
                            nameItem(Material.QUARTZ_PILLAR, "罗马柱"),
                            nameItem(Material.TWISTING_VINES, "直线工具"),
                            nameItem(Material.WEATHERED_COPPER_BULB, "圆形工具")
                    ),
                    p,
                    1
            );

            List<BlockDisplay> picked = new ArrayList<>();

            @Override
            public void run() {

                BlockDisplay bl = getTargetByAngle(p, choices);
                for (BlockDisplay display : choices) {
                    if (bl == display) {
                        if (display.getTransformation().equals(normal)) enlarge(display);
                    } else {
                        if (display.getTransformation().equals(huge)) reduce(display);
                    }
                }

                if(!p.hasMetadata("click"))return;

                if (bl != null) switch (bl.getBlock().getMaterial()) {
                    case
                }




            }
        }.runTaskTimerAsynchronously(get(), 0, 2);
    }
}
