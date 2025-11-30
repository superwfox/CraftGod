package sudark2.Sudark.craftGod.Mark;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
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

    public static List<BlockDisplay> markDisplay(List<Mark> marks, Location loc) {
        List<BlockDisplay> blockDisplays = new ArrayList<>();
        for (Mark mark : marks) {
            BlockDisplay block = (BlockDisplay) loc.getWorld().spawnEntity(loc.clone().add(mark.getDx(), mark.getDy(), mark.getDz()), EntityType.BLOCK_DISPLAY);
            block.setBlock(mark.getData());
            block.setTransformation(x0of8);
            blockDisplays.add(block);
        }
        return blockDisplays;
    }

    static float length = 0.875f;//0.0625 x 10
    static Transformation x0of8 = new Transformation(
            new Vector3f(0.0625f, 0.0625f, 0.0625f),
            new Quaternionf(),
            new Vector3f(length, length, length),
            new Quaternionf()
    );

    public int[] getLoc() {
        return new int[]{dx, dy, dz};
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
