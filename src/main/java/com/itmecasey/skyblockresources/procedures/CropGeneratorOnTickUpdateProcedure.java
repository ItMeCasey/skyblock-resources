package com.itmecasey.skyblockresources.procedures;

import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;


import com.itmecasey.skyblockresources.drops.NaturalDrops;
import static com.itmecasey.skyblockresources.machine.MachineHelpers.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class CropGeneratorOnTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, double maxTicks, double maxTransfer) {
		double ParticleTimer = 0;
		double Remainder = 0;
		double NumberToOutput = 0;
		double FixedDuration = 0;
		ItemStack OutputItemstack = ItemStack.EMPTY;
		if (!((new Object() {
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
				return _cur.getStringOr(_parts[_parts.length - 1].trim(), "");
			}
		}.getValue(world, BlockPos.containing(x, y, z), "resource_name")).equals("empty")) && !isInventoryFull(world, BlockPos.containing(x, y, z))) {
			if (new Object() {
				public double getValue(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos pos, String path) {
					net.minecraft.world.level.block.entity.BlockEntity be = world.getBlockEntity(pos);
					if (be == null)
						return 0.0;
					net.minecraft.nbt.CompoundTag _cur = be.getPersistentData();
					String[] _parts = path.split("[./]");
					for (int _i = 0; _i < _parts.length - 1; _i++) {
						String _p = _parts[_i].trim();
						if (_p.isEmpty())
							continue;
						if (_cur.get(_p) instanceof net.minecraft.nbt.CompoundTag _sub) {
							_cur = _sub;
						} else {
							return 0.0;
						}
					}
					return _cur.getDoubleOr(_parts[_parts.length - 1].trim(), 0.0);
				}
			}.getValue(world, BlockPos.containing(x, y, z), "timer") < new Object() {
				public double getValue(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos pos, String path) {
					net.minecraft.world.level.block.entity.BlockEntity be = world.getBlockEntity(pos);
					if (be == null)
						return 0.0;
					net.minecraft.nbt.CompoundTag _cur = be.getPersistentData();
					String[] _parts = path.split("[./]");
					for (int _i = 0; _i < _parts.length - 1; _i++) {
						String _p = _parts[_i].trim();
						if (_p.isEmpty())
							continue;
						if (_cur.get(_p) instanceof net.minecraft.nbt.CompoundTag _sub) {
							_cur = _sub;
						} else {
							return 0.0;
						}
					}
					return _cur.getDoubleOr(_parts[_parts.length - 1].trim(), 0.0);
				}
			}.getValue(world, BlockPos.containing(x, y, z), "duration")) {
				if (!world.isClientSide()) {
					net.minecraft.core.BlockPos _bp = net.minecraft.core.BlockPos.containing((double) x, (double) y, (double) z);
					net.minecraft.world.level.block.entity.BlockEntity _be = world.getBlockEntity(_bp);
					net.minecraft.world.level.block.state.BlockState _bs = world.getBlockState(_bp);
					if (_be != null) {
						net.minecraft.nbt.CompoundTag _cur = _be.getPersistentData();
						String[] _parts = ("timer").split("[./]");
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
						String _key = _parts[_parts.length - 1].trim();
						double _existing = _cur.getDoubleOr(_key, 0.0);
						_cur.putDouble(_key, _existing + 1.0);
						if (world instanceof net.minecraft.world.level.Level _level) {
							_level.sendBlockUpdated(_bp, _bs, _bs, 3);
						}
					}
				}
			} else {
				if (!world.isClientSide()) {
					net.minecraft.core.BlockPos _bp = BlockPos.containing(x, y, z);
					net.minecraft.world.level.block.entity.BlockEntity _blockEntity = world.getBlockEntity(_bp);
					net.minecraft.world.level.block.state.BlockState _bs = world.getBlockState(_bp);
					if (_blockEntity != null) {
						{
							net.minecraft.nbt.CompoundTag _cur = _blockEntity.getPersistentData();
							String[] _parts = ("timer").split("[./]");
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
				if (world instanceof ServerLevel serverLevel) {
					BlockPos pos = BlockPos.containing(x, y, z);
					for (ItemStack drop : NaturalDrops.forCrop(serverLevel, resourceItem(world, x, y, z), pos)) {
						insertItemIntoSide(world, pos, -1, drop.getItem(), (int) (drop.getCount() * growthMultiplier(world, x, y, z)));
					}
					Item seedItem = resourceItem(world, x, y, z);
					double extraChance = extraSeedChance(world, BlockPos.containing(x, y, z), new ItemStack(seedItem));
					if (extraChance > 0 && Mth.nextDouble(RandomSource.create(), 0, 1) < extraChance) {
						insertItemIntoSide(world, BlockPos.containing(x, y, z), -1, seedItem, 1);
					}
				}
			}
		}
		if (hasStock(world, BlockPos.containing(x, y, z))) {
		if (new Object() {
			public double getValue(net.minecraft.world.level.LevelAccessor world, net.minecraft.core.BlockPos pos, String path) {
				net.minecraft.world.level.block.entity.BlockEntity be = world.getBlockEntity(pos);
				if (be == null)
					return 0.0;
				net.minecraft.nbt.CompoundTag _cur = be.getPersistentData();
				String[] _parts = path.split("[./]");
				for (int _i = 0; _i < _parts.length - 1; _i++) {
					String _p = _parts[_i].trim();
					if (_p.isEmpty())
						continue;
					if (_cur.get(_p) instanceof net.minecraft.nbt.CompoundTag _sub) {
						_cur = _sub;
					} else {
						return 0.0;
					}
				}
				return _cur.getDoubleOr(_parts[_parts.length - 1].trim(), 0.0);
			}
		}.getValue(world, BlockPos.containing(x, y, z), "output_timer") < maxTicks) {
			if (!world.isClientSide()) {
				net.minecraft.core.BlockPos _bp = net.minecraft.core.BlockPos.containing((double) x, (double) y, (double) z);
				net.minecraft.world.level.block.entity.BlockEntity _be = world.getBlockEntity(_bp);
				net.minecraft.world.level.block.state.BlockState _bs = world.getBlockState(_bp);
				if (_be != null) {
					net.minecraft.nbt.CompoundTag _cur = _be.getPersistentData();
					String[] _parts = ("output_timer").split("[./]");
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
					String _key = _parts[_parts.length - 1].trim();
					double _existing = _cur.getDoubleOr(_key, 0.0);
					_cur.putDouble(_key, _existing + 1.0);
					if (world instanceof net.minecraft.world.level.Level _level) {
						_level.sendBlockUpdated(_bp, _bs, _bs, 3);
					}
				}
			}
		} else {
			if (!world.isClientSide()) {
				net.minecraft.core.BlockPos _bp = BlockPos.containing(x, y, z);
				net.minecraft.world.level.block.entity.BlockEntity _blockEntity = world.getBlockEntity(_bp);
				net.minecraft.world.level.block.state.BlockState _bs = world.getBlockState(_bp);
				if (_blockEntity != null) {
					{
						net.minecraft.nbt.CompoundTag _cur = _blockEntity.getPersistentData();
						String[] _parts = ("output_timer").split("[./]");
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
			pushItemToSide(world, BlockPos.containing(x, y, z), TextToDirectionProcedure.execute(new Object() {
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
					return _cur.getStringOr(_parts[_parts.length - 1].trim(), "");
				}
			}.getValue(world, BlockPos.containing(x, y, z), "output_direction")), -1, (int) maxTransfer);
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
		if (!isItemOutputAllowed(level, pos, dir))
			return 0;
		if (!(level instanceof ILevelExtension ext && level instanceof Level world))
			return 0;
		if (ext.getCapability(Capabilities.Item.BLOCK, pos, dir) instanceof ResourceHandler<ItemResource> src && ext.getCapability(Capabilities.Item.BLOCK, pos.relative(dir), dir.getOpposite()) instanceof ResourceHandler<ItemResource> dst) {
			int remainingAmount = maxAmount;
			int totalTransferred = 0;
			try (var tx = Transaction.openRoot()) {
				if (slot >= 0 && slot < src.size()) {
					var res = src.getResource(slot);
					if (!res.isEmpty()) {
						int extracted = (int) src.extract(slot, res, remainingAmount, tx);
						if (extracted > 0) {
							int inserted = (int) dst.insert(res, extracted, tx);
							if (inserted > 0) {
								tx.commit();
								return inserted;
							}
						}
					}
				} else if (slot < 0) {
					for (int s = 0; s < src.size() && remainingAmount > 0; s++) {
						var res = src.getResource(s);
						if (res.isEmpty())
							continue;
						int extracted = (int) src.extract(s, res, remainingAmount, tx);
						if (extracted > 0) {
							int inserted = (int) dst.insert(res, extracted, tx);
							if (inserted > 0) {
								totalTransferred += inserted;
								remainingAmount -= inserted;
							}
						}
					}
					if (totalTransferred > 0) {
						tx.commit();
						return totalTransferred;
					}
				}
			}
		}
		return 0;
	}

	// Inventory query helpers. These inspect the LOCAL block's own item handler
	// (i.e. the block at `pos`, not a neighbor). Transfer API (ResourceHandler).
	private static boolean isInventoryFull(LevelAccessor level, BlockPos pos) {
		// A slot is 'full' when its amount equals the per-slot limit for whatever
		// resource is currently held there. An empty slot is NEVER full — it can
		// always accept at least one item of the appropriate type. The previous
		// implementation skipped empty slots when computing 'max', which made a
		// single 64-cobblestone slot in a 3-slot inventory report 'full' even
		// though 2 slots were empty. Per-slot max respects item-specific stack
		// sizes (e.g. ender pearls max at 16, swords at 1, etc.).
		if (!(level instanceof ILevelExtension ext))
			return false;
		if (ext.getCapability(Capabilities.Item.BLOCK, pos, null) instanceof ResourceHandler<ItemResource> inv) {
			if (inv.size() == 0)
				return false;
			for (int s = 0; s < inv.size(); s++) {
				var res = inv.getResource(s);
				if (res.isEmpty())
					return false; // empty slot — can always accept more
				int amount = inv.getAmountAsInt(s);
				int cap = inv.getCapacityAsInt(s, res);
				if (amount < cap)
					return false; // not at capacity for its current resource
			}
			return true; // every slot at its per-resource capacity
		}
		return false;
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