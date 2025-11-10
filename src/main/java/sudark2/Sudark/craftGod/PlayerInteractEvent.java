package sudark2.Sudark.craftGod;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import static sudark2.Sudark.craftGod.BlockMenu.menuInit;
import static sudark2.Sudark.craftGod.CraftGod.get;

public class PlayerInteractEvent implements Listener {

    @EventHandler
    public void onPlayerClickAtAir(org.bukkit.event.player.PlayerInteractEvent event) {
        Player pl = event.getPlayer();
        Action ac = event.getAction();

        if (ac.equals(Action.RIGHT_CLICK_AIR) || ac.equals(Action.RIGHT_CLICK_BLOCK)) {
            pl.setMetadata("click",new FixedMetadataValue(get(), true));
        }

    }

    @EventHandler
    public void onPlayerClickAtBlock(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        Player pl = event.getPlayer();
        ItemStack item = pl.getItemInHand();
        if (item.getType() != Material.LIGHTNING_ROD) return;

        if(pl.isSneaking()){
            pl.setMetadata("sneak", new FixedMetadataValue(get(), true));
            pl.setSneaking(true);
        }else{
            pl.setMetadata("sneak", new FixedMetadataValue(get(), true));
            pl.setSneaking(true);
            menuInit(pl);
        }



    }
}
