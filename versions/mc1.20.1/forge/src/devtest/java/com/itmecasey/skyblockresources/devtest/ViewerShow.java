package com.itmecasey.skyblockresources.devtest;

import net.minecraft.world.item.Item;

/** Opens the recipes or uses of an item in JEI, REI or EMI. Only the class for the viewer that is installed is ever touched. */
final class ViewerShow {
	private ViewerShow() {
	}

	static boolean show(String viewer, Item item, boolean uses) {
		return switch (viewer) {
			case "emi" -> Emi.show(item, uses);
			case "rei" -> Rei.show(item, uses);
			case "jei" -> Jei.show(item, uses);
			default -> false;
		};
	}

	private static final class Emi {
		static boolean show(Item item, boolean uses) {
			var stack = dev.emi.emi.api.stack.EmiStack.of(item);
			// EMI opens on the Crafting tab; show one of our own recipes of the right kind instead, so its tab is the one open.
			String path = switch (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath()) {
				case "apple" -> "tree_drops";
				case "carrot" -> "crop_drops";
				case "farmland" -> "soil_type";
				default -> "resource_generator";
			};
			dev.emi.emi.api.recipe.EmiRecipe first = null;
			for (dev.emi.emi.api.recipe.EmiRecipe recipe : dev.emi.emi.api.EmiApi.getRecipeManager().getRecipes()) {
				if (recipe.getCategory().getId().getNamespace().equals("skyblock_resources") && recipe.getCategory().getId().getPath().equals(path)) {
					boolean matches = recipe.getOutputs().stream().anyMatch(o -> o.getEmiStacks().stream().anyMatch(s -> s.isEqual(stack)))
							|| recipe.getInputs().stream().anyMatch(i -> i.getEmiStacks().stream().anyMatch(s -> s.isEqual(stack)));
					if (matches) {
						first = recipe;
						break;
					}
					if (first == null) {
						first = recipe;
					}
				}
			}
			if (first != null) {
				dev.emi.emi.api.EmiApi.displayRecipe(first);
				return true;
			}
			if (uses) {
				dev.emi.emi.api.EmiApi.displayUses(stack);
			} else {
				dev.emi.emi.api.EmiApi.displayRecipes(stack);
			}
			return true;
		}
	}

	private static final class Rei {
		static boolean show(Item item, boolean uses) {
			var stack = me.shedaniel.rei.api.common.util.EntryStacks.of(item);
			var builder = me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder();
			if (uses) {
				builder.addUsagesFor(stack);
			} else {
				builder.addRecipesFor(stack);
			}
			// REI opens on the Crafting tab; ask for our own category first.
			String path = switch (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath()) {
				case "apple" -> "tree_drops";
				case "carrot" -> "crop_drops";
				case "farmland" -> "soil_type";
				default -> "resource_generator";
			};
			builder.setPreferredOpenedCategory(me.shedaniel.rei.api.common.category.CategoryIdentifier.of("skyblock_resources", path));
			return builder.open();
		}
	}

	private static final class Jei {
		static boolean show(Item item, boolean uses) {
			var runtime = JeiCapture.runtime;
			if (runtime == null) {
				return false;
			}
			// JEI opens on the Crafting tab for a plain item focus, so show our own category by its type; the generator is a catalyst.
			String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
			mezz.jei.api.recipe.RecipeType<?> type = switch (id) {
				case "cobblestone" -> com.itmecasey.skyblockresources.init.SkyblockResourcesModJeiPlugin.RESOURCE_GENERATOR_JEI_CATEGORY.getRecipeType();
				case "apple" -> com.itmecasey.skyblockresources.init.SkyblockResourcesModJeiPlugin.TREE_DROPS_JEI_CATEGORY.getRecipeType();
				case "carrot" -> com.itmecasey.skyblockresources.init.SkyblockResourcesModJeiPlugin.CROP_DROPS_JEI_CATEGORY.getRecipeType();
				case "farmland" -> com.itmecasey.skyblockresources.init.SkyblockResourcesModJeiPlugin.SOIL_TYPE_JEI_CATEGORY.getRecipeType();
				default -> null;
			};
			if (type != null) {
				runtime.getRecipesGui().showTypes(java.util.List.of(type));
				return true;
			}
			var role = mezz.jei.api.recipe.RecipeIngredientRole.CATALYST;
			var focus =runtime.getJeiHelpers().getFocusFactory().createFocus(role, mezz.jei.api.constants.VanillaTypes.ITEM_STACK, new net.minecraft.world.item.ItemStack(item));
			runtime.getRecipesGui().show(focus);
			return true;
		}
	}
}
