package com.itmecasey.skyblockresources.init;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import com.itmecasey.skyblockresources.item.UpgradeCardItem;
import com.itmecasey.skyblockresources.item.ScrewdriverItem;
import com.itmecasey.skyblockresources.SkyblockResourcesMod;

import java.util.function.Function;

public class SkyblockResourcesModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, SkyblockResourcesMod.MODID);
	public static final RegistryObject<Item> WOODEN_GENERATOR = block(SkyblockResourcesModBlocks.WOODEN_GENERATOR);
	public static final RegistryObject<Item> STONE_GENERATOR = block(SkyblockResourcesModBlocks.STONE_GENERATOR);
	public static final RegistryObject<Item> IRON_GENERATOR = block(SkyblockResourcesModBlocks.IRON_GENERATOR);
	public static final RegistryObject<Item> DIAMOND_GENERATOR = block(SkyblockResourcesModBlocks.DIAMOND_GENERATOR);
	public static final RegistryObject<Item> NETHERITE_GENERATOR = block(SkyblockResourcesModBlocks.NETHERITE_GENERATOR);
	public static final RegistryObject<Item> STONE_UPGRADE = register("stone_upgrade", props -> new UpgradeCardItem(props, 1));
	public static final RegistryObject<Item> IRON_UPGRADE = register("iron_upgrade", props -> new UpgradeCardItem(props, 2));
	public static final RegistryObject<Item> DIAMOND_UPGRADE = register("diamond_upgrade", props -> new UpgradeCardItem(props, 3));
	public static final RegistryObject<Item> NETHERITE_UPGRADE = register("netherite_upgrade", props -> new UpgradeCardItem(props, 4));
	public static final RegistryObject<Item> SCREWDRIVER = register("screwdriver", ScrewdriverItem::new);
	public static final RegistryObject<Item> WOODEN_TREE_GENERATOR = block(SkyblockResourcesModBlocks.WOODEN_TREE_GENERATOR);
	public static final RegistryObject<Item> STONE_TREE_GENERATOR = block(SkyblockResourcesModBlocks.STONE_TREE_GENERATOR);
	public static final RegistryObject<Item> IRON_TREE_GENERATOR = block(SkyblockResourcesModBlocks.IRON_TREE_GENERATOR);
	public static final RegistryObject<Item> DIAMOND_TREE_GENERATOR = block(SkyblockResourcesModBlocks.DIAMOND_TREE_GENERATOR);
	public static final RegistryObject<Item> NETHERITE_TREE_GENERATOR = block(SkyblockResourcesModBlocks.NETHERITE_TREE_GENERATOR);
	public static final RegistryObject<Item> WOODEN_CROP_GENERATOR = block(SkyblockResourcesModBlocks.WOODEN_CROP_GENERATOR);
	public static final RegistryObject<Item> STONE_CROP_GENERATOR = block(SkyblockResourcesModBlocks.STONE_CROP_GENERATOR);
	public static final RegistryObject<Item> IRON_CROP_GENERATOR = block(SkyblockResourcesModBlocks.IRON_CROP_GENERATOR);
	public static final RegistryObject<Item> DIAMOND_CROP_GENERATOR = block(SkyblockResourcesModBlocks.DIAMOND_CROP_GENERATOR);
	public static final RegistryObject<Item> NETHERITE_CROP_GENERATOR = block(SkyblockResourcesModBlocks.NETHERITE_CROP_GENERATOR);

	private static <I extends Item> RegistryObject<I> register(String name, Function<Item.Properties, ? extends I> supplier) {
		return REGISTRY.register(name, () -> supplier.apply(new Item.Properties()));
	}

	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return block(block, new Item.Properties());
	}

	private static RegistryObject<Item> block(RegistryObject<Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}
