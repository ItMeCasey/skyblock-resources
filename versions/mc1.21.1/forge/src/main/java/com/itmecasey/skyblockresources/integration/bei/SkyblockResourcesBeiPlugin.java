package com.itmecasey.skyblockresources.integration.bei;

import com.itmecasey.bei.api.BeiPlugin;
import com.itmecasey.bei.api.CardPainter;
import com.itmecasey.bei.api.CustomRecipe;
import com.itmecasey.bei.api.IBeiPlugin;
import com.itmecasey.bei.api.ICategoryRegistration;
import com.itmecasey.bei.api.IRecipeRegistration;
import com.itmecasey.bei.api.RecipeCategory;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Barely Enough Items support: a card category for resource generator outputs, tree drops, crop drops and soil
 * types, fed from the same recipes JEI shows. BEI finds this class by its annotation, so it is only ever loaded
 * when BEI is installed.
 */
@BeiPlugin
public class SkyblockResourcesBeiPlugin implements IBeiPlugin {
	private static final int SLOT = 18;
	private static final int PAD = 6;
	private static final int ARROW = 20;
	private static final int DROP_SLOTS = 6;
	private static final int TEXT = 0xFFA0A0A0;

	private RecipeCategory resource;
	private RecipeCategory trees;
	private RecipeCategory crops;
	private RecipeCategory soils;

	/** What a card draws besides the slots: the cycle time and the chance of each output. */
	private record Timing(int ticks, List<Double> chances) {
	}

	private record Soil(double speed, double growth, double bonusSeed) {
	}

