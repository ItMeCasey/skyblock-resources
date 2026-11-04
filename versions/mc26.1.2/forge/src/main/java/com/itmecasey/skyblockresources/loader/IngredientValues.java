package com.itmecasey.skyblockresources.loader;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/** NeoForge adds {@code Ingredient.getValues()}; vanilla has {@code items()}, a stream of the same holders. */
public final class IngredientValues {
	private IngredientValues() {
	}

	public static List<Holder<Item>> of(Ingredient ingredient) {
		return ingredient.items().toList();
	}
}
