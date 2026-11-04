package com.itmecasey.skyblockresources.loader;

import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

/** The few ItemUtil helpers the machines use, with NeoForge's signatures. */
public final class ItemUtil {
	private ItemUtil() {
	}

	public static ItemStack getStack(ResourceHandler<ItemResource> handler, int index) {
		ItemResource resource = handler.getResource(index);
		if (resource.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return resource.toStack(handler.getAmountAsInt(index));
	}

	/** Inserts into one slot and returns what did not fit. With {@code simulate} nothing is kept. */
	public static ItemStack insertItemReturnRemaining(ResourceHandler<ItemResource> handler, int index, ItemStack stack, boolean simulate, @Nullable Transaction transaction) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		try (Transaction tx = Transaction.open(transaction)) {
			int inserted = handler.insert(index, ItemResource.of(stack), stack.getCount(), tx);
			if (!simulate) {
				tx.commit();
			}
			int leftover = stack.getCount() - inserted;
			return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(leftover);
		}
	}

	/** Inserts into any slot and returns what did not fit. */
	public static ItemStack insertItemReturnRemaining(ResourceHandler<ItemResource> handler, ItemStack stack, boolean simulate, @Nullable Transaction transaction) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		try (Transaction tx = Transaction.open(transaction)) {
			int inserted = handler.insert(ItemResource.of(stack), stack.getCount(), tx);
			if (!simulate) {
				tx.commit();
			}
			int leftover = stack.getCount() - inserted;
			return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(leftover);
		}
	}
}
