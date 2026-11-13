package com.itmecasey.skyblockresources.script;

import com.itmecasey.skyblockresources.machine.GeneratorTiers;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Script events. The generator code calls {@link #produced} and {@link #upgraded}; they do nothing unless a script listens,
 * so the mod behaves the same without ModJS. A listener that throws is logged on one line and skipped.
 */
public final class ScriptHooks {
	public static final String PRODUCED = "produced";
	public static final String UPGRADED = "upgraded";
	private static final String[] KINDS = {"resource", "tree", "crop"};
	private static final Map<String, List<Consumer<GeneratorEvent>>> LISTENERS = new ConcurrentHashMap<>();

	private ScriptHooks() {
	}

	public static void on(String event, Consumer<GeneratorEvent> listener) {
		if (!PRODUCED.equals(event) && !UPGRADED.equals(event)) {
			throw new IllegalArgumentException("unknown event '" + event + "', use 'produced' or 'upgraded'");
		}
		LISTENERS.computeIfAbsent(event, k -> new CopyOnWriteArrayList<>()).add(listener);
	}

	public static void remove(String event, Consumer<GeneratorEvent> listener) {
		List<Consumer<GeneratorEvent>> list = LISTENERS.get(event);
		if (list != null) {
			list.remove(listener);
		}
	}

	public static boolean hasListeners(String event) {
		List<Consumer<GeneratorEvent>> list = LISTENERS.get(event);
		return list != null && !list.isEmpty();
	}

	/** Called when a generator put items into its inventory. */
	public static void produced(LevelAccessor level, BlockPos pos, Item item, int inserted) {
		if (inserted <= 0 || level == null || level.isClientSide() || !hasListeners(PRODUCED)) {
			return;
		}
		emit(PRODUCED, level, pos, item, inserted);
	}

	/** Called after an upgrade card swapped the generator for the next tier. */
	public static void upgraded(Level level, BlockPos pos) {
		if (!hasListeners(UPGRADED)) {
			return;
		}
		emit(UPGRADED, level, pos, null, 0);
	}

	private static void emit(String name, LevelAccessor level, BlockPos pos, Item item, int count) {
		int tier = GeneratorTiers.tierOf(level.getBlockState(pos).getBlock());
		int kind = GeneratorTiers.kindOf(level.getBlockState(pos).getBlock());
		if (tier < 0 || kind < 0) {
			return;
		}
		String itemId = item == null ? "" : BuiltInRegistries.ITEM.getKey(item).toString();
		GeneratorEvent event = new GeneratorEvent(name, KINDS[kind], tier, tierName(tier), dimensionOf(level), pos.getX(), pos.getY(), pos.getZ(), itemId, count);
		dispatch(name, event);
	}

	/** Runs the listeners of an event; public so tests can drive it without a world. */
	public static void dispatch(String name, GeneratorEvent event) {
		List<Consumer<GeneratorEvent>> list = LISTENERS.get(name);
		if (list == null) {
			return;
		}
		for (Consumer<GeneratorEvent> listener : list) {
			try {
				listener.accept(event);
			} catch (RuntimeException | LinkageError e) {
				java.util.logging.Logger.getLogger("skyblock_resources").warning("A script listener for '" + name + "' failed: " + e);
			}
		}
	}

	public static String tierName(int tier) {
		return switch (tier) {
			case 0 -> "wooden";
			case 1 -> "stone";
			case 2 -> "iron";
			case 3 -> "diamond";
			case 4 -> "netherite";
			default -> "unknown";
		};
	}

	public static String kindName(int kind) {
		return kind >= 0 && kind < KINDS.length ? KINDS[kind] : "";
	}

	/** The dimension id, such as minecraft:overworld, read from the key's text so it works on every Minecraft version. */
	static String dimensionOf(LevelAccessor level) {
		if (level instanceof Level world) {
			String text = String.valueOf(world.dimension());
			int slash = text.lastIndexOf("/ ");
			int end = text.lastIndexOf(']');
			if (slash >= 0 && end > slash) {
				return text.substring(slash + 2, end).trim();
			}
			return text;
		}
		return "";
	}
}
