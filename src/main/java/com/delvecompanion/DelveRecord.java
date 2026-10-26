package com.delvecompanion;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;

/** Everything recorded about one delve level, shown after the level ends. */
@Data
class DelveRecord
{
	enum Outcome
	{
		IN_PROGRESS,
		COMPLETED,
		DIED,
		LEFT
	}

	private int level;
	private Outcome outcome = Outcome.IN_PROGRESS;
	private int startTick;
	private int ticks;
	/** Duration as printed by the game, if the completion message was seen. */
	private String gameDuration;

	private final Map<DamageSource, Integer> damageTaken = new EnumMap<>(DamageSource.class);
	private final Map<DamageSource, Integer> hitsTaken = new EnumMap<>(DamageSource.class);
	private int orbHitsWrongPrayer;
	private int missedOrbs = -1;

	private int larvaeSpawned;
	private int larvaeKilled;
	private int larvaeAbsorbed;
	private int acidSteps;

	private int normalTicks;
	private int shieldTicks;
	private int burrowTicks;

	private final Map<String, Integer> suppliesUsed = new TreeMap<>();
	private int specEnergyUsed;

	private long lootValueAfter;

	void addDamage(DamageSource source, int amount)
	{
		damageTaken.merge(source, amount, Integer::sum);
		hitsTaken.merge(source, 1, Integer::sum);
	}

	int totalDamage()
	{
		return damageTaken.values().stream().mapToInt(Integer::intValue).sum();
	}

	void addSupply(String name)
	{
		suppliesUsed.merge(name, 1, Integer::sum);
	}
}
