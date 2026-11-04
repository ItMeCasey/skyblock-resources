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
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade integration for the generators.
 * Live dynamic progress bars (Arrow, Jade Bar, Unicode Bar) and NBT watchers for Jade HUD.
 */
@WailaPlugin
public class SkyblockResourcesJadePlugin implements IWailaPlugin {

    public static final ResourceLocation JADE_BLOCK_PROVIDER = new ResourceLocation("skyblock_resources", "jade_block_provider");

    static {
        disableJadeTranslationCheck();
    }

    static final java.util.regex.Pattern QUOTE_PATTERN = java.util.regex.Pattern.compile("^\"|\"$");
    static final java.util.regex.Pattern NUMERIC_ONLY_PATTERN = java.util.regex.Pattern.compile("[^0-9.-]");
    static final java.util.regex.Pattern NBT_SUFFIX_PATTERN = java.util.regex.Pattern.compile("(?i)[dflsb]$");

    @Override
    public void register(IWailaCommonRegistration registration) {
        IServerDataProvider<BlockAccessor> dataProvider = new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                BlockEntity be = accessor.getBlockEntity();
                if (be != null) {
                    CompoundTag persistent = be.getPersistentData();
                    if (persistent != null) {
                        copyAllTags(persistent, data);
                        try {
                            Tag forgeData = persistent.get("ForgeData");
                            if (forgeData instanceof CompoundTag) {
                                copyAllTags((CompoundTag) forgeData, data);
                            }
                        } catch (Throwable ignored) {}
                    }
                    try {
                        CompoundTag beTag = be.saveWithoutMetadata();
                        if (beTag != null) {
                            copyAllTags(beTag, data);
                        }
                    } catch (Throwable ignored) {}
                    data.putBoolean("JadeSynced", true);
                }
            }

