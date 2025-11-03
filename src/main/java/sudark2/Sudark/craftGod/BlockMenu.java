package sudark2.Sudark.craftGod;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static sudark2.Sudark.craftGod.CraftGod.get;

public class BlockMenu {
    // 1 创造 2 打印他人的投影 3 投射他人的投影 4 建造
    public static void menuCreate() {
        new BukkitRunnable() {
            List<Mark> marks = new ArrayList<>();
            @Override
            public void run() {



            }
        }.runTaskTimerAsynchronously(get(), 0, 10);
    }

    public static void printMenu() {
    }

    public static void reflect() {
    }

    public static void build() {
    }
}
