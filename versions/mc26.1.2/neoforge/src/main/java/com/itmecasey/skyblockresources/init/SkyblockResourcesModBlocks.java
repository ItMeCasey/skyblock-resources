package com.itmecasey.skyblockresources.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;

import com.itmecasey.skyblockresources.block.*;
import com.itmecasey.skyblockresources.configuration.ConfigurationConfiguration;
import com.itmecasey.skyblockresources.SkyblockResourcesMod;

import java.util.function.Function;

public class SkyblockResourcesModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(SkyblockResourcesMod.MODID);
	public static final DeferredBlock<Block> WOODEN_GENERATOR = register("wooden_generator", WoodenGeneratorBlock::new);
	public static final DeferredBlock<Block> STONE_GENERATOR = register("stone_generator", StoneGeneratorBlock::new);
	public static final DeferredBlock<Block> IRON_GENERATOR = register("iron_generator", IronGeneratorBlock::new);
	public static final DeferredBlock<Block> DIAMOND_GENERATOR = register("diamond_generator", DiamondGeneratorBlock::new);
	public static final DeferredBlock<Block> NETHERITE_GENERATOR = register("netherite_generator", NetheriteGeneratorBlock::new);
	public static final DeferredBlock<Block> WOODEN_TREE_GENERATOR = register("wooden_tree_generator", WoodenTreeGeneratorBlock::new);
	public static final DeferredBlock<Block> STONE_TREE_GENERATOR = register("stone_tree_generator", StoneTreeGeneratorBlock::new);
	public static final DeferredBlock<Block> IRON_TREE_GENERATOR = register("iron_tree_generator", IronTreeGeneratorBlock::new);
	public static final DeferredBlock<Block> DIAMOND_TREE_GENERATOR = register("diamond_tree_generator", DiamondTreeGeneratorBlock::new);
	public static final DeferredBlock<Block> NETHERITE_TREE_GENERATOR = register("netherite_tree_generator", NetheriteTreeGeneratorBlock::new);
	public static final DeferredBlock<Block> WOODEN_CROP_GENERATOR = register("wooden_crop_generator",
			props -> new CropGeneratorBlock(props, new CropGeneratorBlock.Tier(() -> ConfigurationConfiguration.WOODEN_CROP_SLOTS.get(),
					() -> ConfigurationConfiguration.WOODEN_CROP_MAX_TICKS.get(),
					() -> ConfigurationConfiguration.WOODEN_MAX_TRANSFER.get())));
	public static final DeferredBlock<Block> STONE_CROP_GENERATOR = register("stone_crop_generator",
			props -> new CropGeneratorBlock(props, new CropGeneratorBlock.Tier(() -> ConfigurationConfiguration.STONE_CROP_SLOTS.get(),
					() -> ConfigurationConfiguration.STONE_CROP_MAX_TICKS.get(),
					() -> ConfigurationConfiguration.STONE_CROP_MAX_TRANSFER.get())));
	public static final DeferredBlock<Block> IRON_CROP_GENERATOR = register("iron_crop_generator",
			props -> new CropGeneratorBlock(props, new CropGeneratorBlock.Tier(() -> ConfigurationConfiguration.IRON_CROP_SLOTS.get(),
					() -> ConfigurationConfiguration.IRON_CROP_MAX_TICKS.get(),
					() -> ConfigurationConfiguration.IRON_CROP_MAX_TRANSFER.get())));
	public static final DeferredBlock<Block> DIAMOND_CROP_GENERATOR = register("diamond_crop_generator",
			props -> new CropGeneratorBlock(props, new CropGeneratorBlock.Tier(() -> ConfigurationConfiguration.DIAMOND_CROP_SLOTS.get(),
					() -> ConfigurationConfiguration.DIAMOND_CROP_MAX_TICKS.get(),
					() -> ConfigurationConfiguration.DIAMOND_CROP_MAX_TRANSFER.get())));
	public static final DeferredBlock<Block> NETHERITE_CROP_GENERATOR = register("netherite_crop_generator",
			props -> new CropGeneratorBlock(props, new CropGeneratorBlock.Tier(() -> ConfigurationConfiguration.NETHERITE_CROP_SLOTS.get(),
					() -> ConfigurationConfiguration.NETHERITE_CROP_MAX_TICKS.get(),
					() -> ConfigurationConfiguration.NETHERITE_CROP_MAX_TRANSFER.get())));

	private static <B extends Block> DeferredBlock<B> register(String name, Function<BlockBehaviour.Properties, ? extends B> supplier) {
		return REGISTRY.registerBlock(name, supplier);
	}
}
