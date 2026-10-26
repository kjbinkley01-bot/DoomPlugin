package com.delvecompanion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.Value;
import net.runelite.api.HitsplatID;
import net.runelite.api.gameval.SpotanimID;

/**
 * Works out what caused a hit on the player, from the boss activity seen around the same tick.
 * Results are only used in the debrief after a delve ends; nothing here is shown during the fight.
 */
class HitClassifier
{
	private static final int NEVER = Integer.MIN_VALUE / 2;

	@Value
	static class Hit
	{
		int tick;
		int amount;
		int hitsplatType;
		boolean onAcid;
		Set<Integer> playerSpotAnims;
		boolean bossBurrowed;
	}

	@Value
	private static class Landing
	{
		DamageSource source;
		int tick;
	}

	private final List<Landing> landings = new ArrayList<>();

	int lastShockwaveTick = NEVER;
	int lastBeamTick = NEVER;
	int lastBurrowHitTick = NEVER;
	int lastTongueLashTick = NEVER;
	int lastBossHealTick = NEVER;
	int lastLarvaDeathNearPlayerTick = NEVER;
	int lastRockImpactTick = NEVER;

	void reset()
	{
		landings.clear();
		lastShockwaveTick = NEVER;
		lastBeamTick = NEVER;
		lastBurrowHitTick = NEVER;
		lastTongueLashTick = NEVER;
		lastBossHealTick = NEVER;
		lastLarvaDeathNearPlayerTick = NEVER;
		lastRockImpactTick = NEVER;
	}

	/** Record a projectile aimed at the player and the tick it should land. */
	void addLanding(DamageSource source, int tick)
	{
		landings.add(new Landing(source, tick));
	}

	/** Drop landings that are too old to match any hit. */
	void expire(int currentTick)
	{
		landings.removeIf(l -> l.getTick() < currentTick - 2);
	}

	static DamageSource sourceForProjectile(int projectileId)
	{
		switch (projectileId)
		{
			case SpotanimID.VFX_STANDARD_PROJECTILE_MELEE:
				return DamageSource.ORB_MELEE;
			case SpotanimID.VFX_STANDARD_PROJECTILE_MAGIC:
			case SpotanimID.VFX_ROCK_PROJECTILE_LAUNCH_MAGIC:
			case SpotanimID.VFX_ROCK_PROJECTILE_SPLIT_MAGIC:
				return DamageSource.ORB_MAGIC;
			case SpotanimID.VFX_STANDARD_PROJECTILE_RANGE:
			case SpotanimID.VFX_ROCK_PROJECTILE_LAUNCH_RANGE:
			case SpotanimID.VFX_ROCK_PROJECTILE_SPLIT_RANGE:
				return DamageSource.ORB_RANGED;
			default:
				if (projectileId >= SpotanimID.VFX_ROCK_PROJECTILE_PROJECTILE01
					&& projectileId <= SpotanimID.VFX_ROCK_PROJECTILE_PROJECTILE_ALT08)
				{
					return DamageSource.FALLING_ROCKS;
				}
				return null;
		}
	}

	DamageSource classify(Hit hit)
	{
		int t = hit.getTick();

		if (hit.getHitsplatType() == HitsplatID.VENOM || hit.getHitsplatType() == HitsplatID.POISON)
		{
			return DamageSource.ACID;
		}

		DamageSource landed = takeLanding(t);
		if (landed != null)
		{
			return landed;
		}

		Set<Integer> anims = hit.getPlayerSpotAnims();
		if (anims.contains(SpotanimID.VFX_STANDARD_IMPACT_MAGIC))
		{
			return DamageSource.ORB_MAGIC;
		}
		if (anims.contains(SpotanimID.VFX_STANDARD_IMPACT_RANGE))
		{
			return DamageSource.ORB_RANGED;
		}
		if (anims.contains(SpotanimID.VFX_STANDARD_IMPACT_MELEE))
		{
			return DamageSource.ORB_MELEE;
		}
		if (anims.contains(SpotanimID.VFX_BEAM_IMPACT_01) || within(lastBeamTick, t, 2))
		{
			return DamageSource.BEAM;
		}
		if (within(lastShockwaveTick, t, 1))
		{
			return DamageSource.SHOCKWAVE;
		}
		if (hit.isBossBurrowed() || within(lastBurrowHitTick, t, 1))
		{
			return DamageSource.BURROW;
		}
		if (within(lastBossHealTick, t, 1) || within(lastLarvaDeathNearPlayerTick, t, 1))
		{
			return DamageSource.LARVA;
		}
		if (within(lastTongueLashTick, t, 1))
		{
			return DamageSource.TONGUE_LASH;
		}
		if (hit.isOnAcid() && hit.getAmount() <= 7)
		{
			return DamageSource.ACID;
		}
		if (within(lastRockImpactTick, t, 1))
		{
			return DamageSource.FALLING_ROCKS;
		}
		return DamageSource.UNKNOWN;
	}

	private DamageSource takeLanding(int tick)
	{
		Landing best = null;
		for (Landing l : landings)
		{
			int d = Math.abs(l.getTick() - tick);
			if (d <= 1 && (best == null || d < Math.abs(best.getTick() - tick)))
			{
				best = l;
			}
		}
		if (best == null)
		{
			return null;
		}
		for (Iterator<Landing> it = landings.iterator(); it.hasNext(); )
		{
			if (it.next() == best)
			{
				it.remove();
				break;
			}
		}
		return best.getSource();
	}

	private static boolean within(int eventTick, int tick, int window)
	{
		return Math.abs(tick - eventTick) <= window;
	}
}
