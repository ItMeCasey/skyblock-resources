package com.itmecasey.skyblockresources.integration.bei;

import com.itmecasey.skyblockresources.SkyblockResourcesMod;
import java.util.List;
import net.minecraft.client.Minecraft;

/**
 * Forge 26.1 sends this mod's recipes in a message of its own, which can arrive after BEI has already built its index from the
 * server's recipes. When the message arrives, this asks BEI to read its stored server recipes again, so BEI asks the plugin for
 * its cards a second time with the recipes now in place. Does nothing without BEI: the class lookup fails and nothing happens.
 */
public final class BeiRefresh {
	private BeiRefresh() {
	}

	/** Called on the client after this mod's recipes have arrived. */
	public static void afterRecipes() {
		Minecraft.getInstance().execute(() -> {
			try {
				Class.forName("com.itmecasey.bei.index.RecipeIndex").getMethod("receive", boolean.class, List.class, boolean.class).invoke(null, false, List.of(), true);
			} catch (ReflectiveOperationException | LinkageError e) {
				SkyblockResourcesMod.LOGGER.debug("Could not ask BEI to read the recipes again", e);
			}
		});
	}
}
