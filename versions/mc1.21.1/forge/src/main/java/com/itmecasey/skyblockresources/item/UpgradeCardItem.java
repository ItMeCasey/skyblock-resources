package com.itmecasey.skyblockresources.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.itmecasey.skyblockresources.machine.GeneratorTiers;

/** Sneak-clicking a generator one tier below this card's tier upgrades it in place and uses up the card. */
public class UpgradeCardItem extends Item {
	private final int tier;

	public UpgradeCardItem(Item.Properties properties, int tier) {
		super(properties);
		this.tier = tier;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !player.isShiftKeyDown())
			return InteractionResult.PASS;

		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (GeneratorTiers.tierOf(level.getBlockState(pos).getBlock()) < 0)
			return InteractionResult.PASS;
		if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()))
			return InteractionResult.FAIL;
		if (level.isClientSide())
			return InteractionResult.SUCCESS;

		// NeoForge snapshots block changes made during useOn and re-fires onPlace on them afterward, which would
		// reset the machine. Run the swap once useOn has returned.
		ItemStack stack = context.getItemInHand();
		level.getServer().execute(() -> {
			GeneratorTiers.Result result = GeneratorTiers.upgrade(level, pos, tier);
			Component message = switch (result) {
				case UPGRADED -> Component.translatable("skyblock_resources.upgrade.done", Component.translatable("skyblock_resources.tier." + tier));
				case WRONG_TIER -> Component.translatable("skyblock_resources.upgrade.wrong_tier");
				case NO_ROOM -> Component.translatable("skyblock_resources.upgrade.no_room");
				case NOT_GENERATOR -> null;
			};
			if (result == GeneratorTiers.Result.UPGRADED) {
				if (!player.hasInfiniteMaterials())
					stack.shrink(1);
				level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
			}
			if (message != null && player instanceof ServerPlayer serverPlayer)
				serverPlayer.sendSystemMessage(message, true);
		});
		return InteractionResult.SUCCESS;
	}
}
