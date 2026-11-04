package com.itmecasey.skyblockresources.integration.jei;

import net.neoforged.neoforge.fluids.FluidStack;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.itmecasey.skyblockresources.compat.TextAlignment;
import com.itmecasey.skyblockresources.compat.GuiText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import com.itmecasey.skyblockresources.recipe.ResourceGeneratorRecipe;
import com.itmecasey.skyblockresources.recipe.RecipeUtils;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlocks;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.constants.VanillaTypes;

import java.util.List;
import java.util.ArrayList;

public class ResourceGeneratorJeiCategory extends AbstractJeiCategory<ResourceGeneratorJeiCategory.RecipeGroup> {
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

		/** Items produced per minute, assuming one batch per recipe duration. */
		public double itemsPerMinute() {
			if (duration <= 0)
				return 0;
			double count = (minCount > 0 && maxCount > 0) ? (minCount + maxCount) / 2.0 : stack.getCount();
			if (chance > 0 && chance < 1)
				count *= chance;
			return count * 1200.0 / duration;
		}

		public String itemsPerMinuteText() {
			return new java.text.DecimalFormat("#.##", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.ROOT)).format(itemsPerMinute());
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
		private final List<RecipeHolder<ResourceGeneratorRecipe>> recipes;
		private final ItemStack activeDynamicItem;

		public RecipeGroup(List<RecipeHolder<ResourceGeneratorRecipe>> recipes) {
			this(recipes, ItemStack.EMPTY);
		}

		public RecipeGroup(List<RecipeHolder<ResourceGeneratorRecipe>> recipes, ItemStack activeDynamicItem) {
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

		public List<RecipeHolder<ResourceGeneratorRecipe>> getRecipes() {
			return recipes;
		}

		public RecipeHolder<ResourceGeneratorRecipe> getPrimaryRecipe() {
			return recipes.get(0);
		}

		public ResourceGeneratorRecipe value() {
			return recipes.get(0).value();
		}

		public RecipeHolder<ResourceGeneratorRecipe> recipe() {
			return recipes.get(0);
		}

		public List<OutputEntry> getAllItemOutputs() {
			List<OutputEntry> list = new ArrayList<>();
			for (RecipeHolder<ResourceGeneratorRecipe> holder : recipes) {
				{
					ItemStack stack = holder.value().getItemStackResult("output");
					if (stack != null && !stack.isEmpty()) {
						int min = (int) holder.value().getDoubleResult("output_min_count");
						int max = (int) holder.value().getDoubleResult("output_max_count");
						double chance = holder.value().getDoubleResult("output_chance");
						double duration = holder.value().getDoubleResult("duration");
						list.add(new OutputEntry(stack, min, max, chance, duration));
					}
				}
			}
			return list;
		}

		public List<FluidOutputEntry> getAllFluidOutputs() {
			List<FluidOutputEntry> list = new ArrayList<>();
			for (RecipeHolder<ResourceGeneratorRecipe> holder : recipes) {
			}
			return list;
		}
	}

	public static boolean hasSameInputs(ResourceGeneratorRecipe a, ResourceGeneratorRecipe b) {
		if (!RecipeUtils.areInputsEqual(a.generatorItemInput(), b.generatorItemInput()))
			return false;
		if (!RecipeUtils.areInputsEqual(a.resourceItemInput(), b.resourceItemInput()))
			return false;
		return true;
	}

	public static List<RecipeGroup> groupRecipes(List<RecipeHolder<ResourceGeneratorRecipe>> allRecipes) {
		List<RecipeGroup> groups = new ArrayList<>();
		for (RecipeHolder<ResourceGeneratorRecipe> holder : allRecipes) {
			boolean matched = false;
			for (RecipeGroup group : groups) {
				if (hasSameInputs(group.getPrimaryRecipe().value(), holder.value())) {
					group.getRecipes().add(holder);
					matched = true;
					break;
				}
			}
			if (!matched) {
				List<RecipeHolder<ResourceGeneratorRecipe>> list = new ArrayList<>();
				list.add(holder);
				groups.add(new RecipeGroup(list));
			}
		}
		return groups;
	}

