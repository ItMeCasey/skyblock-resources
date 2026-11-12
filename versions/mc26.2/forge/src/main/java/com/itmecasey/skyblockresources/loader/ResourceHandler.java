package com.itmecasey.skyblockresources.loader;

/**
 * Slot-based access to items or fluids, shaped like NeoForge's ResourceHandler. The implementations in this
 * package wrap Forge's IItemHandler and IFluidHandler.
 */
public interface ResourceHandler<T extends Resource> {
	int size();

	T getResource(int index);

	long getAmountAsLong(int index);

	default int getAmountAsInt(int index) {
		return (int) Math.min(Integer.MAX_VALUE, getAmountAsLong(index));
	}

	long getCapacityAsLong(int index, T resource);

	default int getCapacityAsInt(int index, T resource) {
		return (int) Math.min(Integer.MAX_VALUE, getCapacityAsLong(index, resource));
	}

	boolean isValid(int index, T resource);

	int insert(int index, T resource, int amount, Transaction transaction);

	default int insert(T resource, int amount, Transaction transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		int inserted = 0;
		int size = size();
		for (int index = 0; index < size; index++) {
			inserted += insert(index, resource, amount - inserted, transaction);
			if (inserted == amount)
				break;
		}
		return inserted;
	}

	int extract(int index, T resource, int amount, Transaction transaction);

	default int extract(T resource, int amount, Transaction transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		int extracted = 0;
		int size = size();
		for (int index = 0; index < size; index++) {
			extracted += extract(index, resource, amount - extracted, transaction);
			if (extracted == amount)
				break;
		}
		return extracted;
	}
}
