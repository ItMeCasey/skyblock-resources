package com.itmecasey.skyblockresources.network;

import com.itmecasey.skyblockresources.compat.NbtCompat;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.registries.BuiltInRegistries;

import com.itmecasey.skyblockresources.SkyblockResourcesMod;

import java.util.ArrayList;
import java.util.function.Supplier;

@EventBusSubscriber
public class SkyblockResourcesModVariables {
	public static void init(FMLCommonSetupEvent event) {
		SkyblockResourcesMod.addNetworkMessage(SavedDataSyncMessage.class, SavedDataSyncMessage::buffer, SavedDataSyncMessage::new, SavedDataSyncMessage::handler);
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			SavedData mapdata = MapVariables.get(player.level());
			SavedData worlddata = WorldVariables.get(player.level());
			if (mapdata != null)
				SkyblockResourcesMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SavedDataSyncMessage(0, mapdata));
			if (worlddata != null)
				SkyblockResourcesMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SavedDataSyncMessage(1, worlddata));
		}
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			SavedData worlddata = WorldVariables.get(player.level());
			if (worlddata != null)
				SkyblockResourcesMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SavedDataSyncMessage(1, worlddata));
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.LevelTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) {
			WorldVariables worldVariables = WorldVariables.get(level);
			if (worldVariables.syncDirty) {
				SkyblockResourcesMod.PACKET_HANDLER.send(PacketDistributor.DIMENSION.with(level::dimension), new SavedDataSyncMessage(1, worldVariables));
				worldVariables.syncDirty = false;
			}
			MapVariables mapVariables = MapVariables.get(level);
			if (mapVariables.syncDirty) {
				SkyblockResourcesMod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new SavedDataSyncMessage(0, mapVariables));
				mapVariables.syncDirty = false;
			}
		}
	}

	public static class WorldVariables extends SavedData {
		public static final String NAME = "skyblock_resources_worldvars";
		boolean syncDirty = false;

		public static WorldVariables load(CompoundTag tag) {
			WorldVariables instance = new WorldVariables();
			instance.read(tag);
			return instance;
		}

		public void read(CompoundTag nbt) {
		}

		@Override
		public CompoundTag save(CompoundTag nbt) {
			return nbt;
		}

		public void markSyncDirty() {
			this.setDirty();
			this.syncDirty = true;
		}

		static WorldVariables clientSide = new WorldVariables();

		public static WorldVariables get(LevelAccessor world) {
			if (world instanceof ServerLevel level) {
				return level.getDataStorage().computeIfAbsent(WorldVariables::load, WorldVariables::new, WorldVariables.NAME);
			} else {
				return clientSide;
			}
		}
	}

	public static class MapVariables extends SavedData {
		public static final String NAME = "skyblock_resources_mapvars";
		boolean syncDirty = false;
		public double GLOBAL_ITEMS = 0;
		public String GLOBAL_RESOURCE_NAME = "\"\"";
		public ArrayList<Object> WoodenItems = new ArrayList<>();
		public String GLOBAL_ITEM_UUID = "\"\"";
		public ArrayList<Object> ALL_ITEMS = new ArrayList<>();

		public static MapVariables load(CompoundTag tag) {
			MapVariables instance = new MapVariables();
			instance.read(tag);
			return instance;
		}

		public void read(CompoundTag nbt) {
			GLOBAL_ITEMS = NbtCompat.getDoubleOr(nbt, "GLOBAL_ITEMS", 0);
			GLOBAL_RESOURCE_NAME = NbtCompat.getStringOr(nbt, "GLOBAL_RESOURCE_NAME", "");
			WoodenItems = NbtArrayLists.loadGlobalMap(nbt.getList("WoodenItems", Tag.TAG_COMPOUND), BuiltInRegistries.BLOCK.asLookup());
			GLOBAL_ITEM_UUID = NbtCompat.getStringOr(nbt, "GLOBAL_ITEM_UUID", "");
			ALL_ITEMS = NbtArrayLists.loadGlobalMap(nbt.getList("ALL_ITEMS", Tag.TAG_COMPOUND), BuiltInRegistries.BLOCK.asLookup());
		}

		@Override
		public CompoundTag save(CompoundTag nbt) {
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
				return serverLevelAccessor.getLevel().getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(MapVariables::load, MapVariables::new, MapVariables.NAME);
			} else {
				return clientSide;
			}
		}
	}

	public static class SavedDataSyncMessage {
		private final int dataType;
		private final SavedData data;

		public SavedDataSyncMessage(FriendlyByteBuf buffer) {
			this.dataType = buffer.readInt();
			CompoundTag nbt = buffer.readNbt();
			SavedData loaded = null;
			if (nbt != null) {
				if (this.dataType == 0) {
					MapVariables map = new MapVariables();
					map.read(nbt);
					loaded = map;
				} else {
					WorldVariables world = new WorldVariables();
					world.read(nbt);
					loaded = world;
				}
			}
			this.data = loaded;
		}

		public SavedDataSyncMessage(int dataType, SavedData data) {
			this.dataType = dataType;
			this.data = data;
		}

		public static void buffer(SavedDataSyncMessage message, FriendlyByteBuf buffer) {
			buffer.writeInt(message.dataType);
			buffer.writeNbt(message.data != null ? message.data.save(new CompoundTag()) : null);
		}

		public static void handler(SavedDataSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				if (message.data == null)
					return;
				if (message.dataType == 0)
					MapVariables.clientSide.read(message.data.save(new CompoundTag()));
				else
					WorldVariables.clientSide.read(message.data.save(new CompoundTag()));
			});
			context.setPacketHandled(true);
		}
	}
}
