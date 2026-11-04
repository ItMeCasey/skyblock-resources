# Port notes: Forge 1.21.1

Project: `versions/mc1.21.1/forge` (Forge 52.1.20, ForgeGradle 7.0.40 with the Mavenizer, official mappings, Java 21). Jar: `skyblock_resources-forge-1.21.1-2026.11.1.jar`.
Started from `versions/mc1.21.1/neoforge`; data from `common/` through `tools/common-data.gradle` (format `v21`) plus `overrides/1.21.1/` and `overrides/1.21.1-forge/`.

## Verified

- `./gradlew jar` builds.
- `./gradlew runGameTest` (from `versions/mc1.21.1/forge`) prints `SELFTEST PASSED` and the process exits. Run with JEI, SMWTI (Forge jar), Biomes O' Plenty + GlitchCore + TerraBlender, Terralith and Oh The Trees You'll Grow loaded: 84 tree and 74 crop drop recipes, 91 resource recipes, all with drops. The server log has no recipe, tag or model error from this mod.
- Not verified: `runClient`, overlays, JEI pages, SMWTI line, a real dedicated server with players (network message), a production (non-dev) Forge install. Nobody has looked at any rendering in game.

## Data keys (verified in Forge 1.21.1 source)

- Load condition: field `forge:condition`, one object (not an array), type `forge:mod_loaded`, field `modid`. Set with `conditionsKey: 'forge:condition'`, `conditionsMode: 'single'`, `modLoadedType: 'forge:mod_loaded'`.
- Stone tag is `c:stones` (Forge 1.21.1 uses `c:` common tags), so no `tagMap`. `#neoforge:wrench` stays: it is our own tag file and any namespace loads.
- `overrides/1.21.1-forge/data/skyblock_resources/tags/item/{crop_soils,wooden_tree_items}.json`: the shared 1.21.1 override versions list other mods' items as required entries; one missing required entry empties the tag, and Forge 1.21.1 lacks Mystical Agriculture and others. The Forge copies mark every other-mod entry `{"id", "required": false}`.

## Code changes versus NeoForge 1.21.1

Mechanical: `net.neoforged.*` imports to `net.minecraftforge.*`, `DeferredBlock/Item/Holder` to `RegistryObject`, `@EventBusSubscriber` to `Mod.EventBusSubscriber`, `ModConfigSpec` to `ForgeConfigSpec`, `ILevelExtension.getCapability(Capabilities...)` to `Transfer.items/fluids/energy`. Files not mentioned here changed only that way or not at all.

- `SkyblockResourcesMod`: constructor takes `FMLJavaModLoadingContext`; `MinecraftForge.EVENT_BUS`; `TickEvent.ServerTickEvent.Post`; registers the registers, recipe types, config and the network message directly (no `FMLConstructModEvent` / `RegisterPayloadHandlersEvent`).
- Networking: `SimpleChannel` (`ChannelBuilder`), one clientbound message `SavedDataSyncMessage` (a plain record now, same StreamCodec). Required on both sides (protocol version 1). Data attachments were not used (`ATTACHMENT_TYPES` was empty) and are gone.
- Config: `registerConfig` in the mod constructor. The NeoForge auto config screen does not exist on Forge 1.21.1, so there is no in-game config screen; edit `config/skyblock_resources.toml`.
- `BlockEntity.getPersistentData()` is NeoForge-only. `compat/BlockEntityData.of(be)` keeps the same CompoundTag in a weak map and saves/syncs it as a Forge capability tag (`ForgeCaps` / `skyblock_resources:persistent_data`, also in the client update tag). About 170 call sites were rewritten to it. Block entities of other mods get a throwaway tag.
- Item handler: Forge's `BaseContainerBlockEntity` already exposes an `InvWrapper`; our block entities allow every slot and face, so nothing is registered (NeoForge registered `SidedInvWrapper`; same result). `Transfer` looks capabilities up on the block entity (`LazyOptional`) and wraps `WorldlyContainerHolder` blocks (composter).
- `compat/FakePlayers` replaces `FakePlayerFactory` (not in Forge 1.21.1): a bare `ServerPlayer` without connection, packet-sending methods overridden.
- `compat/SizedIngredient`, `compat/SizedFluidIngredient`: stubs for NeoForge classes that `RecipeUtils` still checks. Nothing creates them on Forge.
- `OverlayRender.poseStack(event)`: Forge's `RenderLevelStageEvent.getPoseStack()` returns a `Matrix4f`; it is wrapped in a `PoseStack`.
- Recipe types and serializers: `DeferredRegister.create(Registries.RECIPE_TYPE ...)`, attached in the mod constructor.
- `FluidStack.matches` became `isFluidStackIdentical`.
- Self-test: `HeadlessSelfTest` skips the Croptopia, Mystical Agriculture and Nature's Spirit checks when those mods are not loaded and ends the JVM itself (below). `gametest/ForgeGameTestHook` registers a non-required no-op test.

