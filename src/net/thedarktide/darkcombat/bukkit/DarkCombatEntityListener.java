package net.thedarktide.darkcombat.bukkit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.thedarktide.darkcombat.kdr.DamageRecord;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

/**
 * @author epuidokas, Celeo, KingHarper
 */
public class DarkCombatEntityListener implements Listener {

	private final DarkCombatPlugin plugin;
	private static Map<String, Long> lastHit = new HashMap<String, Long>();

	public DarkCombatEntityListener(DarkCombatPlugin instance) {
		this.plugin = instance;
	}

	/**
	 * Register all DarkCombat entity events.
	 */
	public void registerEvents() {
		PluginManager pluginManager = this.plugin.getServer().getPluginManager();
		pluginManager.registerEvents(this, this.plugin);
	}

	/**
	 * Nearly Vanilla combat, just with the reach hack fix and respawn invulnerability.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getEntity().isDead())
			return;

		Entity target = event.getEntity();
		int damage = event.getDamage();
		boolean isInvulnerable = false;
		if (target instanceof Player && plugin.getEffectsManager().getInvulnerabilityEffect().hasEffect((Player) target))
			isInvulnerable = true;
		if (event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent eveEvent = (EntityDamageByEntityEvent) event;
			if (eveEvent.getDamager() instanceof Player)
			{
				Player damager = (Player) eveEvent.getDamager();
				if (plugin.getEffectsManager().getInvulnerabilityEffect().hasEffect(damager))
				{
					damager.sendMessage("You cannot attack while invulnerable.");
					event.setCancelled(true);
					return;
				}
				if (!isValidAttackDistance(damager, target))
					damage = 0;
				if (isInvulnerable)
				{
					damager.sendMessage("Target is invulnerable.");
					event.setCancelled(true);
					return;
				}
				if (target instanceof Player)
				{
					Player t = (Player) target;
					plugin.getEffectsManager().getRecentlyAttackedEffect().addEffect(plugin, t, Long.valueOf(10000));

					// Enforce a .5 second delay between attacks to reduce the effect of autoclickers
					if (lastHit.containsKey(t.getName()) && lastHit.get(t.getName()).longValue() + 500 <= System.currentTimeMillis())
						event.setCancelled(true);
					lastHit.put(t.getName(), Long.valueOf(System.currentTimeMillis()));

					// Arena world-specific effects
					if (!event.isCancelled() && target.getWorld().getName().equals("arenas") && damager.getWorld().getName().equals("arenas")
							&& damager.getItemInHand().getType().equals(Material.DIAMOND_PICKAXE) && damage != 0)
					{
						damage = 8;
					}
				}
			}
			else
			{
				if (isInvulnerable)
				{
					event.setCancelled(true);
					return;
				}
			}
		}
		else
		{
			if (!event.getCause().equals(DamageCause.FALL) && target instanceof Player)
				plugin.getEffectsManager().getRecentlyAttackedEffect().addEffect(plugin, (Player) target, Long.valueOf(10000));
		}
		event.setDamage(damage);
	}

	/**
	 * Checks to make sure that the attack distance between the two players
	 * 	is inside the accepted range.
	 * @param damager
	 * @param target
	 * @return True is valid attack distance 
	 */
	@SuppressWarnings("boxing")
	private static boolean isValidAttackDistance(Player damager, Entity target) {
		// Strict anti-reach against Players; Loose anti-reach against others.
		Double maxDistance = (target instanceof Player) ? 3D : 3.85D;
		Location damagerLoc = damager.getLocation();
		Location damageeLoc = target.getLocation();
		Double x = damagerLoc.getX() - damageeLoc.getX();
		Double y = (damagerLoc.getY() + damager.getEyeHeight()) - (damageeLoc.getY() + 1D);
		Double z = damagerLoc.getZ() - damageeLoc.getZ();
		Double distanceSquared = (x * x) + (y * y) + (z * z);

		// If distance is > max distance, then we'll consider it invalid
		return (distanceSquared < maxDistance * maxDistance);
	}

	/**
	 * Returns a modified damage value 0-20 based on item id. If null is returned, then keep the previous damage value.
	 * @param itemId
	 * @return
	 */
	@SuppressWarnings({ "unused", "boxing" })
	private static Integer getItemDamage(Integer itemId) {
		switch(itemId.intValue()) {
		case 256: //ishovel
			return 6;
		case 257: //ipick
			return 5;
		case 258: //iaxe
			return 10;
		case 261: //bow
			return null;
		case 267: //isword
			return 8;
		case 268: //wsword
			return 4;
		case 269: //wshovel
			return 3;
		case 270: //wpick
			return 3;
		case 271: //waxe
			return 5;
		case 272: //ssword
			return 6;
		case 273: //sshovel
			return 4;
		case 274: //spick
			return 4;
		case 275: //saxe
			return 7;
		case 276: //dsword
			return 10;
		case 277: //dshovel
			return 8;
		case 278: //dpick
			return 8;
		case 279: //daxe
			return 12;
		case 283: //gsword
			return 4;
		case 284: //gshovel
			return 3;
		case 285: //gpick
			return 3;
		case 286: //gaxe
			return 5;
		default:
			return 1;
		}
	}

