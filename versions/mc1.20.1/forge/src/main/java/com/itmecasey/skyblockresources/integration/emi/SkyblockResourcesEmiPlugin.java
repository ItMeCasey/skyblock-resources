package com.itmecasey.skyblockresources.integration.emi;

import com.itmecasey.skyblockresources.integration.viewer.ViewerCards;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * EMI support, the same pages as the JEI and REI plugins: resource generator outputs, tree drops, crop drops and soil
 * types, with every tier of the generator as a workstation. EMI finds this class by its annotation and only when EMI
 * is installed.
 */
@EmiEntrypoint
public class SkyblockResourcesEmiPlugin implements EmiPlugin {
	/** The category of each kind of card, made once: EMI compares categories by identity. */
	private static final Map<ViewerCards.Kind, EmiRecipeCategory> CATEGORIES = new EnumMap<>(ViewerCards.Kind.class);

	static {
		for (ViewerCards.Kind kind : ViewerCards.Kind.values()) {
			CATEGORIES.put(kind, new KindCategory(kind));
		}
	}

	@Override
	public void register(EmiRegistry registry) {
		CATEGORIES.forEach((kind, category) -> {
			registry.addCategory(category);
			if (kind != ViewerCards.Kind.SOIL) {
				for (ItemStack station : kind.stations()) {
					registry.addWorkstation(category, EmiStack.of(station));
				}
			}
		});
		for (ViewerCards.Card card : ViewerCards.all()) {
			registry.addRecipe(new CardRecipe(CATEGORIES.get(card.kind()), card));
		}
	}

	private static final class KindCategory extends EmiRecipeCategory {
		private final ViewerCards.Kind kind;

		KindCategory(ViewerCards.Kind kind) {
			super(new ResourceLocation("skyblock_resources", kind.path()), EmiStack.of(kind.stations().get(0)));
			this.kind = kind;
		}

		@Override
		public Component getName() {
			return Component.translatable(kind.titleKey());
		}
	}

	/** One card in EMI: the inputs, an arrow that fills as long as the wooden generator takes, the outputs, and the time below. */
	private static final class CardRecipe extends BasicEmiRecipe {
		private static final int SLOT = 18;
		/** One game tick in milliseconds, for the arrow. */
		private static final int TICK_MS = 50;

		private final ViewerCards.Card card;

		CardRecipe(EmiRecipeCategory category, ViewerCards.Card card) {
			super(category, new ResourceLocation(card.id()), card.kind() == ViewerCards.Kind.SOIL ? 150 : 12 + card.kind().inputs() * SLOT + 28 + (card.kind() == ViewerCards.Kind.RESOURCE ? 1 : ViewerCards.DROP_SLOTS) * SLOT,
					card.kind() == ViewerCards.Kind.SOIL ? 40 : 40);
			this.card = card;
			for (var stacks : card.inputs()) {
				inputs.add(EmiIngredient.of(stacks.stream().map(EmiStack::of).toList()));
			}
			for (int i = 0; i < card.outputs().size(); i++) {
				outputs.add(EmiStack.of(card.outputs().get(i)).setChance(card.chances().get(i).floatValue()));
			}
		}

		@Override
		public void addWidgets(WidgetHolder widgets) {
			for (int i = 0; i < inputs.size(); i++) {
				widgets.addSlot(inputs.get(i), 1 + i * SLOT, 5);
			}
			if (card.kind() == ViewerCards.Kind.SOIL) {
				int x = SLOT + 10;
				widgets.addText(Component.translatable("skyblock_resources.viewer.speed", Math.round(card.speed() * 100)), x, 3, 0xFF606060, false);
				widgets.addText(Component.translatable("skyblock_resources.viewer.growth", new DecimalFormat("#.##").format(card.growth())), x, 14, 0xFF606060, false);
				widgets.addText(Component.translatable("skyblock_resources.viewer.bonus_seed", Math.round(card.bonusSeed() * 100)), x, 25, 0xFF606060, false);
				return;
			}
			int arrowX = 1 + inputs.size() * SLOT + 4;
			widgets.addFillingArrow(arrowX, 6, Math.max(1, card.ticks()) * TICK_MS);
			for (int i = 0; i < outputs.size(); i++) {
				widgets.addSlot(outputs.get(i), arrowX + 28 + i * SLOT, 5).recipeContext(this);
			}
			widgets.addText(Component.translatable("skyblock_resources.viewer.ticks", card.ticks()), 0, 28, 0xFF606060, false);
		}
	}
}
