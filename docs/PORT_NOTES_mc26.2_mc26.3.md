# Port notes: Minecraft 26.2 and 26.3 (Forge and NeoForge)

Four projects, each a copy of the 26.1.2 project of the same loader, changed only where the new version differs:

| Project | Loader | Java | Self-test port |
|---|---|---|---|
| `versions/mc26.3/neoforge` | NeoForge `26.3.0.101` (ModDev `2.0.148`) | 25 | 8906 |
| `versions/mc26.3/forge` | Forge `26.3-66.1.5` | 25 | 8907 |
| `versions/mc26.2/neoforge` | NeoForge `26.2.0.93` (ModDev `2.0.148`) | 25 | 8908 |
| `versions/mc26.2/forge` | Forge `26.2-65.1.6` | 25 | 8909 |

Loader versions, Gradle and the JEI API versions were taken from BEI's 26.2 and 26.3 projects. Jar names are `skyblock_resources-<loader>-<mc>-2026.11.3.jar`. Data and assets still come from `common/` (data format `v26`, same as 26.1.2); `pack.mcmeta` was left at format 101.1 and the self-test servers load the data without complaint.

## What changed in the code

Only client rendering, a few method signatures, and the dependency list.

| Where | 26.1.2 | 26.2 | 26.3 |
|---|---|---|---|
| World overlay pipelines (`TreeGeneratorOverlay`, `CropGeneratorOverlay`, `ShowGeneratorResource`) | `RenderPipeline.builder(MATRICES_FOG_SNIPPET)` with `withSampler`, `withVertexFormat`, `withVertexShader` | `RenderPipeline.builder(ENTITY_SNIPPET)`, which already carries the entity shaders, the sampler layout, the vertex binding and the depth state | same, and the pipeline classes moved from `com.mojang.blaze3d.pipeline` to `com.mojang.renderpearl.api.pipeline` |
| Light for a block position | `LevelRenderer.getLightCoords` | `LightCoordsUtil.getLightCoords` | same |
| `PoseStack` rotation by a quaternion | `mulPose(Quaternionf)` | unchanged | `rotate(Quaternionfc)` (`mulPose` takes a matrix now) |
| `BlockPos.getCenter()` in the self-test | present | gone, use `Vec3.atCenterOf` | same |
| `Inventory.placeItemBackInInventory(stack)` | one argument | unchanged | needs a `Prediction`; the server-side take uses `SERVER_ONLY` |
| `ModConfig.Type.COMMON` (NeoForge) | present | present | renamed `LOCAL` |
| `RecipeMap` on Forge | `create(Collection)` | unchanged | `create` wants a registry lookup and there is no `createClient`; the Forge client keeps the synced recipes as a list instead of a `RecipeMap` |
| `TreeConfiguration`, `ConfiguredFeature` imports in `RecipeUtils` | used as imports only | present | gone; the unused imports were deleted |

## Optional integrations

Only mods that have a build for the version are compiled against (compile-only, never required):

| Mod | 26.2 NeoForge | 26.3 NeoForge | 26.2 / 26.3 Forge |
|---|---|---|---|
| BEI 2026.11.4 API jar (`libs/`) | yes | yes | yes |
| SMWTI 2026.11.3 (`libs/`, the full jar because the plugin uses its `client.Palette`) | yes | yes | yes |
| JEI API | 30.48.0.244 | 31.17.0.109 | none |
| REI (needs Cloth Config and Architectury) | 26.2.820 | 26.3.824 | none |
| Jade | 26.2.10 | 26.3.5 | none |
| KubeJS | none | none | none |
| EMI | none | none | none |

KubeJS has no 26.2 or 26.3 build, so the NeoForge projects exclude `integration` code under `kubejs/` and `kubejs.plugins.txt` in `build.gradle`. The Croptopia, Mystical Agriculture, Nature's Spirit and similar dev-environment test mods have no 26.2 or 26.3 build either, so the NeoForge self-test now runs the checks for those mods only when they are loaded (the Forge self-test already did that).

The SMWTI and BEI jars in `libs/` are local copies of the builds from the sibling projects. They are compile-only. Replace them with the private Maven coordinates once those builds are mirrored there.

## A render crash the client check found

On 26.3 the first client run died on the first frame that had one of our generators in view: `Render pass color attachment count must match pipeline color target state count`. The opaque overlay pipeline (the one with `ALPHA_CUTOUT`) had no color target, and 26.3 no longer fills in a default one. Each opaque pipeline now sets `withColorTargetState(ColorTargetState.DEFAULT)`, as vanilla's own entity pipelines do. The same line is in all four projects. The headless self-test cannot catch this because a server never builds a pipeline.

## Verified

- `gradlew build` passes in all four projects.
- `gradlew runGameTest` prints `SELFTEST PASSED` in all four (75 resource recipes, 33 tree and 60 crop drop recipes, tier upgrades through the item-use path, the push-conserves-items check).
- A client check on all four builds (`SKYRES_SHOTS=work`, `-PviewerCheck`, run through `launch-isolated.ps1`): the mod loads, a netherite resource generator fed cobblestone fills a chest above it with stacks of 64 cobblestone, a netherite crop generator fed farmland and wheat seeds starts growing and later puts wheat and wheat seeds into its chest, and the in-world overlays (the crop in its planter, the cobblestone with its count) render without a crash. Frames: `docs/frames/work_<mc>_<loader>_generators.png`. The tour sets the output side with a data write instead of a screwdriver click (`BlockDirections` `item_touched_up` and `item_out_up`, `output_direction` `UP`).

## Not verified

- JEI, REI, Jade, BEI and SMWTI were not opened in a client on the new versions; they only compile.
- The `-PviewerCheck` tours for JEI, REI and EMI (`SKYRES_SHOTS=viewer`) were not run on 26.2 or 26.3.
- Not checked on a dedicated server with the EULA accepted (`gradlew runServer`); the headless self-test runs the real server code but no EULA step.