	/**
	 * Returns rooting time in milliseconds
	 * @param itemId
	 * @return
	 */
	@SuppressWarnings("unused")
	private static Long getRootingTime(Integer itemId) {
		switch(itemId.intValue()) {
		case 256: //ishovel
		case 269: //wshovel
		case 273: //sshovel
		case 277: //dshovel
		case 284: //gshovel
			if ((new Random()).nextBoolean()) return 2000L; // 50% chance of rooting
		default:
			return 0L;
		}
	}

	/**
	 * On death, we remove all combat effects from the player and log their death
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		Player player = null;
		boolean deadPlayer = false;

		if (entity instanceof Player) 
		{
			player = (Player) event.getEntity();
			List<String> removalBlacklist = new LinkedList<String>();
			removalBlacklist.add("lastRespawn");
			plugin.getEffectsManager().removeAllEffects(player, removalBlacklist);
			deadPlayer = true;
		}
		if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent)
		{
			if (((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager() instanceof Player)
			{
				Player damager = (Player) ((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager();
				if (player != null)
				{
					if (plugin.isLoggingKDR)
					{
						this.plugin.getPlayerKDR(damager).logKillAgainstPlayer(player.getDisplayName());
						this.plugin.getPlayerKDR(player).logDeathToPlayer();
					}
				}
				else
				{
					if (plugin.isLoggingKDR)
					{
						String mobName = event.getEntity().toString().toLowerCase();
						if (mobName.contains("craft"))
							mobName.replace("craft", "");
						if (mobName.toLowerCase().contains("wolf"))
							mobName = "wolf";
						this.plugin.getPlayerKDR(damager).logKillAgainstEntity(mobName);
					}
				}
				ItemStack weapon = damager.getItemInHand();
				if (weapon != null && !deadPlayer) {
					switch(weapon.getTypeId()) {
					case 256:
					case 257:
					case 258:
					case 261:
					case 267:
					case 268:
					case 269:
					case 270:
					case 271:
					case 272:
					case 273:
					case 274:
					case 275:
					case 276:
					case 277:
					case 278:
					case 279:
					case 283:
					case 284:
					case 285:
					case 286:
						break;
					default:
						event.getDrops().clear();
					}
				}
			}
			else if (((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager() != null &&
					!(((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager() instanceof Player))
			{
				if (event.getEntity() instanceof Player && plugin.isLoggingKDR)
				{
					String name = ((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager().toString();
					this.plugin.getPlayerKDR((Player) event.getEntity()).logLastDamagerToThis(new DamageRecord(DamageRecord.Type.ENTITY,
							name.contains("Craft") ? name.replace("Craft" , "") : name));
					this.plugin.getPlayerKDR((Player) event.getEntity()).logDeathToEntity();
				}
			}
		}
		else
		{
			if (event.getEntity() instanceof Player && plugin.isLoggingKDR)
				this.plugin.getPlayerKDR((Player) event.getEntity()).logDeathToEnvironment();
		}
	}

	@SuppressWarnings("static-access")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityTame(EntityTameEvent event)
	{
		Entity entity = event.getEntity();
		if (!(entity instanceof Wolf))
			return;
		Wolf wolf = (Wolf) entity;
		String owner = event.getOwner().toString();
		owner = owner.replace("CraftPlayer{name=", "").replace("}", "");
		if (getTameCount(owner, wolf) > plugin.MAX_WOLVES)
		{
			Player player = plugin.getServer().getPlayer(owner); 
			if (player != null && player.isOnline())
				player.sendMessage(plugin.format("&cYou have already tamed the max number of wolves."));
			event.setCancelled(true);
		}
	}

	public static int getTameCount(String playerName, Entity entity)
	{
		int count = 0;
		for (Entity aliveEntity : entity.getWorld().getEntities())
		{
			if (!(aliveEntity instanceof Wolf))
				continue;
			if (((Wolf) aliveEntity).getOwner() != null && ((Wolf) aliveEntity).getOwner().toString().contains(playerName))
				count ++;
		}
		return count;
	}

}