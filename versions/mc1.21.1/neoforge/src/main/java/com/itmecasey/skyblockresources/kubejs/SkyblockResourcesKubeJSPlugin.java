package com.itmecasey.skyblockresources.kubejs;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.CustomObjectRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;

/**
 * Teaches KubeJS the recipe types this mod uses for soils, trees and crops, so scripts get
 * typed fields when they add, edit or remove them.
 */
public class SkyblockResourcesKubeJSPlugin implements KubeJSPlugin {
	private static final String MODID = "skyblock_resources";

	@Override
	public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
		RecipeKey<?> soil = IngredientComponent.INGREDIENT.inputKey("soil");
		RecipeKey<?> speedBonus = NumberComponent.DOUBLE.otherKey("speed_bonus").optional(0.0);
		RecipeKey<?> growthMultiplier = NumberComponent.DOUBLE.otherKey("growth_multiplier").optional(1.0);
		RecipeKey<?> bonusSeedChance = NumberComponent.DOUBLE.otherKey("bonus_seed_chance").optional(0.0);
		RecipeKey<?> sapling = IngredientComponent.INGREDIENT.inputKey("sapling");
		RecipeKey<?> seed = IngredientComponent.INGREDIENT.inputKey("seed");
		RecipeKey<?> generator = IngredientComponent.INGREDIENT.inputKey("generator");
		RecipeKey<?> outputs = ItemStackComponent.ITEM_STACK.outputKey("outputs");
		RecipeKey<?> duration = NumberComponent.DOUBLE.otherKey("duration").optional(1200.0);
		// What one cycle can yield: { id, min, max, chance }; max defaults to min and chance to 1. Required, since KubeJS rejects an empty list.
		RecipeKey<?> drops = new CustomObjectRecipeComponent(List.of(
				new CustomObjectRecipeComponent.Key("id", StringComponent.ID.instance()),
				new CustomObjectRecipeComponent.Key("min", NumberComponent.INT, true),
				new CustomObjectRecipeComponent.Key("max", NumberComponent.INT, true),
				new CustomObjectRecipeComponent.Key("chance", NumberComponent.DOUBLE, true))).asList().otherKey("drops");

		registry.register(ResourceLocation.fromNamespaceAndPath(MODID, "soil_type"),
				new RecipeSchema(soil, speedBonus, growthMultiplier, bonusSeedChance).constructor(soil));
		registry.register(ResourceLocation.fromNamespaceAndPath(MODID, "tree_drops"),
				new RecipeSchema(sapling, soil, generator, outputs, drops, duration).constructor(sapling, soil, generator, outputs, drops));
		registry.register(ResourceLocation.fromNamespaceAndPath(MODID, "crop_drops"),
				new RecipeSchema(seed, soil, generator, outputs, drops, duration).constructor(seed, soil, generator, outputs, drops));
	}
}
