package com.itmecasey.skyblockresources.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * 1.21.1 has no getXOr accessors on CompoundTag (26.x does). These helpers keep the same
 * "missing or wrong type gives the default" behavior so the rest of the code reads the same on both versions.
 */
public final class NbtCompat {
	private NbtCompat() {
	}

	public static String getStringOr(CompoundTag tag, String key, String def) {
		return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : def;
	}

	public static double getDoubleOr(CompoundTag tag, String key, double def) {
		return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getDouble(key) : def;
	}

	public static int getIntOr(CompoundTag tag, String key, int def) {
		return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : def;
	}

	public static boolean getBooleanOr(CompoundTag tag, String key, boolean def) {
		return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(key) : def;
	}

	public static CompoundTag getCompoundOrEmpty(CompoundTag tag, String key) {
		return tag.contains(key, Tag.TAG_COMPOUND) ? tag.getCompound(key) : new CompoundTag();
	}
}
