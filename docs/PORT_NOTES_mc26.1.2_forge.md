# Port notes: Forge 26.1.2

Project: `versions/mc26.1.2/forge/` (Forge `26.1.2-64.1.6`, ForgeGradle `7.0.x`, Java 25). Jar: `skyblock_resources-forge-26.1.2-2026.11.1.jar`.
Source: a copy of `versions/mc26.1.2/neoforge/src/main/java`, changed only where Forge 26.1.2 differs. 30 of the 78 shared Java files are
identical to the NeoForge ones, 48 differ (most of those only in import lines and three mechanical rewrites, listed below), 22 files are new, 10 were dropped.

## Commands

Run from `versions/mc26.1.2/forge`:

| Command | What it does |
|---|---|
| `./gradlew jar` | Builds the jar. |
| `./gradlew runGameTest` | The headless self-test on Forge's game test server (a headless `MinecraftServer`, no EULA check). Prints `SELFTEST PASSED` or `SELFTEST FAILED` and stops. Passes. |
| `./gradlew runGameTestDedicated` | The same self-test on a real dedicated server with `--port 8901`, like the NeoForge project's `runGameTest`. Needs `run/eula.txt` to say `eula=true`. The first start writes it with `eula=false`; accepting the Minecraft EULA was left to Casey, so this variant has not been run. |
| `./gradlew runServer`, `runClient` | Standard dev runs. Not run (see "Not verified"). |

## Findings that decided the design

Forge 26.1.2 is a different API family from NeoForge 26.1.2:

- Capabilities are the old Forge system: `Capability<T>`, `LazyOptional`, `ForgeCapabilities.ITEM_HANDLER` / `FLUID_HANDLER` / `ENERGY`,
  `IItemHandler`, `IFluidHandler`, `IEnergyStorage`. There is no `ResourceHandler`/`ItemResource`/`Transaction` API and no
  block capabilities on bare blocks (only on block entities).
- The event bus is EventBus 7: events carry their own `BUS` (`TickEvent.ServerTickEvent.Post.BUS`), `@SubscribeEvent` comes from
  `net.minecraftforge.eventbus.api.listener`, `@Mod.EventBusSubscriber(modid=..., value=...)` replaces `@EventBusSubscriber`, and the
  mod constructor takes `FMLJavaModLoadingContext` (`getModBusGroup()`, `registerConfig`).
- `BlockEntity.getPersistentData()` does not exist. The whole mod keeps machine state in it.
- `FakePlayerFactory`, `ModConfigSpec` (it is `ForgeConfigSpec`), `DeferredBlock`/`DeferredItem`, attachments, `PayloadRegistrar`
  (it is `SimpleChannel`), `SubmitCustomGeometryEvent`, `RegisterRenderPipelinesEvent`, `OnDatapackSyncEvent.sendRecipes`,
  `RecipesReceivedEvent`, `ConfigurationScreen`, `RecipeManager.recipeMap()`, `Ingredient.getValues()`, `SizedIngredient`,
  `SizedFluidIngredient`, a 3-argument `SavedDataType` constructor and the `BlockEntityType(supplier, Block...)` constructor are all NeoForge-only.
- **Load conditions are different.** Forge 26.1.2 reads one object under `"forge:condition"` (type `forge:mod_loaded`), for example
  `"forge:condition": { "type": "forge:mod_loaded", "modid": "ae2" }`. It does not read `forge:conditions` arrays. The default that
  `tools/common-data.gradle` uses for `loader: 'forge'` (`forge:conditions` plus `forge:mod_loaded`) is therefore wrong for 26.1.2 (it is the
  1.20.1 format). The key and type renames in that script cannot express "array of one becomes object", so the Forge build turns the rename
  off (`conditionsKey`/`modLoadedType` set to the NeoForge names) and converts in a `doLast` on `prepareCommonData` in its own `build.gradle`.
  All 648 conditioned recipes are single `mod_loaded` entries; the step fails the build if any other shape appears.

## The seam: `com.itmecasey.skyblockresources.loader`

A small package that gives the machine code the same calls it used on NeoForge, so most files differ only in their imports:

