package com.itmecasey.skyblockresources.compat;

import java.util.List;

import net.minecraftforge.fluids.FluidStack;

/** NeoForge's SizedFluidIngredient. Forge 1.21.1 has none and nothing here produces one; see {@link SizedIngredient}. */
public record SizedFluidIngredient(List<FluidStack> fluids, int amount) {
	public FluidStack[] getFluids() {
		return fluids.toArray(new FluidStack[0]);
	}

	public boolean test(FluidStack stack) {
		return fluids.stream().anyMatch(f -> f.getFluid() == stack.getFluid()) && stack.getAmount() >= amount;
	}
}
