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

Seen in a client (frames in `docs/frames/`):

- BEI on NeoForge 1.21.1: the browser lists our items, the Wooden Generator recipe popup shows its crafting grid, and the
  Cobblestone popup shows our "Resource Generator" cards (generator tier and resource in, output and count out, ticks below,
  with the generators as the category's workstation tab). Tree drop, crop drop and soil cards were not opened in that run
  (the oak sapling lookup grouped all saplings and did not open a popup).

Not tested, and why:

- BEI on the other five builds, and REI and EMI on every build: the only client tour that exists is a dev-only one for BEI on
  NeoForge 1.21.1 (`versions/mc1.21.1/neoforge/src/devtest`, compiled only with `-PviewerCheck`). Each other viewer needs its own
  tour, so those were not launched. All of them compile.
- JEI, Jade and SMWTI: unchanged by this audit and still compile on every build; not looked at in a client in this run.
