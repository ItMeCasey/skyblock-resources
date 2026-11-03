package com.itmecasey.skyblockresources.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

import com.itmecasey.skyblockresources.network.SkyblockResourcesModVariables;

import java.util.Locale;
import static com.itmecasey.skyblockresources.machine.MachineHelpers.*;

public class GeneratorBlockDestroyedByPlayerProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		String itemUuid = SkyblockResourcesModVariables.MapVariables.get(world).GLOBAL_ITEM_UUID;
		Vec3 center = new Vec3(x + 0.5, y + 0.3, z + 0.5);
		for (ItemEntity itemEntity : world.getEntitiesOfClass(ItemEntity.class, new AABB(center, center).inflate(1 / 2d), e -> true)) {
			if (getDisplayType(itemEntity.getItem()).equals("display") && itemEntity.getStringUUID().equals(itemUuid)) {
				if (!itemEntity.level().isClientSide())
					itemEntity.discard();
			}
		}
		String resourceName = SkyblockResourcesModVariables.MapVariables.get(world).GLOBAL_RESOURCE_NAME;
		ItemStack droppedStack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(resourceName.toLowerCase(Locale.ENGLISH))));
		droppedStack.setCount((int) SkyblockResourcesModVariables.MapVariables.get(world).GLOBAL_ITEMS);
		if (world instanceof ServerLevel level) {
			ItemEntity droppedItem = new ItemEntity(level, x, y, z, droppedStack);
			droppedItem.setPickUpDelay(10);
			level.addFreshEntity(droppedItem);
		}
	}

}
