package sudark2.Sudark.craftGod.Listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import sudark2.Sudark.craftGod.Mark.Mark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.craftGod.Mark.Mark.markDisplay;

public class BuildingCreate implements Listener {

   public static ConcurrentHashMap<String, List<Mark>> BuildingTemplate = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<Mark>> BuildingMark = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Material type = event.getBlock().getType();

        if (type != Material.LIGHTNING_ROD) return;

        if(BuildingTemplate.containsKey(event.getPlayer().getName())) {
            Location putLoc = block.getLocation();
            String name = event.getPlayer().getName();
            List<Mark> marks = BuildingTemplate.get(name);
            markDisplay(marks, putLoc);
            appendMark(putLoc,name, marks);

            event.setBuild(false);
        }

    }

    public static void appendMark(Location putLoc,String name, List<Mark> mark) {

        if(!BuildingMark.containsKey(name)) {
            BuildingMark.put(name, mark);
            return;
        }

        int[] start = mark.get(0).getLoc();
        int[] offset = new int[]{putLoc.getBlockX() - start[0], putLoc.getBlockY() - start[1], putLoc.getBlockZ() - start[2]};

        List<Mark> newMark = new ArrayList<>();
        mark.forEach(m ->{
            newMark.add(new Mark(m.getLoc()[0] + offset[0], m.getLoc()[1] + offset[1], m.getLoc()[2] + offset[2], m.getData()));
        });
        BuildingMark.get(name).addAll(newMark);

    }
}
