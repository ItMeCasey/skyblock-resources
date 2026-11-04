package com.itmecasey.skyblockresources.loader;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An ingredient with a count, like NeoForge's SizedIngredient. This mod's own recipes only use plain ingredients, so
 * nothing creates one on Forge; the recipe helpers still know how to read one, as they do on NeoForge.
 */
public record SizedIngredient(Ingredient ingredient, int count) {
	public boolean test(ItemStack stack) {
		return ingredient.test(stack) && stack.getCount() >= count;
	}
}
