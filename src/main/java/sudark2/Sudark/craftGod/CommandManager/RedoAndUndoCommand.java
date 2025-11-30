package sudark2.Sudark.craftGod.CommandManager;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import sudark2.Sudark.craftGod.Mark.Mark;
import sudark2.Sudark.craftGod.RandomCodeGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.craftGod.BlockMenu.title;
import static sudark2.Sudark.craftGod.CraftGod.get;
import static sudark2.Sudark.craftGod.FileManager.markFolder;
import static sudark2.Sudark.craftGod.FileManager.saveTemplate;
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
                    case "finish" -> over(plName, pl);
                    case "cancel" -> cancel(plName,pl);
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
        if (!Redo.containsKey(name)) {
            title(pl, "[无法重做]", "没有可重做的步骤 你需要/undo");
            return;
        }
        List<Mark> marks = Redo.get(name);

        PreviewTemplate.get(name).addAll(markDisplay(marks, putLoc));
        BuildingMark.get(name).addAll(marks);
        PlayerStep.get(name).add(marks.size());
        Redo.remove(name);
    }

    public void over(String name, Player pl) {
        if (!BuildingMark.containsKey(name)) {
            title(pl, "[失败]", "没有可创建的建筑");
            return;
        }

        title(pl, "[成功]", "请稍等 正在保存建筑");
        String code = new RandomCodeGenerator().generateUniqueCode(20);
        if (code.equals("ERROR")) {
            title(pl, "[失败]", "无法生成唯一码 请重试");
            return;
        }

        int[] offset = BuildingMark.get(name).getFirst().getLoc();
        Location putLoc = new Location(pl.getWorld(), offset[0], offset[1], offset[2]);
        saveTemplate(markFolder, code, Pair.of(putLoc, BuildingMark.get(name)));

        PreviewTemplate.get(name).forEach(display -> {
            display.setTeleportDuration(38);
            display.teleport(display.getLocation().subtract(0, 20, 0));
        });

        Bukkit.getScheduler().runTaskLater(get(), () -> {
            String formattedCode = code.substring(0, 3) + " " + code.substring(3);
            title(pl, "[成功]", "已保存建筑码为 §e§l" + formattedCode);
            pl.sendMessage("[创世神] 建筑码： §e§l" + formattedCode);

            PreviewTemplate.get(name).forEach(BlockDisplay::remove);
            PreviewTemplate.remove(name);

            PlayerStep.remove(name);

            BuildingMark.remove(name);
            BuildingStartPoint.remove(name);

        }, 40);

    }

    public void cancel(String name, Player pl) {
        if (!BuildingMark.containsKey(name)) {
            title(pl, "[失败]", "没有可取消的建筑");
            return;
        }

        title(pl, "[取消]", "已取消创建");
        PreviewTemplate.get(name).forEach(BlockDisplay::remove);
        PreviewTemplate.remove(name);

        PlayerStep.remove(name);

        BuildingMark.remove(name);
        BuildingStartPoint.remove(name);
    }
}
