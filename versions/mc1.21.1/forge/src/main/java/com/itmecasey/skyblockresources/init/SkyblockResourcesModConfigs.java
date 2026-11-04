package com.itmecasey.skyblockresources.init;

import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.itmecasey.skyblockresources.configuration.ConfigurationConfiguration;

public class SkyblockResourcesModConfigs {
	/**
	 * Forge registers configs from the mod constructor. NeoForge also registers an automatic config screen
	 * (ConfigurationScreen); Forge 1.21.1 has none, so the file skyblock_resources.toml is edited by hand.
	 */
	public static void register(FMLJavaModLoadingContext context) {
		context.registerConfig(ModConfig.Type.COMMON, ConfigurationConfiguration.SPEC, "skyblock_resources.toml");
	}
}
