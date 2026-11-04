package com.itmecasey.skyblockresources.procedures;

import com.itmecasey.skyblockresources.compat.NbtCompat;

import net.minecraftforge.items.IItemHandler;
import com.itmecasey.skyblockresources.compat.Transfer;

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
		if (world != null) {
			LevelAccessor extension = world;
			IItemHandler itemHandler = Transfer.items(extension, pos, null);
			if (itemHandler != null)
				return itemHandler.getStackInSlot(slot);
		}
		return ItemStack.EMPTY;
	}
}
