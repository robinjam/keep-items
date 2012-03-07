package net.robinjam.bukkit.keepitems;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin class.
 * 
 * @author robinjam
 */
public class KeepItems extends JavaPlugin implements Listener {
    
    private Map<Player, Death> deaths = new HashMap<Player, Death>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @Override
    public void onDisable() {
        // When the plugin is disabled, drop all managed items and clear the list
        for (Death death : deaths.values()) {
            death.drop();
        }
        
        deaths.clear();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(final EntityDeathEvent event) {
        // Skip if the entity that died is not a player
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        
        // If the player already has a death on record, drop the items
        Death death = deaths.get(player);
        if (death != null)
            death.drop();
        
        ItemStack[] inventoryContents = new ItemStack[0];
        ItemStack[] armorContents = new ItemStack[0];
        int level = 0;
        
        if (player.hasPermission("keep-items.items")) {
            inventoryContents = player.getInventory().getContents();
            armorContents = player.getInventory().getArmorContents();
            
            // Don't drop any items at the death location
            event.getDrops().clear();
        }
        
        if (player.hasPermission("keep-items.experience")) {
            level = player.getLevel();
            
            // Don't drop any experience at the death location
            event.setDroppedExp(0);
        }
        
        // Register the death event
        deaths.put(player, new Death(this, player.getLocation(), inventoryContents, armorContents, level));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // If the player has a death on record, drop the items and experience at their respawn location
        Death death = deaths.remove(player);
        if (death != null)
            death.give(player);
    }
    
}
