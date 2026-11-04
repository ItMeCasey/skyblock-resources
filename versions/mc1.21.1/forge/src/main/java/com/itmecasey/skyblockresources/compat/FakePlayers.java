package com.itmecasey.skyblockresources.compat;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

/** Forge 1.21.1 has no FakePlayerFactory (NeoForge does): a bare, never-added ServerPlayer with no connection. */
public final class FakePlayers {
	private static final GameProfile PROFILE = new GameProfile(UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77"), "[Minecraft]");
	private static final Map<ServerLevel, ServerPlayer> CACHE = new WeakHashMap<>();

	private FakePlayers() {
	}

	public static synchronized ServerPlayer getMinecraft(ServerLevel level) {
		return CACHE.computeIfAbsent(level, l -> new ServerPlayer(l.getServer(), l, PROFILE, ClientInformation.createDefault()) {
			@Override
			public void sendSystemMessage(Component message) {
			}

			@Override
			public void sendSystemMessage(Component message, boolean overlay) {
			}

			@Override
			public void displayClientMessage(Component message, boolean overlay) {
			}

			private GameType mode = GameType.SURVIVAL;

			/** The vanilla setter sends a player-info packet through the connection, which a fake player does not have. */
			@Override
			public boolean setGameMode(GameType gameType) {
				this.mode = gameType;
				this.getAbilities().instabuild = gameType.isCreative();
				return true;
			}

			@Override
			public boolean isSpectator() {
				return mode == GameType.SPECTATOR;
			}

			@Override
			public boolean isCreative() {
				return mode == GameType.CREATIVE;
			}
		});
	}
}
