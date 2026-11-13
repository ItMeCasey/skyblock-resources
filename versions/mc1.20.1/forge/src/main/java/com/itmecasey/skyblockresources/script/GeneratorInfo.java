package com.itmecasey.skyblockresources.script;

import java.util.Map;

/** A read-only snapshot of one generator, as returned by {@code SkyblockResources.info(level, x, y, z)}. */
public final class GeneratorInfo {
	public final String block;
	/** "resource", "tree" or "crop". */
	public final String kind;
	public final int tier;
	public final String tierName;
	/** The item being made (resource), or the sapling or seed in use; "empty" when none is set. */
	public final String resource;
	/** The soil block of a tree or crop generator; "empty" when none is set. */
	public final String soil;
	/** Cycle progress from 0 to 1. */
	public final double progress;
	/** Ticks one cycle takes. */
	public final double duration;
	/** The side set with the screwdriver (NORTH, EAST, ...), or an empty string. */
	public final String outputDirection;
	/** Item id to total count inside the generator. */
	public final Map<String, Integer> items;

	public GeneratorInfo(String block, String kind, int tier, String tierName, String resource, String soil, double progress, double duration, String outputDirection, Map<String, Integer> items) {
		this.block = block;
		this.kind = kind;
		this.tier = tier;
		this.tierName = tierName;
		this.resource = resource;
		this.soil = soil;
		this.progress = progress;
		this.duration = duration;
		this.outputDirection = outputDirection;
		this.items = items;
	}
}
