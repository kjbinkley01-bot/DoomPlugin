package com.delvecompanion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.runelite.client.util.QuantityFormatter;

final class DebriefFormatter
{
	private DebriefFormatter()
	{
	}

	static String formatTicks(int ticks)
	{
		int tenths = ticks * 6;
		int minutes = tenths / 600;
		int seconds = (tenths / 10) % 60;
		return String.format("%d:%02d.%d", minutes, seconds, tenths % 10);
	}

	static String duration(DelveRecord r)
	{
		return r.getGameDuration() != null ? r.getGameDuration() : formatTicks(r.getTicks());
	}

	static String outcome(DelveRecord r)
	{
		switch (r.getOutcome())
		{
			case COMPLETED:
				return "cleared";
			case DIED:
				return "died";
			case LEFT:
				return "left";
			default:
				return "in progress";
		}
	}

	private static List<Map.Entry<DamageSource, Integer>> damageBySize(DelveRecord r)
	{
		return r.getDamageTaken().entrySet().stream()
			.filter(e -> e.getValue() > 0)
			.sorted(Map.Entry.<DamageSource, Integer>comparingByValue(Comparator.reverseOrder()))
			.collect(Collectors.toList());
	}

	static String chatLine(DelveRecord r)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Delve ").append(r.getLevel()).append(' ').append(outcome(r))
			.append(" in ").append(duration(r))
			.append(" | took ").append(r.totalDamage());

		List<Map.Entry<DamageSource, Integer>> damage = damageBySize(r);
		if (!damage.isEmpty())
		{
			sb.append(" (").append(damage.stream()
				.limit(3)
				.map(e -> e.getKey().getLabel() + " " + e.getValue())
				.collect(Collectors.joining(", "))).append(')');
		}
		sb.append(" | wrong-prayer orbs ").append(r.getOrbHitsWrongPrayer());
		sb.append(" | larvae ").append(r.getLarvaeKilled()).append(" killed/")
			.append(r.getLarvaeAbsorbed()).append(" absorbed");
		sb.append(" | acid steps ").append(r.getAcidSteps());
		return sb.toString();
	}

	static String panelHtml(DelveRecord r)
	{
		StringBuilder sb = new StringBuilder("<html>");
		sb.append("<b>Delve ").append(r.getLevel()).append("</b> - ").append(outcome(r))
			.append(" in ").append(duration(r)).append("<br>");

		sb.append("Damage taken: <b>").append(r.totalDamage()).append("</b><br>");
		for (Map.Entry<DamageSource, Integer> e : damageBySize(r))
		{
			sb.append("&nbsp;&nbsp;").append(e.getKey().getLabel()).append(": ").append(e.getValue())
				.append(" (").append(r.getHitsTaken().getOrDefault(e.getKey(), 0)).append(" hits)<br>");
		}

		sb.append("Orbs hit on wrong prayer: ").append(r.getOrbHitsWrongPrayer()).append("<br>");
		if (r.getMissedOrbs() >= 0)
		{
			sb.append("Missed orbs (game counter): ").append(r.getMissedOrbs()).append("<br>");
		}
		sb.append("Larvae: ").append(r.getLarvaeSpawned()).append(" spawned, ")
			.append(r.getLarvaeKilled()).append(" killed, ")
			.append(r.getLarvaeAbsorbed()).append(" absorbed<br>");
		sb.append("Acid steps: ").append(r.getAcidSteps()).append("<br>");
		sb.append("Phases: normal ").append(formatTicks(r.getNormalTicks()))
			.append(", shield ").append(formatTicks(r.getShieldTicks()))
			.append(", burrow ").append(formatTicks(r.getBurrowTicks())).append("<br>");

		if (!r.getSuppliesUsed().isEmpty() || r.getSpecEnergyUsed() > 0)
		{
			List<String> used = new ArrayList<>();
			r.getSuppliesUsed().forEach((name, n) -> used.add(n + "x " + name));
			if (r.getSpecEnergyUsed() > 0)
			{
				used.add(r.getSpecEnergyUsed() + "% spec");
			}
			sb.append("Used: ").append(String.join(", ", used)).append("<br>");
		}
		if (r.getLootValueAfter() > 0)
		{
			sb.append("Unclaimed loot after: ").append(QuantityFormatter.quantityToStackSize(r.getLootValueAfter()))
				.append(" gp<br>");
		}
		return sb.append("</html>").toString();
	}

	/** Risk/reward lines for the burrow hole, as label/value pairs. */
	static List<String[]> riskLines(int completedLevel, long lootValue, boolean uniqueInPile, int targetDelve)
	{
		List<String[]> lines = new ArrayList<>();
		int next = completedLevel + 1;
		lines.add(new String[]{"Unclaimed loot", QuantityFormatter.quantityToStackSize(lootValue) + " gp"});
		if (uniqueInPile)
		{
			lines.add(new String[]{"Unique in pile", "YES - dying loses it"});
		}
		lines.add(new String[]{"Next delve", String.valueOf(next)});
		lines.add(new String[]{"  Unique", DelveOdds.oneIn(DelveOdds.uniqueChance(next))
			+ " (" + DelveOdds.uniquePool(next) + ")"});
		if (DelveOdds.petChance(next) > 0)
		{
			lines.add(new String[]{"  Pet", DelveOdds.oneIn(DelveOdds.petChance(next))});
		}
		lines.add(new String[]{"  Demon tears", String.valueOf(DelveOdds.demonTears(next))});
		if (targetDelve > next)
		{
			lines.add(new String[]{"Delves " + next + "-" + targetDelve,
				DelveOdds.percent(DelveOdds.cumulativeUniqueChance(next, targetDelve)) + " any unique"});
		}
		return lines;
	}
}
