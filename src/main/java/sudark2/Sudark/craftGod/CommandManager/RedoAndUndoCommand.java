package sudark2.Sudark.craftGod.CommandManager;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import sudark2.Sudark.craftGod.Mark.Mark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.craftGod.Listeners.BuildingCreate.*;
import static sudark2.Sudark.craftGod.Mark.Mark.markDisplay;

public class RedoAndUndoCommand implements org.bukkit.command.CommandExecutor {

    static ConcurrentHashMap<String, List<Mark>> Redo = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (commandSender instanceof Player pl)
            if (args.length == 1) {

                String plName = pl.getName();
                switch (args[0]) {
                    case "undo" -> undo(plName);
                    case "redo" -> redo(plName, pl);
                }

                return true;
            }
        return false;
    }

    public void undo(String name) {
        int step = PlayerStep.get(name).getLast();

        // 处理 PreviewTemplate
        List<BlockDisplay> previews = PreviewTemplate.get(name);
        int size = previews.size();

        for (int i = size - 1; i >= size - step; i--) {
            previews.get(i).remove();
            previews.remove(i);
        }

        // 处理 BuildingMark
        List<Mark> marks = BuildingMark.get(name);
        size = marks.size();

        for (int i = size - 1; i >= size - step; i--) {
            Redo.computeIfAbsent(name, k -> new ArrayList<>())
                    .add(marks.get(i));
            marks.remove(i);
        }

        PlayerStep.get(name).removeLast();
    }


    public void redo(String name, Player pl) {
        int[] offset = BuildingMark.get(name).getFirst().getLoc();
        Location putLoc = new Location(pl.getWorld(), offset[0], offset[1], offset[2]);
        List<Mark> marks = Redo.get(name);

        PreviewTemplate.get(name).addAll(markDisplay(marks, putLoc));
        BuildingMark.get(name).addAll(marks);
        PlayerStep.get(name).add(marks.size());
    }
}
