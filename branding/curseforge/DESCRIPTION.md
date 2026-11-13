![Skyblock Resources](https://raw.githubusercontent.com/ItMeCasey/skyblock-resources/main/branding/banners/banner.png)

**Generators, trees and crops that turn one block of dirt into a resource engine, from stone up to netherite.**

Skyblock Resources adds resource generators, tree generators and crop generators for skyblock packs. Each machine has a tier, and you upgrade it in place when you are ready for more.

![Features](https://raw.githubusercontent.com/ItMeCasey/skyblock-resources/main/branding/banners/h_features.png)

- **Resource generators:** stone, iron, diamond and netherite. Each tier makes more, faster.
- **Tree generators:** plant a sapling and it grows into a resource tree. The drops depend on the tier.
- **Crop generators:** wheat and produce from other food mods, each with its own growth timer.
- **Upgrade cards:** move a generator, tree or crop up one tier in place. It keeps its stored data.
- **Scripting:** tree and crop drops are plain data, so KubeJS and ModJS packs can add or change plants, and ModJS scripts can react when a generator produces items or is upgraded and read a generator's tier and state.

Minecraft 1.20.1, 1.21.1, 26.1.2, 26.2 and 26.3, on NeoForge and Forge.

![Compatibility](https://raw.githubusercontent.com/ItMeCasey/skyblock-resources/main/branding/banners/h_features.png)

All of these are optional. The mod loads and works with none of them installed.

- **Recipe viewers:** Barely Enough Items, REI, EMI and JEI, on every build where that viewer exists. They show resource generator outputs, tree drops, crop drops and soil types, with every generator as a workstation.
- **In-world info:** Show Me What This Is and Jade show what a generator is working on and how far along it is, on every build where that mod exists.
- **KubeJS:** typed recipe schemas for soil types, tree drops and crop drops.
- **ModJS:** a `SkyblockResources` global with generator events, read access to a generator's tier and state, and recipe helpers.

![FAQ](https://raw.githubusercontent.com/ItMeCasey/skyblock-resources/main/branding/banners/h_faq.png)

**Can I change the drops?** Yes. Tree and crop drops are recipes, so a pack can add or change plants with KubeJS or ModJS.

**License:** GPL-3.0. Source and issues on [GitHub](https://github.com/ItMeCasey/skyblock-resources).
