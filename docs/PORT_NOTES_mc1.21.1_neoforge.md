# Port notes: NeoForge 1.21.1

Project: `versions/mc1.21.1/neoforge` (NeoForge 21.1.265, Java 21, ModDevGradle 2.0.148). Jar: `skyblock_resources-neoforge-1.21.1-2026.11.1.jar`.
Data and assets come from `common/` through `tools/common-data.gradle` (format `v21`), plus `overrides/1.21.1/`.

## Verified

- `./gradlew jar` builds.
- `./gradlew runGameTest` (dedicated server, port 8902) prints `SELFTEST PASSED`. All 15 original checks pass, including Croptopia, Mystical Agriculture and Nature's Spirit drops, upgrade cards through `ItemStack.useOn`, and 249 tree / 304 crop recipes with drops. The server log has no recipe, tag or model errors from this mod.
- `./gradlew runClient` reaches the title screen with all test mods; no errors from this mod's classes (renderers, JEI, Jade classes load). Block and item models load after the override below.
- Not verified in a world: the floating item, plant model, text and textured quad overlays, JEI pages, Jade tooltip, SMWTI line. They compile and their classes load, but nobody has looked at them in game.

## Code changes versus 26.1.2

A small `compat` package holds the version seams: `NbtCompat` (getXOr accessors), `Transfer` (item/fluid/energy handlers), `IngredientCodecs`, `Serializers`, `OverlayRender`, `GuiText`, `TextAlignment`. The rest of the code is the 26.1.2 code with API fixes.

- `Identifier` is `ResourceLocation`.
- Transfer API (`ResourceHandler`, `ItemResource`, `Transaction`) is replaced by `IItemHandler`, `IFluidHandler`, `IEnergyStorage` (`Capabilities.ItemHandler.BLOCK` etc.). Our block entities expose `SidedInvWrapper`.
- Block entities load and save through `CompoundTag` plus `HolderLookup.Provider` instead of `ValueInput`/`ValueOutput`.
- Recipes: `Recipe` is implemented with `assemble(input, provider)`, `getResultItem`, `canCraftInDimensions`, `isSpecial`; serializers go through `Serializers.of`. Recipes are read from `RecipeManager.getAllRecipesFor`. The client gets every recipe type from vanilla sync, so the extra `OnDatapackSyncEvent`/`RecipesReceivedEvent` plumbing is gone.
- Ingredient codec: reads `"#tag"`, `"ns:item"`, a list of those, or the native `{"tag"}`/`{"item"}` form; writes the native form. Outputs use `ItemStack.CODEC` (`{"id","count"}`); counts above the stack size (64 and 16 occur) are accepted.
- Blocks: `onRemove` instead of `affectNeighborsAfterRemoval`, old `updateShape`, `getAnalogOutputSignal(state, level, pos)`, `SoundType.METAL` instead of `IRON`, `getLightBlock`/`propagatesSkylightDown` signatures. The waterlogged shape map is a constant box.
- Saved data uses `SavedData.Factory` with new names (`skyblock_resources_worldvars`, `skyblock_resources_mapvars`); both are empty today.
- Overlays: `SubmitCustomGeometryEvent` and render pipelines are replaced by `RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES` and the level buffer source (`OverlayRender`). Tree structure tint uses `BlockColors.getColor` at the same position as before.
- HUD classes: `GuiGraphics` instead of `GuiGraphicsExtractor`; scaled JEI text through `GuiText`.
- JEI 19.64 (`RecipeType.create`, `addRecipeCatalysts`), Jade 15.10.6 (`IElementHelper`, custom `Element` with `render(...)`), KubeJS 2101.7.2 (`StringComponent.ID.instance()`), SMWTI 1.21.1 build (`GuiGraphics`).

## Behavior differences