	public ResourceGeneratorJeiCategory(IGuiHelper helper) {
		super(RecipeType.create("skyblock_resources", "resource_generator", ResourceGeneratorJeiCategory.RecipeGroup.class), "resource_generator", background(helper, "blank_background", 0, 0, 110, 40),
				icon(helper, SkyblockResourcesModBlocks.WOODEN_GENERATOR.get().asItem()));
	}

	@Override
	public Item[] getCatalysts() {
		return catalysts(SkyblockResourcesModBlocks.WOODEN_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.STONE_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.IRON_GENERATOR.get().asItem(),
				SkyblockResourcesModBlocks.DIAMOND_GENERATOR.get().asItem(), SkyblockResourcesModBlocks.NETHERITE_GENERATOR.get().asItem());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeGroup recipe) {
		builder.addSlot(RecipeIngredientRole.INPUT, 5, 5).addIngredients(VanillaTypes.ITEM_STACK, RecipeUtils.getItemStacks(recipe.value().generatorItemInput()));
		builder.addSlot(RecipeIngredientRole.INPUT, 29, 5).addIngredients(VanillaTypes.ITEM_STACK, RecipeUtils.getItemStacks(recipe.value().resourceItemInput()));
		{
			int _slotIdx = 0;
			int _totalSlots = 1;
			List<OutputEntry> _allOutputs = recipe.getAllItemOutputs();
			if (_totalSlots == 1 && _allOutputs.size() > 1) {
				int _maxShow = Math.min(10, _allOutputs.size());
				for (int _i = 0; _i < _maxShow; _i++) {
					final int _slotIndex = _i;
					int _col = _i % 5;
					int _row = _i / 5;
					int _slotX = 82 + _col * 18;
					int _slotY = 5 + _row * 18;
					List<ItemStack> _slotStacks = new ArrayList<>();
					if (_i == 9 && _allOutputs.size() > 10) {
						for (int _j = 9; _j < _allOutputs.size(); _j++) {
							_slotStacks.add(_allOutputs.get(_j).stack);
						}
					} else {
						_slotStacks.add(_allOutputs.get(_i).stack);
					}
					builder.addSlot(RecipeIngredientRole.OUTPUT, _slotX, _slotY).addIngredients(VanillaTypes.ITEM_STACK, _slotStacks).addRichTooltipCallback((slotView, tooltip) -> {
						OutputEntry _entry;
						if (_slotIndex == 9 && _allOutputs.size() > 10) {
							int _cycleIdx = (int) ((System.currentTimeMillis() / 1000) % (_allOutputs.size() - 9));
							_entry = _allOutputs.get(9 + _cycleIdx);
						} else {
							_entry = _allOutputs.get(_slotIndex);
						}
						String _chanceStr = RecipeUtils.formatChance(_entry.chance);
						if (_chanceStr != null) {
							tooltip.add(Component.literal("Chance: " + _chanceStr));
						}
						if (_entry.minCount > 0 && _entry.maxCount > 0 && _entry.minCount != _entry.maxCount) {
							tooltip.add(Component.literal("Amount: " + _entry.minCount + " - " + _entry.maxCount));
						} else if (_entry.stack.getCount() > 1) {
							tooltip.add(Component.literal("Amount: " + _entry.stack.getCount()));
						}
						String _durStr = RecipeUtils.formatDuration(_entry.duration);
						if (_durStr != null) {
							tooltip.add(Component.literal("Cycle time: " + _durStr));
							tooltip.add(Component.literal("Items/min: " + _entry.itemsPerMinuteText()));
						}
					});
				}
			} else if (_slotIdx < Math.min(10, _allOutputs.size())) {
				final int _slotIndex = _slotIdx;
				List<ItemStack> _slotStacks = new ArrayList<>();
				if (_slotIdx == _totalSlots - 1 && _allOutputs.size() > _totalSlots) {
					for (int _i = _slotIdx; _i < _allOutputs.size(); _i++) {
						_slotStacks.add(_allOutputs.get(_i).stack);
					}
				} else {
					_slotStacks.add(_allOutputs.get(_slotIdx).stack);
				}
				builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 5).addIngredients(VanillaTypes.ITEM_STACK, _slotStacks).addRichTooltipCallback((slotView, tooltip) -> {
					OutputEntry _entry;
					if (_slotIndex == _totalSlots - 1 && _allOutputs.size() > _totalSlots) {
						int _cycleIdx = (int) ((System.currentTimeMillis() / 1000) % (_allOutputs.size() - _slotIndex));
						_entry = _allOutputs.get(_slotIndex + _cycleIdx);
					} else {
						_entry = _allOutputs.get(_slotIndex);
					}
					String _chanceStr = RecipeUtils.formatChance(_entry.chance);
					if (_chanceStr != null) {
						tooltip.add(Component.literal("Chance: " + _chanceStr));
					}
					if (_entry.minCount > 0 && _entry.maxCount > 0 && _entry.minCount != _entry.maxCount) {
						tooltip.add(Component.literal("Amount: " + _entry.minCount + " - " + _entry.maxCount));
					} else if (_entry.stack.getCount() > 1) {
						tooltip.add(Component.literal("Amount: " + _entry.stack.getCount()));
					}
					String _durStr = RecipeUtils.formatDuration(_entry.duration);
					if (_durStr != null) {
						tooltip.add(Component.literal("Cycle time: " + _durStr));
						tooltip.add(Component.literal("Items/min: " + _entry.itemsPerMinuteText()));
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
			int _slotIdx = 0;
			int _totalSlots = 1;
			List<OutputEntry> _allOutputs = recipe.getAllItemOutputs();
			if (_totalSlots == 1 && _allOutputs.size() > 1) {
				int _maxShow = Math.min(10, _allOutputs.size());
				for (int _i = 0; _i < _maxShow; _i++) {
					int _col = _i % 5;
					int _row = _i / 5;
					int _slotX = 82 + _col * 18;
					int _slotY = 5 + _row * 18;
					OutputEntry _entry;
					if (_i == 9 && _allOutputs.size() > 10) {
						int _cycleIdx = (int) ((System.currentTimeMillis() / 1000) % (_allOutputs.size() - 9));
						_entry = _allOutputs.get(9 + _cycleIdx);
					} else {
						_entry = _allOutputs.get(_i);
					}
					int _minCount = _entry.minCount;
					int _maxCount = _entry.maxCount;
					int _count = _entry.stack.getCount();
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
			} else if (_slotIdx < Math.min(10, _allOutputs.size())) {
				OutputEntry _entry;
				if (_slotIdx == _totalSlots - 1 && _allOutputs.size() > _totalSlots) {
					int _cycleIdx = (int) ((System.currentTimeMillis() / 1000) % (_allOutputs.size() - _slotIdx));
					_entry = _allOutputs.get(_slotIdx + _cycleIdx);
				} else {
					_entry = _allOutputs.get(_slotIdx);
				}
				int _minCount = _entry.minCount;
				int _maxCount = _entry.maxCount;
				int _count = _entry.stack.getCount();
				String _amountText = null;
				if (_minCount > 0 && _maxCount > 0 && (_minCount != 1 || _maxCount != 1)) {
					_amountText = (_minCount == _maxCount) ? String.valueOf(_minCount) : (_minCount + "-" + _maxCount);
				} else if (_count > 1) {
					_amountText = String.valueOf(_count);
				}
				if (_amountText != null) {
					GuiText.draw(guiGraphics, TextAlignment.RIGHT, (82 + 17) * 2, (5 + 12) * 2 + 1, 0.5f, Component.literal(_amountText), 0xFFFFFF);
				}
			}
		}
		GuiText.draw(guiGraphics, TextAlignment.LEFT, (int) ((56) / (1)), (int) ((10) / (1)), (float) (1), Component.literal(String.valueOf(new java.text.DecimalFormat("#s").format(((double) recipe.value().getDoubleResult("duration") / 20)))), 0xFEFEFE);
		List<OutputEntry> _outputs = recipe.getAllItemOutputs();
		if (!_outputs.isEmpty() && _outputs.get(0).duration > 0)
			GuiText.draw(guiGraphics, TextAlignment.LEFT, 5, 29, 1.0f, Component.literal(_outputs.get(0).itemsPerMinuteText() + " items/min, " + RecipeUtils.formatDuration(_outputs.get(0).duration) + " cycle"), 0xFEFEFE);
	}
}