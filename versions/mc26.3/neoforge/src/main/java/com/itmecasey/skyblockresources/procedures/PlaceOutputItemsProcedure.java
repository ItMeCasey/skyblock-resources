package com.itmecasey.skyblockresources.procedures;

import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import java.util.Locale;

public class PlaceOutputItemsProcedure {
	public static double execute(LevelAccessor world, double x, double y, double z, double number_of_items, String output_direction, String resource_name) {
		if (output_direction == null || resource_name == null)
			return 0;
		BlockPos target = getOutputPosition(x, y, z, output_direction);
		double remainingItems = number_of_items;
		for (int slot = 0; slot < getBlockInventorySlotCount(world, target) && remainingItems > 0; slot++) {
			Item resourceItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(resource_name.toLowerCase(Locale.ENGLISH)));
			ItemStack slotStack = itemFromBlockInventory(world, target, slot);
			if (slotStack.getItem() == resourceItem || slotStack.getItem() == Blocks.AIR.asItem()) {
				int availableItems = getBlockInventorySlotStackLimit(world, target, slot) - slotStack.getCount();
				if (remainingItems <= availableItems) {
					insertInBlockInventory(world, target, slot, (int) remainingItems, new ItemStack(resourceItem), false);
					remainingItems = 0;
				} else {
					insertInBlockInventory(world, target, slot, availableItems, new ItemStack(resourceItem), false);
					remainingItems -= availableItems;
				}
			}
		}
		return remainingItems;
	}

	private static BlockPos getOutputPosition(double x, double y, double z, String direction) {
		return switch (direction.toUpperCase()) {
			case "DOWN" -> BlockPos.containing(x, y - 1, z);
			case "UP" -> BlockPos.containing(x, y + 1, z);
			case "EAST" -> BlockPos.containing(x + 1, y, z);
			case "WEST" -> BlockPos.containing(x - 1, y, z);
			case "NORTH" -> BlockPos.containing(x, y, z - 1);
			case "SOUTH" -> BlockPos.containing(x, y, z + 1);
			default -> BlockPos.ZERO;
		};
	}

	private static int getBlockInventorySlotCount(LevelAccessor world, BlockPos pos) {
		if (world instanceof ILevelExtension extension) {
			ResourceHandler<ItemResource> itemHandler = extension.getCapability(Capabilities.Item.BLOCK, pos, null);
			if (itemHandler != null)
				return itemHandler.size();
		}
		return 0;
	}

	private static ItemStack itemFromBlockInventory(LevelAccessor world, BlockPos pos, int slot) {
		if (world instanceof ILevelExtension extension) {
			ResourceHandler<ItemResource> itemHandler = extension.getCapability(Capabilities.Item.BLOCK, pos, null);
			if (itemHandler != null)
				return ItemUtil.getStack(itemHandler, slot);
		}
		return ItemStack.EMPTY;
	}

	private static int getBlockInventorySlotStackLimit(LevelAccessor world, BlockPos pos, int slotId) {
		if (world instanceof ILevelExtension extension) {
			ResourceHandler<ItemResource> itemHandler = extension.getCapability(Capabilities.Item.BLOCK, pos, null);
			if (itemHandler != null && slotId >= 0 && slotId < itemHandler.size()) {
				return itemHandler.getCapacityAsInt(slotId, itemHandler.getResource(slotId));
			}
		}
		return 0;
	}

	private static int insertInBlockInventory(LevelAccessor world, BlockPos pos, int slotId, int amount, ItemStack itemstack, boolean simulate) {
		if (world instanceof ILevelExtension extension) {
			ResourceHandler<ItemResource> itemHandler = extension.getCapability(Capabilities.Item.BLOCK, pos, null);
			if (itemHandler != null && slotId >= 0 && slotId < itemHandler.size()) {
				ItemStack inserted = itemstack.copy();
				inserted.setCount(amount);
				return ItemUtil.insertItemReturnRemaining(itemHandler, slotId, inserted, simulate, null).getCount();
			}
		}
		return itemstack.getCount();
	}
}
