package com.itmecasey.skyblockresources.compat;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * The 1.21.1 capability API (IItemHandler, IFluidHandler, IEnergyStorage) behind the same small set of
 * operations the 26.1.2 code does with ResourceHandler and Transaction. Every move simulates first, so
 * nothing is lost when the target takes less than the source gave.
 */
public final class Transfer {
	private Transfer() {
	}

	@Nullable
	private static <T> T capability(LevelAccessor level, BlockPos pos, Capability<T> capability, @Nullable Direction side) {
		BlockEntity be = level.getBlockEntity(pos);
		return be == null ? null : be.getCapability(capability, side).orElse(null);
	}

	@Nullable
	public static IItemHandler items(LevelAccessor level, BlockPos pos, @Nullable Direction side) {
		return capability(level, pos, ForgeCapabilities.ITEM_HANDLER, side);
	}

	@Nullable
	public static IFluidHandler fluids(LevelAccessor level, BlockPos pos, @Nullable Direction side) {
		return capability(level, pos, ForgeCapabilities.FLUID_HANDLER, side);
	}

	@Nullable
	public static IEnergyStorage energy(LevelAccessor level, BlockPos pos, @Nullable Direction side) {
		return capability(level, pos, ForgeCapabilities.ENERGY, side);
	}

	/** Capacity of one slot for the item it holds (or for a full stack when empty). */
	public static int slotCapacity(IItemHandler handler, int slot) {
		ItemStack held = handler.getStackInSlot(slot);
		int limit = handler.getSlotLimit(slot);
		return held.isEmpty() ? limit : Math.min(limit, held.getMaxStackSize());
	}

	/** True when every slot holds its item's maximum. An empty slot is never full. */
	public static boolean isFull(@Nullable IItemHandler inv) {
		if (inv == null || inv.getSlots() == 0)
			return false;
		for (int s = 0; s < inv.getSlots(); s++) {
			ItemStack held = inv.getStackInSlot(s);
			if (held.isEmpty())
				return false;
			if (held.getCount() < slotCapacity(inv, s))
				return false;
		}
		return true;
	}

	public static boolean isFull(LevelAccessor level, BlockPos pos) {
		return isFull(items(level, pos, null));
	}

	/** Inserts {@code amount} of {@code item}, in stacks, into any slot (or only {@code slot} when it is valid). Returns how many went in. */
	public static int insert(IItemHandler inv, Item item, int amount, int slot) {
		int inserted = 0;
		int left = amount;
		int stackMax = new ItemStack(item).getMaxStackSize();
		while (left > 0) {
			ItemStack chunk = new ItemStack(item, Math.min(left, stackMax));
			ItemStack remainder;
			if (slot >= 0 && slot < inv.getSlots())
				remainder = inv.insertItem(slot, chunk, false);
			else
				remainder = ItemHandlerHelper.insertItem(inv, chunk, false);
			int done = chunk.getCount() - remainder.getCount();
			if (done <= 0)
				break;
			inserted += done;
			left -= done;
		}
		return inserted;
	}

	/** Moves up to {@code max} items from {@code src} to {@code dst}. A valid {@code slot} limits the source to that slot. Returns the count moved. */
	public static int moveItems(IItemHandler src, IItemHandler dst, int max, int slot) {
		return moveItems(src, dst, max, slot, -1);
	}

	/** Same, and a valid {@code dstSlot} makes the target take the items in that slot only. */
	public static int moveItems(IItemHandler src, IItemHandler dst, int max, int slot, int dstSlot) {
		int remaining = max;
		int total = 0;
		int from = 0;
		int to = src.getSlots();
		if (slot >= 0) {
			if (slot >= src.getSlots())
				return 0;
			from = slot;
			to = slot + 1;
		}
		for (int s = from; s < to && remaining > 0; s++) {
			ItemStack probe = src.extractItem(s, remaining, true);
			if (probe.isEmpty())
				continue;
			ItemStack left = put(dst, dstSlot, probe.copy(), true);
			int fits = probe.getCount() - left.getCount();
			if (fits <= 0)
				continue;
			ItemStack taken = src.extractItem(s, fits, false);
			if (taken.isEmpty())
				continue;
			ItemStack rest = put(dst, dstSlot, taken, false);
			if (!rest.isEmpty())
				src.insertItem(s, rest, false);
			int moved = taken.getCount() - rest.getCount();
			total += moved;
			remaining -= moved;
			if (slot >= 0 && moved > 0)
				return total;
		}
		return total;
	}

	private static ItemStack put(IItemHandler dst, int dstSlot, ItemStack stack, boolean simulate) {
		if (dstSlot >= 0 && dstSlot < dst.getSlots())
			return dst.insertItem(dstSlot, stack, simulate);
		return ItemHandlerHelper.insertItem(dst, stack, simulate);
	}

	public static int insertFluid(IFluidHandler handler, Fluid fluid, int amount) {
		return handler.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
	}

	/** Moves fluid from the first tank that gives some. Returns the amount moved. */
	public static int moveFluid(IFluidHandler src, IFluidHandler dst, int max) {
		for (int i = 0; i < src.getTanks(); i++) {
			FluidStack held = src.getFluidInTank(i);
			if (held.isEmpty() || held.getAmount() <= 0)
				continue;
			FluidStack probe = src.drain(new FluidStack(held.getFluid(), Math.min(max, held.getAmount())), IFluidHandler.FluidAction.SIMULATE);
			if (probe.isEmpty())
				continue;
			int fits = dst.fill(probe, IFluidHandler.FluidAction.SIMULATE);
			if (fits <= 0)
				continue;
			FluidStack taken = src.drain(new FluidStack(held.getFluid(), fits), IFluidHandler.FluidAction.EXECUTE);
			if (taken.isEmpty())
				continue;
			int filled = dst.fill(taken, IFluidHandler.FluidAction.EXECUTE);
			if (filled < taken.getAmount()) {
				FluidStack back = taken.copy();
				back.setAmount(taken.getAmount() - filled);
				src.fill(back, IFluidHandler.FluidAction.EXECUTE);
			}
			if (filled > 0)
				return filled;
		}
		return 0;
	}

	public static int moveEnergy(IEnergyStorage src, IEnergyStorage dst, int max) {
		int probe = src.extractEnergy(max, true);
		if (probe <= 0)
			return 0;
		int fits = dst.receiveEnergy(probe, true);
		if (fits <= 0)
			return 0;
		int taken = src.extractEnergy(fits, false);
		return dst.receiveEnergy(taken, false);
	}
}
