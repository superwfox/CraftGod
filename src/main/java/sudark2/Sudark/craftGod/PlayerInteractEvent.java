package sudark2.Sudark.craftGod;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static sudark2.Sudark.craftGod.CraftGod.get;

public class PlayerInteractEvent implements Listener {

    @EventHandler
    public void onPlayerClickAtAir(org.bukkit.event.player.PlayerInteractEvent event) {
        Player pl = event.getPlayer();
        Action ac = event.getAction();

        if (!ac.equals(Action.RIGHT_CLICK_AIR)) return;

        ItemStack item = pl.getItemInHand();
        if (item.getType() != Material.LIGHTNING_ROD) return;

        new BukkitRunnable() {
            World world = pl.getWorld();
            int n = 0;

            @Override
            public void run() {

                n++;
                if (n == 40)
                    cancel();
            }
        }.runTaskTimer(get(), 0, 4 * 20);


    }
}
