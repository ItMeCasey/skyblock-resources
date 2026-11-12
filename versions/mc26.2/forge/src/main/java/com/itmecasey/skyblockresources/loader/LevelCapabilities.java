package com.itmecasey.skyblockresources.loader;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jspecify.annotations.Nullable;

/**
 * Looks a capability up on the block entity at a position, like NeoForge's
 * {@code level.getCapability(cap, pos, side)}. Forge only has capabilities on block entities, not on bare blocks.
 */
public final class LevelCapabilities {
	private LevelCapabilities() {
	}

	@Nullable
	public static <T> T get(Level level, BlockCapability<T> capability, BlockPos pos, @Nullable Direction side) {
		BlockEntity entity = level.getBlockEntity(pos);
		if (entity == null) {
			return null;
		}
		return entity.getCapability(capability.forgeCapability(), side).resolve().map(handler -> capability.wrap(handler)).orElse(null);
	}
}
