# Scripting

Skyblock Resources works with two script engines. Both are optional: without them nothing loads and nothing fails.

| Engine | What you get | Builds |
|---|---|---|
| KubeJS | typed recipe schemas `soil_type`, `tree_drops`, `crop_drops` | NeoForge 26.1.2, NeoForge 1.21.1, both 1.20.1 builds (KubeJS has no build for the others) |
| ModJS | the `SkyblockResources` global: generator events, generator state, recipe JSON helpers | all ten builds |

KubeJS recipes are described in the wiki page "KubeJS". This page is the ModJS reference.

## The `SkyblockResources` global

Available in every ModJS script type. State reads and events only work with a server world, so use them in server scripts.

| Call | Returns |
|---|---|
| `SkyblockResources.on(event, handler)` | registers a listener for `'produced'` or `'upgraded'` |
| `SkyblockResources.info(level, x, y, z)` | a snapshot of the generator at the position, or `null` |
| `SkyblockResources.isGenerator(level, x, y, z)` | whether a generator is at the position |
| `SkyblockResources.tiers()` | `['wooden', 'stone', 'iron', 'diamond', 'netherite']` |
| `SkyblockResources.recipes.*` | recipe JSON builders, see below |

`level` is the world a ModJS event gives you (`event.level`). Listeners are removed and registered again when scripts reload.
A listener that throws is logged on one line and skipped; the game and the other listeners carry on.

### Events

`'produced'` fires when a generator puts items into its inventory (once per item type per cycle). `'upgraded'` fires after an upgrade
card moved a generator up a tier. The handler receives:

| Field | Meaning |
|---|---|
| `name` | `produced` or `upgraded` |
| `kind` | `resource`, `tree` or `crop` |
| `tier`, `tierName` | 0 to 4, and `wooden` to `netherite` (the new tier for `upgraded`) |
| `dimension` | for example `minecraft:overworld` |
| `x`, `y`, `z` | the generator's position |
| `item`, `count` | what was produced (empty and 0 for `upgraded`) |

### Reading a generator

`info(...)` returns an object with `block`, `kind`, `tier`, `tierName`, `resource` (the item being made, or the sapling or seed in
use, `empty` when none), `soil`, `progress` (0 to 1), `duration` (ticks per cycle), `outputDirection` and `items` (item id to total
count inside). It never changes the generator.

### Recipes

`SkyblockResources.recipes` builds the JSON of this mod's recipe types as text. Give it to ModJS's recipe event with `custom` and
remove recipes with ModJS's own filters.

| Builder | Recipe type |
|---|---|
| `resourceGenerator(generator, resource, output, count, duration)` | `skyblock_resources:resource_generator` |
| `soil(soil, speedBonus, growthMultiplier, bonusSeedChance)` | `skyblock_resources:soil_type` |
| `treeDrops(sapling, soil, generator, output, count, drops, duration)` | `skyblock_resources:tree_drops` |
| `cropDrops(seed, soil, generator, output, count, drops, duration)` | `skyblock_resources:crop_drops` |

`drops` is a list of `{ id, min, max, chance }` objects (`max` defaults to `min`, `chance` to 1). `generator` and `soil` accept a block
id or a `#tag`.

## Example

```js
// server_scripts/generators.js
SkyblockResources.on('produced', e => {
  if (e.kind === 'crop' && e.tier === 4) {
    console.info('netherite crop generator at ' + e.x + ' ' + e.y + ' ' + e.z + ' made ' + e.count + ' ' + e.item);
  }
});

SkyblockResources.on('upgraded', e => console.info('a ' + e.kind + ' generator is now ' + e.tierName));

ServerEvents.recipes(event => {
  // a new crop
  event.custom(JSON.parse(SkyblockResources.recipes.cropDrops(
    'minecraft:beetroot_seeds', '#skyblock_resources:crop_soils', '#skyblock_resources:crop_generators',
    'minecraft:beetroot', 1, [{ id: 'minecraft:beetroot', min: 1, max: 2 }], 800)));
  // drop every soil type, then add one
  event.remove({ type: 'skyblock_resources:soil_type' });
  event.custom(JSON.parse(SkyblockResources.recipes.soil('minecraft:moss_block', 0.2, 1.5, 0)));
});
```

## Without ModJS

The bridge looks for ModJS by name when the mod starts. When ModJS is not installed it does nothing. The headless self test
(`gradlew runGameTest`) runs the events, the state read and the recipe builders without ModJS.
