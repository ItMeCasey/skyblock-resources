package com.itmecasey.skyblockresources.compat;

import java.util.function.BiFunction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * 1.20.1 recipe serializers are fromJson / fromNetwork / toNetwork and carry the recipe id, not a Codec. This builds one
 * from the same MapCodec the newer versions use. The recipe is sent to the client as its own JSON text.
 */
public final class Serializers {
	private static final int MAX_JSON = 1 << 22;

	private Serializers() {
	}

	/** @param withId copies a decoded recipe (which has a null id) with the recipe id set */
	public static <T extends Recipe<?>> RecipeSerializer<T> of(MapCodec<T> codec, BiFunction<T, ResourceLocation, T> withId) {
		return new RecipeSerializer<>() {
			private T read(ResourceLocation id, JsonElement json) {
				DataResult<T> result = codec.codec().parse(JsonOps.INSTANCE, json);
				if (result.error().isPresent())
					throw new JsonParseException(result.error().get().message());
				return withId.apply(result.result().get(), id);
			}

			@Override
			public T fromJson(ResourceLocation id, JsonObject json) {
				return read(id, json);
			}

			@Override
			public T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
				return read(id, JsonParser.parseString(buffer.readUtf(MAX_JSON)));
			}

			@Override
			public void toNetwork(FriendlyByteBuf buffer, T recipe) {
				JsonElement json = codec.codec().encodeStart(JsonOps.INSTANCE, recipe).getOrThrow(false, message -> {
					throw new IllegalStateException("Cannot encode recipe: " + message);
				});
				buffer.writeUtf(json.toString(), MAX_JSON);
			}
		};
	}
}
