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

        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);

                    if (block.getType() == Material.AIR) continue;
                    Mark mark = new Mark(x, y, z, block.getBlockData());
                    marks.add(mark);
                }

        return Pair.of(start, marks);
    }
}
