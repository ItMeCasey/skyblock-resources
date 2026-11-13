package com.itmecasey.skyblockresources.script;

import com.itmecasey.skyblockresources.machine.GeneratorTiers;
import com.itmecasey.skyblockresources.machine.MachineHelpers;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The {@code SkyblockResources} global of ModJS scripts. One instance is made per script run, so the listeners a script registers
 * go away when the scripts reload. Server-side only; every call is safe to make from a server script or one of its events.
 *
 * <pre>
 * SkyblockResources.on('produced', e => console.info(e.tierName + ' ' + e.kind + ' made ' + e.count + ' ' + e.item));
 * SkyblockResources.on('upgraded', e => console.info('now ' + e.tierName));
 * const info = SkyblockResources.info(event.level, event.x, event.y, event.z);   // null when no generator is there
 * </pre>
 */
public final class SkyblockResourcesApi {
	/** Builds recipe JSON for {@code event.custom(...)}. */
	public final Recipes recipes = new Recipes();
	private final List<Object[]> mine = new ArrayList<>();

	/** Listens to "produced" (a generator put items into its inventory) or "upgraded" (an upgrade card changed its tier). */
	public void on(String event, Consumer<GeneratorEvent> listener) {
		ScriptHooks.on(event, listener);
		mine.add(new Object[] {event, listener});
	}

	/** Drops every listener this script run registered. The bridge calls this when the scripts reload. */
	public void clear() {
		for (Object[] entry : mine) {
			@SuppressWarnings("unchecked")
			Consumer<GeneratorEvent> listener = (Consumer<GeneratorEvent>) entry[1];
			ScriptHooks.remove((String) entry[0], listener);
		}
		mine.clear();
	}

	/** The tier names, wooden first. */
	public String[] tiers() {
		return new String[] {"wooden", "stone", "iron", "diamond", "netherite"};
	}

	/** Whether the block at the position is one of this mod's generators. {@code level} is the world a ModJS event gives you. */
	public boolean isGenerator(Object level, double x, double y, double z) {
		return info(level, x, y, z) != null;
	}

	/** A snapshot of the generator at the position, or null when there is none (or the world is not a server world). */
	public GeneratorInfo info(Object level, double x, double y, double z) {
		if (!(level instanceof LevelAccessor world) || world.isClientSide()) {
			return null;
		}
		BlockPos pos = BlockPos.containing(x, y, z);
		Block block = world.getBlockState(pos).getBlock();
		int tier = GeneratorTiers.tierOf(block);
		int kind = GeneratorTiers.kindOf(block);
		if (tier < 0 || kind < 0) {
			return null;
		}
		double timer = MachineHelpers.getBlockNumber(world, pos, "timer");
		double duration = MachineHelpers.getBlockNumber(world, pos, "duration");
		String resource = MachineHelpers.getBlockString(world, pos, "resource_name");
		String soil = MachineHelpers.getBlockString(world, pos, "soil_type");
		Map<String, Integer> items = new LinkedHashMap<>();
		BlockEntity entity = world.getBlockEntity(pos);
		if (entity instanceof Container container) {
			for (int slot = 0; slot < container.getContainerSize(); slot++) {
				ItemStack stack = container.getItem(slot);
				if (!stack.isEmpty()) {
					items.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
				}
			}
		}
		return new GeneratorInfo(BuiltInRegistries.BLOCK.getKey(block).toString(), ScriptHooks.kindName(kind), tier, ScriptHooks.tierName(tier), resource.isEmpty() ? "empty" : resource,
				soil.isEmpty() ? "empty" : soil, duration > 0 ? Math.min(1.0, timer / duration) : 0.0, duration, MachineHelpers.getBlockString(world, pos, "output_direction"), items);
	}
}
