# Porting layout

Targets: Forge and NeoForge for 1.20.1, 1.21.1, and 26.1.2 (six builds). The current code is NeoForge 26.1.2. The mod version stays YYYY.M.increment on every build.

## Decision

One repository, one source of truth for data, and one independent project per Minecraft version AND loader. Forge and NeoForge are different loaders with different APIs, mod builds and mod ids; they are never treated as interchangeable, and nothing is assumed to work on both.

```
SkyblockResources2/
  common/                      data and assets, written once in the newest format (26.1.2)
    data/skyblock_resources/   recipes (tiers, plants/<modid>, compat/<modid>), tags, loot tables, structures
    assets/skyblock_resources/ models, blockstates, textures, lang, item definitions
  versions/
    mc26.1.2/
      neoforge/                Gradle project: build.gradle, gradle.properties, loader-only java, mods.toml
      forge/
    mc1.21.1/ (same shape)
    mc1.20.1/ (same shape)
  overrides/<mc>/              data fixes for one Minecraft version (formats, vanilla items), used by both loaders of it
  overrides/<mc>-<loader>/     files for one version AND loader: recipes for mods, which differ between Forge and NeoForge; wins over overrides/<mc>/
  tools/                       the sync script and the check script
  docs/  MOD_MATRIX.md  PROJECT_STATUS.md
```

Each `versions/<mc>/<loader>` is a self-contained Gradle project, like SMWTI's `versions/` folder. It has its own full source tree and builds the data from `common/` through a transform (below). Nothing is generated into git: the transform runs inside `processResources`.

## Why code is split per Minecraft version

The Java uses version-specific APIs, so it cannot be one tree: 26.1.2 has `Identifier`, `TagValueInput` and the new recipe-input classes, and 1.21.1 and 1.20.1 use `ResourceLocation`, `CompoundTag` load and save, `Container` recipes, and different networking and registry calls. Forge and NeoForge are separate loaders with different event buses, registries, capability systems, networking, mod metadata, and different builds (and sometimes ids) of every mod, so each loader gets its own complete project even at the same Minecraft version. Code that two projects happen to share is copied, not shared.

Porting a version means copying the nearest newer version of the SAME loader (and using the other loader's port only as a reference for what had to change), fixing what the compiler reports, and recording the changes in `docs/PORT_NOTES_<mc>.md`. Port in this order: NeoForge 1.21.1 (closest API), NeoForge 1.20.1, then Forge for each version.

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
2. (Done) `tools/common-data.gradle` builds each version's data from `common/` (task `prepareCommonData`, wired into `processResources`). A version project sets `ext.dataTarget = [mc: ..., format: 'v26'|'v21'|'v20', loader: ...]` and applies it. For 26.1.2 the jar is byte-identical to the pre-transform jar. For v21 and v20 it was checked on the output files: folder names, load conditions, `c:stones` to `forge:stone`, vanilla crafting recipes (ingredient and result shapes), and the dropped `items/` definitions.

   Not transformed, so each port's recipe codecs must accept both shapes: our own recipe types write ingredients as strings (`"#tag"`, `"ns:item"`) and the output as `{"id", "count"}`. Older versions' Ingredient and ItemStack codecs want `{"item"}`/`{"tag"}` and `{"item", "count"}`, so the port reads both.
3. (Dropped) A shared Forge/NeoForge source folder: the loaders are not compatible, so every project keeps its own source.
4. Add `mc26.1.2/forge`.
5. Start `mc1.21.1/neoforge` from a copy of 26.1.2, then 1.20.1.

## Open questions

- Whether NeoForge 1.20.1 still uses `forge:` condition keys (check the loader's source when step 5 starts).
- Whether the 30 structure files (NBT) load unchanged on 1.20.1 (they should; the NBT structure format is stable).
- Which JEI and KubeJS versions to pin for each target.
