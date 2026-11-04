package com.itmecasey.skyblockresources.loader;

import net.minecraftforge.energy.IEnergyStorage;

/** Energy access shaped like NeoForge's EnergyHandler, over a Forge {@link IEnergyStorage}. */
public final class EnergyHandler {
	private final IEnergyStorage storage;

	EnergyHandler(IEnergyStorage storage) {
		this.storage = storage;
	}

	public int insert(int amount, Transaction transaction) {
		if (amount <= 0) {
			return 0;
		}
		int inserted = storage.receiveEnergy(amount, false);
		if (inserted > 0) {
			transaction.onRollback(() -> storage.extractEnergy(inserted, false));
		}
		return inserted;
	}

	public int extract(int amount, Transaction transaction) {
		if (amount <= 0) {
			return 0;
		}
		int extracted = storage.extractEnergy(amount, false);
		if (extracted > 0) {
			transaction.onRollback(() -> storage.receiveEnergy(extracted, false));
		}
		return extracted;
	}
}