            @Override
            public ResourceLocation getUid() {
                return JADE_BLOCK_PROVIDER;
            }
        };

        for (Class<? extends BlockEntity> type : java.util.List.of(
                com.itmecasey.skyblockresources.block.entity.WoodenGeneratorBlockEntity.class, com.itmecasey.skyblockresources.block.entity.StoneGeneratorBlockEntity.class,
                com.itmecasey.skyblockresources.block.entity.IronGeneratorBlockEntity.class, com.itmecasey.skyblockresources.block.entity.DiamondGeneratorBlockEntity.class,
                com.itmecasey.skyblockresources.block.entity.NetheriteGeneratorBlockEntity.class, com.itmecasey.skyblockresources.block.entity.WoodenTreeGeneratorBlockEntity.class,
                com.itmecasey.skyblockresources.block.entity.StoneTreeGeneratorBlockEntity.class, com.itmecasey.skyblockresources.block.entity.IronTreeGeneratorBlockEntity.class,
                com.itmecasey.skyblockresources.block.entity.DiamondTreeGeneratorBlockEntity.class, com.itmecasey.skyblockresources.block.entity.NetheriteTreeGeneratorBlockEntity.class,
                com.itmecasey.skyblockresources.block.entity.CropGeneratorBlockEntity.class)) {
            registration.registerBlockDataProvider(dataProvider, type);
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        disableJadeTranslationCheck();
        SkyblockResourcesJadeClient.register(registration);
    }

    private static void disableJadeTranslationCheck() {
        try {
            Class<?> jc = Class.forName("snownee.jade.JadeClient");
            for (java.lang.reflect.Field f : jc.getDeclaredFields()) {
                if (f.getName().equals("translationChecked") || (f.getType() == boolean.class && java.lang.reflect.Modifier.isStatic(f.getModifiers()))) {
                    f.setAccessible(true);
                    f.setBoolean(null, true);
                }
            }
        } catch (Throwable ignored) {}
    }

    private static final java.lang.reflect.Method SCREEN_HAS_SHIFT_DOWN = resolveHasShiftDownMethod();

    private static java.lang.reflect.Method resolveHasShiftDownMethod() {
        try {
            Class<?> screenCls = Class.forName("net.minecraft.client.gui.screens.Screen");
            return screenCls.getMethod("hasShiftDown");
        } catch (Throwable ignored) {
            return null;
        }
    }

    static boolean isShiftPressed(BlockAccessor accessor) {
        try {
            if (accessor.showDetails()) return true;
        } catch (Throwable ignored) {}
        try {
            if (accessor.getPlayer() != null && accessor.getPlayer().isShiftKeyDown()) return true;
        } catch (Throwable ignored) {}
        try {
            if (SCREEN_HAS_SHIFT_DOWN != null && (Boolean) SCREEN_HAS_SHIFT_DOWN.invoke(null)) return true;
        } catch (Throwable ignored) {}
        return false;
    }

    static void copyAllTags(CompoundTag src, CompoundTag dest) {
        if (src == null || dest == null) return;
        try {
            for (String key : src.getAllKeys()) {
                if (isSystemTag(key)) continue;
                Tag tag = src.get(key);
                if (tag != null) {
                    dest.put(key, tag.copy());
                }
            }
        } catch (Throwable ignored) {}
    }

    static String formatNbtValue(Tag tag, String type) {
        if (tag == null) return "";
        String raw = QUOTE_PATTERN.matcher(tag.toString()).replaceAll("").trim();
        if ("BOOLEAN".equalsIgnoreCase(type)) {
            boolean b = "1b".equalsIgnoreCase(raw) || "1".equals(raw) || "true".equalsIgnoreCase(raw);
            return b ? "true" : "false";
        }
        if ("PERCENT".equalsIgnoreCase(type)) {
            try {
                String clean = NBT_SUFFIX_PATTERN.matcher(raw).replaceAll("").trim();
                double d = Double.parseDouble(clean);
                return Math.round(d * 100.0) + "%";
            } catch (Throwable t) {
                String digits = NUMERIC_ONLY_PATTERN.matcher(raw).replaceAll("");
                try {
                    if (!digits.isEmpty()) {
                        double d = Double.parseDouble(digits);
                        return Math.round(d * 100.0) + "%";
                    }
                } catch (Throwable ignored) {}
                return raw;
            }
        }
        if ("INTEGER".equalsIgnoreCase(type)) {
            try {
                String clean = NBT_SUFFIX_PATTERN.matcher(raw).replaceAll("").trim();
                double d = Double.parseDouble(clean);
                return String.valueOf((long) Math.round(d));
            } catch (Throwable t) {
                String digits = NUMERIC_ONLY_PATTERN.matcher(raw).replaceAll("");
                try {
                    if (!digits.isEmpty()) {
                        return String.valueOf((long) Math.round(Double.parseDouble(digits)));
                    }
                } catch (Throwable ignored) {}
                return raw;
            }
        }
        if ("DOUBLE".equalsIgnoreCase(type) || "FLOAT".equalsIgnoreCase(type)) {
            try {
                String clean = NBT_SUFFIX_PATTERN.matcher(raw).replaceAll("").trim();
                double d = Double.parseDouble(clean);
                if (d == (long) d) {
                    return String.valueOf((long) d);
                }
                String s = String.valueOf(d);
                if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
                return s;
            } catch (Throwable t) {
                return NBT_SUFFIX_PATTERN.matcher(raw).replaceAll("");
            }
        }
        if ("ITEM_BLOCK_NAME".equalsIgnoreCase(type) || "ITEM_NAME".equalsIgnoreCase(type) || "BLOCK_NAME".equalsIgnoreCase(type)) {
            return formatItemOrBlockName(raw);
        }
        return raw;
    }

    static ItemStack resolveItemStack(Tag tag) {
        if (tag == null) return ItemStack.EMPTY;
        String raw = QUOTE_PATTERN.matcher(tag.toString()).replaceAll("").trim();
        if (raw.isEmpty()) return ItemStack.EMPTY;
        try {
            ResourceLocation loc = ResourceLocation.tryParse(raw.contains(":") ? raw : ("minecraft:" + raw));
            if (loc != null) {
                if (BuiltInRegistries.ITEM.containsKey(loc)) {
                    return BuiltInRegistries.ITEM.get(loc).getDefaultInstance();
                }
                if (BuiltInRegistries.BLOCK.containsKey(loc)) {
                    return BuiltInRegistries.BLOCK.get(loc).asItem().getDefaultInstance();
                }
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    static String formatItemOrBlockName(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) return "";
        String id = rawId.trim().toLowerCase();
        try {
            ResourceLocation loc = ResourceLocation.tryParse(id.contains(":") ? id : ("minecraft:" + id));
            if (loc != null) {
                if (BuiltInRegistries.ITEM.containsKey(loc)) {
                    return BuiltInRegistries.ITEM.get(loc).getDefaultInstance().getHoverName().getString();
                }
                if (BuiltInRegistries.BLOCK.containsKey(loc)) {
                    return BuiltInRegistries.BLOCK.get(loc).getName().getString();
                }
            }
        } catch (Throwable ignored) {}
        String clean = id.contains(":") ? id.split(":", 2)[1] : id;
        return formatTitleCase(clean);
    }

    private static String formatTitleCase(String str) {
        if (str == null || str.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean capNext = true;
        for (char c : str.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ') {
                sb.append(' ');
                capNext = true;
            } else if (capNext) {
                sb.append(Character.toUpperCase(c));
                capNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    static double findNumber(CompoundTag data, String... keys) {
        if (data == null) return -1.0;
        for (String key : keys) {
            if (data.contains(key)) {
                try {
                    Tag tag = data.get(key);
                    if (tag != null) {
                        String raw = NUMERIC_ONLY_PATTERN.matcher(QUOTE_PATTERN.matcher(tag.toString()).replaceAll("")).replaceAll("");
                        if (!raw.isEmpty()) {
                            return Double.parseDouble(raw);
                        }
                    }
                } catch (Throwable ignored) {}
            }
        }
        // Case-insensitive fallback scan
        for (String key : keys) {
            for (String actualKey : data.getAllKeys()) {
                if (actualKey.equalsIgnoreCase(key)) {
                    try {
                        Tag tag = data.get(actualKey);
                        if (tag != null) {
                            String raw = NUMERIC_ONLY_PATTERN.matcher(QUOTE_PATTERN.matcher(tag.toString()).replaceAll("")).replaceAll("");
                            if (!raw.isEmpty()) {
                                return Double.parseDouble(raw);
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        }
        return -1.0;
    }

    private static boolean isSystemTag(String key) {
        if (key == null) return true;
        String lower = key.toLowerCase();
        return lower.equals("id") || lower.equals("x") || lower.equals("y") || lower.equals("z") ||
               lower.equals("items") || lower.equals("item_storage") || lower.contains("minecraft:") ||
               lower.equals("blockid") || lower.equals("components") || lower.equals("blockentitytag") ||
               lower.equals("neoforge:attachments") || lower.equals("neoforge:data") || lower.startsWith("neoforge:");
    }
}
