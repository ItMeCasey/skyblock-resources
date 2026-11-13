package com.itmecasey.skyblockresources.gametest;

import com.itmecasey.skyblockresources.compat.NbtCompat;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import com.itmecasey.skyblockresources.drops.RecipeDrops;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlocks;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModItems;
import com.itmecasey.skyblockresources.machine.GeneratorTiers;

/**
 * Headless checks for drops and generator upgrades. Only runs when started with
 * -Dskyblock.selftest=true, then stops the server.
 */
@EventBusSubscriber
public final class HeadlessSelfTest {
	private static final Logger LOGGER = LogManager.getLogger();

	private HeadlessSelfTest() {
	}

	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		if (!Boolean.getBoolean("skyblock.selftest"))
			return;

		MinecraftServer server = event.getServer();
		ServerLevel level = server.overworld();
		BlockPos origin = BlockPos.ZERO;

		List<ItemStack> tree = listed(level, Items.OAK_SAPLING, true);
		List<ItemStack> crop = listed(level, Items.WHEAT_SEEDS, false);

		boolean passed = check("oak sapling drops oak logs", contains(tree, Items.OAK_LOG), tree)
				& check("wheat seeds drop wheat", contains(crop, Items.WHEAT), crop);

		passed &= checkUpgrade(level, SkyblockResourcesModBlocks.WOODEN_GENERATOR.get(), SkyblockResourcesModBlocks.STONE_GENERATOR.get());
		passed &= checkUpgrade(level, SkyblockResourcesModBlocks.STONE_TREE_GENERATOR.get(), SkyblockResourcesModBlocks.IRON_TREE_GENERATOR.get());
		passed &= checkUpgrade(level, SkyblockResourcesModBlocks.DIAMOND_CROP_GENERATOR.get(), SkyblockResourcesModBlocks.NETHERITE_CROP_GENERATOR.get());

		passed &= checkCardUse(server, level, SkyblockResourcesModBlocks.WOODEN_GENERATOR.get(), SkyblockResourcesModItems.STONE_UPGRADE.get());
		passed &= checkCardUse(server, level, SkyblockResourcesModBlocks.STONE_TREE_GENERATOR.get(), SkyblockResourcesModItems.IRON_UPGRADE.get());
		passed &= checkCardUse(server, level, SkyblockResourcesModBlocks.DIAMOND_CROP_GENERATOR.get(), SkyblockResourcesModItems.NETHERITE_UPGRADE.get());

