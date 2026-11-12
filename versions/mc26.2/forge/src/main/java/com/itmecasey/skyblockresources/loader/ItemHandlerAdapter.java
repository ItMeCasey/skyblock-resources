package com.itmecasey.skyblockresources.loader;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/** {@code ResourceHandler<ItemResource>} over a Forge {@link IItemHandler}. */
final class ItemHandlerAdapter implements ResourceHandler<ItemResource> {
	private final IItemHandler handler;

	ItemHandlerAdapter(IItemHandler handler) {
		this.handler = handler;
	}

	@Override
	public int size() {
		return handler.getSlots();
	}

	@Override
	public ItemResource getResource(int index) {
		return ItemResource.of(handler.getStackInSlot(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return handler.getStackInSlot(index).getCount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return resource.isEmpty() ? 0 : Math.min(handler.getSlotLimit(index), resource.getMaxStackSize());
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return !resource.isEmpty() && handler.isItemValid(index, resource.toStack());
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, Transaction transaction) {
		if (resource.isEmpty() || amount <= 0 || index < 0 || index >= handler.getSlots()) {
			return 0;
		}
		transaction.touch(this, this::snapshot);
		ItemStack remainder = handler.insertItem(index, resource.toStack(amount), false);
		return amount - remainder.getCount();
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, Transaction transaction) {
		if (resource.isEmpty() || amount <= 0 || index < 0 || index >= handler.getSlots()) {
			return 0;
		}
		if (!resource.matches(handler.getStackInSlot(index))) {
			return 0;
		}
		transaction.touch(this, this::snapshot);
		return handler.extractItem(index, amount, false).getCount();
	}

	/** Copies every slot now and returns the step that puts them back. */
	private Runnable snapshot() {
		int slots = handler.getSlots();
		ItemStack[] before = new ItemStack[slots];
		for (int slot = 0; slot < slots; slot++) {
			before[slot] = handler.getStackInSlot(slot).copy();
		}
		return () -> {
			for (int slot = 0; slot < Math.min(slots, handler.getSlots()); slot++) {
				ItemStack now = handler.getStackInSlot(slot);
				if (ItemStack.matches(now, before[slot])) {
					continue;
				}
				if (handler instanceof IItemHandlerModifiable modifiable) {
					modifiable.setStackInSlot(slot, before[slot].copy());
				} else if (now.getCount() > before[slot].getCount() && (before[slot].isEmpty() || ItemStack.isSameItemSameComponents(now, before[slot]))) {
					// A handler we cannot write to directly: take back what was added.
					handler.extractItem(slot, now.getCount() - before[slot].getCount(), false);
				} else if (before[slot].getCount() > now.getCount() && (now.isEmpty() || ItemStack.isSameItemSameComponents(now, before[slot]))) {
					// ... and give back what was taken.
					handler.insertItem(slot, before[slot].copyWithCount(before[slot].getCount() - now.getCount()), false);
				}
			}
		};
	}
}