1. Item moves are simulated first, then done (`Transfer.moveItems`). The 26.1.2 code extracted inside one transaction and committed it even if the target took less than was extracted, which could destroy items. 1.21.1 never loses them. Otherwise push, pull and insert behave the same.
2. Upgrade cards: 1.21.1 has no flag to skip `onPlace` and the old block's removal side effects. `GeneratorTiers.isUpgrading()` is set around `setBlock` and the generator blocks check it in `onPlace` and `onRemove`. Same result (machine data kept, nothing dropped).
3. Breaking a generator drops its contents in `onRemove` (vanilla container behavior of that version); 26.1.2 does it in `preRemoveSideEffects`.
4. Overlay drawing uses vanilla block/item renderers and `RenderType.entityTranslucent/entityCutout`, not custom pipelines. Translucent water quad and items may sort slightly differently against other translucent geometry. Jade: text next to an item is offset by 2 px instead of Jade's `alignSelfCenter` (not in Jade 15).
5. Tag expansion in `RecipeUtils.getItemStacks` uses `Ingredient.getItems()` and drops the "Empty Tag" barrier placeholder.

## Data differences (all from `overrides/1.21.1/`)

- 16 recipes deleted because their items do not exist in 1.21.1 or in the pinned mod versions: pale oak sapling (3), wildflowers, golden dandelion, closed/open eyeblossom, cactus flower, BOP origin oak sapling (3), origin rose, origin dandelion, purple wildflowers, marigold, Mystical Agriculture awakened supremium farmland soil.
- 3 Occultism otherworld sapling recipes: the `occultism:pitaya` drop removed (item missing in 1.21.1).
- Tags `crop_soils` and `wooden_tree_items`: the missing entries removed (a missing required entry empties the whole tag).
- 3 `minecraft:smithing_transform` recipes rewritten with `{"item"}` ingredients.
- 6 block models (`custom/{wooden,stone,iron,diamond,netherite}_generator2`, `custom/wooden_generator`): elements with a -180 degree Y rotation (only +-45/22.5 are legal before 1.21.4) are rewritten as mirrored, unrotated elements with swapped faces and explicit UVs. Up/down faces get a 180 degree UV rotation. Not looked at in game.
- `models/custom/New_Block.json` removed (capital letters are an invalid path; nothing uses it).
- Item definitions (`items/`) are dropped by the transform, as planned; `models/item` was complete (20 of 20).
- `pack.mcmeta` (pack_format 34) and `neoforge.mods.toml` live in the project's own `src/main/resources`.

## Dev environment and test mods

Pinned: KubeJS 2101.7.2-build.381 + Rhino + Better Advanced Tooltips, Croptopia 4.2.4 (CurseForge) + EpheroLib 1.2.0, Mystical Agriculture 8.0.29 + Cucumber, Biomes O' Plenty 21.1.0.14 + GlitchCore + TerraBlender, Terralith + Lithostitched, Oh The Trees You'll Grow, Ecologics, Nature's Spirit, Occultism + Modonomicon/Curios/GeckoLib/SmartBrainLib, EvilCraft, Integrated Dynamics + CyclopsCore/CommonCapabilities, Mystical Agradditions, Forbidden Arcanus + Valhelsia Core, JEI 19.64.0.455, Jade 15.10.6 (compile only), SMWTI 1.21.1 (local jar). Refined Storage 2.0.9 is in the dev runtime only because CyclopsCore's gametest mixin otherwise crashes the dedicated server (`NoClassDefFoundError: com.refinedmods.refinedstorage...`).
Left out of the 26.1.2 list: Placebo, Toast Control, Mouse Tweaks, Inventory Sorter (client conveniences; the mods.toml still lists the last three as optional). Every mod the self-test uses is present; the test needed no tolerance for absent mods.

## Open questions and things for the parent

- `tools/common-data.gradle` should convert `minecraft:smithing_transform` (template/base/addition to `{"item"}`) for v21/v20 so the three overrides are not needed; also the 1.20.1 port will hit the same rotation, smithing and missing-item problems.
- The generator model fix and all overlay rendering need a look in a live client.
- `PROJECT_STATUS.md` is not updated by this port; add a step for NeoForge 1.21.1.
- The build script keeps `-Xmaxerrs 2000` (harmless).
