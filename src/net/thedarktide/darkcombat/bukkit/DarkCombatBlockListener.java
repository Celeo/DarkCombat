package net.thedarktide.darkcombat.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

/**
 * @authors noahgolm, epuidokas, KingHarper
 */
public class DarkCombatBlockListener implements Listener {

	private final DarkCombatPlugin plugin;

	public DarkCombatBlockListener(DarkCombatPlugin instance) {
		this.plugin = instance;
	}

	/**
	 * Register all DarkCombat block events.
	 */
	public void registerEvents() {
		PluginManager pluginManager = plugin.getServer().getPluginManager();
		pluginManager.registerEvents(this, this.plugin);
	}

	/**
	 * If a player is enfeebled, prevent them placing blocks.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		if (plugin.getEffectsManager().getEnfeebleEffect().hasEffect(player)) {
			event.setBuild(false);
			player.sendMessage("You are enfeebled.");
		}
	}

	/**
	 * If the player is enfeebled, prevent them from breaking blocks.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		if (plugin.getEffectsManager().getEnfeebleEffect().hasEffect(player)) {
			event.setCancelled(true);
			player.sendMessage("You are enfeebled.");
		}
	}

	/**
	 * Update the last attack counter on any block damage event.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockDamage(BlockDamageEvent event) {
		plugin.getEffectsManager().getLastAttackEffect().addEffect(plugin, event.getPlayer(), 3000L);
	}

}