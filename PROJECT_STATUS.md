# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-04
Summary: Skyblock Resources 2026.11.1 is released as a beta for six builds (NeoForge and Forge on 26.1.2, 1.21.1 and 1.20.1); CI passes on all six; remaining work is live-client checks, and the logo check.
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
- [~] Resource generator compat recipes for Create, Ex Deorum, Ex Nihilo Sequentia, AE2, and Quark blocks, only loaded when the mod is present; item ids not yet checked against the real mods (w:2)
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

## Open
- [ ] Verify crop tiers in a live world (side config, break drops, Jade and overlay) (w:2)
- [ ] Check every port in a client: overlays, models, JEI, Jade, KubeJS, SMWTI (only NeoForge 26.1.2 has been looked at) (w:4)
- [~] Run the client and check generators, upgrade cards (done), tree and crop drops, screwdriver, and overlays (w:3)
- [ ] Check the logo against the live CurseForge logo (w:1)

## Release
- [x] CI build of all six jars with the headless self test (build.yml) (w:2)
- [x] Release workflow and CurseForge upload script for project 594061 (release.yml, publish_curseforge.py) (w:2)
- [x] CI fixed: EULA accepted for the test server, self-test timeout of 15 minutes, Terralith removed from the 26.1.2 test dependencies (w:2)
- [x] CURSEFORGE_TOKEN, IMC_MAVEN_USER and IMC_MAVEN_PASSWORD repository secrets set (w:1)
- [x] Private Maven: curse.maven dependencies and SMWTI resolve from itmecasey.com/maven, with cursemaven as fallback (w:2)
- [x] Release v2026.11.1 as a GitHub prerelease with six jars; tag v2026.11.1 pushed (w:2)
- [x] CurseForge: six 2026.11.1 files uploaded and all six approved (w:2)
- [ ] Logo and banners uploaded to CurseForge (w:1)

## Wiki and branding
- [x] Wiki live at itmecasey.com/wiki/skyblock-resources with 12 pages and the logo (w:3)
- [~] Logo: empty diamond cube frame, drawn by branding/source/make_icon.py; not yet checked against the live CurseForge logo (w:1)
- [x] Banners and section headers in the SMWTI style: banner, social, features and section headers in branding/banners/ (w:2)
- [x] CurseForge and Modrinth listing copy in branding/LISTING.md; CurseForge project 594061 recorded, Modrinth not created (w:2)

## Ports
- [x] Local self-test server ports 8900-8905 registered in the port table in CLAUDE.md (w:1)
