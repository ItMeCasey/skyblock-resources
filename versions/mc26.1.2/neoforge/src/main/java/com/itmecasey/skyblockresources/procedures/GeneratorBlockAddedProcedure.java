package com.itmecasey.skyblockresources.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public class GeneratorBlockAddedProcedure {
	private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (world.isClientSide())
			return;
		BlockPos pos = BlockPos.containing(x, y, z);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		BlockState state = world.getBlockState(pos);
		// Only initialize a fresh machine; an upgraded or moved one arrives with its data and must keep it.
		if (blockEntity != null && blockEntity.getPersistentData().contains("resource_name"))
			LOGGER.info("Setup ran on a {} at {} that already has data; keeping it", state.getBlock(), pos, new Throwable());
		if (blockEntity != null && !blockEntity.getPersistentData().contains("resource_name")) {
			blockEntity.getPersistentData().putDouble("timer", 0);
			blockEntity.getPersistentData().putDouble("output_timer", 0);
			blockEntity.getPersistentData().putString("resource_name", "empty");
		}
		if (world instanceof Level level)
			level.sendBlockUpdated(pos, state, state, 3);
	}
}
