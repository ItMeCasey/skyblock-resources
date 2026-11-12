package com.itmecasey.skyblockresources.loader.client;

import java.util.function.Consumer;

import com.mojang.blaze3d.pipeline.RenderPipeline;

/** Shaped like NeoForge's RegisterRenderPipelinesEvent; Forge 26.1.2 has none, so the client setup fires this itself. */
public final class RegisterRenderPipelinesEvent {
	private final Consumer<RenderPipeline> registrar;

	public RegisterRenderPipelinesEvent(Consumer<RenderPipeline> registrar) {
		this.registrar = registrar;
	}

	public void registerPipeline(RenderPipeline pipeline) {
		registrar.accept(pipeline);
	}
}
