package com.itmecasey.skyblockresources.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import com.itmecasey.skyblockresources.compat.FakePlayers;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.commands.Commands;

import com.itmecasey.skyblockresources.procedures.GeneratorBlockDestroyedByExplosionProcedure;

@EventBusSubscriber
public class ClearDisplayItemsCommand {
	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("cleardisplayitems").requires(source -> source.hasPermission(2))

				.executes(arguments -> {
					Level world = arguments.getSource().getLevel();
					double x = arguments.getSource().getPosition().x;
					double y = arguments.getSource().getPosition().y;
					double z = arguments.getSource().getPosition().z;
					Entity entity = arguments.getSource().getEntity();
					if (entity == null && world instanceof ServerLevel _servLevel)
						entity = FakePlayers.getMinecraft(_servLevel);
					Direction direction = Direction.DOWN;
					if (entity != null)
						direction = entity.getDirection();

					GeneratorBlockDestroyedByExplosionProcedure.execute(world, x, y, z);
					return 0;
				}));
	}

}