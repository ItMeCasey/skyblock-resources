# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-09
Summary: Skyblock Resources 2026.11.2 (six builds) is released; BEI, Jade, SMWTI, JEI, REI and EMI are seen working in a client on every build that has them, as are all five crop generator tiers and the KubeJS recipe schemas; only the banner upload (Casey) is open.
Spend: none

## Done
- [x] Crop generators for all five tiers, one shared block, block entity, and tick procedure; crop tiers use the tree tier's slots and timing (w:3)
- [x] `gradlew build` passes; dedicated server starts with no mod-load errors (w:2)
- [x] Headless self-test (`runGameTest`) passes: drops for trees, crops, produce, Croptopia, Mystical Agriculture, and generator upgrades (w:2)
- [x] Port folder created on the desktop, separate from the MCreator project (w:1)
- [x] Package moved to com.itmecasey.skyblockresources (w:2)
- [x] Mini Bee hidden: block, item, block entity, assets, and loot table removed (w:2)
- [x] MCreator source files removed: .mcreator, elements, models, trees, changelogs (w:1)
- [x] build.gradle rewritten without MCreator references; testing.gradle removed (it was never applied) (w:2)
- [x] KubeJS typed recipe schemas (soil_type, tree_drops, crop_drops) with a `drops` list ({id, min, max, chance}); verified with a script that adds tree and crop recipes with drops and a custom duration (w:2)
- [x] Tier upgrade cards (stone, iron, diamond, netherite): sneak-click upgrades a resource, tree, or crop generator one tier in place, keeps machine data (confirmed in a live world), recomputes duration and output count for the new tier, with icons, recipes, and a headless test through the real item-use path (w:3)
- [x] Resource generator compat recipes for Create, Ex Deorum, Ex Nihilo Sequentia, AE2, and Quark blocks, only loaded when the mod is present; every id checked against real mod jars (Create 0.5.1/6.0.9, Ex Deorum 1.43/3.10/4.0, Ex Nihilo Sequentia 5.0.0/7.0.0, AE2 15.2.13/19.2.17/26.1.13, Quark 4.0-458/4.1-488); Create, Quark and Ex Nihilo Sequentia have no 26.1.2 build (w:2)
- [x] Tree and crop drops: Croptopia fruit trees, Mystical Agriculture seeds, produce items (wheat, beetroot, melon slice, Croptopia vegetables), and berries all produce drops; items that can never drop are no longer accepted by the crop generator (w:3)
- [x] All tree and crop machines roll drops from explicit per-plant tree_drops and crop_drops recipes (442 files under recipe/plants/<modid>/, generated once and now plain data); no runtime guessing or any-sapling fallback; a self-test checks every recipe lists drops (w:4)
- [x] Shared machine helpers moved to machine/MachineHelpers (side config, inventory insert, NBT access) (w:2)
- [x] Code cleanup: MCreator markers and comments stripped, orphan code removed, Mini Bee lang entries removed (w:4)
- [x] TestOverlay no longer exists; no Mini Bee references remain (w:1)
- [x] All 15 generator recipes exist; wooden tree and crop generators now have their own (saplings or wheat seeds above the hopper) (w:1)
- [x] DropSelfTest renamed to HeadlessSelfTest now that it holds the regression checks (w:1)
- [x] Security fixes: wrench check no longer loads client code on the server, upgrade cards and screwdriver respect build permissions, /cleardisplayitems needs gamemaster (w:2)
- [x] Plant and block recipes for Nature's Spirit, Ecologics, Occultism, EvilCraft, Integrated Dynamics, Forbidden Arcanus on NeoForge 26.1.2 (112 plant files, 53 resource-generator files); Terralith, Oh The Trees You'll Grow, and Mystical Agradditions add nothing to cover; MOD_MATRIX.md lists the remaining mods and target versions (w:3)
- [x] Porting layout designed (docs/PORTING_LAYOUT.md) and step 1 done: project moved to versions/mc26.1.2/neoforge, data and assets split into common/; the jar is identical to the pre-move jar and the self-test passes (w:2)
- [x] Port step 2: tools/common-data.gradle builds per-version data from common/ (v26 byte-identical, v21 and v20 checked on output) (w:2)
- [x] Ports built and self-tested: NeoForge 26.1.2, Forge 26.1.2, NeoForge 1.21.1, Forge 1.21.1, NeoForge 1.20.1, Forge 1.20.1 (each its own project under versions/; Forge and NeoForge are compatible only on 1.20.1) (w:5)
- [x] Mod recipes by version and loader: 26.1.2 in common/, overrides/1.21.1-forge, overrides/1.20.1 (shared by both 1.20.1 loaders); overrides/1.21.1-neoforge (w:4)
- [x] Drakma author and website fields changed to ItMeCasey in neoforge.mods.toml (w:1)
- [x] End, nether, and mycelium soil tags exist and are used by their soil recipes (w:1)
- [x] Orphan code deleted: fireball, eye of ender, portal, teleport, block damage, AI goals, unused renderers (w:2)
- [x] Dependencies come from Modrinth maven and the private Maven (curse.maven mirror, SMWTI included); no local jars (w:2)

