package com.itmecasey.skyblockresources.machine;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.registries.DeferredBlock;

import com.itmecasey.skyblockresources.init.SkyblockResourcesModBlocks;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;
import com.itmecasey.skyblockresources.recipe.CropDropsRecipe;
import com.itmecasey.skyblockresources.recipe.RecipeUtils;
import com.itmecasey.skyblockresources.recipe.ResourceGeneratorRecipe;
import com.itmecasey.skyblockresources.recipe.TreeDropsRecipe;

/**
 * The three generator families (resource, tree, crop), each ordered wooden to netherite,
 * and the in-place tier upgrade used by the upgrade cards.
 */
public final class GeneratorTiers {
	public enum Result {
		UPGRADED, NOT_GENERATOR, WRONG_TIER, NO_ROOM
	}

	private static final List<List<DeferredBlock<Block>>> FAMILIES = List.of(
			List.of(SkyblockResourcesModBlocks.WOODEN_GENERATOR, SkyblockResourcesModBlocks.STONE_GENERATOR, SkyblockResourcesModBlocks.IRON_GENERATOR, SkyblockResourcesModBlocks.DIAMOND_GENERATOR,
					SkyblockResourcesModBlocks.NETHERITE_GENERATOR),
			List.of(SkyblockResourcesModBlocks.WOODEN_TREE_GENERATOR, SkyblockResourcesModBlocks.STONE_TREE_GENERATOR, SkyblockResourcesModBlocks.IRON_TREE_GENERATOR,
					SkyblockResourcesModBlocks.DIAMOND_TREE_GENERATOR, SkyblockResourcesModBlocks.NETHERITE_TREE_GENERATOR),
			List.of(SkyblockResourcesModBlocks.WOODEN_CROP_GENERATOR, SkyblockResourcesModBlocks.STONE_CROP_GENERATOR, SkyblockResourcesModBlocks.IRON_CROP_GENERATOR,
					SkyblockResourcesModBlocks.DIAMOND_CROP_GENERATOR, SkyblockResourcesModBlocks.NETHERITE_CROP_GENERATOR));

	private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

	private GeneratorTiers() {
	}

	/** 0 resource, 1 tree, 2 crop, or -1 when the block is not a generator. */
	public static int kindOf(Block block) {
		for (int kind = 0; kind < FAMILIES.size(); kind++)
			for (DeferredBlock<Block> candidate : FAMILIES.get(kind))
				if (candidate.get() == block)
					return kind;
		return -1;
	}

	/** Tier 0 (wooden) to 4 (netherite), or -1 when the block is not a generator. */
	public static int tierOf(Block block) {
		for (List<DeferredBlock<Block>> family : FAMILIES)
			for (int tier = 0; tier < family.size(); tier++)
				if (family.get(tier).get() == block)
					return tier;
		return -1;
	}

