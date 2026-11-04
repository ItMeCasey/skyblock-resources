package com.itmecasey.skyblockresources;

import com.itmecasey.skyblockresources.compat.NbtCompat;
import com.itmecasey.skyblockresources.compat.OverlayRender;


import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;

import com.itmecasey.skyblockresources.init.SkyblockResourcesModItems;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;

@EventBusSubscriber(value = Dist.CLIENT)
public class ShowGeneratorResource {
	private static net.minecraft.client.renderer.RenderType blockOverlayTextureRenderType(ResourceLocation texture) {
		return net.minecraft.client.renderer.RenderType.entityTranslucent(texture);
	}

	private static net.minecraft.client.renderer.RenderType blockOverlayTextureRenderTypeOpaque(ResourceLocation texture) {
		return net.minecraft.client.renderer.RenderType.entityCutout(texture);
	}


	private static java.util.Set<net.minecraft.world.level.block.Block> TARGET_BLOCKS = null;

	private static boolean matchesTarget(BlockState state) {
		if (TARGET_BLOCKS == null) {
			java.util.Set<net.minecraft.world.level.block.Block> set = new HashSet<>();
			for (String bid : java.util.List.of("skyblock_resources:wooden_generator", "skyblock_resources:stone_generator", "skyblock_resources:iron_generator", "skyblock_resources:diamond_generator", "skyblock_resources:netherite_generator")) {
				var b = BuiltInRegistries.BLOCK.get(new ResourceLocation(bid));
				if (b != null)
					set.add(b);
			}
			TARGET_BLOCKS = set;
		}
		return TARGET_BLOCKS.contains(state.getBlock());
	}

	private static final Map<Long, Set<Long>> VISIBLE_SECTION_POSITIONS = new HashMap<>();
	private static long lastVisibleSectionRefresh = -20;

