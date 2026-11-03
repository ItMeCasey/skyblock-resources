package com.itmecasey.skyblockresources.smwti;

import java.util.Locale;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.itmecasey.smwti.api.ClientRegistration;
import com.itmecasey.smwti.api.Element;
import com.itmecasey.smwti.api.ISmwtiPlugin;
import com.itmecasey.smwti.api.Position;
import com.itmecasey.smwti.api.SmwtiPlugin;
import com.itmecasey.smwti.client.Palette;

import com.itmecasey.skyblockresources.machine.MachineHelpers;

/**
 * Show Me What This Is support: under a machine's name, what it is working on (centered) and a blue progress bar showing
 * how far along its current cycle is. Loaded by SMWTI only when SMWTI is installed.
 */
@SmwtiPlugin
public class SkyblockResourcesSmwtiPlugin implements ISmwtiPlugin {
	private static final int PROGRESS_COLOR = 0xFF3A7BFF;
	private static final int BAR_HEIGHT = 3;

	@Override
	public void registerClient(ClientRegistration registration) {
		registration.block((tooltip, view) -> {
			BlockEntity entity = view.entity();
			if (entity == null || !entity.getPersistentData().contains("timer"))
				return;
			double ratio = MachineHelpers.progress(entity);
			if (ratio <= 0)
				return;
			tooltip.element(Position.HEAD, new WorkElement(workingOn(entity), ratio));
		});
	}

	/** The resource, sapling, or seed the machine is working on, or empty when none is set. */
	private static Component workingOn(BlockEntity entity) {
		String resource = entity.getPersistentData().getStringOr("resource_name", "");
		if (resource.isEmpty() || resource.equals("empty"))
			return Component.empty();
		Identifier id = Identifier.tryParse(resource.toLowerCase(Locale.ENGLISH));
		Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
		return item == null ? Component.empty() : item.getDefaultInstance().getHoverName();
	}

	/** A centered name line over a progress bar, drawn the way the overlay draws its own bars. */
	private record WorkElement(Component label, double ratio) implements Element {
		@Override
		public int width(Font font) {
			return font.width(label);
		}

		@Override
		public int height(Font font) {
			return (label.getString().isEmpty() ? 0 : font.lineHeight + 1) + BAR_HEIGHT;
		}

		@Override
		public void draw(GuiGraphicsExtractor graphics, Font font, int x, int y, int width) {
			Palette palette = Palette.current();
			int barY = y;
			if (!label.getString().isEmpty()) {
				graphics.text(font, label, x + (width - font.width(label)) / 2, y, palette.text(), false);
				barY += font.lineHeight + 1;
			}
			graphics.fill(x, barY, x + width, barY + BAR_HEIGHT, palette.barBack());
			graphics.fill(x, barY, x + Math.round(width * (float) Math.min(1.0, ratio)), barY + BAR_HEIGHT, PROGRESS_COLOR);
		}
	}
}