| Class | Replaces | Notes |
|---|---|---|
| `ResourceHandler<T>`, `ItemResource`, `FluidResource`, `EnergyHandler`, `ItemUtil` | NeoForge transfer API | Wrap `IItemHandler`, `IFluidHandler`, `IEnergyStorage`. Same method names and signatures as the code uses. |
| `Transaction` | `Transaction.openRoot()` / `open(parent)` | Forge handlers have no transactions, so the wrappers change the real inventory at once and register an undo step (a snapshot of all slots for items, the inverse operation for fluids and energy). Closing a transaction without `commit()` runs the undo steps, newest first. Nested transactions merge into the parent on commit. |
| `Capabilities.Item.BLOCK` etc., `LevelCapabilities.get(level, cap, pos, side)` | `ILevelExtension.getCapability(...)` | Looks up the block entity's Forge capability and wraps the result. |
| `PersistentData.of(blockEntity)` | `blockEntity.getPersistentData()` | A capability attached (through `AttachCapabilitiesEvent.BlockEntities`) to this mod's 11 block entity classes. Forge saves it under `ForgeCaps` and it is part of the block entity update tag, so it reaches clients like NeoForge's data does. Other block entities get a throwaway tag. The 11 block entity classes are unchanged. |
| `RecipeLookup.recipeMap(manager)` | `RecipeManager.recipeMap()` | Reads the private `RecipeMap` field by type (falls back to building one). |
| `IngredientValues.of(ingredient)` | `Ingredient.getValues()` | `ingredient.items().toList()`. |
| `SizedIngredient`, `SizedFluidIngredient` | NeoForge classes | Records so `RecipeUtils` keeps compiling. Nothing creates them (the mod's recipes use plain ingredients). |
| `loader.client.SubmitCustomGeometryEvent`, `RegisterRenderPipelinesEvent` | NeoForge client events | Plain objects the overlay code reads. See "Overlays". |

Mechanical rewrites applied by script to the procedure and helper files (the NeoForge-side change that would make them identical is to use the same `loader` calls):
`x instanceof ILevelExtension ext` became `x instanceof Level ext`, `ext.getCapability(` became `LevelCapabilities.get(ext, `,
`be.getPersistentData()` became `PersistentData.of(be)`, and the NeoForge transfer imports point at `loader`.

## Other changes by file

- `SkyblockResourcesMod`: Forge constructor; registers `DeferredRegister`s, config, network channel, the persistent-data listener and the server tick queue on the mod and Forge buses.
- `init/*Blocks`, `*Items`, `*BlockEntities`, `*Tabs`, `*RecipeTypes`: `DeferredRegister` + `RegistryObject` (block and item ids are set on the properties by hand, as NeoForge's helpers do). `GeneratorTiers` uses `RegistryObject<Block>`.
  The block entity capability registration is gone: Forge gives every `BaseContainerBlockEntity` an item handler on all sides, which matches `WorldlyContainerWrapper` here (every slot on every side, no restrictions).
- `init/SkyblockResourcesModConfigs` removed (config registered in the constructor; no auto-generated config screen on Forge).
- `configuration/ConfigurationConfiguration`: `ForgeConfigSpec`.
- `network/ForgeNetwork` (new) and `SkyblockResourcesModVariables`: `SimpleChannel` instead of payloads. Two messages: the saved-data sync, and a recipe sync message (below).
- `SkyblockResourcesModRecipeTypes`: Forge has no `sendRecipes`, so on `OnDatapackSyncEvent` the server sends the mod's recipes (all four types) in a `RecipeSyncMessage` using each recipe serializer's stream codec; the client builds a `RecipeMap` from it and clears it on logout.
  The self-test round-trips all 272 recipes through the message.
- `WoodenGeneratorBlock`: `shouldDisplayFluidOverlay` takes Forge's `net.minecraft.client.renderer.block.BlockAndTintGetter`.
- `ClearDisplayItemsCommand`: dropped the `FakePlayerFactory` assignment (its result was never used).
- `gametest/HeadlessSelfTest`: Forge imports; a `TestPlayer` (a `ServerPlayer` without a connection) replaces `FakePlayerFactory`; Croptopia and Mystical Agriculture checks run only when those mods are loaded (no Forge 26.1.2 build of either); added two checks: a real item push through a generator's tick procedure into a chest (and into a full chest, which must leave the items in the generator), and the recipe sync round trip.
- `SkyblockResourcesModVariables`: `SavedDataType` now uses the vanilla constructor. NeoForge's version gives the codec a registry context; here the codec uses `RegistryAccess.EMPTY` (only block-state entries need it, and the mod never writes any) and `DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES` (NeoForge's constructor passes none; vanilla requires one).

## Overlays (floating crop, sapling, resource above generators)

NeoForge draws these from `SubmitCustomGeometryEvent`, scanning chunk sections within 16 blocks. Forge 26.1.2 has no such event. The three overlay classes keep their drawing code and their per-position `renderAt`
(now package-visible); `GeneratorOverlayRenderer`, a `BlockEntityRenderer` registered for all 11 generator block entity types in `ClientSetup`, calls them for its own block with a stand-in event whose pose stack starts at the camera.
The render pipelines are added to vanilla's table by reflection (the private `PIPELINES_BY_LOCATION`); if that fails a warning is logged and the pipelines compile on first use.
**Behavior difference:** a generator's overlay is now drawn only while its block entity is drawn (inside the view frustum and within 16 blocks), where NeoForge also drew overlays of generators just outside the view.

## Integrations

| Integration | Forge 26.1.2 | Result |
|---|---|---|
| SMWTI | `smwti-forge-26.1.2-2026.11.1.jar` from `SMWTI/versions/mc26.1.2/forge/build/libs` | Kept. The plugin class is unchanged apart from `PersistentData.of`. The server log shows "Loaded 1 SMWTI plugin(s)". Optional dependency in `mods.toml`. |
| JEI | No Forge build for 26.1.x (JEI publishes `jei-26.1.2-neoforge`, `fabric`, `common` only; checked blamejared and progwml6 maven, Modrinth, CurseForge) | Left out: the four JEI categories and `SkyblockResourcesModJeiPlugin`. |
| Jade | NeoForge-only for 26.x (CurseForge file list) | Left out: `jade/`. |
| KubeJS | NeoForge-only for 26.1.2 (8.0.x) | Left out: `kubejs/` and `kubejs.plugins.txt`. Typed recipe schemas are not available on Forge. |

Dev-environment mods: Biomes O' Plenty `26.1.2.0.22` with GlitchCore and TerraBlender, Oh The Trees You'll Grow, and Mouse Tweaks (all Modrinth versions, Forge 26.1.2). No Forge 26.1.2 build exists for Croptopia, Mystical Agriculture, Cucumber, Nature's Spirit, Ecologics, Occultism, EvilCraft, Integrated Dynamics, Forbidden Arcanus, Terralith (checked CurseForge/Modrinth),
so the recipes for those mods (about 300 plant files) cannot be exercised on Forge yet. Inventory Sorter, Toast Control, Better Advanced Tooltips and Placebo were not added (no Forge 26.1.2 build found for them, or client-only).

## Data

The jar has the same 1,056 data and asset entries as the NeoForge jar apart from `META-INF/mods.toml` (instead of `neoforge.mods.toml`) and the missing `kubejs.plugins.txt`. 648 recipe files differ only in the load condition; two tag files differ as described next.

Two data problems showed up that the NeoForge build only hides because the optional mods are always loaded in its dev environment. They are in `common/`, which I did not edit:

1. `tags/item/crop_soils.json` and `tags/item/farm_crops.json` list Mystical Agriculture items as plain strings. Without that mod the whole tag fails to load, and every plant recipe that uses `#skyblock_resources:crop_soils` then fails to parse (all of `recipe/plants/minecraft/crops`, so beetroot, melon, berries and others lose their drops). The same happens on NeoForge without Mystical Agriculture installed.
   The Forge build works around it in the same `doLast`: strings from other mods in any tag become `{ "id": ..., "required": false }`. The proper fix is to write them that way in `common/`.
2. The six `recipe/mystical_*_farmland_soil_type.json` files have no `mod_loaded` condition and log a parse error when Mystical Agriculture is absent. Harmless, but noisy.

## Behavior differences from NeoForge

- Overlays: see above (frustum-limited).
- Item, fluid and energy moves: same call order and same results in the cases tested; undo uses slot snapshots (items) and inverse operations (fluids, energy), so a rollback on a handler that cannot be written to directly (a non-`IItemHandlerModifiable` from another mod) is best effort. The fluid wrapper cannot target a tank on insert (Forge fills a handler, not a tank).
- Existing NeoForge bug carried over unchanged: the push and pull loops extract first and insert second inside one transaction, then commit when anything was inserted, so when the target takes fewer items than were extracted the difference is lost (NeoForge's transaction behaves the same). It is not new, but worth fixing on both loaders.
- Saved machine data lives under `ForgeCaps` instead of NeoForge's data key; worlds are not interchangeable between loaders.
- No auto-generated config screen (`ConfigurationScreen`); the config file is `config/skyblock_resources.toml` as before.
- The network channel is required on both sides (protocol 1), as the NeoForge payloads were.
- Saved-data type (`mapvars`, `worldvars`) registry context and datafixer: see above.

## Not verified

- `runClient` and the overlays: the Forge client was not started, so the block entity renderer, the pipeline registration and the client-side recipe sync were only compiled, not seen. First things to check in a client: a generator with a plant shows its overlay, and after joining a dedicated server the overlay and the Screwdriver and upgrade-card behavior still see the mod's recipes (that is what the recipe sync message is for).
- `runGameTestDedicated` (needs the EULA).
- All behavior of mods that have no Forge 26.1.2 build.
- `runServer` with a player.

## Open questions

- `tools/common-data.gradle`: should it learn a `conditionsMode` (`array` or `single`) so the Forge 26.1.2 conversion can live there instead of in this `build.gradle`? The other Forge versions need `forge:conditions` arrays (1.20.1) and probably `neoforge:conditions` style on 1.21.1 Forge, so the per-version choice belongs in the `dataTarget` map.
- Should the `loader` seam also be used by the NeoForge project (thin adapters over the real transfer API) so the 48 changed files become identical and can move to a shared folder? The cheapest way: NeoForge gets a `loader` package with the same class names that delegates to its own API.
- `PersistentData` as a capability versus an intermediate base class for the 11 block entities. The capability keeps those classes identical; a base class would make `getPersistentData()` a real method and avoid a capability lookup per call.
