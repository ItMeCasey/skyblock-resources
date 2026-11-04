package com.itmecasey.skyblockresources.integration.jei;

import net.minecraftforge.fluids.FluidStack;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.itmecasey.skyblockresources.compat.TextAlignment;
import com.itmecasey.skyblockresources.compat.GuiText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import com.itmecasey.skyblockresources.recipe.TreeDropsRecipe;
import com.itmecasey.skyblockresources.recipe.RecipeUtils;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlocks;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.constants.VanillaTypes;

import java.util.List;
import java.util.ArrayList;

public class TreeDropsJeiCategory extends AbstractJeiCategory<TreeDropsJeiCategory.RecipeGroup> {
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
		private final List<RecipeHolder<TreeDropsRecipe>> recipes;
		private final ItemStack activeDynamicItem;

		public RecipeGroup(List<RecipeHolder<TreeDropsRecipe>> recipes) {
			this(recipes, ItemStack.EMPTY);
		}

		public RecipeGroup(List<RecipeHolder<TreeDropsRecipe>> recipes, ItemStack activeDynamicItem) {
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

		public List<RecipeHolder<TreeDropsRecipe>> getRecipes() {
			return recipes;
		}

		public RecipeHolder<TreeDropsRecipe> getPrimaryRecipe() {
			return recipes.get(0);
		}

		public TreeDropsRecipe value() {
			return recipes.get(0).value();
		}

		public RecipeHolder<TreeDropsRecipe> recipe() {
			return recipes.get(0);
		}

		public List<OutputEntry> getAllItemOutputs() {
			List<OutputEntry> list = new ArrayList<>();
			for (RecipeHolder<TreeDropsRecipe> holder : recipes) {
			}
			return list;
		}

		public List<FluidOutputEntry> getAllFluidOutputs() {
			List<FluidOutputEntry> list = new ArrayList<>();
			for (RecipeHolder<TreeDropsRecipe> holder : recipes) {
			}
			return list;
		}
	}

	public static boolean hasSameInputs(TreeDropsRecipe a, TreeDropsRecipe b) {
		if (!RecipeUtils.areInputsEqual(a.saplingItemInput(), b.saplingItemInput()))
			return false;
		if (!RecipeUtils.areInputsEqual(a.soilItemInput(), b.soilItemInput()))
			return false;
		if (!RecipeUtils.areInputsEqual(a.generatorItemInput(), b.generatorItemInput()))
			return false;
		return true;
	}

	public static List<RecipeGroup> groupRecipes(List<RecipeHolder<TreeDropsRecipe>> allRecipes) {
		List<RecipeGroup> groups = new ArrayList<>();
		for (RecipeHolder<TreeDropsRecipe> holder : allRecipes) {
			List<ItemStack> saplings = holder.value().getSaplingResults("outputs");
			if (saplings.isEmpty()) {
				groups.add(new RecipeGroup(List.of(holder), ItemStack.EMPTY));
			} else {
				for (ItemStack s : saplings) {
					groups.add(new RecipeGroup(List.of(holder), s));
				}
			}
		}
		return groups;
	}

	public TreeDropsJeiCategory(IGuiHelper helper) {
		super(RecipeType.create("skyblock_resources", "tree_drops", TreeDropsJeiCategory.RecipeGroup.class), "tree_drops", background(helper, "tree_jei_output", 0, 0, 190, 76),
				icon(helper, SkyblockResourcesModBlocks.WOODEN_TREE_GENERATOR.get().asItem()));
	}

