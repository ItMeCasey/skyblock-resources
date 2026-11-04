package com.itmecasey.skyblockresources.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * NeoForge's SizedIngredient (an ingredient with a count). Forge 1.21.1 has none and no recipe here produces one, so
 * RecipeUtils keeps its branches for it; this only lets them compile.
 */
public record SizedIngredient(Ingredient ingredient, int count) {
	public boolean test(ItemStack stack) {
		return ingredient.test(stack) && stack.getCount() >= count;
	}
}