	/** Upgrades the generator at pos by exactly one tier, keeping its contents, facing, and settings. */
	public static Result upgrade(Level level, BlockPos pos, int cardTier) {
		BlockState old = level.getBlockState(pos);
		List<DeferredBlock<Block>> family = FAMILIES.stream().filter(f -> f.stream().anyMatch(b -> b.get() == old.getBlock())).findFirst().orElse(null);
		if (family == null)
			return Result.NOT_GENERATOR;

		int tier = tierOf(old.getBlock());
		if (tier != cardTier - 1)
			return Result.WRONG_TIER;

		Block next = family.get(tier + 1).get();
		BlockState state = next.defaultBlockState();
		for (Property<?> property : old.getProperties())
			state = copy(old, state, property);

		BlockEntity oldEntity = level.getBlockEntity(pos);
		if (oldEntity instanceof Container container && next instanceof EntityBlock entityBlock && entityBlock.newBlockEntity(pos, state) instanceof Container probe) {
			for (int slot = probe.getContainerSize(); slot < container.getContainerSize(); slot++)
				if (!container.getItem(slot).isEmpty())
					return Result.NO_ROOM;
		}

		CompoundTag saved = oldEntity == null ? null : oldEntity.saveWithFullMetadata(level.registryAccess());

		LOGGER.info("Upgrading {} at {}: data before = {}", old.getBlock(), pos, oldEntity == null ? null : oldEntity.getPersistentData());

		// Dirty replace: skip onPlace (it resets the machine's timers and resource) and the old block's removal side effects.
		level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_ON_PLACE | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS);
		BlockEntity newEntity = level.getBlockEntity(pos);
		if (newEntity != null && saved != null) {
			newEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
			newEntity.setChanged();
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
		}
		if (newEntity != null)
			refreshTierStats(level, pos, newEntity, family, tier + 1, next);
		LOGGER.info("Upgraded to {}: data after = {}", next, newEntity == null ? null : newEntity.getPersistentData());
		// onPlace also starts the block's tick loop, so do that here.
		level.scheduleTick(pos, next, 1);
		level.updateNeighborsAt(pos, next);
		com.itmecasey.skyblockresources.script.ScriptHooks.upgraded(level, pos);
		return Result.UPGRADED;
	}

	/** Duration (and output count for resource generators) come from the tier's own recipe, so recompute them for the new tier. */
	private static void refreshTierStats(Level level, BlockPos pos, BlockEntity entity, List<DeferredBlock<Block>> family, int newTier, Block next) {
		CompoundTag data = entity.getPersistentData();
		ItemStack generator = new ItemStack(next);
		int kind = FAMILIES.indexOf(family);
		if (kind == 0) {
			ItemStack resource = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(data.getStringOr("resource_name", "minecraft:air"))));
			for (ResourceGeneratorRecipe recipe : RecipeUtils.getRecipes(level, SkyblockResourcesModRecipeTypes.RESOURCEGENERATOR_TYPE.get())) {
				if (RecipeUtils.validate(generator, recipe.generatorItemInput()) && RecipeUtils.validate(resource, recipe.resourceItemInput())) {
					data.putDouble("duration", recipe.getDoubleResult("duration"));
					data.putDouble("count", recipe.getDoubleResult("output_min_count") != recipe.getDoubleResult("output_max_count") ? 0.0d
							: (recipe.getItemStackResult("output").getCount() > 0 ? (double) recipe.getItemStackResult("output").getCount() : recipe.getDoubleResult("output")));
					break;
				}
			}
		} else {
			ItemStack plant = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(data.getStringOr("resource_name", "minecraft:air"))));
			ItemStack soil = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(data.getStringOr("soil_type", "minecraft:air"))));
			double speed = data.getDoubleOr("speed_bonus", 1.0d);
			if (kind == 1) {
				for (TreeDropsRecipe recipe : RecipeUtils.getRecipes(level, SkyblockResourcesModRecipeTypes.TREEDROPS_TYPE.get())) {
					if ((RecipeUtils.validate(plant, recipe.saplingItemInput()) || RecipeUtils.isSapling(plant)) && RecipeUtils.validate(soil, recipe.soilItemInput()) && RecipeUtils.validate(generator, recipe.generatorItemInput())) {
						data.putDouble("duration", (int) Math.floor(recipe.getDoubleResult("duration") * speed));
						break;
					}
				}
			} else {
				for (CropDropsRecipe recipe : RecipeUtils.getRecipes(level, SkyblockResourcesModRecipeTypes.CROPDROPS_TYPE.get())) {
					if ((RecipeUtils.validate(plant, recipe.seedItemInput()) || RecipeUtils.isCropOrSeed(plant)) && RecipeUtils.validate(soil, recipe.soilItemInput()) && RecipeUtils.validate(generator, recipe.generatorItemInput())) {
						data.putDouble("duration", (int) Math.floor(recipe.getDoubleResult("duration") * speed));
						break;
					}
				}
			}
		}
		// A timer past the shorter duration would otherwise wait for a full cycle of the new, shorter length.
		if (data.getDoubleOr("timer", 0) > data.getDoubleOr("duration", 0))
			data.putDouble("timer", 0);
		entity.setChanged();
		level.sendBlockUpdated(pos, entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
	}

	private static <T extends Comparable<T>> BlockState copy(BlockState from, BlockState to, Property<T> property) {
		return to.hasProperty(property) ? to.setValue(property, from.getValue(property)) : to;
	}
}
