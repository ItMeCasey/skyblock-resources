package com.itmecasey.skyblockresources.init;

import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import com.itmecasey.skyblockresources.block.entity.*;
import com.itmecasey.skyblockresources.SkyblockResourcesMod;

@EventBusSubscriber
public class SkyblockResourcesModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, SkyblockResourcesMod.MODID);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodenGeneratorBlockEntity>> WOODEN_GENERATOR = register("wooden_generator", SkyblockResourcesModBlocks.WOODEN_GENERATOR, WoodenGeneratorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StoneGeneratorBlockEntity>> STONE_GENERATOR = register("stone_generator", SkyblockResourcesModBlocks.STONE_GENERATOR, StoneGeneratorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronGeneratorBlockEntity>> IRON_GENERATOR = register("iron_generator", SkyblockResourcesModBlocks.IRON_GENERATOR, IronGeneratorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DiamondGeneratorBlockEntity>> DIAMOND_GENERATOR = register("diamond_generator", SkyblockResourcesModBlocks.DIAMOND_GENERATOR, DiamondGeneratorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NetheriteGeneratorBlockEntity>> NETHERITE_GENERATOR = register("netherite_generator", SkyblockResourcesModBlocks.NETHERITE_GENERATOR, NetheriteGeneratorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodenTreeGeneratorBlockEntity>> WOODEN_TREE_GENERATOR = register("wooden_tree_generator", SkyblockResourcesModBlocks.WOODEN_TREE_GENERATOR,
			WoodenTreeGeneratorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StoneTreeGeneratorBlockEntity>> STONE_TREE_GENERATOR = register("stone_tree_generator", SkyblockResourcesModBlocks.STONE_TREE_GENERATOR, StoneTreeGeneratorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronTreeGeneratorBlockEntity>> IRON_TREE_GENERATOR = register("iron_tree_generator", SkyblockResourcesModBlocks.IRON_TREE_GENERATOR, IronTreeGeneratorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DiamondTreeGeneratorBlockEntity>> DIAMOND_TREE_GENERATOR = register("diamond_tree_generator", SkyblockResourcesModBlocks.DIAMOND_TREE_GENERATOR,
			DiamondTreeGeneratorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NetheriteTreeGeneratorBlockEntity>> NETHERITE_TREE_GENERATOR = register("netherite_tree_generator", SkyblockResourcesModBlocks.NETHERITE_TREE_GENERATOR,
			NetheriteTreeGeneratorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CropGeneratorBlockEntity>> CROP_GENERATOR = REGISTRY.register("crop_generator",
			() -> BlockEntityType.Builder.of(CropGeneratorBlockEntity::new,
					SkyblockResourcesModBlocks.WOODEN_CROP_GENERATOR.get(),
					SkyblockResourcesModBlocks.STONE_CROP_GENERATOR.get(),
					SkyblockResourcesModBlocks.IRON_CROP_GENERATOR.get(),
					SkyblockResourcesModBlocks.DIAMOND_CROP_GENERATOR.get(),
					SkyblockResourcesModBlocks.NETHERITE_CROP_GENERATOR.get()).build(null));

	private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String registryName, DeferredHolder<Block, Block> block, BlockEntityType.BlockEntitySupplier<T> supplier) {
		return REGISTRY.register(registryName, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, WOODEN_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, STONE_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IRON_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DIAMOND_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, NETHERITE_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, WOODEN_TREE_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, STONE_TREE_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IRON_TREE_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DIAMOND_TREE_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, NETHERITE_TREE_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CROP_GENERATOR.get(), (be, side) -> new SidedInvWrapper(be, side));
	}
}
