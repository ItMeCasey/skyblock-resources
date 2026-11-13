package com.itmecasey.skyblockresources.script;

/**
 * What a script listener receives. Plain strings and numbers only, so any script engine can read it.
 * {@code item} and {@code count} are set for "produced"; {@code tier} is the tier after the change for "upgraded".
 */
public final class GeneratorEvent {
	public final String name;
	/** "resource", "tree" or "crop". */
	public final String kind;
	/** 0 (wooden) to 4 (netherite). */
	public final int tier;
	public final String tierName;
	public final String dimension;
	public final int x;
	public final int y;
	public final int z;
	public final String item;
	public final int count;

	public GeneratorEvent(String name, String kind, int tier, String tierName, String dimension, int x, int y, int z, String item, int count) {
		this.name = name;
		this.kind = kind;
		this.tier = tier;
		this.tierName = tierName;
		this.dimension = dimension;
		this.x = x;
		this.y = y;
		this.z = z;
		this.item = item;
		this.count = count;
	}
}
