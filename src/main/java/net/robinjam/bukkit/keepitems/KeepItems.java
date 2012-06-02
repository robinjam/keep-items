package net.robinjam.bukkit.keepitems;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
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
		registerPermissions();
	}

	@Override
	public void onDisable() {
		// When the plugin is disabled, drop all managed items and clear the
		// list
		for (Death death : deaths.values()) {
			death.drop();
		}

		deaths.clear();
	}

	/**
	 * Registers the additional dynamic permissions required by this plugin,
	 * which cannot be included in the plugin.yml file.
	 */
	public void registerPermissions() {
		Map<String, Boolean> children = new HashMap<String, Boolean>();

		// Register keep-items.cause.<type> for each damage cause
		for (DamageCause cause : DamageCause.values()) {
			Permission p = new Permission("keep-items.cause."
					+ cause.name().toLowerCase(),
					"Allows the user to keep their items and experience when they are killed by "
							+ cause.name().toLowerCase(),
					PermissionDefault.FALSE);
			getServer().getPluginManager().addPermission(p);
			children.put(p.getName(), true);
		}

		// Register keep-items.cause.*
		getServer()
				.getPluginManager()
				.addPermission(
						new Permission(
								"keep-items.cause.*",
								"Allows the player to keep their items and experience when they die for any reason.",
								PermissionDefault.TRUE, children));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDeath(final EntityDeathEvent event) {
		// Skip if the entity that died is not a player
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();

		// If the player already has a death on record, drop the items
		Death death = deaths.get(player);
		if (death != null)
			death.drop();

		// Check if the player has permission for this death cause
		String damageCause = player.getLastDamageCause().getCause().name()
				.toLowerCase();
		if (!player.hasPermission("keep-items.cause." + damageCause)) {
			System.out
					.println("Player "
							+ player.getName()
							+ " was killed by "
							+ damageCause
							+ ", but does not have permission to keep their items. Hint: give them the 'keep-items.cause."
							+ damageCause
							+ "' or 'keep-items.cause.*' permission.");
			return;
		}

		ItemStack[] inventoryContents = new ItemStack[0];
		ItemStack[] armorContents = new ItemStack[0];
		int experience = 0;

		if (player.hasPermission("keep-items.items")) {
			inventoryContents = player.getInventory().getContents();
			armorContents = player.getInventory().getArmorContents();

			// Don't drop any items at the death location
			event.getDrops().clear();
		}

		if (player.hasPermission("keep-items.experience")) {
			if (player.hasPermission("keep-items.progress"))
				experience = calcExperience(player.getLevel(), player.getExp());
			else
				experience = calcExperience(player.getLevel());

			// Don't drop any experience at the death location
			event.setDroppedExp(0);
		}

		// Register the death event
		deaths.put(player, new Death(this, player.getLocation(),
				inventoryContents, armorContents, experience));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		Player player = event.getPlayer();

		// If the player has a death on record, drop the items and experience at
		// their respawn location
		Death death = deaths.remove(player);
		if (death != null)
			death.give(player);
	}

	/**
	 * Calculates the total amount of experience required to reach the given
	 * level from level 0.
	 * 
	 * @param level
	 *            The level for which to calculate the required experience
	 * @return The amount of experience required
	 */
	private int calcExperience(int level) {
		return calcExperience(level, 0.0f);
	}

	/**
	 * Calculates the total amount of experience required to reach the given
	 * level from level 0.
	 * 
	 * @param level
	 *            The level for which to calculate the required experience
	 * @param exp
	 *            The player's progress towards the next level
	 * @return The amount of experience required
	 */
	private int calcExperience(int level, double exp) {
		int xp = (int) ((7 + (int) Math.floor(level * 3.5)) * exp);

		while (level > 1) {
			// Calculate the amount of experience required to reach this level
			// from the previous one
			xp += 7 + (int) Math.floor((--level) * 3.5);
		}

		return xp;
	}

}
