package com.itmecasey.skyblockresources.init;

import net.minecraft.resources.Identifier;

import com.itmecasey.skyblockresources.integration.jei.TreeDropsJeiCategory;
import com.itmecasey.skyblockresources.integration.jei.SoilTypeJeiCategory;
import com.itmecasey.skyblockresources.integration.jei.ResourceGeneratorJeiCategory;
import com.itmecasey.skyblockresources.integration.jei.CropDropsJeiCategory;

import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.IModPlugin;

@JeiPlugin
public class SkyblockResourcesModJeiPlugin implements IModPlugin {
	public static ResourceGeneratorJeiCategory RESOURCE_GENERATOR_JEI_CATEGORY;
	public static TreeDropsJeiCategory TREE_DROPS_JEI_CATEGORY;
	public static SoilTypeJeiCategory SOIL_TYPE_JEI_CATEGORY;
	public static CropDropsJeiCategory CROP_DROPS_JEI_CATEGORY;

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath("skyblock_resources", "jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(RESOURCE_GENERATOR_JEI_CATEGORY = new ResourceGeneratorJeiCategory(helper));
		registration.addRecipeCategories(TREE_DROPS_JEI_CATEGORY = new TreeDropsJeiCategory(helper));
		registration.addRecipeCategories(SOIL_TYPE_JEI_CATEGORY = new SoilTypeJeiCategory(helper));
		registration.addRecipeCategories(CROP_DROPS_JEI_CATEGORY = new CropDropsJeiCategory(helper));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(RESOURCE_GENERATOR_JEI_CATEGORY.getRecipeType(), ResourceGeneratorJeiCategory.groupRecipes(SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.RESOURCE_GENERATOR_TYPE.get()).toList()));
		registration.addRecipes(TREE_DROPS_JEI_CATEGORY.getRecipeType(), TreeDropsJeiCategory.groupRecipes(SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.TREE_DROPS_TYPE.get()).toList()));
		registration.addRecipes(SOIL_TYPE_JEI_CATEGORY.getRecipeType(), SoilTypeJeiCategory.groupRecipes(SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.SOIL_TYPE_TYPE.get()).toList()));
		registration.addRecipes(CROP_DROPS_JEI_CATEGORY.getRecipeType(), CropDropsJeiCategory.groupRecipes(SkyblockResourcesModRecipeTypes.getRecipes(SkyblockResourcesModRecipeTypes.CROP_DROPS_TYPE.get()).toList()));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addCraftingStation(RESOURCE_GENERATOR_JEI_CATEGORY.getRecipeType(), RESOURCE_GENERATOR_JEI_CATEGORY.getCatalysts());
		registration.addCraftingStation(TREE_DROPS_JEI_CATEGORY.getRecipeType(), TREE_DROPS_JEI_CATEGORY.getCatalysts());
		registration.addCraftingStation(SOIL_TYPE_JEI_CATEGORY.getRecipeType(), SOIL_TYPE_JEI_CATEGORY.getCatalysts());
		registration.addCraftingStation(CROP_DROPS_JEI_CATEGORY.getRecipeType(), CROP_DROPS_JEI_CATEGORY.getCatalysts());
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
	}
}