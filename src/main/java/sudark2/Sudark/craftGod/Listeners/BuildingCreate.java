package sudark2.Sudark.craftGod.Listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.checkerframework.checker.units.qual.C;
import sudark2.Sudark.craftGod.Mark.Mark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.craftGod.Mark.Mark.markDisplay;

public class BuildingCreate implements Listener {

    public static ConcurrentHashMap<String, List<Mark>> BuildingTemplate = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, List<Mark>> BuildingMark = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, List<BlockDisplay>> PreviewTemplate = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, List<Integer>> PlayerStep = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Material type = event.getBlock().getType();

        if (type != Material.LIGHTNING_ROD) return;

        if (BuildingTemplate.containsKey(event.getPlayer().getName())) {
            Location putLoc = block.getLocation();
            String name = event.getPlayer().getName();

            applyMark(name, BuildingTemplate.get(name), putLoc);
            event.setBuild(false);
        }

    }

    public static void applyMark(String name, List<Mark> marks, Location putLoc) {
        PreviewTemplate.putIfAbsent(name, new ArrayList<>());
        PreviewTemplate.get(name).addAll(markDisplay(marks, putLoc));

        PlayerStep.putIfAbsent(name, new ArrayList<>());
        PlayerStep.get(name).add(marks.size());

        appendMark(putLoc, name, marks);
    }

    public static void appendMark(Location putLoc, String name, List<Mark> mark) {

        if (!BuildingMark.containsKey(name)) {
            BuildingMark.put(name, new ArrayList<>(mark));
            return;
        }

        List<Mark> B = BuildingMark.get(name);
        int[] start = B.getFirst().getLoc();
        int[] offset = new int[]{
                putLoc.getBlockX() - start[0],
                putLoc.getBlockY() - start[1],
                putLoc.getBlockZ() - start[2]
        };

        List<Mark> newMark = new ArrayList<>();
        mark.forEach(m -> {
            newMark.add(new Mark(
                    m.getLoc()[0] + offset[0],
                    m.getLoc()[1] + offset[1],
                    m.getLoc()[2] + offset[2],
                    m.getData()
            ));
        });

        B.addAll(newMark);
    }

}