## Compatibility
- [x] Table of which viewers and overlays have a build per version and loader, from CurseForge (docs/COMPATIBILITY.md) (w:1)
- [x] BEI plugin on all six builds (integration/bei), compiles on each (w:3)
- [x] REI plugin on 26.1.2 NeoForge, 1.21.1 NeoForge and both 1.20.1 builds; EMI plugin on 1.21.1 NeoForge and both 1.20.1 builds; the other builds have no REI or EMI file (w:3)
- [x] JEI, Jade and SMWTI integrations still compile on every build they exist on (w:1)
- [x] BEI checked in a client on all six builds: resource generator, tree drop, crop drop and soil type cards seen, frames in docs/frames (w:4)
- [x] Client checks for BEI (all six builds) and Jade (four builds) and SMWTI (all six builds), frames in docs/frames (w:4)
- [x] Client checks for REI, EMI and JEI on NeoForge 1.21.1: resource generator, tree drop, crop drop and soil cards and the generator workstations seen, frames in docs/frames (w:2)
- [x] Client checks for JEI, REI and EMI on every other build that has them, frames in docs/frames: JEI on 26.1.2 NeoForge, 1.21.1 Forge, both 1.20.1; REI on 26.1.2 NeoForge and both 1.20.1; EMI on both 1.20.1 (`-PviewerCheck -Pviewer=jei|rei|emi`, `SKYRES_SHOTS=viewer`; 1.20.1 needs JAVA_HOME=jdk-21 for Gradle). n/a: no REI build for Forge 26.1.2, 1.21.1 Forge; no EMI build for 26.1.2 or 1.21.1 Forge (w:2)
- [x] Tree drop, crop drop and soil card views seen in the BEI check on five builds (w:1)
- [x] Forge 26.1.2: BEI showed none of our cards because our recipes arrive after BEI indexes; fixed with BeiRefresh (w:2)

## Open
- [x] Verify crop tiers in a live world: all five tiers took soil and seed, the screwdriver set the output side to a chest and every tier filled it, breaking each dropped its contents and itself, Jade showed each tier (NeoForge 1.21.1, docs/frames) (w:2)
- [x] Check every port in a client: BEI, Jade, SMWTI, JEI, REI and EMI done on every build that has them; KubeJS checked on NeoForge 1.21.1 (typed recipe schemas add soil_type, crop_drops and tree_drops recipes in a live world, all three loaded); n/a: no KubeJS build for Forge 1.21.1 or Forge 26.1.2 (w:4)
- [x] Run the client and check generators, upgrade cards, tree and crop drops, screwdriver, and overlays (crop tiers, screwdriver side and Jade on NeoForge 1.21.1; overlays and cards on all builds earlier) (w:3)
- [x] Check the logo against the live CurseForge logo: the live logo is the same cube icon as branding/icon (w:1)

## Release
- [x] Version 2026.11.2 set on all six builds, changelog and CurseForge description updated (w:1)
- [x] Release 2026.11.2: pushed, CI green, tagged v2026.11.2, six jars on CurseForge as beta (w:2)
- [x] CI build of all six jars with the headless self test (build.yml) (w:2)
- [x] Release workflow and CurseForge upload script for project 594061 (release.yml, publish_curseforge.py) (w:2)
- [x] CI fixed: EULA accepted for the test server, self-test timeout of 15 minutes, Terralith removed from the 26.1.2 test dependencies (w:2)
- [x] CURSEFORGE_TOKEN, IMC_MAVEN_USER and IMC_MAVEN_PASSWORD repository secrets set (w:1)
- [x] Private Maven: curse.maven dependencies and SMWTI resolve from itmecasey.com/maven, with cursemaven as fallback (w:2)
- [x] Release v2026.11.1 as a GitHub prerelease with six jars; tag v2026.11.1 pushed (w:2)
- [x] CurseForge: six 2026.11.1 files uploaded and all six approved (w:2)
- [~] Logo is live on CurseForge; banners are not uploadable through the API and are not confirmed on the page (w:1)

## Wiki and branding
- [x] Wiki live at itmecasey.com/wiki/skyblock-resources with 12 pages and the logo (w:3)
- [x] Logo: empty diamond cube frame, drawn by branding/source/make_icon.py; matches the live CurseForge logo (w:1)
- [x] Banners and section headers in the SMWTI style: banner, social, features and section headers in branding/banners/ (w:2)
- [x] CurseForge and Modrinth listing copy in branding/LISTING.md; CurseForge project 594061 recorded, Modrinth not created (w:2)

## Ports
- [x] Local self-test server ports 8900-8905 registered in the port table in CLAUDE.md (w:1)
