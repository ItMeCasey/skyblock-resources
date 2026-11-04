package com.itmecasey.skyblockresources.compat;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 1.20.1 has no Codec for Ingredient or ItemStack, only JSON helpers. The recipe JSON of this mod writes an ingredient
 * as a string ("#namespace:tag" or "namespace:item"), as 26.1.2 reads it, or as a list of those. These codecs read
 * that, plus the native 1.20.1 form ({"item"}, {"tag"}, a list of them), and write the native form.
 * Item stacks are read from {"id", "count"} (26.1.2 / 1.21.1) or {"item", "count"} (1.20.1); counts above the stack size are kept.
 */
public final class IngredientCodecs {
	private IngredientCodecs() {
	}

	/** The item for an id. The item registry returns air for an unknown id, so check for the key first. */
	public static Item item(ResourceLocation id) {
		if (id == null || !BuiltInRegistries.ITEM.containsKey(id))
			throw new IllegalArgumentException("Unknown item: " + id);
		return BuiltInRegistries.ITEM.get(id);
	}

	private static Ingredient fromString(String text) {
		if (text.startsWith("#")) {
			ResourceLocation id = ResourceLocation.tryParse(text.substring(1));
			if (id == null)
				throw new IllegalArgumentException("Invalid tag id: " + text);
			return Ingredient.of(TagKey.create(Registries.ITEM, id));
		}
		return Ingredient.of(item(ResourceLocation.tryParse(text)));
	}

	static Ingredient parse(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
			return fromString(json.getAsString());
		if (json.isJsonArray()) {
			JsonArray array = json.getAsJsonArray();
			if (array.size() == 0)
				throw new IllegalArgumentException("Empty ingredient list");
			List<Ingredient.Value> values = new ArrayList<>();
			for (JsonElement entry : array) {
				if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
					String text = entry.getAsString();
					if (text.startsWith("#")) {
						ResourceLocation id = ResourceLocation.tryParse(text.substring(1));
						if (id == null)
							throw new IllegalArgumentException("Invalid tag id: " + text);
						values.add(new Ingredient.TagValue(TagKey.create(Registries.ITEM, id)));
					} else {
						values.add(new Ingredient.ItemValue(new ItemStack(item(ResourceLocation.tryParse(text)))));
					}
				} else {
					return Ingredient.fromJson(json);
				}
			}
			return Ingredient.fromValues(values.stream());
		}
		return Ingredient.fromJson(json);
	}

	private static <T> DataResult<T> guarded(JsonElement json, java.util.function.Function<JsonElement, T> reader) {
		try {
			return DataResult.success(reader.apply(json));
		} catch (RuntimeException e) {
			String message = e.getMessage() == null ? e.toString() : e.getMessage();
			return DataResult.error(() -> message);
		}
	}

	private static final Codec<JsonElement> JSON = Codec.PASSTHROUGH.xmap(dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(), json -> new Dynamic<>(JsonOps.INSTANCE, json));

	/** Reads the string forms first, then the native form; writes the native form. */
	public static final Codec<Ingredient> LENIENT = JSON.comapFlatMap(json -> guarded(json, IngredientCodecs::parse), Ingredient::toJson);

	static ItemStack parseStack(JsonElement json) {
		if (json.isJsonPrimitive())
			return new ItemStack(item(ResourceLocation.tryParse(json.getAsString())));
		JsonObject object = json.getAsJsonObject();
		JsonElement idElement = object.has("id") ? object.get("id") : object.get("item");
		if (idElement == null)
			throw new IllegalArgumentException("Item stack without \"id\" or \"item\"");
		ItemStack stack = new ItemStack(item(ResourceLocation.tryParse(idElement.getAsString())), object.has("count") ? object.get("count").getAsInt() : 1);
		if (object.has("nbt")) {
			try {
				JsonElement nbt = object.get("nbt");
				CompoundTag tag = nbt.isJsonObject() ? TagParser.parseTag(nbt.toString()) : TagParser.parseTag(nbt.getAsString());
				stack.setTag(tag);
			} catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
				throw new IllegalArgumentException("Invalid nbt: " + e.getMessage());
			}
		}
		return stack;
	}

	static JsonElement writeStack(ItemStack stack) {
		JsonObject object = new JsonObject();
		object.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
		object.add("count", new JsonPrimitive(stack.getCount()));
		if (stack.hasTag())
			object.addProperty("nbt", stack.getTag().toString());
		return object;
	}

	/** An item stack as the recipe output of this mod. Accepts both "id" and "item"; writes "item". */
	public static final Codec<ItemStack> STACK = JSON.comapFlatMap(json -> guarded(json, IngredientCodecs::parseStack), IngredientCodecs::writeStack);

	/** The stacks an ingredient accepts. */
	public static List<ItemStack> items(Ingredient ingredient) {
		List<ItemStack> stacks = new ArrayList<>();
		for (ItemStack stack : ingredient.getItems()) {
			if (!stack.isEmpty())
				stacks.add(stack);
		}
		return stacks;
	}
}
