package com.itmecasey.skyblockresources.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.item.ItemEntity;
import static com.itmecasey.skyblockresources.machine.MachineHelpers.*;

public class GeneratorBlockDestroyedByExplosionProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		Vec3 center = new Vec3(x, y, z);
		for (ItemEntity itemEntity : world.getEntitiesOfClass(ItemEntity.class, new AABB(center, center).inflate(1.1 / 2d), e -> true)) {
			if (getDisplayType(itemEntity.getItem()).equals("display") && !itemEntity.level().isClientSide())
				itemEntity.discard();
		}
	}

}
