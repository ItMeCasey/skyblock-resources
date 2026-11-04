package com.itmecasey.skyblockresources.loader;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

/** {@code ResourceHandler<FluidResource>} over a Forge {@link IFluidHandler}; tanks are the slots. */
final class FluidHandlerAdapter implements ResourceHandler<FluidResource> {
	private final IFluidHandler handler;

	FluidHandlerAdapter(IFluidHandler handler) {
		this.handler = handler;
	}

	@Override
	public int size() {
		return handler.getTanks();
	}

	@Override
	public FluidResource getResource(int index) {
		return FluidResource.of(handler.getFluidInTank(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return handler.getFluidInTank(index).getAmount();
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		return handler.getTankCapacity(index);
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return !resource.isEmpty() && handler.isFluidValid(index, resource.toStack(1));
	}

	/** Forge fills a handler, not one of its tanks, so the index only decides whether the tank accepts the fluid. */
	@Override
	public int insert(int index, FluidResource resource, int amount, Transaction transaction) {
		if (resource.isEmpty() || amount <= 0 || index < 0 || index >= handler.getTanks() || !handler.isFluidValid(index, resource.toStack(1))) {
			return 0;
		}
		return fill(resource, amount, transaction);
	}

	@Override
	public int insert(FluidResource resource, int amount, Transaction transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		return fill(resource, amount, transaction);
	}

	private int fill(FluidResource resource, int amount, Transaction transaction) {
		int filled = handler.fill(resource.toStack(amount), FluidAction.EXECUTE);
		if (filled > 0) {
			transaction.onRollback(() -> handler.drain(resource.toStack(filled), FluidAction.EXECUTE));
		}
		return filled;
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, Transaction transaction) {
		if (resource.isEmpty() || amount <= 0 || index < 0 || index >= handler.getTanks() || !resource.matches(handler.getFluidInTank(index))) {
			return 0;
		}
		return drain(resource, Math.min(amount, handler.getFluidInTank(index).getAmount()), transaction);
	}

	@Override
	public int extract(FluidResource resource, int amount, Transaction transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		return drain(resource, amount, transaction);
	}

	private int drain(FluidResource resource, int amount, Transaction transaction) {
		FluidStack drained = handler.drain(resource.toStack(amount), FluidAction.EXECUTE);
		if (!drained.isEmpty()) {
			FluidStack taken = drained.copy();
			transaction.onRollback(() -> handler.fill(taken, FluidAction.EXECUTE));
		}
		return drained.getAmount();
	}
}
