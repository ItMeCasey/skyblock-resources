package com.itmecasey.skyblockresources.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Forge's game test server is the one dedicated server target that needs no EULA, but it refuses to start without at
 * least one registered test. This registers a no-op, non-required test so that server starts; the real checks run in
 * {@link HeadlessSelfTest} when the server has started.
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public final class ForgeGameTestHook {
	private ForgeGameTestHook() {
	}

	@SubscribeEvent
	public static void register(RegisterGameTestsEvent event) {
		if (Boolean.getBoolean("skyblock.selftest"))
			event.register(Noop.class);
	}

	public static final class Noop {
		@GameTest(template = "empty", required = false)
		public static void noop(GameTestHelper helper) {
			helper.succeed();
		}
	}
}
