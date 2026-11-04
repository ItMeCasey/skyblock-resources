# Skyblock Resources branding

Logo, banners and store listing copy for Skyblock Resources, in the same style as SMWTI and BEI.

- `icon/`: the logo, an empty diamond cube frame, 32x32 pixel art scaled up without smoothing. `icon_<size>.png` for every size, `icon.svg` for vector use, `skyblock_logo.png` for the jar. Use `icon_512.png` for CurseForge and Modrinth.
- `banners/`: description banner, gallery features card, social preview and section headers.
- `LISTING.md`: CurseForge and Modrinth fields, summaries, description text and a setup checklist.
- `source/`: `make_icon.py` draws the logo; `banners.html` draws everything (original pixel font and sprites, no Minecraft textures); `render_banners.py` screenshots it.

Regenerate from the repository root (needs Python, Pillow and Google Chrome):

```
python branding/source/make_icon.py
python branding/source/render_banners.py
```

## Palette

| Name | Hex | Use |
|---|---|---|
| Night | `#0f0e1a` to `#1a1830` | Backgrounds |
| Edge | `#2e2a46` | Borders |
| Diamond | `#4ae2ff` (light `#e0ffff`, dark `#1b9fc0`) | Wordmark, logo frame, accents |
| Gold | `#f2b33d` | Sparkle on the logo |
| Mod blue | `#7aa8ff` | Mod names in panels |
| Text | `#f4f2ff`, muted `#a9a4c8` | Copy |
