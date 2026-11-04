# Port notes: Forge 1.20.1

Project: `versions/mc1.20.1/forge` (Forge 47.4.10, Java 17, ForgeGradle 6, official mappings). Jar: `skyblock_resources-forge-1.20.1-2026.11.1.jar`.
Copied from `versions/mc1.20.1/neoforge`; data from `common/` through `tools/common-data.gradle` (`mc: '1.20.1', format: 'v20', loader: 'forge'`) plus `overrides/1.20.1/` (shared, untouched). No `overrides/1.20.1-forge/` was needed.

## Verified

- `./gradlew jar` builds (with `JAVA_HOME=C:\Users\casey\tools\jdk-21` for Gradle).
- `./gradlew runGameTest` (from `versions/mc1.20.1/forge`) prints `SELFTEST PASSED`, the process exits with code 0 (about 2.5 minutes). 240 tree and 298 crop drop recipes without empty drops, 132 resource recipes, upgrades through `ItemStack.useOn`, and the Croptopia, Mystical Agriculture, Occultism and Nature's Spirit (sandy soil) checks ran for real (those mods are loaded).
- The log has no recipe, tag, model or loot error from this mod. The only `RecipeManager` errors are the 9 `forbidden_arcanus:*` recipes (Forbidden Arcanus' own, same as on NeoForge). Two harmless lines: `server.properties` missing on the first run, and the game test server's NPE at shutdown (below).
- Not run: client (`runClient`), overlays, models in game, JEI pages, Jade, SMWTI line, KubeJS scripts, a real dedicated server with players, a production (non-dev) Forge install.

## Differences from NeoForge 1.20.1

Source: none needed. The NeoForge 1.20.1 sources already use `net.minecraftforge.*` (`@Mod.EventBusSubscriber`, `ForgeConfigSpec`, `SimpleChannel`, `ForgeCapabilities`), and they compile unchanged against Forge 47.4.10. The only Java change is in `gametest/HeadlessSelfTest` (shutdown, below). Comments that mention NeoForge's snapshot behavior stay as they were.

Metadata and build:
- `build.gradle`: `minecraft 'net.minecraftforge:forge:1.20.1-47.4.10'` instead of `net.neoforged:forge:1.20.1-47.1.106`; `ext.dataTarget.loader = 'forge'`; jar name `skyblock_resources-forge-1.20.1`; SMWTI dependency is the Forge jar (`SMWTI/versions/mc1.20.1/forge/build/libs/smwti-forge-1.20.1-2026.11.1.jar`); the NeoForged Maven repository is gone. The ForgeGradle 6 workarounds (mixin `remapRefMap` with `build/createSrgToMcp/output.srg`, non-exclusive Modrinth repository) are kept.
- `mods.toml`: only the `forge` dependency range changed, `[47.1.106,)` to `[47.0.0,)` (47.1.106 is a NeoForge number).
- Load conditions: the v20 defaults give `"conditions": [ {"type": "forge:mod_loaded", "modid": ...} ]` in recipes (checked in the built jar), which is what Forge 1.20.1 reads. Stone tag is renamed `c:stones` to `forge:stone` by the script's v20 default.

Self-test run:
- NeoForge's `runGameTest` runs the dedicated server. Here `runGameTest` depends on `runGameTestServer`, a ForgeGradle 6 run for Forge's `gameTestServer` launch target (`forgegametestserveruserdev`), working directory `run-gametest/`, `--port 8905`, `-Dskyblock.selftest=true`, `-Dforge.enabledGameTestNamespaces=skyblock_resources`. It does not stop at the EULA prompt, so no `eula.txt` was created or edited (confirmed: `run-gametest/` has none). On 1.20.1 this server needed no dummy test (unlike 1.21.1).
- The game test server throws a NullPointerException in `GameTestServer.onServerExit` when halted before its tests ran, which leaves the JVM alive (same as 1.21.1). `HeadlessSelfTest` therefore starts a daemon thread that calls `Runtime.halt(0 or 1)` 5 seconds after the result, after the world was saved. This is the only source change. Note that this server does not open a listening port, so 8905 is passed but not bound.

## Test mods (dev runtime)

Same Forge builds as NeoForge 1.20.1 (KubeJS, Rhino, Architectury, JEI, Croptopia + EpheroLib, Mystical Agriculture + Cucumber, Mystical Agradditions, Terralith, Occultism + Modonomicon + Curios + GeckoLib + SmartBrainLib, EvilCraft + Cyclops Core + Common Capabilities, Integrated Dynamics, Forbidden Arcanus + Valhelsia Core) plus, new on Forge: Biomes O' Plenty 19.0.0.96, GlitchCore 0.0.1.1, TerraBlender 3.0.1.11 (BOP and Nature's Spirit need it), Nature's Spirit 2.2.5. All of them load and the self-test passes with them.

Still absent: Ecologics 2.2.7 and Oh The Trees You'll Grow 1.7.3. Both were tried on Forge 47.4.10 and fail to load in the dev environment exactly as on NeoForge: their mixins use SRG names (`m_7023_`, `m_8714_`) without a reference map, ForgeGradle 6 dev runs use official names, so Mixin reports "could not find any targets". Their recipes and tags were therefore not tested in a live game; they will be correct only through load conditions. This is a dev-environment limit; a production Forge install would map names.

## Dropped / unchanged features

Same as NeoForge 1.20.1: no config screen (edit `config/skyblock_resources.toml`), tree machine particle `CLOUD`, machines only move items to or from block entities that expose a Forge capability. JEI, Jade 11 and KubeJS 6 integrations are present and compile (Jade and KubeJS are compile-time plus KubeJS loaded in dev; none were exercised). Nothing dropped for Forge.

## Open questions

- Client look at overlays, generator models, JEI pages, Jade tooltip.
- A test with Ecologics and Oh The Trees You'll Grow in a real (non-dev) install.
- `JAVA_HOME` JDK 21 is still needed to run Gradle 8.14; the toolchain compiles with Java 17.
- Nothing is committed; `PROJECT_STATUS.md` is not updated.
