package com.itmecasey.skyblockresources.compat;

import java.util.function.BiConsumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Draws the floating items, plant models, text and textured quads of the generator overlays with the 1.21.1
 * buffer-source API. 26.1.2 collects this geometry in a submit node collector; here it goes straight into the
 * level renderer's buffer source, which the caller flushes with {@link #flush()} once per frame.
 */
public final class OverlayRender {
	private OverlayRender() {
	}

	private static MultiBufferSource.BufferSource buffers() {
		return Minecraft.getInstance().renderBuffers().bufferSource();
	}

	public static void flush() {
		buffers().endBatch();
	}

	/** Runs {@code body} with the vertex consumer of {@code renderType} and the current pose. */
	public static void submitCustomGeometry(PoseStack poseStack, RenderType renderType, BiConsumer<PoseStack.Pose, VertexConsumer> body) {
		body.accept(poseStack.last(), buffers().getBuffer(renderType));
	}

	/** Draws one block model. {@code tint} is an RGB color for tinted faces, or -1 to leave them untinted. */
	public static void submitBlockModel(PoseStack poseStack, BlockState state, int tint, int light, int overlay) {
		var blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BakedModel model = blockRenderer.getBlockModel(state);
		float r = 1.0f;
		float g = 1.0f;
		float b = 1.0f;
		if (tint != -1) {
			r = (float) (tint >> 16 & 255) / 255.0f;
			g = (float) (tint >> 8 & 255) / 255.0f;
			b = (float) (tint & 255) / 255.0f;
		}
		RandomSource random = RandomSource.create(42L);
		for (RenderType chunkType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
			RenderType renderType = RenderTypeHelper.getEntityRenderType(chunkType, false);
			blockRenderer.getModelRenderer().renderModel(poseStack.last(), buffers().getBuffer(renderType), state, model, r, g, b, light, overlay, ModelData.EMPTY, chunkType);
		}
	}

	public static void submitItem(PoseStack poseStack, ItemStack stack, int light, int overlay) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poseStack, buffers(), minecraft.level, 0);
	}

	public static void submitText(PoseStack poseStack, float x, float y, String text, int light, int color) {
		Font font = Minecraft.getInstance().font;
		font.drawInBatch(Component.literal(text).getVisualOrderText(), x, y, color, false, poseStack.last().pose(), buffers(), Font.DisplayMode.NORMAL, 0, light);
	}
}
