package com.itmecasey.skyblockresources.item;

import com.itmecasey.skyblockresources.loader.PersistentData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import com.itmecasey.skyblockresources.SkyblockResourcesMod;
import com.itmecasey.skyblockresources.machine.MachineHelpers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ScrewdriverItem extends Item {
	public ScrewdriverItem(Item.Properties properties) {
		super(properties.stacksTo(1));
	}

	/**
	 * Sneak-clicking a side of a generator sets that side as its output. Sneaking skips the block's
	 * own right-click, so this has to happen on the item.
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level level = context.getLevel();
		if (player == null || !player.isShiftKeyDown())
			return InteractionResult.PASS;

		BlockPos pos = context.getClickedPos();
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null || !BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getNamespace().equals(SkyblockResourcesMod.MODID))
			return InteractionResult.PASS;

		if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()))
			return InteractionResult.FAIL;
		if (level.isClientSide())
			return InteractionResult.SUCCESS;

		Direction side = context.getClickedFace();
		PersistentData.of(blockEntity).putString("output_direction", side.name());
		MachineHelpers.setSideMode(level, pos, side, "ITEM", "OUTPUT_ONLY");
		BlockState state = level.getBlockState(pos);
		level.sendBlockUpdated(pos, state, state, 3);

		if (player instanceof ServerPlayer serverPlayer)
			serverPlayer.sendSystemMessage(Component.translatable("skyblock_resources.output_direction_changed").append(Component.literal(side.name()).withStyle(ChatFormatting.DARK_GREEN)), true);
		return InteractionResult.SUCCESS;
	}
}
