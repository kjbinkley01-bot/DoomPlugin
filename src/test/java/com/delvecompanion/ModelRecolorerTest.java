package com.delvecompanion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Set;
import org.junit.Test;

public class ModelRecolorerTest
{
	@Test
	public void parsesRangesAndSingles()
	{
		Set<Integer> ids = ModelRecolorer.parseIds("3388-3390, 3404 ,x, 12-");
		assertEquals(4, ids.size());
		assertTrue(ids.contains(3388));
		assertTrue(ids.contains(3390));
		assertTrue(ids.contains(3404));
	}

	@Test
	public void emptyInput()
	{
		assertTrue(ModelRecolorer.parseIds("").isEmpty());
		assertTrue(ModelRecolorer.parseIds(null).isEmpty());
	}
}
