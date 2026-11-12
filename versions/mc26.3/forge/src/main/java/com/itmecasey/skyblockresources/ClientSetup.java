package com.itmecasey.skyblockresources;

import java.lang.reflect.Field;
import java.util.Map;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlockEntities;
import com.itmecasey.skyblockresources.loader.client.RegisterRenderPipelinesEvent;

/** Client-only setup: the generator overlay renderer and the overlays' render pipelines. */
@Mod.EventBusSubscriber(modid = SkyblockResourcesMod.MODID, value = Dist.CLIENT)
public final class ClientSetup {
	private ClientSetup() {
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.WOODEN_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.STONE_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.IRON_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.DIAMOND_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.NETHERITE_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.WOODEN_TREE_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.STONE_TREE_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.IRON_TREE_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.DIAMOND_TREE_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.NETHERITE_TREE_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
		event.registerBlockEntityRenderer(SkyblockResourcesModBlockEntities.CROP_GENERATOR.get(), context -> new GeneratorOverlayRenderer());
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			RegisterRenderPipelinesEvent pipelines = new RegisterRenderPipelinesEvent(ClientSetup::registerPipeline);
			CropGeneratorOverlay.onRegisterBlockOverlayPipelines(pipelines);
			TreeGeneratorOverlay.onRegisterBlockOverlayPipelines(pipelines);
			ShowGeneratorResource.onRegisterBlockOverlayPipelines(pipelines);
		});
	}

	/**
	 * NeoForge's RegisterRenderPipelinesEvent adds a pipeline to vanilla's table so it is compiled with the others.
	 * Forge has no such hook and the table is private, so add to it by reflection; if that fails the pipeline is
	 * still compiled the first time it is drawn.
	 */
	@SuppressWarnings("unchecked")
	private static void registerPipeline(RenderPipeline pipeline) {
		try {
			Field table = RenderPipelines.class.getDeclaredField("PIPELINES_BY_LOCATION");
			table.setAccessible(true);
			((Map<Identifier, RenderPipeline>) table.get(null)).putIfAbsent(pipeline.getLocation(), pipeline);
		} catch (ReflectiveOperationException | RuntimeException e) {
			SkyblockResourcesMod.LOGGER.warn("Could not add pipeline {} to the pipeline table; it will be compiled on first use", pipeline.getLocation(), e);
		}
	}
}
