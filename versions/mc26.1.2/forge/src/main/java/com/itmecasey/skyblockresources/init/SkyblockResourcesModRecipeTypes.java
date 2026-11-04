package com.itmecasey.skyblockresources.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.*;
import net.minecraft.core.registries.Registries;

import com.itmecasey.skyblockresources.recipe.TreeDropsRecipe;
import com.itmecasey.skyblockresources.recipe.SoilTypeRecipe;
import com.itmecasey.skyblockresources.recipe.ResourceGeneratorRecipe;
import com.itmecasey.skyblockresources.recipe.CropDropsRecipe;
import com.itmecasey.skyblockresources.loader.RecipeLookup;
import com.itmecasey.skyblockresources.network.ForgeNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = "skyblock_resources")
public class SkyblockResourcesModRecipeTypes {
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "skyblock_resources");
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "skyblock_resources");
	private static RecipeMap recipeMap = null;

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

	private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> recipeType(String registryName) {
		return RECIPE_TYPES.register(registryName, () -> new RecipeType<T>() {
			@Override
			public String toString() {
				return registryName;
			}
		});
	}

	private static <T extends Recipe<?>> RegistryObject<RecipeSerializer<T>> recipeSerializer(String name, Supplier<RecipeSerializer<T>> serializer) {
		return RECIPE_SERIALIZER.register(name, serializer);
	}

	/** Sends this mod's recipes to the joining player, or to everyone after a reload (NeoForge's sendRecipes). */
	@SubscribeEvent
	public static void sync(OnDatapackSyncEvent event) {
		ServerLevel level = event.getPlayerList().getServer().overworld();
		RecipeMap map = RecipeLookup.recipeMap(level.recipeAccess());
		List<RecipeHolder<?>> holders = new ArrayList<>();
		holders.addAll(map.byType(RESOURCE_GENERATOR_TYPE.get()));
		holders.addAll(map.byType(TREE_DROPS_TYPE.get()));
		holders.addAll(map.byType(SOIL_TYPE_TYPE.get()));
		holders.addAll(map.byType(CROP_DROPS_TYPE.get()));
		ForgeNetwork.RecipeSyncMessage message = new ForgeNetwork.RecipeSyncMessage(holders);
		for (ServerPlayer player : event.getPlayers())
			ForgeNetwork.sendToPlayer(player, message);
	}

	public static <I extends RecipeInput, R extends Recipe<I>> Stream<RecipeHolder<R>> getRecipes(RecipeType<R> recipeType) {
		if (recipeMap != null) {
			return recipeMap.byType(recipeType).stream();
		}
		return Stream.empty();
	}

	/** The client side of the recipe sync. */
	@Mod.EventBusSubscriber(modid = "skyblock_resources", value = Dist.CLIENT)
	public static class RecipeSync {
		/** Called on the client when the server's recipe message arrives. */
		public static void receive(List<RecipeHolder<?>> recipes) {
			recipeMap = RecipeMap.create(recipes);
		}

		@SubscribeEvent
		public static void clear(ClientPlayerNetworkEvent.LoggingOut event) {
			recipeMap = null;
		}
	}
}
