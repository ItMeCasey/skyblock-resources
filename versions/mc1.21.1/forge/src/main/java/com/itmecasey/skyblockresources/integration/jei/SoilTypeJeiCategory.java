package com.itmecasey.skyblockresources.integration.jei;

import net.minecraftforge.fluids.FluidStack;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.itmecasey.skyblockresources.compat.TextAlignment;
import com.itmecasey.skyblockresources.compat.GuiText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import com.itmecasey.skyblockresources.recipe.SoilTypeRecipe;
import com.itmecasey.skyblockresources.recipe.RecipeUtils;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.constants.VanillaTypes;

import java.util.List;
import java.util.ArrayList;

public class SoilTypeJeiCategory extends AbstractJeiCategory<SoilTypeJeiCategory.RecipeGroup> {
	public static class OutputEntry {
		public final ItemStack stack;
		public final int minCount;
		public final int maxCount;
		public final double chance;
		public final double duration;

		public OutputEntry(ItemStack stack, int minCount, int maxCount, double chance, double duration) {
			this.stack = stack;
			this.minCount = minCount;
			this.maxCount = maxCount;
			this.chance = chance;
			this.duration = duration;
		}
	}

	public static class FluidOutputEntry {
		public final FluidStack stack;
		public final int minCount;
		public final int maxCount;
		public final double chance;
		public final double duration;

		public FluidOutputEntry(FluidStack stack, int minCount, int maxCount, double chance, double duration) {
			this.stack = stack;
			this.minCount = minCount;
			this.maxCount = maxCount;
			this.chance = chance;
			this.duration = duration;
		}
	}

	public static class RecipeGroup {
		private final List<RecipeHolder<SoilTypeRecipe>> recipes;
		private final ItemStack activeDynamicItem;

		public RecipeGroup(List<RecipeHolder<SoilTypeRecipe>> recipes) {
			this(recipes, ItemStack.EMPTY);
		}

		public RecipeGroup(List<RecipeHolder<SoilTypeRecipe>> recipes, ItemStack activeDynamicItem) {
			this.recipes = recipes;
			this.activeDynamicItem = activeDynamicItem != null ? activeDynamicItem : ItemStack.EMPTY;
		}

		public ItemStack getActiveSapling() {
			return activeDynamicItem;
		}

		public ItemStack getActiveCrop() {
			return activeDynamicItem;
		}

		public ItemStack getActiveSpawnEgg() {
			return activeDynamicItem;
		}

		public ItemStack getActiveDynamicItem() {
			return activeDynamicItem;
		}

		public List<RecipeHolder<SoilTypeRecipe>> getRecipes() {
			return recipes;
		}

		public RecipeHolder<SoilTypeRecipe> getPrimaryRecipe() {
			return recipes.get(0);
		}

		public SoilTypeRecipe value() {
			return recipes.get(0).value();
		}

		public RecipeHolder<SoilTypeRecipe> recipe() {
			return recipes.get(0);
		}

		public List<OutputEntry> getAllItemOutputs() {
			List<OutputEntry> list = new ArrayList<>();
			for (RecipeHolder<SoilTypeRecipe> holder : recipes) {
			}
			return list;
		}

		public List<FluidOutputEntry> getAllFluidOutputs() {
			List<FluidOutputEntry> list = new ArrayList<>();
			for (RecipeHolder<SoilTypeRecipe> holder : recipes) {
			}
			return list;
		}
	}

	public static boolean hasSameInputs(SoilTypeRecipe a, SoilTypeRecipe b) {
		if (!RecipeUtils.areInputsEqual(a.soilItemInput(), b.soilItemInput()))
			return false;
		return true;
	}

	public static List<RecipeGroup> groupRecipes(List<RecipeHolder<SoilTypeRecipe>> allRecipes) {
		List<RecipeGroup> groups = new ArrayList<>();
		for (RecipeHolder<SoilTypeRecipe> holder : allRecipes) {
			boolean matched = false;
			for (RecipeGroup group : groups) {
				if (hasSameInputs(group.getPrimaryRecipe().value(), holder.value())) {
					group.getRecipes().add(holder);
					matched = true;
					break;
				}
			}
			if (!matched) {
				List<RecipeHolder<SoilTypeRecipe>> list = new ArrayList<>();
				list.add(holder);
				groups.add(new RecipeGroup(list));
			}
		}
		return groups;
	}

	public SoilTypeJeiCategory(IGuiHelper helper) {
		super(RecipeType.create("skyblock_resources", "soil_type", SoilTypeJeiCategory.RecipeGroup.class), "soil_type", background(helper, "blank_background", 0, 0, 150, 39), icon(helper, Blocks.DIRT.asItem()));
	}

	@Override
	public Item[] getCatalysts() {
		return catalysts();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeGroup recipe) {
		builder.addSlot(RecipeIngredientRole.INPUT, 10, 10).addIngredients(VanillaTypes.ITEM_STACK, RecipeUtils.getItemStacks(recipe.value().soilItemInput()));
	}

	@Override
	public void draw(RecipeGroup recipe, GuiGraphics guiGraphics, double mouseX, double mouseY) {
	}

	@Override
	public void drawExtras(RecipeGroup recipe, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		Minecraft minecraft = Minecraft.getInstance();
		int tooltipX = RecipeUtils.getTooltipX();
		int tooltipY = RecipeUtils.getTooltipY();
		GuiText.draw(guiGraphics, TextAlignment.LEFT, (int) ((30) / (0.75)), (int) ((11) / (0.75)), (float) (0.75), Component.literal(String.valueOf("" + "Duration Reduction: " + new java.text.DecimalFormat("#%").format(recipe.value().getDoubleResult("speed_bonus")))), 0xFEFEFE);
		GuiText.draw(guiGraphics, TextAlignment.LEFT, (int) ((30) / (0.75)), (int) ((19) / (0.75)), (float) (0.75), Component.literal(String.valueOf("" + "Drop Multiplier: " + new java.text.DecimalFormat("#x").format(recipe.value().getDoubleResult("growth_multiplier")))), 0xFEFEFE);
	}
}