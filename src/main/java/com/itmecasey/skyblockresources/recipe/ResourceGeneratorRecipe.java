package com.itmecasey.skyblockresources.recipe;

import org.jetbrains.annotations.NotNull;

import net.neoforged.neoforge.fluids.FluidStack;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.ItemStackTemplate;
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

public record ResourceGeneratorRecipe(Ingredient generatorItemInput, Ingredient resourceItemInput, ItemStackTemplate outputItemOutput, int outputMinCount, int outputMaxCount, double outputChance,
		double durationNumberOutput) implements Recipe<RecipeInput> {
	@Override
	public @NotNull RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
		return SkyblockResourcesModRecipeTypes.RESOURCE_GENERATOR_SERIALIZER.get();
	}

	@Override
	public @NotNull RecipeType<? extends Recipe<RecipeInput>> getType() {
		return SkyblockResourcesModRecipeTypes.RESOURCE_GENERATOR_TYPE.get();
	}

	public @NotNull List<ItemStack> getSaplingResults(String output) {
		return RecipeUtils.collectMatchingStacks(Arrays.asList(generatorItemInput, resourceItemInput), stack -> RecipeUtils.isSapling(stack) && !RecipeUtils.getTreeDropEntries(Minecraft.getInstance().level, stack).isEmpty());
	}

	public @NotNull ItemStack getSaplingResult(String output) {
		List<ItemStack> list = getSaplingResults(output);
		return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
	}

	public @NotNull List<ItemStack> getCropResults(String output) {
		return RecipeUtils.collectMatchingStacks(Arrays.asList(generatorItemInput, resourceItemInput), stack -> RecipeUtils.isCropOrSeed(stack) && !RecipeUtils.getCropDropEntries(Minecraft.getInstance().level, stack).isEmpty());
	}

	public @NotNull ItemStack getCropResult(String output) {
		List<ItemStack> list = getCropResults(output);
		return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
	}

	public @NotNull List<ItemStack> getSpawnEggResults(String output) {
		return RecipeUtils.collectMatchingStacks(Arrays.asList(generatorItemInput, resourceItemInput), stack -> RecipeUtils.isSpawnEgg(stack));
	}

	public @NotNull ItemStack getSpawnEggResult(String output) {
		List<ItemStack> list = getSpawnEggResults(output);
		return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
	}

	public @NotNull ItemStack getItemStackResult(String output) {
		if (output.equals("output")) {
			return outputItemOutput.create();
		}
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
		if (output.equals("output") || output.equals("output_count")) {
			if (outputMinCount != outputMaxCount) {
				return 0.0d;
			}
			return (double) getItemStackResult("output").getCount();
		}
		if (output.equals("output_min_count") || output.equals("min_count")) {
			return (double) outputMinCount;
		}
		if (output.equals("output_max_count") || output.equals("max_count")) {
			return (double) outputMaxCount;
		}
		if (output.equals("output_chance") || output.equals("chance")) {
			return outputChance;
		}
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
		if (input.equals("generator") || input.equals("generator_count") || input.equals("count") || input.equals("amount")) {
			return (double) RecipeUtils.getItemStacks(generatorItemInput).stream().mapToInt(ItemStack::getCount).findFirst().orElse(1);
		}
		if (input.equals("resource") || input.equals("resource_count") || input.equals("count") || input.equals("amount")) {
			return (double) RecipeUtils.getItemStacks(resourceItemInput).stream().mapToInt(ItemStack::getCount).findFirst().orElse(1);
		}
		return 0.0d;
	}

	public @NotNull String getStringInput(String input) {
		return "";
	}

	public static final MapCodec<ResourceGeneratorRecipe> CODEC = RecordCodecBuilder
			.mapCodec(instance -> instance.group(Ingredient.CODEC.fieldOf("generator").forGetter(ResourceGeneratorRecipe::generatorItemInput), Ingredient.CODEC.fieldOf("resource").forGetter(ResourceGeneratorRecipe::resourceItemInput),
					ItemStackTemplate.CODEC.fieldOf("output").forGetter(ResourceGeneratorRecipe::outputItemOutput), Codec.INT.optionalFieldOf("output_min_count", 0).forGetter(ResourceGeneratorRecipe::outputMinCount),
					Codec.INT.optionalFieldOf("output_max_count", 0).forGetter(ResourceGeneratorRecipe::outputMaxCount), Codec.DOUBLE.optionalFieldOf("output_chance", 1.0d).forGetter(ResourceGeneratorRecipe::outputChance),
					Codec.DOUBLE.optionalFieldOf("duration", 100d).forGetter(ResourceGeneratorRecipe::durationNumberOutput)).apply(instance, ResourceGeneratorRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ResourceGeneratorRecipe> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
	public static final RecipeSerializer<ResourceGeneratorRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	@Override
	public boolean matches(@NotNull RecipeInput pContainer, @NotNull Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(@NotNull RecipeInput input) {
		return ItemStack.EMPTY;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}
}