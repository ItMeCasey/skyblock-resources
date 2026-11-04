package com.itmecasey.skyblockresources.procedures;

import com.itmecasey.skyblockresources.compat.BlockEntityData;
import com.itmecasey.skyblockresources.compat.NbtCompat;

import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.items.IItemHandler;
import com.itmecasey.skyblockresources.compat.Transfer;
import com.itmecasey.skyblockresources.compat.Transfer;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import java.util.Locale;

public class LoadInventoryProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		BlockPos pos = BlockPos.containing(x, y, z);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		double blocksToGet = 64 - itemFromBlockInventory(world, pos, 0).getCount();
		double blocksAvailable = blockEntity == null ? 0.0 : NbtCompat.getDoubleOr(BlockEntityData.of(blockEntity), "items", 0.0);
		if (blocksAvailable < blocksToGet)
			blocksToGet = blocksAvailable;
		if (blocksToGet <= 0)
			return;

		if (!world.isClientSide()) {
			if (blockEntity != null)
				BlockEntityData.of(blockEntity).putDouble("items", blocksAvailable - blocksToGet);
			BlockState state = world.getBlockState(pos);
			if (world instanceof Level level)
				level.sendBlockUpdated(pos, state, state, 3);
		}

		if (world instanceof ServerLevel serverLevel && serverLevel.getBlockEntity(pos) instanceof Container container) {
			String resourceName = NbtCompat.getStringOr(BlockEntityData.of(blockEntity), "resource_name", "");
			ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(resourceName.toLowerCase(Locale.ENGLISH))));
			stack.setCount((int) (itemFromBlockInventory(world, pos, 0).getCount() + blocksToGet));
			container.setItem(0, stack);
		}
	}

	private static ItemStack itemFromBlockInventory(LevelAccessor world, BlockPos pos, int slot) {
		if (world instanceof LevelAccessor extension) {
			IItemHandler itemHandler = Transfer.items(extension, pos, null);
			if (itemHandler != null)
				return itemHandler.getStackInSlot(slot);
		}
		return ItemStack.EMPTY;
	}
}
