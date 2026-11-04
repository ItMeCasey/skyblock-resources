package com.itmecasey.skyblockresources.jade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.config.IPluginConfig;
import static com.itmecasey.skyblockresources.jade.SkyblockResourcesJadePlugin.*;

/**
 * Client-only Jade UI: arrows, bars, timers, and the tooltip provider.
 * Kept out of the plugin class so dedicated servers never load client classes.
 */
public final class SkyblockResourcesJadeClient {
    private SkyblockResourcesJadeClient() {}

    static void register(IWailaClientRegistration registration) {
        registration.addConfig(JADE_BLOCK_PROVIDER, true);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                CompoundTag serverData = accessor.getServerData();
                BlockEntity be = accessor.getBlockEntity();

                // Server data is already the fully-merged, filtered snapshot (see appendServerData below),
                // so once it has synced we can read it directly instead of re-copying NBT every frame.
                boolean serverSynced = serverData != null && serverData.contains("JadeSynced");
                CompoundTag data;
                if (serverSynced) {
                    data = serverData;
                } else {
                    data = new CompoundTag();
                    if (be != null && be.getPersistentData() != null) {
                        copyAllTags(be.getPersistentData(), data);
                        try {
                            Tag forgeData = be.getPersistentData().get("ForgeData");
                            if (forgeData instanceof CompoundTag) {
                                copyAllTags((CompoundTag) forgeData, data);
                            }
                        } catch (Throwable ignored) {}
                    }
                    if (serverData != null) {
                        copyAllTags(serverData, data);
                    }
                }

                boolean extended = isShiftPressed(accessor);
                ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(accessor.getBlock());
                String blockId = blockKey.toString();
                String blockPath = blockKey.getPath();
                boolean blockMatched = false;

                // Block Config: Wooden Generator [skyblock_resources:wooden_generator]
                if (blockId.equalsIgnoreCase("skyblock_resources:wooden_generator") || blockPath.equalsIgnoreCase("wooden_generator")) {
                    blockMatched = true;
                    if (data.contains("generated_item") && true) {
                        try {
                            Tag t = data.get("generated_item");
                            if (t != null) {
                                String val = formatNbtValue(t, "ITEM_BLOCK_NAME");
                                String formatted = "{label}: {value}".replace("{label}", "Resource").replace("{value}", val);
                                MutableComponent line = Component.literal(formatted).withStyle(style -> style.withColor(0xffffff55));
                                ItemStack iconStack = resolveItemStack(t);
                                if (iconStack != null && !iconStack.isEmpty()) {
                                    tooltip.add(snownee.jade.api.ui.IElementHelper.get().smallItem(iconStack));
                                    tooltip.append(snownee.jade.api.ui.IElementHelper.get().text(line).translate(new net.minecraft.world.phys.Vec2(0, 2)));
                                } else {
                                    tooltip.add(line);
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                    double cur_timer = findNumber(data, "timer", "timer", "TIMER");
                    double max_timer = findNumber(data, "duration", "duration", "DURATION");
                    if (cur_timer >= 0 && true) {
                        if (max_timer <= 0) max_timer = 100.0;
                        float ratio = (float) Math.min(Math.max(cur_timer / max_timer, 0.0), 1.0);
                        int percent = (int) (ratio * 100);
                        renderJadeBar(tooltip, "skyblock_resources:wooden_generator_timer_" + accessor.getPosition().getX() + "_" + accessor.getPosition().getY() + "_" + accessor.getPosition().getZ(), "", ratio, percent, "IN_BAR", 10, 0xff339900, 0x0, 0x0, 0xff666666, 0xffffffff, true);
                    }
                }

                // Block Config: Stone Generator [skyblock_resources:stone_generator]
                if (blockId.equalsIgnoreCase("skyblock_resources:stone_generator") || blockPath.equalsIgnoreCase("stone_generator")) {
                    blockMatched = true;
                    double cur_timer = findNumber(data, "timer", "timer", "TIMER");
                    double max_timer = findNumber(data, "duration", "duration", "DURATION");
                    if (cur_timer >= 0 && true) {
                        if (max_timer <= 0) max_timer = 100.0;
                        float ratio = (float) Math.min(Math.max(cur_timer / max_timer, 0.0), 1.0);
                        int percent = (int) (ratio * 100);
                        renderJadeBar(tooltip, "skyblock_resources:stone_generator_timer_" + accessor.getPosition().getX() + "_" + accessor.getPosition().getY() + "_" + accessor.getPosition().getZ(), "", ratio, percent, "IN_BAR", 12, 0xff4caf50, 0x0, 0x0, 0xff666666, 0xffffffff, true);
                    }
                }

                // Block Config: Iron Generator [skyblock_resources:iron_generator]
                if (blockId.equalsIgnoreCase("skyblock_resources:iron_generator") || blockPath.equalsIgnoreCase("iron_generator")) {
                    blockMatched = true;
                    double cur_timer = findNumber(data, "timer", "timer", "TIMER");
                    double max_timer = findNumber(data, "duration", "duration", "DURATION");
                    if (cur_timer >= 0 && true) {
                        if (max_timer <= 0) max_timer = 100.0;
                        float ratio = (float) Math.min(Math.max(cur_timer / max_timer, 0.0), 1.0);
                        int percent = (int) (ratio * 100);
                        renderJadeBar(tooltip, "skyblock_resources:iron_generator_timer_" + accessor.getPosition().getX() + "_" + accessor.getPosition().getY() + "_" + accessor.getPosition().getZ(), "", ratio, percent, "IN_BAR", 12, 0xff4caf50, 0x0, 0x0, 0xffffffff, 0xffffffff, true);
                    }
                }

                // Block Config: Diamond Generator [skyblock_resources:diamond_generator]
                if (blockId.equalsIgnoreCase("skyblock_resources:diamond_generator") || blockPath.equalsIgnoreCase("diamond_generator")) {
                    blockMatched = true;
                    double cur_timer = findNumber(data, "timer", "timer", "TIMER");
                    double max_timer = findNumber(data, "duration", "duration", "DURATION");
                    if (cur_timer >= 0 && true) {
                        if (max_timer <= 0) max_timer = 100.0;
                        float ratio = (float) Math.min(Math.max(cur_timer / max_timer, 0.0), 1.0);
                        int percent = (int) (ratio * 100);
                        renderJadeBar(tooltip, "skyblock_resources:diamond_generator_timer_" + accessor.getPosition().getX() + "_" + accessor.getPosition().getY() + "_" + accessor.getPosition().getZ(), "", ratio, percent, "IN_BAR", 12, 0xff4caf50, 0x0, 0x0, 0xffffffff, 0xffffffff, true);
                    }
                }

                // Block Config: Netherite Generator [skyblock_resources:netherite_generator]
                if (blockId.equalsIgnoreCase("skyblock_resources:netherite_generator") || blockPath.equalsIgnoreCase("netherite_generator")) {
                    blockMatched = true;
                    double cur_timer = findNumber(data, "timer", "timer", "TIMER");
                    double max_timer = findNumber(data, "max_timer", "max_timer", "MAX_TIMER");
                    if (cur_timer >= 0 && true) {
                        if (max_timer <= 0) max_timer = 100.0;
                        float ratio = (float) Math.min(Math.max(cur_timer / max_timer, 0.0), 1.0);
                        int percent = (int) (ratio * 100);
                        renderJadeBar(tooltip, "skyblock_resources:netherite_generator_timer_" + accessor.getPosition().getX() + "_" + accessor.getPosition().getY() + "_" + accessor.getPosition().getZ(), "", ratio, percent, "IN_BAR", 12, 0xff4caf50, 0x0, 0x0, 0xffffffff, 0xffffffff, true);
                    }
                }

                // Block Config: Wooden Tree Generator [skyblock_resources:wooden_tree_generator]
                if (blockId.equalsIgnoreCase("skyblock_resources:wooden_tree_generator") || blockPath.equalsIgnoreCase("wooden_tree_generator")) {
                    blockMatched = true;
                    double cur_timer = findNumber(data, "timer", "timer", "TIMER");
                    double max_timer = findNumber(data, "duration", "duration", "DURATION");
                    if (cur_timer >= 0 && true) {
                        if (max_timer <= 0) max_timer = 100.0;
                        float ratio = (float) Math.min(Math.max(cur_timer / max_timer, 0.0), 1.0);
                        int percent = (int) (ratio * 100);
                        renderJadeBar(tooltip, "skyblock_resources:wooden_tree_generator_timer_" + accessor.getPosition().getX() + "_" + accessor.getPosition().getY() + "_" + accessor.getPosition().getZ(), "", ratio, percent, "IN_BAR", 10, 0xff4caf50, 0x0, 0x0, 0xffffffff, 0xffffffff, true);
                    }
                    if (data.contains("growth_multiplier") && extended) {
                        try {
                            Tag t = data.get("growth_multiplier");
                            if (t != null) {
                                String val = formatNbtValue(t, "INTEGER");
                                String formatted = "{label}  {value}x".replace("{label}", "Multiplier").replace("{value}", val);
                                MutableComponent line = Component.literal(formatted).withStyle(style -> style.withColor(0xff33cc00));
                                tooltip.add(snownee.jade.api.ui.IElementHelper.get().text(line).scale(0.65f));
                            }
                        } catch (Throwable ignored) {}
                    }
                    if (data.contains("speed_bonus") && extended) {
                        try {
                            Tag t = data.get("speed_bonus");
                            if (t != null) {
                                String val = formatNbtValue(t, "PERCENT");
                                String formatted = "{label}: {value}".replace("{label}", "Speed").replace("{value}", val);
                                MutableComponent line = Component.literal(formatted).withStyle(style -> style.withColor(0xff33cc00));
                                tooltip.add(snownee.jade.api.ui.IElementHelper.get().text(line).scale(0.65f));
                            }
                        } catch (Throwable ignored) {}
                    }
                }

                // Block Config: WoodenCropGenerator - Block [skyblock_resources:wooden_crop_generator]
                if (blockId.equalsIgnoreCase("skyblock_resources:wooden_crop_generator") || blockPath.equalsIgnoreCase("wooden_crop_generator")) {
                    blockMatched = true;
                    if (data.contains("resource_name") && true) {
                        try {
                            Tag t = data.get("resource_name");
                            if (t != null) {
                                String val = formatNbtValue(t, "ITEM_BLOCK_NAME");
                                String formatted = "{value}".replace("{label}", "resource_name").replace("{value}", val);
                                MutableComponent line = Component.literal(formatted).withStyle(style -> style.withColor(0xff0099ff));
                                ItemStack iconStack = resolveItemStack(t);
                                if (iconStack != null && !iconStack.isEmpty()) {
                                    tooltip.add(snownee.jade.api.ui.IElementHelper.get().smallItem(iconStack));
                                    tooltip.append(snownee.jade.api.ui.IElementHelper.get().text(line).translate(new net.minecraft.world.phys.Vec2(0, 2)));
                                } else {
                                    tooltip.add(line);
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                    double cur_timer = findNumber(data, "timer", "timer", "TIMER");
                    double max_timer = findNumber(data, "duration", "duration", "DURATION");
                    if (cur_timer >= 0 && true) {
                        if (max_timer <= 0) max_timer = 100.0;
                        float ratio = (float) Math.min(Math.max(cur_timer / max_timer, 0.0), 1.0);
                        int percent = (int) (ratio * 100);
                        renderJadeBar(tooltip, "skyblock_resources:wooden_crop_generator_timer_" + accessor.getPosition().getX() + "_" + accessor.getPosition().getY() + "_" + accessor.getPosition().getZ(), "", ratio, percent, "IN_BAR", 10, 0xff4caf50, 0x0, 0x0, 0xffffffff, 0xffffffff, true);
                    }
                    if (data.contains("growth_multiplier") && extended) {
                        try {
                            Tag t = data.get("growth_multiplier");
                            if (t != null) {
                                String val = formatNbtValue(t, "INTEGER");
                                String formatted = "{label}  {value}x".replace("{label}", "Multiplier").replace("{value}", val);
                                MutableComponent line = Component.literal(formatted).withStyle(style -> style.withColor(0xff33cc00));
                                tooltip.add(snownee.jade.api.ui.IElementHelper.get().text(line).scale(0.65f));
                            }
                        } catch (Throwable ignored) {}
                    }
                    if (data.contains("speed_bonus") && extended) {
                        try {
                            Tag t = data.get("speed_bonus");
                            if (t != null) {
                                String val = formatNbtValue(t, "PERCENT");
                                String formatted = "{label}: {value}".replace("{label}", "Speed").replace("{value}", val);
                                MutableComponent line = Component.literal(formatted).withStyle(style -> style.withColor(0xff33cc00));
                                tooltip.add(snownee.jade.api.ui.IElementHelper.get().text(line).scale(0.65f));
                            }
                        } catch (Throwable ignored) {}
                    }
                }

            }

            @Override
            public boolean isRequired() {
                return true;
            }

            @Override
            public ResourceLocation getUid() {
                return JADE_BLOCK_PROVIDER;
            }
        }, Block.class);
    }

    private static void renderArrow(ITooltip tooltip, String label, float ratio, int percent, String pctPos, int barColor, int bgColor, int borderColor, int textColor, boolean textShadow) {
        try {
            if ("ABOVE".equals(pctPos)) {
                tooltip.add(Component.literal((label.isEmpty() ? "" : (label + ": ")) + "§e" + percent + "%"));
                tooltip.add(snownee.jade.api.ui.IElementHelper.get().progress(ratio));
            } else {
                if (!label.isEmpty()) {
                    tooltip.add(Component.literal("§f" + label + ": "));
                    tooltip.append(snownee.jade.api.ui.IElementHelper.get().progress(ratio));
                } else {
                    tooltip.add(snownee.jade.api.ui.IElementHelper.get().progress(ratio));
                }
                if (!"NONE".equals(pctPos)) {
                    tooltip.append(snownee.jade.api.ui.IElementHelper.get().text(Component.literal(" §e" + percent + "%")).translate(new net.minecraft.world.phys.Vec2(0, 2)));
                }
            }
        } catch (Throwable fallback) {
            renderBlockUnicodeBar(tooltip, label, ratio, percent, pctPos, barColor, bgColor, borderColor, textColor, textShadow);
        }
    }

    private static void renderJadeBar(ITooltip tooltip, String animKey, String label, float ratio, int percent, String pctPos, int barHeight, int barColor, int bgStartColor, int bgEndColor, int borderColor, int textColor, boolean textShadow) {
        try {
            if (!label.isEmpty()) {
                if ("ABOVE".equals(pctPos)) {
                    tooltip.add(Component.literal(label + ": " + percent + "%").withStyle(s -> s.withColor(textColor)));
                } else {
                    tooltip.add(Component.literal(label).withStyle(s -> s.withColor(textColor)));
                }
            } else if ("ABOVE".equals(pctPos)) {
                tooltip.add(Component.literal(percent + "%").withStyle(s -> s.withColor(textColor)));
            }
            int effectiveH = barHeight > 0 ? barHeight : 12;
            JadeBarElement barElem = new JadeBarElement(animKey, 120, effectiveH, ratio, percent, pctPos, barColor, bgStartColor, bgEndColor, borderColor, textColor, textShadow);
            barElem.size(new net.minecraft.world.phys.Vec2(120, effectiveH));
            try {
                barElem.tag(ResourceLocation.tryParse("jade:progress_" + Math.abs(animKey.hashCode())));
            } catch (Throwable ignored) {}
            if ("TRAILING".equals(pctPos)) {
                tooltip.add(barElem);
                tooltip.append(snownee.jade.api.ui.IElementHelper.get().text(Component.literal(" " + percent + "%").withStyle(s -> s.withColor(textColor))).translate(new net.minecraft.world.phys.Vec2(0, 2)));
            } else {
                tooltip.add(barElem);
            }
        } catch (Throwable fallback) {
            renderBlockUnicodeBar(tooltip, label, ratio, percent, pctPos, barColor, bgStartColor, borderColor, textColor, textShadow);
        }
    }

    private static class JadeBarElement extends snownee.jade.api.ui.Element {
        private static final java.util.Map<String, Float> PROGRESS_ANIM_VALUES = new java.util.concurrent.ConcurrentHashMap<>();
        private static final java.util.Map<String, Long> PROGRESS_ANIM_TIMES = new java.util.concurrent.ConcurrentHashMap<>();

        private int width;
        private int height;
        private final String animKey;
        private final int barHeight;
        private final float ratio;
        private final int percent;
        private final String pctPos;
        private final int barColor;
        private final int bgStartColor;
        private final int bgEndColor;
        private final int borderColor;
        private final int textColor;
        private final boolean textShadow;

        public JadeBarElement(String animKey, int w, int h, float ratio, int percent, String pctPos, int barColor, int bgStartColor, int bgEndColor, int borderColor, int textColor, boolean textShadow) {
            this.animKey = animKey != null ? animKey : "progress";
            this.width = Math.max(2, w);
            this.height = Math.max(2, h);
            this.barHeight = this.height;
            this.ratio = Math.max(0.0f, Math.min(1.0f, ratio));
            this.percent = percent;
            this.pctPos = pctPos != null ? pctPos : "NONE";
            this.barColor = barColor;
            this.bgStartColor = bgStartColor;
            this.bgEndColor = bgEndColor;
            this.borderColor = borderColor;
            this.textColor = textColor;
            this.textShadow = textShadow;
            this.size(new net.minecraft.world.phys.Vec2(this.width, this.height));
        }

        @Override
        public net.minecraft.world.phys.Vec2 getSize() {
            return new net.minecraft.world.phys.Vec2(this.width, this.height);
        }

        @Override
        public void render(net.minecraft.client.gui.GuiGraphics graphics, float fx, float fy, float maxX, float maxY) {
            int x = Math.round(fx);
            int y = Math.round(fy);
            int w = Math.round(maxX - fx);
            int h = this.barHeight > 0 ? this.barHeight : Math.round(maxY - fy);
            if (w <= 0 || h <= 0) return;

            // Interpolate progress ratio when growing, but snap immediately when resetting to 0 (or decreasing)
            float targetRatio = this.ratio;
            long now = System.currentTimeMillis();
            Float prevVal = PROGRESS_ANIM_VALUES.get(animKey);
            Long prevTime = PROGRESS_ANIM_TIMES.get(animKey);
            float currentRatio;
            if (prevVal == null || (prevTime != null && (now - prevTime) > 500L)) {
                currentRatio = targetRatio;
            } else if (targetRatio <= 0.0001f || targetRatio < prevVal) {
                currentRatio = targetRatio;
            } else {
                long dt = Math.min(100L, Math.max(1L, now - prevTime));
                float factor = 1.0f - (float) Math.exp(-10.0f * (dt / 1000.0f));
                currentRatio = prevVal + (targetRatio - prevVal) * factor;
                if (Math.abs(targetRatio - currentRatio) < 0.0005f) {
                    currentRatio = targetRatio;
                }
            }
            PROGRESS_ANIM_VALUES.put(animKey, currentRatio);
            PROGRESS_ANIM_TIMES.put(animKey, now);

            // Draw 1px border
            graphics.fill(x, y, x + w, y + 1, borderColor);
            graphics.fill(x, y + h - 1, x + w, y + h, borderColor);
            graphics.fill(x, y + 1, x + 1, y + h - 1, borderColor);
            graphics.fill(x + w - 1, y + 1, x + w, y + h - 1, borderColor);

            int innerX = x + 1;
            int innerY = y + 1;
            int innerW = Math.max(0, w - 2);
            int innerH = Math.max(0, h - 2);

            if (innerW > 0 && innerH > 0) {
                // Background (gradient or solid)
                if (bgStartColor == bgEndColor) {
                    graphics.fill(innerX, innerY, innerX + innerW, innerY + innerH, bgStartColor);
                } else {
                    for (int col = 0; col < innerW; col++) {
                        float t = (float) col / Math.max(1, innerW - 1);
                        int c = blendColor(bgStartColor, bgEndColor, t);
                        graphics.fill(innerX + col, innerY, innerX + col + 1, innerY + innerH, c);
                    }
                }

                // Filled portion (uses interpolated currentRatio)
                int filledW = Math.round(innerW * currentRatio);
                if (filledW > innerW) filledW = innerW;
                if (filledW > 0) {
                    graphics.fill(innerX, innerY, innerX + filledW, innerY + innerH, barColor);
                }
            }

            // In-bar percentage text (centered, scale down if bar is small, never up)
            if ("IN_BAR".equals(pctPos)) {
                int displayPercent = Math.min(100, Math.max(0, Math.round(currentRatio * 100.0f)));
                String text = displayPercent + "%";
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.font != null) {
                    int textW = mc.font.width(text);
                    int fontH = 9;
                    int availH = Math.max(1, h - 2);
                    float scale = Math.min(1.0f, (float) availH / (float) fontH);
                    float scaledW = textW * scale;
                    float scaledH = fontH * scale;
                    float textX = x + (w - scaledW) / 2.0f;
                    float textY = y + (h - scaledH) / 2.0f;
                    graphics.pose().pushPose();
                    graphics.pose().translate(textX, textY, 0.0f);
                    if (scale < 1.0f) {
                        graphics.pose().scale(scale, scale, 1.0f);
                    }
                    snownee.jade.api.ui.IDisplayHelper.get().drawText(graphics, text, 0.0f, 0.0f, textColor);
                    graphics.pose().popPose();
                }
            }
        }

        private static int blendColor(int first, int second, float amount) {
            int a = (int) (((first >>> 24) & 255) + (((second >>> 24) & 255) - ((first >>> 24) & 255)) * amount);
            int r = (int) (((first >>> 16) & 255) + (((second >>> 16) & 255) - ((first >>> 16) & 255)) * amount);
            int g = (int) (((first >>> 8) & 255) + (((second >>> 8) & 255) - ((first >>> 8) & 255)) * amount);
            int b = (int) ((first & 255) + ((second & 255) - (first & 255)) * amount);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    private static void renderBlockUnicodeBar(ITooltip tooltip, String label, float ratio, int percent, String pctPos, int barColor, int bgColor, int borderColor, int textColor, boolean textShadow) {
        int totalBars = 12;
        int filled = (int) Math.round(ratio * totalBars);
        MutableComponent barComp = Component.literal("");
        barComp.append(Component.literal("[").withStyle(s -> s.withColor(borderColor)));
        for (int i = 0; i < totalBars; i++) {
            if ("IN_BAR".equals(pctPos) && i == totalBars / 2) {
                barComp.append(Component.literal(" " + percent + "% ").withStyle(s -> s.withColor(0xFFFFFFFF)));
            }
            if (i < filled) {
                barComp.append(Component.literal("█").withStyle(s -> s.withColor(barColor)));
            } else {
                barComp.append(Component.literal("░").withStyle(s -> s.withColor(bgColor)));
            }
        }
        barComp.append(Component.literal("]").withStyle(s -> s.withColor(borderColor)));
        MutableComponent line = label.isEmpty() ? Component.literal("") : Component.literal("§f" + label + ": ");
        line.append(barComp);
        if ("TRAILING".equals(pctPos)) {
            line.append(Component.literal(" §e" + percent + "%"));
        }
        tooltip.add(line);
    }

    private static void renderTimer(ITooltip tooltip, String label, String timerKey, long durationSeconds, String pctPos, int barColor, int bgStartColor, int bgEndColor, int borderColor, int textColor, boolean textShadow) {
        long start = TIMER_START_TIMES.computeIfAbsent(timerKey, key -> System.currentTimeMillis());
        int percent = (int) Math.min(100L, Math.max(0L, (System.currentTimeMillis() - start) * 100L / (Math.max(1L, durationSeconds) * 1000L)));
        if ("ABOVE".equals(pctPos)) {
            tooltip.add(Component.literal((label == null || label.isEmpty() ? "" : label + ": " ) + percent + "%"));
        } else if (label != null && !label.isEmpty()) {
            tooltip.add(Component.literal("§f" + label + "\n"));
        }
        TimerElement timer = new TimerElement(timerKey, durationSeconds, 24, pctPos, barColor, bgStartColor, bgEndColor, borderColor, textColor);
        tooltip.add(timer);
        if ("TRAILING".equals(pctPos)) {
            tooltip.append(snownee.jade.api.ui.IElementHelper.get().text(Component.literal(" " + percent + "%")).translate(new net.minecraft.world.phys.Vec2(0, 2)));
        }
    }

    private static final java.util.Map<String, Long> TIMER_START_TIMES = new java.util.concurrent.ConcurrentHashMap<>();

    private static class TimerElement extends snownee.jade.api.ui.Element {
        private static final java.util.Map<String, Long> START_TIMES = new java.util.concurrent.ConcurrentHashMap<>();
        private int width;
        private int height;
        private final String timerKey;
        private final long durationMillis;
        private final String pctPos;
        private final int barColor;
        private final int bgStartColor;
        private final int bgEndColor;
        private final int borderColor;

        private final int textColor;

        private TimerElement(String timerKey, long durationSeconds, int size, String pctPos, int barColor, int bgStartColor, int bgEndColor, int borderColor, int textColor) {
            this.timerKey = timerKey;
            this.durationMillis = Math.max(1L, durationSeconds) * 1000L;
            this.pctPos = pctPos != null ? pctPos : "NONE";
            this.width = size;
            this.height = size;
            this.barColor = barColor;
            this.bgStartColor = bgStartColor;
            this.bgEndColor = bgEndColor;
            this.borderColor = borderColor;
            this.textColor = textColor;
        }

        @Override
        public net.minecraft.world.phys.Vec2 getSize() {
            return new net.minecraft.world.phys.Vec2(this.width, this.height);
        }

        @Override
        public void render(net.minecraft.client.gui.GuiGraphics graphics, float fx, float fy, float maxX, float maxY) {
            int elemWidth = Math.round(maxX - fx);
            int elemHeight = Math.round(maxY - fy);
            long start = TIMER_START_TIMES.computeIfAbsent(timerKey, key -> System.currentTimeMillis());
            float progress = Math.min(1.0f, Math.max(0.0f, (System.currentTimeMillis() - start) / (float) durationMillis));
            int percent = (int) (progress * 100.0f);
            int centerX = Math.round(fx) + elemWidth / 2;
            int centerY = Math.round(fy) + elemHeight / 2;
            int radius = Math.max(3, Math.min(elemWidth, elemHeight) / 2 - 3);
            int segments = 72;
            for (int i = 0; i < segments; i++) {
                double angle = -Math.PI / 2.0 + (Math.PI * 2.0 * i / segments);
                int px = centerX + (int) Math.round(Math.cos(angle) * radius);
                int py = centerY + (int) Math.round(Math.sin(angle) * radius);
                graphics.fill(px - 1, py - 1, px + 2, py + 2, borderColor);
                int backgroundColor = blendTimerColor(bgStartColor, bgEndColor, i / (float) (segments - 1));
                graphics.fill(px, py, px + 1, py + 1, i < progress * segments ? barColor : backgroundColor);
            }
            if ("IN_BAR".equals(pctPos)) {
                String text = percent + "%";
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.font != null) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(centerX - mc.font.width(text) / 2.0f, centerY - 4.0f, 0.0f);
                    snownee.jade.api.ui.IDisplayHelper.get().drawText(graphics, text, 0.0f, 0.0f, textColor);
                    graphics.pose().popPose();
                }
            }
        }

        private static int blendTimerColor(int first, int second, float amount) {
            int a = (int) (((first >>> 24) & 255) + (((second >>> 24) & 255) - ((first >>> 24) & 255)) * amount);
            int r = (int) (((first >>> 16) & 255) + (((second >>> 16) & 255) - ((first >>> 16) & 255)) * amount);
            int g = (int) (((first >>> 8) & 255) + (((second >>> 8) & 255) - ((first >>> 8) & 255)) * amount);
            int b = (int) ((first & 255) + ((second & 255) - (first & 255)) * amount);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
    }
}
