# Mod matrix for pre-generated recipes

Reference for porting. Target versions: **Forge** 1.20.1, 1.21.1, 26.1.2 and **NeoForge** 1.20.1, 1.21.1, 26.1.2. The current port is NeoForge 26.1.2.

Data pulled from the Modrinth API on 2026-11-03. Y means a release exists for that Minecraft version and loader. It does not mean the mod has been checked for sapling, seed, or block ids; that comes from loading each mod. Mods that only publish on CurseForge cannot be checked this way and are marked CF.

| Mod | Adds (verify when loaded) | Forge 1.20.1 | Forge 1.21.1 | Forge 26.1.2 | NeoForge 1.20.1 | NeoForge 1.21.1 | NeoForge 26.1.2 | Recipes today |
|---|---|:-:|:-:|:-:|:-:|:-:|:-:|---|
| Oh The Trees You'll Grow | worldgen only; bigger trees from existing saplings | Y | Y | Y | Y | Y | Y | none (worldgen only, no items) |
| Biomes O' Plenty | saplings, flowers, soils (mud/sand/stone variants) | Y | Y | Y | - | Y | Y | compat + plants |
| Terralith | worldgen only; no blocks or items | Y | Y | - | Y | Y | Y | none (worldgen only, no items) |
| Regions Unexplored | saplings, many flowers/plants, stone & soil variants | Y | Y | - | - | Y | Y | compat |
| Applied Energistics 2 | certus/sky stone, flawless crystals | Y | - | - | Y | Y | Y | compat |
| The Undergarden | saplings, mushrooms, depthrock/soils | Y | - | - | Y | Y | Y | compat |
| Occultism | otherworld saplings, stone variants | Y | - | - | Y | Y | Y | plants + compat |
| Ex Deorum | sieve materials, soils, saplings | Y | - | - | Y | Y | Y | compat |
| Nature's Spirit | saplings, plants, soils | Y | - | - | - | Y | Y | plants + compat |
| Ecologics | saplings (azalea, flowering), coconut | Y | - | - | - | Y | Y | plants |
| Mystical Agriculture | essence crops, farmlands | Y | - | - | - | Y | Y | plants |
| Mystical Agradditions | extra essence blocks and ores; its crops come through Mystical Agriculture | Y | - | - | - | Y | Y | none (adds no plants or raw blocks) |
| Forbidden Arcanus | aurum/edelwood saplings, stone variants | Y | - | - | - | Y | Y | plants + compat |
| EvilCraft | undead saplings | Y | - | - | - | Y | Y | plants |
| Integrated Dynamics | menril saplings | Y | - | - | - | Y | Y | plants |
| Oh The Biomes We've Gone | saplings, flowers, stone/soil variants | Y | Y | - | Y | Y | - |  |
| Firmalife | crops, trees | Y | Y | - | Y | Y | - |  |
| Create | stone variants (limestone, scoria, etc.), zinc | Y | - | - | Y | Y | - | compat |
| Farmer's Delight | crops (cabbage, tomato, onion, rice) | Y | - | - | Y | Y | - |  |
| The Aether | skyroot/golden oak saplings, aether grass/dirt | Y | - | - | Y | Y | - |  |
| [Let's Do] Meadow | saplings, flowers | Y | - | - | Y | Y | - |  |
| [Let's Do] BloomingNature | saplings, flowers, plants | Y | - | - | Y | Y | - |  |
| Ars Nouveau | archwood saplings, sourceberry, mana bloom crops | Y | - | - | Y | Y | - |  |
| Ube's Delight | crops | Y | - | - | Y | Y | - |  |
| Immersive Engineering | hemp, stone/ore blocks | Y | - | - | Y | Y | - |  |
| Upgrade Aquatic | sea plants, driftwood | Y | - | - | Y | Y | - |  |
| Autumnity | maple saplings, plants | Y | - | - | Y | Y | - |  |
| Atmospheric | saplings, plants, soils | Y | - | - | Y | Y | - |  |
| Expanded Ecosphere | biome blocks (limited) | Y | - | - | Y | Y | - |  |
| Environmental | saplings (cherry/willow/pine), plants | Y | - | - | Y | Y | - |  |
| Eidolon Repraised | saplings (illwood), plants | Y | - | - | Y | Y | - |  |
| ArborFirmaCraft | trees | Y | - | - | Y | Y | - |  |
| Productive Trees | sapling variety (modded trees) | Y | - | - | Y | Y | - |  |
| Quark | saplings (blossom/ancient/azalea), stone variants, soils | Y | - | - | - | Y | - | compat |
| Deeper and Darker | saplings, plants, stone variants | Y | - | - | - | Y | - |  |
| Alex's Caves | cave stone/soil variants | Y | - | - | Y | - | - |  |
| Galosphere | crystals/stone variants | Y | - | - | - | Y | - |  |
| Ad Astra | planet stone/soil blocks | Y | - | - | - | Y | - |  |
| Tinkers' Construct | slimy saplings/grass/dirt, stone | Y | - | - | Y | - | - |  |
| Gardens of the Dead | nether trees/fungi | Y | - | - | - | Y | - |  |
| Blue Skies | saplings, plants, soils/stone variants | Y | - | - | Y | - | - |  |
| Botany Pots | crop/soil definitions | Y | - | - | - | Y | - |  |
| Biome Makeover | saplings, plants | Y | - | - | Y | - | - |  |
| Ex Nihilo: Sequentia | sieve materials, soils, seeds | Y | - | - | Y | - | - | compat |
| Expanded Delight | crops | - | - | - | - | Y | - |  |
| Promenade | saplings (palm/dark amaranth) | - | - | - | - | Y | - |  |
| Botania | flowers, livingrock/livingwood, soils | Y | - | - | - | - | - |  |
| Bountiful Fares | crops | - | - | - | - | Y | - |  |
| Vintage Delight | crops | Y | - | - | - | - | - |  |
| Thermal Cultivation | crops, soils | Y | - | - | - | - | - |  |
| Create: Integrated Farming | crops/trees (Create farming) | - | - | - | - | Y | - |  |
| Pam's HarvestCraft 2 - Trees | fruit trees | - | - | - | - | Y | - |  |
| Pam's HarvestCraft 2 - Crops | crops | - | - | - | - | Y | - |  |
| Wilder Wild | saplings, plants, mud/soil | - | - | - | - | - | - |  |
| Croptopia | crops, fruit/nut trees (CurseForge) | CF | CF | CF | CF | CF | CF | plants |
| Twilight Forest | saplings, plants, stone/soil variants (CurseForge) | CF | CF | CF | CF | CF | CF |  |
| Allthemodium | saplings/ores (CurseForge) | CF | CF | CF | CF | CF | CF |  |
