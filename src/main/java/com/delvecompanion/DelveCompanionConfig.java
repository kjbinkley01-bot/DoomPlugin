package com.delvecompanion;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(DelveCompanionConfig.GROUP)
public interface DelveCompanionConfig extends Config
{
	String GROUP = "delvecompanion";

	@ConfigSection(
		name = "Risk / reward",
		description = "Burrow-hole risk and reward information",
		position = 0
	)
	String riskSection = "risk";

	@ConfigSection(
		name = "Debrief",
		description = "Post-delve debrief",
		position = 1
	)
	String debriefSection = "debrief";

	@ConfigSection(
		name = "Manual timers",
		description = "Countdowns you start yourself with a hotkey when you see the mechanic begin",
		position = 2
	)
	String timerSection = "timers";

	@ConfigSection(
		name = "Recolour (grey area)",
		description = "Recolour graphics the game already draws. Off by default; not covered by the rules either way",
		position = 3,
		closedByDefault = true
	)
	String recolorSection = "recolor";

	@ConfigSection(
		name = "Developer",
		description = "Calibration tools",
		position = 4,
		closedByDefault = true
	)
	String devSection = "dev";

	@ConfigItem(
		keyName = "showRiskOverlay",
		name = "Risk overlay at burrow hole",
		description = "Show unclaimed loot value and next-level odds while the end-of-level interface is open",
		section = riskSection,
		position = 0
	)
	default boolean showRiskOverlay()
	{
		return true;
	}

	@Range(min = 2, max = 50)
	@ConfigItem(
		keyName = "targetDelve",
		name = "Target delve",
		description = "Delve level you plan to reach; used for the cumulative unique chance",
		section = riskSection,
		position = 1
	)
	default int targetDelve()
	{
		return 8;
	}

	@ConfigItem(
		keyName = "riskWarningValue",
		name = "Warn above value",
		description = "Chat warning when the unclaimed loot is worth at least this much as you reach the burrow hole (0 = off)",
		section = riskSection,
		position = 2
	)
	default int riskWarningValue()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "chatDebrief",
		name = "Chat debrief",
		description = "Print a one-line debrief to chat after each delve level",
		section = debriefSection,
		position = 0
	)
	default boolean chatDebrief()
	{
		return true;
	}

	@Range(min = 1, max = 200)
	@ConfigItem(
		keyName = "maxDebriefs",
		name = "Debriefs kept in panel",
		description = "How many delve debriefs the side panel keeps",
		section = debriefSection,
		position = 1
	)
	default int maxDebriefs()
	{
		return 30;
	}

	@ConfigItem(
		keyName = "saveToFile",
		name = "Save debriefs to file",
		description = "Append each debrief as a JSON line to .runelite/delve-companion/delves.jsonl",
		section = debriefSection,
		position = 2
	)
	default boolean saveToFile()
	{
		return true;
	}

	@ConfigItem(
		keyName = "shockwaveHotkey",
		name = "Shockwave timer key",
		description = "Press when volatile earth spawns to start the shockwave countdown",
		section = timerSection,
		position = 0
	)
	default Keybind shockwaveHotkey()
	{
		return Keybind.NOT_SET;
	}

	@Range(min = 1, max = 50)
	@Units(Units.TICKS)
	@ConfigItem(
		keyName = "shockwaveTicks",
		name = "Shockwave countdown",
		description = "Ticks from volatile earth spawning to the first shockwave (wiki: 20)",
		section = timerSection,
		position = 1
	)
	default int shockwaveTicks()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "rockHotkey",
		name = "Rock throw timer key",
		description = "Press when the Doom throws its rock to start the rock countdown",
		section = timerSection,
		position = 2
	)
	default Keybind rockHotkey()
	{
		return Keybind.NOT_SET;
	}

	@Range(min = 1, max = 20)
	@Units(Units.TICKS)
	@ConfigItem(
		keyName = "rockTicks",
		name = "Rock countdown",
		description = "Ticks from the rock being thrown to it exploding (wiki: 7)",
		section = timerSection,
		position = 3
	)
	default int rockTicks()
	{
		return 7;
	}

	@ConfigItem(
		keyName = "recolorRockShadows",
		name = "Recolour rock shadows",
		description = "Recolour the falling-rock graphics the game draws (including their shadows)",
		section = recolorSection,
		position = 0
	)
	default boolean recolorRockShadows()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "rockShadowColor",
		name = "Rock shadow colour",
		description = "Colour for the rock graphics",
		section = recolorSection,
		position = 1
	)
	default Color rockShadowColor()
	{
		return new Color(255, 105, 180);
	}

	@ConfigItem(
		keyName = "rockShadowMode",
		name = "Rock recolour mode",
		description = "Whole graphic, or only its dark faces (the shadow part, if the shadow is part of the rock model)",
		section = recolorSection,
		position = 2
	)
	default ModelRecolorer.Mode rockShadowMode()
	{
		return ModelRecolorer.Mode.WHOLE_GRAPHIC;
	}

	@ConfigItem(
		keyName = "rockShadowIds",
		name = "Rock graphic IDs",
		description = "Graphic/projectile IDs to recolour, e.g. 3388-3403, 3404. Use Developer > Log Doom events to find the shadow's ID",
		section = recolorSection,
		position = 3
	)
	default String rockShadowIds()
	{
		return "3388-3403";
	}

	@ConfigItem(
		keyName = "recolorAcid",
		name = "Recolour acid",
		description = "Recolour the acid blood pools the game draws",
		section = recolorSection,
		position = 4
	)
	default boolean recolorAcid()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "acidColor",
		name = "Acid colour",
		description = "Colour for acid pools",
		section = recolorSection,
		position = 5
	)
	default Color acidColor()
	{
		return new Color(0, 255, 255);
	}

	@ConfigItem(
		keyName = "debugLogging",
		name = "Log Doom events",
		description = "Log Doom animations, graphics, projectiles and hit classification to the client log, for checking IDs",
		section = devSection,
		position = 0
	)
	default boolean debugLogging()
	{
		return false;
	}
}
