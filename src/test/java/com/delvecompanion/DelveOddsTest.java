package com.delvecompanion;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DelveOddsTest
{
	@Test
	public void uniqueChanceFollowsWikiTable()
	{
		assertEquals(0, DelveOdds.uniqueChance(1), 0);
		assertEquals(1.0 / 2500, DelveOdds.uniqueChance(2), 1e-12);
		assertEquals(1.0 / 450, DelveOdds.uniqueChance(4), 1e-12);
		assertEquals(1.0 / 180, DelveOdds.uniqueChance(9), 1e-12);
		assertEquals(1.0 / 180, DelveOdds.uniqueChance(25), 1e-12);
	}

	@Test
	public void cumulativeChance()
	{
		double expected = 1 - (1 - 1.0 / 450) * (1 - 1.0 / 270);
		assertEquals(expected, DelveOdds.cumulativeUniqueChance(4, 5), 1e-12);
		assertEquals(0, DelveOdds.cumulativeUniqueChance(1, 1), 0);
	}

	@Test
	public void demonTears()
	{
		assertEquals(0, DelveOdds.demonTears(2));
		assertEquals(50, DelveOdds.demonTears(3));
		assertEquals(100, DelveOdds.demonTears(8));
		assertEquals(100, DelveOdds.demonTears(14));
	}

	@Test
	public void formatting()
	{
		assertEquals("1/2,500", DelveOdds.oneIn(1.0 / 2500));
		assertEquals("-", DelveOdds.oneIn(0));
	}
}
