# Changelog

All notable changes to the **Skyblock Resources** mod will be documented in this file.

## [Unreleased]

### Tested in a client
- JEI, REI and EMI on every build that has them, KubeJS recipe schemas on NeoForge 1.21.1, and all five crop generator tiers in a live world (screwdriver output side, breaking, Jade). No jar changed. Create, Ex Deorum, Ex Nihilo Sequentia, AE2 and Quark recipe ids were checked against the real mods.

## [2026.11.4] - 2026-11-12 (Forge and NeoForge 26.3 only)

### Fixed
- With Biomes O' Plenty 26.3 installed, a world could not load: four brimstone generator recipes and nine tree recipes for the orange, red and yellow maple saplings named items that Biomes O' Plenty 26.3 no longer has, and Minecraft stopped on "Errors in the currently selected data pack(s)". The brimstone recipes are removed on 26.3, and the three maple recipes now use the single `maple_sapling` BoP 26.3 has (one recipe per soil instead of three duplicates).

## [2026.11.3] - Unreleased

### Added
- Builds for Minecraft 26.3 and 26.2 on Forge and NeoForge (four new jars). Barely Enough Items and Show Me What This Is are supported on all four; JEI, REI and Jade are supported on the NeoForge builds, where those mods have a build. KubeJS has no 26.2 or 26.3 build, so its recipe schemas are not in these jars.

## [2026.11.2] - 2026-11-06

### Added
- Barely Enough Items support on all six builds, and REI and EMI support where those viewers have a build (REI: 26.1.2 NeoForge, 1.21.1 NeoForge, both 1.20.1 builds; EMI: 1.21.1 NeoForge, both 1.20.1 builds). The viewers show cards for resource generator outputs, tree drops, crop drops and soil types, with the generators as workstations.

### Fixed
- Forge 26.1.2: Barely Enough Items showed none of this mod's cards, because the mod's recipes arrive after BEI builds its index. The mod now asks BEI to read the recipes again when they arrive.

