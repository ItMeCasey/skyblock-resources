package com.itmecasey.skyblockresources.integration.jei;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.constants.VanillaTypes;

public abstract class AbstractJeiCategory<T> implements IRecipeCategory<T> {
	private final IRecipeType<T> recipeType;
	private final Component title;
	private final IDrawable background;
	private final IDrawable icon;

	public AbstractJeiCategory(IRecipeType<T> recipeType, String title, IDrawable background, IDrawable icon) {
		this.recipeType = recipeType;
		this.title = Component.translatable("jei.skyblock_resources." + title);
		this.background = background;
		this.icon = icon;
	}

	protected static IDrawable background(IGuiHelper helper, String id, int u, int v, int width, int height) {
		if (id == null || id.trim().isEmpty()) {
			return helper.createBlankDrawable(width, height);
		}
		var bid = Identifier.fromNamespaceAndPath("skyblock_resources", "textures/screens/" + id + ".png");
		return helper.createDrawable(bid, u, v, width, height);
	}

	protected static IDrawable icon(IGuiHelper helper, ItemLike item) {
		return helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item));
	}

	@Override
	public final IRecipeType<T> getRecipeType() {
		return recipeType;
	}

	public abstract Item[] getCatalysts();

	protected static Item[] catalysts(Item... items) {
		return items;
	}

	@Override
	public final Component getTitle() {
		return title;
	}

	@Override
	public final IDrawable getIcon() {
		return icon;
	}

	@Override
	public final int getWidth() {
		return background.getWidth();
	}

	@Override
	public final int getHeight() {
		return background.getHeight();
	}

	@Override
	public final void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
		background.draw(guiGraphics);
		draw(recipe, guiGraphics, mouseX, mouseY);
	}

	public abstract void draw(T recipe, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY);

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
		builder.addWidget(new IRecipeWidget() {
			@Override
			public ScreenPosition getPosition() {
				return new ScreenPosition(0, 0);
			}

			@Override
			public void drawWidget(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
				drawExtras(recipe, guiGraphics, mouseX, mouseY);
			}
		});
	}

	public void drawExtras(T recipe, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
	}

	@Override
	public final void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		setRecipe(builder, recipe);
	}

	public abstract void setRecipe(IRecipeLayoutBuilder builder, T recipe);
}