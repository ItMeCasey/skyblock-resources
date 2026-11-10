package com.itmecasey.skyblockresources.devtest;

import net.minecraft.world.item.Item;

/** Opens the recipes or uses of an item in JEI, REI or EMI. Only the class for the viewer that is installed is ever touched. */
final class ViewerShow {
	private ViewerShow() {
	}

	static boolean show(String viewer, Item item, boolean uses) {
		return switch (viewer) {
			case "jei" -> Jei.show(item, uses);
			default -> false;
		};
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
