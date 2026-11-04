package com.itmecasey.skyblockresources.init;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import com.itmecasey.skyblockresources.SkyblockResourcesMod;

public class SkyblockResourcesModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SkyblockResourcesMod.MODID);
	public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = REGISTRY.register("creative_tab",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.skyblock_resources.creative_tab")).icon(() -> new ItemStack(SkyblockResourcesModBlocks.NETHERITE_GENERATOR.get())).displayItems((parameters, tabData) -> {
				tabData.accept(SkyblockResourcesModBlocks.WOODEN_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.STONE_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.IRON_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.DIAMOND_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.NETHERITE_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModItems.SCREWDRIVER.get());
				tabData.accept(SkyblockResourcesModItems.STONE_UPGRADE.get());
				tabData.accept(SkyblockResourcesModItems.IRON_UPGRADE.get());
				tabData.accept(SkyblockResourcesModItems.DIAMOND_UPGRADE.get());
				tabData.accept(SkyblockResourcesModItems.NETHERITE_UPGRADE.get());
				tabData.accept(SkyblockResourcesModBlocks.WOODEN_TREE_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.STONE_TREE_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.IRON_TREE_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.DIAMOND_TREE_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.NETHERITE_TREE_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.WOODEN_CROP_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.STONE_CROP_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.IRON_CROP_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.DIAMOND_CROP_GENERATOR.get().asItem());
				tabData.accept(SkyblockResourcesModBlocks.NETHERITE_CROP_GENERATOR.get().asItem());
			}).withSearchBar().build());
}