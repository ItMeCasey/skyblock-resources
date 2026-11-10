package com.itmecasey.skyblockresources.devtest;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

/** Dev-only JEI plugin that keeps the runtime, so the tour can open JEI's recipe screen. */
@JeiPlugin
public class JeiCapture implements IModPlugin {
	static volatile IJeiRuntime runtime;

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath("skyblock_resources", "devtest_capture");
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
	}

	@Override
	public void onRuntimeUnavailable() {
		runtime = null;
	}
}
