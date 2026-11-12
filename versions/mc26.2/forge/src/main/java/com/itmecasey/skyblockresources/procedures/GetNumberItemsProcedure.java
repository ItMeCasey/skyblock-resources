package com.itmecasey.skyblockresources.procedures;

import net.minecraft.world.level.Level;
import com.itmecasey.skyblockresources.loader.PersistentData;
import com.itmecasey.skyblockresources.loader.ItemUtil;
import com.itmecasey.skyblockresources.loader.ItemResource;
import com.itmecasey.skyblockresources.loader.ResourceHandler;
import com.itmecasey.skyblockresources.loader.LevelCapabilities;
import com.itmecasey.skyblockresources.loader.Capabilities;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

public class GetNumberItemsProcedure {
	public static double execute(LevelAccessor world, double x, double y, double z) {
		BlockPos pos = BlockPos.containing(x, y, z);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		double storedItems = blockEntity == null ? 0.0 : PersistentData.of(blockEntity).getDoubleOr("items", 0.0);
		return itemFromBlockInventory(world, pos, 0).getCount() + storedItems;
	}

	private static ItemStack itemFromBlockInventory(LevelAccessor world, BlockPos pos, int slot) {
		if (world instanceof Level extension) {
			ResourceHandler<ItemResource> itemHandler = LevelCapabilities.get(extension, Capabilities.Item.BLOCK, pos, null);
			if (itemHandler != null)
				return ItemUtil.getStack(itemHandler, slot);
		}
		return ItemStack.EMPTY;
	}
}
