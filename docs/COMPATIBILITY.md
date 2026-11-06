# Compatibility

Which viewer and overlay mods have a build for each of our six builds, looked up on CurseForge on 2026-11-05 with the
`curseforge-lookup` skill. Every integration is optional: Skyblock Resources loads and works with none of them installed.

On 1.20.1, NeoForge runs the Forge build of a mod. Where CurseForge lists only a Forge file for 1.20.1 (REI, JEI, EMI), the
1.20.1 NeoForge build uses that Forge file.

## Builds that exist

| Mod | 26.1.2 NeoForge | 26.1.2 Forge | 1.21.1 NeoForge | 1.21.1 Forge | 1.20.1 NeoForge | 1.20.1 Forge |
|---|---|---|---|---|---|---|
| BEI 2026.11.4 | yes | yes | yes | yes | yes | yes |
| JEI | 29.53.0.115 | none | 19.70.0.469 | 19.70.0.469 | Forge file | 15.74.0.234 |
| REI | 26.1.819 (beta) | none | 16.0.799 (beta) | none | Forge file | 12.1.785 (beta) |
| EMI | none | none | 1.1.24 | none | Forge file | 1.1.24 |
| Jade | 26.1.12 | none | 15.10.6 | none | 11.13.3 | 11.13.3 |
| SMWTI 2026.11.2 | yes | yes | yes | yes | yes | yes |

"none" means CurseForge has no file for that Minecraft version and loader, so there is nothing to connect to.

## Integrations in this mod

| Mod | 26.1.2 NeoForge | 26.1.2 Forge | 1.21.1 NeoForge | 1.21.1 Forge | 1.20.1 NeoForge | 1.20.1 Forge |
|---|---|---|---|---|---|---|
| BEI | yes | yes | yes | yes | yes | yes |
| JEI | yes | n/a | yes | yes | yes | yes |
| REI | yes | n/a | yes | n/a | yes | yes |
| EMI | n/a | n/a | yes | n/a | yes | yes |
| Jade | yes | n/a | yes | n/a | yes | yes |
| SMWTI | yes | yes | yes | yes | yes | yes |

"n/a" means the viewer has no build there. "yes" means the plugin is in the jar, compiles, and loads only when that mod is
installed. The BEI, REI and EMI plugins show four kinds of card: resource generator outputs, tree drops, crop drops and soil
types, each with the generators of its family as workstations. They read the same recipes as JEI through
`integration/viewer/ViewerCards`.

## Client checks

Client checks use a dev-only tour (`versions/<build>/src/devtest`, compiled only with `-PviewerCheck`, started with
`SKYRES_SHOTS=bei`). It drives the game with its own handlers and reflection; nothing touches the desktop cursor or keyboard. Every
frame below was read by eye. Frames are in `docs/frames/`, named `bei_<mc>_<loader>_<view>.png`.

| Check | 26.1.2 NeoForge | 26.1.2 Forge | 1.21.1 NeoForge | 1.21.1 Forge | 1.20.1 NeoForge | 1.20.1 Forge |
|---|---|---|---|---|---|---|
| BEI: our items in the browser, generator popup, Resource Generator cards | seen | seen | seen | seen | seen | seen |
| BEI: Tree Drops cards (apple popup) | seen | seen | not opened | seen | seen | seen |
| BEI: Crop Drops card (carrot popup) | seen | seen | not opened | seen | seen | seen |
| BEI: Soil Type card (farmland popup) | seen | seen | not opened | seen | seen | seen |

The NeoForge 1.21.1 frames are from the first run, before the tour opened tree, crop and soil popups; they show the resource
generator cards and the generator recipe only.

Two things the checks found and fixed:

- **Forge 26.1.2 showed none of our cards.** Forge 26.1 sends this mod's recipes in a message of its own, which arrived after BEI had
  built its index. `BeiRefresh` now asks BEI to read its stored server recipes again when the message arrives.
- **JEI classes on a viewer-check run.** Another dev mod (Occultism) loads JEI classes when the player logs out, so the 26.1.2
  NeoForge viewer check keeps JEI's API jars on the classpath without the JEI mod.

Not tested, and why:

- **REI and EMI on every build, and JEI, Jade and SMWTI on every build:** the tours so far only drive BEI. Each other viewer needs
  its own tour step. They compile, and JEI, Jade and SMWTI were not changed by this work.
