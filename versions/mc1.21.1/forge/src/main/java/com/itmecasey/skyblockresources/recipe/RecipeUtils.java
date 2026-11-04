package com.itmecasey.skyblockresources.recipe;

import com.itmecasey.skyblockresources.compat.SizedIngredient;
import net.minecraftforge.fluids.FluidStack;
import com.itmecasey.skyblockresources.compat.SizedFluidIngredient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;
import com.itmecasey.skyblockresources.compat.IngredientCodecs;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
public class RecipeUtils {
	private RecipeUtils() {
	}

	public static String formatChance(double chance) {
		if (chance <= 0 || chance >= 1.0)
			return null;
		return formatNumber(chance * 100.0) + "%";
	}

	public static String formatDuration(double ticks) {
		if (ticks <= 0)
			return null;
		return formatNumber(ticks / 20.0) + "s";
	}

	private static String formatNumber(double value) {
		if (value == Math.floor(value))
			return String.valueOf((int) value);
		DecimalFormat format = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ROOT));
		return format.format(value);
	}

	// Returns a list of all recipes from the given recipe type
	public static <I extends RecipeInput, R extends Recipe<I>> List<R> getRecipes(LevelAccessor world, RecipeType<R> type) {
		List<R> recipes = new ArrayList<>();
		if (world instanceof ServerLevel serverLevel) {
			recipes.addAll(serverLevel.getRecipeManager().getAllRecipesFor(type).stream().map(RecipeHolder::value).toList());
		} else if (world instanceof ClientLevel) {
			recipes.addAll(SkyblockResourcesModRecipeTypes.getRecipes(type).map(RecipeHolder::value).toList());
		}
		return recipes;
	}

	// Generic method for handling Optional<>
	public static <T> T unwrap(Object value, Class<T> clazz, T defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if (clazz.isInstance(value)) {
			return clazz.cast(value);
		}
		if (value instanceof Optional<?> opt && opt.isPresent()) {
			if (clazz.isInstance(opt.get())) {
				return clazz.cast(opt.get());
			}
		}
		return defaultValue;
	}

	// Gets all inputs from the recipe object as a List<ItemStack> of all possible values
	public static List<ItemStack> getItemStacks(Object in) {
		List<ItemStack> stacks = new ArrayList<>();
		Object value = in instanceof Optional<?> opt ? opt.orElse(null) : in;
		if (value instanceof SizedIngredient sized) {
			for (ItemStack stack : IngredientCodecs.items(sized.ingredient())) {
				stacks.add(stack.copyWithCount(sized.count()));
			}
		} else if (value instanceof Ingredient ingre) {
			stacks.addAll(IngredientCodecs.items(ingre));
		}
		return stacks;
	}

	// Collects the non-empty stacks from each ingredient that pass the filter, skipping duplicates
	public static List<ItemStack> collectMatchingStacks(List<Ingredient> ingredients, Predicate<ItemStack> filter) {
		List<ItemStack> matches = new ArrayList<>();
		for (Ingredient ingredient : ingredients) {
			for (ItemStack stack : getItemStacks(ingredient)) {
				if (!stack.isEmpty() && filter.test(stack) && matches.stream().noneMatch(existing -> ItemStack.isSameItemSameComponents(existing, stack)))
					matches.add(stack);
			}
		}
		return matches;
	}

	// Gets all inputs from the recipe object as a List<FluidStack> of all possible values
	public static List<FluidStack> getFluidStacks(Object in) {
		List<FluidStack> stacks = new ArrayList<>();
		Object value = in instanceof Optional<?> opt ? opt.orElse(null) : in;
		if (value instanceof SizedFluidIngredient sized) {
			for (FluidStack fluid : sized.getFluids()) {
				stacks.add(fluid.copy());
			}
		}
		return stacks;
	}

	// Compares two recipe inputs to check if they match in ingredient and quantity
	public static boolean areInputsEqual(Object in1, Object in2) {
		if (in1 == in2)
			return true;
		if (in1 == null || in2 == null)
			return false;
		List<ItemStack> list1 = getItemStacks(in1);
		List<ItemStack> list2 = getItemStacks(in2);
		if (!list1.isEmpty() || !list2.isEmpty()) {
			if (list1.size() != list2.size())
				return false;
			for (int i = 0; i < list1.size(); i++) {
				if (!ItemStack.matches(list1.get(i), list2.get(i)))
					return false;
			}
			return true;
		}
		List<FluidStack> f1 = getFluidStacks(in1);
		List<FluidStack> f2 = getFluidStacks(in2);
		if (!f1.isEmpty() || !f2.isEmpty()) {
			if (f1.size() != f2.size())
				return false;
			for (int i = 0; i < f1.size(); i++) {
				if (!f1.get(i).isFluidStackIdentical(f2.get(i)))
					return false;
			}
			return true;
		}
		if (in1 instanceof double[] d1 && in2 instanceof double[] d2) {
			return Arrays.equals(d1, d2);
		}
		return Objects.equals(in1, in2);
	}

	// Validates a recipe ingredient with it's given recipe input
	public static boolean validate(Object given, Object recipe) {
		// Test Item
		if (given instanceof ItemStack item) {
			if (recipe instanceof SizedIngredient sIngre) {
				return sIngre.test(item);
			} else if (recipe instanceof Ingredient ingre) {
				return ingre.test(item);
			} else if (recipe instanceof Optional<?> opt) {
				if (item.isEmpty()) {
					return opt.isEmpty();
				}
				if (opt.isPresent()) {
					if (opt.get() instanceof SizedIngredient sIngred) {
						return sIngred.test(item);
					} else if (opt.get() instanceof Ingredient ingred) {
						return ingred.test(item);
					}
				}
				return false;
			}
		}
		// Test Fluid
		if (given instanceof FluidStack fluid) {
			if (recipe instanceof SizedFluidIngredient ingre) {
				return ingre.test(fluid);
			} else if (recipe instanceof Optional<?> opt) {
				if (fluid.isEmpty()) {
					return opt.isEmpty();
				}
				if (opt.isPresent()) {
					if (opt.get() instanceof SizedFluidIngredient ingred) {
						return ingred.test(fluid);
					}
				}
				return false;
			}
		}
		// Test boolean
		if (given instanceof Boolean bool && recipe instanceof Boolean rec) {
			return bool.equals(rec);
		}
		// Test double
		if (given instanceof double[] doub && recipe instanceof Double rec) {
			return doub[0] >= rec;
		}
		// Test String
		if (given instanceof String str && recipe instanceof String rec) {
			return str.equals(rec);
		}
		return false;
	}

	// Returns the recipe ingredient amount
	public static int amount(Object recipe) {
		if (recipe instanceof SizedIngredient sized) {
			return sized.count();
		} else if (recipe instanceof Ingredient) {
			return 1;
		} else if (recipe instanceof Optional<?> opt) {
			if (opt.isPresent()) {
				if (opt.get() instanceof SizedIngredient sizedO) {
					return sizedO.count();
				} else if (opt.get() instanceof Ingredient) {
					return 1;
				}
			}
		}
		if (recipe instanceof SizedFluidIngredient sizedF) {
			return sizedF.amount();
		} else if (recipe instanceof Optional<?> opt) {
			if (opt.isPresent()) {
				if (opt.get() instanceof SizedFluidIngredient sizedFO) {
					return sizedFO.amount();
				}
			}
		}
		if (recipe instanceof Double doub) {
			return doub.intValue();
		}
		return 0;
	}

	public static int getTooltipX() {
		Minecraft minecraft = Minecraft.getInstance();
		return (int) (minecraft.mouseHandler.xpos() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth());
	}

	public static int getTooltipY() {
		Minecraft minecraft = Minecraft.getInstance();
		return (int) (minecraft.mouseHandler.ypos() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight());
	}

	// Shapeless item validation: matches provided ItemStacks against required recipe ingredients in an order-independent manner
	public static boolean validateShapeless(List<ItemStack> provided, List<Object> required) {
		List<ItemStack> remainingProvided = new ArrayList<>();
		for (ItemStack st : provided) {
			if (st != null && !st.isEmpty()) {
				remainingProvided.add(st.copy());
			}
		}
		List<Object> nonNullRequired = new ArrayList<>();
		for (Object req : required) {
			if (req instanceof Optional<?> opt) {
				if (opt.isPresent()) {
					nonNullRequired.add(opt.get());
				}
			} else if (req != null) {
				nonNullRequired.add(req);
			}
		}
		if (remainingProvided.size() < nonNullRequired.size()) {
			return false;
		}
		for (Object req : nonNullRequired) {
			boolean matched = false;
			for (int i = 0; i < remainingProvided.size(); i++) {
				ItemStack prov = remainingProvided.get(i);
				if (validate(prov, req)) {
					remainingProvided.remove(i);
					matched = true;
					break;
				}
			}
			if (!matched) {
				return false;
			}
		}
		return true;
	}

	public static class TreeDropEntry {
		private final ItemStack stack;
		private final int minCount;
		private final int maxCount;
		private final double chance;

		public TreeDropEntry(ItemStack stack, int minCount, int maxCount, double chance) {
			this.stack = stack;
			this.minCount = minCount;
			this.maxCount = maxCount;
			this.chance = chance;
		}

		public ItemStack getStack() {
			return stack;
		}

		public ItemStack item() {
			return stack;
		}

		public double count() {
			if (minCount != maxCount)
				return 0.0d;
			return (double) stack.getCount();
		}

		public double minCount() {
			return (double) minCount;
		}

		public double maxCount() {
			return (double) maxCount;
		}

		public double chance() {
			return chance;
		}
	}

	public static boolean isSapling(Object input) {
		if (input == null)
			return false;
		if (input instanceof ItemStack stack) {
			if (stack.isEmpty())
				return false;
			return isSaplingItem(stack.getItem());
		}
		if (input instanceof Item item) {
			return isSaplingItem(item);
		}
		if (input instanceof BlockState state) {
			return isSaplingItem(state.getBlock().asItem());
		}
		if (input instanceof Block block) {
			return isSaplingItem(block.asItem());
		}
		if (input instanceof TagKey<?> tagKey) {
			String path = tagKey.location().getPath().toLowerCase(Locale.ROOT);
			if (path.contains("sapling"))
				return true;
			try {
				@SuppressWarnings("unchecked")
				var named = BuiltInRegistries.ITEM.getTagOrEmpty((TagKey<Item>) tagKey);
				for (var holder : named) {
					if (isSaplingItem(holder.value()))
						return true;
				}
			} catch (Throwable ignored) {
			}
			return false;
		}
		if (input instanceof String str) {
			String clean = str.trim().toLowerCase(Locale.ROOT);
			if (clean.contains("sapling"))
				return true;
			try {
				var loc = ResourceLocation.tryParse(clean.startsWith("#") ? clean.substring(1) : clean);
				if (loc != null) {
					var tagKey = TagKey.create(Registries.ITEM, loc);
					var named = BuiltInRegistries.ITEM.getTagOrEmpty(tagKey);
					for (var holder : named) {
						if (isSaplingItem(holder.value()))
							return true;
					}
					var it = BuiltInRegistries.ITEM.getOptional(loc).orElse(null);
					if (it != null && it != Items.AIR) {
						return isSaplingItem(it);
					}
				}
			} catch (Throwable ignored) {
			}
			return false;
		}
		if (input instanceof Ingredient ing) {
			for (ItemStack st : getItemStacks(ing)) {
				if (isSapling(st))
					return true;
			}
			return false;
		}
		if (input instanceof Iterable<?> it) {
			for (Object el : it) {
				if (isSapling(el))
					return true;
			}
			return false;
		}
		if (input instanceof Optional<?> opt && opt.isPresent()) {
			return isSapling(opt.get());
		}
		return false;
	}

	public static boolean isSapling(ItemStack item) {
		return isSapling((Object) item);
	}

	private static boolean isSaplingItem(Item rawItem) {
		if (rawItem == null || rawItem == Items.AIR)
			return false;
		String path = BuiltInRegistries.ITEM.getKey(rawItem).getPath();
		if (path.startsWith("potted_"))
			return false;
		if (path.endsWith("_sapling") || path.endsWith("_propagule") || rawItem == Items.AZALEA || rawItem == Items.FLOWERING_AZALEA) {
			return true;
		}
		if (rawItem instanceof BlockItem bi) {
			Block b = bi.getBlock();
			if (b instanceof SaplingBlock || b == Blocks.AZALEA || b == Blocks.FLOWERING_AZALEA) {
				return true;
			}
		}
		try {
			for (TagKey<Item> tag : rawItem.builtInRegistryHolder().tags().toList()) {
				String tagPath = tag.location().getPath();
				if (tagPath.contains("sapling")) {
					return true;
				}
			}
		} catch (Throwable ignored) {
		}
		return false;
	}

	public static boolean isCropOrSeed(Object input) {
		if (input == null)
			return false;
		if (input instanceof ItemStack stack) {
			if (stack.isEmpty())
				return false;
			return isCropOrSeedItem(stack.getItem());
		}
		if (input instanceof Item item) {
			return isCropOrSeedItem(item);
		}
		if (input instanceof BlockState state) {
			return isCropOrSeedItem(state.getBlock().asItem()) || isCropBlock(state.getBlock());
		}
		if (input instanceof Block block) {
			return isCropOrSeedItem(block.asItem()) || isCropBlock(block);
		}
		if (input instanceof TagKey<?> tagKey) {
			String path = tagKey.location().getPath().toLowerCase(Locale.ROOT);
			if (path.contains("seed") || path.contains("crop") || path.contains("plantable"))
				return true;
			try {
				@SuppressWarnings("unchecked")
				var named = BuiltInRegistries.ITEM.getTagOrEmpty((TagKey<Item>) tagKey);
				for (var holder : named) {
					if (isCropOrSeedItem(holder.value()))
						return true;
				}
			} catch (Throwable ignored) {
			}
			return false;
		}
		if (input instanceof String str) {
			String clean = str.trim().toLowerCase(Locale.ROOT);
			if (clean.contains("seed") || clean.contains("crop") || clean.contains("plantable"))
				return true;
			try {
				var loc = ResourceLocation.tryParse(clean.startsWith("#") ? clean.substring(1) : clean);
				if (loc != null) {
					var tagKey = TagKey.create(Registries.ITEM, loc);
					var named = BuiltInRegistries.ITEM.getTagOrEmpty(tagKey);
					for (var holder : named) {
						if (isCropOrSeedItem(holder.value()))
							return true;
					}
					var it = BuiltInRegistries.ITEM.getOptional(loc).orElse(null);
					if (it != null && it != Items.AIR) {
						return isCropOrSeedItem(it);
					}
				}
			} catch (Throwable ignored) {
			}
			return false;
		}
		if (input instanceof Ingredient ing) {
			for (ItemStack st : getItemStacks(ing)) {
				if (isCropOrSeed(st))
					return true;
			}
			return false;
		}
		if (input instanceof Iterable<?> it) {
			for (Object el : it) {
				if (isCropOrSeed(el))
					return true;
			}
			return false;
		}
		if (input instanceof Optional<?> opt && opt.isPresent()) {
			return isCropOrSeed(opt.get());
		}
		return false;
	}

	public static boolean isCropOrSeed(ItemStack item) {
		return isCropOrSeed((Object) item);
	}

	private static boolean isCropOrSeedItem(Item rawItem) {
		if (rawItem == null || rawItem == Items.AIR)
			return false;
		String path = BuiltInRegistries.ITEM.getKey(rawItem).getPath();
		if (path.endsWith("_seeds") || path.endsWith("_seed") || path.endsWith("_spores") || path.endsWith("_pod")) {
			return true;
		}
		if (path.equals("wheat") || path.equals("carrot") || path.equals("carrots") || path.equals("potato") || path.equals("potatoes") || path.equals("beetroot") || path.equals("beetroots") || path.equals("melon_seeds") || path.equals("melon")
				|| path.equals("melon_slice") || path.equals("pumpkin_seeds") || path.equals("pumpkin") || path.equals("nether_wart") || path.equals("cocoa_beans") || path.equals("sweet_berries") || path.equals("glow_berries")
				|| path.equals("sugar_cane") || path.equals("cactus") || path.equals("bamboo") || path.equals("torchflower") || path.equals("torchflower_seeds") || path.equals("pitcher_pod") || path.equals("pitcher_plant")) {
			return true;
		}
		if (rawItem instanceof BlockItem bi) {
			Block b = bi.getBlock();
			if (b instanceof CropBlock || b instanceof StemBlock || b instanceof AttachedStemBlock
					|| b instanceof CocoaBlock || b instanceof SweetBerryBushBlock || b instanceof NetherWartBlock
					|| b instanceof SugarCaneBlock || b instanceof CactusBlock || b instanceof BambooStalkBlock
					|| b instanceof FlowerBlock || b instanceof TallFlowerBlock) {
				return true;
			}
			for (Property<?> prop : b.getStateDefinition().getProperties()) {
				if ("age".equalsIgnoreCase(prop.getName())) {
					return true;
				}
			}
		}
		try {
			for (TagKey<Item> tag : rawItem.builtInRegistryHolder().tags().toList()) {
				String tagPath = tag.location().getPath();
				if (tagPath.contains("seed") || tagPath.contains("crop") || tagPath.contains("plantable")) {
					return true;
				}
			}
		} catch (Throwable ignored) {
		}
		return false;
	}

	private static boolean isCropBlock(Block b) {
		if (b == null)
			return false;
		if (b instanceof CropBlock || b instanceof StemBlock || b instanceof AttachedStemBlock || b instanceof CocoaBlock
				|| b instanceof SweetBerryBushBlock || b instanceof NetherWartBlock || b instanceof SugarCaneBlock
				|| b instanceof CactusBlock || b instanceof BambooStalkBlock || b instanceof FlowerBlock
				|| b instanceof TallFlowerBlock) {
			return true;
		}
		for (Property<?> prop : b.getStateDefinition().getProperties()) {
			if ("age".equalsIgnoreCase(prop.getName())) {
				return true;
			}
		}
		return false;
	}

	public static List<TreeDropEntry> getTreeDropEntries(LevelAccessor world, Object input) {
		List<TreeDropEntry> drops = new ArrayList<>();
		if (input == null)
			return drops;
		if (world == null) {
			try {
				world = Minecraft.getInstance().level;
			} catch (Throwable ignored) {
			}
		}
		if (input instanceof ItemStack stack) {
			return getTreeDropEntriesSingle(world, stack);
		}
		if (input instanceof Item item) {
			return getTreeDropEntriesSingle(world, new ItemStack(item));
		}
		if (input instanceof BlockState state) {
			return getTreeDropEntriesSingle(world, new ItemStack(state.getBlock().asItem()));
		}
		if (input instanceof Block block) {
			return getTreeDropEntriesSingle(world, new ItemStack(block.asItem()));
		}
		if (input instanceof TagKey<?> tagKey) {
			try {
				@SuppressWarnings("unchecked")
				var named = BuiltInRegistries.ITEM.getTagOrEmpty((TagKey<Item>) tagKey);
				for (var holder : named) {
					mergeTreeDrops(drops, getTreeDropEntriesSingle(world, new ItemStack(holder.value())));
				}
			} catch (Throwable ignored) {
			}
			return drops;
		}
		if (input instanceof String str) {
			String clean = str.trim();
			if (clean.startsWith("#"))
				clean = clean.substring(1);
			try {
				var loc = ResourceLocation.tryParse(clean);
				if (loc != null) {
					var tagKey = TagKey.create(Registries.ITEM, loc);
					var named = BuiltInRegistries.ITEM.getTagOrEmpty(tagKey);
					for (var holder : named) {
						mergeTreeDrops(drops, getTreeDropEntriesSingle(world, new ItemStack(holder.value())));
					}
					if (drops.isEmpty()) {
						var it = BuiltInRegistries.ITEM.getOptional(loc).orElse(null);
						if (it != null && it != Items.AIR) {
							return getTreeDropEntriesSingle(world, new ItemStack(it));
						}
					}
				}
			} catch (Throwable ignored) {
			}
			return drops;
		}
		if (input instanceof Ingredient ing) {
			for (ItemStack st : getItemStacks(ing)) {
				mergeTreeDrops(drops, getTreeDropEntriesSingle(world, st));
			}
			return drops;
		}
		if (input instanceof Iterable<?> it) {
			for (Object el : it) {
				mergeTreeDrops(drops, getTreeDropEntries(world, el));
			}
			return drops;
		}
		if (input instanceof Optional<?> opt && opt.isPresent()) {
			return getTreeDropEntries(world, opt.get());
		}
		return drops;
	}

	public static List<TreeDropEntry> getTreeDropEntries(LevelAccessor world, ItemStack item) {
		return getTreeDropEntries(world, (Object) item);
	}

	private static void mergeTreeDrops(List<TreeDropEntry> target, List<TreeDropEntry> source) {
		for (TreeDropEntry entry : source) {
			boolean exists = false;
			for (TreeDropEntry cur : target) {
				if (cur.getStack().getItem() == entry.getStack().getItem()) {
					exists = true;
					break;
				}
			}
			if (!exists)
				target.add(entry);
		}
	}

	private static List<TreeDropEntry> getTreeDropEntriesSingle(LevelAccessor world, ItemStack item) {
		List<TreeDropEntry> drops = new ArrayList<>();
		if (item == null || item.isEmpty())
			return drops;
		for (DropEntry entry : com.itmecasey.skyblockresources.drops.RecipeDrops.entries(world, item.getItem(), true))
			drops.add(new TreeDropEntry(new ItemStack(entry.item(), entry.maxCount()), entry.min(), entry.maxCount(), entry.chance()));
		return drops;
	}

	public static class CropDropEntry {
		private final ItemStack stack;
		private final int minCount;
		private final int maxCount;
		private final double chance;

		public CropDropEntry(ItemStack stack, int minCount, int maxCount, double chance) {
			this.stack = stack;
			this.minCount = minCount;
			this.maxCount = maxCount;
			this.chance = chance;
		}

		public ItemStack getStack() {
			return stack;
		}

		public ItemStack item() {
			return stack;
		}

		public double count() {
			if (minCount != maxCount)
				return 0.0d;
			return (double) stack.getCount();
		}

		public double minCount() {
			return (double) minCount;
		}

		public double maxCount() {
			return (double) maxCount;
		}

		public double chance() {
			return chance;
		}
	}

	public static List<CropDropEntry> getCropDropEntries(LevelAccessor world, Object input) {
		List<CropDropEntry> drops = new ArrayList<>();
		if (input == null)
			return drops;
		if (world == null) {
			try {
				world = Minecraft.getInstance().level;
			} catch (Throwable ignored) {
			}
		}
		if (input instanceof ItemStack stack) {
			return getCropDropEntriesSingle(world, stack);
		}
		if (input instanceof Item item) {
			return getCropDropEntriesSingle(world, new ItemStack(item));
		}
		if (input instanceof BlockState state) {
			return getCropDropEntriesSingle(world, new ItemStack(state.getBlock().asItem()));
		}
		if (input instanceof Block block) {
			return getCropDropEntriesSingle(world, new ItemStack(block.asItem()));
		}
		if (input instanceof TagKey<?> tagKey) {
			try {
				@SuppressWarnings("unchecked")
				var named = BuiltInRegistries.ITEM.getTagOrEmpty((TagKey<Item>) tagKey);
				for (var holder : named) {
					mergeCropDrops(drops, getCropDropEntriesSingle(world, new ItemStack(holder.value())));
				}
			} catch (Throwable ignored) {
			}
			return drops;
		}
		if (input instanceof String str) {
			String clean = str.trim();
			if (clean.startsWith("#"))
				clean = clean.substring(1);
			try {
				var loc = ResourceLocation.tryParse(clean);
				if (loc != null) {
					var tagKey = TagKey.create(Registries.ITEM, loc);
					var named = BuiltInRegistries.ITEM.getTagOrEmpty(tagKey);
					for (var holder : named) {
						mergeCropDrops(drops, getCropDropEntriesSingle(world, new ItemStack(holder.value())));
					}
					if (drops.isEmpty()) {
						var it = BuiltInRegistries.ITEM.getOptional(loc).orElse(null);
						if (it != null && it != Items.AIR) {
							return getCropDropEntriesSingle(world, new ItemStack(it));
						}
					}
				}
			} catch (Throwable ignored) {
			}
			return drops;
		}
		if (input instanceof Ingredient ing) {
			for (ItemStack st : getItemStacks(ing)) {
				mergeCropDrops(drops, getCropDropEntriesSingle(world, st));
			}
			return drops;
		}
		if (input instanceof Iterable<?> it) {
			for (Object el : it) {
				mergeCropDrops(drops, getCropDropEntries(world, el));
			}
			return drops;
		}
		if (input instanceof Optional<?> opt && opt.isPresent()) {
			return getCropDropEntries(world, opt.get());
		}
		return drops;
	}

	public static List<CropDropEntry> getCropDropEntries(LevelAccessor world, ItemStack item) {
		return getCropDropEntries(world, (Object) item);
	}

	private static void mergeCropDrops(List<CropDropEntry> target, List<CropDropEntry> source) {
		for (CropDropEntry entry : source) {
			boolean exists = false;
			for (CropDropEntry cur : target) {
				if (cur.getStack().getItem() == entry.getStack().getItem()) {
					exists = true;
					break;
				}
			}
			if (!exists)
				target.add(entry);
		}
	}

	private static List<CropDropEntry> getCropDropEntriesSingle(LevelAccessor world, ItemStack item) {
		List<CropDropEntry> drops = new ArrayList<>();
		if (item == null || item.isEmpty())
			return drops;
		for (DropEntry entry : com.itmecasey.skyblockresources.drops.RecipeDrops.entries(world, item.getItem(), false))
			drops.add(new CropDropEntry(new ItemStack(entry.item(), entry.maxCount()), entry.min(), entry.maxCount(), entry.chance()));
		return drops;
	}

	public static boolean isSpawnEgg(ItemStack item) {
		return item != null && !item.isEmpty() && item.getItem() instanceof SpawnEggItem;
	}

	public static boolean isSpawnEgg(Item item) {
		return item instanceof SpawnEggItem;
	}

	public static class EntityDropEntry {
		private final ItemStack stack;
		private final int minCount;
		private final int maxCount;
		private final double chance;

		public EntityDropEntry(ItemStack stack, int minCount, int maxCount, double chance) {
			this.stack = stack;
			this.minCount = minCount;
			this.maxCount = maxCount;
			this.chance = chance;
		}

		public ItemStack getStack() {
			return stack;
		}

		public ItemStack item() {
			return stack;
		}

		public double count() {
			if (minCount != maxCount)
				return 0.0d;
			return (double) stack.getCount();
		}

		public double minCount() {
			return (double) minCount;
		}

		public double maxCount() {
			return (double) maxCount;
		}

		public double chance() {
			return chance;
		}
	}

	public static List<EntityDropEntry> getEntityDropEntries(LevelAccessor world, Object input) {
		List<EntityDropEntry> drops = new ArrayList<>();
		if (input == null)
			return drops;
		if (world == null) {
			try {
				world = Minecraft.getInstance().level;
			} catch (Throwable ignored) {
			}
		}
		if (input instanceof ItemStack stack) {
			return getEntityDropEntriesSingle(world, stack);
		}
		if (input instanceof Item item) {
			return getEntityDropEntriesSingle(world, new ItemStack(item));
		}
		if (input instanceof BlockState state) {
			return getEntityDropEntriesSingle(world, new ItemStack(state.getBlock().asItem()));
		}
		if (input instanceof Block block) {
			return getEntityDropEntriesSingle(world, new ItemStack(block.asItem()));
		}
		if (input instanceof TagKey<?> tagKey) {
			try {
				@SuppressWarnings("unchecked")
				var named = BuiltInRegistries.ITEM.getTagOrEmpty((TagKey<Item>) tagKey);
				for (var holder : named) {
					mergeEntityDrops(drops, getEntityDropEntriesSingle(world, new ItemStack(holder.value())));
				}
			} catch (Throwable ignored) {
			}
			return drops;
		}
		if (input instanceof String str) {
			String clean = str.trim();
			if (clean.startsWith("#"))
				clean = clean.substring(1);
			try {
				var loc = ResourceLocation.tryParse(clean);
				if (loc != null) {
					var tagKey = TagKey.create(Registries.ITEM, loc);
					var named = BuiltInRegistries.ITEM.getTagOrEmpty(tagKey);
					for (var holder : named) {
						mergeEntityDrops(drops, getEntityDropEntriesSingle(world, new ItemStack(holder.value())));
					}
					if (drops.isEmpty()) {
						var it = BuiltInRegistries.ITEM.getOptional(loc).orElse(null);
						if (it != null && it != Items.AIR) {
							return getEntityDropEntriesSingle(world, new ItemStack(it));
						}
					}
				}
			} catch (Throwable ignored) {
			}
			return drops;
		}
		if (input instanceof Ingredient ing) {
			for (ItemStack st : getItemStacks(ing)) {
				mergeEntityDrops(drops, getEntityDropEntriesSingle(world, st));
			}
			return drops;
		}
		if (input instanceof Iterable<?> it) {
			for (Object el : it) {
				mergeEntityDrops(drops, getEntityDropEntries(world, el));
			}
			return drops;
		}
		if (input instanceof Optional<?> opt && opt.isPresent()) {
			return getEntityDropEntries(world, opt.get());
		}
		return drops;
	}

	public static List<EntityDropEntry> getEntityDropEntries(LevelAccessor world, ItemStack item) {
		return getEntityDropEntries(world, (Object) item);
	}

	private static void mergeEntityDrops(List<EntityDropEntry> target, List<EntityDropEntry> source) {
		for (EntityDropEntry entry : source) {
			boolean exists = false;
			for (EntityDropEntry cur : target) {
				if (cur.getStack().getItem() == entry.getStack().getItem()) {
					exists = true;
					break;
				}
			}
			if (!exists)
				target.add(entry);
		}
	}

	private static EntityType<?> getSpawnEggEntityType(ItemStack item) {
		if (item == null || item.isEmpty())
			return null;
		Item rawItem = item.getItem();
		if (!(rawItem instanceof SpawnEggItem))
			return null;
		for (java.lang.reflect.Method m : rawItem.getClass().getMethods()) {
			if (!"getType".equals(m.getName()))
				continue;
			if (!EntityType.class.isAssignableFrom(m.getReturnType()))
				continue;
			Class<?>[] params = m.getParameterTypes();
			try {
				if (params.length == 1 && ItemStack.class.isAssignableFrom(params[0])) {
					return (EntityType<?>) m.invoke(rawItem, item);
				} else if (params.length == 2 && ItemStack.class.isAssignableFrom(params[1])) {
					return (EntityType<?>) m.invoke(rawItem, (Object) null, item);
				} else if (params.length == 0) {
					return (EntityType<?>) m.invoke(rawItem);
				}
			} catch (Throwable ignored) {
			}
		}
		return null;
	}

	private static ResourceKey<LootTable> getEntityLootTableKey(EntityType<?> type) {
		try {
			java.lang.reflect.Method m = type.getClass().getMethod("getDefaultLootTable");
			Object result = m.invoke(type);
			if (result instanceof Optional<?> opt) {
				if (opt.isPresent() && opt.get() instanceof ResourceKey<?> rk) {
					return (ResourceKey<LootTable>) rk;
				}
			} else if (result instanceof ResourceKey<?> rk) {
				return (ResourceKey<LootTable>) rk;
			}
		} catch (Throwable ignored) {
		}
		return null;
	}

	private static Entity trySpawnDummyEntity(EntityType<?> type, ServerLevel level) {
		for (java.lang.reflect.Method m : type.getClass().getMethods()) {
			if (!"create".equals(m.getName()))
				continue;
			if (!Entity.class.isAssignableFrom(m.getReturnType()))
				continue;
			Class<?>[] params = m.getParameterTypes();
			try {
				if (params.length == 1 && params[0].isAssignableFrom(ServerLevel.class)) {
					Object result = m.invoke(type, level);
					if (result instanceof Entity e)
						return e;
				} else if (params.length == 2 && params[0].isAssignableFrom(ServerLevel.class) && params[1].isEnum()) {
					Object[] constants = params[1].getEnumConstants();
					Object enumConst = constants != null && constants.length > 0 ? constants[0] : null;
					Object result = m.invoke(type, level, enumConst);
					if (result instanceof Entity e)
						return e;
				}
			} catch (Throwable ignored) {
			}
		}
		return null;
	}

	private static List<EntityDropEntry> getEntityDropEntriesSingle(LevelAccessor world, ItemStack item) {
		List<EntityDropEntry> drops = new ArrayList<>();
		if (item == null || item.isEmpty() || !isSpawnEgg(item))
			return drops;
		if (world == null) {
			try {
				world = Minecraft.getInstance().level;
			} catch (Throwable ignored) {
			}
		}
		EntityType<?> entityType = getSpawnEggEntityType(item);
		if (entityType == null)
			return drops;
		ResourceKey<LootTable> lootTableKey = getEntityLootTableKey(entityType);
		if (lootTableKey == null)
			return drops;
		if (world instanceof ServerLevel serverLevel) {
			Entity dummy = null;
			try {
				dummy = trySpawnDummyEntity(entityType, serverLevel);
				if (dummy == null)
					return drops;
				LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableKey);
				LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
						.withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
						.withParameter(LootContextParams.DAMAGE_SOURCE, serverLevel.damageSources().generic())
						.withParameter(LootContextParams.THIS_ENTITY, dummy);
				for (ItemStack s : lootTable.getRandomItems(lootBuilder.create(LootContextParamSets.ENTITY))) {
					if (!s.isEmpty()) {
						boolean exists = false;
						for (EntityDropEntry cur : drops) {
							if (cur.getStack().getItem() == s.getItem()) {
								exists = true;
								break;
							}
						}
						if (!exists) {
							drops.add(new EntityDropEntry(new ItemStack(s.getItem(), Math.max(1, s.getCount())), 1, Math.max(1, s.getCount()), 1.0d));
						}
					}
				}
			} catch (Throwable ignored) {
			} finally {
				if (dummy != null) {
					try {
						dummy.discard();
					} catch (Throwable ignored) {
					}
				}
			}
		}
		return drops;
	}
}