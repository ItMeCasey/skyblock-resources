package com.itmecasey.skyblockresources.procedures;

import com.itmecasey.skyblockresources.loader.PersistentData;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

import com.itmecasey.skyblockresources.network.SkyblockResourcesModVariables;

public class GeneratorPlayerStartsToDestroyProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		BlockEntity blockEntity = world.getBlockEntity(BlockPos.containing(x, y, z));
		SkyblockResourcesModVariables.MapVariables variables = SkyblockResourcesModVariables.MapVariables.get(world);
		variables.GLOBAL_ITEMS = blockEntity == null ? 0.0 : PersistentData.of(blockEntity).getDoubleOr("items", 0.0);
		variables.GLOBAL_RESOURCE_NAME = blockEntity == null ? "" : PersistentData.of(blockEntity).getStringOr("resource_name", "");
		variables.GLOBAL_ITEM_UUID = blockEntity == null ? "" : PersistentData.of(blockEntity).getStringOr("item_uuid", "");
		variables.markSyncDirty();
	}
}