## Self-test setup

`runGameTest` uses launch target `forge_userdev_server_gametest` with `--port 8903`, because the plain Forge dev server stops at the EULA prompt (no `eula.txt` was written by this work). Forge's game test server refuses to start with zero tests (hence the no-op test) and throws in `onServerExit` when halted before its tests ran, which leaves the JVM alive; the self-test therefore calls `System.exit` 5 s after the result (exit code 0 or 1). The log shows one `Uncaught exception in server thread` NPE at that point; it comes from Forge's game test server. That server does not open the listening port, so 8903 is passed but not actually bound.
ForgeGradle 7 needs `net.minecraftforge.gradle.merge-source-sets=true` in `gradle.properties` (class and resource output in one folder) and an explicit `mainClass` for custom runs.

## Dropped / unavailable (no Forge 1.21.1 build)

- KubeJS (no Forge build for 1.21): plugin and `kubejs.plugins.txt` left out. Jade (NeoForge/Fabric only at 1.21.1): plugin left out.
- Test mods with no Forge 1.21.1 build, so absent from the dev runtime: Croptopia, Mystical Agriculture (+ Cucumber, Agradditions), Nature's Spirit, Occultism (+ Modonomicon, Curios, GeckoLib, SmartBrainLib), Ecologics, EvilCraft, Integrated Dynamics (+ CyclopsCore, CommonCapabilities), Forbidden Arcanus (+ Valhelsia Core), Lithostitched, Refined Storage, Better Advanced Tooltips. Their self-test checks are skipped, and their recipes load only with their mods (load conditions). Present: JEI 19.64.0.455, SMWTI (Forge jar from the SMWTI project), BOP 21.1.0.14 + GlitchCore + TerraBlender, Terralith 2.5.8, Oh The Trees You'll Grow 5.3.5. Tag and recipe correctness for the missing mods is untested.

## Behavior differences

1. No automatic config screen.
2. Block entity persistent data is stored as a Forge capability tag; other mods' block entities do not keep data written through `BlockEntityData`.
3. Forge has no level-wide capability lookup: containers other than block-entity ones and the composter are not seen by push/pull (NeoForge wraps a few more vanilla providers).
4. External hoppers/pipes get an unsided `InvWrapper` instead of `SidedInvWrapper`; identical for these block entities.
5. The fake player is a plain `ServerPlayer`; it is used only by `/cleardisplayitems` (direction lookup) and the self-test.

## Open questions

- FG7 produces a jar with Mojang-mapped names (no reobfuscation). Whether a production Forge 1.21.1 install (SRG names at runtime) loads it is untested; `SkyblockResourcesMod.clientPlayer()` (unused) uses reflection by name and would break there.
- Client behavior (overlay matrix wrapper, SMWTI, JEI) needs a `runClient` look.
- `overrides/1.21.1/.../crop_soils.json` is redundant now that shared `common/` marks other-mod entries optional; consider deleting it.
- `PROJECT_STATUS.md` was not updated.
