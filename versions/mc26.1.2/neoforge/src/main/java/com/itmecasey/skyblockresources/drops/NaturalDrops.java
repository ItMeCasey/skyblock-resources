package com.itmecasey.skyblockresources.drops;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** What one cycle of a tree or crop generator yields, rolled from the plant's tree_drops or crop_drops recipe. */
public final class NaturalDrops {
	private NaturalDrops() {
	}

	public static List<ItemStack> forTree(ServerLevel level, Item sapling, BlockPos pos) {
		return RecipeDrops.roll(level, sapling, true, level.getRandom());
	}

	public static List<ItemStack> forCrop(ServerLevel level, Item seed, BlockPos pos) {
		return RecipeDrops.roll(level, seed, false, level.getRandom());
	}
}
