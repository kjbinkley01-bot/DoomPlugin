package com.delvecompanion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.Test;

public class DebriefFormatterTest
{
	@Test
	public void formatTicks()
	{
		assertEquals("0:00.0", DebriefFormatter.formatTicks(0));
		assertEquals("0:00.6", DebriefFormatter.formatTicks(1));
		assertEquals("1:00.0", DebriefFormatter.formatTicks(100));
		assertEquals("1:52.2", DebriefFormatter.formatTicks(187));
	}

	@Test
	public void chatLine()
	{
		DelveRecord r = new DelveRecord();
		r.setLevel(4);
		r.setOutcome(DelveRecord.Outcome.COMPLETED);
		r.setGameDuration("1:52.20");
		r.addDamage(DamageSource.ORB_MAGIC, 30);
		r.addDamage(DamageSource.FALLING_ROCKS, 21);
		r.addDamage(DamageSource.ORB_MAGIC, 25);
		r.setLarvaeKilled(6);
		r.setLarvaeAbsorbed(1);
		String line = DebriefFormatter.chatLine(r);
		assertTrue(line, line.startsWith("Delve 4 cleared in 1:52.20 | took 76 (Orb (magic) 55, Falling rocks 21)"));
		assertTrue(line, line.contains("larvae 6 killed/1 absorbed"));
	}

	@Test
	public void riskLines()
	{
		List<String[]> lines = DebriefFormatter.riskLines(3, 1_500_000, true, 8);
		assertEquals("Unclaimed loot", lines.get(0)[0]);
		assertEquals("1.5M gp", lines.get(0)[1]);
		assertEquals("Unique in pile", lines.get(1)[0]);
		assertTrue(lines.stream().anyMatch(l -> l[0].equals("Delves 4-8")));
	}

	@Test
	public void levelEndMessage()
	{
		Matcher m = DelveCompanionPlugin.LEVEL_END.matcher("Delve level: 3 duration: 1:23.40");
		assertTrue(m.find());
		assertEquals("3", m.group(1));
		assertEquals(null, m.group(2));
		assertEquals("1:23.40", m.group(3));

		m = DelveCompanionPlugin.LEVEL_END.matcher("Delve level: 8+ (12) duration: 2:01.20. Personal best: 1:58.00");
		assertTrue(m.find());
		assertEquals("12", m.group(2));
		assertEquals("2:01.20", m.group(3));
	}
}
