package com.itmecasey.skyblockresources.loader;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** An item type plus its data components, without a count. Mirrors NeoForge's ItemResource over a Forge ItemStack. */
public final class ItemResource implements Resource {
	public static final ItemResource EMPTY = new ItemResource(ItemStack.EMPTY);

	private final ItemStack prototype;

	private ItemResource(ItemStack prototype) {
		this.prototype = prototype;
	}

	public static ItemResource of(ItemStack stack) {
		return stack.isEmpty() ? EMPTY : new ItemResource(stack.copyWithCount(1));
	}

	public static ItemResource of(Item item) {
		return of(new ItemStack(item));
	}

	@Override
	public boolean isEmpty() {
		return prototype.isEmpty();
	}

	public Item getItem() {
		return prototype.getItem();
	}

	public ItemStack toStack() {
		return prototype.copy();
	}

	public ItemStack toStack(int count) {
		return prototype.isEmpty() || count <= 0 ? ItemStack.EMPTY : prototype.copyWithCount(count);
	}

	public int getMaxStackSize() {
		return prototype.getMaxStackSize();
	}

	public boolean matches(ItemStack stack) {
		return ItemStack.isSameItemSameComponents(prototype, stack);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof ItemResource resource && (isEmpty() ? resource.isEmpty() : ItemStack.isSameItemSameComponents(prototype, resource.prototype));
	}

	@Override
	public int hashCode() {
		return isEmpty() ? 0 : ItemStack.hashItemAndComponents(prototype);
	}
}
