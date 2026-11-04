package com.itmecasey.skyblockresources.procedures;

import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.items.IItemHandler;
import com.itmecasey.skyblockresources.compat.Transfer;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import com.itmecasey.skyblockresources.configuration.ConfigurationConfiguration;

import java.util.Locale;
import static com.itmecasey.skyblockresources.machine.MachineHelpers.*;

public class WoodenGeneratorOnTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		BlockPos pos = BlockPos.containing(x, y, z);
		if (!getBlockString(world, pos, "resource_name").equals("empty") && !isInventoryFull(world, pos)) {
			if (getBlockNumber(world, pos, "timer") < getBlockNumber(world, pos, "duration")) {
				setBlockNumber(world, pos, "timer", getBlockNumber(world, pos, "timer") + 1);
			} else {
				setBlockNumber(world, pos, "timer", 0);
				Item resourceItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(getBlockString(world, pos, "resource_name").toLowerCase(Locale.ENGLISH)));
				insertItem(world, pos, resourceItem, (int) getBlockNumber(world, pos, "count"));
			}
		}

		if (getBlockNumber(world, pos, "output_timer") < ConfigurationConfiguration.WOODEN_MAX_TICKS.get()) {
			setBlockNumber(world, pos, "output_timer", getBlockNumber(world, pos, "output_timer") + 1);
		} else {
			setBlockNumber(world, pos, "output_timer", 0);
			Direction outputDirection = TextToDirectionProcedure.execute(getBlockString(world, pos, "output_direction"));
			pushItemToSide(world, pos, outputDirection, (int) (double) ConfigurationConfiguration.WOODEN_MAX_TRANSFER.get());
		}
	}

	private static boolean isInventoryFull(LevelAccessor level, BlockPos pos) {
		return Transfer.isFull(level, pos);
	}

	// Inserts into the first slot that accepts the item and returns how many were placed.
	// Moves items from this block into the neighbor on `dir`, if that side is enabled for items.
	private static int pushItemToSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
		if (!isItemOutputAllowed(level, pos, dir))
			return 0;
		IItemHandler source = Transfer.items(level, pos, dir);
		IItemHandler target = Transfer.items(level, pos.relative(dir), dir.getOpposite());
		if (source == null || target == null)
			return 0;
		return Transfer.moveItems(source, target, maxAmount, -1);
	}

	// Item output is allowed on a side only when the block has an item capability
	// and the side was explicitly enabled for output in its BlockDirections data.
}
