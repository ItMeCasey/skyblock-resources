# Mod recipes for 1.20.1 (Forge and NeoForge)

Plant and resource-block recipes for the mods in `MOD_MATRIX.md` that have a 1.20.1 build. They are plain data in `overrides/1.20.1/data/skyblock_resources/recipe/plants/<modid>/` and `.../compat/<modid>/`, written in the 26.1.2 JSON format (`neoforge:conditions`; the build transform rewrites it). One set serves NeoForge 1.20.1 and Forge 1.20.1, because NeoForge forked from Forge there. Every file is guarded by `mod_loaded`, so recipes for mods that are not installed are inert. Nothing regenerates them at build time. The rules are in `PLANT_RECIPE_RULES.md`.

## How the data was made (2026-11-03)

- Scanner: a temporary server-started event handler (kept outside the repo, in the scratch directory) lists every registered item per mod. Trees: it plants the sapling, grows it with the sapling block's own `advanceTree` (or bonemeal for other growth blocks), and diffs the world to find the real logs, leaves and fruit blocks. Drops: the trunk loot once, the leaf loot 20 times (spread over all leaf types by how many were placed, leaf blocks at full age), and every placed fruit block once at full age; 500 rolls, summarized per item as smallest and largest total plus the chance it dropped at all. Crops and flowers: `BlockState.getDrops` at full growth, 500 rolls, with an empty hand. Which ground a plant survives on (farmland or dirt, sand, mycelium, Nether, End) was tested with `canSurvive`. Resource blocks: block dump with tags, hardness and the loot with a netherite pickaxe, then a hand-written include list.
- Environment: a real Forge 47.4.28 dedicated server for 1.20.1 with 80 mod jars, the scanner as a reobfuscated jar, a flat world, port 8912. This replaced the planned dev-environment scan: Ecologics, Biome Makeover (its jar-in-jar Taniwha) and Oh The Trees You'll Grow fail in a ForgeGradle dev run because their mixins have no usable refmap, and Biomes O' Plenty and GlitchCore need Forge 47.3+. In a production-style run all of them load, one scan covers everything, and the data is the same that a Forge or NeoForge 1.20.1 player gets. Galosphere had to be scanned alone (a second run, with Expanded Ecosphere) because another mod's recipes break its recipe parsing, and Expanded Ecosphere cannot share a world with Oh The Biomes We've Gone and Atmospheric (biome feature order cycle).
- Mod builds scanned: Biomes O' Plenty 19.0.0.96, Regions Unexplored 0.5.6, Oh The Biomes We've Gone 1.8.2 (mod id `biomeswevegone`), Nature's Spirit 2.2.5, Environmental 4.1.2, Atmospheric 6.1.1, Autumnity 5.0.2, Upgrade Aquatic 6.0.3, The Twilight Forest 4.3.2508 (CurseForge file 5468648), The Aether 1.5.2, The Undergarden 0.8.14, Deeper and Darker 1.3.3, Blue Skies 1.3.31, Galosphere 1.5.2, Biome Makeover 1.11.4, Ecologics 2.2.7, Gardens of the Dead 4.0.4, Farmer's Delight 1.3.4, Croptopia 4.0.1 (CurseForge 8066550), Pam's HarvestCraft 2 Crops 1.0.3 and Trees 1.0.2 (CurseForge), Ube's Delight 0.4.15, Vintage Delight 0.1.6, Mystical Agriculture 7.0.24, Botania 456, Thermal Cultivation 11.0.1 (with Thermal Foundation 11.0.6), Immersive Engineering 10.2.0-183, Integrated Dynamics 1.31.4, Productive Trees 0.2.6, Ex Deorum 1.53, Ex Nihilo Sequentia 5.0.0 (needs Nova Core), AE2 15.4.12, Quark 4.0-462, Create 6.0.8, Tinkers' Construct 3.12.1.231, Ars Nouveau 4.12.7, Eidolon Repraised 0.3.13, Occultism 1.158.0, EvilCraft 1.3.5, Firmalife 2.1.28, ArborFirmaCraft 1.0.23 (both with TerraFirmaCraft 3.2.28), Ad Astra 1.15.21, Alex's Caves 2.0.2, Forbidden Arcanus 2.2.6, Let's Do BloomingNature 1.0.12 and Meadow 1.3.25.

## What was written

Counts are files in `overrides/1.20.1`. A tree is 3 files (dirt, farmland, improved soils). Recipes of the 26.1.2 set that still match the 1.20.1 build were left alone (not rewritten).

