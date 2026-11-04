package com.itmecasey.skyblockresources;

import com.itmecasey.skyblockresources.loader.PersistentData;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

import com.itmecasey.skyblockresources.loader.client.SubmitCustomGeometryEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.Minecraft;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;

public class TreeGeneratorOverlay {
	private static final com.mojang.blaze3d.pipeline.RenderPipeline BLOCK_OVERLAY_TEXTURE_PIPELINE = com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.MATRICES_FOG_SNIPPET)
			.withLocation("blockoverlays_texture_pipeline_treegeneratoroverlay").withVertexShader("core/entity").withFragmentShader("core/entity").withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING").withSampler("Sampler0")
			.withSampler("Sampler2").withColorTargetState(new com.mojang.blaze3d.pipeline.ColorTargetState(com.mojang.blaze3d.pipeline.BlendFunction.TRANSLUCENT))
			.withVertexFormat(com.mojang.blaze3d.vertex.DefaultVertexFormat.ENTITY, com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS).withCull(false).withDepthStencilState(com.mojang.blaze3d.pipeline.DepthStencilState.DEFAULT).build();
	// Opaque counterpart of the pipeline above (no BlendFunction override = opaque, ALPHA_CUTOUT for
	// the texture's own transparent pixels instead of smooth blending, no sortOnUpload needed since
	// opaque geometry doesn't need depth sorting). Translucent rendering carries real, well-known
	// fixed GPU overhead in Minecraft's pipeline (its own sorted render pass) independent of how much
	// geometry uses it - when the overlay doesn't actually need transparency (the common case), this
	// avoids that cost entirely, matching the same class of render path every other overlay type uses.
	private static final com.mojang.blaze3d.pipeline.RenderPipeline BLOCK_OVERLAY_TEXTURE_OPAQUE_PIPELINE = com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.MATRICES_FOG_SNIPPET)
			.withLocation("blockoverlays_texture_opaque_pipeline_treegeneratoroverlay").withVertexShader("core/entity").withFragmentShader("core/entity").withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("NO_OVERLAY")
			.withShaderDefine("NO_CARDINAL_LIGHTING").withSampler("Sampler0").withSampler("Sampler2").withVertexFormat(com.mojang.blaze3d.vertex.DefaultVertexFormat.ENTITY, com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS).withCull(false)
			.withDepthStencilState(com.mojang.blaze3d.pipeline.DepthStencilState.DEFAULT).build();

	public static void onRegisterBlockOverlayPipelines(com.itmecasey.skyblockresources.loader.client.RegisterRenderPipelinesEvent event) {
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
			BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_CACHED = net.minecraft.client.renderer.rendertype.RenderType.create("blockoverlays_texture_treegeneratoroverlay",
					net.minecraft.client.renderer.rendertype.RenderSetup.builder(BLOCK_OVERLAY_TEXTURE_PIPELINE).withTexture("Sampler0", texture).useLightmap().sortOnUpload().createRenderSetup());
		}
		return BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_CACHED;
	}

	private static net.minecraft.client.renderer.rendertype.RenderType blockOverlayTextureRenderTypeOpaque(Identifier texture) {
		if (BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_CACHED == null || !texture.equals(BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_TEXTURE)) {
			BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_TEXTURE = texture;
			BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_CACHED = net.minecraft.client.renderer.rendertype.RenderType.create("blockoverlays_texture_opaque_treegeneratoroverlay",
					net.minecraft.client.renderer.rendertype.RenderSetup.builder(BLOCK_OVERLAY_TEXTURE_OPAQUE_PIPELINE).withTexture("Sampler0", texture).useLightmap().createRenderSetup());
		}
		return BLOCK_OVERLAY_TEXTURE_RENDER_TYPE_OPAQUE_CACHED;
	}

	private static java.util.Set<net.minecraft.world.level.block.Block> TARGET_BLOCKS = null;

	private static boolean matchesTarget(BlockState state) {
		if (TARGET_BLOCKS == null) {
			java.util.Set<net.minecraft.world.level.block.Block> set = new HashSet<>();
			for (String bid : java.util.List.of("skyblock_resources:wooden_tree_generator", "skyblock_resources:stone_tree_generator", "skyblock_resources:iron_tree_generator", "skyblock_resources:diamond_tree_generator",
					"skyblock_resources:netherite_tree_generator")) {
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

	private record BlockOverlayTreeStructureBlock(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
	}

	private record BlockOverlayTreeStructureData(java.util.List<BlockOverlayTreeStructureBlock> blocks, double centerX, double centerZ, int minY, int maxY, int maxDimension) {
	}

	private static final BlockOverlayTreeStructureData BLOCK_OVERLAY_EMPTY_TREE_STRUCTURE = new BlockOverlayTreeStructureData(java.util.List.of(), 0.0, 0.0, 0, 0, 1);
	private static final java.util.Map<String, BlockOverlayTreeStructureData> BLOCK_OVERLAY_TREE_STRUCTURE_CACHE = new java.util.HashMap<>();

	// Biome tint (colorInWorld) was being recomputed for every block in the structure, every frame -
	// keyed by (target position, block state) since the same state can legitimately tint differently
	// at different world positions (different biome), but never changes moment to moment.
	private record BlockOverlayTreeTintKey(long pos, net.minecraft.world.level.block.state.BlockState state) {
	}

	private record BlockOverlayTreeTintEntry(long timestampMs, int tint) {
	}

	private static final long BLOCK_OVERLAY_TREE_TINT_CACHE_INTERVAL_MS = 15_000L;
	private static final java.util.Map<BlockOverlayTreeTintKey, BlockOverlayTreeTintEntry> BLOCK_OVERLAY_TREE_TINT_CACHE = new java.util.HashMap<>();

	private static int blockOverlayGetCachedTreeTint(long posKey, net.minecraft.world.level.block.state.BlockState state, java.util.function.IntSupplier compute) {
		var key = new BlockOverlayTreeTintKey(posKey, state);
		long now = System.currentTimeMillis();
		var cached = BLOCK_OVERLAY_TREE_TINT_CACHE.get(key);
		if (cached != null && now - cached.timestampMs() < BLOCK_OVERLAY_TREE_TINT_CACHE_INTERVAL_MS)
			return cached.tint();
		int fresh = compute.getAsInt();
		BLOCK_OVERLAY_TREE_TINT_CACHE.put(key, new BlockOverlayTreeTintEntry(now, fresh));
		return fresh;
	}

	// Model-parts cache for tree structures, kept separate from the crop overlay's cache.
	private static final java.util.Map<net.minecraft.world.level.block.state.BlockState, java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>> BLOCK_OVERLAY_TREE_STRUCTURE_MODEL_PARTS_CACHE = new java.util.HashMap<>();

	private static java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> blockOverlayGetTreeStructureModelParts(net.minecraft.world.level.block.state.BlockState state) {
		return BLOCK_OVERLAY_TREE_STRUCTURE_MODEL_PARTS_CACHE.computeIfAbsent(state, s -> {
			var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(s);
			java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts = new java.util.ArrayList<>();
			model.collectParts(net.minecraft.util.RandomSource.create(42L), parts);
			return parts;
		});
	}

	private static BlockOverlayTreeStructureData blockOverlayGetTreeStructure(String name) {
		if (name == null || name.isBlank())
			return BLOCK_OVERLAY_EMPTY_TREE_STRUCTURE;
		return BLOCK_OVERLAY_TREE_STRUCTURE_CACHE.computeIfAbsent(name, structureName -> {
			try {
				String[] candidatePaths = new String[]{"data/skyblock_resources/structure/" + structureName + ".nbt", "assets/skyblock_resources/structures/" + structureName + ".nbt"};
				java.io.InputStream foundStream = null;
				for (String path : candidatePaths) {
					foundStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
					if (foundStream != null) {
						break;
					}
				}
				if (foundStream == null) {
					return BLOCK_OVERLAY_EMPTY_TREE_STRUCTURE;
				}
				net.minecraft.nbt.CompoundTag tag;
				try (java.io.InputStream in = foundStream) {
					tag = net.minecraft.nbt.NbtIo.readCompressed(in, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
				}
				var template = new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate();
				template.load(net.minecraft.core.registries.BuiltInRegistries.BLOCK, tag);
				// StructureTemplate.filterBlocks(pos, settings, block) returns ONLY blocks matching that
				// exact type (Palette.blocks(Block) is an inclusive filter, not an exclusion) and there is
				// no public "get everything" accessor, so discover the structure's own palette block types
				// first, then aggregate filterBlocks(...) once per distinct type.
				java.util.Set<net.minecraft.world.level.block.Block> paletteBlockTypes = new java.util.LinkedHashSet<>();
				net.minecraft.nbt.ListTag paletteListList = tag.getListOrEmpty("palettes");
				net.minecraft.nbt.ListTag paletteTag = !paletteListList.isEmpty() ? paletteListList.getListOrEmpty(0) : tag.getListOrEmpty("palette");
				for (int i = 0; i < paletteTag.size(); i++) {
					var paletteState = net.minecraft.nbt.NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK, paletteTag.getCompoundOrEmpty(i));
					if (!paletteState.isAir() && paletteState.getBlock() != net.minecraft.world.level.block.Blocks.STRUCTURE_VOID)
						paletteBlockTypes.add(paletteState.getBlock());
				}
				var placeSettings = new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings();
				java.util.List<net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo> infos = new java.util.ArrayList<>();
				for (var blockType : paletteBlockTypes) {
					infos.addAll(template.filterBlocks(net.minecraft.core.BlockPos.ZERO, placeSettings, blockType));
				}
				java.util.List<BlockOverlayTreeStructureBlock> blocks = new java.util.ArrayList<>();
				int minX = Integer.MAX_VALUE;
				int maxX = Integer.MIN_VALUE;
				int minY = Integer.MAX_VALUE;
				int maxY = Integer.MIN_VALUE;
				int minZ = Integer.MAX_VALUE;
				int maxZ = Integer.MIN_VALUE;
				for (var info : infos) {
					if (info.state().isAir())
						continue;
					blocks.add(new BlockOverlayTreeStructureBlock(info.pos(), info.state()));
					minX = Math.min(minX, info.pos().getX());
					maxX = Math.max(maxX, info.pos().getX());
					minY = Math.min(minY, info.pos().getY());
					maxY = Math.max(maxY, info.pos().getY());
					minZ = Math.min(minZ, info.pos().getZ());
					maxZ = Math.max(maxZ, info.pos().getZ());
				}
				if (blocks.isEmpty()) {
					return BLOCK_OVERLAY_EMPTY_TREE_STRUCTURE;
				}
				// Center on the structure's own declared size (the capture region, always a symmetric
				// square around the trunk), NOT the bounding box of actual non-air blocks - a lopsided
				// canopy (random leaf spread) would otherwise pull the center away from the trunk.
				net.minecraft.core.Vec3i declaredSize = template.getSize();
				double centerX = declaredSize.getX() / 2.0;
				double centerZ = declaredSize.getZ() / 2.0;
				int maxDimension = Math.max(maxX - minX + 1, Math.max(maxY - minY + 1, maxZ - minZ + 1));
				return new BlockOverlayTreeStructureData(java.util.List.copyOf(blocks), centerX, centerZ, minY, maxY, maxDimension);
			} catch (Exception exception) {
				return BLOCK_OVERLAY_EMPTY_TREE_STRUCTURE;
			}
		});
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

	static void renderAt(SubmitCustomGeometryEvent event, Minecraft minecraft, BlockPos position) {
		if (!matchesTarget(minecraft.level.getBlockState(position)))
			return;
		var world = minecraft.level;
		var entity = minecraft.player;
		double x = position.getX();
		double y = position.getY();
		double z = position.getZ();
		if (!((nbtString(world, BlockPos.containing(x, y, z), "resource_name")).equals("empty"))) {
			if (event instanceof SubmitCustomGeometryEvent _overlayEvent) {
				float _growth = Math.max(0.0f, Math.min(1.0f, (float) ((Number) (com.itmecasey.skyblockresources.machine.MachineHelpers.progress(world, BlockPos.containing(x, y, z)))).doubleValue()));
				double _offsetY = ((Number) (-1)).doubleValue() / 16.0;
				float _baseScale = (float) ((Number) 1).doubleValue();
				String _growthMode = "UNIFORM_SCALE";
				String _structureName = ((nbtString(world, BlockPos.containing(x, y, z), "resource_name")).replace(":", "_"));
				Vec3 _camera = _overlayEvent.getLevelRenderState().cameraRenderState.pos;
				var _structureData = blockOverlayGetTreeStructure(_structureName);
				// A structure can be 100+ blocks, each submitted individually every frame - skip the whole
				// thing beyond a reasonable view distance so multiple nearby trees don't tank framerate.
				if (!_structureData.blocks().isEmpty() && _camera.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) <= 4096.0) {
					// Shape bounds calculation
					double _shapeTop = 0.0;
					if (true) {
						var _shape = world.getBlockState(BlockPos.containing(x, y, z)).getShape(world, BlockPos.containing(x, y, z));
						if (!_shape.isEmpty()) {
							_shapeTop = _shape.bounds().maxY;
						}
					}
					PoseStack _poseStack = _overlayEvent.getPoseStack();
					int _light = net.minecraft.client.renderer.LevelRenderer.getLightCoords(Minecraft.getInstance().level, net.minecraft.core.BlockPos.containing(x, y + 1, z));
					var _blockColors = Minecraft.getInstance().getBlockColors();
					net.minecraft.core.BlockPos _tintPos = net.minecraft.core.BlockPos.containing(x, y + 1, z);
					boolean _bottomUpReveal = "BOTTOM_UP_REVEAL".equals(_growthMode);
					float _growthScale = _bottomUpReveal ? 1.0f : Math.max(0.001f, _growth);
					// scale=1 means the whole structure (its longest axis) fits within one block, matching
					// how scale=1 means "normal block size" on the single-block tree overlay.
					float _scale = (_baseScale * _growthScale) / _structureData.maxDimension();
					int _structureHeight = _structureData.maxY() - _structureData.minY() + 1;
					int _revealedMaxY = _structureData.minY() + Math.max(0, Math.round(_growth * _structureHeight) - 1);
					_poseStack.pushPose();
					_poseStack.translate(x - _camera.x + 0.5, y - _camera.y + _offsetY + _shapeTop, z - _camera.z + 0.5);
					_poseStack.scale(_scale, _scale, _scale);
					_poseStack.translate(-_structureData.centerX(), -_structureData.minY(), -_structureData.centerZ());
					long _tintPosKey = _tintPos.asLong();
					for (var _block : _structureData.blocks()) {
						if (_bottomUpReveal && _block.pos().getY() > _revealedMaxY)
							continue;
						_poseStack.pushPose();
						_poseStack.translate(_block.pos().getX(), _block.pos().getY(), _block.pos().getZ());
						var _parts = blockOverlayGetTreeStructureModelParts(_block.state());
						var _tintSource = _blockColors.getTintSource(_block.state(), 0);
						int[] _tints = _tintSource != null ? new int[]{blockOverlayGetCachedTreeTint(_tintPosKey, _block.state(), () -> _tintSource.colorInWorld(_block.state(), world, _tintPos))} : new int[0];
						_overlayEvent.getSubmitNodeCollector().submitBlockModel(_poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(), _parts, _tints, _light, OverlayTexture.NO_OVERLAY, 0);
						_poseStack.popPose();
					}
					_poseStack.popPose();
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
					int _a = (int) (Math.max(0.0f, Math.min(1.0f, (float) 1)) * 255.0f);
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
						int _computedLight = net.minecraft.client.renderer.LevelRenderer.getLightCoords(Minecraft.getInstance().level, _targetPos.relative(_side));
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
					var _targetState = Blocks.GRASS_BLOCK.defaultBlockState();
					String _blockSideStr = "UP";
					net.minecraft.core.BlockPos _targetPos = BlockPos.containing(x, y, z);
					long _cacheKey = net.minecraft.core.BlockPos.asLong(_targetPos.getX(), _targetPos.getY(), _targetPos.getZ()) * 6L + _side.get3DDataValue();
					var _cacheEntry = blockOverlayGetTextureCacheEntry(world, _cacheKey, () -> {
						var _model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(_targetState);
						net.minecraft.client.renderer.texture.TextureAtlasSprite _sprite = _model.particleMaterial().sprite();
						if (_sprite == null) {
							_sprite = _model.particleMaterial().sprite();
						}
						int _computedLight = net.minecraft.client.renderer.LevelRenderer.getLightCoords(Minecraft.getInstance().level, _targetPos.relative(_side));
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
						int _computedLight = net.minecraft.client.renderer.LevelRenderer.getLightCoords(Minecraft.getInstance().level, _targetPos.relative(_side));
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
		CompoundTag current = PersistentData.of(blockEntity);
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
		CompoundTag current = PersistentData.of(blockEntity);
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
