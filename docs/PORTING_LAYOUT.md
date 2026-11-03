# Porting layout

Targets: Forge and NeoForge for 1.20.1, 1.21.1, and 26.1.2 (six builds). The current code is NeoForge 26.1.2. The mod version stays YYYY.M.increment on every build.

## Decision

One repository, one source of truth for data, one source tree per Minecraft version shared by its two loaders.

```
SkyblockResources2/
  common/                      data and assets, written once in the newest format (26.1.2)
    data/skyblock_resources/   recipes (tiers, plants/<modid>, compat/<modid>), tags, loot tables, structures
    assets/skyblock_resources/ models, blockstates, textures, lang, item definitions
  versions/
    mc26.1.2/
      common/java/             code shared by Forge and NeoForge at this version
      neoforge/                Gradle project: build.gradle, gradle.properties, loader-only java, mods.toml
      forge/
    mc1.21.1/ (same shape)
    mc1.20.1/ (same shape)
  overrides/<mc>/              per-version data patches (see below), applied last
  tools/                       the sync script and the check script
  docs/  MOD_MATRIX.md  PROJECT_STATUS.md
```

Each `versions/<mc>/<loader>` is a self-contained Gradle project, like SMWTI's `versions/` folder. It adds `../common/java` as a source directory and builds the data from `common/` through a transform (below). Nothing is generated into git: the transform runs inside `processResources`.

## Why code is split per Minecraft version

The Java uses version-specific APIs, so it cannot be one tree: 26.1.2 has `Identifier`, `TagValueInput` and the new recipe-input classes, and 1.21.1 and 1.20.1 use `ResourceLocation`, `CompoundTag` load and save, `Container` recipes, and different networking and registry calls. Forge and NeoForge at the same Minecraft version differ far less (event bus, registration, config), so they share `versions/<mc>/common/java` and keep only loader glue in their own folder. That is the same ratio SMWTI measured: 15 of its files differ between Forge and NeoForge at one version.

Porting a version means copying the nearest newer version's `common/java`, fixing what the compiler reports, and recording the changes in `docs/PORT_NOTES_<mc>.md`. Port in this order: NeoForge 1.21.1 (closest API), NeoForge 1.20.1, then Forge for each version.

## Why data is shared, and what has to be transformed

The recipe JSON for our own types is defined by our codecs, so the shape can stay identical on every version as long as each version's codec reads it. The things that differ are format details, and the transform handles them:

| Difference | 26.1.2 | 1.21.1 | 1.20.1 | Transform |
|---|---|---|---|---|
| Data folder names | `recipe`, `loot_table`, `structure`, `tags/item` | same | `recipes`, `loot_tables`, `structures`, `tags/items` | rename folders |
| Load conditions | `neoforge:conditions` | `neoforge:conditions` | `forge:conditions` (verify for NeoForge 1.20.1) | rename key and `mod_loaded` type |
| Item model definitions | `assets/.../items/*.json` | none (uses `models/item`) | none | drop `items/`, keep `models/item` |
| `pack.mcmeta` pack format | per version | per version | per version | one file per version in `overrides/` |
| Vanilla recipe shapes | new ingredient and result form | older result form | oldest | only for our few crafting recipes: per-version files in `overrides/` |

The 850 recipe files, tags, models and textures are the bulk and pass through unchanged apart from folder and key renames.

## Per-version patches

`overrides/<mc>/` holds files that replace or delete shared ones for that version only:
- a mod whose block or item ids differ (see `MOD_MATRIX.md`), so its compat recipes are replaced or removed,
- crafting recipes whose vanilla JSON shape changed,
- `pack.mcmeta`.

A file in `overrides/<mc>/` with the same path wins; an empty `.delete` marker next to a name removes the shared file.

## Dependencies and the build

Each version's `build.gradle` pins its own KubeJS, JEI, Jade, SMWTI and test-mod versions (Modrinth maven version ids, the same way `build.gradle` does today). The jar is named `skyblock_resources-<loader>-<mc>-<version>.jar`. `gradlew runGameTest` works in every project; the self-test is plain Java in `versions/<mc>/common/java` so it ports with the code.

`tools/check-data` reads `common/` once and fails on: a recipe that does not parse, an item id that does not exist in any loaded mod for that version (done by the self-test), and a drops list with air or a zero amount (already a self-test).

## Steps

1. (Done) Move the current project to `versions/mc26.1.2/neoforge` and split `common/` out of its `src/main/resources`. No behavior change: the built jar is byte-identical entry for entry, and the self-test passes. Run Gradle from `versions/mc26.1.2/neoforge`.
2. Add the data transform to `processResources` and prove it by building the 26.1.2 jar from `common/` and diffing it against today's jar.
3. Create `versions/mc26.1.2/common/java` and leave only loader glue in `neoforge/`.
4. Add `mc26.1.2/forge`.
5. Start `mc1.21.1/neoforge` from a copy of 26.1.2, then 1.20.1.

## Open questions

- Whether NeoForge 1.20.1 still uses `forge:` condition keys (check the loader's source when step 5 starts).
- Whether the 30 structure files (NBT) load unchanged on 1.20.1 (they should; the NBT structure format is stable).
- Which JEI and KubeJS versions to pin for each target.
