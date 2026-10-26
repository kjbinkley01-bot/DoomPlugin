package com.delvecompanion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

/** Shows the countdowns the player started by hotkey. */
class TimerOverlay extends OverlayPanel
{
	private final DelveCompanionPlugin plugin;

	@Inject
	TimerOverlay(DelveCompanionPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		boolean any = false;
		for (ManualTimer timer : plugin.getTimers())
		{
			if (!timer.isActive())
			{
				continue;
			}
			any = true;
			int left = timer.getRemaining();
			panelComponent.getChildren().add(LineComponent.builder()
				.left(timer.getName())
				.right(left == 0 ? "NOW" : String.valueOf(left))
				.rightColor(left <= 1 ? Color.RED : left <= 3 ? Color.ORANGE : Color.WHITE)
				.build());
		}
		return any ? super.render(graphics) : null;
	}
}
