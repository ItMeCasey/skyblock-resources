package com.itmecasey.skyblockresources.compat;

import net.minecraft.world.item.ItemStack;

/** ItemStack helpers newer than 1.20.1. */
public final class Stacks {
	private Stacks() {
	}

	/** {@code ItemStack.copyWithCount}, which 1.20.2 added. */
	public static ItemStack withCount(ItemStack stack, int count) {
		ItemStack copy = stack.copy();
		copy.setCount(count);
		return copy;
	}
}