	@Override
	public void registerCategories(ICategoryRegistration registration) {
		resource = category("resource_generator", "jei.skyblock_resources.resource_generator", new ItemStack(SkyblockResourcesModBlocks.WOODEN_GENERATOR.get()), 2, 1);
		trees = category("tree_drops", "jei.skyblock_resources.tree_drops", new ItemStack(SkyblockResourcesModBlocks.WOODEN_TREE_GENERATOR.get()), 3, DROP_SLOTS);
		crops = category("crop_drops", "jei.skyblock_resources.crop_drops", new ItemStack(SkyblockResourcesModBlocks.WOODEN_CROP_GENERATOR.get()), 3, DROP_SLOTS);
		soils = soilCategory();
		registration.register(resource);
		registration.register(trees);
		registration.register(crops);
		registration.register(soils);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		Level level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}
		for (RecipeHolder<ResourceGeneratorRecipe> holder : SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.RESOURCE_GENERATOR_TYPE.get()).toList()) {
			ResourceGeneratorRecipe recipe = holder.value();
			if (recipe.outputItemOutput().isEmpty()) {
				continue;
			}
			registration.add(resource, CustomRecipe.builder().id(holder.id().toString()).input(RecipeUtils.getItemStacks(recipe.generatorItemInput())).input(RecipeUtils.getItemStacks(recipe.resourceItemInput()))
					.output(recipe.outputItemOutput().copy()).data(new Timing((int) recipe.durationNumberOutput(), List.of(recipe.outputChance()))).build());
		}
		for (RecipeHolder<TreeDropsRecipe> holder : SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.TREE_DROPS_TYPE.get()).toList()) {
			TreeDropsRecipe recipe = holder.value();
			addDrops(registration, trees, holder.id().toString(), recipe.saplingItemInput(), recipe.soilItemInput(), recipe.generatorItemInput(), recipe.outputsTreeDropsOutput(), recipe.drops(), recipe.durationNumberOutput());
		}
		for (RecipeHolder<CropDropsRecipe> holder : SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.CROP_DROPS_TYPE.get()).toList()) {
			CropDropsRecipe recipe = holder.value();
			addDrops(registration, crops, holder.id().toString(), recipe.seedItemInput(), recipe.soilItemInput(), recipe.generatorItemInput(), recipe.outputsCropDropsOutput(), recipe.drops(), recipe.durationNumberOutput());
		}
		for (RecipeHolder<SoilTypeRecipe> holder : SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.SOIL_TYPE_TYPE.get()).toList()) {
			SoilTypeRecipe recipe = holder.value();
			List<ItemStack> stacks = RecipeUtils.getItemStacks(recipe.soilItemInput());
			if (stacks.isEmpty()) {
				continue;
			}
			registration.add(soils, CustomRecipe.builder().id(holder.id().toString()).input(stacks).output(stacks.get(0).copy())
					.data(new Soil(recipe.speed_bonusNumberOutput(), recipe.growth_multiplierNumberOutput(), recipe.bonus_seed_chanceNumberOutput())).build());
		}
	}

	private static void addDrops(IRecipeRegistration registration, RecipeCategory category, String id, Object plant, Object soil, Object generator, ItemStack fallback, List<DropEntry> drops,
			double duration) {
		CustomRecipe.Builder builder = CustomRecipe.builder().id(id).input(RecipeUtils.getItemStacks(plant)).input(RecipeUtils.getItemStacks(soil)).input(RecipeUtils.getItemStacks(generator));
		List<Double> chances = new ArrayList<>();
		for (DropEntry drop : drops) {
			if (chances.size() == DROP_SLOTS) {
				break;
			}
			builder.output(new ItemStack(drop.item(), drop.maxCount()));
			chances.add(drop.chance());
		}
		if (chances.isEmpty()) {
			if (fallback.isEmpty()) {
				return;
			}
			builder.output(fallback.copy());
			chances.add(1.0);
		}
		registration.add(category, builder.data(new Timing((int) duration, chances)).build());
	}

	// --- Cards ----------------------------------------------------------------------------

	private static RecipeCategory category(String path, String titleKey, ItemStack station, int inputs, int outputs) {
		int arrowX = PAD + inputs * (SLOT + 4);
		int outputX = arrowX + ARROW + 4;
		RecipeCategory.Builder builder = RecipeCategory.builder(ResourceLocation.fromNamespaceAndPath("skyblock_resources", path)).title(Component.translatable(titleKey)).station(station)
				.size(outputX + outputs * SLOT + PAD, 40);
		for (int i = 0; i < inputs; i++) {
			builder.input(PAD + i * (SLOT + 4), 5);
		}
		for (int i = 0; i < outputs; i++) {
			builder.output(outputX + i * SLOT, 5);
		}
		return builder.painter(new DropsPainter(arrowX, outputX)).build();
	}

	private static RecipeCategory soilCategory() {
		return RecipeCategory.builder(ResourceLocation.fromNamespaceAndPath("skyblock_resources", "soil_type")).title(Component.translatable("jei.skyblock_resources.soil_type"))
				.station(new ItemStack(Items.FARMLAND)).size(PAD + 2 * SLOT + 120, 40).input(PAD, 5).output(PAD + SLOT + 4, 5).painter(new SoilPainter()).build();
	}

	private record DropsPainter(int arrowX, int outputX) implements CardPainter {
		@Override
		public void paint(GuiGraphics g, CustomRecipe recipe, int mouseX, int mouseY) {
			g.fill(arrowX, 12, arrowX + ARROW - 6, 14, 0xFF8B8B8B);
			g.fill(arrowX + ARROW - 6, 9, arrowX + ARROW - 4, 17, 0xFF8B8B8B);
			if (recipe.data() instanceof Timing timing) {
				g.drawString(Minecraft.getInstance().font, Component.translatable("skyblock_resources.viewer.ticks", timing.ticks()), PAD, 28, TEXT, false);
			}
		}

		@Override
		public @Nullable List<Component> tooltip(CustomRecipe recipe, int mouseX, int mouseY) {
			if (!(recipe.data() instanceof Timing timing)) {
				return null;
			}
			for (int i = 0; i < timing.chances().size(); i++) {
				int x = outputX + i * SLOT;
				double chance = timing.chances().get(i);
				if (chance < 1.0 && mouseX >= x && mouseX < x + SLOT && mouseY >= 5 && mouseY < 5 + SLOT) {
					return List.of(Component.translatable("skyblock_resources.viewer.chance", Math.round(chance * 1000) / 10.0));
				}
			}
			return null;
		}
	}

	private static final class SoilPainter implements CardPainter {
		@Override
		public void paint(GuiGraphics g, CustomRecipe recipe, int mouseX, int mouseY) {
			if (recipe.data() instanceof Soil soil) {
				int x = PAD + 2 * SLOT + 8;
				g.drawString(Minecraft.getInstance().font, Component.translatable("skyblock_resources.viewer.speed", Math.round(soil.speed() * 100)), x, 4, TEXT, false);
				g.drawString(Minecraft.getInstance().font, Component.translatable("skyblock_resources.viewer.growth", new java.text.DecimalFormat("#.##").format(soil.growth())), x, 15, TEXT, false);
				g.drawString(Minecraft.getInstance().font, Component.translatable("skyblock_resources.viewer.bonus_seed", Math.round(soil.bonusSeed() * 100)), x, 26, TEXT, false);
			}
		}
	}
}
