package sudark2.Sudark.craftGod;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class MarkCreator {

    public static Pair<Location, List<Mark>> createMark(Location start, Location end) {
        List<Mark> marks = new ArrayList<>();
        int minX = Math.min(start.getBlockX(), end.getBlockX());
        int maxX = Math.max(start.getBlockX(), end.getBlockX());
        int minZ = Math.min(start.getBlockZ(), end.getBlockZ());
        int maxZ = Math.max(start.getBlockZ(), end.getBlockZ());
        int minY = Math.min(start.getBlockY(), end.getBlockY());
        int maxY = Math.max(start.getBlockY(), end.getBlockY());
        World world = start.getWorld();

        int length = maxX - minX + 1;
        int width = maxZ - minZ + 1;
        int height = maxY - minY + 1;
        for (int x = 0; x <= length; x++)
            for (int y = 0; y <= width; y++)
                for (int z = 0; z <= height; z++) {
                    Block block = world.getBlockAt(minX + x, minY + y, minZ + z);

                    if (block.getType() == Material.AIR) continue;
                    Mark mark = new Mark(x, y, z, block.getBlockData());
                    marks.add(mark);
                }

        return Pair.of(start, marks);
    }
}
