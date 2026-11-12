package com.itmecasey.skyblockresources.network;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.datafix.DataFixTypes;

import com.itmecasey.skyblockresources.SkyblockResourcesMod;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = SkyblockResourcesMod.MODID)
public class SkyblockResourcesModVariables {
	/** Builds the mod's network channel; called from the mod constructor. */
	public static void registerNetworking() {
		ForgeNetwork.register();
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			SavedData mapdata = MapVariables.get(player.level());
			SavedData worlddata = WorldVariables.get(player.level());
			if (mapdata != null)
				ForgeNetwork.sendToPlayer(player, new SavedDataSyncMessage(0, mapdata));
			if (worlddata != null)
				ForgeNetwork.sendToPlayer(player, new SavedDataSyncMessage(1, worlddata));
		}
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			SavedData worlddata = WorldVariables.get(player.level());
			if (worlddata != null)
				ForgeNetwork.sendToPlayer(player, new SavedDataSyncMessage(1, worlddata));
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.LevelTickEvent.Post event) {
		if (event.level() instanceof ServerLevel level) {
			WorldVariables worldVariables = WorldVariables.get(level);
			if (worldVariables.syncDirty) {
				ForgeNetwork.sendToDimension(level, new SavedDataSyncMessage(1, worldVariables));
				worldVariables.syncDirty = false;
			}
			MapVariables mapVariables = MapVariables.get(level);
			if (mapVariables.syncDirty) {
				ForgeNetwork.sendToAll(new SavedDataSyncMessage(0, mapVariables));
				mapVariables.syncDirty = false;
			}
		}
	}

	public static class WorldVariables extends SavedData {
		public static final SavedDataType<WorldVariables> TYPE = new SavedDataType<>(Identifier.parse("skyblock_resources:worldvars"), WorldVariables::new, CompoundTag.CODEC.xmap(tag -> {
			WorldVariables instance = new WorldVariables();
			instance.read(tag, RegistryAccess.EMPTY);
			return instance;
		}, instance -> instance.save(new CompoundTag(), RegistryAccess.EMPTY)), DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
		boolean syncDirty = false;

		public void read(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
		}

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
				return level.getDataStorage().computeIfAbsent(WorldVariables.TYPE);
			} else {
				return clientSide;
			}
		}
	}

	public static class MapVariables extends SavedData {
		public static final SavedDataType<MapVariables> TYPE = new SavedDataType<>(Identifier.parse("skyblock_resources:mapvars"), MapVariables::new, CompoundTag.CODEC.xmap(tag -> {
			MapVariables instance = new MapVariables();
			instance.read(tag, RegistryAccess.EMPTY);
			return instance;
		}, instance -> instance.save(new CompoundTag(), RegistryAccess.EMPTY)), DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
		boolean syncDirty = false;
		public double GLOBAL_ITEMS = 0;
		public String GLOBAL_RESOURCE_NAME = "\"\"";
		public ArrayList<Object> WoodenItems = new ArrayList<>();
		public String GLOBAL_ITEM_UUID = "\"\"";
		public ArrayList<Object> ALL_ITEMS = new ArrayList<>();

		public void read(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
			GLOBAL_ITEMS = nbt.getDoubleOr("GLOBAL_ITEMS", 0);
			GLOBAL_RESOURCE_NAME = nbt.getStringOr("GLOBAL_RESOURCE_NAME", "");
			WoodenItems = NbtArrayLists.loadGlobalMap(nbt.getListOrEmpty("WoodenItems"), lookupProvider);
			GLOBAL_ITEM_UUID = nbt.getStringOr("GLOBAL_ITEM_UUID", "");
			ALL_ITEMS = NbtArrayLists.loadGlobalMap(nbt.getListOrEmpty("ALL_ITEMS"), lookupProvider);
		}

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
				return serverLevelAccessor.getLevel().getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(MapVariables.TYPE);
			} else {
				return clientSide;
			}
		}
	}

	public record SavedDataSyncMessage(int dataType, SavedData data) {
		public void write(RegistryFriendlyByteBuf buffer) {
			buffer.writeInt(dataType);
			if (data instanceof MapVariables mapVariables)
				buffer.writeNbt(mapVariables.save(new CompoundTag(), buffer.registryAccess()));
			else if (data instanceof WorldVariables worldVariables)
				buffer.writeNbt(worldVariables.save(new CompoundTag(), buffer.registryAccess()));
		}

		public static SavedDataSyncMessage read(RegistryFriendlyByteBuf buffer) {
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
		}

		public static void handleData(final SavedDataSyncMessage message) {
			if (message.data != null)
				ClientHandler.apply(message);
		}

		/** Kept in its own class so a dedicated server never loads the client classes it touches. */
		private static final class ClientHandler {
			static void apply(SavedDataSyncMessage message) {
				var access = net.minecraft.client.Minecraft.getInstance().level.registryAccess();
				if (message.dataType == 0)
					MapVariables.clientSide.read(((MapVariables) message.data).save(new CompoundTag(), access), access);
				else
					WorldVariables.clientSide.read(((WorldVariables) message.data).save(new CompoundTag(), access), access);
			}
		}
	}
}