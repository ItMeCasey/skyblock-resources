package com.itmecasey.skyblockresources.procedures;

import com.itmecasey.skyblockresources.compat.NbtCompat;

import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.items.IItemHandler;
import com.itmecasey.skyblockresources.compat.Transfer;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.Container;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

import com.itmecasey.skyblockresources.configuration.ConfigurationConfiguration;

import java.util.Comparator;
import java.util.Calendar;
import com.itmecasey.skyblockresources.drops.NaturalDrops;
import static com.itmecasey.skyblockresources.machine.MachineHelpers.*;
import net.minecraft.world.item.Item;
import net.minecraft.core.Direction;
import com.itmecasey.skyblockresources.procedures.TextToDirectionProcedure;

public class IronTreeGeneratorOnTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		double ParticleTimer = 0;
		double Remainder = 0;
		double NumberToOutput = 0;
		double SlotLooper = 0;
		double InventoryCount = 0;
		SlotLooper = 0;
		InventoryCount = 0;
		while (SlotLooper < getBlockInventorySlotCount(world, BlockPos.containing(x, y, z))) {
			InventoryCount = new Object() {
				public double changeBy(Object _base, Object _val) {
					double _v = (_val instanceof Number _n) ? _n.doubleValue() : 0.0D;
					if (_base instanceof Integer _i)
						return _i + (int) _v;
					if (_base instanceof Long _l)
						return _l + (long) _v;
					if (_base instanceof Float _f)
						return _f + (float) _v;
					if (_base instanceof Double _d)
						return _d + _v;
					if (_base instanceof Number _n)
						return _n.doubleValue() + _v;
					return 0;
				}
			}.changeBy(InventoryCount, (getBlockInventorySlotStackLimit(world, BlockPos.containing(x, y, z), (int) SlotLooper) - itemFromBlockInventory(world, BlockPos.containing(x, y, z), (int) SlotLooper).getCount()));
			SlotLooper = new Object() {
				public double change(Object _obj) {
					if (_obj instanceof Integer _i)
						return _i + 1;
					if (_obj instanceof Long _l)
						return _l + 1;
					if (_obj instanceof Float _f)
						return _f + 1.0f;
					if (_obj instanceof Double _d)
						return _d + 1.0d;
					if (_obj instanceof Number _n)
						return _n.doubleValue() + 1;
					return 0;
				}
			}.change(SlotLooper);
		}
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
				return NbtCompat.getStringOr(_cur, _parts[_parts.length - 1].trim(), "");
			}
		}.getValue(world, BlockPos.containing(x, y, z), "resource_name")).equals("empty"))) {
			if (InventoryCount > 0) {
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
						return NbtCompat.getDoubleOr(_cur, _parts[_parts.length - 1].trim(), 0.0);
					}
				}.getValue(world, BlockPos.containing(x, y, z), "timer") < getBlockNumber(world, BlockPos.containing(x, y, z), "duration")) {
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
								_cur.putDouble(_parts[_parts.length - 1].trim(), (double) (new Object() {
									public double change(Object _obj) {
										if (_obj instanceof Integer _i)
											return _i + 1;
										if (_obj instanceof Long _l)
											return _l + 1;
										if (_obj instanceof Float _f)
											return _f + 1.0f;
										if (_obj instanceof Double _d)
											return _d + 1.0d;
										if (_obj instanceof Number _n)
											return _n.doubleValue() + 1;
										return 0;
									}
								}.change((new Object() {
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
										return NbtCompat.getDoubleOr(_cur, _parts[_parts.length - 1].trim(), 0.0);
									}
								}.getValue(world, BlockPos.containing(x, y, z), "timer")))));
							}
						}
						if (world instanceof net.minecraft.world.level.Level _level)
							_level.sendBlockUpdated(_bp, _bs, _bs, 3);
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
					if (world instanceof ServerLevel _level)
						_level.sendParticles(ParticleTypes.WHITE_SMOKE, (x + 0.5), (y + 0.5), (z + 0.5), 3, (-0.3), (-0.3), (-0.3), 0);
					if (world instanceof ServerLevel serverLevel) {
						BlockPos pos = BlockPos.containing(x, y, z);
						for (ItemStack drop : NaturalDrops.forTree(serverLevel, resourceItem(world, x, y, z), pos)) {
							insertItemIntoSide(world, BlockPos.containing(x, y, z), -1, drop.getItem(), (int) (drop.getCount() * growthMultiplier(world, x, y, z)));
						}
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
				return NbtCompat.getDoubleOr(_cur, _parts[_parts.length - 1].trim(), 0.0);
			}
		}.getValue(world, BlockPos.containing(x, y, z), "output_timer") < (double) ConfigurationConfiguration.IRON_TREE_MAX_TICKS.get()) {
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
						_cur.putDouble(_parts[_parts.length - 1].trim(), (double) (new Object() {
							public double change(Object _obj) {
								if (_obj instanceof Integer _i)
									return _i + 1;
								if (_obj instanceof Long _l)
									return _l + 1;
								if (_obj instanceof Float _f)
									return _f + 1.0f;
								if (_obj instanceof Double _d)
									return _d + 1.0d;
								if (_obj instanceof Number _n)
									return _n.doubleValue() + 1;
								return 0;
							}
						}.change((new Object() {
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
								return NbtCompat.getDoubleOr(_cur, _parts[_parts.length - 1].trim(), 0.0);
							}
						}.getValue(world, BlockPos.containing(x, y, z), "output_timer")))));
					}
				}
				if (world instanceof net.minecraft.world.level.Level _level)
					_level.sendBlockUpdated(_bp, _bs, _bs, 3);
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
			SlotLooper = 0;
			Direction outputDirection = TextToDirectionProcedure.execute(getBlockString(world, BlockPos.containing(x, y, z), "output_direction"));
			pushItemToSide(world, BlockPos.containing(x, y, z), outputDirection, (int) (double) ConfigurationConfiguration.IRON_TREE_MAX_TRANSFER.get());
		}
					}
		if (Calendar.getInstance().get(Calendar.MINUTE) % 5 == 0) {
			{
				final Vec3 _center = new Vec3(x, (y + 0.8), z);
				for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
					if (entityiterator instanceof ItemEntity) {
						if ((new Object() {
							public String getValue(net.minecraft.world.item.ItemStack item, String path) {
								net.minecraft.nbt.CompoundTag _cur = item.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
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
						}.getValue((entityiterator instanceof ItemEntity _itemEnt ? _itemEnt.getItem() : ItemStack.EMPTY), "type")).equals("display")) {
							if (entityiterator instanceof ItemEntity itemEntity) {
								// Set pickup delay in ticks (20 ticks = 1 second)
								itemEntity.setPickUpDelay(30000);
							}
						}
					}
				}
			}
		}
	}

	private static int pushItemToSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
		if (!isItemOutputAllowed(level, pos, dir))
			return 0;
		IItemHandler source = Transfer.items(level, pos, dir);
		IItemHandler target = Transfer.items(level, pos.relative(dir), dir.getOpposite());
		if (source == null || target == null)
			return 0;
		return Transfer.moveItems(source, target, maxAmount, -1);
	}

	private static int getBlockInventorySlotCount(LevelAccessor world, BlockPos pos) {
		if (world instanceof ILevelExtension ext) {
			IItemHandler itemHandler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (itemHandler != null)
				return itemHandler.getSlots();
		}
		return 0;
	}

	private static int getBlockInventorySlotStackLimit(LevelAccessor world, BlockPos pos, int slotId) {
		if (world instanceof ILevelExtension ext) {
			IItemHandler itemHandler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (itemHandler != null && slotId >= 0 && slotId < itemHandler.getSlots()) {
				return itemHandler.getSlotLimit(slotId);
			}
		}
		return 0;
	}

	private static ItemStack itemFromBlockInventory(LevelAccessor world, BlockPos pos, int slot) {
		if (world instanceof ILevelExtension ext) {
			IItemHandler itemHandler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (itemHandler != null)
				return itemHandler.getStackInSlot(slot);
		}
		return ItemStack.EMPTY;
	}

}