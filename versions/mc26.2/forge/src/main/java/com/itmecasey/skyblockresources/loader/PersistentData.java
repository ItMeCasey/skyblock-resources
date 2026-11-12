package com.itmecasey.skyblockresources.loader;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import com.itmecasey.skyblockresources.SkyblockResourcesMod;
import com.itmecasey.skyblockresources.block.entity.CropGeneratorBlockEntity;
import com.itmecasey.skyblockresources.block.entity.DiamondGeneratorBlockEntity;
import com.itmecasey.skyblockresources.block.entity.DiamondTreeGeneratorBlockEntity;
import com.itmecasey.skyblockresources.block.entity.IronGeneratorBlockEntity;
import com.itmecasey.skyblockresources.block.entity.IronTreeGeneratorBlockEntity;
import com.itmecasey.skyblockresources.block.entity.NetheriteGeneratorBlockEntity;
import com.itmecasey.skyblockresources.block.entity.NetheriteTreeGeneratorBlockEntity;
import com.itmecasey.skyblockresources.block.entity.StoneGeneratorBlockEntity;
import com.itmecasey.skyblockresources.block.entity.StoneTreeGeneratorBlockEntity;
import com.itmecasey.skyblockresources.block.entity.WoodenGeneratorBlockEntity;
import com.itmecasey.skyblockresources.block.entity.WoodenTreeGeneratorBlockEntity;

/**
 * NeoForge gives every block entity {@code getPersistentData()}, a tag that is saved with it and sent to clients. Forge
 * 26.1.2 has no such method, so the machines get one here: a capability that Forge saves as part of the block entity
 * (under {@code ForgeCaps}) and includes in the update tag. {@link #of(BlockEntity)} replaces {@code getPersistentData()}.
 * Block entities that are not ours get a throwaway tag, so a write to one is not kept.
 */
public final class PersistentData {
	private static final Capability<Holder> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
	});
	private static final Identifier KEY = Identifier.fromNamespaceAndPath(SkyblockResourcesMod.MODID, "persistent_data");

	private PersistentData() {
	}

	/** The block entity's persistent tag; the same tag object every time for one block entity. */
	public static CompoundTag of(BlockEntity entity) {
		if (entity == null) {
			return new CompoundTag();
		}
		return entity.getCapability(CAPABILITY).resolve().map(Holder::tag).orElseGet(CompoundTag::new);
	}

	/** Listener for the block entity attach event: puts the capability on this mod's machines. */
	public static void attach(AttachCapabilitiesEvent.BlockEntities event) {
		BlockEntity entity = event.getObject();
		if (entity instanceof WoodenGeneratorBlockEntity || entity instanceof StoneGeneratorBlockEntity || entity instanceof IronGeneratorBlockEntity || entity instanceof DiamondGeneratorBlockEntity
				|| entity instanceof NetheriteGeneratorBlockEntity || entity instanceof WoodenTreeGeneratorBlockEntity || entity instanceof StoneTreeGeneratorBlockEntity
				|| entity instanceof IronTreeGeneratorBlockEntity || entity instanceof DiamondTreeGeneratorBlockEntity || entity instanceof NetheriteTreeGeneratorBlockEntity
				|| entity instanceof CropGeneratorBlockEntity) {
			event.addCapability(KEY, new Provider());
		}
	}

	@AutoRegisterCapability
	private record Holder(CompoundTag tag) {
	}

	private static final class Provider implements ICapabilitySerializable<CompoundTag> {
		private final Holder holder = new Holder(new CompoundTag());
		private final LazyOptional<Holder> optional = LazyOptional.of(() -> holder);

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
			return CAPABILITY.orEmpty(capability, optional);
		}

		@Override
		public CompoundTag serializeNBT(HolderLookup.Provider registries) {
			return holder.tag().copy();
		}

		@Override
		public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
			holder.tag().keySet().clear();
			holder.tag().merge(tag);
		}
	}
}
