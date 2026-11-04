package com.itmecasey.skyblockresources.loader;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * A fluid ingredient with an amount, like NeoForge's SizedFluidIngredient. This mod's own recipes have no fluid
 * inputs, so nothing creates one on Forge; the recipe helpers still know how to read one, as they do on NeoForge.
 */
public record SizedFluidIngredient(FluidIngredient ingredient, int amount) {
	public record FluidIngredient(List<Holder<Fluid>> fluids) {
	}

	public boolean test(FluidStack stack) {
		return ingredient.fluids().stream().anyMatch(holder -> holder.value() == stack.getFluid()) && stack.getAmount() >= amount;
	}
}