### Tested in a client
- Barely Enough Items on all six builds; Jade on the four builds that have it; Show Me What This Is on all six. See docs/COMPATIBILITY.md for what each check showed and what was not tested (REI, EMI and JEI in a client).

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **TreeGeneratorOverlay (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **TreeGeneratorOverlay (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **TreeGeneratorOverlay (blockoverlay)**
- Modified **WoodenTreeGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenTreeGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenTreeGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Added
- Added **TreeGeneratorOverlay (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenTreeGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenTreeGeneratorOnTickUpdate (procedure)**

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenTreeGeneratorOnTickUpdate (procedure)**

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenTreeGeneratorOnTickUpdate (procedure)**

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenTreeGeneratorOnTickUpdate (procedure)**
- Modified **TreeGeneratorOnBlockRightclicked (procedure)**
- Modified **WoodenTreeGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Added
- Added **WoodenTreeGeneratorOnTickUpdate (element)**

### Changed
- Modified **TreeGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenTreeGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Added
- Added **TreeGeneratorBlockAdded (element)**
- Added **TreeGeneratorOnBlockRightclicked (element)**

### Changed
- Modified **NetheriteDirtGeneration (custom_recipe)**
- Modified **NetheriteGravelGeneration (custom_recipe)**
- Modified **DiamondDirtGeneration (custom_recipe)**
- Modified **WoodenTreeGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **DiamondGeneratorOnTickUpdate (procedure)**
- Modified **IronGeneratorOnTickUpdate (procedure)**
- Modified **StoneGeneratorOnTickUpdate (procedure)**
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **NetheriteGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **StoneUpgradeRightclickedOnBlock (procedure)**

## [2.2.0] - 2026-09-06

### Added
- Added **WoodenDirtGeneration (element)**
- Added **WoodenGravelGeneration (element)**

### Changed
- Modified **Configuration (config)**
- Modified **ResourceGenerator (recipe_type)**

### Removed
- Removed **DirtGeneration (element)**
- Removed **GravelGeneration (element)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **IronDirtGeneration (custom_recipe)**
- Modified **IronGravelGeneration (custom_recipe)**
- Modified **DiamondDirtGeneration (custom_recipe)**
- Modified **DiamondGravelGeneration (custom_recipe)**
- Modified **NetheriteDirtGeneration (custom_recipe)**
- Modified **NetheriteGravelGeneration (custom_recipe)**
- Modified **StoneGravelGeneration (custom_recipe)**
- Modified **StoneDirtGeneration (custom_recipe)**
- Modified **GravelGeneration (custom_recipe)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **IronDirtGeneration (custom_recipe)**
- Modified **IronGravelGeneration (custom_recipe)**
- Modified **DiamondDirtGeneration (custom_recipe)**
- Modified **DiamondGravelGeneration (custom_recipe)**
- Modified **NetheriteDirtGeneration (custom_recipe)**
- Modified **NetheriteGravelGeneration (custom_recipe)**
- Modified **StoneDirtGeneration (custom_recipe)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **IronGenerator (block)**
- Modified **DiamondGenerator (block)**
- Modified **NetheriteGenerator (block)**
- Modified **DiamondGenerator (block)**
- Modified **IronGenerator (block)**
- Modified **StoneGenerator (block)**
- Modified **StoneGenerator (block)**
- Modified **WoodenGenerator (block)**
- Modified **IronGenerator (block)**
- Modified **DiamondGenerator (block)**
- Modified **NetheriteGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **DiamondGenerator (block)**
- Modified **WoodenGenerator (block)**
- Modified **StoneGenerator (block)**
- Modified **IronGenerator (block)**
- Modified **DiamondGenerator (block)**
- Modified **NetheriteGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Added
- Added **NetheriteGeneratorOnTickUpdate (element)**
- Added **IronDirtGeneration (element)**
- Added **IronGravelGeneration (element)**
- Added **DiamondDirtGeneration (element)**
- Added **DiamondGravelGeneration (element)**
- Added **NetheriteDirtGeneration (element)**
- Added **NetheriteGravelGeneration (element)**

### Changed
- Modified **IronGenerator (block)**
- Modified **DiamondGenerator (block)**
- Modified **NetheriteGenerator (block)**
- Modified **NetheriteGenerator (block)**
- Modified **IronDirtGeneration (custom_recipe)**
- Modified **IronGravelGeneration (custom_recipe)**
- Modified **DiamondDirtGeneration (custom_recipe)**
- Modified **DiamondGravelGeneration (custom_recipe)**
- Modified **DirtGeneration (custom_recipe)**
- Modified **StoneDirtGeneration (custom_recipe)**
- Modified **StoneGravelGeneration (custom_recipe)**
- Modified **IronDirtGeneration (custom_recipe)**
- Modified **IronGravelGeneration (custom_recipe)**
- Modified **DiamondDirtGeneration (custom_recipe)**
- Modified **DiamondGravelGeneration (custom_recipe)**
- Modified **NetheriteDirtGeneration (custom_recipe)**
- Modified **NetheriteGravelGeneration (custom_recipe)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Added
- Added **DiamondGeneratorOnTickUpdate (element)**

### Changed
- Modified **DiamondGeneratorOnTickUpdate (procedure)**
- Modified **IronGenerator (block)**
- Modified **DiamondGenerator (block)**
- Modified **DiamondGeneratorOnTickUpdate (procedure)**
- Modified **DiamondGenerator (block)**
- Modified **NetheriteGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Added
- Added **IronGeneratorOnTickUpdate (element)**

### Changed
- Modified **IronGeneratorOnTickUpdate (procedure)**
- Modified **GeneratorOnBlockRightclicked (procedure)**
- Modified **IronGeneratorOnTickUpdate (procedure)**
- Modified **IronGenerator (block)**
- Modified **DiamondGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenGenerator (block)**
- Modified **StoneGenerator (block)**
- Modified **IronGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Added
- Added **StoneGravelGeneration (element)**

### Changed
- Modified **StoneGenerator (block)**
- Modified **StoneGravelGeneration (custom_recipe)**

### Resources & Assets
- Deleted resource asset: models/wooden_generator2.json.textures
- Deleted json 3d model: models/wooden_generator2.json
- Added resource asset: models/wooden_generator2.json.textures
- Added json 3d model: models/wooden_generator2.json
- Deleted json 3d model: models/stone_generator2.json
- Deleted resource asset: models/stone_generator2.json.textures
- Added resource asset: models/stone_generator2.json.textures
- Added json 3d model: models/stone_generator2.json
- Deleted resource asset: models/wooden_generator2.json.textures
- Deleted json 3d model: models/wooden_generator2.json
- Deleted json 3d model: models/stone_generator2.json
- Deleted resource asset: models/stone_generator2.json.textures
- Added resource asset: models/wooden_generator2.json.textures
- Added json 3d model: models/wooden_generator2.json
- Added resource asset: models/stone_generator2.json.textures
- Added json 3d model: models/stone_generator2.json
- Added resource asset: models/iron_generator2.json.textures
- Added json 3d model: models/iron_generator2.json
- Added resource asset: models/diamond_generator2.json.textures
- Added json 3d model: models/diamond_generator2.json
- Added json 3d model: models/netherite_generator2.json
- Added resource asset: models/netherite_generator2.json.textures

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **StoneGeneratorOnTickUpdate (procedure)**
- Modified **StoneGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Added
- Added **StoneGeneratorOnTickUpdate (element)**
- Added **StoneDirtGeneration (element)**

### Changed
- Modified **StoneDirtGeneration (custom_recipe)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **StoneGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Added
- Added **GeneratorOnBlockRightclicked (element)**

### Changed
- Modified **StoneGenerator (block)**
- Modified **WoodenGeneratorOnTickUpdate (procedure)**
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**
- Modified **WoodenGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **StoneGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-06

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **ResourceGenerator (recipe_type)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Resources & Assets
- Added gui texture: src/main/resources/assets/skyblock_resources/textures/screens/resource_jeigui_copy.png
- Added gui texture: src/main/resources/assets/skyblock_resources/textures/screens/blank_background.png
- Deleted gui texture: src/main/resources/assets/skyblock_resources/textures/screens/resource_jeigui_copy.png

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **ResourceGenerator (recipe_type)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **ResourceGenerator (recipe_type)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Added
- Added **ResourceJEIGUI (gui)**

### Changed
- Modified **ResourceGenerator (recipe_type)**
- Modified **ResourceGenerator (recipe_type)**

### Resources & Assets
- Added gui texture: src/main/resources/assets/skyblock_resources/textures/screens/resource_jeigui.png

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **GravelGeneration (custom_recipe)**
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **WoodenGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **WoodenGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Added
- Added **GravelGeneration (element)**

### Changed
- Modified **GravelGeneration (custom_recipe)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**
- Modified **WoodenTreeGeneratorOnTickUpdate (procedure)**

## [2.2.0] - 2026-09-05

### Changed
- Modified **DiamondTreeGeneratorOnTickUpdate (procedure)**
- Modified **IronTreeGeneratorOnTickUpdate (procedure)**
- Modified **NetheriteTreeGeneratorOnTickUpdate (procedure)**
- Modified **StoneTreeGeneratorOnTickUpdate (procedure)**
- Modified **NetheriteTreeGeneratorOnTickUpdate (procedure)**

## [2.2.0] - 2026-09-05

### Added
- Added **ResourceGenerator (recipe_type)**
- Added **DirtGeneration (custom_recipe)**

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Added
- Added **Dirt (recipe_type)**

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Refactored
- Workspace settings refactored (Mod ID: skyblock_resources, Mod Name: Skyblock Resources, Package: net.drakma.skyblockresources)

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Refactored
- Workspace settings refactored (Mod ID: skyblock_resources, Mod Name: Skyblock Resources, Package: net.drakma.skyblockresources)

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-05

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**
- Modified **ShowGeneratorResource (blockoverlay)**
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **GeneratorBlockAdded (procedure)**
- Modified **WoodenGenerator (block)**
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**
- Modified **GeneratorBlockAdded (procedure)**
- Modified **WoodenGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**
- Modified **Screwdriver (item)**
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGenerator (block)**
- Modified **ShowGeneratorResource (blockoverlay)**

### Resources & Assets
- Added gui texture: src/main/resources/assets/skyblock_resources/textures/screens/ouput.png

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGenerator (block)**
- Modified **ShowGeneratorResource (blockoverlay)**
- Modified **WoodenGenerator (block)**

### Resources & Assets
- Deleted resource asset: models/wooden_generator.json.textures
- Deleted json 3d model: models/wooden_generator.json
- Added resource asset: models/wooden_generator2.json.textures
- Added json 3d model: models/wooden_generator2.json

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**
- Modified **Configuration (config)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

## [2.2.0] - 2026-09-03

### Changed
- Modified **GeneratorBlockAdded (procedure)**
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**
- Modified **WoodenGenerator (block)**
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-03

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**
- Modified **GeneratorBlockAdded (procedure)**
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**
- Modified **WoodenGenerator (block)**

## [2.2.0] - 2026-09-02

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**

## [2.2.0] - 2026-09-02

### Added
- Added **TextToDirection (procedure)**

### Changed
- Modified **WoodenGeneratorOnTickUpdate (procedure)**
- Modified **TextToDirection (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Refactored
- Workspace settings refactored (Mod ID: skyblock_resources, Mod Name: Skyblock Resources, Package: net.drakma.skyblockresources)

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Changed
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**
- Modified **ShowGeneratorResource (blockoverlay)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Changed
- Modified **WoodenGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Added
- Added **ShowGeneratorResource (blockoverlay)**

### Changed
- Modified **WoodenGeneratorOnBlockRightclicked (procedure)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Rebuilds & Maintenance
- Workspace rebuilt successfully

## [2.2.0] - 2026-09-02

### Changed
- Modified **WoodenGenerator (block)**

### Rebuilds & Maintenance
- Workspace rebuilt successfully

