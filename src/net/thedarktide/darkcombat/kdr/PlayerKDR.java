package net.thedarktide.darkcombat.kdr;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.thedarktide.darkcombat.bukkit.DarkCombatPlugin;

import org.bukkit.entity.Player;

/**
 * Object for keeping track of diverse data regarding a player's life and death.
 * @author Celeo
 */
public class PlayerKDR
{

	//================================
	//			VARS
	//================================

	private final Player player;
	private final DarkCombatPlugin plugin;

	private Map<String, Integer> playerKills = new HashMap<String, Integer>();
	private Map<String, Integer> entityKills = new HashMap<String, Integer>();
	private Map<String, Integer> playersKilledThis = new HashMap<String, Integer>();
	private Map<String, Integer> entitiesKilledThis = new HashMap<String, Integer>();
	private Map<String, Integer> environmentalDeaths = new HashMap<String, Integer>();

	private DamageRecord lastDamagerToThis = null;
	private DamageRecord lastDamagedByThis = null;

	private final File file;
	private List<String> lines = new ArrayList<String>();

	//================================
	//			CONSTRUCTOR
	//================================

	public PlayerKDR(Player player, DarkCombatPlugin plugin)
	{
		this.player = player;
		this.plugin = plugin;
		this.file = new File(this.plugin.getDataFolder(), player.getDisplayName() + ".txt");
		loadData();
	}

	//================================
	//			GET
	//================================

	public Player getPlayer()
	{
		return player;
	}

	public DarkCombatPlugin getPlugin()
	{
		return plugin;
	}

	public Map<String, Integer> getPlayerKills()
	{
		return playerKills;
	}

	public Map<String, Integer> getEntityKills()
	{
		return entityKills;
	}

	public Map<String, Integer> getPlayersKilledThis()
	{
		return playersKilledThis;
	}

	public Map<String, Integer> getEntitesKilledThis()
	{
		return entitiesKilledThis;
	}

	public Map<String, Integer> getEnvironmentalDeaths()
	{
		return environmentalDeaths;
	}

	public DamageRecord getLastDamagerToThis()
	{
		return lastDamagerToThis;
	}

	public DamageRecord getLastDamagedByThis()
	{
		return lastDamagedByThis;
	}

	public Integer getTotalKillCount()
	{
		return playerKills.size();
	}

	public Integer getTotalDeathCount()
	{
		return playersKilledThis.size() + entitiesKilledThis.size() + environmentalDeaths.size();
	}

	//================================
	//			LOGGING
	//================================

	public void logLastDamagerToThis(DamageRecord record)
	{
		this.lastDamagerToThis = record;
	}

	public void logLastDamagedByThis(DamageRecord record)
	{
		this.lastDamagedByThis = record;
	}

	public void logDeathToEntity()
	{
		//TODO: Better error checking here for NPE
		if (entitiesKilledThis == null || lastDamagerToThis == null)
			return;
		if (entitiesKilledThis.containsKey(lastDamagerToThis.identifier))
			entitiesKilledThis.put(lastDamagerToThis.identifier, entitiesKilledThis.get(lastDamagerToThis.identifier) + 1);
		else
			entitiesKilledThis.put(lastDamagerToThis.identifier, 1);
		lastDamagerToThis = null;
		writeToFile();
	}

	public void logDeathToPlayer()
	{
		//TODO: Better error checking here for NPE
		if (playersKilledThis == null || lastDamagerToThis == null)
			return;
		if (playersKilledThis.containsKey(lastDamagerToThis.identifier))
			playersKilledThis.put(lastDamagerToThis.identifier, playersKilledThis.get(lastDamagerToThis.identifier) + 1);
		else
			playersKilledThis.put(lastDamagerToThis.identifier, 1);
		lastDamagerToThis = null;
		writeToFile();
	}

	public void logDeathToEnvironment()
	{
		//TODO: Better error checking here for NPE
		if (environmentalDeaths == null || lastDamagerToThis == null)
			return;
		if (environmentalDeaths.containsKey(lastDamagerToThis.identifier))
			environmentalDeaths.put(lastDamagerToThis.identifier, environmentalDeaths.get(lastDamagerToThis.identifier) + 1);
		else
			environmentalDeaths.put(lastDamagerToThis.identifier, 1);
		lastDamagerToThis = null;
		writeToFile();
	}

