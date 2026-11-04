package com.itmecasey.skyblockresources.loader;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

/** A fluid type, without an amount. Mirrors NeoForge's FluidResource over a Forge FluidStack. */
public final class FluidResource implements Resource {
	public static final FluidResource EMPTY = new FluidResource(FluidStack.EMPTY);

	private final FluidStack prototype;

	private FluidResource(FluidStack prototype) {
		this.prototype = prototype;
	}

	public static FluidResource of(Fluid fluid) {
		return fluid == null || fluid == Fluids.EMPTY ? EMPTY : new FluidResource(new FluidStack(fluid, 1));
	}

	public static FluidResource of(FluidStack stack) {
		return stack.isEmpty() ? EMPTY : new FluidResource(new FluidStack(stack, 1));
	}

	@Override
	public boolean isEmpty() {
		return prototype.isEmpty();
	}

	public Fluid getFluid() {
		return prototype.getFluid();
	}

	public FluidStack toStack(int amount) {
		return prototype.isEmpty() || amount <= 0 ? FluidStack.EMPTY : new FluidStack(prototype, amount);
	}

	public boolean matches(FluidStack stack) {
		return !stack.isEmpty() && prototype.isFluidEqual(stack);
	}
}
