package com.itmecasey.skyblockresources.integration.viewer;

import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlocks;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;
import com.itmecasey.skyblockresources.recipe.CropDropsRecipe;
import com.itmecasey.skyblockresources.recipe.DropEntry;
import com.itmecasey.skyblockresources.recipe.RecipeUtils;
import com.itmecasey.skyblockresources.recipe.ResourceGeneratorRecipe;
import com.itmecasey.skyblockresources.recipe.SoilTypeRecipe;
import com.itmecasey.skyblockresources.recipe.TreeDropsRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * What the REI and EMI plugins show: one card for each resource generator, tree drop, crop drop and soil type
 * recipe, read from the client's recipes. The viewers each draw the cards their own way, so this class holds only
 * data and no viewer classes.
 */
public final class ViewerCards {
	/** The most drops a tree or crop card shows. */
	public static final int DROP_SLOTS = 6;

	private ViewerCards() {
	}

	/** The four kinds of card. */
	public enum Kind {
		RESOURCE("resource_generator", "jei.skyblock_resources.resource_generator"), TREE("tree_drops", "jei.skyblock_resources.tree_drops"), CROP("crop_drops", "jei.skyblock_resources.crop_drops"),
		SOIL("soil_type", "jei.skyblock_resources.soil_type");

		private final String path;
		private final String titleKey;

		Kind(String path, String titleKey) {
			this.path = path;
			this.titleKey = titleKey;
		}

		public String path() {
			return path;
		}

		public String titleKey() {
			return titleKey;
		}

		/** The blocks that do this, wooden first; the first one is the category's icon. */
		public List<ItemStack> stations() {
			return switch (this) {
				case RESOURCE -> stacks(SkyblockResourcesModBlocks.WOODEN_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.STONE_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.IRON_GENERATOR.get().asItem(),
						SkyblockResourcesModBlocks.DIAMOND_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.NETHERITE_GENERATOR.get().asItem());
				case TREE -> stacks(SkyblockResourcesModBlocks.WOODEN_TREE_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.STONE_TREE_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.IRON_TREE_GENERATOR.get().asItem(),
						SkyblockResourcesModBlocks.DIAMOND_TREE_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.NETHERITE_TREE_GENERATOR.get().asItem());
				case CROP -> stacks(SkyblockResourcesModBlocks.WOODEN_CROP_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.STONE_CROP_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.IRON_CROP_GENERATOR.get().asItem(),
						SkyblockResourcesModBlocks.DIAMOND_CROP_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.NETHERITE_CROP_GENERATOR.get().asItem());
				case SOIL -> stacks(Items.FARMLAND);
			};
		}

		/** How many input slots a card of this kind has. */
		public int inputs() {
			return switch (this) {
				case RESOURCE -> 2;
				case TREE, CROP -> 3;
				case SOIL -> 1;
			};
		}
	}

	private static List<ItemStack> stacks(Item... items) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Item item : items) {
			stacks.add(new ItemStack(item));
		}
		return stacks;
	}

	/**
	 * One recipe card.
	 *
	 * @param chances one per output: the chance that output drops
	 * @param ticks   the time of a cycle, or 0 for a soil
	 */
	public record Card(Kind kind, String id, List<List<ItemStack>> inputs, List<ItemStack> outputs, List<Double> chances, int ticks, double speed, double growth, double bonusSeed) {
	}

	/** Every card for the recipes the client has; empty before a world is joined. */
	public static List<Card> all() {
		List<Card> cards = new ArrayList<>();
		for (RecipeHolder<ResourceGeneratorRecipe> holder : SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.RESOURCE_GENERATOR_TYPE.get()).toList()) {
			ResourceGeneratorRecipe recipe = holder.value();
			ItemStack made = recipe.outputItemOutput().create();
			if (made.isEmpty()) {
				continue;
			}
			cards.add(new Card(Kind.RESOURCE, holder.id().identifier().toString(), List.of(RecipeUtils.getItemStacks(recipe.generatorItemInput()), RecipeUtils.getItemStacks(recipe.resourceItemInput())), List.of(made),
					List.of(recipe.outputChance()), (int) recipe.durationNumberOutput(), 0, 0, 0));
		}
		for (RecipeHolder<TreeDropsRecipe> holder : SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.TREE_DROPS_TYPE.get()).toList()) {
			TreeDropsRecipe recipe = holder.value();
			drops(cards, Kind.TREE, holder.id().identifier().toString(), recipe.saplingItemInput(), recipe.soilItemInput(), recipe.generatorItemInput(), recipe.outputsTreeDropsOutput().create(), recipe.drops(),
					recipe.durationNumberOutput());
		}
		for (RecipeHolder<CropDropsRecipe> holder : SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.CROP_DROPS_TYPE.get()).toList()) {
			CropDropsRecipe recipe = holder.value();
			drops(cards, Kind.CROP, holder.id().identifier().toString(), recipe.seedItemInput(), recipe.soilItemInput(), recipe.generatorItemInput(), recipe.outputsCropDropsOutput().create(), recipe.drops(),
					recipe.durationNumberOutput());
		}
		for (RecipeHolder<SoilTypeRecipe> holder : SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.SOIL_TYPE_TYPE.get()).toList()) {
			SoilTypeRecipe recipe = holder.value();
			List<ItemStack> soils = RecipeUtils.getItemStacks(recipe.soilItemInput());
			if (soils.isEmpty()) {
				continue;
			}
			cards.add(new Card(Kind.SOIL, holder.id().identifier().toString(), List.of(soils), List.of(soils.get(0).copy()), List.of(1.0), 0, recipe.speed_bonusNumberOutput(), recipe.growth_multiplierNumberOutput(),
					recipe.bonus_seed_chanceNumberOutput()));
		}
		return cards;
	}

	private static void drops(List<Card> cards, Kind kind, String id, Object plant, Object soil, Object generator, ItemStack fallback, List<DropEntry> entries, double duration) {
		List<ItemStack> outputs = new ArrayList<>();
		List<Double> chances = new ArrayList<>();
		for (DropEntry drop : entries) {
			if (outputs.size() == DROP_SLOTS) {
				break;
			}
			outputs.add(new ItemStack(drop.item(), drop.maxCount()));
			chances.add(drop.chance());
		}
		if (outputs.isEmpty()) {
			if (fallback.isEmpty()) {
				return;
			}
			outputs.add(fallback);
			chances.add(1.0);
		}
		cards.add(new Card(kind, id, List.of(RecipeUtils.getItemStacks(plant), RecipeUtils.getItemStacks(soil), RecipeUtils.getItemStacks(generator)), outputs, chances, (int) duration, 0, 0, 0));
	}
}
