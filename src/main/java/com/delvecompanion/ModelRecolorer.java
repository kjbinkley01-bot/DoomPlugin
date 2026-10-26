package com.delvecompanion;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.JagexColor;
import net.runelite.api.Model;

/**
 * Recolours models the game is already drawing. It changes how an existing graphic looks;
 * it never adds a marker, moves anything, or shows something earlier than the game does.
 */
final class ModelRecolorer
{
	/** Faces at or below this luminance (0-127) count as "dark" for the dark-faces-only mode. */
	static final int DARK_LUMINANCE = 40;

	private ModelRecolorer()
	{
	}

	enum Mode
	{
		WHOLE_GRAPHIC,
		DARK_FACES_ONLY
	}

	static short toHsl(Color color)
	{
		return JagexColor.rgbToHSL(color.getRGB() & 0xFFFFFF, 1.0);
	}

	static void recolor(Model model, short hsl, Mode mode)
	{
		if (model == null)
		{
			return;
		}
		int[] c1 = model.getFaceColors1();
		int[] c2 = model.getFaceColors2();
		int[] c3 = model.getFaceColors3();
		if (c1 == null || c2 == null || c3 == null)
		{
			return;
		}
		int faces = Math.min(c1.length, Math.min(c2.length, c3.length));
		for (int i = 0; i < faces; i++)
		{
			// -2 marks a hidden face.
			if (c3[i] == -2)
			{
				continue;
			}
			if (mode == Mode.DARK_FACES_ONLY && JagexColor.unpackLuminance((short) c1[i]) > DARK_LUMINANCE)
			{
				continue;
			}
			c1[i] = hsl;
			// -1 marks a flat-shaded face, which only uses the first colour.
			if (c3[i] != -1)
			{
				c2[i] = hsl;
				c3[i] = hsl;
			}
		}
	}

	/** Parses "3388-3403, 3404" into a set of IDs. Bad entries are skipped. */
	static Set<Integer> parseIds(String text)
	{
		Set<Integer> ids = new HashSet<>();
		if (text == null)
		{
			return ids;
		}
		for (String part : text.split(","))
		{
			String p = part.trim();
			if (p.isEmpty())
			{
				continue;
			}
			try
			{
				int dash = p.indexOf('-');
				if (dash > 0)
				{
					int from = Integer.parseInt(p.substring(0, dash).trim());
					int to = Integer.parseInt(p.substring(dash + 1).trim());
					for (int id = from; id <= to && id - from < 1000; id++)
					{
						ids.add(id);
					}
				}
				else
				{
					ids.add(Integer.parseInt(p));
				}
			}
			catch (NumberFormatException ignored)
			{
				// skip
			}
		}
		return ids;
	}
}
