package com.itmecasey.skyblockresources;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;

import com.itmecasey.skyblockresources.network.SkyblockResourcesModVariables;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModConfigs;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModTabs;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModItems;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlocks;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlockEntities;

import javax.annotation.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;

import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandle;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;

@Mod("skyblock_resources")
public class SkyblockResourcesMod {
	public static final Logger LOGGER = LogManager.getLogger(SkyblockResourcesMod.class);
	public static final String MODID = "skyblock_resources";

	/** Forge 1.21.1 has no payload registrar; the saved-data sync message goes through one SimpleChannel. */
	public static final SimpleChannel CHANNEL = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(MODID, "main")).networkProtocolVersion(1).simpleChannel();

	private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<>();
	private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<>(Comparator.comparingInt(TickTask::getTick));
	private static Object minecraft;
	private static MethodHandle playerHandle;

	public SkyblockResourcesMod(FMLJavaModLoadingContext context) {
		com.itmecasey.skyblockresources.script.ModJsBridge.install();
		IEventBus modEventBus = context.getModEventBus();
		MinecraftForge.EVENT_BUS.register(this);
		SkyblockResourcesModBlocks.REGISTRY.register(modEventBus);
		SkyblockResourcesModBlockEntities.REGISTRY.register(modEventBus);
		SkyblockResourcesModItems.REGISTRY.register(modEventBus);
		SkyblockResourcesModTabs.REGISTRY.register(modEventBus);
		SkyblockResourcesModRecipeTypes.RECIPE_TYPES.register(modEventBus);
		SkyblockResourcesModRecipeTypes.RECIPE_SERIALIZER.register(modEventBus);
		SkyblockResourcesModConfigs.register(context);
		SkyblockResourcesModVariables.registerMessages(CHANNEL);
	}

	public static void sendToPlayer(ServerPlayer player, Object message) {
		CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
	}

	public static void sendToPlayersInDimension(ServerLevel level, Object message) {
		CHANNEL.send(message, PacketDistributor.DIMENSION.with(level.dimension()));
	}

	public static void sendToAllPlayers(Object message) {
		CHANNEL.send(message, PacketDistributor.ALL.noArg());
	}

	public static void queueServerWork(int delay, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workToBeScheduled.add(new IntObjectImmutablePair<>(delay, action));
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent.Post event) {
		int currentTick = event.getServer().getTickCount();
		IntObjectPair<Runnable> work;
		while ((work = workToBeScheduled.poll()) != null) {
			workQueue.add(new TickTask(currentTick + work.leftInt(), work.right()));
		}
		while (!workQueue.isEmpty() && currentTick >= workQueue.peek().getTick()) {
			workQueue.poll().run();
		}
	}

	@Nullable
	public static Player clientPlayer() {
		if (!FMLEnvironment.dist.isClient())
			return null;
		try {
			if (minecraft == null || playerHandle == null) {
				Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
				minecraft = MethodHandles.publicLookup().findStatic(minecraftClass, "getInstance", MethodType.methodType(minecraftClass)).invoke();
				playerHandle = MethodHandles.publicLookup().findGetter(minecraftClass, "player", Class.forName("net.minecraft.client.player.LocalPlayer"));
			}
			return (Player) playerHandle.invoke(minecraft);
		} catch (Throwable e) {
			LOGGER.error("Failed to get client player", e);
			return null;
		}
	}
}
