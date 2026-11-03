package com.itmecasey.skyblockresources.drops;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;
import com.itmecasey.skyblockresources.recipe.CropDropsRecipe;
import com.itmecasey.skyblockresources.recipe.DropEntry;
import com.itmecasey.skyblockresources.recipe.RecipeUtils;
import com.itmecasey.skyblockresources.recipe.TreeDropsRecipe;

/** Looks up what a sapling or seed yields in the tree_drops and crop_drops recipes. */
public final class RecipeDrops {
	private RecipeDrops() {
	}

	/** The drops listed by the first recipe for this plant; a recipe without a drops list yields its single output. */
	public static List<DropEntry> entries(LevelAccessor world, Item plant, boolean tree) {
		ItemStack given = new ItemStack(plant);
		if (tree) {
			for (TreeDropsRecipe recipe : RecipeUtils.getRecipes(world, SkyblockResourcesModRecipeTypes.TREEDROPS_TYPE.get()))
				if (RecipeUtils.validate(given, recipe.saplingItemInput()))
					return recipe.drops().isEmpty() ? single(recipe.outputsTreeDropsOutput().create()) : recipe.drops();
		} else {
			for (CropDropsRecipe recipe : RecipeUtils.getRecipes(world, SkyblockResourcesModRecipeTypes.CROPDROPS_TYPE.get()))
				if (RecipeUtils.validate(given, recipe.seedItemInput()))
					return recipe.drops().isEmpty() ? single(recipe.outputsCropDropsOutput().create()) : recipe.drops();
		}
		return List.of();
	}

	public static List<ItemStack> roll(LevelAccessor world, Item plant, boolean tree, RandomSource random) {
		return DropEntry.roll(entries(world, plant, tree), random);
	}

	private static List<DropEntry> single(ItemStack stack) {
		List<DropEntry> list = new ArrayList<>();
		if (!stack.isEmpty())
			list.add(new DropEntry(stack.getItem(), stack.getCount(), stack.getCount(), 1.0d));
		return list;
	}
}
