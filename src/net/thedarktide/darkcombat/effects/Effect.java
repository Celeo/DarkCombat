package net.thedarktide.darkcombat.effects;

import java.util.Map;
import java.util.WeakHashMap;

import net.thedarktide.darkcombat.bukkit.DarkCombatPlugin;

import org.bukkit.entity.Player;

public class Effect {

	private DarkCombatPlugin plugin = null;
	private final Map<String, Long> players = new WeakHashMap<String, Long>();
	private final String condition;

	public Effect() {
		condition = null;
	}

	public Effect(String condition) {
		this.condition = condition;
	}

	/**
	 * Add an effect to a player for the specified amount of time and alert
	 * nearby players.
	 * @param player
	 * @param timeInMilliseconds
	 * @return
	 */
	public boolean addEffect(DarkCombatPlugin instance, Player player, Long timeInMilliseconds) {
		if (this.plugin == null)
			this.plugin = instance;
		return addEffect(plugin, player, timeInMilliseconds, true);
	}

	/**
	 * Add an effect to a player for the specified amount of time.
	 * @param player
	 * @param timeInMilliseconds
	 * @param alertNearbyPlayers
	 * @return
	 */
	public boolean addEffect(DarkCombatPlugin instance, Player player, Long timeInMilliseconds, boolean alertNearbyPlayers) {
		if (this.plugin == null)
			this.plugin = instance;
		if (timeInMilliseconds.longValue() < 1)
			return false;
		long until = System.currentTimeMillis() + timeInMilliseconds.longValue();
		String playerName = player.getName();
		if (!players.containsKey(playerName) || players.get(playerName) < until) {
			players.put(playerName, Long.valueOf(until));
			if (condition != null) {
				int timeInSeconds = Long.valueOf(timeInMilliseconds.longValue() / 1000).intValue();
				player.sendMessage("You are " + condition + " for " + timeInSeconds + " seconds.");
				if (alertNearbyPlayers)
					plugin.alertNearbyPlayers(player, playerName + " is " + condition + " for " + timeInSeconds + " seconds.");
			}
			return true;
		}
		return false;
	}

	/**
	 * Removes an effect from a player.
	 * @param player
	 * @return
	 */
	public boolean removeEffect(Player player) {
		return removeEffect(player, false);
	}

	/**
	 * Removes an effect from a player and optionally notifies nearby players.
	 * @param player
	 * @param alertNearbyPlayers
	 * @return
	 */
	public boolean removeEffect(Player player, boolean alertNearbyPlayers) {
		String playerName = player.getName();
		if (!players.containsKey(playerName))
			return false;
		players.remove(playerName);
		if (condition != null && alertNearbyPlayers)
			plugin.alertNearbyPlayers(player, playerName + " is no longer " + condition + ".");
		return true;
	}

	/**
	 * Checks if the effect still applies for the given player. If there is no time remaining, the effect gets removed.
	 * @param player
	 * @return
	 */
	public boolean hasEffect(Player player) {
		if (!players.containsKey(player.getName())) return false;
		if (players.get(player.getName()).longValue() > System.currentTimeMillis()) {
			//if (condition != null) player.sendMessage("You are " + condition + ".");
			return true;
		}
		removeEffect(player, false); // The effect has expired, so remove it
		//Would send message only when a nearby player hit another with an expired effect
		return false;
	}

	/**
	 * Get the remaining time in milliseconds of the effect for the given player. If there is no time remaining, the effect gets removed.
	 * @param player
	 * @return
	 */
	public Long getTimeLeft(Player player) {
		if (!players.containsKey(player.getName())) return 0L;
		long now = System.currentTimeMillis();
		long affectedUntil = players.get(player.getName()).longValue();
		if (affectedUntil > now) {
			return affectedUntil - now;
		}
		removeEffect(player, true); // The effect has expired, so remove it
		return 0L;
	}

	public DarkCombatPlugin getPlugin() {
		return plugin;
	}
}