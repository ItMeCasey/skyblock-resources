package com.itmecasey.skyblockresources.kubejs;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;

/**
 * Teaches KubeJS 6 the recipe types this mod uses for soils, trees and crops, so scripts get
 * typed fields when they add, edit or remove them.
 */
public class SkyblockResourcesKubeJSPlugin extends KubeJSPlugin {
	private static final String MODID = "skyblock_resources";

	@Override
	public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
		RecipeKey<?> soil = ItemComponents.INPUT.key("soil");
		RecipeKey<?> speedBonus = NumberComponent.DOUBLE.key("speed_bonus").optional(0.0);
		RecipeKey<?> growthMultiplier = NumberComponent.DOUBLE.key("growth_multiplier").optional(1.0);
		RecipeKey<?> bonusSeedChance = NumberComponent.DOUBLE.key("bonus_seed_chance").optional(0.0);
		RecipeKey<?> sapling = ItemComponents.INPUT.key("sapling");
		RecipeKey<?> seed = ItemComponents.INPUT.key("seed");
		RecipeKey<?> generator = ItemComponents.INPUT.key("generator");
		RecipeKey<?> outputs = ItemComponents.OUTPUT.key("outputs");
		RecipeKey<?> duration = NumberComponent.DOUBLE.key("duration").optional(1200.0);
		// What one cycle can yield: { id, min, max, chance }; max defaults to min and chance to 1. Required, since KubeJS rejects an empty list.
		RecipeKey<?> drops = RecipeComponent.builder(
				StringComponent.ID.key("id"),
				NumberComponent.INT.key("min").optional(1),
				NumberComponent.INT.key("max").optional(0),
				NumberComponent.DOUBLE.key("chance").optional(1.0)).asArray().key("drops");

		event.register(new ResourceLocation(MODID, "soil_type"),
				new RecipeSchema(soil, speedBonus, growthMultiplier, bonusSeedChance).constructor(soil));
		event.register(new ResourceLocation(MODID, "tree_drops"),
				new RecipeSchema(sapling, soil, generator, outputs, drops, duration).constructor(sapling, soil, generator, outputs, drops));
		event.register(new ResourceLocation(MODID, "crop_drops"),
				new RecipeSchema(seed, soil, generator, outputs, drops, duration).constructor(seed, soil, generator, outputs, drops));
	}
}
