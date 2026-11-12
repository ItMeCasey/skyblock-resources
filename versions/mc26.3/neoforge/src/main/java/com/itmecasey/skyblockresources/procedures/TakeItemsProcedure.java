package com.itmecasey.skyblockresources.procedures;

import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.Container;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

public class TakeItemsProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		BlockPos pos = BlockPos.containing(x, y, z);
		for (int slot = 0; slot < getBlockInventorySlotCount(world, pos); slot++) {
			if (entity.isShiftKeyDown()) {
				int amount = itemFromBlockInventory(world, pos, slot).getCount();
				if (entity instanceof Player player)
					giveToPlayer(player, itemFromBlockInventory(world, pos, slot), amount);
				removeFromSlot(world, pos, slot, amount);
				playPickupSound(world, x, y, z, pos);
			} else if (itemFromBlockInventory(world, pos, slot).getCount() >= 1) {
				if (entity instanceof Player player)
					giveToPlayer(player, itemFromBlockInventory(world, pos, slot), 1);
				removeFromSlot(world, pos, slot, 1);
				playPickupSound(world, x, y, z, pos);
			}
		}
	}

	private static void giveToPlayer(Player player, ItemStack source, int amount) {
		ItemStack stack = source.copy();
		stack.setCount(amount);
		player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
	}

	private static void removeFromSlot(LevelAccessor world, BlockPos pos, int slot, int amount) {
		if (world instanceof ServerLevel serverLevel && serverLevel.getBlockEntity(pos) instanceof Container container)
			container.getItem(slot).shrink(amount);
	}

	private static void playPickupSound(LevelAccessor world, double x, double y, double z, BlockPos pos) {
		if (!(world instanceof Level level))
			return;
		SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.item.pickup"));
		if (level.isClientSide())
			level.playLocalSound(x, y, z, sound, SoundSource.NEUTRAL, 1, 1, false);
		else
			level.playSound(null, pos, sound, SoundSource.NEUTRAL, 1, 1);
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
}
