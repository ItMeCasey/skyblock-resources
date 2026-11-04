package com.itmecasey.skyblockresources;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import com.itmecasey.skyblockresources.loader.client.SubmitCustomGeometryEvent;

/**
 * Draws the floating crop, sapling, or resource above a generator. NeoForge draws these from its
 * SubmitCustomGeometryEvent, which scans the area around the player; Forge 26.1.2 has no such event, so each generator
 * block entity draws its own overlay here, and the overlay classes' per-position code is the same.
 */
public class GeneratorOverlayRenderer implements BlockEntityRenderer<BlockEntity, BlockEntityRenderState> {
	/** The overlays only show within this many blocks, as with NeoForge. */
	private static final double MAX_DISTANCE = 16.0;

	@Override
	public BlockEntityRenderState createRenderState() {
		return new BlockEntityRenderState();
	}

	@Override
	public void extractRenderState(BlockEntity entity, BlockEntityRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(entity, state, crumbling);
	}

	@Override
	public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null)
			return;
		if (minecraft.player.distanceToSqr(Vec3.atCenterOf(state.blockPos)) > MAX_DISTANCE * MAX_DISTANCE)
			return;
		// The renderer's pose stack is already moved to the block; the overlay code moves from the camera itself.
		poseStack.pushPose();
		poseStack.translate(camera.pos.x - state.blockPos.getX(), camera.pos.y - state.blockPos.getY(), camera.pos.z - state.blockPos.getZ());
		SubmitCustomGeometryEvent event = new SubmitCustomGeometryEvent(camera, collector, poseStack);
		CropGeneratorOverlay.renderAt(event, minecraft, state.blockPos);
		TreeGeneratorOverlay.renderAt(event, minecraft, state.blockPos);
		ShowGeneratorResource.renderAt(event, minecraft, state.blockPos);
		poseStack.popPose();
	}

	@Override
	public int getViewDistance() {
		return (int) MAX_DISTANCE;
	}
}
