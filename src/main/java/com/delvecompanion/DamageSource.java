package com.delvecompanion;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum DamageSource
{
	ORB_MAGIC("Orb (magic)"),
	ORB_RANGED("Orb (ranged)"),
	ORB_MELEE("Orb (melee)"),
	FALLING_ROCKS("Falling rocks"),
	SHOCKWAVE("Shockwave"),
	BEAM("Beam"),
	BURROW("Burrow / trample / slam"),
	TONGUE_LASH("Tongue lash (melee)"),
	LARVA("Larva"),
	ACID("Acid / venom"),
	UNKNOWN("Unknown");

	@Getter
	private final String label;

	boolean isOrb()
	{
		return this == ORB_MAGIC || this == ORB_RANGED || this == ORB_MELEE;
	}
}
