package com.itmecasey.skyblockresources.machine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import com.itmecasey.skyblockresources.recipe.SoilTypeRecipe;
import com.itmecasey.skyblockresources.recipe.RecipeUtils;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;

/**
 * Shared helpers for the generator machines: side configuration, inventory insertion,
 * NBT access, and tier drop lookup. Used by the procedure classes.
 */
public final class MachineHelpers {
	private MachineHelpers() {
	}

	public static void copySideConfig(LevelAccessor level, BlockPos fromPos, BlockPos toPos) {
		if (!(level instanceof Level world))
			return;
		BlockEntity srcBe = world.getBlockEntity(fromPos);
		BlockEntity dstBe = world.getBlockEntity(toPos);
		if (srcBe == null || dstBe == null)
			return;
		CompoundTag srcTag = srcBe.getPersistentData();
		if (srcTag.contains("BlockDirections")) {
			dstBe.getPersistentData().put("BlockDirections", srcTag.getCompoundOrEmpty("BlockDirections").copy());
			dstBe.setChanged();
			world.sendBlockUpdated(toPos, world.getBlockState(toPos), world.getBlockState(toPos), 3);
			world.updateNeighborsAt(toPos, world.getBlockState(toPos).getBlock());
		}
	}

	public static int countMatchingSides(LevelAccessor level, BlockPos pos, String type, String mode) {
		return getMatchingSides(level, pos, type, mode).size();
	}

	public static CompoundTag getBlockDirectionsData(BlockEntity be) {
		if (be == null)
			return null;
		CompoundTag tag = be.getPersistentData();
		if (!tag.contains("BlockDirections")) {
			tag.put("BlockDirections", new CompoundTag());
		}
		return tag.getCompoundOrEmpty("BlockDirections");
	}

