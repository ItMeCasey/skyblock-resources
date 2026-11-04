package com.itmecasey.skyblockresources.procedures;

import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import com.itmecasey.skyblockresources.init.SkyblockResourcesModItems;

import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class ScrewdriverTooltipsProcedure {
	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		execute(event.getItemStack(), event.getToolTip());
	}

	public static void execute(ItemStack itemstack, List<Component> tooltip) {
		if (tooltip == null)
			return;
		if (itemstack.getItem() == SkyblockResourcesModItems.SCREWDRIVER.get()) {
			tooltip.add(Component.literal("§a" + "§o" + "Shift + Right Click to set the output side of a Resource Generator"));
		}
	}
}
