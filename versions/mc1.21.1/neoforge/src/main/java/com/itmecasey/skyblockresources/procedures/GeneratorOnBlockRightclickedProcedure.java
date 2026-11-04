package com.itmecasey.skyblockresources.procedures;

import com.itmecasey.skyblockresources.compat.NbtCompat;

import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.items.IItemHandler;
import com.itmecasey.skyblockresources.compat.Transfer;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.ItemTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import com.itmecasey.skyblockresources.recipe.ResourceGeneratorRecipe;
import com.itmecasey.skyblockresources.recipe.RecipeUtils;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;

import static com.itmecasey.skyblockresources.machine.MachineHelpers.*;

public class GeneratorOnBlockRightclickedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Direction direction, Entity entity) {
		if (direction == null || entity == null)
			return;
		ItemStack ItemType = ItemStack.EMPTY;
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).is(ItemTags.create(ResourceLocation.parse("c:wrench"))) && ClientKeys.controlDown()) {
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal((Component.translatable("skyblock_resources.output_direction_changed").getString() + "\u00A72" + ("" + direction).toUpperCase() + "\u00A7r")), true);
			if (!world.isClientSide()) {
				net.minecraft.core.BlockPos _bp = BlockPos.containing(x, y, z);
				net.minecraft.world.level.block.entity.BlockEntity _blockEntity = world.getBlockEntity(_bp);
				net.minecraft.world.level.block.state.BlockState _bs = world.getBlockState(_bp);
				if (_blockEntity != null) {
					{
						net.minecraft.nbt.CompoundTag _cur = _blockEntity.getPersistentData();
						String[] _parts = ("output_direction").split("[./]");
						for (int _i = 0; _i < _parts.length - 1; _i++) {
							String _p = _parts[_i].trim();
							if (_p.isEmpty())
								continue;
							if (_cur.get(_p) instanceof net.minecraft.nbt.CompoundTag _sub) {
								_cur = _sub;
							} else {
								net.minecraft.nbt.CompoundTag _next = new net.minecraft.nbt.CompoundTag();
								_cur.put(_p, _next);
								_cur = _next;
							}
						}
						_cur.putString(_parts[_parts.length - 1].trim(), ("" + direction).toUpperCase());
					}
				}
				if (world instanceof net.minecraft.world.level.Level _level)
					_level.sendBlockUpdated(_bp, _bs, _bs, 3);
			}
			setSideMode(world, BlockPos.containing(x, y, z), /* BlockDirections ANY-direction sentinel. When a procedure block that
																accepts a direction sees this null Direction, it loops over all 6
																sides. See block_directions_helper.java.ftl for the consumer logic. */
					((Direction) null), "ITEM", "DENY_ALL");
			setSideMode(world, BlockPos.containing(x, y, z), direction, "ITEM", "OUTPUT_ONLY");
		} else if ((new Object() {
			public String getValue(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos pos, String path) {
				net.minecraft.world.level.block.entity.BlockEntity be = world.getBlockEntity(pos);
				if (be == null)
					return "";
				net.minecraft.nbt.CompoundTag _cur = be.getPersistentData();
				String[] _parts = path.split("[./]");
				for (int _i = 0; _i < _parts.length - 1; _i++) {
					String _p = _parts[_i].trim();
					if (_p.isEmpty())
						continue;
					if (_cur.get(_p) instanceof net.minecraft.nbt.CompoundTag _sub) {
						_cur = _sub;
					} else {
						return "";
					}
				}
				return NbtCompat.getStringOr(_cur, _parts[_parts.length - 1].trim(), "");
			}
		}.getValue(world, BlockPos.containing(x, y, z), "resource_name")).equals("empty")) {
			if (new Object() {
				boolean isRecipeValid(ItemStack generatorItemInput, ItemStack resourceItemInput) {
					boolean recipeValid = false;
					boolean recipeInputNotConsumed = true;
					for (ResourceGeneratorRecipe recipe : RecipeUtils.getRecipes(world, SkyblockResourcesModRecipeTypes.RESOURCEGENERATOR_TYPE.get())) {
						boolean _itemMatch = true && RecipeUtils.validate(generatorItemInput, recipe.generatorItemInput()) && RecipeUtils.validate(resourceItemInput, recipe.resourceItemInput());
						boolean _fluidMatch = true;
						boolean _logicMatch = true;
						boolean _numberMatch = true;
						boolean _textMatch = true;
						if (_itemMatch && _fluidMatch && _logicMatch && _numberMatch && _textMatch) {
							recipeValid = true;
							if (recipeInputNotConsumed) {
								recipeInputNotConsumed = false;
							}
							break;
						}
					}
					return recipeValid;
				}
			}.isRecipeValid((new ItemStack((world.getBlockState(BlockPos.containing(x, y, z))).getBlock())), (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY))) {
				if (!world.isClientSide()) {
					net.minecraft.core.BlockPos _bp = BlockPos.containing(x, y, z);
					net.minecraft.world.level.block.entity.BlockEntity _blockEntity = world.getBlockEntity(_bp);
					net.minecraft.world.level.block.state.BlockState _bs = world.getBlockState(_bp);
					if (_blockEntity != null) {
						{
							net.minecraft.nbt.CompoundTag _cur = _blockEntity.getPersistentData();
							String[] _parts = ("count").split("[./]");
							for (int _i = 0; _i < _parts.length - 1; _i++) {
								String _p = _parts[_i].trim();
								if (_p.isEmpty())
									continue;
								if (_cur.get(_p) instanceof net.minecraft.nbt.CompoundTag _sub) {
									_cur = _sub;
								} else {
									net.minecraft.nbt.CompoundTag _next = new net.minecraft.nbt.CompoundTag();
									_cur.put(_p, _next);
									_cur = _next;
								}
							}
							_cur.putDouble(_parts[_parts.length - 1].trim(), (double) new Object() {
								double getResult(ItemStack generatorItemInput, ItemStack resourceItemInput) {
									double result = 0d;
									boolean recipeInputNotConsumed = true;
									for (ResourceGeneratorRecipe recipe : RecipeUtils.getRecipes(world, SkyblockResourcesModRecipeTypes.RESOURCEGENERATOR_TYPE.get())) {
										boolean _itemMatch = true && RecipeUtils.validate(generatorItemInput, recipe.generatorItemInput()) && RecipeUtils.validate(resourceItemInput, recipe.resourceItemInput());
										boolean _fluidMatch = true;
										boolean _logicMatch = true;
										boolean _numberMatch = true;
										boolean _textMatch = true;
										if (_itemMatch && _fluidMatch && _logicMatch && _numberMatch && _textMatch) {
											result = (recipe.getDoubleResult("output_min_count") != recipe.getDoubleResult("output_max_count"))
													? 0.0d
													: (recipe.getItemStackResult("output").getCount() > 0 ? (double) recipe.getItemStackResult("output").getCount() : recipe.getDoubleResult("output"));
											if (recipeInputNotConsumed) {
												recipeInputNotConsumed = false;
											}
											break;
										}
									}
									return result;
								}
							}.getResult((new ItemStack((world.getBlockState(BlockPos.containing(x, y, z))).getBlock())), (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)));
						}
						{
							net.minecraft.nbt.CompoundTag _cur = _blockEntity.getPersistentData();
							String[] _parts = ("duration").split("[./]");
							for (int _i = 0; _i < _parts.length - 1; _i++) {
								String _p = _parts[_i].trim();
								if (_p.isEmpty())
									continue;
								if (_cur.get(_p) instanceof net.minecraft.nbt.CompoundTag _sub) {
									_cur = _sub;
								} else {
									net.minecraft.nbt.CompoundTag _next = new net.minecraft.nbt.CompoundTag();
									_cur.put(_p, _next);
									_cur = _next;
								}
							}
							_cur.putDouble(_parts[_parts.length - 1].trim(), (double) new Object() {
								double getResult(ItemStack generatorItemInput, ItemStack resourceItemInput) {
									double result = 0d;
									boolean recipeInputNotConsumed = true;
									for (ResourceGeneratorRecipe recipe : RecipeUtils.getRecipes(world, SkyblockResourcesModRecipeTypes.RESOURCEGENERATOR_TYPE.get())) {
										boolean _itemMatch = true && RecipeUtils.validate(generatorItemInput, recipe.generatorItemInput()) && RecipeUtils.validate(resourceItemInput, recipe.resourceItemInput());
										boolean _fluidMatch = true;
										boolean _logicMatch = true;
										boolean _numberMatch = true;
										boolean _textMatch = true;
										if (_itemMatch && _fluidMatch && _logicMatch && _numberMatch && _textMatch) {
											result = recipe.getDoubleResult("duration");
											if (recipeInputNotConsumed) {
												recipeInputNotConsumed = false;
											}
											break;
										}
									}
									return result;
								}
							}.getResult((new ItemStack((world.getBlockState(BlockPos.containing(x, y, z))).getBlock())), (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)));
						}
						{
							net.minecraft.nbt.CompoundTag _cur = _blockEntity.getPersistentData();
							String[] _parts = ("resource_name").split("[./]");
							for (int _i = 0; _i < _parts.length - 1; _i++) {
								String _p = _parts[_i].trim();
								if (_p.isEmpty())
									continue;
								if (_cur.get(_p) instanceof net.minecraft.nbt.CompoundTag _sub) {
									_cur = _sub;
								} else {
									net.minecraft.nbt.CompoundTag _next = new net.minecraft.nbt.CompoundTag();
									_cur.put(_p, _next);
									_cur = _next;
								}
							}
							_cur.putString(_parts[_parts.length - 1].trim(), (BuiltInRegistries.ITEM.getKey((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()).toString()));
						}
					}
					if (world instanceof net.minecraft.world.level.Level _level)
						_level.sendBlockUpdated(_bp, _bs, _bs, 3);
				}
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.note_block.chime")), SoundSource.NEUTRAL, (float) 0.3, 1);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.note_block.chime")), SoundSource.NEUTRAL, (float) 0.3, 1, false);
					}
				}
				(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).shrink(1);
			} else {
				if (!world.isClientSide()) {
					net.minecraft.core.BlockPos _bp = BlockPos.containing(x, y, z);
					net.minecraft.world.level.block.entity.BlockEntity _blockEntity = world.getBlockEntity(_bp);
					net.minecraft.world.level.block.state.BlockState _bs = world.getBlockState(_bp);
					if (_blockEntity != null) {
						{
							net.minecraft.nbt.CompoundTag _cur = _blockEntity.getPersistentData();
							String[] _parts = ("count").split("[./]");
							for (int _i = 0; _i < _parts.length - 1; _i++) {
								String _p = _parts[_i].trim();
								if (_p.isEmpty())
									continue;
								if (_cur.get(_p) instanceof net.minecraft.nbt.CompoundTag _sub) {
									_cur = _sub;
								} else {
									net.minecraft.nbt.CompoundTag _next = new net.minecraft.nbt.CompoundTag();
									_cur.put(_p, _next);
									_cur = _next;
								}
							}
							_cur.putDouble(_parts[_parts.length - 1].trim(), (double) 0);
						}
						{
							net.minecraft.nbt.CompoundTag _cur = _blockEntity.getPersistentData();
							String[] _parts = ("duration").split("[./]");
							for (int _i = 0; _i < _parts.length - 1; _i++) {
								String _p = _parts[_i].trim();
								if (_p.isEmpty())
									continue;
								if (_cur.get(_p) instanceof net.minecraft.nbt.CompoundTag _sub) {
									_cur = _sub;
								} else {
									net.minecraft.nbt.CompoundTag _next = new net.minecraft.nbt.CompoundTag();
									_cur.put(_p, _next);
									_cur = _next;
								}
							}
							_cur.putDouble(_parts[_parts.length - 1].trim(), (double) 0);
						}
					}
					if (world instanceof net.minecraft.world.level.Level _level)
						_level.sendBlockUpdated(_bp, _bs, _bs, 3);
				}
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.villager.no")), SoundSource.NEUTRAL, (float) 0.3, 1);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.villager.no")), SoundSource.NEUTRAL, (float) 0.3, 1, false);
					}
				}
			}
		} else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Blocks.AIR.asItem()) {
			if (entity.isShiftKeyDown()) {
				if (placeIntoPlayerInventory(world, BlockPos.containing(x, y, z), -1, entity, 64) > 0) {
					if (world.isClientSide()) {
						if (world instanceof Level _level) {
							if (!_level.isClientSide()) {
								_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.item.pickup")), SoundSource.NEUTRAL, 1, 1);
							} else {
								_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.item.pickup")), SoundSource.NEUTRAL, 1, 1, false);
							}
						}
					}
				}
			} else {
				if (placeIntoPlayerInventory(world, BlockPos.containing(x, y, z), -1, entity, 1) > 0) {
					if (world.isClientSide()) {
						if (world instanceof Level _level) {
							if (!_level.isClientSide()) {
								_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.item.pickup")), SoundSource.NEUTRAL, 1, 1);
							} else {
								_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.item.pickup")), SoundSource.NEUTRAL, 1, 1, false);
							}
						}
					}
				}
			}
		}
	}

	// True if the block at `pos` actually exposes the capability for `type`.
	// ITEM / FLUID / ENERGY require a real capability. REDSTONE has no capability
	// in NeoForge (signal is queried via Level.getSignal) so we treat it as
	// always-present — the user's procedure blocks are the source of truth.
	// 26.x renamed the nested capability types: Item / Fluid / Energy instead of
	// ItemHandler / FluidHandler / EnergyStorage.
	// True if the user has called setBlockSideMode for this exact (type, dir,
	// in/out) entry. Used by the iterator to filter INPUT_ONLY / OUTPUT_ONLY /
	// DENY_ALL to only the sides the user has explicitly configured.
	// Strict per-side allow check used by the push/pull helpers and the iterator's
	// ALLOW_ALL mode. Returns true only when the block has the capability AND the
	// user has explicitly enabled this (type, dir, in/out) combination. Unconfigured
	// sides return false even if the block declares the capability.
	private static int pushItemToSide(LevelAccessor level, BlockPos pos, Direction dir, int slot, int maxAmount) {
		// null dir = "any direction" sentinel: try each of the 6 sides with the
		// full maxAmount, accumulating the transferred total.
		if (dir == null) {
			int total = 0;
			for (Direction d : Direction.values()) {
				total += pushItemToSide(level, pos, d, slot, maxAmount);
			}
			return total;
		}
		if (!isSideAllowed(level, pos, dir, "ITEM", false))
			return 0;
		if (!(level instanceof ILevelExtension ext && level instanceof Level world))
			return 0;
		IItemHandler src = Transfer.items(level, pos, dir);
		IItemHandler dst = Transfer.items(level, pos.relative(dir), dir.getOpposite());
		if (src != null && dst != null) {
			return Transfer.moveItems(src, dst, maxAmount, slot);
		}
		return 0;
	}

	// Inventory query helpers. These inspect the LOCAL block's own item handler
	// (i.e. the block at `pos`, not a neighbor). Transfer API (ResourceHandler).
	private static boolean isInventoryFull(LevelAccessor level, BlockPos pos) {
		return Transfer.isFull(level, pos);
	}

	// "Insert" helper: generate `amount` items of `template` and place them
	// into slot S of the LOCAL block's own inventory.
	// `template` is an Item; it is wrapped in a new ItemStack for the ItemResource API.
	// slot = -1 means first-fit. Returns the number of items actually placed.
	// "Place into player inventory" helper: extracts up to `amount` items from
	// the block's inventory at `pos` (slot = -1 for any/first non-empty slot),
	// places them into the player's inventory, and shrinks/removes the items
	// from the block's inventory by the count actually transferred.
	// Returns the number of items successfully placed into the player's inventory.
	// "Insert fluid" helper: inserts up to `amount` mB of `fluid` directly
	// into the block at `pos` (attempts null/internal capability first, then falls
	// back to any side). Returns the amount in mB actually inserted.
	// "Insert energy" helper: inserts up to `amount` energy directly
	// into the block at `pos` (attempts null/internal capability first, then falls
	// back to any side). Returns the amount of energy actually inserted.
}