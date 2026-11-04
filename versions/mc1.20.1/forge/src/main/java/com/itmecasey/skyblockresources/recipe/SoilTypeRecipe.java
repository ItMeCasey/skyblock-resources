package com.itmecasey.skyblockresources.recipe;

import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fluids.FluidStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import com.itmecasey.skyblockresources.compat.IngredientCodecs;
import com.itmecasey.skyblockresources.compat.Serializers;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.ItemStack;

import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;

public record SoilTypeRecipe(ResourceLocation id, Ingredient soilItemInput, double speed_bonusNumberOutput, double growth_multiplierNumberOutput, double bonus_seed_chanceNumberOutput) implements Recipe<Container> {

	public SoilTypeRecipe(Ingredient soilItemInput, double speed_bonusNumberOutput, double growth_multiplierNumberOutput, double bonus_seed_chanceNumberOutput) {
		this(null, soilItemInput, speed_bonusNumberOutput, growth_multiplierNumberOutput, bonus_seed_chanceNumberOutput);
	}

	public SoilTypeRecipe withId(ResourceLocation newId) {
		return new SoilTypeRecipe(newId, soilItemInput, speed_bonusNumberOutput, growth_multiplierNumberOutput, bonus_seed_chanceNumberOutput);
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SkyblockResourcesModRecipeTypes.SOIL_TYPE_SERIALIZER.get();
	}

	@Override
	public @NotNull RecipeType<?> getType() {
		return SkyblockResourcesModRecipeTypes.SOIL_TYPE_TYPE.get();
	}

	public @NotNull List<ItemStack> getSaplingResults(String output) {
		return RecipeUtils.collectMatchingStacks(Arrays.asList(soilItemInput), stack -> RecipeUtils.isSapling(stack) && !RecipeUtils.getTreeDropEntries(Minecraft.getInstance().level, stack).isEmpty());
	}

	public @NotNull ItemStack getSaplingResult(String output) {
		List<ItemStack> list = getSaplingResults(output);
		return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
	}

	public @NotNull List<ItemStack> getCropResults(String output) {
		return RecipeUtils.collectMatchingStacks(Arrays.asList(soilItemInput), stack -> RecipeUtils.isCropOrSeed(stack) && !RecipeUtils.getCropDropEntries(Minecraft.getInstance().level, stack).isEmpty());
	}

	public @NotNull ItemStack getCropResult(String output) {
		List<ItemStack> list = getCropResults(output);
		return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
	}

	public @NotNull List<ItemStack> getSpawnEggResults(String output) {
		return RecipeUtils.collectMatchingStacks(Arrays.asList(soilItemInput), stack -> RecipeUtils.isSpawnEgg(stack));
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
		if (output.equals("speed_bonus")) {
			return speed_bonusNumberOutput;
		}
		if (output.equals("growth_multiplier")) {
			return growth_multiplierNumberOutput;
		}
		if (output.equals("bonus_seed_chance")) {
			return bonus_seed_chanceNumberOutput;
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
		if (input.equals("soil") || input.equals("soil_count") || input.equals("count") || input.equals("amount")) {
			return (double) RecipeUtils.getItemStacks(soilItemInput).stream().mapToInt(ItemStack::getCount).findFirst().orElse(1);
		}
		return 0.0d;
	}

	public @NotNull String getStringInput(String input) {
		return "";
	}

	public static final MapCodec<SoilTypeRecipe> CODEC = RecordCodecBuilder
			.mapCodec(instance -> instance.group(IngredientCodecs.LENIENT.fieldOf("soil").forGetter(SoilTypeRecipe::soilItemInput), Codec.DOUBLE.optionalFieldOf("speed_bonus", 0d).forGetter(SoilTypeRecipe::speed_bonusNumberOutput),
					Codec.DOUBLE.optionalFieldOf("growth_multiplier", 1d).forGetter(SoilTypeRecipe::growth_multiplierNumberOutput),
					Codec.DOUBLE.optionalFieldOf("bonus_seed_chance", 0d).forGetter(SoilTypeRecipe::bonus_seed_chanceNumberOutput)).apply(instance, SoilTypeRecipe::new));
	public static final RecipeSerializer<SoilTypeRecipe> SERIALIZER = Serializers.of(CODEC, SoilTypeRecipe::withId);

	@Override
	public boolean matches(@NotNull Container pContainer, @NotNull Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(@NotNull Container input, @NotNull RegistryAccess registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem(@NotNull RegistryAccess registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}
}
