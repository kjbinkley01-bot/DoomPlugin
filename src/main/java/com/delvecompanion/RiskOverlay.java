package com.delvecompanion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/** Risk/reward summary, shown only while the end-of-level (burrow hole) interface is open. */
class RiskOverlay extends OverlayPanel
{
	private final DelveCompanionPlugin plugin;
	private final DelveCompanionConfig config;

	@Inject
	RiskOverlay(DelveCompanionPlugin plugin, DelveCompanionConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showRiskOverlay() || !plugin.isEndLevelInterfaceOpen())
		{
			return null;
		}

		panelComponent.setPreferredSize(new Dimension(230, 0));
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Delve " + plugin.getLastCompletedLevel() + " cleared")
			.color(Color.ORANGE)
			.build());
		for (String[] line : DebriefFormatter.riskLines(plugin.getLastCompletedLevel(), plugin.getLootValue(),
			plugin.isUniqueInPile(), config.targetDelve()))
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left(line[0])
				.right(line[1])
				.rightColor(line[0].startsWith("Unique in pile") ? Color.GREEN : Color.WHITE)
				.build());
		}
		return super.render(graphics);
	}
}
