package com.itmecasey.skyblockresources.procedures;

import net.minecraft.world.level.Level;
import com.itmecasey.skyblockresources.loader.ItemUtil;
import com.itmecasey.skyblockresources.loader.ItemResource;
import com.itmecasey.skyblockresources.loader.ResourceHandler;
import com.itmecasey.skyblockresources.loader.LevelCapabilities;
import com.itmecasey.skyblockresources.loader.Capabilities;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

public class PlaceItemStackInInventoryProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, ItemStack stack) {
		BlockPos pos = BlockPos.containing(x, y, z);
		for (int slot = 0; slot < getBlockInventorySlotCount(world, pos); slot++) {
			if (canInsertInBlockInventory(world, pos, slot, stack.getCount(), stack)) {
				insertInBlockInventory(world, pos, slot, stack.getCount(), stack, false);
				stack.shrink(stack.getCount());
			}
		}
	}

	private static int getBlockInventorySlotCount(LevelAccessor world, BlockPos pos) {
		if (world instanceof Level extension) {
			ResourceHandler<ItemResource> itemHandler = LevelCapabilities.get(extension, Capabilities.Item.BLOCK, pos, null);
			if (itemHandler != null)
				return itemHandler.size();
		}
		return 0;
	}

	public static boolean canInsertInBlockInventory(LevelAccessor world, BlockPos pos, int slotId, int amount, ItemStack itemstack) {
		if (world instanceof Level extension) {
			ResourceHandler<ItemResource> itemHandler = LevelCapabilities.get(extension, Capabilities.Item.BLOCK, pos, null);
			if (itemHandler != null && slotId >= 0 && slotId < itemHandler.size()) {
				ItemStack inserted = itemstack.copy();
				inserted.setCount(amount);
				return ItemUtil.insertItemReturnRemaining(itemHandler, slotId, inserted, true, null).getCount() == 0;
			}
		}
		return false;
	}

	private static int insertInBlockInventory(LevelAccessor world, BlockPos pos, int slotId, int amount, ItemStack itemstack, boolean simulate) {
		if (world instanceof Level extension) {
			ResourceHandler<ItemResource> itemHandler = LevelCapabilities.get(extension, Capabilities.Item.BLOCK, pos, null);
			if (itemHandler != null && slotId >= 0 && slotId < itemHandler.size()) {
				ItemStack inserted = itemstack.copy();
				inserted.setCount(amount);
				return ItemUtil.insertItemReturnRemaining(itemHandler, slotId, inserted, simulate, null).getCount();
			}
		}
		return itemstack.getCount();
	}
}
