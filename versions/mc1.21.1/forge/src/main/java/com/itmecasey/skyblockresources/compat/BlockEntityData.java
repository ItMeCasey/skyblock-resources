package com.itmecasey.skyblockresources.compat;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import com.itmecasey.skyblockresources.SkyblockResourcesMod;

/**
 * NeoForge gives every block entity {@code getPersistentData()}, a CompoundTag that is saved with it and sent to the
 * client. Forge 1.21.1 has that for entities only. This keeps the same tag for this mod's block entities in a weak map
 * and saves it through Forge's capability serialization ("ForgeCaps"), which also goes into the client update tag.
 * Block entities of other mods get a throwaway tag.
 */
@EventBusSubscriber
public final class BlockEntityData {
	private static final Map<BlockEntity, CompoundTag> DATA = Collections.synchronizedMap(new WeakHashMap<>());
	private static final ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath(SkyblockResourcesMod.MODID, "persistent_data");

	private BlockEntityData() {
	}

	public static CompoundTag of(BlockEntity entity) {
		if (!isOurs(entity))
			return new CompoundTag();
		return DATA.computeIfAbsent(entity, e -> new CompoundTag());
	}

	private static boolean isOurs(BlockEntity entity) {
		ResourceLocation id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entity.getType());
		return id != null && id.getNamespace().equals(SkyblockResourcesMod.MODID);
	}

	@SubscribeEvent
	public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
		BlockEntity entity = event.getObject();
		if (!isOurs(entity))
			return;
		event.addCapability(KEY, new ICapabilitySerializable<CompoundTag>() {
			@Override
			public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
				return LazyOptional.empty();
			}

			@Override
			public CompoundTag serializeNBT(HolderLookup.Provider registries) {
				return of(entity).copy();
			}

			@Override
			public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
				CompoundTag target = of(entity);
				for (String key : target.getAllKeys().toArray(new String[0]))
					target.remove(key);
				target.merge(tag);
			}
		});
	}
}