	private static Set<Long> findMatches(Level level, int secX, int secY, int secZ) {
		if (!level.hasChunk(secX, secZ))
			return java.util.Collections.emptySet();
		net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunk(secX, secZ);
		if (chunk == null)
			return java.util.Collections.emptySet();
		int secIndex = chunk.getSectionIndexFromSectionY(secY);
		if (secIndex < 0 || secIndex >= chunk.getSections().length)
			return java.util.Collections.emptySet();
		net.minecraft.world.level.chunk.LevelChunkSection section = chunk.getSection(secIndex);
		if (section == null || section.hasOnlyAir())
			return java.util.Collections.emptySet();
		Set<Long> positions = new HashSet<>();
		BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
		int originX = secX << 4;
		int originY = secY << 4;
		int originZ = secZ << 4;
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					if (matchesTarget(section.getBlockState(x, y, z))) {
						mut.set(originX + x, originY + y, originZ + z);
						positions.add(mut.asLong());
					}
				}
			}
		}
		return positions.isEmpty() ? java.util.Collections.emptySet() : positions;
	}


	private static double anchorX(String placement) {
		return placement.endsWith("_LEFT") ? 1.0 : placement.endsWith("_RIGHT") ? 0.0 : 0.5;
	}

	private static double anchorY(String placement) {
		return placement.startsWith("TOP_") ? 1.0 : placement.startsWith("BOTTOM_") ? 0.0 : 0.5;
	}

	private static double itemPlacementX(String placement, double scale) {
		double half = Math.min(0.5, Math.abs(scale) / 2.0);
		return placement.endsWith("_LEFT") ? 0.5 - half : placement.endsWith("_RIGHT") ? -0.5 + half : 0.0;
	}

	private static double itemPlacementY(String placement, double scale) {
		double half = Math.min(0.5, Math.abs(scale) / 2.0);
		return placement.startsWith("TOP_") ? 0.5 - half : placement.startsWith("BOTTOM_") ? -0.5 + half : 0.0;
	}

	private static float localX(String placement, float width) {
		return placement.endsWith("_LEFT") ? 0 : placement.endsWith("_RIGHT") ? -width : -width / 2;
	}

	private static float localY(String placement, float height) {
		return placement.startsWith("TOP_") ? 0 : placement.startsWith("BOTTOM_") ? -height : -height / 2;
	}

	private static double placementOffsetX(String placement, double padding) {
		return placement.endsWith("_LEFT") ? padding : placement.endsWith("_RIGHT") ? -padding : 0.0;
	}

	private static double placementOffsetY(String placement, double padding) {
		return placement.startsWith("TOP_") ? -padding : placement.startsWith("BOTTOM_") ? padding : 0.0;
	}

	private static org.joml.Quaternionf faceRotation(Direction side) {
		return switch (side) {
			case DOWN -> Axis.XP.rotationDegrees(-90.0F);
			case UP -> Axis.XP.rotationDegrees(90.0F);
			case NORTH -> new org.joml.Quaternionf();
			case SOUTH -> Axis.YP.rotationDegrees(180.0F);
			case WEST -> Axis.YP.rotationDegrees(90.0F);
			case EAST -> Axis.YP.rotationDegrees(-90.0F);
		};
	}

	private static Vec3 shapeBoundsPlacement(BlockState state, Level level, BlockPos position, Direction side, String placement) {
		return shapeBoundsPlacement(state, level, position, side, placement, 0.0, 0.0, 0.0);
	}

	private static Vec3 shapeBoundsPlacement(BlockState state, Level level, BlockPos position, Direction side, String placement, double elementWidth, double elementHeight) {
		return shapeBoundsPlacement(state, level, position, side, placement, elementWidth, elementHeight, 0.0);
	}

	private static Vec3 shapeBoundsPlacement(BlockState state, Level level, BlockPos position, Direction side, String placement, double elementWidth, double elementHeight, double padding) {
		net.minecraft.world.phys.shapes.VoxelShape shape = state.getShape(level, position);
		net.minecraft.world.phys.AABB bounds = shape.isEmpty() ? net.minecraft.world.phys.shapes.Shapes.block().bounds() : shape.bounds();
		double horizontalMinimum = 0;
		double horizontalMaximum = 1;
		double verticalMinimum = 0;
		double verticalMaximum = 1;
		double depth = -0.501;
		switch (side) {
			case NORTH -> {
				horizontalMinimum = bounds.minX;
				horizontalMaximum = bounds.maxX;
				verticalMinimum = bounds.minY;
				verticalMaximum = bounds.maxY;
				depth = bounds.minZ - 0.501;
			}
			case SOUTH -> {
				horizontalMinimum = 1 - bounds.maxX;
				horizontalMaximum = 1 - bounds.minX;
				verticalMinimum = bounds.minY;
				verticalMaximum = bounds.maxY;
				depth = 0.499 - bounds.maxZ;
			}
			case WEST -> {
				horizontalMinimum = 1 - bounds.maxZ;
				horizontalMaximum = 1 - bounds.minZ;
				verticalMinimum = bounds.minY;
				verticalMaximum = bounds.maxY;
				depth = bounds.minX - 0.501;
			}
			case EAST -> {
				horizontalMinimum = bounds.minZ;
				horizontalMaximum = bounds.maxZ;
				verticalMinimum = bounds.minY;
				verticalMaximum = bounds.maxY;
				depth = 0.499 - bounds.maxX;
			}
			case UP -> {
				horizontalMinimum = 1 - bounds.maxX;
				horizontalMaximum = 1 - bounds.minX;
				verticalMinimum = 1 - bounds.maxZ;
				verticalMaximum = 1 - bounds.minZ;
				depth = 0.499 - bounds.maxY;
			}
			case DOWN -> {
				horizontalMinimum = 1 - bounds.maxX;
				horizontalMaximum = 1 - bounds.minX;
				verticalMinimum = 1 - bounds.maxZ;
				verticalMaximum = 1 - bounds.minZ;
				depth = bounds.minY - 0.501;
			}
		}
		double hSpan = horizontalMaximum - horizontalMinimum;
		double vSpan = verticalMaximum - verticalMinimum;
		double halfW = Math.min(hSpan / 2.0, Math.abs(elementWidth) / 2.0);
		double halfH = Math.min(vSpan / 2.0, Math.abs(elementHeight) / 2.0);
		double h = placement.endsWith("_LEFT") ? horizontalMaximum - halfW : placement.endsWith("_RIGHT") ? horizontalMinimum + halfW : (horizontalMinimum + horizontalMaximum) / 2.0;
		double v = placement.startsWith("TOP_") ? verticalMaximum - halfH : placement.startsWith("BOTTOM_") ? verticalMinimum + halfH : (verticalMinimum + verticalMaximum) / 2.0;
		double offsetX = placement.endsWith("_LEFT") ? padding : placement.endsWith("_RIGHT") ? -padding : 0.0;
		double offsetY = placement.startsWith("TOP_") ? -padding : placement.startsWith("BOTTOM_") ? padding : 0.0;
		return new Vec3(h - 0.5 + offsetX, v - 0.5 + offsetY, depth);
	}



	// Helper for overlay_builder_outline: emit N parallel copies of one shape edge, each offset
	// along the face plane(s) the edge sits on. Width=1 -> single edge on the original line.
	// Width>1 -> additional parallel edges extending INTO the faces it touches (not outwards
	// from the block). Caller is responsible for translating the pose stack into shape space.




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














	// Inventory query helpers. These inspect the LOCAL block's own item handler
	// (i.e. the block at `pos`, not a neighbor). Transfer API (ResourceHandler).
	private static int getInventoryLevel(LevelAccessor level, BlockPos pos) {
		return com.itmecasey.skyblockresources.machine.MachineHelpers.getInventoryLevel(level, pos);
	}


	private static boolean isInventoryFull(LevelAccessor level, BlockPos pos) {
		return com.itmecasey.skyblockresources.compat.Transfer.isFull(level, pos);
	}

	// "Insert" helper: generate `amount` items of `template` and place them
	// into slot S of the LOCAL block's own inventory.
	// The item is an Item reference, so it is wrapped in an ItemStack before
	// handing it to the ItemResource API.
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

	private static Direction getBlockDirection(LevelAccessor world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		Property<?> property = blockState.getBlock().getStateDefinition().getProperty("facing");
		if (property != null && blockState.getValue(property) instanceof Direction direction)
			return direction;
		else if (blockState.hasProperty(BlockStateProperties.AXIS))
			return Direction.fromAxisAndDirection(blockState.getValue(BlockStateProperties.AXIS), Direction.AxisDirection.POSITIVE);
		else if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
			return Direction.fromAxisAndDirection(blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS), Direction.AxisDirection.POSITIVE);
		return Direction.NORTH;
	}

	@SubscribeEvent
	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)
			return;
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null)
			return;
		Vec3 playerPos = minecraft.player.position();
		double maxDist = 24D;
		double maxDistSq = maxDist * maxDist;
		int minSecX = (int) Math.floor((playerPos.x - maxDist) / 16.0);
		int maxSecX = (int) Math.floor((playerPos.x + maxDist) / 16.0);
		int minSecY = (int) Math.floor((playerPos.y - maxDist) / 16.0);
		int maxSecY = (int) Math.floor((playerPos.y + maxDist) / 16.0);
		int minSecZ = (int) Math.floor((playerPos.z - maxDist) / 16.0);
		int maxSecZ = (int) Math.floor((playerPos.z + maxDist) / 16.0);
		long now = minecraft.level.getGameTime();
		boolean doRefresh = now - lastVisibleSectionRefresh >= 20;
		if (doRefresh) {
			lastVisibleSectionRefresh = now;
		}
		Set<Long> activeSectionKeys = new HashSet<>();
		for (int sx = minSecX; sx <= maxSecX; sx++) {
			for (int sy = minSecY; sy <= maxSecY; sy++) {
				for (int sz = minSecZ; sz <= maxSecZ; sz++) {
					long sectionKey = BlockPos.asLong(sx, sy, sz);
					activeSectionKeys.add(sectionKey);
					if (doRefresh || !VISIBLE_SECTION_POSITIONS.containsKey(sectionKey)) {
						VISIBLE_SECTION_POSITIONS.put(sectionKey, findMatches(minecraft.level, sx, sy, sz));
					}
					Set<Long> positions = VISIBLE_SECTION_POSITIONS.get(sectionKey);
					if (positions != null && !positions.isEmpty()) {
						for (long packedPosition : positions) {
							BlockPos position = BlockPos.of(packedPosition);
							if (minecraft.player.distanceToSqr(Vec3.atCenterOf(position)) <= maxDistSq)
								renderAt(event, minecraft, position);
						}
					}
				}
			}
		}
		if (doRefresh) {
			VISIBLE_SECTION_POSITIONS.keySet().removeIf(k -> !activeSectionKeys.contains(k));
		}
		OverlayRender.flush();
	}

	private static void renderAt(RenderLevelStageEvent event, Minecraft minecraft, BlockPos position) {
		if (!matchesTarget(minecraft.level.getBlockState(position)))
			return;
		var world = minecraft.level;
		var entity = minecraft.player;
		double x = position.getX();
		double y = position.getY();
		double z = position.getZ();
		if (!((nbtString(world, BlockPos.containing(x, y, z), "resource_name")).equals("empty"))) {
			if (!isInventoryFull(world, BlockPos.containing(x, y, z))) {
				if (event != null) {
					RenderLevelStageEvent _overlayEvent = event;
					float _scale = (float) 0.6;
					float _rotX = (float) 0;
					float _rotY = (float) 0;
					float _rotZ = (float) 0;
					float _bobHeight = Math.max(0, (float) 0.05);
					float _animTime = (float) (System.nanoTime() / 1_000_000L % 36000000L) / 50.0f;
					float _spinDegrees = _animTime * (float) 1;
					float _bob = (float) Math.sin(_animTime * 0.075f) * _bobHeight;
					double _offsetY = ((Number) 0).doubleValue() / 16.0;
					PoseStack _poseStack = _overlayEvent.getPoseStack();
					Vec3 _camera = _overlayEvent.getCamera().getPosition();
					_poseStack.pushPose();
					_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + 0.5 + _bob + _offsetY, z - _camera.z + 0.5);
					_poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(_spinDegrees));
					if (_rotX != 0.0F)
						_poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(_rotX));
					if (_rotY != 0.0F)
						_poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(_rotY));
					if (_rotZ != 0.0F)
						_poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(_rotZ));
					_poseStack.scale(_scale, _scale, _scale);
					
					final ItemStack _itemStackToDraw = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(((nbtString(world, BlockPos.containing(x, y, z), "resource_name"))).toLowerCase(java.util.Locale.ENGLISH))));
					OverlayRender.submitItem(_poseStack, _itemStackToDraw, net.minecraft.client.renderer.LevelRenderer.getLightColor(Minecraft.getInstance().level, net.minecraft.core.BlockPos.containing(x, y, z)), OverlayTexture.NO_OVERLAY);
					_poseStack.popPose();
				}
			} else {
				if (event != null) {
					RenderLevelStageEvent _overlayEvent = event;
					float _scale = (float) 0.6;
					float _rotX = (float) 0;
					float _rotY = (float) 45;
					float _rotZ = (float) 0;
					float _bobHeight = Math.max(0, (float) 0);
					float _animTime = (float) (System.nanoTime() / 1_000_000L % 36000000L) / 50.0f;
					float _spinDegrees = _animTime * (float) 0;
					float _bob = (float) Math.sin(_animTime * 0.075f) * _bobHeight;
					double _offsetY = ((Number) 0).doubleValue() / 16.0;
					PoseStack _poseStack = _overlayEvent.getPoseStack();
					Vec3 _camera = _overlayEvent.getCamera().getPosition();
					_poseStack.pushPose();
					_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + 0.5 + _bob + _offsetY, z - _camera.z + 0.5);
					_poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(_spinDegrees));
					if (_rotX != 0.0F)
						_poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(_rotX));
					if (_rotY != 0.0F)
						_poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(_rotY));
					if (_rotZ != 0.0F)
						_poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(_rotZ));
					_poseStack.scale(_scale, _scale, _scale);
					
					final ItemStack _itemStackToDraw = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(((nbtString(world, BlockPos.containing(x, y, z), "resource_name"))).toLowerCase(java.util.Locale.ENGLISH))));
					OverlayRender.submitItem(_poseStack, _itemStackToDraw, net.minecraft.client.renderer.LevelRenderer.getLightColor(Minecraft.getInstance().level, net.minecraft.core.BlockPos.containing(x, y, z)), OverlayTexture.NO_OVERLAY);
					_poseStack.popPose();
				}
			}
		}
		if (event != null) {
					RenderLevelStageEvent _overlayEvent = event;
			String _placement = "TOP_CENTER";
			Direction _side = ((getBlockDirection(world, BlockPos.containing(x, y, z))));
			int _decimals = (int) Math.max(0, Math.min(0, 10));
			String _text = String.format(java.util.Locale.ROOT, "%." + _decimals + "f", (double) ((getInventoryLevel(world, BlockPos.containing(x, y, z)))));
			Font _font = Minecraft.getInstance().font;
			float _scale = Math.copySign(Math.min(0.025f * Math.abs((float) 0.4), Math.min(0.95f / Math.max(1, _font.width(_text)), 0.95f / Math.max(1, _font.lineHeight))), (float) 0.4);
			String _rawColor = "ffffff";
			long _parsedColor = Long.parseLong(_rawColor, 16);
			int _colorAlpha = (_rawColor.length() == 8) ? (int) ((_parsedColor >> 24) & 0xFF) : 255;
			int _alphaInt = (int) (Math.max(0.0f, Math.min(1.0f, (float) 1)) * (float) _colorAlpha);
			int _textColor = (_alphaInt << 24) | (int) (_parsedColor & 0x00FFFFFF);
			PoseStack _poseStack = _overlayEvent.getPoseStack();
			Vec3 _camera = _overlayEvent.getCamera().getPosition();
			double _padding = ((Number) 0.5).doubleValue() / 16.0;
			Vec3 _position = true
					? shapeBoundsPlacement(world.getBlockState(BlockPos.containing(x, y, z)), world, BlockPos.containing(x, y, z), _side, _placement, 0.0, 0.0, _padding)
					: new Vec3(anchorX(_placement) - 0.5 + placementOffsetX(_placement, _padding), anchorY(_placement) - 0.5 + placementOffsetY(_placement, _padding), -0.501);
			_poseStack.pushPose();
			_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + 0.5, z - _camera.z + 0.5);
			_poseStack.mulPose(faceRotation(_side));
			_poseStack.translate(_position.x, _position.y, _position.z);
			_poseStack.scale(-_scale, -_scale, _scale);
			OverlayRender.submitText(_poseStack, localX(_placement, _font.width(_text)), localY(_placement, _font.lineHeight), _text,
					net.minecraft.client.renderer.LevelRenderer.getLightColor(Minecraft.getInstance().level, net.minecraft.core.BlockPos.containing(x, y, z).relative(_side)), _textColor);
			_poseStack.popPose();
		}
		if (entity.isShiftKeyDown()) {
			if (entity.getMainHandItem().getItem() == SkyblockResourcesModItems.SCREWDRIVER.get()) {
				for (Direction directioniterator : outputSides(world, BlockPos.containing(x, y, z))) {
					if (event != null) {
					RenderLevelStageEvent _overlayEvent = event;
						String _placement = "MIDDLE_CENTER";
						Direction _side = directioniterator;
						float _scale = (float) 0.6;
						ResourceLocation _texture = new ResourceLocation("skyblock_resources:textures/screens/ouput.png");
						String _rawColor = "" + "#ffffffff";
						_rawColor = _rawColor.trim().replace("#", "").replace("0x", "").replace("0X", "").replace("\"", "");
						long _parsedColor = 0xFFFFFFFFL;
						try {
							_parsedColor = Long.parseLong(_rawColor, 16);
						} catch (Exception _e) {
							_parsedColor = 0xFFFFFFFFL;
						}
						int _rTint, _gTint, _bTint, _aTint;
						if (_rawColor.length() == 8) {
							_rTint = (int) ((_parsedColor >> 24) & 0xFF);
							_gTint = (int) ((_parsedColor >> 16) & 0xFF);
							_bTint = (int) ((_parsedColor >> 8) & 0xFF);
							_aTint = (int) (_parsedColor & 0xFF);
						} else {
							_rTint = (int) ((_parsedColor >> 16) & 0xFF);
							_gTint = (int) ((_parsedColor >> 8) & 0xFF);
							_bTint = (int) (_parsedColor & 0xFF);
							_aTint = 255;
						}
						boolean _useColor = true;
						float _tintAlpha = _useColor ? Math.max(0.0f, Math.min(1.0f, (float) _aTint / 255.0f)) : 0.0f;
						int _r = Math.max(0, Math.min(255, Math.round(255.0f * (1.0f - _tintAlpha) + (float) _rTint * _tintAlpha)));
						int _g = Math.max(0, Math.min(255, Math.round(255.0f * (1.0f - _tintAlpha) + (float) _gTint * _tintAlpha)));
						int _b = Math.max(0, Math.min(255, Math.round(255.0f * (1.0f - _tintAlpha) + (float) _bTint * _tintAlpha)));
						int _a = (int) (Math.max(0.0f, Math.min(1.0f, (float) 0.75)) * 255.0f);
						PoseStack _poseStack = _overlayEvent.getPoseStack();
						Vec3 _camera = _overlayEvent.getCamera().getPosition();
						double _padding = ((Number) 0).doubleValue() / 16.0;
						Vec3 _position = true
								? shapeBoundsPlacement(world.getBlockState(BlockPos.containing(x, y, z)), world, BlockPos.containing(x, y, z), _side, _placement, _scale, _scale, _padding)
								: new Vec3(itemPlacementX(_placement, _scale) + placementOffsetX(_placement, _padding), itemPlacementY(_placement, _scale) + placementOffsetY(_placement, _padding), -0.501);
						_poseStack.pushPose();
						_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + 0.5, z - _camera.z + 0.5);
						_poseStack.mulPose(faceRotation(_side));
						_poseStack.translate(_position.x, _position.y, _position.z);
						_poseStack.scale(_scale, _scale, 1.0f);
						boolean _flipWinding = _side == Direction.UP || _side == Direction.DOWN;
						OverlayRender.submitCustomGeometry(_poseStack, blockOverlayTextureRenderType(_texture), (_pose, _consumer) -> {
							if (_flipWinding) {
								_consumer.vertex(_pose.pose(), -0.5f, -0.5f, 0).color(_r, _g, _b, _a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(_pose.normal(), 0, 0, -1).endVertex();
								_consumer.vertex(_pose.pose(), -0.5f, 0.5f, 0).color(_r, _g, _b, _a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(_pose.normal(), 0, 0, -1).endVertex();
								_consumer.vertex(_pose.pose(), 0.5f, 0.5f, 0).color(_r, _g, _b, _a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(_pose.normal(), 0, 0, -1).endVertex();
								_consumer.vertex(_pose.pose(), 0.5f, -0.5f, 0).color(_r, _g, _b, _a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(_pose.normal(), 0, 0, -1).endVertex();
							} else {
								_consumer.vertex(_pose.pose(), 0.5f, -0.5f, 0).color(_r, _g, _b, _a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(_pose.normal(), 0, 0, -1).endVertex();
								_consumer.vertex(_pose.pose(), 0.5f, 0.5f, 0).color(_r, _g, _b, _a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(_pose.normal(), 0, 0, -1).endVertex();
								_consumer.vertex(_pose.pose(), -0.5f, 0.5f, 0).color(_r, _g, _b, _a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(_pose.normal(), 0, 0, -1).endVertex();
								_consumer.vertex(_pose.pose(), -0.5f, -0.5f, 0).color(_r, _g, _b, _a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(_pose.normal(), 0, 0, -1).endVertex();
							}
						});
						_poseStack.popPose();
					}
				}
			}
		}
	}

	private static List<Direction> outputSides(LevelAccessor world, BlockPos pos) {
		Direction side = Direction.byName(nbtString(world, pos, "output_direction").toLowerCase(java.util.Locale.ENGLISH));
		return side == null ? List.of() : List.of(side);
	}

	private static String nbtString(LevelAccessor world, BlockPos pos, String path) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity == null)
			return "";
		CompoundTag current = blockEntity.getPersistentData();
		String[] parts = path.split("[./]");
		for (int i = 0; i < parts.length - 1; i++) {
			String part = parts[i].trim();
			if (part.isEmpty())
				continue;
			if (current.get(part) instanceof CompoundTag sub) {
				current = sub;
			} else {
				return "";
			}
		}
		return NbtCompat.getStringOr(current, parts[parts.length - 1].trim(), "");
	}
}
