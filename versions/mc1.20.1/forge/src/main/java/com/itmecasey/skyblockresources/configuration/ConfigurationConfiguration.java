package com.itmecasey.skyblockresources.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigurationConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	private static final String TICKS_COMMENT = "Ticks between item generation";
	private static final String TRANSFER_COMMENT = "Max items pushed to output storage per generation cycle (max 64)";

	public static final ForgeConfigSpec.ConfigValue<Double> WOODEN_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> WOODEN_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> WOODEN_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> STONE_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> STONE_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> STONE_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> IRON_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> IRON_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> IRON_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> DIAMOND_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> DIAMOND_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> DIAMOND_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> NETHERITE_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> NETHERITE_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> NETHERITE_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> WOODEN_TREE_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> WOODEN_TREE_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> WOODEN_TREE_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> STONE_TREE_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> STONE_TREE_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> STONE_TREE_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> IRON_TREE_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> IRON_TREE_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> IRON_TREE_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> DIAMOND_TREE_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> DIAMOND_TREE_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> DIAMOND_TREE_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> NETHERITE_TREE_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> NETHERITE_TREE_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> NETHERITE_TREE_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> WOODEN_CROP_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> WOODEN_CROP_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> WOODEN_CROP_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> STONE_CROP_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> STONE_CROP_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> STONE_CROP_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> IRON_CROP_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> IRON_CROP_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> IRON_CROP_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> DIAMOND_CROP_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> DIAMOND_CROP_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> DIAMOND_CROP_SLOTS;
	public static final ForgeConfigSpec.ConfigValue<Double> NETHERITE_CROP_MAX_TICKS;
	public static final ForgeConfigSpec.ConfigValue<Double> NETHERITE_CROP_MAX_TRANSFER;
	public static final ForgeConfigSpec.ConfigValue<Integer> NETHERITE_CROP_SLOTS;

	static {
		BUILDER.push("wooden");
		WOODEN_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("wooden_max_ticks_per_generation", 40.0);
		WOODEN_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("wooden_item_transfer_per_generation", 2.0);
		WOODEN_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("wooden_storage_slots", 4, 1, 256);
		BUILDER.pop();

		BUILDER.push("stone");
		STONE_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("stone_max_ticks_per_generation", 40.0);
		STONE_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("stone_item_transfer_per_generation", 4.0);
		STONE_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("stone_storage_slots", 8, 1, 256);
		BUILDER.pop();

		BUILDER.push("iron");
		IRON_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("iron_max_ticks_per_generation", 20.0);
		IRON_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("iron_item_transfer_per_generation", 4.0);
		IRON_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("iron_storage_slots", 16, 1, 256);
		BUILDER.pop();

		BUILDER.push("diamond");
		DIAMOND_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("diamond_max_ticks_per_generation", 10.0);
		DIAMOND_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("diamond_item_transfer_per_generation", 16.0);
		DIAMOND_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("diamond_storage_slots", 32, 1, 256);
		BUILDER.pop();

		BUILDER.push("netherite");
		NETHERITE_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("netherite_max_ticks_per_generation", 10.0);
		NETHERITE_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("netherite_item_transfer_per_generation", 32.0);
		NETHERITE_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("netherite_storage_slots", 64, 1, 256);
		BUILDER.pop();

		BUILDER.push("wooden_tree_generator");
		WOODEN_TREE_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("wooden_tree_max_ticks_per_generation", 1400.0);
		WOODEN_TREE_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("wooden_tree_item_transfer_per_generation", 4.0);
		WOODEN_TREE_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("wooden_tree_storage_slots", 8, 1, 256);
		BUILDER.pop();

		BUILDER.push("stone_tree_generator");
		STONE_TREE_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("stone_tree_max_ticks_per_generation", 1000.0);
		STONE_TREE_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("stone_tree_item_transfer_per_generation", 8.0);
		STONE_TREE_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("stone_tree_storage_slots", 8, 1, 256);
		BUILDER.pop();

		BUILDER.push("iron_tree_generator");
		IRON_TREE_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("iron_tree_max_ticks_per_generation", 800.0);
		IRON_TREE_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("iron_tree_item_transfer_per_generation", 16.0);
		IRON_TREE_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("iron_tree_storage_slots", 16, 1, 256);
		BUILDER.pop();

		BUILDER.push("diamond_tree_generator");
		DIAMOND_TREE_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("diamond_tree_max_ticks_per_generation", 400.0);
		DIAMOND_TREE_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("diamond_tree_item_transfer_per_generation", 32.0);
		DIAMOND_TREE_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("diamond_tree_storage_slots", 32, 1, 256);
		BUILDER.pop();

		BUILDER.push("netherite_tree_generator");
		NETHERITE_TREE_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("netherite_tree_max_ticks_per_generation", 200.0);
		NETHERITE_TREE_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("netherite_tree_item_transfer_per_generation", 64.0);
		NETHERITE_TREE_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("netherite_tree_storage_slots", 64, 1, 256);
		BUILDER.pop();


		BUILDER.push("wooden_crop_generator");
		WOODEN_CROP_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("wooden_crop_max_ticks_per_generation", 1400.0);
		WOODEN_CROP_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("wooden_crop_item_transfer_per_generation", 4.0);
		WOODEN_CROP_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("wooden_crop_storage_slots", 8, 1, 256);
		BUILDER.pop();

		BUILDER.push("stone_crop_generator");
		STONE_CROP_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("stone_crop_max_ticks_per_generation", 1000.0);
		STONE_CROP_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("stone_crop_item_transfer_per_generation", 8.0);
		STONE_CROP_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("stone_crop_storage_slots", 8, 1, 256);
		BUILDER.pop();

		BUILDER.push("iron_crop_generator");
		IRON_CROP_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("iron_crop_max_ticks_per_generation", 800.0);
		IRON_CROP_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("iron_crop_item_transfer_per_generation", 16.0);
		IRON_CROP_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("iron_crop_storage_slots", 16, 1, 256);
		BUILDER.pop();

		BUILDER.push("diamond_crop_generator");
		DIAMOND_CROP_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("diamond_crop_max_ticks_per_generation", 400.0);
		DIAMOND_CROP_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("diamond_crop_item_transfer_per_generation", 32.0);
		DIAMOND_CROP_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("diamond_crop_storage_slots", 32, 1, 256);
		BUILDER.pop();

		BUILDER.push("netherite_crop_generator");
		NETHERITE_CROP_MAX_TICKS = BUILDER.comment(TICKS_COMMENT).define("netherite_crop_max_ticks_per_generation", 200.0);
		NETHERITE_CROP_MAX_TRANSFER = BUILDER.comment(TRANSFER_COMMENT).define("netherite_crop_item_transfer_per_generation", 64.0);
		NETHERITE_CROP_SLOTS = BUILDER.comment("Internal storage slots, each holding up to 64 items").defineInRange("netherite_crop_storage_slots", 64, 1, 256);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}
}
