package com.itmecasey.skyblockresources.procedures;

import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import com.itmecasey.skyblockresources.configuration.ConfigurationConfiguration;

import java.util.Locale;
import static com.itmecasey.skyblockresources.machine.MachineHelpers.*;

public class NetheriteGeneratorOnTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		BlockPos pos = BlockPos.containing(x, y, z);
		if (!getBlockString(world, pos, "resource_name").equals("empty") && !isInventoryFull(world, pos)) {
			if (getBlockNumber(world, pos, "timer") < getBlockNumber(world, pos, "duration")) {
				setBlockNumber(world, pos, "timer", getBlockNumber(world, pos, "timer") + 1);
			} else {
				setBlockNumber(world, pos, "timer", 0);
				Item resourceItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(getBlockString(world, pos, "resource_name").toLowerCase(Locale.ENGLISH)));
				insertItem(world, pos, resourceItem, (int) getBlockNumber(world, pos, "count"));
			}
		}

		// Netherite reads the diamond transfer/tick settings. This matches the original generated code.
		if (getBlockNumber(world, pos, "output_timer") < ConfigurationConfiguration.NETHERITE_MAX_TICKS.get()) {
			setBlockNumber(world, pos, "output_timer", getBlockNumber(world, pos, "output_timer") + 1);
		} else {
			setBlockNumber(world, pos, "output_timer", 0);
			Direction outputDirection = TextToDirectionProcedure.execute(getBlockString(world, pos, "output_direction"));
			pushItemToSide(world, pos, outputDirection, (int) (double) ConfigurationConfiguration.NETHERITE_MAX_TRANSFER.get());
		}
	}

	private static boolean isInventoryFull(LevelAccessor level, BlockPos pos) {
		if (!(level instanceof ILevelExtension extension))
			return false;
		if (!(extension.getCapability(Capabilities.Item.BLOCK, pos, null) instanceof ResourceHandler<ItemResource> inventory))
			return false;
		if (inventory.size() == 0)
			return false;
		for (int slot = 0; slot < inventory.size(); slot++) {
			var resource = inventory.getResource(slot);
			if (resource.isEmpty())
				return false;
			if (inventory.getAmountAsInt(slot) < inventory.getCapacityAsInt(slot, resource))
				return false;
		}
		return true;
	}

	// Inserts into the first slot that accepts the item and returns how many were placed.
	// Moves items from this block into the neighbor on `dir`, if that side is enabled for items.
	private static int pushItemToSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
		if (!isItemOutputAllowed(level, pos, dir) || !(level instanceof ILevelExtension extension))
			return 0;
		if (!(extension.getCapability(Capabilities.Item.BLOCK, pos, dir) instanceof ResourceHandler<ItemResource> source))
			return 0;
		if (!(extension.getCapability(Capabilities.Item.BLOCK, pos.relative(dir), dir.getOpposite()) instanceof ResourceHandler<ItemResource> target))
			return 0;
		int remainingAmount = maxAmount;
		int totalTransferred = 0;
		try (var tx = Transaction.openRoot()) {
			for (int slot = 0; slot < source.size() && remainingAmount > 0; slot++) {
				var resource = source.getResource(slot);
				if (resource.isEmpty())
					continue;
				int extracted = (int) source.extract(slot, resource, remainingAmount, tx);
				if (extracted > 0) {
					int inserted = (int) target.insert(resource, extracted, tx);
					if (inserted > 0) {
						totalTransferred += inserted;
						remainingAmount -= inserted;
					}
				}
			}
			if (totalTransferred > 0) {
				tx.commit();
				return totalTransferred;
			}
		}
		return 0;
	}

	// Item output is allowed on a side only when the block has an item capability
	// and the side was explicitly enabled for output in its BlockDirections data.
}
