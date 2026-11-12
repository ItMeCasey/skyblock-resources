package com.itmecasey.skyblockresources.integration.rei;

import com.itmecasey.skyblockresources.integration.viewer.ViewerCards;
import com.mojang.serialization.MapCodec;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * REI support, the same pages as the JEI plugin: resource generator outputs, tree drops, crop drops and soil types,
 * with every tier of the generator as a workstation. The displays are built on the client from the recipes the
 * server sent (see {@link ViewerCards}). REI finds this class by its annotation and only when REI is installed.
 */
@REIPluginClient
public class SkyblockResourcesReiPlugin implements REIClientPlugin {
	private static final int SLOT = 18;
	private static final int TEXT = 0xFF606060;

	private static CategoryIdentifier<SkyDisplay> id(ViewerCards.Kind kind) {
		return CategoryIdentifier.of(Identifier.fromNamespaceAndPath("skyblock_resources", kind.path()));
	}

	@Override
	public void registerCategories(CategoryRegistry registry) {
		for (ViewerCards.Kind kind : ViewerCards.Kind.values()) {
			registry.add(new Category(kind));
			if (kind != ViewerCards.Kind.SOIL) {
				registry.configure(id(kind), config -> {
					for (ItemStack station : kind.stations()) {
						config.addWorkstations(EntryStacks.of(station));
					}
				});
			}
		}
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		for (ViewerCards.Card card : ViewerCards.all()) {
			registry.add(new SkyDisplay(card));
		}
	}

	/** One card as REI sees it: the inputs, the outputs, and the card itself for the page to read. */
	static final class SkyDisplay extends BasicDisplay {
		/**
		 * These displays are made on the client and never travel, so the serializer only exists because REI asks every display
		 * for one; it refuses to encode anything.
		 */
		private static final DisplaySerializer<SkyDisplay> SERIALIZER = DisplaySerializer.of(MapCodec.unit(() -> {
			throw new UnsupportedOperationException("Skyblock Resources REI displays are client-side only");
		}), StreamCodec.of((buf, display) -> {
			throw new UnsupportedOperationException("Skyblock Resources REI displays are client-side only");
		}, buf -> {
			throw new UnsupportedOperationException("Skyblock Resources REI displays are client-side only");
		}), false);

		private final ViewerCards.Card card;

		SkyDisplay(ViewerCards.Card card) {
			super(card.inputs().stream().map(EntryIngredients::ofItemStacks).toList(), card.outputs().stream().map(EntryIngredients::of).toList(), Optional.empty());
			this.card = card;
		}

		ViewerCards.Card card() {
			return card;
		}

		@Override
		public CategoryIdentifier<?> getCategoryIdentifier() {
			return id(card.kind());
		}

		@Override
		public DisplaySerializer<? extends SkyDisplay> getSerializer() {
			return SERIALIZER;
		}
	}

	private static final class Category implements DisplayCategory<SkyDisplay> {
		private final ViewerCards.Kind kind;

		Category(ViewerCards.Kind kind) {
			this.kind = kind;
		}

		@Override
		public CategoryIdentifier<? extends SkyDisplay> getCategoryIdentifier() {
			return id(kind);
		}

		@Override
		public Component getTitle() {
			return Component.translatable(kind.titleKey());
		}

		@Override
		public Renderer getIcon() {
			return EntryStacks.of(kind.stations().get(0));
		}

		@Override
		public int getDisplayHeight() {
			return 52;
		}

		@Override
		public int getDisplayWidth(SkyDisplay display) {
			return kind == ViewerCards.Kind.SOIL ? 160 : 12 + kind.inputs() * SLOT + 28 + (kind == ViewerCards.Kind.RESOURCE ? 1 : ViewerCards.DROP_SLOTS) * SLOT;
		}

		@Override
		public List<Widget> setupDisplay(SkyDisplay display, Rectangle bounds) {
			List<Widget> widgets = new ArrayList<>();
			widgets.add(Widgets.createRecipeBase(bounds));
			int x = bounds.x + 6;
			int y = bounds.y + 8;
			ViewerCards.Card card = display.card();
			List<EntryIngredient> inputs = display.getInputEntries();
			for (int i = 0; i < inputs.size(); i++) {
				widgets.add(Widgets.createSlot(new Point(x + i * SLOT, y)).entries(inputs.get(i)).markInput());
			}
			if (kind == ViewerCards.Kind.SOIL) {
				int textX = x + SLOT + 10;
				widgets.add(label(textX, y, Component.translatable("skyblock_resources.viewer.speed", Math.round(card.speed() * 100))));
				widgets.add(label(textX, y + 11, Component.translatable("skyblock_resources.viewer.growth", new DecimalFormat("#.##").format(card.growth()))));
				widgets.add(label(textX, y + 22, Component.translatable("skyblock_resources.viewer.bonus_seed", Math.round(card.bonusSeed() * 100))));
				return widgets;
			}
			int arrowX = x + inputs.size() * SLOT + 4;
			widgets.add(Widgets.createArrow(new Point(arrowX, y)));
			List<EntryIngredient> outputs = display.getOutputEntries();
			for (int i = 0; i < outputs.size(); i++) {
				double chance = card.chances().get(i);
				EntryIngredient shown = chance < 1.0 ? outputs.get(i).map(stack -> stack.copy().tooltip(Component.translatable("skyblock_resources.viewer.chance", Math.round(chance * 1000) / 10.0))) : outputs.get(i);
				widgets.add(Widgets.createSlot(new Point(arrowX + 28 + i * SLOT, y)).entries(shown).markOutput());
			}
			widgets.add(label(x, y + 26, Component.translatable("skyblock_resources.viewer.ticks", card.ticks())));
			return widgets;
		}

		private static Widget label(int x, int y, Component text) {
			return Widgets.createLabel(new Point(x, y), text).leftAligned().noShadow().color(TEXT);
		}
	}
}
