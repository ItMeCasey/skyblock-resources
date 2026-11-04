package com.itmecasey.skyblockresources.compat;

import java.util.List;
import java.util.ArrayList;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * The recipe JSON of this mod writes an ingredient as a string ("#namespace:tag" or "namespace:item"), as 26.1.2 reads it.
 * 1.21.1's own Ingredient codec wants {"tag": ...} or {"item": ...}. This codec reads both shapes (a string, a list of
 * strings, or the native form) and writes the native form.
 */
public final class IngredientCodecs {
	private IngredientCodecs() {
	}

	private static DataResult<Ingredient> fromString(String text) {
		if (text.startsWith("#")) {
			ResourceLocation id = ResourceLocation.tryParse(text.substring(1));
			if (id == null)
				return DataResult.error(() -> "Invalid tag id: " + text);
			return DataResult.success(Ingredient.of(TagKey.create(Registries.ITEM, id)));
		}
		ResourceLocation id = ResourceLocation.tryParse(text);
		if (id == null)
			return DataResult.error(() -> "Invalid item id: " + text);
		Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
		if (item == null)
			return DataResult.error(() -> "Unknown item: " + text);
		return DataResult.success(Ingredient.of(item));
	}

	private static final Codec<Ingredient> STRING_FORM = Codec.STRING.comapFlatMap(IngredientCodecs::fromString, ingredient -> "");

	private static final Codec<Ingredient> STRING_LIST_FORM = Codec.list(Codec.STRING).comapFlatMap(list -> {
		if (list.isEmpty())
			return DataResult.error(() -> "Empty ingredient list");
		List<Ingredient.Value> values = new ArrayList<>();
		for (String entry : list) {
			if (entry.startsWith("#")) {
				ResourceLocation id = ResourceLocation.tryParse(entry.substring(1));
				if (id == null)
					return DataResult.error(() -> "Invalid tag id: " + entry);
				values.add(new Ingredient.TagValue(TagKey.create(Registries.ITEM, id)));
			} else {
				ResourceLocation id = ResourceLocation.tryParse(entry);
				Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
				if (item == null)
					return DataResult.error(() -> "Unknown item: " + entry);
				values.add(new Ingredient.ItemValue(new ItemStack(item)));
			}
		}
		return DataResult.success(Ingredient.fromValues(values.stream()));
	}, ingredient -> List.of());

	/** Reads the string forms first, then the native form; writes the native form. */
	public static final Codec<Ingredient> LENIENT = Codec.withAlternative(Ingredient.CODEC, Codec.withAlternative(STRING_FORM, STRING_LIST_FORM));

	/**
	 * The stacks an ingredient accepts. 1.21.1 puts a named barrier in place of an empty tag; that placeholder is not an
	 * item the recipe wants, so it is left out.
	 */
	public static List<ItemStack> items(Ingredient ingredient) {
		List<ItemStack> stacks = new ArrayList<>();
		for (ItemStack stack : ingredient.getItems()) {
			if (stack.is(Items.BARRIER) && stack.has(DataComponents.CUSTOM_NAME))
				continue;
			stacks.add(stack);
		}
		return stacks;
	}
}
