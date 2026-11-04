package com.itmecasey.skyblockresources.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/** 1.20.1 recipes carry their own id; newer versions wrap them in RecipeHolder. This keeps the call sites the same. */
public record RecipeHolder<T extends Recipe<?>>(ResourceLocation id, T value) {
}
