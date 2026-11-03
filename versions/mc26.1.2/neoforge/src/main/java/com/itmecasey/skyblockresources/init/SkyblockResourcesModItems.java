package com.itmecasey.skyblockresources.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import com.itmecasey.skyblockresources.item.UpgradeCardItem;
import com.itmecasey.skyblockresources.item.ScrewdriverItem;
import com.itmecasey.skyblockresources.SkyblockResourcesMod;

import java.util.function.Function;

public class SkyblockResourcesModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(SkyblockResourcesMod.MODID);
	public static final DeferredItem<Item> WOODEN_GENERATOR = block(SkyblockResourcesModBlocks.WOODEN_GENERATOR);
	public static final DeferredItem<Item> STONE_GENERATOR = block(SkyblockResourcesModBlocks.STONE_GENERATOR);
	public static final DeferredItem<Item> IRON_GENERATOR = block(SkyblockResourcesModBlocks.IRON_GENERATOR);
	public static final DeferredItem<Item> DIAMOND_GENERATOR = block(SkyblockResourcesModBlocks.DIAMOND_GENERATOR);
	public static final DeferredItem<Item> NETHERITE_GENERATOR = block(SkyblockResourcesModBlocks.NETHERITE_GENERATOR);
	public static final DeferredItem<Item> STONE_UPGRADE = register("stone_upgrade", props -> new UpgradeCardItem(props, 1));
	public static final DeferredItem<Item> IRON_UPGRADE = register("iron_upgrade", props -> new UpgradeCardItem(props, 2));
	public static final DeferredItem<Item> DIAMOND_UPGRADE = register("diamond_upgrade", props -> new UpgradeCardItem(props, 3));
	public static final DeferredItem<Item> NETHERITE_UPGRADE = register("netherite_upgrade", props -> new UpgradeCardItem(props, 4));
	public static final DeferredItem<Item> SCREWDRIVER = register("screwdriver", ScrewdriverItem::new);
	public static final DeferredItem<Item> WOODEN_TREE_GENERATOR = block(SkyblockResourcesModBlocks.WOODEN_TREE_GENERATOR);
	public static final DeferredItem<Item> STONE_TREE_GENERATOR = block(SkyblockResourcesModBlocks.STONE_TREE_GENERATOR);
	public static final DeferredItem<Item> IRON_TREE_GENERATOR = block(SkyblockResourcesModBlocks.IRON_TREE_GENERATOR);
	public static final DeferredItem<Item> DIAMOND_TREE_GENERATOR = block(SkyblockResourcesModBlocks.DIAMOND_TREE_GENERATOR);
	public static final DeferredItem<Item> NETHERITE_TREE_GENERATOR = block(SkyblockResourcesModBlocks.NETHERITE_TREE_GENERATOR);
	public static final DeferredItem<Item> WOODEN_CROP_GENERATOR = block(SkyblockResourcesModBlocks.WOODEN_CROP_GENERATOR);
	public static final DeferredItem<Item> STONE_CROP_GENERATOR = block(SkyblockResourcesModBlocks.STONE_CROP_GENERATOR);
	public static final DeferredItem<Item> IRON_CROP_GENERATOR = block(SkyblockResourcesModBlocks.IRON_CROP_GENERATOR);
	public static final DeferredItem<Item> DIAMOND_CROP_GENERATOR = block(SkyblockResourcesModBlocks.DIAMOND_CROP_GENERATOR);
	public static final DeferredItem<Item> NETHERITE_CROP_GENERATOR = block(SkyblockResourcesModBlocks.NETHERITE_CROP_GENERATOR);

	private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, ? extends I> supplier) {
		return REGISTRY.registerItem(name, supplier, Item.Properties::new);
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.registerItem(block.getId().getPath(), prop -> new BlockItem(block.get(), prop), () -> properties);
	}
}
