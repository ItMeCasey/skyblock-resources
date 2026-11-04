package com.itmecasey.skyblockresources.compat;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Scaled, aligned text for the JEI pages. The coordinates are in the scaled text space, like the 26.x text renderer
 * takes them: the pose is scaled first and the text is then placed at (x, y) in that space.
 */
public final class GuiText {
	private GuiText() {
	}

	public static void draw(GuiGraphics graphics, TextAlignment alignment, int x, int y, float scale, Component text, int rgb) {
		Font font = Minecraft.getInstance().font;
		int drawX = switch (alignment) {
			case LEFT -> x;
			case CENTER -> x - font.width(text) / 2;
			case RIGHT -> x - font.width(text);
		};
		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.scale(scale, scale, 1.0f);
		graphics.drawString(font, text, drawX, y, 0xFF000000 | (rgb & 0xFFFFFF), false);
		pose.popPose();
	}
}
