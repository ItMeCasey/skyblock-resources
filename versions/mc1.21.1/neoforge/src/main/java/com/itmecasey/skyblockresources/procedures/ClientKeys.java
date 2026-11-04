package com.itmecasey.skyblockresources.procedures;

import net.neoforged.fml.loading.FMLEnvironment;

/** Client-only key state, kept behind a nested class so dedicated servers never load Minecraft's client classes. */
public final class ClientKeys {
	private ClientKeys() {
	}

	public static boolean controlDown() {
		return FMLEnvironment.dist.isClient() && Holder.controlDown();
	}

	private static final class Holder {
		static boolean controlDown() {
			return net.minecraft.client.gui.screens.Screen.hasControlDown();
		}
	}
}
