package com.itmecasey.skyblockresources;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.TickTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import com.itmecasey.skyblockresources.network.SkyblockResourcesModVariables;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModTabs;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;
import com.itmecasey.skyblockresources.configuration.ConfigurationConfiguration;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModItems;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlocks;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlockEntities;

import javax.annotation.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandle;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;

@Mod("skyblock_resources")
public class SkyblockResourcesMod {
	public static final Logger LOGGER = LogManager.getLogger(SkyblockResourcesMod.class);
	public static final String MODID = "skyblock_resources";

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;
	private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<>();
	private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<>(Comparator.comparingInt(TickTask::getTick));
	private static Object minecraft;
	private static MethodHandle playerHandle;

	public SkyblockResourcesMod() {
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(SkyblockResourcesModVariables::init);
		SkyblockResourcesModBlocks.REGISTRY.register(modEventBus);
		SkyblockResourcesModBlockEntities.REGISTRY.register(modEventBus);
		SkyblockResourcesModItems.REGISTRY.register(modEventBus);
		SkyblockResourcesModTabs.REGISTRY.register(modEventBus);
		SkyblockResourcesModRecipeTypes.RECIPE_TYPES.register(modEventBus);
		SkyblockResourcesModRecipeTypes.RECIPE_SERIALIZER.register(modEventBus);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigurationConfiguration.SPEC, "skyblock_resources.toml");
	}

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}

	public static void queueServerWork(int delay, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workToBeScheduled.add(new IntObjectImmutablePair<>(delay, action));
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
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
