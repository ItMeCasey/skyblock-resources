package com.itmecasey.skyblockresources;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.Minecraft;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;

@EventBusSubscriber(value = Dist.CLIENT)
public class CropGeneratorOverlay {
	private static final com.mojang.blaze3d.pipeline.RenderPipeline BLOCK_OVERLAY_TEXTURE_PIPELINE = com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.ENTITY_SNIPPET)
			.withLocation("blockoverlays_texture_pipeline_cropgeneratoroverlay").withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING").withColorTargetState(new com.mojang.blaze3d.pipeline.ColorTargetState(com.mojang.blaze3d.pipeline.BlendFunction.TRANSLUCENT)).withCull(false).build();
	// Opaque counterpart of the pipeline above (no BlendFunction override = opaque, ALPHA_CUTOUT for
	// the texture's own transparent pixels instead of smooth blending, no sortOnUpload needed since
	// opaque geometry doesn't need depth sorting). Translucent rendering carries real, well-known
	// fixed GPU overhead in Minecraft's pipeline (its own sorted render pass) independent of how much
	// geometry uses it - when the overlay doesn't actually need transparency (the common case), this
	// avoids that cost entirely, matching the same class of render path every other overlay type uses.
	private static final com.mojang.blaze3d.pipeline.RenderPipeline BLOCK_OVERLAY_TEXTURE_OPAQUE_PIPELINE = com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.ENTITY_SNIPPET)
			.withLocation("blockoverlays_texture_opaque_pipeline_cropgeneratoroverlay").withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("NO_OVERLAY")
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
			BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_CACHED = net.minecraft.client.renderer.rendertype.RenderType.create("blockoverlays_texture_cropgeneratoroverlay",
					net.minecraft.client.renderer.rendertype.RenderSetup.builder(BLOCK_OVERLAY_TEXTURE_PIPELINE).withTexture("Sampler0", texture).useLightmap().sortOnUpload().createRenderSetup());
		}
		return BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_CACHED;
	}

	private static net.minecraft.client.renderer.rendertype.RenderType blockOverlayTextureRenderTypeOpaque(Identifier texture) {
		if (BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_CACHED == null || !texture.equals(BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_TEXTURE)) {
			BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_TEXTURE = texture;
			BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_CACHED = net.minecraft.client.renderer.rendertype.RenderType.create("blockoverlays_texture_opaque_cropgeneratoroverlay",
					net.minecraft.client.renderer.rendertype.RenderSetup.builder(BLOCK_OVERLAY_TEXTURE_OPAQUE_PIPELINE).withTexture("Sampler0", texture).useLightmap().createRenderSetup());
		}
		return BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_CACHED;
	}

	private static java.util.Set<net.minecraft.world.level.block.Block> TARGET_BLOCKS = null;

	private static boolean matchesTarget(BlockState state) {
		if (TARGET_BLOCKS == null) {
			java.util.Set<net.minecraft.world.level.block.Block> set = new HashSet<>();
			for (String bid : java.util.List.of("skyblock_resources:wooden_crop_generator", "skyblock_resources:stone_crop_generator", "skyblock_resources:iron_crop_generator", "skyblock_resources:diamond_crop_generator", "skyblock_resources:netherite_crop_generator")) {
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

	// Item->BlockState resolution (a long chain of comparisons/registry lookups) and block model part
	// collection are pure functions of their inputs - they never need to be redone every render frame,
	// only when the actual input (item / resolved state) changes. Redoing them every frame for every
	// visible tree/crop overlay was a real, uncached cost, independent of anything else in this plugin.
	//
	// Model parts are cached globally by BlockState (not by position) since a state's model never
	// depends on where it's rendered, and BlockState instances are canonical/interned so this is a safe
	// map key. Item->state resolution is similarly cached by Item. Neither needs a TTL or level
	// invalidation the way the texture overlay's cached TextureAtlasSprite did, since these don't hold
	// anything tied to a specific world/atlas instance across reloads - only to static block/model
	// registrations for the JVM session.
	private static final java.util.Map<net.minecraft.world.level.block.state.BlockState, java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>> BLOCK_OVERLAY_MODEL_PARTS_CACHE = new java.util.HashMap<>();

	private static java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> blockOverlayGetModelParts(net.minecraft.world.level.block.state.BlockState state) {
		return BLOCK_OVERLAY_MODEL_PARTS_CACHE.computeIfAbsent(state, s -> {
			var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(s);
			java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts = new java.util.ArrayList<>();
			model.collectParts(net.minecraft.util.RandomSource.create(42L), parts);
			return parts;
		});
	}

	// Only the pure item->state resolution chain is cached here (not the world-position fallback used
	// when an item isn't recognized at all, since that legitimately depends on the live world block).
	private static final java.util.Map<Item, net.minecraft.world.level.block.state.BlockState> BLOCK_OVERLAY_TREE_RESOLUTION_CACHE = new java.util.HashMap<>();

	private static net.minecraft.world.level.block.state.@org.jspecify.annotations.Nullable BlockState blockOverlayGetCachedTreeResolution(Item item,
			java.util.function.Supplier<net.minecraft.world.level.block.state.@org.jspecify.annotations.Nullable BlockState> resolve) {
		if (BLOCK_OVERLAY_TREE_RESOLUTION_CACHE.containsKey(item))
			return BLOCK_OVERLAY_TREE_RESOLUTION_CACHE.get(item);
		var resolved = resolve.get();
		BLOCK_OVERLAY_TREE_RESOLUTION_CACHE.put(item, resolved);
		return resolved;
	}

	private static final java.util.Map<Item, net.minecraft.world.level.block.state.BlockState> BLOCK_OVERLAY_CROP_RESOLUTION_CACHE = new java.util.HashMap<>();

	private static net.minecraft.world.level.block.state.@org.jspecify.annotations.Nullable BlockState blockOverlayGetCachedCropResolution(Item item,
			java.util.function.Supplier<net.minecraft.world.level.block.state.@org.jspecify.annotations.Nullable BlockState> resolve) {
		if (BLOCK_OVERLAY_CROP_RESOLUTION_CACHE.containsKey(item))
			return BLOCK_OVERLAY_CROP_RESOLUTION_CACHE.get(item);
		var resolved = resolve.get();
		BLOCK_OVERLAY_CROP_RESOLUTION_CACHE.put(item, resolved);
		return resolved;
	}

	// Shape info (double-tall second half, multi-stage age/growth property range) depends only on the
	// final resolved BlockState, whether that came from the cached item resolution above or the live
	// world-position fallback - so it's cached separately, keyed by state, and works correctly either way.
	private record BlockOverlayCropShapeInfo(net.minecraft.world.level.block.state.@org.jspecify.annotations.Nullable BlockState secondBaseState, boolean isVisualMultiStage,
			net.minecraft.world.level.block.state.properties.@org.jspecify.annotations.Nullable IntegerProperty ageProp, int minAge, int maxAge, int totalStages) {
	}

	private static final java.util.Map<net.minecraft.world.level.block.state.BlockState, BlockOverlayCropShapeInfo> BLOCK_OVERLAY_CROP_SHAPE_CACHE = new java.util.HashMap<>();

	private static BlockOverlayCropShapeInfo blockOverlayGetCachedCropShapeInfo(net.minecraft.world.level.block.state.BlockState state, java.util.function.Supplier<BlockOverlayCropShapeInfo> compute) {
		return BLOCK_OVERLAY_CROP_SHAPE_CACHE.computeIfAbsent(state, s -> compute.get());
	}

	// The sprite lookup (collectParts + quad scan) and light lookup are non-trivial CPU work; redoing
	// them every render frame for a texture overlay that basically never changes was the actual cause
	// of a large FPS hit even with just a single instance. Refresh periodically instead.
	//
	// Kept short (not the original 60s) because right after a block is placed or a chunk loads, the
	// model/lighting system can momentarily return a transient wrong answer (missing-texture sprite
	// before the model is ready, or a too-dark/wrong light value before the lighting engine has
	// propagated) - a long TTL would freeze that wrong answer for a long time instead of it
	// self-correcting within a frame or two like it did before caching existed. A couple of seconds
	// still eliminates the vast majority of the original per-frame cost while keeping any transient
	// glitch barely noticeable.
	//
	// Cache is invalidated whenever the level instance changes (new world/dimension/reload) - without
	// this, a cached TextureAtlasSprite from a previous world's (possibly since-rebuilt) texture atlas
	// could be served as "still fresh" and render as garbage/invisible, and using world.getGameTime()
	// (which resets on a new world) as the freshness clock could make an old entry look falsely fresh
	// via a negative time delta. System.currentTimeMillis() never goes backward within a session.
	private static final long BLOCK_OVERLAY_TEXTURE_CACHE_INTERVAL_MS = 15_000L;

	private record BlockOverlayTextureCacheEntry(long timestampMs, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite, int light) {
	}

	private static final java.util.Map<Long, BlockOverlayTextureCacheEntry> BLOCK_OVERLAY_TEXTURE_CACHE = new java.util.HashMap<>();
	private static net.minecraft.world.level.Level BLOCK_OVERLAY_TEXTURE_CACHE_LEVEL = null;

	private static BlockOverlayTextureCacheEntry blockOverlayGetTextureCacheEntry(net.minecraft.world.level.Level level, long key, java.util.function.Supplier<BlockOverlayTextureCacheEntry> compute) {
		if (level != BLOCK_OVERLAY_TEXTURE_CACHE_LEVEL) {
			BLOCK_OVERLAY_TEXTURE_CACHE.clear();
			BLOCK_OVERLAY_TEXTURE_CACHE_LEVEL = level;
		}
		long now = System.currentTimeMillis();
		var cached = BLOCK_OVERLAY_TEXTURE_CACHE.get(key);
		if (cached != null && now - cached.timestampMs() < BLOCK_OVERLAY_TEXTURE_CACHE_INTERVAL_MS)
			return cached;
		BlockOverlayTextureCacheEntry fresh = compute.get();
		// The "missing texture" placeholder almost never means the user actually wants that sprite - it
		// means the model system wasn't ready yet (e.g. right after a block was placed). Don't let that
		// transient result get locked in for the normal TTL: store it but with a timestamp of 0 so the
		// very next frame's check sees it as already-expired and retries immediately, until a real
		// texture resolves.
		boolean _isMissing = fresh.sprite().contents().name().equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation());
		BLOCK_OVERLAY_TEXTURE_CACHE.put(key, _isMissing ? new BlockOverlayTextureCacheEntry(0L, fresh.sprite(), fresh.light()) : fresh);
		return fresh;
	}

	@SubscribeEvent
	public static void render(SubmitCustomGeometryEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null)
			return;
		Vec3 playerPos = minecraft.player.position();
		double maxDist = 16D;
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
			if ((nbtString(world, BlockPos.containing(x, y, z), "soil_type")).equals("minecraft:water") && !((nbtString(world, BlockPos.containing(x, y, z), "resource_name")).equals("minecraft:lily_pad"))) {
				if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
					float _growth = Math.max(0.0f, Math.min(1.0f, (float) ((Number) (0.85 * com.itmecasey.skyblockresources.machine.MachineHelpers.progress(world, BlockPos.containing(x, y, z)))).doubleValue()));
					double _offsetY = ((Number) (-3)).doubleValue() / 16.0;
					float _baseScale = (float) ((Number) 0.65).doubleValue();
					ItemStack _cropStack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(((nbtString(world, BlockPos.containing(x, y, z), "resource_name"))).toLowerCase(java.util.Locale.ENGLISH))));
					Item _cropItem = _cropStack.getItem();
					BlockState _cropState = blockOverlayGetCachedCropResolution(_cropItem, () -> {
						BlockState _resolved = null;
						if (_cropItem == net.minecraft.world.item.Items.WHEAT_SEEDS || _cropItem == net.minecraft.world.item.Items.WHEAT) {
							_resolved = net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.CARROT) {
							_resolved = net.minecraft.world.level.block.Blocks.CARROTS.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.POTATO) {
							_resolved = net.minecraft.world.level.block.Blocks.POTATOES.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.BEETROOT_SEEDS || _cropItem == net.minecraft.world.item.Items.BEETROOT) {
							_resolved = net.minecraft.world.level.block.Blocks.BEETROOTS.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.PUMPKIN_SEEDS || _cropItem == net.minecraft.world.item.Items.PUMPKIN || _cropItem == net.minecraft.world.item.Items.CARVED_PUMPKIN) {
							_resolved = net.minecraft.world.level.block.Blocks.PUMPKIN.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.MELON_SEEDS || _cropItem == net.minecraft.world.item.Items.MELON || _cropItem == net.minecraft.world.item.Items.MELON_SLICE) {
							_resolved = net.minecraft.world.level.block.Blocks.MELON.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.NETHER_WART) {
							_resolved = net.minecraft.world.level.block.Blocks.NETHER_WART.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.COCOA_BEANS) {
							_resolved = net.minecraft.world.level.block.Blocks.COCOA.defaultBlockState().setValue(net.minecraft.world.level.block.CocoaBlock.AGE, 2);
						} else if (_cropItem == net.minecraft.world.item.Items.SWEET_BERRIES) {
							_resolved = net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.GLOW_BERRIES) {
							_resolved = net.minecraft.world.level.block.Blocks.CAVE_VINES.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.TORCHFLOWER_SEEDS) {
							_resolved = net.minecraft.world.level.block.Blocks.TORCHFLOWER_CROP.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.PITCHER_POD) {
							_resolved = net.minecraft.world.level.block.Blocks.PITCHER_CROP.defaultBlockState();
						} else if (_cropItem instanceof net.minecraft.world.item.BlockItem _bi) {
							_resolved = _bi.getBlock().defaultBlockState();
						} else {
							var _itemLoc = BuiltInRegistries.ITEM.getKey(_cropItem);
							if (_itemLoc != null) {
								String _path = _itemLoc.getPath();
								String _basePath = _path.endsWith("_seeds") ? _path.substring(0, _path.length() - 6) : _path.endsWith("_seed") ? _path.substring(0, _path.length() - 5) : _path;
								var _block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(_itemLoc.getNamespace() + ":" + _basePath));
								if (_block == null || _block == net.minecraft.world.level.block.Blocks.AIR) {
									_block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(_itemLoc.getNamespace() + ":" + _basePath + "_crop"));
								}
								if (_block == null || _block == net.minecraft.world.level.block.Blocks.AIR) {
									_block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(_itemLoc.getNamespace() + ":" + _basePath + "_plant"));
								}
								if (_block != null && _block != net.minecraft.world.level.block.Blocks.AIR) {
									_resolved = _block.defaultBlockState();
								}
							}
						}
						return _resolved;
					});
					if (_cropState == null || _cropState.isAir()) {
						_cropState = world.getBlockState(BlockPos.containing(x, y, z));
						if (_cropState.isAir())
							_cropState = world.getBlockState(BlockPos.containing(x, y + 1, z));
						if (_cropState.isAir())
							_cropState = net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState();
					}
					if (_cropState.is(net.minecraft.world.level.block.Blocks.COCOA)) {
						_cropState = _cropState.setValue(net.minecraft.world.level.block.CocoaBlock.AGE, 2);
					}
					float _scaleX = _baseScale;
					float _scaleY = _baseScale;
					float _scaleZ = _baseScale;
					// Double-tall detection and multi-stage age/growth property range depend only on the final
					// resolved state, so this is cached by state - correct whether _cropState came from the cached
					// item resolution above or the live world-position fallback.
					final BlockState _shapeStateKey = _cropState;
					var _shapeInfo = blockOverlayGetCachedCropShapeInfo(_cropState, () -> {
						BlockState _s = _shapeStateKey;
						BlockState _second = null;
						if (_s.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)) {
							_s = _s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
							_second = _s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
						}
						boolean _isVisualMultiStage = false;
						net.minecraft.world.level.block.state.properties.IntegerProperty _ageProp = null;
						int _minAge = 0;
						int _maxAge = 7;
						// Only true multi-stage visual crops extrapolate discrete growth stages from percentage
						if (_shapeStateKey.getBlock() instanceof net.minecraft.world.level.block.CropBlock || _shapeStateKey.getBlock() instanceof net.minecraft.world.level.block.NetherWartBlock
								|| _shapeStateKey.getBlock() instanceof net.minecraft.world.level.block.SweetBerryBushBlock || _shapeStateKey.is(net.minecraft.world.level.block.Blocks.TORCHFLOWER_CROP)
								|| _shapeStateKey.is(net.minecraft.world.level.block.Blocks.PITCHER_CROP) || _shapeStateKey.is(net.minecraft.world.level.block.Blocks.PUMPKIN_STEM)
								|| _shapeStateKey.is(net.minecraft.world.level.block.Blocks.MELON_STEM)) {
							for (net.minecraft.world.level.block.state.properties.Property<?> _p : _shapeStateKey.getProperties()) {
								if (_p instanceof net.minecraft.world.level.block.state.properties.IntegerProperty _ip && (_ip.getName().equals("age") || _ip.getName().equals("stage") || _ip.getName().equals("growth"))) {
									_ageProp = _ip;
									_isVisualMultiStage = true;
									break;
								}
							}
						}
						if (_ageProp != null) {
							_minAge = _ageProp.getPossibleValues().stream().mapToInt(Integer::intValue).min().orElse(0);
							_maxAge = _ageProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(7);
						}
						return new BlockOverlayCropShapeInfo(_second, _isVisualMultiStage, _ageProp, _minAge, _maxAge, _maxAge - _minAge + 1);
					});
					BlockState _secondState = null;
					double _secondOffsetY = 0.0;
					float _secondScaleY = _baseScale;
					// Special Case: Double-tall plants (Rose Bush, Sunflower, Peony, Pitcher Crop, etc.)
					if (_shapeInfo.secondBaseState() != null) {
						float _growthScale = Math.max(0.001f, _growth);
						_scaleX = _baseScale * _growthScale;
						_scaleY = _baseScale * _growthScale;
						_scaleZ = _baseScale * _growthScale;
						_cropState = _cropState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
						_secondState = _shapeInfo.secondBaseState();
						_secondOffsetY = (double) _scaleY;
						_secondScaleY = _scaleY;
					} else if (_shapeInfo.isVisualMultiStage() && _shapeInfo.ageProp() != null) {
						int _calculatedStage = (int) Math.floor(_growth * _shapeInfo.totalStages());
						if (_calculatedStage >= _shapeInfo.totalStages())
							_calculatedStage = _shapeInfo.totalStages() - 1;
						int _targetAge = _shapeInfo.minAge() + _calculatedStage;
						_cropState = _cropState.setValue(_shapeInfo.ageProp(), _targetAge);
					} else {
						// Single-stage crops (Cactus, Sugar Cane, Bamboo, Kelp, Gourds like Pumpkin/Melon, Flowers, Saplings, etc.):
						// Percentage complete directly scales the block from 0 to 1 of maximum size (scale)
						float _growthScale = Math.max(0.001f, _growth);
						_scaleX *= _growthScale;
						_scaleY *= _growthScale;
						_scaleZ *= _growthScale;
					}
					// Shape bounds calculation
					double _shapeTop = 0.0;
					if (true) {
						var _shape = world.getBlockState(BlockPos.containing(x, y, z)).getShape(world, BlockPos.containing(x, y, z));
						if (!_shape.isEmpty()) {
							_shapeTop = _shape.bounds().maxY;
						}
					}
					PoseStack _poseStack = _overlayEvent.getPoseStack();
					Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
					int _light = net.minecraft.util.LightCoordsUtil.getLightCoords(Minecraft.getInstance().level, net.minecraft.core.BlockPos.containing(x, y + 1, z));
					// Render primary crop block model with proper transforms
					_poseStack.pushPose();
					_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + _offsetY + _shapeTop, z - _camera.z + 0.5);
					_poseStack.scale(_scaleX, _scaleY, _scaleZ);
					_poseStack.translate(-0.5, 0.0, -0.5);
					var _parts = blockOverlayGetModelParts(_cropState);
					_overlayEvent.getSubmitNodeCollector().submitBlockModel(_poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(), _parts, new int[0], _light, OverlayTexture.NO_OVERLAY, 0);
					_poseStack.popPose();
					// Render secondary upper block if double plant (e.g. upper half of Rose Bush or Pitcher Plant)
					if (_secondState != null) {
						_poseStack.pushPose();
						_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + _offsetY + _shapeTop + _secondOffsetY, z - _camera.z + 0.5);
						_poseStack.scale(_scaleX, _secondScaleY, _scaleZ);
						_poseStack.translate(-0.5, 0.0, -0.5);
						var _secondParts = blockOverlayGetModelParts(_secondState);
						_overlayEvent.getSubmitNodeCollector().submitBlockModel(_poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(), _secondParts, new int[0], _light, OverlayTexture.NO_OVERLAY, 0);
						_poseStack.popPose();
					}
				}
			} else if ((nbtString(world, BlockPos.containing(x, y, z), "soil_type")).equals("minecraft:water") && (nbtString(world, BlockPos.containing(x, y, z), "resource_name")).equals("minecraft:lily_pad")) {
				if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
					float _transparency = Math.max(0.0f, Math.min(1.0f, (float) 1));
					if (_transparency > 0.0f) {
						String _placement = "MIDDLE_CENTER";
						Direction _side = Direction.UP;
						float _scale = (float) (1.1 * (com.itmecasey.skyblockresources.machine.MachineHelpers.progress(world, BlockPos.containing(x, y, z))));
						float _rotX = (float) (-30);
						float _rotY = (float) 0;
						float _rotZ = (float) 0;
						float _thickness = (float) 0;
						PoseStack _poseStack = _overlayEvent.getPoseStack();
						Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
						double _padding = ((Number) 0).doubleValue() / 16.0;
						Vec3 _position = true
								? shapeBoundsPlacement(world.getBlockState(BlockPos.containing(x, y, z)), world, BlockPos.containing(x, y, z), _side, _placement, _scale, _scale, _padding)
								: new Vec3(itemPlacementX(_placement, _scale) + placementOffsetX(_placement, _padding), itemPlacementY(_placement, _scale) + placementOffsetY(_placement, _padding), -0.501);
						ItemStack _itemStack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(((nbtString(world, BlockPos.containing(x, y, z), "resource_name"))).toLowerCase(java.util.Locale.ENGLISH))));
						ItemStackRenderState _itemState = new ItemStackRenderState();
						Minecraft.getInstance().getItemModelResolver().updateForTopItem(_itemState, _itemStack, ItemDisplayContext.FIXED, Minecraft.getInstance().level, null, 0);
						int _light = net.minecraft.util.LightCoordsUtil.getLightCoords(Minecraft.getInstance().level, net.minecraft.core.BlockPos.containing(x, y, z).relative(_side));
						int _layers = _thickness <= 0 ? 1 : Math.max(1, Math.min(64, Math.round(_thickness * 8.0f)));
						double _totalExtrusion = _thickness <= 0 ? 0.0 : (_thickness / 16.0) * _scale;
						double _step = _layers > 1 ? _totalExtrusion / (_layers - 1) : 0.0;
						for (int _i = 0; _i < _layers; _i++) {
							_poseStack.pushPose();
							_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + 0.5, z - _camera.z + 0.5);
							_poseStack.mulPose(faceRotation(_side));
							_poseStack.translate(_position.x, _position.y, _position.z - (_i * _step));
							_poseStack.scale(_scale, _scale, 0.001f);
							if (_rotX != 0.0F)
								_poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(_rotX));
							if (_rotY != 0.0F)
								_poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(_rotY));
							if (_rotZ != 0.0F)
								_poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(_rotZ));
							if (_itemStack.getItem() instanceof BlockItem) {
								_poseStack.scale(0.625f, 0.625f, 0.625f);
							}
							_itemState.submit(_poseStack, _overlayEvent.getSubmitNodeCollector(), _light, OverlayTexture.NO_OVERLAY, 0);
							_poseStack.popPose();
						}
					}
				}
			} else {
				if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
					float _growth = Math.max(0.0f, Math.min(1.0f, (float) ((Number) (0.85 * com.itmecasey.skyblockresources.machine.MachineHelpers.progress(world, BlockPos.containing(x, y, z)))).doubleValue()));
					double _offsetY = ((Number) (-1)).doubleValue() / 16.0;
					float _baseScale = (float) ((Number) 0.65).doubleValue();
					ItemStack _cropStack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(((nbtString(world, BlockPos.containing(x, y, z), "resource_name"))).toLowerCase(java.util.Locale.ENGLISH))));
					Item _cropItem = _cropStack.getItem();
					BlockState _cropState = blockOverlayGetCachedCropResolution(_cropItem, () -> {
						BlockState _resolved = null;
						if (_cropItem == net.minecraft.world.item.Items.WHEAT_SEEDS || _cropItem == net.minecraft.world.item.Items.WHEAT) {
							_resolved = net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.CARROT) {
							_resolved = net.minecraft.world.level.block.Blocks.CARROTS.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.POTATO) {
							_resolved = net.minecraft.world.level.block.Blocks.POTATOES.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.BEETROOT_SEEDS || _cropItem == net.minecraft.world.item.Items.BEETROOT) {
							_resolved = net.minecraft.world.level.block.Blocks.BEETROOTS.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.PUMPKIN_SEEDS || _cropItem == net.minecraft.world.item.Items.PUMPKIN || _cropItem == net.minecraft.world.item.Items.CARVED_PUMPKIN) {
							_resolved = net.minecraft.world.level.block.Blocks.PUMPKIN.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.MELON_SEEDS || _cropItem == net.minecraft.world.item.Items.MELON || _cropItem == net.minecraft.world.item.Items.MELON_SLICE) {
							_resolved = net.minecraft.world.level.block.Blocks.MELON.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.NETHER_WART) {
							_resolved = net.minecraft.world.level.block.Blocks.NETHER_WART.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.COCOA_BEANS) {
							_resolved = net.minecraft.world.level.block.Blocks.COCOA.defaultBlockState().setValue(net.minecraft.world.level.block.CocoaBlock.AGE, 2);
						} else if (_cropItem == net.minecraft.world.item.Items.SWEET_BERRIES) {
							_resolved = net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.GLOW_BERRIES) {
							_resolved = net.minecraft.world.level.block.Blocks.CAVE_VINES.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.TORCHFLOWER_SEEDS) {
							_resolved = net.minecraft.world.level.block.Blocks.TORCHFLOWER_CROP.defaultBlockState();
						} else if (_cropItem == net.minecraft.world.item.Items.PITCHER_POD) {
							_resolved = net.minecraft.world.level.block.Blocks.PITCHER_CROP.defaultBlockState();
						} else if (_cropItem instanceof net.minecraft.world.item.BlockItem _bi) {
							_resolved = _bi.getBlock().defaultBlockState();
						} else {
							var _itemLoc = BuiltInRegistries.ITEM.getKey(_cropItem);
							if (_itemLoc != null) {
								String _path = _itemLoc.getPath();
								String _basePath = _path.endsWith("_seeds") ? _path.substring(0, _path.length() - 6) : _path.endsWith("_seed") ? _path.substring(0, _path.length() - 5) : _path;
								var _block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(_itemLoc.getNamespace() + ":" + _basePath));
								if (_block == null || _block == net.minecraft.world.level.block.Blocks.AIR) {
									_block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(_itemLoc.getNamespace() + ":" + _basePath + "_crop"));
								}
								if (_block == null || _block == net.minecraft.world.level.block.Blocks.AIR) {
									_block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(_itemLoc.getNamespace() + ":" + _basePath + "_plant"));
								}
								if (_block != null && _block != net.minecraft.world.level.block.Blocks.AIR) {
									_resolved = _block.defaultBlockState();
								}
							}
						}
						return _resolved;
					});
					if (_cropState == null || _cropState.isAir()) {
						_cropState = world.getBlockState(BlockPos.containing(x, y, z));
						if (_cropState.isAir())
							_cropState = world.getBlockState(BlockPos.containing(x, y + 1, z));
						if (_cropState.isAir())
							_cropState = net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState();
					}
					if (_cropState.is(net.minecraft.world.level.block.Blocks.COCOA)) {
						_cropState = _cropState.setValue(net.minecraft.world.level.block.CocoaBlock.AGE, 2);
					}
					float _scaleX = _baseScale;
					float _scaleY = _baseScale;
					float _scaleZ = _baseScale;
					// Double-tall detection and multi-stage age/growth property range depend only on the final
					// resolved state, so this is cached by state - correct whether _cropState came from the cached
					// item resolution above or the live world-position fallback.
					final BlockState _shapeStateKey = _cropState;
					var _shapeInfo = blockOverlayGetCachedCropShapeInfo(_cropState, () -> {
						BlockState _s = _shapeStateKey;
						BlockState _second = null;
						if (_s.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)) {
							_s = _s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
							_second = _s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
						}
						boolean _isVisualMultiStage = false;
						net.minecraft.world.level.block.state.properties.IntegerProperty _ageProp = null;
						int _minAge = 0;
						int _maxAge = 7;
						// Only true multi-stage visual crops extrapolate discrete growth stages from percentage
						if (_shapeStateKey.getBlock() instanceof net.minecraft.world.level.block.CropBlock || _shapeStateKey.getBlock() instanceof net.minecraft.world.level.block.NetherWartBlock
								|| _shapeStateKey.getBlock() instanceof net.minecraft.world.level.block.SweetBerryBushBlock || _shapeStateKey.is(net.minecraft.world.level.block.Blocks.TORCHFLOWER_CROP)
								|| _shapeStateKey.is(net.minecraft.world.level.block.Blocks.PITCHER_CROP) || _shapeStateKey.is(net.minecraft.world.level.block.Blocks.PUMPKIN_STEM)
								|| _shapeStateKey.is(net.minecraft.world.level.block.Blocks.MELON_STEM)) {
							for (net.minecraft.world.level.block.state.properties.Property<?> _p : _shapeStateKey.getProperties()) {
								if (_p instanceof net.minecraft.world.level.block.state.properties.IntegerProperty _ip && (_ip.getName().equals("age") || _ip.getName().equals("stage") || _ip.getName().equals("growth"))) {
									_ageProp = _ip;
									_isVisualMultiStage = true;
									break;
								}
							}
						}
						if (_ageProp != null) {
							_minAge = _ageProp.getPossibleValues().stream().mapToInt(Integer::intValue).min().orElse(0);
							_maxAge = _ageProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(7);
						}
						return new BlockOverlayCropShapeInfo(_second, _isVisualMultiStage, _ageProp, _minAge, _maxAge, _maxAge - _minAge + 1);
					});
					BlockState _secondState = null;
					double _secondOffsetY = 0.0;
					float _secondScaleY = _baseScale;
					// Special Case: Double-tall plants (Rose Bush, Sunflower, Peony, Pitcher Crop, etc.)
					if (_shapeInfo.secondBaseState() != null) {
						float _growthScale = Math.max(0.001f, _growth);
						_scaleX = _baseScale * _growthScale;
						_scaleY = _baseScale * _growthScale;
						_scaleZ = _baseScale * _growthScale;
						_cropState = _cropState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
						_secondState = _shapeInfo.secondBaseState();
						_secondOffsetY = (double) _scaleY;
						_secondScaleY = _scaleY;
					} else if (_shapeInfo.isVisualMultiStage() && _shapeInfo.ageProp() != null) {
						int _calculatedStage = (int) Math.floor(_growth * _shapeInfo.totalStages());
						if (_calculatedStage >= _shapeInfo.totalStages())
							_calculatedStage = _shapeInfo.totalStages() - 1;
						int _targetAge = _shapeInfo.minAge() + _calculatedStage;
						_cropState = _cropState.setValue(_shapeInfo.ageProp(), _targetAge);
					} else {
						// Single-stage crops (Cactus, Sugar Cane, Bamboo, Kelp, Gourds like Pumpkin/Melon, Flowers, Saplings, etc.):
						// Percentage complete directly scales the block from 0 to 1 of maximum size (scale)
						float _growthScale = Math.max(0.001f, _growth);
						_scaleX *= _growthScale;
						_scaleY *= _growthScale;
						_scaleZ *= _growthScale;
					}
					// Shape bounds calculation
					double _shapeTop = 0.0;
					if (true) {
						var _shape = world.getBlockState(BlockPos.containing(x, y, z)).getShape(world, BlockPos.containing(x, y, z));
						if (!_shape.isEmpty()) {
							_shapeTop = _shape.bounds().maxY;
						}
					}
					PoseStack _poseStack = _overlayEvent.getPoseStack();
					Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
					int _light = net.minecraft.util.LightCoordsUtil.getLightCoords(Minecraft.getInstance().level, net.minecraft.core.BlockPos.containing(x, y + 1, z));
					// Render primary crop block model with proper transforms
					_poseStack.pushPose();
					_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + _offsetY + _shapeTop, z - _camera.z + 0.5);
					_poseStack.scale(_scaleX, _scaleY, _scaleZ);
					_poseStack.translate(-0.5, 0.0, -0.5);
					var _parts = blockOverlayGetModelParts(_cropState);
					_overlayEvent.getSubmitNodeCollector().submitBlockModel(_poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(), _parts, new int[0], _light, OverlayTexture.NO_OVERLAY, 0);
					_poseStack.popPose();
					// Render secondary upper block if double plant (e.g. upper half of Rose Bush or Pitcher Plant)
					if (_secondState != null) {
						_poseStack.pushPose();
						_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + _offsetY + _shapeTop + _secondOffsetY, z - _camera.z + 0.5);
						_poseStack.scale(_scaleX, _secondScaleY, _scaleZ);
						_poseStack.translate(-0.5, 0.0, -0.5);
						var _secondParts = blockOverlayGetModelParts(_secondState);
						_overlayEvent.getSubmitNodeCollector().submitBlockModel(_poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(), _secondParts, new int[0], _light, OverlayTexture.NO_OVERLAY, 0);
						_poseStack.popPose();
					}
				}
			}
		}
		if (!((nbtString(world, BlockPos.containing(x, y, z), "soil_type")).equals("empty"))) {
			if ((nbtString(world, BlockPos.containing(x, y, z), "soil_type")).equals("minecraft:water")) {
				if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
					String _placement = "MIDDLE_CENTER";
					Direction _side = Direction.UP;
					double _scale = ((Number) 0.75).doubleValue();
					String _rawColor = "" + String.format("#%06X", net.minecraft.client.renderer.BiomeColors.getAverageWaterColor(Minecraft.getInstance().level, BlockPos.containing(x, y, z)) & 0xFFFFFF);
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
					int _a = (int) (Math.max(0.0f, Math.min(1.0f, (float) 0.6)) * 255.0f);
					var _targetState = Blocks.WATER.defaultBlockState();
					String _blockSideStr = "UP";
					net.minecraft.core.BlockPos _targetPos = BlockPos.containing(x, y, z);
					long _cacheKey = net.minecraft.core.BlockPos.asLong(_targetPos.getX(), _targetPos.getY(), _targetPos.getZ()) * 6L + _side.get3DDataValue();
					var _cacheEntry = blockOverlayGetTextureCacheEntry(world, _cacheKey, () -> {
						var _model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(_targetState);
						net.minecraft.client.renderer.texture.TextureAtlasSprite _sprite = _model.particleMaterial().sprite();
						if (_sprite == null) {
							_sprite = _model.particleMaterial().sprite();
						}
						int _computedLight = net.minecraft.util.LightCoordsUtil.getLightCoords(Minecraft.getInstance().level, _targetPos.relative(_side));
						return new BlockOverlayTextureCacheEntry(System.currentTimeMillis(), _sprite, _computedLight);
					});
					final net.minecraft.client.renderer.texture.TextureAtlasSprite _finalSprite = _cacheEntry.sprite();
					int _light = _cacheEntry.light();
					PoseStack _poseStack = _overlayEvent.getPoseStack();
					Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
					double _padding = ((Number) 0).doubleValue() / 16.0;
					double _offsetY = ((Number) (-1)).doubleValue() / 16.0;
					Vec3 _position = true
							? shapeBoundsPlacement(world.getBlockState(BlockPos.containing(x, y, z)), world, BlockPos.containing(x, y, z), _side, _placement, _scale, _scale, _padding)
							: new Vec3(itemPlacementX(_placement, _scale) + placementOffsetX(_placement, _padding), itemPlacementY(_placement, _scale) + placementOffsetY(_placement, _padding), -0.501);
					_poseStack.pushPose();
					_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + 0.5 + _offsetY, z - _camera.z + 0.5);
					_poseStack.mulPose(faceRotation(_side));
					_poseStack.translate(_position.x, _position.y, _position.z);
					_poseStack.scale((float) _scale, (float) _scale, 1.0f);
					boolean _flipWinding = _side == Direction.UP || _side == Direction.DOWN;
					var _renderType = _a >= 255
							? blockOverlayTextureRenderTypeOpaque(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
							: blockOverlayTextureRenderType(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
					try {
						_overlayEvent.getSubmitNodeCollector().submitCustomGeometry(_poseStack, _renderType, (_pose, _consumer) -> {
							if (_flipWinding) {
								_consumer.addVertex(_pose, -0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
							} else {
								_consumer.addVertex(_pose, 0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
							}
						});
					} catch (Exception _renderException) {
						// Ignore - a single bad render shouldn't take down the rest of the frame.
					}
					_poseStack.popPose();
				}
			} else if ((nbtString(world, BlockPos.containing(x, y, z), "soil_type")).equals("minecraft:grass_block")) {
				if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
					String _placement = "MIDDLE_CENTER";
					Direction _side = Direction.UP;
					double _scale = ((Number) 0.75).doubleValue();
					String _rawColor = "" + String.format("#%06X", net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(Minecraft.getInstance().level, BlockPos.containing(x, y, z)) & 0xFFFFFF);
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
					int _a = (int) (Math.max(0.0f, Math.min(1.0f, (float) 1)) * 255.0f);
					var _targetState = BuiltInRegistries.BLOCK.getValue(Identifier.parse(((nbtString(world, BlockPos.containing(x, y, z), "soil_type"))).toLowerCase(java.util.Locale.ENGLISH))).defaultBlockState();
					if (!_targetState.isAir()) {
					String _blockSideStr = "UP";
					net.minecraft.core.BlockPos _targetPos = BlockPos.containing(x, y, z);
					long _cacheKey = net.minecraft.core.BlockPos.asLong(_targetPos.getX(), _targetPos.getY(), _targetPos.getZ()) * 6L + _side.get3DDataValue();
					var _cacheEntry = blockOverlayGetTextureCacheEntry(world, _cacheKey, () -> {
						var _model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(_targetState);
						net.minecraft.client.renderer.texture.TextureAtlasSprite _sprite = _model.particleMaterial().sprite();
						if (_sprite == null) {
							_sprite = _model.particleMaterial().sprite();
						}
						int _computedLight = net.minecraft.util.LightCoordsUtil.getLightCoords(Minecraft.getInstance().level, _targetPos.relative(_side));
						return new BlockOverlayTextureCacheEntry(System.currentTimeMillis(), _sprite, _computedLight);
					});
					final net.minecraft.client.renderer.texture.TextureAtlasSprite _finalSprite = _cacheEntry.sprite();
					int _light = _cacheEntry.light();
					PoseStack _poseStack = _overlayEvent.getPoseStack();
					Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
					double _padding = ((Number) 0).doubleValue() / 16.0;
					double _offsetY = ((Number) (-1)).doubleValue() / 16.0;
					Vec3 _position = true
							? shapeBoundsPlacement(world.getBlockState(BlockPos.containing(x, y, z)), world, BlockPos.containing(x, y, z), _side, _placement, _scale, _scale, _padding)
							: new Vec3(itemPlacementX(_placement, _scale) + placementOffsetX(_placement, _padding), itemPlacementY(_placement, _scale) + placementOffsetY(_placement, _padding), -0.501);
					_poseStack.pushPose();
					_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + 0.5 + _offsetY, z - _camera.z + 0.5);
					_poseStack.mulPose(faceRotation(_side));
					_poseStack.translate(_position.x, _position.y, _position.z);
					_poseStack.scale((float) _scale, (float) _scale, 1.0f);
					boolean _flipWinding = _side == Direction.UP || _side == Direction.DOWN;
					var _renderType = _a >= 255
							? blockOverlayTextureRenderTypeOpaque(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
							: blockOverlayTextureRenderType(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
					try {
						_overlayEvent.getSubmitNodeCollector().submitCustomGeometry(_poseStack, _renderType, (_pose, _consumer) -> {
							if (_flipWinding) {
								_consumer.addVertex(_pose, -0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
							} else {
								_consumer.addVertex(_pose, 0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
							}
						});
					} catch (Exception _renderException) {
						// Ignore - a single bad render shouldn't take down the rest of the frame.
					}
					_poseStack.popPose();
					}
				}
			} else {
				if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
					String _placement = "MIDDLE_CENTER";
					Direction _side = Direction.UP;
					double _scale = ((Number) 0.75).doubleValue();
					String _rawColor = "" + String.format("#%06X", net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(Minecraft.getInstance().level, BlockPos.containing(x, y, z)) & 0xFFFFFF);
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
					boolean _useColor = false;
					float _tintAlpha = _useColor ? Math.max(0.0f, Math.min(1.0f, (float) _aTint / 255.0f)) : 0.0f;
					int _r = Math.max(0, Math.min(255, Math.round(255.0f * (1.0f - _tintAlpha) + (float) _rTint * _tintAlpha)));
					int _g = Math.max(0, Math.min(255, Math.round(255.0f * (1.0f - _tintAlpha) + (float) _gTint * _tintAlpha)));
					int _b = Math.max(0, Math.min(255, Math.round(255.0f * (1.0f - _tintAlpha) + (float) _bTint * _tintAlpha)));
					int _a = (int) (Math.max(0.0f, Math.min(1.0f, (float) 1)) * 255.0f);
					var _targetState = BuiltInRegistries.BLOCK.getValue(Identifier.parse(((nbtString(world, BlockPos.containing(x, y, z), "soil_type"))).toLowerCase(java.util.Locale.ENGLISH))).defaultBlockState();
					if (!_targetState.isAir()) {
					String _blockSideStr = "UP";
					net.minecraft.core.BlockPos _targetPos = BlockPos.containing(x, y, z);
					long _cacheKey = net.minecraft.core.BlockPos.asLong(_targetPos.getX(), _targetPos.getY(), _targetPos.getZ()) * 6L + _side.get3DDataValue();
					var _cacheEntry = blockOverlayGetTextureCacheEntry(world, _cacheKey, () -> {
						var _model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(_targetState);
						net.minecraft.client.renderer.texture.TextureAtlasSprite _sprite = _model.particleMaterial().sprite();
						if (_sprite == null) {
							_sprite = _model.particleMaterial().sprite();
						}
						int _computedLight = net.minecraft.util.LightCoordsUtil.getLightCoords(Minecraft.getInstance().level, _targetPos.relative(_side));
						return new BlockOverlayTextureCacheEntry(System.currentTimeMillis(), _sprite, _computedLight);
					});
					final net.minecraft.client.renderer.texture.TextureAtlasSprite _finalSprite = _cacheEntry.sprite();
					int _light = _cacheEntry.light();
					PoseStack _poseStack = _overlayEvent.getPoseStack();
					Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
					double _padding = ((Number) 0).doubleValue() / 16.0;
					double _offsetY = ((Number) (-1)).doubleValue() / 16.0;
					Vec3 _position = true
							? shapeBoundsPlacement(world.getBlockState(BlockPos.containing(x, y, z)), world, BlockPos.containing(x, y, z), _side, _placement, _scale, _scale, _padding)
							: new Vec3(itemPlacementX(_placement, _scale) + placementOffsetX(_placement, _padding), itemPlacementY(_placement, _scale) + placementOffsetY(_placement, _padding), -0.501);
					_poseStack.pushPose();
					_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + 0.5 + _offsetY, z - _camera.z + 0.5);
					_poseStack.mulPose(faceRotation(_side));
					_poseStack.translate(_position.x, _position.y, _position.z);
					_poseStack.scale((float) _scale, (float) _scale, 1.0f);
					boolean _flipWinding = _side == Direction.UP || _side == Direction.DOWN;
					var _renderType = _a >= 255
							? blockOverlayTextureRenderTypeOpaque(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
							: blockOverlayTextureRenderType(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
					try {
						_overlayEvent.getSubmitNodeCollector().submitCustomGeometry(_poseStack, _renderType, (_pose, _consumer) -> {
							if (_flipWinding) {
								_consumer.addVertex(_pose, -0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
							} else {
								_consumer.addVertex(_pose, 0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, 0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU0(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, 0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
								_consumer.addVertex(_pose, -0.5f, -0.5f, 0).setColor(_r, _g, _b, _a).setUv(_finalSprite.getU1(), _finalSprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(_light).setNormal(_pose, 0, 0, -1);
							}
						});
					} catch (Exception _renderException) {
						// Ignore - a single bad render shouldn't take down the rest of the frame.
					}
					_poseStack.popPose();
					}
				}
			}
		}
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

	private static double nbtDouble(LevelAccessor world, BlockPos pos, String path) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity == null)
			return 0.0;
		CompoundTag current = blockEntity.getPersistentData();
		String[] parts = path.split("[./]");
		for (int i = 0; i < parts.length - 1; i++) {
			String part = parts[i].trim();
			if (part.isEmpty())
				continue;
			if (current.get(part) instanceof CompoundTag sub) {
				current = sub;
			} else {
				return 0.0;
			}
		}
		return current.getDoubleOr(parts[parts.length - 1].trim(), 0.0);
	}
}
