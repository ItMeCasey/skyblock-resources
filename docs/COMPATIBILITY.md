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

Client checks use a dev-only tour (`versions/<build>/src/devtest`, compiled only with `-PviewerCheck`; `SKYRES_SHOTS=bei` for the
recipe viewer, `SKYRES_SHOTS=overlay` for the in-world info mods). It drives the game with its own handlers and reflection; nothing
touches the desktop cursor or keyboard. Each run has one viewer or overlay mod in the dev client (`-PjadeCheck` swaps SMWTI for Jade).
Every frame below was read by eye. Frames are in `docs/frames/`, named `<mod>_<mc>_<loader>_<view>.png`.

| Check | 26.1.2 NeoForge | 26.1.2 Forge | 1.21.1 NeoForge | 1.21.1 Forge | 1.20.1 NeoForge | 1.20.1 Forge |
|---|---|---|---|---|---|---|
| BEI: Resource Generator cards | seen | seen | seen | seen | seen | seen |
| BEI: Tree Drops cards (apple) | seen | seen | seen | seen | seen | seen |
| BEI: Crop Drops card (carrot) | seen | seen | seen | seen | seen | seen |
| BEI: Soil Type card (farmland) | seen | seen | seen | seen | seen | seen |
| Jade: generator progress, "Empty" crop generator, mod name | seen | n/a | seen | n/a | seen | seen |
| SMWTI: generator name, mod, working-on item, progress bar | seen | seen | seen | seen | seen | seen |
| SMWTI: tree and crop generator name and mod | tree and crop | tree only | tree and crop | tree and crop | tree only | tree and crop |
| JEI: Resource Generator, Tree Drops, Crop Drops, Soil Type cards, generators as catalysts | seen | n/a | seen | seen | seen | seen |
| REI: the four cards, generators as workstations | seen | n/a | seen | n/a | seen | seen |
| EMI: the four cards, generators as workstations | n/a | n/a | seen | n/a | seen | seen |
| Crop generator tiers in a live world (side config, break drops, Jade) | n/a | n/a | seen | n/a | n/a | n/a |

Notes on the SMWTI row: a freshly fed tree or crop generator shows no progress line, because it needs a soil and a seed or sapling
the tour does not set up. The "tree only" cells are camera misses in the tour (it aimed at stone terrain), not SMWTI failures; the
generator name was seen on another build of the same version.

Things the checks found and fixed:

- **Forge 26.1.2 showed none of our BEI cards.** Forge 26.1 sends this mod's recipes in a message of its own, which arrived after BEI
  had built its index. `BeiRefresh` now asks BEI to read its stored server recipes again when the message arrives.
- **JEI classes on a viewer-check run.** Another dev mod (Occultism) loads JEI classes when the player logs out, so the 26.1.2
  NeoForge viewer check keeps JEI's API jars on the classpath without the JEI mod.

Not tested, and why:

- **KubeJS** was checked on NeoForge 1.21.1 only (a server script added one recipe of each of our types and all three loaded); the other KubeJS builds share the schema code. Nothing else is untested.

## 26.2 and 26.3 (added 2026-11-12)

Builds that exist for these versions (Modrinth maven and the sibling projects; CurseForge was not queried again):

| Mod | 26.2 NeoForge | 26.2 Forge | 26.3 NeoForge | 26.3 Forge |
|---|---|---|---|---|
| BEI 2026.11.4 | yes | yes | yes | yes |
| SMWTI 2026.11.3 | yes | yes | yes | yes |
| JEI | 30.48.0.244 | none | 31.17.0.109 | none |
| REI | 26.2.820 | none | 26.3.824 | none |
| Jade | 26.2.10 | none | 26.3.5 | none |
| EMI | none | none | none | none |
| KubeJS | none | none | none | none |

Checked in a client (`SKYRES_SHOTS=work`): mod loads, resource generator fills a chest, crop generator grows and delivers, overlays render. Seen on all four builds (`docs/frames/work_<mc>_<loader>_generators.png`). Not opened in a client on these versions: BEI, SMWTI, JEI, REI and Jade (they compile). See `PORT_NOTES_mc26.2_mc26.3.md`.
