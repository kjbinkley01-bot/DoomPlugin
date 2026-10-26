package com.delvecompanion;

/**
 * Reward odds per delve level, from the OSRS Wiki drop tables.
 * Levels above 9 use the 9+ values.
 */
final class DelveOdds
{
	// Index = delve level. 0 = no roll.
	private static final int[] UNIQUE_ONE_IN = {0, 0, 2500, 1000, 450, 270, 255, 240, 210, 180};
	private static final int[] PET_ONE_IN = {0, 0, 0, 0, 0, 0, 1000, 750, 500, 250};
	private static final int[] DEMON_TEARS = {0, 0, 0, 50, 60, 70, 80, 90, 100, 100};

	private DelveOdds()
	{
	}

	private static int index(int level)
	{
		return Math.max(0, Math.min(level, UNIQUE_ONE_IN.length - 1));
	}

	/** Chance of a unique (cloth, Eye of Ayak or Avernic treads) on this level. */
	static double uniqueChance(int level)
	{
		int oneIn = UNIQUE_ONE_IN[index(level)];
		return oneIn == 0 ? 0 : 1.0 / oneIn;
	}

	/** Chance of the Dom pet on this level. */
	static double petChance(int level)
	{
		int oneIn = PET_ONE_IN[index(level)];
		return oneIn == 0 ? 0 : 1.0 / oneIn;
	}

	static int demonTears(int level)
	{
		return DEMON_TEARS[index(level)];
	}

	/** Uniques available on this level, for display. */
	static String uniquePool(int level)
	{
		if (level < 2)
		{
			return "none";
		}
		if (level == 2)
		{
			return "cloth";
		}
		if (level == 3)
		{
			return "cloth, eye";
		}
		return "cloth, eye, treads";
	}

	/** Chance of at least one unique across levels from..to inclusive. */
	static double cumulativeUniqueChance(int from, int to)
	{
		double none = 1;
		for (int level = Math.max(from, 1); level <= to; level++)
		{
			none *= 1 - uniqueChance(level);
		}
		return 1 - none;
	}

	static String oneIn(double chance)
	{
		if (chance <= 0)
		{
			return "-";
		}
		return String.format("1/%,d", Math.round(1 / chance));
	}

	static String percent(double chance)
	{
		return String.format("%.2f%%", chance * 100);
	}
}
