package net.robinjam.bukkit.keepitems;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a death event, recording its location and the items dropped.
 * 
 * @author robinjam
 */
public class Death {
	
	private final Location location;
	private final ItemStack[] inventoryContents;
	private final ItemStack[] armorContents;
	private final int droppedExp;
	private final int level;
	private final float exp;
	
	/**
	 * @param location The location at which the player died.
	 * @param inventoryContents The contents of the player's inventory (a shallow copy of this parameter is automatically made).
	 * @param armorContents The armour the player was wearing when they died (a shallow copy of this parameter is automatically made).
	 * @param droppedExp The amount of experience that would have dropped at the player's death point.
	 * @param level The player's current level.
	 * @param exp The player's progress towards the next level.
	 */
	public Death(Location location, ItemStack[] inventoryContents, ItemStack[] armorContents, int droppedExp, int level, float exp) {
		this.location = location;
		this.inventoryContents = inventoryContents.clone();
		this.armorContents = armorContents.clone();
		this.droppedExp = droppedExp;
		this.level = level;
		this.exp = exp;
	}
	
	/**
	 * Drops every item held by this instance, at the location where the player died.
	 */
	public void drop() {
		for (ItemStack is : inventoryContents)
			if (is != null && is.getType() != Material.AIR)
				location.getWorld().dropItem(location, is);
		
		for (ItemStack is : armorContents)
			if (is != null && is.getType() != Material.AIR)
				location.getWorld().dropItem(location, is);
		
		if (droppedExp > 0)
			(location.getWorld().spawn(location, ExperienceOrb.class)).setExperience(droppedExp);
	}
	
	/**
	 * Gives every item held by this instance to the given player.
	 * 
	 * @param player The player to give the items and experience to.
	 */
	public void give(final Player player) {
		player.getInventory().setContents(inventoryContents);
		player.getInventory().setArmorContents(armorContents);
		
		// Players cannot be given experience during the respawn event, so schedule this to run after the player has respawned
		Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("KeepItems"), new Runnable() {
			
			public void run() {
				player.setLevel(level);
				player.setExp(exp);
			}
			
		});
	}
	
}