	public void logKillAgainstEntity(String entityName)
	{
		//TODO: Better error checking here for NPE
		if (entityKills == null)
			return;
		if (entityKills.containsKey(entityName))
			entityKills.put(entityName, entityKills.get(entityName) + 1);
		else
			entityKills.put(entityName, 1);
		lastDamagedByThis = null;
		writeToFile();
	}

	public void logKillAgainstPlayer(String playerName)
	{
		//TODO: Better error checking here for NPE
		if (playerKills == null)
			return;
		if (playerKills.containsKey(playerName))
			playerKills.put(playerName, playerKills.get(playerName) + 1);
		else
			playerKills.put(playerName, 1);
		lastDamagedByThis = null;
		writeToFile();
	}

	//================================
	//			DATA
	//================================

	public void makeFile()
	{
		if (!new File(this.plugin.getDataFolder(), this.player.getDisplayName() + ".txt").exists())
		{
			try
			{
				this.plugin.getDataFolder().createNewFile();
				file.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void writeToFile()
	{
		if (!plugin.isLoggingKDR)
			return;
		makeFile();
		storeData();
		try
		{
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for (Object line : lines)
				writer.write(String.valueOf(line) + "\n");
					writer.close();
		}
		catch (IOException e)
		{
			makeFile();
			e.printStackTrace();
		}
	}

	public void loadData()
	{
		if (!plugin.isLoggingKDR)
			return;
		makeFile();
		String line = "";
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null)
				lines.add(line);
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		getData();
	}

	public void storeData()
	{
		int totalkills = 0;
		int totaldeaths = 0;
		lines = new ArrayList<String>();
		lines.add("#PLAYER_KILLS");
		for (String s : playerKills.keySet())
		{
			lines.add(String.valueOf(s + ":" + playerKills.get(s)));
			totalkills += playerKills.get(s).intValue();
		}
		lines.add("#ENTITY_KILLS");
		for (String s : entityKills.keySet())
		{
			lines.add(String.valueOf(s + ":" + entityKills.get(s)));
			totalkills += entityKills.get(s).intValue();
		}	
		lines.add("#PLAYERS_KILLED_THIS");
		for (String s : playersKilledThis.keySet())
		{
			lines.add(String.valueOf(s + ":" + playersKilledThis.get(s)));
			totaldeaths += playersKilledThis.get(s).intValue();
		}
		lines.add("#ENTITIES_KILLED_THIS");
		for (String s : entitiesKilledThis.keySet())
		{
			lines.add(String.valueOf(s + ":" + entitiesKilledThis.get(s)));
			totaldeaths += entitiesKilledThis.get(s).intValue();
		}
		lines.add("#ENVIRONMENTAL_DEATHS");
		for (String s : environmentalDeaths.keySet())
		{
			lines.add(String.valueOf(s + ":" + environmentalDeaths.get(s)));
			totaldeaths += environmentalDeaths.get(s).intValue();
		}
		lines.add("#TOTAL_KILLS");
		lines.add(String.valueOf(totalkills));
		lines.add("#TOTAL_DEATHS");
		lines.add(String.valueOf(totaldeaths));
	}

	public void getData()
	{
		String stage = null;
		String[] split = null;
		if (lines == null || lines.isEmpty())
			return;
		for (String str : lines)
		{
			if (str.startsWith("#"))
				stage = str.replace("#", "");
			else
			{
				split = str.split(":");
				if (stage == null)
					continue;
				if (stage.equalsIgnoreCase("PLAYER_KILLS"))
					playerKills.put(split[0], Integer.valueOf(split[1]));
				else if (stage.equalsIgnoreCase("ENTITY_KILLS"))
					entityKills.put(split[0], Integer.valueOf(split[1]));
				else if (stage.equalsIgnoreCase("PLAYERS_KILLED_THIS"))
					playersKilledThis.put(split[0], Integer.valueOf(split[1]));
				else if (stage.equalsIgnoreCase("ENTITIES_KILLED_THIS"))
					entitiesKilledThis.put(split[0], Integer.valueOf(split[1]));
				else if (stage.equalsIgnoreCase("ENVIRONMENTAL_DEATHS"))
					environmentalDeaths.put(split[0], Integer.valueOf(split[1]));
				else
					plugin.log("Error with the loading of a " + this.player.getDisplayName() + "'s file.");
			}
		}
	}

}