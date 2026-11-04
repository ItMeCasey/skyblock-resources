package com.itmecasey.skyblockresources;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.TickTask;

import com.itmecasey.skyblockresources.configuration.ConfigurationConfiguration;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlockEntities;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlocks;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModItems;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModTabs;
import com.itmecasey.skyblockresources.loader.PersistentData;
import com.itmecasey.skyblockresources.network.SkyblockResourcesModVariables;

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

	private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<>();
	private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<>(Comparator.comparingInt(TickTask::getTick));
	private static Object minecraft;
	private static MethodHandle playerHandle;

	public SkyblockResourcesMod(FMLJavaModLoadingContext context) {
		BusGroup modBus = context.getModBusGroup();
		context.registerConfig(ModConfig.Type.COMMON, ConfigurationConfiguration.SPEC, "skyblock_resources.toml");
		SkyblockResourcesModBlocks.REGISTRY.register(modBus);
		SkyblockResourcesModBlockEntities.REGISTRY.register(modBus);
		SkyblockResourcesModItems.REGISTRY.register(modBus);
		SkyblockResourcesModTabs.REGISTRY.register(modBus);
		SkyblockResourcesModRecipeTypes.RECIPE_TYPES.register(modBus);
		SkyblockResourcesModRecipeTypes.RECIPE_SERIALIZER.register(modBus);
		// Forge wants network channels built while the mod loads.
		SkyblockResourcesModVariables.registerNetworking();
		AttachCapabilitiesEvent.BlockEntities.BUS.addListener(PersistentData::attach);
		TickEvent.ServerTickEvent.Post.BUS.addListener(SkyblockResourcesMod::tick);
	}

	public static void queueServerWork(int delay, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workToBeScheduled.add(new IntObjectImmutablePair<>(delay, action));
	}

	private static void tick(TickEvent.ServerTickEvent.Post event) {
		int currentTick = event.server().getTickCount();
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