	@Override
	public Item[] getCatalysts() {
		return catalysts(SkyblockResourcesModBlocks.DIAMOND_TREE_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.IRON_TREE_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.NETHERITE_TREE_GENERATOR.get().asItem(),
				SkyblockResourcesModBlocks.STONE_TREE_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.WOODEN_TREE_GENERATOR.get().asItem());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeGroup recipe) {
		{
			ItemStack _activeDyn = recipe.getActiveDynamicItem();
			List<ItemStack> _inputs = RecipeUtils.getItemStacks(recipe.value().saplingItemInput());
			boolean _containsActive = false;
			if (!_activeDyn.isEmpty()) {
				for (ItemStack _in : _inputs) {
					if (ItemStack.isSameItemSameComponents(_in, _activeDyn)) {
						_containsActive = true;
						break;
					}
				}
			}
			if (_containsActive) {
				builder.addSlot(RecipeIngredientRole.INPUT, 43, 14).addIngredients(VanillaTypes.ITEM_STACK, List.of(_activeDyn));
			} else {
				builder.addSlot(RecipeIngredientRole.INPUT, 43, 14).addIngredients(VanillaTypes.ITEM_STACK, _inputs);
			}
		}
		{
			ItemStack _activeDyn = recipe.getActiveDynamicItem();
			List<ItemStack> _inputs = RecipeUtils.getItemStacks(recipe.value().soilItemInput());
			boolean _containsActive = false;
			if (!_activeDyn.isEmpty()) {
				for (ItemStack _in : _inputs) {
					if (ItemStack.isSameItemSameComponents(_in, _activeDyn)) {
						_containsActive = true;
						break;
					}
				}
			}
			if (_containsActive) {
				builder.addSlot(RecipeIngredientRole.INPUT, 43, 46).addIngredients(VanillaTypes.ITEM_STACK, List.of(_activeDyn));
			} else {
				builder.addSlot(RecipeIngredientRole.INPUT, 43, 46).addIngredients(VanillaTypes.ITEM_STACK, _inputs);
			}
		}
		{
			ItemStack _activeDyn = recipe.getActiveDynamicItem();
			List<ItemStack> _inputs = RecipeUtils.getItemStacks(recipe.value().generatorItemInput());
			boolean _containsActive = false;
			if (!_activeDyn.isEmpty()) {
				for (ItemStack _in : _inputs) {
					if (ItemStack.isSameItemSameComponents(_in, _activeDyn)) {
						_containsActive = true;
						break;
					}
				}
			}
			if (_containsActive) {
				builder.addSlot(RecipeIngredientRole.INPUT, 15, 29).addIngredients(VanillaTypes.ITEM_STACK, List.of(_activeDyn));
			} else {
				builder.addSlot(RecipeIngredientRole.INPUT, 15, 29).addIngredients(VanillaTypes.ITEM_STACK, _inputs);
			}
		}
		{
			ItemStack _curSap = !recipe.getActiveSapling().isEmpty() ? recipe.getActiveSapling() : recipe.value().getSaplingResult("outputs");
			List<RecipeUtils.TreeDropEntry> _drops = RecipeUtils.getTreeDropEntries(Minecraft.getInstance().level, _curSap);
			int _maxShow = Math.min(10, _drops.size());
			for (int _i = 0; _i < _maxShow; _i++) {
				final int _slotIndex = _i;
				int _col = _i % 5;
				int _row = _i / 5;
				int _slotX = 90 + _col * 18;
				int _slotY = 21 + _row * 18;
				RecipeUtils.TreeDropEntry _entry = _drops.get(_i);
				builder.addSlot(RecipeIngredientRole.OUTPUT, _slotX, _slotY).addIngredients(VanillaTypes.ITEM_STACK, List.of(_entry.getStack().copyWithCount(1))).addRichTooltipCallback((slotView, tooltip) -> {
					String _chanceStr = RecipeUtils.formatChance(_entry.chance());
					if (_chanceStr != null) {
						tooltip.add(Component.literal("Chance: " + _chanceStr));
					}
					if (_entry.minCount() > 0 && _entry.maxCount() > 0 && _entry.minCount() != _entry.maxCount()) {
						tooltip.add(Component.literal("Amount: " + (int) _entry.minCount() + " - " + (int) _entry.maxCount()));
					} else if (_entry.count() > 1) {
						tooltip.add(Component.literal("Amount: " + (int) _entry.count()));
					}
				});
			}
		}
	}

	@Override
	public void draw(RecipeGroup recipe, GuiGraphics guiGraphics, double mouseX, double mouseY) {
	}

	@Override
	public void drawExtras(RecipeGroup recipe, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		Minecraft minecraft = Minecraft.getInstance();
		int tooltipX = RecipeUtils.getTooltipX();
		int tooltipY = RecipeUtils.getTooltipY();
		{
			ItemStack _curSap = !recipe.getActiveSapling().isEmpty() ? recipe.getActiveSapling() : recipe.value().getSaplingResult("outputs");
			List<RecipeUtils.TreeDropEntry> _curDrops = RecipeUtils.getTreeDropEntries(Minecraft.getInstance().level, _curSap);
			int _maxShow = Math.min(10, _curDrops.size());
			for (int _i = 0; _i < _maxShow; _i++) {
				int _col = _i % 5;
				int _row = _i / 5;
				int _slotX = 90 + _col * 18;
				int _slotY = 21 + _row * 18;
				RecipeUtils.TreeDropEntry _entry = _curDrops.get(_i);
				int _minCount = (int) _entry.minCount();
				int _maxCount = (int) _entry.maxCount();
				int _count = (int) _entry.count();
				String _amountText = null;
				if (_minCount > 0 && _maxCount > 0 && (_minCount != 1 || _maxCount != 1)) {
					_amountText = (_minCount == _maxCount) ? String.valueOf(_minCount) : (_minCount + "-" + _maxCount);
				} else if (_count > 1) {
					_amountText = String.valueOf(_count);
				}
				if (_amountText != null) {
					GuiText.draw(guiGraphics, TextAlignment.RIGHT, (_slotX + 17) * 2, (_slotY + 12) * 2 + 1, 0.5f, Component.literal(_amountText), 0xFFFFFF);
				}
			}
		}
		GuiText.draw(guiGraphics, TextAlignment.LEFT, (int) (66), (int) (33), 1.0f, Component.literal(String.valueOf(new java.text.DecimalFormat("#,###s").format(((double) recipe.value().getDoubleResult("duration") / 20)))), 0xFEFEFE);
	}
}