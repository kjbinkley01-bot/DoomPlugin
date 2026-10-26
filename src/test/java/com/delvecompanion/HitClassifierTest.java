package com.delvecompanion;

import static org.junit.Assert.assertEquals;
import java.util.Collections;
import java.util.Set;
import net.runelite.api.HitsplatID;
import net.runelite.api.gameval.SpotanimID;
import org.junit.Test;

public class HitClassifierTest
{
	private static HitClassifier.Hit hit(int tick, int amount, int type, boolean onAcid, Set<Integer> anims, boolean burrowed)
	{
		return new HitClassifier.Hit(tick, amount, type, onAcid, anims, burrowed);
	}

	private static HitClassifier.Hit hit(int tick, int amount)
	{
		return hit(tick, amount, HitsplatID.DAMAGE_ME, false, Collections.emptySet(), false);
	}

	@Test
	public void projectileLandingWins()
	{
		HitClassifier c = new HitClassifier();
		c.addLanding(DamageSource.ORB_MAGIC, 100);
		c.lastShockwaveTick = 100;
		assertEquals(DamageSource.ORB_MAGIC, c.classify(hit(100, 30)));
		// Landing is consumed, so the next hit falls through to the shockwave.
		assertEquals(DamageSource.SHOCKWAVE, c.classify(hit(100, 30)));
	}

	@Test
	public void landingWindowIsOneTick()
	{
		HitClassifier c = new HitClassifier();
		c.addLanding(DamageSource.ORB_RANGED, 100);
		assertEquals(DamageSource.UNKNOWN, c.classify(hit(103, 10)));
		assertEquals(DamageSource.ORB_RANGED, c.classify(hit(101, 10)));
	}

	@Test
	public void venomIsAcid()
	{
		HitClassifier c = new HitClassifier();
		assertEquals(DamageSource.ACID, c.classify(hit(5, 6, HitsplatID.VENOM, false, Collections.emptySet(), false)));
	}

	@Test
	public void smallHitOnAcid()
	{
		HitClassifier c = new HitClassifier();
		assertEquals(DamageSource.ACID, c.classify(hit(5, 7, HitsplatID.DAMAGE_ME, true, Collections.emptySet(), false)));
		assertEquals(DamageSource.UNKNOWN, c.classify(hit(5, 20, HitsplatID.DAMAGE_ME, true, Collections.emptySet(), false)));
	}

	@Test
	public void impactSpotAnim()
	{
		HitClassifier c = new HitClassifier();
		Set<Integer> anims = Collections.singleton(SpotanimID.VFX_STANDARD_IMPACT_RANGE);
		assertEquals(DamageSource.ORB_RANGED, c.classify(hit(5, 20, HitsplatID.DAMAGE_ME, false, anims, false)));
	}

	@Test
	public void burrowedBoss()
	{
		HitClassifier c = new HitClassifier();
		assertEquals(DamageSource.BURROW, c.classify(hit(5, 30, HitsplatID.DAMAGE_ME, false, Collections.emptySet(), true)));
	}

	@Test
	public void expiredLandingsAreDropped()
	{
		HitClassifier c = new HitClassifier();
		c.addLanding(DamageSource.FALLING_ROCKS, 10);
		c.expire(20);
		assertEquals(DamageSource.UNKNOWN, c.classify(hit(10, 21)));
	}

	@Test
	public void projectileMapping()
	{
		assertEquals(DamageSource.ORB_MELEE, HitClassifier.sourceForProjectile(SpotanimID.VFX_STANDARD_PROJECTILE_MELEE));
		assertEquals(DamageSource.ORB_MAGIC, HitClassifier.sourceForProjectile(SpotanimID.VFX_ROCK_PROJECTILE_LAUNCH_MAGIC));
		assertEquals(DamageSource.FALLING_ROCKS, HitClassifier.sourceForProjectile(SpotanimID.VFX_ROCK_PROJECTILE_PROJECTILE_ALT03));
		assertEquals(null, HitClassifier.sourceForProjectile(1));
	}
}
