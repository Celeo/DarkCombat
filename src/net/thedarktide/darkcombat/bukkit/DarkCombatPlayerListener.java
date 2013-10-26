package net.thedarktide.darkcombat.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author SquallSeeD31, Celeo, epuidokas, KingHarper
 */
public class DarkCombatPlayerListener implements Listener
{

	private final DarkCombatPlugin plugin;

	// @TODO store this list in a file somewhere, so it persists past plugin
	// reloads
	// Why?
	private List<String> cowards = new ArrayList<String>();

	public DarkCombatPlayerListener(DarkCombatPlugin plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Register all DarkCombat player events.
	 */
	public void registerEvents()
	{
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}

	/**
	 * Drop a player's inventory if they have been recently attacked.
	 * 
	 * @param player
	 */
	protected void dropItems(Player player)
	{
		if (plugin.getEffectsManager().getRecentlyAttackedEffect().hasEffect(player))
		{
			ItemStack[] inventory = player.getInventory().getContents();
			ItemStack[] armor = player.getInventory().getArmorContents();
			Location dropLoc = player.getLocation();

			// Wipe their inventory
			clearInventory(player);

			// Add them to the list of cowards to prevent duping
			cowards.add(player.getName());

			// Drop their inventory
			for (ItemStack i : inventory)
			{
				if (i != null && i.getType() != Material.AIR)
				{
					player.getWorld().dropItemNaturally(dropLoc, i);
				}
			}
			for (ItemStack i : armor)
			{
				if (i != null && i.getType() != Material.AIR)
				{
					player.getWorld().dropItemNaturally(dropLoc, i);
				}
			}

			DarkCombatPlugin.Log.info(String.format("[DarkCombat] %s dropped their inventory.", player.getName()));
			plugin.getEffectsManager().getRecentlyAttackedEffect().removeEffect(player);
		}
	}

	/**
	 * Clears a player's inventory and armor
	 * 
	 * @param player
	 */
	protected static void clearInventory(Player player)
	{
		player.getInventory().clear();
		player.getInventory().setBoots(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setHelmet(null);
	}

	/**
	 * If a player is kicked and has been recently attacked, make them drop
	 * their inventory.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerKick(PlayerKickEvent event)
	{
		dropItems(event.getPlayer());
	}

	/**
	 * If a player quits and has been recently attacked, make them drop their
	 * inventory.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		dropItems(event.getPlayer());
	}

	/**
	 * When a player joins, root, enfeeble and make them invulnerable for 5
	 * seconds.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		if (cowards.remove(player.getName()))
		{
			clearInventory(player); // Make sure items don't get duped
			player.setHealth(0); // Cowards don't deserve to live
		}

		// Apply default login effects
		plugin.getEffectsManager().getRootingEffect().addEffect(plugin, player, 5000L);
		plugin.getEffectsManager().getEnfeebleEffect().addEffect(plugin, player, 5000L);
		plugin.getEffectsManager().getInvulnerabilityEffect().addEffect(plugin, player, 5000L);
	}

	/**
	 * If it's been less than 90 seconds since a player's last respawn, root
	 * them for the remaining time. Enfeeble and make them invulnerable for 10
	 * seconds past that.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();
		Long remainingWait = plugin.getEffectsManager().getLastRespawnEffect().getTimeLeft(player);

		if (remainingWait < 0)
			remainingWait = 0L;

		plugin.getEffectsManager().getEnfeebleEffect().addEffect(plugin, player, remainingWait + 10000L, false);
		plugin.getEffectsManager().getInvulnerabilityEffect().addEffect(plugin, player, remainingWait + 10000L, false);
		plugin.getEffectsManager().getLastRespawnEffect().addEffect(plugin, player, 90000L, false);
	}

	/**
	 * Prevent a player from emptying a bucket if they're enfeebled.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.getEffectsManager().getEnfeebleEffect().hasEffect(player))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * If the player is rooted, keep them in one place.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Location from = event.getFrom();
		Location to = event.getTo();

		// Rooting only affects movement in the X and Z directions
		if ((from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ())
				&& plugin.getEffectsManager().getRootingEffect().hasEffect(event.getPlayer()))
		{
			Location newLoc = event.getFrom();
			newLoc.setX(newLoc.getBlockX() + 0.5);
			newLoc.setY(newLoc.getBlockY());
			newLoc.setZ(newLoc.getBlockZ() + 0.5);
			event.setTo(newLoc);
			event.getPlayer().sendMessage("You are rooted.");
			return;
		}
	}

}