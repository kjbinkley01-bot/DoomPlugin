package com.delvecompanion;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

/** Side panel: risk/reward for the current run, and the debrief history. */
class DelveCompanionPanel extends PluginPanel
{
	private final JLabel riskLabel = new JLabel();
	private final JPanel debriefList = new JPanel();

	DelveCompanionPanel(Runnable onClear)
	{
		setLayout(new BorderLayout(0, 8));
		setBorder(new EmptyBorder(8, 8, 8, 8));

		JPanel top = new JPanel(new BorderLayout(0, 6));
		JLabel title = new JLabel("Delve Companion");
		title.setFont(FontManager.getRunescapeBoldFont());
		top.add(title, BorderLayout.NORTH);

		riskLabel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
			new EmptyBorder(6, 6, 6, 6)));
		top.add(riskLabel, BorderLayout.CENTER);

		JButton clear = new JButton("Clear debriefs");
		clear.addActionListener(e -> onClear.run());
		top.add(clear, BorderLayout.SOUTH);

		debriefList.setLayout(new BoxLayout(debriefList, BoxLayout.Y_AXIS));

		add(top, BorderLayout.NORTH);
		add(debriefList, BorderLayout.CENTER);
		setRisk(null);
	}

	/** Must be called on the Swing thread. */
	void setRisk(List<String[]> lines)
	{
		if (lines == null)
		{
			riskLabel.setText("<html>Not in a delve.<br>Risk/reward shows here once you clear a level.</html>");
			return;
		}
		StringBuilder sb = new StringBuilder("<html>");
		for (String[] line : lines)
		{
			sb.append(line[0].replace("  ", "&nbsp;&nbsp;")).append(": <b>").append(line[1]).append("</b><br>");
		}
		riskLabel.setText(sb.append("</html>").toString());
	}

	/** Must be called on the Swing thread. Newest first. */
	void setDebriefs(List<String> htmlNewestFirst)
	{
		debriefList.removeAll();
		if (htmlNewestFirst.isEmpty())
		{
			JLabel none = new JLabel("No delves recorded yet.");
			none.setFont(none.getFont().deriveFont(Font.ITALIC));
			debriefList.add(none);
		}
		for (String html : htmlNewestFirst)
		{
			JLabel entry = new JLabel(html);
			entry.setFont(FontManager.getRunescapeSmallFont());
			entry.setBorder(BorderFactory.createCompoundBorder(
				new EmptyBorder(0, 0, 6, 0),
				BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR.brighter()),
					new EmptyBorder(4, 4, 4, 4))));
			entry.setAlignmentX(LEFT_ALIGNMENT);
			debriefList.add(entry);
		}
		debriefList.revalidate();
		debriefList.repaint();
	}
}
