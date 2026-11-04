package com.itmecasey.skyblockresources.loader;

import java.util.function.Function;

import net.minecraftforge.common.capabilities.Capability;

/** A capability a block entity can offer, with the wrapper that turns Forge's handler into this package's handler. */
public final class BlockCapability<T> {
	private final Capability<?> forgeCapability;
	private final Function<Object, T> wrapper;

	BlockCapability(Capability<?> forgeCapability, Function<Object, T> wrapper) {
		this.forgeCapability = forgeCapability;
		this.wrapper = wrapper;
	}

	Capability<?> forgeCapability() {
		return forgeCapability;
	}

	T wrap(Object handler) {
		return wrapper.apply(handler);
	}
}
