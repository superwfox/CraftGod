package sudark2.Sudark.craftGod.Menus;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.craftGod.BlockMenu.menuLoc;
import static sudark2.Sudark.craftGod.BlockMenu.title;
import static sudark2.Sudark.craftGod.CraftGod.get;

public class menuPrint {

    public static NamespacedKey amountKey = new NamespacedKey("sudark", "extra_amount");

    public static ConcurrentHashMap<String, BukkitTask> tasks = new ConcurrentHashMap<>();

    public static void menu(Player pl) {
        String key = pl.getName();

        menuLoc.remove(key);

        if (tasks.containsKey(key)) {
            title(pl, "[建造结束]", "现在不会自动搭建了");
            tasks.get(key).cancel();
            tasks.remove(key);
            return;
        }

        title(pl, "[建造开始]", "现在开始会自动填充周围方块");
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                pl.sendActionBar("§e§l[BUILDING]");

                List<BlockDisplay> blocks = pl.getNearbyEntities(15, 15, 15).stream()
                        .filter(e -> e instanceof BlockDisplay)
                        .map(e -> (BlockDisplay) e)
                        .toList();

                if (blocks.isEmpty()) {
                    return;
                }

                Inventory inv = pl.getInventory();
                for (BlockDisplay bd : blocks) {
                    BlockData data = bd.getBlock();
                    Material type = data.getMaterial();

                    ItemStack cost = new ItemStack(type, 1);
                    boolean lackItem = consumeItem(cost, inv);

                    if (lackItem) {
                        pl.sendActionBar("§7[材料不足 缺少 §b" + type.name() + "]");
                        break;
                    }

                    pl.playSound(pl, Sound.BLOCK_BAMBOO_PLACE, 1, 1);
                    Block block = bd.getLocation().getBlock();
                    block.setBlockData(data, false); // 第二个参数要不要物理更新看你需求
                    bd.remove();
                }

            }
        }.runTaskTimer(get(), 20, 20);
        tasks.put(key, task);
    }

    public static boolean consumeItem(ItemStack need, Inventory inv) {

        Material type = need.getType();

        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack stack = inv.getItem(slot);
            if (stack == null || stack.getType().isAir()) continue;
            if (stack.getType() != type) continue;

            ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (!pdc.has(amountKey, PersistentDataType.INTEGER)) {
                continue;
            }

            int amount = pdc.getOrDefault(amountKey, PersistentDataType.INTEGER, 0);

            if (amount <= type.getMaxStackSize()) {
                pdc.remove(amountKey);
                meta.setLore(null);
                stack.setItemMeta(meta);
                continue;
            }

            amount -= 1;
            pdc.set(amountKey, PersistentDataType.INTEGER, amount);
            meta.setLore(List.of("§7+" + amount));
            stack.setItemMeta(meta);

            return true; // 成功从 PDC 物品中扣除了 1 个
        }

        ItemStack cost = need.clone();
        cost.setAmount(1);

        Map<Integer, ItemStack> leftover = inv.removeItem(cost);
        return !leftover.isEmpty(); // true 表示扣成功，false 表示材料不足
    }

}
