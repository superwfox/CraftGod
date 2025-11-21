package sudark2.Sudark.craftGod.Mark;

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
        int minX = Math.min(start.getBlockX(), end.getBlockX()) + 1;
        int maxX = Math.max(start.getBlockX(), end.getBlockX());
        int minZ = Math.min(start.getBlockZ(), end.getBlockZ()) + 1;
        int maxZ = Math.max(start.getBlockZ(), end.getBlockZ());
        int minY = Math.min(start.getBlockY(), end.getBlockY()) + 1;
        int maxY = Math.max(start.getBlockY(), end.getBlockY());
        World world = start.getWorld();

        int length = maxX - minX;
        int width = maxZ - minZ;
        int height = maxY - minY;
        for (int x = 0; x < length; x++)
            for (int y = 0; y < height; y++)
                for (int z = 0; z < width; z++) {
                    Block block = world.getBlockAt(minX + x, minY + y, minZ + z);

                    if (block.getType() == Material.AIR) continue;
                    Mark mark = new Mark(x, y, z, block.getBlockData());
                    marks.add(mark);
                }

        return Pair.of(new Location(world, minX, minY, minZ), marks);
    }
}
