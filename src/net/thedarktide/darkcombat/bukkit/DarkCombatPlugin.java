package net.thedarktide.darkcombat.bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.thedarktide.darkcombat.effects.EffectsManager;
import net.thedarktide.darkcombat.kdr.PlayerKDR;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.darktidegames.celeo.DarkVanish;

/**
 * DarkCombat
 * @authors Atomjay, epuidokas, Celeo
 */
public class DarkCombatPlugin extends JavaPlugin {

	public final static Logger Log = Logger.getLogger("Minecraft");

	public final EffectsManager effectsManager = new EffectsManager();
	public Map<String, PlayerKDR> playerKDRs = new HashMap<String, PlayerKDR>();

	private DarkVanish DarkVanishPlugin;

	private static DarkCombatPlugin instance;

	public boolean isLoggingKDR = false;
	public static boolean debugging = false;
	public final int MAX_WOLVES = 4;

	@Override
	public void onDisable() {
		log("Successfully disabled.");
	}

	@Override
	public void onEnable() {
		setupPlugins();
		instance = this;
		(new DarkCombatEntityListener(this)).registerEvents();
		(new DarkCombatPlayerListener(this)).registerEvents();
		(new DarkCombatBlockListener(this)).registerEvents();
		log(String.format("Successfully enabled version %s.", this.getDescription().getVersion()));
		if (!this.getDataFolder().exists())
			this.getDataFolder().mkdirs();
		if (isLoggingKDR)
			for(Player p : this.getServer().getOnlinePlayers())
				this.getPlayerKDR(p);
	}

	@SuppressWarnings("static-method")
	public void log(String message)
	{
		Log.info("[DarkCombat] " + message);
	}

	private void setupPlugins()
	{
		Plugin temp = getServer().getPluginManager().getPlugin("DarkVanish");
		if (temp != null)
		{
			DarkVanishPlugin = (DarkVanish) temp;
			log("Connected to DarkVanish.");
		}
		else
			log("Could not connect to DarkVanish.");
	}

	public boolean isVanished(Player player)
	{
		try
		{
			if (DarkVanishPlugin != null && DarkVanishPlugin.isVanished(player))
				return true;
		}
		catch (Exception e) { }
		return false;
	}

	public EffectsManager getEffectsManager() {
		return effectsManager;
	}

	public PlayerKDR getPlayerKDR(Player player)
	{
		if (playerKDRs == null || playerKDRs.isEmpty())
			playerKDRs = new HashMap<String, PlayerKDR>();
		if(playerKDRs.containsKey(player.getName()))
			return playerKDRs.get(player.getName());
		PlayerKDR kdr = new PlayerKDR(player, this);
		playerKDRs.put(player.getName(), kdr);
		return kdr;
	}

	public void checkList()
	{
		if (playerKDRs == null || playerKDRs.isEmpty())
			for (Player p : this.getServer().getOnlinePlayers())
				getPlayerKDR(p);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (!(sender instanceof Player))
			return false;
		if (!command.getName().equalsIgnoreCase("effects") || args.length < 1)
			return false;

		Player temp = (Player) sender;
		if (args[0].equalsIgnoreCase("clear"))
		{
			if (temp.hasPermission("sudo.mod"))
			{
				getEffectsManager().removeAllEffects(temp);
				temp.sendMessage(format("&7Effects cleared."));
				Log.info("[DarkCombat] " + temp.getDisplayName() + " has cleared their effects!");
			}
			return true;
		}

		OfflinePlayer offlinePlayer = getServer().getOfflinePlayer(args[0]);
		if (offlinePlayer == null || !offlinePlayer.isOnline()) {
			sender.sendMessage(args[0] + " is not online.");
			return true;
		}

		Player player = offlinePlayer.getPlayer();
		sender.sendMessage("Invulnerability time: " + getEffectsManager().getInvulnerabilityEffect().getTimeLeft(player));
		sender.sendMessage("Enfeeble time: " + getEffectsManager().getEnfeebleEffect().getTimeLeft(player));
		sender.sendMessage("Rooting time: " + getEffectsManager().getRootingEffect().getTimeLeft(player));

		return true;
	}

	/**
	 * Alert all players within 10 blocks of the given player.
	 * @param player
	 */
	public void alertNearbyPlayers(Player player, String message) {
		for (Entity nearbyEntity : player.getNearbyEntities(10D, 10D, 10D))
			if (nearbyEntity instanceof Player)
				if (isVanished((Player) nearbyEntity))
					((Player) nearbyEntity).sendMessage(message);
	}

	public static String format(String raw)
	{
		if (!raw.contains("&"))
			return raw;
		return raw.replaceAll("&", "\u00A7");
	}

	public DarkVanish getDarkVanishPlugin() {
		return DarkVanishPlugin;
	}

	public static DarkCombatPlugin getInstance() {
		return instance;
	}

}