| Mod id | Tree files | Crop files | Block files | Deletes |
|---|---|---|---|---|
| ad_astra | - | 2 | 64 | 0 |
| aether | 6 (2 saplings) | 3 | 9 | 0 |
| afc (ArborFirmaCraft) | 135 (45 saplings) | 0 | 0 | 0 |
| alexscaves | 9 (3) | 6 | 16 | 0 |
| ars_nouveau | 12 (4) | 2 | 0 | 0 |
| atmospheric | 30 (10) | 10 | 24 | 0 |
| autumnity | 12 (4) | 1 | 0 | 0 |
| biomemakeover | 12 (4) | 11 | 5 | 0 |
| biomesoplenty | 9 (3) | 5 | 2 | 0 |
| biomeswevegone (Oh The Biomes We've Gone) | 129 (43) | 82 | 63 | 0 |
| bloomingnature | 27 (9) | 32 | 20 | 0 |
| blue_skies | 24 (8) | 26 | 55 | 0 |
| botania | - | 64 | 4 | 0 |
| create | - | 0 | 0 | 4 |
| deeperdarker | 3 (1) | 4 | 21 | 0 |
| ecologics | 6 (2) | 0 | 0 | 3 |
| eidolon | 3 (1) | 5 | 0 | 0 |
| environmental | 27 (9) | 21 | 0 | 0 |
| exnihilosequentia | - | 0 | 20 | 0 |
| farmersdelight | - | 6 | 0 | 0 |
| forbidden_arcanus | 3 (1) | 4 | 0 | 0 |
| galosphere | 3 (1) | 0 | 17 | 0 |
| gardens_of_the_dead | - | 2 | 0 | 0 |
| immersiveengineering | - | 1 | 0 | 0 |
| integrateddynamics | 3 (1) | 0 | 0 | 0 |
| meadow | 3 (1) | 7 | 8 | 0 |
| mysticalagriculture | - | 4 | 0 | 0 |
| natures_spirit | 18 (6) | 10 | 0 | 0 |
| occultism | 3 (1) | 0 | 0 | 0 |
| pamhc2crops | - | 97 | 0 | 0 |
| pamhc2trees | 150 (50) | 0 | 0 | 0 |
| productivetrees | 486 (162) | 0 | 0 | 0 |
| quark | 18 (6) | 1 | 0 | 0 |
| regions_unexplored | 108 (36) | 107 | 13 | 0 |
| tconstruct | 9 (3) | 2 | 20 | 0 |
| thermal_cultivation | - | 22 | 0 | 0 |
| thermal_foundation | 3 (1) | 0 | 8 | 0 |
| twilightforest | 30 (10) | 5 | 8 | 0 |
| ubesdelight | - | 4 | 0 | 0 |
| undergarden | 9 (3) | 12 | 4 | 2 |
| upgrade_aquatic | 3 (1) | 16 | 0 | 0 |
| vintagedelight | 3 (1) | 5 | 0 | 0 |
| Total | 1296 | 579 | 381 | 9 |

Notes on the 26.1.2 set (`common/`) versus the 1.20.1 builds:
- Replaced (drops differ: other ids or chances more than 0.15 apart): Nature's Spirit 4 wisteria saplings and `coconut_sprout`, Biomes O' Plenty `flowering_oak_sapling`, Forbidden Arcanus `aurum_sapling`, Ecologics `coconut_seedling` and `walnut_sapling`, Integrated Dynamics `menril_sapling`, Occultism `otherworld_sapling` (the older 1.20.1 override listed `occultism:pitaya`, which is not in Occultism 1.20.1).
- Deleted (item not in the 1.20.1 build): Create `weathered_limestone` (4 tiers), Undergarden `dreadrock` (2), Ecologics `maple_sapling` (3). The earlier deletes for Occultism, Mystical Agriculture and Biomes O' Plenty stay and still match.
- New in a mod that already had recipes: Mystical Agriculture chrome, mithril, titanium and tungsten seeds; Nature's Spirit succulents, azolla, cattail, gray polypore, shiitake, joshua sapling; Biomes O' Plenty cattail, rose, wildflower, tiny cactus, waterlily, huge lily pad, origin and rainbow birch saplings; Forbidden Arcanus fungyss, nipa, golden orchid seeds; Ex Nihilo Sequentia crushed basalt, blackstone, calcite, deepslate, tuff; Regions Unexplored chalk, argillite, ashen dirt.
- The 26.1.2 Croptopia fruit numbers are close to the 1.20.1 ones. The fruit sits on separate `*_crop` leaf blocks in 1.20.1; those blocks are rolled at full age, so the recipes keep the same shape (log, sapling, stick, fruit).

## Mods skipped or with nothing to write

- Terralith, Oh The Trees You'll Grow, Expanded Ecosphere: worldgen only, no registered items.
- Botany Pots: its only items are pots (no plants or raw blocks). Mystical Agradditions: no plants or raw blocks (its crops come through Mystical Agriculture).
- Firmalife: its crops use TerraFirmaCraft loot that drops nothing from `getDrops`, and its fruit trees (cocoa, fig) are TFC tree blocks that do not grow like a sapling. Nothing was written. ArborFirmaCraft is covered (45 saplings, conditions `afc` plus `tfc`), TerraFirmaCraft's own plants are not (not in the mod list).
- Pam's HarvestCraft 2 Food Core and Extended, Croptopia Delight: food only.
- Not on 1.20.1 for either loader (matrix rows with no 1.20.1 build): Expanded Delight, Promenade, Bountiful Fares, Create Integrated Farming, Wilder Wild. Allthemodium is not in the task list.
- No 1.20.1 NeoForge-only builds were needed: every candidate has a Forge 1.20.1 build (The Aether, Ex Nihilo Sequentia and Nova Core are tagged NeoForge on Modrinth; the files load on Forge 47 too).

## Judgment calls

- Included: flowers and small plants that drop themselves (shrubs, cacti, lily pads, mushrooms); seeds and crops; fungi and mushrooms as crops on their ground (the vanilla `crimson_fungus` precedent). Left out: grass, ferns, vines, roots, leaf piles, petals, flower pots and boxes, signs, beds, huge-mushroom blocks, fruit/crop leaf blocks, `*_natural` worldgen variants, wild crop variants, any plant with empty drops or only wheat seeds.
- Pam's HarvestCraft 2: both the seed item and the produce item can be planted; the produce items were skipped and the `*seeditem` kept. Pam's `Garden` blocks and tree fruit blocks are not plants.
- Botania: the 16 mystical, 16 shiny and 16 double flowers and the 16 mushrooms are written; the functional (special) flowers and floating flowers are machines, not plants, and were left out.
- Upgrade Aquatic corals need silk touch to drop themselves (empty-hand sample is empty), so the 10 living coral plants were written with a drop of themselves, 1 to 2, like the vanilla coral recipes. Prismarine coral drops its shards.
- Farmer's Delight `tomato_seeds`: the budding block only drops seeds, so the ripe `tomatoes` vine was sampled.
- Ground: plants that survive on dirt or farmland use the farmland crop soil (named `_in_dirt`, as the existing files); sand-only, mycelium-only, Nether-only, End-only and water plants use the matching soil tag or the water bucket. Trees always get the three standard soils, including Nether-style trees.
- Resource blocks: natural stones, cobbles, sands, soils, ashes and dusts only. Tiers: soils all five, stones and sands from stone, End-only (`unmapped_end_stone`, `null_end_stone`, `crushed_end_stone`, `myalite`) netherite only. Blocks that already have a 26.1.2 recipe keep their tier set as is. Left out: Botania metamorphic stones (made, not found), Blue Skies dungeon stones, Aether clouds, bricks and polished variants, colored kaolin and chalk, ores, storage blocks.
- Twilight Forest blocks: only `deadrock` and `trollsteinn`. Thermal: oil sand and red oil sand (`thermal_foundation`); `thermal:rubberwood_sapling` is also Foundation, the crops are Cultivation.
- Ad Astra planets use the stone tier start (the rule only moves Nether and End blocks to netherite).

## Not covered / open

- Saplings that did not grow in the scan: Regions Unexplored `kapok_sapling`, Alex's Caves `licoroot_sprout`, Tinkers' `ender_slime_leaves` entry (not a sapling), TFC fruit saplings. Thermal Cultivation `hops_seeds` and a few other seed items are not block items (planted by custom code), so they are not listed.
- Sapling tags for the tree generators (for example `#skyblock_resources:wooden_tree_items`) were not touched; saplings of the new mods only work in generators that accept every sapling. `tags/item/biomeswevegone_saplings.json` has the wrong name in `wooden_tree_items` (`biomeswebegone_saplings`); that is in `common/` and was left alone.
- Not run in a client: the new recipes' JEI pages.

## Checks

- Real project: `cd versions/mc1.20.1/neoforge; JAVA_HOME=C:\Users\casey\tools\jdk-21 ./gradlew runGameTest` (port 8904): `SELFTEST PASSED`, `SELFTEST drop recipes: 123 tree, 261 crop, 0 + 0 without drops: ok`, 87 resource recipes, no `Parsing error loading recipe skyblock_resources` lines.
- Scratch copy of the NeoForge 1.20.1 project (port 8911) with Farmer's Delight, Ube's and Vintage Delight, Blueprint mods (Atmospheric, Environmental, Autumnity, Upgrade Aquatic), The Aether, The Undergarden, Regions Unexplored, Create, Botania, Deeper and Darker, Let's Do BloomingNature and Meadow, Blue Skies, AE2, Quark, Tinkers' Construct, Alex's Caves, Twilight Forest, Pam's Crops and Trees, plus the project's own test mods: `SELFTEST drop recipes: 594 tree, 690 crop, 0 + 0 without drops: ok`, 354 resource recipes, `SELFTEST PASSED`, no recipe parse errors from `skyblock_resources`.
- Every generated file was checked by script: all ids exist in the scanned registry, all condition mods exist, no `minecraft:air`, no zero amounts, no empty drops, output equals the first drop.

## Gap fill (2026-11-04): Allthemodium, Alex's Caves licoroot, Regions Unexplored kapok

Changes in `overrides/1.20.1/data/skyblock_resources/recipe/`: 8 recipe files added and 21 `.delete` markers. One scan serves Forge and NeoForge 1.20.1 as before; the loader jars are Forge builds. Scratch copy `%LOCALAPPDATA%\Temp\skyres-scan-gaps\f120` (a ForgeGradle 6 dev server with the old scanner, extended; it ran in the existing `forge-scan\run` folder so no new `eula.txt` was made, port 8915).

- Twilight Forest: nothing new. The 4.3.2508 jar (CurseForge 5468648, already in this file) is the newest 1.20.1 build, and there is no official Modrinth project.
- Allthemodium 2.5.8 (CurseForge 8946365, project 364466; needs All The Ores 2.2.4, file 5348605, and GeckoLib): Forge 1.20.1 build (the Modrinth file is the 1.21.1 jar). This old build has no saplings and no herbs, only blocks. Written: `ancient_grass` (drops itself, soil, all five tiers = 5 files). `ancient_dirt` and `ancient_stone` need no new file: the 26.1.2 recipes in `common/` are identical (same ids, tiers, counts).
- Alex's Caves `licoroot_sprout`: it never grew because its feature only places on chocolate ground (`block_of_chocolate`, `block_of_frosted_chocolate`, `cake_layer`) and its trunk block `alexscaves:licoroot` is not a log-tagged block, so the scanner did not count it. With the chocolate ground and the trunk added to the scanner, a real tree grows. Written: 3 tree files, drops: licoroot block only (the vines drop nothing).
- Regions Unexplored `kapok_sapling`: grows as a plus of 5 saplings. The result matches the 26.1.2 recipe that is now in `common/` (same ids), so no new file here.
- `.delete` markers (21): the 26.1.2 recipes now in `common/` name items that RU 0.5.6, Undergarden 0.8.14 and Allthemodium 2.5.8 do not have: RU `salmon_poppy` and the sky, lavender and salmon wisteria saplings (10 files), Undergarden `puff_mushroom` and `twistybush` (2), Allthemodium ancient, demonic and soul saplings (9).
- Still open: Tinkers' `ender_slime_leaves` (a leaf block, not a sapling), TFC and Firmalife fruit saplings (TFC tree blocks, no sapling-style growth), `cactus_flower` (grows a cactus, not a tree). BWG has no spirit sapling in 1.8.2.

Checks: real projects `versions/mc1.20.1/neoforge` and `versions/mc1.20.1/forge`: neoforge `SELFTEST drop recipes: 123 tree, 261 crop, 0 + 0 without drops: ok`, `resource recipes: 87`, `SELFTEST PASSED`; forge `249 tree, 321 crop`, `resource recipes: 134`, `SELFTEST PASSED` (the Forge project loads more test mods). No `Parsing error loading recipe skyblock_resources` in either log (the `forbidden_arcanus:*` parse errors are that mod's own recipes). Checker over `common/` + `overrides/1.20.1`: no error from the new files. The new recipes were validated against the scanned item registry, not run with the mods loaded in a project (the new mods are not in the real projects).
