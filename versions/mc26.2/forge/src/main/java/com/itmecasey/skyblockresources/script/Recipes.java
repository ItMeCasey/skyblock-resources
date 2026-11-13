package com.itmecasey.skyblockresources.script;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

/**
 * Builds the JSON of this mod's recipe types, so a script can pass it to the script engine's recipe event (ModJS:
 * {@code event.custom(JSON.parse(SkyblockResources.recipes.cropDrops(...)))}). Removing a recipe uses the engine's own
 * filters, for example {@code event.remove({ type: 'skyblock_resources:crop_drops' })}.
 */
public final class Recipes {
	private static final String MODID = "skyblock_resources";

	/** A resource generator recipe: the generator (a block id or #tag) makes {@code count} of {@code output} from {@code resource}. */
	public String resourceGenerator(String generator, String resource, String output, int count, double duration) {
		JsonObject json = base("resource_generator");
		json.addProperty("generator", generator);
		json.addProperty("resource", resource);
		json.add("output", stack(output, count));
		json.addProperty("duration", duration);
		return json.toString();
	}

	/** A soil: how much faster and richer a tree or crop generator with this soil block works. */
	public String soil(String soil, double speedBonus, double growthMultiplier, double bonusSeedChance) {
		JsonObject json = base("soil_type");
		json.addProperty("soil", soil);
		json.addProperty("speed_bonus", speedBonus);
		json.addProperty("growth_multiplier", growthMultiplier);
		json.addProperty("bonus_seed_chance", bonusSeedChance);
		return json.toString();
	}

	/** What a tree generator makes from a sapling on a soil. {@code drops} is a list of {id, min, max, chance} objects. */
	public String treeDrops(String sapling, String soil, String generator, String output, int count, List<?> drops, double duration) {
		return plant("tree_drops", "sapling", sapling, soil, generator, output, count, drops, duration);
	}

	/** What a crop generator makes from a seed on a soil. {@code drops} is a list of {id, min, max, chance} objects. */
	public String cropDrops(String seed, String soil, String generator, String output, int count, List<?> drops, double duration) {
		return plant("crop_drops", "seed", seed, soil, generator, output, count, drops, duration);
	}

	private static String plant(String type, String key, String plant, String soil, String generator, String output, int count, List<?> drops, double duration) {
		JsonObject json = base(type);
		json.addProperty(key, plant);
		json.addProperty("soil", soil);
		json.addProperty("generator", generator);
		json.add("outputs", stack(output, count));
		JsonArray list = new JsonArray();
		if (drops != null) {
			for (Object entry : drops) {
				if (entry instanceof Map<?, ?> map) {
					JsonObject drop = new JsonObject();
					drop.addProperty("id", String.valueOf(map.get("id")));
					copyNumber(map, drop, "min");
					copyNumber(map, drop, "max");
					copyNumber(map, drop, "chance");
					list.add(drop);
				}
			}
		}
		if (list.isEmpty()) {
			// A recipe with no drops is rejected by the engine, so list the main output.
			JsonObject drop = new JsonObject();
			drop.addProperty("id", output);
			drop.addProperty("min", Math.max(1, count));
			list.add(drop);
		}
		json.add("drops", list);
		json.addProperty("duration", duration);
		return json.toString();
	}

	private static void copyNumber(Map<?, ?> from, JsonObject to, String key) {
		Object value = from.get(key);
		if (value instanceof Number number) {
			to.addProperty(key, number);
		}
	}

	private static JsonObject base(String type) {
		JsonObject json = new JsonObject();
		json.addProperty("type", MODID + ":" + type);
		return json;
	}

	private static JsonObject stack(String id, int count) {
		JsonObject json = new JsonObject();
		json.addProperty("id", id);
		json.addProperty("count", Math.max(1, count));
		return json;
	}
}
