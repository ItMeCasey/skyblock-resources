package com.itmecasey.skyblockresources.network;

import com.itmecasey.skyblockresources.compat.NbtCompat;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;

import com.itmecasey.skyblockresources.SkyblockResourcesMod;

import java.util.ArrayList;

@EventBusSubscriber
public class SkyblockResourcesModVariables {
	/** Registers the saved-data sync message on the mod's channel (Forge has no payload registrar event). */
	public static void registerMessages(SimpleChannel channel) {
		channel.messageBuilder(SavedDataSyncMessage.class, NetworkDirection.PLAY_TO_CLIENT).codec(SavedDataSyncMessage.STREAM_CODEC).consumerMainThread(SavedDataSyncMessage::handleData).add();
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			SavedData mapdata = MapVariables.get(player.level());
			SavedData worlddata = WorldVariables.get(player.level());
			if (mapdata != null)
				SkyblockResourcesMod.sendToPlayer(player, new SavedDataSyncMessage(0, mapdata));
			if (worlddata != null)
				SkyblockResourcesMod.sendToPlayer(player, new SavedDataSyncMessage(1, worlddata));
		}
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			SavedData worlddata = WorldVariables.get(player.level());
			if (worlddata != null)
				SkyblockResourcesMod.sendToPlayer(player, new SavedDataSyncMessage(1, worlddata));
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.LevelTickEvent.Post event) {
		if (event.level instanceof ServerLevel level) {
			WorldVariables worldVariables = WorldVariables.get(level);
			if (worldVariables.syncDirty) {
				SkyblockResourcesMod.sendToPlayersInDimension(level, new SavedDataSyncMessage(1, worldVariables));
				worldVariables.syncDirty = false;
			}
			MapVariables mapVariables = MapVariables.get(level);
			if (mapVariables.syncDirty) {
				SkyblockResourcesMod.sendToAllPlayers(new SavedDataSyncMessage(0, mapVariables));
				mapVariables.syncDirty = false;
			}
		}
	}

	public static class WorldVariables extends SavedData {
		public static final String NAME = "skyblock_resources_worldvars";
		public static final SavedData.Factory<WorldVariables> FACTORY = new SavedData.Factory<>(WorldVariables::new, (tag, lookupProvider) -> {
			WorldVariables instance = new WorldVariables();
			instance.read(tag, lookupProvider);
			return instance;
		}, null);
		boolean syncDirty = false;

		public void read(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
		}

		@Override
		public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
			return nbt;
		}

		public void markSyncDirty() {
			this.setDirty();
			this.syncDirty = true;
		}

		static WorldVariables clientSide = new WorldVariables();

		public static WorldVariables get(LevelAccessor world) {
			if (world instanceof ServerLevel level) {
				return level.getDataStorage().computeIfAbsent(WorldVariables.FACTORY, WorldVariables.NAME);
			} else {
				return clientSide;
			}
		}
	}

	public static class MapVariables extends SavedData {
		public static final String NAME = "skyblock_resources_mapvars";
		public static final SavedData.Factory<MapVariables> FACTORY = new SavedData.Factory<>(MapVariables::new, (tag, lookupProvider) -> {
			MapVariables instance = new MapVariables();
			instance.read(tag, lookupProvider);
			return instance;
		}, null);
		boolean syncDirty = false;
		public double GLOBAL_ITEMS = 0;
		public String GLOBAL_RESOURCE_NAME = "\"\"";
		public ArrayList<Object> WoodenItems = new ArrayList<>();
		public String GLOBAL_ITEM_UUID = "\"\"";
		public ArrayList<Object> ALL_ITEMS = new ArrayList<>();

		public void read(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
			GLOBAL_ITEMS = NbtCompat.getDoubleOr(nbt, "GLOBAL_ITEMS", 0);
			GLOBAL_RESOURCE_NAME = NbtCompat.getStringOr(nbt, "GLOBAL_RESOURCE_NAME", "");
			WoodenItems = NbtArrayLists.loadGlobalMap(nbt.getList("WoodenItems", Tag.TAG_COMPOUND), lookupProvider);
			GLOBAL_ITEM_UUID = NbtCompat.getStringOr(nbt, "GLOBAL_ITEM_UUID", "");
			ALL_ITEMS = NbtArrayLists.loadGlobalMap(nbt.getList("ALL_ITEMS", Tag.TAG_COMPOUND), lookupProvider);
		}

		@Override
		public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
			nbt.putDouble("GLOBAL_ITEMS", GLOBAL_ITEMS);
			nbt.putString("GLOBAL_RESOURCE_NAME", GLOBAL_RESOURCE_NAME);
			nbt.put("WoodenItems", NbtArrayLists.saveGlobalMap(WoodenItems));
			nbt.putString("GLOBAL_ITEM_UUID", GLOBAL_ITEM_UUID);
			nbt.put("ALL_ITEMS", NbtArrayLists.saveGlobalMap(ALL_ITEMS));
			return nbt;
		}

		public void markSyncDirty() {
			this.setDirty();
			this.syncDirty = true;
		}

		static MapVariables clientSide = new MapVariables();

		public static MapVariables get(LevelAccessor world) {
			if (world instanceof ServerLevelAccessor serverLevelAccessor) {
				return serverLevelAccessor.getLevel().getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(MapVariables.FACTORY, MapVariables.NAME);
			} else {
				return clientSide;
			}
		}
	}

	public record SavedDataSyncMessage(int dataType, SavedData data) {
		public static final StreamCodec<RegistryFriendlyByteBuf, SavedDataSyncMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, SavedDataSyncMessage message) -> {
			buffer.writeInt(message.dataType);
			if (message.data instanceof MapVariables mapVariables)
				buffer.writeNbt(mapVariables.save(new CompoundTag(), buffer.registryAccess()));
			else if (message.data instanceof WorldVariables worldVariables)
				buffer.writeNbt(worldVariables.save(new CompoundTag(), buffer.registryAccess()));
		}, (RegistryFriendlyByteBuf buffer) -> {
			int dataType = buffer.readInt();
			CompoundTag nbt = buffer.readNbt();
			SavedData data = null;
			if (nbt != null) {
				data = dataType == 0 ? new MapVariables() : new WorldVariables();
				if (data instanceof MapVariables mapVariables)
					mapVariables.read(nbt, buffer.registryAccess());
				else if (data instanceof WorldVariables worldVariables)
					worldVariables.read(nbt, buffer.registryAccess());
			}
			return new SavedDataSyncMessage(dataType, data);
		});

		public static void handleData(final SavedDataSyncMessage message, final CustomPayloadEvent.Context context) {
			if (context.isClientSide() && message.data != null) {
				context.enqueueWork(() -> {
					HolderLookup.Provider registries = ClientAccess.registries();
					if (message.dataType == 0)
						MapVariables.clientSide.read(((MapVariables) message.data).save(new CompoundTag(), registries), registries);
					else
						WorldVariables.clientSide.read(((WorldVariables) message.data).save(new CompoundTag(), registries), registries);
				}).exceptionally(e -> {
					context.getConnection().disconnect(Component.literal(e.getMessage()));
					return null;
				});
			}
		}
	}

	/** Client-only lookup, kept in its own class so a dedicated server never loads Minecraft's client classes. */
	private static final class ClientAccess {
		static HolderLookup.Provider registries() {
			return net.minecraft.client.Minecraft.getInstance().getConnection().registryAccess();
		}
	}
}