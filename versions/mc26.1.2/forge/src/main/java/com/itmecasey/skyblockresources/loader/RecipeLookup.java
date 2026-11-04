package com.itmecasey.skyblockresources.loader;

import java.lang.reflect.Field;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;

/**
 * NeoForge adds {@code RecipeManager.recipeMap()}; vanilla keeps the map in a private field, so read it from there.
 * If the field cannot be read, a map is built from the manager's recipes (slower, and rebuilt on every call).
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
		return RecipeMap.create(manager.getRecipes());
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
