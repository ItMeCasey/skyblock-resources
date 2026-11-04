package com.itmecasey.skyblockresources.recipe;

import org.jetbrains.annotations.NotNull;

import net.neoforged.neoforge.fluids.FluidStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import com.itmecasey.skyblockresources.compat.IngredientCodecs;
import com.itmecasey.skyblockresources.compat.Serializers;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;

import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;

public record CropDropsRecipe(Ingredient seedItemInput, Ingredient soilItemInput, Ingredient generatorItemInput, ItemStack outputsCropDropsOutput, double durationNumberOutput, List<DropEntry> drops) implements Recipe<RecipeInput> {
	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SkyblockResourcesModRecipeTypes.CROP_DROPS_SERIALIZER.get();
	}

	@Override
	public @NotNull RecipeType<?> getType() {
		return SkyblockResourcesModRecipeTypes.CROP_DROPS_TYPE.get();
	}

	public @NotNull List<ItemStack> getSaplingResults(String output) {
		return RecipeUtils.collectMatchingStacks(Arrays.asList(seedItemInput, soilItemInput, generatorItemInput), stack -> RecipeUtils.isSapling(stack) && !RecipeUtils.getTreeDropEntries(Minecraft.getInstance().level, stack).isEmpty());
	}

	public @NotNull ItemStack getSaplingResult(String output) {
		List<ItemStack> list = getSaplingResults(output);
		return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
	}

	public @NotNull List<ItemStack> getCropResults(String output) {
		List<ItemStack> list = RecipeUtils.collectMatchingStacks(Arrays.asList(seedItemInput, soilItemInput, generatorItemInput), stack -> RecipeUtils.isCropOrSeed(stack) && !RecipeUtils.getCropDropEntries(Minecraft.getInstance().level, stack).isEmpty());
		if (list.isEmpty() && output.equals("outputs")) {
			ItemStack outputStack = outputsCropDropsOutput.copy();
			if (!outputStack.isEmpty() && outputStack.getItem() != Items.AIR)
				list.add(outputStack);
		}
		return list;
	}

	public @NotNull ItemStack getCropResult(String output) {
		List<ItemStack> list = getCropResults(output);
		return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
	}

	public @NotNull List<ItemStack> getSpawnEggResults(String output) {
		return RecipeUtils.collectMatchingStacks(Arrays.asList(seedItemInput, soilItemInput, generatorItemInput), stack -> RecipeUtils.isSpawnEgg(stack));
	}

	public @NotNull ItemStack getSpawnEggResult(String output) {
		List<ItemStack> list = getSpawnEggResults(output);
		return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
	}

	public @NotNull ItemStack getItemStackResult(String output) {
		ItemStack sapling = getSaplingResult(output);
		if (!sapling.isEmpty()) {
			return sapling;
		}
		ItemStack crop = getCropResult(output);
		if (!crop.isEmpty()) {
			return crop;
		}
		ItemStack entityDrop = getSpawnEggResult(output);
		if (!entityDrop.isEmpty()) {
			return entityDrop;
		}
		return ItemStack.EMPTY;
	}

	public @NotNull FluidStack getFluidStackResult(String output) {
		return FluidStack.EMPTY;
	}

	public boolean getBooleanResult(String output) {
		return false;
	}

	public double getDoubleResult(String output) {
		if (output.equals("duration")) {
			return durationNumberOutput;
		}
		return 0.0d;
	}

	public @NotNull String getStringResult(String output) {
		return "";
	}

	public boolean getBooleanInput(String input) {
		return false;
	}

	public double getDoubleInput(String input) {
		if (input.equals("seed") || input.equals("seed_count") || input.equals("count") || input.equals("amount")) {
			return (double) RecipeUtils.getItemStacks(seedItemInput).stream().mapToInt(ItemStack::getCount).findFirst().orElse(1);
		}
		if (input.equals("soil") || input.equals("soil_count") || input.equals("count") || input.equals("amount")) {
			return (double) RecipeUtils.getItemStacks(soilItemInput).stream().mapToInt(ItemStack::getCount).findFirst().orElse(1);
		}
		if (input.equals("generator") || input.equals("generator_count") || input.equals("count") || input.equals("amount")) {
			return (double) RecipeUtils.getItemStacks(generatorItemInput).stream().mapToInt(ItemStack::getCount).findFirst().orElse(1);
		}
		return 0.0d;
	}

	public @NotNull String getStringInput(String input) {
		return "";
	}

	public static final MapCodec<CropDropsRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(IngredientCodecs.LENIENT.fieldOf("seed").forGetter(CropDropsRecipe::seedItemInput),
			IngredientCodecs.LENIENT.fieldOf("soil").forGetter(CropDropsRecipe::soilItemInput), IngredientCodecs.LENIENT.fieldOf("generator").forGetter(CropDropsRecipe::generatorItemInput),
			ItemStack.CODEC.fieldOf("outputs").forGetter(CropDropsRecipe::outputsCropDropsOutput), Codec.DOUBLE.optionalFieldOf("duration", 1200d).forGetter(CropDropsRecipe::durationNumberOutput),
			DropEntry.CODEC.listOf().optionalFieldOf("drops", List.of()).forGetter(CropDropsRecipe::drops)).apply(instance, CropDropsRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, CropDropsRecipe> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
	public static final RecipeSerializer<CropDropsRecipe> SERIALIZER = Serializers.of(CODEC, STREAM_CODEC);

	@Override
	public boolean matches(@NotNull RecipeInput pContainer, @NotNull Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(@NotNull RecipeInput input, @NotNull HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}
}
