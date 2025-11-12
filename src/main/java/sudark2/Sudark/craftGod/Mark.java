package sudark2.Sudark.craftGod;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;

import java.util.List;

public class Mark {
    private final int dx, dy, dz;
    private final BlockData data;

    public Mark(int dx, int dy, int dz, BlockData data) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.data = data;
    }

    public void markDisplay(List<Mark> marks, Location loc) {
        for (Mark mark : marks) {
           BlockDisplay block = (BlockDisplay) loc.getWorld().spawnEntity(loc.add(mark.getDx(), mark.getDy(), mark.getDz()),EntityType.BLOCK_DISPLAY);
            block.setBlock(mark.getData());
        }
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getDz() {
        return dz;
    }

    public BlockData getData() {
        return data;
    }

    public String getDataAsString() {
        return data.getAsString(true);
    }
}
