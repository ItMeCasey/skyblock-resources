package com.itmecasey.skyblockresources.procedures;

import com.itmecasey.skyblockresources.compat.NbtCompat;

import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.items.IItemHandler;
import com.itmecasey.skyblockresources.compat.Transfer;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

public class GetNumberItemsProcedure {
	public static double execute(LevelAccessor world, double x, double y, double z) {
		BlockPos pos = BlockPos.containing(x, y, z);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		double storedItems = blockEntity == null ? 0.0 : NbtCompat.getDoubleOr(blockEntity.getPersistentData(), "items", 0.0);
		return itemFromBlockInventory(world, pos, 0).getCount() + storedItems;
	}

	private static ItemStack itemFromBlockInventory(LevelAccessor world, BlockPos pos, int slot) {
		if (world instanceof ILevelExtension extension) {
			IItemHandler itemHandler = extension.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (itemHandler != null)
				return itemHandler.getStackInSlot(slot);
		}
		return ItemStack.EMPTY;
	}
}
