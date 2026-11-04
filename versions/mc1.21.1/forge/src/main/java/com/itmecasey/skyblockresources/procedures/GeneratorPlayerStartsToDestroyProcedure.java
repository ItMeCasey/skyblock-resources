package com.itmecasey.skyblockresources.procedures;

import com.itmecasey.skyblockresources.compat.BlockEntityData;
import com.itmecasey.skyblockresources.compat.NbtCompat;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

import com.itmecasey.skyblockresources.network.SkyblockResourcesModVariables;

public class GeneratorPlayerStartsToDestroyProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		BlockEntity blockEntity = world.getBlockEntity(BlockPos.containing(x, y, z));
		SkyblockResourcesModVariables.MapVariables variables = SkyblockResourcesModVariables.MapVariables.get(world);
		variables.GLOBAL_ITEMS = blockEntity == null ? 0.0 : NbtCompat.getDoubleOr(BlockEntityData.of(blockEntity), "items", 0.0);
		variables.GLOBAL_RESOURCE_NAME = blockEntity == null ? "" : NbtCompat.getStringOr(BlockEntityData.of(blockEntity), "resource_name", "");
		variables.GLOBAL_ITEM_UUID = blockEntity == null ? "" : NbtCompat.getStringOr(BlockEntityData.of(blockEntity), "item_uuid", "");
		variables.markSyncDirty();
	}
}
