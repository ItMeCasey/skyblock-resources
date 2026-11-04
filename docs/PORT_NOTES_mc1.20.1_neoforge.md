# Port notes: NeoForge 1.20.1

Project: `versions/mc1.20.1/neoforge` (NeoForge 47.1.106, Java 17, ForgeGradle 6). Jar: `skyblock_resources-neoforge-1.20.1-2026.11.1.jar`.
Data and assets come from `common/` through `tools/common-data.gradle` (format `v20`), plus `overrides/1.20.1/`.

## Verified

- `./gradlew jar` builds (reobfuscated by ForgeGradle).
- `./gradlew runGameTest` (dedicated server, port 8904) prints `SELFTEST PASSED`: 123 tree and 253 crop recipes with drops, upgrades through `ItemStack.useOn`, Croptopia, Mystical Agriculture and Occultism checks. The log has no recipe parse errors from this mod (the 9 `forbidden_arcanus:*` recipe errors are that mod's own).
- Block-model rotations checked offline: no illegal angles. Models, overlays, JEI, Jade, SMWTI, KubeJS were not run in a client or script.

## Build setup differs from the template

The brief said to use SMWTI's NeoGradle 7 setup. NeoGradle 7 compiles, but on 1.20.1 it runs with official names while Forge-built mods use SRG names in mixin reference maps, so KubeJS and the other test mods fail to load. This project uses ForgeGradle 6 (`net.minecraftforge.gradle`, `minecraft 'net.neoforged:forge:1.20.1-47.1.106'`, `fg.deobf` for mods) plus `mixin.env.remapRefMap` with `build/createSrgToMcp/output.srg`. ForgeGradle 6 cannot derive a second server run, so `runGameTest` is a task that depends on `runServer`; when `runGameTest` is requested, the server run uses `run-gametest/`, `-Dskyblock.selftest=true` and `--port 8904`. The Modrinth repository is not `exclusiveContent` (the deobf repository must serve the same group).
`run-gametest/eula.txt` contains `eula=true`, written by the port to run the dedicated server (the other builds' run folders have it too). Delete it if you did not mean to accept the EULA there.

## Code changes versus 1.21.1

- Packages: `net.minecraftforge.*` (`MinecraftForge.EVENT_BUS`, `@Mod.EventBusSubscriber`, `ForgeConfigSpec`, `RegistryObject`, `ForgeRegistries`). `@Mod` constructor without arguments; config registered in it. The config screen (`ConfigurationScreen`) does not exist on 1.20.1 and is dropped.
- Registration: `DeferredRegister<Block/Item/BlockEntityType/RecipeType/RecipeSerializer>`; no `DeferredBlock`/`DeferredItem`.
- Capabilities: block entities override `getCapability` (`SidedInvWrapper.create`, plus an unsided wrapper for `null`), `Transfer` reads `BlockEntity.getCapability(ForgeCapabilities.*)`. Items, fluids and energy are looked up through the block entity only, so vanilla blocks without a Forge capability (composter, furnace) are not targets.
- Block entities: `load`/`saveAdditional(CompoundTag)`, `getUpdateTag()`; no `HolderLookup.Provider`. Blocks use `use(..., InteractionHand, ...)`.
- Recipes: `Recipe<Container>` records carry a `ResourceLocation id` (first component; `withId` copies after decoding). `Serializers.of` builds a `fromJson/fromNetwork/toNetwork` serializer from the same MapCodec; the network form is the recipe's JSON text. `RecipeHolder` is a small compat record so call sites are unchanged.
- `IngredientCodecs`: no Ingredient/ItemStack codecs on 1.20.1, so it parses JSON itself: strings (`"#tag"`, `"ns:item"`), lists, native `{"item"}`/`{"tag"}`; stacks from `{"id"}` or `{"item"}` with any count. Unknown item ids fail the recipe (the item registry returns air for unknown ids, so the key is checked first).
- Item data: stack NBT root (`getTag`) instead of `CUSTOM_DATA`. Networking: `SimpleChannel` for the (empty) saved-data sync. Saved data: `SavedData.load/save` with the `skyblock_resources_worldvars`/`mapvars` names.
- Overlays: vertex calls are `vertex(...).color().uv().overlayCoords().uv2().normal().endVertex()`; structure NBT is also looked up under `data/.../structures/`.
- Sized ingredients and sized fluid ingredients do not exist; fluid recipe inputs always read as empty (no recipe uses them).
- Java 17: pattern switch and redundant `instanceof` patterns rewritten.

## Behavior differences

1. Tree machine "white smoke" particle is `CLOUD` (WHITE_SMOKE came later).
2. No config screen; edit `skyblock_resources.toml`.
3. Machines only move items to or from block entities that expose a Forge capability.
4. Upgrade cards and sneak handling work as on 1.21.1 (self-test passes through `ItemStack.useOn`).

## Integrations

- JEI: compiled against the 1.20.1 Forge build, categories unchanged (not run in a client).
- Jade 11.13: block data provider is registered per block-entity class (the API wants block entities); scaled text is a custom `Element` since Jade 11 has no `scale`; progress uses the 5-argument builder. Jade is compile-only here (never run).
- KubeJS 2001.6.5 (KubeJS 6): the typed schemas can be reproduced (`RegisterRecipeSchemasEvent`, `ItemComponents.INPUT/OUTPUT`, `RecipeComponent.builder(...).asArray()` for drops). Compiles and loads; scripts using the schemas were not run.
- SMWTI: uses `smwti-neoforge-1.20.1-2026.11.1.jar` from the SMWTI project (optional at runtime).

## Data differences

- `overrides/1.20.1/` is a copy of `overrides/1.21.1/` (missing-item recipe deletions, `crop_soils`/`wooden_tree_items` tag fixes, rewritten generator models, `New_Block.json` removed, Occultism otherworld sapling recipes) except the three netherite smithing recipes, which `common-data.gradle` now converts itself.
- Added: deleted 12 Occultism `othercobblestone/othercobblerock/otherrock` generation recipes and `otherflower_in_dirt` (items not in Occultism 1.20.1; `otherstone` ones stay), and 4 Mystical Agriculture seed recipes (breeze, sculk, phantom, armadillo; not in MA 7.0.24).
- Not checked: that the pale oak and other 1.21-only items in the 1.21.1 deletions are the only missing vanilla items; the server log shows no tag errors, and no recipe errors from this mod.

## Test mods

Present in the dev runtime (Forge builds, which run on NeoForge 1.20.1): KubeJS, Rhino, Architectury, JEI, Croptopia 4.0.1 + EpheroLib, Mystical Agriculture + Cucumber, Mystical Agradditions, Terralith, Occultism + Modonomicon + Curios + GeckoLib + SmartBrainLib, EvilCraft + Cyclops Core + Common Capabilities, Integrated Dynamics, Forbidden Arcanus + Valhelsia Core.
Absent: Biomes O' Plenty and GlitchCore (require Forge 47.3.0 or newer; NeoForge 1.20.1 ends at 47.1.106), Nature's Spirit (calls `VillagerType.registerBiomeType`, missing on NeoForge: crashes in common setup), Ecologics and Oh The Trees You'll Grow (mixins use SRG names without a reference map, fail in the dev environment). The self-test skips checks for a mod that is not loaded (Croptopia, Mystical Agriculture, Occultism, Nature's Spirit sandy soil).

## Open questions

- The overlays, generator models, JEI pages and Jade tooltip need a look in a live client.
- Whether `tools/common-data.gradle` should drop the Forbidden Arcanus/Occultism-specific deletions into a per-mod-version list.
- Nothing is committed; `PROJECT_STATUS.md` is not updated.
