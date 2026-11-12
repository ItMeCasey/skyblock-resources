package com.itmecasey.skyblockresources.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Direction;
import net.minecraft.commands.Commands;

import com.itmecasey.skyblockresources.procedures.GeneratorBlockDestroyedByExplosionProcedure;

@Mod.EventBusSubscriber(modid = "skyblock_resources")
public class ClearDisplayItemsCommand {
	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("cleardisplayitems").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))

				.executes(arguments -> {
					Level world = arguments.getSource().getUnsidedLevel();
					double x = arguments.getSource().getPosition().x();
					double y = arguments.getSource().getPosition().y();
					double z = arguments.getSource().getPosition().z();
					Entity entity = arguments.getSource().getEntity();
					Direction direction = Direction.DOWN;
					if (entity != null)
						direction = entity.getDirection();

					GeneratorBlockDestroyedByExplosionProcedure.execute(world, x, y, z);
					return 0;
				}));
	}

}