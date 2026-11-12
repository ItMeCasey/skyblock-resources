package com.itmecasey.skyblockresources.loader;

import java.lang.reflect.Field;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;

/**
 * NeoForge adds {@code RecipeManager.recipeMap()}; vanilla keeps the map in a private field, so read it from there.
 * If the field cannot be read there is nothing to fall back to on 26.3, where RecipeMap only builds from a registry lookup.
 */
public final class RecipeLookup {
	private static final Field MAP_FIELD = findMapField();

	private RecipeLookup() {
	}

	public static RecipeMap recipeMap(RecipeManager manager) {
		if (MAP_FIELD != null) {
			try {
				RecipeMap map = (RecipeMap) MAP_FIELD.get(manager);
				if (map != null)
					return map;
			} catch (IllegalAccessException | RuntimeException e) {
				// Fall through to the slow path.
			}
		}
		throw new IllegalStateException("RecipeManager has no readable recipe map");
	}

	private static Field findMapField() {
		for (Field field : RecipeManager.class.getDeclaredFields()) {
			if (field.getType() == RecipeMap.class && !java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				try {
					field.setAccessible(true);
					return field;
				} catch (RuntimeException e) {
					return null;
				}
			}
		}
		return null;
	}
}
