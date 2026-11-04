/**
 * Copyright (c) 2026 zSemper
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the MIT License
 */
package com.itmecasey.skyblockresources.network;

import com.itmecasey.skyblockresources.compat.NbtCompat;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Direction;

import java.util.ArrayList;

import java.io.File;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public final class NbtArrayLists {
	public static ArrayList<Object> loadGlobalWorld(ListTag tag, HolderGetter<Block> lookup) {
		return loadGlobal(tag, lookup);
	}

	public static ArrayList<Object> loadGlobalMap(ListTag tag, HolderGetter<Block> lookup) {
		return loadGlobal(tag, lookup);
	}

	private static ArrayList<Object> loadGlobal(ListTag list, HolderGetter<Block> lookup) {
		ArrayList<Object> objects = new ArrayList<>();
		for (Tag tag : list) {
			CompoundTag entry = (CompoundTag) tag;
			Object value = switch (NbtCompat.getStringOr(entry, "Type", "")) {
				case "BlockState" -> NbtUtils.readBlockState(lookup, NbtCompat.getCompoundOrEmpty(entry, "Value"));
				case "Direction" -> Direction.from3DDataValue(NbtCompat.getIntOr(entry, "Value", 0));
				case "ItemStack" -> ItemStack.of(NbtCompat.getCompoundOrEmpty(entry, "Value"));
				case "Boolean" -> NbtCompat.getBooleanOr(entry, "Value", false);
				case "Double" -> NbtCompat.getDoubleOr(entry, "Value", 0.0d);
				case "String" -> NbtCompat.getStringOr(entry, "Value", "");
				case "Vec3" -> {
					ListTag vec3List = entry.getList("Value", Tag.TAG_DOUBLE);
					yield new Vec3(vec3List.getDouble(0), vec3List.getDouble(1), vec3List.getDouble(2));
				}
				case "File" -> new File(NbtCompat.getStringOr(entry, "Value", ""));
				case "JsonObject" -> JsonParser.parseString(NbtCompat.getStringOr(entry, "Value", "")).getAsJsonObject();
				case "JsonArray" -> JsonParser.parseString(NbtCompat.getStringOr(entry, "Value", "")).getAsJsonArray();
				default -> NbtCompat.getStringOr(entry, "Value", "");
			};
			objects.add(value);
		}
		return objects;
	}

	public static ListTag saveGlobalWorld(ArrayList<Object> list) {
		return saveGlobal(list);
	}

	public static ListTag saveGlobalMap(ArrayList<Object> list) {
		return saveGlobal(list);
	}

	private static ListTag saveGlobal(ArrayList<Object> list) {
		ListTag listTag = new ListTag();
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = new CompoundTag();
			entry.putString("Type", list.get(i).getClass().getSimpleName());
			Object obj = list.get(i);
			if (obj instanceof BlockState blockState) {
				entry.put("Value", NbtUtils.writeBlockState(blockState));
			} else if (obj instanceof Direction direction) {
				entry.putInt("Value", direction.get3DDataValue());
			} else if (obj instanceof ItemStack itemStack) {
				entry.put("Value", itemStack.save(new CompoundTag()));
			} else if (obj instanceof Boolean bool) {
				entry.putBoolean("Value", bool);
			} else if (obj instanceof Double doub) {
				entry.putDouble("Value", doub);
			} else if (obj instanceof String str) {
				entry.putString("Value", str);
			} else if (obj instanceof Vec3 vec) {
				ListTag vec3List = new ListTag();
				vec3List.add(DoubleTag.valueOf(vec.x));
				vec3List.add(DoubleTag.valueOf(vec.y));
				vec3List.add(DoubleTag.valueOf(vec.z));
				entry.put("Value", vec3List);
			} else if (obj instanceof File file) {
				entry.putString("Value", file.getPath());
			} else if (obj instanceof JsonObject jsObj) {
				entry.putString("Value", jsObj.toString());
			} else if (obj instanceof JsonArray jsArr) {
				entry.putString("Value", jsArr.toString());
			} else {
				entry.putString("Value", String.valueOf(obj));
			}
			listTag.add(entry);
		}
		return listTag;
	}
}