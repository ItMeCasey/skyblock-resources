package com.itmecasey.skyblockresources.procedures;

import com.itmecasey.skyblockresources.loader.PersistentData;
import com.itmecasey.skyblockresources.loader.ItemUtil;
import com.itmecasey.skyblockresources.loader.ItemResource;
import com.itmecasey.skyblockresources.loader.ResourceHandler;
import com.itmecasey.skyblockresources.loader.LevelCapabilities;
import com.itmecasey.skyblockresources.loader.Capabilities;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import java.util.Locale;

public class LoadInventoryProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		BlockPos pos = BlockPos.containing(x, y, z);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		double blocksToGet = 64 - itemFromBlockInventory(world, pos, 0).getCount();
		double blocksAvailable = blockEntity == null ? 0.0 : PersistentData.of(blockEntity).getDoubleOr("items", 0.0);
		if (blocksAvailable < blocksToGet)
			blocksToGet = blocksAvailable;
		if (blocksToGet <= 0)
			return;

		if (!world.isClientSide()) {
			if (blockEntity != null)
				PersistentData.of(blockEntity).putDouble("items", blocksAvailable - blocksToGet);
			BlockState state = world.getBlockState(pos);
			if (world instanceof Level level)
				level.sendBlockUpdated(pos, state, state, 3);
		}

		if (world instanceof ServerLevel serverLevel && serverLevel.getBlockEntity(pos) instanceof Container container) {
			String resourceName = PersistentData.of(blockEntity).getStringOr("resource_name", "");
			ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(resourceName.toLowerCase(Locale.ENGLISH))));
			stack.setCount((int) (itemFromBlockInventory(world, pos, 0).getCount() + blocksToGet));
			container.setItem(0, stack);
		}
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
