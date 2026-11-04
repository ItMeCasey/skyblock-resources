package com.itmecasey.skyblockresources.loader.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * What the overlay renderers draw with, shaped like NeoForge's SubmitCustomGeometryEvent. Forge 26.1.2 has no such
 * event, so a block entity renderer (see GeneratorOverlayRenderer) builds one per generator per frame. The pose
 * stack starts at the camera, as NeoForge's does.
 */
public final class SubmitCustomGeometryEvent {
	private final LevelRenderState levelRenderState;
	private final SubmitNodeCollector submitNodeCollector;
	private final PoseStack poseStack;

	public SubmitCustomGeometryEvent(CameraRenderState camera, SubmitNodeCollector submitNodeCollector, PoseStack poseStack) {
		this.levelRenderState = new LevelRenderState(camera);
		this.submitNodeCollector = submitNodeCollector;
		this.poseStack = poseStack;
	}

	public LevelRenderState getLevelRenderState() {
		return levelRenderState;
	}

	public SubmitNodeCollector getSubmitNodeCollector() {
		return submitNodeCollector;
	}

	public PoseStack getPoseStack() {
		return poseStack;
	}

	/** The part of the level render state the overlays read: the camera. */
	public static final class LevelRenderState {
		public final CameraRenderState cameraRenderState;

		LevelRenderState(CameraRenderState cameraRenderState) {
			this.cameraRenderState = cameraRenderState;
		}
	}
}
