package sudark2.Sudark.craftGod.Menus.createMenus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import sudark2.Sudark.craftGod.Mark.Mark;

import java.util.ArrayList;
import java.util.List;

import static sudark2.Sudark.craftGod.CraftGod.displays;
import static sudark2.Sudark.craftGod.CraftGod.get;

public class menuController {

    public static int[][] directions = {
            {-1, 1}, {1, -1}, {1, -1}, {-1, 1}//z+ x+ x- z-
    };

    public static int[][] faces = {
            {0, 3}, {3, 0}, {0, -3}, {-3, 0}//z+ x+ x- z-
    };

    public static List<BlockDisplay> displayTemplate(Player p, List<Mark> marks) {
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

        return spawnCreature(moveX, moveZ, marks, p, dx, dz, world);
    }

    public static List<BlockDisplay> spawnCreature(int moveX, int moveZ, List<Mark> marks, Player pl, int dx, int dz, World world) {
        double factor = Math.log(marks.size());
        Location startLoc = pl.getLocation().getBlock().getLocation().add((int) (moveX * factor), 0, (int) (moveZ * factor));
        List<BlockDisplay> marksPlaced = new ArrayList<>();
        Bukkit.getScheduler().runTask(get(), () -> {
            for (Mark mark : marks) {
                Location loc = startLoc.clone().add(dx * mark.getDx(), mark.getDy(), dz * mark.getDz());
                BlockDisplay display = world.spawn(loc, BlockDisplay.class);
                display.setBlock(mark.getData());
                displays.add(display);
                marksPlaced.add(display);
            }
        });
        return marksPlaced;
    }
}