		List<ItemStack> fruit = listed(level, net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("croptopia:apple_sapling")), true);
		List<ItemStack> mystical = listed(level, net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("mysticalagriculture:inferium_seeds")), false);
		passed &= check("croptopia apple sapling drops apples", contains(fruit, Items.APPLE), fruit);
		passed &= check("inferium seeds drop something", !mystical.isEmpty(), mystical);
		for (String produce : new String[] { "minecraft:wheat_seeds", "minecraft:beetroot_seeds", "minecraft:melon_seeds", "minecraft:sweet_berries", "minecraft:glow_berries", "croptopia:coffee_seed", "croptopia:tea_seed", "croptopia:artichoke_seed", "minecraft:seagrass", "minecraft:tube_coral", "minecraft:brain_coral", "minecraft:bubble_coral", "minecraft:fire_coral", "minecraft:horn_coral", "minecraft:chorus_flower" }) {
			List<ItemStack> grown = listed(level, net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(produce)), false);
			passed &= check(produce + " drops something", !grown.isEmpty(), grown);
		}

		passed &= checkDropRecipes(level);

		passed &= checkScripting(level);

		LOGGER.info("SELFTEST {}", passed ? "PASSED" : "FAILED");
		server.halt(false);
	}

	private static boolean checkUpgrade(ServerLevel level, Block from, Block to) {
		BlockPos pos = new BlockPos(0, 100, 0);
		level.setBlock(pos, from.defaultBlockState(), 3);
		((Container) level.getBlockEntity(pos)).setItem(0, new ItemStack(Items.DIAMOND, 5));
		level.getBlockEntity(pos).getPersistentData().putString("output_direction", "UP");
		level.getBlockEntity(pos).getPersistentData().putString("resource_name", "minecraft:dirt");
		level.getBlockEntity(pos).getPersistentData().putDouble("count", 7);
		level.getBlockEntity(pos).getPersistentData().putDouble("timer", 5);
		level.getBlockEntity(pos).getPersistentData().put("BlockDirections", new net.minecraft.nbt.CompoundTag());
		NbtCompat.getCompoundOrEmpty(level.getBlockEntity(pos).getPersistentData(), "BlockDirections").putBoolean("item_out_UP", true);

		GeneratorTiers.Result wrong = GeneratorTiers.upgrade(level, pos, GeneratorTiers.tierOf(from) + 2);
		GeneratorTiers.Result result = GeneratorTiers.upgrade(level, pos, GeneratorTiers.tierOf(to));
		BlockEntity entity = level.getBlockEntity(pos);
		boolean ok = wrong == GeneratorTiers.Result.WRONG_TIER && result == GeneratorTiers.Result.UPGRADED && level.getBlockState(pos).getBlock() == to && entity instanceof Container container
				&& container.getItem(0).is(Items.DIAMOND) && container.getItem(0).getCount() == 5 && "UP".equals(NbtCompat.getStringOr(entity.getPersistentData(), "output_direction", ""));
		var data = entity == null ? new net.minecraft.nbt.CompoundTag() : entity.getPersistentData();
		var update = entity == null ? new net.minecraft.nbt.CompoundTag() : entity.getUpdateTag(level.registryAccess());
		LOGGER.info("SELFTEST persistent after upgrade: {}", data);
		LOGGER.info("SELFTEST update tag: {}", update);
		ok &= "minecraft:dirt".equals(NbtCompat.getStringOr(data, "resource_name", "")) && data.contains("count") && NbtCompat.getBooleanOr(NbtCompat.getCompoundOrEmpty(data, "BlockDirections"), "item_out_UP", false);
		LOGGER.info("SELFTEST upgrade {} -> {}: {}", from, to, ok ? "ok" : "FAIL");
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		return ok;
	}

	/** The ModJS-facing API without ModJS: events reach listeners (a failing one is skipped), info reads a generator, recipe JSON is valid. */
	private static boolean checkScripting(ServerLevel level) {
		int[] seen = { 0, 0 };
		String[] produced = { "" };
		java.util.function.Consumer<com.itmecasey.skyblockresources.script.GeneratorEvent> bad = e -> {
			throw new IllegalStateException("script bug on purpose");
		};
		java.util.function.Consumer<com.itmecasey.skyblockresources.script.GeneratorEvent> up = e -> {
			if (e.tier == 1 && e.kind.equals("resource") && e.tierName.equals("stone"))
				seen[0]++;
		};
		java.util.function.Consumer<com.itmecasey.skyblockresources.script.GeneratorEvent> made = e -> {
			seen[1] += e.count;
			produced[0] = e.item + " " + e.dimension;
		};
		com.itmecasey.skyblockresources.script.ScriptHooks.on("upgraded", bad);
		com.itmecasey.skyblockresources.script.ScriptHooks.on("upgraded", up);
		com.itmecasey.skyblockresources.script.ScriptHooks.on("produced", made);
		BlockPos pos = new BlockPos(0, 100, 0);
		level.setBlock(pos, SkyblockResourcesModBlocks.WOODEN_GENERATOR.get().defaultBlockState(), 3);
		var api = new com.itmecasey.skyblockresources.script.SkyblockResourcesApi();
		int[] viaApi = { 0 };
		api.on("produced", e -> viaApi[0]++);
		var before = api.info(level, 0, 100, 0);
		GeneratorTiers.upgrade(level, pos, 1);
		var after = api.info(level, 0, 100, 0);
		com.itmecasey.skyblockresources.script.ScriptHooks.produced(level, pos, Items.DIAMOND, 3);
		boolean none = api.info(level, 5, 5, 5) == null && !api.isGenerator(level, 5, 5, 5);
		com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(api.recipes.cropDrops("minecraft:wheat_seeds", "minecraft:dirt", "skyblock_resources:wooden_crop_generator", "minecraft:wheat", 1,
				java.util.List.of(java.util.Map.of("id", "minecraft:wheat", "min", 1, "max", 2, "chance", 0.5)), 1000)).getAsJsonObject();
		boolean recipe = "skyblock_resources:crop_drops".equals(json.get("type").getAsString()) && json.getAsJsonArray("drops").size() == 1 && json.get("duration").getAsDouble() == 1000
				&& json.getAsJsonArray("drops").get(0).getAsJsonObject().get("chance").getAsDouble() == 0.5;
		api.clear();
		com.itmecasey.skyblockresources.script.ScriptHooks.remove("upgraded", bad);
		com.itmecasey.skyblockresources.script.ScriptHooks.remove("upgraded", up);
		com.itmecasey.skyblockresources.script.ScriptHooks.remove("produced", made);
		com.itmecasey.skyblockresources.script.ScriptHooks.produced(level, pos, Items.DIAMOND, 1);
		boolean bridge = viaApi[0] == 1;
		boolean ok = before != null && before.tier == 0 && before.kind.equals("resource") && before.tierName.equals("wooden") && after != null && after.tier == 1 && seen[0] == 1 && seen[1] == 3
				&& produced[0].equals("minecraft:diamond minecraft:overworld") && none && recipe && bridge;
		LOGGER.info("SELFTEST scripting: upgraded events {}, produced {} ({}), no generator elsewhere {}, recipe json {}, listeners cleared {}: {}", seen[0], seen[1], produced[0], none, recipe, bridge, ok ? "ok" : "FAIL");
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		return ok;
	}

	/** Every tree and crop recipe must list at least one real drop, so a bad generated or hand-edited file fails here. */
	private static boolean checkDropRecipes(ServerLevel level) {
		var trees = com.itmecasey.skyblockresources.recipe.RecipeUtils.getRecipes(level, com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes.TREEDROPS_TYPE.get());
		var crops = com.itmecasey.skyblockresources.recipe.RecipeUtils.getRecipes(level, com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes.CROPDROPS_TYPE.get());
		long emptyTrees = trees.stream().filter(r -> r.drops().isEmpty()).count();
		long emptyCrops = crops.stream().filter(r -> r.drops().isEmpty()).count();
		var resources = com.itmecasey.skyblockresources.recipe.RecipeUtils.getRecipes(level, com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes.RESOURCEGENERATOR_TYPE.get());
		boolean sandySoil = resources.stream().anyMatch(r -> r.getItemStackResult("output").is(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("natures_spirit:sandy_soil"))));
		LOGGER.info("SELFTEST resource recipes: {}, natures_spirit:sandy_soil present: {}", resources.size(), sandySoil);
		boolean ok = !trees.isEmpty() && !crops.isEmpty() && emptyTrees == 0 && emptyCrops == 0 && sandySoil
				&& trees.stream().noneMatch(r -> r.drops().stream().anyMatch(e -> e.item() == Items.AIR || e.maxCount() <= 0))
				&& crops.stream().noneMatch(r -> r.drops().stream().anyMatch(e -> e.item() == Items.AIR || e.maxCount() <= 0));
		LOGGER.info("SELFTEST drop recipes: {} tree, {} crop, {} + {} without drops: {}", trees.size(), crops.size(), emptyTrees, emptyCrops, ok ? "ok" : "FAIL");
		return ok;
	}

	/** Uses a real upgrade card through ItemStack.useOn (NeoForge's block snapshot path) and checks the machine data survives. */
	private static boolean checkCardUse(MinecraftServer server, ServerLevel level, Block from, Item card) {
		BlockPos pos = new BlockPos(0, 100, 0);
		level.setBlock(pos, from.defaultBlockState(), 3);
		var data = level.getBlockEntity(pos).getPersistentData();
		data.putString("resource_name", "minecraft:dirt");
		data.putString("soil_type", "minecraft:dirt");
		data.putDouble("timer", 5);
		data.putDouble("duration", 999);
		if (from == SkyblockResourcesModBlocks.STONE_TREE_GENERATOR.get())
			data.putString("resource_name", "minecraft:oak_sapling");
		if (from == SkyblockResourcesModBlocks.DIAMOND_CROP_GENERATOR.get())
			data.putString("resource_name", "minecraft:wheat_seeds");
		var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);
		player.setShiftKeyDown(true);
		player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
		ItemStack stack = new ItemStack(card);
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
		var hit = new net.minecraft.world.phys.BlockHitResult(pos.getCenter(), net.minecraft.core.Direction.UP, pos, false);
		// Run inside a server task like a real packet, so the card's deferred work is queued instead of running inline.
		boolean[] done = { false };
		server.execute(() -> {
			LOGGER.info("SELFTEST card useOn result: {}", stack.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit)));
			server.execute(() -> done[0] = true);
		});
		server.managedBlock(() -> done[0]);
		// A stray onPlace on the upgraded block (NeoForge re-fires it in some paths) must not wipe the machine either.
		level.getBlockState(pos).onPlace(level, pos, from.defaultBlockState(), false);
		BlockEntity entity = level.getBlockEntity(pos);
		var after = entity == null ? new net.minecraft.nbt.CompoundTag() : entity.getPersistentData();
		boolean ok = level.getBlockState(pos).getBlock() != from && NbtCompat.getStringOr(after, "resource_name", "").startsWith("minecraft:") && NbtCompat.getDoubleOr(after, "timer", 0) == 5;
		if (from != SkyblockResourcesModBlocks.WOODEN_GENERATOR.get())
			LOGGER.info("SELFTEST tree/crop duration after upgrade: {}", NbtCompat.getDoubleOr(after, "duration", -1));
		if (from == SkyblockResourcesModBlocks.WOODEN_GENERATOR.get())
			ok &= NbtCompat.getDoubleOr(after, "duration", 999) != 999; // taken from the stone tier's dirt recipe
		LOGGER.info("SELFTEST card use {} -> {}: {} {}", from, level.getBlockState(pos).getBlock(), ok ? "ok" : "FAIL", after);
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		return ok;
	}

	/** What a plant's recipe lists, without the chance rolls, so the checks do not flake. */
	private static List<ItemStack> listed(ServerLevel level, Item plant, boolean tree) {
		return RecipeDrops.entries(level, plant, tree).stream().map(entry -> new ItemStack(entry.item())).toList();
	}

	private static boolean check(String name, boolean ok, List<ItemStack> drops) {
		LOGGER.info("SELFTEST {}: {} {}", name, ok ? "ok" : "FAIL", drops);
		return ok;
	}

	private static boolean contains(List<ItemStack> drops, Item item) {
		return drops.stream().anyMatch(stack -> stack.is(item));
	}
}
