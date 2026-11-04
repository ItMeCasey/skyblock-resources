package com.itmecasey.skyblockresources.init;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import com.itmecasey.skyblockresources.block.entity.*;
import com.itmecasey.skyblockresources.SkyblockResourcesMod;

public class SkyblockResourcesModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SkyblockResourcesMod.MODID);
	public static final RegistryObject<BlockEntityType<WoodenGeneratorBlockEntity>> WOODEN_GENERATOR = register("wooden_generator", SkyblockResourcesModBlocks.WOODEN_GENERATOR, WoodenGeneratorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<StoneGeneratorBlockEntity>> STONE_GENERATOR = register("stone_generator", SkyblockResourcesModBlocks.STONE_GENERATOR, StoneGeneratorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<IronGeneratorBlockEntity>> IRON_GENERATOR = register("iron_generator", SkyblockResourcesModBlocks.IRON_GENERATOR, IronGeneratorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<DiamondGeneratorBlockEntity>> DIAMOND_GENERATOR = register("diamond_generator", SkyblockResourcesModBlocks.DIAMOND_GENERATOR, DiamondGeneratorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<NetheriteGeneratorBlockEntity>> NETHERITE_GENERATOR = register("netherite_generator", SkyblockResourcesModBlocks.NETHERITE_GENERATOR, NetheriteGeneratorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<WoodenTreeGeneratorBlockEntity>> WOODEN_TREE_GENERATOR = register("wooden_tree_generator", SkyblockResourcesModBlocks.WOODEN_TREE_GENERATOR,
			WoodenTreeGeneratorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<StoneTreeGeneratorBlockEntity>> STONE_TREE_GENERATOR = register("stone_tree_generator", SkyblockResourcesModBlocks.STONE_TREE_GENERATOR, StoneTreeGeneratorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<IronTreeGeneratorBlockEntity>> IRON_TREE_GENERATOR = register("iron_tree_generator", SkyblockResourcesModBlocks.IRON_TREE_GENERATOR, IronTreeGeneratorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<DiamondTreeGeneratorBlockEntity>> DIAMOND_TREE_GENERATOR = register("diamond_tree_generator", SkyblockResourcesModBlocks.DIAMOND_TREE_GENERATOR,
			DiamondTreeGeneratorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<NetheriteTreeGeneratorBlockEntity>> NETHERITE_TREE_GENERATOR = register("netherite_tree_generator", SkyblockResourcesModBlocks.NETHERITE_TREE_GENERATOR,
			NetheriteTreeGeneratorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<CropGeneratorBlockEntity>> CROP_GENERATOR = REGISTRY.register("crop_generator",
			() -> BlockEntityType.Builder.of(CropGeneratorBlockEntity::new,
					SkyblockResourcesModBlocks.WOODEN_CROP_GENERATOR.get(),
					SkyblockResourcesModBlocks.STONE_CROP_GENERATOR.get(),
					SkyblockResourcesModBlocks.IRON_CROP_GENERATOR.get(),
					SkyblockResourcesModBlocks.DIAMOND_CROP_GENERATOR.get(),
					SkyblockResourcesModBlocks.NETHERITE_CROP_GENERATOR.get()).build(null));

	private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String registryName, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<T> supplier) {
		return REGISTRY.register(registryName, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}