	public static double getBlockNumber(LevelAccessor world, BlockPos pos, String key) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity == null ? 0.0 : blockEntity.getPersistentData().getDoubleOr(key, 0.0);
	}

	public static String getBlockString(LevelAccessor world, BlockPos pos, String key) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity == null ? "" : blockEntity.getPersistentData().getStringOr(key, "");
	}

	public static String getDisplayType(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getStringOr("type", "");
	}

	public static ItemStack getHeldItem(Entity entity) {
		return entity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
	}

	public static int getInventoryLevel(LevelAccessor level, BlockPos pos) {
		if (!(level instanceof ILevelExtension ext))
			return 0;
		if (ext.getCapability(Capabilities.Item.BLOCK, pos, null) instanceof ResourceHandler<ItemResource> inv) {
			int total = 0;
			for (int s = 0; s < inv.size(); s++) {
				total += inv.getAmountAsInt(s);
			}
			return total;
		}
		return 0;
	}

	public static List<Direction> getMatchingSides(LevelAccessor level, BlockPos pos, String type, String mode) {
		List<Direction> sides = new java.util.ArrayList<>();
		for (Direction dir : Direction.values()) {
			boolean match = false;
			if (mode.equals("ALL_SIDES")) {
				match = true;
			} else if (mode.equals("INPUT_ONLY")) {
				// Strict: only sides the user has explicitly marked as input.
				match = isSideAllowed(level, pos, dir, type, true);
			} else if (mode.equals("OUTPUT_ONLY")) {
				// Strict: only sides the user has explicitly marked as output.
				match = isSideAllowed(level, pos, dir, type, false);
			} else if (mode.equals("DENY_ALL")) {
				// Strict: only sides the user has explicitly set to false for both
				// input and output (a side they configured then turned off).
				match = isSideConfigured(level, pos, dir, type, true) && isSideConfigured(level, pos, dir, type, false) && !isSideAllowed(level, pos, dir, type, true) && !isSideAllowed(level, pos, dir, type, false);
			} else {
				// ALLOW_ALL: permissive — any side the user has configured, OR all
				// 6 sides if the block has the capability and nothing configured.
				if (hasCapability(level, pos, type)) {
					if (isSideConfigured(level, pos, dir, type, true) || isSideConfigured(level, pos, dir, type, false)) {
						match = isSideAllowed(level, pos, dir, type, true) || isSideAllowed(level, pos, dir, type, false);
					} else {
						match = true;
					}
				}
			}
			if (match) {
				sides.add(dir);
			}
		}
		return sides;
	}

	public static int getMaxInventoryLevel(LevelAccessor level, BlockPos pos) {
		// 26.x ResourceHandler.getCapacityAsInt(int, ItemResource) needs a resource
		// to report a per-slot limit. We can't pick the right one without knowing
		// what the caller intends to insert, so we report the per-slot maximum for
		// the SAME ItemResource we'd build for an insert probe. For empty slots
		// the answer is undefined by design, so we count them as 'fully available'
		// (use the per-slot max for the same item being inserted most commonly).
		// The caller should treat this as an upper bound.
		if (!(level instanceof ILevelExtension ext))
			return 0;
		if (ext.getCapability(Capabilities.Item.BLOCK, pos, null) instanceof ResourceHandler<ItemResource> inv) {
			// We don't have an Item to probe with, so the most useful answer is the
			// slot count * typical max stack (64). For slot-specific capacities the
			// caller can use isInventoryFull, which inspects the actual resources.
			return inv.size() * 64;
		}
		return 0;
	}

	public static int getRedstonePower(LevelAccessor level, BlockPos pos, Direction dir) {
		// null dir = "any direction" sentinel: return the max redstone signal
		// received across all 6 sides.
		if (dir == null) {
			int best = 0;
			for (Direction d : Direction.values()) {
				int v = getRedstonePower(level, pos, d);
				if (v > best)
					best = v;
			}
			return best;
		}
		if (!(level instanceof Level world))
			return 0;
		if (!isSideAllowed(level, pos, dir, "REDSTONE", true))
			return 0;
		return world.getSignal(pos.relative(dir), dir);
	}

	public static String getSideMode(LevelAccessor level, BlockPos pos, Direction dir, String type) {
		// null dir = "any direction" sentinel: aggregate across all 6 sides.
		if (dir == null) {
			boolean anyIn = false, anyOut = false;
			for (Direction d : Direction.values()) {
				if (isSideAllowed(level, pos, d, type, true))
					anyIn = true;
				if (isSideAllowed(level, pos, d, type, false))
					anyOut = true;
				if (anyIn && anyOut)
					return "BOTH";
			}
			if (anyIn)
				return "INPUT";
			if (anyOut)
				return "OUTPUT";
			return "NONE";
		}
		boolean in = isSideAllowed(level, pos, dir, type, true);
		boolean out = isSideAllowed(level, pos, dir, type, false);
		if (in && out)
			return "BOTH";
		if (in)
			return "INPUT";
		if (out)
			return "OUTPUT";
		return "NONE";
	}

	public static double growthMultiplier(LevelAccessor world, double x, double y, double z) {
		BlockEntity be = world.getBlockEntity(BlockPos.containing(x, y, z));
		return be == null ? 0.0 : be.getPersistentData().getDoubleOr("growth_multiplier", 0.0);
	}

	public static boolean hasCapability(LevelAccessor level, BlockPos pos, String type) {
		if (!(level instanceof ILevelExtension ext))
			return false;
		switch (type) {
			case "ITEM" :
				return ext.getCapability(Capabilities.Item.BLOCK, pos, null) != null;
			case "FLUID" :
				return ext.getCapability(Capabilities.Fluid.BLOCK, pos, null) != null;
			case "ENERGY" :
				return ext.getCapability(Capabilities.Energy.BLOCK, pos, null) != null;
			case "REDSTONE" :
				return true;
			default :
				return true;
		}
	}

	public static int insertEnergyIntoSide(LevelAccessor level, BlockPos pos, int amount) {
		if (amount <= 0)
			return 0;
		if (!(level instanceof ILevelExtension ext))
			return 0;
		if (ext.getCapability(Capabilities.Energy.BLOCK, pos, null) instanceof EnergyHandler handler) {
			try (var tx = Transaction.openRoot()) {
				int inserted = (int) handler.insert(amount, tx);
				if (inserted > 0) {
					tx.commit();
					return inserted;
				}
			}
		}
		for (Direction d : Direction.values()) {
			if (ext.getCapability(Capabilities.Energy.BLOCK, pos, d) instanceof EnergyHandler handler) {
				try (var tx = Transaction.openRoot()) {
					int inserted = (int) handler.insert(amount, tx);
					if (inserted > 0) {
						tx.commit();
						return inserted;
					}
				}
			}
		}
		return 0;
	}

	public static int insertFluidIntoSide(LevelAccessor level, BlockPos pos, net.minecraft.world.level.material.Fluid fluid, int amount) {
		if (fluid == null || amount <= 0)
			return 0;
		if (!(level instanceof ILevelExtension ext))
			return 0;
		if (ext.getCapability(Capabilities.Fluid.BLOCK, pos, null) instanceof ResourceHandler<FluidResource> handler) {
			try (var tx = Transaction.openRoot()) {
				int inserted = (int) handler.insert(FluidResource.of(fluid), amount, tx);
				if (inserted > 0) {
					tx.commit();
					return inserted;
				}
			}
		}
		for (Direction d : Direction.values()) {
			if (ext.getCapability(Capabilities.Fluid.BLOCK, pos, d) instanceof ResourceHandler<FluidResource> handler) {
				try (var tx = Transaction.openRoot()) {
					int inserted = (int) handler.insert(FluidResource.of(fluid), amount, tx);
					if (inserted > 0) {
						tx.commit();
						return inserted;
					}
				}
			}
		}
		return 0;
	}

	public static int insertItem(LevelAccessor level, BlockPos pos, Item item, int amount) {
		int inserted = insertItemRaw(level, pos, item, amount);
		com.itmecasey.skyblockresources.script.ScriptHooks.produced(level, pos, item, inserted);
		return inserted;
	}

	private static int insertItemRaw(LevelAccessor level, BlockPos pos, Item item, int amount) {
		if (item == null || amount <= 0 || !(level instanceof ILevelExtension extension))
			return 0;
		if (!(extension.getCapability(Capabilities.Item.BLOCK, pos, null) instanceof ResourceHandler<ItemResource> inventory))
			return 0;
		try (var tx = Transaction.openRoot()) {
			ItemResource resource = ItemResource.of(new ItemStack(item));
			int inserted = (int) inventory.insert(resource, amount, tx);
			tx.commit();
			return inserted;
		}
	}

	public static int insertItemIntoSide(LevelAccessor level, BlockPos pos, int slot, Item template, int amount) {
		int inserted = insertItemIntoSideRaw(level, pos, slot, template, amount);
		com.itmecasey.skyblockresources.script.ScriptHooks.produced(level, pos, template, inserted);
		return inserted;
	}

	private static int insertItemIntoSideRaw(LevelAccessor level, BlockPos pos, int slot, Item template, int amount) {
		if (template == null || amount <= 0)
			return 0;
		if (!(level instanceof ILevelExtension ext))
			return 0;
		if (ext.getCapability(Capabilities.Item.BLOCK, pos, null) instanceof ResourceHandler<ItemResource> inv) {
			try (var tx = Transaction.openRoot()) {
				// ItemResource has only a PRIVATE constructor in 26.x; the public
				// factory ItemResource.of(ItemStack) returns a default resource for
				// plain items (no NBT/components).
				ItemResource res = ItemResource.of(new ItemStack(template));
				if (slot >= 0 && slot < inv.size()) {
					int inserted = (int) inv.insert(slot, res, amount, tx);
					tx.commit();
					return inserted;
				}
				int inserted = (int) inv.insert(res, amount, tx);
				tx.commit();
				return inserted;
			}
		}
		return 0;
	}

	public static boolean isItemOutputAllowed(LevelAccessor level, BlockPos pos, Direction dir) {
		if (!(level instanceof Level world) || !(level instanceof ILevelExtension extension))
			return false;
		if (extension.getCapability(Capabilities.Item.BLOCK, pos, null) == null)
			return false;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity == null || !blockEntity.getPersistentData().contains("BlockDirections"))
			return false;
		CompoundTag directions = blockEntity.getPersistentData().getCompoundOrEmpty("BlockDirections");
		return directions.getBooleanOr("item_touched_" + dir.getName(), false) && directions.getBooleanOr("item_out_" + dir.getName(), false);
	}

	public static boolean isSideAllowed(LevelAccessor level, BlockPos pos, Direction dir, String type, boolean isInput) {
		// null dir = "any direction" sentinel: return true if ANY side allows.
		if (dir == null) {
			for (Direction d : Direction.values()) {
				if (isSideAllowed(level, pos, d, type, isInput))
					return true;
			}
			return false;
		}
		if (!isSideConfigured(level, pos, dir, type, isInput))
			return false;
		if (!(level instanceof Level world))
			return false;
		BlockEntity be = world.getBlockEntity(pos);
		if (be == null)
			return false;
		CompoundTag tag = be.getPersistentData();
		CompoundTag data = tag.getCompoundOrEmpty("BlockDirections");
		String key = type.toLowerCase(java.util.Locale.ENGLISH) + (isInput ? "_in_" : "_out_") + dir.getName();
		return data.getBooleanOr(key, false);
	}

	public static boolean isSideConfigured(LevelAccessor level, BlockPos pos, Direction dir, String type, boolean isInput) {
		if (dir == null)
			return false;
		if (!(level instanceof Level world))
			return false;
		if (!hasCapability(level, pos, type))
			return false;
		BlockEntity be = world.getBlockEntity(pos);
		if (be == null)
			return false;
		CompoundTag tag = be.getPersistentData();
		if (!tag.contains("BlockDirections"))
			return false;
		CompoundTag data = tag.getCompoundOrEmpty("BlockDirections");
		String touchedKey = type.toLowerCase(java.util.Locale.ENGLISH) + "_touched_" + dir.getName();
		return data.getBooleanOr(touchedKey, false);
	}

	public static double parseDouble(String s) {
		try {
			return Double.parseDouble(s.trim());
		} catch (Exception e) {
			return 0;
		}
	}

	public static int placeIntoPlayerInventory(LevelAccessor level, BlockPos pos, int slot, net.minecraft.world.entity.Entity entity, int amount) {
		if (level.isClientSide() || amount <= 0 || !(entity instanceof net.minecraft.world.entity.player.Player player))
			return 0;
		// 1. Direct Container access (vanilla and modded blocks with an inventory)
		if (level.getBlockEntity(pos) instanceof net.minecraft.world.Container container) {
			if (slot >= 0) {
				if (slot >= container.getContainerSize())
					return 0;
				ItemStack inSlot = container.getItem(slot);
				if (inSlot.isEmpty())
					return 0;
				int toTake = Math.min(amount, inSlot.getCount());
				ItemStack moving = inSlot.copyWithCount(toTake);
				int countBefore = moving.getCount();
				player.getInventory().add(moving);
				int moved = countBefore - moving.getCount();
				if (moved > 0) {
					inSlot.shrink(moved);
					if (inSlot.isEmpty()) {
						container.setItem(slot, ItemStack.EMPTY);
					} else {
						container.setItem(slot, inSlot);
					}
					container.setChanged();
					if (level instanceof Level world) {
						world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
					}
					player.containerMenu.broadcastChanges();
				}
				return moved;
			} else {
				int remaining = amount;
				int totalMoved = 0;
				for (int s = 0; s < container.getContainerSize() && remaining > 0; s++) {
					ItemStack inSlot = container.getItem(s);
					if (!inSlot.isEmpty()) {
						int toTake = Math.min(remaining, inSlot.getCount());
						ItemStack moving = inSlot.copyWithCount(toTake);
						int countBefore = moving.getCount();
						player.getInventory().add(moving);
						int moved = countBefore - moving.getCount();
						if (moved > 0) {
							inSlot.shrink(moved);
							if (inSlot.isEmpty()) {
								container.setItem(s, ItemStack.EMPTY);
							} else {
								container.setItem(s, inSlot);
							}
							totalMoved += moved;
							remaining -= moved;
						}
						if (moving.getCount() > 0) {
							break;
						}
					}
				}
				if (totalMoved > 0) {
					container.setChanged();
					if (level instanceof Level world) {
						world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
					}
					player.containerMenu.broadcastChanges();
				}
				return totalMoved;
			}
		}
		// 2. Fallback for pure capability blocks (Transfer API)
		if (level instanceof ILevelExtension ext && ext.getCapability(Capabilities.Item.BLOCK, pos, null) instanceof ResourceHandler<ItemResource> inv) {
			try (var tx = Transaction.openRoot()) {
				if (slot >= 0) {
					if (slot >= inv.size())
						return 0;
					var res = inv.getResource(slot);
					if (!res.isEmpty()) {
						int avail = Math.min(amount, inv.getAmountAsInt(slot));
						ItemStack moving = res.toStack(avail);
						int countBefore = moving.getCount();
						player.getInventory().add(moving);
						int moved = countBefore - moving.getCount();
						if (moved > 0) {
							inv.extract(slot, res, moved, tx);
							tx.commit();
							player.containerMenu.broadcastChanges();
							return moved;
						}
					}
				} else {
					int remaining = amount;
					int totalMoved = 0;
					for (int s = 0; s < inv.size() && remaining > 0; s++) {
						var res = inv.getResource(s);
						if (res.isEmpty())
							continue;
						int avail = Math.min(remaining, inv.getAmountAsInt(s));
						ItemStack moving = res.toStack(avail);
						int countBefore = moving.getCount();
						player.getInventory().add(moving);
						int moved = countBefore - moving.getCount();
						if (moved > 0) {
							inv.extract(s, res, moved, tx);
							totalMoved += moved;
							remaining -= moved;
						}
						if (moving.getCount() > 0) {
							break;
						}
					}
					if (totalMoved > 0) {
						tx.commit();
						player.containerMenu.broadcastChanges();
					}
					return totalMoved;
				}
			}
		}
		return 0;
	}

	public static void plantTree(LevelAccessor world, double x, double y, double z, BlockPos pos, ItemStack heldItem) {
		ItemStack displayStack = heldItem.copy();
		displayStack.setCount(1);
		CustomData.update(DataComponents.CUSTOM_DATA, displayStack, tag -> tag.putString("type", "display"));
		if (world instanceof ServerLevel level) {
			ItemEntity displayItem = new ItemEntity(level, x + 0.5, y + 0.3, z + 0.5, displayStack);
			displayItem.setPickUpDelay(10000);
			displayItem.setUnlimitedLifetime();
			level.addFreshEntity(displayItem);
		}

		Vec3 center = new Vec3(x + 0.5, y + 0.5, z + 0.5);
		for (ItemEntity itemEntity : world.getEntitiesOfClass(ItemEntity.class, new AABB(center, center).inflate(1 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center))).toList()) {
			if (!getDisplayType(itemEntity.getItem()).equals("display"))
				continue;
			updateBlockString(world, pos, "item_uuid", itemEntity.getStringUUID());
			itemEntity.teleportTo(x + 0.5, y + 0.3, z + 0.5);
			itemEntity.setDeltaMovement(new Vec3(0, 0, 0));
			itemEntity.setNoGravity(true);
		}

		updateBlockString(world, pos, "resource_name", BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString());
		playSound(world, x, y, z, "block.cherry_sapling.place", 0.3f);
		heldItem.shrink(1);
	}

	public static void playSound(LevelAccessor world, double x, double y, double z, String soundId, float volume) {
		if (!(world instanceof Level level))
			return;
		SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(soundId));
		if (level.isClientSide())
			level.playLocalSound(x, y, z, sound, SoundSource.NEUTRAL, volume, 1, false);
		else
			level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, volume, 1);
	}

	public static int pullEnergyFromSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
		// null dir = "any direction" sentinel: pull from all 6 sides up to maxAmount.
		if (dir == null) {
			int remaining = maxAmount;
			int total = 0;
			for (Direction d : Direction.values()) {
				if (remaining <= 0)
					break;
				int t = pullEnergyFromSide(level, pos, d, remaining);
				total += t;
				remaining -= t;
			}
			return total;
		}
		if (!isSideAllowed(level, pos, dir, "ENERGY", true))
			return 0;
		if (!(level instanceof ILevelExtension ext && level instanceof Level world))
			return 0;
		if (ext.getCapability(Capabilities.Energy.BLOCK, pos, dir) instanceof EnergyHandler dst && ext.getCapability(Capabilities.Energy.BLOCK, pos.relative(dir), dir.getOpposite()) instanceof EnergyHandler src) {
			try (var tx = Transaction.openRoot()) {
				int extracted = (int) src.extract(maxAmount, tx);
				if (extracted > 0) {
					int inserted = (int) dst.insert(extracted, tx);
					if (inserted > 0) {
						tx.commit();
						return inserted;
					}
				}
			}
		}
		return 0;
	}

	public static int pullFluidFromSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
		// null dir = "any direction" sentinel: pull from all 6 sides, accumulated.
		if (dir == null) {
			int remaining = maxAmount;
			int total = 0;
			for (Direction d : Direction.values()) {
				if (remaining <= 0)
					break;
				int t = pullFluidFromSide(level, pos, d, remaining);
				total += t;
				remaining -= t;
			}
			return total;
		}
		if (!isSideAllowed(level, pos, dir, "FLUID", true))
			return 0;
		if (!(level instanceof ILevelExtension ext && level instanceof Level world))
			return 0;
		if (ext.getCapability(Capabilities.Fluid.BLOCK, pos, dir) instanceof ResourceHandler<FluidResource> dst && ext.getCapability(Capabilities.Fluid.BLOCK, pos.relative(dir), dir.getOpposite()) instanceof ResourceHandler<FluidResource> src) {
			try (var tx = Transaction.openRoot()) {
				for (int i = 0; i < src.size(); i++) {
					var res = src.getResource(i);
					if (res.isEmpty())
						continue;
					int available = src.getAmountAsInt(i);
					if (available <= 0)
						continue;
					int toExtract = Math.min(maxAmount, available);
					int extracted = (int) src.extract(i, res, toExtract, tx);
					if (extracted > 0) {
						int inserted = (int) dst.insert(res, extracted, tx);
						// Put back what the target did not take, or the difference would vanish when the transaction commits.
						if (inserted < extracted)
							src.insert(i, res, extracted - inserted, tx);
						if (inserted > 0) {
							tx.commit();
							return inserted;
						}
					}
				}
			}
		}
		return 0;
	}

	public static int pullItemFromSide(LevelAccessor level, BlockPos pos, Direction dir, int slot, int maxAmount) {
		// null dir = "any direction" sentinel: pull from each of the 6 neighbors
		// (each has its own inventory), accumulating the total.
		if (dir == null) {
			int total = 0;
			for (Direction d : Direction.values()) {
				total += pullItemFromSide(level, pos, d, slot, maxAmount);
			}
			return total;
		}
		if (!isSideAllowed(level, pos, dir, "ITEM", true))
			return 0;
		if (!(level instanceof ILevelExtension ext && level instanceof Level world))
			return 0;
		if (ext.getCapability(Capabilities.Item.BLOCK, pos, dir) instanceof ResourceHandler<ItemResource> dst && ext.getCapability(Capabilities.Item.BLOCK, pos.relative(dir), dir.getOpposite()) instanceof ResourceHandler<ItemResource> src) {
			int remainingAmount = maxAmount;
			int totalTransferred = 0;
			try (var tx = Transaction.openRoot()) {
				for (int srcSlot = 0; srcSlot < src.size() && remainingAmount > 0; srcSlot++) {
					var res = src.getResource(srcSlot);
					if (res.isEmpty())
						continue;
					int extracted = (int) src.extract(srcSlot, res, remainingAmount, tx);
					if (extracted > 0) {
						int inserted = (slot >= 0 && slot < dst.size()) ? (int) dst.insert(slot, res, extracted, tx) : (int) dst.insert(res, extracted, tx);
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
		return 0;
	}

	public static int pushEnergyToSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
		// null dir = "any direction" sentinel: try to push up to maxAmount across
		// all 6 sides, accumulating the actually-transferred amount.
		if (dir == null) {
			int remaining = maxAmount;
			int total = 0;
			for (Direction d : Direction.values()) {
				if (remaining <= 0)
					break;
				int t = pushEnergyToSide(level, pos, d, remaining);
				total += t;
				remaining -= t;
			}
			return total;
		}
		if (!isSideAllowed(level, pos, dir, "ENERGY", false))
			return 0;
		if (!(level instanceof ILevelExtension ext && level instanceof Level world))
			return 0;
		if (ext.getCapability(Capabilities.Energy.BLOCK, pos, dir) instanceof EnergyHandler src && ext.getCapability(Capabilities.Energy.BLOCK, pos.relative(dir), dir.getOpposite()) instanceof EnergyHandler dst) {
			try (var tx = Transaction.openRoot()) {
				int extracted = (int) src.extract(maxAmount, tx);
				if (extracted > 0) {
					int inserted = (int) dst.insert(extracted, tx);
					if (inserted > 0) {
						tx.commit();
						return inserted;
					}
				}
			}
		}
		return 0;
	}

	public static int pushFluidToSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
		// null dir = "any direction" sentinel: push across all 6 sides, accumulated.
		if (dir == null) {
			int remaining = maxAmount;
			int total = 0;
			for (Direction d : Direction.values()) {
				if (remaining <= 0)
					break;
				int t = pushFluidToSide(level, pos, d, remaining);
				total += t;
				remaining -= t;
			}
			return total;
		}
		if (!isSideAllowed(level, pos, dir, "FLUID", false))
			return 0;
		if (!(level instanceof ILevelExtension ext && level instanceof Level world))
			return 0;
		if (ext.getCapability(Capabilities.Fluid.BLOCK, pos, dir) instanceof ResourceHandler<FluidResource> src && ext.getCapability(Capabilities.Fluid.BLOCK, pos.relative(dir), dir.getOpposite()) instanceof ResourceHandler<FluidResource> dst) {
			try (var tx = Transaction.openRoot()) {
				for (int i = 0; i < src.size(); i++) {
					var res = src.getResource(i);
					if (res.isEmpty())
						continue;
					int available = src.getAmountAsInt(i);
					if (available <= 0)
						continue;
					int toExtract = Math.min(maxAmount, available);
					int extracted = (int) src.extract(i, res, toExtract, tx);
					if (extracted > 0) {
						int inserted = (int) dst.insert(res, extracted, tx);
						// Put back what the target did not take, or the difference would vanish when the transaction commits.
						if (inserted < extracted)
							src.insert(i, res, extracted - inserted, tx);
						if (inserted > 0) {
							tx.commit();
							return inserted;
						}
					}
				}
			}
		}
		return 0;
	}

	public static void resetSideConfig(LevelAccessor level, BlockPos pos) {
		if (!(level instanceof Level world))
			return;
		BlockEntity be = world.getBlockEntity(pos);
		if (be == null)
			return;
		be.getPersistentData().remove("BlockDirections");
		be.setChanged();
		world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		world.updateNeighborsAt(pos, world.getBlockState(pos).getBlock());
	}

	public static Item resourceItem(LevelAccessor world, double x, double y, double z) {
		BlockEntity be = world.getBlockEntity(BlockPos.containing(x, y, z));
		if (be == null)
			return Items.AIR;
		String name = be.getPersistentData().getStringOr("resource_name", "");
		return BuiltInRegistries.ITEM.getValue(Identifier.parse(name.toLowerCase(java.util.Locale.ENGLISH)));
	}

	public static void setAllSidesMode(LevelAccessor level, BlockPos pos, String type, String mode) {
		for (Direction dir : Direction.values()) {
			setSideMode(level, pos, dir, type, mode);
		}
	}

	public static void setBlockNumber(LevelAccessor world, BlockPos pos, String key, double value) {
		if (world.isClientSide())
			return;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		BlockState state = world.getBlockState(pos);
		if (blockEntity != null)
			blockEntity.getPersistentData().putDouble(key, value);
		if (world instanceof Level level)
			level.sendBlockUpdated(pos, state, state, 3);
	}

	public static void setRedstonePower(LevelAccessor level, BlockPos pos, Direction dir, int power) {
		// null dir = "any direction" sentinel: emit on all 6 sides.
		if (dir == null) {
			for (Direction d : Direction.values())
				setRedstonePower(level, pos, d, power);
			return;
		}
		if (!(level instanceof Level world))
			return;
		BlockEntity be = world.getBlockEntity(pos);
		if (be == null)
			return;
		CompoundTag data = getBlockDirectionsData(be);
		if (data == null)
			return;
		data.putInt("redstone_power_" + dir.getName(), Math.max(0, Math.min(15, power)));
		be.setChanged();
		world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		world.updateNeighborsAt(pos, world.getBlockState(pos).getBlock());
		world.updateNeighborsAt(pos.relative(dir), world.getBlockState(pos.relative(dir)).getBlock());
	}

	public static void setSideMode(LevelAccessor level, BlockPos pos, Direction dir, String type, String mode) {
		// null dir = BlockDirections "any direction" sentinel: apply to all 6 sides.
		if (dir == null) {
			for (Direction d : Direction.values())
				setSideMode(level, pos, d, type, mode);
			return;
		}
		if (!(level instanceof Level world))
			return;
		BlockEntity be = world.getBlockEntity(pos);
		if (be == null)
			return;
		CompoundTag data = getBlockDirectionsData(be);
		if (data == null)
			return;
		boolean in = mode.equals("ALLOW_ALL") || mode.equals("INPUT_ONLY");
		boolean out = mode.equals("ALLOW_ALL") || mode.equals("OUTPUT_ONLY");
		if (type.equals("ALL") || type.equals("ENERGY")) {
			data.putBoolean("energy_in_" + dir.getName(), in);
			data.putBoolean("energy_out_" + dir.getName(), out);
			data.putBoolean("energy_touched_" + dir.getName(), true);
		}
		if (type.equals("ALL") || type.equals("FLUID")) {
			data.putBoolean("fluid_in_" + dir.getName(), in);
			data.putBoolean("fluid_out_" + dir.getName(), out);
			data.putBoolean("fluid_touched_" + dir.getName(), true);
		}
		if (type.equals("ALL") || type.equals("ITEM")) {
			data.putBoolean("item_in_" + dir.getName(), in);
			data.putBoolean("item_out_" + dir.getName(), out);
			data.putBoolean("item_touched_" + dir.getName(), true);
		}
		if (type.equals("ALL") || type.equals("REDSTONE")) {
			data.putBoolean("redstone_in_" + dir.getName(), in);
			data.putBoolean("redstone_out_" + dir.getName(), out);
			data.putBoolean("redstone_touched_" + dir.getName(), true);
		}
		be.setChanged();
		world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		world.updateNeighborsAt(pos, world.getBlockState(pos).getBlock());
	}

	public static void updateBlockString(LevelAccessor world, BlockPos pos, String key, String value) {
		if (world.isClientSide())
			return;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		BlockState state = world.getBlockState(pos);
		if (blockEntity != null)
			blockEntity.getPersistentData().putString(key, value);
		if (world instanceof Level level)
			level.sendBlockUpdated(pos, state, state, 3);
	}

	/** Ticks for one generation cycle: the recipe duration stored on the machine, for every machine type. */
	public static double generationTicks(BlockEntity be) {
		return be.getPersistentData().getDoubleOr("duration", 0.0);
	}

	/** Fraction of the current generation cycle that has passed, from 0 to 1. */
	public static double progress(BlockEntity be) {
		double ticks = generationTicks(be);
		if (ticks <= 0)
			return 0.0;
		return Math.min(1.0, Math.max(0.0, be.getPersistentData().getDoubleOr("timer", 0.0) / ticks));
	}

	public static double progress(LevelAccessor world, BlockPos pos) {
		BlockEntity be = world.getBlockEntity(pos);
		return be == null ? 0.0 : progress(be);
	}

	/** True when the machine's internal inventory holds at least one item. */
	public static boolean hasStock(LevelAccessor world, BlockPos pos) {
		BlockEntity be = world.getBlockEntity(pos);
		return be instanceof net.minecraft.world.Container container && !container.isEmpty();
	}

	private static final String MYSTICAL_NAMESPACE = "mysticalagriculture";

	public static boolean isMysticalItem(ItemStack stack) {
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(MYSTICAL_NAMESPACE);
	}

	/** Mystical Agriculture seeds may only go on plain farmland or on Mystical Agriculture farmland. */
	public static boolean isAllowedSoilFor(ItemStack seed, ItemStack soil) {
		if (!isMysticalItem(seed))
			return true;
		Identifier id = BuiltInRegistries.ITEM.getKey(soil.getItem());
		if (id.equals(Identifier.parse("minecraft:farmland")))
			return true;
		return id.getNamespace().equals(MYSTICAL_NAMESPACE) && id.getPath().endsWith("_farmland");
	}

	/**
	 * Chance of one extra seed per harvest: the soil recipe's bonus, plus another bonus when a
	 * Mystical Agriculture seed is planted on farmland of its own tier. Capped at 1.
	 */
	public static double extraSeedChance(LevelAccessor world, BlockPos pos, ItemStack seed) {
		String soilId = world.getBlockEntity(pos) == null ? "" : world.getBlockEntity(pos).getPersistentData().getStringOr("soil_type", "");
		if (soilId.isEmpty() || soilId.equals("empty"))
			return 0.0;
		ItemStack soil = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(soilId)));
		double chance = 0.0;
		if (world instanceof Level level) {
			for (SoilTypeRecipe recipe : RecipeUtils.getRecipes(level, SkyblockResourcesModRecipeTypes.SOILTYPE_TYPE.get())) {
				if (RecipeUtils.validate(soil, recipe.soilItemInput())) {
					chance = recipe.getDoubleResult("bonus_seed_chance");
					break;
				}
			}
		}
		if (isMysticalItem(seed) && soilId.startsWith(MYSTICAL_NAMESPACE + ":")) {
			String seedTier = BuiltInRegistries.ITEM.getKey(seed.getItem()).getPath().split("_")[0];
			String soilTier = soilId.substring(soilId.indexOf(':') + 1).split("_")[0];
			if (seedTier.equals(soilTier))
				chance += 0.10;
		}
		return Math.min(1.0, chance);
	}
}
