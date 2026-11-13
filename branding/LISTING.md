# Skyblock Resources listing kit: CurseForge and Modrinth

Everything you need to fill in the two project forms. Files are in `branding/icon/` and `branding/banners/`.

## Project fields

| Field | CurseForge | Modrinth |
|---|---|---|
| Project ID | 594061 | _not used yet_ |
| Name | Skyblock Resources | Skyblock Resources |
| Slug / URL | `skyblock-resources` | `skyblock-resources` |
| Icon / logo | `icon/icon_512.png` (square, at least 400x400) | `icon/icon_512.png` (square, under 256 KiB) |
| Class | Mods | Mod |
| Main category | Map and Information or Utility; pick one | Utility |
| Loader | NeoForge, Forge | NeoForge, Forge |
| Game versions | Set on each uploaded file | Set on each uploaded file |
| Environment | Client and server | Client and server |
| License | GPL-3.0 | GPL-3.0 |
| Source | https://github.com/ItMeCasey/skyblock-resources | same |
| Issues | https://github.com/ItMeCasey/skyblock-resources/issues | same |

Replace `skyblock-resources` with the GitHub repository name, which is not set in the project yet.

### One-line description

> Generators, trees and crops that turn one block of dirt into a resource engine, from stone up to netherite.

### Summary

Modrinth (256 characters max):

> Resource generators, tree generators and crop generators for skyblock packs. Upgrade any machine a tier in place with an upgrade card. Supports NeoForge and Forge.

CurseForge (keep it short; it shows on cards):

> Generators, trees and crops that turn one block of dirt into a resource engine.

### Gallery

| File | Use |
|---|---|
| `banners/features.png` (1920x1080) | Featured gallery image on Modrinth, first image on CurseForge |
| `banners/social.png` (1280x640) | GitHub social preview (repo Settings > Social preview), second gallery image |
| `banners/banner.png` (800x150, saved at 2x) | Top of both descriptions |
| `banners/h_*.png` (800x46, saved at 2x) | Section headers inside the descriptions |

The panels in these images are illustrations drawn for the branding, not game screenshots. Add real in-game screenshots to the gallery once the machines have been checked in a client.

### Image links in the descriptions

The descriptions below load images from the GitHub repository, so they only show once `branding/` is pushed and the repository is public. If it stays private, upload the images to the project gallery (Modrinth) or Images tab (CurseForge) and swap in those links.

---

## Description (Modrinth, Markdown)

```markdown
![Skyblock Resources](https://raw.githubusercontent.com/ItMeCasey/skyblock-resources/main/branding/banners/banner.png)

**Generators, trees and crops that turn one block of dirt into a resource engine, from stone up to netherite.**

Skyblock Resources adds resource generators, tree generators and crop generators to skyblock packs. Each machine runs on its own tier, and you upgrade it in place when you are ready for more.

![Features](https://raw.githubusercontent.com/ItMeCasey/skyblock-resources/main/branding/banners/h_features.png)

- **Resource generators:** stone, iron, diamond and netherite. Each tier makes more, faster.
- **Tree generators:** plant a sapling and the tree grows into a resource tree, with the drops set by the tier.
- **Crop generators:** wheat and produce from other food mods. Crops keep their own growth timers.
- **Upgrade cards:** move a generator, tree or crop up one tier in place. It keeps its stored data.
- **Scripting:** tree and crop drops are plain data, so KubeJS and ModJS packs can add or change plants, and ModJS scripts can react when a generator produces items or is upgraded and read a generator's tier and state.

Minecraft 1.20.1, 1.21.1, 26.1.2, 26.2 and 26.3, on NeoForge and Forge.

![FAQ](https://raw.githubusercontent.com/ItMeCasey/skyblock-resources/main/branding/banners/h_faq.png)

**Can I change the drops?** Yes. Tree and crop drops are recipes, so a pack can add or change plants with KubeJS or ModJS.

**License:** GPL-3.0. Source and issues on [GitHub](https://github.com/ItMeCasey/skyblock-resources).
```

## Checklist

1. Fill in the `skyblock-resources` placeholder and the project IDs, after the GitHub repository name is set.
2. Push `branding/` to GitHub (and make the repository public) so the description images load.
3. Create the Modrinth project: name, slug, summary, icon, categories, environment, license, links, description. Upload `features.png` as the featured gallery image.
4. Create the CurseForge project the same way, with `icon_512.png` as the logo. CurseForge reviews new projects before they go live, so it needs a first file uploaded.
5. Set `banners/social.png` as the GitHub social preview.
6. Before publishing, check the claims in the description in a client: the tiers and upgrade cards. The status file lists the live-client checks still open.
7. Optional: add `icon/skyblock_logo.png` to the jar as the mod list logo (`logoFile` in `mods.toml`, file in the resources folder).
