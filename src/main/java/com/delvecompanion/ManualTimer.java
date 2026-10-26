package com.delvecompanion;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** A tick countdown started by the player pressing a hotkey. */
@RequiredArgsConstructor
class ManualTimer
{
	@Getter
	private final String name;
	@Getter
	private int remaining = -1;

	void start(int ticks)
	{
		remaining = ticks;
	}

	void tick()
	{
		if (remaining >= 0)
		{
			remaining--;
		}
	}

	boolean isActive()
	{
		return remaining >= 0;
	}

	void stop()
	{
		remaining = -1;
	}
}
