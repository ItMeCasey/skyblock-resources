package com.itmecasey.skyblockresources;


import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

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
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.item.ItemStackRenderState;
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
	private static final com.mojang.blaze3d.pipeline.RenderPipeline BLOCK_OVERLAY_TEXTURE_PIPELINE = com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.ENTITY_SNIPPET)
			.withLocation("blockoverlays_texture_pipeline_showgeneratorresource").withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING").withColorTargetState(new com.mojang.blaze3d.pipeline.ColorTargetState(com.mojang.blaze3d.pipeline.BlendFunction.TRANSLUCENT)).withCull(false).build();
	// Opaque counterpart of the pipeline above (no BlendFunction override = opaque, ALPHA_CUTOUT for
	// the texture's own transparent pixels instead of smooth blending, no sortOnUpload needed since
	// opaque geometry doesn't need depth sorting). Translucent rendering carries real, well-known
	// fixed GPU overhead in Minecraft's pipeline (its own sorted render pass) independent of how much
	// geometry uses it - when the overlay doesn't actually need transparency (the common case), this
	// avoids that cost entirely, matching the same class of render path every other overlay type uses.
	private static final com.mojang.blaze3d.pipeline.RenderPipeline BLOCK_OVERLAY_TEXTURE_OPAQUE_PIPELINE = com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.ENTITY_SNIPPET)
			.withLocation("blockoverlays_texture_opaque_pipeline_showgeneratorresource").withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("NO_OVERLAY")
			.withShaderDefine("NO_CARDINAL_LIGHTING").withCull(false).build();

	@SubscribeEvent
	public static void onRegisterBlockOverlayPipelines(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
		event.registerPipeline(BLOCK_OVERLAY_TEXTURE_PIPELINE);
		event.registerPipeline(BLOCK_OVERLAY_TEXTURE_OPAQUE_PIPELINE);
	}

	// Building a RenderSetup (and its internal texture-binding map) is not free, and unlike vanilla's
	// own RenderType constants (e.g. RenderTypes.entityTranslucent), this one was being rebuilt from
	// scratch on every single submission - every frame, for every matching target block. Cache it
	// per texture instead, since it never actually changes once built.
	private static Identifier BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_TEXTURE = null;
	private static net.minecraft.client.renderer.rendertype.RenderType BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_CACHED = null;
	private static Identifier BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_TEXTURE = null;
	private static net.minecraft.client.renderer.rendertype.RenderType BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_CACHED = null;

	private static net.minecraft.client.renderer.rendertype.RenderType blockOverlayTextureRenderType(Identifier texture) {
		if (BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_CACHED == null || !texture.equals(BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_TEXTURE)) {
			BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_TEXTURE = texture;
			BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_CACHED = net.minecraft.client.renderer.rendertype.RenderType.create("blockoverlays_texture_showgeneratorresource",
					net.minecraft.client.renderer.rendertype.RenderSetup.builder(BLOCK_OVERLAY_TEXTURE_PIPELINE).withTexture("Sampler0", texture).useLightmap().sortOnUpload().createRenderSetup());
		}
		return BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_CACHED;
	}

	private static net.minecraft.client.renderer.rendertype.RenderType blockOverlayTextureRenderTypeOpaque(Identifier texture) {
		if (BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_CACHED == null || !texture.equals(BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_TEXTURE)) {
			BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_TEXTURE = texture;
			BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_CACHED = net.minecraft.client.renderer.rendertype.RenderType.create("blockoverlays_texture_opaque_showgeneratorresource",
					net.minecraft.client.renderer.rendertype.RenderSetup.builder(BLOCK_OVERLAY_TEXTURE_OPAQUE_PIPELINE).withTexture("Sampler0", texture).useLightmap().createRenderSetup());
		}
		return BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_CACHED;
	}

	private static java.util.Set<net.minecraft.world.level.block.Block> TARGET_BLOCKS = null;

	private static boolean matchesTarget(BlockState state) {
		if (TARGET_BLOCKS == null) {
			java.util.Set<net.minecraft.world.level.block.Block> set = new HashSet<>();
			for (String bid : java.util.List.of("skyblock_resources:wooden_generator", "skyblock_resources:stone_generator", "skyblock_resources:iron_generator", "skyblock_resources:diamond_generator", "skyblock_resources:netherite_generator")) {
				var b = BuiltInRegistries.BLOCK.getValue(Identifier.parse(bid));
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

	private static net.minecraft.nbt.CompoundTag parseNbt(String snbt) {
		try {
			return net.minecraft.nbt.TagParser.parseCompoundFully(snbt);
		} catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
			return null;
		}
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

	private static Vec3 shapeBoundsPrecise(BlockState state, Level level, BlockPos position, Direction side, double coordX, double coordY) {
		return shapeBoundsPrecise(state, level, position, side, coordX, coordY, 0.0);
	}

	private static Vec3 shapeBoundsPrecise(BlockState state, Level level, BlockPos position, Direction side, double coordX, double coordY, double padding) {
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
		double h = horizontalMaximum - (coordX / 16.0) * hSpan;
		double v = verticalMinimum + (coordY / 16.0) * vSpan;
		double offsetX = coordX < 8.0 ? -padding : coordX > 8.0 ? padding : 0.0;
		double offsetY = coordY > 8.0 ? -padding : coordY < 8.0 ? padding : 0.0;
		return new Vec3(h - 0.5 + offsetX, v - 0.5 + offsetY, depth);
	}

	// Helper for overlay_builder_outline: emit N parallel copies of one shape edge, each offset
	// along the face plane(s) the edge sits on. Width=1 -> single edge on the original line.
	// Width>1 -> additional parallel edges extending INTO the faces it touches (not outwards
	// from the block). Caller is responsible for translating the pose stack into shape space.
	private static void blockOverlayOutlineDrawEdgeBand(com.mojang.blaze3d.vertex.VertexConsumer consumer, org.joml.Matrix4f pose, org.joml.Matrix3f normal, int color, int width, float step, double x1, double y1, double z1, double x2, double y2,
			double z2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;
		// Dominant axis -> which way the edge runs.
		int edgeAxis;
		if (Math.abs(dx) >= Math.abs(dy) && Math.abs(dx) >= Math.abs(dz))
			edgeAxis = 0;
		else if (Math.abs(dy) >= Math.abs(dx) && Math.abs(dy) >= Math.abs(dz))
			edgeAxis = 1;
		else
			edgeAxis = 2;
		double mx = (x1 + x2) * 0.5;
		double my = (y1 + y2) * 0.5;
		double mz = (z1 + z2) * 0.5;
		float nx = 0, ny = 0, nz = 0;
		switch (edgeAxis) {
			case 0 :
				ny = (my < 0.5) ? -1.0f : 1.0f;
				nz = (mz < 0.5) ? -1.0f : 1.0f;
				break;
			case 1 :
				nx = (mx < 0.5) ? -1.0f : 1.0f;
				nz = (mz < 0.5) ? -1.0f : 1.0f;
				break;
			case 2 :
				nx = (mx < 0.5) ? -1.0f : 1.0f;
				ny = (my < 0.5) ? -1.0f : 1.0f;
				break;
		}
		for (int k = 0; k < width; k++) {
			float o = k * step;
			float ox = nx * o, oy = ny * o, oz = nz * o;
			float ax = (float) x1 + ox, ay = (float) y1 + oy, az = (float) z1 + oz;
			float bx = (float) x2 + ox, by = (float) y2 + oy, bz = (float) z2 + oz;
			consumer.addVertex(pose, ax, ay, az).setColor(color).setNormal((float) dx, (float) dy, (float) dz);
			consumer.addVertex(pose, bx, by, bz).setColor(color).setNormal((float) dx, (float) dy, (float) dz);
		}
	}

	private static CompoundTag getBlockDirectionsData(BlockEntity be) {
		if (be == null)
			return null;
		CompoundTag tag = be.getPersistentData();
		if (!tag.contains("BlockDirections")) {
			tag.put("BlockDirections", new CompoundTag());
		}
		return tag.getCompoundOrEmpty("BlockDirections");
	}

	private static void setSideMode(LevelAccessor level, BlockPos pos, Direction dir, String type, String mode) {
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

	private static void setAllSidesMode(LevelAccessor level, BlockPos pos, String type, String mode) {
		for (Direction dir : Direction.values()) {
			setSideMode(level, pos, dir, type, mode);
		}
	}

	// True if the block at `pos` actually exposes the capability for `type`.
	// ITEM / FLUID / ENERGY require a real capability. REDSTONE has no capability
	// in NeoForge (signal is queried via Level.getSignal) so we treat it as
	// always-present — the user's procedure blocks are the source of truth.
	// 26.x renamed the nested capability types: Item / Fluid / Energy instead of
	// ItemHandler / FluidHandler / EnergyStorage.
	private static boolean hasCapability(LevelAccessor level, BlockPos pos, String type) {
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

	// True if the user has called setBlockSideMode for this exact (type, dir,
	// in/out) entry. Used by the iterator to filter INPUT_ONLY / OUTPUT_ONLY /
	// DENY_ALL to only the sides the user has explicitly configured.
	private static boolean isSideConfigured(LevelAccessor level, BlockPos pos, Direction dir, String type, boolean isInput) {
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

	// Strict per-side allow check used by the push/pull helpers and the iterator's
	// ALLOW_ALL mode. Returns true only when the block has the capability AND the
	// user has explicitly enabled this (type, dir, in/out) combination. Unconfigured
	// sides return false even if the block declares the capability.
	private static boolean isSideAllowed(LevelAccessor level, BlockPos pos, Direction dir, String type, boolean isInput) {
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

	private static String getSideMode(LevelAccessor level, BlockPos pos, Direction dir, String type) {
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

	private static List<Direction> getMatchingSides(LevelAccessor level, BlockPos pos, String type, String mode) {
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

	private static int countMatchingSides(LevelAccessor level, BlockPos pos, String type, String mode) {
		return getMatchingSides(level, pos, type, mode).size();
	}

	private static void copySideConfig(LevelAccessor level, BlockPos fromPos, BlockPos toPos) {
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

	private static void resetSideConfig(LevelAccessor level, BlockPos pos) {
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

	private static void setRedstonePower(LevelAccessor level, BlockPos pos, Direction dir, int power) {
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

	private static int getRedstonePower(LevelAccessor level, BlockPos pos, Direction dir) {
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

	private static int pushEnergyToSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
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

	private static int pullEnergyFromSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
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

	private static int pushFluidToSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
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

	private static int pullFluidFromSide(LevelAccessor level, BlockPos pos, Direction dir, int maxAmount) {
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
							// Put back what the target did not take, or the difference would vanish when the transaction commits.
							if (inserted < extracted)
								src.insert(slot, res, extracted - inserted, tx);
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
							// Put back what the target did not take, or the difference would vanish when the transaction commits.
							if (inserted < extracted)
								src.insert(s, res, extracted - inserted, tx);
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

	private static int pullItemFromSide(LevelAccessor level, BlockPos pos, Direction dir, int slot, int maxAmount) {
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

	// Inventory query helpers. These inspect the LOCAL block's own item handler
	// (i.e. the block at `pos`, not a neighbor). Transfer API (ResourceHandler).
	private static int getInventoryLevel(LevelAccessor level, BlockPos pos) {
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

	private static int getMaxInventoryLevel(LevelAccessor level, BlockPos pos) {
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
	// The item is an Item reference, so it is wrapped in an ItemStack before
	// handing it to the ItemResource API.
	// slot = -1 means first-fit. Returns the number of items actually placed.
	private static int insertItemIntoSide(LevelAccessor level, BlockPos pos, int slot, Item template, int amount) {
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

	// "Place into player inventory" helper: extracts up to `amount` items from
	// the block's inventory at `pos` (slot = -1 for any/first non-empty slot),
	// places them into the player's inventory, and shrinks/removes the items
	// from the block's inventory by the count actually transferred.
	// Returns the number of items successfully placed into the player's inventory.
	private static int placeIntoPlayerInventory(LevelAccessor level, BlockPos pos, int slot, net.minecraft.world.entity.Entity entity, int amount) {
		if (level.isClientSide() || amount <= 0 || !(entity instanceof net.minecraft.world.entity.player.Player player))
			return 0;
		// Direct Container access for blocks with an inventory
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

	// "Insert fluid" helper: inserts up to `amount` mB of `fluid` directly
	// into the block at `pos` (attempts null/internal capability first, then falls
	// back to any side). Returns the amount in mB actually inserted.
	private static int insertFluidIntoSide(LevelAccessor level, BlockPos pos, net.minecraft.world.level.material.Fluid fluid, int amount) {
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

	// "Insert energy" helper: inserts up to `amount` energy directly
	// into the block at `pos` (attempts null/internal capability first, then falls
	// back to any side). Returns the amount of energy actually inserted.
	private static int insertEnergyIntoSide(LevelAccessor level, BlockPos pos, int amount) {
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
	public static void render(SubmitCustomGeometryEvent event) {
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
	}

	private static void renderAt(SubmitCustomGeometryEvent event, Minecraft minecraft, BlockPos position) {
		if (!matchesTarget(minecraft.level.getBlockState(position)))
			return;
		var world = minecraft.level;
		var entity = minecraft.player;
		double x = position.getX();
		double y = position.getY();
		double z = position.getZ();
		if (!((nbtString(world, BlockPos.containing(x, y, z), "resource_name")).equals("empty"))) {
			if (!isInventoryFull(world, BlockPos.containing(x, y, z))) {
				if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
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
					Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
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
					ItemStackRenderState _itemState = new ItemStackRenderState();
					Minecraft.getInstance().getItemModelResolver().updateForTopItem(_itemState, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(((nbtString(world, BlockPos.containing(x, y, z), "resource_name"))).toLowerCase(java.util.Locale.ENGLISH)))), ItemDisplayContext.FIXED, Minecraft.getInstance().level, null, 0);
					_itemState.submit(_poseStack, _overlayEvent.getSubmitNodeCollector(), net.minecraft.util.LightCoordsUtil.getLightCoords(Minecraft.getInstance().level, net.minecraft.core.BlockPos.containing(x, y, z)),
							OverlayTexture.NO_OVERLAY, 0);
					_poseStack.popPose();
				}
			} else {
				if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
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
					Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
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
					ItemStackRenderState _itemState = new ItemStackRenderState();
					Minecraft.getInstance().getItemModelResolver().updateForTopItem(_itemState, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(((nbtString(world, BlockPos.containing(x, y, z), "resource_name"))).toLowerCase(java.util.Locale.ENGLISH)))), ItemDisplayContext.FIXED, Minecraft.getInstance().level, null, 0);
					_itemState.submit(_poseStack, _overlayEvent.getSubmitNodeCollector(), net.minecraft.util.LightCoordsUtil.getLightCoords(Minecraft.getInstance().level, net.minecraft.core.BlockPos.containing(x, y, z)),
							OverlayTexture.NO_OVERLAY, 0);
					_poseStack.popPose();
				}
			}
		}
		if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
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
			Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
			double _padding = ((Number) 0.5).doubleValue() / 16.0;
			Vec3 _position = true
					? shapeBoundsPlacement(world.getBlockState(BlockPos.containing(x, y, z)), world, BlockPos.containing(x, y, z), _side, _placement, 0.0, 0.0, _padding)
					: new Vec3(anchorX(_placement) - 0.5 + placementOffsetX(_placement, _padding), anchorY(_placement) - 0.5 + placementOffsetY(_placement, _padding), -0.501);
			_poseStack.pushPose();
			_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + 0.5, z - _camera.z + 0.5);
			_poseStack.mulPose(faceRotation(_side));
			_poseStack.translate(_position.x, _position.y, _position.z);
			_poseStack.scale(-_scale, -_scale, _scale);
			_overlayEvent.getSubmitNodeCollector().submitText(_poseStack, localX(_placement, _font.width(_text)), localY(_placement, _font.lineHeight), Component.literal(_text).getVisualOrderText(), false, Font.DisplayMode.NORMAL,
					net.minecraft.util.LightCoordsUtil.getLightCoords(Minecraft.getInstance().level, net.minecraft.core.BlockPos.containing(x, y, z).relative(_side)), _textColor, 0, 0);
			_poseStack.popPose();
		}
		if (entity.isShiftKeyDown()) {
			if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SkyblockResourcesModItems.SCREWDRIVER.get()) {
				for (Direction directioniterator : outputSides(world, BlockPos.containing(x, y, z))) {
					if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
						String _placement = "MIDDLE_CENTER";
						Direction _side = directioniterator;
						float _scale = (float) 0.6;
						Identifier _texture = Identifier.parse("skyblock_resources:textures/screens/ouput.png");
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
						Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
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
						_overlayEvent.getSubmitNodeCollector().submitCustomGeometry(_poseStack, blockOverlayTextureRenderType(_texture), (_pose, _consumer) -> {
							if (_flipWinding) {
								_consumer.addVertex(_pose, -0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(_pose, 0, 0, -1);
							} else {
								_consumer.addVertex(_pose, 0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(_pose, 0, 0, -1);
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
		return current.getStringOr(parts[parts.length - 1].trim(), "");
	}
}
