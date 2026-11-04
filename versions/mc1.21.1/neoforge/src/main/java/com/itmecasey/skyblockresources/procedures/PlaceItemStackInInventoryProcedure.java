package com.itmecasey.skyblockresources.procedures;

import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.items.IItemHandler;
import com.itmecasey.skyblockresources.compat.Transfer;
import net.neoforged.neoforge.capabilities.Capabilities;

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
		if (world instanceof ILevelExtension extension) {
			IItemHandler itemHandler = extension.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (itemHandler != null)
				return itemHandler.getSlots();
		}
		return 0;
	}

	public static boolean canInsertInBlockInventory(LevelAccessor world, BlockPos pos, int slotId, int amount, ItemStack itemstack) {
		if (world instanceof ILevelExtension extension) {
			IItemHandler itemHandler = extension.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (itemHandler != null && slotId >= 0 && slotId < itemHandler.getSlots()) {
				ItemStack inserted = itemstack.copy();
				inserted.setCount(amount);
				return itemHandler.insertItem(slotId, inserted, true).getCount() == 0;
			}
		}
		return false;
	}

	private static int insertInBlockInventory(LevelAccessor world, BlockPos pos, int slotId, int amount, ItemStack itemstack, boolean simulate) {
		if (world instanceof ILevelExtension extension) {
			IItemHandler itemHandler = extension.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (itemHandler != null && slotId >= 0 && slotId < itemHandler.getSlots()) {
				ItemStack inserted = itemstack.copy();
				inserted.setCount(amount);
				return itemHandler.insertItem(slotId, inserted, simulate).getCount();
			}
		}
		return itemstack.getCount();
	}
}
