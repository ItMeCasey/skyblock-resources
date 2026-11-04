package com.itmecasey.skyblockresources.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.world.item.crafting.*;
import net.minecraft.core.registries.BuiltInRegistries;

import com.itmecasey.skyblockresources.recipe.TreeDropsRecipe;
import com.itmecasey.skyblockresources.recipe.SoilTypeRecipe;
import com.itmecasey.skyblockresources.recipe.ResourceGeneratorRecipe;
import com.itmecasey.skyblockresources.recipe.CropDropsRecipe;

import java.util.stream.Stream;
import java.util.function.Supplier;

@EventBusSubscriber
public class SkyblockResourcesModRecipeTypes {
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, "skyblock_resources");
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, "skyblock_resources");

	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		IEventBus bus = ModList.get().getModContainerById("skyblock_resources").get().getEventBus();
		event.enqueueWork(() -> {
			RECIPE_TYPES.register(bus);
			RECIPE_SERIALIZER.register(bus);
		});
	}

	public static final Supplier<RecipeType<ResourceGeneratorRecipe>> RESOURCE_GENERATOR_TYPE = recipeType("resource_generator");
	public static final Supplier<RecipeType<ResourceGeneratorRecipe>> RESOURCEGENERATOR_TYPE = RESOURCE_GENERATOR_TYPE;
	public static final Supplier<RecipeType<TreeDropsRecipe>> TREE_DROPS_TYPE = recipeType("tree_drops");
	public static final Supplier<RecipeType<TreeDropsRecipe>> TREEDROPS_TYPE = TREE_DROPS_TYPE;
	public static final Supplier<RecipeType<SoilTypeRecipe>> SOIL_TYPE_TYPE = recipeType("soil_type");
	public static final Supplier<RecipeType<SoilTypeRecipe>> SOILTYPE_TYPE = SOIL_TYPE_TYPE;
	public static final Supplier<RecipeType<CropDropsRecipe>> CROP_DROPS_TYPE = recipeType("crop_drops");
	public static final Supplier<RecipeType<CropDropsRecipe>> CROPDROPS_TYPE = CROP_DROPS_TYPE;
	public static final Supplier<RecipeSerializer<ResourceGeneratorRecipe>> RESOURCE_GENERATOR_SERIALIZER = recipeSerializer("resource_generator", () -> ResourceGeneratorRecipe.SERIALIZER);
	public static final Supplier<RecipeSerializer<ResourceGeneratorRecipe>> RESOURCEGENERATOR_SERIALIZER = RESOURCE_GENERATOR_SERIALIZER;
	public static final Supplier<RecipeSerializer<TreeDropsRecipe>> TREE_DROPS_SERIALIZER = recipeSerializer("tree_drops", () -> TreeDropsRecipe.SERIALIZER);
	public static final Supplier<RecipeSerializer<TreeDropsRecipe>> TREEDROPS_SERIALIZER = TREE_DROPS_SERIALIZER;
	public static final Supplier<RecipeSerializer<SoilTypeRecipe>> SOIL_TYPE_SERIALIZER = recipeSerializer("soil_type", () -> SoilTypeRecipe.SERIALIZER);
	public static final Supplier<RecipeSerializer<SoilTypeRecipe>> SOILTYPE_SERIALIZER = SOIL_TYPE_SERIALIZER;
	public static final Supplier<RecipeSerializer<CropDropsRecipe>> CROP_DROPS_SERIALIZER = recipeSerializer("crop_drops", () -> CropDropsRecipe.SERIALIZER);
	public static final Supplier<RecipeSerializer<CropDropsRecipe>> CROPDROPS_SERIALIZER = CROP_DROPS_SERIALIZER;

	private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> recipeType(String registryName) {
		return RECIPE_TYPES.register(registryName, () -> new RecipeType<T>() {
			@Override
			public String toString() {
				return registryName;
			}
		});
	}

	private static <T extends Recipe<?>> DeferredHolder<RecipeSerializer<?>, RecipeSerializer<T>> recipeSerializer(String name, Supplier<RecipeSerializer<T>> serializer) {
		return RECIPE_SERIALIZER.register(name, serializer);
	}

	/** The client's recipes of one type. 1.21.1 syncs every recipe type to the client, so no extra packet is needed. */
	public static <I extends RecipeInput, R extends Recipe<I>> Stream<RecipeHolder<R>> getRecipes(RecipeType<R> recipeType) {
		net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
		if (level == null)
			return Stream.empty();
		return level.getRecipeManager().getAllRecipesFor(recipeType).stream();
	}
}
