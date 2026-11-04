package com.itmecasey.skyblockresources.loader;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

/** The block capabilities the machines use, named like NeoForge's {@code Capabilities.Item.BLOCK}. */
public final class Capabilities {
	private Capabilities() {
	}

	public static final class Item {
		public static final BlockCapability<ResourceHandler<ItemResource>> BLOCK = new BlockCapability<>(ForgeCapabilities.ITEM_HANDLER, handler -> new ItemHandlerAdapter((IItemHandler) handler));

		private Item() {
		}
	}

	public static final class Fluid {
		public static final BlockCapability<ResourceHandler<FluidResource>> BLOCK = new BlockCapability<>(ForgeCapabilities.FLUID_HANDLER, handler -> new FluidHandlerAdapter((IFluidHandler) handler));

		private Fluid() {
		}
	}

	public static final class Energy {
		public static final BlockCapability<EnergyHandler> BLOCK = new BlockCapability<>(ForgeCapabilities.ENERGY, handler -> new EnergyHandler((IEnergyStorage) handler));

		private Energy() {
		}
	}
}
