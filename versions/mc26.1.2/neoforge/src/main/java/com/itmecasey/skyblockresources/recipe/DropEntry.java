package com.itmecasey.skyblockresources.recipe;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** One thing a tree or crop generator can yield per cycle: {@code min} to {@code max} of an item, with the given chance. */
public record DropEntry(Item item, int min, int max, double chance) {
	public static final Codec<DropEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(DropEntry::item),
			Codec.INT.optionalFieldOf("min", 1).forGetter(DropEntry::min),
			Codec.INT.optionalFieldOf("max", 0).forGetter(DropEntry::max),
			Codec.DOUBLE.optionalFieldOf("chance", 1.0d).forGetter(DropEntry::chance)).apply(instance, DropEntry::new));

	/** A missing or too-small max means "exactly min". */
	public int maxCount() {
		return Math.max(min, max);
	}

	public ItemStack roll(RandomSource random) {
		if (chance < 1.0d && random.nextDouble() >= chance)
			return ItemStack.EMPTY;
		int count = min + random.nextInt(maxCount() - min + 1);
		return count <= 0 ? ItemStack.EMPTY : new ItemStack(item, count);
	}

	public static List<ItemStack> roll(List<DropEntry> entries, RandomSource random) {
		List<ItemStack> drops = new ArrayList<>();
		for (DropEntry entry : entries) {
			ItemStack stack = entry.roll(random);
			if (!stack.isEmpty())
				drops.add(stack);
		}
		return drops;
	}
}
