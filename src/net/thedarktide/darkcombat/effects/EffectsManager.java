package net.thedarktide.darkcombat.effects;

import java.util.List;

import org.bukkit.entity.Player;

public class EffectsManager {

	private final Effect enfeeble = new Effect("enfeebled");
	private final Effect rooting = new Effect("rooted");
	private final Effect invulnerability = new Effect("invulnerable");
	private final Effect recentlyAttacked = new Effect();
	private final Effect lastAttack = new Effect();
	private final Effect lastRespawn = new Effect();

	public Effect getEnfeebleEffect() {
		return enfeeble;
	}

	public Effect getRootingEffect() {
		return rooting;
	}

	public Effect getInvulnerabilityEffect() {
		return invulnerability;
	}

	public Effect getRecentlyAttackedEffect() {
		return recentlyAttacked;
	}

	public Effect getLastAttackEffect() {
		return lastAttack;
	}

	public Effect getLastRespawnEffect() {
		return lastRespawn;
	}

	/**
	 * Remove all effects with optional blacklist.
	 * @param player
	 * @param blacklist list of effects to not remove
	 */
	// @TODO: switch to using enum to represent blacklist
	public void removeAllEffects(Player player, List<String> blacklist) {
		if (!blacklist.contains("enfeeble")) getEnfeebleEffect().removeEffect(player);
		if (!blacklist.contains("rooting")) getRootingEffect().removeEffect(player);
		if (!blacklist.contains("invulnerability")) getInvulnerabilityEffect().removeEffect(player);
		if (!blacklist.contains("recentlyAttacked")) getRecentlyAttackedEffect().removeEffect(player);
		if (!blacklist.contains("lastAttack")) getLastAttackEffect().removeEffect(player);
		if (!blacklist.contains("lastRespawn")) getLastRespawnEffect().removeEffect(player);
	}

	public void removeAllEffects(Player player) {
		getEnfeebleEffect().removeEffect(player);
		getRootingEffect().removeEffect(player);
		getInvulnerabilityEffect().removeEffect(player);
		getRecentlyAttackedEffect().removeEffect(player);
		getLastAttackEffect().removeEffect(player);
		getLastRespawnEffect().removeEffect(player);
	}

}